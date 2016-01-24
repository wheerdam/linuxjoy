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

package org.bbi.linuxjoy.hacks;

import java.nio.channels.spi.*;
import java.io.*;

/**
 * When you need the channel object in LinuxJoystick to not be null and you
 * need an implementation of AbstractInterruptibleChannel!
 *
 * This hack can be used if your implementation of LinuxJoystick channel
 * functions don't actually use a Java NIO channel. In your channelOpen()
 * function you can do something like:
 *
 *   fc = new org.bbi.linuxjoy.hacks.DummyInterruptibleChannel();
 *
 * to prevent it being null (which signifies that the open function failed)
 *
 * @author wira
 */
public class DummyInterruptibleChannel extends AbstractInterruptibleChannel {
	protected void implCloseChannel() throws IOException {

	}
}
