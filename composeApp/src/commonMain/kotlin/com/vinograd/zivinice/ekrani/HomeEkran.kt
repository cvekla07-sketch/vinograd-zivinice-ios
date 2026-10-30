package com.vinograd.zivinice.ekrani

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.vinograd.zivinice.Ekran
import com.vinograd.zivinice.Navigator
import com.vinograd.zivinice.platform.Datum
import com.vinograd.zivinice.platform.otvoriLink
import com.vinograd.zivinice.podaci.Cjenovnik
import com.vinograd.zivinice.podaci.Korpa
import com.vinograd.zivinice.podaci.Objava
import com.vinograd.zivinice.podaci.Objave
import com.vinograd.zivinice.podaci.Sezona
import com.vinograd.zivinice.podaci.Sorta
import com.vinograd.zivinice.podaci.Sorte
import com.vinograd.zivinice.resources.Res
import com.vinograd.zivinice.resources.ic_arrow_right
import com.vinograd.zivinice.resources.ic_book
import com.vinograd.zivinice.resources.ic_calendar
import com.vinograd.zivinice.resources.ic_cart
import com.vinograd.zivinice.resources.ic_contact
import com.vinograd.zivinice.resources.ic_grape
import com.vinograd.zivinice.resources.ic_home
import com.vinograd.zivinice.resources.ic_info
import com.vinograd.zivinice.resources.ic_leaf
import com.vinograd.zivinice.resources.ic_location
import com.vinograd.zivinice.resources.ic_menu
import com.vinograd.zivinice.resources.ic_news
import com.vinograd.zivinice.resources.ic_price_tag
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.InfoDijalog
import com.vinograd.zivinice.ui.Poppins
import com.vinograd.zivinice.ui.SlikaSorte
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Sorte koje se ističu na početnoj (redom) — kao MainActivity.ISTAKNUTE. */
private val ISTAKNUTE = listOf(
    "Arkadia", "Kišmiš Lučistij", "Alvika", "Preobraženje", "Viktor",
    "Bajkonur", "Kišmiš Veles", "Tajson", "Rusko rano", "Kodrianka",
)

/** Isti link kao Android (otvoriLokacijuNaMapi, rezervni URL). */
private const val LINK_MAPA = "https://www.google.com/maps/search/?api=1&query=Vinograd+%C5%BDivinice"

private const val TEKST_O_APLIKACIJI =
    "Aplikacija Vinograd Živinice predstavlja moderan vodič kroz svijet vinogradarstva.\n\n" +
        "Korisnicima omogućava pregled sorti vinove loze, slanje narudžbi i pristup " +
        "korisnim savjetima i informacijama o uzgoju grožđa."

// ---------------- Hero SEZONA (1:1 azurirajBrojacSezone) ----------------

private data class Hero(val nadnaslov: String, val datum: String, val podnaslov: String, val dugme: String)

private fun heroStanje(s: Sezona.Stanje): Hero {
    val danas = Datum.danas()
    val g = danas.godina
    val sezonaStart = Datum(g, 9, 1)
    val krajRezervacija = Datum(g, 11, 14)
    val krajProdaje = Datum(g, 12, 31)

    // Ručno stanje iz admin panela ima prednost nad datumima
    if (s.status == Sezona.ZATVORENA) return Hero(
        "SEZONA $g", "Sezona zatvorena",
        s.poruka ?: "Hvala na povjerenju! Vidimo se sljedeće sezone 🍇", "Pogledaj sorte  →",
    )
    if (s.status == Sezona.OTVORENA) return Hero(
        "SEZONA $g", "Sezona u toku",
        s.poruka ?: "Naručite sadnice i reznice odmah", "Otvori katalog  →",
    )
    return when {
        danas < sezonaStart -> {
            val danaDo = sezonaStart.epohaDan - danas.epohaDan
            Hero("SEZONA $g", "Počinje 01. sep", "za $danaDo dana · rezervišite unaprijed", "Rezerviši  →")
        }
        danas <= krajRezervacija ->
            Hero("SEZONA $g", "Sezona u toku", "Naručite sadnice i reznice odmah", "Otvori katalog  →")
        danas <= krajProdaje ->
            Hero("SEZONA $g", "Prodaja u toku", "Naručite dok traju zalihe", "Otvori katalog  →")
        else -> {
            val sljedeci = Datum(g + 1, 9, 1)
            val danaDo = sljedeci.epohaDan - danas.epohaDan
            Hero("SEZONA ${g + 1}", "Počinje 01. sep", "za $danaDo dana", "Pogledaj sorte  →")
        }
    }
}

/** Period rezervacija → rezervacije; inače katalog (1:1 otvoriHeroAkciju). */
private fun heroAkcija(nav: Navigator) {
    val danas = Datum.danas()
    val start = Datum(danas.godina, 9, 1)
    when {
        Sezona.zatvorena() || Sezona.otvorena() -> nav.otvori(Ekran.Katalog)
        danas < start -> nav.otvori(Ekran.Rezervacija)
        else -> nav.otvori(Ekran.Katalog)
    }
}

// ---------------- Ekran ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeEkran(nav: Navigator) {
    val sorte by Sorte.sve.collectAsState()
    val sezona by Sezona.stanje.collectAsState()
    val cijene by Cjenovnik.mapa.collectAsState()
    val korpa by Korpa.stavke.collectAsState(initial = emptyList())
    val objave by Objave.lista.collectAsState()
    val procitane by Objave.procitane.collectAsState()
    val nove = objave.filter { Objave.jeNova(it, procitane) }

    var prikaziVise by remember { mutableStateOf(false) }
    var prikaziInfo by remember { mutableStateOf(false) }

    val vrh = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val dno = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val hero = heroStanje(sezona)

    // Istaknute sorte + tekst cijene (osvježava se kad stignu sorte ili cjenovnik)
    val istaknute: List<Pair<Sorta, String>> = remember(sorte, cijene) {
        val l = ISTAKNUTE.mapNotNull { Sorte.poNazivu(it) }
            .ifEmpty { sorte.take(10) }   // ako nijedna istaknuta ne postoji — prvih 10
        l.map { it to Cjenovnik.prikazObje(it.naziv) }
    }

    Box(Modifier.fillMaxSize().background(Boje.Krem)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // ===== HEADER + HERO =====
            Box(Modifier.fillMaxWidth()) {
                Box(
                    Modifier.fillMaxWidth().height(vrh + 128.dp)
                        .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
                        .background(Brush.verticalGradient(listOf(Boje.ZelenaSrednja, Boje.ZelenaTamna)))
                )
                Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = vrh + 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(44.dp).clip(CircleShape).background(Color(0x26FFFFFF)),
                            contentAlignment = Alignment.Center,
                        ) { Ikona(Res.drawable.ic_grape, Color.White, 24) }
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(
                                "Dobro došli 🍇", fontSize = 12.sp, color = Color.White,
                                fontFamily = Poppins(), modifier = Modifier.alpha(0.85f),
                            )
                            Text(
                                "Vinograd Živinice", fontSize = 19.sp, fontWeight = FontWeight.Bold,
                                color = Color.White, fontFamily = Poppins(),
                            )
                        }
                        Box(
                            Modifier.size(44.dp).clip(CircleShape).background(Color(0x26FFFFFF))
                                .clickable { nav.otvori(Ekran.Korpa) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Ikona(Res.drawable.ic_cart, Color.White, 22)
                            if (korpa.isNotEmpty()) {
                                Box(
                                    Modifier.align(Alignment.TopEnd).size(18.dp)
                                        .clip(RoundedCornerShape(100.dp)).background(Boje.Vino),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        if (korpa.size > 9) "9+" else korpa.size.toString(),
                                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White,
                                    )
                                }
                            }
                        }
                    }

                    // Hero kartica
                    Column(
                        Modifier.padding(top = 14.dp).fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.linearGradient(listOf(Boje.VinoTamno, Boje.VinoSvijetlo)))
                            .clickable { heroAkcija(nav) }
                            .padding(horizontal = 16.dp, vertical = 13.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    hero.nadnaslov, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                    color = Color.White, fontFamily = Poppins(), letterSpacing = 1.65.sp,
                                    modifier = Modifier.alpha(0.85f),
                                )
                                Text(
                                    hero.datum, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                                    color = Color.White, fontFamily = Poppins(), modifier = Modifier.padding(top = 2.dp),
                                )
                                Text(
                                    hero.podnaslov, fontSize = 13.sp, color = Color.White,
                                    fontFamily = Poppins(), modifier = Modifier.alpha(0.9f),
                                )
                            }
                            Text("🍇", fontSize = 26.sp)
                        }
                        Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                hero.dugme, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                color = Boje.VinoTamno, fontFamily = Poppins(),
                                modifier = Modifier.clip(RoundedCornerShape(100.dp))
                                    .background(Boje.PovrsinaTopla)
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                            )
                            Text(
                                "${sorte.size} sorti", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                color = Color.White, fontFamily = Poppins(), maxLines = 1, textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f).padding(start = 12.dp).alpha(0.85f),
                            )
                        }
                    }
                }
            }

            // ===== NOVO IZ VINOGRADA — samo dok postoji nepročitana objava =====
            nove.firstOrNull()?.let { o ->
                TrakaNovosti(o) { nav.otvori(Ekran.JednaObjava(o.id)) }
            }

            // ===== ISTAKNUTE SORTE =====
            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Naslov("Istaknute sorte", Modifier.weight(1f))
                Text(
                    "Sve sorte →", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Boje.Vino,
                    fontFamily = Poppins(),
                    modifier = Modifier.clickable { nav.otvori(Ekran.Katalog) }.padding(4.dp),
                )
            }
            LazyRow(
                Modifier.padding(top = 8.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(istaknute, key = { it.first.kljuc }) { (s, cijena) ->
                    KarticaIstaknute(s, cijena) { nav.otvori(Ekran.Detalji(s.naziv)) }
                }
            }

            // ===== BRZI PRISTUP =====
            Naslov("Brzi pristup", Modifier.padding(top = 14.dp, start = 16.dp))
            Row(Modifier.fillMaxWidth().padding(top = 8.dp, start = 12.dp, end = 12.dp)) {
                BrzaKartica("Katalog", Res.drawable.ic_grape, Boje.KoralPastel, Boje.KoralSrednja) { nav.otvori(Ekran.Katalog) }
                BrzaKartica("Cjenovnik", Res.drawable.ic_price_tag, Boje.ZelenaPastel, Boje.ZelenaSvijetla) { nav.otvori(Ekran.Cjenovnik) }
                BrzaKartica("Rezerviši", Res.drawable.ic_calendar, Boje.LjubicastaPastel, Boje.LjubicastaSrednja) { nav.otvori(Ekran.Rezervacija) }
                BrzaKartica("Novosti", Res.drawable.ic_news, Boje.ZlatnaSvijetla, Boje.VinoTamno, znacka = nove.size) { nav.otvori(Ekran.Novosti) }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                BrzaKartica("Vodič", Res.drawable.ic_book, Boje.NarandzastaPastel, Boje.NarandzastaSrednja) { nav.otvori(Ekran.Vodic) }
                BrzaKartica("Lokacija", Res.drawable.ic_location, Boje.RozaPastel, Boje.RozaSrednja) { otvoriLink(LINK_MAPA) }
                BrzaKartica("Kontakt", Res.drawable.ic_contact, Boje.VinoPastel, Boje.Vino) { nav.otvori(Ekran.Kontakt) }
                BrzaKartica("Tretmani", Res.drawable.ic_leaf, Boje.PlavaPastel, Boje.PlavaSrednja) { nav.otvori(Ekran.Tretmani) }
            }

            Spacer(Modifier.height(96.dp + dno))
        }

        // ===== DONJA NAVIGACIJA =====
        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .shadow(12.dp)
                .background(Boje.ZelenaTamna)
                .padding(bottom = dno)
                .height(80.dp),
            verticalAlignment = Alignment.Top,
        ) {
            NavStavka("Početna", Res.drawable.ic_home, true) {}
            NavStavka("Katalog", Res.drawable.ic_grape, false) { nav.otvori(Ekran.Katalog) }
            NavStavka("Korpa", Res.drawable.ic_cart, false) { nav.otvori(Ekran.Korpa) }
            NavStavka("Tretmani", Res.drawable.ic_leaf, false) { nav.otvori(Ekran.Tretmani) }
            NavStavka("Više", Res.drawable.ic_menu, false) { prikaziVise = true }
        }
    }

    // ===== SHEET "VIŠE" =====
    if (prikaziVise) {
        val stanjeSheeta = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()
        fun zatvoriPa(akcija: () -> Unit) {
            scope.launch { stanjeSheeta.hide() }.invokeOnCompletion {
                prikaziVise = false
                akcija()
            }
        }
        ModalBottomSheet(
            onDismissRequest = { prikaziVise = false },
            sheetState = stanjeSheeta,
            containerColor = Boje.PovrsinaTopla,
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
            dragHandle = {
                Box(
                    Modifier.padding(top = 10.dp).width(40.dp).height(4.dp)
                        .clip(RoundedCornerShape(100.dp)).background(Boje.ObrubMeki)
                )
            },
        ) {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
                Text(
                    "VIŠE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Boje.TekstSvijetli,
                    fontFamily = Poppins(), letterSpacing = 1.3.sp,
                    modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
                )
                RedSheeta(Res.drawable.ic_news, "Novo iz vinograda") { zatvoriPa { nav.otvori(Ekran.Novosti) } }
                RedSheeta(Res.drawable.ic_price_tag, "Cjenovnik") { zatvoriPa { nav.otvori(Ekran.Cjenovnik) } }
                RedSheeta(Res.drawable.ic_calendar, "Rezervacije") { zatvoriPa { nav.otvori(Ekran.Rezervacija) } }
                RedSheeta(Res.drawable.ic_book, "Vinogradarski vodič i dnevnik") { zatvoriPa { nav.otvori(Ekran.Vodic) } }
                RedSheeta(Res.drawable.ic_contact, "Kontakt") { zatvoriPa { nav.otvori(Ekran.Kontakt) } }
                RedSheeta(Res.drawable.ic_location, "Lokacija na mapi") { zatvoriPa { otvoriLink(LINK_MAPA) } }
                RedSheeta(Res.drawable.ic_info, "O aplikaciji") { zatvoriPa { prikaziInfo = true } }
            }
        }
    }

    if (prikaziInfo) {
        InfoDijalog(
            ikona = "🍇", naslov = "O aplikaciji", sadrzaj = TEKST_O_APLIKACIJI,
            onZatvori = { prikaziInfo = false }, pozitivTekst = "Zatvori",
        )
    }
}

// ---------------- dijelovi ekrana ----------------

@Composable
private fun Ikona(res: DrawableResource, boja: Color, velicina: Int) {
    Icon(painterResource(res), contentDescription = null, tint = boja, modifier = Modifier.size(velicina.dp))
}

@Composable
private fun Naslov(tekst: String, modifier: Modifier = Modifier) {
    Text(
        tekst, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Boje.TekstGlavni,
        fontFamily = Poppins(), modifier = modifier,
    )
}

/** item_sorta_istaknuta.xml */
@Composable
private fun KarticaIstaknute(s: Sorta, cijena: String, onKlik: () -> Unit) {
    Card(
        onClick = onKlik,
        modifier = Modifier.width(124.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Boje.PovrsinaTopla),
        border = BorderStroke(0.5.dp, Boje.ObrubMeki),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        SlikaSorte(s.kljuc, Modifier.fillMaxWidth().height(92.dp))
        Column(Modifier.padding(8.dp)) {
            Text(
                s.naziv, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Boje.TekstGlavni,
                fontFamily = Poppins(), maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Text(
                cijena, fontSize = 10.5.sp, lineHeight = 15.sp, minLines = 2, maxLines = 2,
                fontWeight = FontWeight.Bold, color = Boje.Vino, fontFamily = Poppins(),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/** Kartica "Brzi pristup" (bg_quick_card + bg_icon_circle); znacka > 0 → zlatni okvir i broj. */
@Composable
private fun RowScope.BrzaKartica(
    tekst: String, ikona: DrawableResource, pozadinaIkone: Color, bojaIkone: Color,
    znacka: Int = 0, onKlik: () -> Unit,
) {
    Box(Modifier.weight(1f).padding(4.dp)) {
        Column(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Boje.PovrsinaTopla)
                .border(
                    if (znacka > 0) 1.5.dp else 0.5.dp,
                    if (znacka > 0) Boje.Zlatna else Boje.ObrubMeki,
                    RoundedCornerShape(18.dp),
                )
                .clickable(onClick = onKlik)
                .padding(vertical = 11.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(pozadinaIkone),
                contentAlignment = Alignment.Center,
            ) { Ikona(ikona, bojaIkone, 22) }
            Text(
                tekst, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Boje.TekstGlavni,
                fontFamily = Poppins(), maxLines = 1, modifier = Modifier.padding(top = 6.dp),
            )
        }
        if (znacka > 0) {
            Box(
                Modifier.align(Alignment.TopEnd).size(20.dp).clip(CircleShape).background(Boje.Vino),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (znacka > 9) "9+" else znacka.toString(),
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White,
                )
            }
        }
    }
}

/** Traka "Novo iz vinograda" — zlatna, sa slikom objave; vidi se samo dok postoji nepročitana objava. */
@Composable
private fun TrakaNovosti(o: Objava, onKlik: () -> Unit) {
    val oblik = RoundedCornerShape(18.dp)
    val tamnoZlatna = Color(0xFF412402)
    val slika by produceState<String?>(null, o.id) { if (o.imaSliku) value = Objave.slika(o.id) }
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, top = 14.dp, end = 16.dp)
            .shadow(4.dp, oblik)
            .clip(oblik)
            .background(Brush.linearGradient(listOf(Boje.Zlatna, Color(0xFFB8954C))))
            .clickable(onClick = onKlik)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(Boje.ZlatnaSvijetla),
            contentAlignment = Alignment.Center,
        ) {
            if (slika != null) {
                AsyncImage(
                    model = slika, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(),
                )
            } else {
                Ikona(Res.drawable.ic_news, Boje.VinoTamno, 26)
            }
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "NOVO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Boje.VinoTamno,
                    fontFamily = Poppins(), letterSpacing = 1.sp,
                    modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(Color.White)
                        .padding(horizontal = 7.dp, vertical = 1.dp),
                )
                Text(
                    "Novo iz vinograda", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tamnoZlatna,
                    fontFamily = Poppins(), modifier = Modifier.padding(start = 6.dp),
                )
            }
            Text(
                o.naslov, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold,
                color = Boje.TekstGlavni, fontFamily = Poppins(), maxLines = 2, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Ikona(Res.drawable.ic_arrow_right, tamnoZlatna, 20)
    }
}

/** Stavka donje navigacije (bottom_nav_menu + nav_item_color). */
@Composable
private fun RowScope.NavStavka(tekst: String, ikona: DrawableResource, odabrana: Boolean, onKlik: () -> Unit) {
    val boja = if (odabrana) Color.White else Color(0x99FFFFFF)
    // Material 3 BottomNavigationView: visina 80 dp, 12 dp odozgo, pill 64×32, tekst 4 dp ispod
    Column(
        Modifier.weight(1f).fillMaxHeight().clickable(onClick = onKlik).padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(width = 64.dp, height = 32.dp).clip(RoundedCornerShape(100.dp))
                .background(if (odabrana) Color(0x33FFFFFF) else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) { Ikona(ikona, boja, 24) }
        Text(
            tekst, fontSize = 12.sp, color = boja, fontFamily = Poppins(), maxLines = 1,
            fontWeight = if (odabrana) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** Red u sheetu "Više" (item_sheet_red.xml). */
@Composable
private fun RedSheeta(ikona: DrawableResource, tekst: String, onKlik: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onKlik).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(Boje.ZelenaPastel),
            contentAlignment = Alignment.Center,
        ) { Ikona(ikona, Boje.ZelenaTamna, 20) }
        Text(
            tekst, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Boje.TekstGlavni,
            fontFamily = Poppins(), modifier = Modifier.weight(1f).padding(start = 14.dp),
        )
        Ikona(Res.drawable.ic_arrow_right, Boje.TekstSvijetli, 18)
    }
}
