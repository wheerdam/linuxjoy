import org.bbi.linuxjoy.*;

class JoyFactoryTest {
	public static void main(String args[]) {
		int[] joyInfo = JoyFactory.enumerate();
		if(joyInfo == null || joyInfo[0] == -1) {
			return;
		}
		LinuxJoystick j = JoyFactory.get(0);
        if(!j.isDeviceOpen()) {
            System.exit(-1);
        }

        j.setCallback(new Callback());
        XboxController.setDeadZone(0.12);
        j.startPollingThread(5);
        try {
            Thread.sleep(60000);
        } catch(Exception e) {

        }
        j.stopPollingThread();
        j.close();
    }

    static class Callback implements LinuxJoystickEventCallback {
        public void callback(LinuxJoystick j, LinuxJoystickEvent ev) {
            switch(ev.isAxisChanged()) {
                case XboxController.RT:
                    if(XboxController.RT(j) == 1.0) {
                        System.out.println("Full steam ahead!");
                    }
            }

            switch(ev.isButtonDown()) {
                case XboxController.LOGO:
                    System.out.println("The Xbox logo button is pressed, quitting.");
                    System.exit(0);
                case XboxController.A:
                    System.out.println("The 'A' button is pressed");
                    break;
            }

            switch(ev.isButtonUp()) {
                case XboxController.A:
                    System.out.println("The 'A' button is released");
                    break;
            }
        }
    }
}
