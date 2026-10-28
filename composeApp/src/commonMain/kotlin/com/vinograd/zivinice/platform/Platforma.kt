package com.vinograd.zivinice.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/** Trenutno vrijeme u milisekundama (kao System.currentTimeMillis()). */
expect fun sadaMs(): Long

/** Pomak lokalne vremenske zone od UTC, u milisekundama. */
expect fun pomakZoneMs(): Long

/** DataStore fajl u folderu aplikacije (zamjena za SharedPreferences sa istim imenom). */
expect fun kreirajDataStore(ime: String): DataStore<Preferences>

/** Otvara link u sistemskoj aplikaciji (mape, web, telefon…) — kao Intent.ACTION_VIEW. */
expect fun otvoriLink(url: String)

/** Kao otvoriLink, ali javi da li se link otvorio (false = nema aplikacije / neispravan link). */
expect fun otvoriLinkJavi(url: String, rezultat: (Boolean) -> Unit)

/** iOS simulator / Android emulator — ne ulazi u statistiku (kao Uredjaj.jeEmulator). */
expect fun jeSimulator(): Boolean

/** Interni naziv modela (npr. "iPhone16,1"). */
expect fun modelUredjaja(): String

/** Verzija operativnog sistema (npr. "18.6"). */
expect fun verzijaSistema(): String

/** Verzija aplikacije (MARKETING_VERSION, npr. "4.4"). */
expect fun verzijaAplikacije(): String

/** Broj builda (CFBundleVersion). */
expect fun verzijaKodAplikacije(): Int

/** Da li su notifikacije dozvoljene. */
expect suspend fun notifDozvoljene(): Boolean
