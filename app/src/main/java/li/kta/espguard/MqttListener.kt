package li.kta.espguard

import android.content.Context
import android.util.Log
import android.widget.Toast
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage

typealias MessageCallback = (String) -> Unit

class MqttListener(private val context: Context,
                   private val mqttClient: MqttAndroidClient,
                   private val topic: String,
                   private val messageCallback: MessageCallback)
    : MqttCallbackExtended {

    companion object {
        private const val TAG = "MqttListener"
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        Log.i(TAG, "MQTT Connected")
        Toast.makeText(context, "MQTT Connected, subscribing...", Toast.LENGTH_SHORT).show()

        mqttClient.subscribe(topic, 0)
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        Log.i(TAG, "MQTT Message: ${message.toString()}")
        messageCallback(message.toString())
    }

    override fun connectionLost(cause: Throwable?) {
        Log.i(TAG, "MQTT Connection Lost!")
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        Log.i(TAG, "MQTT Message Delivered!")
    }
}