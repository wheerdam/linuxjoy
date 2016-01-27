import org.bbi.linuxjoy.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.channels.spi.*;

class FakeDataSource extends LinuxJoystick {
	public FakeDataSource(int buttons, int axes) {
		super("FakeDataSource", buttons, axes);
	}	

	@Override
	protected boolean channelOpen() {
		return true;
	}

	@Override
	protected int channelRead() {
		try {
			Thread.sleep(500);
		} catch(Exception e) {

		}
		buf.clear();
		// fill the buffer with a fake Linux event packet 
		// this is a "button 0 is pressed" event
		buf.put(LinuxJoystickEvent.constructPacket(
			(int) System.currentTimeMillis(), (short) 1, (byte) 1, (byte) 0), 0, 8
		);
		return 8;
	}
}
