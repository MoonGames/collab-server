/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.mgn.collabserver.http;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author indy
 */
public class HTTPImage {

    protected BufferedImage image = null;

    public HTTPImage(BufferedImage image) {
        this.image = image;
    }

    public byte[] getImageData() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", out);
            return out.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(HTTPImage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new byte[]{};
    }
}
