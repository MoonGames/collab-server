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
package cz.mgn.collabserver.http;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 *
 * @author indy
 */
public class HTTPServer extends AbstractHandler {

    protected static final String HTML_MARK = "<!-- collab-generate -->";
    protected static final String IMAGES_PREFIX = "";
    protected int port = 8080;
    protected String address = "";
    protected TreeMap<String, HTTPImage> images = new TreeMap<String, HTTPImage>();

    public HTTPServer(int port, String address) {
        this.port = port;
        this.address = address;
        try {
            Server server = new Server(port);
            server.setHandler(this);
            server.start();
        } catch (Exception ex) {
            Logger.getLogger(HTTPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void removeRoomImages(String roomName) {
        String name = "/" + IMAGES_PREFIX + "" + roomName + ".png";
        images.remove(name);
    }

    public URL addImage(BufferedImage image, String roomName) {
        try {
            String name = "/" + IMAGES_PREFIX + "" + roomName + ".png";
            if (images.containsKey(name)) {
                images.remove(name);
            }
            images.put(name, new HTTPImage(image));
            return new URL("http", address, port, name);
        } catch (MalformedURLException ex) {
            Logger.getLogger(HTTPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void handle(String string, Request rqst, HttpServletRequest hsr, HttpServletResponse hsr1) throws IOException, ServletException {
        if (string.equals("/index.html") || string.equals("/")) {
            String rooms = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(HTTPServer.class.getResourceAsStream("/resources/rooms.html")));
            String line = "";
            while ((line = in.readLine()) != null) {
                rooms = rooms + "\n" + line;
            }
            in.close();

            String html = "";
            html = html + "<ul>" + "\n";
            Set<String> keys = images.keySet();
            for (String image : keys) {
                html = html + "<li><a href=\"" + image + "\">" + image.substring(1) + "</a></li>";
            }
            html = html + "</ul>" + "\n";
            rooms = rooms.replaceFirst(HTML_MARK, html);

            hsr1.setContentType("text/html;charset=utf-8");
            hsr1.setStatus(HttpServletResponse.SC_OK);
            rqst.setHandled(true);
            hsr1.getWriter().println(rooms);
        } else if (string.startsWith("/" + IMAGES_PREFIX)) {
            HTTPImage image = images.get(string);
            if (image != null) {
                hsr1.setContentType("image/png");
                hsr1.setStatus(HttpServletResponse.SC_OK);
                rqst.setHandled(true);
                byte[] data = image.getImageData();
                hsr1.getOutputStream().write(data);
            } else {
                hsr1.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            hsr1.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
