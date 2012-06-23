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
