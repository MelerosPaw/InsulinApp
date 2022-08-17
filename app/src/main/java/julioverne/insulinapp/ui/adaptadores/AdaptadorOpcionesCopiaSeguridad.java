package julioverne.insulinapp.ui.adaptadores;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import julioverne.insulinapp.R;
import julioverne.insulinapp.constants.Constants;
import julioverne.insulinapp.utils.StringUtils;
import julioverne.insulinapp.utils.TypefacesUtils;

/**
 * Created by Juan José Melero on 20/06/2015.
 */
public class AdaptadorOpcionesCopiaSeguridad extends ArrayAdapter<String> {

    private Activity context;
    private String[] datos;
    private int resource;
    private boolean hayCopia;
    private Typeface typeface;
    private SparseBooleanArray enabled;

    public interface EnableCallback {
        void setEnabled(boolean enabled);
    }

    public AdaptadorOpcionesCopiaSeguridad(Context context, int resource, int textViewResourceId,
        String[] datos, boolean hayCopia) {
        super(context, textViewResourceId, datos);
        this.context = (Activity) context;
        this.datos = datos;
        this.resource = resource;
        this.hayCopia = hayCopia;
        this.typeface = TypefacesUtils.get(context, "fonts/DejaVuSansCondensed.ttf");
        this.enabled = new SparseBooleanArray();
    }

    class ViewHolder {

        @BindView(R.id.iv_miniatura)
        ImageView ivMiniatura;
        @BindView(R.id.tv_opcion)
        TextView tvOpcion;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        View item = convertView;
        final ViewHolder holder;

        if (item == null) {
            item = context.getLayoutInflater().inflate(resource, null);
            holder = new ViewHolder(item);
            item.setTag(holder);
        } else {
            holder = (ViewHolder) item.getTag();
        }

        holder.tvOpcion.setTypeface(typeface);
        holder.tvOpcion.setText(datos[position]);

        String opcion = StringUtils.getEmptyIfNull(getItem(position));
        @DrawableRes int drawableRes;

        switch (opcion) {
            case Constants.GUARDAR_COPIA:
                drawableRes = R.mipmap.ic_guardar;
                break;
            case Constants.RESTAURAR_COPIA:
                drawableRes = R.mipmap.ic_restaurar;
                break;
            case Constants.ELIMINAR_COPIA:
            default:
                drawableRes = R.mipmap.ic_alarma;
                break;
        }

        holder.ivMiniatura.setImageDrawable(ContextCompat.getDrawable(context, drawableRes));
        holder.ivMiniatura.setVisibility(View.VISIBLE);

        if (position == 0) {
            item.setEnabled(true);
            enabled.put(position, true);
        } else {
            habilitarItem(item, holder);
            enabled.put(position, hayCopia);
        }

        return item;
    }

    public void habilitarItem(View item, ViewHolder holder) {
        item.setEnabled(hayCopia);
        holder.tvOpcion.setEnabled(hayCopia);
        holder.ivMiniatura.setEnabled(hayCopia);
    }

    public void setHayCopia(boolean hayCopia) {
        this.hayCopia = hayCopia;
    }

    public boolean isItemEnabled(int position) {
        return enabled.get(position);
    }
}
