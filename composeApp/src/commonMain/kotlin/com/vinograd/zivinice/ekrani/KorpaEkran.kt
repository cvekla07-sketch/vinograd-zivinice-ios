package com.vinograd.zivinice.ekrani

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vinograd.zivinice.Ekran
import com.vinograd.zivinice.Navigator
import com.vinograd.zivinice.podaci.Cijena
import com.vinograd.zivinice.podaci.Cjenovnik
import com.vinograd.zivinice.podaci.Korpa
import com.vinograd.zivinice.podaci.Sezona
import com.vinograd.zivinice.podaci.Sorte
import com.vinograd.zivinice.podaci.StavkaKorpe
import com.vinograd.zivinice.resources.Res
import com.vinograd.zivinice.resources.ic_arrow_back
import com.vinograd.zivinice.resources.ic_arrow_right
import com.vinograd.zivinice.resources.ic_minus
import com.vinograd.zivinice.resources.ic_plus
import com.vinograd.zivinice.resources.ic_trash
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.DugmeKrug
import com.vinograd.zivinice.ui.InfoDijalog
import com.vinograd.zivinice.ui.Poppins
import com.vinograd.zivinice.ui.SlikaSorteMreza
import com.vinograd.zivinice.ui.TIP_SADNICA
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/** Android Snackbar.LENGTH_LONG = 2750 ms. */
private const val TRAJANJE_PORUKE_MS = 2750L

/** Red stavke sa izračunatom cijenom i stanjem (kao petlja u ucitajKorpu). */
private class RedKorpe(val s: StavkaKorpe, val jedinicna: Double, val dostupno: Boolean) {
    val iznos: Double get() = jedinicna * s.kolicina
}

/** Korpa (1:1 KorpaActivity + activity_korpa.xml + item_korpa.xml). */
@Composable
fun KorpaEkran(nav: Navigator) {
    val stavke by Korpa.stavke.collectAsState<List<StavkaKorpe>, List<StavkaKorpe>?>(initial = null)
    val cijene by Cjenovnik.mapa.collectAsState()
    val sezona by Sezona.stanje.collectAsState()

    val vrh = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val dno = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val gustina = LocalDensity.current
    var visinaDna by remember { mutableStateOf(0.dp) }
    val poruke = remember { SnackbarHostState() }
    var porukaZelena by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    var dijalogIsprazni by remember { mutableStateOf(false) }
    var dijalogSezona by remember { mutableStateOf(false) }

    val lista = stavke ?: emptyList()
    val redovi: List<RedKorpe> = remember(lista, cijene) {
        lista.map { s ->
            val c: Cijena? = Cjenovnik.cijenaZaNaziv(s.sorta)
            RedKorpe(s, Cjenovnik.jedinicna(s.sorta, s.tip), c == null || c.naStanju(s.tip))
        }
    }
    val zatvorena = remember(sezona) { Sezona.zatvorena() }

    val sadnice = redovi.filter { it.s.tip == TIP_SADNICA }
    val reznice = redovi.filter { it.s.tip != TIP_SADNICA }
    val sadniceKom = sadnice.sumOf { it.s.kolicina }
    val rezniceKom = reznice.sumOf { it.s.kolicina }
    val sadniceIznos = sadnice.sumOf { it.iznos }
    val rezniceIznos = reznice.sumOf { it.iznos }
    val ukupnoKom = sadniceKom + rezniceKom
    val ukupno = sadniceIznos + rezniceIznos
    val nekeBezCijene = redovi.any { it.jedinicna <= 0 }
    val nekeNedostupne = redovi.any { !it.dostupno }
    val prazna = stavke != null && lista.isEmpty()

    fun obrisiSaUndo(sorta: String, tip: String, kolicina: Int) {
        scope.launch {
            Korpa.obrisi(sorta, tip)
            porukaZelena = true
            pokaziPoruku(this, poruke, "$sorta uklonjeno iz korpe", "VRATI") {
                scope.launch { Korpa.dodaj(sorta, tip, kolicina) }
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Boje.Krem)) {
        Column(Modifier.fillMaxSize()) {

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
                        "Vaša korpa", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        color = Color.White, fontFamily = Poppins(),
                    )
                    Text(
                        if (lista.isEmpty()) "Nema stavki"
                        else "${lista.size}" + (if (lista.size == 1) " stavka" else " stavke") + " · $ukupnoKom kom",
                        fontSize = 12.sp, color = Color.White, fontFamily = Poppins(),
                        modifier = Modifier.alpha(0.8f),
                    )
                }
                if (lista.isNotEmpty()) {
                    Text(
                        "Isprazni", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White,
                        fontFamily = Poppins(),
                        modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(Color(0xCC1F3D2E))
                            .clickable { dijalogIsprazni = true }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }

            // ===== SADRŽAJ =====
            Column(
                Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(14.dp)
            ) {
                redovi.forEach { r ->
                    StavkaKartica(
                        r,
                        onMinus = {
                            if (r.s.kolicina > 1) scope.launch { Korpa.postaviKolicinu(r.s.sorta, r.s.tip, r.s.kolicina - 1) }
                            else obrisiSaUndo(r.s.sorta, r.s.tip, r.s.kolicina)
                        },
                        onPlus = {
                            if (r.s.kolicina < 999) scope.launch { Korpa.postaviKolicinu(r.s.sorta, r.s.tip, r.s.kolicina + 1) }
                        },
                        onObrisi = { obrisiSaUndo(r.s.sorta, r.s.tip, r.s.kolicina) },
                    )
                }

                if (prazna) {
                    // Prazna korpa
                    Column(
                        Modifier.fillMaxWidth().padding(top = 70.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("🛒", fontSize = 52.sp)
                        Text(
                            "Korpa je prazna", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = Boje.TekstGlavni, fontFamily = Poppins(), modifier = Modifier.padding(top = 12.dp),
                        )
                        Text(
                            "Izaberite sorte u katalogu i dodajte ih ovdje", fontSize = 13.sp,
                            color = Boje.TekstSporedni, fontFamily = Poppins(), textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Button(
                            onClick = { nav.otvori(Ekran.Katalog) },
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Boje.ZelenaTamna),
                            contentPadding = PaddingValues(horizontal = 26.dp),
                            modifier = Modifier.padding(top = 20.dp).height(46.dp),
                        ) {
                            Text(
                                "Otvori katalog", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                color = Color.White, fontFamily = Poppins(),
                            )
                        }
                    }
                } else if (lista.isNotEmpty()) {
                    // Sažetak
                    Column(
                        Modifier.padding(top = 6.dp).fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp)).background(Boje.PovrsinaTopla)
                            .border(0.5.dp, Boje.ObrubMeki, RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            "SAŽETAK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Boje.TekstSvijetli,
                            fontFamily = Poppins(), letterSpacing = 0.12.em,
                        )
                        RedSazetka(
                            "🌱 Sadnice",
                            if (sadnice.isNotEmpty()) "$sadniceKom kom · " + Cjenovnik.format(sadniceIznos) else "—",
                            Modifier.padding(top = 10.dp),
                        )
                        RedSazetka(
                            "✂️ Reznice",
                            if (reznice.isNotEmpty()) "$rezniceKom kom · " + Cjenovnik.format(rezniceIznos) else "—",
                            Modifier.padding(top = 6.dp),
                        )
                        Box(
                            Modifier.padding(vertical = 12.dp).fillMaxWidth().height(0.5.dp)
                                .background(Boje.ObrubMeki)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Ukupno", fontSize = 15.sp, fontWeight = FontWeight.Bold,
                                    color = Boje.TekstGlavni, fontFamily = Poppins(),
                                )
                                Text(
                                    "$ukupnoKom komada", fontSize = 12.sp,
                                    color = Boje.TekstSporedni, fontFamily = Poppins(),
                                )
                            }
                            Text(
                                if (ukupno > 0) Cjenovnik.format(ukupno) else "na upit",
                                fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Boje.Vino,
                                fontFamily = Poppins(),
                            )
                        }
                        if (nekeBezCijene && ukupno > 0) {
                            Text(
                                "* Neke stavke nemaju postavljenu cijenu — iznos je orijentacioni.",
                                fontSize = 11.sp, color = Boje.TekstSvijetli, fontFamily = Poppins(),
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }

            // ===== STICKY DNO =====
            val sivo = prazna || zatvorena || nekeNedostupne
            val dugmeTekst = when {
                prazna -> "Korpa je prazna"
                zatvorena -> "Sezona zatvorena — narudžbe nisu moguće"
                nekeNedostupne -> "Uklonite stavke koje nisu na stanju"
                ukupno > 0 -> "Nastavi do narudžbe · " + Cjenovnik.format(ukupno)
                else -> "Nastavi do narudžbe"
            }
            Column(
                Modifier.fillMaxWidth()
                    .onSizeChanged { visinaDna = with(gustina) { it.height.toDp() } }
                    .shadow(12.dp, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .background(Boje.PovrsinaTopla)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp + dno)
            ) {
                Button(
                    onClick = {
                        when {
                            lista.isEmpty() -> Unit
                            Sezona.zatvorena() -> dijalogSezona = true
                            nekeNedostupne -> {
                                porukaZelena = false
                                scope.launch {
                                    pokaziPoruku(this, poruke, "Uklonite stavke koje nisu na stanju", null) {}
                                }
                            }
                            else -> nav.otvori(Ekran.Narudzba)
                        }
                    },
                    enabled = !prazna && stavke != null,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (sivo) Boje.TekstSvijetli else Boje.Vino,
                        contentColor = Color.White,
                        disabledContainerColor = Boje.TekstSvijetli,
                        disabledContentColor = Color.White,
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Text(
                        dugmeTekst, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins(),
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false),
                    )
                    Icon(
                        painterResource(Res.drawable.ic_arrow_right), contentDescription = null,
                        modifier = Modifier.padding(start = 8.dp).size(18.dp),
                    )
                }
            }
        }

        // ===== PORUKA (iznad donje trake) =====
        SnackbarHost(
            poruke,
            Modifier.align(Alignment.BottomCenter).padding(bottom = visinaDna),
        ) { d ->
            if (porukaZelena) {
                Snackbar(d, containerColor = Boje.ZelenaTamna, contentColor = Color.White, actionColor = Boje.ZlatnaSvijetla)
            } else {
                Snackbar(d)
            }
        }
    }

    if (dijalogIsprazni) {
        InfoDijalog(
            ikona = "🗑️",
            naslov = "Isprazniti korpu?",
            sadrzaj = "Sve stavke će biti uklonjene iz korpe.",
            onZatvori = { dijalogIsprazni = false },
            pozitivTekst = "Isprazni",
            onPozitiv = { scope.launch { Korpa.isprazni() } },
            neutralTekst = "Otkaži",
        )
    }
    if (dijalogSezona) {
        InfoDijalog(
            ikona = "ℹ️",
            naslov = "Sezona zatvorena",
            sadrzaj = Sezona.porukaZatvoreno(),
            onZatvori = { dijalogSezona = false },
            pozitivTekst = "U redu",
        )
    }
}

/** Snackbar LENGTH_LONG (2,75 s) sa opcionalnom akcijom. */
private suspend fun pokaziPoruku(
    scope: CoroutineScope,
    host: SnackbarHostState,
    tekst: String,
    akcija: String?,
    onAkcija: () -> Unit,
) {
    host.currentSnackbarData?.dismiss()
    val tajmer = scope.launch {
        delay(TRAJANJE_PORUKE_MS)
        host.currentSnackbarData?.dismiss()
    }
    val r = host.showSnackbar(tekst, actionLabel = akcija, duration = SnackbarDuration.Indefinite)
    tajmer.cancel()
    if (r == SnackbarResult.ActionPerformed) onAkcija()
}

@Composable
private fun RedSazetka(lijevo: String, desno: String, modifier: Modifier) {
    Row(modifier.fillMaxWidth()) {
        Text(lijevo, fontSize = 13.sp, color = Boje.TekstSporedni, fontFamily = Poppins(), modifier = Modifier.weight(1f))
        Text(desno, fontSize = 13.sp, color = Boje.TekstGlavni, fontFamily = Poppins())
    }
}

/** item_korpa.xml */
@Composable
private fun StavkaKartica(r: RedKorpe, onMinus: () -> Unit, onPlus: () -> Unit, onObrisi: () -> Unit) {
    val s = r.s
    val jeSadnica = s.tip == TIP_SADNICA
    val meta = Sorte.poNazivu(s.sorta)

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Boje.PovrsinaTopla),
        border = BorderStroke(0.5.dp, Boje.ObrubMeki),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(12.dp)) {
            SlikaSorteMreza(
                meta?.kljuc ?: Sorte.kljucOd(s.sorta), meta?.slikaUrl,
                Modifier.size(72.dp).clip(RoundedCornerShape(14.dp)),
            )
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        s.sorta, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Boje.TekstGlavni,
                        fontFamily = Poppins(), maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        painterResource(Res.drawable.ic_trash), contentDescription = "Ukloni",
                        tint = Boje.TekstSvijetli,
                        modifier = Modifier.size(30.dp).clip(RoundedCornerShape(100.dp))
                            .clickable(onClick = onObrisi).padding(6.dp),
                    )
                }
                Row(Modifier.padding(top = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (jeSadnica) "Sadnica" else "Reznica", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = if (jeSadnica) Boje.TipSadnica else Boje.TipReznica, fontFamily = Poppins(),
                        modifier = Modifier.clip(RoundedCornerShape(100.dp))
                            .background(if (jeSadnica) Boje.TipSadnicaPozadina else Boje.TipReznicaPozadina)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                    Text(
                        if (r.jedinicna > 0) Cjenovnik.format(r.jedinicna) + " / kom" else "cijena na upit",
                        fontSize = 12.sp, color = Boje.TekstSporedni, fontFamily = Poppins(),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                if (!r.dostupno) {
                    Text(
                        "⚠ Trenutno nema na stanju — uklonite stavku", fontSize = 11.sp, color = Boje.Vino,
                        fontFamily = Poppins(), modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Row(Modifier.padding(top = 8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        Modifier.height(36.dp).clip(RoundedCornerShape(100.dp)).background(Boje.ChipPozadina)
                            .padding(horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painterResource(Res.drawable.ic_minus), contentDescription = "Manje",
                            tint = Boje.TekstGlavni,
                            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(100.dp))
                                .clickable(onClick = onMinus).padding(10.dp),
                        )
                        Text(
                            s.kolicina.toString(), fontSize = 15.sp, fontWeight = FontWeight.Bold,
                            color = Boje.TekstGlavni, fontFamily = Poppins(), textAlign = TextAlign.Center,
                            modifier = Modifier.width(30.dp),
                        )
                        Icon(
                            painterResource(Res.drawable.ic_plus), contentDescription = "Više",
                            tint = Boje.TekstGlavni,
                            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(100.dp))
                                .clickable(onClick = onPlus).padding(10.dp),
                        )
                    }
                    Text(
                        if (r.jedinicna > 0) Cjenovnik.format(r.iznos) else "—",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Boje.Vino,
                        fontFamily = Poppins(), textAlign = TextAlign.End, modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
