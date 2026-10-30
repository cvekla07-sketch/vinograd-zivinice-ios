import UIKit
import UserNotifications
import FirebaseCore
import FirebaseMessaging
import ComposeApp

/// Push obavijesti — kao MojFirebaseMessagingService + pretplata iz SplashActivity.
class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        FirebaseApp.configure()
        Messaging.messaging().delegate = self
        UNUserNotificationCenter.current().delegate = self
        // Registracija kod Applea — ne pita korisnika (dozvolu traži naš dijalog)
        application.registerForRemoteNotifications()
        return true
    }

    func application(_ application: UIApplication,
                     didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }

    // FCM token spreman → pretplata na topic, na svakom pokretanju (kao SplashActivity)
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        guard fcmToken != nil else { return }
        Messaging.messaging().subscribe(toTopic: "vinograd_svi")
    }

    // Obavijest stigla dok je aplikacija otvorena → ipak prikaži (kao Android)
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        if #available(iOS 14.0, *) {
            completionHandler([.banner, .list, .sound])
        } else {
            completionHandler([.alert, .sound])
        }
    }

    // Dodir na obavijest → ekran iz data polja "ekran"
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        let ekran = response.notification.request.content.userInfo["ekran"] as? String ?? ""
        PushEkran.shared.otvori(ekran: ekran)
        completionHandler()
    }
}
