package julioverne.insulinapp.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.DataManager;

/**
 * Created by Juan José Melero on 13/06/2015.<br/>
 * Servicio que actualiza la lista de alimentos disponible en la memoria del teléfono. Comprueba si
 * existe conexión a la red, descarga todos los alimentos del servidor y los almacena si no están
 * ya en memoria.
 */
public class ActualizarAlimentosService extends Service {

    public static final String TAG = ActualizarAlimentosService.class.getSimpleName();
    public static final String BROADCAST_ACTION = "julioverne.insulinapp.ui.AlimentosActivity";
    public static final String RECARGAR = "RECARGAR";
    public static final String MENSAJE = "MENSAJE";

    private DataManager dataManager;
    private Thread thread;

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
                executeService();
                terminate();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    //Hace una consulta a la base de datos para obtener los alimentos y, si hay alimentos nuevos,
    //los guarda
    public void executeService() {

        //RETURN CASE: No hay red disponible
        if (!isNetworkAvailable(getApplicationContext())) {
            Intent intent = new Intent(BROADCAST_ACTION);
            intent.putExtra(MENSAJE, getString(R.string.advertencia_sin_conexion));
            sendBroadcast(intent);
            return;
        } else {
            dataManager.actualizarAlimentos();
        }
    }

    //Comprueba que estén activados los servicios de red: WiFi o tráfico de datos
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public void terminate() {
        stopSelf();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
