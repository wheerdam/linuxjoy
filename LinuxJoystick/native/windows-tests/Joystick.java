import org.bbi.linuxjoy.*;

class Joystick {
	public static void main(String args[]) {
		LinuxJoystick j = JoyFactory.getFirstUsableDevice();
		if(j != null && j.isDeviceOpen()) {
			j.setCallback(new Callback());
			j.startPollingThread(10);
		}
	}
	
	static class Callback implements LinuxJoystickEventCallback {
		public void callback(LinuxJoystick j, LinuxJoystickEvent ev) {
			//if(ev.getType() == LinuxJoystickEvent.BUTTON) {
			//	System.out.println(String.format("%08X", ev.getTimestamp()));
			//}
			
			switch(ev.isAxisChanged()) {
				case XboxController.LT:
					System.out.println("LT value : " + String.format("%.2f", XboxController.LT(j)));
					break;
				case XboxController.RT:
					System.out.println("RT value : " + String.format("%.2f", XboxController.RT(j)));
					break;		
				case XboxController.LS_X:
					System.out.println("LS_X value : " + String.format("%.2f", XboxController.LS_X(j)));
					break;		
				case XboxController.LS_Y:
					System.out.println("LS_Y value : " + String.format("%.2f", XboxController.LS_Y(j)));
					break;
				case XboxController.RS_X:
					System.out.println("RS_X value : " + String.format("%.2f", XboxController.RS_X(j)));
					break;		
				case XboxController.RS_Y:
					System.out.println("RS_Y value : " + String.format("%.2f", XboxController.RS_Y(j)));
					break;		
			}
		
			switch(ev.isButtonDown()) {
				case XboxController.A:
					System.out.println("Button A is pressed");
					break;
				case XboxController.B:
					System.out.println("Button B is pressed");
					break;
				case XboxController.X:
					System.out.println("Button X is pressed");
					break;
				case XboxController.Y:
					System.out.println("Button Y is pressed");
					break;
				case XboxController.LB:
					System.out.println("Button LB is pressed");
					break;
				case XboxController.RB:
					System.out.println("Button RB is pressed");
					break;
				case XboxController.BACK:
					System.out.println("Button BACK is pressed");
					break;
				case XboxController.START:
					System.out.println("Button START is pressed");
					break;
				case XboxController.LS:
					System.out.println("Left Stick is pressed");
					break;
				case XboxController.RS:
					System.out.println("Right stick is pressed");
					break;
			}
			
			switch(ev.isButtonUp()) {
				case XboxController.A:
					System.out.println("Button A is released");
					break;
				case XboxController.B:
					System.out.println("Button B is released");
					break;
				case XboxController.X:
					System.out.println("Button X is released");
					break;
				case XboxController.Y:
					System.out.println("Button Y is released");
					break;
				case XboxController.LB:
					System.out.println("Button LB is released");
					break;
				case XboxController.RB:
					System.out.println("Button RB is released");
					break;
				case XboxController.BACK:
					System.out.println("Button BACK is released");
					break;
				case XboxController.START:
					System.out.println("Button START is released");
					break;
				case XboxController.LS:
					System.out.println("Left Stick is released");
					break;
				case XboxController.RS:
					System.out.println("Right stick is released");
					break;
			}
		}
	}
}
