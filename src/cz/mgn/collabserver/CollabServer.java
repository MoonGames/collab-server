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
            } else if ("-h".equals(args[i])) {
                http = true;
            } else if ("-a".equals(args[i])) {
                i++;
                if (i < args.length) {
                    address = args[i];
                }
            } else if ("-ph".equals(args[i])) {
                i++;
                if (i < args.length) {
                    try {
                        httpPort = Integer.parseInt(args[i]);
                    } catch (NumberFormatException ex) {
                    }
                }
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
