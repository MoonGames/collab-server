/*
 * Collab server - Server from Collab project
 * Copyright (C) 2012 Martin Indra <aktive@seznam.cz>
 *
 * This file is part of Collab server.
 *
 * Collab server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Collab server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Collab server.  If not, see <http://www.gnu.org/licenses/>.
 */

package cz.mgn.collabserver.server.client;

import cz.mgn.collabserver.CSUtils;
import cz.mgn.collabserver.CollabServer;
import cz.mgn.collabserver.server.Server;
import cz.mgn.collabserver.server.room.Room;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author indy
 */
public class Client extends Thread {

    protected String nick = "user";
    protected Server server = null;
    protected Socket socket = null;
    protected OutputStream out = null;
    protected InputStream is = null;
    protected static byte msStart = 111;
    protected Room parentRoom = null;
    protected int idInParentRoom = 0;

    public Client(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            if (socket.isConnected()) {
                is = socket.getInputStream();
                out = socket.getOutputStream();

                while (readNext(is)) {
                }
            }
        } catch (IOException ex) {
            server.error(this, "" + ex);
        } finally {
            close();
        }
    }

    /**
     * read next standard message from client
     *
     * @param in input stream
     * @return if reading was sucesfull
     */
    protected boolean readNext(InputStream in) throws IOException {
        byte[] ms = new byte[1];
        if (in.read(ms) == -1) {
            return false;
        }
        if (ms[0] == msStart) {
            byte[] len = new byte[4];
            if (!read(len, in)) {
                return false;
            }
            int length = CSUtils.byteArrayToInt(len);
            if (length <= 0 || length > (10 * 1024 * 1024)) {
                return false;
            }
            byte[] data = new byte[length];
            if (!read(data, in)) {
                return false;
            }
            dataReaded(data);
        }
        return true;
    }

    protected boolean read(byte[] bytes, InputStream in) throws IOException {
        byte[] r = new byte[1];
        for (int i = 0; i < bytes.length; i++) {
            if (in.read(r) == -1) {
                return false;
            }
            bytes[i] = r[0];
        }
        return true;
    }

    /**
     * process incoming data
     *
     * @param bytes data to process
     */
    protected void dataReaded(byte[] bytes) {
        //TODO: (5 + bytes.length) use some symbolic constatn instead of 5
        CollabServer.logLevelAll(server, "client " + this + " received " + (5
                + bytes.length) + " bytes. Message bytes: " + CSUtils.
                byteArrayToString(bytes, false));
        server.messageReceived(this, bytes);
    }

    /**
     * create and send standard message to client
     *
     * @param bytes data of message
     */
    public void send(byte[] bytes) {
        synchronized (out) {
            try {
                if (!socket.isClosed()) {
                    try {
                        byte[] len = CSUtils.intToByteArray(bytes.length);
                        CollabServer.logLevelAll(server, "client " + this
                                + " sending " + (1 + len.length + bytes.length)
                                + " bytes. Message bytes: " + CSUtils.
                                byteArrayToString(bytes, false));
                        out.write(msStart);
                        out.write(len);
                        out.write(bytes);
                        out.flush();
                    } catch (SocketException ex) {
                        CollabServer.logLevelError(server, ex.getMessage());
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * close client connection and inform server
     */
    public void close() {
        try {
            out.close();
            is.close();
            socket.close();
            server.cliendClosed(this);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * set parent room of this client, or null if isnt
     */
    public void setParentRoom(Room room, int idInParentRoom) {
        this.parentRoom = room;
        this.idInParentRoom = idInParentRoom;
    }

    /**
     * return parent room of this client (if isnt returns null)
     */
    public Room getParentRoom() {
        return parentRoom;
    }

    public int getIDInParentRoom() {
        return idInParentRoom;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getNick() {
        return nick;
    }

    public boolean isConnectionValid() {
        return socket.isConnected();
    }

    @Override
    public String toString() {
        return "[remote address: " + socket.getInetAddress().getHostAddress() + " remote port: " + socket.getPort() + " local port: " + socket.getLocalPort() + "]";
    }
}
