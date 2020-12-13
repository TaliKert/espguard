#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <FirebaseESP32.h>
#include "time.h"
#include "secrets.h"
#include "params.h"

WiFiClient wifiClient;
PubSubClient mqttClient(wifiClient);

FirebaseData firebaseData;
const int TOKEN_LIMIT = 10;
const int TOKEN_LENGTH = 163+1;
char registeredTokens[TOKEN_LIMIT][TOKEN_LENGTH];
int registeredTokenCount = 0;

//// Power pin is LOW -> power to the sensor is interrupted
//#define SENSOR_POWER_PIN 22
#define SENSOR_PIN 23

time_t lastNotificationTimestamp = 0;

int notificationTimeoutPeriod = TIMEOUT;
bool active = ACTIVE;

// This is needed to avoid executing heavy API calls in the interrupt function
bool movementDetected = false;

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
  configTime(0, 0, "pool.ntp.org");

  // Connect to MQTT server
  mqttClient.setServer(MQTT_BROKER, 1883);
  mqttClient.setCallback(callback);

  // Setup cloud messaging API
  Firebase.begin(FCM_PROJECTNAME, "");
  Firebase.reconnectWiFi(true);
  Firebase.setMaxRetry(firebaseData, 3);
  firebaseData.fcm.begin(FCM_APIKEY);
  
//// Not implemented power saving switch
//  pinMode(SENSOR_POWER_PIN, OUTPUT);
//  digitalWrite(SENSOR_POWER_PIN, HIGH);

#if MOCK_SENSOR
  pinMode(SENSOR_PIN, INPUT_PULLUP);
#else
  pinMode(SENSOR_PIN, INPUT);
#endif
  
  attachInterrupt(SENSOR_PIN, detectMovement, RISING);
}

void detectMovement() {
  if (active) movementDetected = true;
}

void loop() {
  reconnect();
  mqttClient.loop();
  if (movementDetected) notifyMovement();
}

int firebaseRetries = 0;
void notifyMovement() {
  Serial.println("Movement detected");
  movementDetected = false;

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
  StaticJsonDocument<200> jsonPayload;
  jsonPayload["deviceId"] = DEVICE_ID;
  jsonPayload["timestamp"] = now;
  String output = "";
  serializeJson(jsonPayload, output);
  Serial.println(output);
  firebaseData.fcm.setDataMessage(output);

  if (Firebase.broadcastMessage(firebaseData)) {
    Serial.println("PASSED");
    Serial.println(firebaseData.fcm.getSendResult());
    Serial.println("------------------------------------");
    Serial.println();
  } else {
    Serial.println("FAILED");
    Serial.println("REASON: " + firebaseData.errorReason());
    Serial.println("------------------------------------");
    Serial.println();
    if (firebaseRetries > 2) {
      firebaseRetries = 0;
      Serial.println("Notification sending failed after 3 retries, something is very wrong.");
    } else {
      movementDetected = true;
      lastNotificationTimestamp = 0; // Try again on the next loop iteration.
    }
  }
}

// Send back a status payload, describing all current params as json
void sendStatus() {
  StaticJsonDocument<200> jsonPayload;
  jsonPayload["active"] = active;
  jsonPayload["timeout"] = notificationTimeoutPeriod;
  
  String output = "";
  serializeJson(jsonPayload, output);
  mqttClient.publish(("espguard/status/" + String(DEVICE_ID)).c_str(), output.c_str());
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [" + String(topic) + "] ");
  
  if (!memcmp(&topic[9], (char*)"health", 6)) {
    Serial.println("health query");

    StaticJsonDocument<200> jsonPayload;
    DeserializationError error = deserializeJson(jsonPayload, payload);
    if (error) {
      Serial.print(F("JSON deserialization failed: "));
      Serial.println(error.f_str());
      return;
    }
    
//    // attempt to register device token as notification receiver by token
    if (jsonPayload.containsKey("token")) {
      Serial.println("The message contained token");
      bool found = false;
      int i = 0;
      for (; i < registeredTokenCount; i++) {
        Serial.print("Comparing: ");
        Serial.println(registeredTokens[i]);
        Serial.print("To       : ");
        Serial.println(jsonPayload["token"].as<String>());
        if (!strncmp(registeredTokens[i], jsonPayload["token"], TOKEN_LENGTH)) {
          Serial.println("Valid token found");
          found = true;
          break;
        }
      }
      if ((!found) && (registeredTokenCount < TOKEN_LIMIT)) {
          Serial.println("Token not found, trying to add: " + jsonPayload["token"].as<String>());
          strncpy(registeredTokens[i], jsonPayload["token"].as<char*>(), TOKEN_LENGTH);
          firebaseData.fcm.addDeviceToken(jsonPayload["token"]);
          registeredTokenCount++;
        }
    }
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
    
    if (mqttClient.connect(DEVICE_ID)) {
      Serial.println("MQTT Connected");
      mqttClient.subscribe(
        ("espguard/config/" + String(DEVICE_ID)).c_str(), 1
      );
      mqttClient.subscribe(
        ("espguard/health/" + String(DEVICE_ID)).c_str(), 1
      );
      mqttClient.subscribe(
        ("espguard/delete/" + String(DEVICE_ID)).c_str(), 1
      );
    } else {
      Serial.print("failed, rc=");
      Serial.print(mqttClient.state());
      Serial.println("retry in 5 seconds");
      delay(5000);
    }
  }
}
