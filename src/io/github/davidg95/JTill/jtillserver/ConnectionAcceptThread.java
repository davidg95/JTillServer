/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread which accepts incoming connections from clients.
 *
 * @author David
 */
public class ConnectionAcceptThread extends Thread {

    private static final Logger LOG = Logger.getGlobal();

    /**
     * The port which is being used by the server.
     */
    public static int PORT_IN_USE;

    private final Settings settings;

    private final ServerSocket socket;
    
    public static List<ConnectionThread> connections;

    /**
     * Constructor which starts the ThreadPoolExcecutor.
     *
     * @param PORT the port number to listen on.
     * @throws IOException if there was a network error.
     */
    public ConnectionAcceptThread(int PORT) throws IOException {
        super("ConnectionAcceptThread");
        this.socket = new ServerSocket(PORT);
        PORT_IN_USE = PORT;
        settings = Settings.getInstance();
        connections = new ArrayList<>();
    }

    /**
     * Constructor which starts the ThreadPoolExcecutor on the default port.
     *
     * @throws IOException if there was a network error.
     */
    public ConnectionAcceptThread() throws IOException {
        this(Settings.DEFAULT_PORT);
    }
    
    public static void removeConnection(ConnectionThread th){
        connections.remove(th);
    }

    @Override
    public void run() {
        LOG.log(Level.INFO, "Starting Thread Pool Excecutor");
        ThreadPoolExecutor pool = new ThreadPoolExecutor(Integer.parseInt(settings.getSetting("max_conn")), Integer.parseInt(settings.getSetting("max_queue")), 50000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(Integer.parseInt(settings.getSetting("max_queue"))));
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        try {
            LOG.log(Level.INFO, "JTill Server local IP address is " + InetAddress.getLocalHost().getHostAddress());
            LOG.log(Level.INFO, "Server Socket running on port number " + PORT_IN_USE);
        } catch (UnknownHostException ex) {
            LOG.log(Level.WARNING, "For some reason, the ip address of the local server could not be retrieved");
        }
        LOG.log(Level.INFO, "Ready to accept connections");
        for (;;) {
            try {
                Socket incoming = socket.accept(); //Wait for a connection.
                ConnectionThread th = new ConnectionThread(socket.getInetAddress().getHostAddress(), incoming);
                pool.submit(th); //Submit the socket to the excecutor.
                connections.add(th);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }
}
