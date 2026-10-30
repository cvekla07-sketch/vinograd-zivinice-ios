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
import platform.Foundation.secondsFromGMT
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIApplication
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.Foundation.NSBundle
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import platform.posix.uname
import platform.posix.utsname
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

actual fun otvoriLinkJavi(url: String, rezultat: (Boolean) -> Unit) {
    val u = NSURL.URLWithString(url)
    if (u == null) {
        rezultat(false)
        return
    }
    UIApplication.sharedApplication.openURL(u, options = emptyMap<Any?, Any>()) { uspjeh -> rezultat(uspjeh) }
}

actual fun jeSimulator(): Boolean =
    NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null

@OptIn(ExperimentalForeignApi::class)
actual fun modelUredjaja(): String = memScoped {
    val u = alloc<utsname>()
    uname(u.ptr)
    u.machine.toKString()
}

actual fun verzijaSistema(): String = UIDevice.currentDevice.systemVersion

actual fun verzijaAplikacije(): String =
    NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "?"

actual fun verzijaKodAplikacije(): Int =
    (NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String)?.toIntOrNull() ?: 0

actual suspend fun notifDozvoljene(): Boolean = suspendCoroutine { k ->
    UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { st ->
        val s = st?.authorizationStatus
        k.resume(s == UNAuthorizationStatusAuthorized || s == UNAuthorizationStatusProvisional)
    }
}

actual suspend fun notifNeodredjeno(): Boolean = suspendCoroutine { k ->
    UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { st ->
        k.resume(st?.authorizationStatus == UNAuthorizationStatusNotDetermined)
    }
}

actual fun traziDozvoluNotif(gotovo: () -> Unit) {
    UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
        UNAuthorizationOptionAlert or UNAuthorizationOptionSound
    ) { _, _ -> gotovo() }
}
