package com.vinograd.zivinice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinograd.zivinice.ui.BojaBobice
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.Poppins
import com.vinograd.zivinice.ui.SlikaSorte
import com.vinograd.zivinice.ui.TIP_REZNICA
import com.vinograd.zivinice.ui.TIP_SADNICA
import com.vinograd.zivinice.ui.TackaBobice
import com.vinograd.zivinice.ui.TipPill
import com.vinograd.zivinice.ui.VinoPill
import com.vinograd.zivinice.ui.VinogradTema

/** Privremeni ekran (Dio 2) — provjera teme, fonta, slika i komponenti. Pravi ekrani dolaze u dijelu 4. */
@Composable
fun App() {
    VinogradTema {
        Column(
            modifier = Modifier.fillMaxSize().background(Boje.Krem).systemBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Vinograd Živinice", fontFamily = Poppins(), fontSize = 24.sp, color = Boje.ZelenaTamna)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SlikaSorte("arkadia", Modifier.size(140.dp).clip(RoundedCornerShape(20.dp)))
                SlikaSorte("andelika", Modifier.size(140.dp).clip(RoundedCornerShape(20.dp)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TackaBobice(BojaBobice.BIJELA)
                TackaBobice(BojaBobice.ROZA)
                TackaBobice(BojaBobice.CRNA)
                TipPill(TIP_SADNICA)
                TipPill(TIP_REZNICA)
                VinoPill("15,00 KM")
            }
        }
    }
}
