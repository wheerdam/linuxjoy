/*
	Copyright 2016 Wira Mulia

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

package org.bbi.linuxjoy;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

/**
 * Platform-dependent handlers
 *
 * @author wira
 */
public class JoyFactory {
	private static LinuxJoystick[] jjs;

	public static final int NOT_OPEN = -1;
	public static final int GENERIC = 0x00;
	public static final int XINPUT = 0x01;
	
	public static boolean ALWAYS_USE_NATIVE = false;

	/**
	 * Enumerate all the joystick devices on the platform and instantiate
	 * a LinuxJoystick handle for each of them, if possible.
	 *
	 * @return An array of information integer with the following format:<br />
	 * ByteOffset_Value <br />
	 * 3_________ nothing <br />
	 * 2_________ Number of axes <br />
	 * 1_________ Number of buttons <br />
	 * 0_________ Some sort of identification as defined by the constants
	 */
	public static int[] enumerate() {
		int joyInfo[] = null;
		int i;

		if(!ALWAYS_USE_NATIVE && System.getProperty("os.name").toLowerCase().equals("linux")) {
			String jf[];

			// if it's linux, we look at /dev/input/js*
			File f = new File("/dev/input");
			jf = f.list(new FilenameFilter() {
				public boolean accept(File f, String n) {
					return n.startsWith("js");
				}
			});

			// no joystick found
			if(jf.length == 0) {
				System.out.println("JoyFactory.enumerate: no joysticks found");
				return null;
			}

			// create a LinuxJoystick handle for each device
			jjs = new LinuxJoystick[jf.length];
			joyInfo = new int[jf.length];
			Arrays.sort(jf);
			for(i = 0; i < jf.length; i++) {
				// use temporary number of buttons and axes
				jjs[i] = new LinuxJoystick(f.getAbsolutePath() + "/" + jf[i], 64, 64); 
				if(jjs[i].isDeviceOpen()) {
					// the linux joystick driver always puts out initial state events
					// that we can use to determine the number of axes and buttons
					LinuxJoystickInit init = new LinuxJoystickInit();
					jjs[i].setCallback(init);
					jjs[i].poll();
					jjs[i].setCallback(null); // free the callback
					jjs[i].setButtonsAxes(init.getButtons(), init.getAxes());

					joyInfo[i] = 0; // generic controller
					joyInfo[i] |= (init.getButtons() << 8) & 0x00ff00;
					joyInfo[i] |= (init.getAxes() << 16) & 0xff0000;

					System.out.println("JoyFactory enumerate(" + i + "): " +
							init.getButtons() + " buttons and " +
							init.getAxes() + " axes.");

				} else {
					// we failed to open this device
					joyInfo[i] = NOT_OPEN;
				}
			}
		} else {
			// let's ask NoJoy to enumerate some native devices for us, if it can
			joyInfo = NoJoy.getEnumeration();
			if(joyInfo == null || joyInfo.length == 0) {
				System.out.println("JoyFactory.enumerate: no joysticks found");
				return null;
			}
			
			jjs = new LinuxJoystick[joyInfo.length];
			for(i = 0; i < joyInfo.length; i++) {
				jjs[i] = new NoJoy(i);
				jjs[i].setButtonsAxes(BUTTONS(joyInfo[i]), AXES(joyInfo[i]));
			}
		}

		return joyInfo;
	}

	/**
	 * Get the handle for the specified joystick device
	 *
	 * @param index Enumerated index
	 * @return Reference to the joystick device
	 */
	public static LinuxJoystick get(int index) {
		if(jjs == null) {
			System.err.println("JoyFactor: devices have not been enumerated");
			return null;
		} else if(index < 0 || index >= jjs.length) {
			return null;
		}

		return jjs[index];
	}

	/**
	 * Get an open joystick. Will enumerate if the devices have not been
	 * enumerated yet
	 *
	 * @return Handle to first usable joystick, null if there is none
	 */
	public static LinuxJoystick getFirstUsableDevice() {
		if(jjs == null) {
			if(enumerate() == null) {
				return null;
			}
		}

		for(int i = 0; i < jjs.length; i++) {
			if(jjs[i].isDeviceOpen()) {
				return jjs[i];
			}
		}

		return null;
	}

	/**
	 * Return number of buttons for the specified joystick information
	 *
	 * @param joyinfo Joystick information integer
	 * @return Number of buttons
	 */
	public static int BUTTONS(int joyinfo) {
		return (joyinfo >> 8) & 0xff;
	}

	/**
	 * Return number of axes for the specified joystick information
	 *
	 * @param joyinfo Joystick information integer
	 * @return Number of axes
	 */
	public static int AXES(int joyinfo) {
		return (joyinfo >> 16) & 0xff;
	}

	/**
	 * Return the device ID for the specified joystick information
	 *
	 * @param joyinfo Joystick information integer
	 * @return Device ID
	 */
	public static int ID(int joyinfo) {
		return joyinfo & 0xff;
	}

	static class LinuxJoystickInit implements LinuxJoystickEventCallback {
		private int buttons;
		private int axes;

		public LinuxJoystickInit() {
			buttons = 0;
			axes = 0;
		}

		public void callback(LinuxJoystick j, LinuxJoystickEvent ev) {
			if((ev.getType() & 0xf) == 1) {
				buttons++;
			} else if((ev.getType() & 0xf) == 2) {
				axes++;
			}
		}

		public int getButtons() {
			return buttons;
		}

		public int getAxes() {
			return axes;
		}
	}
}
