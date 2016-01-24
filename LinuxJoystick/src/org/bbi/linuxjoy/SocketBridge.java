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
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Opens a socket interface for the Linux joystick device.
 *
 * @author wira
 */
public class SocketBridge {
    private ServerSocketChannel server;
    private SocketChannel s;
    private LinuxJoystick j;

    /**
     * Polling interval in milliseconds. Note that polling is a blocking
     * operation, so this interval will only come into play between events
     * and not when the underlying joystick object is idle
     */
    public static int POLL_INTERVAL_MS = 5;

    /**
     * Stop listening to connections
     */
    public boolean STOP;

    /**
     * Stop sending data
     */
    public boolean STOP_SEND;

    /**
     * Serve the provided joystick object to the specified socket port
     *
     * @param jj Reference to the joystick object
     * @param port Port number to listen to
     */
    public void serve(LinuxJoystick jj, int port) {
        if(server != null) {
            System.err.println("Server is already running.");
            return;
        }

        if(s != null) {
            System.err.println("Client socket is already open.");
            return;
        }
        
        STOP = false;
        this.j = jj;

        while(!STOP && j.isDeviceOpen()) {
            try {
                server = ServerSocketChannel.open();
                server.socket().setReuseAddress(true);
                server.socket().bind(new InetSocketAddress(port));
                System.out.println("Listening on port " + port);
                s = server.accept();
                System.out.println("Connection from " + s.getRemoteAddress());
            } catch(IOException e) {
                System.err.println("I/O exception on server socket open/accept: " + e.getMessage());
                return;
            }
            STOP_SEND = false;
            poll(j);

            try {
                s.close();
                server.close();
            } catch(IOException e) {
                System.err.println("I/O exception on socket close: " + e.getMessage());
            }
        }
    }

    /**
     * Connect to a SocketJoystick server and send joystick data
     *
     * @param jj Reference to the joystick object
     * @param address Address of the server
     * @param port Port of the server
     */
    public void connect(LinuxJoystick jj, String address, int port) {
        if(s != null) {
            System.err.println("Client socket is already open.");
            return;
        }

        this.j = jj;

        try {
            s = SocketChannel.open(new InetSocketAddress(address, port));
        } catch(IOException e) {
            System.err.println("I/O exception on socket connect: " + e.getMessage());
            return;
        }

        STOP_SEND = false;
        poll(j);

        try {
            s.close();
        } catch(IOException e) {
            System.err.println("I/O exception on socket close: " + e.getMessage());
        }
    }

    private void poll(LinuxJoystick j) {
        j.setCallback(new Callback());
        while(j.isDeviceOpen() && !STOP_SEND) {
            j.poll();
            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch(Exception e) {

            }
        }
    }

    /**
     * Stop connection
     */
    public void stop() {
        STOP = true;
        try {
            if(server != null) {
                server.close();
            }
            if(s != null) {
                s.close();
            }
            if(j != null) {
                j.setCallback(null);
                j.close();
            }
            j = null;
            server = null;
            s = null;
        } catch(Exception e) {
            System.err.println("I/O exception during service stop: " + e.getMessage());
        }
    }

    /**
     * Callback implementation that will send data whenever a joystick event
     * has occured
     */
    class Callback implements LinuxJoystickEventCallback {
        @Override
        public void callback(LinuxJoystick jUnused, LinuxJoystickEvent ev) {
            ByteBuffer buf = ByteBuffer.allocate(8);
            buf.clear();
            buf.put(ev.getRaw());
            buf.flip();
            try {
                System.out.println(ev);
                while(buf.hasRemaining()) {
                    s.write(buf);
                }
            } catch(IOException e) {
                System.err.println("I/O exception on socket write: " + e.getMessage());
                STOP_SEND = true;
                j.setCallback(null);
            }
        }
    }
}
