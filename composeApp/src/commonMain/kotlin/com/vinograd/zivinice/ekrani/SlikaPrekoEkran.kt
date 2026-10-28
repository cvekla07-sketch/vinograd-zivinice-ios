package com.vinograd.zivinice.ekrani

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinograd.zivinice.Navigator
import com.vinograd.zivinice.podaci.Sorte
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.Poppins
import com.vinograd.zivinice.ui.SlikaSorteMreza

private const val MIN_ZUM = 1f
private const val MAX_ZUM = 5f
private const val ZUM_DVOSTRUKI = 2.5f

/** Slika preko cijelog ekrana sa zumom prstima (1:1 FullscreenSlikaActivity, umjesto PhotoView). */
@Composable
fun SlikaPrekoEkran(naziv: String, nav: Navigator) {
    val sve by Sorte.sve.collectAsState()
    val sorta = remember(sve, naziv) { Sorte.poNazivu(naziv) }
    val kljuc = sorta?.kljuc ?: Sorte.kljucOd(naziv)

    val vrh = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val dno = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var skala by remember { mutableFloatStateOf(1f) }
    var pomak by remember { mutableStateOf(Offset.Zero) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {

        // Slika + zum / pomjeranje / dvostruki dodir
        Box(
            Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val nova = (skala * zoom).coerceIn(MIN_ZUM, MAX_ZUM)
                        val maxX = size.width * (nova - 1f) / 2f
                        val maxY = size.height * (nova - 1f) / 2f
                        val p = pomak + pan
                        skala = nova
                        pomak = if (nova <= MIN_ZUM) Offset.Zero
                        else Offset(p.x.coerceIn(-maxX, maxX), p.y.coerceIn(-maxY, maxY))
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = {
                        if (skala > MIN_ZUM) {
                            skala = MIN_ZUM
                            pomak = Offset.Zero
                        } else {
                            skala = ZUM_DVOSTRUKI
                        }
                    })
                }
                .graphicsLayer {
                    scaleX = skala
                    scaleY = skala
                    translationX = pomak.x
                    translationY = pomak.y
                }
        ) {
            SlikaSorteMreza(kljuc, sorta?.slikaUrl, Modifier.fillMaxSize(), ContentScale.Fit)
        }

        // Zatvori (×)
        Box(
            Modifier.align(Alignment.TopEnd).padding(top = vrh + 36.dp, end = 16.dp).size(44.dp)
                .clip(RoundedCornerShape(100.dp)).background(Color(0xE6FFFFFF))
                .clickable { nav.nazad() },
            contentAlignment = Alignment.Center,
        ) {
            Text("×", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Boje.ZelenaTamna)
        }

        // Naslov
        if (naziv.isNotEmpty()) {
            Text(
                naziv, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White,
                fontFamily = Poppins(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = vrh + 42.dp)
                    .background(Color(0x80000000)).padding(horizontal = 14.dp, vertical = 6.dp),
            )
        }

        // Uputa
        Text(
            "Spojite prste za zumiranje", fontSize = 11.sp, color = Color(0xCCFFFFFF),
            fontFamily = Poppins(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = dno + 32.dp)
                .background(Color(0x80000000)).padding(horizontal = 14.dp, vertical = 6.dp),
        )
    }
}
