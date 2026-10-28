package com.vinograd.zivinice.ekrani

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vinograd.zivinice.Navigator
import com.vinograd.zivinice.podaci.KupacPodaci
import com.vinograd.zivinice.podaci.Rezervacija
import com.vinograd.zivinice.podaci.Sezona
import com.vinograd.zivinice.podaci.Slanje
import com.vinograd.zivinice.podaci.StavkaRezervacije
import com.vinograd.zivinice.resources.Res
import com.vinograd.zivinice.resources.ic_arrow_back
import com.vinograd.zivinice.resources.ic_check
import com.vinograd.zivinice.resources.ic_location
import com.vinograd.zivinice.resources.outline_add_call_24
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.DugmeKrug
import com.vinograd.zivinice.ui.InfoDijalog
import com.vinograd.zivinice.ui.Poppins
import com.vinograd.zivinice.ui.UspjehDijalog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/** Prikaz načina ("viber" → "Viber" …) za čipove, natpise i pamćenje kupca. */
private fun nacinPrikaz(n: String): String = when (n) {
    Rezervacija.NACIN_WHATSAPP -> "WhatsApp"
    Rezervacija.NACIN_EMAIL -> "Email"
    else -> "Viber"
}

/** Rezervacija — korak 2 (1:1 KontaktRezervacijeActivity + activity_kontakt_rezervacije.xml). */
@Composable
fun KontaktRezervacijeEkran(stavke: List<StavkaRezervacije>, nav: Navigator) {
    val scope = rememberCoroutineScope()
    val fokus = LocalFocusManager.current
    val gustina = LocalDensity.current
    val skrol = rememberScrollState()

    val vrh = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val dnoNav = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val dnoTastatura = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val dno: Dp = maxOf(dnoNav, dnoTastatura)

    var ime by remember { mutableStateOf("") }
    var prezime by remember { mutableStateOf("") }
    var telefon by remember { mutableStateOf("") }
    var grad by remember { mutableStateOf("") }
    var postanski by remember { mutableStateOf("") }
    var adresa by remember { mutableStateOf("") }
    var kontakt by remember { mutableStateOf("") }
    var napomena by remember { mutableStateOf("") }
    var nacin by remember { mutableStateOf(Rezervacija.NACIN_VIBER) }   // "viber" / "whatsapp" / "email"

    val greske = remember { mutableStateMapOf<Polje, String>() }
    val pozicije = remember { mutableStateMapOf<Polje, Float>() }

    var salje by remember { mutableStateOf(false) }
    var dijalogSezona by remember { mutableStateOf(false) }
    var dijalogGreska by remember { mutableStateOf(false) }
    var uspjeh by remember { mutableStateOf<Rezervacija?>(null) }

    val jeEmail = nacin == Rezervacija.NACIN_EMAIL

    /** azurirajPoljeKontakta() */
    fun uskladiKontakt(k: String, n: String): String {
        greske.remove(Polje.KONTAKT)
        return if (n == Rezervacija.NACIN_EMAIL) {
            if (!k.contains("@")) "" else k
        } else {
            if (k.contains("@")) "" else k
        }
    }

    LaunchedEffect(Unit) {
        if (stavke.isEmpty()) {
            nav.nazad()
            return@LaunchedEffect
        }
        // popuniZapamcene()
        val k = KupacPodaci.ucitaj()
        if (k.imaPodatke()) {
            ime = k.ime
            prezime = k.prezime
            telefon = k.telefon
            grad = k.grad
            postanski = k.postanskiBroj
            adresa = k.adresa
            val n = k.nacinRacuna.lowercase()
            nacin = when {
                n.contains("whatsapp") -> Rezervacija.NACIN_WHATSAPP
                n.contains("email") -> Rezervacija.NACIN_EMAIL
                else -> Rezervacija.NACIN_VIBER
            }
            kontakt = k.kontaktZaRacun
        }
        kontakt = uskladiKontakt(kontakt, nacin)
    }

    // prikaziPregled()
    val us = remember(stavke) { stavke.sumOf { it.kolicinaSadnica } }
    val ur = remember(stavke) { stavke.sumOf { it.kolicinaReznica } }

    fun validiraj(): Boolean {
        greske.clear()
        if (ime.trim().isEmpty()) greske[Polje.IME] = "Unesite ime"
        if (prezime.trim().isEmpty()) greske[Polje.PREZIME] = "Unesite prezime"
        if (samoCifre(telefon.trim()).length < 8) greske[Polje.TELEFON] = "Unesite ispravan broj (najmanje 8 cifara)"
        if (grad.trim().isEmpty()) greske[Polje.GRAD] = "Unesite grad"
        if (samoCifre(postanski.trim()).length != 5) greske[Polje.POSTANSKI] = "5 cifara, npr. 75270"
        if (adresa.trim().length < 3) greske[Polje.ADRESA] = "Unesite ulicu i broj"
        val k = kontakt.trim()
        if (jeEmail) {
            if (!EMAIL.matches(k)) greske[Polje.KONTAKT] = "Unesite ispravnu email adresu"
        } else if (samoCifre(k).length < 8) {
            greske[Polje.KONTAKT] = "Unesite broj za " + nacinPrikaz(nacin)
        }

        val prvi = Polje.entries.firstOrNull { greske.containsKey(it) }
        if (prvi != null) {
            val y = pozicije[prvi]
            if (y != null) {
                val pomak = with(gustina) { 16.dp.toPx() }
                scope.launch { skrol.animateScrollTo((y - pomak).toInt().coerceAtLeast(0)) }
            }
        }
        return greske.isEmpty()
    }

    fun posalji() {
        if (Sezona.zatvorena()) {
            dijalogSezona = true
            return
        }
        if (!validiraj()) return
        fokus.clearFocus()

        val tIme = ime.trim()
        val tPrezime = prezime.trim()
        val tTelefon = telefon.trim()
        val tGrad = grad.trim()
        val tPostanski = samoCifre(postanski.trim())
        val tAdresa = adresa.trim()
        val tKontakt = kontakt.trim()
        val tNapomena = napomena.trim()
        val tNacin = nacin

        salje = true
        scope.launch {
            // Zapamti kupca ("WhatsApp" / "Email" / "Viber" — kao Android)
            KupacPodaci(
                ime = tIme, prezime = tPrezime, telefon = tTelefon, grad = tGrad,
                postanskiBroj = tPostanski, adresa = tAdresa,
                nacinRacuna = nacinPrikaz(tNacin), kontaktZaRacun = tKontakt,
            ).sacuvaj()

            val r = Rezervacija(
                id = Slanje.noviId(),
                ime = tIme, prezime = tPrezime, telefon = tTelefon, grad = tGrad,
                adresa = tAdresa, napomena = tNapomena,
                nacinSlanjaRacuna = tNacin, kontaktZaRacun = tKontakt,
                postanskiBroj = tPostanski, stavke = stavke,
            )
            val ok = Slanje.posaljiRezervaciju(r)
            salje = false
            if (ok) uspjeh = r else dijalogGreska = true
        }
    }

    Column(Modifier.fillMaxSize().background(Boje.Krem)) {

        // ===== HEADER =====
        Row(
            Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Boje.ZelenaSrednja, Boje.ZelenaTamna)))
                .padding(start = 16.dp, end = 16.dp, top = vrh + 10.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DugmeKrug(Res.drawable.ic_arrow_back, Color(0x26FFFFFF), { nav.nazad() })
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    "Rezervacija", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    color = Color.White, fontFamily = Poppins(),
                )
                Text(
                    "Korak 2 od 2 · vaši podaci", fontSize = 12.sp, color = Color.White,
                    fontFamily = Poppins(), modifier = Modifier.alpha(0.8f),
                )
            }
        }

        // ===== FORMA =====
        Column(
            Modifier.fillMaxWidth().weight(1f)
                .pointerInput(Unit) { detectTapGestures { fokus.clearFocus() } }
                .verticalScroll(skrol)
                .padding(14.dp)
        ) {
            // SAŽETAK
            Column(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)).background(Boje.PovrsinaTopla)
                    .border(0.5.dp, Boje.ObrubMeki, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "VAŠA REZERVACIJA", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = Boje.TekstSvijetli, fontFamily = Poppins(), letterSpacing = 0.12.em,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "Uredi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Boje.ZelenaSvijetla,
                        fontFamily = Poppins(),
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { nav.nazad() }.padding(4.dp),
                    )
                }
                Column(Modifier.padding(top = 8.dp)) {
                    stavke.forEach { s ->
                        val sb = StringBuilder(s.sorta)
                        if (s.kolicinaSadnica > 0) sb.append("   🌱 ").append(s.kolicinaSadnica)
                        if (s.kolicinaReznica > 0) sb.append("   ✂️ ").append(s.kolicinaReznica)
                        Text(
                            sb.toString(), fontSize = 13.sp, color = Boje.TekstGlavni, fontFamily = Poppins(),
                            modifier = Modifier.padding(vertical = 3.dp),
                        )
                    }
                }
                Box(Modifier.padding(vertical = 10.dp).fillMaxWidth().height(0.5.dp).background(Boje.ObrubMeki))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${stavke.size}" + (if (stavke.size == 1) " sorta" else " sorti"),
                        fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = Boje.TekstGlavni, fontFamily = Poppins(), modifier = Modifier.weight(1f),
                    )
                    Text(
                        "$us sadnica · $ur reznica",
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Boje.Vino, fontFamily = Poppins(),
                    )
                }
            }

            // PODACI ZA DOSTAVU
            NaslovSekcije("PODACI ZA DOSTAVU", Modifier.padding(top = 22.dp, start = 4.dp))
            Text(
                "Sva polja su obavezna — pošiljka ide poštom na ovu adresu.", fontSize = 12.sp,
                color = Boje.TekstSporedni, fontFamily = Poppins(), modifier = Modifier.padding(top = 2.dp, start = 4.dp),
            )

            Row(
                Modifier.padding(top = 10.dp).fillMaxWidth()
                    .onGloballyPositioned {
                        pozicije[Polje.IME] = it.positionInParent().y
                        pozicije[Polje.PREZIME] = it.positionInParent().y
                    },
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PoljeForme(
                    ime, { ime = it; greske.remove(Polje.IME) }, "Ime", greske[Polje.IME],
                    Modifier.weight(1f), kapitalizacija = KeyboardCapitalization.Words,
                )
                PoljeForme(
                    prezime, { prezime = it; greske.remove(Polje.PREZIME) }, "Prezime", greske[Polje.PREZIME],
                    Modifier.weight(1f), kapitalizacija = KeyboardCapitalization.Words,
                )
            }

            PoljeForme(
                telefon, { telefon = it; greske.remove(Polje.TELEFON) }, "Broj telefona", greske[Polje.TELEFON],
                Modifier.fillMaxWidth().onGloballyPositioned { pozicije[Polje.TELEFON] = it.positionInParent().y },
                tastatura = KeyboardType.Phone, ikona = Res.drawable.outline_add_call_24,
            )

            Row(
                Modifier.fillMaxWidth().onGloballyPositioned {
                    pozicije[Polje.GRAD] = it.positionInParent().y
                    pozicije[Polje.POSTANSKI] = it.positionInParent().y
                },
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PoljeForme(
                    grad, { grad = it; greske.remove(Polje.GRAD) }, "Grad / mjesto", greske[Polje.GRAD],
                    Modifier.weight(3f), kapitalizacija = KeyboardCapitalization.Words,
                )
                PoljeForme(
                    postanski, { postanski = it.filter(Char::isDigit).take(5); greske.remove(Polje.POSTANSKI) },
                    "Poštanski broj", greske[Polje.POSTANSKI],
                    Modifier.weight(2f), tastatura = KeyboardType.Number,
                )
            }

            PoljeForme(
                adresa, { adresa = it; greske.remove(Polje.ADRESA) }, "Ulica i broj", greske[Polje.ADRESA],
                Modifier.fillMaxWidth().onGloballyPositioned { pozicije[Polje.ADRESA] = it.positionInParent().y },
                kapitalizacija = KeyboardCapitalization.Sentences, ikona = Res.drawable.ic_location, maxRedova = 2,
            )

            // RAČUN
            NaslovSekcije("GDJE DA POŠALJEMO RAČUN?", Modifier.padding(top = 14.dp, start = 4.dp))
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(Rezervacija.NACIN_VIBER, Rezervacija.NACIN_WHATSAPP, Rezervacija.NACIN_EMAIL).forEach { n ->
                    FilterChip(
                        selected = nacin == n,
                        onClick = {
                            if (nacin != n) {
                                nacin = n
                                kontakt = uskladiKontakt(kontakt, n)
                            }
                        },
                        label = { Text(nacinPrikaz(n), fontFamily = Poppins()) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Boje.ChipPozadina,
                            labelColor = Boje.TekstGlavni,
                            selectedContainerColor = Boje.Vino,
                            selectedLabelColor = Color.White,
                        ),
                        border = null,
                    )
                }
            }

            PoljeForme(
                kontakt, { kontakt = it; greske.remove(Polje.KONTAKT) },
                if (jeEmail) "Email adresa" else nacinPrikaz(nacin) + " broj", greske[Polje.KONTAKT],
                Modifier.padding(top = 8.dp).fillMaxWidth()
                    .onGloballyPositioned { pozicije[Polje.KONTAKT] = it.positionInParent().y },
                tastatura = if (jeEmail) KeyboardType.Email else KeyboardType.Phone,
            )
            if (!jeEmail) {
                Text(
                    "Koristi isti broj kao telefon", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = Boje.ZelenaSvijetla, fontFamily = Poppins(),
                    modifier = Modifier.padding(start = 4.dp).clip(RoundedCornerShape(8.dp))
                        .clickable {
                            kontakt = telefon.trim()
                            greske.remove(Polje.KONTAKT)
                        }
                        .padding(4.dp),
                )
            }

            // NAPOMENA
            PoljeForme(
                napomena, { napomena = it }, "Napomena (nije obavezno)", null,
                Modifier.padding(top = 10.dp).fillMaxWidth(),
                kapitalizacija = KeyboardCapitalization.Sentences,
                minRedova = 2, maxRedova = 4, prostorZaGresku = false,
            )
            Text(
                "Rezervacija nije obavezujuća kupovina. Sadnice i reznice se šalju poštom na ovu adresu u sezoni.",
                fontSize = 11.sp, color = Boje.TekstSvijetli, fontFamily = Poppins(), textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 10.dp),
            )
        }

        // ===== STICKY DNO =====
        Column(
            Modifier.fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(Boje.PovrsinaTopla)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp + dno),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (salje) {
                CircularProgressIndicator(color = Boje.ZelenaTamna, modifier = Modifier.padding(bottom = 8.dp))
            }
            Button(
                onClick = { posalji() },
                enabled = !salje && stavke.isNotEmpty(),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Boje.Vino, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Icon(painterResource(Res.drawable.ic_check), contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    if (salje) "Šalje se…" else "Pošalji rezervaciju",
                    fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins(),
                    maxLines = 1, modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }

    // ===== DIJALOZI =====
    if (dijalogSezona) {
        InfoDijalog(
            ikona = "ℹ️",
            naslov = "Rezervacije zatvorene",
            sadrzaj = Sezona.porukaZatvoreno(),
            onZatvori = { dijalogSezona = false },
            pozitivTekst = "U redu",
        )
    }
    if (dijalogGreska) {
        InfoDijalog(
            ikona = "⚠️",
            naslov = "Greška pri slanju",
            sadrzaj = "Nije moguće poslati rezervaciju. Provjerite internet i pokušajte ponovo.",
            onZatvori = { dijalogGreska = false },
            pozitivTekst = "U redu",
        )
    }
    uspjeh?.let { r ->
        val nacinLijep = when (r.nacinSlanjaRacuna) {
            Rezervacija.NACIN_EMAIL -> "email"
            Rezervacija.NACIN_WHATSAPP -> "WhatsApp"
            else -> "Viber"
        }
        val poruka = "Hvala ${r.ime}!\n\n" +
            "Vaša rezervacija je zaprimljena.\n\n" +
            "Račun i potvrdu šaljemo na $nacinLijep:\n${r.kontaktZaRacun}" +
            "\n\nPošiljka ide na:\n${r.adresa}, ${r.postanskiBroj} ${r.grad}"
        UspjehDijalog("🍇", "Rezervacija poslana!", poruka) {
            uspjeh = null
            nav.naPocetnu()
        }
    }
}
