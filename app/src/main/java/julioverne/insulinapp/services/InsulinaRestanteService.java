package julioverne.insulinapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import julioverne.insulinapp.data.DataManager;

/**
 * Created by Juan José Melero on 09/06/2015.
 */
public class InsulinaRestanteService extends Service {

    private Thread thread;
    private DataManager dataManager;
    public static final String BROADCAST_ACTION = "julioverne.insulinapp.ui.BaseActivity";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dataManager = DataManager.getInstance(this);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                float insulinaRestante = dataManager.calcularInsulinaRestante();
                Intent intent = new Intent(BROADCAST_ACTION);
                intent.putExtra("INSULINA", insulinaRestante);
                sendBroadcast(intent);
                stopSelf();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }
}
