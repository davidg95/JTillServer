/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.TillSplashScreen;
import io.github.davidg95.JTill.jtill.*;
import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Main starting class.
 *
 * @author 1301480
 */
public class TillServer {

    /**
     * The Connection to the database.
     */
    public DBConnect dc;
    /**
     * The GUI.
     */
    public static GUI g;
    /**
     * The thread which handle incoming connections.
     */
    private ConnectionAcceptThread connThread;

    /**
     * The image icon for the windows.
     */
    public static Image icon;
    /**
     * The tray icon for the system tray.
     */
    public static TrayIcon trayIcon;
    public static SystemTray tray;

    private Settings settings;
    
    public static final String VERSION = "v0.0.1";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(TillServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Headless operation not currently supported");
            System.exit(0);
        }
        new TillServer().start();
    }

    /**
     * Initialise the server.
     */
    public TillServer() {
        TillSplashScreen.showSplashScreen();
        icon = new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/tillIcon.png")).getImage();
        settings = Settings.getInstance();
        dc = new DBConnect();
        TillSplashScreen.setLabel("Loading configurations");
        TillSplashScreen.addBar(5);
        settings.loadProperties();
        TillSplashScreen.addBar(5);
        if (!GraphicsEnvironment.isHeadless()) {
            g = GUI.create(dc, false, icon);
            setSystemTray();
        }
        try {
            TillSplashScreen.setLabel("Starting server socket");
            connThread = new ConnectionAcceptThread(dc);
            TillSplashScreen.addBar(10);
        } catch (IOException ex) {
        }
    }

    /**
     * Start the server.
     */
    public void start() {
        g.databaseLogin();
        if (connThread != null) {
            connThread.start();
        }
        TillSplashScreen.addBar(24);
        TillSplashScreen.hideSplashScreen();
        g.setVisible(true);
        GUI.getInstance().updateLables();
        g.checkUpdate();
        g.login();
    }

    /**
     * Method to load the system tray.
     */
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
                    + settings.getSetting("port") + " with " + g.clientCounter + " connections.\n"
                    + dc.toString(), "JTill Server",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        showItem.addActionListener((ActionEvent e) -> {
            g.setVisible(true);
            if (!g.isLoggedOn()) {
                g.login();
            }
        });

        statusItem.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(null, "Connected Clients: " + g.clientCounter + "/" + settings.getSetting("max_conn")
                    + "\nDatabase Address- " + dc.getAddress()
                    + "\nDatabase User- " + dc.getUsername(),
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

    /**
     * Method to remove the system tray icon.
     */
    public static void removeSystemTrayIcon() {
        tray.remove(trayIcon);
    }
}
