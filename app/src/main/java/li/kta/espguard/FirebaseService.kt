package li.kta.espguard

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import li.kta.espguard.activities.SettingsActivity
import li.kta.espguard.room.EventEntity
import li.kta.espguard.room.LocalSensorDb
import java.time.Instant
import java.time.ZonedDateTime
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
        val event = EventEntity(0,
            data["deviceId"],
            ZonedDateTime.ofInstant(Instant.parse(data["timestamp"]), TimeZone.getDefault().toZoneId())
        )

        LocalSensorDb.getInstance(this).getEventDao().insertEvents(event)

        this.sendBroadcast(Intent(STATUS_RESPONSE_ACTION))

        Log.i(TAG, event.toString())
        Log.i(TAG, data.toString())
    }
}