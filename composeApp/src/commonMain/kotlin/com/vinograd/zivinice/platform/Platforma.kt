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
