package li.kta.espguard

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import li.kta.espguard.activities.SettingsActivity
import li.kta.espguard.room.EventEntity
import li.kta.espguard.room.LocalSensorDb

class FirebaseService : FirebaseMessagingService() {

    companion object {
        const val TAG = "FirebaseService"
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
        //TODO
        val notification = message.notification
        val data = message.data

        val event = EventEntity(0, data["deviceId"], data["timestamp"]?.toLong())
        LocalSensorDb.getInstance(this).getEventDao().insertEvents(event)
        Log.i(TAG, data.toString())

    }
}