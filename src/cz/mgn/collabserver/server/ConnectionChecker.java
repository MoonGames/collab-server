/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.mgn.collabserver.server;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *      @author indy
 */
public class ConnectionChecker extends Thread {

    protected volatile boolean running = false;
    protected static final long INTERVAL = 2000;
    protected Server server = null;

    public ConnectionChecker(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            server.checkConnections();
            try {
                Thread.sleep(INTERVAL);
            } catch (InterruptedException ex) {
                Logger.getLogger(ConnectionChecker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void stopChecking() {
        running = false;
    }
}
