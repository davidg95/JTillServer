/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import io.github.davidg95.jconn.JConnData;
import io.github.davidg95.jconn.JConnEvent;
import io.github.davidg95.jconn.JConnListener;
import io.github.davidg95.jconn.JConnServer;
import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Main starting class.
 *
 * @author 1301480
 */
public class TillServer implements JConnListener {

    private GUI g;

    public static Image icon;

    //System tray
    public static TrayIcon trayIcon;
    public static SystemTray tray;

    private final Settings settings;

    public static final String VERSION = "v0.0.1";

    public static final int PORT_IN_USE = 52341;

    public static JConnServer server;

    private static TillServer tillServer;

    private int connections;

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
            System.out.println("***Headless operation not currently supported***");
            System.out.println("Press any key to exit");
            new Scanner(System.in).next();
            System.exit(0);
        }
        tillServer = new TillServer();
        tillServer.start();
    }

    /**
     * Initialise the server.
     */
    public TillServer() {
        connections = 0;
        TillSplashScreen.showSplashScreen();
        icon = new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/tillIcon.png")).getImage();
        settings = Settings.getInstance();
        DataConnect.dataconnect = DBConnect.getInstance();
        TillSplashScreen.setLabel("Loading configurations");
        TillSplashScreen.addBar(5);
        boolean init = settings.loadProperties();
        if (!init) {
            TillSplashScreen.hideSplashScreen();
            init = InitialSetupWindow.showWindow();
            if (!init) {
                System.exit(0);
            }
            TillSplashScreen.showSplashScreen();
        }
        TillSplashScreen.addBar(5);
        try {
            g = GUI.create(false, icon);
        } catch (Exception ex) {
            TillSplashScreen.hideSplashScreen();
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(TillServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        setSystemTray();
    }

    /**
     * Start the server.
     */
    public void start() {
        g.databaseLogin();
        try {
            TillSplashScreen.setLabel("Starting server socket");
            server = JConnServer.start(PORT_IN_USE, ConnectionHandler.class);
            server.registerListener(this);
            DBConnect.getInstance().setServer(server);
            TillSplashScreen.addBar(20);
        } catch (IOException ex) {
        }
        TillSplashScreen.addBar(10);
        GUI.getInstance().updateLables();
        TillSplashScreen.setLabel("Checking for update");
        g.checkUpdate();
        TillSplashScreen.addBar(10);
        TillSplashScreen.hideSplashScreen();
        g.setVisible(true);
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
                    + settings.getSetting("port") + "\n"
                    + DataConnect.dataconnect.toString(), "JTill Server",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        showItem.addActionListener((ActionEvent e) -> {
            g.setVisible(true);
            if (!g.isLoggedOn()) {
                g.login();
            }
        });

        statusItem.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(null, "Database Address- " + DBConnect.getInstance().getAddress()
                    + "\nDatabase User- " + DBConnect.getInstance().getUsername(),
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

    @Override
    public void onReceive(JConnData data) {
    }

    @Override
    public void onConnectionDrop(JConnEvent event) {
        connections--;
        g.setClientLabel(connections);
    }

    @Override
    public void onConnectionEstablish(JConnEvent event) {
        connections++;
        g.setClientLabel(connections);
    }

    @Override
    public void onServerGracefulEnd() {
        connections--;
        g.setClientLabel(connections);
    }
}
