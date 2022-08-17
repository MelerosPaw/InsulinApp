package julioverne.insulinapp.data.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Juan José Melero on 26/04/2015.
 */

@DatabaseTable(tableName = "Periodos")
public class PeriodoDAO {

  @DatabaseField(columnName = "horaInicio", id = true)
  private Integer horaInicio;
  @DatabaseField(columnName = "minutosInicio")
  private Integer minutosInicio;
  @DatabaseField(columnName = "horaFin")
  private Integer horaFin;
  @DatabaseField(columnName = "minutosFin")
  private Integer minutosFin;
  @DatabaseField(columnName = "unidadesInsulina")
  private Float unidadesInsulina;

  public PeriodoDAO() {
  }

  public PeriodoDAO(Integer horaInicio, Integer minutosInicio, Integer horaFin, Integer minutosFin,
      Float unidadesInsulina) {
    this.horaInicio = horaInicio;
    this.minutosInicio = minutosInicio;
    this.horaFin = horaFin;
    this.minutosFin = minutosFin;
    this.unidadesInsulina = unidadesInsulina;
  }

  public Integer getHoraInicio() {
    return horaInicio;
  }

  public void setHoraInicio(Integer horaInicio) {
    this.horaInicio = horaInicio;
  }

  public Integer getMinutosInicio() {
    return minutosInicio;
  }

  public void setMinutosInicio(Integer minutosInicio) {
    this.minutosInicio = minutosInicio;
  }

  public Integer getHoraFin() {
    return horaFin;
  }

  public void setHoraFin(Integer horaFin) {
    this.horaFin = horaFin;
  }

  public Integer getMinutosFin() {
    return minutosFin;
  }

  public void setMinutosFin(Integer minutosFin) {
    this.minutosFin = minutosFin;
  }

  public Float getUnidadesInsulina() {
    return unidadesInsulina;
  }

  public void setUnidadesInsulina(Float unidadesInsulina) {
    this.unidadesInsulina = unidadesInsulina;
  }
}
