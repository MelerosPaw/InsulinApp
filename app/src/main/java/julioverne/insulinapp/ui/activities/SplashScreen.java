package julioverne.insulinapp.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.BackUpManager;
import julioverne.insulinapp.data.DataManager;
import julioverne.insulinapp.databinding.ActivitySplashScreenLayoutBinding;
import julioverne.insulinapp.extensions.ActivityExtensions;
import julioverne.insulinapp.ui.ResultadoCopiaSeguridad;
import julioverne.insulinapp.utils.MessageExtensions;
import julioverne.insulinapp.utils.TypefacesUtils;

public class SplashScreen extends AppCompatActivity {

    private static final Integer SLEEP_TIME = 2000;

    private ActivitySplashScreenLayoutBinding binding;
    private DataManager dataManager;
    private boolean isPaused;
    private boolean timeHasPassed;
    private Handler sleepingHandler = new Handler();
    private Runnable launchActivityRunnable = new Runnable() {
        public void run() {
            executeAction();
        }
    };
    private View.OnClickListener proceedListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            executeAction();
            sleepingHandler.removeCallbacks(launchActivityRunnable);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExtensions.bind(this, R.layout.activity_splash_screen_layout);
        binding.setOnClickListener(proceedListener);

        dataManager = DataManager.getInstance(this);
        setTypefaces();
        sleepingHandler.postDelayed(launchActivityRunnable, SLEEP_TIME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;

        if (timeHasPassed) {
            abrirMainActivity();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    /**
     * Si no se ha mostrado el mensaje de restaurarCopiaSeguridad un base de datos existente por primera vez, lo
     * muestra. Si no hay ninguna base de datos que restaurarCopiaSeguridad, se abre directamente la MainActivity.
     */
    private void executeAction() {
        timeHasPassed = true;

        if (!dataManager.seHaOfrecidoRestaurar() && dataManager.hayCopiaSeguridad()) {
            intentarRestaurarCopiasSeguridad();
        } else {
            abrirMainActivity();
        }
    }

    /**
     * Si hay base de datos o copia de ajustes, intenta restaurarlos y luego inicia la MainActivity.
     */
    private void intentarRestaurarCopiasSeguridad() {
        if (dataManager.hayCopiaSeguridad()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            dataManager.restaurarCopiaSeguridad(builder, new BackUpManager.RestoreListener() {
                @Override
                public void isRestoring(ResultadoCopiaSeguridad resultado) {
                    MessageExtensions.showAlert(SplashScreen.this, resultado.getMensaje(),
                        getString(R.string.de_acuerdo),
                        new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                abrirMainActivity();
                                dataManager.setRestoringMessageShown(true);
                            }
                        });
                }

                @Override
                public void isNotRestoring() {
                    abrirMainActivity();
                    dataManager.setRestoringMessageShown(true);
                }
            });
        }
    }

    private void abrirMainActivity() {
        if (!isPaused && timeHasPassed) {
            //            final Intent intent = new Intent(SplashScreen.this, MenuFragment.class);
            //            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            final Intent intent = new Intent(this, NavigationActivity.class);
            startActivity(intent);
        }
    }

    private void setTypefaces() {
        final Typeface typeface = TypefacesUtils.get(this, "fonts/DejaVuSansCondensed.ttf");
        binding.tvTitulo.setTypeface(typeface);
    }
}
