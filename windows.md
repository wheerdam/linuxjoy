# Windows XInput Native Library

A Windows [XInput](https://msdn.microsoft.com/en-us/library/windows/desktop/hh405053(v=vs.85).aspx) native library that implements the native API is included in LinuxJoystick. The source code of the native library is named [`winxinput.cpp`](LinuxJoystick/native/winxinput.cpp). This library can be compiled in Visual Studio 2015 with the provided solution file in `LinuxJoystick/native/njnative` and will generate `njnative.dll`.

**Note:** The architecture of the native library must match with the JVM architecture. E.g. a 32-bit native library will not be able to link with LinuxJoystick running on a 64-bit JVM, and vice versa. `NoJoy` keeps a stack trace if a link failure has occured. This stack trace can be accessed with the `NoJoy.getLinkErrorString()` function.

The native library will read the device status with XInput's `XInputGetState` function when its `nativePoll(int index)` function is called. It will then construct and return Linux Joystick API packets if there is a change in the state of the specified controller. The LinuxJoystick framework will decode these packets, update the joystick object state, and generate callback events.

## Usage

As long as the native library can be linked properly, the user program can use the LinuxJoystick library as usual. The XInput native library will not block on `poll()` as required by the [`NoJoy` native API](native-api.md). The native library will always report 4 devices on enumeration. The integer value of a device will be set to -1 if it is not connected. It will be set to 0x080B01 otherwise (8 axes, 11 buttons, Xbox controller). The Xbox button **will not work** as Windows will intercept this button (the Xbox button is reported as button 8 in Linux).

The native library does not support the d-pad, yet. The d-pad is treated as axes in Linux (axes 6 and 7), but they are treated as buttons in XInput. Support will be coming soon, but it will be most likely follow the Linux model.

## Miscellaneous

The NoJoy Windows XInput library has a few properties that can be useful:

- Key=0 - set dead zone value (0 to 32767) for the specified controller in the index value
- Key=1 - set vibration setting for the specified controller. High 16-bit of value field is LEFT motor, and the lower 16-bit controls the RIGHT motor
- Key=2 - return version array (4 bytes)
 
`getVersionString()` will return "NoJoy Windows XInput (njnative.dll) v1.00" with possible different version number.