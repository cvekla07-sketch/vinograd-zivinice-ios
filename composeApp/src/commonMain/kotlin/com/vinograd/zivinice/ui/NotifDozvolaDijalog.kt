package com.vinograd.zivinice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.vinograd.zivinice.platform.kreirajDataStore
import com.vinograd.zivinice.platform.notifNeodredjeno
import com.vinograd.zivinice.resources.Res
import com.vinograd.zivinice.resources.ic_grape
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.painterResource

/** Pamti da je dijalog jednom prikazan — isti naziv i ključ kao Android (vinograd_prefs). */
object NotifDozvola {
    private val ds by lazy { kreirajDataStore("vinograd_prefs") }
    private val K_PRIKAZANO = booleanPreferencesKey("notif_dijalog_prikazan")

    /** True samo prvi put i samo ako iOS još nije pitao korisnika (sistemski prozor ide samo jednom). */
    suspend fun trebaPitati(): Boolean = try {
        when {
            ds.data.first()[K_PRIKAZANO] == true -> false
            !notifNeodredjeno() -> false
            else -> {
                ds.edit { it[K_PRIKAZANO] = true }
                true
            }
        }
    } catch (_: Exception) {
        false
    }
}

/** Pre-permission dijalog: objašnjenje PRIJE sistemskog zahtjeva. Dodir pored ga zatvara (setCancelable(true)). */
@Composable
fun NotifDozvolaDijalog(onUkljuci: () -> Unit, onZatvori: () -> Unit) {
    Dialog(onDismissRequest = onZatvori, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(start = 28.dp, top = 32.dp, end = 28.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.padding(bottom = 18.dp).size(84.dp).clip(CircleShape).background(Boje.ZelenaPastel),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(Res.drawable.ic_grape), contentDescription = null,
                    tint = Boje.ZelenaSvijetla, modifier = Modifier.size(44.dp),
                )
            }
            Text(
                "Ostanite u toku 🍇", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = Boje.ZelenaTamna, fontFamily = Poppins(), textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Text(
                "Uključite obavijesti da prvi saznate za nove cijene, " +
                    "mjesečne planove zaštite i novi katalog sorti — bez da otvarate aplikaciju.",
                fontSize = 14.sp, color = Color(0xFF5C7268), fontFamily = Poppins(),
                textAlign = TextAlign.Center, lineHeight = 24.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            )
            Box(
                Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Boje.ZelenaTamna)
                    .clickable { onUkljuci() }
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "🔔  Uključi obavijesti", fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = Color.White, fontFamily = Poppins(),
                )
            }
            Box(
                Modifier.fillMaxWidth().clickable { onZatvori() }.padding(top = 10.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Možda kasnije", fontSize = 14.sp, color = Color(0xFF5C7268), fontFamily = Poppins())
            }
        }
    }
}
