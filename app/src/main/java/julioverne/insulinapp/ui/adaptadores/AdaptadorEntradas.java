package julioverne.insulinapp.ui.adaptadores;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java.util.List;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.DataManager;
import julioverne.insulinapp.data.SharedPreferencesManager;
import julioverne.insulinapp.data.dao.EntradaDAO;
import julioverne.insulinapp.utils.DateUtils;
import julioverne.insulinapp.utils.TypefacesUtils;

public class AdaptadorEntradas extends RecyclerView.Adapter<AdaptadorEntradas.EntradaViewHolder> {

  private static final int LAYOUT = R.layout.item_entrada_layout;
  private final Typeface light;
  private final Typeface negrita;
  private final List<EntradaDAO> mDatos;
  private final Context context;
  private final DataManager dataManager;
  private OnItemClickListener itemClickListener;
  private View selectedItem;

  public AdaptadorEntradas(Context context, List<EntradaDAO> entradas) {
    this.dataManager = DataManager.getInstance(context);
    this.mDatos = entradas;
    this.context = context;
    light = TypefacesUtils.get(context, "fonts/DejaVuSans-ExtraLight.ttf");
    negrita = TypefacesUtils.get(context, "fonts/DejaVuSansCondensed-Bold.ttf");
  }

  @Override
  public EntradaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new EntradaViewHolder(LayoutInflater.from(context).inflate(LAYOUT, parent, false));
  }

  @Override
  public void onBindViewHolder(EntradaViewHolder holder, int position) {
    holder.loadView(mDatos.get(position));
  }

  @Override
  public int getItemCount() {
    return mDatos.size();
  }

  public void deseleccionarItem() {
    selectedItem.setSelected(false);
    selectedItem = null;
  }

  public void setOnItemClickListener(OnItemClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
  }

  public interface OnItemClickListener {
    void onItemClicked(EntradaDAO entrada, int posicion);
  }

  class EntradaViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.fl_root)
    FrameLayout flRoot;
    @BindView(R.id.ll_selector)
    LinearLayout llSelector;
    @BindView(R.id.tv_fecha_control)
    TextView tvFechaControl;
    @BindView(R.id.tv_hora_control)
    TextView tvHoraControl;
    @BindView(R.id.tv_glucosa_sangre)
    TextView tvGlucosaSangre;
    @BindView(R.id.tv_unidades_dosis)
    TextView tvUnidadesDosis;
    @BindView(R.id.iv_manzanita)
    ImageView ivManzanita;
    @BindView(R.id.tv_mgdl)
    TextView tvMgdl;
    @BindView(R.id.tv_uds)
    TextView tvUds;

    EntradaViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    //Llena los campos
    @SuppressLint("SetTextI18n")
    void loadView(EntradaDAO entrada) {

      //Tipografía
      tvHoraControl.setTypeface(light);
      tvFechaControl.setTypeface(light);
      tvGlucosaSangre.setTypeface(negrita);
      tvUnidadesDosis.setTypeface(negrita);
      tvMgdl.setTypeface(light);
      tvUds.setTypeface(light);

      tvFechaControl.setText(DateUtils.dateToString(entrada.getFecha()));
      tvHoraControl.setText(DateUtils.dateToHour(entrada.getFecha()));
      tvGlucosaSangre.setText(entrada.getGlucosaSangre().toString());

      //Tiñe la fila de un color rojo si la cantidad de glucosa era incorrecta
      if (entrada.getGlucosaSangre() < Integer.parseInt(
          dataManager.getPreferencia(SharedPreferencesManager.GLUCOSA_MINIMA))) {
        flRoot.setBackgroundResource(R.drawable.selector_degradado_inferior);
      } else if (entrada.getGlucosaSangre() > Integer.parseInt(
          dataManager.getPreferencia(SharedPreferencesManager.GLUCOSA_MAXIMA))) {
        flRoot.setBackgroundResource(R.drawable.selector_degradado_superior);
      } else {
        flRoot.setBackgroundResource(R.drawable.selector_degradado_correcto);
      }

      tvUnidadesDosis.setText(entrada.getInsulinaDosis().toString());

      //Muestra la manzanita según si fue antes o después de comer
      ivManzanita.setImageDrawable(ContextCompat.getDrawable(context, entrada.isAntesDeComer() ?
          R.mipmap.ic_manzanita : R.mipmap.ic_manzanita_mordida));
    }

    @OnClick(R.id.ll_selector)
    public void onClick() {
      //Al hacer click, selecciona el item y muestra un dialog
      llSelector.setSelected(true);
      selectedItem = llSelector;
      itemClickListener.onItemClicked(mDatos.get(getAdapterPosition()), getAdapterPosition());
    }
  }
}
