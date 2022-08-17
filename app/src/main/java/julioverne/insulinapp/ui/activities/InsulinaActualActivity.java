package julioverne.insulinapp.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.DataManager;
import julioverne.insulinapp.utils.DecimalFormatUtils;

/**
 * Created by Juan José Melero on 09/05/2015.
 */
public class InsulinaActualActivity extends BaseActivity {

    @BindView(R.id.tv_insulina_actual)
    TextView tvInsulinaActual;
    @BindView(R.id.tv_actualmente)
    TextView tvActualmente;
    @BindView(R.id.tv_insulina_total)
    TextView tvInsulinaTotal;
    @BindView(R.id.tv_uds)
    TextView tvUds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insulina_actual_layout);
        ButterKnife.bind(this);
        this.dataManager = DataManager.getInstance(this);
        loadView();
    }

    @Override
    protected void loadView() {

        //        cargarTipografiaCabecera();

        //Tipografías
        tvInsulinaActual.setTypeface(light);
        tvActualmente.setTypeface(light);
        tvInsulinaTotal.setTypeface(negrita);
        tvUds.setTypeface(light);

        cargarCantidadInsulina();
    }

    public void cargarCantidadInsulina() {

        float insulinaActual = dataManager.calcularInsulinaRestante();

        if (insulinaActual == -1f) {
            tvActualmente.setText(R.string.sin_controles);
            tvInsulinaTotal.setVisibility(View.GONE);
            tvUds.setVisibility(View.GONE);
        } else {
            tvActualmente.setText(R.string.insulina_actual_aproximada);
            tvInsulinaTotal.setText(DecimalFormatUtils.decimalToStringIfZero(insulinaActual, 2, ".", ","));
            tvInsulinaTotal.setVisibility(View.VISIBLE);
            tvUds.setVisibility(View.VISIBLE);
        }
    }

    //    @Override
    //    protected void onResume() {
    //        final String BROADCAST_ACTION = "julioverne.insulinapp.ui.AlimentosActivity";
    //        registerReceiver(receiver, new IntentFilter(BROADCAST_ACTION));
    //        super.onResume();
    //    }
    //
    //
    //    @Override
    //    protected void onPause() {
    //        unregisterReceiver(receiver);
    //        super.onPause();
    //    }
}
