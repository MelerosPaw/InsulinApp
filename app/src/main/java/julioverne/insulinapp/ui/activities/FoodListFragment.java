package julioverne.insulinapp.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Collections;
import java.util.List;
import julioverne.insulinapp.R;
import julioverne.insulinapp.bo.EditFoodBO;
import julioverne.insulinapp.constants.Constants;
import julioverne.insulinapp.data.Resource;
import julioverne.insulinapp.data.Status;
import julioverne.insulinapp.data.dao.AlimentoDAO;
import julioverne.insulinapp.databinding.FragmentFoodLayoutBinding;
import julioverne.insulinapp.databinding.ToolbarBinding;
import julioverne.insulinapp.extensions.EditTextEX;
import julioverne.insulinapp.services.ActualizarAlimentosService;
import julioverne.insulinapp.ui.adaptadores.AdaptadorAlimentos;
import julioverne.insulinapp.ui.adaptadores.ModoVisualizacion;
import julioverne.insulinapp.ui.base.NavigationFragment;
import julioverne.insulinapp.ui.callbacks.CardViewResizeAnimationManager;
import julioverne.insulinapp.ui.dialogs.DialogEditarAlimento;
import julioverne.insulinapp.ui.listeners.OnDeleteFoodListener;
import julioverne.insulinapp.ui.listeners.OnSelectFoodListener;
import julioverne.insulinapp.ui.listeners.SearchListener;
import julioverne.insulinapp.ui.viewmodels.AlimentosViewModel;
import julioverne.insulinapp.utils.InternalMemoryUtils;
import julioverne.insulinapp.utils.ViewExtensions;
import julioverne.insulinapp.widgets.ScrollControlLayoutManager;

/**
 * Created by Juan José Melero on 09/05/2015.
 */
public class FoodListFragment extends NavigationFragment
    implements DialogEditarAlimento.OnFoodSavedListener {

  public static final String TAG = FoodListFragment.class.getSimpleName();
  public static final String KEY_SELECT_FOOD = "KEY_SELECT_FOOD";
  public static final String PARAM_SELECTED_FOOD = "PARAM_SELECTED_FOOD";
  // TODO Melero 27/06/2022: Este se ha cambiado por el de arriba
  public static final String BUNDLE_FOR_RESULT = "BUNDLE_FOR_RESULT";
  public static final String BUNDLE_INCLUIDOS = "BUNDLE_INCLUIDOS";

  private AlimentosViewModel viewModel;
  private FragmentFoodLayoutBinding binding;
  private ToolbarBinding toolbarBinding;
  private Menu menu;
  private boolean isForResult;
  private List<AlimentoDAO> alimentosYaIncluidos;
  private CardViewResizeAnimationManager resizeManager;
  @NonNull
  private final Observer<Resource<List<AlimentoDAO>>> foodListObserver = new Observer<Resource<List<AlimentoDAO>>>() {

    @Override
    public void onChanged(@Nullable Resource<List<AlimentoDAO>> resource) {
      if (resource.getStatus() == Status.SUCCESS) {
        onFoodListChanged(resource);
      } else if (resource.getStatus() == Status.ERROR) {
        onFoodListError();
      }

      showLoading(resource.getStatus() == Status.LOADING);
    }

    private void onFoodListChanged(Resource<List<AlimentoDAO>> resource) {
      final List<AlimentoDAO> alimentos = resource.getData();

      if (alimentos != null) {
        setUpFoodList(alimentos);
      } else {
        FoodListFragment.this.toast(R.string.advertencia_recargar_alimentos,
            Toast.LENGTH_LONG);
        // AlimentosFragment.super.onBackPressed();
        showLoading(false);
      }
    }

    private void onFoodListError() {
      FoodListFragment.this.toast(R.string.problema_al_obtener_alimentos);
      showLoading(false);
      // AlimentosFragment.super.onBackPressed();
    }
  };
  @NonNull
  private final Observer<Resource<List<AlimentoDAO>>> deleteFoodObserver = new Observer<Resource<List<AlimentoDAO>>>() {
    @Override
    public void onChanged(@Nullable Resource<List<AlimentoDAO>> resource) {
      if (resource != null) {
        if (resource.isSuccessful()) {
          final List<AlimentoDAO> deleted = resource.getData();
          final int size = deleted.size();
          binding.getFoodAdapter().eliminarBorrados(deleted);
          final String deletedCountMessage = getResources().getQuantityString(
              R.plurals.dialog_eliminado, size, size
          );
          toast(deletedCountMessage);
        } else if (resource.getStatus() == Status.ERROR && resource.getError() != null) {
          toast(resource.getError());
        }
      }
    }
  };
  @NonNull
  private final Observer<Resource<EditFoodBO>> onImageRetrievedObserver = resource -> {
    if (resource != null && resource.isSuccessful()) {
      final EditFoodBO data = resource.getData();
      mostrarDialogEditar(data.getFood(), data.getFoodImage(), data.getPosition());
    }
  };

  // Registra un receiver para recibir la respuesta del servicio de actualización de listaAlimentos. Si
  // llegan listaAlimentos, muestra un mensaje y recarga el adaptador, si no, solo muestra un mensaje.
  private final BroadcastReceiver receiverActualizacionAlimentos = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getBooleanExtra(ActualizarAlimentosService.RECARGAR, false)) {
        viewModel.requestFood(true);
      } else {
        final String message = intent.getStringExtra(ActualizarAlimentosService.MENSAJE);

        if (message != null) {
          toast(message);
        }
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState
  ) {
    toolbarBinding = ToolbarBinding.inflate(inflater);
    toolbarBinding.setToolbarTitle(getString(R.string.texto_titulo_seccion_alimentos));

    binding = FragmentFoodLayoutBinding.inflate(inflater, container, false);
    baseBinding = binding;

    viewModel = new ViewModelProvider(this).get(AlimentosViewModel.class);
    viewModel.getFoodListLiveData().observe(getViewLifecycleOwner(), foodListObserver);
    viewModel.getFoodDeleteLiveData().observe(getViewLifecycleOwner(), deleteFoodObserver);

    abrirParams();

    if (isForResult) {
      binding.setOnSelectFoodListener(new OnSelectFoodListener(this));
    }

    loadView();
    return binding.getRoot();
  }

  @Override
  public void onResume() {
    super.onResume();
    requireContext().registerReceiver(receiverActualizacionAlimentos,
        new IntentFilter(Constants.ACTION_ACTUALIZACION_ALIMENTOS));
  }

  @Override
  public void onPause() {
    super.onPause();
    requireContext().unregisterReceiver(receiverActualizacionAlimentos);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    this.menu = menu;
    inflater.inflate(R.menu.menu_alimentos, menu);
    cambioOpcionVisualizacion(null, dataManager.getModoVisualizacion());
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.actualizar:
        viewModel.requestFood(true);
        return true;
      case R.id.modo_lista:
        cambioModoVisualizacion(item, ModoVisualizacion.LISTA);
        return true;
      case R.id.modo_tarjeta:
        cambioModoVisualizacion(item, ModoVisualizacion.TARJETAS);
        return true;
      case android.R.id.home:
        onHomeButtonPressed();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  protected void loadView() {
    binding.etNombreAlimento.setTypeface(light);
    viewModel.requestFood(false);
  }

  //region OnFoodSavedListener
  @Override
  public void onSaved(AlimentoDAO alimento, int posicion) {
    final AdaptadorAlimentos adaptadorAlimentos = binding.getFoodAdapter();

    if (adaptadorAlimentos != null) {
      adaptadorAlimentos.nuevoAlimento(alimento, posicion);

      if (posicion == RecyclerView.NO_POSITION) {
        final String message = String.format(getString(R.string.alimento_anadido),
            alimento.getNombre());
        toast(message);
      }
    }
  }

  @Override
  public void onFailed(AlimentoDAO alimento) {
    Toast.makeText(getContext(), "No se ha podido guardar el alimento", Toast.LENGTH_SHORT).show();
  }
  //endregion

  private void abrirParams() {
    final boolean hasArguments = getArguments() != null;
    isForResult = hasArguments && getArguments().getBoolean(BUNDLE_FOR_RESULT, false);
    alimentosYaIncluidos = hasArguments
        ? getArguments().getParcelableArrayList(BUNDLE_INCLUIDOS)
        : Collections.emptyList();
  }

  private void cambioModoVisualizacion(
      @Nullable final MenuItem item,
      @NonNull final ModoVisualizacion nuevoModo
  ) {
    final AdaptadorAlimentos adaptadorAlimentos = binding.getFoodAdapter();

    if (adaptadorAlimentos != null) {
      dataManager.cambioModoVisualizacionAlimentos(nuevoModo);
      adaptadorAlimentos.cambioModo(nuevoModo);
      cambioOpcionVisualizacion(item, nuevoModo);
    }
  }

  private void cambioOpcionVisualizacion(
      @Nullable final MenuItem item,
      @NonNull final ModoVisualizacion nuevoModo
  ) {
    ocultarOpcionActual(item, nuevoModo);
    mostrarOpcionNueva(nuevoModo);
  }

  private void ocultarOpcionActual(@Nullable MenuItem item, @NonNull ModoVisualizacion nuevoModo) {
    getCurrentDisplayMenuOption(item, nuevoModo).setVisible(false);
  }

  @NonNull
  private MenuItem getCurrentDisplayMenuOption(
      @Nullable final MenuItem item,
      @NonNull final ModoVisualizacion nuevoModo
  ) {
    final MenuItem opcion;

    if (item != null) {
      opcion = item;
    } else {
      @IdRes final int itemId = nuevoModo == ModoVisualizacion.TARJETAS
          ? R.id.modo_tarjeta
          : R.id.modo_lista;
      opcion = menu.findItem(itemId);
    }

    return opcion;
  }

  private void mostrarOpcionNueva(@NonNull ModoVisualizacion nuevoModo) {
    @IdRes final int idOpcionVisible = nuevoModo == ModoVisualizacion.TARJETAS
        ? R.id.modo_lista
        : R.id.modo_tarjeta;
    menu.findItem(idOpcionVisible).setVisible(true);
  }

  private void setUpFoodList(@NonNull final List<AlimentoDAO> foodList) {
    if (binding.getFoodAdapter() == null) {
      setUpFoodAdapter(foodList);
    } else {
      binding.getFoodAdapter().swapItems(foodList);
    }
  }

  private void setUpFoodAdapter(@NonNull final List<AlimentoDAO> foodList) {
    final ScrollControlLayoutManager sclm =
        (ScrollControlLayoutManager) binding.rvAlimentos.getLayoutManager();

    if (sclm != null) {
      resizeManager = new CardViewResizeAnimationManager(binding.rvAlimentos, sclm);
    }

    final AdaptadorAlimentos adaptadorAlimentos = new AdaptadorAlimentos(getContext(),
        foodList, alimentosYaIncluidos, isForResult);
    binding.setFoodAdapter(adaptadorAlimentos);
    binding.setSearchListener(new SearchListener(viewModel));
    binding.setOnDeleteFoodListener(new OnDeleteFoodListener(foods -> viewModel.deleteFoods(foods)));
    setListeners(adaptadorAlimentos);
  }

  private void setListeners(@NonNull final AdaptadorAlimentos adaptadorAlimentos) {
    adaptadorAlimentos.setOnEditarAlimento((food, position) -> {
      viewModel.getFoodImageLoadedLiveData().observe(FoodListFragment.this.getViewLifecycleOwner(),
          onImageRetrievedObserver);
      viewModel.retrieveFoodImage(food, position);
    });
    adaptadorAlimentos.setOnRedimensionarTarjeta(resizeManager::resize);
    adaptadorAlimentos.setOnSeleccionarAlimento(this::activarModoSeleccion);
  }

  private void activarModoSeleccion(final boolean activado) {
    cambiarBotonesFlotantes(activado);
    cambiarActionBar(activado);
  }

  private void cambiarBotonesFlotantes(boolean activado) {
    ViewExtensions.visible(!activado, binding.fabCrearAlimentos);
    ViewExtensions.visible(activado && !isForResult, binding.fabCrearAlimentos);
    ViewExtensions.visible(activado && isForResult, binding.fabSeleccionarAlimentos);
  }

  private void cambiarActionBar(final boolean activado) {
    final Context context = getContext();

    if (context != null) {
      final @ColorRes int color = !activado
          ? R.color.primary
          : isForResult
              ? R.color.naranja_seleccion
              : R.color.rojo_borrar;
      final @StringRes int title = activado
          ? R.string.titulo_seleccion_alimentos
          : R.string.texto_titulo_seccion_alimentos;

      @ColorInt final int toolbarColor = ContextCompat.getColor(context, color);
      final String toolbarTitle = getString(title);
      toolbarBinding.toolbar.setBackgroundColor(toolbarColor);
      toolbarBinding.setToolbarTitle(toolbarTitle);
    }
  }

  //    @Override
  //    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  //        if (resultCode == RESULT_OK && dataManager.isRequestCodeForThisView(requestCode)) {
  //            showDialogDefinirAlimento(dataManager.getBitmapFromResult(getContext(), requestCode, data));
  //        } else {
  //            super.onActivityResult(requestCode, resultCode, data);
  //        }
  //    }

  private void onHomeButtonPressed() {
    // Si se ha realizado algún filtro, el botón atrás elimina el filtrado primero
    if (viewModel.isListFiltered()) {
      viewModel.filterFood(null);
      EditTextEX.setValue(binding.etNombreAlimento, null);
    } else {
      viewModel.cancelFoodRequest();

      if (isForResult) {
        // Si se pulsa "Atrás", se vuelve sin seleccionar ningún alimento. Podríamos prescindir de esto, pero bueno,
        // así tenemos hecho ya el callback. Si se seleccionan alimentos, hay que volver por el clic del botón
        // fab_seleccionar_alimentos (OnSelectFoodListener)
        getParentFragmentManager().setFragmentResult(KEY_SELECT_FOOD, new Bundle());
      }

      goBack();
    }
  }

  public void actualizarLista(boolean correcto) {
    if (correcto) {
      viewModel.requestFood(false);
      binding.etNombreAlimento.setText(null);
    }
  }

  private void showLoading(boolean show) {
    binding.loadingView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
  }

  private void definirAlimento(Bitmap bitmap) {

    // Redimensiona el Bitmap
    final Bitmap b = InternalMemoryUtils.prepararBitmap(bitmap);
    mostrarDialogEditar(null, b, -1);
    binding.loadingView.setVisibility(View.INVISIBLE);
  }

  private void mostrarDialogEditar(
      @Nullable final AlimentoDAO alimento,
      @NonNull final Bitmap bitmap,
      final int posicion
  ) {
    final DialogEditarAlimento fragment = DialogEditarAlimento.newInstance(alimento, posicion);
    fragment.setUp(bitmap);
    fragment.show(getChildFragmentManager(), DialogEditarAlimento.TAG);
  }

  private void showDialogDefinirAlimento(final Bitmap bitmap) {
    if (bitmap != null) {
      // Después de llegar el Bitmap resultado, espera 1,5 segundos para que se reconstruya la
      // Activity si se ha girado el móvil para tomar la foto y luego llama al método
      // definirAlimento()
      showLoading(true);
      final Handler handler = new Handler();
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          definirAlimento(bitmap);
        }
      }, 1500);
    } else {
      toast("Ha habido un problema al obtener la imagen.");
    }
  }
}
