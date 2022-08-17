@file:JvmName("StringExt")

package julioverne.insulinapp.extensions

import java.text.Normalizer

fun String.normalize() =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .toLowerCase()
        .replace(",".toRegex(), "")
        .replace("-".toRegex(), "")
        .replace("/".toRegex(), "")
        .replace(":".toRegex(), "")
        .replace("\"".toRegex(), "")
        .replace("€".toRegex(), "e")
        .replace("\\$".toRegex(), "s")
        .replace("¡".toRegex(), "i")
        .replace("!".toRegex(), "l")
        .replace("\\*".toRegex(), "")