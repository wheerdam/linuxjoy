# LinuxJoystick API Documentation

This document describes the LinuxJoystick Library API. The description includes the `LinuxJoystick` and `JoyFactory` classes and documentation on how to extend `LinuxJoystick` to support more than the Linux Joystick API.

## The LinuxJoystick Class

`LinuxJoystick` is a Java class that can be used to interface with a Joystick device in Linux. The constructor for the class takes the path to the Joystick device file and the number of buttons and axes that the controller has. The following is a list of `LinuxJoystick` public functions that are to be used by user programs:


```java
public void poll()
public void open(String path, int buttons, int axes)
public void reset()
public void close()
public void setButtonsAxes(int buttons, int axes)

public boolean isDeviceOpen()
public int getNumButtons()
public int getNumAxes()
public int getAxisState(int index)
public int getButtonState(int index)
public boolean isChanged()

public void setCallback(LinuxJoystickEventCallback cb)
public void setCloseCallback(LinuxJoystickEventCallback cb)
public void startPollingThread(int interval_ms)
public void stopPollingThread()
```

The following is an example initialization of the class:


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

`LinuxJoystick` has a couple of callback points that can be used by the user to asynchronously check on the controller's states. This callback expects an implementation of the `LinuxJoystickEventCallback` interface in the library. The `LinuxJoystickEventCallback` interface has a single function that will need to be implemented: `callback(LinuxJoystick j, LinuxJoystickEvent ev)`. Use `setCallback(LinuxJoystickEventCallback)` to register the callback object that you have implemented. `LinuxJoystick` will perform the callback whenever an event has occured. Combined with the polling thread (or a user-defined thread that calls `poll()`), the user can implement an event handling mechanism that is asynchronous with the rest of the program.

In the callback function, the user can either use the provided reference to the `LinuxJoystick` instance to inspect the controller states, or the user can use the event reference itself to determine what has occured. The `LinuxJoystickEvent` class has three useful functions that can be used to determine a state change: `isAxisChanged()`, `isButtonDown()`, and `isButtonUp()`. Each of the function will return the index of the axis or the button that the event corresponds to. The functions will return `-1` if it is not the correct event. The README document for the library has an example code that takes advantage of this interface.

`LinuxJoystick` also has a `setCloseCallback(LinuxJoystickEventCallback)` function that sets a callback that gets called when the `close()` function is called. The event field of the `callback(LinuxJoystick j, LinuxJoystickEvent ev)` function will be `null` when the callback is performed. **Do not call `close()` within this callback** as this will cause a circular call that will overflow the call stack and crash the program.

### Linux Joystick API Event Data Structure

The user can access the event fields as defined in [Linux Joystick API documentation](https://www.kernel.org/doc/Documentation/input/joystick-api.txt) directly through the event object. The functions to retrieve this information are `getTimestamp()`, `getValue()`, `getType()`, and `getNum()`. The user can also use the `getRaw()` function to get the actual 8-byte array of the event data. `LinuxJoystickEvent` assumes the host's endianness by default to decode the multi-byte fields. The user can force other endianness (e.g. to send over the network) by setting the `LinuxJoystickEvent.ENDIANNESS` static variable to either `ByteOrder.LITTLE_ENDIAN` or `ByteOrder.BIG_ENDIAN`. `ByteOrder` is part of the `java.nio` package.

`LinuxJoystickEvent` also has a function to construct a 8-byte Linux joystick event packet: `construct(int timestamp, short value, byte type, byte num)`. This static function can be used to transmit a Linux Joystick API event over a serial channel. The function uses the `LinuxJoystickEvent.ENDIANNESS` field to determine the byte order.

## The JoyFactory Class

The `JoyFactory` class provides an enumeration and hides platform-specific procedures to access the controllers in the system. `JoyFactory` will enumerate and use `LinuxJoystick` directly if it detects that it is running in Linux. It will use the native library interface `NoJoy` otherwise.

### Enumeration

The `enumerate()` class will attempt to open all detected Joystick devices in order to gather information about them. The function will return an array of integers that describes the enumerated devices (or a `null` if it can't find any). The encoding of this information is as follows:

|  3  |        2       |         1         |  0   |
|:---:|:--------------:|:-----------------:|:----:|
|  -  | Number of Axes | Number of Buttons |  ID  |

`enumerate()` will fill a -1 (0xFFFFFFFF) for an entry if it can not find any information about the device. The user then can proceed to check for the ID to see if the controller is supported. The functions `JoyFactory.BUTTONS(int)` and `JoyFactory.AXES(int)` can be used to isolate the number of buttons and axes. The following are the IDs currently supported by `JoyFactory`:

| ID  | Name                              |
|:---:|-----------------------------------|
| 0   | Generic                           |
| 1   | Xbox 360 Controller               |

Use the `JoyFactory.get(int index)` function to get a `LinuxJoystick` reference of the desired controller.

**Note:** `JoyFactory` currently does not support device identifier look-up in Linux and will always return a generic ID. The buttons and axes will be correctly reported.

`JoyFactory` has the `getFirstUsableDevice()` function that will enumerate (if it has not been done already) and return a `LinuxJoystick` object of the first controller that has a valid ID to the caller. This function is useful if all the user wants to do is to just attempt to get a controller that is connected to the computer and go from there. The function will return `null` if there is no device that can be used.

### ALWAYS_USE_NATIVE

`JoyFactory` can be forced to always try to use the native library by setting the `JoyFactory.ALWAYS_USE_NATIVE` field to `true`.

## Extending LinuxJoystick

The `LinuxJoystick` class uses `FileChannel` to open and read the Linux joystick device. Few functions of the `LinuxJoystick` class can be overridden by a subclass so a different data source can be used to supply joystick data to the user program. The following are the `LinuxJoystick` class members that will be inherited and overridden that are crucial in the subclass:


```java
protected AbstractInterruptibleChannel fc;
protected String path;
protected ByteBuffer buf = ByteBuffer.allocate(8192);
protected boolean deviceOpen;

protected boolean channelOpen() { ... }
protected int channelRead() { ... }
protected void channelClose() { ... }
```

### Data Source Identifier and channelOpen()

`fc` is the channel object that is used by `LinuxJoystick` to open and read the Linux joystick device. If the user subclass does not use channels, this member can be ignored completely. 

The `path` member is the resource identifier (e.g. path to device file for `LinuxJoystick`). In some cases the subclass may only need a numerical identifier. If this is the case, the identifier will still need to be stored as a `String` in the constructor and then type casted to an integer when it is actually being used in the channel functions. `LinuxJoystick` does not have an empty constructor, so a `super` must be passed on in the subclass' constructor, e.g.:


```java
public LinuxJoystickSubclass(int identifier, int buttons, int axes) {
	super(String.valueOf(identifier), buttons, axes);
}
```

The subclass then can override the `channelOpen()` function to actually open the data source identified by `path`. `channelOpen()` will need to return `true` if the data source is successfully opened and `false` otherwise. Returning `false` from `channelOpen()` will cause the `deviceOpen` member to be set as false, signifying to the rest of the class that this joystick reference is not open.

### Data Read and channelRead()

`buf` is an 8KB buffer that is used by `LinuxJoystick` to process the event data. The subclass will need to fill this buffer by overriding the `channelRead()` function. `channelRead()` also needs to return the number of bytes that were read. The subclass can call a `close()` from `channelRead()` if it determines that the device has been closed / becomes unavailable while reading. The subclass' implementation of `channelRead()` **must** follow the Linux Joystick API packets when filling this buffer (it's why the library is called LinuxJoystick after all). Please mind the machine's endianness when serializing the data. Check for `LinuxJoystickEvent.ENDIANNESS` to figure out what endian the rest of the framework will use to decode the data or use the `LinuxJoystickEvent.construct(int timestamp, short value, byte type, byte num)` function.

If the subclass' `channelRead()` is a blocking operation, **it must be interruptible by `channelClose()`**. Alternatively, making `channelRead()` non-blocking is safe to do and will not break the rest of the `LinuxJoystick` framework. The function will just have to make sure that it returns a zero if it did not read in any data. `LinuxJoystick` can also handle torn reads, i.e. a packet split into two or more `channelRead()` calls, so the subclass implementation can always immediately return with whatever it currently has.

### Closing the Device

Finally, the subclass will need to override `channelClose()` to clean up resources when the user determines that the input device is no longer needed.

