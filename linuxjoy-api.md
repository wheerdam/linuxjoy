# LinuxJoystick API Documentation

This document describes the LinuxJoystick Library API. The description includes the `LinuxJoystick` class, support classes, extensions to support multiple platforms and use-cases, and description of the framework that enables these extensions.

## The LinuxJoystick Class

`LinuxJoystick` is a Java class that can be used to interface with a Joystick device in Linux. The constructor for the class takes the path to the Joystick device file and the number of buttons and axes that the controller has. The following is an example initialization of the class:

```java
// get a controller with 11 buttons and 8 axes on /dev/input/js0 
LinuxJoystick j = new LinuxJoystick("/dev/input/js0", 11, 8);
```

The `poll()` function then can be called to read any new data that the kernel driver writes out through the device file. `LinuxJoystick` will read this data and update the state of the joystick accordingly. `LinuxJoystick` uses `FileChannel` to open and read the device file. The `FileChannel` class allows an interruptible blocking read on the file. A thread should be utilized to regularly poll the controller so `LinuxJoystick` will not block the flow of the rest of the program when it is being polled.

The function `setButtonsAxes(int buttons, int axes)` can be used to update the number of buttons and axes that the controller has. This function is useful because in some cases it is possible to determine the number of buttons and axes after the device has been opened. In the case of Linux, the kernel driver will send synthetic initial state packets that can be used to determine the number of buttons and axes of the controller. See the [Linux Joystick API documentation](https://www.kernel.org/doc/Documentation/input/joystick-api.txt) for details.

### Background Polling Thread

`LinuxJoystick` can have a background thread running that will regularly poll the controller. The thread can be started using the `startPollingThread(int interval)` function. The interval is the number of milliseconds between `poll()` calls. The polling thread can be interrupted and stopped at anytime by calling the `stopPollingThread()` function. Closing the controller with `close()` will also interrupt and stop the polling thread.

The functions that update the Joystick states data structure are synchronized with the functions that the user can use to retrieve this data structure. The user does not have to be concerned about ensuring that there is no race condition while reading off the Joystick with the polling thread running.

### Get Joystick States

Two functions `getAxisState(int index)` and `getButtonState(int index)` can be used to get the state of an axis or a button. The `getAxisState(int index)` function returns a value between -32767 to 32767 inclusive as specified in the [Linux Joystick API documentation](https://www.kernel.org/doc/Documentation/input/joystick-api.txt). The `getButtonState(int index)` returns either a True or a False depending if the specified button is being pressed or not.

### Joystick Event Object and Asynchronous Joystick State Retrieval

`LinuxJoystick` has a couple of callback points that can be used by the user to asynchronously check on the controller's states. This callback expects an implementation of the `LinuxJoystickEventCallback` interface in the library. The `LinuxJoystickEventCallback` interface has a single function that will need to be implemented: `public void callback(LinuxJoystick j, LinuxJoystickEvent ev)`. Use `setCallback(LinuxJoystickEvent)` to register the callback object that you have implemented. `LinuxJoystick` will perform the callback whenever an event has occured. Combined with the polling thread (or a user-defined thread that calls `poll()`), the user can implement an event handling mechanism that is asynchronous with the rest of the program.

In the callback function, the user can either use the provided reference to the `LinuxJoystick` instance to inspect the controller states, or the user can use the event reference itself to determine what has occured. The `LinuxJoystickEvent` class has three useful functions that can be used to determine a state change: `isAxisChanged()`, `isButtonDown()`, and `isButtonUp()`. Each of the function will return the index of the axis or the button that the event corresponds to. The functions will return `-1` if it is not the correct event. The README document for the library has an example code that takes advantage of this interface.

### Linux Joystick API Event Data Structure

The user can access the event fields as defined in [Linux Joystick API documentation](https://www.kernel.org/doc/Documentation/input/joystick-api.txt) directly through the event object. The functions to retrieve this information are `getTimestamp()`, `getValue()`, `getType()`, and `getNum()`. The user can also use the `getRaw()` function to get the actual 8-byte array of the event data. `LinuxJoystickEvent` assumes the host's endianness by default to decode the multi-byte fields. The user can force other endianness (e.g. to send the over the network) by setting the `LinuxJoystickEvent.ENDIANNESS` static variable to either `ByteOrder.LITTLE_ENDIAN` or `ByteOrder.BIG_ENDIAN`. `ByteOrder` is part of the `java.nio` package.

`LinuxJoystickEvent` also has a function to construct a 8-byte Linux joystick event packet: `construct(int timestamp, short value, byte type, byte num)`. This static function can be used to transmit a Linux Joystick API event over a serial channel. The function uses the `LinuxJoystickEvent.ENDIANNESS` field to determine the byte order.

## The JoyFactory Class