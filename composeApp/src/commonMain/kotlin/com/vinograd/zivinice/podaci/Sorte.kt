package com.vinograd.zivinice.podaci

import com.vinograd.zivinice.opisSorte
import com.vinograd.zivinice.ui.BojaBobice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonObject

/** Jedna sorta — kao Sorte.Sorta na Androidu. */
data class Sorta(
    val naziv: String,
    val kljuc: String,
    val boja: BojaBobice,
    val kismis: Boolean,
    /** Iz Firebasea; null → opis iz composeResources/files/opisi. */
    val opis: String? = null,
    /** Iz Firebasea; null → slika iz composeResources/drawable. */
    val slikaUrl: String? = null,
) {
    val tipPrikaz: String get() = if (kismis) "Kišmiš" else "Stolna"
}

/**
 * Centralni registar sorti (1:1 Sorte.java).
 *  1) UGRAĐENA lista — radi odmah i bez interneta.
 *  2) Firebase "sorte/{kljuc}" — kad stigne, ZAMJENJUJE ugrađenu listu.
 */
object Sorte {
    private val ugradjene = mutableListOf<Sorta>()

    private val _sve = MutableStateFlow<List<Sorta>>(emptyList())
    /** Sve aktivne sorte, abecedno (ugrađene dok Firebase ne stigne). */
    val sve: StateFlow<List<Sorta>> = _sve

    private val _izFirebase = MutableStateFlow(false)
    val izFirebase: StateFlow<Boolean> = _izFirebase

    private var poNazivu: Map<String, Sorta> = emptyMap()
    private var poKljucu: Map<String, Sorta> = emptyMap()

    private fun s(naziv: String, boja: BojaBobice) = s(naziv, kljucOd(naziv), boja)
    private fun s(naziv: String, kljuc: String, boja: BojaBobice) {
        ugradjene.add(Sorta(naziv, kljuc, boja, kismisIzNaziva(naziv)))
    }

    init {
        s("Alinka", BojaBobice.BIJELA)
        s("Almira", BojaBobice.BIJELA)
        s("Alvika", BojaBobice.CRNA)
        s("Aromatika", BojaBobice.BIJELA)
        s("Arkadia", BojaBobice.BIJELA)
        s("Avtograf Kalugina", BojaBobice.CRNA)
        s("Azija", BojaBobice.CRNA)
        s("Bajkonur", BojaBobice.CRNA)
        s("Bananas", BojaBobice.BIJELA)
        s("Basanti", BojaBobice.ROZA)
        s("Bila Nadija", BojaBobice.BIJELA)
        s("Bijela lejdi", BojaBobice.BIJELA)
        s("Bogatjanovski", BojaBobice.BIJELA)
        s("Bogema", BojaBobice.ROZA)
        s("Bravisimo", BojaBobice.CRNA)
        s("Bronislav", BojaBobice.BIJELA)
        s("Bruno", BojaBobice.CRNA)
        s("Buba", BojaBobice.BIJELA)
        s("Crni kristal", BojaBobice.CRNA)
        s("Damska krasunja", BojaBobice.ROZA)  // ?
        s("Damski ugodnik", BojaBobice.BIJELA)
        s("Dikson", BojaBobice.ROZA)
        s("Dnjeprovski suvenir", BojaBobice.ROZA)  // ?
        s("Dzoker", BojaBobice.ROZA)
        s("Elina", BojaBobice.ROZA)  // ?
        s("Emir", BojaBobice.CRNA)
        s("Energodar", BojaBobice.BIJELA)
        s("Etjud", BojaBobice.CRNA)  // ?
        s("Everest", BojaBobice.CRNA)
        s("Glamur", BojaBobice.ROZA)
        s("Grafinija", BojaBobice.CRNA)
        s("Halloween", BojaBobice.CRNA)
        s("Halloween 2", BojaBobice.ROZA)
        s("Halloween bijeli", BojaBobice.BIJELA)
        s("Hilton", BojaBobice.BIJELA)
        s("Husein Kelin Barmak", BojaBobice.BIJELA)
        s("Ilona", BojaBobice.ROZA)
        s("Imperatricja", BojaBobice.ROZA)
        s("Itum 9", BojaBobice.ROZA)  // ?
        s("Itum 10", BojaBobice.CRNA)  // ?
        s("Julian", BojaBobice.ROZA)
        s("Jubilej Novočerkaska", BojaBobice.ROZA)
        s("Kameleon", BojaBobice.ROZA)
        s("Karolina Kalugina", BojaBobice.ROZA)  // ?
        s("Kataleja", BojaBobice.ROZA)
        s("Katalonia", BojaBobice.CRNA)
        s("Karnaval", BojaBobice.ROZA)
        s("Kazanova", BojaBobice.ROZA)
        s("Kazimir", BojaBobice.BIJELA)
        s("Kišmiš Lučistij", "kismislucistij", BojaBobice.ROZA)  // ispravljen tipfeler "Kišimiš"
        s("Kišmiš Alisa iz zemlje čuda", BojaBobice.ROZA)
        s("Kišmiš Ararat", BojaBobice.BIJELA)
        s("Kišmiš Avatar", BojaBobice.CRNA)
        s("Kišmiš Brat Vavilona", BojaBobice.ROZA)
        s("Kišmiš Čornobaivka", BojaBobice.ROZA)  // ?
        s("Kišmiš Cimus", BojaBobice.BIJELA)
        s("Kišmiš Geliodor", BojaBobice.BIJELA)
        s("Kišmiš Jumbo", BojaBobice.ROZA)  // ?
        s("Kišmiš Lamborghini", BojaBobice.BIJELA)
        s("Kišmiš Land Kruzer", BojaBobice.BIJELA)
        s("Kišmiš marmeladnij", BojaBobice.ROZA)
        s("Kišmiš Pandora", BojaBobice.ROZA)  // ?
        s("Kišmiš Ratatuj", BojaBobice.BIJELA)
        s("Kišmiš Solomija", BojaBobice.BIJELA)
        s("Kišmiš Šarlota", BojaBobice.CRNA)
        s("Kišmiš Veles", BojaBobice.ROZA)
        s("KM Samuraj", "kismissamuraj", BojaBobice.ROZA)  // ? ključ uparen sa fajlovima
        s("Kodrianka", BojaBobice.CRNA)
        s("Krasava", BojaBobice.BIJELA)
        s("Ksenija", BojaBobice.ROZA)
        s("Kumir", BojaBobice.ROZA)  // ?
        s("Kvartet", BojaBobice.CRNA)
        s("Landiš", BojaBobice.BIJELA)
        s("Maljčik s paljčik", BojaBobice.BIJELA)
        s("Marmeladka", BojaBobice.CRNA)
        s("Maša", BojaBobice.ROZA)
        s("Moldavia", BojaBobice.CRNA)
        s("Mon Blan", BojaBobice.BIJELA)
        s("Mustang", BojaBobice.CRNA)
        s("Pamjati Bronislava", BojaBobice.CRNA)
        s("Pervenec rani", BojaBobice.CRNA)
        s("Podarok Energodaru", BojaBobice.BIJELA)
        s("Podarok nesvetaja", BojaBobice.BIJELA)
        s("Polonez 50", BojaBobice.ROZA)
        s("Poltavski manjukur", BojaBobice.ROZA)
        s("Pompei", BojaBobice.CRNA)
        s("Poslanik", BojaBobice.CRNA)
        s("Preobraženje", BojaBobice.ROZA)
        s("Rafinad", BojaBobice.BIJELA)
        s("Rambo", BojaBobice.CRNA)
        s("Rombik", BojaBobice.CRNA)
        s("Romansa", BojaBobice.ROZA)
        s("Rubinov Jubilej", BojaBobice.ROZA)
        s("Rumejka", BojaBobice.ROZA)
        s("Rusko rano", BojaBobice.BIJELA)
        s("Sfinks", BojaBobice.CRNA)
        s("Sjećanje na učitelja", BojaBobice.BIJELA)
        s("Slava Ukrajine", BojaBobice.BIJELA)
        s("Standart", BojaBobice.BIJELA)
        s("Stoletie", BojaBobice.BIJELA)
        s("Striž", BojaBobice.CRNA)
        s("Sugra 48", BojaBobice.ROZA)  // ?
        s("Sugra 53", BojaBobice.BIJELA)  // ?
        s("Sweet Celebration", BojaBobice.ROZA)  // ?
        s("Šarada", BojaBobice.CRNA)
        s("Šćelkunčik", BojaBobice.ROZA)
        s("Tajson", BojaBobice.CRNA)
        s("Taldun", BojaBobice.ROZA)
        s("Timoša", BojaBobice.BIJELA)  // ? (nema slike ni opisa)
        s("Urum", BojaBobice.ROZA)
        s("Vatra", BojaBobice.CRNA)
        s("Veljur", BojaBobice.CRNA)
        s("Velora", BojaBobice.ROZA)  // ?
        s("Viktor", BojaBobice.ROZA)
        s("Vzljot", BojaBobice.BIJELA)
        s("Zolotij telec", BojaBobice.BIJELA)
        postaviListu(ugradjene)
    }

    private fun postaviListu(nova: List<Sorta>) {
        poNazivu = nova.associateBy { it.naziv }
        poKljucu = nova.associateBy { it.kljuc }
        _sve.value = nova.toList()
    }

    /** Čita "sorte/" iz baze (kao pokreniSync). Prazna baza / bez interneta → ostaje zadnja lista. */
    suspend fun osvjezi() {
        val snap = Baza.citaj("sorte") as? JsonObject ?: return
        val lista = mutableListOf<Sorta>()
        for ((kljuc, v) in snap) {
            val d = v as? JsonObject ?: continue
            val naziv = d.tekst("naziv")
            if (naziv.isNullOrBlank()) continue
            if (d.istina("aktivna") == false) continue
            lista.add(
                Sorta(
                    naziv = naziv.trim(),
                    kljuc = kljuc,
                    boja = bojaIz(d.tekst("boja")),
                    kismis = d.istina("kismis") ?: kismisIzNaziva(naziv),
                    opis = d.tekst("opis")?.trim()?.ifEmpty { null },
                    slikaUrl = d.tekst("slikaUrl")?.trim()?.ifEmpty { null },
                )
            )
        }
        if (lista.isEmpty()) return
        lista.sortWith { a, b -> a.naziv.compareTo(b.naziv, ignoreCase = true) }
        postaviListu(lista)
        _izFirebase.value = true
    }

    fun broj(): Int = _sve.value.size
    fun poNazivu(naziv: String?): Sorta? = naziv?.let { poNazivu[it] }
    fun poKljucu(kljuc: String?): Sorta? = kljuc?.let { poKljucu[it] }

    /** Ključ za sortu po nazivu — koristi override ako postoji (npr. KM Samuraj). */
    fun kljucZa(naziv: String): String = poNazivu(naziv)?.kljuc ?: kljucOd(naziv)

    /** Opis: iz Firebasea ako postoji, inače iz ugrađenog fajla; null ako nema. */
    suspend fun opis(s: Sorta): String? = s.opis ?: opisSorte(s.kljuc)

    /** Ista logika kao Sorte.kljucOd() na Androidu (ključevi kompatibilni sa cjenovnikom). */
    fun kljucOd(naziv: String): String = naziv.lowercase()
        .replace("š", "s")
        .replace("č", "c")
        .replace("ć", "c")
        .replace("ž", "z")
        .replace("đ", "dj")
        .replace(" ", "")
        .replace("-", "")

    private fun kismisIzNaziva(naziv: String): Boolean {
        val n = naziv.lowercase()
        return n.startsWith("kišmiš") || n.startsWith("km ") || n.contains("kišmiš")
    }

    private fun bojaIz(s: String?): BojaBobice = when (s?.lowercase()) {
        "roza", "crvena" -> BojaBobice.ROZA
        "crna", "plava" -> BojaBobice.CRNA
        else -> BojaBobice.BIJELA
    }
}
