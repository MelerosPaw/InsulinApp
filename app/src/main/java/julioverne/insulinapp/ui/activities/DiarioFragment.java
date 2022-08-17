package julioverne.insulinapp.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import java.util.List;
import julioverne.insulinapp.data.Resource;
import julioverne.insulinapp.data.Status;
import julioverne.insulinapp.data.dao.EntradaDAO;
import julioverne.insulinapp.databinding.FragmentDiaryLayoutBinding;
import julioverne.insulinapp.ui.adaptadores.DiaryAdapter;
import julioverne.insulinapp.ui.base.BaseFragment;
import julioverne.insulinapp.ui.dialogs.DialogDetalleEntrada;
import julioverne.insulinapp.ui.viewmodels.DiarioViewModel;

public class DiarioFragment extends BaseFragment {

  private FragmentDiaryLayoutBinding binding;
  private DiarioViewModel viewModel;
  private final DiaryAdapter.OnEntryClickedListener onEntryClickedListener =
      new DiaryAdapter.OnEntryClickedListener() {
        @Override
        public void onEntryClicked(@NonNull EntradaDAO entrada) {
          viewModel.setSelectedEntry(entrada);
          binding.getDiaryAdapter().deselectItem();
          mostrarDialogAlimento(entrada);
        }
      };
  private Observer<Resource<List<EntradaDAO>>> diaryObserver = new Observer<Resource<List<EntradaDAO>>>() {
    @Override
    public void onChanged(@Nullable Resource<List<EntradaDAO>> resource) {
      if (resource != null) {
        final Status status = resource.getStatus();
        binding.setIsLoading(status == Status.LOADING);

        if (resource.isSuccessful()) {
          final DiaryAdapter adapter = new DiaryAdapter(resource.getData(), onEntryClickedListener);
          binding.setDiaryAdapter(adapter);
          binding.setIsEmpty(resource.getData().isEmpty());
        } else if (status == Status.ERROR) {
          binding.setIsEmpty(true);
        }
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewModel = new ViewModelProvider(this).get(DiarioViewModel.class);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentDiaryLayoutBinding.inflate(inflater, container, false);
    baseBinding = binding;
    loadView();
    return binding.getRoot();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        //                onBackPressed();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  public void loadView() {
    viewModel.getDiaryLiveData().observe(getViewLifecycleOwner(), diaryObserver);
    viewModel.requestDiary();
  }

  private void mostrarDialogAlimento(@NonNull final EntradaDAO entrada) {
    final Activity activity = getActivity();

    if (activity instanceof AppCompatActivity) {
      final DialogDetalleEntrada dialog = DialogDetalleEntrada.newInstance(entrada);
      dialog.setOnCloseListener(new DialogDetalleEntrada.OnCloseListener() {
        @Override
        public void onClose() {
          if (binding != null) {
            binding.getDiaryAdapter().deselectItem();
            binding.setDiaryAdapter(null);
          }
        }
      });
      dialog.show(((AppCompatActivity) activity).getSupportFragmentManager(), DialogDetalleEntrada.TAG);
    }
  }

  //  protected void onResume() {
  //     TODO: Melero 28/11/2021 Esto por qué?
  //    if (!dataManager.isDiarioEmpty()) {
  //      adaptadorEntradas.notifyDataSetChanged();
  //    }
  //    super.onResume();
  //  }
}
