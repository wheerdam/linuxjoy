# linuxjoy

LinuxJoystick is a Java joystick input library. The library provides Joystick input device enumeration function, device polling, and an event handling mechanism. Contrary to its name, LinuxJoystick can be used in other platforms without having to modify the user Java program. This is accomplished using native libraries. LinuxJoystick will function with just the JRE in Linux unless the user prefers an in-house native implementation.

The following is an example code that uses the library:

```java
import org.bbi.linuxjoy.*;
```

Initialization and using the provided polling thread:
```java
LinuxJoystick j = JoyFactory.getFirstUsableDevice();
if(j != null) {
	j.setCallback(new EventCallbackHandler());
	j.startPollingThread(5);
}
```

Callback handler class with a function that is called whenever a joystick event has occured:
```java
class EventCallbackHandler implements LinuxJoystickEventCallback {
	public void callback(LinuxJoystick j, LinuxJoystickEvent ev) {
		switch(ev.isAxisChanged()) {
			case 0:
				// handle axis 0 change with j.getAxisState(0)
				break;
			case 1:
				// handle axis 1 change with j.getAxisState(1)
				break;
		}

		switch(ev.isButtonDown()) {
			case 0:
				// handle button 0 press here
				break;
			case 1:
				// handle button 1 press here
				break;
		}
		
		switch(ev.isButtonUp()) {
			case 1:
				// handle button 1 release here
				break;
		}
	}
}
```

An alternative to using the callback handler is to write your own polling thread by calling the `poll()` function of LinuxJoystick and checking the axis and button states immediately after the poll. The `poll()` function is a blocking function when no native library is being used.

To close the device, call the following functions:
```java
j.stopPollingThread(); // if being used
j.close();
```

Any blocking `poll()` will be interrupted when `close()` is called.

## Building the Library

You will need a JDK and `ant` to build the library. If they're configured properly, you can just go into the LinuxJoystick directory and run `ant jar`. The compiled library will be in the `LinuxJoystick/dist` directory.

## Other Platforms

A native library for Windows is available. This native library uses the XInput API to interface with any Xbox controllers connected to the Windows machine. The JoyFactory class hides the native implementation which allows your Java programs that use LinuxJoystick to be, at least ostensibly, a cross-platform program (no different codepaths for different platforms).

Note: the poll() function of the native library is non-blocking and will return immediately. As long as your Java program uses the polling thread, this should be a minor issue other than an increase in CPU usage.

The native library development files are in the `LinuxJoystick/native` directory. `org_bbi_linuxjoy_NoJoy.h` is the header that has the JNI function signatures of the Java native functions. `winxinput.cpp` is the C++ source that implements this interface and uses Windows XInput API to access the game controllers. A Visual C++ 2015 solution is provided in `LinuxJoystick/native/njnative` to build the DLL.