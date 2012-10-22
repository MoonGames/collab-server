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

import cz.mgn.collabserver.CSUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 *       @author indy
 */
public class CommandGenerator {

    //outcomming commands
    public static final byte OUT_COMMAND_PAINT = 1;
    public static final byte OUT_COMMAND_REMOVE = 2;
    public static final byte OUT_COMMAND_ADD_LAYER = 3;
    public static final byte OUT_COMMAND_LAYERS_ORDER = 4;
    public static final byte OUT_COMMAND_SET_RESOLUTION = 5;
    public static final byte OUT_COMMAND_SET_LAYER_NAME = 6;
    public static final byte OUT_COMMAND_ROOMS_LIST = 7;
    public static final byte OUT_COMMAND_JOIN_ROOM = 8;
    public static final byte OUT_COMMAND_USERS_LIST = 9;
    public static final byte OUT_COMMAND_CHAT = 10;
    public static final byte OUT_COMMAND_DISCONNECT_FROM_ROOM = 11;

    protected static byte[] splitDataAndCommand(byte[] data, byte command) {
        byte[] result = new byte[1 + data.length];
        result[0] = command;
        System.arraycopy(data, 0, result, 1, data.length);
        return result;
    }

    protected static byte[] splitByteArrays(ArrayList<byte[]> arrays) {
        int len = 0;
        for (byte[] array : arrays) {
            len += array.length;
        }
        byte[] data = new byte[len];
        int index = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, data, index, array.length);
            index += array.length;
        }
        return data;
    }

    protected static byte[] generatePaintRemoveCommandData(ImageData image) {
        try {
            ArrayList<byte[]> data = new ArrayList<byte[]>();
            data.add(CSUtils.intToByteArray(image.getIdentificator()));
            data.add(CSUtils.intToByteArray(image.getLayerID()));
            data.add(CSUtils.intToByteArray(image.getX()));
            data.add(CSUtils.intToByteArray(image.getY()));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image.getImage(), "PNG", baos);
            data.add(baos.toByteArray());

            return splitByteArrays(data);
        } catch (IOException ex) {
            Logger.getLogger(CommandGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static byte[] generatePaintCommand(ImageData image) {
        return splitDataAndCommand(generatePaintRemoveCommandData(image), OUT_COMMAND_PAINT);
    }

    public static byte[] generateRemoveCommand(ImageData image) {
        return splitDataAndCommand(generatePaintRemoveCommandData(image), OUT_COMMAND_REMOVE);
    }

    public static byte[] generateAddLayerCommand(int layerID, int commandIdentificator, boolean sucess) {
        ArrayList<byte[]> data = new ArrayList<byte[]>();
        data.add(CSUtils.intToByteArray(layerID));
        data.add(CSUtils.intToByteArray(commandIdentificator));
        data.add(CSUtils.booleanToByteArray(sucess));

        return splitDataAndCommand(splitByteArrays(data), OUT_COMMAND_ADD_LAYER);
    }

    public static byte[] generateLayersOrderCommand(int[] layersOrder) {
        ArrayList<byte[]> data = new ArrayList<byte[]>();
        for (int j = 0; j < layersOrder.length; j++) {
            data.add(CSUtils.intToByteArray(layersOrder[j]));
        }
        return splitDataAndCommand(splitByteArrays(data), OUT_COMMAND_LAYERS_ORDER);
    }

    public static byte[] generateSetResolutionCommand(int width, int height) {
        ArrayList<byte[]> data = new ArrayList<byte[]>();
        data.add(CSUtils.intToByteArray(width));
        data.add(CSUtils.intToByteArray(height));

        return splitDataAndCommand(splitByteArrays(data), OUT_COMMAND_SET_RESOLUTION);
    }

    public static byte[] generateSetLayerNameCommand(int layerID, String name) {
        byte[] layerIDData = CSUtils.intToByteArray(layerID);
        byte[] layerNameData = new byte[0];
        try {
            layerNameData = name.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CommandGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] data = new byte[layerIDData.length + layerNameData.length];
        int index = 0;
        for (int i = 0; i < layerIDData.length; i++) {
            data[index++] = layerIDData[i];
        }
        for (int i = 0; i < layerNameData.length; i++) {
            data[index++] = layerNameData[i];
        }

        return splitDataAndCommand(data, OUT_COMMAND_SET_LAYER_NAME);
    }

    public static byte[] generateRoomsListCommand(ArrayList<RoomInfo> rooms) {
        ArrayList<byte[]> data = new ArrayList<byte[]>();
        for (RoomInfo room : rooms) {
            try {
                byte[] roomIDData = CSUtils.intToByteArray(room.getID());
                byte[] usersCountData = CSUtils.intToByteArray(room.getUsersCount());
                byte[] widthData = CSUtils.intToByteArray(room.getWidth());
                byte[] heightData = CSUtils.intToByteArray(room.getHeight());
                byte[] lockedData = CSUtils.booleanToByteArray(room.isLocked());
                byte[] nameData = room.getName().getBytes("UTF-8");
                byte[] nameLengthData = CSUtils.intToByteArray(nameData.length);

                data.add(roomIDData);
                data.add(usersCountData);
                data.add(widthData);
                data.add(heightData);
                data.add(lockedData);
                data.add(nameLengthData);
                data.add(nameData);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(CommandGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return splitDataAndCommand(splitByteArrays(data), OUT_COMMAND_ROOMS_LIST);
    }

    public static byte[] generateJoinRoomCommand(int yourID, String roomName) {
        try {
            ArrayList<byte[]> data = new ArrayList<byte[]>();
            data.add(CSUtils.intToByteArray(yourID));
            data.add(roomName.getBytes("UTF-8"));
            return splitDataAndCommand(splitByteArrays(data), OUT_COMMAND_JOIN_ROOM);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CommandGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static byte[] generateUsersListCommand(String[] nicks) {
        ArrayList<byte[]> data = new ArrayList<byte[]>();
        for (int i = 0; i < nicks.length; i++) {
            try {
                byte[] nickData = nicks[i].getBytes("UTF-8");
                data.add(CSUtils.intToByteArray(nickData.length));
                data.add(nickData);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(CommandGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return splitDataAndCommand(splitByteArrays(data), OUT_COMMAND_USERS_LIST);
    }

    public static byte[] generateChatCommand(String nick, String message) {
        ArrayList<byte[]> data = new ArrayList<byte[]>();
        try {
            byte[] nickData = nick.getBytes("UTF-8");
            byte[] messageData = message.getBytes("UTF-8");

            data.add(CSUtils.intToByteArray(nickData.length));
            data.add(nickData);
            data.add(messageData);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CommandGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return splitDataAndCommand(splitByteArrays(data), OUT_COMMAND_CHAT);
    }

    public static byte[] generateDisconnectFromRoomCommand() {
        return splitDataAndCommand(new byte[]{}, OUT_COMMAND_DISCONNECT_FROM_ROOM);
    }

    public static class RoomInfo {

        protected String name = "";
        protected int id = 0;
        protected int usersCount = 0;
        protected int width = 0;
        protected int height = 0;
        protected boolean locked = false;

        public RoomInfo(String name, int id, int usersCount, int width, int height, boolean locked) {
            this.name = name;
            this.id = id;
            this.usersCount = usersCount;
            this.width = width;
            this.height = height;
            this.locked = locked;
        }

        public String getName() {
            return name;
        }

        public int getID() {
            return id;
        }

        public boolean isLocked() {
            return locked;
        }

        public int getUsersCount() {
            return usersCount;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
