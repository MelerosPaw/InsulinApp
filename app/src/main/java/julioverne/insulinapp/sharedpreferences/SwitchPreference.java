package julioverne.insulinapp.sharedpreferences;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.DataManager;

/**
 * Created by Juan José Melero on 01/06/2015.
 */
public class SwitchPreference extends Preference {

    @BindView(R.id.tv_titulo)
    TextView tvTitulo;
    @BindView(R.id.tv_summary)
    TextView tvSummary;
    @BindView(R.id.sw_value)
    Switch swValue;

    private DataManager dataManager;

    public SwitchPreference(Context context) {
        super(context);
    }

    public SwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        ButterKnife.bind(this, view);
        dataManager = DataManager.getInstance(getContext());
        loadView();
    }

    public void loadView() {
        tvTitulo.setText(getTitle());
        tvSummary.setText(getSummary());
        swValue.setChecked(dataManager.isCopiaSeguridadActiva());
    }

    @OnCheckedChanged(R.id.sw_value)
    public void enable(boolean isChecked) {
        dataManager.activarCopiaSeguridad(isChecked);
    }
}
