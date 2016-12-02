/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.tillserver;

import io.github.davidg95.Till.till.DBConnect;
import java.awt.Image;
import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 *
 * @author 1301480
 */
public class TillServer {

    public static int PORT = 600;
    public static int MAX_CONNECTIONS = 10;
    public static int MAX_QUEUE = 10;

    private Semaphore productsSem;
    private Semaphore customersSem;
    private Semaphore salesSem;
    private Semaphore staffSem;

    private ServerSocket s;
    public static Data data;
    public static GUI g;
    private ConnectionAcceptThread connThread;

    public static DBConnect dbConnection;
    
    public static Image icon;

    public static Timer updateTimer;
    public static DatabaseUpdate updateTask;
    public static long updateInterval = 60000L;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new TillServer().start();
    }

    public TillServer() {
        icon = new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/resources/tillIcon.png")).getImage();
        dbConnection = new DBConnect();
        data = new Data(dbConnection, g);
        g = new GUI(data, dbConnection);
        updateTask = new DatabaseUpdate();
        productsSem = new Semaphore(1);
        customersSem = new Semaphore(1);
        salesSem = new Semaphore(1);
        staffSem = new Semaphore(1);
        try {
            s = new ServerSocket(PORT);
            connThread = new ConnectionAcceptThread(s, productsSem, customersSem, salesSem, staffSem, data);
        } catch (IOException ex) {
        }
    }

    public void start() {
        TillSplashScreen.showSplashScreen();
        connThread.start();
        g.databaseLogin();
        setUpdateTimer();
        TillSplashScreen.hideSplashScreen();
        g.setVisible(true);
        g.login();
    }

    public static DBConnect getDBConnection() {
        return dbConnection;
    }

    public static Data getData() {
        return data;
    }
    
    public static Image getIcon(){
        return icon;
    }

    public static void setUpdateTimer() {
        updateTimer = new Timer();
        updateTimer.schedule(updateTask, 10000L, updateInterval);
    }

    public static void resetUpdateTimer() {
        updateTask.cancel();
        updateTimer.cancel();
        updateTimer.purge();
        setUpdateTimer();
    }

    /**
     * Timer class for updating the database.
     */
    public class DatabaseUpdate extends TimerTask {

        @Override
        public void run() {
            try {
                if (dbConnection.isConnected()) {
                    g.setUpdateLabel("Updating Database");//Set the label
                    g.log("Updating database");
                    data.updateDatabase();
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(() -> {
                                g.setUpdateLabel("");
                            });
                        }

                    }, 5000L); //Clear the label after 5 seconds
                }
            } catch (SQLException ex) {
                Logger.getLogger(TillServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
