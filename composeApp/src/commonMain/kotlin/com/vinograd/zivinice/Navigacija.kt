package com.vinograd.zivinice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinograd.zivinice.podaci.StavkaRezervacije
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.Poppins

/** Svi ekrani aplikacije (zamjena za Activity klase). */
sealed class Ekran {
    data object Home : Ekran()
    data object Katalog : Ekran()                                  // SadnicaActivity
    data class Detalji(val naziv: String) : Ekran()                // DetaljiSadniceActivity
    data class SlikaPreko(val naziv: String) : Ekran()             // FullscreenSlikaActivity
    data object Korpa : Ekran()
    data object Narudzba : Ekran()
    data object Cjenovnik : Ekran()
    data object Rezervacija : Ekran()
    data class KontaktRezervacije(val stavke: List<StavkaRezervacije>) : Ekran()  // stavke umjesto Intent nizova
    data object Tretmani : Ekran()
    data class DetaljiTretmana(val mjesecKey: String) : Ekran()
    data object Vodic : Ekran()
    data object Dnevnik : Ekran()
    data object Kontakt : Ekran()
    data object Novosti : Ekran()                                   // Novo iz vinograda
    data class JednaObjava(val id: String) : Ekran()
    data class Kalkulator(val sorta: String? = null) : Ekran()     // kalkulator sadnica
}

/** Stack ekrana — "nazad" skida zadnji (kao back dugme na Androidu). */
class Navigator {
    val stack = mutableStateListOf<Ekran>(Ekran.Home)
    val trenutni: Ekran get() = stack.last()

    fun otvori(e: Ekran) {
        stack.add(e)
    }

    /** Vraća false ako smo već na početnoj. */
    fun nazad(): Boolean {
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        return true
    }

    /** Kao FLAG_ACTIVITY_CLEAR_TOP na MainActivity. */
    fun naPocetnu() {
        while (stack.size > 1) stack.removeAt(stack.lastIndex)
    }
}

/** Privremeno za ekrane koji još nisu urađeni (Dio 4b…). */
@Composable
fun UIzradi(naslov: String, nav: Navigator) {
    Column(
        Modifier.fillMaxSize().background(Boje.Krem).systemBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(naslov, fontFamily = Poppins(), fontSize = 20.sp, color = Boje.ZelenaTamna)
        Text("U izradi", color = Boje.TekstSporedni)
        Button(
            onClick = { nav.nazad() },
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Boje.ZelenaTamna),
        ) { Text("← Nazad", fontFamily = Poppins()) }
    }
}
