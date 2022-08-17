package julioverne.insulinapp.ui.activities;

import android.app.AlertDialog;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import julioverne.insulinapp.R;
import julioverne.insulinapp.constants.Constants;
import julioverne.insulinapp.data.BackUpManager;
import julioverne.insulinapp.ui.ResultadoCopiaSeguridad;
import julioverne.insulinapp.ui.adaptadores.AdaptadorOpcionesCopiaSeguridad;
import julioverne.insulinapp.utils.AnimationUtil;
import julioverne.insulinapp.utils.MessageExtensions;
import julioverne.insulinapp.utils.StringUtils;
import melerospaw.memoryutil.Result;

/**
 * Created by Juan José Melero on 09/05/2015.
 */
public class CopiaSeguridadActivity extends BaseActivity {

  @BindView(R.id.lv_opciones_copia_seguridad)
  ListView lvOpcionesCopiaSeguridad;
  @BindView(R.id.tv_cabecera_descripcion_copia)
  TextView tvCabeceraDescripcionCopia;
  @BindView(R.id.cv_info_copia_container)
  @Nullable
  CardView cvInfoCopiaContainer;
  @BindView(R.id.ll_info_copia_container)
  @Nullable
  LinearLayout llInfoCopiaContainer;
  @BindView(R.id.tv_descripcion_copia)
  TextView tvDescripcionCopia;
  @BindView(R.id.sv_scroll_view)
  ScrollView svScrollView;
  @BindView(R.id.ll_info_detalles_container)
  LinearLayout llInfoDetallesContainer;
  @BindView(R.id.tv_contenido_anterior)
  TextView tvContenidoAnterior;
  @BindView(R.id.tv_boton_alimentos)
  Button tvBotonAlimentos;
  @BindView(R.id.tv_boton_entradas)
  Button tvBotonEntradas;
  @BindView(R.id.tv_boton_periodos)
  Button tvBotonPeriodos;
  @BindView(R.id.tv_boton_ajustes)
  Button tvBotonAjustes;

  private AdaptadorOpcionesCopiaSeguridad adaptadorOpciones;
  private Button botonPulsado;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.copia_seguridad_layout);
    ButterKnife.bind(this);
    loadView();
  }

  @Override
  public void loadView() {
    //        cargarTipografiaCabecera();

    String[] opciones = getResources().getStringArray(R.array.opciones_copia_seguridad);
    adaptadorOpciones = new AdaptadorOpcionesCopiaSeguridad(this,
        R.layout.item_opcion_simple_layout, R.id.tv_opcion, opciones, dataManager.hayCopiaSeguridad());
    lvOpcionesCopiaSeguridad.setAdapter(adaptadorOpciones);

    tvCabeceraDescripcionCopia.setTypeface(negrita);
    tvDescripcionCopia.setTypeface(light);
    tvContenidoAnterior.setTypeface(light);
    tvBotonAjustes.setTypeface(light);
    tvBotonPeriodos.setTypeface(light);
    tvBotonEntradas.setTypeface(light);
    tvBotonAlimentos.setTypeface(light);

    actualizarInformacionCopia();
  }

  private void actualizarInformacionCopia() {

    if (!dataManager.hayCopiaSeguridad()) {
      llInfoDetallesContainer.setVisibility(View.GONE);
      if (dataManager.isCopiaSeguridadActiva()) {
        tvDescripcionCopia.setText(String.format(getString(R.string.sin_copia_activado),
            dataManager.getHoraCopiaSeguridad()));
      } else {
        tvDescripcionCopia.setText(R.string.sin_copia_no_activado);
      }

      setInfoContainerHeight();
    } else {
      llInfoDetallesContainer.setVisibility(View.VISIBLE);
      tvDescripcionCopia.setText(dataManager.getDescripcionCopiaSeguridad());
      if (botonPulsado != null) {
        onClick(botonPulsado);
      } else {
        onClick(tvBotonAlimentos);
      }
    }
  }

  public void mostrarInformacionCopiaSeguridad(int tipo) {
    mostrarInformacionAnterior(tipo);
    llInfoDetallesContainer.setVisibility(View.VISIBLE);
  }

  private void seleccionarBoton(int tipo) {

    Button nuevoBoton;

    switch (tipo) {
      case Constants.INFO_ALIMENTOS:
        nuevoBoton = tvBotonAlimentos;
        break;
      case Constants.INFO_ENTRADAS:
        nuevoBoton = tvBotonEntradas;
        break;
      case Constants.INFO_PERIODOS:
        nuevoBoton = tvBotonPeriodos;
        break;
      default:
        nuevoBoton = tvBotonAjustes;
        break;
    }

    if (botonPulsado != null) {
      botonPulsado.setSelected(false);
    }

    botonPulsado = nuevoBoton;
    botonPulsado.setSelected(true);
  }

  private void mostrarInformacionAnterior(int tipo) {
    String informacion = StringUtils.getEmptyIfNull(dataManager.getInformacionCopiaSeguridad(tipo));
    tvContenidoAnterior.setText(informacion);
  }

  private void setInfoContainerHeight() {

    final View container;

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      //            container = llInfoCopiaContainer;
      return;
    } else {
      container = cvInfoCopiaContainer;
    }

    // Obtiene las medidas de la pantalla.
    Rect screen = new Rect();
    getWindow().getWindowManager().getDefaultDisplay().getRectSize(screen);

    // Obtiene la posición de la esquina superior izquierda del ScrollView
    int[] locationOnScreen = new int[2];
    container.getLocationOnScreen(locationOnScreen);

    // Mide el tamaño que tiene el contenedor. Aunque cambie el contendo, el tamaño no cambia
    // hasta que no pasa por la animación. Si el tamaño fuera WRAP_CONTENT, cambiaría solo, pero
    // como es MATCH_PARENT, no cambia y tampoco lo haría si fuera un tamaño fijo; no cambia
    // aunque lo haga su contenido. Entonces lo medimos con 0, 0, lo cual parece que equivale
    // a WRAP_CONTENT. Esto nos dará el tamaño que tendría la vista si estuviera a wrap_content.
    container.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    // Con esto obtenemos el tamaño medido.
    int height = container.getMeasuredHeight();

    // Si el alto más la posición de la vista es mayor que el tamaño de la pantalla, es que parte
    // de la vista se está pintando fuera, por lo que hay que ponerlo a MATCH_PARENT.
    final int mode;
    if (height + locationOnScreen[1] > screen.height()) {
      mode = AnimationUtil.MATCH_PARENT;
    } else {
      mode = AnimationUtil.WRAP_CONTENT;
    }

    // Calcula la cantidad de líneas que tendrá el mensaje de cuando no hay base de datos.
    // Cuando se realiza la medición de la altura de container, si contiene TextViews,
    // considera que tienen tantas líneas como saltos de línea hayas puesto, pero no contará
    // los saltos de línea automáticos introducidos por ser el texto  demasiado largo, por lo
    // que la altura se mide mal. Por lo tanto tenemos que averiguar cuántas líneas tiene
    // realmente con el método getLineCount(), pero por desgracia este solo funciona
    // correctamente si se le llama desde dentro de otro hilo.
    tvDescripcionCopia.post(new Runnable() {
      @Override
      public void run() {
        tvDescripcionCopia.setLines(tvDescripcionCopia.getLineCount());
        AnimationUtil.resizeHeight(container, mode,
            container.getVisibility() == View.VISIBLE && container.getHeight() != 0);
      }
    });
  }

  @OnItemClick(R.id.lv_opciones_copia_seguridad)
  public void escogerOpcion(int position) {

    String opcion = (String) lvOpcionesCopiaSeguridad.getItemAtPosition(position);

    switch (opcion) {
      case Constants.GUARDAR_COPIA:
        hacerCopiaSeguridad();
        break;
      case Constants.RESTAURAR_COPIA:
        if (adaptadorOpciones.isItemEnabled(position) && dataManager.hayCopiaSeguridad()) {
          restaurarCopiaSeguridad();
        }
        break;
      case Constants.ELIMINAR_COPIA:
        if (adaptadorOpciones.isItemEnabled(position)) {
          eliminarCopia();
        }
        break;
    }
  }

  @OnClick({
      R.id.tv_boton_alimentos,
      R.id.tv_boton_entradas,
      R.id.tv_boton_periodos,
      R.id.tv_boton_ajustes
  })
  public void onClick(View view) {

    int tipo;
    switch (view.getId()) {
      case R.id.tv_boton_alimentos:
        tipo = Constants.INFO_ALIMENTOS;
        break;
      case R.id.tv_boton_entradas:
        tipo = Constants.INFO_ENTRADAS;
        break;
      case R.id.tv_boton_periodos:
        tipo = Constants.INFO_PERIODOS;
        break;
      case R.id.tv_boton_ajustes:
      default:
        tipo = Constants.INFO_AJUSTES;
    }

    seleccionarBoton(tipo);
    mostrarInformacionCopiaSeguridad(tipo);
    setInfoContainerHeight();
  }

  public void hacerCopiaSeguridad() {
    final ResultadoCopiaSeguridad resultado = dataManager.realizarCopiaSeguridad();
    if (!resultado.isRealizada()) {
      MessageExtensions.showAlert(this, resultado.getMensaje(), getString(R.string.de_acuerdo));
    } else {
      adaptadorOpciones.setHayCopia(dataManager.hayCopiaSeguridad());
      adaptadorOpciones.notifyDataSetChanged();
      actualizarInformacionCopia();
      Toast.makeText(this, resultado.getMensaje(), Toast.LENGTH_SHORT).show();
    }
  }

  public void restaurarCopiaSeguridad() {
    dataManager.restaurarCopiaSeguridad(new AlertDialog.Builder(this), new BackUpManager.RestoreListener() {
      @Override
      public void isRestoring(ResultadoCopiaSeguridad resultado) {
        if (!resultado.isRealizada()) {
          MessageExtensions.showAlert(CopiaSeguridadActivity.this, resultado.getMensaje(),
              getString(R.string.de_acuerdo));
        } else {
          Toast.makeText(CopiaSeguridadActivity.this, resultado.getMensaje(), Toast.LENGTH_SHORT).show();
        }
      }

      @Override
      public void isNotRestoring() {
        // No hay que hacer nada si no se quiere restaurar.
      }
    });
  }

  public void eliminarCopia() {
    Result resultado = dataManager.eliminarCopia();
    if (!resultado.isSuccessful()) {
      MessageExtensions.showAlert(this, resultado.getMessage(), getString(R.string.de_acuerdo));
    } else {
      adaptadorOpciones.setHayCopia(dataManager.hayCopiaSeguridad());
      adaptadorOpciones.notifyDataSetChanged();
      actualizarInformacionCopia();
    }
  }
}