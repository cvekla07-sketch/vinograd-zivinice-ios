package com.vinograd.zivinice.ekrani

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vinograd.zivinice.Ekran
import com.vinograd.zivinice.Navigator
import com.vinograd.zivinice.podaci.Cijena
import com.vinograd.zivinice.podaci.Cjenovnik
import com.vinograd.zivinice.podaci.Korpa
import com.vinograd.zivinice.podaci.Sorta
import com.vinograd.zivinice.podaci.Sorte
import com.vinograd.zivinice.resources.Res
import com.vinograd.zivinice.resources.ic_arrow_back
import com.vinograd.zivinice.resources.ic_cart
import com.vinograd.zivinice.resources.ic_close_small
import com.vinograd.zivinice.resources.ic_search
import com.vinograd.zivinice.ui.BojaBobice
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.DugmeKrug
import com.vinograd.zivinice.ui.Poppins
import com.vinograd.zivinice.ui.SlikaSorteMreza
import com.vinograd.zivinice.ui.TackaBobice
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/** Filteri kataloga — isti redoslijed i nazivi kao čipovi u activity_sadnica.xml. */
private enum class Filter(val naziv: String) {
    SVE("Sve"), KISMIS("Kišmiš"), STOLNE("Stolne"), BIJELE("Bijele"), ROZA("Roza / crvene"), CRNE("Crne / plave")
}

/** Katalog sorti (1:1 SadnicaActivity + activity_sadnica.xml). */
@Composable
fun KatalogEkran(nav: Navigator) {
    val sorte by Sorte.sve.collectAsState()
    val cijene by Cjenovnik.mapa.collectAsState()
    val korpa by Korpa.stavke.collectAsState(initial = emptyList())

    var upit by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(Filter.SVE) }
    val grid = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val fokus = LocalFocusManager.current

    val vrh = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val dno = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // primijeniFilter()
    val q = upit.trim().lowercase()
    val rezultat: List<Sorta> = remember(sorte, q, filter) {
        sorte.filter { s ->
            if (q.isNotEmpty() && !s.naziv.lowercase().contains(q)) return@filter false
            when (filter) {
                Filter.KISMIS -> s.kismis
                Filter.STOLNE -> !s.kismis
                Filter.BIJELE -> s.boja == BojaBobice.BIJELA
                Filter.ROZA -> s.boja == BojaBobice.ROZA
                Filter.CRNE -> s.boja == BojaBobice.CRNA
                Filter.SVE -> true
            }
        }
    }
    val brojTekst = if (q.isEmpty() && filter == Filter.SVE) "${sorte.size} sorti · sadnice i reznice"
    else "${rezultat.size} od ${sorte.size} sorti"

    Column(Modifier.fillMaxSize().background(Boje.Krem)) {

        // ===== HEADER =====
        Column(
            Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Boje.ZelenaSrednja, Boje.ZelenaTamna)))
                .padding(start = 16.dp, end = 16.dp, top = vrh + 10.dp, bottom = 14.dp)
        ) {
            // Red 1: nazad · naslov · korpa
            Row(verticalAlignment = Alignment.CenterVertically) {
                DugmeKrug(Res.drawable.ic_arrow_back, Color(0x26FFFFFF), { nav.nazad() })
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        "Katalog sorti", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        color = Color.White, fontFamily = Poppins(),
                    )
                    Text(
                        brojTekst, fontSize = 12.sp, color = Color.White, fontFamily = Poppins(),
                        modifier = Modifier.alpha(0.8f),
                    )
                }
                DugmeKrug(
                    Res.drawable.ic_cart, Color(0x26FFFFFF), { nav.otvori(Ekran.Korpa) },
                    znacka = korpa.size,
                )
            }

            // Red 2: pretraga
            Row(
                Modifier.padding(top = 14.dp).fillMaxWidth().height(44.dp)
                    .clip(RoundedCornerShape(100.dp)).background(Boje.PovrsinaTopla)
                    .padding(start = 14.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painterResource(Res.drawable.ic_search), contentDescription = "Pretraga",
                    tint = Boje.TekstSvijetli, modifier = Modifier.size(18.dp),
                )
                BasicTextField(
                    value = upit,
                    onValueChange = { upit = it },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = Boje.TekstGlavni, fontFamily = Poppins()),
                    cursorBrush = SolidColor(Boje.ZelenaTamna),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { fokus.clearFocus() }),
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    decorationBox = { polje ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (upit.isEmpty()) {
                                Text("Pretraži sortu…", fontSize = 14.sp, color = Boje.TekstSvijetli, fontFamily = Poppins())
                            }
                            polje()
                        }
                    },
                )
                if (upit.trim().isNotEmpty()) {
                    Icon(
                        painterResource(Res.drawable.ic_close_small), contentDescription = "Obriši",
                        tint = Boje.TekstSvijetli,
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(100.dp))
                            .clickable { upit = "" }.padding(7.dp),
                    )
                }
            }
        }

        // ===== FILTER ČIPOVI =====
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Filter.entries.forEach { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = {
                        if (filter != f) {
                            filter = f
                            scope.launch { grid.scrollToItem(0) }
                        }
                    },
                    label = { Text(f.naziv, fontFamily = Poppins()) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Boje.ChipPozadina,
                        labelColor = Boje.TekstGlavni,
                        selectedContainerColor = Boje.ChipOdabran,
                        selectedLabelColor = Color.White,
                    ),
                    border = null,
                )
            }
        }

        // ===== GRID =====
        Box(Modifier.fillMaxWidth().weight(1f)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = grid,
                contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 6.dp, bottom = dno + 10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(rezultat) { s ->
                    KarticaSorte(s, cijene) { nav.otvori(Ekran.Detalji(s.naziv)) }
                }
            }

            if (rezultat.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("🍇", fontSize = 40.sp)
                    Text(
                        "Nema sorte s tim nazivom", fontSize = 15.sp, fontWeight = FontWeight.Bold,
                        color = Boje.TekstGlavni, fontFamily = Poppins(), modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(
                        "Pokušajte drugi naziv ili filter", fontSize = 13.sp,
                        color = Boje.TekstSporedni, fontFamily = Poppins(), modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

/** item_sadnica_kartica.xml */
@Composable
private fun KarticaSorte(s: Sorta, cijene: Map<String, Cijena>, onKlik: () -> Unit) {
    val c = cijene[s.kljuc]
    val nema = c != null && c.nistaNaStanju()
    val cijenaTekst = remember(s, cijene) { Cjenovnik.prikazObje(s.naziv) }

    Card(
        onClick = onKlik,
        modifier = Modifier.fillMaxWidth().padding(5.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Boje.PovrsinaTopla),
        border = BorderStroke(0.5.dp, Boje.ObrubMeki),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(150.dp)) {
            SlikaSorteMreza(
                s.kljuc, s.slikaUrl,
                Modifier.fillMaxSize().alpha(if (nema) 0.45f else 1f),
            )
            Text(
                s.tipPrikaz, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White,
                fontFamily = Poppins(), letterSpacing = 0.05.em,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                    .clip(RoundedCornerShape(100.dp)).background(Color(0xCC1F3D2E))
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            )
            if (nema) {
                Text(
                    "Nema na stanju", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White,
                    fontFamily = Poppins(),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                        .clip(RoundedCornerShape(100.dp)).background(Boje.Vino)
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                )
            }
        }
        Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TackaBobice(s.boja, Modifier.padding(end = 6.dp))
                Text(
                    s.naziv, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Boje.TekstGlavni,
                    fontFamily = Poppins(), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                cijenaTekst, fontSize = 11.5.sp, lineHeight = 16.sp, maxLines = 2,
                fontWeight = FontWeight.Bold, color = Boje.Vino, fontFamily = Poppins(),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
