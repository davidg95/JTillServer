/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.tillserver;

import io.github.davidg95.Till.till.DBConnect;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Semaphore;

/**
 *
 * @author 1301480
 */
public class TillServer {
    
    private String database_address = "jdbc:derby://localhost:1527/TillTest";
    private String username = "davidg95";
    private String password = "adventures";
    
    public static final int PORT = 600;
    public static final int MAX_CONNECTIONS = 10;
    public static final int MAX_QUEUE = 10;

    private Semaphore productsSem;
    private Semaphore customersSem;
    private Semaphore salesSem;

    private ServerSocket s;
    private Data data;
    private GUI g;
    private ConnectionAcceptThread connThread;
    
    private DBConnect dbConnection;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new TillServer().start();
    }
    
    public TillServer(){
        g = new GUI(data, dbConnection);
        productsSem = new Semaphore(1);
        customersSem = new Semaphore(1);
        salesSem = new Semaphore(1);
        try{
            s = new ServerSocket(PORT);
            connThread = new ConnectionAcceptThread(s, productsSem, customersSem, salesSem, data);
        } catch (IOException ex) {
        }
    }
    
    public void start(){
        connThread.start();
        g.setVisible(true);
    }
    
}
