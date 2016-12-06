/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.tillserver;

import io.github.davidg95.Till.till.DBConnect;
import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Timer;
import javax.swing.JOptionPane;

/**
 *
 * @author 1301480
 */
public class TillServer {

    public static int PORT = 600;
    public static int MAX_CONNECTIONS = 10;
    public static int MAX_QUEUE = 10;

    private ServerSocket s;
    public static Data data;
    public static GUI g;
    private ConnectionAcceptThread connThread;

    public static DBConnect dbConnection;

    public static Image icon;

    public static Timer updateTimer;
//    public static DatabaseUpdate updateTask;
    public static long updateInterval = 60000L;

    private Properties properties;

    private static String hostName;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new TillServer().start();
    }

    public TillServer() {
        icon = new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/resources/tillIcon.png")).getImage();
        loadProperties();
        dbConnection = new DBConnect();
        data = new Data(dbConnection, g);
        g = new GUI(data, dbConnection);
        setSystemTray();
        try {
            s = new ServerSocket(PORT);
            connThread = new ConnectionAcceptThread(s, data);
        } catch (IOException ex) {
        }
    }

    public void start() {
        TillSplashScreen.showSplashScreen();
        g.databaseLogin();
        if (connThread != null) {
            connThread.start();
        }
        TillSplashScreen.hideSplashScreen();
        g.setVisible(true);
        g.login();
    }

    private void loadProperties() {
        properties = new Properties();
        InputStream in;

        try {
            in = new FileInputStream("server.properties");

            properties.load(in);

            hostName = properties.getProperty("host");

            in.close();
        } catch (FileNotFoundException | UnknownHostException ex) {
            saveProperties();
        } catch (IOException ex) {
        }
    }

    private void saveProperties() {
        properties = new Properties();
        OutputStream out;

        try {
            out = new FileOutputStream("server.properties");

            hostName = InetAddress.getLocalHost().getHostName();

            properties.setProperty("host", hostName);

            properties.store(out, null);
            out.close();
        } catch (FileNotFoundException | UnknownHostException ex) {
        } catch (IOException ex) {
        }
    }

    private void setSystemTray() {
        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon
                = new TrayIcon(icon);
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a pop-up menu components
        MenuItem aboutItem = new MenuItem("About");

        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "JTill Server is running on port number "
                        + PORT + " with " + g.clientCounter + " connections.\n"
                        + dbConnection.toString(), "JTill Server",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        //Add components to pop-up menu
        popup.add(aboutItem);

        trayIcon.setPopupMenu(popup);
        trayIcon.setToolTip("JTill Server is running");

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    public static DBConnect getDBConnection() {
        return dbConnection;
    }

    public static Data getData() {
        return data;
    }

    public static Image getIcon() {
        return icon;
    }

    public static String getHostName() {
        return hostName;
    }

//    public static void setUpdateTimer() {
//        updateTimer = new Timer();
//        updateTimer.schedule(updateTask, 10000L, updateInterval);
//    }
//
//    public static void resetUpdateTimer() {
//        updateTask.cancel();
//        updateTimer.cancel();
//        updateTimer.purge();
//        setUpdateTimer();
//    }
//    /**
//     * Timer class for updating the database.
//     */
//    public class DatabaseUpdate extends TimerTask {
//
//        @Override
//        public void run() {
//            try {
//                if (dbConnection.isConnected()) {
//                    g.setUpdateLabel("Updating Database");//Set the label
//                    g.log("Updating database");
//                    data.updateDatabase();
//                    new Timer().schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            SwingUtilities.invokeLater(() -> {
//                                g.setUpdateLabel("");
//                            });
//                        }
//
//                    }, 5000L); //Clear the label after 5 seconds
//                }
//            } catch (SQLException ex) {
//                Logger.getLogger(TillServer.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//
//    }
}
