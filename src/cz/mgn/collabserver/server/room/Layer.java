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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author indy
 */
public class Layer {

    protected BufferedImage image = null;
    protected String name = "";
    protected int id = 0;

    public Layer(int id, int width, int height) {
        this.id = id;
        this.name = "#" + id;
        image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
    }

    public Layer(int id, int width, int height, BufferedImage background) {
        this(id, width, height);

        Graphics2D g = (Graphics2D) image.getGraphics();
        for (int x = 0; x < image.getWidth(); x += background.getWidth()) {
            for (int y = 0; y < image.getHeight(); y += background.getHeight()) {
                g.drawImage(background, x, y, null);
            }
        }
    }

    public void setLayerName(String name) {
        synchronized (this.name) {
            this.name = name;
        }
    }

    public String getLayerName() {
        synchronized (name) {
            return name;
        }
    }

    public BufferedImage getLayerImage() {
        synchronized (image) {
            return image;
        }
    }

    public int getID() {
        return id;
    }

    public boolean addChange(BufferedImage change, int x, int y) {
        synchronized (image) {
            Graphics2D g = (Graphics2D) image.getGraphics();
            g.drawImage(change, x, y, null);
            return true;
        }
    }

    public boolean removeChange(BufferedImage change, int x, int y) {
        synchronized (image) {
            Graphics2D g = (Graphics2D) image.getGraphics();
            g.setComposite(AlphaComposite.DstOut);
            g.drawImage(change, x, y, null);
            g.dispose();
            return true;
        }
    }
}
