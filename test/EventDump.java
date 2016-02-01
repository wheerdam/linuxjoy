import org.bbi.linuxjoy.*;

class EventDump {
	public static void main(String args[]) {
		if(args.length > 0 && args[0].equals("--native")) {
			JoyFactory.ALWAYS_USE_NATIVE = true;
		}

		int joyInfo[] = JoyFactory.enumerate();
		if(args.length == 0 || (args.length == 1 && JoyFactory.ALWAYS_USE_NATIVE)) {
			System.out.println();
			System.out.println("Usage: java EventDump [--native] <index>");
			if(joyInfo == null) {
				System.out.println("No joysticks founds.");
				return;
			} 
			System.out.println("Enumerated joysticks:");
			for(int i = 0; i < joyInfo.length; i++) {
				String str = String.format("%d: ID(%d) %d buttons %d axes", i, joyInfo[i] & 0xff,
						JoyFactory.BUTTONS(joyInfo[i]), JoyFactory.AXES(joyInfo[i]));
				System.out.println(str);
			}
		} else {
			int index;
			try {
				index = (args.length == 1 ? Integer.parseInt(args[0]) : Integer.parseInt(args[1]));
			} catch(Exception e) {
				System.out.println("Failed to parse the given index.");
				return;
			}
			LinuxJoystick j = JoyFactory.get(index);
			if(j == null) {
				System.out.println("Specified index is invalid.");
				return;
			}
			j.open();
			j.setCallback(new EventCallbackHandler());
			j.startPollingThread(5);
		}
	}

	static class EventCallbackHandler implements LinuxJoystickEventCallback {
		public void callback(LinuxJoystick j, LinuxJoystickEvent ev) {
			System.out.println(ev);

			switch(ev.isButtonDown()) {
				case 8:
					System.exit(0);
			}	
		}
	}
}
