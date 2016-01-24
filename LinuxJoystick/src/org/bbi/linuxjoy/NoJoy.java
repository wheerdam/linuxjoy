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

import org.bbi.linuxjoy.hacks.DummyInterruptibleChannel;
import java.io.IOException;

/**
 * NoJoy is a native implementation interface for LinuxJoystick. This
 * interface can be used for other platforms other than Linux via JNI
 * native library. The library name is "njnative" (njnative.dll in
 * Windows and libnjnative.so in *nixes)
 *
 * @author wira
 */
public class NoJoy extends LinuxJoystick {
	private native byte[] nativePoll(int index);
	private native boolean openNativeDevice(int index);
	private native boolean isNativeDeviceOpen(int index);
	private static native int[] enumerate();
	private native void closeNativeDevice(int index);

	/**
	 * Set a native property for the library. This is a very non-portable
	 * function that should only be used for troubleshooting.
	 *
	 * @param index Library-defined index
	 * @param key Library-defined property key
	 * @param value Library-defined property value
	 */
	public static native void setNativeProperty(int index, int key, int val);

	/**
	 * Get a version information from the native library. Use this function
	 * to check for dynamic linking as this function should not change the
	 * state of the library data structures.
	 */
	public static native String getVersionString();
	
	private static boolean LINK_SATISFIED;
	
	private static final int MAX_BUTTONS = 64;
	private static final int MAX_AXES = 64;

	static {
		try {
			System.loadLibrary("njnative");
			LINK_SATISFIED = true;
		} catch(UnsatisfiedLinkError e) {
			System.err.println("NoJoy: unable to link with njnative library. " +
				"Native device functionality will be unavailable.");
			LINK_SATISFIED = false;
		}
	}

	private int deviceIndex;

	/**
	 * All native joysticks are identified by a numerical ID. The path string
	 * will be parsed on open
	 *
	 * @param index Numerical ID of the device
	 */
	public NoJoy(int index) {
		super(String.valueOf(index), MAX_BUTTONS, MAX_AXES);		
	}

	/**
	 * Enumerate native devices. The native library MUST return an array of
	 * informative 32-bit data. Follow JoyFactory enumeration format.
	 *
	 * @return JoyFactory enumeration format native joystick info
	 */
	public static int[] getEnumeration() {
		if(!LINK_SATISFIED) {
			return null;
		}
	
		return enumerate();
	}

	@Override
	protected void channelOpen() {
		deviceIndex = Integer.parseInt(path);
	
		if(LINK_SATISFIED && openNativeDevice(deviceIndex)) {
			fc = new DummyInterruptibleChannel();
		} else {
			System.err.println(this + ": native device unavailable");
			fc = null;
		}
	}

	@Override
	protected int channelRead() {
		if(LINK_SATISFIED && isNativeDeviceOpen(deviceIndex)) {
			byte[] data = nativePoll(deviceIndex);	// native poll
			buf.clear();
			buf.put(data);			// and just dump it to our buffer
			return data.length;
		} else {
			fc = null;
			return 0;
		}
	}

	@Override
	public void channelClose() {
		try {
			if(fc != null) {
				fc.close();
			}
			if(LINK_SATISFIED) {
				closeNativeDevice(deviceIndex);
			}
		} catch(IOException ioe) {
			System.err.println(this + ": close I/O exception: " + ioe.getMessage());
		}

		fc = null;
	}

	@Override
	public String toString() {
		return "NoJoy(" + deviceIndex + ")";
	}
}
