package com.vinograd.zivinice.ekrani

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinograd.zivinice.Ekran
import com.vinograd.zivinice.Navigator
import com.vinograd.zivinice.podaci.Cjenovnik
import com.vinograd.zivinice.podaci.Sezona
import com.vinograd.zivinice.podaci.Sorta
import com.vinograd.zivinice.podaci.Sorte
import com.vinograd.zivinice.podaci.StavkaRezervacije
import com.vinograd.zivinice.resources.Res
import com.vinograd.zivinice.resources.ic_arrow_back
import com.vinograd.zivinice.resources.ic_arrow_right
import com.vinograd.zivinice.resources.ic_close_small
import com.vinograd.zivinice.resources.ic_minus
import com.vinograd.zivinice.resources.ic_plus
import com.vinograd.zivinice.resources.ic_search
import com.vinograd.zivinice.ui.BojaBobice
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.DugmeKrug
import com.vinograd.zivinice.ui.InfoDijalog
import com.vinograd.zivinice.ui.Poppins
import com.vinograd.zivinice.ui.SlikaSorteMreza
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Filteri rezervacije — isti redoslijed i nazivi kao čipovi u activity_rezervacija.xml. */
private enum class FilterRez(val naziv: String) {
    SVE("Sve"), KISMIS("Kišmiš"), STOLNE("Stolne"), BIJELE("Bijele"),
    ROZA("Roza / crvene"), CRNE("Crne / plave"), ODABRANE("✓ Odabrane")
}

/** Količine se čuvaju kao lista "naziv\u0001broj" (sigurno za rememberSaveable na svakoj platformi). */
private val MapaSaver = Saver<Map<String, Int>, ArrayList<String>>(
    save = { m -> ArrayList(m.map { "${it.key}\u0001${it.value}" }) },
    restore = { l ->
        l.mapNotNull {
            val d = it.split('\u0001')
            if (d.size == 2) d[0] to (d[1].toIntOrNull() ?: 0) else null
        }.toMap()
    },
)

private val FilterSaver = Saver<FilterRez, String>(save = { it.name }, restore = { FilterRez.valueOf(it) })

/** Rezervacija — korak 1 (1:1 RezervacijaActivity + activity_rezervacija.xml + item_rezervacija_sorta.xml). */
@Composable
fun RezervacijaEkran(nav: Navigator) {
    // Sezona zatvorena pri otvaranju → samo poruka, pa nazad (kao onCreate + finish)
    val zatvorenaPriOtvaranju = remember { Sezona.zatvorena() }
    if (zatvorenaPriOtvaranju) {
        Box(Modifier.fillMaxSize().background(Boje.Krem))
        InfoDijalog(
            ikona = "ℹ️",
            naslov = "Rezervacije zatvorene",
            sadrzaj = Sezona.porukaZatvoreno(),
            onZatvori = { nav.nazad() },
            pozitivTekst = "U redu",
        )
        return
    }

    val sorte by Sorte.sve.collectAsState()
    val cijene by Cjenovnik.mapa.collectAsState()

    // Stanje ostaje dok je ekran u stacku (povratak iz kontakta / "Uredi")
    var sadnice by rememberSaveable(stateSaver = MapaSaver) { mutableStateOf(emptyMap<String, Int>()) }
    var reznice by rememberSaveable(stateSaver = MapaSaver) { mutableStateOf(emptyMap<String, Int>()) }
    var upit by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable(stateSaver = FilterSaver) { mutableStateOf(FilterRez.SVE) }

    var dijalogReset by remember { mutableStateOf(false) }
    var ciljSkrola by remember { mutableStateOf<String?>(null) }
    val lista = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val fokus = LocalFocusManager.current

    val vrh = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val dnoNav = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val dnoTastatura = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val dno: Dp = maxOf(dnoNav, dnoTastatura)

    fun postavi(sorta: String, sadnica: Boolean, nova: Int) {
        val k = nova.coerceIn(0, 999)
        val m = (if (sadnica) sadnice else reznice).toMutableMap()
        if (k > 0) m[sorta] = k else m.remove(sorta)
        if (sadnica) sadnice = m else reznice = m
    }

    val odabrane: Set<String> = remember(sadnice, reznice) { (sadnice.keys + reznice.keys).sorted().toSet() }
    val us = sadnice.values.sum()
    val ur = reznice.values.sum()
    val n = odabrane.size

    // Što nije na stanju ne može se rezervisati — obriši količine
    LaunchedEffect(cijene, sadnice, reznice) {
        val bezS = sadnice.keys.filter { s -> Cjenovnik.cijenaZaNaziv(s)?.naStanjuSadnica == false }
        val bezR = reznice.keys.filter { s -> Cjenovnik.cijenaZaNaziv(s)?.naStanjuReznica == false }
        if (bezS.isNotEmpty()) sadnice = sadnice - bezS.toSet()
        if (bezR.isNotEmpty()) reznice = reznice - bezR.toSet()
    }

    // primijeniFilter()
    val q = upit.trim().lowercase()
    val rez: List<Sorta> = remember(sorte, q, filter, if (filter == FilterRez.ODABRANE) odabrane else null) {
        sorte.filter { s ->
            if (q.isNotEmpty() && !s.naziv.lowercase().contains(q)) return@filter false
            when (filter) {
                FilterRez.KISMIS -> s.kismis
                FilterRez.STOLNE -> !s.kismis
                FilterRez.BIJELE -> s.boja == BojaBobice.BIJELA
                FilterRez.ROZA -> s.boja == BojaBobice.ROZA
                FilterRez.CRNE -> s.boja == BojaBobice.CRNA
                FilterRez.ODABRANE -> odabrane.contains(s.naziv)
                FilterRez.SVE -> true
            }
        }
    }

    // skroluNaSortu() — nakon što se pretraga obriše i lista osvježi
    LaunchedEffect(ciljSkrola, rez) {
        val cilj = ciljSkrola ?: return@LaunchedEffect
        val i = rez.indexOfFirst { it.naziv == cilj }
        if (i >= 0) lista.scrollToItem(i)
        ciljSkrola = null
    }

    Box(
        Modifier.fillMaxSize().background(Boje.Krem)
            .pointerInput(Unit) { detectTapGestures { fokus.clearFocus() } }
    ) {
        Column(Modifier.fillMaxSize()) {

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
                            "Rezervacija", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                            color = Color.White, fontFamily = Poppins(),
                        )
                        Text(
                            "Korak 1 od 2 · izaberite sorte i količine", fontSize = 12.sp,
                            color = Color.White, fontFamily = Poppins(), modifier = Modifier.alpha(0.8f),
                        )
                    }
                    if (n > 0) {
                        Text(
                            "Resetuj", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White,
                            fontFamily = Poppins(),
                            modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(Color(0xCC1F3D2E))
                                .clickable { dijalogReset = true }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                        )
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
                FilterRez.entries.forEach { f ->
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

            // ===== ODABRANO (čipovi) =====
            if (n > 0) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        .padding(start = 12.dp, end = 12.dp, top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    odabrane.forEach { sorta ->
                        val s = sadnice[sorta] ?: 0
                        val r = reznice[sorta] ?: 0
                        val t = StringBuilder(sorta)
                        if (s > 0) t.append("  🌱").append(s)
                        if (r > 0) t.append("  ✂️").append(r)
                        InputChip(
                            selected = false,
                            onClick = {
                                if (upit.isNotEmpty()) upit = ""
                                ciljSkrola = sorta
                            },
                            label = { Text(t.toString(), fontSize = 12.sp, fontFamily = Poppins()) },
                            trailingIcon = {
                                Icon(
                                    painterResource(Res.drawable.ic_close_small), contentDescription = "Ukloni",
                                    modifier = Modifier.size(18.dp).clip(RoundedCornerShape(100.dp))
                                        .clickable {
                                            sadnice = sadnice - sorta
                                            reznice = reznice - sorta
                                        },
                                )
                            },
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = Boje.VinoPastel,
                                labelColor = Boje.VinoTamno,
                                trailingIconColor = Boje.Vino,
                            ),
                            border = null,
                        )
                    }
                }
            }

            // ===== LISTA =====
            Box(Modifier.fillMaxWidth().weight(1f)) {
                LazyColumn(
                    state = lista,
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(rez) { s ->
                        val c = cijene[Sorte.kljucZa(s.naziv)]
                        RedRezervacije(
                            s,
                            kolS = sadnice[s.naziv] ?: 0,
                            kolR = reznice[s.naziv] ?: 0,
                            sOk = c == null || c.naStanjuSadnica,
                            rOk = c == null || c.naStanjuReznica,
                            onS = { postavi(s.naziv, true, it) },
                            onR = { postavi(s.naziv, false, it) },
                        )
                    }
                }
                if (rez.isEmpty()) {
                    Text(
                        "Nema sorti za ovaj filter", fontSize = 14.sp, color = Boje.TekstSporedni,
                        fontFamily = Poppins(), modifier = Modifier.align(Alignment.Center),
                    )
                }
            }

            // ===== STICKY DNO =====
            Column(
                Modifier.fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .background(Boje.PovrsinaTopla)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp + dno)
            ) {
                Row(Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (n == 0) "Ništa nije odabrano" else "$n" + (if (n == 1) " sorta odabrana" else " sorti odabrano"),
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Boje.TekstGlavni,
                        fontFamily = Poppins(), modifier = Modifier.weight(1f),
                    )
                    Text(
                        "$us sadnica · $ur reznica", fontSize = 12.sp, color = Boje.TekstSporedni,
                        fontFamily = Poppins(),
                    )
                }
                Button(
                    onClick = {
                        if (odabrane.isEmpty()) return@Button
                        val stavke = odabrane.map { StavkaRezervacije(it, sadnice[it] ?: 0, reznice[it] ?: 0) }
                        nav.otvori(Ekran.KontaktRezervacije(stavke))
                    },
                    enabled = n > 0,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Boje.Vino, contentColor = Color.White,
                        disabledContainerColor = Boje.TekstSvijetli, disabledContentColor = Color.White,
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                ) {
                    Text(
                        "Nastavi do kontakt podataka", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        fontFamily = Poppins(), maxLines = 1,
                    )
                    Icon(
                        painterResource(Res.drawable.ic_arrow_right), contentDescription = null,
                        modifier = Modifier.padding(start = 8.dp).size(18.dp),
                    )
                }
            }
        }
    }

    if (dijalogReset) {
        InfoDijalog(
            ikona = "🗑️",
            naslov = "Resetovati sve količine?",
            sadrzaj = "Sve unesene količine biće obrisane.",
            onZatvori = { dijalogReset = false },
            pozitivTekst = "Resetuj",
            onPozitiv = {
                sadnice = emptyMap()
                reznice = emptyMap()
                upit = ""
            },
            neutralTekst = "Otkaži",
        )
    }
}

/** item_rezervacija_sorta.xml */
@Composable
private fun RedRezervacije(
    s: Sorta, kolS: Int, kolR: Int, sOk: Boolean, rOk: Boolean,
    onS: (Int) -> Unit, onR: (Int) -> Unit,
) {
    val odabrana = kolS > 0 || kolR > 0
    val oblik = RoundedCornerShape(18.dp)
    Row(
        Modifier.padding(bottom = 6.dp).fillMaxWidth()
            .clip(oblik)
            .background(if (odabrana) Boje.VinoPastel else Boje.PovrsinaTopla)
            .border(if (odabrana) 1.dp else 0.5.dp, if (odabrana) Boje.VinoSvijetlo else Boje.ObrubMeki, oblik)
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SlikaSorteMreza(s.kljuc, s.slikaUrl, Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)))
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
        Column(horizontalAlignment = Alignment.End) {
            MiniBrojac("🌱", kolS, Boje.TipSadnica, sOk, onS)
            MiniBrojac("✂️", kolR, Boje.TipReznica, rOk, onR, Modifier.padding(top = 4.dp))
        }
    }
}

/** Mini stepper (bg_mini_stepper): ikona · − · [unos] · + */
@Composable
private fun MiniBrojac(
    ikona: String, kolicina: Int, boja: Color, dostupno: Boolean,
    onPromjena: (Int) -> Unit, modifier: Modifier = Modifier,
) {
    val prozirnost = if (dostupno) 1f else 0.35f
    Row(
        modifier.height(30.dp).clip(RoundedCornerShape(100.dp)).background(Boje.ChipPozadina),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(ikona, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp))
        DugmeBrojaca(Res.drawable.ic_minus, boja, dostupno, prozirnost) { onPromjena(kolicina - 1) }
        BasicTextField(
            value = if (kolicina > 0) kolicina.toString() else "",
            onValueChange = { t -> onPromjena(t.filter(Char::isDigit).take(3).toIntOrNull() ?: 0) },
            enabled = dostupno,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = boja,
                fontFamily = Poppins(), textAlign = TextAlign.Center,
            ),
            cursorBrush = SolidColor(boja),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            modifier = Modifier.width(34.dp).fillMaxHeight().alpha(prozirnost),
            decorationBox = { polje ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (kolicina <= 0) {
                        Text(
                            "0", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Boje.TekstSvijetli,
                            fontFamily = Poppins(), textAlign = TextAlign.Center,
                        )
                    }
                    polje()
                }
            },
        )
        DugmeBrojaca(Res.drawable.ic_plus, boja, dostupno, prozirnost, Modifier.padding(end = 2.dp)) {
            onPromjena(kolicina + 1)
        }
    }
}

@Composable
private fun DugmeBrojaca(
    ikona: DrawableResource, boja: Color, dostupno: Boolean, prozirnost: Float,
    modifier: Modifier = Modifier, onKlik: () -> Unit,
) {
    Icon(
        painterResource(ikona), contentDescription = null, tint = boja,
        modifier = modifier.size(28.dp).alpha(prozirnost).clip(RoundedCornerShape(100.dp))
            .clickable(enabled = dostupno, onClick = onKlik).padding(8.dp),
    )
}
