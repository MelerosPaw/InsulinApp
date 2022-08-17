package julioverne.insulinapp.ui.activities;

import android.app.FragmentManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import julioverne.insulinapp.R;
import julioverne.insulinapp.ui.fragments.ConfiguracionFragment;
import julioverne.insulinapp.utils.TypefacesUtils;

public class ConfiguracionActivity extends AppCompatActivity {

    @BindView(R.id.btn_volver)
    Button btnVolver;
    @BindView(R.id.tv_mensaje_superior)
    TextView tvMensajeSuperior;
    @BindView(R.id.tv_titulo_configuracion)
    TextView tvTituloConfiguracion;

    private Typeface light;
    private Typeface negrita;
    private Typeface cursiva;
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuracion_layout);
        unbinder = ButterKnife.bind(this);

        light = TypefacesUtils.get(this, "fonts/DejaVuSans-ExtraLight.ttf");
        negrita = TypefacesUtils.get(this, "fonts/DejaVuSansCondensed-Bold.ttf");
        cursiva = TypefacesUtils.get(this, "fonts/DejaVuSansCondensed-Oblique.ttf");

        //Tipografía
        tvTituloConfiguracion.setTypeface(negrita);
        tvMensajeSuperior.setTypeface(cursiva);
        btnVolver.setTypeface(negrita);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
            .replace(R.id.fragment, new ConfiguracionFragment())
            .commit();
    }

    @OnClick(R.id.btn_volver)
    public void volver() {
        onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
