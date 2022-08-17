package julioverne.insulinapp.ui.base;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import julioverne.insulinapp.data.DataManager;
import julioverne.insulinapp.extensions.FragmentExtensions;
import julioverne.insulinapp.ui.callbacks.BooleanCallback;
import julioverne.insulinapp.utils.TypefacesUtils;

/**
 * Created by Juan José Melero on 19/06/2015.
 */
public class BaseFragment extends Fragment {

    protected Typeface light;
    protected Typeface negrita;
    protected Typeface cursiva;
    protected DataManager dataManager;
    protected ViewDataBinding baseBinding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dataManager = DataManager.getInstance(getContext());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        light = TypefacesUtils.get(getActivity(), "fonts/DejaVuSans-ExtraLight.ttf");
        negrita = TypefacesUtils.get(getActivity(), "fonts/DejaVuSansCondensed-Bold.ttf");
        cursiva = TypefacesUtils.get(getActivity(), "fonts/DejaVuSansCondensed-Oblique.ttf");
    }

    /**
     * Muestra un {@link AlertDialog} que pregunta al usuario si desea salir realmente.
     * El {@code callback} devuelve la respuesta en forma de booleano.
     */
    public void preguntarAlSalir(final BooleanCallback booleanCallback) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Salir sin guardar")
            .setMessage("¿Quieres salir sin haber guardado el resultado del control?")
            .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    booleanCallback.onCompleted(true);
                    dialog.dismiss();
                }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    booleanCallback.onCompleted(false);
                    dialog.dismiss();
                }
            })
            .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbind();
    }

    private void unbind() {
        if (baseBinding != null) {
            baseBinding.unbind();
        }
    }

    protected void toast(@NonNull final String text, final int duration) {
        FragmentExtensions.toast(this, text, duration);
    }

    protected void toast(@StringRes final int stringId, final int duration) {
        FragmentExtensions.toast(this, stringId, duration);
    }

    protected void toast(@NonNull final String text) {
        FragmentExtensions.toast(this, text);
    }

    protected void longToast(@NonNull final String text) {
        FragmentExtensions.longToast(this, text);
    }

    protected void toast(@StringRes final int stringId) {
        FragmentExtensions.toast(this, stringId);
    }
}
