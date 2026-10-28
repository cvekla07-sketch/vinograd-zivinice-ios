package com.vinograd.zivinice.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/** Trenutno vrijeme u milisekundama (kao System.currentTimeMillis()). */
expect fun sadaMs(): Long

/** DataStore fajl u folderu aplikacije (zamjena za SharedPreferences sa istim imenom). */
expect fun kreirajDataStore(ime: String): DataStore<Preferences>
