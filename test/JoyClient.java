import org.bbi.linuxjoy.*;

// open and serve the first usable device
class JoyClient {
	public static void main(String args[]) {
		SocketBridge sb = new SocketBridge();
		LinuxJoystick j = JoyFactory.getFirstUsableDevice();
		if(j != null) {
			sb.connect(j, "localhost", 8000);
		}
	}
}
