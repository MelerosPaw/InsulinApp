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
import julioverne.insulinapp.data.DataManager;
import julioverne.insulinapp.data.dao.EntradaDAO;
import julioverne.insulinapp.data.dao.ItemAEntrada;
import julioverne.insulinapp.data.dao.PeriodoDAO;
import julioverne.insulinapp.ui.base.BaseFragment;
import julioverne.insulinapp.utils.CalendarExtensions;
import julioverne.insulinapp.utils.DateUtils;
import julioverne.insulinapp.utils.DecimalFormatUtils;
import julioverne.insulinapp.utils.StringUtils;

/**
 * Created by Juan José Melero on 19/06/2015.
 */
public class DosisFragment extends BaseFragment {

    public static final String TAG = DosisFragment.class.getSimpleName();

    @BindView(R.id.tv_titulo_dosis)
    TextView tvTituloDosis;
    @BindView(R.id.tv_dosisTotal)
    TextView tvDosisTotal;
    @BindView(R.id.tv_uds)
    TextView tvUds;
    @BindView(R.id.tv_tituloDosisCorrectiva)
    TextView tvTituloDosisCorrectiva;
    @BindView(R.id.tv_unidadesDosisCorrectiva)
    TextView tvDosisCorrectiva;
    @BindView(R.id.tv_titulo_alimentos)
    TextView tvTituloAlimentos;
    @BindView(R.id.tv_unidadesAlimentos)
    TextView tvUnidadesAlimentos;
    @BindView(R.id.tv_tituloInsulinaRestante)
    TextView tvTituloInsulinaRestante;
    @BindView(R.id.tv_insulinaRestante)
    TextView tvInsulinaRestante;

    private DataManager dataManager;

    private int glucosaSangre;
    private boolean postprandial;
    private ArrayList<ItemAEntrada> alimentosSeleccionados;
    private int glucosaMaxima;
    private int glucosaMinima;
    private int factorSensibilidad;
    private float insulinaRestante;
    private Integer glucosaFinAbsorcion;
    private float dosisCorrectiva;
    private float dosisAlimentos;
    private int unidadesSobrantes;
    private float dosisTotal;
    private Unbinder unbinder;

    /**
     * Crea una nueva instancia de la clase inicializando sus parámetros.
     *
     * @param nivelGlucosa Número entero que aparecerá en el <i>EditText</i> cuando se inicie
     * el <i>Fragment</i>.
     * @param alimentosSeleccionados Los alimentos seleccionados y las cantidades de cada uno.
     * @param postprandial Booleano que indica si fue después de comer o antes.
     * @return Instancia del <i>Fragment</i>
     */
    public static DosisFragment getInstance(int nivelGlucosa,
        List<ItemAEntrada> alimentosSeleccionados,
        boolean postprandial) {
        DosisFragment f = new DosisFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("GLUCOSA", nivelGlucosa);
        bundle.putBoolean("POSTPRANDIAL", postprandial);
        bundle.putParcelableArrayList("SELECCIONADOS", new ArrayList<>(alimentosSeleccionados));
        f.setArguments(bundle);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataManager = DataManager.getInstance(getActivity());
        //Recupera la información
        glucosaSangre = getArguments().getInt("GLUCOSA");
        postprandial = getArguments().getBoolean("POSTPRANDIAL");
        alimentosSeleccionados = getArguments().getParcelableArrayList("SELECCIONADOS");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dosis_total_layout, container, false);
        unbinder = ButterKnife.bind(this, view);

        //Tipografias
        tvTituloDosis.setTypeface(light);
        tvTituloDosisCorrectiva.setTypeface(light);
        tvDosisCorrectiva.setTypeface(light);
        tvTituloAlimentos.setTypeface(light);
        tvUnidadesAlimentos.setTypeface(light);
        tvDosisTotal.setTypeface(negrita);
        tvTituloInsulinaRestante.setTypeface(light);
        tvInsulinaRestante.setTypeface(light);
        tvUds.setTypeface(light);

        // Obtención de las preferencias necesarias
        String[] preferencias = dataManager.getPreferenciasDosis();
        if (preferencias.length != 0) {
            glucosaMaxima = Integer.parseInt(preferencias[0]);
            glucosaMinima = Integer.parseInt(preferencias[1]);
            factorSensibilidad = Integer.parseInt(preferencias[2]);
        }

        //Calcular la dosis correctiva
        boolean hayEntradas = !dataManager.isDiarioEmpty();

        //Si hay entradas en el diario, se comprueba si hay insulina restante, si no hay,
        //el cálculo se hace sin comprobarlo
        if (hayEntradas) {
            boolean calculadoConInsulina = calcularConInsulina();
            if (!calculadoConInsulina) {
                calcularSinInsulina();
            }
            //Si no hay entradas, no se puede calcular con insulina restante, así que se calcula sobre
            //la cantidad directamente
        } else {
            calcularSinInsulina();
        }

        //Calcula la dosis por alimentos
        if (!alimentosSeleccionados.isEmpty()) {
            calcularDosisAlimentos();
        }

        //Calcula la dosis total de insulina
        calcularDosisTotal();

        //Rellena la información
        tvDosisTotal.setText(String.valueOf((int) dosisTotal));
        tvDosisCorrectiva.setText(String.valueOf((int) dosisCorrectiva));
        tvUnidadesAlimentos.setText(String.valueOf((int) dosisAlimentos));
        tvInsulinaRestante.setText(String.valueOf((int) insulinaRestante));

        return view;
    }

    /**
     * Calcula la dosis comprobando si hay insulina.
     *
     * @return Devuelve <i>true</i> si había insulina y se han realizado los cálculos.
     */
    public boolean calcularConInsulina() {
        //Si hay insulina calcula si se estabiliza el nivel de glucosa al final de su absorción
        if (dataManager.hayInsulina()) {
            insulinaRestante = dataManager.calcularInsulinaRestante();
            calcularGlucosaFinAbsorcion();

            //Si cuando sea absorbida se normaliza, no se calcula corrección
            if (glucosaFinAbsorcion < glucosaMaxima && glucosaFinAbsorcion > glucosaMinima) {
                dosisCorrectiva = 0;
                return true;
                //Si no se estabiliza, se calcula corrección
            } else {
                calcularDosisCorrectiva();
                return true;
            }
        }

        return false;
    }

    /** Calcula la dosis en caso de que no haya insulina desde la última entrada */
    public void calcularSinInsulina() {
        //Si no hay insulina, la glucosa al final de la absorción será la misma que la actual
        glucosaFinAbsorcion = glucosaSangre;

        //Si la cantidad es correcta, no se calcula nada
        if (glucosaSangre >= glucosaMinima && glucosaSangre <= glucosaMaxima) {
            dosisCorrectiva = 0;
            //Si no es correcta, se calcula la pauta correctora
        } else {
            calcularDosisCorrectiva();
            //Si la cantidad es menor que 70, recomienda tomar algo
            if (glucosaSangre <= 70) {
                tvTituloDosis.setText(getString(R.string.texto_dosis) + "\n" + getString(R.string.texto_tomar_glucosa));
            }
        }
    }

    /**
     * Calcula cuánta glucosa quedará después de haberse consumido toda la insulina que queda
     * usando el factor de sensibilidad
     */
    public void calcularGlucosaFinAbsorcion() {
        glucosaFinAbsorcion = (int) (glucosaSangre - (insulinaRestante * factorSensibilidad));
    }

    /**
     * Calcula cuántas unidades tiene que inyectarse para corregir el nivel de glucosa si este no
     * se corrije cuando toda la insulina se haya consumido
     */
    public void calcularDosisCorrectiva() {
        //Si está por debajo, devuelve cero
        if (glucosaFinAbsorcion < glucosaMinima) {
            dosisCorrectiva = 0;
            //Si está por encima, se calcula la corrección
        } else {
            int glucosaSobrante = glucosaFinAbsorcion - glucosaMaxima;
            dosisCorrectiva = glucosaSobrante / factorSensibilidad;
        }
    }

    /**
     * Calcula la dosis correspondiente a la cantidad de alimentos seleccionados. Si no hay alimentos
     * en la lista, la dosis será de 0 unidades.
     */
    public void calcularDosisAlimentos() {
        PeriodoDAO periodo = dataManager.getPeriodo(DateUtils.getHoraActual());
        if (!alimentosSeleccionados.isEmpty()) {
            for (ItemAEntrada c : alimentosSeleccionados)
                dosisAlimentos += c.getCantidad() * c.getAlimento().getRacionesUnidad() * periodo.getUnidadesInsulina();
        } else {
            dosisAlimentos = 0;
        }
    }

    /**
     * Calcula la dosis total sumando las dosis correctiva y de los alimentos y restándole las unidades
     * sobrantes.
     */
    public void calcularDosisTotal() {
        calcularUnidadesSobrantes();
        dosisTotal = dosisCorrectiva + dosisAlimentos - unidadesSobrantes;

        // Si la dosis es negativa, se debe a que hay insulina sobrante, y esto es porque se espera
        // una subida del nivel de glucosa todavía. Esto se da cuando el control se realiza antes de
        // superar el momento del pico. De lo contrario, no habría unidades sobrantes, ya que los
        // cálculos son certeros
        dosisTotal = dosisTotal < 0 ? 0 : dosisTotal;
    }

    /**
     * Si el nivel de glucosa se corrije y hay más insulina en la sangre, da valor a esa cantidad.
     * Si cuando acabe la absorción de la glucosa, el nivel queda por debajo del mínimo, quiere
     * decir que hay unidades de insulina de más. Podemos saber cuántas hay dividiendo la cantidad
     * que excede del mínimo entre el factor de sensibilidad del paciente.
     */
    private void calcularUnidadesSobrantes() {

        //Si la glucosa queda por debajo del mínimo, se calculan las unidades sobrantes.
        if (glucosaFinAbsorcion < glucosaMinima) {
            unidadesSobrantes = (glucosaMinima - glucosaFinAbsorcion) / factorSensibilidad;
        }
        //Si queda por encima o queda igual, no sobra insulina
        else {
            unidadesSobrantes = 0;
        }
    }

    /** Guarda una entrada con los datos indicados por el usuario */
    public boolean guardarEntrada() {

        EntradaDAO entrada = new EntradaDAO();
        entrada.setAntesComer(!postprandial);
        entrada.setFecha(CalendarExtensions.now());
        entrada.setGlucosaSangre(glucosaSangre);
        entrada.setUnidadesInyectadas((int) dosisTotal);
        entrada.setUnidadesSangre(insulinaRestante);

        // Resumen de alimentos de la entrada
        String resumenAlimentos = "";

        if (!alimentosSeleccionados.isEmpty()) {
            for (int i = 0; i < alimentosSeleccionados.size(); i++) {
                String cantidad =
                    DecimalFormatUtils.decimalToStringIfZero(alimentosSeleccionados.get(i).getCantidad(), 2, ".", ",");
                String unidadMedida = alimentosSeleccionados.get(i).getAlimento().getUnidadMedida();
                String nombre = alimentosSeleccionados.get(i).getAlimento().getNombre();
                resumenAlimentos += "- " + cantidad + " " + unidadMedida + " " + nombre;
                if (i != alimentosSeleccionados.size() - 1) {
                    resumenAlimentos += "\n";
                }
            }
        }

        entrada.setResumenAlimentos(resumenAlimentos);

        // Una vez creada la entrada, la intenta guardar
        boolean guardado = dataManager.guardarEntrada(entrada);
        if (guardado) {
            StringUtils.toastCorto(getActivity(), getString(R.string.advertencia_entrada_guardada));
        } else {
            StringUtils.toastLargo(getActivity(), getString(R.string.advertencia_imposible_guardar));
        }

        return guardado;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
