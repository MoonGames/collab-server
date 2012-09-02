/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
