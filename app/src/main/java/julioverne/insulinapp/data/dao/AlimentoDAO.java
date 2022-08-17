package julioverne.insulinapp.data.dao;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Comparator;
import julioverne.insulinapp.extensions.StringExt;
import julioverne.insulinapp.utils.StringUtils;

/**
 * Created by Juan José Melero on 02/05/2015.
 */
@DatabaseTable(tableName = "Alimentos")
public class AlimentoDAO implements Parcelable {

    public static final String FIELD_NOMBRE = "nombre";
    public static final String FIELD_HOSTING = "hosting";
    public static final String FIELD_IMAGEN = "imagen";
    public static final String FIELD_UNIDAD_MEDIDA = "unidadMedida";
    public static final String FIELD_CANTIDAD_UNIDADES = "cantidadUnidades";
    public static final String FIELD_RACIONES_UNIDAD = "racionesUnidad";

    @DatabaseField(columnName = FIELD_NOMBRE, id = true)
    private String nombre;
    @DatabaseField(columnName = FIELD_HOSTING)
    private Boolean hosting;
    @DatabaseField(columnName = FIELD_IMAGEN)
    private String imagen;
    @DatabaseField(columnName = FIELD_UNIDAD_MEDIDA)
    private String unidadMedida;
    @DatabaseField(columnName = FIELD_CANTIDAD_UNIDADES)
    private Float cantidadUnidades;
    @DatabaseField(columnName = FIELD_RACIONES_UNIDAD)
    private Float racionesUnidad;

    public AlimentoDAO() {

    }

    public AlimentoDAO(String nombre, Boolean hosting, String imagen, String unidadMedida,
        Float cantidadUnidades, Float racionesUnidad) {
        this.nombre = nombre;
        this.hosting = hosting;
        this.imagen = imagen;
        this.unidadMedida = unidadMedida;
        this.cantidadUnidades = cantidadUnidades;
        this.racionesUnidad = racionesUnidad;
    }

    //Constructor con parcel
    public AlimentoDAO(Parcel parcel) {
        this.nombre = parcel.readString();
        boolean[] arrayBoolean = new boolean[1];
        parcel.readBooleanArray(arrayBoolean);
        this.hosting = arrayBoolean[0];
        this.imagen = parcel.readString();
        this.unidadMedida = parcel.readString();
        this.cantidadUnidades = parcel.readFloat();
        this.racionesUnidad = parcel.readFloat();
    }

    public Float getCantidadUnidades() {
        return cantidadUnidades;
    }

    public void setCantidadUnidades(Float cantidadUnidades) {
        this.cantidadUnidades = cantidadUnidades;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Float getRacionesUnidad() {
        return racionesUnidad;
    }

    public void setRacionesUnidad(Float racionesUnidad) {
        this.racionesUnidad = racionesUnidad;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public boolean isFromHosting() {
        return hosting;
    }

    public void setHosting(boolean hosting) {
        this.hosting = hosting;
    }

    public boolean matchesNameOrMeasurementUnit(@NonNull final String word) {
        return StringUtils.containsIgnoreCase(StringExt.normalize(nombre), word)
            || StringUtils.containsIgnoreCase(StringExt.normalize(unidadMedida), word);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nombre);
        dest.writeBooleanArray(new boolean[] { hosting });
        dest.writeString(imagen);
        dest.writeString(unidadMedida);
        dest.writeFloat(cantidadUnidades);
        dest.writeFloat(racionesUnidad);
    }

    public static final Parcelable.Creator CREATOR =
        new Parcelable.Creator() {
            public AlimentoDAO createFromParcel(Parcel in) {
                return new AlimentoDAO(in);
            }

            public AlimentoDAO[] newArray(int size) {
                return new AlimentoDAO[size];
            }
        };

    public static final Comparator<AlimentoDAO> COMPARATOR = new Comparator<AlimentoDAO>() {
        @Override
        public int compare(AlimentoDAO lhs, AlimentoDAO rhs) {
            return lhs.getNombre().compareTo(rhs.getNombre());
        }
    };

    @Override
    public String toString() {
        return nombre + " (" + (isFromHosting() ? "SERVER" : "DATABASE") + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AlimentoDAO) {
            AlimentoDAO other = (AlimentoDAO) obj;
            return other.getNombre().equals(nombre) && other.isFromHosting() == hosting;
        } else {
            return false;
        }
    }
}
