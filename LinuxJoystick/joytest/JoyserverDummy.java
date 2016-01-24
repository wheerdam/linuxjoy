import org.bbi.linuxjoy.*;

class JoyserverDummy {
	public static void main(String args[]) {
		LinuxJoystick j = new FakeDataSource(XboxController.BUTTONS, XboxController.AXES);
		SocketBridge ljs = new SocketBridge();
		ljs.serve(j, 8000);
	}
}
