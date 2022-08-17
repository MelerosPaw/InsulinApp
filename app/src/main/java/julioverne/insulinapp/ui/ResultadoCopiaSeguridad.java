package julioverne.insulinapp.ui;

/**
 * Created by Juan José Melero on 31/12/2016.
 */

public class ResultadoCopiaSeguridad {

  private boolean realizada;
  private String mensaje;

  public ResultadoCopiaSeguridad(boolean realizada, String mensaje) {
    this.realizada = realizada;
    this.mensaje = mensaje;
  }

  public boolean isRealizada() {
    return realizada;
  }

  public String getMensaje() {
    return mensaje;
  }
}
