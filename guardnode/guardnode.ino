#include <WiFi.h>
#include <ArduinoHttpClient.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include "time.h"
#include "secrets.h"

const char* mqttServer = "kta.li";
const char* ntpServer = "pool.ntp.org";
const char* fcmServer = "fcm.googleapis.com";

const String deviceId = "allahuakbar";

WiFiClient wifiClient;
HttpClient fcmClient = HttpClient(wifiClient, fcmServer, 80);
PubSubClient mqttClient(wifiClient);

//// Power pin is LOW -> power to the sensor is interrupted
//#define SENSOR_POWER_PIN 22
#define SENSOR_PIN 23
#define LED_PIN 2

String projectName = "espguard-mciot";


time_t lastNotificationTimestamp = 0;
int notificationTimeoutPeriod = 900; // TODO: configurable over MQTT

bool active = false;


void setup() {
  Serial.begin(115200);

  Serial.print("Connecting to ");
  Serial.println(SECRET_SSID);
  
  WiFi.begin(SECRET_SSID, SECRET_PASS);
  while (WiFi.status() != WL_CONNECTED) {
      delay(500);
      Serial.print(".");
  }
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  // Sync device time with NTP server
  configTime(0, 0, ntpServer);

  // Connect to MQTT server
  mqttClient.setServer(mqttServer, 1883);
  mqttClient.setCallback(callback);

  pinMode(LED_PIN, OUTPUT);
  
//// Not implemented power saving switch
//  pinMode(SENSOR_POWER_PIN, OUTPUT);
//  digitalWrite(SENSOR_POWER_PIN, HIGH);

// Use the following pinmode when running RCWL-0516 sensor
  pinMode(SENSOR_PIN, INPUT);
//// Use the following if using a button to mock a sensor
//  pinMode(SENSOR_PIN, INPUT_PULLUP);
  
  attachInterrupt(SENSOR_PIN, notifyMovement, RISING);
}

void notifyMovement() {
  if (!active) return;
  Serial.println("Movement detected");

  // Acquire current time
  struct tm timeinfo;
  getLocalTime(&timeinfo);
  time_t now;
  time(&now);
  Serial.println(now);

  if (now - lastNotificationTimestamp < notificationTimeoutPeriod) {
    Serial.println("Not enough time passed from last notification");
    return;
  }
  lastNotificationTimestamp = now;

  // Send a notification to firebase cloud messaging
  //fcmClient.post("https://fcm.googleapis.com/v1/projects/" + projectName + "/messages:send");
}

// Send back a status payload, describing all current params as json
void sendStatus() {
  StaticJsonDocument<200> jsonPayload;
  jsonPayload["active"] = active;
  jsonPayload["timeout"] = notificationTimeoutPeriod;
  
  String output = "";
  serializeJson(jsonPayload, output);
  mqttClient.publish(("espguard/status/" + deviceId).c_str(), output.c_str());
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [" + String(topic) + "] ");
  
  if (!memcmp(&topic[9], (char*)"health", 6)) {
    Serial.println("health query");
    sendStatus(); 
  }
  
  if (!memcmp(&topic[9], (char*)"config", 6)) {
    Serial.println("config command");
    
    StaticJsonDocument<200> jsonPayload;
    DeserializationError error = deserializeJson(jsonPayload, payload);
    if (error) {
      Serial.print(F("JSON deserialization failed: "));
      Serial.println(error.f_str());
      return;
    }

    if (jsonPayload.containsKey("active")) {
      active = jsonPayload["active"];
      Serial.print("Set param `active` to ");
      Serial.println(active);
    }
    if (jsonPayload.containsKey("timeout")) {
      notificationTimeoutPeriod = jsonPayload["timeout"];
      Serial.print("Set param `timeout` to ");
      Serial.println(notificationTimeoutPeriod);
    }
  }
}

void reconnect() {
  while (!mqttClient.connected()) {
    Serial.print("Attempting MQTT connection...");
    
    if (mqttClient.connect(deviceId.c_str())) {
      Serial.println("MQTT Connected");
      mqttClient.subscribe(
        ("espguard/config/" + deviceId).c_str(), 1
      );
      mqttClient.subscribe(
        ("espguard/health/" + deviceId).c_str(), 1
      );
    } else {
      Serial.print("failed, rc=");
      Serial.print(mqttClient.state());
      Serial.println("retry in 5 seconds");
      delay(5000);
    }
  }
}

void loop() {
  reconnect();
  mqttClient.loop();
}
