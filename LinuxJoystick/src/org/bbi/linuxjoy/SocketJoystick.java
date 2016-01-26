/*
	Copyright 2016 Wira Mulia

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

package org.bbi.linuxjoy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;

/**
 * This joystick implementation reads off a socket instead of a file.
 * The "path" variable is the internet socket address (host:port) if acting
 * as a client, or just the port if we are acting as a server
 *
 * @author wira
 */
public class SocketJoystick extends LinuxJoystick {
	public SocketJoystick(String address, int buttons, int axes) {
		super(address, buttons, axes);
	}

	@Override
	protected boolean channelOpen() {
		String addr;
		int port;
		String[] tokens = path.split(":");
		addr = tokens[0];
		if(tokens.length == 1) {
			try {
				port = Integer.parseInt(tokens[0]);
				fc = ServerSocketChannel.open();
				((ServerSocketChannel) fc).socket().setReuseAddress(true);
				((ServerSocketChannel) fc).socket().bind(new InetSocketAddress(port));
				System.out.println(this + ": waiting for connection");
				fc = ((ServerSocketChannel) fc).accept(); // block
				return true;
			} catch(Exception e) {
				System.err.println(this + ": failed to host on port " + path + ", message: " + e.getMessage());
				return false;
			}
		} else if(tokens.length == 2) {
			try {
				port = Integer.parseInt(tokens[1]);
				fc = SocketChannel.open();
				((SocketChannel) fc).connect(new InetSocketAddress(addr, port));
				return true;
			} catch(Exception e) {
				System.err.println(this + ": failed to connect to " + path + ", message: " + e.getMessage());
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	protected int channelRead() throws IOException {
		return ((SocketChannel)fc).read(buf);
	}

	@Override
	public String toString() {
		return "SocketJoystick" + (fc != null ? "(" + path + ")" : "");
	}
}
