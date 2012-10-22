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
