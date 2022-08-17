package julioverne.insulinapp.ui.adaptadores;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.List;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.DataManager;
import julioverne.insulinapp.data.dao.PeriodoDAO;
import julioverne.insulinapp.utils.ComparaPeriodos;
import julioverne.insulinapp.utils.TimeUtils;
import julioverne.insulinapp.utils.TypefacesUtils;

/**
 * Created by Juan José Melero on 26/04/2015.
 */
public class AdaptadorPeriodos extends ArrayAdapter<PeriodoDAO> {

    private List<PeriodoDAO> datos;
    private Context context;
    private DataManager dataManager;
    private int resource;
    private ListView listView;
    private final Typeface light;

    public AdaptadorPeriodos(Activity context, int resource, List<PeriodoDAO> periodos) {
        super(context, resource, periodos);
        this.context = context;
        this.dataManager = DataManager.getInstance(context);
        this.datos = periodos;
        this.resource = resource;
        light = TypefacesUtils.get(context, "fonts/DejaVuSans-ExtraLight.ttf");
    }

    class ViewHolder {

        @BindView(R.id.tv_hora_inicio)
        TextView tvHoraInicio;
        @BindView(R.id.tv_horaFin)
        TextView tvHoraFin;
        @BindView(R.id.tv_unidadesRacion)
        TextView tvUnidadesRacion;
        @BindView(R.id.tv_uds)
        TextView tvUds;
        @BindView(R.id.iv_eliminar)
        ImageView ivEliminar;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (this.listView == null) {
            this.listView = (ListView) parent;
        }

        View item = convertView;
        final ViewHolder holder;

        if (item == null) {
            item = LayoutInflater.from(context).inflate(resource, null);
            holder = new ViewHolder(item);
            item.setTag(holder);
        } else {
            holder = (ViewHolder) item.getTag();
        }

        //Tipografias

        holder.tvHoraFin.setTypeface(light);
        holder.tvHoraInicio.setTypeface(light);
        holder.tvUnidadesRacion.setTypeface(light);
        holder.tvUds.setTypeface(light);

        final PeriodoDAO periodo = datos.get(position);
        //Si los minutos son 0, se mostrará 00 para el formato de la hora
        String minutosInicio = TimeUtils.ceroCero(periodo.getMinutosInicio());
        String minutosFin = TimeUtils.ceroCero(datos.get(position).getMinutosFin());
        holder.tvHoraInicio.setText(datos.get(position).getHoraInicio() + ":" + minutosInicio);
        holder.tvHoraFin.setText(datos.get(position).getHoraFin() + ":" + minutosFin);
        holder.tvUnidadesRacion.setText(String.valueOf(datos.get(position).getUnidadesInsulina()));

        //Listener para btnEliminar: elimina el periodo de la lista
        holder.ivEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View item = (View) v.getParent();
                int position = listView.getPositionForView(item);
                boolean eliminado = dataManager.eliminarPeriodo(periodo);
                if (eliminado) {
                    remove((PeriodoDAO) listView.getItemAtPosition(position));
                    notifyDataSetChanged();
                }
            }
        });

        return item;
    }

    public List<PeriodoDAO> getDatos() {
        return datos;
    }

    @Override
    public void add(PeriodoDAO object) {
        super.add(object);
        sort(new ComparaPeriodos());
        notifyDataSetChanged();
    }
}
