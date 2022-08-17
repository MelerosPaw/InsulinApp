package julioverne.insulinapp.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import java.util.ArrayList;
import java.util.List;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.dao.ItemAEntrada;
import julioverne.insulinapp.ui.base.BaseFragment;
import julioverne.insulinapp.utils.DecimalFormatUtils;

public class ResumenFragment extends BaseFragment {

    public static final String TAG = ResumenFragment.class.getSimpleName();

    public static final String BUNDLE_GLUCOSA = "BUNDLE_GLUCOSA";
    public static final String BUNDLE_POSTPRANDIAL = "BUNDLE_GLUCOSA";
    public static final String BUNDLE_SELECCIONADOS = "BUNDLE_SELECCIONADOS";

    @BindView(R.id.tv_preguntaSeguro)
    TextView tvPreguntaSeguro;
    @BindView(R.id.tv_titulo_glucosa_sangre)
    TextView tvTituloGlucosaSangre;
    @BindView(R.id.tv_glucosa_sangre)
    TextView tvGlucosaSangre;
    @BindView(R.id.tv_mgdl)
    TextView tvMgdl;
    @BindView(R.id.tv_tituloPostprandial)
    TextView tvTituloPostprandial;
    @BindView(R.id.tv_postprandial)
    TextView tvPostprandial;
    @BindView(R.id.tv_tituloResumen)
    TextView tvTituloResumen;
    @BindView(R.id.tv_resumen)
    TextView tvResumen;

    private int glucosaSangre;
    private boolean postprandial;
    private ArrayList<ItemAEntrada> alimentosSeleccionados;
    private Unbinder unbinder;

    /**
     * Crea una nueva instancia de la clase inicializando sus parámetros.
     *
     * @param nivelGlucosa Número entero que aparecerá en el <i>EditText</i> cuando se inicie
     * el <i>Fragment</i>
     * @return Instancia del <i>Fragment</i>
     */
    public static ResumenFragment getInstance(int nivelGlucosa, List<ItemAEntrada> alimentosSeleccionados,
        boolean postprandial) {
        ResumenFragment f = new ResumenFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_GLUCOSA, nivelGlucosa);
        bundle.putBoolean(BUNDLE_POSTPRANDIAL, postprandial);
        bundle.putParcelableArrayList(BUNDLE_SELECCIONADOS, new ArrayList<>(alimentosSeleccionados));
        f.setArguments(bundle);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Recupera la información
        glucosaSangre = getArguments().getInt(BUNDLE_GLUCOSA);
        postprandial = (getArguments().getBoolean(BUNDLE_POSTPRANDIAL));
        alimentosSeleccionados = getArguments().getParcelableArrayList(BUNDLE_SELECCIONADOS);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resumen_layout, container, false);
        unbinder = ButterKnife.bind(this, view);

        //Tipografias
        tvPreguntaSeguro.setTypeface(light);
        tvTituloGlucosaSangre.setTypeface(light);
        tvGlucosaSangre.setTypeface(light);
        tvMgdl.setTypeface(light);
        tvTituloPostprandial.setTypeface(light);
        tvPostprandial.setTypeface(light);
        tvTituloResumen.setTypeface(light);
        tvResumen.setTypeface(light);

        tvGlucosaSangre.setText(String.valueOf(glucosaSangre));
        if (postprandial) {
            tvPostprandial.setText(R.string.si);
        } else {
            tvPostprandial.setText(R.string.no);
        }

        //Si hay alimentos seleccionados, rellena la lista con sus datos
        if (!alimentosSeleccionados.isEmpty()) {
            StringBuilder resumen = new StringBuilder();
            for (int i = 0; i < alimentosSeleccionados.size(); i++) {
                String cantidad =
                    DecimalFormatUtils.decimalToStringIfZero(alimentosSeleccionados.get(i).getCantidad(), 2, ".", ",");
                String unidadMedida = alimentosSeleccionados.get(i).getAlimento().getUnidadMedida();
                String nombre = alimentosSeleccionados.get(i).getAlimento().getNombre();
                resumen.append("- ").append(cantidad).append(" ").append(unidadMedida).append(" de ").append(nombre);
                if (i != alimentosSeleccionados.size() - 1) {
                    resumen.append("\n");
                }
            }
            tvResumen.setText(resumen.toString());
        } else {
            tvTituloResumen.setVisibility(View.GONE);
            tvResumen.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
