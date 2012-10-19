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
package cz.mgn.collabserver.server.commandprocessor;

import java.awt.image.BufferedImage;

/**
 *
 * @author indy
 */
public class ImageData {

    protected int identificator = -1;
    protected int layerID = 0;
    protected int x = 0;
    protected int y = 0;
    protected BufferedImage image = null;

    public ImageData(int identificator, int layerID, int x, int y, BufferedImage image) {
        this.identificator = identificator;
        this.image = image;
        this.x = x;
        this.y = y;
        this.layerID = layerID;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLayerID() {
        return layerID;
    }

    public int getIdentificator() {
        return identificator;
    }
}
