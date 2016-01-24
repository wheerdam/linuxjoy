/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
