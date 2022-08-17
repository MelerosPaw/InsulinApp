package julioverne.insulinapp.data.dao;

import android.os.Parcel;
import android.os.Parcelable;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Juan José Melero on 02/05/2015.
 */
@DatabaseTable(tableName = "Entradas")
public class EntradaDAO implements Parcelable {

  //    @ForeignCollectionField(columnName = "contieneAlimentos", foreignFieldName = "entrada")   private Collection<ContieneAlimentoDAO> alimentos;
  @DatabaseField(columnName = "fecha", id = true)
  private Date fecha;
  @DatabaseField(columnName = "unidadesInyectadas")
  private Integer unidadesInyectadas;
  @DatabaseField(columnName = "unidadesSangre")
  private Float unidadesSangre;
  @DatabaseField(columnName = "glucosaSangre")
  private Integer glucosaSangre;
  @DatabaseField(columnName = "antesComer")
  private Boolean antesComer;
  @DatabaseField(columnName = "resumenAlimentos")
  private String resumenAlimentos;

  public EntradaDAO() {
  }

  public EntradaDAO(Boolean antesComer, Date fecha, Integer glucosaSangre, Integer unidadesInyectadas,
      Float unidadesSangre, String resumenAlimentos) {
    this.antesComer = antesComer;
    this.fecha = fecha;
    this.glucosaSangre = glucosaSangre;
    this.unidadesInyectadas = unidadesInyectadas;
    this.unidadesSangre = unidadesSangre;
    this.resumenAlimentos = resumenAlimentos;
  }

  //Constructor para Parcelable
  public EntradaDAO(Parcel parcel) {
    this.fecha = new Date(parcel.readLong());
    this.unidadesInyectadas = parcel.readInt();
    this.unidadesSangre = parcel.readFloat();
    this.glucosaSangre = parcel.readInt();
    boolean[] arrayBoolean = new boolean[1];
    parcel.readBooleanArray(arrayBoolean);
    this.antesComer = arrayBoolean[0];
    this.resumenAlimentos = parcel.readString();
  }

  public String getResumenAlimentos() {
    return resumenAlimentos;
  }

  public void setResumenAlimentos(String resumenAlimentos) {
    this.resumenAlimentos = resumenAlimentos;
  }

  public Integer getUnidadesInyectadas() {
    return unidadesInyectadas;
  }

  public Boolean isAntesDeComer() {
    return antesComer;
  }

  public void setAntesComer(Boolean antesComer) {
    this.antesComer = antesComer;
  }

  public Date getFecha() {
    return fecha;
  }

  public void setFecha(Date fecha) {
    this.fecha = fecha;
  }

  public Integer getGlucosaSangre() {
    return glucosaSangre;
  }

  public void setGlucosaSangre(Integer glucosaSangre) {
    this.glucosaSangre = glucosaSangre;
  }

  public Integer getInsulinaDosis() {
    return unidadesInyectadas;
  }

  public void setUnidadesInyectadas(Integer unidadesInyectadas) {
    this.unidadesInyectadas = unidadesInyectadas;
  }

  public Float getUnidadesSangre() {
    return unidadesSangre;
  }

  public void setUnidadesSangre(Float unidadesSangre) {
    this.unidadesSangre = unidadesSangre;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(fecha.getTime());
    dest.writeInt(unidadesInyectadas);
    dest.writeFloat(unidadesSangre);
    dest.writeInt(glucosaSangre);
    dest.writeBooleanArray(new boolean[] { antesComer });
    dest.writeString(resumenAlimentos);
  }

  public static final Comparator<EntradaDAO> COMPARATOR = new Comparator<EntradaDAO>() {
    @Override
    public int compare(EntradaDAO lhs, EntradaDAO rhs) {
      return rhs.getFecha().compareTo(lhs.getFecha());
    }
  };
}