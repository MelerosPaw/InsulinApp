package julioverne.insulinapp.data;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.SpannableString;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.volley.VolleyError;
import com.google.gson.GsonBuilder;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import julioverne.insulinapp.BuildConfig;
import julioverne.insulinapp.R;
import julioverne.insulinapp.constants.Constants;
import julioverne.insulinapp.data.dao.AlimentoDAO;
import julioverne.insulinapp.data.dao.EntradaDAO;
import julioverne.insulinapp.data.dao.PeriodoDAO;
import julioverne.insulinapp.extensions.StringExt;
import julioverne.insulinapp.ui.ResultadoCopiaSeguridad;
import julioverne.insulinapp.ui.adaptadores.ModoVisualizacion;
import julioverne.insulinapp.ui.callbacks.VolleyBitmapCallback;
import julioverne.insulinapp.ui.callbacks.VolleyImageSavedCallback;
import julioverne.insulinapp.ui.callbacks.VolleyJSONObjectResponseCallback;
import julioverne.insulinapp.ui.receiver.BackupReceiver;
import julioverne.insulinapp.ui.receiver.CalculoInsulinaReceiver;
import julioverne.insulinapp.utils.DateUtils;
import julioverne.insulinapp.utils.DecimalFormatUtils;
import julioverne.insulinapp.utils.InternalMemoryUtils;
import julioverne.insulinapp.utils.MathematicsUtils;
import julioverne.insulinapp.utils.StringUtils;
import julioverne.insulinapp.utils.VolleyUtils;
import melerospaw.memoryutil.MemoryUtil;
import melerospaw.memoryutil.Path;
import melerospaw.memoryutil.Result;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Juan José Melero on 02/06/2015.<br/>
 * Esta clase funciona como singleton (instancia única) de la base de datos, ya que siempre devuelve
 * la misma conexión.
 */
public class DataManager {

  private static final String TAG = DataManager.class.getSimpleName();

  private static final int LIMITE_ENTRADAS = 750;
  private static final int COPIA_SEGURIDAD_CODE = 100;
  private static final int CALCULO_INSULINA_CODE = 101;
  private static final int REQUEST_CODE_GET_ALIMENTOS = 102;
  private static final int REQUEST_CODE_GET_IMAGEN_ALIMENTO = 103;
  public static final String IMAGES_URL = "http://" + BuildConfig.ENDPOINT + "/Alimentos/img/";
  public static final String ALIMENTOS_URL =
      "http://" + BuildConfig.ENDPOINT + "/Alimentos/api_InsulinApp.php?action=getAlimentos";

  private static DataManager manager;

  private PendingIntent alarmIntent;
  private AlarmManager alarmManager;
  private static final Object LOCKED = new Object();
  private Context context;
  private SharedPreferencesManager sharedPreferencesManager;
  private Path databasePath;
  private Path.Builder imagesDirectoryPath;
  private BackUpManager backUpManager;

  private DataManager(Context context) {
    this.context = context;
    this.sharedPreferencesManager = new SharedPreferencesManager(context);
    this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    this.alarmIntent = PendingIntent.getBroadcast(context, COPIA_SEGURIDAD_CODE,
        new Intent(context, BackupReceiver.class),
        PendingIntent.FLAG_CANCEL_CURRENT);
    this.databasePath = new Path.Builder(context)
        .databaseDirectory(DatabaseHelper.DATABASE_NAME)
        .build();
    this.imagesDirectoryPath = new Path.Builder(context)
        .storageDirectory(Path.STORAGE_PRIVATE_INTERNAL)
        .folder(Constants.APP_DIRECTORY)
        .folder(Constants.IMAGES_DIRECTORY);
    this.backUpManager = new BackUpManager(context, databasePath, imagesDirectoryPath.duplicate());
  }

  public static DataManager getInstance(Context context) {
    if (manager == null) {
      manager = new DataManager(context.getApplicationContext());
    }

    return manager;
  }

  private DatabaseHelper getHelper() {
    return DatabaseHelper.getHelper(context);
  }

  /**
   * Devuelve un booleano indicando si el diario está vacío o no.
   *
   * @return Returns true if the diary is new.
   */
  public boolean isDiarioEmpty() {
    return getEntradas().isEmpty();
  }

  /**
   * Devuelve un objeto {@code Date} con la fecha de la última entrada.
   *
   * @return Date.
   */
  @Nullable
  public Date getFechaUltimaEntrada() {
    Boolean isDiarioEmpty = isDiarioEmpty();
    if (!isDiarioEmpty) {
      synchronized (LOCKED) {
        EntradaDAO ultimaEntrada = getUltimaEntrada();
        if (ultimaEntrada != null) {
          return ultimaEntrada.getFecha();
        } else {
          StringUtils.toastCorto(context, "No se ha podido obtener la hora de la última entrada. La entrada es nula.");
        }
      }
    }

    return null;
  }

  /**
   * Devuelve la última entrada del diario
   *
   * @return {@code EntradaDAO} última entrada almacenada. Si no hay entrada o el diario no se ha
   */
  @Nullable
  public EntradaDAO getUltimaEntrada() {
    if (!isDiarioEmpty()) {
      final List<EntradaDAO> diario = getEntradas();

      if (diario != null && !diario.isEmpty()) {
        return diario.get(0);
      }
    }

    return null;
  }

  /**
   * Devuelve la duración de la insulina en milisegundos.
   *
   * @return Devuelve la duración de la insulina, guardada en las {@code SharedPreferences}.
   */
  public long getDuracionInsulina() {
    final String horas = sharedPreferencesManager.get(SharedPreferencesManager.DURACION_HORAS);
    final String minutos = sharedPreferencesManager.get(SharedPreferencesManager.DURACION_MINUTOS);

    final long horasDuracionInsulina = Integer.parseInt(horas) * 60 * 60 * 1000L;
    final long minutosDuracionInsulina = Integer.parseInt(minutos) * 60 * 1000L;

    return horasDuracionInsulina + minutosDuracionInsulina;
  }

  public String[] getPreferenciasDosis() {
    String[] preferenciasSolicitadas = {
        SharedPreferencesManager.GLUCOSA_MAXIMA,
        SharedPreferencesManager.GLUCOSA_MINIMA,
        SharedPreferencesManager.FACTOR_SENSIBILIDAD
    };
    return sharedPreferencesManager.getPreferencias(preferenciasSolicitadas);
  }

  public String getPreferencia(String key) {
    return sharedPreferencesManager.get(key);
  }

  public void setPreference(String key, String value) {
    sharedPreferencesManager.set(key, value);
  }

  /**
   * Indica si se han configurado los parámeros mínimos para que se pueda realizar un control.
   *
   * @return Devuelve true si se han configurado las preferencias suficientes como para poder registrar
   * entradas.
   */
  public boolean arePreferencesSet() {
    return sharedPreferencesManager.arePreferencesSet();
  }

  public void getAllAlimentos(final GetAlimentosCallback callback) {

    final List<AlimentoDAO> todosAlimentos = new ArrayList<>();
    final int[] peticionesFinalizadas = { 0 };

    getAlimentos(DataSource.DATABASE, new GetAlimentosCallback() {
      @Override
      public void onSuccess(List<AlimentoDAO> alimentos) {
        peticionesFinalizadas[0]++;
        todosAlimentos.addAll(alimentos);
        if (peticionesFinalizadas[0] == 2) {
          ordenarYDevolver(callback, todosAlimentos);
        }
      }

      @Override
      public void onFailure() {
        peticionesFinalizadas[0]++;
        if (peticionesFinalizadas[0] == 2) {
          ordenarYDevolver(callback, todosAlimentos);
          Toast.makeText(context, context.getString(R.string.imposible_obtener_alimentos_db), Toast.LENGTH_SHORT)
              .show();
        }
      }
    });

    if (!isNetworkAvailable()) {
      Toast.makeText(context, R.string.sin_internet_para_alimento, Toast.LENGTH_LONG).show();
      peticionesFinalizadas[0]++;
      if (peticionesFinalizadas[0] == 2) {
        ordenarYDevolver(callback, todosAlimentos);
      }
    } else {
      getAlimentos(DataSource.SERVER, new GetAlimentosCallback() {
        @Override
        public void onSuccess(List<AlimentoDAO> alimentos) {
          peticionesFinalizadas[0]++;
          todosAlimentos.addAll(alimentos);
          if (peticionesFinalizadas[0] == 2) {
            ordenarYDevolver(callback, todosAlimentos);
          }
        }

        @Override
        public void onFailure() {
          peticionesFinalizadas[0]++;
          if (peticionesFinalizadas[0] == 2) {
            ordenarYDevolver(callback, todosAlimentos);
            Toast.makeText(context, R.string.imposible_obtener_alimentos_server, Toast.LENGTH_SHORT).show();
          }
        }
      });
    }
  }

  private void ordenarYDevolver(GetAlimentosCallback callback, List<AlimentoDAO> todosAlimentos) {
    Collections.sort(todosAlimentos, AlimentoDAO.COMPARATOR);
    callback.onSuccess(todosAlimentos);
  }

  /**
   * Devuelve todos los alimentos de la base de datos o del servicio en el callback.
   */
  public void getAlimentos(DataSource source, final GetAlimentosCallback callback) {
    if (source == DataSource.DATABASE) {
      getAlimentosFromDatabase(callback);
    } else if (source == DataSource.SERVER) {
      getAlimentosFromServer(callback);
    }
  }

  private void getAlimentosFromDatabase(GetAlimentosCallback callback) {
    try {
      List<AlimentoDAO> alimentos = getHelper().getAlimentoDao().queryForAll();
      ordenarAlimentos(alimentos);
      callback.onSuccess(alimentos);
    } catch (SQLException e) {
      StringUtils.toastCorto(context, "Ha fallado la carga de alimentos.");
      Log.e(TAG, "getAlimentos: " + e.getMessage(), e);
      callback.onFailure();
    }
  }

  private void getAlimentosFromServer(final GetAlimentosCallback callback) {
    VolleyUtils.makeRequest(context, ALIMENTOS_URL, REQUEST_CODE_GET_ALIMENTOS,
        new VolleyJSONObjectResponseCallback() {
          @Override
          public void onSuccess(JSONObject jsonObject) {
            List<AlimentoDAO> alimentos = procesarJSONAlimentos(jsonObject);
            ordenarAlimentos(alimentos);
            callback.onSuccess(alimentos);
          }

          @Override
          public void onFailure(VolleyError volleyError) {
            Log.e(TAG, "getAlimentos: " + volleyError.getMessage(), volleyError);
            callback.onFailure();
          }
        });
  }

  private List<AlimentoDAO> procesarJSONAlimentos(JSONObject jsonObject) {

    List<AlimentoDAO> alimentos;

    if (!jsonObject.optBoolean("correcto")) {
      Toast.makeText(context, "No hay ningun alimeno que obtener.", Toast.LENGTH_SHORT).show();
      alimentos = Collections.emptyList();
    } else {
      JSONArray jsonArray = jsonObject.optJSONArray("datos");
      alimentos = new LinkedList<>();
      for (int i = 0; i < jsonArray.length(); i++) {
        alimentos.add(new GsonBuilder().create().fromJson(jsonArray.opt(i).toString(), AlimentoDAO.class));
      }
    }

    return alimentos;
  }

  private void ordenarAlimentos(List<AlimentoDAO> alimentos) {
    // Ordena la lista alfabéticamente
    if (alimentos != null && !alimentos.isEmpty()) {
      Collections.sort(alimentos, AlimentoDAO.COMPARATOR);
    }
  }

  public void cancelarPeticionAlimentos() {
    VolleyUtils.cancelQueue(context, REQUEST_CODE_GET_ALIMENTOS);
  }

  public void cargarImagenAlimento(String nombreImagen, VolleyBitmapCallback callback) {
    String url = IMAGES_URL + nombreImagen;
    VolleyUtils.imageRequest(context, url, REQUEST_CODE_GET_IMAGEN_ALIMENTO, callback);
  }

  /**
   * Devuelve una lista con los nombres de los alimentos existentes en la base de datos
   * InsulinAppAlimentos.sqlite.
   */
  public void getNombresAlimentos(final GetNombresAlimentosCallback callback) {
    final List<String> nombresAlimentos = new ArrayList<>();
    getAlimentos(DataSource.DATABASE, new GetAlimentosCallback() {
      @Override
      public void onSuccess(List<AlimentoDAO> alimentos) {
        if (alimentos != null) {
          for (AlimentoDAO a : alimentos) {
            nombresAlimentos.add(a.getNombre());
          }
        }
        callback.onSuccess(nombresAlimentos);
      }

      @Override
      public void onFailure() {
        callback.onFailure();
      }
    });
  }

  /**
   * Devuelve un alimento de la base de datos a partir de su nombre
   *
   * @param nombre Nombre del alimento que se quiere recuperar.
   * @return Devuelve el {@code AlimentoDAO} cuyo nombre sea {@code nombre} o nulo si no existe el
   * alimento.
   */
  @Nullable
  public AlimentoDAO getAlimento(String nombre) {
    try {
      return getHelper().getAlimentoDao().queryForEq("nombre", nombre).get(0);
    } catch (SQLException e) {
      StringUtils.toastCorto(context, R.string.advertencia_alimento_no_encontrado);
      return null;
    }
  }

  /**
   * Guarda una entrada y devuelve un booleano indicando si se ha guardado o no. Si ya hay más
   * entradas de las permitidas, borra la más antigua.
   */
  public boolean guardarEntrada(final EntradaDAO entrada) {

    ConnectionSource connectionSource = getHelper().getConnectionSource();
    boolean guardada;

    try {
      guardada = TransactionManager.callInTransaction(connectionSource, new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          int insertedRows = getHelper().getEntradaDao().create(entrada);

          if (insertedRows == 1) {
            borrarMasAntiguo();
            return true;
          }

          return false;
        }
      });
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      return false;
    }

    return guardada;
  }

  /**
   * Obtiene una entrada mediante la fecha.
   *
   * @param fecha Objeto {@code Date} para hacer la comparación.
   * @return Devuelve una {@link EntradaDAO} o nulo si no hay entrada para dicha fecha.
   */
  @Nullable
  public EntradaDAO getEntrada(Date fecha) {
    try {
      return getHelper().getEntradaDao().queryForEq("fecha", fecha).get(0);
    } catch (SQLException e) {
      return null;
    }
  }

  /**
   * Recupera todas las entradas o devuelve una lista vacía. Las devuelve en orden inverso al de
   * inserción.
   *
   * @return Devuelve una lista de EntradasDAO ordanadas por la fecha de manera descendente.
   */
  @NonNull
  public List<EntradaDAO> getEntradas() {
    List<EntradaDAO> entradas = new ArrayList<>();

    try {
      entradas = getHelper().getEntradaDao().queryForAll();
    } catch (SQLException e) {
      StringUtils.toastCorto(context, "No se han podido obtener las entradas del diario.");
    }

    Collections.sort(entradas, EntradaDAO.COMPARATOR);
    return entradas;
  }

  /**
   * Indica si al usuario le queda insulina en la sangre tomando la hora a la que se suministró
   * la última dosis y, en caso de que no haya transcurrido el tiempo de duración de la insulina
   * desde entonces, significa que aún queda insulina.
   *
   * @return Devuelve un boolean indicando si aún tiene o ya no le queda insulina en la sangre.
   */
  public boolean hayInsulina() {
    long tiempoActual = DateUtils.fechaActualEnMilisegundos();
    if (!isDiarioEmpty()) {
      EntradaDAO ultimaEntrada = getUltimaEntrada();
      if (ultimaEntrada.getInsulinaDosis() + ultimaEntrada.getUnidadesSangre() != 0) {
        Long tiempoUltimaEntrada = DateUtils.fechaEnMilisegundos(ultimaEntrada.getFecha());
          if (tiempoActual - tiempoUltimaEntrada < getDuracionInsulina()) {
              return true;
          }
      }
    }

    return false;
  }

  /**
   * Calcula cuánta insulina queda desde que se suministró la dosis según el tiempo transcurrido
   * y la cantidad de la última entrada.
   *
   * @return Devuelve la cantidad de insulina si hay una última entrada o -1 si no hay ninguna
   * entrada.
   */
  public float calcularInsulinaRestante() {
    final EntradaDAO ultimaEntrada = getUltimaEntrada();

    if (ultimaEntrada != null) {
      long tiempoUltimaEntrada = DateUtils.fechaEnMilisegundos(ultimaEntrada.getFecha());
      long tiempoActual = DateUtils.fechaActualEnMilisegundos();
      float insulinaMomentoUltimaEntrada = ultimaEntrada.getInsulinaDosis() + ultimaEntrada.getUnidadesSangre();
      long tiempoTranscurrido = tiempoActual - tiempoUltimaEntrada;
      long tiempoRestante = getDuracionInsulina() - tiempoTranscurrido;
      double insulinaPreviaDouble =
          MathematicsUtils.reglaDeTres(getDuracionInsulina(), insulinaMomentoUltimaEntrada, tiempoRestante);
      float insulinaRestante = Float.parseFloat(DecimalFormatUtils.decimalToString(insulinaPreviaDouble, 2, ".", "."));
      return insulinaRestante < 0 ? 0F : insulinaRestante;
    } else {
      return -1f;
    }
  }

  /**
   * Elimina una lista de alimentos que se le pase y devuelve la cantidad de alimentos eliminados.
   *
   * @param alimentos Lista de {@code AlimentoDAO} que se quieren eliminar.
   * @return Devuelve la cantidad de alimentos eliminados.
   */
  public int eliminarAlimentos(final List<AlimentoDAO> alimentos) {

    ConnectionSource connectionSource = getHelper().getConnectionSource();

    int totalEliminados;

    try {
      totalEliminados = TransactionManager.callInTransaction(connectionSource, new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {

          int eliminados = 0;

          for (AlimentoDAO alimento : alimentos) {
            if (!eliminarAlimento(alimento)) {
              throw new Exception("No se ha podido eliminar el alimento " + alimento.getNombre());
            } else {
              eliminados++;
            }
          }

          return eliminados;
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }

    return totalEliminados;
  }

  public boolean eliminarAlimento(AlimentoDAO alimento) {
    return getHelper().eliminarAlimento(alimento);
  }

  public boolean crearAlimento(AlimentoDAO alimento) {
    return getHelper().crearAlimento(alimento);
  }

  /**
   * Devuelve todos los periodos definidos por el usuario. Si no hay periodos.
   *
   * @return Lista de {@code PeriodoDAO}. Si no hay periodos, devuelve una lista vacía.
   */
  public List<PeriodoDAO> getPeriodos() {
    List<PeriodoDAO> periodos = new ArrayList<>();

    try {
      periodos = obtenerPeriodos();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return periodos;
  }

  private List<PeriodoDAO> obtenerPeriodos() throws SQLException {
    return getHelper().obtenerPeriodos();
  }

  /**
   * Devuelve el periodo en el que se encuentre una hora en concreto.
   *
   * @param tiempo Hora y minutos cuyo periodo queremos obtener
   * @return {@code PeriodoDAO} que comience en la hora y minutos indicados o que comience antes
   * que estos y acabe después. Si no hay periodos o no hay un periodo para esa hora y minutos en
   * concreto, devuelve un periodo con una cantidad de insulina de 1, que se corresponde con la
   * cantidad de insulina estándar para cada periodo.
   */
  public PeriodoDAO getPeriodo(int[] tiempo) {
    List<PeriodoDAO> periodos = getPeriodos();
    if (periodos.isEmpty()) {
      PeriodoDAO p = new PeriodoDAO();
      p.setUnidadesInsulina(1f);
      return p;
    } else {
      for (PeriodoDAO p : periodos) {
        if (p.getHoraInicio() == tiempo[0]
            || (tiempo[0] > p.getHoraInicio() && tiempo[0] < p.getHoraFin())) {
          if (p.getMinutosInicio() == tiempo[1]
              || (tiempo[1] > p.getMinutosInicio() && tiempo[1] < p.getMinutosFin())) {
            return p;
          }
        }
      }

      PeriodoDAO p = new PeriodoDAO();
      p.setUnidadesInsulina(1f);
      return p;
    }
  }

  /**
   * Guarda un periodo en la base de datos.
   *
   * @param periodo Colección con los periodos que se quieren guardar.
   * @return En caso de fallo, devuelve false. Si no, devuelve true si la cantidad de periodos
   * guardados es la misma que la cantidad de periodos recibidos.
   */
  public boolean guardarPeriodo(PeriodoDAO periodo) {
    int guardado;

    boolean isRepetido = !isPeriodoValido(periodo);

    if (isRepetido) {
      return false;
    }

    try {
      guardado = getHelper().getPeriodoDao().create(periodo);
    } catch (SQLException e) {
      StringUtils.toastLargo(context, R.string.advertencia_periodo_no_guardado);
      return false;
    }

    return guardado == 1;
  }

  /**
   * Comprueba que un periodo no se encuentre ya registrado, cotejando las horas que comprende
   * con las de los demás periodos existentes. Un periodo no será válido cuando sus horas se
   * pisen con otro.
   *
   * @param periodo El periodo que queremos comparar con los ya guardados
   * @return Devuelve <i>true</i> si el periodo es valido.
   */
  private boolean isPeriodoValido(PeriodoDAO periodo) {

    boolean isPeriodoValido = true;

    List<PeriodoDAO> periodos = getPeriodos();

    //Respecto a si mismo, no debe acabar antes de que empiece y acabar antes
    if (periodo.getHoraInicio().equals(periodo.getHoraFin())) {
      StringUtils.toastCorto(context, R.string.advertencia_misma_hora);
      isPeriodoValido = false;
    } else {
      for (PeriodoDAO p : periodos) {
        //Directamente no es válido si comienza a la misma hora que otro
        if (periodo.getHoraInicio().equals(p.getHoraInicio())) {
          isPeriodoValido = false;
          //Primero el caso de que el periodo comience antes de las 0:00 y acabe a las 0:00 o después
        } else if (periodo.getHoraInicio() > periodo.getHoraFin()) {
          if (p.getHoraInicio() > p.getHoraFin()) {
            isPeriodoValido = false;
          } else if (periodo.getHoraInicio() < p.getHoraInicio()) {
            isPeriodoValido = false;
          } else if (periodo.getHoraInicio() < p.getHoraFin()) {
            isPeriodoValido = false;
          } else if (periodo.getHoraFin().equals(p.getHoraInicio()) && periodo.getMinutosFin() > p.getMinutosInicio()) {
            isPeriodoValido = false;
          } else if (periodo.getHoraInicio().equals(p.getHoraFin()) && periodo.getMinutosInicio() < p.getMinutosFin()) {
            isPeriodoValido = false;
          }
          //Luego el caso de que estemos comparando con un periodo comienza antes de las 0:00 y acabe a las 0:00 o después
        } else if (p.getHoraInicio() > p.getHoraFin()) {
          if (periodo.getHoraInicio() > p.getHoraInicio()) {
            isPeriodoValido = false;
          } else if (periodo.getHoraInicio() < p.getHoraFin()) {
            isPeriodoValido = false;
          } else if (periodo.getHoraFin().equals(p.getHoraInicio()) && periodo.getMinutosFin() > p.getMinutosInicio()) {
            isPeriodoValido = false;
          } else if (periodo.getHoraInicio() < p.getHoraInicio() && periodo.getHoraFin() > p.getHoraInicio()) {
            isPeriodoValido = false;
          } else if (periodo.getHoraInicio() < p.getHoraInicio() && periodo.getHoraFin() < p.getHoraFin()) {
            isPeriodoValido = false;
          } else if (periodo.getHoraInicio() < p.getHoraInicio()
              && periodo.getHoraFin().equals(p.getHoraFin())
              && periodo.getMinutosFin() < p.getMinutosFin()) {
            isPeriodoValido = false;
          } else if (periodo.getHoraInicio().equals(p.getHoraFin()) && periodo.getMinutosInicio() < p.getMinutosFin()) {
            isPeriodoValido = false;
          }
          //Por último el caso en que ninguna de los dos periodos comience antes de las 0:00 y acabe después
        } else {
          //No es válido si comienza a una hora comprendida entre el inicio y el fin de otro periodo
          if (periodo.getHoraInicio() > p.getHoraInicio() && periodo.getHoraInicio() < p.getHoraFin()) {
            isPeriodoValido = false;
            //No es válido si acaba una hora comprendida entre el inicio y el fin de otro periodo
          } else if (periodo.getHoraFin() > p.getHoraInicio() && periodo.getHoraFin() < p.getHoraFin()) {
            isPeriodoValido = false;
          } else if (periodo.getHoraFin().equals(p.getHoraInicio()) && periodo.getMinutosFin() > p.getMinutosInicio()) {
            isPeriodoValido = false;
            //No es válido si comienza a la hora que acaba otro periodo pero no después de los minutos en que acaba
          } else if (periodo.getHoraInicio().equals(p.getHoraFin()) && periodo.getMinutosInicio() < p.getMinutosFin()) {
            isPeriodoValido = false;
            //No es válido si empieza antes de otro periodo pero acaba después de que empiece ese otro periodo
          } else if (periodo.getHoraInicio() < p.getHoraInicio() && periodo.getHoraFin() > p.getHoraInicio()) {
            isPeriodoValido = false;
            //No es válido si empieza antes que otro periodo pero acaba a la misma hora pero minutos después
          } else if (periodo.getHoraInicio() < p.getHoraInicio()
              && periodo.getHoraFin().equals(p.getHoraInicio())
              && periodo.getMinutosFin() > p.getMinutosInicio()) {
            isPeriodoValido = false;
          }
        }
      }

      if (!isPeriodoValido) {
        StringUtils.toastCorto(context, R.string.advertencia_misma_hora);
      }
    }

    return isPeriodoValido;
  }

  /**
   * Borra los periodos existentes en la base de datos y los sustuye por los nuevos. Si se produce
   * un fallo, permanecen los periodos anteriores.
   *
   * @param periodos Colección con los periodos que se quieren guardar
   * @return En caso de fallo, devuelve false. Si no, devuelve true si la cantidad de periodos
   * guardados es la misma que la cantidad de periodos recibidos.
   */
  public boolean guardarPeriodos(final List<PeriodoDAO> periodos) {

    ConnectionSource connectionSource = getHelper().getConnectionSource();
    boolean guardados;

    try {
      guardados = TransactionManager.callInTransaction(connectionSource, new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          for (PeriodoDAO p : periodos) {
            getHelper().getPeriodoDao().create(p);
          }

          return getPeriodos().size() == periodos.size();
        }
      });
    } catch (Exception e) {
      return false;
    }

    return guardados;
  }

  /**
   * Recibe un {@code JSONArray} con los alimentos recibidos y los guarda en la memoria interna.
   */
  public void actualizarAlimentos() {
    //        Intent intent = new Intent(context, ActualizarAlimentosService.class);
    //        intent.putExtra("ALIMENTOS", alimentos.toString());
    //        context.startService(intent);

    //Ejecuta la consulta al servidor
    getAlimentos(DataSource.SERVER, new GetAlimentosCallback() {
      @Override
      public void onSuccess(List<AlimentoDAO> alimentos) {
        combinarConAlimentosEnDB(alimentos);
      }

      @Override
      public void onFailure() {
        Toast.makeText(context, "No se ha podido acceder al servidor para obtener los alimentos.", Toast.LENGTH_LONG)
            .show();
      }
    });
  }

  public void combinarConAlimentosEnDB(final List<AlimentoDAO> alimentosServidor) {
    //Obtiene la lista de alimentos actuales y crea una lista para meter aquellos que no estén repetidos
    getAlimentos(DataSource.DATABASE, new GetAlimentosCallback() {
      @Override
      public void onSuccess(List<AlimentoDAO> alimentosDB) {
        try {
          guardarAlimentos(descartarAlimentosRepetidos(alimentosServidor, alimentosDB));
        } catch (SQLException e) {
          Toast.makeText(context, "No se han podido guardar los alimentos nuevos", Toast.LENGTH_SHORT).show();
          Log.e(TAG, "SQLError al guardar los alimentos nuevos.", e);
        }
      }

      @Override
      public void onFailure() {
        Toast.makeText(context,
            "No se han podido obtener los alimentos de la base de datos para combinarlos con los obtenidos del servidor",
            Toast.LENGTH_LONG).show();
      }
    });
  }

  private List<AlimentoDAO> descartarAlimentosRepetidos(List<AlimentoDAO> alimentosServidor,
      List<AlimentoDAO> alimentosDB) {
    List<AlimentoDAO> alimentosNoRepetidos = new LinkedList<>();
    for (AlimentoDAO alimentoServidor : alimentosServidor) {
      boolean repetido = false;
      for (AlimentoDAO alimentoDB : alimentosDB) {
        if (alimentoServidor.isFromHosting() == alimentoDB.isFromHosting()
            && alimentoServidor.getNombre().equals(alimentoDB.getNombre())) {
          repetido = true;
          break;
        }
      }

      if (!repetido) {
        alimentosNoRepetidos.add(alimentoServidor);
      }
    }

    return alimentosNoRepetidos;
  }

  /**
   * Guarda un alimento en la base de datos asegurándose de obtener la imagen del servidor.
   *
   * @param alimento Alimento que se quiere guardar.
   * @param callback <i>Callback</i> que informará solo en caso de que la imagen no se haya podido guardar.
   */
  public void guardarAlimento(final AlimentoDAO alimento, final VolleyImageSavedCallback callback) {
    String url = IMAGES_URL + alimento.getImagen();
    VolleyUtils.imageRequest(context, url, REQUEST_CODE_GET_IMAGEN_ALIMENTO, new VolleyBitmapCallback() {
      @Override
      public void onSuccess(Bitmap bitmap) {
        boolean imagenGuardada = InternalMemoryUtils.guardarImagen(bitmap,
            alimento.getImagen(), getImagesDirectory());
        if (imagenGuardada) {
          try {
            getHelper().getAlimentoDao().create(alimento);
          } catch (SQLException e) {
            Log.e(TAG, "No se ha guardado el alimento " + alimento.getNombre());
            eliminarImagen(alimento.getImagen());
            callback.onFailure();
          }
        }
      }

      @Override
      public void onFailure(VolleyError volleyError) {
        Log.e(TAG, "VolleryError: " + volleyError.getMessage());
        callback.onFailure();
      }
    });
  }

  /**
   * Borra el último alimento en caso de que haya más entradas que el máximo permitido.
   */
  private void borrarMasAntiguo() throws SQLException {

    if (getHelper().getEntradaDao().queryForAll().size() > LIMITE_ENTRADAS) {
      QueryBuilder<EntradaDAO, Integer> qb = getHelper().getEntradaDao().queryBuilder();
      qb.orderBy("fecha", true);

      List<EntradaDAO> entradas = getHelper().getEntradaDao().query(qb.prepare());
      EntradaDAO entradaBorrar = entradas.get(0);

      //Borra la entrada
      int borrada = getHelper().getEntradaDao().delete(entradaBorrar);
      Log.e(TAG, "Borradas " + borrada);
    }
  }

  /**
   * Guarda en la base de datos del dispositivo un alimento creado por el usuario. Si no se puede
   * guardar la imagen, no se guarda tampoco el alimento.
   *
   * @param alimento El {@code AlimentoDAO} que queremos guardar. El atributo {@code hosting} tiene
   * que ser {@code false} porque es creado por el usuario.
   * @param bitmap Imagen del alimento que el usuario haya tomado con la cámara.
   * @return Devuelve {@code true} si se ha guardado el alimento.
   */
  public boolean guardarAlimento(final AlimentoDAO alimento, final Bitmap bitmap) {

    ConnectionSource connectionSoure = getHelper().getConnectionSource();
    boolean guardado;

    try {
      guardado = TransactionManager.callInTransaction(connectionSoure, new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          getHelper().crearAlimento(alimento);
          boolean imagenGuardada = InternalMemoryUtils.guardarImagen(bitmap,
              alimento.getImagen(), getImagesDirectory());
          if (!imagenGuardada) {
            throw new Exception("No se ha guardado la imagen.");
          }
          return true;
        }
      });
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      return false;
    }

    return guardado;
  }

  /**
   * Elimina un el {@code PeriodoDAO} recibido.
   *
   * @param periodo {@code PeriodoDAO} que queremos eliminar.
   * @return Devuelve {@code true} si se ha eliminado el periodo.
   */
  public boolean eliminarPeriodo(PeriodoDAO periodo) {
    int eliminado;

    try {
      eliminado = getHelper().getPeriodoDao().delete(periodo);
    } catch (SQLException e) {
      return false;
    }

    return eliminado == 1;
  }

  public boolean cambiarNombreImagen(String nombreAntiguo, String nombreNuevo) {
    boolean nombreCambiado;
    Path archivoAntiguo = getImagesDirectory().file(nombreAntiguo).build();
    Path archivoNuevo = getImagesDirectory().file(nombreNuevo).build();
    Result<File> resultadoCambio = MemoryUtil.duplicateFile(archivoAntiguo, archivoNuevo);
    if (resultadoCambio.isSuccessful()) {
      nombreCambiado = eliminarImagen(nombreAntiguo);
      if (!nombreCambiado) {
        Log.e(TAG, "No se ha podido eliminar el archivo anterior");
      }
    } else {
      Log.e(TAG, "No se ha podido duplicar el archivo con otro nombre");
      nombreCambiado = false;
    }
    return nombreCambiado;
  }

  public boolean isNombreAlimentoRepetido(String nombre) {
    return getHelper().isNombreAlimentoRepetido(nombre);
  }

  public boolean isNombreImagenRepetido(String nombre) {
    return getHelper().isNombreImagenRepetido(nombre);
  }

  public boolean isArchivoRepetido(String nombreArchivo) {
    return InternalMemoryUtils.isArchivoRepetido(context, nombreArchivo);
  }

  /**
   * Sobrescribe un alimento de la base de datos.
   *
   * @param alimentoAntiguo Alimento que se quiere actualizar.
   * @param alimentoNuevo Alimento redefinido por el que se sustituirá el {@code alimentoAntiguo}.
   * @param bitmap Imagen del alimento.
   */
  public boolean sobrescribirAlimento(final AlimentoDAO alimentoAntiguo,
      final AlimentoDAO alimentoNuevo, final Bitmap bitmap) {
    ConnectionSource connectionSource = getHelper().getConnectionSource();
    boolean sobrescrito;

    try {
      sobrescrito = TransactionManager.callInTransaction(connectionSource, new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          boolean imagenEliminada = eliminarImagen(alimentoAntiguo.getImagen());
          if (!imagenEliminada) {
            throw new Exception("No se ha podido eliminar la imagen anterior.");
          } else {
            boolean imagenGuardada = InternalMemoryUtils.guardarImagen(bitmap,
                alimentoNuevo.getImagen(), getImagesDirectory());
            if (!imagenGuardada) {
              throw new Exception("No se ha podido guardar la imagen.");
            } else {
              if (!alimentoAntiguo.equals(alimentoNuevo)) {
                boolean eliminado = eliminarAlimento(alimentoAntiguo);
                boolean insertado = crearAlimento(alimentoNuevo);
                return eliminado && insertado;
              } else {
                return true;
              }
            }
          }
        }
      });
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      return false;
    }

    return sobrescrito;
  }

  public boolean guardarEntradas(List<EntradaDAO> entradas) {

    int entradasGuardadas = 0;

    for (EntradaDAO entrada : entradas) {
      boolean guardada = guardarEntrada(entrada);
      if (guardada) {
        entradasGuardadas++;
      }
    }

    if (entradasGuardadas < entradas.size()) {
      Log.e(TAG, "No se han guardado todas las entradas.");
    }

    return entradasGuardadas == entradas.size();
  }

  public boolean isImagenesCargadas() {
    return sharedPreferencesManager.getBoolean(SharedPreferencesManager.IMAGENES_CARGADAS);
  }

  // La primera vez que se inicia la aplicación se guardan las imágenes de assets en el directorio files
  public void cargarImagenes() {

    //Mete las imagenes en files
    String[] nombreImagenes = {
        "1.gif",
        "2.gif",
        "3.gif",
        "4.gif"
    };
    int cargadas = 0;
    boolean cargada;

    for (int i = 0; i < nombreImagenes.length; i++) {
      cargada = InternalMemoryUtils.guardarImagen(
          InternalMemoryUtils.obtenerImagenDesdeAssets(context, nombreImagenes[i]),
          nombreImagenes[i], getImagesDirectory());
        if (cargada) {
            cargadas++;
        }
    }

    Toast.makeText(context, "Se han cargado " + cargadas + " de " + nombreImagenes.length
        + " imagenes.", Toast.LENGTH_SHORT).show();

      if (cargadas == 4) {
          sharedPreferencesManager.setImagenesCargadas(true);
      } else {
          sharedPreferencesManager.setImagenesCargadas(false);
      }
  }

  public void guardarAlimento(AlimentoDAO a) throws SQLException {
    getHelper().getAlimentoDao().create(a);
  }

  public void guardarAlimentos(List<AlimentoDAO> alimentos) throws SQLException {
    for (AlimentoDAO alimento : alimentos) {
      guardarAlimento(alimento);
    }
  }

  /**
   * Cancela el pendingIntent para alarmManager que hace que se haga una copia de seguridad.
   */
  private void cancelarAlarmaCopiaSeguridad() {
    alarmManager.cancel(alarmIntent);
  }

  /**
   * Establece una alarma para que a la hora dada, se haga una copia de seguridad y luego se
   * repite cada día.
   */
  public void establecerCopiaSeguridadAutomatica() {
    if (isCopiaSeguridadActiva()) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(System.currentTimeMillis());
      calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(getPreferencia(SharedPreferencesManager.BACKUP_HORAS)));
      calendar.set(Calendar.MINUTE, Integer.parseInt(getPreferencia(SharedPreferencesManager.BACKUP_MINUTOS)));
      //        alarmManager.setRepeating(AlarmManager.RTC, 10000, 60000, alarmIntent);
      alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
          AlarmManager.INTERVAL_DAY, alarmIntent);
    } else {
      cancelarAlarmaCopiaSeguridad();
    }
  }

  /**
   * Crea y lanza un PendingIntent que hará que se actualice la cantidad de insulina cada
   * 15 minutos.
   */
  public void establecerRepeticionCalculoInsulina() {
    Intent intent = new Intent(context, CalculoInsulinaReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, CALCULO_INSULINA_CODE,
        intent, PendingIntent.FLAG_CANCEL_CURRENT);
    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
        AlarmManager.INTERVAL_FIFTEEN_MINUTES, Constants.MS_ACTUALIZACION_INSULINA,
        pendingIntent);
  }

  /**
   * Cancela el pendingIntent para alarmManager que hace que se se solicite
   * la cantidad de insulina transcurrido un minuto.
   */
  public void cancelarRepeticionCalculoInsulina() {
    alarmManager.cancel(PendingIntent.getBroadcast(context, CALCULO_INSULINA_CODE,
        new Intent(context, CalculoInsulinaReceiver.class), 0));
  }

  /**
   * Comprueba si ya se ha mostrado por primera vez el mensaje de restaurarCopiaSeguridad la base de datos o
   * no.
   */
  public boolean seHaOfrecidoRestaurar() {
    return sharedPreferencesManager.getBoolean(SharedPreferencesManager.MENSAJE_RESTAURAR_MOSTRADO);
  }

  /**
   * Estable si ya no es necesario mostrar el mensaje de restaurarCopiaSeguridad la base de datos al iniciar
   * la aplicación o no.
   *
   * @param shown Si es true, no se volverá a mostrar el mensaje al inicio de la aplicación
   */
  public void setRestoringMessageShown(boolean shown) {
    sharedPreferencesManager.set(SharedPreferencesManager.MENSAJE_RESTAURAR_MOSTRADO, shown);
  }

  /**
   * Establece unas preferences predeterminadas para no tener que andar entrando en la
   * configuración cada vez que se inicie la aplicación.
   */
  public void loadDefaultPreferences() {
    sharedPreferencesManager.setDefaultPreferences();
  }

  /**
   * Devuelve true si en las preferencias se ha activado la realización copia de seguridad.
   *
   * @return True si la realización de la copia de seguridad está activada.
   */
  public boolean isCopiaSeguridadActiva() {
    return sharedPreferencesManager.isCopiaSeguridadActiva();
  }

  /**
   * Des/activa la preferencia que habilita la copia de seguridad.
   *
   * @param activar Activa/desactiva la copia de seguridad con true/false.
   */
  public void activarCopiaSeguridad(boolean activar) {
    sharedPreferencesManager.set(SharedPreferencesManager.BACKUP_ENABLED, activar);
  }

  /**
   * Inicia un {@link julioverne.insulinapp.ui.dialogs.AnywhereDialog} para indicar desde dónde
   * se quiere obtener la imagen.
   *
   * @param callback El listener para recibir el tipo de origen de la imagen.
   * @param originView La imagen utilizada para crear el {@code AnywhereDialog} de selección
   * de imagen.
   */
  public void seleccionarOrigenImagen(@NonNull final View originView, final GetOrigenImagenCallback callback) {
    final PopupMenu popup = new PopupMenu(originView.getContext(), originView);
    popup.getMenuInflater().inflate(R.menu.menu_origen_imagen, popup.getMenu());
    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.item_archivo) {
          callback.onOrigenSelected(OrigenImagen.DESDE_ARCHIVO);
          return true;
        } else if (item.getItemId() == R.id.item_camara) {
          callback.onOrigenSelected(OrigenImagen.DESDE_CAMARA);
          return true;
        }
        return false;
      }
    });
    popup.show();
  }

  /**
   * Devuelve true si se recibe uno de los requestCode definidos para el intent de la cámara o
   * de la búsqueda de imagen en archivos correctamente. Si el intent se lanza desde la activity,
   * en los Fragment llegará un código que no coincidirá y viceversa, por lo que esta condición
   * solo será true cuando la procese la vista que lanzó el Intent.
   *
   * @param requestCode El request code recibido.
   * @return True si el requestCode coincide con uno de los correctos.
   */
  public boolean isRequestCodeForThisView(int requestCode) {
    return requestCode == Constants.FILE_SEARCH_INTENT_REQUEST_CODE ||
        requestCode == Constants.CAMERA_INTENT_REQUEST_CODE;
  }

  /**
   * Obtiene el Bitmap que viene en un Intent según el código requestCode recibido.
   *
   * @param context Contexto para obtener un contentResolver.
   * @param requestCode RequestCode recibido para saber cómo tiene que extraer el bitmap.
   * @param data El intent que contiene el bitmap obtenido.
   * @return Devolverá el bitmap que viene en el intent o null si el intent es nulo, si el
   * requestCode no corresponde a ninguno de los establecidos o si no puede obtener el bitmap
   * del intent.
   */
  public Bitmap getBitmapFromResult(Context context, int requestCode, Intent data) {

    Bitmap bitmap;

    if (data != null) {
      switch (requestCode) {
        case Constants.CAMERA_INTENT_REQUEST_CODE:
          bitmap = (Bitmap) data.getExtras().get("data");
          if (bitmap == null) {
            Log.e(TAG, context.getString(R.string.log_no_picture_received_from_camera));
          }
          break;
        case Constants.FILE_SEARCH_INTENT_REQUEST_CODE:
          bitmap = getBitmapFromUri(data.getData());
          break;
        default:
          bitmap = null;
      }
    } else {
      bitmap = null;
    }

    return bitmap;
  }

  private Bitmap getBitmapFromUri(Uri uri) {
    return MemoryUtil.loadBitmap(context, uri).getResult();
  }

  @Nullable
  public Bitmap getBitmapFromMemory(@NonNull final String imagen) {
    return InternalMemoryUtils.obtenerImagenBitmap(imagen, getImagesDirectory());
  }

  /**
   * Obtiene la imagen de un alimento escalada.
   *
   * @param alimentoDAO Alimento del cual queremos obtener la imagen.
   * @return El bitmap de la imagen desde el dispositivo o null si el alimento es nulo.
   */
  @Nullable
  public Bitmap obtenerImagenAlimento(AlimentoDAO alimentoDAO) {

    Bitmap imagen;

    if (alimentoDAO != null) {

      imagen = InternalMemoryUtils.obtenerImagenBitmap(alimentoDAO.getImagen(),
          getImagesDirectory());

      //            if (imagen != null) {
      //                imagen = InternalMemoryUtils.prepararBitmap(imagen);
      //            }
    } else {
      imagen = null;
    }

    return imagen;
  }

  public Path.Builder getImagesDirectory() {
    return this.imagesDirectoryPath.duplicate();
  }

  public boolean hayCopiaSeguridad() {
    return backUpManager.hayCopiaSeguridad();
  }

  public SharedPreferences getSharedPreferences() {
    return sharedPreferencesManager.getSharedPreferences();
  }

  public void restaurarCopiaSeguridad(AlertDialog.Builder builder, BackUpManager.RestoreListener callback) {
    backUpManager.restaurarCopiaSeguridad(builder, getSharedPreferences(), callback);
  }

  public ResultadoCopiaSeguridad realizarCopiaSeguridad() {
    return backUpManager.realizarCopiaSeguridad(getSharedPreferences());
  }

  public Result eliminarCopia() {
    return backUpManager.eliminarCopia();
  }

  public SpannableString getDescripcionCopiaSeguridad() {
    return backUpManager.getDescripcionCopiaSeguridad();
  }

  public String getHoraCopiaSeguridad() {
    return sharedPreferencesManager.getHoraCopiaSeguridad();
  }

  public boolean eliminarImagen(String nombreArchivo) {
    return InternalMemoryUtils.eliminarImagen(nombreArchivo, getImagesDirectory());
  }

  public String getInformacionCopiaSeguridad(int tipo) {
    return backUpManager.getResumenCopiaSeguridad(tipo);
  }

  public boolean isHoraCopiaSeguridadEstablecida() {
    return sharedPreferencesManager.isHoraCopiaSeguridadSet();
  }

  public String normalizarNombre(@NonNull final String nombreAlimento) {
    return StringExt.normalize(nombreAlimento);
  }

  public String procesarNombre(String nombreImagenSinExtension) {
    return nombreImagenSinExtension.concat(".gif");
  }

  //Comprueba que estén activados los servicios de red: WiFi o tráfico de datos
  public boolean isNetworkAvailable() {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null;
  }

  public String getUniqueName(Context context, String nombreAlimento) {
    return InternalMemoryUtils.getUniqueName(context, nombreAlimento);
  }

  public ModoVisualizacion getModoVisualizacion() {
    return sharedPreferencesManager.getModoVisualizacion();
  }

  public void cambioModoVisualizacionAlimentos(@NonNull ModoVisualizacion nuevoModo) {
    sharedPreferencesManager.cambioModoVisualizacion(nuevoModo);
  }

  public interface GetAlimentosCallback {

    void onSuccess(List<AlimentoDAO> alimentos);

    void onFailure();
  }

  public interface GetNombresAlimentosCallback {

    void onSuccess(@NonNull List<String> nombres);

    void onFailure();
  }

  public interface GetAlimentoRepetidoCallback {

    void onResult(boolean isRepetido);
  }

  public interface GetOrigenImagenCallback {

    void onOrigenSelected(OrigenImagen origen);
  }

  public enum DataSource {
    DATABASE,
    SERVER
  }

  public enum OrigenImagen {
    DESDE_CAMARA,
    DESDE_ARCHIVO
  }
}