package julioverne.insulinapp.ui;

import android.content.Context;
import androidx.multidex.MultiDexApplication;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.DataManager;
import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by Juan José Melero on 14/08/2016.
 */
@ReportsCrashes(
    mailTo = "juanjose.melero@s-dos.es",
    mode = ReportingInteractionMode.DIALOG,
    customReportContent = { ReportField.ANDROID_VERSION },
    //        resToastText = R.string.acra_toast // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
    resDialogText = R.string.acra_dialog_message,
    resDialogIcon = R.mipmap.ic_manzanita, //optional. default is a warning sign
    resDialogTitle = R.string.acra_dialog_title, // optional. default is your application name
    resDialogCommentPrompt = R.string.acra_dialog_comment, // optional. When defined, adds a user text field input with this text resource as a label
    //        resDialogEmailPrompt = R.string.crash_user_email_label, // optional. When defined, adds a user email text entry with this text resource as label. The email address will be populated from SharedPreferences and will be provided as an ACRA field if configured.
    resDialogOkToast = R.string.acra_dialog_confirm, // optional. displays a Toast message when the user accepts to send a report.
    resDialogTheme = R.style.Basico //optional. default is Theme.Dialog
)
public class InsulinApplication extends MultiDexApplication {

  @Override
  public void onCreate() {
    super.onCreate();
    DataManager.getInstance(this).establecerCopiaSeguridadAutomatica();
  }

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    ACRA.init(this);
  }
}
