package com.vinograd.zivinice.ekrani

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.vinograd.zivinice.Ekran
import com.vinograd.zivinice.Navigator
import com.vinograd.zivinice.platform.otvoriLink
import com.vinograd.zivinice.podaci.JednomNaEkranu
import com.vinograd.zivinice.podaci.Objava
import com.vinograd.zivinice.podaci.Objave
import com.vinograd.zivinice.podaci.Sorte
import com.vinograd.zivinice.podaci.Stat
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.Poppins

/** Lista objava "Novo iz vinograda" (najnovija gore). */
@Composable
fun NovostiEkran(nav: Navigator) {
    val lista by Objave.lista.collectAsState()
    val procitane by Objave.procitane.collectAsState()
    val dno = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(Modifier.fillMaxSize().background(Boje.Krem)) {
        ZaglavljeEkrana("Novo iz vinograda", "Savjeti, novosti i video iz vinograda", nav)
        if (lista.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Uskoro prve objave 🍇", fontSize = 15.sp, color = Boje.TekstSporedni,
                    fontFamily = Poppins(), textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 16.dp + dno),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(lista, key = { it.id }) { o ->
                    KarticaObjave(o, Objave.jeNova(o, procitane)) { nav.otvori(Ekran.JednaObjava(o.id)) }
                }
            }
        }
    }
}

@Composable
private fun KarticaObjave(o: Objava, nova: Boolean, onKlik: () -> Unit) {
    val oblik = RoundedCornerShape(18.dp)
    Column(
        Modifier.fillMaxWidth().clip(oblik).background(Boje.PovrsinaTopla)
            .border(if (nova) 1.5.dp else 0.5.dp, if (nova) Boje.Zlatna else Boje.ObrubMeki, oblik)
            .clickable(onClick = onKlik)
    ) {
        if (o.imaSliku) SlikaObjave(o, 170.dp)
        Column(Modifier.padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (nova) {
                    NovoPill()
                    Spacer(Modifier.width(8.dp))
                }
                Text(datumTekst(o.vrijeme), fontSize = 11.sp, color = Boje.TekstSvijetli, fontFamily = Poppins())
                if (o.video != null) {
                    Spacer(Modifier.weight(1f))
                    Text("▶ video", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Boje.Vino, fontFamily = Poppins())
                }
            }
            Text(
                o.naslov, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Boje.TekstGlavni,
                fontFamily = Poppins(), modifier = Modifier.padding(top = 6.dp),
            )
            if (o.tekst.isNotEmpty()) {
                Text(
                    o.tekst, fontSize = 13.sp, lineHeight = 19.sp, color = Boje.TekstSporedni, fontFamily = Poppins(),
                    maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

/** Jedna objava — cijeli tekst, slika, video i sorta. */
@Composable
fun ObjavaEkran(id: String, nav: Navigator) {
    val lista by Objave.lista.collectAsState()
    val sorte by Sorte.sve.collectAsState()
    val o = lista.firstOrNull { it.id == id }
    val dno = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    if (o != null) JednomNaEkranu { Objave.otvorena(o.id) }

    Column(Modifier.fillMaxSize().background(Boje.Krem)) {
        ZaglavljeEkrana("Novo iz vinograda", o?.let { datumTekst(it.vrijeme) } ?: "", nav)
        if (o == null) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Objava se učitava ili više ne postoji.", fontSize = 15.sp, color = Boje.TekstSporedni,
                    fontFamily = Poppins(), textAlign = TextAlign.Center,
                )
            }
            return@Column
        }
        val sorta = o.sorta?.let { k -> sorte.firstOrNull { it.kljuc == k } }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 16.dp + dno)) {
            if (o.imaSliku) SlikaObjave(o, null)
            Column(Modifier.padding(16.dp)) {
                Text(o.naslov, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Boje.TekstGlavni, fontFamily = Poppins())
                if (o.tekst.isNotEmpty()) {
                    Text(
                        o.tekst, fontSize = 15.sp, lineHeight = 23.sp, color = Boje.TekstGlavni,
                        fontFamily = Poppins(), modifier = Modifier.padding(top = 10.dp),
                    )
                }
                Spacer(Modifier.height(6.dp))
                o.video?.let { url ->
                    DugmeObjave("▶  Pogledaj video", puno = true) {
                        Stat.objava(o.id, Stat.OBJAVA_VIDEO)
                        otvoriLink(url)
                    }
                }
                if (sorta != null) {
                    DugmeObjave("🍇  Pogledaj sortu ${sorta.naziv}", puno = false) {
                        nav.otvori(Ekran.Detalji(sorta.naziv))
                    }
                }
            }
        }
    }
}

/** Slika objave: sa visinom → izrezana (lista); bez visine → cijela širina (detalj). */
@Composable
private fun SlikaObjave(o: Objava, visina: Dp?) {
    val slika by produceState<String?>(null, o.id) { value = Objave.slika(o.id) }
    if (slika == null) {
        Box(Modifier.fillMaxWidth().height(visina ?: 220.dp).background(Boje.ZamjenskaPozadina))
    } else {
        AsyncImage(
            model = slika, contentDescription = null,
            contentScale = if (visina != null) ContentScale.Crop else ContentScale.FillWidth,
            modifier = if (visina != null) Modifier.fillMaxWidth().height(visina) else Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DugmeObjave(tekst: String, puno: Boolean, onKlik: () -> Unit) {
    val oblik = RoundedCornerShape(100.dp)
    Box(
        Modifier.fillMaxWidth().padding(top = 12.dp).clip(oblik)
            .background(if (puno) Boje.ZelenaTamna else Boje.PovrsinaTopla)
            .border(1.dp, Boje.ZelenaTamna, oblik)
            .clickable(onClick = onKlik)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            tekst, fontSize = 14.sp, fontWeight = FontWeight.Bold,
            color = if (puno) Color.White else Boje.ZelenaTamna, fontFamily = Poppins(),
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Oznaka "NOVO" (lista objava i traka na Home). */
@Composable
internal fun NovoPill() {
    Text(
        "NOVO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White,
        fontFamily = Poppins(), letterSpacing = 1.sp,
        modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(Boje.Vino)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}
