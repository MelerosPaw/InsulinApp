package julioverne.insulinapp.ui.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.DataManager;
import julioverne.insulinapp.data.dao.PeriodoDAO;
import julioverne.insulinapp.ui.adaptadores.AdaptadorPeriodos;
import julioverne.insulinapp.ui.dialogs.DialogDefinirPeriodo;
import julioverne.insulinapp.utils.ComparaPeriodos;
import julioverne.insulinapp.utils.StringUtils;
import julioverne.insulinapp.utils.TimeUtils;
import julioverne.insulinapp.utils.TypefacesUtils;

/**
 * Created by Juan José Melero on 25/04/2015.
 */
public class PeriodosActivity extends AppCompatActivity {

  @BindView(R.id.tv_titulo_periodos)
  TextView tvTituloPeriodos;
  @BindView(R.id.tv_texto_superior)
  TextView tvTextoSuperior;
  @BindView(R.id.lv_periodos)
  ListView lvPeriodos;
  //    @BindView(R.id.tv_desde)            TextView tvDesde;
  //    @BindView(R.id.et_hora_inicio)      EditText etHoraInicio;
  //    @BindView(R.id.et_minutos_inicio)   EditText etMinutosInicio;
  //    @BindView(R.id.tv_hasta)            TextView tvHasta;
  //    @BindView(R.id.et_hora_fin)         EditText etHoraFin;
  //    @BindView(R.id.et_minutos_fin)      EditText etMinutosFin;
  //    @BindView(R.id.tv_unidades)         TextView tvUnidades;
  //    @BindView(R.id.et_unidades)         EditText etUnidades;
  @BindView(R.id.btn_terminar)
  Button btnTerminar;
  @BindView(R.id.btn_nuevo_periodo)
  Button btnNuevoPeriodo;

  //    @BindViews({R.id.et_hora_inicio, R.id.et_minutos_inicio, R.id.et_hora_fin, R.id.et_minutos_fin,
  //            R.id.et_unidades})
  //    List<EditText> vistasReseteables;

  private DataManager dataManager;
  private List<PeriodoDAO> periodos = new ArrayList<>();
  private AdaptadorPeriodos adaptador;
  private Unbinder unbinder;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.configuracion_periodos_layout);
    unbinder = ButterKnife.bind(this);
    dataManager = DataManager.getInstance(this);

    cargarTipografia();

    periodos = dataManager.getPeriodos();
    periodos = ordenarPeriodos(periodos);
    adaptador = new AdaptadorPeriodos(this, R.layout.item_periodo, periodos);
    lvPeriodos.setAdapter(adaptador);
  }

  private void cargarTipografia() {
    final Typeface light = TypefacesUtils.get(this, "fonts/DejaVuSans-ExtraLight.ttf");
    final Typeface negrita = TypefacesUtils.get(this, "fonts/DejaVuSansCondensed-Bold.ttf");
    final Typeface cursiva = TypefacesUtils.get(this, "fonts/DejaVuSansCondensed-Oblique.ttf");

    //Tipografia
    tvTituloPeriodos.setTypeface(negrita);
    tvTextoSuperior.setTypeface(cursiva);
    //        tvDesde.setTypeface(negrita);
    //        tvHasta.setTypeface(negrita);
    //        etHoraInicio.setTypeface(light);
    //        etMinutosInicio.setTypeface(light);
    //        etHoraFin.setTypeface(light);
    //        etMinutosFin.setTypeface(light);
    //        etUnidades.setTypeface(light);
    //        tvUnidades.setTypeface(negrita);
    btnTerminar.setTypeface(negrita);
    btnNuevoPeriodo.setTypeface(negrita);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unbinder.unbind();
  }

  //Hace las comprobaciones necesarias con las horas y las devuelve si son correctas
  public int[] procesarTiempo(String[] horas) {

    //Elimina los espacios en blanco si los hubiera
    horas = StringUtils.eliminarEspacios(horas);

    //Comprueba que las horas estén entre las 0:00 y las 23:00
    int[] horasInicio = TimeUtils.convertirHora(horas[0] + ":" + horas[1]);
    int[] horasFinal = TimeUtils.convertirHora(horas[2] + ":" + horas[3]);

    //RETURN CASE: Las horas no están en formato de hora
      if (horasInicio == null || horasFinal == null) {
          return null;
      }

    return new int[] {
        horasInicio[0],
        horasInicio[1],
        horasFinal[0],
        horasFinal[1]
    };
  }

  //Ordena los periodos de menor a mayor según las horas
  public List<PeriodoDAO> ordenarPeriodos(List<PeriodoDAO> periodos) {

    Collections.sort(periodos, new ComparaPeriodos());
    return periodos;
  }

  //    Comprueba exhaustivamente que las horas y la cantidad de unidades sean correctas y luego guarda
  //    un nuevo periodo
  //    @OnClick (R.id.btn_nuevo_periodo)
  //    public void nuevoPeriodo(){
  //        String horaInicio = etHoraInicio.getText().toString();
  //        String horaFin = etHoraFin.getText().toString();
  //        String minutosInicio = etMinutosInicio.getText().toString();
  //        String minutosFin = etMinutosFin.getText().toString();
  //        String unidades = etUnidades.getText().toString();
  //
  //        RETURN CASE: No se ha escrito nada
  //        if (StringUtils.isCadenaVacia(new String[]{horaInicio, minutosInicio, horaFin, minutosFin, unidades})){
  //            StringUtils.toastCorto(this, R.string.advertencia_faltan_datos);
  //            return;
  //        }
  //
  //        Comprueba el tiempo
  //        int[] horas = procesarTiempo(new String[]{horaInicio, minutosInicio, horaFin, minutosFin});
  //        if (horas == null) {
  //            StringUtils.toastCorto(this, R.string.advertencia_horas_incorrectas);
  //            return;
  //        }
  //
  //        Comprueba la cantidad de unidades
  //        unidades = unidades.replaceAll(" ", "");
  //        unidades = unidades.replaceAll(",", ".");
  //        String resultado = StringUtils.convertible(unidades);
  //
  //        RETURN CASE: Si la cantidad no es un número, se muestra un mensaje
  //        if (!resultado.equals("integer") && !resultado.equals("float")){
  //            StringUtils.toastCorto(this, R.string.advertencia_unidades_incorrectas);
  //            return;
  //        }
  //
  //        float unidadesRacion;
  //
  //        if (resultado.equals("integer"))
  //            unidadesRacion = (float) Integer.parseInt(unidades);
  //        else {
  //            unidadesRacion = Float.parseFloat(unidades);
  //        }
  //
  //        Guarda el alimento y si no se guarda, indica que ha habido un error
  //        PeriodoDAO p = new PeriodoDAO(horas[0], horas[1], horas[2], horas[3], unidadesRacion);
  //        boolean guardado = dataManager.guardarPeriodo(p);
  //        if (guardado) {
  //            adaptador.add(p);
  //            ButterKnife.apply(vistasReseteables, ButterKnifeUtils.SET_TEXT, "");
  //            TecladoUtil.ocultarTeclado(this);
  //        }
  //    }
  //

  @OnClick(R.id.btn_nuevo_periodo)
  public void nuevoPeriodo() {
    final DialogDefinirPeriodo dialogo = new DialogDefinirPeriodo();
    dialogo.show(getSupportFragmentManager(), DialogDefinirPeriodo.TAG);
  }

  //Guarda los periodos si hay alguno
  @OnClick(R.id.btn_terminar)
  public void terminar() {
    if (adaptador.getCount() == 0) {
      StringUtils.toastCorto(this, getString(R.string.advertencia_sin_periodos));
    }
    onBackPressed();
  }
}