package julioverne.insulinapp.ui.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.lang.ref.WeakReference;
import java.util.List;
import julioverne.insulinapp.data.dao.EntradaDAO;
import julioverne.insulinapp.databinding.ItemEntradaLayoutBinding;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.EntradaViewHolder> {

  public interface OnEntryClickedListener {
    void onEntryClicked(@NonNull EntradaDAO entrada);
  }

  private final List<EntradaDAO> mDatos;
  private OnEntryClickedListener onEntryClickedListener;
  private View selectedItem;

  public DiaryAdapter(
      @NonNull final List<EntradaDAO> entradas,
      @NonNull final OnEntryClickedListener onEntryClickedListener
  ) {
    this.mDatos = entradas;
    this.onEntryClickedListener = onEntryClickedListener;
  }

  @Override
  public EntradaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    return new EntradaViewHolder(
        ItemEntradaLayoutBinding.inflate(inflater, parent, false),
        new OnEntryClicked(this, onEntryClickedListener));
  }

  @Override
  public void onBindViewHolder(@NonNull EntradaViewHolder holder, int position) {
    holder.loadView(mDatos.get(position));
  }

  @Override
  public int getItemCount() {
    return mDatos.size();
  }

  public void swapSelectedView(@NonNull final View selectableView) {
    deselectItem();
    selectableView.setSelected(true);
    selectedItem = selectableView;
  }

  public void deselectItem() {
    if (selectedItem != null) {
      selectedItem.setSelected(false);
    }
  }

  static class EntradaViewHolder extends RecyclerView.ViewHolder {

    @NonNull
    private final ItemEntradaLayoutBinding binding;

    private EntradaViewHolder(
        @NonNull final ItemEntradaLayoutBinding binding,
        @NonNull final OnEntryClicked onEntryClickedListener
    ) {
      super(binding.getRoot());
      this.binding = binding;
      binding.setOnEntryClicked(onEntryClickedListener);
    }

    void loadView(@NonNull final EntradaDAO entrada) {
      binding.setDiaryEntry(entrada);
    }
  }

  public static class OnEntryClicked {

    @NonNull
    private final WeakReference<DiaryAdapter> adapterWR;
    @NonNull
    private final OnEntryClickedListener listener;

    public OnEntryClicked(
        @NonNull final DiaryAdapter adapter,
        @NonNull final OnEntryClickedListener listener
    ) {
      this.adapterWR = new WeakReference(adapter);
      this.listener = listener;
    }

    public void onEntryClicked(@NonNull final EntradaDAO entry, @NonNull final View selectableView) {
      // Al hacer click, selecciona el item y muestra un dialog
      final DiaryAdapter adapter = adapterWR.get();

      if (adapter != null) {
        adapter.swapSelectedView(selectableView);
        listener.onEntryClicked(entry);
      }
    }
  }
}
