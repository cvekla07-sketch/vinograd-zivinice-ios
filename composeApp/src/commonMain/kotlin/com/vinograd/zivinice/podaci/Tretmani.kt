package com.vinograd.zivinice.podaci

import kotlinx.serialization.json.JsonObject

/** Tretman za jedan mjesec — Firebase "tretmani/2026/{mjesec}" (1:1 Tretman.java). */
data class Tretman(val tekst: String?, val vrijemeAzuriranja: Long = 0) {
    fun jePopunjen(): Boolean = !tekst.isNullOrBlank()
}

object Tretmani {
    /** Ista godina kao na Androidu (TretmaniActivity.GODINA). */
    const val GODINA = "2026"

    val MJESECI_KEY = listOf(
        "01_januar", "02_februar", "03_mart", "04_april", "05_maj", "06_juni",
        "07_juli", "08_august", "09_septembar", "10_oktobar", "11_novembar", "12_decembar",
    )

    /** Ključevi mjeseci koji imaju upisan tekst (prazno ako nema podataka ili interneta). */
    suspend fun popunjeniMjeseci(): Set<String> {
        val obj = Baza.citaj("tretmani/$GODINA") as? JsonObject ?: return emptySet()
        return obj.filter { (_, v) -> !(v as? JsonObject)?.tekst("tekst").isNullOrBlank() }.keys
    }

    /** Tretman za jedan mjesec; null ako ga nema ili nema interneta. */
    suspend fun tretman(mjesecKey: String): Tretman? {
        val t = Baza.citaj("tretmani/$GODINA/$mjesecKey") as? JsonObject ?: return null
        return Tretman(t.tekst("tekst"), t.dugi("vrijemeAzuriranja") ?: 0)
    }
}
