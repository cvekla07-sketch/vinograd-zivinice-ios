package com.vinograd.zivinice.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.vinograd.zivinice.SlikeSorti
import org.jetbrains.compose.resources.painterResource

/** Tip stavke — isti stringovi kao Android ("sadnica" / "reznica"). */
const val TIP_SADNICA = "sadnica"
const val TIP_REZNICA = "reznica"

/** Boja bobice sorte — kao Android Sorte.Boja. */
enum class BojaBobice(val naziv: String) {
    BIJELA("Bijela"), ROZA("Roza / crvena"), CRNA("Crna / plava");

    val boja get() = when (this) {
        BIJELA -> Boje.BobicaBijela
        ROZA -> Boje.BobicaRoza
        CRNA -> Boje.BobicaCrna
    }
}

/** Slika sorte po ključu; ako je nema → ZamjenskaSlika (kao default_sadnica na Androidu). */
@Composable
fun SlikaSorte(kljuc: String, modifier: Modifier = Modifier) {
    val res = SlikeSorti.za(kljuc)
    if (res != null) {
        Image(
            painter = painterResource(res),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        ZamjenskaSlika(modifier)
    }
}

/** Krem pozadina sa ikonom grožđa (default_sadnica.xml). */
@Composable
fun ZamjenskaSlika(modifier: Modifier = Modifier) {
    Box(modifier.background(Boje.ZamjenskaPozadina), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(80.dp)) {
            val k = size.minDimension / 24f
            // peteljka
            val p = Path().apply {
                moveTo(12f * k, 2f * k); lineTo(8f * k, 6f * k); lineTo(10f * k, 6f * k)
                lineTo(10f * k, 9f * k); lineTo(14f * k, 9f * k); lineTo(14f * k, 6f * k)
                lineTo(16f * k, 6f * k); close()
            }
            drawPath(p, Boje.Zlatna, style = Fill)
            // bobice
            listOf(8f to 10.5f, 16f to 10.5f, 12f to 13.5f, 6f to 16.5f, 18f to 16.5f, 12f to 19.5f)
                .forEach { (x, y) -> drawCircle(Boje.ZelenaSvijetla, 2f * k, Offset(x * k, y * k)) }
        }
    }
}

/** Pill "Sadnica" / "Reznica" (pill_sadnica.xml / pill_reznica.xml). */
@Composable
fun TipPill(tip: String, modifier: Modifier = Modifier) {
    val sadnica = tip == TIP_SADNICA
    Text(
        text = if (sadnica) "Sadnica" else "Reznica",
        color = if (sadnica) Boje.TipSadnica else Boje.TipReznica,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = Poppins(),
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(if (sadnica) Boje.TipSadnicaPozadina else Boje.TipReznicaPozadina)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

/** Tačkica boje bobice na kartici sorte. */
@Composable
fun TackaBobice(boja: BojaBobice, modifier: Modifier = Modifier) {
    Box(modifier.size(10.dp).clip(CircleShape).background(boja.boja))
}

/** Bordo pill (bg_pill_wine.xml) — za cijene / oznake. */
@Composable
fun VinoPill(tekst: String, modifier: Modifier = Modifier) {
    Text(
        text = tekst,
        color = Boje.Bijela,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = Poppins(),
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Boje.Vino)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
