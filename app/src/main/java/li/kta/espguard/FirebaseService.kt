package li.kta.espguard

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {

        //TODO
        val notification = message.notification
        val data = message.data

        Log.i("FIREBASE MSG", data.toString())

    }
}