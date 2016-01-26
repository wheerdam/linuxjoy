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

package joymonitor;

import org.bbi.linuxjoy.*;

/**
 *
 * @author wira
 */
public class JoyEventCallback implements LinuxJoystickEventCallback {
    private MonitorCanvas c;
    private MonitorWindow w;
    
    public JoyEventCallback(MonitorWindow w, MonitorCanvas c) {
        this.c = c;
        this.w = w;
    }

    public void callback(LinuxJoystick j, LinuxJoystickEvent ev) {
        // a LinuxJoystick.close() callback
        if(ev == null) {
            w.closeCallback();
            return;
        }

        w.appendStreamOutput(ev);

        switch(ev.getType()) {
            case LinuxJoystickEvent.BUTTON:
                c.setButtonValue(ev.getNum(), ev.getValue() == 1);
                break;
            case LinuxJoystickEvent.AXIS:
                c.setAxisValue(ev.getNum(), ev.getValue());
                break;
        }
        c.repaint();
    }
}
