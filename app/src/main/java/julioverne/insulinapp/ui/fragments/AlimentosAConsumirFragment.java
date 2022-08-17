package julioverne.insulinapp.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;
import java.util.List;
import julioverne.insulinapp.R;
import julioverne.insulinapp.constants.Constants;
import julioverne.insulinapp.data.Resource;
import julioverne.insulinapp.data.dao.AlimentoDAO;
import julioverne.insulinapp.data.dao.ItemAEntrada;
import julioverne.insulinapp.databinding.FragmentAlimentosLayoutBinding;
import julioverne.insulinapp.ui.activities.FoodListFragment;
import julioverne.insulinapp.ui.adaptadores.AdaptadorAlimentosControl;
import julioverne.insulinapp.ui.base.NavigationFragment;
import julioverne.insulinapp.ui.viewmodels.FoodToBeEatenViewModel;
import julioverne.insulinapp.utils.StringUtils;
import julioverne.insulinapp.utils.TecladoUtil;

public class AlimentosAConsumirFragment extends NavigationFragment {

  public static final String TAG = AlimentosAConsumirFragment.class.getSimpleName();
  public static final String BUNDLE_POSTPRANDIAL = "BUNDLE_POSTPRANDIAL";
  public static final String BUNDLE_SELECCIONADOS = "BUNDLE_SELECCIONADOS";
  public static final String BUNDLE_CONTIENE = "BUNDLE_CONTIENE";

  private FragmentAlimentosLayoutBinding binding;
  private AdaptadorAlimentosControl adapterContieneAlimentos;
  private ArrayAdapter<String> adapterNombres;
  private FoodToBeEatenViewModel viewModel;
  @NonNull
  private final Observer<Resource<List<String>>> foodNamesObserver = new Observer<Resource<List<String>>>() {
    @Override
    public void onChanged(@Nullable Resource<List<String>> resource) {
      if (resource != null) {
        if (resource.isSuccessful()) {
          final List<String> foodNames = resource.getData();

          //RETURN CASE: Si no hay alimentos, regresa a MainActivity borrando los estados del botón
          // "Atrás" del dispositivo.
          if (foodNames.isEmpty()) {
            onEmptyList();
          } else {
            viewModel.setFoodNames(foodNames);
            eliminarNombresContenidos();
            adapterNombres = new ArrayAdapter<>(
                getActivity(), R.layout.item_autocomplete_alimentos_layout,
                R.id.textView, foodNames);

            binding.actvNombreAlimento.setAdapter(adapterNombres);
            binding.actvNombreAlimento.setThreshold(1);
            binding.actvNombreAlimento.setEnabled(true);
          }
        } else {
          onEmptyList();
        }
      }
    }
  };

  @NonNull
  public static AlimentosAConsumirFragment getInstance(
      final boolean postprandial,
      final List<ItemAEntrada> alimentosSeleccionados
  ) {
    final AlimentosAConsumirFragment f = new AlimentosAConsumirFragment();
    final Bundle bundle = new Bundle();
    bundle.putBoolean(BUNDLE_POSTPRANDIAL, postprandial);
    bundle.putParcelableArrayList(BUNDLE_SELECCIONADOS, new ArrayList<>(alimentosSeleccionados));
    f.setArguments(bundle);
    return f;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final boolean postprandial = getArguments().getBoolean(BUNDLE_POSTPRANDIAL);
    final List<ItemAEntrada> selectedFood = getArguments().getParcelableArrayList(BUNDLE_SELECCIONADOS);
    viewModel = new ViewModelProvider(this).get(FoodToBeEatenViewModel.class);
    viewModel.initialize(postprandial, selectedFood);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentAlimentosLayoutBinding.inflate(inflater, container, false);
    baseBinding = binding;

    binding.swPostprandial.setChecked(viewModel.isPostprandial());
    binding.actvNombreAlimento.setEnabled(false);
    viewModel.getFoodNamesLiveData().observe(getViewLifecycleOwner(), foodNamesObserver);

    adapterContieneAlimentos = new AdaptadorAlimentosControl(getActivity(), this, viewModel.getSelectedFood());
    binding.lvAlimentos.setAdapter(adapterContieneAlimentos);

    // Cuando se selecciona el nombre de la lista de autocompletado, se añade el alimento
    binding.actvNombreAlimento.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final String nombre = (String) parent.getAdapter().getItem(position);
        annadirAlimento(nombre);
      }
    });

    // Asigna un listener para que cambie el foco al AutoCompleteTextView cada vez que se haga
    // scroll en la lista
    binding.lvAlimentos.setOnScrollListener(new AbsListView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {
        binding.actvNombreAlimento.requestFocus();
      }

      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
          int totalItemCount) {
        // No hay que hacer nada al hacer scroll
      }
    });

    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel.requestFoodNames().observe(getViewLifecycleOwner(), foodNamesObserver);
    binding.ibAnnadir.setOnClickListener(button -> abrirListaAlimentos());
  }

  private void onEmptyList() {
    longToast(getString(R.string.advertencia_no_hay_alimentos));
    goToMainActivity();
  }

  private void goToMainActivity() {
    // TODO: Melero 08/11/2021 Habrá que volver a la activity de arriba
    //        MenuFragment.start(getContext());
    goBack();
  }

  /**
   * Añade el alimento que haya en el AutoCompleteTextView con cero unidades
   */
  private void abrirListaAlimentos() {
    // Primero registramos un listener para el resultado y luego lanzamos el fragment para obtener un resultado.
    getParentFragmentManager().setFragmentResultListener(FoodListFragment.KEY_SELECT_FOOD, getViewLifecycleOwner(),
        new FragmentResultListener() {
          @Override
          public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
            final List<AlimentoDAO> seleccionados = result.getParcelableArrayList(FoodListFragment.PARAM_SELECTED_FOOD);

            if (seleccionados != null) {
              annadirAlimentos(seleccionados);
              // Restablece la forma en que se muestra el teclado porque parece ser que se pierde
              TecladoUtil.keyboardToAdjustPan(requireActivity());
            }
          }
        });

    Intent intent = new Intent(getActivity(), FoodListFragment.class);
    intent.putExtra(FoodListFragment.BUNDLE_FOR_RESULT, true);
    intent.putParcelableArrayListExtra(FoodListFragment.BUNDLE_INCLUIDOS,
        new ArrayList<>(adapterContieneAlimentos.getAlimentosIncluidos()));

    startActivityForResult(intent, Constants.SELECCION_ALIMENTOS_REQUEST_CODE);
    /*TODO Melero 27/06/2022: Aquí habrá que navegar al fragment añadiéndolo en lugar de reemplazándolo*/
    //navigateTo();
  }

  /**
   * Hace que cambie el foco para que se guarde el último valor seleccionado y obtiene todos los
   * alimentos y sus cantidades.
   *
   * @return Devuelve la lista de alimentos y cantidades o nulo si no se ha indicado alguna cantidad.
   */
  public List<ItemAEntrada> getAlimentosSeleccionados() {
    binding.actvNombreAlimento.requestFocus();
    return adapterContieneAlimentos.getAlimentos();
  }

  /**
   * Devuelve los alimentos del array son comprobar si hay cantidaddes vacías
   */
  @NonNull
  public List<ItemAEntrada> getAlimentosSeleccionadosRaw() {
    binding.actvNombreAlimento.requestFocus();
    return adapterContieneAlimentos.getDatos();
  }

  /**
   * Devuelve un booleano que indica si la dosis se tomó después de comer.
   */
  public boolean getPostprandial() {
    return binding.swPostprandial.isChecked();
  }

  /**
   * Al eliminarse un alimento de la lista, vuelve a estar disponible en el autocompletado
   */
  public void actualizarAutoCompleteTextView(@NonNull final String nombre) {
    if (!StringUtils.isCadenaVacia(nombre)) {
      viewModel.addFoodName(nombre);
      adapterNombres.add(nombre);
    }
  }

  /**
   * Añade varios nombres al AutoCompleteTextView
   *
   * @param nombres <i>Array</i> de cadenas.
   */
  public void actualizarAutoCompleteTextView(String[] nombres) {
    for (int i = 0; i < nombres.length; i++) {
      actualizarAutoCompleteTextView((nombres[i]));
    }
  }

  /**
   * Elimina los nombres que haya en la lista de alimentos seleccionados de la lista de nombres
   * del adapter del <i>AutoCompleteTextView</i>
   */
  public void eliminarNombresContenidos() {
    for (ItemAEntrada contiene : viewModel.getSelectedFood()) {
      viewModel.removeFoodName(contiene.getAlimento().getNombre());
    }
  }

  /**
   * Añade el alimento con el nombre autocompletado con cero unidades
   */
  public void annadirAlimento(String nombre) {
    AlimentoDAO alimento = dataManager.getAlimento(nombre);
    if (alimento != null) {
      adapterContieneAlimentos.nuevoAlimento(alimento);
      viewModel.removeFoodName(alimento.getNombre());
      adapterNombres.remove(alimento.getNombre());
      binding.actvNombreAlimento.setText("");
      TecladoUtil.ocultarTeclado(getActivity());
    }
  }

  /**
   * Añade un nuevo alimento a la lista con una cantidad de 0
   */
  public void annadirAlimento(AlimentoDAO alimento) {
    if (alimento != null) {
      adapterContieneAlimentos.nuevoAlimento(alimento);
      viewModel.removeFoodName(alimento.getNombre());
      adapterNombres.remove(alimento.getNombre());
      binding.actvNombreAlimento.setText("");
    }
  }

  /**
   * Inserta un {@code ContieneAlimento} por cada alimento que recibe
   */
  public void annadirAlimentos(List<AlimentoDAO> seleccionados) {
    boolean insertado = false;

    //Comprueba alimento por alimento, que no estén ya insertados. Si alguno no lo está, lo
    //inserta.
    for (AlimentoDAO alimento : seleccionados) {
      for (ItemAEntrada contieneExistente : adapterContieneAlimentos.getDatos()) {
        if (contieneExistente.getAlimento().getNombre().equals(alimento.getNombre())) {
          insertado = true;
        }
      }

      if (!insertado) {
        annadirAlimento(alimento);
      } else {
        insertado = false;
      }
    }
  }
}
