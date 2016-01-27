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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

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
	private native boolean closeNativeDevice(int index);

	/**
	 * Set a native property for the library. This is a very non-portable
	 * function that should only be used for troubleshooting.
	 *
	 * @param index Library-defined index
	 * @param key Library-defined property key
	 * @param val Library-defined property value
	 * @return the native function may return a byte array
	 */
	public static native byte[] setNativeProperty(int index, int key, int val);

	/**
	 * Get a version information from the native library. Use this function
	 * to check for dynamic linking as this function should not change the
	 * state of the library data structures.
	 */
	public static native String getVersionString();
	
	private static boolean LINK_SATISFIED;
	private static String linkErrorStackTrace;
	
	private static final int MAX_BUTTONS = 64;
	private static final int MAX_AXES = 64;

	static {
		try {
			System.loadLibrary("njnative");
			LINK_SATISFIED = true;
			linkErrorStackTrace = "No error";
		} catch(UnsatisfiedLinkError e) {
			System.err.println("NoJoy: unable to link with njnative library. " +
				"Native device functionality will be unavailable.");
			LINK_SATISFIED = false;
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			linkErrorStackTrace = sw.toString();	
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

	/**
	 * Get the stack trace of the link error if it has occured.
	 *
	 * @return Stack trace for the link error as string
	 */
	public static String getLinkErrorString() {
		return linkErrorStackTrace;
	}

	@Override
	protected boolean channelOpen() {
		deviceIndex = Integer.parseInt(path);
	
		if(LINK_SATISFIED && openNativeDevice(deviceIndex)) {
			return true;
		} else {
			System.err.println(this + ": native device unavailable");
			return false;
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
			close();
			return 0;
		}
	}

	@Override
	public void channelClose() {
		if(LINK_SATISFIED) {
			if(!closeNativeDevice(deviceIndex)) {
				System.err.println(this + ": native device close error");
			} else {
				System.out.println(this + ": native device closed");
			}
		}
	}

	@Override
	public String toString() {
		return "NoJoy(" + deviceIndex + ")";
	}
}
