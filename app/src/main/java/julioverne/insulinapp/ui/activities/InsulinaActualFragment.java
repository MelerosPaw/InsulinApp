package julioverne.insulinapp.ui.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import julioverne.insulinapp.databinding.FragmentInsulineAmountLayoutBinding;
import julioverne.insulinapp.ui.base.BaseFragment;
import julioverne.insulinapp.ui.viewmodels.InsulineAmountViewModel;

/**
 * Created by Juan José Melero on 09/05/2015.
 */
public class InsulinaActualFragment extends BaseFragment {

  private FragmentInsulineAmountLayoutBinding binding;
  private InsulineAmountViewModel viewModel;
  private final Observer<Float> insulineAmountObserver = new Observer<Float>() {
    @Override
    public void onChanged(@Nullable Float currentInsulineAmount) {
      if (currentInsulineAmount != null) {
        drawInsulineAmount(currentInsulineAmount);
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewModel = new ViewModelProvider(this).get(InsulineAmountViewModel.class);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentInsulineAmountLayoutBinding.inflate(inflater, container, false);
    baseBinding = binding;
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    loadView();
  }

  private void loadView() {
    viewModel.getCurrentInsulineLiveData().observe(this, insulineAmountObserver);
  }

  public void drawInsulineAmount(final float insulineAmount) {
    binding.setGlucoseAmount(insulineAmount);
  }

  //    @Override
  //    protected void onResume() {
  //        final String BROADCAST_ACTION = "julioverne.insulinapp.ui.AlimentosActivity";
  //        registerReceiver(receiver, new IntentFilter(BROADCAST_ACTION));
  //        super.onResume();
  //    }
  //
  //
  //    @Override
  //    protected void onPause() {
  //        unregisterReceiver(receiver);
  //        super.onPause();
  //    }
}
