package julioverne.insulinapp.ui.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.lang.ref.WeakReference;
import julioverne.insulinapp.R;
import julioverne.insulinapp.constants.Constants;
import julioverne.insulinapp.data.DataManager;
import julioverne.insulinapp.data.dao.AlimentoDAO;
import julioverne.insulinapp.databinding.DialogDefinirAlimentoLayoutBinding;
import julioverne.insulinapp.extensions.FragmentExtensions;
import julioverne.insulinapp.utils.DecimalFormatUtils;
import julioverne.insulinapp.utils.IntentUtil;
import julioverne.insulinapp.utils.InternalMemoryUtils;
import julioverne.insulinapp.utils.StringUtils;

import static android.app.Activity.RESULT_OK;

public class DialogEditarAlimento extends DialogFragment {

  public interface OnFoodSavedListener {
    void onSaved(@NonNull AlimentoDAO alimento, int posicion);

    void onFailed(@NonNull AlimentoDAO alimento);
  }

  public static final String TAG = DialogEditarAlimento.class.getSimpleName();
  private static final String BUNDLE_ALIMENTO = "BUNDLE_ALIMENTO";
  private static final String BUNDLE_POSICION = "BUNDLE_POSICION";

  private DialogDefinirAlimentoLayoutBinding binding;
  private Bitmap bitmap;
  private AlimentoDAO alimento;
  private DataManager dataManager;
  private int posicion;

  public DialogEditarAlimento() {
    setCancelable(false);
  }

  public static DialogEditarAlimento newInstance(
      @Nullable AlimentoDAO alimento,
      final int posicion
  ) {
    final DialogEditarAlimento fragment = new DialogEditarAlimento();
    final Bundle args = new Bundle();
    args.putParcelable(BUNDLE_ALIMENTO, alimento);
    args.putInt(BUNDLE_POSICION, posicion);
    fragment.setArguments(args);
    return fragment;
  }

  public void setUp(Bitmap bitmap) {
    this.bitmap = bitmap;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    dataManager = DataManager.getInstance(getContext());

    if (getArguments() != null) {
      alimento = getArguments().getParcelable(BUNDLE_ALIMENTO);
      posicion = getArguments().getInt(BUNDLE_POSICION, -1);
    }
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = DialogDefinirAlimentoLayoutBinding.inflate(LayoutInflater.from(getContext()),
        container, false);
    binding.setDialogListener(new EditFoodDialogListener(this));
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    loadView();
  }

  public void loadView() {

    binding.ivImagenAlimento.setImageBitmap(bitmap);

    //Si vamos a actualizar un alimento, rellenamos los campos con los valores del alimento
    if (alimento != null) {
      binding.etNombreAlimento.setText(alimento.getNombre());
      binding.etUnidadMedida.setText(alimento.getUnidadMedida());
      binding.etCantidad.setText(DecimalFormatUtils.decimalToStringIfZero(
          alimento.getCantidadUnidades().toString(), 2, ".", ","));
      binding.etRaciones.setText(DecimalFormatUtils.decimalToStringIfZero(
          alimento.getRacionesUnidad().toString(), 2, ".", ","));
    }
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    return dialog;
  }

  @Override
  public void onStart() {
    super.onStart();
    // Pone el dialog en modo pantalla completa
    if (getDialog().getWindow() != null) {
      getDialog().getWindow().setLayout(
          LinearLayout.LayoutParams.MATCH_PARENT,
          LinearLayout.LayoutParams.WRAP_CONTENT
      );
    }
  }

  /**
   * Guarda el alimento definido en el dialog.
   */
  public void guardarAlimento(
      @NonNull final String foodName,
      @NonNull final String measurementUnit,
      @NonNull final String chAmount,
      @NonNull final String portionAmount
  ) {

    //Si la información es correcta, crea el alimento
    final AlimentoDAO editedFood = createFood(foodName, measurementUnit, chAmount, portionAmount);

    if (editedFood != null) {
      //El nombre de la imagen será el nombre del alimento en minúsculas con extensión gif.
      // TODO: Hay que diferenciar entre cuando se crea un alimento nuevo y cuando se edita
      // TODO: Cuando se crea, hay que comprobar que el nombre sea único y ponerle gif
      // TODO: Cuando se edita, hay que comprobar solo si el nobre nuevo + gif existe
      // TODO: 11/11/2017 En algún momento se tendrá que comprobar si el nombre está repetido
      final String imageName = getImageName(foodName);
      editedFood.setImagen(imageName);
      final boolean guardado = saveFood(editedFood);

      if (!guardado) {
        StringUtils.toastLargo(getContext(), R.string.advertencia_alimento_no_guardado);
      }

      callListener(editedFood, guardado);
      dismiss();
    }
  }

  private boolean saveFood(AlimentoDAO alimentoNuevo) {
    final boolean guardado;

    if (alimento == null) {
      guardado = dataManager.guardarAlimento(alimentoNuevo, bitmap);
    } else {
      guardado = dataManager.sobrescribirAlimento(alimento, alimentoNuevo, bitmap);
    }
    return guardado;
  }

  private String getImageName(String foodName) {
    final String nombreImagen;

    if (alimento == null) {
      nombreImagen = getNuevoNombre(foodName);

      if (dataManager.isArchivoRepetido(nombreImagen)) {
        Log.e(TAG, "guardarAlimento: IMPOSSIBLE!! Ya existe un fichero con el nombre " + nombreImagen);
      }
    } else if (!foodName.equals(alimento.getNombre())) {
      nombreImagen = getNuevoNombre(foodName);
    } else {
      nombreImagen = alimento.getImagen();
    }
    return nombreImagen;
  }

  private void callListener(@NonNull final AlimentoDAO alimentoNuevo, final boolean guardado) {
    final OnFoodSavedListener listener =
        FragmentExtensions.getInvokingClassAs(this, OnFoodSavedListener.class);

    if (listener != null) {
      if (!guardado) {
        listener.onFailed(alimentoNuevo);
      } else {
        listener.onSaved(alimentoNuevo, posicion);
      }
    }
  }

  private String getNuevoNombre(@NonNull final String name) {
    String nombreImagen = dataManager.normalizarNombre(name);
    if (dataManager.isNombreImagenRepetido(dataManager.procesarNombre(nombreImagen))) {
      nombreImagen = dataManager.getUniqueName(getContext(), nombreImagen);
    }
    return dataManager.procesarNombre(nombreImagen);
  }

  /**
   * Comprueba que los datos introducidos sean correctos y crea un alimento.
   *
   * @return El alimento creado o nulo si no se ha podido crear porque alguno de los campos
   * no es corrego. si los campos son válidos para guardar.
   */
  @Nullable
  private AlimentoDAO createFood(
      @NonNull final String foodName,
      @NonNull final String measurementUnit,
      @NonNull final String chs,
      @NonNull final String portions
  ) {
    // RETURN CASE: Si hay alguna cadena vacía, no puede seguir
    if (StringUtils.isCadenaVacia(new String[] {
        foodName,
        measurementUnit,
        chs,
        portions
    })) {
      StringUtils.toastCorto(getContext(), R.string.advertencia_faltan_datos);
      return null;

      // RETURN CASE: Si el alimento está repetido, no se puede guardar.
    } else if (alimento == null && dataManager.isNombreAlimentoRepetido(foodName)) {
      StringUtils.toastLargo(getContext(),
          String.format(getContext().getString(R.string.advertencia_repetido),
              foodName));
      return null;

      // RETURN CASE: La cantidad de unidades no es una cifra.
    } else if (StringUtils.convertible(chs).equals("string")) {
      StringUtils.toastCorto(getContext(), R.string.advertencia_cantidad_incorrecta);
      return null;

      // RETURN CASE: La cantidad de raciones no es una cifra.
    } else if (StringUtils.convertible(portions).equals("string")) {
      StringUtils.toastCorto(getContext(), R.string.advertencia_raciones_incorrectas);
      return null;
    } else {
      // Convierte la cantidad de unidades y las raciones en float para guardarlos en un objeto.
      final Float parsedChs = Float.parseFloat(chs.replaceAll(",", "."));
      final Float parsedPortions = Float.parseFloat(portions.replaceAll(",", "."));
      return new AlimentoDAO(foodName, false, null, measurementUnit, parsedChs,
          parsedPortions);
    }
  }

  /**
   * Cierra el cuadro de diálogo.
   */
  public void cancelar() {
    dismiss();
  }

  /**
   * Cambia la imagen del alimento.
   */
  public void cambiarImagen(View view) {
    seleccionarOrigenImagen(view);
  }

  // Activa la cámara de fotos para tomar una imagen del alimento
  public void seleccionarOrigenImagen(View view) {
    dataManager.seleccionarOrigenImagen(view, new DataManager.GetOrigenImagenCallback() {
      @Override
      public void onOrigenSelected(DataManager.OrigenImagen origen) {
        final IntentUtil.IntentType intentType = origen == DataManager.OrigenImagen.DESDE_CAMARA
            ? IntentUtil.IntentType.CAMERA
            : IntentUtil.IntentType.FILE_SELECT;
        launchIntent(intentType);
      }
    });
  }

  /**
   * Inicia un intent para obtener la imagen desde la camara o desde el sistema de archivos.
   */
  private void launchIntent(@NonNull final IntentUtil.IntentType type) {

    final int requestCode = type == IntentUtil.IntentType.CAMERA ?
        Constants.CAMERA_INTENT_REQUEST_CODE : Constants.FILE_SEARCH_INTENT_REQUEST_CODE;

    final Intent intent = IntentUtil.getIntent(getContext(), type);

    if (intent != null) {
      switch (type) {
        case CAMERA:
          FragmentExtensions.longToast(this, R.string.advertencia_foto);
          startActivityForResult(intent, requestCode);
          break;
        case FILE_SELECT:
          startActivityForResult(Intent.createChooser(intent, getString(R.string.mensaje_elige_image)), requestCode);
          break;
      }
    } else {
      switch (type) {
        case CAMERA:
          FragmentExtensions.toast(this, R.string.advertencia_sin_camara);
          break;
        case FILE_SELECT:
          FragmentExtensions.toast(this, R.string.advertencia_sin_galeria);
          break;
      }
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK && dataManager.isRequestCodeForThisView(requestCode)) {
      bitmap = dataManager.getBitmapFromResult(getActivity(), requestCode, data);
      bitmap = InternalMemoryUtils.prepararBitmap(bitmap);
      binding.ivImagenAlimento.setImageBitmap(bitmap);
    }
  }

  public static final class EditFoodDialogListener {

    @NonNull
    WeakReference<DialogEditarAlimento> dialogWR;

    public EditFoodDialogListener(@NonNull final DialogEditarAlimento dialog) {
      this.dialogWR = new WeakReference<>(dialog);
    }

    public void saveFood(
        @NonNull final String foodName,
        @NonNull final String measurementUnit,
        @NonNull final String chAmount,
        @NonNull final String portionAmount
    ) {
      final DialogEditarAlimento dialog = dialogWR.get();

      if (dialog != null) {
        dialogWR.get().guardarAlimento(foodName, measurementUnit, chAmount, portionAmount);
      }
    }

    public void onCancelClicked() {
      final DialogEditarAlimento dialog = dialogWR.get();

      if (dialog != null) {
        dialog.dismiss();
      }
    }

    public void cambiarImagen(@NonNull View view) {
      final DialogEditarAlimento dialog = dialogWR.get();

      if (dialog != null) {
        dialog.cambiarImagen(view);
      }
    }
  }
}