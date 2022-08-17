package julioverne.insulinapp.data.dao;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Juan José Melero on 02/05/2015.
 */

public class ItemAEntrada implements Parcelable {

  private EntradaDAO entrada;
  private AlimentoDAO alimento;
  private Float cantidad;

  public ItemAEntrada() {
  }

  public ItemAEntrada(Float cantidad, AlimentoDAO alimento) {
    this.cantidad = cantidad;
    this.alimento = alimento;
  }

  //Constructor para Parcelable
  public ItemAEntrada(Parcel parcel) {
    entrada = parcel.readParcelable(EntradaDAO.class.getClassLoader());
    alimento = parcel.readParcelable(AlimentoDAO.class.getClassLoader());
    cantidad = parcel.readFloat();
  }

  public AlimentoDAO getAlimento() {
    return alimento;
  }

  public void setAlimento(AlimentoDAO alimento) {
    this.alimento = alimento;
  }

  public Float getCantidad() {
    return cantidad;
  }

  public void setCantidad(Float cantidad) {
    this.cantidad = cantidad;
  }

  public EntradaDAO getEntrada() {
    return entrada;
  }

  public void setEntrada(EntradaDAO entrada) {
    this.entrada = entrada;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(entrada, flags);
    dest.writeParcelable(alimento, flags);
    dest.writeFloat(cantidad);
  }

  public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

    public ItemAEntrada createFromParcel(Parcel in) {
      return new ItemAEntrada(in);
    }

    public ItemAEntrada[] newArray(int size) {
      return new ItemAEntrada[size];
    }
  };
}
