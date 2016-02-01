import org.bbi.linuxjoy.*;

class NoJoyTest {
	public static void main(String args[]) {
		NoJoy.getEnumeration();
		NoJoy j = new NoJoy(0);
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
