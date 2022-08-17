package julioverne.insulinapp.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import butterknife.BindView;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.DataManager;
import julioverne.insulinapp.services.InsulinaRestanteService;
import julioverne.insulinapp.ui.dialogs.DialogClave;
import julioverne.insulinapp.utils.DecimalFormatUtils;
import julioverne.insulinapp.utils.TypefacesUtils;

/**
 * Created by Juan José Melero on 02/05/2015.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @BindView(R.id.tv_tituloInsulinaActual)
    @Nullable
    TextView tvCabeceraTituloInsulinaActual;
    @BindView(R.id.tv_insulinaActual)
    @Nullable
    TextView tvCabeceraInsulinaActual;
    @BindView(R.id.tv_uds)
    @Nullable
    TextView tvCabeceraUds;
    @BindView(R.id.ll_insulinaActual)
    @Nullable
    LinearLayout llCabeceraInsulinaActual;

    private static float insulinaRestante = 0.0f;
    protected DataManager dataManager;
    protected Typeface light;
    protected Typeface negrita;
    protected Typeface cursiva;

    private boolean isForResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataManager = DataManager.getInstance(BaseActivity.this);
        light = TypefacesUtils.get(this, "fonts/DejaVuSans-ExtraLight.ttf");
        negrita = TypefacesUtils.get(this, "fonts/DejaVuSansCondensed-Bold.ttf");
        cursiva = TypefacesUtils.get(this, "fonts/DejaVuSansCondensed-Oblique.ttf");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (receiver != null) {
            unregisterReceiver(receiver);
            dataManager.cancelarRepeticionCalculoInsulina();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(InsulinaRestanteService.BROADCAST_ACTION));
        // Activa la cabecera de insulina
        //        mostrarInsulinaActual();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onHomeButtonPressed();
                break;
            case R.id.configuracion:
                //                solicitarClave();
                startActivity(new Intent(this, ConfiguracionActivity.class));
                break;
            case R.id.copia_seguridad:
                abrirCopiaSeguridadActivity();
                break;
        }

        return true;
    }

    protected void onHomeButtonPressed() {
        if (isForResult) {
            onBackPressed();
        } else {
            NavUtils.navigateUpFromSameTask(this);
        }
    }

    protected abstract void loadView();

    public void setIsForResult() {
        this.isForResult = true;
    }

    /** Solicita la clave del médico para acceder a la configuración. */
    private void solicitarClave() {
        DialogClave dialog = new DialogClave(this);
        dialog.show();
    }

    // Activa un AlarmManager que llama al servicio de insulina restante cada cinco segundos
    public void mostrarInsulinaActual() {
        tvCabeceraInsulinaActual.setText(DecimalFormatUtils.decimalToStringIfZero(insulinaRestante, 2, ".", ","));
        if (!dataManager.isDiarioEmpty() && dataManager.hayInsulina()) {

            // Runs the service once
            if (insulinaRestante == 0.0f) {
                Intent intentService = new Intent(this, InsulinaRestanteService.class);
                startService(intentService);
            }

            // Runs the service every minute
            dataManager.establecerRepeticionCalculoInsulina();
        } else {
            tvCabeceraInsulinaActual.setText("0");
        }

        llCabeceraInsulinaActual.setVisibility(View.VISIBLE);
    }

    public void abrirCopiaSeguridadActivity() {
        startActivity(new Intent(this, BackUpFragment.class));
    }

    // Recibe un mensaje del servicio y muestra la cantidad de insulina
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            insulinaRestante = intent.getFloatExtra("INSULINA", 0.0f);
            tvCabeceraInsulinaActual.setText(DecimalFormatUtils.decimalToStringIfZero(insulinaRestante, 2, ".", ","));
            llCabeceraInsulinaActual.setVisibility(View.VISIBLE);
        }
    };
}
