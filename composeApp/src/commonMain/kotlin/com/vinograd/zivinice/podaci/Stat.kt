package com.vinograd.zivinice.podaci

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.vinograd.zivinice.platform.Datum
import com.vinograd.zivinice.platform.jeSimulator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * Anonimni brojači za admin panel (Firebase "statistika/") — 1:1 Stat.java + brojač posjeta iz MainActivity.
 *
 *  statistika/ukupnoPosjeta, statistika/posjeteDanas/{datum}  — pokretanje aplikacije
 *  statistika/sorte/{kljuc}                  — otvoren detalj sorte
 *  statistika/lijevak/{dogadjaj}             — ukupno (katalog, korpa, narudzbaOtvorena, narudzbaPoslana)
 *  statistika/lijevakDani/{datum}/{dogadjaj} — isto po danima
 *
 * Firebase REST: {".sv": {"increment": 1}} = ServerValue.increment(1). Simulator se ne broji.
 */
object Stat {
    const val KATALOG = "katalog"
    const val KORPA = "korpa"
    const val NARUDZBA_OTVORENA = "narudzbaOtvorena"
    const val NARUDZBA_POSLANA = "narudzbaPoslana"

    /** Statistika nikad ne čeka i ne ruši ekran. */
    internal val opseg = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    internal val PLUS_JEDAN: JsonObject = buildJsonObject { putJsonObject(".sv") { put("increment", 1) } }

    /** "yyyy-MM-dd" po lokalnom vremenu telefona (kao SimpleDateFormat na Androidu). */
    private fun danas(): String {
        val d = Datum.danas()
        return "${d.godina}-${d.mjesec.toString().padStart(2, '0')}-${d.dan.toString().padStart(2, '0')}"
    }

    /** Pokretanje aplikacije (MainActivity.izbrojiPosjetu). */
    fun posjeta() {
        if (jeSimulator()) return
        val dan = danas()
        opseg.launch {
            Baza.upisi("statistika/ukupnoPosjeta", PLUS_JEDAN)
            Baza.upisi("statistika/posjeteDanas/$dan", PLUS_JEDAN)
        }
    }

    /** Događaj u prodajnom lijevku (ukupno + po danu). */
    fun dogadjaj(naziv: String) {
        if (jeSimulator()) return
        val dan = danas()
        opseg.launch {
            Baza.dopuni("statistika", buildJsonObject {
                put("lijevak/$naziv", PLUS_JEDAN)
                put("lijevakDani/$dan/$naziv", PLUS_JEDAN)
            })
        }
    }

    /** Otvoren detalj sorte. */
    fun sorta(kljuc: String?) {
        if (kljuc.isNullOrEmpty() || jeSimulator()) return
        opseg.launch { Baza.upisi("statistika/sorte/$kljuc", PLUS_JEDAN) }
    }
}

/**
 * Pozove akciju jednom dok je ekran u stacku — kao poziv u Activity.onCreate
 * (povratak na ekran sa sljedećeg ne broji ponovo).
 */
@Composable
fun JednomNaEkranu(akcija: () -> Unit) {
    var uradjeno by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!uradjeno) {
            uradjeno = true
            akcija()
        }
    }
}
