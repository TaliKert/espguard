package li.kta.espguard

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import li.kta.espguard.activities.SettingsActivity.Companion.PREFERENCES_FIREBASE_TOKEN
import li.kta.espguard.activities.SettingsActivity.Companion.getSharedPreferences
import li.kta.espguard.room.LocalSensorDb
import li.kta.espguard.room.SensorEntity
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.time.ZonedDateTime
import java.util.*

class MqttService(private val context: Context, var sensors: Array<SensorEntity>)
    : MqttCallbackExtended {

    companion object {
        private const val TAG = "MqttService"
        private const val SERVER_URI = "tcp://kta.li:1883"

        private const val HEALTH_HEARTBEAT_INTERVAL = 15_000L // ms

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
        const val STATUS_REQUEST_ACTION = "li.kta.status.request"

        private var mqttService: MqttService? = null

        fun initializeMqttService(context: Context, sensors: Array<SensorEntity>) {
            mqttService = MqttService(context, sensors).apply {
                initialize()
                initializePeriodicHealthCheck()
            }

            Log.i(TAG, "MqttService initialized")
        }

        fun destroyMqttService() {
            mqttService?.destroy()
            Log.i(TAG, "MqttService destroyed")
        }

        fun getInstance(): MqttService? = mqttService
    }

    private var mqttClient: MqttAndroidClient? = null
    private var periodicHealthCheckJob: Job? = null

    /** Initialize connection to the MQTT server. Should be alive when the app is alive. */
    fun initialize() {
        mqttClient = MqttAndroidClient(context, SERVER_URI, UUID.randomUUID().toString())
        mqttClient?.let {
            it.setCallback(this)
            it.connect(MqttConnectOptions().apply { isAutomaticReconnect = true })
        }
    }

    fun destroy() {
        mqttClient?.disconnect()

        periodicHealthCheckJob?.let { if (it.isActive) it.cancel() }
    }

    /** Call this method if you bind a new sensor to your app */
    fun subscribe(sensor: SensorEntity) {
        mqttClient?.subscribe(SENSOR_STATUS_TOPIC_PREFIX + sensor.deviceId, 1)
    }

    fun healthCheck(sensor: SensorEntity) {
        mqttClient?.let { client ->
            if (!client.isConnected) return

            val token = getSharedPreferences(context).getString(PREFERENCES_FIREBASE_TOKEN, "")
            client.publish(SENSOR_HEALTH_TOPIC_PREFIX + sensor.deviceId,
                           MqttMessage("{\"token\": \"$token\"}".toByteArray()))

            Log.i(TAG, "Sent health check msg to ${sensor.name} with token $token")

            LocalSensorDb.getSensorDao(context)
                    .updateSensorLastHealthCheck(ZonedDateTime.now(), sensor.id)
        }
    }

    fun healthCheckAllSensors() = sensors.forEach { healthCheck(it) }

    fun turnOnOff(sensor: SensorEntity) {
        mqttClient?.let { client ->
            if (!client.isConnected) return

            val activityStatus = if (sensor.turnedOn) 1 else 0
            client.publish(SENSOR_CONFIG_TOPIC_PREFIX + sensor.deviceId,
                           MqttMessage("{active: $activityStatus}".toByteArray()))

            Log.i(TAG, "Turned on/off sensor ${sensor.name}")
        }
    }

    fun initializePeriodicHealthCheck() {
        periodicHealthCheckJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                healthCheckAllSensors()
                context.sendBroadcast(Intent(STATUS_REQUEST_ACTION))
                delay(HEALTH_HEARTBEAT_INTERVAL)
            }
        }

        periodicHealthCheckJob?.start()
    }

    /** Either connected or reconnected. The given sensors' status topics are subscribed to. */
    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        Log.i(TAG, "MQTT Connected")

        for (sensor in sensors) subscribe(sensor)

        healthCheckAllSensors()
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        Log.i(TAG, "MQTT Message: \"${message.toString()}\" (topic=\"${topic}\")")

        topic?.split("/")?.last()?.let { deviceId ->
            updateSensor(deviceId, message)
            sendMessageBroadcast(deviceId, message)
        }
    }

    private fun updateSensor(deviceId: String, message: MqttMessage?) {
        LocalSensorDb.getSensorDao(context).findSensorByDeviceId(deviceId)?.let { sensor ->
            val json = Gson().fromJson(message.toString(), JsonObject::class.java)

            sensor.turnedOn = json.get("active").asBoolean // TODO - magic string
            sensor.successfulHealthCheck = ZonedDateTime.now()

            LocalSensorDb.getSensorDao(context).updateSensor(sensor)
        }
    }

    private fun sendMessageBroadcast(deviceId: String, message: MqttMessage?): Unit =
            context.sendBroadcast(Intent(STATUS_RESPONSE_ACTION).apply {
                putExtra("deviceId", deviceId) // TODO - magic extra name to const
                putExtra("message", message.toString()) // TODO - magic extra name to const
            })

    override fun connectionLost(cause: Throwable?) {
        Log.i(TAG, "MQTT Connection Lost!")
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        Log.i(TAG, "MQTT Message Delivered!")
    }
}