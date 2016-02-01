import org.bbi.linuxjoy.*;

class Test {
	public static void main(String args[]) {
		if(args.length == 1 && args[0].equals("--native")) {
			JoyFactory.ALWAYS_USE_NATIVE = true;
		}

		LinuxJoystick j = JoyFactory.getFirstUsableDevice();
		j.startPollingThread(5);
		while(true) {
			if(j.getButtonState(0)) {
				j.close();
				return;
			}
			try {
				Thread.sleep(5);
			} catch(Exception e) {

			}
		}
	}
}
