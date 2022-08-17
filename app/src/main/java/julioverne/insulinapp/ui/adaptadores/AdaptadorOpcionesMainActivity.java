package julioverne.insulinapp.ui.adaptadores;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;
import julioverne.insulinapp.R;
import julioverne.insulinapp.constants.Constants;
import julioverne.insulinapp.utils.TypefacesUtils;

/**
 * Created by Juan José Melero on 20/06/2015.
 */
public class AdaptadorOpcionesMainActivity extends ArrayAdapter<String> {

    private Activity context;
    private String[] datos;
    private int resource;
    private ListView parent;
    private Typeface typeface;

    public AdaptadorOpcionesMainActivity(Context context, int resource, int textViewResourceId, String[] datos) {
        super(context, textViewResourceId, datos);
        this.context = (Activity) context;
        this.datos = datos;
        this.resource = resource;
        typeface = TypefacesUtils.get(context, "fonts/DejaVuSansCondensed.ttf");
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

        if (this.parent == null) {
            this.parent = (ListView) parent;
        }

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
        Drawable drawable;

        switch (getItem(position)) {
            case Constants.REALIZAR_CONTROL:
                drawable = context.getResources().getDrawable(R.mipmap.ic_control);
                break;
            case Constants.DIARIO:
                drawable = context.getResources().getDrawable(R.mipmap.ic_diario);
                break;
            case Constants.ALIMENTOS:
                drawable = context.getResources().getDrawable(R.mipmap.ic_alimentos);
                break;
            case Constants.INSULINA_ACTUAL:
                drawable = context.getResources().getDrawable(R.mipmap.ic_actual);
                break;
            case Constants.ALARMAS:
            default:
                drawable = context.getResources().getDrawable(R.mipmap.ic_alarma);
                break;
        }

        holder.ivMiniatura.setImageDrawable(drawable);
        holder.ivMiniatura.setVisibility(View.VISIBLE);

        return item;
    }
}
