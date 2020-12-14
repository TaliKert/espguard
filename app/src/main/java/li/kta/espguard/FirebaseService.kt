package li.kta.espguard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
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

        const val DATA_DEVICE_ID = "deviceId"
        const val DATA_TIMESTAMP = "timestamp"


        fun formattedDate(eventTime: ZonedDateTime): String =
                eventTime.format(DateTimeFormatter.ofPattern("HH:mm 'on' EEEE, MMM dd"))
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
            getSharedPreferences(this).edit().let {
                it.putString(SettingsActivity.PREFERENCES_FIREBASE_TOKEN, token)
                it.apply()
            }


    override fun onMessageReceived(message: RemoteMessage): Unit = onMessageReceived(message.data)

    private fun onMessageReceived(data: Map<String, String>) {
        val deviceId = data[DATA_DEVICE_ID] ?: return

        val sensor = LocalSensorDb.getSensorDao(this).findSensorByDeviceId(deviceId) ?: return
        val event = EventEntity(deviceId = deviceId, eventTime = getEventTime(data[DATA_TIMESTAMP]))

        LocalSensorDb.getEventDao(this).insertEvents(event)

        // 'Esik' detected movement at 02:07 on Saturday, Dec 12
        if (!ignoreNotifications()) sendNotification(sensor, event)

        sendBroadcast(Intent(STATUS_RESPONSE_ACTION))

        Log.i(TAG, event.toString())
        Log.i(TAG, data.toString())
    }

    private fun getEventTime(timestamp: String?): ZonedDateTime = ZonedDateTime.ofInstant(
            timestamp?.toLong()?.let { Instant.ofEpochSecond(it) },
            TimeZone.getDefault().toZoneId()
    )

    private fun ignoreNotifications(): Boolean =
            getBooleanPreference(this, PREFERENCES_IGNORE_NOTIFICATIONS)

    private fun sendNotification(sensor: SensorEntity, event: EventEntity) {
        val notificationBuilder = buildNotification(sensor, event)

        putNotificationsOnQuiet(notificationBuilder)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationManager.createNotificationChannel(
                    NotificationChannel(NOTIFICATIONS_CHANNEL_ID,
                                        "Channel human readable title", // TODO - solve magic string...
                                        NotificationManager.IMPORTANCE_DEFAULT))

        notificationManager.notify(0 /* auto-generated */, notificationBuilder.build())
    }


    private fun buildNotification(sensor: SensorEntity, event: EventEntity): NotificationCompat.Builder =
            NotificationCompat.Builder(this, NOTIFICATIONS_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_baseline_eye_24)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getNotificationMessage(sensor, event))
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentIntent(getNotificationPendingIntent(sensor))

    private fun getNotificationMessage(sensor: SensorEntity, event: EventEntity): String =
            resources.getString(R.string.notification_text_template,
                                sensor.name, event.eventTime?.let { formattedDate(it) })

    private fun getNotificationPendingIntent(sensor: SensorEntity): PendingIntent? =
            TaskStackBuilder.create(this).run {
                addNextIntentWithParentStack(getNotificationIntent(sensor))
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }

    private fun getNotificationIntent(sensor: SensorEntity): Intent =
            Intent(this, SensorDetailsActivity::class.java)
                    .apply { putExtra(SensorDetailsActivity.EXTRA_SENSOR_ID, sensor.id) }

    private fun putNotificationsOnQuiet(notificationBuilder: NotificationCompat.Builder) {
        if (getBooleanPreference(this, PREFERENCES_QUIET_NOTIFICATIONS))
            notificationBuilder.setNotificationSilent()
    }
}