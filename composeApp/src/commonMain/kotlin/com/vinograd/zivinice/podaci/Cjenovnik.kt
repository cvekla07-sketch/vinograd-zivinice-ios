package com.vinograd.zivinice.podaci

import com.vinograd.zivinice.ui.TIP_SADNICA
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonObject
import kotlin.math.roundToLong

/**
 * Cijene jedne sorte — Firebase "cjenovnik/{kljuc}/" (1:1 Cijena.java).
 * naStanju* — ako polje ne postoji u bazi, podrazumijeva se da IMA na stanju.
 */
data class Cijena(
    val cijenaSadnica: Double = 0.0,
    val cijenaReznica: Double = 0.0,
    val vrijemeAzuriranja: Long = 0,
    val naStanjuSadnica: Boolean = true,
    val naStanjuReznica: Boolean = true,
) {
    fun imaCijenu(): Boolean = cijenaSadnica > 0 || cijenaReznica > 0

    /** Da li se dati tip ("sadnica"/"reznica") trenutno može naručiti. */
    fun naStanju(tip: String): Boolean = if (tip == TIP_SADNICA) naStanjuSadnica else naStanjuReznica

    /** Ni sadnica ni reznica nisu dostupne. */
    fun nistaNaStanju(): Boolean = !naStanjuSadnica && !naStanjuReznica
}

/** Keš cjenovnika (1:1 CjenovnikKes.java). */
object Cjenovnik {
    private val _mapa = MutableStateFlow<Map<String, Cijena>>(emptyMap())
    val mapa: StateFlow<Map<String, Cijena>> = _mapa

    private val _ucitan = MutableStateFlow(false)
    val ucitan: StateFlow<Boolean> = _ucitan

    /** Čita "cjenovnik/" iz baze. Greška / bez interneta → ostaje zadnje poznato stanje. */
    suspend fun osvjezi() {
        val snap = Baza.citaj("cjenovnik") ?: return
        val obj = snap as? JsonObject ?: return
        val nova = mutableMapOf<String, Cijena>()
        for ((kljuc, v) in obj) {
            val c = v as? JsonObject ?: continue
            nova[kljuc] = Cijena(
                cijenaSadnica = c.broj("cijenaSadnica") ?: 0.0,
                cijenaReznica = c.broj("cijenaReznica") ?: 0.0,
                vrijemeAzuriranja = c.dugi("vrijemeAzuriranja") ?: 0,
                naStanjuSadnica = c.istina("naStanjuSadnica") ?: true,
                naStanjuReznica = c.istina("naStanjuReznica") ?: true,
            )
        }
        _mapa.value = nova
        _ucitan.value = true
    }

    fun cijena(kljuc: String?): Cijena? = kljuc?.let { _mapa.value[it] }

    fun cijenaZaNaziv(naziv: String): Cijena? = cijena(Sorte.kljucZa(naziv))

    /** Cijena jednog komada za (naziv, tip) ili 0 ako nije postavljena. */
    fun jedinicna(naziv: String, tip: String): Double {
        val c = cijenaZaNaziv(naziv) ?: return 0.0
        return if (tip == TIP_SADNICA) c.cijenaSadnica else c.cijenaReznica
    }

    /** Najniža postavljena cijena (sadnica ili reznica) — za "od X KM"; 0 ako nema. */
    fun najniza(naziv: String): Double {
        val c = cijenaZaNaziv(naziv) ?: return 0.0
        val s = c.cijenaSadnica
        val r = c.cijenaReznica
        if (s > 0 && r > 0) return minOf(s, r)
        return maxOf(s, r)
    }

    /**
     * Prikaz obje cijene za karticu, svaka u svom redu:
     *   "🌱 Sadnica 15,00 KM\n✂️ Reznica 7,00 KM"
     * Ako nijedna nije postavljena → "Cijena na upit".
     */
    fun prikazObje(naziv: String): String {
        val c = cijenaZaNaziv(naziv)
        val s = if (c != null && c.naStanjuSadnica) c.cijenaSadnica else 0.0
        val r = if (c != null && c.naStanjuReznica) c.cijenaReznica else 0.0
        if (c != null && c.nistaNaStanju()) return "Nema na stanju"
        if (s <= 0 && r <= 0) return "Cijena na upit"
        val sb = StringBuilder()
        if (s > 0) sb.append("🌱 Sadnica ").append(format(s))
        if (r > 0) {
            if (sb.isNotEmpty()) sb.append("\n")
            sb.append("✂️ Reznica ").append(format(r))
        }
        return sb.toString()
    }

    /** "12,00 KM" ili "—" */
    fun format(iznos: Double): String {
        if (iznos <= 0) return "—"
        val centi = (iznos * 100).roundToLong()
        return "${centi / 100},${(centi % 100).toString().padStart(2, '0')} KM"
    }
}
