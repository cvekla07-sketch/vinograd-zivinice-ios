package com.vinograd.zivinice.podaci

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.vinograd.zivinice.platform.kreirajDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/*
 * Lokalni podaci na telefonu (zamjena za SharedPreferences, ista imena fajlova i ključeva).
 * Ništa odavde ne ide u Firebase.
 */

private fun parsirajNiz(json: String?): List<JsonObject> = try {
    (Json.parseToJsonElement(json ?: "[]") as? JsonArray)?.mapNotNull { it as? JsonObject } ?: emptyList()
} catch (e: Exception) {
    emptyList()
}

// ======================= KORPA (1:1 Korpa.java) =======================

object Korpa {
    private val ds by lazy { kreirajDataStore("vinograd_korpa") }
    private val KLJUC = stringPreferencesKey("stavke_json")

    /** Stavke korpe — prati promjene (za značku na ikoni korpe). */
    val stavke: Flow<List<StavkaKorpe>> get() = ds.data.map { procitaj(it[KLJUC]) }

    private fun procitaj(json: String?): List<StavkaKorpe> = try {
        parsirajNiz(json).map {
            StavkaKorpe(
                sorta = it.tekst("sorta")!!,
                tip = it.tekst("tip")!!,
                kolicina = it.dugi("kolicina")!!.toInt(),
            )
        }
    } catch (e: Exception) {
        emptyList()   // u slučaju greške — prazna korpa (kao Android)
    }

    private fun uJson(stavke: List<StavkaKorpe>): String = buildJsonArray {
        stavke.forEach {
            add(buildJsonObject {
                put("sorta", it.sorta)
                put("tip", it.tip)
                put("kolicina", it.kolicina)
            })
        }
    }.toString()

    private suspend fun izmijeni(promjena: (MutableList<StavkaKorpe>) -> Unit) {
        ds.edit { p ->
            val lista = procitaj(p[KLJUC]).toMutableList()
            promjena(lista)
            p[KLJUC] = uJson(lista)
        }
    }

    suspend fun dohvatiSveStavke(): List<StavkaKorpe> = stavke.first()

    /** Dodaje sortu u korpu; ako već postoji ista (tip + sorta), povećava količinu. */
    suspend fun dodaj(sorta: String, tip: String, kolicina: Int) {
        if (sorta.isEmpty() || kolicina <= 0) return
        izmijeni { l ->
            val i = l.indexOfFirst { it.sorta == sorta && it.tip == tip }
            if (i >= 0) l[i] = l[i].copy(kolicina = l[i].kolicina + kolicina)
            else l.add(StavkaKorpe(sorta, tip, kolicina))
        }
    }

    /** Mijenja količinu stavke; ako je <= 0, briše stavku. */
    suspend fun postaviKolicinu(sorta: String, tip: String, novaKolicina: Int) {
        izmijeni { l ->
            if (novaKolicina <= 0) {
                l.removeAll { it.sorta == sorta && it.tip == tip }
            } else {
                val i = l.indexOfFirst { it.sorta == sorta && it.tip == tip }
                if (i >= 0) l[i] = l[i].copy(kolicina = novaKolicina)
            }
        }
    }

    suspend fun obrisi(sorta: String, tip: String) {
        izmijeni { l -> l.removeAll { it.sorta == sorta && it.tip == tip } }
    }

    /** Broj stavki (sorti), ne komada. */
    suspend fun brojStavki(): Int = dohvatiSveStavke().size

    suspend fun ukupanBrojKomada(): Int = dohvatiSveStavke().sumOf { it.kolicina }

    /** Prazni korpu (nakon uspješno poslane narudžbe). */
    suspend fun isprazni() {
        ds.edit { it.remove(KLJUC) }
    }

    suspend fun jePrazna(): Boolean = brojStavki() == 0
}

// ======================= KUPAC (1:1 KupacPodaci.java) =======================

/** Podaci kupca zapamćeni na uređaju, da ih ne kuca ponovo. */
data class KupacPodaci(
    val ime: String = "",
    val prezime: String = "",
    val telefon: String = "",
    val grad: String = "",
    val postanskiBroj: String = "",
    val adresa: String = "",
    val nacinRacuna: String = "",
    val kontaktZaRacun: String = "",
) {
    fun imaPodatke(): Boolean = ime.isNotEmpty() || telefon.isNotEmpty()

    suspend fun sacuvaj() {
        ds.edit { p ->
            p[K_IME] = ime
            p[K_PREZIME] = prezime
            p[K_TELEFON] = telefon
            p[K_GRAD] = grad
            p[K_POSTANSKI] = postanskiBroj
            p[K_ADRESA] = adresa
            p[K_NACIN] = nacinRacuna
            p[K_KONTAKT] = kontaktZaRacun
        }
    }

    companion object {
        private val ds by lazy { kreirajDataStore("vinograd_kupac") }
        private val K_IME = stringPreferencesKey("ime")
        private val K_PREZIME = stringPreferencesKey("prezime")
        private val K_TELEFON = stringPreferencesKey("telefon")
        private val K_GRAD = stringPreferencesKey("grad")
        private val K_POSTANSKI = stringPreferencesKey("postanskiBroj")
        private val K_ADRESA = stringPreferencesKey("adresa")
        private val K_NACIN = stringPreferencesKey("nacinRacuna")
        private val K_KONTAKT = stringPreferencesKey("kontaktZaRacun")

        suspend fun ucitaj(): KupacPodaci {
            val p = ds.data.first()
            return KupacPodaci(
                ime = p[K_IME] ?: "",
                prezime = p[K_PREZIME] ?: "",
                telefon = p[K_TELEFON] ?: "",
                grad = p[K_GRAD] ?: "",
                postanskiBroj = p[K_POSTANSKI] ?: "",
                adresa = p[K_ADRESA] ?: "",
                nacinRacuna = p[K_NACIN] ?: "",
                kontaktZaRacun = p[K_KONTAKT] ?: "",
            )
        }
    }
}

// ======================= DNEVNIK (1:1 DnevnikStorage.java + Biljeska.java) =======================

/** Bilješka u dnevniku — privatna, samo na telefonu. */
data class Biljeska(
    val id: String,        // jedinstveni ID (timestamp)
    val datum: Long,       // datum bilješke (millis)
    val tekst: String,     // šta je urađeno
    val kategorija: String // prskanje / rezidba / berba / ...
)

object Dnevnik {
    private val ds by lazy { kreirajDataStore("dnevnik_vinograda") }
    private val KLJUC = stringPreferencesKey("biljeske")

    private fun procitaj(json: String?): List<Biljeska> = parsirajNiz(json).mapNotNull { o ->
        val id = o.tekst("id") ?: return@mapNotNull null
        val datum = o.dugi("datum") ?: return@mapNotNull null
        val tekst = o.tekst("tekst") ?: return@mapNotNull null
        Biljeska(id, datum, tekst, o.tekst("kategorija") ?: "Ostalo")
    }

    private fun uJson(lista: List<Biljeska>): String = buildJsonArray {
        lista.forEach {
            add(buildJsonObject {
                put("id", it.id)
                put("datum", it.datum)
                put("tekst", it.tekst)
                put("kategorija", it.kategorija)
            })
        }
    }.toString()

    /** Sve bilješke, od najnovije ka najstarijoj. */
    val biljeske: Flow<List<Biljeska>>
        get() = ds.data.map { p -> procitaj(p[KLJUC]).sortedByDescending { it.datum } }

    suspend fun dohvatiSve(): List<Biljeska> = biljeske.first()

    private suspend fun izmijeniListu(promjena: (MutableList<Biljeska>) -> Unit) {
        ds.edit { p ->
            val l = procitaj(p[KLJUC]).toMutableList()
            promjena(l)
            p[KLJUC] = uJson(l)
        }
    }

    suspend fun dodaj(nova: Biljeska) = izmijeniListu { it.add(nova) }

    suspend fun izmijeni(b: Biljeska) = izmijeniListu { l ->
        val i = l.indexOfFirst { it.id == b.id }
        if (i >= 0) l[i] = b
    }

    suspend fun obrisi(id: String) = izmijeniListu { l -> l.removeAll { it.id == id } }
}
