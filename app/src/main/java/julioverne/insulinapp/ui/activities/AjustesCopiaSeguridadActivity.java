package julioverne.insulinapp.ui.activities;

import android.app.FragmentManager;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.DataManager;
import julioverne.insulinapp.databinding.ConfiguracionCopiaSeguridadBinding;
import julioverne.insulinapp.ui.fragments.BackUpSettingsFragment;

public class AjustesCopiaSeguridadActivity extends AppCompatActivity {

    @BindView(R.id.btn_volver)
    Button btnVolver;

    private DataManager dataManager;
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfiguracionCopiaSeguridadBinding binding =
            DataBindingUtil.setContentView(this, R.layout.configuracion_copia_seguridad);
        unbinder = ButterKnife.bind(this);
        loadView();
    }

    public void loadView() {

        dataManager = DataManager.getInstance(this);

        //Tipografía
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
            .replace(R.id.fragment, new BackUpSettingsFragment())
            .commit();
    }

    @OnClick(R.id.btn_volver)
    public void volver() {
        onBackPressed();
        dataManager.establecerCopiaSeguridadAutomatica();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
