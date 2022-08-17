package julioverne.insulinapp.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import butterknife.OnClick;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.dao.EntradaDAO;
import julioverne.insulinapp.databinding.DialogDetalleEntradaLayoutBinding;

public class DialogDetalleEntrada extends DialogFragment {

  public static final String TAG = DialogDetalleEntrada.class.getSimpleName();
  private static final String BUNDLE_ENTRADA = "BUNDLE_ENTRADA";

  private DialogDetalleEntradaLayoutBinding binding;
  private OnCloseListener closeListener;
  private EntradaDAO entrada;

  @NonNull
  public static DialogDetalleEntrada newInstance(@Nullable final EntradaDAO entrada) {
    Bundle args = new Bundle();
    args.putParcelable(BUNDLE_ENTRADA, entrada);

    DialogDetalleEntrada fragment = new DialogDetalleEntrada();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      entrada = getArguments().getParcelable(BUNDLE_ENTRADA);
    }

    setCancelable(false);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState
  ) {
    binding = DialogDetalleEntradaLayoutBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    loadView();
  }

  private void loadView() {
    binding.setDiaryEntry(entrada);
  }

  @Override
  public void onStart() {
    super.onStart();
    // Pone el dialog en modo wrap_content
    if (getDialog().getWindow() != null) {
      getDialog().getWindow().setLayout(
          LinearLayout.LayoutParams.MATCH_PARENT,
          LinearLayout.LayoutParams.WRAP_CONTENT
      );
    }
  }

  @OnClick(R.id.btn_cerrar)
  public void onClick() {
    closeListener.onClose();
    dismiss();
  }

  public void setOnCloseListener(OnCloseListener closeListener) {
    this.closeListener = closeListener;
  }

  public interface OnCloseListener {
    void onClose();
  }
}