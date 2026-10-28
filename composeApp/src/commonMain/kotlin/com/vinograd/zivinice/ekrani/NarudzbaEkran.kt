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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vinograd.zivinice.Navigator
import com.vinograd.zivinice.podaci.JednomNaEkranu
import com.vinograd.zivinice.podaci.Stat
import com.vinograd.zivinice.podaci.Cjenovnik
import com.vinograd.zivinice.podaci.Korpa
import com.vinograd.zivinice.podaci.KupacPodaci
import com.vinograd.zivinice.podaci.Narudzba
import com.vinograd.zivinice.podaci.Sezona
import com.vinograd.zivinice.podaci.Slanje
import com.vinograd.zivinice.podaci.StavkaKorpe
import com.vinograd.zivinice.podaci.StavkaNarudzbe
import com.vinograd.zivinice.resources.Res
import com.vinograd.zivinice.resources.ic_arrow_back
import com.vinograd.zivinice.resources.ic_check
import com.vinograd.zivinice.resources.ic_location
import com.vinograd.zivinice.resources.outline_add_call_24
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.DugmeKrug
import com.vinograd.zivinice.ui.InfoDijalog
import com.vinograd.zivinice.ui.Poppins
import com.vinograd.zivinice.ui.TIP_SADNICA
import com.vinograd.zivinice.ui.UspjehDijalog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

internal val EMAIL = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

internal fun samoCifre(s: String): String = s.replace(Regex("[^0-9]"), "")

/** Polja forme — redoslijed = redoslijed provjere (za skrol do prve greške). */
internal enum class Polje { IME, PREZIME, TELEFON, GRAD, POSTANSKI, ADRESA, KONTAKT }

/** Narudžba — korak 2 (1:1 NarudzbaActivity + activity_narudzba.xml). */
@Composable
fun NarudzbaEkran(nav: Navigator) {
    val scope = rememberCoroutineScope()
    val fokus = LocalFocusManager.current
    JednomNaEkranu { Stat.dogadjaj(Stat.NARUDZBA_OTVORENA) }
    val gustina = LocalDensity.current
    val skrol = rememberScrollState()

    val vrh = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val dnoNav = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val dnoTastatura = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val dno: Dp = maxOf(dnoNav, dnoTastatura)

    // Korpa — učita se jednom pri otvaranju (kao onCreate)
    var stavkeIzKorpe by remember { mutableStateOf<List<StavkaKorpe>?>(null) }

    // Polja
    var ime by remember { mutableStateOf("") }
    var prezime by remember { mutableStateOf("") }
    var telefon by remember { mutableStateOf("") }
    var grad by remember { mutableStateOf("") }
    var postanski by remember { mutableStateOf("") }
    var adresa by remember { mutableStateOf("") }
    var kontakt by remember { mutableStateOf("") }
    var napomena by remember { mutableStateOf("") }
    var nacin by remember { mutableStateOf(Narudzba.NACIN_VIBER) }

    val greske = remember { mutableStateMapOf<Polje, String>() }
    val pozicije = remember { mutableStateMapOf<Polje, Float>() }

    var salje by remember { mutableStateOf(false) }
    var dijalogSezona by remember { mutableStateOf(false) }
    var dijalogGreska by remember { mutableStateOf(false) }
    var uspjeh by remember { mutableStateOf<Narudzba?>(null) }

    val jeEmail = nacin == Narudzba.NACIN_EMAIL

    /** azurirajPoljeKontakta() — očisti polje ako ne odgovara načinu, ukloni grešku. */
    fun uskladiKontakt(k: String, n: String): String {
        greske.remove(Polje.KONTAKT)
        return if (n == Narudzba.NACIN_EMAIL) {
            if (!k.contains("@")) "" else k
        } else {
            if (k.contains("@")) "" else k
        }
    }

    LaunchedEffect(Unit) {
        val s = Korpa.dohvatiSveStavke()
        if (s.isEmpty()) {
            nav.nazad()
            return@LaunchedEffect
        }
        stavkeIzKorpe = s

        // popuniZapamcene()
        val k = KupacPodaci.ucitaj()
        if (k.imaPodatke()) {
            ime = k.ime
            prezime = k.prezime
            telefon = k.telefon
            grad = k.grad
            postanski = k.postanskiBroj
            adresa = k.adresa
            nacin = when (k.nacinRacuna) {
                Narudzba.NACIN_WHATSAPP -> Narudzba.NACIN_WHATSAPP
                Narudzba.NACIN_EMAIL -> Narudzba.NACIN_EMAIL
                else -> Narudzba.NACIN_VIBER
            }
            kontakt = k.kontaktZaRacun
        }
        kontakt = uskladiKontakt(kontakt, nacin)
    }

    val stavke = stavkeIzKorpe ?: emptyList()
    // prikaziPregledKorpe()
    val ukupnoKom = remember(stavke) { stavke.sumOf { it.kolicina } }
    val ukupnoIznos = remember(stavke) {
        stavke.sumOf { Cjenovnik.jedinicna(it.sorta, it.tip) * it.kolicina }
    }
    val tekstDugmeta = when {
        salje -> "Šalje se…"
        ukupnoIznos > 0 -> "Pošalji narudžbu · " + Cjenovnik.format(ukupnoIznos)
        else -> "Pošalji narudžbu"
    }

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
            greske[Polje.KONTAKT] = "Unesite $nacin broj"
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

        val stavkeNarudzbe = stavke.map {
            StavkaNarudzbe(it.tip, it.sorta, it.kolicina, Cjenovnik.jedinicna(it.sorta, it.tip))
        }

        salje = true
        scope.launch {
            // Zapamti kupca za sljedeći put
            KupacPodaci(
                ime = tIme, prezime = tPrezime, telefon = tTelefon, grad = tGrad,
                postanskiBroj = tPostanski, adresa = tAdresa,
                nacinRacuna = tNacin, kontaktZaRacun = tKontakt,
            ).sacuvaj()

            val n = Narudzba(
                id = Slanje.noviId(),
                ime = tIme, prezime = tPrezime, telefon = tTelefon, grad = tGrad,
                adresa = tAdresa, napomena = tNapomena,
                nacinSlanjaRacuna = tNacin, kontaktZaRacun = tKontakt,
                postanskiBroj = tPostanski, ukupnoIznos = ukupnoIznos,
                stavke = stavkeNarudzbe,
            )
            val ok = Slanje.posaljiNarudzbu(n)
            salje = false
            if (ok) {
                Stat.dogadjaj(Stat.NARUDZBA_POSLANA)
                Korpa.isprazni()
                uspjeh = n
            } else {
                dijalogGreska = true
            }
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
                    "Narudžba", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    color = Color.White, fontFamily = Poppins(),
                )
                Text(
                    "Korak 2 od 2 · podaci za dostavu", fontSize = 12.sp, color = Color.White,
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
                        "VAŠA NARUDŽBA", fontSize = 11.sp, fontWeight = FontWeight.Bold,
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
                        val jed = Cjenovnik.jedinicna(s.sorta, s.tip)
                        Row(Modifier.padding(vertical = 3.dp)) {
                            Text(
                                "${s.kolicina}× ${s.sorta}  ·  " + (if (s.tip == TIP_SADNICA) "sadnica" else "reznica"),
                                fontSize = 13.sp, color = Boje.TekstGlavni, fontFamily = Poppins(),
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                if (jed > 0) Cjenovnik.format(jed * s.kolicina) else "na upit",
                                fontSize = 13.sp, color = Boje.TekstSporedni, fontFamily = Poppins(),
                            )
                        }
                    }
                }
                Box(Modifier.padding(vertical = 10.dp).fillMaxWidth().height(0.5.dp).background(Boje.ObrubMeki))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Ukupno · $ukupnoKom kom", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = Boje.TekstGlavni, fontFamily = Poppins(), modifier = Modifier.weight(1f),
                    )
                    Text(
                        if (ukupnoIznos > 0) Cjenovnik.format(ukupnoIznos) else "na upit",
                        fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Boje.Vino, fontFamily = Poppins(),
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
                listOf(Narudzba.NACIN_VIBER, Narudzba.NACIN_WHATSAPP, Narudzba.NACIN_EMAIL).forEach { n ->
                    FilterChip(
                        selected = nacin == n,
                        onClick = {
                            if (nacin != n) {
                                nacin = n
                                kontakt = uskladiKontakt(kontakt, n)
                            }
                        },
                        label = { Text(n, fontFamily = Poppins()) },
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
                if (jeEmail) "Email adresa" else "$nacin broj", greske[Polje.KONTAKT],
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
                "Slanjem narudžbe potvrđujete da su podaci tačni. Kontaktiraćemo vas samo ako bude potrebno.",
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
                enabled = !salje && stavkeIzKorpe != null,
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Boje.Vino, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Icon(painterResource(Res.drawable.ic_check), contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    tekstDugmeta, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins(),
                    maxLines = 1, modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }

    // ===== DIJALOZI =====
    if (dijalogSezona) {
        InfoDijalog(
            ikona = "ℹ️",
            naslov = "Sezona zatvorena",
            sadrzaj = Sezona.porukaZatvoreno(),
            onZatvori = { dijalogSezona = false },
            pozitivTekst = "U redu",
        )
    }
    if (dijalogGreska) {
        InfoDijalog(
            ikona = "⚠️",
            naslov = "Greška pri slanju",
            sadrzaj = "Nije moguće poslati narudžbu. Provjerite internet i pokušajte ponovo.",
            onZatvori = { dijalogGreska = false },
            pozitivTekst = "U redu",
        )
    }
    uspjeh?.let { n ->
        val poruka = "Hvala ${n.ime}!\n\n" +
            "Narudžba je zaprimljena" +
            (if (n.ukupnoIznos > 0) " — ukupno " + Cjenovnik.format(n.ukupnoIznos) else "") +
            ".\n\nRačun šaljemo na ${n.nacinSlanjaRacuna}:\n${n.kontaktZaRacun}" +
            "\n\nPošiljka ide na:\n${n.adresa}, ${n.postanskiBroj} ${n.grad}"
        UspjehDijalog("🍇", "Narudžba poslana!", poruka) {
            uspjeh = null
            nav.naPocetnu()
        }
    }
}

@Composable
internal fun NaslovSekcije(tekst: String, modifier: Modifier) {
    Text(
        tekst, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Boje.TekstSvijetli,
        fontFamily = Poppins(), letterSpacing = 0.12.em, modifier = modifier,
    )
}

/** TextInputLayout.OutlinedBox (uglovi 14 dp, topla pozadina, errorEnabled → rezervisan prostor za grešku). */
@Composable
internal fun PoljeForme(
    vrijednost: String,
    onPromjena: (String) -> Unit,
    natpis: String,
    greska: String?,
    modifier: Modifier,
    tastatura: KeyboardType = KeyboardType.Text,
    kapitalizacija: KeyboardCapitalization = KeyboardCapitalization.None,
    ikona: DrawableResource? = null,
    minRedova: Int = 1,
    maxRedova: Int = 1,
    prostorZaGresku: Boolean = true,
) {
    OutlinedTextField(
        value = vrijednost,
        onValueChange = onPromjena,
        modifier = modifier,
        label = { Text(natpis, fontFamily = Poppins()) },
        textStyle = TextStyle(fontSize = 15.sp, fontFamily = Poppins(), color = Boje.TekstGlavni),
        leadingIcon = if (ikona != null) {
            { Icon(painterResource(ikona), contentDescription = null, modifier = Modifier.size(24.dp)) }
        } else null,
        isError = greska != null,
        supportingText = if (prostorZaGresku) {
            { Text(greska ?: "", fontFamily = Poppins()) }
        } else null,
        singleLine = maxRedova == 1,
        minLines = minRedova,
        maxLines = maxRedova,
        keyboardOptions = KeyboardOptions(capitalization = kapitalizacija, keyboardType = tastatura),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Boje.PovrsinaTopla,
            unfocusedContainerColor = Boje.PovrsinaTopla,
            errorContainerColor = Boje.PovrsinaTopla,
        ),
    )
}
