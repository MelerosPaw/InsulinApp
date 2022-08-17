package julioverne.insulinapp.sharedpreferences;

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.DataManager;
import julioverne.insulinapp.data.SharedPreferencesManager;
import julioverne.insulinapp.utils.StringUtils;

/**
 * Created by Juan José Melero on 31/05/2015.
 */
public class ExtendedEditTextPreference extends EditTextPreference {

    private TextView tv_titulo;
    private TextView tv_summary;
    private RelativeLayout rl_valorActual;
    private TextView tv_valorActual;

    private TextView tv_message;
    private EditText et_cantidad;

    private boolean previoAlmacenado;
    private DataManager dataManager;

    public ExtendedEditTextPreference(Context context) {
        super(context);
        initialize(context);
    }

    public ExtendedEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public ExtendedEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public void initialize(Context context) {
        dataManager = DataManager.getInstance(context);
    }

    @Override
    protected void onBindView(View view) {
        setPersistent(false);
        super.onBindView(view);

        tv_titulo = (TextView) view.findViewById(R.id.tv_titulo);
        tv_summary = (TextView) view.findViewById(R.id.tv_summary);
        rl_valorActual = (RelativeLayout) view.findViewById(R.id.rl_valor_actual);
        tv_valorActual = (TextView) view.findViewById(R.id.tv_valor_actual);

        loadView();
    }

    public void loadView() {
        tv_titulo.setText(getTitle());
        tv_summary.setText(getSummary());
        String valorActual = dataManager.getPreferencia(getKey());

        if (!valorActual.equals("")) {
            previoAlmacenado = true;
            switch (getKey()) {
                case SharedPreferencesManager.FACTOR_SENSIBILIDAD:
                    valorActual += "mg/dl";
                    break;
                case SharedPreferencesManager.GLUCOSA_MINIMA:
                    valorActual += "mg/dl";
                    break;
                case SharedPreferencesManager.GLUCOSA_MAXIMA:
                    valorActual += "mg/dl";
                    break;
            }
            tv_valorActual.setText(valorActual);
            rl_valorActual.setVisibility(View.VISIBLE);
        } else {
            rl_valorActual.setVisibility(View.GONE);
        }
    }

    //Cambia los botones de aceptar y cancelar del dialog
    @Override
    protected void showDialog(Bundle state) {
        setPositiveButtonText(R.string.dialog_aceptar);
        setNegativeButtonText(R.string.cancelar);
        super.showDialog(state);
    }

    //Gestiona las vistas del nuestro Dialog personalizado
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        et_cantidad = (EditText) view.findViewById(R.id.et_cantidad);
        tv_message = (TextView) view.findViewById(R.id.tv_message);

        tv_message.setText(getDialogMessage());

        if (previoAlmacenado) {
            et_cantidad.setText(dataManager.getPreferencia(getKey()));
        } else {
            et_cantidad.setText("");
        }
    }

    //Si no se asigna un valor correcto, no se guarda el valor y se informa al usuario con un Toast
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        boolean correcto = false;
        String valorNuevo = et_cantidad.getText().toString();

        //Si se ha indicado un valor, se comprueba que sea correcto
        if (positiveResult && !valorNuevo.equals("")) {
            switch (getKey()) {
                case SharedPreferencesManager.FACTOR_SENSIBILIDAD:
                    correcto = isNumero(valorNuevo);
                    break;
                case SharedPreferencesManager.GLUCOSA_MAXIMA:
                    correcto = isNumero(valorNuevo) && !isInferiorAMinima(valorNuevo);
                    break;
                case SharedPreferencesManager.GLUCOSA_MINIMA:
                    correcto = isNumero(valorNuevo) && !isSuperiorAMaxima(valorNuevo);
                    break;
            }
            //Si no se ha indicado valor, se deja la casilla en blaco o
        } else if (valorNuevo.equals("")) {
            if (previoAlmacenado) {
                StringUtils.toastLargo(getContext(), R.string.advertencia_sin_valor);
                et_cantidad.setText(dataManager.getPreferencia(getKey()));
            }
            return;
        }

        if (correcto) {
            dataManager.setPreference(getKey(), valorNuevo);
        } else {
            String valorActual = dataManager.getPreferencia(getKey());
            et_cantidad.setText(valorActual);
        }
    }

    //Comprueba que la cantidad máxima de glucosa introducida no sea menor que la cantidad mínima
    //si la hay
    private boolean isInferiorAMinima(String valor) {
        String glucosaMinima = dataManager.getPreferencia(SharedPreferencesManager.GLUCOSA_MINIMA);
        if (!TextUtils.isEmpty(glucosaMinima)) {
            int gmin = Integer.parseInt(glucosaMinima);
            int gmax = Integer.parseInt(valor);
            if (gmax <= gmin) {
                StringUtils.toastCorto(getContext(), R.string.advertencia_maxima_incorrecta);
                return true;
            }
        }

        return false;
    }

    //Comprueba que la cantidad mínima de glucosa introducida no sea mayor que la cantidad máxima
    // si la hay
    private boolean isSuperiorAMaxima(String valor) {
        String glucosaMaxima = dataManager.getPreferencia(SharedPreferencesManager.GLUCOSA_MAXIMA);
        if (!TextUtils.isEmpty(glucosaMaxima)) {
            int gmax = Integer.parseInt(glucosaMaxima);
            int gmin = Integer.parseInt(valor);
            if (gmin >= gmax) {
                StringUtils.toastCorto(getContext(), R.string.advertencia_minima_incorrecta);
                return true;
            }
        }

        return false;
    }

    public boolean isNumero(String valor) {
        try {
            Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            StringUtils.toastLargo(getContext(), "Tienes que introducir un número entero.");
            return false;
        }

        return true;
    }
}

