//
// Example parameters file.
// Create your own, named `params.h`
//

#define DEVICE_ID "<your node's identifier; must be unique>"
#define FCM_PROJECTNAME "espguard-mciot" // keep as is
#define MQTT_BROKER "kta.li" // keep as is

#define ACTIVE true // active at boot?
#define TIMEOUT 900 // seconds to wait until sending the next movement notification

#define MOCK_SENSOR true // define as `true` if using a button to mock the movement. Use `false` if you use the RCWL-0516