package com.vinograd.zivinice.ekrani

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vinograd.zivinice.Ekran
import com.vinograd.zivinice.Navigator
import com.vinograd.zivinice.resources.Res
import com.vinograd.zivinice.resources.ic_arrow_right
import com.vinograd.zivinice.resources.ic_book
import com.vinograd.zivinice.resources.ic_grape
import com.vinograd.zivinice.resources.ic_leaf
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.Poppins
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Vinogradarski vodič (1:1 VodicActivity + activity_vodic.xml). */
@Composable
fun VodicEkran(nav: Navigator) {
    val dno = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(Modifier.fillMaxSize().background(Boje.Krem)) {
        ZaglavljeEkrana("Vinogradarski vodič", "Dnevnik, savjeti i plan zaštite", nav)

        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 14.dp + dno)
        ) {
            // ===== MOJ DNEVNIK (bg_hero_wine, 315°) =====
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(Boje.VinoTamno, Boje.VinoSvijetlo), Offset.Zero, Offset.Infinite))
                    .clickable { nav.otvori(Ekran.Dnevnik) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(Color(0x26FFFFFF)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(Res.drawable.ic_book), "Dnevnik", Modifier.size(24.dp), tint = Color.White)
                }
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(
                        "MOJ DNEVNIK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White,
                        fontFamily = Poppins(), letterSpacing = 0.15.em, modifier = Modifier.alpha(0.85f),
                    )
                    Text(
                        "Bilješke o radu u vinogradu", fontSize = 15.sp, fontWeight = FontWeight.Bold,
                        color = Color.White, fontFamily = Poppins(),
                    )
                    Text(
                        "Privatno, čuva se samo na vašem telefonu", fontSize = 11.sp, color = Color.White,
                        fontFamily = Poppins(), modifier = Modifier.alpha(0.85f),
                    )
                }
                Icon(painterResource(Res.drawable.ic_arrow_right), "Otvori", Modifier.size(18.dp), tint = Color.White)
            }

            Text(
                "SAVJETI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Boje.TekstSvijetli,
                fontFamily = Poppins(), letterSpacing = 0.12.em,
                modifier = Modifier.padding(top = 18.dp, start = 4.dp),
            )

            KarticaVodica(
                Res.drawable.ic_leaf, Boje.PlavaPastel, Boje.PlavaSrednja,
                "Plan zaštite po mjesecima", "Tretmani vinove loze kroz godinu",
                Modifier.padding(top = 6.dp),
            ) { nav.otvori(Ekran.Tretmani) }

            KarticaVodica(
                Res.drawable.ic_grape, Boje.NarandzastaPastel, Boje.NarandzastaSrednja,
                "Kako saditi i rezati lozu", "Uskoro",
                Modifier.padding(top = 8.dp).alpha(0.6f),
                onKlik = null,
            )
        }
    }
}

/** Kartica u vodiču (bg_quick_card, ikona u pastelnom krugu 44 dp). onKlik == null → bez strelice i klika. */
@Composable
private fun KarticaVodica(
    ikona: DrawableResource, pozadinaIkone: Color, bojaIkone: Color,
    naslov: String, opis: String, modifier: Modifier,
    onKlik: (() -> Unit)?,
) {
    val oblik = RoundedCornerShape(18.dp)
    var m = modifier.fillMaxWidth().clip(oblik).background(Boje.PovrsinaTopla).border(0.5.dp, Boje.ObrubMeki, oblik)
    if (onKlik != null) m = m.clickable(onClick = onKlik)
    Row(m.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(pozadinaIkone),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(ikona), naslov, Modifier.size(22.dp), tint = bojaIkone)
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(naslov, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Boje.TekstGlavni, fontFamily = Poppins())
            Text(opis, fontSize = 12.sp, color = Boje.TekstSporedni, fontFamily = Poppins())
        }
        if (onKlik != null) {
            Icon(painterResource(Res.drawable.ic_arrow_right), "Otvori", Modifier.size(18.dp), tint = Boje.TekstSvijetli)
        }
    }
}
