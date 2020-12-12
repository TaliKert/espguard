package li.kta.espguard

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import li.kta.espguard.activities.SettingsActivity
import li.kta.espguard.room.LocalSensorDb
import li.kta.espguard.room.SensorEntity
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.time.ZonedDateTime
import java.util.*

class MqttService(
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

        const val STATUS_RESPONSE_ACTION = "li.kta.status.response"
        const val HEALTH_CHECK_COMPLETE = "health check complete"

        private lateinit var mqttService: MqttService

        fun initializeMqttService(context: Context, sensors: Array<SensorEntity>) {
            mqttService = MqttService(context, sensors)
            mqttService.initialize()
            Log.i(TAG, "MqttService initialized")
        }

        fun destroyMqttService() {
            mqttService.destroy()
            Log.i(TAG, "MqttService destroyed")
        }

        fun getInstance(): MqttService? {
            if (this::mqttService.isInitialized)
                return mqttService
            return null
        }
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
    fun subscribe(sensor: SensorEntity) {
//    sensors.add(sensor) // Make sensors an arraylist
        mqttClient.subscribe(SENSOR_STATUS_TOPIC_PREFIX + sensor.deviceId, 1)
    }

    fun healthCheck(sensor: SensorEntity) {
        val preferences =
            context.getSharedPreferences(SettingsActivity.PREFERENCES_FILE, Context.MODE_PRIVATE)
        val token = preferences.getString(SettingsActivity.PREFERENCES_FIREBASE_TOKEN, "")
        val msg = MqttMessage("{\"token\": \"$token\"}".toByteArray())

        mqttClient.publish(SENSOR_HEALTH_TOPIC_PREFIX + sensor.deviceId, msg)
        Log.i(TAG, "Sent health check msg to ${sensor.name} with token $token")

        sensor.lastHealthCheck = ZonedDateTime.now()
        LocalSensorDb.getInstance(context).getSensorDao().updateSensor(sensor)
    }

    fun healthCheckAllSensors() {
        sensors.forEach {
            healthCheck(it)
        }

        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            delay(300)
            context.sendBroadcast(
                Intent(HEALTH_CHECK_COMPLETE))
        }

    }

    fun turnOnOff(sensor: SensorEntity) {
        val activityStatus = if (sensor.turnedOn) 1 else 0
        val msg = MqttMessage("{active: $activityStatus}".toByteArray())
        mqttClient.publish(SENSOR_CONFIG_TOPIC_PREFIX + sensor.deviceId, msg)
        Log.i(TAG, "Turned on/off sensor ${sensor.name}")
    }

    /**
     * Either connected or reconnected. The given sensors' status topics are subscribed to.
     */
    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        Log.i(TAG, "MQTT Connected")
        for (sensor in sensors) {
            subscribe(sensor)
        }
        healthCheckAllSensors()
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        Log.i(TAG, "MQTT Message: `${message.toString()}` (topic=`${topic}`)")

        if (topic != null) {
            val deviceId = topic.split("/").last()
            val sensor = LocalSensorDb.getInstance(context).getSensorDao().findSensorByDeviceId(deviceId)
            if (sensor != null) {
                val json = Gson().fromJson(message.toString(), JsonObject::class.java)
                sensor.turnedOn = json.get("active").asBoolean
                sensor.successfulHealthCheck = ZonedDateTime.now()
                LocalSensorDb.getInstance(context).getSensorDao().updateSensor(sensor)

                context.sendBroadcast(
                    Intent(STATUS_RESPONSE_ACTION)
                        .putExtra("deviceId", deviceId)
                        .putExtra("message", message.toString())
                )
            }
        }
    }

    override fun connectionLost(cause: Throwable?) {
        Log.i(TAG, "MQTT Connection Lost!")
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        Log.i(TAG, "MQTT Message Delivered!")
    }
}