package com.vinograd.zivinice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinograd.zivinice.podaci.Cjenovnik
import com.vinograd.zivinice.podaci.Sezona
import com.vinograd.zivinice.podaci.Sorte
import com.vinograd.zivinice.podaci.Tretmani
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.Poppins
import com.vinograd.zivinice.ui.SlikaSorte
import com.vinograd.zivinice.ui.VinoPill
import com.vinograd.zivinice.ui.VinogradTema

/** Privremeni ekran (Dio 3) — provjera da REST čita bazu. Pravi ekrani dolaze u dijelu 4. */
@Composable
fun App() {
    val sorte by Sorte.sve.collectAsState()
    val izFirebase by Sorte.izFirebase.collectAsState()
    val cijene by Cjenovnik.mapa.collectAsState()
    val cjenovnikUcitan by Cjenovnik.ucitan.collectAsState()
    val sezona by Sezona.stanje.collectAsState()
    var tretmani by remember { mutableStateOf<Set<String>?>(null) }

    LaunchedEffect(Unit) {
        Sorte.osvjezi()
        Cjenovnik.osvjezi()
        Sezona.osvjezi()
        tretmani = Tretmani.popunjeniMjeseci()
    }

    VinogradTema {
        Column(
            modifier = Modifier.fillMaxSize().background(Boje.Krem).systemBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Vinograd Živinice", fontFamily = Poppins(), fontSize = 24.sp, color = Boje.ZelenaTamna)
            SlikaSorte("arkadia", Modifier.size(120.dp).clip(RoundedCornerShape(20.dp)))
            Text("Sorte: ${sorte.size} (${if (izFirebase) "iz baze" else "ugrađene"})", color = Boje.TekstGlavni)
            Text("Cjenovnik: ${if (cjenovnikUcitan) "${cijene.size} cijena" else "nije učitan"}", color = Boje.TekstGlavni)
            Text("Sezona: ${sezona.status}${sezona.poruka?.let { " — $it" } ?: ""}", color = Boje.TekstGlavni)
            Text("Tretmani ${Tretmani.GODINA}: ${tretmani?.size ?: "…"} mjeseci popunjeno", color = Boje.TekstGlavni)
            VinoPill(Cjenovnik.prikazObje("Arkadia").replace("\n", " · "))
        }
    }
}
