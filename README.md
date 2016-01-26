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
	j.startPollingThread(5); // sleep for 5 ms between polls
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

To close the device, call the following function:
```java
j.close(); // will also stop the polling thread
```

Any blocking `poll()` will be interrupted when `close()` is called.

## Building the Library

You will need a JDK and `ant` to build the library. If they're configured properly, you can just go into the LinuxJoystick directory and run `ant jar`. The compiled library will be in the `LinuxJoystick/dist` directory. You can package LinuxJoystick with your own program however you see fit. You just have to make sure that `LinuxJoystick.jar` or its contents are in your classpath when compiling and running your program.

## Device Enumeration

To get a list of devices that the library detects, do the following:

```java
int[] joyInfo = JoyFactory.enumerate();
if(joyInfo != null) {
	// iterate through the array
}
```

The joystick information has the following encoding for each byte:

|  3  |        2       |         1         |  0   |
|:---:|:--------------:|:-----------------:|:----:|
|  -  | Number of Axes | Number of Buttons |  ID  |

`JoyFactory.enumerate()` will return `null` if no joysticks were found. It will return a value of -1 for a specific entry if it was unable to gather information about that particular controller. Use `JoyFactory.get(index)` to get a `LinuxJoystick` handle to the device and start jamming. If you do not want to enumerate the devices manually and just want to get the first device `JoyFactory` can detect and use, use the `JoyFactory.getFirstUsableDevice()` as in the previous example code to get your joystick handle. This function will return `null` if it can not find any device to use.

## Other Platforms

LinuxJoystick has a native library interface and a Windows implementation to access Xbox controllers. The `JoyFactory` class hides the native implementation which allows your Java programs that use LinuxJoystick to be, at least ostensibly, a cross-platform program (no different codepaths for different platforms).

Note: the `poll()` function of the native library is non-blocking and will return immediately. As long as your Java program uses the polling thread, this should be a minor issue other than an increase in CPU usage.

The native library development files are in the `LinuxJoystick/native` directory. `org_bbi_linuxjoy_NoJoy.h` is the header that has the JNI function signatures of the Java native functions. `winxinput.cpp` is the C++ source that implements this interface and uses Windows XInput API to access the game controllers. A Visual Studio 2015 solution is provided in `LinuxJoystick/native/njnative` to build the `njnative.dll` Windows library.

### Running LinuxJoystick with Native Library

Make sure the native library is in your Java library path (`java.library.path` property). For example, if your Java program, the LinuxJoystick library, and the Windows native library (`njnative.dll`) are in the same directory, you can use the following command to run your program in Windows:

```
java -cp LinuxJoystick.jar;YourProgram.jar -Djava.library.path=. yourpackage.YourProgram
```

Or if your program is a single class file (e.g. `YourProgram.class`):

```
java -cp LinuxJoystick.jar;. -Djava.library.path=. YourProgram
```

### Force Use Native Library

You can force LinuxJoystick to use the native library in your Java program by omitting JoyFactory and using the native interface directly. Instead of using `JoyFactory.enumerate()` and `JoyFactory.get(index)` to enumerate and get a handle to your device, you use `NoJoy.getEnumeration()` and `LinuxJoystick j = new NoJoy(index)`. `NoJoy` is a subclass of `LinuxJoystick` that uses the native library functions to read the device data instead of the `FileChannel` implementation that the superclass uses in Linux.
