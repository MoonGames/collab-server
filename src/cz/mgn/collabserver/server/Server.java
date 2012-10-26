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
            CollabServer.logLevelMedium(this, "listening started");
            while (running) {
                Socket clientSocket = socket.accept();
                Client client = new Client(this, clientSocket);
                client.start();
                clients.add(client);
                CollabServer.logLevelMedium(this, "connection accepted " + client);
                logClientsCount();
            }
        } catch (IOException ex) {
            CollabServer.logLevelError(this, "" + ex);
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                CollabServer.logLevelError(this, "" + ex);
            }
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
        CollabServer.logLevelMedium(this, "connection closed " + client);
        logClientsCount();
    }

    public void addRoom(Room room) {
        rooms.add(room);
        CollabServer.logLevelMedium(this, "room created " + room);
        logRoomsCount();
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
        CollabServer.logLevelMedium(this, "room destroyed " + room);
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

    public void error(Client client, String message) {
        CollabServer.logLevelError(this, "on client " + client + ": " + message);
    }

    protected void logClientsCount() {
        CollabServer.logLevelMedium(this, "" + clients.size() + " clients are now connected");
    }

    protected void logRoomsCount() {
        CollabServer.logLevelMedium(this, "" + rooms.size() + " rooms are now opened");
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
