package li.kta.espguard

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import li.kta.espguard.activities.MainActivity
import li.kta.espguard.activities.SettingsActivity
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

    private fun saveTokenToPreferences(token: String) {
        val editor = getSharedPreferences(SettingsActivity.PREFERENCES_FILE, AppCompatActivity.MODE_PRIVATE).edit()
        editor.putString(SettingsActivity.PREFERENCES_FIREBASE_TOKEN, token)
        editor.apply()
    }


    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data

        data["deviceId"]?.let {
            val sensor = LocalSensorDb.getInstance(this).getSensorDao().findSensorByDeviceId(it)
            if (sensor != null) {
                val event = EventEntity(
                    deviceId = data["deviceId"],
                    eventTime = ZonedDateTime.ofInstant(
                        data["timestamp"]?.toLong()?.let { Instant.ofEpochSecond(it) },
                        TimeZone.getDefault().toZoneId()
                    )
                )

                LocalSensorDb.getInstance(this).getEventDao().insertEvents(event)

                // 'Esik' detected movement at 02:07 on Saturday, Dec 12
                if (!ignoreNotifications()) {
                    sendNotification("'${sensor.name}' detected movement at ${event.eventTime?.format(
                        DateTimeFormatter.ofPattern("HH:mm 'on' EEEE, MMM dd"))}")
                }


                this.sendBroadcast(Intent(STATUS_RESPONSE_ACTION))

                Log.i(TAG, event.toString())
                Log.i(TAG, data.toString())
            }
        }
    }

    private fun ignoreNotifications(): Boolean {
        val preferences =
            this.getSharedPreferences(SettingsActivity.PREFERENCES_FILE, Context.MODE_PRIVATE)
        return preferences.getBoolean(SettingsActivity.PREFERENCES_IGNORE_NOTIFICATIONS, false)
    }

    /** TODO how to get this to send the user to the details view when clicking the notification
     * https://github.com/firebase/quickstart-android/blob/master/messaging/app/src/main/java/com/google/firebase/quickstart/fcm/kotlin/MyFirebaseMessagingService.kt
     */
    private fun sendNotification(message: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, "default") // TODO channelId -> string resource
            .setSmallIcon(R.drawable.ic_menu_view)
            .setContentTitle("Movement Alert") // TODO String resource
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        putNotificationsOnQuiet(notificationBuilder)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("default",
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    fun putNotificationsOnQuiet(notificationBuilder: NotificationCompat.Builder) {
        val preferences =
            this.getSharedPreferences(SettingsActivity.PREFERENCES_FILE, Context.MODE_PRIVATE)
        val setNotificationsQuiet = preferences.getBoolean(SettingsActivity.PREFERENCES_QUIET_NOTIFICATIONS, false)

        if (setNotificationsQuiet) {
            notificationBuilder.setNotificationSilent()
        }
    }
}