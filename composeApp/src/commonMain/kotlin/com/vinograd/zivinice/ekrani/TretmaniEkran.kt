package com.vinograd.zivinice.ekrani

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vinograd.zivinice.Ekran
import com.vinograd.zivinice.Navigator
import com.vinograd.zivinice.platform.Datum
import com.vinograd.zivinice.podaci.Tretman
import com.vinograd.zivinice.podaci.Tretmani
import com.vinograd.zivinice.resources.Res
import com.vinograd.zivinice.resources.ic_arrow_back
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.DugmeKrug
import com.vinograd.zivinice.ui.Poppins

/** Nazivi mjeseci — isti redoslijed kao Tretmani.MJESECI_KEY (TretmaniActivity.MJESECI_BS). */
internal val MJESECI_BS = listOf(
    "Januar", "Februar", "Mart", "April", "Maj", "Juni",
    "Juli", "August", "Septembar", "Oktobar", "Novembar", "Decembar",
)

/** Zeleni header sa dugmetom nazad (bg_katalog_header, padding 16/10+statusBars/16/18). */
@Composable
internal fun ZaglavljeEkrana(naslov: String, podnaslov: String, nav: Navigator) {
    val vrh = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Row(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Boje.ZelenaSrednja, Boje.ZelenaTamna)))
            .padding(start = 16.dp, end = 16.dp, top = vrh + 10.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DugmeKrug(Res.drawable.ic_arrow_back, Color(0x26FFFFFF), { nav.nazad() })
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(naslov, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = Poppins())
            Text(
                podnaslov, fontSize = 12.sp, color = Color.White, fontFamily = Poppins(),
                modifier = Modifier.alpha(0.8f),
            )
        }
    }
}

/** Žuta kartica upozorenja (bg_warn_card). */
@Composable
private fun UpozorenjeAgronom() {
    val oblik = RoundedCornerShape(16.dp)
    Row(
        Modifier.fillMaxWidth().clip(oblik).background(Color(0xFFFFF7E0))
            .border(0.5.dp, Boje.Zlatna, oblik).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("⚠️", fontSize = 18.sp)
        Text(
            "Konsultujte agronoma prije primjene zaštitnih sredstava.",
            fontSize = 12.sp, color = Boje.TekstGlavni, fontFamily = Poppins(),
            modifier = Modifier.weight(1f).padding(start = 10.dp),
        )
    }
}

/** Plan zaštite — 12 mjeseci u 2 kolone, trenutni istaknut (1:1 TretmaniActivity + activity_tretmani.xml). */
@Composable
fun TretmaniEkran(nav: Navigator) {
    val dno = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    var popunjeni by remember { mutableStateOf<Set<String>>(emptySet()) }
    val trenutni = remember { Datum.danas().mjesec - 1 } // 0..11

    LaunchedEffect(Unit) { popunjeni = Tretmani.popunjeniMjeseci() }

    Column(Modifier.fillMaxSize().background(Boje.Krem)) {
        ZaglavljeEkrana("Plan zaštite", "Mjesečni tretmani vinove loze · ${Tretmani.GODINA}", nav)

        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 14.dp + dno)
        ) {
            UpozorenjeAgronom()
            Text(
                "ODABERITE MJESEC", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = Boje.TekstSvijetli, fontFamily = Poppins(), letterSpacing = 0.12.em,
                modifier = Modifier.padding(top = 18.dp, start = 4.dp),
            )
            Column(Modifier.padding(top = 6.dp)) {
                for (red in 0 until 6) {
                    Row(Modifier.fillMaxWidth()) {
                        for (kol in 0 until 2) {
                            val i = red * 2 + kol
                            val kljuc = Tretmani.MJESECI_KEY[i]
                            KarticaMjeseca(
                                broj = i + 1,
                                naziv = MJESECI_BS[i],
                                popunjen = kljuc in popunjeni,
                                trenutni = i == trenutni,
                                modifier = Modifier.weight(1f),
                            ) { nav.otvori(Ekran.DetaljiTretmana(kljuc)) }
                        }
                    }
                }
            }
        }
    }
}

/** item_tretman_mjesec.xml (+ bg_card_current_month za trenutni mjesec). */
@Composable
private fun KarticaMjeseca(
    broj: Int, naziv: String, popunjen: Boolean, trenutni: Boolean,
    modifier: Modifier, onKlik: () -> Unit,
) {
    val oblik = RoundedCornerShape(18.dp)
    Row(
        modifier.padding(4.dp).clip(oblik).background(Boje.PovrsinaTopla)
            .border(if (trenutni) 1.5.dp else 0.5.dp, if (trenutni) Boje.Vino else Boje.ObrubMeki, oblik)
            .clickable(onClick = onKlik).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape)
                .background(if (trenutni) Boje.VinoPastel else Boje.ZelenaPastel),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                broj.toString(), fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins(),
                color = if (trenutni) Boje.VinoTamno else Boje.ZelenaTamna,
            )
        }
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text(
                naziv, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Boje.TekstGlavni,
                fontFamily = Poppins(), maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Text(
                if (popunjen) "✓ Plan dostupan" else "Nije objavljen",
                fontSize = 10.5.sp, fontFamily = Poppins(), maxLines = 1, overflow = TextOverflow.Ellipsis,
                color = if (popunjen) Boje.ZelenaSvijetla else Boje.TekstSvijetli,
            )
        }
    }
}

/** Tretman za jedan mjesec (1:1 DetaljiTretmanaActivity + activity_detalji_tretmana.xml). */
@Composable
fun DetaljiTretmanaEkran(mjesecKey: String, nav: Navigator) {
    val dno = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val idx = Tretmani.MJESECI_KEY.indexOf(mjesecKey)
    val naziv = if (idx >= 0) MJESECI_BS[idx] else ""

    var ucitava by remember { mutableStateOf(true) }
    var tretman by remember { mutableStateOf<Tretman?>(null) }

    LaunchedEffect(mjesecKey) {
        ucitava = true
        tretman = Tretmani.tretman(mjesecKey)
        ucitava = false
    }

    Column(Modifier.fillMaxSize().background(Boje.Krem)) {
        ZaglavljeEkrana(naziv, "Plan zaštite · ${Tretmani.GODINA}", nav)

        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 14.dp + dno)
        ) {
            UpozorenjeAgronom()
            val t = tretman
            when {
                ucitava -> Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Boje.ZelenaTamna)
                }
                t == null || !t.jePopunjen() -> Column(
                    Modifier.fillMaxWidth().padding(top = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("📭", fontSize = 44.sp)
                    Text(
                        "Plan još nije objavljen", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = Boje.TekstGlavni, fontFamily = Poppins(), modifier = Modifier.padding(top = 10.dp),
                    )
                    Text(
                        "Plan zaštite za ovaj mjesec biće objavljen uskoro", fontSize = 13.sp,
                        color = Boje.TekstSporedni, fontFamily = Poppins(), textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                else -> {
                    val oblik = RoundedCornerShape(18.dp)
                    Box(
                        Modifier.padding(top = 14.dp).fillMaxWidth().clip(oblik)
                            .background(Boje.PovrsinaTopla).border(0.5.dp, Boje.ObrubMeki, oblik)
                    ) {
                        Text(
                            t.tekst ?: "", fontSize = 14.sp, lineHeight = 26.sp, color = Color(0xFF3D4A40),
                            fontFamily = Poppins(), modifier = Modifier.fillMaxWidth().padding(18.dp),
                        )
                    }
                    if (t.vrijemeAzuriranja > 0) {
                        Text(
                            "Ažurirano: ${datumTekst(t.vrijemeAzuriranja)}.", fontSize = 11.sp,
                            color = Boje.TekstSvijetli, fontFamily = Poppins(),
                            modifier = Modifier.padding(top = 10.dp, start = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
