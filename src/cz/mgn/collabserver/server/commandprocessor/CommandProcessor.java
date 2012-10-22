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

package cz.mgn.collabserver.server.commandprocessor;

import cz.mgn.collabserver.CSSettings;
import cz.mgn.collabserver.CSUtils;
import cz.mgn.collabserver.CollabServer;
import cz.mgn.collabserver.http.HTTPServer;
import cz.mgn.collabserver.server.Server;
import cz.mgn.collabserver.server.client.Client;
import cz.mgn.collabserver.server.room.Layer;
import cz.mgn.collabserver.server.room.Room;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author indy
 */
public class CommandProcessor {

    protected Server server = null;
    protected int lastRoomID = 100;
    //incoming commands
    public static final byte IN_COMMAND_PAINT = 1;
    public static final byte IN_COMMAND_REMOVE = 2;
    public static final byte IN_COMMAND_ADD_LAYER = 3;
    public static final byte IN_COMMAND_REMOVE_LAYER = 4;
    public static final byte IN_COMMAND_SET_LAYER_LOCATION = 5;
    public static final byte IN_COMMAND_SET_LAYER_NAME = 6;
    public static final byte IN_COMMAND_GET_ROOMS_LIST = 7;
    public static final byte IN_COMMAND_CREATE_ROOM = 8;
    public static final byte IN_COMMAND_JOIN_ROOM = 9;
    public static final byte IN_COMMAND_CHAT = 10;
    public static final byte IN_COMMAND_SET_NICK = 11;
    public static final byte IN_COMMAND_DISCONNECT_FROM_ROOM = 12;
    public static final byte IN_COMMAND_MAKE_HTTP_IMAGE = 13;

    public CommandProcessor(Server server) {
        this.server = server;
    }

    protected int generateRoomID() {
        return lastRoomID++;
    }

    protected void addClientToRoom(Client client, Room room) {
        int id = room.getFreeID();
        if (id >= 0) {
            removeClientFromRoom(client);
            client.setParentRoom(room, id);
            room.addClient(client);
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(CommandProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }

            byte[] send = CommandGenerator.generateJoinRoomCommand(id, room.getRoomName());
            server.sendData(send, client);
            sendStartRoomData(client, room);
            sendUsersInRoom(room, room.getClients());
            send = CommandGenerator.generateChatCommand("System", "User " + client.getNick() + " joined room.");
            server.sendData(send, room.getClients());
        }
    }

    protected void sendStartRoomData(Client client, Room room) {
        byte[] send = CommandGenerator.generateSetResolutionCommand(room.getWidth(), room.getHeight());
        server.sendData(send, client);
        send = CommandGenerator.generateLayersOrderCommand(room.getLayersIDOrded());
        server.sendData(send, client);
        ArrayList<Layer> layers = room.getLayers();
        for (Layer layer : layers) {
            send = CommandGenerator.generateSetLayerNameCommand(layer.getID(), layer.getLayerName());
            server.sendData(send, client);
        }
        for (Layer layer : layers) {
            ImageData image = new ImageData(-2, layer.getID(), 0, 0, layer.getLayerImage());
            send = CommandGenerator.generatePaintCommand(image);
            server.sendData(send, client);
        }
    }

    protected void removeClientFromRoom(Client client) {
        Room room = client.getParentRoom();
        if (room != null) {
            room.removeClient(client);
            client.setParentRoom(null, -1);

            byte[] send = CommandGenerator.generateDisconnectFromRoomCommand();
            server.sendData(send, client);

            sendUsersInRoom(room, room.getClients());

            if (room.getClients().isEmpty()) {
                destroyRoom(room);
            } else {
                send = CommandGenerator.generateChatCommand("System", "User " + client.getNick() + " left room.");
                server.sendData(send, room.getClients());
            }
        }
    }

    protected void sendUsersInRoom(Room room, ArrayList<Client> to) {
        ArrayList<Client> clients = room.getClients();
        String[] nicks = new String[clients.size()];
        for (int i = 0; i < clients.size(); i++) {
            nicks[i] = clients.get(i).getNick();
        }
        byte[] send = CommandGenerator.generateUsersListCommand(nicks);
        server.sendData(send, to);
    }

    protected void destroyRoom(Room room) {
        ArrayList<Client> clients = room.getClients();
        for (Client client : clients) {
            removeClientFromRoom(client);
        }
        server.removeRoom(room);
        HTTPServer http = CollabServer.getHTTPServer();
        if (http != null) {
            http.removeRoomImages(room.getRoomName());
        }
    }

    protected Room createRoom(String name, int width, int height) {
        width = Math.max(1, Math.min(CSSettings.MAXIMAL_ROOM_WIDTH, width));
        height = Math.max(1, Math.min(CSSettings.MAXIMAL_ROOM_HEIGHT, height));
        Room room = new Room(generateRoomID(), name, width, height);
        return room;
    }

    protected void commandPaint(Client client, byte[] data) {
        Room room = client.getParentRoom();
        if (room != null) {
            ImageData image = CommandReader.readPaintCommand(data);
            if (image != null) {
                byte[] send = CommandGenerator.generatePaintCommand(image);
                server.sendData(send, room.getClients());
                room.addChange(image.getLayerID(), image.getImage(), image.getX(), image.getY());
            }
        }
    }

    protected void commandRemove(Client client, byte[] data) {
        Room room = client.getParentRoom();
        if (room != null) {
            ImageData image = CommandReader.readRemoveCommand(data);
            if (image != null) {
                byte[] send = CommandGenerator.generateRemoveCommand(image);
                server.sendData(send, room.getClients());
                room.removeChange(image.getLayerID(), image.getImage(), image.getX(), image.getY());
            }
        }
    }

    protected void commandAddLayer(Client client, byte[] data) {
        Room room = client.getParentRoom();
        if (room != null) {
            Object[] commandData = CommandReader.readAddLayerCommand(data);
            if (commandData != null) {
                int newLayerLocation = (Integer) commandData[0];
                int commandIdentificator = (Integer) commandData[1];
                String layerName = (String) commandData[2];
                int newLayerID = room.createNewLayer();
                if (newLayerID >= 0) {
                    if (newLayerLocation >= 0 && newLayerLocation < room.getLayers().size()) {
                        room.setLayerLocation(newLayerLocation, newLayerID);
                    }
                    room.setLayerName(newLayerID, layerName);
                }

                byte[] send = CommandGenerator.generateAddLayerCommand(newLayerID, commandIdentificator, newLayerID >= 0);
                server.sendData(send, client);
                if (newLayerID >= 0) {
                    send = CommandGenerator.generateLayersOrderCommand(room.getLayersIDOrded());
                    server.sendData(send, room.getClients());
                    send = CommandGenerator.generateSetLayerNameCommand(newLayerID, layerName);
                    server.sendData(send, room.getClients());
                }
            }
        }
    }

    protected void commandRemoveLayer(Client client, byte[] data) {
        Room room = client.getParentRoom();
        if (room != null) {
            int layerID = CommandReader.readRemoveLayerCommand(data);
            room.removeLayer(layerID);
            byte[] send = CommandGenerator.generateLayersOrderCommand(room.getLayersIDOrded());
            server.sendData(send, room.getClients());
        }
    }

    protected void commandSetLayerLocation(Client client, byte[] data) {
        Room room = client.getParentRoom();
        if (room != null) {
            int[] commandData = CommandReader.readSetLayerLocationCommand(data);
            if (commandData != null) {
                int layerID = commandData[0];
                int newLocation = commandData[1];
                room.setLayerLocation(newLocation, layerID);
                byte[] send = CommandGenerator.generateLayersOrderCommand(room.getLayersIDOrded());
                server.sendData(send, room.getClients());
            }
        }
    }

    protected void commandSetLayerName(Client client, byte[] data) {
        Room room = client.getParentRoom();
        if (room != null) {
            Object[] commandData = CommandReader.readSetLayerNameCommand(data);
            if (commandData != null) {
                int layerID = (Integer) commandData[0];
                String name = (String) commandData[1];
                room.setLayerName(layerID, name);
                byte[] send = CommandGenerator.generateSetLayerNameCommand(layerID, name);
                server.sendData(send, room.getClients());
            }
        }
    }

    protected void commandGetRoomsList(Client client, byte[] data) {
        ArrayList<CommandGenerator.RoomInfo> rooms = new ArrayList<CommandGenerator.RoomInfo>();
        ArrayList<Room> sRooms = server.getRooms();
        for (Room room : sRooms) {
            rooms.add(new CommandGenerator.RoomInfo(room.getRoomName(), room.getID(), room.getClients().size(),
                    room.getWidth(), room.getHeight(), !room.getPassword().isEmpty()));
        }

        byte[] send = CommandGenerator.generateRoomsListCommand(rooms);
        server.sendData(send, client);
    }

    protected void commandCreateRoom(Client client, byte[] data) {
        CommandReader.RoomInfo roomInfo = CommandReader.readCreateRoomCommand(data);
        if (roomInfo != null) {
            if (server.getRooms().size() < CSSettings.MAXIMAL_ROOMS_COUNT) {
                String nameBase = CSUtils.toCompatibleCharacters(roomInfo.getName());
                String name = nameBase;
                int i = 1;
                while (server.getRoomByName(name) != null) {
                    name = nameBase + "_(" + (i++) + ")";
                }
                Room room = createRoom(name, roomInfo.getWidth(), roomInfo.getHeight());
                room.setPassword(roomInfo.getPassword());
                server.addRoom(room);
                addClientToRoom(client, room);
            }
        }
    }

    protected void commandJoinRoom(Client client, byte[] data) {
        Object[] joinData = CommandReader.readJoinCommand(data);
        if (joinData != null) {
            int roomID = (Integer) joinData[0];
            Room room = server.getRoomByID(roomID);
            if (room != null) {
                String password = (String) joinData[1];
                if (room.getPassword().length() == 0 || password.equals(room.getPassword())) {
                    addClientToRoom(client, room);
                }
            }
        }
    }

    protected void commandChat(Client client, byte[] data) {
        String message = CommandReader.readChatCommand(data);
        if (message != null) {
            Room room = client.getParentRoom();
            if (room != null) {
                byte[] send = CommandGenerator.generateChatCommand(client.getNick(), message);
                server.sendData(send, room.getClients());
            }
        }
    }

    protected void commandSetNick(Client client, byte[] data) {
        String nick = CommandReader.readSetNickCommand(data);
        if (nick != null) {
            if ("System".equals(nick)) {
                nick = nick + " (user)";
            }
            client.setNick(nick);
            Room room = client.getParentRoom();
            if (room != null) {
                sendUsersInRoom(room, room.getClients());
            }
        }
    }

    protected void commandDisconnectFromRoom(Client client, byte[] data) {
        Room room = client.getParentRoom();
        if (room != null) {
            removeClientFromRoom(client);
        }
    }

    protected void commandMakeHTTPImage(Client client, byte[] data) {
        Room room = client.getParentRoom();
        if (room != null) {
            HTTPServer http = CollabServer.getHTTPServer();
            if (http != null) {
                BufferedImage roomImage = room.generateRoomImage();
                URL url = http.addImage(roomImage, room.getRoomName());
                byte[] send = CommandGenerator.generateChatCommand("System", "Room image is now available on " + url.toString());
                server.sendData(send, room.getClients());
            }
        }
    }

    public void processCommand(Client client, byte[] data) {
        if (data.length > 0) {
            byte command = data[0];
            byte[] realData = new byte[data.length - 1];
            System.arraycopy(data, 1, realData, 0, realData.length);
            switch (command) {
                case IN_COMMAND_PAINT:
                    commandPaint(client, realData);
                    break;
                case IN_COMMAND_REMOVE:
                    commandRemove(client, realData);
                    break;
                case IN_COMMAND_ADD_LAYER:
                    commandAddLayer(client, realData);
                    break;
                case IN_COMMAND_REMOVE_LAYER:
                    commandRemoveLayer(client, realData);
                    break;
                case IN_COMMAND_SET_LAYER_LOCATION:
                    commandSetLayerLocation(client, realData);
                    break;
                case IN_COMMAND_SET_LAYER_NAME:
                    commandSetLayerName(client, realData);
                    break;
                case IN_COMMAND_GET_ROOMS_LIST:
                    commandGetRoomsList(client, realData);
                    break;
                case IN_COMMAND_CREATE_ROOM:
                    commandCreateRoom(client, realData);
                    break;
                case IN_COMMAND_JOIN_ROOM:
                    commandJoinRoom(client, realData);
                    break;
                case IN_COMMAND_CHAT:
                    commandChat(client, realData);
                    break;
                case IN_COMMAND_SET_NICK:
                    commandSetNick(client, realData);
                    break;
                case IN_COMMAND_DISCONNECT_FROM_ROOM:
                    commandDisconnectFromRoom(client, realData);
                    break;
                case IN_COMMAND_MAKE_HTTP_IMAGE:
                    commandMakeHTTPImage(client, realData);
                    break;
            }
        }
    }

    public void destroyClient(Client client) {
        removeClientFromRoom(client);
    }
}
