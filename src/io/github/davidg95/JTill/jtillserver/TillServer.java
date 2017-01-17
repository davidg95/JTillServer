/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.DBConnect;
import io.github.davidg95.JTill.jtill.DataConnectInterface;
import io.github.davidg95.JTill.jtill.LoginException;
import io.github.davidg95.JTill.jtill.Staff;
import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Timer;
import javax.swing.JOptionPane;

/**
 *
 * @author 1301480
 */
public class TillServer {

    public static Data data;
    public static DBConnect dbConnection;
    public static GUI g;

    private ServerSocket s;
    private ConnectionAcceptThread connThread;

    public static Timer updateTimer;
//    public static DatabaseUpdate updateTask;
    public static long updateInterval = 60000L;

    public static Image icon;
    public static TrayIcon trayIcon;
    public static SystemTray tray;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Headless operation not currently supported");
            System.exit(0);
        }
        new TillServer().start();
    }

    public TillServer() {
        icon = new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/tillIcon.png")).getImage();
        dbConnection = new DBConnect();
        dbConnection.loadProperties();
        data = new Data(dbConnection, g);
        if (!GraphicsEnvironment.isHeadless()) {
            g = new GUI(data, dbConnection, false);
            setSystemTray();
        }
        try {
            s = new ServerSocket(DBConnect.PORT);
            connThread = new ConnectionAcceptThread(s, data);
        } catch (IOException ex) {
        }
    }

    public void start() {
        if (!GraphicsEnvironment.isHeadless()) {
            TillSplashScreen.showSplashScreen();
            g.databaseLogin();
        } else {
            headlessDatabaseLogin();
        }
        if (connThread != null) {
            connThread.start();
        }
        TillSplashScreen.addBar(50);
        if (!GraphicsEnvironment.isHeadless()) {
            TillSplashScreen.hideSplashScreen();
            g.setVisible(true);
            g.login();
        } else {
            headlessLogin();
        }
    }

    public void headlessDatabaseLogin() {
        try {
            dbConnection.connect("jdbc:derby:TillEmbedded;create=false", "APP", "App");
        } catch (SQLException ex) {
        }
    }

    public void headlessLogin() {
        Staff staff;
        System.out.println("Enter Username");
        String username = new Scanner(System.in).nextLine();
        System.out.println("Enter password");
        String password = new Scanner(System.in).nextLine();

        if (username.equals("") || password.equals("")) {
            headlessLogin();
        }
        try {
            staff = data.login(username, password);
            System.out.println("You are logged in");
        } catch (IOException | LoginException | SQLException ex) {
            System.out.println(ex);
        }
    }

    private void setSystemTray() {
        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        trayIcon = new TrayIcon(icon);
        tray = SystemTray.getSystemTray();
        
        trayIcon.setImageAutoSize(true);

        // Create a pop-up menu components
        MenuItem aboutItem = new MenuItem("About");
        MenuItem showItem = new MenuItem("Show");
        MenuItem statusItem = new MenuItem("Status");

        aboutItem.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(null, "JTill Server is running on port number "
                    + DBConnect.PORT + " with " + g.clientCounter + " connections.\n"
                    + dbConnection.toString(), "JTill Server",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        showItem.addActionListener((ActionEvent e) -> {
            g.setVisible(true);
            if (!g.isLoggedOn()) {
                g.login();
            }
        });

        statusItem.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(null, "Connected Clients: " + g.clientCounter + "/" + DBConnect.MAX_CONNECTIONS
                    + "\nDatabase Address- " + dbConnection.getAddress()
                    + "\nDatabase User- " + dbConnection.getUsername(),
                    "JTill Server Status", JOptionPane.INFORMATION_MESSAGE);
        });

        //Add components to pop-up menu
        popup.add(aboutItem);
        popup.add(showItem);
        popup.add(statusItem);

        trayIcon.setPopupMenu(popup);
        trayIcon.setToolTip("JTill Server is running");

        trayIcon.addActionListener((ActionEvent e) -> {
            g.setVisible(true);
            if (!g.isLoggedOn()) {
                g.login();
            }
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    public static void removeSystemTrayIcon() {
        tray.remove(trayIcon);
    }

    public static DataConnectInterface getDataConnection() {
        DataConnectInterface dbi = dbConnection;
        return dbi;
    }

    public static Data getData() {
        return data;
    }

    public static Image getIcon() {
        return icon;
    }

    public static String getHostName() {
        return DBConnect.hostName;
    }
}
