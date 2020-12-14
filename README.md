# Home surveillance made easy with ESPGUARD

Group project by Kert Tali, Kaspar Valk, Oliver Vainumäe.

LTAT.06.009 Mobile Computing and Internet of Things (2020/21 fall)

University of Tartu

## Quick Setup

### Creating a mock movement sensor

The ESP32 side of the code is intended to be used with a RCWL-0516 microwave movement sensor, but it can be mocked with a momentary switch for testing.

1. Wire the button so that it connects GND to pin 23 on an ESP32.
2. Create your own `params.h` and `secrets.h` files in `guardnode/`, the base of which is provided in the corresponding `example_*.h` file.
3. Make sure to have `#define MOCK_SENSOR true` in `params.h` for the button to work correctly
