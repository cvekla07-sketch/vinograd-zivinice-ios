package com.vinograd.zivinice.ekrani

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vinograd.zivinice.Ekran
import com.vinograd.zivinice.Navigator
import com.vinograd.zivinice.podaci.Cijena
import com.vinograd.zivinice.podaci.Cjenovnik
import com.vinograd.zivinice.podaci.Sorta
import com.vinograd.zivinice.podaci.Sorte
import com.vinograd.zivinice.resources.Res
import com.vinograd.zivinice.resources.ic_arrow_back
import com.vinograd.zivinice.resources.ic_close_small
import com.vinograd.zivinice.resources.ic_search
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.DugmeKrug
import com.vinograd.zivinice.ui.Poppins
import com.vinograd.zivinice.ui.SlikaSorteMreza
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/** Filteri cjenovnika — isti redoslijed i nazivi kao čipovi u activity_cjenovnik.xml. */
private enum class FilterCjen(val naziv: String) {
    SVE("Sve"), SADNICE("🌱 Sadnice"), REZNICE("✂️ Reznice"), NA_STANJU("Na stanju")
}

private val SIRINA_KOLONE = 76.dp

/** Cjenovnik (1:1 CjenovnikActivity + activity_cjenovnik.xml + item_cjenovnik_sorta.xml). */
@Composable
fun CjenovnikEkran(nav: Navigator) {
    val sorte by Sorte.sve.collectAsState()
    val cijene by Cjenovnik.mapa.collectAsState()
    val ucitan by Cjenovnik.ucitan.collectAsState()

    var upit by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(FilterCjen.SVE) }
    val lista = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val fokus = LocalFocusManager.current

    val vrh = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val dno = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // primijeniFilter()
    val q = upit.trim().lowercase()
    val rez: List<Sorta> = remember(sorte, cijene, q, filter) {
        sorte.filter { s ->
            if (q.isNotEmpty() && !s.naziv.lowercase().contains(q)) return@filter false
            val c = cijene[s.kljuc]
            when (filter) {
                FilterCjen.SADNICE -> c != null && c.cijenaSadnica > 0
                FilterCjen.REZNICE -> c != null && c.cijenaReznica > 0
                FilterCjen.NA_STANJU -> c != null && c.imaCijenu() && !c.nistaNaStanju()
                FilterCjen.SVE -> true
            }
        }
    }
    val podnaslov = if (q.isEmpty() && filter == FilterCjen.SVE) "Sezona 2026 · ${sorte.size} sorti"
    else "${rez.size} od ${sorte.size} sorti"

    Column(Modifier.fillMaxSize().background(Boje.Krem)) {

        // ===== HEADER =====
        Column(
            Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Boje.ZelenaSrednja, Boje.ZelenaTamna)))
                .padding(start = 16.dp, end = 16.dp, top = vrh + 10.dp, bottom = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DugmeKrug(Res.drawable.ic_arrow_back, Color(0x26FFFFFF), { nav.nazad() })
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        "Cjenovnik", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        color = Color.White, fontFamily = Poppins(),
                    )
                    Text(
                        podnaslov, fontSize = 12.sp, color = Color.White, fontFamily = Poppins(),
                        modifier = Modifier.alpha(0.8f),
                    )
                }
                if (!ucitan) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                }
            }

            // Pretraga
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

        // ===== FILTER =====
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(start = 12.dp, end = 12.dp, top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilterCjen.entries.forEach { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = {
                        if (filter != f) {
                            filter = f
                            scope.launch { lista.scrollToItem(0) }
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

        // ===== ZAGLAVLJE KOLONA =====
        Row(Modifier.fillMaxWidth().padding(start = 26.dp, end = 26.dp, top = 12.dp, bottom = 4.dp)) {
            Text(
                "SORTA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Boje.TekstSvijetli,
                fontFamily = Poppins(), letterSpacing = 0.12.em, modifier = Modifier.weight(1f),
            )
            Text(
                "SADNICA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Boje.TipSadnica,
                fontFamily = Poppins(), letterSpacing = 0.1.em, textAlign = TextAlign.Center,
                modifier = Modifier.width(SIRINA_KOLONE),
            )
            Text(
                "REZNICA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Boje.TipReznica,
                fontFamily = Poppins(), letterSpacing = 0.1.em, textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 6.dp).width(SIRINA_KOLONE),
            )
        }

        // ===== LISTA =====
        LazyColumn(
            state = lista,
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = dno + 12.dp),
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) {
            itemsIndexed(rez) { i, s ->
                val slovo = pocetnoSlovo(s.naziv)
                val novoSlovo = i == 0 || slovo != pocetnoSlovo(rez[i - 1].naziv)
                RedCjenovnika(s, cijene[s.kljuc], if (novoSlovo) slovo else null) {
                    nav.otvori(Ekran.Detalji(s.naziv))
                }
            }
        }
    }
}

private fun pocetnoSlovo(naziv: String): String = if (naziv.isEmpty()) "" else naziv.substring(0, 1).uppercase()

/** item_cjenovnik_sorta.xml */
@Composable
private fun RedCjenovnika(s: Sorta, c: Cijena?, slovo: String?, onKlik: () -> Unit) {
    Column {
        if (slovo != null) {
            Text(
                slovo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Boje.Vino, fontFamily = Poppins(),
                modifier = Modifier.padding(top = 14.dp, bottom = 6.dp, start = 4.dp),
            )
        }
        Row(
            Modifier.padding(bottom = 6.dp).fillMaxWidth()
                .clip(RoundedCornerShape(18.dp)).background(Boje.PovrsinaTopla)
                .border(0.5.dp, Boje.ObrubMeki, RoundedCornerShape(18.dp))
                .clickable(onClick = onKlik)
                .padding(start = 8.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SlikaSorteMreza(s.kljuc, s.slikaUrl, Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)))
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text(
                    s.naziv, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Boje.TekstGlavni,
                    fontFamily = Poppins(), maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
                Text(
                    s.tipPrikaz + " · " + s.boja.naziv, fontSize = 11.sp, color = Boje.TekstSvijetli,
                    fontFamily = Poppins(),
                )
            }
            PillCijene(
                c?.cijenaSadnica ?: 0.0, c == null || c.naStanjuSadnica,
                Boje.TipSadnica, Boje.TipSadnicaPozadina, Modifier,
            )
            PillCijene(
                c?.cijenaReznica ?: 0.0, c == null || c.naStanjuReznica,
                Boje.TipReznica, Boje.TipReznicaPozadina, Modifier.padding(start = 6.dp),
            )
        }
    }
}

/** popuniCijenu(): "—" / precrtano (nema na stanju) / cijena u boji tipa. */
@Composable
private fun PillCijene(cijena: Double, naStanju: Boolean, boja: Color, bojaPozadine: Color, modifier: Modifier) {
    val (tekst, bojaTeksta, prozirnost, precrtano) = when {
        cijena <= 0 -> Cetvorka("—", Boje.TekstSvijetli, 0.6f, false)
        !naStanju -> Cetvorka(Cjenovnik.format(cijena), Boje.Vino, 0.7f, true)
        else -> Cetvorka(Cjenovnik.format(cijena), boja, 1f, false)
    }
    Text(
        tekst, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = bojaTeksta, fontFamily = Poppins(),
        textAlign = TextAlign.Center, maxLines = 1,
        textDecoration = if (precrtano) TextDecoration.LineThrough else null,
        modifier = modifier.width(SIRINA_KOLONE).alpha(prozirnost)
            .clip(RoundedCornerShape(100.dp)).background(bojaPozadine)
            .padding(vertical = 7.dp),
    )
}

private data class Cetvorka(val tekst: String, val boja: Color, val prozirnost: Float, val precrtano: Boolean)
