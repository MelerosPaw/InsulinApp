package julioverne.insulinapp.ui.adaptadores;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.PluralsRes;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.DataManager;
import julioverne.insulinapp.data.LastUpdateKey;
import julioverne.insulinapp.data.dao.AlimentoDAO;
import julioverne.insulinapp.extensions.ViewHolderEXT;
import julioverne.insulinapp.utils.AnimationUtil;
import julioverne.insulinapp.utils.DecimalFormatUtils;
import julioverne.insulinapp.utils.TypefacesUtils;
import melerospaw.memoryutil.MemoryUtil;

public class AdaptadorAlimentos extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  public interface OnEditarAlimentoListener {
    void editar(AlimentoDAO alimento, int posicion);
  }

  public interface OnRedimensionarTarjeta {
    boolean puedeRedimensionar();
  }

  public interface OnSeleccionarAlimento {
    void modoSeleccion(boolean activado);
  }

  private final Typeface negrita;
  private final Typeface light;
  private DataManager dataManager;
  private List<AlimentoDAO> currentList;
  private List<AlimentoDAO> incluidos;
  private Context context;
  private boolean isForResult;
  private OnEditarAlimentoListener onEditarAlimentoListener;
  private OnRedimensionarTarjeta onRedimensionarTarjeta;
  private OnSeleccionarAlimento onSeleccionarAlimento;
  private ModoVisualizacion modoVisualizacion;
  @NonNull
  private List<AlimentoDAO> seleccionados = new LinkedList<>();

  public AdaptadorAlimentos(Context context, List<AlimentoDAO> alimentos,
      List<AlimentoDAO> incluidos, boolean isForResult) {
    this.light = TypefacesUtils.get(context, "fonts/DejaVuSans-ExtraLight.ttf");
    this.negrita = TypefacesUtils.get(context, "fonts/DejaVuSansCondensed-Bold.ttf");
    this.currentList = new ArrayList<>(alimentos);
    //        transform(listaOriginal);

    this.incluidos = incluidos;
    this.context = context;
    this.dataManager = DataManager.getInstance(context);
    this.isForResult = isForResult;
    this.modoVisualizacion = dataManager.getModoVisualizacion();
  }

  private void transform(@NonNull final List<AlimentoDAO> al) {
    for (AlimentoDAO a : al) {
      a.setHosting(true);
    }
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    final int layoutRes = viewType == 1 ? R.layout.subitem_tarjeta : R.layout.subitem_lista;
    final View v = LayoutInflater.from(context).inflate(layoutRes, parent, false);
    return viewType == 1 ? new ViewHolderTarjeta(v) : new ViewHolderLista(v);
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    if (getItemViewType(position) == ModoVisualizacion.TARJETAS.ordinal()) {
      ((ViewHolderTarjeta) holder).cargarVista(currentList.get(position));
    } else {
      ((ViewHolderLista) holder).cargarVista(currentList.get(position));
    }
  }

  @Override
  public int getItemCount() {
    return currentList.size();
  }

  @Override
  public int getItemViewType(int position) {
    return modoVisualizacion.ordinal();
  }

  @Override
  public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
    super.onViewRecycled(holder);

    if (holder instanceof ViewHolderTarjeta) {
      ((ViewHolderTarjeta) holder).unbindImage();
    } else {
      ((ViewHolderLista) holder).unbindImage();
    }
  }

  public void swapItems(@NonNull List<AlimentoDAO> newList) {
    final FoodDiffUtilCallback callback = new FoodDiffUtilCallback(currentList, newList);
    final DiffUtil.DiffResult changes = DiffUtil.calculateDiff(callback);
    currentList = newList;
    changes.dispatchUpdatesTo(this);
  }

  public void addAlimentos(List<AlimentoDAO> alimentos) {
    int nuevos = 0;
    int actualizados = 0;
    for (AlimentoDAO alimentoNuevo : alimentos) {
      if (!currentList.contains(alimentoNuevo)) {
        currentList.add(alimentoNuevo);
        nuevos++;
      } else {
        AlimentoDAO alimentoAntiguo = currentList.get(currentList.indexOf(alimentoNuevo));
        if (!alimentoAntiguo.getCantidadUnidades().equals(alimentoNuevo.getCantidadUnidades())
            || !alimentoAntiguo.getRacionesUnidad().equals(alimentoNuevo.getRacionesUnidad())
            || !alimentoAntiguo.getUnidadMedida().equals(alimentoNuevo.getUnidadMedida())) {
          currentList.set(currentList.indexOf(alimentoAntiguo), alimentoNuevo);
          actualizados++;
        }
      }
    }

    String mensaje;

    if (actualizados > 0 && nuevos > 0) {
      mensaje = "Alimentos nuevos: " + nuevos + "\nAlimentos actualizados: " + actualizados;
    } else if (actualizados > 0) {
      mensaje = context.getResources()
          .getQuantityString(R.plurals.advertencia_alimentos_actualizados, actualizados, actualizados);
    } else {
      mensaje = context.getResources().getQuantityString(R.plurals.advertencia_alimentos_nuevos, nuevos, nuevos);
    }

    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
  }

  public void nuevoAlimento(@NonNull final AlimentoDAO alimento, final int posicion) {
    if (posicion == -1) {
      insertaAlimento(alimento);
    } else {
      actualizarAlimento(alimento, posicion);
    }
  }

  public List<AlimentoDAO>[] obtenerMarcadosParaEliminar() {

    List<AlimentoDAO> alimentosHosting = new LinkedList<>();
    List<AlimentoDAO> alimentosUsuario = new LinkedList<>();
    clasificarAlimentos(alimentosHosting, alimentosUsuario);
    return new List[] {
        alimentosHosting,
        alimentosUsuario
    };
  }

  // Devuelve una lista de alimentos seleccionados y las cantidades a 0
  @NonNull
  public List<AlimentoDAO> getSeleccionados() {
    return seleccionados;
  }

  public void deseleccionarTodos() {
    for (int i = 0; i < currentList.size(); i++) {
      final AlimentoDAO alimento = currentList.get(i);
      if (alimentoEstaSeleccionado(alimento)) {
        seleccionarAlimento(alimento, false);
        notifyItemChanged(i);
      }
    }
  }

  public void eliminarBorrados(List<AlimentoDAO> borrados) {
    for (AlimentoDAO alimento : borrados) {
      int posicion = currentList.indexOf(alimento);
      currentList.remove(alimento);
      notifyItemRemoved(posicion);
    }
  }

  public void cambioModo(ModoVisualizacion nuevoModo) {
    modoVisualizacion = nuevoModo;
    notifyItemRangeChanged(0, currentList.size());
  }

  public void setOnEditarAlimento(OnEditarAlimentoListener onEditarAlimentoListener) {
    this.onEditarAlimentoListener = onEditarAlimentoListener;
  }

  public void setOnRedimensionarTarjeta(OnRedimensionarTarjeta onRedimensionarTarjeta) {
    this.onRedimensionarTarjeta = onRedimensionarTarjeta;
  }

  public void setOnSeleccionarAlimento(OnSeleccionarAlimento onSeleccionarAlimento) {
    this.onSeleccionarAlimento = onSeleccionarAlimento;
  }

  private boolean haySeleccionados() {
    return !seleccionados.isEmpty();
  }

  private void insertaAlimento(AlimentoDAO alimento) {
    currentList.add(alimento);
    ordenarListas();
    notifyItemInserted(currentList.indexOf(alimento));
  }

  private void actualizarAlimento(@NonNull final AlimentoDAO alimento, int posicionOriginal) {
    final int posicionNueva = currentList.indexOf(alimento);
    currentList.set(posicionNueva, alimento);
    ordenarListas();

    if (posicionNueva != posicionOriginal) {
      notifyItemMoved(posicionOriginal, posicionNueva);
    }

    notifyItemChanged(posicionNueva);
  }

  private void ordenarListas() {
    Collections.sort(currentList, AlimentoDAO.COMPARATOR);
  }

  private void clasificarAlimentos(List<AlimentoDAO> alimentosHosting,
      List<AlimentoDAO> alimentosUsuario) {
    for (AlimentoDAO a : currentList) {

      if (alimentoEstaSeleccionado(a)) {
        if (a.isFromHosting()) {
          alimentosHosting.add(a);
        } else {
          alimentosUsuario.add(a);
        }
      }
    }
  }

  private boolean alimentoEstaSeleccionado(AlimentoDAO a) {
    return seleccionados.contains(a);
  }

  private void cargarImagen(AlimentoDAO alimento, final ImageView vista) {
    if (alimento.isFromHosting()) {
      Glide.with(context)
          //                    .asBitmap()
          .load(DataManager.IMAGES_URL + alimento.getImagen())
          //                    .listener(new RequestListener<Bitmap>() {
          //                        @Override
          //                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
          //                                                    Target<Bitmap> target, boolean isFirstResource) {
          //                            vista.setImageDrawable(ContextCompat.getDrawable(context,
          //                                    android.R.drawable.stat_notify_error));
          //                            return true;
          //                        }
          //
          //                        @Override
          //                        public boolean onResourceReady(Bitmap resource, Object model,
          //                                                       Target<Bitmap> target, DataSource dataSource,
          //                                                       boolean isFirstResource) {
          //                            vista.setImageBitmap(resource);
          //                            return true;
          //                        }
          //                    })
          .into(vista);
      //                dataManager.cargarImagenAlimento(mAlimento.getImagen(), new VolleyBitmapCallback() {
      //                    @Override
      //                    public void onSuccess(Bitmap bitmap) {
      //                        if (ivFotoAlimento != null) {
      //                            ivFotoAlimento.setImageBitmap(bitmap);
      //                        }
      //                    }
      //
      //                    @Override
      //                    public void onFailure(VolleyError volleyError) {
      //                        if (ivFotoAlimento != null) {
      //                            putPlaceHolder();
      //                        }
      //                    }
      //                });
    } else {
      final File imageFile = dataManager.getImagesDirectory()
          .file(alimento.getImagen())
          .build()
          .getFile();
      if (MemoryUtil.exists(imageFile)) {
        Glide.with(context)
            .load(imageFile).asBitmap()
            .signature(new LastUpdateKey(imageFile.lastModified()))
            .into(vista);
      } else {
        putPlaceHolder(vista);
      }
    }
  }

  private void putPlaceHolder(ImageView vista) {
    Glide.with(context)
        .load(android.R.drawable.stat_notify_error)
        .into(vista);
  }

  private void loadRoot(View fondo, final AlimentoDAO alimento) {
    setEnabled(fondo, alimento);
    colorearFondo(fondo, alimento);

    if (sePuedeSeleccionar(alimento)) {
      fondo.setOnLongClickListener(v -> {
        setSelected(v, alimento);
        return true;
      });
    }
  }

  private boolean sePuedeSeleccionar(AlimentoDAO alimento) {
    return (!isForResult && !alimento.isFromHosting()) ||
        (isForResult && !incluidos.contains(alimento));
  }

  private void setEnabled(View rootView, AlimentoDAO alimento) {
    rootView.setAlpha(isForResult && incluidos.contains(alimento) ? 0.25F : 1F);
  }

  private void colorearFondo(View fondo, AlimentoDAO alimento) {
    fondo.setBackgroundColor(getColor(alimento, alimentoEstaSeleccionado(alimento)));
  }

  @ColorInt
  private int getColor(AlimentoDAO alimento, boolean estaSeleccionado) {

    @ColorRes final int colorId;

    if (isForResult && incluidos.contains(alimento)) {
      colorId = modoVisualizacion == ModoVisualizacion.LISTA ?
          R.color.negro_claro : android.R.color.white;
    } else if (estaSeleccionado) {
      colorId = R.color.accent;
    } else {
      colorId = modoVisualizacion == ModoVisualizacion.LISTA ?
          android.R.color.transparent : R.color.primary_light;
    }

    return ContextCompat.getColor(context, colorId);
  }

  private void setSelected(View fondo, AlimentoDAO alimento) {
    seleccionarAlimento(alimento, alimentoEstaSeleccionado(alimento));
    colorearFondo(fondo, alimento);
    onSeleccionarAlimento.modoSeleccion(haySeleccionados());
  }

  private void seleccionarAlimento(AlimentoDAO alimento, boolean alimentoEstaSeleccionado) {
    if (alimentoEstaSeleccionado) {
      seleccionados.remove(alimento);
    } else {
      seleccionados.add(alimento);
    }
  }

  private void editarAlimento(AlimentoDAO alimento, int positionInAdapter) {
    if (!alimento.isFromHosting()) {
      onEditarAlimentoListener.editar(alimento, positionInAdapter);
    }
  }

  static class FoodViewHolder extends RecyclerView.ViewHolder {

    public FoodViewHolder(@NonNull final View itemView) {
      super(itemView);
    }

    @NonNull
    protected String getPlural(@PluralsRes int id, float amount) {
      return ViewHolderEXT.getQuantityString(this, id, Math.round(amount));
    }

    @NonNull
    protected String getAmountOfRations(final float amount) {
      return getPlural(R.plurals.rations, amount);
    }
  }

  class ViewHolderTarjeta extends FoodViewHolder {

    @BindView(R.id.ll_contenido)
    ViewGroup llContenido;
    @BindView(R.id.ll_contenedor_nombre)
    ViewGroup llContenedorNombre;
    @BindView(R.id.ll_contenedor_info)
    ViewGroup llContenedorInfo;
    @BindView(R.id.ll_reducida)
    View llReducida;
    @BindView(R.id.tv_parte_arriba)
    TextView tvParteArriba;
    @BindView(R.id.tv_parte_abajo)
    TextView tvParteAbajo;
    @BindView(R.id.ll_ampliada)
    View llAmpliada;
    @BindView(R.id.tv_nombre_alimento)
    TextView tvNombreAlimento;
    @BindView(R.id.tv_cantidad)
    TextView tvCantidadUnidades;
    @BindView(R.id.tv_raciones)
    TextView tvRacionesUnidad;
    @BindView(R.id.tv_unidad_medida)
    TextView tvUnidadMedida;
    @BindView(R.id.tv_titulo_cantidad)
    TextView tvTituloCantidad;
    @BindView(R.id.tv_titulo_unidad_medida)
    TextView tvTituloUnidadMedida;
    @BindView(R.id.tv_titulo_raciones)
    TextView tvTituloRaciones;
    @BindView(R.id.iv_imagen_alimento)
    ImageView ivFotoAlimento;
    @BindView(R.id.iv_editar)
    ImageView ivEditar;
    @BindView(R.id.iv_usuario)
    ImageView ivUsuario;

    ViewHolderTarjeta(@NonNull final View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    void cargarVista(AlimentoDAO alimento) {
      habilitarAnimacionCambios();
      cargarValores(alimento);
      cargarImagen(alimento, ivFotoAlimento);
      cargarIconoHosting(alimento);
      asignarEditarOnClick(alimento);
      asignarItemOnClick(alimento);
      loadRoot(llContenido, alimento);
    }

    void unbindImage() {
      Glide.clear(ivFotoAlimento);
    }

    private void habilitarAnimacionCambios() {
      AnimationUtil.enableAnimateLayoutChanges(llContenedorNombre, llContenedorInfo);
    }

    private void cargarValores(AlimentoDAO alimento) {
      tvParteArriba.setText(getTextoParteArriba(alimento));
      tvParteAbajo.setText(getTextoParteAbajo(alimento));
      tvNombreAlimento.setText(alimento.getNombre());
      tvUnidadMedida.setText(alimento.getUnidadMedida());
      tvCantidadUnidades.setText(DecimalFormatUtils.decimalToString(alimento.getCantidadUnidades()));
      tvRacionesUnidad.setText(DecimalFormatUtils.decimalToString(alimento.getRacionesUnidad()));
    }

    private void cargarIconoHosting(AlimentoDAO alimento) {
      ivUsuario.setVisibility(!alimento.isFromHosting() ? View.VISIBLE : View.GONE);
    }

    private void asignarEditarOnClick(final AlimentoDAO alimento) {
      ivEditar.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          editarAlimento(alimento, getAdapterPosition());
        }
      });
    }

    private void asignarItemOnClick(final AlimentoDAO alimento) {
      llContenido.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          toggleSize(alimento);
        }
      });
    }

    private String getTextoParteArriba(AlimentoDAO alimento) {

      final float cantidadUnidades = alimento.getCantidadUnidades();

      return String.format(Locale.getDefault(),
          ViewHolderEXT.getString(this, R.string.resumen_parte_arriba),
          DecimalFormatUtils.decimalToString(cantidadUnidades),
          alimento.getUnidadMedida().toLowerCase());
    }

    private String getTextoParteAbajo(AlimentoDAO alimento) {

      final float cantidadRaciones = alimento.getRacionesUnidad();

      return String.format(Locale.getDefault(),
          ViewHolderEXT.getString(this, R.string.resumen_parte_abajo),
          DecimalFormatUtils.decimalToString(cantidadRaciones),
          getAmountOfRations(cantidadRaciones));
    }

    private void toggleSize(AlimentoDAO alimento) {
      if (onRedimensionarTarjeta.puedeRedimensionar()) {
        final boolean ahoraEsReducida = seEstaMostrandoInfoReducida();
        mostrarInfo(!ahoraEsReducida);

        if (!alimento.isFromHosting()) {
          ivEditar.setVisibility(ahoraEsReducida ? View.VISIBLE : View.INVISIBLE);
        }
      }
    }

    private boolean seEstaMostrandoInfoReducida() {
      return llReducida.getVisibility() == View.VISIBLE;
    }

    private boolean seEstaMostrandoInfoAmpliada() {
      return llAmpliada.getVisibility() == View.GONE;
    }

    private void mostrarInfo(boolean reducida) {
      llReducida.setVisibility(reducida ? View.VISIBLE : View.GONE);
      llAmpliada.setVisibility(reducida ? View.GONE : View.VISIBLE);
    }
  }

  class ViewHolderLista extends FoodViewHolder {

    ViewHolderLista(@NonNull final View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @BindView(R.id.tv_nombre)
    TextView tvNombre;
    @BindView(R.id.tv_resumen)
    TextView tvResumen;
    @BindView(R.id.iv_imagen)
    ImageView ivImagen;

    void cargarVista(@NonNull final AlimentoDAO alimento) {
      cargarTipografia();
      cargarValores(alimento);
      cargarImagen(alimento, ivImagen);
      asignarOnClick(alimento);
      loadRoot(itemView, alimento);
    }

    void unbindImage() {
      Glide.clear(ivImagen);
    }

    private void cargarTipografia() {
      tvNombre.setTypeface(negrita);
      tvResumen.setTypeface(light);
    }

    private void cargarValores(@NonNull final AlimentoDAO alimento) {
      tvNombre.setText(alimento.getNombre());
      tvResumen.setText(getResumen(alimento));
    }

    private String getResumen(@NonNull final AlimentoDAO alimento) {
      final float units = alimento.getCantidadUnidades();
      final float rations = alimento.getRacionesUnidad();
      final String unitsText = getPlural(R.plurals.units, units);
      final String contain = getPlural(R.plurals.contains, units);
      final String rationsText = getAmountOfRations(units);

      final String stringUnits = DecimalFormatUtils.decimalToString(units);
      final String unitOfMeasurement = alimento.getUnidadMedida().toLowerCase();
      final String stringRations = DecimalFormatUtils.decimalToString(rations);

      return ViewHolderEXT.getString(this, R.string.x_units_contain_x_rations,
          stringUnits, unitsText, unitOfMeasurement, contain, stringRations, rationsText);
    }

    private void asignarOnClick(final AlimentoDAO alimento) {
      itemView.setOnClickListener(v -> mostrarMenuContextual(alimento));
    }

    private void mostrarMenuContextual(final AlimentoDAO alimento) {
      final PopupMenu contextualMenu = new PopupMenu(context, tvResumen);
      contextualMenu.inflate(R.menu.menu_alimento_item_click);
      contextualMenu.setOnMenuItemClickListener(menuItem -> {
        if (menuItem.getItemId() == R.id.menu_item_editar_alimento) {
          editarAlimento(alimento, getAdapterPosition());
          return true;
        } else {
          return false;
        }
      });
      contextualMenu.show();
    }
  }

  private static class FoodDiffUtilCallback extends DiffUtil.Callback {

    @NonNull
    private final List<AlimentoDAO> oldList;
    @NonNull
    private final List<AlimentoDAO> newList;

    public FoodDiffUtilCallback(
        @NonNull final List<AlimentoDAO> oldList,
        @NonNull final List<AlimentoDAO> newList
    ) {
      this.oldList = oldList;
      this.newList = newList;
    }

    @Override
    public int getOldListSize() {
      return oldList.size();
    }

    @Override
    public int getNewListSize() {
      return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
      final AlimentoDAO oldItem = oldList.get(oldItemPosition);
      final AlimentoDAO newItem = newList.get(newItemPosition);
      return oldItem.equals(newItem);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
      // Items contents are never the same, since items properties cannot be edited.
      return false;
    }
  }
}