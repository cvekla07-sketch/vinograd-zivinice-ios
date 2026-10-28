package com.vinograd.zivinice.podaci

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonObject

/**
 * Stanje prodajne sezone — Firebase "postavke/sezona" (1:1 Sezona.java).
 *   status: "auto" (po datumima) | "otvorena" | "zatvorena"
 *   poruka: opcionalni tekst ispod naslova na Home ekranu
 * Dok podaci ne stignu (ili nema interneta) ponaša se kao "auto".
 */
object Sezona {
    const val AUTO = "auto"
    const val OTVORENA = "otvorena"
    const val ZATVORENA = "zatvorena"

    data class Stanje(val status: String = AUTO, val poruka: String? = null)

    private val _stanje = MutableStateFlow(Stanje())
    val stanje: StateFlow<Stanje> = _stanje

    suspend fun osvjezi() {
        val s = Baza.citaj("postavke/sezona") as? JsonObject
        if (s == null) {
            return
        }
        val st = s.tekst("status")
        val p = s.tekst("poruka")?.trim()
        _stanje.value = Stanje(
            status = if (st == OTVORENA || st == ZATVORENA) st else AUTO,
            poruka = if (p.isNullOrEmpty()) null else p,
        )
    }

    fun status(): String = _stanje.value.status
    fun zatvorena(): Boolean = _stanje.value.status == ZATVORENA
    fun otvorena(): Boolean = _stanje.value.status == OTVORENA
    fun poruka(): String? = _stanje.value.poruka

    /** Tekst za kupca kad pokuša naručiti/rezervisati dok je sezona zatvorena. */
    fun porukaZatvoreno(): String = poruka()
        ?: ("Prodajna sezona je završena. Katalog i cijene možete pregledati, " +
            "a narudžbe i rezervacije će ponovo biti moguće kad otvorimo novu sezonu. Hvala na povjerenju! 🍇")
}
