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

    private static final Logger log = Logger.getGlobal();

    public static int PORT_IN_USE;

    private final Settings settings;

    private final ServerSocket socket;
    private final DataConnect dc;

    public ConnectionAcceptThread(DataConnect dc, int PORT) throws IOException {
        super("ConnectionAcceptThread");
        this.socket = new ServerSocket(PORT);
        PORT_IN_USE = PORT;
        this.dc = dc;
        settings = Settings.getInstance();
    }

    public ConnectionAcceptThread(DataConnect dc) throws IOException {
        this(dc, Settings.DEFAULT_PORT);
    }

    @Override
    public void run() {
        log.log(Level.INFO, "Starting Thread Pool Excecutor");
        ThreadPoolExecutor pool = new ThreadPoolExecutor(Integer.parseInt(settings.getSetting("max_conn")), Integer.parseInt(settings.getSetting("max_queue")), 50000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(Integer.parseInt(settings.getSetting("max_queue"))));
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        try {
            log.log(Level.INFO, "JTill Server local IP address is " + InetAddress.getLocalHost().getHostAddress());
            log.log(Level.INFO, "Server Socket running on port number " + PORT_IN_USE);
        } catch (UnknownHostException ex) {
            log.log(Level.WARNING, "For some reason, the ip address of the local server could not be retrieved");
        }
        log.log(Level.INFO, "Ready to accept connections");
        for (;;) {
            try {
                Socket incoming = socket.accept();
                pool.submit(new ConnectionThread(socket.getInetAddress().getHostAddress(), dc, incoming));
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        }
    }
}
