package julioverne.insulinapp.ui.dialogs

import julioverne.insulinapp.data.dao.EntradaDAO
import julioverne.insulinapp.utils.DateUtils

fun EntradaDAO.formatEntryTitle() = DateUtils.dateToString(fecha) + " - " + DateUtils.dateToHour(fecha)