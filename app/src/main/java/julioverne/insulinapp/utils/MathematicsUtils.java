package julioverne.insulinapp.utils;

/**
 * Created by Juan José Melero on 03/06/2015.
 */
public class MathematicsUtils {

  /**
   * Resuelve la incógnita de una regla de tres.
   *
   * Para este problema: "Si una persona lee 10 páginas en 5 minutos, ¿cuántas páginas leerá en 20
   * minutos?"; los parámetros formales serían los siguientes:
   *
   * @param totalIzquierdo 10
   * @param totalDerecho 5
   * @param parte 20
   * @return El resultado de la regla de tres. En este caso sería 20 * 5 / 10 = 10.
   */
  public static double reglaDeTres(double totalIzquierdo, double totalDerecho, double parte) {
    return (parte * totalDerecho) / totalIzquierdo;
  }
}
