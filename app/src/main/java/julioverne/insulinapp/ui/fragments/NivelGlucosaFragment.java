package julioverne.insulinapp.ui.fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnFocusChange;
import julioverne.insulinapp.R;
import julioverne.insulinapp.extensions.FragmentExtensions;
import julioverne.insulinapp.ui.base.BaseFragment;
import julioverne.insulinapp.utils.TecladoUtil;

/**
 * Created by Juan José Melero on 19/06/2015.
 */
public class NivelGlucosaFragment extends BaseFragment {

    public interface OnGlucoseInputListener {
        void onContinue(@NonNull String glucoseAmount);
    }

    public static final String TAG = NivelGlucosaFragment.class.getSimpleName();
    public static final String BUNDLE_GLUCOSA = "BUNDLE_GLUCOSA";

    @BindView(R.id.tv_titulo_glucosa)
    TextView tvTituloGlucosa;
    @BindView(R.id.et_glucosa_sangre)
    EditText etGlucosaSangre;
    @BindView(R.id.tv_mgdl)
    TextView tvMgdl;

    /**
     * Crea una nueva instancia de la clase inicializando sus parámetros.
     *
     * @param nivelGlucosa Número entero que aparecerá en el {@code EditText} cuando se inicie
     * el {@code Fragment}.
     * @return Instancia del {@code Fragment}.
     */
    public static NivelGlucosaFragment getInstance(Integer nivelGlucosa) {
        NivelGlucosaFragment f = new NivelGlucosaFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_GLUCOSA, nivelGlucosa);
        f.setArguments(bundle);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nivel_glucosa_layout, container, false);
        ButterKnife.bind(this, view);

        tvTituloGlucosa.setTypeface(light);
        etGlucosaSangre.setTypeface(negrita);
        tvMgdl.setTypeface(light);

        //Carga la glucosa recibida en el EditText
        int glucosaSangre = getArguments().getInt(BUNDLE_GLUCOSA);

        if (glucosaSangre != 0) {
            etGlucosaSangre.setText(String.valueOf(glucosaSangre));
        }

        etGlucosaSangre.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    final OnGlucoseInputListener listener =
                        FragmentExtensions.getInvokingClassAs(
                            NivelGlucosaFragment.this,
                            OnGlucoseInputListener.class);

                    if (listener != null) {
                        listener.onContinue(getGlucosaSangre());
                        return true;
                    }
                }

                return false;
            }
        });

        return view;
    }

    /** Devuelve la cantidad de glucosa que se ha introducido como resultado del control */
    public String getGlucosaSangre() {
        return etGlucosaSangre.getText().toString();
    }

    @OnFocusChange(R.id.et_glucosa_sangre)
    public void focusChanged() {
        TecladoUtil.keyboardToAdjustPan(getActivity());
    }
}
