import org.bbi.linuxjoy.*;

class Joyclient {
	public static void main(String args[]) {
		LinuxJoystick j = new LinuxJoystick(args[0], XboxController.BUTTONS, XboxController.AXES);
		SocketBridge ljs = new SocketBridge();
		ljs.connect(j, "localhost", 8000);
	}
}
