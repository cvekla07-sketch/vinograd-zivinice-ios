package com.vinograd.zivinice.ekrani

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vinograd.zivinice.Ekran
import com.vinograd.zivinice.Navigator
import com.vinograd.zivinice.podaci.Cjenovnik
import com.vinograd.zivinice.podaci.Korpa
import com.vinograd.zivinice.podaci.Sezona
import com.vinograd.zivinice.podaci.Sorte
import com.vinograd.zivinice.podaci.Stat
import com.vinograd.zivinice.resources.Res
import com.vinograd.zivinice.resources.ic_cart
import com.vinograd.zivinice.ui.Boje
import com.vinograd.zivinice.ui.Poppins
import com.vinograd.zivinice.ui.TIP_SADNICA
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.floor
import kotlin.math.round

private enum class Jedinica(val naziv: String, val m2: Double) {
    M2("m²", 1.0), DUNUM("dunum", 1000.0), HA("hektar", 10_000.0)
}

/** Unos se pamti dok aplikacija radi — kupac ode na sortu i vrati se bez ponovnog kucanja. */
private object Unos {
    var povrsina by mutableStateOf("")
    var jedinica by mutableStateOf(Jedinica.DUNUM)
    var redovi by mutableStateOf("")
    var uRedu by mutableStateOf("")
}

private fun samoBroj(s: String): String = s.filter { it.isDigit() || it == ',' || it == '.' }.take(8)
private fun broj(s: String): Double? = s.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }

/** 1000.0 → "1000", 2.5 → "2,5" */
private fun lijepo(x: Double): String {
    val cijeli = x.toLong()
    if (x == cijeli.toDouble()) return cijeli.toString()
    return (round(x * 100) / 100).toString().replace('.', ',')
}

/** 1 sadnicu, 2–4 sadnice, 5+ sadnica (11–14 sadnica). */
private fun sadnicaRijec(n: Int): String {
    val d = n % 100
    return when {
        d in 11..14 -> "sadnica"
        n % 10 == 1 -> "sadnicu"
        n % 10 in 2..4 -> "sadnice"
        else -> "sadnica"
    }
}

/** Kalkulator broja sadnica: površina ÷ (razmak redova × razmak u redu). naziv == null → bez sorte. */
@Composable
fun KalkulatorEkran(naziv: String?, nav: Navigator) {
    val cijene by Cjenovnik.mapa.collectAsState()
    val sezona by Sezona.stanje.collectAsState()
    val sve by Sorte.sve.collectAsState()
    val scope = rememberCoroutineScope()
    val dno = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val p = broj(Unos.povrsina)
    val r = broj(Unos.redovi)
    val u = broj(Unos.uRedu)
    val m2 = p?.let { it * Unos.jedinica.m2 }
    val komada = if (m2 != null && r != null && u != null) floor(m2 / (r * u)).toInt() else null

    val kljuc = remember(sve, naziv) { naziv?.let { Sorte.poNazivu(it)?.kljuc ?: Sorte.kljucOd(it) } }
    val c = kljuc?.let { cijene[it] }
    val cijena = c?.cijenaSadnica ?: 0.0
    val naStanju = c?.naStanjuSadnica ?: true
    val zatvorena = remember(sezona) { Sezona.zatvorena() }

    Column(Modifier.fillMaxSize().background(Boje.Krem)) {
        ZaglavljeEkrana("Kalkulator sadnica", naziv ?: "Koliko sadnica vam treba?", nav)

        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp + dno)
        ) {
            // POVRŠINA
            NaslovSekcije("POVRŠINA", Modifier.padding(start = 4.dp))
            PoljeForme(
                Unos.povrsina, { Unos.povrsina = samoBroj(it) }, "Površina", null,
                Modifier.fillMaxWidth().padding(top = 8.dp),
                tastatura = KeyboardType.Decimal, prostorZaGresku = false,
            )
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Jedinica.entries.forEach { j ->
                    val izabrana = Unos.jedinica == j
                    Text(
                        j.naziv, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins(),
                        color = if (izabrana) Color.White else Boje.TekstGlavni,
                        modifier = Modifier.clip(RoundedCornerShape(100.dp))
                            .background(if (izabrana) Boje.ZelenaTamna else Boje.ChipPozadina)
                            .clickable { Unos.jedinica = j }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                    )
                }
            }

            // RAZMAK
            NaslovSekcije("RAZMAK SADNJE (METRI)", Modifier.padding(top = 18.dp, start = 4.dp))
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PoljeForme(
                    Unos.redovi, { Unos.redovi = samoBroj(it) }, "Između redova", null,
                    Modifier.weight(1f), tastatura = KeyboardType.Decimal, prostorZaGresku = false,
                )
                PoljeForme(
                    Unos.uRedu, { Unos.uRedu = samoBroj(it) }, "U redu", null,
                    Modifier.weight(1f), tastatura = KeyboardType.Decimal, prostorZaGresku = false,
                )
            }

            // REZULTAT
            Column(
                Modifier.padding(top = 18.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(Boje.ZelenaTamna).padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "POTREBNO SADNICA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White,
                    fontFamily = Poppins(), letterSpacing = 0.12.em, modifier = Modifier.alpha(0.8f),
                )
                Text(
                    komada?.toString() ?: "—", fontSize = 40.sp, fontWeight = FontWeight.Bold,
                    color = Color.White, fontFamily = Poppins(),
                )
                Text(
                    if (komada != null) "${lijepo(m2!!)} m² ÷ (${lijepo(r!!)} m × ${lijepo(u!!)} m)"
                    else "Unesite površinu i oba razmaka",
                    fontSize = 12.sp, color = Color.White, fontFamily = Poppins(), modifier = Modifier.alpha(0.8f),
                )
                if (komada != null && komada > 0 && cijena > 0) {
                    Text(
                        "$komada × ${Cjenovnik.format(cijena)} = ${Cjenovnik.format(cijena * komada)}",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Boje.ZlatnaSvijetla,
                        fontFamily = Poppins(), modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }

            // DUGME
            if (naziv != null) {
                val aktivno = komada != null && komada > 0 && !zatvorena && naStanju
                val tekst = when {
                    zatvorena -> "Sezona zatvorena"
                    !naStanju -> "Sadnice nisu na stanju"
                    komada == null || komada <= 0 -> "Dodaj u korpu"
                    else -> "Dodaj $komada ${sadnicaRijec(komada)} u korpu"
                }
                Button(
                    onClick = {
                        val k = komada ?: return@Button
                        scope.launch {
                            Korpa.dodaj(naziv, TIP_SADNICA, k)
                            Stat.dogadjaj(Stat.KORPA)
                            nav.otvori(Ekran.Korpa)
                        }
                    },
                    enabled = aktivno,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Boje.Vino, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.padding(top = 14.dp).fillMaxWidth().height(50.dp),
                ) {
                    Icon(painterResource(Res.drawable.ic_cart), contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        tekst, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins(),
                        maxLines = 1, modifier = Modifier.padding(start = 8.dp),
                    )
                }
            } else {
                Button(
                    onClick = { nav.otvori(Ekran.Katalog) },
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Boje.Vino, contentColor = Color.White),
                    modifier = Modifier.padding(top = 14.dp).fillMaxWidth().height(50.dp),
                ) {
                    Text("Izaberi sortu u katalogu", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins())
                }
            }
        }
    }
}
