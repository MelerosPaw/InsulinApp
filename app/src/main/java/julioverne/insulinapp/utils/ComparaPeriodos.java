package julioverne.insulinapp.utils;

import java.util.Comparator;
import julioverne.insulinapp.data.dao.PeriodoDAO;

/**
 * Created by Juan José Melero on 18/06/2015.<br/>
 * Compara periodos por la horaInicio para ordenar de menor a mayor
 */
public class ComparaPeriodos implements Comparator<PeriodoDAO> {

    @Override
    public int compare(PeriodoDAO lhs, PeriodoDAO rhs) {
        return lhs.getHoraInicio().compareTo(rhs.getHoraInicio());
    }
}
