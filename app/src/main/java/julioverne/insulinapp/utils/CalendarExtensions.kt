@file:JvmName("CalendarExtensions")

package julioverne.insulinapp.utils

import java.util.*

var Calendar.hour: Int
    get() = get(Calendar.HOUR)
    set(hour) {
        set(Calendar.HOUR, hour)
    }

var Calendar.minute: Int
    get() = get(Calendar.MINUTE)
    set(minute) {
        set(Calendar.MINUTE, minute)
    }

fun now(): Date = Calendar.getInstance().time