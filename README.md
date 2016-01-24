# linuxjoy
LinuxJoystick is a Java joystick input library. The library exposes device enumeration function, device polling, and event handling mechanism. The following is an example code that uses the library:

```java
import org.bbi.linuxjoy.*;
```

Initialization and using the provided polling thread:
```java
LinuxJoystick j = JoyFactory.getFirstUsableDevice();
j.setCallback(new EventCallbackHandler());
j.startPollingThread(5);
```

Callback handler class:
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
	}
}
```

An alternative to using the callback handler is to write your own polling thread by calling the poll() function of LinuxJoystick and checking the axis and button states directly after the poll. The poll() function is a blocking function when no native library is being used.

To close the device, call the following functions:
```java
j.stopPollingThread(); // if being used
j.close();
```

Any block poll() will be interrupted when close() is called.

## Other Platforms

A native library for Windows is available. This native library uses the XInput API to interface with any Xbox controllers connected to the Windows machine. The poll() function of the native library is non-blocking and will return immediately.
