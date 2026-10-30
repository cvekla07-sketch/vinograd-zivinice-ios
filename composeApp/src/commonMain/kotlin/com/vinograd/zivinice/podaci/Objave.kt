package com.vinograd.zivinice.podaci

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.vinograd.zivinice.platform.kreirajDataStore
import com.vinograd.zivinice.platform.sadaMs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Objava "Novo iz vinograda" (Firebase objave/{id}; slika odvojeno u objaveSlike/{id}). */
data class Objava(
    val id: String,
    val naslov: String,
    val tekst: String,
    val video: String?,
    val sorta: String?,
    val imaSliku: Boolean,
    val vrijeme: Long,
)

/**
 * Objave iz admin panela. Pročitane se pamte na telefonu.
 * "Nova" = nepročitana objava iz zadnjih 7 dana (novi korisnik ne dobije stare objave kao nepročitane).
 */
object Objave {
    private const val NAJVISE = 30
    private const val NOVA_MS = 7 * 86_400_000L

    private val _lista = MutableStateFlow<List<Objava>>(emptyList())
    val lista: StateFlow<List<Objava>> = _lista

    private val _procitane = MutableStateFlow<Set<String>>(emptySet())
    val procitane: StateFlow<Set<String>> = _procitane

    private val ds by lazy { kreirajDataStore("vinograd_objave") }
    private val K_PROCITANE = stringSetPreferencesKey("procitane")

    /** Keš slika (data-URL) — slika se povlači tek kad se objava prikaže. */
    private val slike = mutableMapOf<String, String>()

    /** Učita objave (Uzivo ga zove na svaku promjenu u bazi). */
    suspend fun osvjezi() {
        val odgovor = Baza.citajIliPrazno("objave") ?: return          // nema interneta → ostaje stara lista
        val snap = odgovor as? JsonObject ?: run { _lista.value = emptyList(); return }   // obrisane sve objave
        _lista.value = snap.mapNotNull { (id, v) ->
            val d = v as? JsonObject ?: return@mapNotNull null
            val naslov = d.tekst("naslov")?.trim().orEmpty()
            if (naslov.isEmpty()) return@mapNotNull null
            Objava(
                id = id,
                naslov = naslov,
                tekst = d.tekst("tekst")?.trim().orEmpty(),
                video = d.tekst("video")?.trim()?.ifEmpty { null },
                sorta = d.tekst("sorta")?.trim()?.ifEmpty { null },
                imaSliku = d.istina("imaSliku") == true,
                vrijeme = d.dugi("vrijeme") ?: 0L,
            )
        }.sortedByDescending { it.vrijeme }.take(NAJVISE)
    }

    /** Pročitane objave sa telefona — jednom pri pokretanju. */
    suspend fun ucitajProcitane() {
        try {
            _procitane.value = ds.data.first()[K_PROCITANE] ?: emptySet()
        } catch (_: Exception) {
        }
    }

    fun jeNova(o: Objava, procitane: Set<String>): Boolean =
        o.id !in procitane && sadaMs() - o.vrijeme < NOVA_MS

    /** Objava otvorena: statistika + označi kao pročitanu. */
    fun otvorena(id: String) {
        Stat.objava(id, Stat.OBJAVA_OTVORENA)
        if (id in _procitane.value) return
        // Pamte se samo objave koje još postoje — lista ne raste unedogled
        val postojece = _lista.value.map { it.id }.toSet()
        val stare = if (postojece.isEmpty()) _procitane.value else _procitane.value.filter { it in postojece }
        val nove = (stare + id).toSet()
        _procitane.value = nove
        Stat.opseg.launch {
            try {
                ds.edit { it[K_PROCITANE] = nove }
            } catch (_: Exception) {
            }
        }
    }

    /** Slika objave (data-URL) ili null. */
    suspend fun slika(id: String): String? {
        slike[id]?.let { return it }
        val s = (Baza.citaj("objaveSlike/$id") as? JsonPrimitive)?.takeIf { it.isString }?.content ?: return null
        slike[id] = s
        return s
    }
}
