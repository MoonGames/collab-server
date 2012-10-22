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
package cz.mgn.collabserver;

import cz.mgn.collabserver.http.HTTPServer;
import cz.mgn.collabserver.server.Server;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author indy
 */
public class CollabServer {

    public static final int LOG_LEVEL_ALL = 3;
    public static final int LOG_LEVEL_MEDIUM = 1;
    public static final int LOG_LEVEL_ERROR = 0;
    public static final String VERSION = "1.0 pre-alpha 3";
    public static final String HELP_LOG_LEVLES = "\t\t" + LOG_LEVEL_ERROR + " - error level" + "\n"
            + "\t\t" + LOG_LEVEL_MEDIUM + " - medium level (default)" + "\n"
            + "\t\t" + LOG_LEVEL_ALL + " - log all level" + "\n";
    public static final String HELP = "\n"
            + "This is Collab server version " + VERSION + ", see http://collab.mgn.cz/ for more info."
            + "\n\n"
            + "\t" + "-p" + "\t\t" + "listening port" + "\n"
            + "\t" + "-w" + "\t\t" + "starts HTTP subserver" + "\n"
            + "\t" + "-a" + "\t\t" + "server address (collab.example.com)" + "\n"
            + "\t" + "-q" + "\t\t" + "HTTP subserver listening port" + "\n"
            + "\t" + "-l --log" + "\t" + "sets log level" + "\n"
            + "\n" + HELP_LOG_LEVLES + "\n"
            + "\t" + "-f" + "\t\t" + "log to file" + "\n"
            + "\t" + "-h --help" + "\t" + "shows help" + "\n"
            + "\t" + "-v --version" + "\t" + "shows version" + "\n";
    protected static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    protected static HTTPServer httpServer = null;
    protected static int logLevel = LOG_LEVEL_MEDIUM;
    protected static PrintWriter logToFile = null;

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
            } else if ("-l".equals(args[i]) || "--log".equals(args[i])) {
                i++;
                if (i < args.length) {
                    try {
                        logLevel = Integer.parseInt(args[i]);
                    } catch (NumberFormatException ex) {
                    }
                }
            } else if ("-f".equals(args[i])) {
                i++;
                if (i < args.length) {
                    try {
                        String fileName = args[i];
                        File file = new File(fileName);
                        boolean can = file.exists() && file.isFile() && file.canWrite();
                        can = can || (file.getParentFile() != null && file.getParentFile().exists() && file.getParentFile().canWrite() && file.createNewFile());
                        if (can) {
                            logToFile = new PrintWriter(new FileWriter(file, true));
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(CollabServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        if (http) {
            startServerWithHTTP(httpPort, address, port);
        } else {
            startServer(port);
        }
    }

    public static void startServerWithHTTP(int httpPort, String serverAddress, int port) {
        httpServer = new HTTPServer(httpPort, serverAddress);
        Server main = new Server("main thread", port);
        main.start();
    }

    public static void startServer(int port) {
        Server main = new Server("main thread", port);
        main.start();
    }

    public static void changeLogLevel(int logLevel) {
        CollabServer.logLevel = logLevel;
    }

    public static HTTPServer getHTTPServer() {
        return httpServer;
    }

    public static void logLevelAll(Server server, String message) {
        log(server, message, LOG_LEVEL_ALL);
    }

    public static void logLevelMedium(Server server, String message) {
        log(server, message, LOG_LEVEL_MEDIUM);
    }

    public static void logLevelError(Server server, String message) {
        log(server, message, LOG_LEVEL_ERROR);
    }

    public static void log(Server server, String message, int level) {
        if (level <= logLevel) {
            String time = "(" + dateFormat.format(new Date()) + ")";
            String text = "server: " + server + " message: " + message;
            String string;
            if (level <= LOG_LEVEL_ERROR) {
                string = "error " + time + ": " + text;
                System.err.println(string);
            } else {
                string = "log " + time + ": " + text;
                System.out.println(string);
            }
            if (logToFile != null) {
                logToFile.println(string);
                logToFile.flush();
            }
        }
    }
}
