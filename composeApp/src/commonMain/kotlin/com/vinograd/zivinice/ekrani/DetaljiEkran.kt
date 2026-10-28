package com.vinograd.zivinice.ekrani

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vinograd.zivinice.Ekran
import com.vinograd.zivinice.Navigator
import com.vinograd.zivinice.opisSorte
import com.vinograd.zivinice.podaci.Cjenovnik
import com.vinograd.zivinice.podaci.Korpa
import com.vinograd.zivinice.podaci.Sezona
import com.vinograd.zivinice.podaci.Sorte
import com.vinograd.zivinice.resources.Res
import com.vinograd.zivinice.resources.ic_arrow_back
import com.vinograd.zivinice.resources.ic_cart
import com.vinograd.zivinice.resources.ic_minus
import com.vinograd.zivinice.resources.ic_plus
import com.vinograd.zivinice.resources.ic_zoom
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.DugmeKrug
import com.vinograd.zivinice.ui.Poppins
import com.vinograd.zivinice.ui.SlikaSorteMreza
import com.vinograd.zivinice.ui.TIP_REZNICA
import com.vinograd.zivinice.ui.TIP_SADNICA
import com.vinograd.zivinice.ui.TackaBobice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/** Android Snackbar.LENGTH_LONG = 2750 ms. */
private const val TRAJANJE_PORUKE_MS = 2750L

/** Detalji sorte (1:1 DetaljiSadniceActivity + activity_detalji_sadnice.xml). */
@Composable
fun DetaljiEkran(naziv: String, nav: Navigator) {
    val sve by Sorte.sve.collectAsState()
    val cijene by Cjenovnik.mapa.collectAsState()
    val sezona by Sezona.stanje.collectAsState()
    val korpa by Korpa.stavke.collectAsState(initial = emptyList())

    val sorta = remember(sve, naziv) { Sorte.poNazivu(naziv) }
    val kljuc = sorta?.kljuc ?: Sorte.kljucOd(naziv)

    // Opis (Firebase → fajl → poruka)
    var opis by remember { mutableStateOf<String?>(null) }
    var opisUcitan by remember { mutableStateOf(false) }
    LaunchedEffect(sorta, kljuc) {
        opis = if (sorta != null) Sorte.opis(sorta) else opisSorte(kljuc)
        opisUcitan = true
    }

    // Cijene + stanje (osvjeziCijene)
    val c = cijene[kljuc]
    val cs = c?.cijenaSadnica ?: 0.0
    val cr = c?.cijenaReznica ?: 0.0
    val naStanjuS = c?.naStanjuSadnica ?: true
    val naStanjuR = c?.naStanjuReznica ?: true

    var tip by remember { mutableStateOf(TIP_SADNICA) }
    var kolicina by remember { mutableIntStateOf(1) }

    // Ako je izabrani tip nedostupan ili nema cijenu, a drugi je OK — prebaci
    LaunchedEffect(c) {
        val sOk = naStanjuS && cs > 0
        val rOk = naStanjuR && cr > 0
        if (tip == TIP_SADNICA && !sOk && rOk) tip = TIP_REZNICA
        else if (tip == TIP_REZNICA && !rOk && sOk) tip = TIP_SADNICA
    }

    // osvjeziDugme()
    val zatvorena = remember(sezona) { Sezona.zatvorena() }
    val dostupno = c == null || c.naStanju(tip)
    val jedinicna = if (tip == TIP_SADNICA) cs else cr
    val dugmeAktivno = !zatvorena && dostupno
    val dugmeTekst = when {
        zatvorena -> "Sezona zatvorena"
        !dostupno -> "Nema na stanju"
        jedinicna > 0 -> "Dodaj u korpu · " + Cjenovnik.format(jedinicna * kolicina)
        else -> "Dodaj u korpu"
    }

    val vrh = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val dno = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val gustina = LocalDensity.current
    var visinaDna by remember { mutableStateOf(0.dp) }
    val poruke = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val otvoriSliku = { nav.otvori(Ekran.SlikaPreko(naziv)) }

    fun dodajUKorpu() {
        val k = kolicina
        val t = tip
        scope.launch {
            Korpa.dodaj(naziv, t, k)
            val tipLijep = if (t == TIP_SADNICA) "sadnica" else "reznica"
            poruke.currentSnackbarData?.dismiss()
            val tajmer = launch {
                delay(TRAJANJE_PORUKE_MS)
                poruke.currentSnackbarData?.dismiss()
            }
            val r = poruke.showSnackbar(
                "$k× $naziv ($tipLijep) dodano u korpu",
                actionLabel = "KORPA",
                duration = SnackbarDuration.Indefinite,
            )
            tajmer.cancel()
            if (r == SnackbarResult.ActionPerformed) nav.otvori(Ekran.Korpa)
        }
        kolicina = 1
    }

    Box(Modifier.fillMaxSize().background(Boje.Krem)) {

        // ===== SADRŽAJ (scroll) =====
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // SLIKA
            Box(Modifier.fillMaxWidth().height(340.dp)) {
                SlikaSorteMreza(
                    kljuc, sorta?.slikaUrl,
                    Modifier.fillMaxSize().clickable { otvoriSliku() },
                )
                Row(
                    Modifier.align(Alignment.BottomEnd).padding(end = 14.dp, bottom = 42.dp)
                        .clip(RoundedCornerShape(100.dp)).background(Color(0xCC1F3D2E))
                        .clickable { otvoriSliku() }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painterResource(Res.drawable.ic_zoom), contentDescription = "Zoom",
                        tint = Color.White, modifier = Modifier.size(14.dp),
                    )
                    Text(
                        "Uvećaj", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White,
                        fontFamily = Poppins(), modifier = Modifier.padding(start = 5.dp),
                    )
                }
            }

            // SHEET preko slike
            Column(
                Modifier.offset(y = (-28).dp).fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Boje.Krem)
                    .padding(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 24.dp)
            ) {
                // Pill tip + boja
                if (sorta != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            sorta.tipPrikaz, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = Color.White, fontFamily = Poppins(), letterSpacing = 0.05.em,
                            modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(Color(0xCC1F3D2E))
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                        )
                        TackaBobice(sorta.boja, Modifier.padding(start = 10.dp))
                        Text(
                            sorta.boja.naziv, fontSize = 12.sp, color = Boje.TekstSporedni,
                            fontFamily = Poppins(), modifier = Modifier.padding(start = 5.dp),
                        )
                    }
                }

                // Naziv
                Text(
                    naziv, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Boje.TekstGlavni,
                    fontFamily = Poppins(), modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )

                // Cijene
                Row(
                    Modifier.padding(top = 14.dp).fillMaxWidth().height(IntrinsicSize.Min)
                        .clip(RoundedCornerShape(18.dp)).background(Boje.PovrsinaTopla)
                        .border(0.5.dp, Boje.ObrubMeki, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    KolonaCijene("🌱  SADNICA", Boje.TipSadnica, cs, naStanjuS, Modifier.weight(1f))
                    Box(
                        Modifier.padding(horizontal = 12.dp).width(0.5.dp).fillMaxHeight()
                            .background(Boje.ObrubMeki)
                    )
                    KolonaCijene("✂️  REZNICA", Boje.TipReznica, cr, naStanjuR, Modifier.weight(1f))
                }

                // Opis
                Text(
                    "O SORTI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Boje.TekstSvijetli,
                    fontFamily = Poppins(), letterSpacing = 0.12.em, modifier = Modifier.padding(top = 22.dp),
                )
                Text(
                    if (!opisUcitan) "" else opis?.ifEmpty { null } ?: "Opis za ovu sortu još nije dostupan.",
                    fontSize = 14.sp, lineHeight = 22.sp, color = Color(0xFF3D4A40), fontFamily = Poppins(),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )

                // Prostor da donja traka ne pokrije tekst
                Spacer(Modifier.height(150.dp + dno))
            }
        }

        // ===== NAZAD + KORPA (preko slike) =====
        DugmeKrug(
            Res.drawable.ic_arrow_back, Color(0x66000000), { nav.nazad() },
            Modifier.align(Alignment.TopStart).padding(start = 14.dp, top = vrh + 10.dp),
        )
        DugmeKrug(
            Res.drawable.ic_cart, Color(0x66000000), { nav.otvori(Ekran.Korpa) },
            Modifier.align(Alignment.TopEnd).padding(end = 14.dp, top = vrh + 10.dp),
            znacka = korpa.size,
        )

        // ===== STICKY DNO =====
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .onSizeChanged { visinaDna = with(gustina) { it.height.toDp() } }
                .shadow(12.dp, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(Boje.PovrsinaTopla)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp + dno)
        ) {
            // Sadnica | Reznica
            Row(
                Modifier.fillMaxWidth().height(44.dp)
                    .clip(RoundedCornerShape(100.dp)).background(Boje.ChipPozadina)
                    .padding(4.dp)
            ) {
                SegmentTipa("Sadnica", tip == TIP_SADNICA, naStanjuS, Modifier.weight(1f)) { tip = TIP_SADNICA }
                SegmentTipa("Reznica", tip == TIP_REZNICA, naStanjuR, Modifier.weight(1f)) { tip = TIP_REZNICA }
            }

            // Stepper + Dodaj
            Row(Modifier.padding(top = 10.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.height(48.dp).clip(RoundedCornerShape(100.dp)).background(Boje.ChipPozadina)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painterResource(Res.drawable.ic_minus), contentDescription = "Manje",
                        tint = Boje.TekstGlavni,
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(100.dp))
                            .clickable { if (kolicina > 1) kolicina-- }.padding(11.dp),
                    )
                    Text(
                        kolicina.toString(), fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        color = Boje.TekstGlavni, fontFamily = Poppins(), textAlign = TextAlign.Center,
                        modifier = Modifier.width(34.dp),
                    )
                    Icon(
                        painterResource(Res.drawable.ic_plus), contentDescription = "Više",
                        tint = Boje.TekstGlavni,
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(100.dp))
                            .clickable { if (kolicina < 999) kolicina++ }.padding(11.dp),
                    )
                }

                Button(
                    onClick = { dodajUKorpu() },
                    enabled = dugmeAktivno,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Boje.Vino, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.weight(1f).height(48.dp).padding(start = 10.dp),
                ) {
                    Icon(
                        painterResource(Res.drawable.ic_cart), contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        dugmeTekst, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins(),
                        maxLines = 1, modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }

        // ===== PORUKA (iznad donje trake, kao setAnchorView) =====
        SnackbarHost(
            poruke,
            Modifier.align(Alignment.BottomCenter).padding(bottom = visinaDna),
        ) { d ->
            Snackbar(
                d,
                containerColor = Boje.ZelenaTamna,
                contentColor = Color.White,
                actionColor = Boje.ZlatnaSvijetla,
            )
        }
    }
}

@Composable
private fun KolonaCijene(naslov: String, boja: Color, cijena: Double, naStanju: Boolean, modifier: Modifier) {
    Column(modifier) {
        Text(
            naslov, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = boja,
            fontFamily = Poppins(), letterSpacing = 0.1.em,
        )
        Text(
            if (cijena > 0) Cjenovnik.format(cijena) else "na upit",
            fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Boje.Vino, fontFamily = Poppins(),
            modifier = Modifier.padding(top = 2.dp),
        )
        if (!naStanju) {
            Text("Nema na stanju", fontSize = 11.sp, color = Boje.VinoSvijetlo, fontFamily = Poppins())
        }
    }
}

@Composable
private fun SegmentTipa(
    tekst: String, odabran: Boolean, dostupan: Boolean, modifier: Modifier, onKlik: () -> Unit,
) {
    Box(
        modifier.fillMaxHeight().alpha(if (dostupan) 1f else 0.4f)
            .clip(RoundedCornerShape(100.dp))
            .background(if (odabran) Boje.ZelenaTamna else Color.Transparent)
            .clickable(enabled = dostupan, onClick = onKlik),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            tekst, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins(),
            color = if (odabran) Color.White else Boje.TekstGlavni,
        )
    }
}
