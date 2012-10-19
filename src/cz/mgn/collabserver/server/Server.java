/*
 * Collab desktop - Software for shared drawing via internet in real-time
 * Copyright (C) 2012 Martin Indra <aktive@seznam.cz>
 *
 * This file is part of Collab desktop.
 *
 * Collab desktop is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Collab desktop is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Collab desktop.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.mgn.collabserver.server;

import cz.mgn.collabserver.CollabServer;
import cz.mgn.collabserver.server.client.Client;
import cz.mgn.collabserver.server.commandprocessor.CommandProcessor;
import cz.mgn.collabserver.server.room.Room;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author indy
 */
public class Server extends Thread {

    protected String name = "";
    protected int port = 0;
    protected volatile boolean running = false;
    protected ConnectionChecker checker = null;
    protected ServerSocket socket = null;
    protected ArrayList<Client> clients = new ArrayList<Client>();
    protected ArrayList<Room> rooms = new ArrayList<Room>();
    protected CommandProcessor processor = null;

    public Server(String name, int port) {
        this.name = name;
        this.port = port;
        checker = new ConnectionChecker(this);
        processor = new CommandProcessor(this);
    }

    @Override
    public void run() {
        running = true;
        try {
            checker.start();
            socket = new ServerSocket(port);
            CollabServer.log(this, "listening started", false);
            while (running) {
                Socket clientSocket = socket.accept();
                Client client = new Client(this, clientSocket);
                client.start();
                clients.add(client);
                CollabServer.log(this, "connection accepted " + client, false);
                logClientsCount();
            }
        } catch (IOException ex) {
            CollabServer.log(this, "" + ex, true);
        }
    }

    public void messageReceived(Client client, byte[] data) {
        processor.processCommand(client, data);
    }

    public void sendData(byte[] data, Client client) {
        client.send(data);
    }

    public void sendData(byte[] data, ArrayList<Client> clients) {
        synchronized (clients) {
            for (Client client : clients) {
                sendData(data, client);
            }
        }
    }

    public void cliendClosed(Client client) {
        processor.destroyClient(client);
        clients.remove(client);
        CollabServer.log(this, "connection closed " + client, false);
        logClientsCount();
    }

    public void addRoom(Room room) {
        rooms.add(room);
        CollabServer.log(this, "room created " + room, false);
        logRoomsCount();
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
        CollabServer.log(this, "room destroyed " + room, false);
        logRoomsCount();
    }

    public Room getRoomByName(String name) {
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomName().equals(name)) {
                return rooms.get(i);
            }
        }
        return null;
    }

    public Room getRoomByID(int roomID) {
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getID() == roomID) {
                return rooms.get(i);
            }
        }
        return null;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public void closeClient(Client client) {
        client.close();
    }

    @Override
    public String toString() {
        return "[name: " + name + " port: " + port + "]";
    }

    public void log(Client client, String message) {
        CollabServer.log(this, "on client " + client + ": " + message, false);
    }

    public void error(Client client, String message) {
        CollabServer.log(this, "on client " + client + ": " + message, true);
    }

    protected void logClientsCount() {
        CollabServer.log(this, "" + clients.size() + " clients are now connected", false);
    }

    protected void logRoomsCount() {
        CollabServer.log(this, "" + rooms.size() + " rooms are now opened", false);
    }

    public void checkConnections() {
        for (int i = 0; i < clients.size(); i++) {
            Client client = clients.get(i);
            if (!client.isConnectionValid()) {
                closeClient(client);
            }
        }
    }

    public void stopServer() {
        if (running) {
            checker.stopChecking();
            ArrayList<Client> clients = new ArrayList<Client>();
            clients.addAll(this.clients);
            for (Client client : clients) {
                closeClient(client);
            }
            running = false;
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
