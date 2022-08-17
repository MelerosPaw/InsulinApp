package julioverne.insulinapp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * @deprecated Use {@link MessageExtensions} instead.
 */
@Deprecated
public class MessageUtil {

  public static AlertDialog showAlert(Activity ac, String text, String buttonText) {
    AlertDialog dialog = createAlertDialog(ac, text, buttonText);
    dialog.show();
    return dialog;
  }

  public static AlertDialog showAlert(Activity ac, String text, String buttonText,
      DialogInterface.OnDismissListener dismissListener) {
    AlertDialog dialog = createAlertDialog(ac, text, buttonText);
    dialog.setOnDismissListener(dismissListener);
    dialog.show();
    return dialog;
  }

  private static AlertDialog createAlertDialog(Activity ac, String text, String buttonText) {
    return new AlertDialog.Builder(ac)
        .setTitle("Atención")
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setMessage(text)
        .setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        }).create();
  }
}
