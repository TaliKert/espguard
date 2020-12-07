#define SENSOR_PIN 13
#define LED_PIN 2

void setup() {
  pinMode(SENSOR_PIN, INPUT);
  pinMode(LED_PIN, OUTPUT);
}

int sensorValue;
void loop() {
  sensorValue = digitalRead(SENSOR_PIN);
  if (sensorValue == HIGH) {
    digitalWrite(LED_PIN, HIGH);
  } else {
    digitalWrite(LED_PIN, LOW);
  }
}
