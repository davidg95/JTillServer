/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.DBConnect;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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

    private final ServerSocket socket;

    public ConnectionAcceptThread(ServerSocket s) {
        super("ConnectionAcceptThread");
        this.socket = s;
    }

    @Override
    public void run() {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(DBConnect.MAX_CONNECTIONS, DBConnect.MAX_QUEUE, 50000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(DBConnect.MAX_QUEUE));
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        TillServer.g.log("Ready to accept connections");
        for (;;) {
            try {
                Socket incoming = socket.accept();
                pool.submit(new ConnectionThread(socket.getInetAddress().getHostAddress(), incoming));
            } catch (IOException ex) {
                Logger.getLogger(ConnectionAcceptThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
