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

package cz.mgn.collabserver.server.room;

import cz.mgn.collabserver.CSSettings;
import cz.mgn.collabserver.server.client.Client;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author indy
 */
public class Room {

    protected ArrayList<Client> clients = new ArrayList<Client>();
    protected int id = 0;
    protected String name = "";
    protected String password = "";
    protected int width = 0;
    protected int height = 0;
    protected volatile int lastLayerID = 0;
    protected ArrayList<Layer> layers = new ArrayList<Layer>();
    protected boolean[] ids = new boolean[16];

    public Room(int id, String name, int width, int height) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        init();
    }

    protected void init() {
        BufferedImage background = new BufferedImage(10, 10, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D) background.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 10, 10);
        Layer backgroundLayer = new Layer(genereteLayerID(), width, height, background);
        backgroundLayer.setLayerName("background layer");
        layers.add(backgroundLayer);
        Layer foregroundLayer = new Layer(genereteLayerID(), width, height);
        foregroundLayer.setLayerName("foreground layer");
        layers.add(foregroundLayer);
    }

    protected int genereteLayerID() {
        return ++lastLayerID;
    }

    protected int getLayerIndexByID(int layerID) {
        synchronized (layers) {
            for (int i = 0; i < layers.size(); i++) {
                if (layers.get(i).getID() == layerID) {
                    return i;
                }
            }
            return -1;
        }
    }

    protected Layer getLayerByID(int layerID) {
        synchronized (layers) {
            int index = getLayerIndexByID(layerID);
            if (index == -1) {
                return null;
            }
            return layers.get(index);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ArrayList<Client> getClients() {
        synchronized (clients) {
            return clients;
        }
    }

    public boolean removeClient(Client client) {
        synchronized (clients) {
            int index = clients.indexOf(client);
            if (index >= 0) {
                clients.remove(index);
                ids[client.getIDInParentRoom()] = false;
            }
            return clients.isEmpty();
        }
    }

    public void addClient(Client client) {
        synchronized (clients) {
            clients.add(client);
            ids[client.getIDInParentRoom()] = true;
        }
    }

    public String getRoomName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getID() {
        return id;
    }

    public synchronized ArrayList<Layer> getLayers() {
        synchronized (layers) {
            return layers;
        }
    }

    public int[] getLayersIDOrded() {
        synchronized (layers) {
            int[] l = new int[layers.size()];
            int index = 0;
            for (Layer layer : layers) {
                l[index++] = layer.getID();
            }
            return l;
        }
    }

    public void setLayerLocation(int location, int layerID) {
        synchronized (layers) {
            if (location >= 0 && location < layers.size()) {
                Layer layer = getLayerByID(layerID);
                if (layer != null) {
                    layers.remove(layer);
                    layers.add(location, layer);
                }
            }
        }
    }

    public int createNewLayer() {
        synchronized (layers) {
            if (layers.size() < CSSettings.MAXIMAL_LAYERS_COUNT) {
                Layer layer = new Layer(genereteLayerID(), width, height);
                layers.add(layer);
                return layer.getID();
            }
            return -1;
        }
    }

    public void removeLayer(int layerID) {
        synchronized (layers) {
            int index = getLayerIndexByID(layerID);
            if (index != -1) {
                layers.remove(index);
            }
        }
    }

    public void setLayerName(int layerID, String name) {
        synchronized (layers) {
            Layer layer = getLayerByID(layerID);
            if (layer != null) {
                layer.setLayerName(name);
            }
        }
    }

    public boolean addChange(int layerID, BufferedImage change, int x, int y) {
        Layer layer = getLayerByID(layerID);
        if (layer != null) {
            return layer.addChange(change, x, y);
        }
        return false;
    }

    public boolean removeChange(int layerID, BufferedImage change, int x, int y) {
        Layer layer = getLayerByID(layerID);
        if (layer != null) {
            return layer.removeChange(change, x, y);
        }
        return false;
    }

    /**
     * return free id for this room or -1 if room is full
     */
    public int getFreeID() {
        for (int i = 0; i < ids.length; i++) {
            if (!ids[i]) {
                return i;
            }
        }
        return -1;
    }

    public BufferedImage generateRoomImage() {
        synchronized (layers) {
            BufferedImage roomImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = (Graphics2D) roomImage.getGraphics();
            for (Layer layer : layers) {
                g.drawImage(layer.getLayerImage(), 0, 0, null);
            }
            g.dispose();
            return roomImage;
        }
    }

    @Override
    public String toString() {
        return "[name: " + name + " resolution: " + width + " x " + height + " id: " + id + "]";
    }
}
