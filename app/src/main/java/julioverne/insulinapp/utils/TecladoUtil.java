package julioverne.insulinapp.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

public class TecladoUtil {

  private TecladoUtil() {
  }

  public static void ocultarTeclado(Activity activity) {
    if (activity.getCurrentFocus() != null) {
      InputMethodManager inputMethodManager =
          (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
          .getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
  }

  public static void mostrarTeclado(View view) {
    InputMethodManager inputMethodManager =
        (InputMethodManager) view.getContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE);
    view.requestFocus();
    inputMethodManager.showSoftInput(view, InputMethodManager.RESULT_UNCHANGED_SHOWN);
  }

  public static void keyboardToAdjustPan(Activity activity) {
    activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
  }
}
