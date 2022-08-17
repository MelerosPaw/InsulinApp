package julioverne.insulinapp.utils;

import androidx.annotation.NonNull;
import java.util.StringTokenizer;

public class DecimalFormatUtils {

  /**
   * Formatea un número decimal en una cadena, devolviendo tantos decimales como deseados
   *
   * @param cantidad Cantidad que queremos transformar
   * @param decimalesDeseados Cantidad de decimales que queremos en caso de que no valgan 0
   * @param caracterDecimal Carácter de separación de decimales usado en la cantidad de entrada.
   * Si es float o double, debe ser un '.'
   * @param caracterDecimalDeseado Carácter de separación de decimales que queremos en el String
   * de salida
   * @return String con la cantidad con los decimales indicados usando el ceracterDecimalDeseado
   * como separador
   */
  public static String decimalToString(Object cantidad, int decimalesDeseados, String caracterDecimal,
      String caracterDecimalDeseado) {
    //Convierte a String la cantidad que recibe
    String valor = String.valueOf(cantidad);

    //Separa los decimales de la parte entera
    StringTokenizer elementos = new StringTokenizer(valor, caracterDecimal);
    String entero = "";
    String decimales = "";
    String nuevosDecimales = "";

    //Si el número comienza por una coma, interpreta que la parte entera vale 0
    if (valor.startsWith(",")) {
      entero = "0";
      decimales = elementos.nextToken();
      //Si el número solo tiene un elemento, quiere decir que no hay decimales, por lo que los
      //inicializa a 0
    } else if (elementos.countTokens() == 1) {
      entero = elementos.nextToken();
      decimales = "0";
    } else {
      entero = elementos.nextToken();
      decimales = elementos.nextToken();
    }

    //Si no quiere decimales o introduce un número negativo, devuelve solo la parte entera
    if (decimalesDeseados <= 0) {
      return entero;
    }
    //Si hay más decimales que los deseados, deja solo los deseados
    else if (decimales.length() > decimalesDeseados) {
      for (int i = 0; i < decimalesDeseados; i++)
        nuevosDecimales += decimales.charAt(i);
    }
    //Si hay menos decimales que los deseados, los rellena con ceros
    else if (decimales.length() < decimalesDeseados) {
      for (int i = 0; i < decimales.length(); i++)
        nuevosDecimales += decimales.charAt(i);
      for (int j = nuevosDecimales.length(); j < decimalesDeseados; j++)
        nuevosDecimales += "0";
      //Si la cantidad de decimales es la misma que los decimales deseados, devuelve el número tal cual
    } else if (decimales.length() == decimalesDeseados) {
      return entero + caracterDecimalDeseado + decimales;
    }

    return entero + caracterDecimalDeseado + nuevosDecimales;
  }

  /**
   * Devuelve un entero si los decimales de la cifra valen 0, si no, devuelve tantos decimales como deseados
   *
   * @param cantidad Cantidad que queremos transformar
   * @param decimalesDeseados Cantidad de decimales que queremos en caso de que no valgan 0
   * @param caracterDecimal Carácter de separación de decimales de la cantidad de entrada. Si le
   * pasamos un <i>float</i> o un <i>decimal</i> debemos indicar '.'.
   * @param caracterDecimalDeseado Carácter de separación de decimales que queremos en el String de salida
   * @return String con la cantidad decimal con el ceracterDecimalDeseado como separador
   */
  public static String decimalToStringIfZero(Object cantidad, int decimalesDeseados, String caracterDecimal,
      String caracterDecimalDeseado) {

    String valor = String.valueOf(cantidad);

    //Separa los decimales de la parte entera
    StringTokenizer elementos = new StringTokenizer(valor, caracterDecimal);
    String entero = "";
    String decimales = "";

    //Si el número comienza por una coma, interpreta que la parte entera vale 0
    if (valor.startsWith(",")) {
      entero = "0";
      decimales = elementos.nextToken();
      //Si el número solo tiene un elemento, quiere decir que no hay decimales, por lo que los
      //inicializa a 0
    } else if (elementos.countTokens() == 1) {
      entero = elementos.nextToken();
      decimales = "0";
    } else {
      entero = elementos.nextToken();
      decimales = elementos.nextToken();
    }

    //Si los decimales valen 0, devuelven solo la parte entera
    if (Integer.parseInt(decimales) == 0) {
      return entero;
    }
    //Si los decimales tienen valor, recibir -1 en decimalesDeseados indica que queremos recibir
    //el número con todos sus decimales
    else if (decimalesDeseados == -1) {
      return entero + caracterDecimalDeseado + decimales;
    }
    //Si no, devuelve el número con tantos decimales como se desee
    else {
      return decimalToString(cantidad, decimalesDeseados, caracterDecimal, caracterDecimalDeseado);
    }
  }

  @NonNull
  public static String decimalToString(float decimalValue) {
    return decimalToStringIfZero(String.valueOf(decimalValue), 2, ".", ",");
  }
}
