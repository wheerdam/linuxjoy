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

import java.nio.ByteOrder;

/**
 * The Linux joystick event structure. Refer to the kernel's joystick API for
 * data structure details in the kernel directory:
 *    Documentation/input/joystick-api.txt
 *
 * @author wira
 */
public class LinuxJoystickEvent {
    /**
     * Byte order for raw data. Uses the native byte order the JVM is running
     * on as default
     */
    public static ByteOrder ENDIANNESS = ByteOrder.nativeOrder();

    private int timestamp;
    private short value;
    private byte type;
    private byte num;
    private byte[] raw = new byte[8];

    public static final int BUTTON = 0x1;
    public static final int AXIS = 0x2;

    /**
     * The constructor will decode and populate the fields of the joystick
     * event
     *
     * @param ibuf the 8-byte data
     */
    public LinuxJoystickEvent(byte[] ibuf) {
        System.arraycopy(ibuf, 0, raw, 0, 8);

        if(ENDIANNESS == ByteOrder.LITTLE_ENDIAN) {
            timestamp = (int) ( ((int)ibuf[3] & 0xff)<<24 | ((int)ibuf[2] & 0xff)<<16 | ((int)ibuf[1] & 0xff)<<8 | ((int)ibuf[0] & 0xff) );
            value = (short) ( ((int)ibuf[5] & 0xff)<<8 | ((int)ibuf[4] & 0xff) );
        } else {
            timestamp = (int) ( ((int)ibuf[0] & 0xff)<<24 | ((int)ibuf[1] & 0xff)<<16 | ((int)ibuf[2] & 0xff)<<8 | ((int)ibuf[3] & 0xff) );
            value = (short) ( ((int)ibuf[4] & 0xff)<<8 | ((int)ibuf[5] & 0xff) );
        }

        type = ibuf[6];
        num = ibuf[7];
    }

    /**
     * Get raw data stream
     *
     * @return 8-byte Linux joystick event data as a byte array
     */
    public byte[] getRaw() {
        return raw;
    }

    /**
     * Get kernel timestamp of this event
     *
     * @return Timestamp as int in milliseconds
     */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     * Get 2-byte value from the event, could be a button press (1 or 0)
     * or an axis value (-32767 to 32767)
     *
     * @return Integer ranging from -32767 to 32767
     */
    public int getValue() {
        return value;
    }

    /**
     * Get event type, see kernel documentation for details. In short,
     * 0x1 is a button event and 0x2 is an axis event
     *
     * @return 1-byte value denoting the type of event
     */
    public byte getType() {
        return type;
    }

    /**
     * Get event number. This could be the number of the button as provided
     * by the driver or the axis number. Note that a "stick" input can
     * have two axes.
     *
     * @return 1-byte value denoting the event number
     */
    public byte getNum() {
        return num;
    }

    /**
     * Check if this event corresponds to a pressed button
     *
     * @return The numerical ID of the pressed button or -1 if it is not a
     * button press event
     */
    public int isButtonDown() {
        if(type == BUTTON && value == 1) {
            return num;
        }
        return -1;
    }

    /**
     * Check if this event corresponds to a released button
     *
     * @return The numerical ID of the released button, or -1 if it is not a
     * button release event
     */
    public int isButtonUp() {
        if(type == BUTTON && value == 0) {
            return num;
        }
        return -1;
    }

    /**
     * Check if this event corresponds to a change in an axis
     *
     * @return The numerical ID of the changed axis, or -1 if it is not an
     * axis change event
     */
    public int isAxisChanged() {
        if(type == AXIS) {
            return num;
        }
        return -1;
    }
    
    /**
     * An utility function that can be used to construct an 8-byte Linux
     * joystick event packet given the field values
     *
     * @param timestamp 4-byte timestamp value
     * @param value 2-byte event value (axis value or button state)
     * @param type 1-byte event type
     * @param num 1-byte event number (axis or button numerical ID)
     * @return packed 8-byte serial data
     */
    public static byte[] constructPacket(int timestamp, short value, byte type, byte num) {
        byte[] data = new byte[8];

        if(ENDIANNESS == ByteOrder.LITTLE_ENDIAN) {
            data[0] = (byte) (timestamp & 0xff);
            data[1] = (byte) ((timestamp>>8) & 0xff);
            data[2] = (byte) ((timestamp>>16) & 0xff);
            data[3] = (byte) ((timestamp>>24) & 0xff);
            data[4] = (byte) (value & 0xff);
            data[5] = (byte) ((value>>8) & 0xff);
        } else {
            data[3] = (byte) (timestamp & 0xff);
            data[2] = (byte) ((timestamp>>8) & 0xff);
            data[1] = (byte) ((timestamp>>16) & 0xff);
            data[0] = (byte) ((timestamp>>24) & 0xff);
            data[5] = (byte) (value & 0xff);
            data[4] = (byte) ((value>>8) & 0xff);
        }

        data[6] = type;
        data[7] = num;

        return data;
    }

    @Override
    public String toString() {
        return String.format("ljs event: %08X %04X %02X %02X", timestamp, value, type, num);
    }
}
