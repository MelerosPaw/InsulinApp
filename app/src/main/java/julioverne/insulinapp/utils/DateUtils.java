package julioverne.insulinapp.utils;

import androidx.annotation.NonNull;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Juan José Melero on 02/06/2015.
 */
public class DateUtils {

    private static final String DATE_FORMAT = "dd/MM/yy";
    private static final String HOUR_FORMAT_24 = "hh:mm";

    private DateUtils() {
        //no instance
    }

    public static Long fechaEnMilisegundos(Date date) {
        return date.getTime();
    }

    public static Long fechaActualEnMilisegundos() {
        return Calendar.getInstance().getTime().getTime();
    }

    public static Date getNow() {
        return Calendar.getInstance().getTime();
    }

    @NonNull
    public static String dateToHour(@NonNull final Date fecha) {
        //        final Calendar cal = Calendar.getInstance();
        //        cal.setTime(fecha);
        //        String horas = TimeUtils.ceroCero(cal.get(Calendar.HOUR_OF_DAY));
        //        String minutos = TimeUtils.ceroCero(cal.get(Calendar.MINUTE));
        //
        //        return horas + ":" + minutos;
        return new SimpleDateFormat(HOUR_FORMAT_24, Locale.getDefault()).format(fecha);
    }

    public static String dateToString(Date fecha) {
        return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(fecha);
    }

    /**
     * Obtiene una instancia del momento actual y extrae las horas y los minutos.
     *
     * @return int[] con la hora actual en la primera posición y los minutos actuales en la segunda
     */
    public static int[] getHoraActual() {
        Calendar cal = Calendar.getInstance();
        int[] tiempo = {
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE)
        };
        return tiempo;
    }
}
