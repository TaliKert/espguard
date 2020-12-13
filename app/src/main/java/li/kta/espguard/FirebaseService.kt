package li.kta.espguard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import li.kta.espguard.activities.SensorDetailsActivity
import li.kta.espguard.activities.SettingsActivity
import li.kta.espguard.activities.SettingsActivity.Companion.PREFERENCES_IGNORE_NOTIFICATIONS
import li.kta.espguard.activities.SettingsActivity.Companion.PREFERENCES_QUIET_NOTIFICATIONS
import li.kta.espguard.activities.SettingsActivity.Companion.getBooleanPreference
import li.kta.espguard.activities.SettingsActivity.Companion.getSharedPreferences
import li.kta.espguard.room.EventEntity
import li.kta.espguard.room.LocalSensorDb
import li.kta.espguard.room.SensorEntity
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class FirebaseService : FirebaseMessagingService() {

    companion object {
        const val TAG = "FirebaseService"
        const val STATUS_RESPONSE_ACTION = "firebase movement event"

        const val NOTIFICATIONS_CHANNEL_ID = "default"
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
        data["deviceId"]?.let { onMessageReceived(data, it) }
    }

    private fun onMessageReceived(data: Map<String, String>, deviceId: String) {
        val sensor = LocalSensorDb.getSensorDao(this).findSensorByDeviceId(deviceId) ?: return

        val event = EventEntity(deviceId = deviceId, eventTime = getEventTime(data["timestamp"]))

        LocalSensorDb.getEventDao(this).insertEvents(event)

        // 'Esik' detected movement at 02:07 on Saturday, Dec 12
        if (!ignoreNotifications())
            sendNotification(resources.getString(R.string.notification_text_template,
                                                 sensor.name,
                                                 event.eventTime?.let { formattedDate(it) }),
              sensor)

        sendBroadcast(Intent(STATUS_RESPONSE_ACTION))

        Log.i(TAG, event.toString())
        Log.i(TAG, data.toString())
    }

    private fun getEventTime(timestamp: String?) = ZonedDateTime.ofInstant(
            timestamp?.toLong()?.let { Instant.ofEpochSecond(it) },
            TimeZone.getDefault().toZoneId()
    )

    private fun formattedDate(eventTime: ZonedDateTime): String =
            eventTime.format(DateTimeFormatter.ofPattern("HH:mm 'on' EEEE, MMM dd"))


    private fun ignoreNotifications(): Boolean =
            getBooleanPreference(this, PREFERENCES_IGNORE_NOTIFICATIONS)

    private fun sendNotification(message: String, sensor: SensorEntity) {
        val notificationBuilder = buildNotification(message, sensor)

        putNotificationsOnQuiet(notificationBuilder)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationManager.createNotificationChannel(
                    NotificationChannel(NOTIFICATIONS_CHANNEL_ID,
                                        "Channel human readable title",
                                        NotificationManager.IMPORTANCE_DEFAULT))

        notificationManager.notify(0 /* auto-generated */, notificationBuilder.build())
    }

    private fun buildNotification(message: String, sensor: SensorEntity): NotificationCompat.Builder {
        val intent = Intent(this, SensorDetailsActivity::class.java)
          .apply { putExtra(SensorDetailsActivity.EXTRA_SENSOR_ID, sensor.id) }

        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
          addNextIntentWithParentStack(intent)
          getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        return NotificationCompat.Builder(this, NOTIFICATIONS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_eye_24)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
    }

    private fun putNotificationsOnQuiet(notificationBuilder: NotificationCompat.Builder) {
        if (getBooleanPreference(this, PREFERENCES_QUIET_NOTIFICATIONS))
            notificationBuilder.setNotificationSilent()
    }
}