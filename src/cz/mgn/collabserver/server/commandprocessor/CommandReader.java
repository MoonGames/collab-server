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

package cz.mgn.collabserver.server.commandprocessor;

import cz.mgn.collabserver.CSUtils;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author indy
 */
public class CommandReader {

    protected static ImageData readPaintRemoveCommand(byte[] data) {
        if (data.length > 16) {
            byte[] identificatorData = new byte[4];
            System.arraycopy(data, 0, identificatorData, 0, 4);

            byte[] layerIDData = new byte[4];
            System.arraycopy(data, 4, layerIDData, 0, 4);
            byte[] xData = new byte[4];
            System.arraycopy(data, 8, xData, 0, 4);
            byte[] yData = new byte[4];
            System.arraycopy(data, 12, yData, 0, 4);

            int identificator = CSUtils.byteArrayToInt(identificatorData);
            int layerID = CSUtils.byteArrayToInt(layerIDData);
            int x = CSUtils.byteArrayToInt(xData);
            int y = CSUtils.byteArrayToInt(yData);

            byte[] imageData = new byte[data.length - 16];
            System.arraycopy(data, 16, imageData, 0, imageData.length);
            ByteArrayInputStream stream = new ByteArrayInputStream(imageData);
            try {
                BufferedImage image = ImageIO.read(stream);
                return new ImageData(identificator, layerID, x, y, image);
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }

    public static ImageData readPaintCommand(byte[] data) {
        return readPaintRemoveCommand(data);
    }

    public static ImageData readRemoveCommand(byte[] data) {
        return readPaintRemoveCommand(data);
    }

    /**
     * read add layer command as three objects, first is integer with layer
     * location, second is integer with command identificator and third is
     * String with layer name
     */
    public static Object[] readAddLayerCommand(byte[] data) {
        if (data.length >= 8) {
            byte[] layerLocationData = new byte[4];
            System.arraycopy(data, 0, layerLocationData, 0, 4);
            byte[] commandIdentificatorData = new byte[4];
            System.arraycopy(data, 4, commandIdentificatorData, 0, 4);
            byte[] layerNameData = new byte[data.length - 8];
            System.arraycopy(data, 8, layerNameData, 0, layerNameData.length);
            Object[] result = new Object[3];
            result[0] = CSUtils.byteArrayToInt(layerLocationData);
            result[1] = CSUtils.byteArrayToInt(commandIdentificatorData);
            try {
                result[2] = new String(layerNameData, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(CommandReader.class.getName()).log(Level.SEVERE, null, ex);
            }
            return result;
        }
        return null;
    }

    /**
     * read remove layer command as layer ID
     */
    public static int readRemoveLayerCommand(byte[] data) {
        if (data.length >= 4) {
            return CSUtils.byteArrayToInt(data);
        }
        return -1;
    }

    /**
     * read set layer location as two integers, first layer id second new
     * location
     */
    public static int[] readSetLayerLocationCommand(byte[] data) {
        if (data.length >= 8) {
            byte[] layerID = new byte[4];
            System.arraycopy(data, 0, layerID, 0, 4);
            byte[] newLocation = new byte[4];
            System.arraycopy(data, 4, newLocation, 0, 4);
            int[] result = new int[2];
            result[0] = CSUtils.byteArrayToInt(layerID);
            result[1] = CSUtils.byteArrayToInt(newLocation);
            return result;
        }
        return null;
    }

    /**
     * read set layer name command as two Object first is Integer with layer id,
     * second is String with new name
     */
    public static Object[] readSetLayerNameCommand(byte[] data) {
        if (data.length >= 4) {
            byte[] layerIDData = new byte[4];
            System.arraycopy(data, 0, layerIDData, 0, 4);
            byte[] nameData = new byte[data.length - 4];
            System.arraycopy(data, 4, nameData, 0, nameData.length);
            Object[] result = new Object[2];
            result[0] = CSUtils.byteArrayToInt(layerIDData);
            result[1] = "";
            try {
                result[1] = new String(nameData, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                return null;
            }
            return result;
        }
        return null;
    }

    public static RoomInfo readCreateRoomCommand(byte[] data) {
        if (data.length >= 12) {
            byte[] widthData = new byte[4];
            System.arraycopy(data, 0, widthData, 0, 4);
            byte[] heightData = new byte[4];
            System.arraycopy(data, 4, heightData, 0, 4);

            byte[] passwordLengthData = new byte[4];
            System.arraycopy(data, 8, passwordLengthData, 0, 4);
            int passwordLength = CSUtils.byteArrayToInt(passwordLengthData);
            if (passwordLength < 0 || data.length < (12 + passwordLength)) {
                return null;
            }
            byte[] passwordData = new byte[passwordLength];
            System.arraycopy(data, 12, passwordData, 0, passwordData.length);

            byte[] nameData = new byte[data.length - 12 - passwordData.length];
            System.arraycopy(data, data.length - nameData.length, nameData, 0, nameData.length);


            int width = CSUtils.byteArrayToInt(widthData);
            int height = CSUtils.byteArrayToInt(heightData);
            String password = "";
            String name = "-";
            try {
                password = new String(passwordData, "UTF-8");
                name = new String(nameData, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(CommandReader.class.getName()).log(Level.SEVERE, null, ex);
            }

            return new RoomInfo(width, height, password, name);
        }
        return null;
    }

    /**
     * return join room command as Object array, first item is integer with room
     * id and second is String with password (can be empty)
     */
    public static Object[] readJoinCommand(byte[] data) {
        if (data.length >= 4) {
            Object[] result = new Object[2];

            byte[] roomIDData = new byte[4];
            System.arraycopy(data, 0, roomIDData, 0, 4);

            byte[] passwordData = new byte[data.length - 4];
            System.arraycopy(data, 4, passwordData, 0, passwordData.length);

            result[0] = CSUtils.byteArrayToInt(roomIDData);
            try {
                result[1] = new String(passwordData, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(CommandReader.class.getName()).log(Level.SEVERE, null, ex);
            }
            return result;
        }
        return null;
    }

    public static String readChatCommand(byte[] data) {
        try {
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CommandReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "-";
    }

    public static String readSetNickCommand(byte[] data) {
        try {
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CommandReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "-";
    }

    public static class RoomInfo {

        protected int width = 0;
        protected int height = 0;
        protected String password = "";
        protected String name = "";

        public RoomInfo(int width, int height, String password, String name) {
            this.width = width;
            this.height = height;
            this.password = password;
            this.name = name;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String getPassword() {
            return password;
        }

        public String getName() {
            return name;
        }
    }
}
