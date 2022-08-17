package julioverne.insulinapp.utils;

import androidx.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Created by Juan José Melero on 17/06/2015.<br/>
 * Permite hacer comprobaciones y calculos con horas expresadas formato String
 */
public class TimeUtils {

    /**
     * Devuelve un booleano que indica si una cadena tiene un formato de hora <i>HH:MM</i> correcto
     *
     * @param hora La cadena que queremos comprobar
     * @return Devuelve <i>true</i> si el formato es de hora
     */
    public static boolean isHoraReal(String hora) {

        StringTokenizer stringTokenizer = new StringTokenizer(hora, ":", false);
        if (stringTokenizer.countTokens() != 2) {
            return false;
        }

        String horasString = stringTokenizer.nextToken();
        String minutosString = stringTokenizer.nextToken();
        int horas, minutos;

        try {
            horas = Integer.parseInt(horasString);
            minutos = Integer.parseInt(minutosString);
        } catch (NumberFormatException e) {
            return false;
        }

        if (horas <= 23 && horas >= 0 && minutos >= 0 && minutos < 60) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Convierte una cadena que contenga una hora real en int
     *
     * @param hora Cadena con la hora. Debe ser real y usar ':' como separador
     * @return Un <i>int[]</i> con las horas en la posición 0 y los minutos en la posición 1
     * o <i>null</i> si no es un formato correcto
     */
    @Nullable
    public static int[] convertirHora(String hora) {
        if (!isHoraReal(hora)) {
            return null;
        }

        int[] tiempo = new int[2];
        StringTokenizer stringTokenizer = new StringTokenizer(hora, ":");
        tiempo[0] = Integer.parseInt(stringTokenizer.nextToken());
        tiempo[1] = Integer.parseInt(stringTokenizer.nextToken());

        return tiempo;
    }

    /**
     * Si recibe una cadena que contiene un solo caracter, le concatena un '0' al principio.
     * Sirve para convertir al formatear horas y minutos.
     *
     * @param tiempo Cantidad que queremos formatear.
     * @returns Cadena formateada a dos caracteres.
     */
    public static String ceroCero(String tiempo) {
        if (tiempo.length() == 1) {
            return "0" + tiempo;
        }
        return tiempo;
    }

    /**
     * Si recibe un <i>int</i> que contiene una sola cifra, le concatena un '0' al principio.
     * Sirve para convertir al formatear horas y minutos.
     *
     * @param tiempo Cantidad que queremos formatear.
     * @returns Cadena con la cantidad formateada a dos caracteres.
     */
    public static String ceroCero(int tiempo) {
        String tiempoString = tiempo + "";
        if (tiempoString.length() == 1) {
            return "0" + tiempo;
        }
        return tiempoString;
    }

    /**
     * Devuelve una hora en formato hora
     *
     * @return Un String representando una hora.
     */
    public static String getHoraFormateada(int hora, int minutos) {
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hora);
        cal.set(Calendar.MINUTE, minutos);
        Date horaDate = cal.getTime();
        return sdf.format(horaDate);
    }

    /**
     * Devuelve una hora en formato hora
     *
     * @return Un String representando una hora.
     */
    public static String getHoraFormateada(String hora, String minutos) {
        int intHora = StringUtils.getInteger(hora);
        int intMinutos = StringUtils.getInteger(minutos);
        return getHoraFormateada(intHora, intMinutos);
    }
}
