import org.bbi.linuxjoy.*;

class LinuxTest {
	public static void main(String args[]) {
		if(args.length == 0) {
			System.out.println("Usage: java LinuxTest <device-file>");
			return;
		}

		LinuxJoystick j = new LinuxJoystick(args[0], 64, 64);
		j.open();
		while(true) {
			// wait until user presses button 0
			if(!j.isDeviceOpen()) {
				return;
			}
			j.poll();

			if(j.getButtonState(0)) {
				System.out.println("Button 0 is pressed");
				return;
			}

			try {
				Thread.sleep(5);
			} catch(Exception e) {

			}
		}
	}
}
