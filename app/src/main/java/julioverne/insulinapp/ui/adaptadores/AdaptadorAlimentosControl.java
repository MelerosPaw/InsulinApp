package julioverne.insulinapp.ui.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.List;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.dao.AlimentoDAO;
import julioverne.insulinapp.data.dao.ItemAEntrada;
import julioverne.insulinapp.databinding.ItemAlimentoControlLayoutBinding;
import julioverne.insulinapp.ui.fragments.AlimentosAConsumirFragment;
import julioverne.insulinapp.utils.ContextExtensions;
import julioverne.insulinapp.utils.DecimalFormatUtils;
import julioverne.insulinapp.utils.StringUtils;
import kotlin.collections.CollectionsKt;

public class AdaptadorAlimentosControl extends ArrayAdapter<ItemAEntrada> {

  @NonNull
  private final List<ItemAEntrada> datos;
  @NonNull
  private final Context context;
  private ListView listView;
  private Fragment fragment;

  public AdaptadorAlimentosControl(
      @NonNull final Context context,
      @NonNull final Fragment fragment,
      @NonNull final List<ItemAEntrada> alimentos
  ) {
    super(context, R.layout.item_alimento_control_layout, alimentos);
    this.context = context;
    this.datos = alimentos;
    this.fragment = fragment;
  }

  @Override
  public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

    if (this.listView == null) {
      this.listView = (ListView) parent;
    }

    View item = convertView;
    final ViewHolder holder;

    if (item == null) {
      final LayoutInflater inflater = LayoutInflater.from(context);
      final ItemAlimentoControlLayoutBinding binding =
          ItemAlimentoControlLayoutBinding.inflate(inflater, parent, false);
      item = binding.getRoot();
      holder = new ViewHolder(binding, position);
      item.setTag(holder);
    } else {
      holder = (ViewHolder) item.getTag();
    }

    holder.bind(datos.get(position));
    return item;
  }

  /**
   * Añade un alimento a la lista.
   */
  public void nuevoAlimento(AlimentoDAO alimentoDAO) {
    final ItemAEntrada contiene = new ItemAEntrada();
    contiene.setAlimento(alimentoDAO);
    contiene.setCantidad(0f);
    add(contiene);
  }

  /**
   * Devuelve una {@code List} con los alimentos y las cantidades que se han indicado.
   *
   * @return Si faltan cantidades por indicar o no se han guardado todos los alimentos de la lista
   * devuelve <i>null</i>. Si no, devuelve la lista con los alimentos y las cantidades.
   */
  @NonNull
  public List<ItemAEntrada> getAlimentos() {

    if (hayCantidadesVacias()) {
      ContextExtensions.toast(context, R.string.advertencia_faltan_cantidades);
      return null;
    }

    return datos;
  }

  @NonNull
  public List<AlimentoDAO> getAlimentosIncluidos() {
    return CollectionsKt.map(datos, ItemAEntrada::getAlimento);
  }

  /**
   * Devuelve <i>true</i> si algún alimento no tiene cantidad indicada. Si un <i>ContieneAlimentoDAO</i>
   * de <i>datos</i> tiene una cantida de 0, devuelve lo contrario.
   *
   * @return Devuelve <i>true</i> si por cada alimento que hay en <i>datos</i> hay una cantidad
   * que no es 0.
   */
  private boolean hayCantidadesVacias() {

    for (ItemAEntrada contiene : datos)
      if (contiene.getCantidad() != null && contiene.getCantidad() <= 0) {
        return true;
      }

    return false;
  }

  @NonNull
  public List<ItemAEntrada> getDatos() {
    return datos;
  }

  private class ViewHolder {

    @NonNull
    private final ItemAlimentoControlLayoutBinding binding;
    final int posicion;

    private ViewHolder(@NonNull final ItemAlimentoControlLayoutBinding binding, final int position) {
      this.binding = binding;
      this.posicion = position;
    }

    private void bind(@NonNull final ItemAEntrada contiene) {
      binding.setItem(contiene);

      //Al cambiar el foco recoge el valor solo si la cantidad introducida es correcta.
      binding.etCantidadAlimento.setOnFocusChangeListener(new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
          if (!hasFocus) {
            final String contenido = binding.etCantidadAlimento.getText().toString();

            //Si la cadena está vacía, asigna el valor 0 a la cantidad
            if (contenido.isEmpty()) {
              contiene.setCantidad(0f);
              //Si no, comprueba si es decimal o entero y lo asigna
            } else {
              final String convertible = StringUtils.convertible(contenido);
              final String cantidad;

              if (convertible.equals("float")) {
                cantidad = DecimalFormatUtils.decimalToStringIfZero(contenido, 2, ",", ".");
              } else if (convertible.equals("integer")) {
                cantidad = contenido;
                //Si es una cantidad incorrecta, guarda 0 también
              } else {
                cantidad = "0";
              }

              contiene.setCantidad(Float.parseFloat(cantidad));
              binding.executePendingBindings(); // TODO: Melero 28/02/2022 Hay que ver si esto está actalizando
            }
          }
        }
      });

      binding.btnEliminar.setOnClickListener(v -> {
        remove(contiene);
        ((AlimentosAConsumirFragment) fragment).actualizarAutoCompleteTextView(contiene.getAlimento().getNombre());
      });
    }
  }
}
