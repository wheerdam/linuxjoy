import org.bbi.linuxjoy.*;

class JoyclientDummy {
	public static void main(String args[]) {
		LinuxJoystick j = new FakeDataSource(XboxController.BUTTONS, XboxController.AXES);
		SocketBridge ljs = new SocketBridge();
		ljs.connect(j, "localhost", 8000);
	}
}
