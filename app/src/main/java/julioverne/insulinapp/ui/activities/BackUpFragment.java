package julioverne.insulinapp.ui.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import julioverne.insulinapp.R;
import julioverne.insulinapp.constants.Constants;
import julioverne.insulinapp.data.BackUpManager;
import julioverne.insulinapp.databinding.FragmentBackUpLayoutBinding;
import julioverne.insulinapp.extensions.FragmentExtensions;
import julioverne.insulinapp.ui.ResultadoCopiaSeguridad;
import julioverne.insulinapp.ui.adaptadores.AdaptadorOpcionesCopiaSeguridad;
import julioverne.insulinapp.ui.base.BaseFragment;
import julioverne.insulinapp.utils.AnimationUtil;
import julioverne.insulinapp.utils.MessageExtensions;
import julioverne.insulinapp.utils.StringUtils;
import melerospaw.memoryutil.Result;

/**
 * Created by Juan José Melero on 09/05/2015.
 */
public class BackUpFragment extends BaseFragment {

  private FragmentBackUpLayoutBinding binding;
  private AdaptadorOpcionesCopiaSeguridad adaptadorOpciones;
  private Button botonPulsado;
  private final AdapterView.OnItemClickListener backUpItemClickListener =
      (AdapterView<?> adapterView, View view, int position, long l) ->
          escogerOpcion(position);
  private final View.OnClickListener tableButtonsOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      final int tipo = getType(view);
      seleccionarBoton(tipo);
      mostrarInformacionCopiaSeguridad(tipo);
      setInfoContainerHeight();
    }

    private int getType(@NonNull final View view) {
      final int tipo;
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
      return tipo;
    }
  };

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState
  ) {
    binding = FragmentBackUpLayoutBinding.inflate(inflater, container, false);
    baseBinding = binding;
    return binding.getRoot();
  }

  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    loadView(view.getContext());
  }

  private void loadView(@NonNull final Context context) {
    final String[] opciones = getResources().getStringArray(R.array.opciones_copia_seguridad);
    adaptadorOpciones = new AdaptadorOpcionesCopiaSeguridad(context,
        R.layout.item_opcion_simple_layout, R.id.tv_opcion, opciones, dataManager.hayCopiaSeguridad());
    binding.lvOpcionesCopiaSeguridad.setAdapter(adaptadorOpciones);
    binding.setBackUpOptionListener(backUpItemClickListener);
    binding.setOnTableClickedListener(tableButtonsOnClickListener);

    actualizarInformacionCopia();
  }

  private void actualizarInformacionCopia() {

    if (!dataManager.hayCopiaSeguridad()) {
      binding.llInfoDetallesContainer.setVisibility(View.GONE);
      if (dataManager.isCopiaSeguridadActiva()) {
        binding.tvDescripcionCopia.setText(String.format(getString(R.string.sin_copia_activado),
            dataManager.getHoraCopiaSeguridad()));
      } else {
        binding.tvDescripcionCopia.setText(R.string.sin_copia_no_activado);
      }

      setInfoContainerHeight();
    } else {
      binding.llInfoDetallesContainer.setVisibility(View.VISIBLE);
      binding.tvDescripcionCopia.setText(dataManager.getDescripcionCopiaSeguridad());

      if (botonPulsado != null) {
        tableButtonsOnClickListener.onClick(botonPulsado);
      } else {
        tableButtonsOnClickListener.onClick(binding.tvBotonAlimentos);
      }
    }
  }

  public void escogerOpcion(final int position) {

    final String opcion = (String) binding.lvOpcionesCopiaSeguridad.getItemAtPosition(position);

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

  public void mostrarInformacionCopiaSeguridad(int tipo) {
    mostrarInformacionAnterior(tipo);
    binding.llInfoDetallesContainer.setVisibility(View.VISIBLE);
  }

  private void seleccionarBoton(int tipo) {

    Button nuevoBoton;

    switch (tipo) {
      case Constants.INFO_ALIMENTOS:
        nuevoBoton = binding.tvBotonAlimentos;
        break;
      case Constants.INFO_ENTRADAS:
        nuevoBoton = binding.tvBotonEntradas;
        break;
      case Constants.INFO_PERIODOS:
        nuevoBoton = binding.tvBotonPeriodos;
        break;
      default:
        nuevoBoton = binding.tvBotonAjustes;
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
    binding.tvContenidoAnterior.setText(informacion);
  }

  private void setInfoContainerHeight() {

    final View container;

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      //            container = binding.llInfoCopiaContainer;
      return;
    } else {
      container = binding.cvInfoCopiaContainer;
      final Rect screen = new Rect();
      getActivity().getWindow().getWindowManager().getDefaultDisplay().getRectSize(screen);

      // Obtiene la posición de la esquina superior izquierda del ScrollView
      final int[] locationOnScreen = new int[2];
      container.getLocationOnScreen(locationOnScreen);

      // Mide el tamaño que tiene el contenedor. Aunque cambie el contenido, el tamaño no cambia
      // hasta que no pasa por la animación. Si el tamaño fuera WRAP_CONTENT, cambiaría solo, pero
      // como es MATCH_PARENT, no cambia y tampoco lo haría si fuera un tamaño fijo; no cambia
      // aunque lo haga su contenido. Entonces lo medimos con 0, 0, lo cual parece que equivale
      // a WRAP_CONTENT. Esto nos dará el tamaño que tendría la vista si estuviera a wrap_content.
      container.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

      // Con esto obtenemos el tamaño medido.
      final int height = container.getMeasuredHeight();

      // Si el alto más la posición de la vista es mayor que el tamaño de la pantalla, es que parte
      // de la vista se está pintando fuera, por lo que hay que ponerlo a MATCH_PARENT.
      final int mode = height + locationOnScreen[1] > screen.height()
          ? AnimationUtil.MATCH_PARENT
          : AnimationUtil.WRAP_CONTENT;

      // Calcula la cantidad de líneas que tendrá el mensaje de cuando no hay base de datos.
      // Cuando se realiza la medición de la altura de container, si contiene TextViews,
      // considera que tienen tantas líneas como saltos de línea hayas puesto, pero no contará
      // los saltos de línea automáticos introducidos por ser el texto  demasiado largo, por lo
      // que la altura se mide mal. Por lo tanto tenemos que averiguar cuántas líneas tiene
      // realmente con el método getLineCount(), pero por desgracia este solo funciona
      // correctamente si se le llama desde dentro de otro hilo.
      binding.tvDescripcionCopia.post(new Runnable() {
        @Override
        public void run() {
          binding.tvDescripcionCopia.setLines(binding.tvDescripcionCopia.getLineCount());
          AnimationUtil.resizeHeight(container, mode,
              container.getVisibility() == View.VISIBLE && container.getHeight() != 0);
        }
      });
    }
  }

  public void hacerCopiaSeguridad() {
    final ResultadoCopiaSeguridad resultado = dataManager.realizarCopiaSeguridad();
    if (!resultado.isRealizada()) {
      MessageExtensions.showAlert(getContext(), resultado.getMensaje(), getString(R.string.de_acuerdo));
    } else {
      adaptadorOpciones.setHayCopia(dataManager.hayCopiaSeguridad());
      adaptadorOpciones.notifyDataSetChanged();
      actualizarInformacionCopia();
      FragmentExtensions.toast(this, resultado.getMensaje());
    }
  }

  public void restaurarCopiaSeguridad() {
    dataManager.restaurarCopiaSeguridad(new AlertDialog.Builder(getContext()), new BackUpManager.RestoreListener() {
      @Override
      public void isRestoring(ResultadoCopiaSeguridad resultado) {
        if (!resultado.isRealizada()) {
          MessageExtensions.showAlert(getContext(), resultado.getMensaje(), getString(R.string.de_acuerdo));
        } else {
          FragmentExtensions.toast(BackUpFragment.this, resultado.getMensaje());
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
      MessageExtensions.showAlert(getContext(), resultado.getMessage(), getString(R.string.de_acuerdo));
    } else {
      adaptadorOpciones.setHayCopia(dataManager.hayCopiaSeguridad());
      adaptadorOpciones.notifyDataSetChanged();
      actualizarInformacionCopia();
    }
  }
}