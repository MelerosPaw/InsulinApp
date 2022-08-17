package julioverne.insulinapp.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import java.util.ArrayList;
import java.util.List;
import julioverne.insulinapp.R;
import julioverne.insulinapp.constants.Constants;
import julioverne.insulinapp.data.DataManager;
import julioverne.insulinapp.data.dao.AlimentoDAO;
import julioverne.insulinapp.data.dao.ItemAEntrada;
import julioverne.insulinapp.ui.activities.AlimentosActivity;
import julioverne.insulinapp.ui.activities.MainActivity;
import julioverne.insulinapp.ui.adaptadores.AdaptadorAlimentosControl;
import julioverne.insulinapp.utils.StringUtils;
import julioverne.insulinapp.utils.TecladoUtil;

import static android.app.Activity.RESULT_OK;

public class AlimentosFragment extends BaseFragment {

    public static final String TAG = AlimentosFragment.class.getSimpleName();
    public static final String BUNDLE_POSTPRANDIAL = "BUNDLE_POSTPRANDIAL";
    public static final String BUNDLE_SELECCIONADOS = "BUNDLE_SELECCIONADOS";
    public static final String BUNDLE_CONTIENE = "BUNDLE_CONTIENE";

    @BindView(R.id.tv_pregunta_comida)
    TextView tvPreguntaComida;
    @BindView(R.id.actv_nombre_alimento)
    AutoCompleteTextView actvNombreAlimento;
    @BindView(R.id.ib_annadir)
    ImageButton ibAnnadir;
    @BindView(R.id.lv_alimentos)
    ListView lvAlimentos;
    @BindView(R.id.sw_postprandial)
    Switch swPostprandial;

    private AdaptadorAlimentosControl adapterContieneAlimentos;
    private ArrayAdapter adapterNombres;
    private List<String> nombresAlimentos;
    private boolean postprandial;
    private ArrayList<ItemAEntrada> alimentosSeleccionados;
    private Unbinder unbinder;
    private DataManager dataManager;

    public static AlimentosFragment getInstance(boolean postprandial,
        List<ItemAEntrada> alimentosSeleccionados) {
        AlimentosFragment f = new AlimentosFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(BUNDLE_POSTPRANDIAL, postprandial);
        bundle.putParcelableArrayList(BUNDLE_SELECCIONADOS, new ArrayList<>(alimentosSeleccionados));
        f.setArguments(bundle);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postprandial = getArguments().getBoolean(BUNDLE_POSTPRANDIAL);
        alimentosSeleccionados = getArguments().getParcelableArrayList(BUNDLE_SELECCIONADOS);
        dataManager = DataManager.getInstance(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alimentos_layout, container, false);
        unbinder = ButterKnife.bind(this, view);

        //Tipografía
        tvPreguntaComida.setTypeface(light);
        actvNombreAlimento.setTypeface(light);
        swPostprandial.setTypeface(negrita);

        swPostprandial.setChecked(postprandial);
        actvNombreAlimento.setEnabled(false);
        dataManager.getNombresAlimentos(new DataManager.GetNombresAlimentosCallback() {
            @Override
            public void onSuccess(List<String> nombres) {
                nombresAlimentos = nombres;

                //RETURN CASE: Si no hay alimentos, regresa a MainActivity borrando los estados del botón
                // "Atrás" del dispositivo.
                if (nombresAlimentos.isEmpty()) {
                    StringUtils.toastLargo(getActivity(), getString(R.string.advertencia_no_hay_alimentos));
                    goToMainActivity();
                } else {
                    eliminarNombresContenidos();
                    adapterNombres = new ArrayAdapter<>(getActivity(), R.layout.item_autocomplete_alimentos_layout,
                        R.id.textView, nombresAlimentos);
                    actvNombreAlimento.setAdapter(adapterNombres);
                    actvNombreAlimento.setThreshold(1);
                    actvNombreAlimento.setEnabled(true);
                }
            }

            @Override
            public void onFailure() {
                StringUtils.toastLargo(getActivity(), getString(R.string.advertencia_no_hay_alimentos));
                goToMainActivity();
            }
        });

        adapterContieneAlimentos = new AdaptadorAlimentosControl(requireContext(), this, alimentosSeleccionados);
        lvAlimentos.setAdapter(adapterContieneAlimentos);

        // Cuando se selecciona el nombre de la lista de autocompletado, se añade el alimento
        actvNombreAlimento.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String nombre = (String) parent.getAdapter().getItem(position);
                annadirAlimento(nombre);
            }
        });

        // Asigna un listener para que cambie el foco al AutoCompleteTextView cada vez que se haga
        // scroll en la lista
        lvAlimentos.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                actvNombreAlimento.requestFocus();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
                // No hay que hacer nada al hacer scroll
            }
        });

        return view;
    }

    private void goToMainActivity() {
        MainActivity.start(getContext());
    }

    /** Añade el alimento que haya en el AutoCompleteTextView con cero unidades */
    @OnClick(R.id.ib_annadir)
    public void abrirListaAlimentos() {
        Intent intent = new Intent(getActivity(), AlimentosActivity.class);
        intent.putExtra(AlimentosActivity.BUNDLE_FOR_RESULT, true);
        intent.putParcelableArrayListExtra(AlimentosActivity.BUNDLE_INCLUIDOS,
            new ArrayList<>(adapterContieneAlimentos.getAlimentosIncluidos()));
        startActivityForResult(intent, Constants.SELECCION_ALIMENTOS_REQUEST_CODE);
    }

    /**
     * Hace que cambie el foco para que se guarde el último valor seleccionado y obtiene todos los
     * alimentos y sus cantidades.
     *
     * @return Devuelve la lista de alimentos y cantidades o nulo si no se ha indicado alguna cantidad.
     */
    public List<ItemAEntrada> getAlimentosSeleccionados() {
        actvNombreAlimento.requestFocus();
        return adapterContieneAlimentos.getAlimentos();
    }

    /** Devuelve los alimentos del array son comprobar si hay cantidaddes vacías */
    public List<ItemAEntrada> getAlimentosSeleccionadosRaw() {
        actvNombreAlimento.requestFocus();
        return adapterContieneAlimentos.getDatos();
    }

    /** Devuelve un booleano que indica si la dosis se tomó después de comer. */
    public boolean getPostprandial() {
        return swPostprandial.isChecked();
    }

    /** Al eliminarse un alimento de la lista, vuelve a estar disponible en el autocompletado */
    public void actualizarAutoCompleteTextView(String nombre) {
        if (!StringUtils.isCadenaVacia(nombre)) {
            nombresAlimentos.add(nombre);
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
        for (ItemAEntrada contiene : alimentosSeleccionados) {
            nombresAlimentos.remove(contiene.getAlimento().getNombre());
        }
    }

    /** Añade el alimento con el nombre autocompletado con cero unidades */
    public void annadirAlimento(String nombre) {
        AlimentoDAO alimento = dataManager.getAlimento(nombre);
        if (alimento != null) {
            adapterContieneAlimentos.nuevoAlimento(alimento);
            nombresAlimentos.remove(alimento.getNombre());
            adapterNombres.remove(alimento.getNombre());
            actvNombreAlimento.setText("");
            TecladoUtil.ocultarTeclado(getActivity());
        }
    }

    /** Añade un nuevo alimento a la lista con una cantidad de 0 */
    public void annadirAlimento(AlimentoDAO alimento) {
        if (alimento != null) {
            adapterContieneAlimentos.nuevoAlimento(alimento);
            nombresAlimentos.remove(alimento.getNombre());
            adapterNombres.remove(alimento.getNombre());
            actvNombreAlimento.setText("");
        }
    }

    /** Inserta un {@code ContieneAlimento} por cada alimento que recibe */
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.SELECCION_ALIMENTOS_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<AlimentoDAO> seleccionados = data.getParcelableArrayListExtra(BUNDLE_CONTIENE);
            annadirAlimentos(seleccionados);
            // Restablece la forma en que se muestra el teclado porque parece ser que se pierde
            TecladoUtil.keyboardToAdjustPan(getActivity());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
