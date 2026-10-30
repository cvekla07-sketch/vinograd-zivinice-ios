package com.vinograd.zivinice.podaci

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.vinograd.zivinice.platform.jeSimulator
import com.vinograd.zivinice.platform.kreirajDataStore
import com.vinograd.zivinice.platform.modelUredjaja
import com.vinograd.zivinice.platform.notifDozvoljene
import com.vinograd.zivinice.platform.verzijaAplikacije
import com.vinograd.zivinice.platform.verzijaKodAplikacije
import com.vinograd.zivinice.platform.verzijaSistema
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlin.random.Random

/**
 * Anonimna statistika uređaja za admin panel (Firebase: uredjaji/{id}) — 1:1 Uredjaj.java.
 * Nasumični ID (nema veze sa uređajem), prvo i zadnje otvaranje, broj otvaranja, verzija aplikacije,
 * verzija iOS-a (polje "ios" umjesto "android"), model i da li su notifikacije dozvoljene.
 */
object Uredjaj {
    private val ds by lazy { kreirajDataStore("uredjaj_stat") }
    private val K_ID = stringPreferencesKey("id")
    private val K_PRVI_PUT_UPISAN = booleanPreferencesKey("prviPutUpisan")

    /** Poziva se jednom pri pokretanju aplikacije (kao MainActivity.onCreate). */
    fun zabiljezi() {
        if (jeSimulator()) return
        Stat.opseg.launch {
            try {
                val p = ds.data.first()
                val id = p[K_ID] ?: noviId().also { novi -> ds.edit { it[K_ID] = novi } }
                val prviPut = p[K_PRVI_PUT_UPISAN] != true
                val notif = notifDozvoljene()

                val m = buildJsonObject {
                    putJsonObject("zadnjiPut") { put(".sv", "timestamp") }
                    put("brojOtvaranja", Stat.PLUS_JEDAN)
                    put("verzija", verzijaAplikacije())
                    put("verzijaKod", verzijaKodAplikacije())
                    put("ios", verzijaSistema())
                    put("model", nazivModela(modelUredjaja()))
                    put("notif", notif)
                    if (prviPut) putJsonObject("prviPut") { put(".sv", "timestamp") }
                }
                if (Baza.dopuni("uredjaji/$id", m) && prviPut) {
                    ds.edit { it[K_PRVI_PUT_UPISAN] = true }
                }
            } catch (_: Exception) {
                // statistika nikad ne smije srušiti aplikaciju
            }
        }
    }

    /** Nakon odgovora na dozvolu — upiše samo stvarno stanje notifikacija (bez brojanja otvaranja). */
    fun osvjeziNotif() {
        if (jeSimulator()) return
        Stat.opseg.launch {
            try {
                val id = ds.data.first()[K_ID] ?: return@launch
                Baza.dopuni("uredjaji/$id", buildJsonObject { put("notif", notifDozvoljene()) })
            } catch (_: Exception) {
            }
        }
    }

    /** 32 heks znaka — kao UUID.randomUUID() bez crtica. */
    private fun noviId(): String = buildString {
        repeat(32) { append("0123456789abcdef"[Random.nextInt(16)]) }
    }

    /** Čitljiv naziv modela; nepoznat model → interni naziv (npr. iPhone18,1). */
    private fun nazivModela(id: String): String = MODELI[id] ?: id

    private val MODELI = mapOf(
        "iPhone8,1" to "iPhone 6s", "iPhone8,2" to "iPhone 6s Plus", "iPhone8,4" to "iPhone SE",
        "iPhone9,1" to "iPhone 7", "iPhone9,3" to "iPhone 7", "iPhone9,2" to "iPhone 7 Plus", "iPhone9,4" to "iPhone 7 Plus",
        "iPhone10,1" to "iPhone 8", "iPhone10,4" to "iPhone 8", "iPhone10,2" to "iPhone 8 Plus", "iPhone10,5" to "iPhone 8 Plus",
        "iPhone10,3" to "iPhone X", "iPhone10,6" to "iPhone X",
        "iPhone11,2" to "iPhone XS", "iPhone11,4" to "iPhone XS Max", "iPhone11,6" to "iPhone XS Max", "iPhone11,8" to "iPhone XR",
        "iPhone12,1" to "iPhone 11", "iPhone12,3" to "iPhone 11 Pro", "iPhone12,5" to "iPhone 11 Pro Max", "iPhone12,8" to "iPhone SE (2. gen)",
        "iPhone13,1" to "iPhone 12 mini", "iPhone13,2" to "iPhone 12", "iPhone13,3" to "iPhone 12 Pro", "iPhone13,4" to "iPhone 12 Pro Max",
        "iPhone14,4" to "iPhone 13 mini", "iPhone14,5" to "iPhone 13", "iPhone14,2" to "iPhone 13 Pro", "iPhone14,3" to "iPhone 13 Pro Max",
        "iPhone14,6" to "iPhone SE (3. gen)",
        "iPhone14,7" to "iPhone 14", "iPhone14,8" to "iPhone 14 Plus", "iPhone15,2" to "iPhone 14 Pro", "iPhone15,3" to "iPhone 14 Pro Max",
        "iPhone15,4" to "iPhone 15", "iPhone15,5" to "iPhone 15 Plus", "iPhone16,1" to "iPhone 15 Pro", "iPhone16,2" to "iPhone 15 Pro Max",
        "iPhone17,3" to "iPhone 16", "iPhone17,4" to "iPhone 16 Plus", "iPhone17,1" to "iPhone 16 Pro", "iPhone17,2" to "iPhone 16 Pro Max",
        "iPhone17,5" to "iPhone 16e",
    )
}
