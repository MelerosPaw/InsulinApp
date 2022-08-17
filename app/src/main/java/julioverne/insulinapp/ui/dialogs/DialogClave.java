package julioverne.insulinapp.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import julioverne.insulinapp.R;
import julioverne.insulinapp.ui.activities.ConfiguracionActivity;
import julioverne.insulinapp.utils.StringUtils;

/**
 * Created by Juan José Melero on 20/06/2015.
 */
public class DialogClave extends Dialog {

    @BindView(R.id.et_clave)
    EditText etClave;
    @BindView(R.id.btn_aceptar)
    Button btnAceptar;
    @BindView(R.id.btn_cancelar)
    Button btnCancelar;

    private Unbinder unbinder;

    public DialogClave(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_clave_layout);
        unbinder = ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_cancelar)
    public void cancelar() {
        dismiss();
    }

    //Comprueba que la contraseña se acorrecta
    @OnClick(R.id.btn_aceptar)
    public void aceptar() {

        String clave = etClave.getText().toString();

        //Si no es correcta muestra un mensaje
        if (!clave.equals(getContext().getString(R.string.clave))) {
            etClave.setText("");
            StringUtils.toastCorto(getContext(), R.string.texto_clave_incorrecta);
            //Si es correcta, abre la Activity de configuración
        } else {
            Intent intent = new Intent(getContext(), ConfiguracionActivity.class);
            getContext().startActivity(intent);
            dismiss();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //        unbinder.unbind();
    }
}
