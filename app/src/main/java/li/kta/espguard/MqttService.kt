package li.kta.espguard

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import li.kta.espguard.room.SensorEntity
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*

class MqttService (
  private val context: Context,
  private val sensors: Array<SensorEntity>
) : MqttCallbackExtended {

  companion object {
    private const val TAG = "MqttService"
    private const val SERVER_URI = "tcp://kta.li:1883"

    /**
     * Status topic: Any published message by the node (incl. health check).
     *               Message is a json object of its current configuration.
     *
     * Health topic: Health-check messages sent to the node. Message is empty.
     *               The node should respond with its Status message if everything is OK.
     *
     * Config topic: Remote configuration messages sent to the node.
     *               Message should be json containing configuration values.
     *               The node will not respond to these.
     */
    private const val SENSOR_STATUS_TOPIC_PREFIX = "espguard/status/"
    private const val SENSOR_HEALTH_TOPIC_PREFIX = "espguard/health/"
    private const val SENSOR_CONFIG_TOPIC_PREFIX = "espguard/config/"

    const val STATUS_RESPONSE_ACTION_PREFIX = "li.kta.status.response"
  }

  private lateinit var mqttClient: MqttAndroidClient

  /**
   * Initialize connection to the MQTT server. Should be alive when the app is alive.
   */
  public fun initialize() {
    mqttClient = MqttAndroidClient(context, SERVER_URI, UUID.randomUUID().toString())
    mqttClient.setCallback(this)
    mqttClient.connect(MqttConnectOptions().apply { isAutomaticReconnect = true })
  }

  public fun destroy() {
    mqttClient.disconnect()
  }

  /**
   * Call this method if you bind a new sensor to your app
   */
  public fun subscribe(sensor: SensorEntity) {
//    sensors.add(sensor) // Make sensors an arraylist
    mqttClient.subscribe(SENSOR_STATUS_TOPIC_PREFIX + sensor.deviceId, 1)
  }

  public fun healthCheck(sensor: SensorEntity) {
    mqttClient.publish(SENSOR_HEALTH_TOPIC_PREFIX + sensor.deviceId, MqttMessage())
  }

  /**
   * Either connected or reconnected. The given sensors' status topics are subscribed to.
   */
  override fun connectComplete(reconnect: Boolean, serverURI: String?) {
    Log.i(TAG, "MQTT Connected")
    Toast.makeText(context, "MQTT Connected, subscribing...", Toast.LENGTH_SHORT).show()
    for (sensor in sensors) {
      mqttClient.subscribe(SENSOR_STATUS_TOPIC_PREFIX + sensor.deviceId, 1)
    }
  }

  override fun messageArrived(topic: String?, message: MqttMessage?) {
    Log.i(TAG, "MQTT Message: `${message.toString()}` (topic=`${topic}`)")
    if (topic != null) {
      val deviceId = topic.split("/").last()
      context.sendBroadcast(
        Intent(STATUS_RESPONSE_ACTION_PREFIX + deviceId)
          .putExtra("deviceId", deviceId)
          .putExtra("message", message.toString())
      )
    }
  }

  override fun connectionLost(cause: Throwable?) {
    Log.i(TAG, "MQTT Connection Lost!")
  }

  override fun deliveryComplete(token: IMqttDeliveryToken?) {
    Log.i(TAG, "MQTT Message Delivered!")
  }
}