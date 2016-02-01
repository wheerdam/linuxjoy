import org.bbi.linuxjoy.*;

// remote joystick data source using SocketJoystick
class JoyRemote {
	public static void main(String args[]) {
		if(args.length == 0) {
			System.out.println("Usage: java JoyRemote <host:port> OR <port>");
			return;
		}

		LinuxJoystick j = new SocketJoystick(args[0], 64, 64);
		j.open();
		j.startPollingThread(5);
		j.setCallback(new EventCallbackHandler());
	}

	static class EventCallbackHandler implements LinuxJoystickEventCallback {
		public void callback(LinuxJoystick j, LinuxJoystickEvent ev) {
			switch(ev.isAxisChanged()) {
				case XboxController.RT:
					if(XboxController.RT(j) == 1.0) {
						System.out.println("Full steam ahead!");
					}
			}

			switch(ev.isButtonDown()) {
				case XboxController.Y:
					System.out.println("Y button is pressed, bye!");
					j.close();
			}
		}
	}
}
