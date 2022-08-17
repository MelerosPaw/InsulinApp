package julioverne.insulinapp.utils;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Juan José Melero on 13/12/2016.
 */

public class IntentUtil {

  public static final String TAG = IntentUtil.class.getSimpleName();

  public enum IntentType {
    CAMERA, FILE_SELECT
  }

  @Nullable
  public static Intent getIntent(
      @NonNull final Context context,
      @NonNull final IntentType intentRequested
  ) {

    final Intent intent;

    switch (intentRequested) {
      case CAMERA:
        intent = getPhotoIntent(context);
        break;
      case FILE_SELECT:
        intent = getPictureFromFileIntent(context);
        break;
      default:
        intent = null;
        Log.e(TAG, "Intent \"" + intentRequested + "\" could not be obtained.");
    }

    return intent;
  }

  private static Intent getPhotoIntent(Context context) {

    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

    // Comprueba que el dispositivo cuente con los componentes necesarios para iniciar el
    // intent, en este caso, cámara de fotos
    if (intent.resolveActivity(context.getPackageManager()) != null) {
      return intent;
    } else {
      return null;
    }
  }

  private static Intent getPictureFromFileIntent(Context context) {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("image/*");

    if (intent.resolveActivity(context.getPackageManager()) != null) {
      return intent;
    } else {
      return null;
    }
  }
}
