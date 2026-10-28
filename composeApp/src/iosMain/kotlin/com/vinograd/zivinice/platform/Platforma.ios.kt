package com.vinograd.zivinice.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSTimeZone
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.localTimeZone
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIApplication

actual fun sadaMs(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()

actual fun pomakZoneMs(): Long = NSTimeZone.localTimeZone.secondsFromGMT.toLong() * 1000L

/** Documents folder aplikacije. */
@OptIn(ExperimentalForeignApi::class)
private fun folderAplikacije(): String {
    val url: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )
    return requireNotNull(url?.path) { "Documents folder nije dostupan" }
}

actual fun kreirajDataStore(ime: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { "${folderAplikacije()}/$ime.preferences_pb".toPath() }
    )

actual fun otvoriLink(url: String) {
    val u = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(u, options = emptyMap<Any?, Any>(), completionHandler = null)
}
