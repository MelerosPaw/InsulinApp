package julioverne.insulinapp.ui.dialogs;

import android.os.Bundle;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.OnClick;
import julioverne.insulinapp.R;

/**
 * Created by Juan José Melero on 14/12/2016.
 */

public class OrigenImagenDialog extends AnywhereDialog {

    public static final int DESDE_CAMARA = 1;
    public static final int DESDE_ARCHIVO = 2;

    protected OrigenImagenListener listener;

    public OrigenImagenDialog(View view, OrigenImagenListener origenImagenListener) {
        super(view);
        this.listener = origenImagenListener;
    }

    @Override
    public int getLayout() {
        return R.layout.anywhere_dialog_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
    }

    @OnClick({
        R.id.tv_desde_camara,
        R.id.tv_desde_archivo
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_desde_camara:
                listener.onOptionSelected(DESDE_CAMARA);
                dismiss();
                break;
            case R.id.tv_desde_archivo:
                listener.onOptionSelected(DESDE_ARCHIVO);
                dismiss();
                break;
        }
    }

    /**
     * Interface to provide a callback to the activity.
     */
    public interface OrigenImagenListener {
        void onOptionSelected(int desde);
    }
}
