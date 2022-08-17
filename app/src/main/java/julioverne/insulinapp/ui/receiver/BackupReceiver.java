package julioverne.insulinapp.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import julioverne.insulinapp.data.DataManager;

/**
 * Created by Juan José Melero on 29/03/2015.
 * En esta clase estableceremos qué queremos que pase cuando se active la alarma.
 */
public class BackupReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {

    DataManager dataManager = DataManager.getInstance(context);

    // Si se recibe el intent al iniciarse, establece una alarma para que salte a una hora.
    if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
      dataManager.establecerCopiaSeguridadAutomatica();
      // Si se recibe el intent de la alarma, realiza la copia de seguridad.
    } else {
      dataManager.realizarCopiaSeguridad();
    }
  }
}