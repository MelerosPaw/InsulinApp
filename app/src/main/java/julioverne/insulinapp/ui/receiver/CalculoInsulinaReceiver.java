package julioverne.insulinapp.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import julioverne.insulinapp.services.InsulinaRestanteService;

/**
 * Created by Juan José Melero on 29/03/2015.
 * En esta clase estableceremos qué queremos que pase cuando se active la alarma
 */
public class CalculoInsulinaReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    Intent intent1 = new Intent(context, InsulinaRestanteService.class);
    context.startService(intent1);
  }
}