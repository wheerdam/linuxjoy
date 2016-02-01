import org.bbi.linuxjoy.*;

// open and serve the first usable device
class JoyServer {
	public static void main(String args[]) {
		SocketBridge sb = new SocketBridge();
		LinuxJoystick j = JoyFactory.getFirstUsableDevice();
		if(j != null) {
			sb.serve(j, 8000);
		}
	}
}
