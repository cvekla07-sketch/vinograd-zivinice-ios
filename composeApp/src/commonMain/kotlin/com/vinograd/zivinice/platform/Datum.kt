package com.vinograd.zivinice.platform

/** Kalendarski datum (mjesec 1..12) — zamjena za java.util.Calendar u logici sezone. */
data class Datum(val godina: Int, val mjesec: Int, val dan: Int) : Comparable<Datum> {
    /** Broj dana od 1.1.1970. */
    val epohaDan: Long
        get() {
            val y = if (mjesec <= 2) godina - 1 else godina
            val era = (if (y >= 0) y else y - 399) / 400
            val yoe = y - era * 400
            val m = mjesec
            val doy = (153 * (if (m > 2) m - 3 else m + 9) + 2) / 5 + dan - 1
            val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
            return era.toLong() * 146097 + doe - 719468
        }

    override fun compareTo(other: Datum): Int = epohaDan.compareTo(other.epohaDan)

    companion object {
        fun izEpohaDana(z0: Long): Datum {
            val z = z0 + 719468
            val era = (if (z >= 0) z else z - 146096) / 146097
            val doe = z - era * 146097
            val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
            val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
            val mp = (5 * doy + 2) / 153
            val d = (doy - (153 * mp + 2) / 5 + 1).toInt()
            val m = (if (mp < 10) mp + 3 else mp - 9).toInt()
            val y = (yoe + era * 400 + (if (m <= 2) 1 else 0)).toInt()
            return Datum(y, m, d)
        }

        /** Današnji datum po lokalnoj vremenskoj zoni telefona. */
        fun danas(): Datum = izEpohaDana((sadaMs() + pomakZoneMs()).floorDiv(86_400_000L))
    }
}
