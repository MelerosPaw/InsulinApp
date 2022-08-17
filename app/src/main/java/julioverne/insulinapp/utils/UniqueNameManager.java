package julioverne.insulinapp.utils;

import java.text.Normalizer;

public abstract class UniqueNameManager {

  private static final String SUFFIX_SEPARATOR = "_";

  public String getUniqueName(String nombreOrigen) {
    String nombreProcesado = Normalizer.normalize(nombreOrigen, Normalizer.Form.NFD)
        .toLowerCase()
        .replaceAll("[^\\p{ASCII}]", "")
        .replaceAll(" ", "")
        .trim();

    boolean nombreRepetido = isNombreRepetido(nombreProcesado);
    while (nombreRepetido) {
      nombreProcesado = establecerSufijo(nombreProcesado);
      nombreRepetido = isNombreRepetido(nombreProcesado);
    }

    return nombreProcesado;
  }

  public abstract boolean isNombreRepetido(String nombre);

  private String establecerSufijo(String nombre) {
    String nombreConSufijo;
    if (tieneSufijo(nombre)) {
      nombreConSufijo = aumentarSufijo(nombre);
    } else {
      nombreConSufijo = setSufijo(nombre);
    }

    return nombreConSufijo;
  }

  private boolean tieneSufijo(String nombre) {
    return nombre.contains(SUFFIX_SEPARATOR) && nombre.lastIndexOf(SUFFIX_SEPARATOR) != nombre.length() - 1;
  }

  private String setSufijo(String nombre) {
    return nombre + SUFFIX_SEPARATOR + "1";
  }

  private String aumentarSufijo(String nombre) {
    int sufijo = obtenerSufijo(nombre) + 1;
    return nombre.substring(0, nombre.length() - 1) + sufijo;
  }

  private int obtenerSufijo(String nombre) {
    return Integer.parseInt(nombre.substring(nombre.length() - 1));
  }
}
