import org.bbi.linuxjoy.*;

class Joyserver {
	public static void main(String args[]) {
		LinuxJoystick j = new LinuxJoystick(args[0], XboxController.BUTTONS, XboxController.AXES);
		SocketBridge ljs = new SocketBridge();
		ljs.serve(j, 8000);
	}
}
