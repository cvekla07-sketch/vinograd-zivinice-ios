package com.vinograd.zivinice.ekrani

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vinograd.zivinice.Navigator
import com.vinograd.zivinice.platform.otvoriLinkJavi
import com.vinograd.zivinice.resources.Res
import com.vinograd.zivinice.resources.ic_arrow_right
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.Poppins
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

private const val TELEFON = "+387644135349"
private const val TELEFON_PRIKAZ = "+387 64 413 5349"
private const val FB_URL = "https://www.facebook.com/Vinograd.Zivinice/"
private const val YT_URL = "https://www.youtube.com/@Vinograd_Zivinice"

/** Android Toast.LENGTH_SHORT = 2000 ms. */
private const val TRAJANJE_TOAST_MS = 2000L

private val OBRUB = Color(0xFFE8DCC4)

/** Kontakt (1:1 KontaktActivity + activity_kontakt.xml). */
@Composable
fun KontaktEkran(nav: Navigator) {
    val vrh = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val dno = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val poruke = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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

    fun otvori(url: String, greska: String) {
        otvoriLinkJavi(url) { uspjeh -> if (!uspjeh) poruka(greska) }
    }

    val pozovi = { otvori("tel:$TELEFON", "Poziv nije moguć") }

    Box(Modifier.fillMaxSize().background(Boje.Krem)) {
        Column(Modifier.fillMaxSize()) {

            // ===== HEADER =====
            Row(
                Modifier.fillMaxWidth().background(Boje.ZelenaTamna)
                    .padding(start = 16.dp, end = 16.dp, top = vrh + 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).clickable { nav.nazad() },
                    contentAlignment = Alignment.Center,
                ) { Text("←", color = Color.White, fontSize = 20.sp) }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Kontakt", fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        color = Color.White, fontFamily = Poppins(),
                    )
                    Text(
                        "Vinograd Živinice", fontSize = 11.sp, color = Color.White, fontFamily = Poppins(),
                        modifier = Modifier.padding(top = 2.dp).alpha(0.85f),
                    )
                }
                Spacer(Modifier.size(40.dp))
            }

            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 14.dp + dno)
            ) {
                // ===== GLAVNA KARTICA =====
                val oblikGl = RoundedCornerShape(20.dp)
                Column(
                    Modifier.fillMaxWidth().shadow(3.dp, oblikGl).clip(oblikGl).background(Color.White)
                        .border(0.5.dp, OBRUB, oblikGl).padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier.padding(bottom = 14.dp).size(80.dp).clip(CircleShape).background(Boje.ZelenaPastel),
                        contentAlignment = Alignment.Center,
                    ) { Text("🍇", fontSize = 40.sp) }
                    Text(
                        "Vinograd Živinice", color = Boje.ZelenaTamna, fontSize = 22.sp,
                        fontWeight = FontWeight.Bold, fontFamily = Poppins(),
                    )
                    Text(
                        "📍 Živinice, BiH", color = Boje.TekstSporedni, fontSize = 12.sp, fontFamily = Poppins(),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Box(Modifier.padding(vertical = 14.dp).width(60.dp).height(2.dp).background(Boje.Zlatna))
                    Text(
                        "Za više informacija o sortama,\nrezervacijama i savjetima",
                        color = Color(0xFF3D4A40), fontSize = 13.sp, lineHeight = 20.sp,
                        textAlign = TextAlign.Center, fontFamily = Poppins(),
                    )
                    Text(
                        TELEFON_PRIKAZ, color = Boje.ZelenaTamna, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        fontFamily = Poppins(),
                        modifier = Modifier.padding(top = 14.dp).clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = pozovi).padding(10.dp),
                    )
                    Text("Klikni za poziv", color = Boje.TekstSvijetli, fontSize = 10.sp, fontFamily = Poppins())
                }

                NaslovGrupe("DIREKTNI KONTAKT")
                KarticaKontakta("📞", Boje.ZelenaPastel, "Pozovi", TELEFON_PRIKAZ, 8.dp, pozovi)
                KarticaKontakta("💬", Boje.ZelenaPastel, "WhatsApp", "Brži odgovor preko poruke", 8.dp) {
                    otvori("https://wa.me/${TELEFON.removePrefix("+")}", "WhatsApp nije instaliran")
                }
                KarticaKontakta("📱", Boje.LjubicastaPastel, "Viber", "Pošalji poruku preko Viber-a", 8.dp) {
                    otvori("viber://chat?number=$TELEFON", "Viber nije instaliran")
                }

                NaslovGrupe("DRUŠTVENE MREŽE")
                KarticaKontakta("📘", Boje.PlavaPastel, "Facebook", "Pratite naše novosti", 8.dp) {
                    otvori(FB_URL, "Ne mogu otvoriti link")
                }
                KarticaKontakta("▶️", Boje.KoralPastel, "YouTube", "Gledajte video savjete", 14.dp) {
                    otvori(YT_URL, "Ne mogu otvoriti link")
                }

                Text(
                    "🍇 Vinograd Živinice · 2026", color = Boje.TekstSvijetli, fontSize = 11.sp,
                    textAlign = TextAlign.Center, fontFamily = Poppins(),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                )
            }
        }

        // ===== PORUKA (kao Toast) =====
        SnackbarHost(poruke, Modifier.align(Alignment.BottomCenter).padding(bottom = dno)) { d ->
            Snackbar(d, containerColor = Boje.ZelenaTamna, contentColor = Color.White, actionColor = Boje.ZlatnaSvijetla)
        }
    }
}

@Composable
private fun NaslovGrupe(t: String) {
    Text(
        t, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Boje.TekstSporedni,
        letterSpacing = 0.15.em, fontFamily = Poppins(),
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp, start = 4.dp),
    )
}

/** Akciona kartica (MaterialCardView, uglovi 14, krug 44 dp sa emojijem, strelica desno). */
@Composable
private fun KarticaKontakta(
    emoji: String, pozadinaKruga: Color, naslov: String, opis: String,
    razmakDolje: Dp, onKlik: () -> Unit,
) {
    val oblik = RoundedCornerShape(14.dp)
    Row(
        Modifier.padding(bottom = razmakDolje).fillMaxWidth().shadow(2.dp, oblik).clip(oblik)
            .background(Color.White).border(0.5.dp, OBRUB, oblik)
            .clickable(onClick = onKlik).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.padding(end = 12.dp).size(44.dp).clip(CircleShape).background(pozadinaKruga),
            contentAlignment = Alignment.Center,
        ) { Text(emoji, fontSize = 20.sp) }
        Column(Modifier.weight(1f)) {
            Text(naslov, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Boje.ZelenaTamna, fontFamily = Poppins())
            Text(opis, fontSize = 11.sp, color = Boje.TekstSporedni, fontFamily = Poppins())
        }
        Icon(painterResource(Res.drawable.ic_arrow_right), null, Modifier.size(18.dp), tint = Boje.TekstSporedni)
    }
}
