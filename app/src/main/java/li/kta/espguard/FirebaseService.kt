package li.kta.espguard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import li.kta.espguard.activities.MainActivity
import li.kta.espguard.activities.SettingsActivity
import li.kta.espguard.activities.SettingsActivity.Companion.PREFERENCES_IGNORE_NOTIFICATIONS
import li.kta.espguard.activities.SettingsActivity.Companion.PREFERENCES_QUIET_NOTIFICATIONS
import li.kta.espguard.activities.SettingsActivity.Companion.getBooleanPreference
import li.kta.espguard.activities.SettingsActivity.Companion.getSharedPreferences
import li.kta.espguard.room.EventEntity
import li.kta.espguard.room.LocalSensorDb
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class FirebaseService : FirebaseMessagingService() {

    companion object {
        const val TAG = "FirebaseService"
        const val STATUS_RESPONSE_ACTION = "firebase movement event"
    }

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        saveTokenToPreferences(token)
        MqttService.getInstance()?.healthCheckAllSensors()
    }

    private fun saveTokenToPreferences(token: String): Unit =
            getSharedPreferences(this).edit().let { editor ->
                editor.putString(SettingsActivity.PREFERENCES_FIREBASE_TOKEN, token)
                editor.apply()
            }


    override fun onMessageReceived(message: RemoteMessage): Unit = onMessageReceived(message.data)

    private fun onMessageReceived(data: Map<String, String>) {
        data["deviceId"]?.let { deviceId ->
            val sensor = LocalSensorDb.getSensorDao(this).findSensorByDeviceId(deviceId) ?: return

            val event = EventEntity(deviceId = deviceId, eventTime = getEventTime(data["timestamp"]))

            LocalSensorDb.getEventDao(this).insertEvents(event)

            // 'Esik' detected movement at 02:07 on Saturday, Dec 12
            if (!ignoreNotifications())
                sendNotification(resources.getString(R.string.notification_text_template,
                                                     sensor.name,
                                                     event.eventTime?.let { formateDate(it) }))

            sendBroadcast(Intent(STATUS_RESPONSE_ACTION))

            Log.i(TAG, event.toString())
            Log.i(TAG, data.toString())
        }
    }

    private fun getEventTime(timestamp: String?) = ZonedDateTime.ofInstant(
            timestamp?.toLong()?.let { Instant.ofEpochSecond(it) },
            TimeZone.getDefault().toZoneId()
    )

    private fun formateDate(eventTime: ZonedDateTime): String =
            eventTime.format(DateTimeFormatter.ofPattern("HH:mm 'on' EEEE, MMM dd"))


    private fun ignoreNotifications(): Boolean =
            getBooleanPreference(this, PREFERENCES_IGNORE_NOTIFICATIONS)

    /** TODO how to get this to send the user to the details view when clicking the notification
     * https://github.com/firebase/quickstart-android/blob/master/messaging/app/src/main/java/com/google/firebase/quickstart/fcm/kotlin/MyFirebaseMessagingService.kt
     */
    private fun sendNotification(message: String) {
        val intent = Intent(this, MainActivity::class.java)
                .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
                                                      PendingIntent.FLAG_ONE_SHOT)

        val notificationBuilder = NotificationCompat.Builder(this, "default") // TODO channelId -> string resource
                .setSmallIcon(R.drawable.ic_baseline_eye_24)
                .setContentTitle("Movement Alert") // TODO String resource
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)

        putNotificationsOnQuiet(notificationBuilder)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationManager.createNotificationChannel(
                    NotificationChannel("default", "Channel human readable title",
                                        NotificationManager.IMPORTANCE_DEFAULT))

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    private fun putNotificationsOnQuiet(notificationBuilder: NotificationCompat.Builder) {
        if (getBooleanPreference(this, PREFERENCES_QUIET_NOTIFICATIONS))
            notificationBuilder.setNotificationSilent()
    }
}