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
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;

/**
 * LinuxJoystick is a class that uses the Linux joystick API and presents a
 * nice interface to be used in Java applications. The class uses Java's
 * new I/O system to support asynchronous operation.
 *
 * @author wira
 */
public class LinuxJoystick {
	/**
	 * The data source I/O channel. Used by the channelOpen() and channelRead()
	 * functions to open and read the data source. Subclasses overriding the
	 * specified functions will have to cast this object to the appropriate
	 * channel implementation. The default implementation uses FileChannel
	 */
	protected AbstractInterruptibleChannel fc;

	/**
	 * Path to the data source. This path refers to the joystick device file
	 * for the default implementation.
	 */
	protected String path;

	/**
	 * Internal read buffer. This buffer is filled by the channelRead() function
	 */
	protected ByteBuffer buf = ByteBuffer.allocate(8192);

	/**
	 * A flag that signifies whether the device is still open and ready for reading.
	 * Any part of the program that determines that the device is stale should set
	 * this flag to false.
	 */
	protected boolean deviceOpen;
	
	private byte[] ibuf = new byte[8];
	private int ibuf_index = 0;
	private int nread, n;
		
	private boolean stateChanged;

	private boolean[] buttonStates;
	private int[] axisStates;

	private int threadSleepMs;
	private PollingThread pt;

	private LinuxJoystickEventCallback callback;
	private LinuxJoystickEventCallback closeCallback;

	/**
	 * Create a new Joystick object
	 *
	 * @param path Path to the system device file, typically in /dev/input
	 * @param buttons Number of buttons that the controller has
	 * @param axes Number of axes that the controller has
	 */
	public LinuxJoystick(String path, int buttons, int axes) {
		int i;
		deviceOpen = false;
		stateChanged = false;
		threadSleepMs = 5;
		this.path = path;

		buttonStates = new boolean[buttons];
		axisStates = new int[axes];

		for(i = 0; i < buttons; i++) {
			buttonStates[i] = false;
		}

		for(i = 0; i < axes; i++) {
			axisStates[i] = 0;
		}
	}

	/**
	 * Start a polling thread that will periodically read the controller
	 * device file and update the current states
	 *
	 * @param sleepMs Polling interval in milliseconds
	 */
	public void startPollingThread(int sleepMs) {
		if(pt != null) {
			System.out.println(this + ": polling thread is already running");
		} else {
			if(!deviceOpen) {
				open();
			}
			threadSleepMs = sleepMs;
			System.out.println(this + ": polling thread with " + threadSleepMs + " ms interval started");
			pt = new PollingThread();
			pt.start();
		}
	}

	/**
	 * Stop the polling thread. Note that this will also close the file and
	 * the open function will need to be called again
	 */
	public void stopPollingThread() {
		pt.stopPolling();
		pt = null;
	}

	/**
	 * Open the joystick device given by the path. Override channelOpen() if
	 * data source is not coming from a file.
	 */
	public final void open() {
		if(deviceOpen) {
			close();
		}

		buttonStates = new boolean[buttonStates.length];
		axisStates = new int[axisStates.length];
		nread = 0;
		n = 0;

		deviceOpen = channelOpen();
	}

	/**
	 * Check if this joystick object currenty has a joystick device open
	 *
	 * @return True if a channel is currently open, false otherwise
	 */
	public boolean isDeviceOpen() {
		return deviceOpen;
	}

	/**
	 * Close the device file. Running this function while the polling thread
	 * is running will stop the thread
	 */
	public void close() {
		if(pt != null) {
			pt.stopPolling();
			pt = null;
		}

		channelClose();
		deviceOpen = false;

		if(closeCallback != null) {
			closeCallback.callback(this, null);
		}
	}

	/**
	 * Do a blocking read on the joystick. Use the close function to close
	 * the channel and interrupt the read
	 */
	private void read() {
		if(!deviceOpen) {
			System.err.println("ljs: input file is not open");
			return;
		}

		try {
			if(nread == n) { // buffer empty, we're not dealing with a torn read
				nread = 0;
				buf.clear();
				n = channelRead();
			}
			while(nread != n && n != -1) {
				// buffer indexes dump debug
				//System.out.println(String.format("n: %d nread: %d ibuf_index: %d", n, nread, ibuf_index));

				if((n-nread) >= (8-ibuf_index)) {
					System.arraycopy(buf.array(), nread, ibuf, ibuf_index, 8-ibuf_index);

					LinuxJoystickEvent ev = new LinuxJoystickEvent(ibuf);

					if((ev.getType() & 0xf) == LinuxJoystickEvent.BUTTON) {
						if(ev.getNum() < buttonStates.length) {
							updateButtonState(ev.getNum(), (ev.getValue() == 1));
							stateChanged = true;
						}
					} else if((ev.getType() & 0xf) == LinuxJoystickEvent.AXIS) {
						if(ev.getNum() < axisStates.length) {
							updateAxisState(ev.getNum(), ev.getValue());
							stateChanged = true;
						}
					}
					
					nread += (8-ibuf_index);
					ibuf_index = 0;  // we have one complete event
					if(callback != null) {
						callback.callback(this, ev);
					}

				// in case the data is torn between reads, save our internal
				// buffer position
				} else {
					System.arraycopy(buf.array(), nread, ibuf, ibuf_index, n-nread);
					ibuf_index += (n-nread);
					nread += (n-nread)%8;
				}
			}
		} catch(IOException ioe) {
			System.err.println(this + ": read exception, closing");
			close();
		}
	}

	/**
	 * The channel read routine. This defaults to reading from a file.
	 * This function can be overridden if other channel types are desired.
	 *
	 * @return Number of bytes read
	 * @throws IOException I/O exception is thrown from this function
	 */
	protected int channelRead() throws IOException {
		return ((FileChannel)fc).read(buf);
	}

	/**
	 * The channel open routine. By default, the channel is implemented as a
	 * FileChannel. Override this function if other data input type is needed.
	 *
	 * @return True if open is successful, false otherwise
	 */
	protected boolean channelOpen() {
		try {
			RandomAccessFile f = new RandomAccessFile(path, "r");
			this.fc = f.getChannel();
			System.out.println(this + ": initialized");
			return true;
		} catch(FileNotFoundException fnfe) {
			System.err.println(this + ": file not found: " + path);
			fc = null;
			return false;
		}
	}

	/**
	 * The channel close routine. Override this function if other input type
	 * is needed and it is not a channel
	 */
	protected void channelClose() {
		try {
			if(fc != null) {
				fc.close();
			}
		} catch(IOException ioe) {
			System.err.println("ljs: close I/O exception: " + ioe.getMessage());
		}

		fc = null;
	}

	/**
	 * Poll the device. This is a blocking operation. Use the polling thread
	 * for asynchronous operation.
	 */
	public void poll() {
		if(pt != null) {
			System.err.println(this + ": a polling thread is running, don't use poll()");
			return;
		}

		read();
	}

	/**
	 * Specify a callback object to execute after an event occurs. Note that
	 * the joystick states are updated before the callback is performed, so
	 * getButtonState and getAxisState will return the latest value. Pass
	 * a "null" object to unregister the callback.
	 *
	 * @param cb Reference to callback object.
	 */
	public void setCallback(LinuxJoystickEventCallback cb) {
		this.callback = cb;
	}

	/**
	 * Specify a callback object to execute when the joystick device is closed
	 *
	 * @param cb Reference to callback object
	 */
	public void setCloseCallback(LinuxJoystickEventCallback cb) {
		this.closeCallback = cb;
	}

	/**
	 * Get the state of the button given by the index
	 *
	 * @param index The button index value
	 * @return True if the button is being pressed, false otherwise
	 */
	public synchronized boolean getButtonState(int index) {
		return buttonStates[index];
	}

	/**
	 * Get the state of the axis given by the index
	 *
	 * @param index The axis index value
	 * @return A number between -2^16 to 2^16-1 that represents the axis value
	 */
	public synchronized int getAxisState(int index) {
		return axisStates[index];
	}

	private synchronized void updateButtonState(int index, boolean v) {
		buttonStates[index] = v;
	}

	private synchronized void updateAxisState(int index, int v) {
		axisStates[index] = v;
	}

	/**
	 * Give the total number of buttons that this joystick object was defined
	 * with
	 *
	 * @return Number of buttons
	 */
	public int getNumButtons() {
		return buttonStates.length;
	}

	/**
	 * Give the total number of axes that this joystick object was defined with
	 *
	 * @return Number of axes
	 */
	public int getNumAxes() {
		return axisStates.length;
	}

	/**
	 * Set new number of buttons and axes. Should not need to do this often,
	 * only if the program needs to determine the number of buttons and axes
	 * immediately after instantiation
	 *
	 * @param buttons Number of buttons that the controller has
	 * @param axes Number of axes that the controller has
	 */
	public synchronized void setButtonsAxes(int buttons, int axes) {
		buttonStates = new boolean[buttons];
		axisStates = new int[axes];
	}

	/**
	 * Check if there has been a change in state since the last poll
	 *
	 * @return True if the controller state has changed, false otherwise
	 */
	public boolean isChanged() {
		if(stateChanged) {
			stateChanged = false;
			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		return "LinuxJoystick" + (deviceOpen ? "(" + path + ")" : "");
	}

	class PollingThread extends Thread {
		private boolean stop = false;

		@Override
		public void run() {
			if(!deviceOpen)
				return;

			//System.out.println("ljs(" + path + "): polling thread is running");

			while(!stop) {
				if(!deviceOpen) {
					stop = true;
				}

				try {
					read();
					Thread.sleep(threadSleepMs);
				} catch(Exception e) {

				}
			}
			//System.out.println("ljs" + (fr != null ? "(" + path + ")" : "") + ": polling thread exiting");
		}

		public void stopPolling() {
			stop = true;
		}
	}
}
