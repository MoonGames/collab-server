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
package cz.mgn.collabserver;

import cz.mgn.collabserver.http.HTTPServer;
import cz.mgn.collabserver.server.Server;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author indy
 */
public class CollabServer {

    public static final String VERSION = "1.0 pre-alpha 3";
    public static final String HELP = "\n"
            + "This is Collab server version " + VERSION + ", see http://collab.mgn.cz/ for more info."
            + "\n\n"
            + "\t" + "-p" + "\t\t" + "listening port" + "\n"
            + "\t" + "-w" + "\t\t" + "starts HTTP subserver" + "\n"
            + "\t" + "-a" + "\t\t" + "server address (collab.example.com)" + "\n"
            + "\t" + "-q" + "\t\t" + "HTTP subserver listening port" + "\n"
            + "\t" + "-h --help" + "\t" + "shows help" + "\n"
            + "\t" + "-v --version" + "\t" + "shows version" + "\n";
    protected static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    protected static HTTPServer httpServer = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int port = 30125;
        int httpPort = 8080;
        boolean http = false;
        String address = "";
        for (int i = 0; i < args.length; i++) {
            if ("-p".equals(args[i])) {
                i++;
                if (i < args.length) {
                    try {
                        port = Integer.parseInt(args[i]);
                    } catch (NumberFormatException ex) {
                    }
                }
            } else if ("-w".equals(args[i])) {
                http = true;
            } else if ("-a".equals(args[i])) {
                i++;
                if (i < args.length) {
                    address = args[i];
                }
            } else if ("-q".equals(args[i])) {
                i++;
                if (i < args.length) {
                    try {
                        httpPort = Integer.parseInt(args[i]);
                    } catch (NumberFormatException ex) {
                    }
                }
            } else if ("-h".equals(args[i]) || "--help".equals(args[i])) {
                System.out.println(HELP);
                System.exit(0);
            } else if ("-v".equals(args[i]) || "--version".equals(args[i])) {
                System.out.println(VERSION);
                System.exit(0);
            }
        }
        if (http) {
            httpServer = new HTTPServer(httpPort, address);
        }
        Server main = new Server("main thread", port);
        main.start();
    }

    public static HTTPServer getHTTPServer() {
        return httpServer;
    }

    public static void log(Server server, String message, boolean error) {
        String time = "(" + dateFormat.format(new Date()) + ")";
        String text = "server: " + server + " message: " + message;
        if (error) {
            System.err.println("error " + time + ": " + text);
        } else {
            System.out.println("log " + time + ": " + text);
        }
    }
}
