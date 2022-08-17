package julioverne.insulinapp.utils;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import java.util.List;
import julioverne.insulinapp.extensions.StringExt;

/**
 * Created by Juan José Melero on 30/05/2015.
 */
public class StringUtils {

    //Devuelve true si "busqueda" se encuentra en "cadena"
    public static boolean containsIgnoreCase(String cadena, String busqueda) {

        //Pone las palabras en minúscula
        String cadenaMinuscula = StringExt.normalize(cadena);
        String busquedaMinuscula = StringExt.normalize(busqueda);
        return cadenaMinuscula.contains(busquedaMinuscula);
    }

    //Devuelve true si "busqueda" se encuentra en "elementos"
    public static boolean equalsIgnoreCase(List<String> elementos, String busqueda) {
        for (int i = 0; i < elementos.size(); i++)
            if (elementos.get(i).equalsIgnoreCase(busqueda)) {
                return true;
            }
        return false;
    }

    //Devuelve un boolean indicando si la cadena es un número o no
    public static boolean isNumero(String valor) {
        try {
            Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Devuelve un Integer a partir de una cadena. Si tiene puntos o comas, devolverá solo la parte
     * entera. Si no es un número, lanzará una excepción.
     *
     * @param valor La cadena que contiene el número.
     * @return un {@code Integer} con el entero proporcionado en forma de cadena o co
     */
    public static Integer getInteger(String valor) {
        if (!valor.contains(".") && !valor.contains(",")) {
            return Integer.parseInt(valor);
        } else {
            if (countOccurrences(valor, ",") + countOccurrences(valor, ".") > 1) {
                throw new NumberFormatException("The number contains '.' or ',' characters more than" +
                    "once. It cannot be converted to float and then to integer by StringUtils.getInteger().");
            } else {
                valor = valor.replace(",", ".");
                valor = valor.substring(0, valor.indexOf('.'));
                return Integer.parseInt(valor);
            }
        }
    }

    /**
     * Muestra un <i>Toast</i> corto con el <i>String</i> que le pasemos.
     *
     * @param mensaje String que queremos mostrar
     */
    public static void toastCorto(Context context, String mensaje) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
    }

    /**
     * Muestra un Toast de un recurso string almacenado en strings.xml
     *
     * @param recursoString Recusor String, como R.string.texto
     */
    public static void toastCorto(Context context, int recursoString) {
        Toast.makeText(context, context.getString(recursoString), Toast.LENGTH_SHORT).show();
    }

    public static void toastLargo(Context context, String mensaje) {
        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show();
    }

    /**
     * Muestra un Toast de un recurso string almacenado en strings.xml
     *
     * @param recursoString Recuso String, como R.string.texto
     */
    public static void toastLargo(Context context, @StringRes int recursoString) {
        Toast.makeText(context, context.getString(recursoString), Toast.LENGTH_LONG).show();
    }

    /**
     * Indica si una cadena se puede transformar a <i>int</i> o <i>float</i> según si tiene o no
     * decimales. En caso contrario, indicará que es un <i>string</i>.
     *
     * @param cadena Cadena que queremos comprobar.
     * @return Devuelve una cadena que valdrá "integer" si es convertible en <i>int</i>; <i>float</i>
     * si es convertible en <i>float</i> o <i>string</i> si no es convertible en ninguno de los dos.
     */
    public static String convertible(String cadena) {
        String resultado;
        try {
            Integer.parseInt(cadena);
            resultado = "integer";
        } catch (NumberFormatException e1) {
            try {
                cadena = cadena.replaceAll(",", ".");
                Float.parseFloat(cadena);
                resultado = "float";
            } catch (NumberFormatException e2) {
                resultado = "string";
            }
        }

        return resultado;
    }

    /**
     * Comprueba si la cadena recibida está vacía o no.
     *
     * @param cadena La cadena que queremos comprobar
     * @return <i>true</i>* si la cadena está vacía
     */
    public static boolean isCadenaVacia(@NonNull final String cadena) {
        return cadena.isEmpty();
    }

    /**
     * Comprueba si las cadenas recibidas están vacías o no.
     *
     * @param cadenas Las cadenas que queremos comprobar
     * @return {@code true} si alguna de las cadenas está vacía
     */
    public static boolean isCadenaVacia(String[] cadenas) {
        for (int i = 0; i < cadenas.length; i++) {
            if (isCadenaVacia(cadenas[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Elimimina los espacios de un grupo de cadenas
     *
     * @param cadenas Array con las cadenas que queremos eliminar
     * @return Devuelve las mismas cadenas pero sin espacios. Si no había espacios, las devuelve
     * igualmente
     */
    public static String[] eliminarEspacios(String[] cadenas) {
        for (int i = 0; i < cadenas.length; i++) {
            cadenas[i] = cadenas[i].replaceAll(" ", "");
        }

        return cadenas;
    }

    /**
     * Devuelve la cadena o una cadena vacía si es nulo.
     *
     * @param nullable Cadena que puede ser nula
     * @return La cadena o una cadena vacía si [@code nullable] es nulo.
     */
    public static String getEmptyIfNull(@Nullable String nullable) {
        return nullable == null ? "" : nullable;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        } else {
            int exp = (int) (Math.log(bytes) / Math.log(unit));
            String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 2) + (si ? "" : "i");
            return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
        }
    }

    /**
     * Returns the amount of times that {@code substring can be found in {@code string}.
     *
     * @param string The string to look into.
     * @param substring The substring to be seached within {@code string}.}
     * @return the amount of times {@code string} contains {@code substring}.
     */
    public static int countOccurrences(String string, String substring) {
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = string.indexOf(substring, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += substring.length();
            }
        }
        return count;
    }
}
