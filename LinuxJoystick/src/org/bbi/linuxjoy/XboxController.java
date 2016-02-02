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

/**
 * A nice LinuxJoystick wrapper for Xbox controllers
 *
 * @author wira
 */
public class XboxController {
    private static double deadZone = 0;

    public static final int A = 0;
    public static final int B = 1;
    public static final int X = 2;
    public static final int Y = 3;
    public static final int LS = 4;
    public static final int RS = 5;
    public static final int BACK = 6;
    public static final int START = 7;
    public static final int LOGO = 8;
    public static final int LTHUMB = 9;
    public static final int RTHUMB = 10;

    public static final int LTHUMB_X = 0;
    public static final int LTHUMB_Y = 1;
    public static final int RTHUMB_X = 3;
    public static final int RTHUMB_Y = 4;
    public static final int D_X = 6;
    public static final int D_Y = 7;
    public static final int LT = 2;
    public static final int RT = 5;

    public static final int BUTTONS = 10;
    public static final int AXES = 8;

    public static final int NOTHING = -1;

    public static void setDeadZone(double z) {
        deadZone = z;
    }

    // button mapping
    public static boolean A(LinuxJoystick j)		{ return j.getButtonState(0);    }
    public static boolean B(LinuxJoystick j)		{ return j.getButtonState(1);    }
    public static boolean X(LinuxJoystick j)		{ return j.getButtonState(2);    }
    public static boolean Y(LinuxJoystick j)		{ return j.getButtonState(3);    }
    public static boolean LS(LinuxJoystick j)		{ return j.getButtonState(4);    }
    public static boolean RS(LinuxJoystick j)		{ return j.getButtonState(5);    }
    public static boolean BACK(LinuxJoystick j)		{ return j.getButtonState(6);    }
    public static boolean START(LinuxJoystick j)	{ return j.getButtonState(7);    }
    public static boolean LOGO(LinuxJoystick j)		{ return j.getButtonState(8);    }
    public static boolean LTHUMB(LinuxJoystick j)	{ return j.getButtonState(9);    }
    public static boolean RTHUMB(LinuxJoystick j)	{ return j.getButtonState(10);   }

    // axes
    public static double LTHUMB_X(LinuxJoystick j) { // negative is left, positive is right
        double v = (double) j.getAxisState(0) / 32767;
        if(v > deadZone || v < -deadZone) {
            return v;
        } else {
            return 0;
        }
    }

    public static double LTHUMB_Y(LinuxJoystick j) { // negative is forward, positive is backward
        double v = (double) j.getAxisState(1) / 32767.0;
        if(v > deadZone || v < -deadZone) {
            return v;
        } else {
            return 0;
        }
    }

    public static double RTHUMB_X(LinuxJoystick j) { // negative is left, positive is right
        double v = (double) j.getAxisState(3) / 32767.0;
        if(v > deadZone || v < -deadZone) {
            return v;
        } else {
            return 0;
        }
    }

    public static double RTHUMB_Y(LinuxJoystick j) { // negative is forward, positive is backward
        double v = (double) j.getAxisState(4) / 32767.0;
        if(v > deadZone || v < -deadZone) {
            return v;
        } else {
            return 0;
        }
    }

    public static double D_X(LinuxJoystick j) { // negative is left, positive is right
        double v = (double) j.getAxisState(6) / 32767.0;
        if(v > deadZone || v < -deadZone) {
            return v;
        } else {
            return 0;
        }
    }

    public static double D_Y(LinuxJoystick j) { // negative is forward, positive is backward
        double v = (double) j.getAxisState(7) / 32767.0;
        if(v > deadZone || v < -deadZone) {
            return v;
        } else {
            return 0;
        }
    }

    public static double LT(LinuxJoystick j) { // zero is rest, 1.0 is full pressed
        double v = (double) (j.getAxisState(2)+32767.0) / (32767*2);
        if(v > deadZone) {
            return v;
        } else {
            return 0;
        }
    }

    public static double RT(LinuxJoystick j) { // zero is rest, 1.0 is full pressed
        double v = (double) (j.getAxisState(5)+32767.0) / (32767*2);
        if(v > deadZone) {
            return v;
        } else {
            return 0;
        }
    }
    
    public static void printHeader() {
        System.out.println("A  B  X  Y  LB RB BK ST LG LS RS  LTHUMB_X  LTHUMB_Y  RTHUMB_X  RTHUMB_Y  D_X   D_Y   LT    RT");
    }

    public static void printValues(LinuxJoystick j) {
        String l = (A(j) ? "V" : " ") + "  " +
                   (B(j) ? "V" : " ") + "  " +
                   (X(j) ? "V" : " ") + "  " +
                   (Y(j) ? "V" : " ") + "  " +
                   (LS(j) ? "V" : " ") + "  " +
                   (RS(j) ? "V" : " ") + "  " +
                   (BACK(j) ? "V" : " ") + "  " +
                   (START(j) ? "V" : " ") + "  " +
                   (LOGO(j) ? "V" : " ") + "  " +
                   (LS(j) ? "V" : " ") + "  " +
                   (RS(j) ? "V" : " ") + "  " +
                   "";

        l += String.format("% .2f ", LTHUMB_X(j)) +
             String.format("% .2f ", LTHUMB_Y(j)) +
             String.format("% .2f ", RTHUMB_X(j)) +
             String.format("% .2f ", RTHUMB_Y(j)) +
             String.format("% .2f ", D_X(j)) +
             String.format("% .2f ", D_Y(j)) +
             String.format("% .2f ", LT(j)) +
             String.format("% .2f ", RT(j)) +
                "";

        System.out.println(l);
    }
}
