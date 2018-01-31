/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import io.github.davidg95.jconn.events.*;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
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

    private static final Logger LOG = Logger.getLogger(TillServer.class.getName());

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
    private int conn_limit = 9999;
    private boolean licensed = true;

    private static boolean headless;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("-------------------------------");
        System.out.println("JTill Server version " + VERSION);
        System.out.println("-------------------------------");
        LOG.log(Level.INFO, "Starting JTill Server");
        try {
            javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            LOG.log(Level.WARNING, "Windows look and feel not supported on this system");
        }
        headless = GraphicsEnvironment.isHeadless();
        final long start = new Date().getTime();
        tillServer = new TillServer();
        tillServer.start();
        final long end = new Date().getTime();
        final long time = end - start;
        LOG.log(Level.INFO, "JTill Server started successfully in " + time / 1000D + "s");
    }

    /**
     * Initialise the server.
     */
    public TillServer() {
        connections = 0;
        TillSplashScreen.showSplashScreen();
        icon = new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/tillIcon.png")).getImage();
        settings = Settings.getInstance();
        DataConnect.set(DBConnect.getInstance());
        TillSplashScreen.setLabel("Loading configurations");
        LOG.info("Loading configurations");
        TillSplashScreen.addBar(5);
        boolean init = settings.loadProperties();
        if (!init) {
            if (!headless) {
                TillSplashScreen.hideSplashScreen();
                init = InitialSetupWindow.showWindow();
                if (!init) {
                    System.exit(0);
                }
                TillSplashScreen.showSplashScreen();
            } else {
                Scanner in = new Scanner(System.in);
                System.out.println("Enter site name:");
                String siteName = in.nextLine();
                System.out.println("Enter port number to use:");
                String port = in.nextLine();
                if (!Utilities.isNumber(port)) {
                    System.out.println("Invalid input");
                    System.exit(0);
                }
                System.out.println("Enter currency symbol to use:");
                String currencySymbol = in.nextLine();
                Settings s = Settings.getInstance();
                s.setSetting("SITE_NAME", siteName);
                s.setSetting("port", port);
                s.setSetting("CURRENCY_SYMBOL", currencySymbol);

                System.out.println("Enter company name:");
                String companyName = in.nextLine();
                System.out.println("Enter company address:");
                String companyAddress = in.nextLine();
                System.out.println("Enter VAT number:");
                String vat = in.nextLine();
                Properties properties = new Properties();
                OutputStream out;
                try {
                    out = new FileOutputStream("company.details");
                    properties.setProperty("NAME", companyName);
                    properties.setProperty("ADDRESS", companyAddress);
                    properties.setProperty("VAT", vat);
                    properties.store(out, null);
                    out.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(TillServer.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(0);
                } catch (IOException ex) {
                    Logger.getLogger(TillServer.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(0);
                }
            }
        }
        TillSplashScreen.addBar(5);
        if (!headless) {
            try {
                g = GUI.create(false, icon);
            } catch (Exception ex) {
                TillSplashScreen.hideSplashScreen();
                JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
                LOG.log(Level.SEVERE, null, ex);
            }
            setSystemTray();
        }
    }

    public void databaseLogin() {
        try {
            DBConnect db = (DBConnect) DataConnect.get();
            TillSplashScreen.setLabel("Connecting to database"); //Update the splash screen
            LOG.info("Connecting to database");
            db.connect(settings.getSetting("db_address"), settings.getSetting("db_username"), settings.getSetting("db_password")); //Open a connection to the database
            if (db.getStaffCount() == 0) { //Check to see if any staff members have been created
                Staff s = new Staff("JTill Admin", Staff.AREA_MANAGER, "admin", "jtill", 0.01, true); //Create the admin member of staff if they do not already exists
                try {
                    db.addStaff(s); //Add the member of staff
                } catch (SQLException ex) {
                    LOG.log(Level.SEVERE, "Error creating Admin staff member", ex);
                }
            }
            TillSplashScreen.addBar(20);
        } catch (SQLException ex) {
            initialSetup(); //If there was an issue connecting to the database, go to the initial setup.
        }
    }

    /**
     * Initial setup which creates the database and prompts the user to create
     * the first member of staff.
     */
    private void initialSetup() {
        try {
            DBConnect db = (DBConnect) DataConnect.get();
            TillSplashScreen.setLabel("Creating database...");
            LOG.info("Creating database");
            db.create(settings.getSetting("db_address") + "create=true;", settings.getSetting("db_username"), settings.getSetting("db_password")); //Create the database
            TillSplashScreen.setLabel("Populating database");
            LOG.info("Populating database");
            db.addCustomer(new Customer("NONE", "", "", "", "", "", "", "", "", "", "", 0, BigDecimal.ZERO)); //Create a blank customer
            if (!headless) {
                Staff s = StaffDialog.showNewStaffDialog(null); //Show the create staff dialog
                if (s == null) {
                    System.exit(0); // Exit if the user clicked cancel
                }
            } else {
                Staff s = new Staff("JTill Admin", Staff.AREA_MANAGER, "admin", "jtill", 0.01, true); //Create the admin member of staff if they do not already exists
                try {
                    db.addStaff(s); //Add the member of staff
                } catch (SQLException ex) {
                    LOG.log(Level.SEVERE, "Error creating Admin staff member", ex);
                }
            }
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 40000) { //If another application is already using the database.
                LOG.log(Level.SEVERE, "The database is already in use. The program will now terminate");
                JOptionPane.showMessageDialog(null, "The database is already in use by another application. Program will now terminate.\nError Code " + ex.getErrorCode(), "Database in use", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            } else {
                LOG.log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, ex, "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void checkUpdate() {
        try {
            String latest = UpdateChecker.checkForUpdate();
            if (!latest.equals(TillServer.VERSION)) {
                if (!headless) {
                    if (JOptionPane.showConfirmDialog(null, "Version " + latest + " avaliable. Download now?", "Update", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        UpdateChecker.downloadServerUpdate();
                    }
                } else {
                    System.out.println("Version " + latest + "avaliable. Download now? <y/n>");
                    Scanner in = new Scanner(System.in);
                    String input = in.next();
                    if (input.equalsIgnoreCase("y")) {
                        LOG.info("Downloading update...");
                        UpdateChecker.downloadServerUpdate();
                        LOG.info("Donwload complete!");
                    }
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error checking for update", ex);
            if (!headless) {
                JOptionPane.showMessageDialog(null, "Error checking for update", "Update", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * Start the server.
     */
    public void start() {
        databaseLogin();
        try {
            TillSplashScreen.setLabel("Starting server socket");
            LOG.info("Starting server socket on port number " + PORT_IN_USE);
            server = JConnServer.start(PORT_IN_USE, ConnectionHandler.class);
            server.registerListener(this);
            DBConnect.getInstance().setServer(server);
            TillSplashScreen.addBar(20);
            LOG.log(Level.INFO, "Listening on port number " + PORT_IN_USE);
        } catch (IOException ex) {
        }
        if (!headless) {
            TillSplashScreen.addBar(10);
            GUI.getInstance().updateLables();
            TillSplashScreen.setLabel("Checking for update");
        }
        LOG.info("Checking for update");
        checkUpdate();
        if (!headless) {
            TillSplashScreen.addBar(10);
            TillSplashScreen.hideSplashScreen();
            g.setVisible(true);
        }
//        LicenseThread thread = new LicenseThread();
//        thread.start();
//        licensed = checkLicense();
//        File f = new File("license.dat");
//        if (f.exists()) {
//            licensed = true;
//        } else {
//            LicenseWindow.showWindow(null);
//            licensed = true;
//        }
        if (headless) {
            try {
                LOG.info("Checking database integrity");
                DataConnect.get().integrityCheck();
                LOG.info("Check complete");
            } catch (IOException | SQLException ex) {
                LOG.log(Level.SEVERE, "Error checking database", ex);
            }
        } else {
            g.checkDatabase();
            g.login();
        }
        if (licensed) {
//            g.checkDatabase();
//            g.login();
        } else {
            JOptionPane.showMessageDialog(null, "License file not detected. JTill Server will now close", "License Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private boolean checkLicense() {
        for (File r : File.listRoots()) {
            try {
                for (File f : r.listFiles()) {
                    if (f.getName().equals("license.txt")) {
                        try {
                            String hash = fileHash(f);
                            JOptionPane.showMessageDialog(null, "License Hash-\n" + hash);
                            Scanner in = new Scanner(f);
                            String line = in.nextLine();
                            String liNo = line.substring(line.indexOf(":") + 2);
                            line = in.nextLine();
                            conn_limit = Integer.parseInt(line.substring(line.indexOf(":") + 2));
                            DBConnect.getInstance().setLicenseInfo(liNo, conn_limit);
                            return true;
                        } catch (FileNotFoundException ex) {
                            JOptionPane.showMessageDialog(null, "Error loading license file. JTill Server will now close.", "License", JOptionPane.ERROR_MESSAGE);
                            System.exit(0);
                        }
                    }
                }
            } catch (Exception e) {

            }
        }
        return false;
    }

    private String fileHash(File f) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            Scanner in = new Scanner(f);
            String s = "";
            while (in.hasNext()) {
                s += in.next();
            }
            byte[] byteHash = md5.digest(s.getBytes());
            return new String(byteHash);
        } catch (NoSuchAlgorithmException | FileNotFoundException ex) {
            Logger.getLogger(TillServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private class LicenseThread extends Thread {

        public LicenseThread() {

        }

        @Override
        public void run() {
            Main:
            while (true) {
                try {
                    Thread.sleep(5000);
                    for (File r : File.listRoots()) {
                        try {
                            for (File f : r.listFiles()) {
                                if (f.getName().equals("license.txt")) {
                                    continue Main;
                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                    JOptionPane.showMessageDialog(null, "License file not detected. JTill Server will now close", "License Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TillServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
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
                    + DataConnect.get().toString(), "JTill Server",
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
    public void onReceive(JConnReceiveEvent data) {
    }

    @Override
    public void onConnectionDrop(JConnEvent event) {
        connections--;
        if (!headless) {
            g.setClientLabel(connections);
        }
    }

    @Override
    public void onConnectionEstablish(JConnEvent event) {
        if (connections >= conn_limit) {
            event.setCancelled(true);
        }
        connections++;
        if (!headless) {
            g.setClientLabel(connections);
        }
    }

    @Override
    public void onServerGracefulEnd() {
        connections--;
        if (!headless) {
            g.setClientLabel(connections);
        }
    }
}
