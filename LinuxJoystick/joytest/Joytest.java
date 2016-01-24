import org.bbi.linuxjoy.*;

class Joytest {
	public static void main(String args[]) {
		LinuxJoystick j = new LinuxJoystick(args[0], XboxController.BUTTONS, XboxController.AXES);
		if(!j.isDeviceOpen()) {
			System.exit(-1);
		}

		j.setCallback(new Callback());
		j.setCloseCallback(new CloseCallback());
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

	static class CloseCallback implements LinuxJoystickEventCallback {
		public void callback(LinuxJoystick j, LinuxJoystickEvent ev) {
			System.out.println("Device closed.");
			System.exit(0);
		}
	}
}
