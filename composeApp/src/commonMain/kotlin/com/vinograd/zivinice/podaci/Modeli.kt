package com.vinograd.zivinice.podaci

import com.vinograd.zivinice.platform.sadaMs
import com.vinograd.zivinice.ui.TIP_REZNICA
import com.vinograd.zivinice.ui.TIP_SADNICA
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/*
 * Modeli koji se šalju u bazu — ISTI nazivi polja kao Android (Firebase ih piše iz gettera),
 * uključujući izvedena polja tipPrikaz (StavkaNarudzbe) i opis (StavkaRezervacije),
 * da admin panel radi bez izmjena.
 */

/** Stavka u korpi (lokalno). Ključ za prepoznavanje = tip + sorta. */
data class StavkaKorpe(val sorta: String, val tip: String, val kolicina: Int) {
    val kljuc: String get() = "$tip::$sorta"
    val opis: String get() = "${if (tip == TIP_SADNICA) "Sadnica" else "Reznica"} · $sorta — $kolicina kom"
}

/** Stavka narudžbe (1:1 StavkaNarudzbe.java). */
data class StavkaNarudzbe(
    val tip: String,
    val sorta: String,
    val kolicina: Int,
    /** Cijena po komadu u trenutku narudžbe (0 = nije bila postavljena). */
    val cijena: Double = 0.0,
) {
    val tipPrikaz: String
        get() = when (tip) {
            TIP_SADNICA -> "Sadnica"
            TIP_REZNICA -> "Reznica"
            else -> tip
        }

    fun uJson(): JsonObject = buildJsonObject {
        put("tip", tip)
        put("sorta", sorta)
        put("kolicina", kolicina)
        put("cijena", cijena)
        put("tipPrikaz", tipPrikaz)
    }
}

/** Narudžba (1:1 Narudzba.java). */
data class Narudzba(
    val id: String,
    val ime: String,
    val prezime: String,
    val telefon: String,
    val grad: String,
    val adresa: String,
    val napomena: String,
    /** Narudžba koristi "Viber" / "WhatsApp" / "Email" (kao NarudzbaActivity). */
    val nacinSlanjaRacuna: String,
    val kontaktZaRacun: String,
    val postanskiBroj: String,
    val ukupnoIznos: Double,
    val stavke: List<StavkaNarudzbe>,
    val status: String = STATUS_NOVA,
    val vrijeme: Long = sadaMs(),
) {
    fun uJson(): JsonObject = buildJsonObject {
        put("id", id)
        put("ime", ime)
        put("prezime", prezime)
        put("telefon", telefon)
        put("grad", grad)
        put("adresa", adresa)
        put("napomena", napomena)
        put("status", status)
        put("vrijeme", vrijeme)
        put("stavke", buildJsonArray { stavke.forEach { add(it.uJson()) } })
        put("nacinSlanjaRacuna", nacinSlanjaRacuna)
        put("kontaktZaRacun", kontaktZaRacun)
        put("postanskiBroj", postanskiBroj)
        put("ukupnoIznos", ukupnoIznos)
    }

    companion object {
        const val STATUS_NOVA = "nova"
        const val NACIN_VIBER = "Viber"
        const val NACIN_WHATSAPP = "WhatsApp"
        const val NACIN_EMAIL = "Email"
    }
}

/** Stavka rezervacije — sadnice i reznice iste sorte u jednoj stavci (1:1 StavkaRezervacije.java). */
data class StavkaRezervacije(val sorta: String, val kolicinaSadnica: Int, val kolicinaReznica: Int) {
    fun jePrazna(): Boolean = kolicinaSadnica <= 0 && kolicinaReznica <= 0

    val opis: String
        get() {
            val sb = StringBuilder("$sorta — ")
            if (kolicinaSadnica > 0) sb.append(kolicinaSadnica).append(" sad")
            if (kolicinaSadnica > 0 && kolicinaReznica > 0) sb.append(", ")
            if (kolicinaReznica > 0) sb.append(kolicinaReznica).append(" rez")
            return sb.toString()
        }

    fun uJson(): JsonObject = buildJsonObject {
        put("sorta", sorta)
        put("kolicinaSadnica", kolicinaSadnica)
        put("kolicinaReznica", kolicinaReznica)
        put("opis", opis)
    }
}

/** Rezervacija (1:1 Rezervacija.java). */
data class Rezervacija(
    val id: String,
    val ime: String,
    val prezime: String,
    val telefon: String,
    val grad: String,
    val adresa: String,
    val napomena: String,
    /** Rezervacija koristi "viber" / "whatsapp" / "email" (kao Rezervacija.java). */
    val nacinSlanjaRacuna: String,
    val kontaktZaRacun: String,
    val postanskiBroj: String,
    val stavke: List<StavkaRezervacije>,
    val status: String = STATUS_NOVA,
    val vrijeme: Long = sadaMs(),
) {
    fun uJson(): JsonObject = buildJsonObject {
        put("id", id)
        put("ime", ime)
        put("prezime", prezime)
        put("telefon", telefon)
        put("grad", grad)
        put("adresa", adresa)
        put("napomena", napomena)
        put("nacinSlanjaRacuna", nacinSlanjaRacuna)
        put("kontaktZaRacun", kontaktZaRacun)
        put("postanskiBroj", postanskiBroj)
        put("status", status)
        put("vrijeme", vrijeme)
        put("stavke", buildJsonArray { stavke.forEach { add(it.uJson()) } })
    }

    companion object {
        const val STATUS_NOVA = "nova"
        const val NACIN_EMAIL = "email"
        const val NACIN_WHATSAPP = "whatsapp"
        const val NACIN_VIBER = "viber"
    }
}
