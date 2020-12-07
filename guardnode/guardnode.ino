#include <WiFi.h>
#include <ArduinoHttpClient.h>
#include <PubSubClient.h>
#include "time.h"
#include "secrets.h"

const char* mqttServer = "kta.li";
const char* ntpServer = "pool.ntp.org";
const char* fcmServer = "fcm.googleapis.com";

const String deviceId = "PLACEHOLDER";

WiFiClient wifiClient;
HttpClient fcmClient = HttpClient(wifiClient, fcmServer, 80);
PubSubClient mqttClient(wifiClient);

//// Power pin is LOW -> power to the sensor is interrupted
//#define SENSOR_POWER_PIN 22
#define SENSOR_PIN 23
#define LED_PIN 2

String projectName = "espguard";


time_t lastNotificationTimestamp = 0;
int notificationTimeoutPeriod = 900; // TODO: configurable over MQTT


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

  pinMode(SENSOR_PIN, INPUT);
  pinMode(LED_PIN, OUTPUT);
//  pinMode(SENSOR_POWER_PIN, OUTPUT);
//  digitalWrite(SENSOR_POWER_PIN, HIGH);
  attachInterrupt(SENSOR_PIN, notifyMovement, RISING);
}

void notifyMovement() {
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
  fcmClient.post("https://fcm.googleapis.com/v1/projects/" + projectName + "/messages:send");
}

void reconnect() {
  while (!mqttClient.connected()) {
    Serial.print("Attempting MQTT connection...");
    
    if (mqttClient.connect(deviceId.c_str())) {
      Serial.println("MQTT Connected");
      mqttClient.subscribe(
        ("ESPGUARD/configure/" + deviceId).c_str(), 1
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
  delay(10000);
}
