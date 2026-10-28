package com.vinograd.zivinice.ekrani

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vinograd.zivinice.Navigator
import com.vinograd.zivinice.platform.Datum
import com.vinograd.zivinice.platform.pomakZoneMs
import com.vinograd.zivinice.platform.sadaMs
import com.vinograd.zivinice.podaci.Biljeska
import com.vinograd.zivinice.podaci.Dnevnik
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.InfoDijalog
import com.vinograd.zivinice.ui.Poppins
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DAN_MS = 86_400_000L

/** Android Toast.LENGTH_SHORT = 2000 ms. */
private const val TRAJANJE_TOAST_MS = 2000L

private val KATEGORIJE = listOf(
    "Prskanje", "Rezidba", "Sadnja", "Vezivanje",
    "Berba", "Đubrenje", "Zalijevanje", "Ostalo",
)

/** Lokalni dan (od 1.1.1970.) za trenutak u ms. */
private fun lokalniDan(ms: Long): Long = (ms + pomakZoneMs()).floorDiv(DAN_MS)

/** "dd.MM.yyyy" po lokalnoj zoni telefona (kao SimpleDateFormat na Androidu). */
internal fun datumTekst(ms: Long): String {
    val d = Datum.izEpohaDana(lokalniDan(ms))
    return "${d.dan.toString().padStart(2, '0')}.${d.mjesec.toString().padStart(2, '0')}.${d.godina}"
}

/** Isti trenutak u danu, prebačen na drugi lokalni dan (kao Calendar.set(godina, mjesec, dan)). */
private fun naDan(ms: Long, noviDan: Long): Long = ms + (noviDan - lokalniDan(ms)) * DAN_MS

/** Dnevnik vinogradara — privatne bilješke (1:1 DnevnikActivity + activity_dnevnik.xml + item_biljeska.xml). */
@Composable
fun DnevnikEkran(nav: Navigator) {
    val lista by Dnevnik.biljeske.collectAsState(initial = null)
    val vrh = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val dno = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val gustina = LocalDensity.current
    var visinaDna by remember { mutableStateOf(0.dp) }
    val poruke = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // null = zatvoren, Pair(false, null) = nova, Pair(true, b) = uredi b
    var urediti by remember { mutableStateOf<Pair<Boolean, Biljeska?>?>(null) }
    var zaBrisanje by remember { mutableStateOf<Biljeska?>(null) }

    fun poruka(t: String) {
        scope.launch {
            poruke.currentSnackbarData?.dismiss()
            val tajmer = launch {
                delay(TRAJANJE_TOAST_MS)
                poruke.currentSnackbarData?.dismiss()
            }
            poruke.showSnackbar(t, duration = SnackbarDuration.Indefinite)
            tajmer.cancel()
        }
    }

    val l = lista ?: emptyList()

    Box(Modifier.fillMaxSize().background(Boje.Krem)) {
        Column(Modifier.fillMaxSize()) {

            // ===== HEADER =====
            Row(
                Modifier.fillMaxWidth().background(Boje.ZelenaTamna)
                    .padding(start = 16.dp, end = 16.dp, top = vrh + 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "←", color = Color.White, fontSize = 24.sp,
                    modifier = Modifier.clickable { nav.nazad() }.padding(end = 14.dp),
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        "MOJ DNEVNIK", color = Boje.Zlatna, fontSize = 11.sp,
                        letterSpacing = 0.1.em, fontFamily = Poppins(),
                    )
                    Text(
                        if (l.size == 1) "1 bilješka" else "${l.size} bilješki",
                        color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins(),
                    )
                }
            }

            // ===== LISTA / PRAZNO =====
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (lista != null && l.isEmpty()) {
                    Column(
                        Modifier.align(Alignment.Center).fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("📓", fontSize = 48.sp)
                        Text(
                            "Još nema bilješki", color = Boje.ZelenaTamna, fontSize = 17.sp,
                            fontWeight = FontWeight.Bold, fontFamily = Poppins(),
                            modifier = Modifier.padding(top = 10.dp),
                        )
                        Text(
                            "Dodajte prvu bilješku o radu u vinogradu", color = Boje.TekstSporedni,
                            fontSize = 13.sp, fontFamily = Poppins(), textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(14.dp)) {
                        items(l, key = { it.id }) { b ->
                            KarticaBiljeske(b, { urediti = Pair(true, b) }, { zaBrisanje = b })
                        }
                    }
                }
            }

            // ===== DUGME NOVA BILJEŠKA =====
            Box(
                Modifier.fillMaxWidth()
                    .onSizeChanged { visinaDna = with(gustina) { it.height.toDp() } }
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 14.dp + dno)
            ) {
                Button(
                    onClick = { urediti = Pair(false, null) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Boje.ZelenaTamna),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "+ Nova bilješka", color = Color.White, fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, fontFamily = Poppins(),
                    )
                }
            }
        }

        // ===== PORUKA (kao Toast, iznad dugmeta) =====
        SnackbarHost(poruke, Modifier.align(Alignment.BottomCenter).padding(bottom = visinaDna)) { d ->
            Snackbar(d, containerColor = Boje.ZelenaTamna, contentColor = Color.White, actionColor = Boje.ZlatnaSvijetla)
        }
    }

    // ===== DIJALOG NOVA / UREDI =====
    urediti?.let { (izmjena, postojeca) ->
        DijalogBiljeske(
            postojeca = if (izmjena) postojeca else null,
            onZatvori = { urediti = null },
            onSpasi = { datum, kategorija, tekst ->
                urediti = null
                scope.launch {
                    if (postojeca == null) {
                        Dnevnik.dodaj(Biljeska(sadaMs().toString(), datum, tekst, kategorija))
                        poruka("Bilješka dodana ✓")
                    } else {
                        Dnevnik.izmijeni(postojeca.copy(datum = datum, tekst = tekst, kategorija = kategorija))
                        poruka("Bilješka izmijenjena ✓")
                    }
                }
            },
        )
    }

    // ===== POTVRDA BRISANJA =====
    zaBrisanje?.let { b ->
        InfoDijalog(
            ikona = "🗑️",
            naslov = "Obrisati bilješku?",
            sadrzaj = b.tekst,
            onZatvori = { zaBrisanje = null },
            negativTekst = "Otkaži",
            pozitivTekst = "Obriši",
            onPozitiv = {
                scope.launch {
                    Dnevnik.obrisi(b.id)
                    poruka("Obrisano")
                }
            },
        )
    }
}

/** item_biljeska.xml */
@Composable
private fun KarticaBiljeske(b: Biljeska, onUredi: () -> Unit, onBrisi: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(16.dp))
            .background(Color.White).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                b.kategorija, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                fontFamily = Poppins(),
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFF4A7C5A))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            )
            Text(
                datumTekst(b.datum), color = Boje.TekstSporedni, fontSize = 13.sp, fontFamily = Poppins(),
                textAlign = TextAlign.End, modifier = Modifier.weight(1f),
            )
        }
        Text(
            b.tekst, color = Boje.ZelenaTamna, fontSize = 15.sp, fontFamily = Poppins(),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
            Text(
                "✏️ Uredi", color = Color(0xFF7E5BA1), fontSize = 13.sp, fontFamily = Poppins(),
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onUredi)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
            Text(
                "🗑️ Obriši", color = Color(0xFFC77777), fontSize = 13.sp, fontFamily = Poppins(),
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onBrisi)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

/**
 * Dijalog nova/uredi bilješku (dialog_biljeska.xml) u stilu InfoDijalog.
 * Prazan tekst: dijalog ostaje otvoren, poruka ispod polja (Android ga zatvara i gubi unos).
 * Dodir pored ne zatvara dijalog (iOS tastatura), dodir u tijelu zatvara tastaturu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DijalogBiljeske(
    postojeca: Biljeska?,
    onZatvori: () -> Unit,
    onSpasi: (datum: Long, kategorija: String, tekst: String) -> Unit,
) {
    var datum by remember { mutableStateOf(postojeca?.datum ?: sadaMs()) }
    var kategorija by remember {
        mutableStateOf(postojeca?.kategorija?.takeIf { it in KATEGORIJE } ?: KATEGORIJE[0])
    }
    var tekst by remember { mutableStateOf(postojeca?.tekst ?: "") }
    var greska by remember { mutableStateOf(false) }
    var meniOtvoren by remember { mutableStateOf(false) }
    var kalendar by remember { mutableStateOf(false) }
    val fokus = LocalFocusManager.current

    val stilPolja = Modifier.fillMaxWidth().padding(top = 4.dp).clip(RoundedCornerShape(12.dp))
        .background(Color(0xFFF2EDE0)).border(1.dp, Color(0xFFE0D8C5), RoundedCornerShape(12.dp))

    Dialog(
        onDismissRequest = onZatvori,
        properties = DialogProperties(dismissOnClickOutside = false, usePlatformDefaultWidth = false),
    ) {
        Column(
            Modifier.fillMaxWidth(0.92f).imePadding()
                .clip(RoundedCornerShape(20.dp)).background(Boje.Krem)
                .clickable(remember { MutableInteractionSource() }, null) { fokus.clearFocus() }
        ) {
            // HEADER zeleni
            Row(
                Modifier.fillMaxWidth().background(Boje.ZelenaTamna).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.padding(end = 10.dp).size(40.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center,
                ) { Text("📓", fontSize = 18.sp) }
                Text(
                    if (postojeca == null) "Nova bilješka" else "Uredi bilješku",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = Poppins(),
                )
            }

            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()).padding(20.dp)) {
                OznakaPolja("Datum")
                Text(
                    datumTekst(datum), color = Boje.ZelenaTamna, fontSize = 16.sp, fontFamily = Poppins(),
                    modifier = stilPolja.clickable { fokus.clearFocus(); kalendar = true }.padding(12.dp),
                )

                OznakaPolja("Kategorija", Modifier.padding(top = 14.dp))
                Box {
                    Row(
                        stilPolja.clickable { fokus.clearFocus(); meniOtvoren = true }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            kategorija, color = Boje.ZelenaTamna, fontSize = 16.sp, fontFamily = Poppins(),
                            modifier = Modifier.weight(1f),
                        )
                        Text("▾", color = Boje.TekstSporedni, fontSize = 16.sp)
                    }
                    DropdownMenu(meniOtvoren, { meniOtvoren = false }, Modifier.background(Boje.PovrsinaTopla)) {
                        KATEGORIJE.forEach { k ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        k, fontSize = 15.sp, fontFamily = Poppins(), color = Boje.ZelenaTamna,
                                        fontWeight = if (k == kategorija) FontWeight.Bold else FontWeight.Medium,
                                    )
                                },
                                onClick = { kategorija = k; meniOtvoren = false },
                            )
                        }
                    }
                }

                OznakaPolja("Bilješka", Modifier.padding(top = 14.dp))
                BasicTextField(
                    value = tekst,
                    onValueChange = { tekst = it; if (it.isNotBlank()) greska = false },
                    textStyle = TextStyle(color = Boje.ZelenaTamna, fontSize = 15.sp, fontFamily = Poppins()),
                    cursorBrush = SolidColor(Boje.ZelenaTamna),
                    minLines = 2,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = stilPolja.padding(12.dp),
                    decorationBox = { polje ->
                        Box {
                            if (tekst.isEmpty()) {
                                Text(
                                    "Npr. Prvo prskanje protiv plamenjače", color = Color(0xFF9CABA3),
                                    fontSize = 15.sp, fontFamily = Poppins(),
                                )
                            }
                            polje()
                        }
                    },
                )
                if (greska) {
                    Text(
                        "Unesite tekst bilješke", color = Color(0xFFC0392B), fontSize = 12.sp,
                        fontFamily = Poppins(), modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                    )
                }
            }

            // Dugmad
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
            ) {
                TextButton(
                    onClick = onZatvori,
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                ) {
                    Text("Otkaži", color = Boje.TekstSporedni, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins())
                }
                Button(
                    onClick = {
                        val t = tekst.trim()
                        if (t.isEmpty()) greska = true
                        else { fokus.clearFocus(); onSpasi(datum, kategorija, t) }
                    },
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Boje.ZelenaTamna),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    Text("Spasi", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins())
                }
            }
        }
    }

    // ===== KALENDAR (DatePickerDialog) =====
    if (kalendar) {
        val stanje = rememberDatePickerState(initialSelectedDateMillis = lokalniDan(datum) * DAN_MS)
        DatePickerDialog(
            onDismissRequest = { kalendar = false },
            confirmButton = {
                TextButton(onClick = {
                    stanje.selectedDateMillis?.let { datum = naDan(datum, it.floorDiv(DAN_MS)) }
                    kalendar = false
                }) { Text("U redu", color = Boje.ZelenaTamna, fontWeight = FontWeight.Bold, fontFamily = Poppins()) }
            },
            dismissButton = {
                TextButton(onClick = { kalendar = false }) {
                    Text("Otkaži", color = Boje.TekstSporedni, fontFamily = Poppins())
                }
            },
        ) {
            DatePicker(stanje, showModeToggle = false)
        }
    }
}

@Composable
private fun OznakaPolja(t: String, modifier: Modifier = Modifier) {
    Text(t, color = Boje.TekstSporedni, fontSize = 12.sp, letterSpacing = 0.05.em, fontFamily = Poppins(), modifier = modifier)
}
