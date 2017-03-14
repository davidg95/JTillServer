/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * The main GUI for the server.
 *
 * @author David
 */
public class GUI extends javax.swing.JFrame implements GUIInterface {

    private final Logger log = Logger.getGlobal();

    /**
     * Indicates whether severe messages should show.
     */
    public static boolean SHOW_SEVERE = true;
    /**
     * Indicates whether info messages should show
     */
    public static boolean SHOW_INFO = true;
    /**
     * Indicates whether warning messages should show.
     */
    public static boolean SHOW_WARNING = true;

    private static GUI gui;

    private final DataConnect dc;
    private boolean isLoggedOn;

    public static Staff staff;

    public int clientCounter = 0;
    private final ArrayList<String> connections;

    private final boolean remote;

    private final Image icon;

    private final Settings settings;

    /**
     * Creates new form GUI
     *
     * @param dataConnect
     * @param remote flag indicating whether this is a remote connection or not.
     * @param icon
     */
    public GUI(DataConnect dataConnect, boolean remote, Image icon) {
        this.dc = dataConnect;
        this.remote = remote;
        this.icon = icon;
        this.settings = Settings.getInstance();
        this.setIconImage(icon);
        initComponents();
        try {
            lblServerAddress.setText("Local Server Address: " + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException ex) {
            lblServerAddress.setText("Local Server Address: UNKNOWN");
        }
        connections = new ArrayList<>();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.dc.setGUI(this);
        log.addHandler(new LogHandler());
    }

    /**
     * Creates a new instance of the GUI.
     *
     * @param dataConnect the data connection.
     * @param remote if it is a remote session.
     * @param icon the icon for the windows and dialogs.
     * @return the GUI.
     */
    public static GUI create(DataConnect dataConnect, boolean remote, Image icon) {
        gui = new GUI(dataConnect, remote, icon);
        return gui;
    }

    /**
     * Method to return the instance of the GUI. May return null.
     *
     * @return the GUI. May be null.
     */
    public static GUI getInstance() {
        return gui;
    }

    public void updateLables() {
        try {
            lblServerAddress.setText("Local Server Address: " + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException ex) {
            lblServerAddress.setText("Local Server Address: UNKNOWN");
        }
        lblPort.setText("Port Number: " + ConnectionAcceptThread.PORT_IN_USE);
        try {
            lblProducts.setText("Products in database: " + dc.getAllProducts().size());
        } catch (IOException | SQLException ex) {
            lblProducts.setText("Products in database: UNKNOWN");
        }
    }

    /**
     * Initial setup of the gui which creates the database.
     */
    private void initialSetup() {
        if (dc instanceof DBConnect) {
            try {
                DBConnect db = (DBConnect) dc;
                TillSplashScreen.setLabel("Creating database...");
                db.create(settings.getSetting("db_address") + "create=true;", settings.getSetting("db_username"), settings.getSetting("db_password"));
                TillSplashScreen.setLabel("Creating tables...");
                Staff s = StaffDialog.showNewStaffDialog(this, db);
                if (s == null) {
                    System.exit(0);
                }
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 40000) {
                    log.log(Level.SEVERE, "The database is already in use. The program will now terminate");
                    JOptionPane.showMessageDialog(this, "The database is already in use by another application. Program will now terminate.\nError Code " + ex.getErrorCode(), "Database in use", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                } else {
                    log.log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(this, ex, "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {

        }
    }

    /**
     * Method to log in to the database. this will not attempt login if the
     * remote flag has indicated this this is a remote connection to the main
     * server.
     */
    public void databaseLogin() {
        if (!remote) {
            try {
                DBConnect db = (DBConnect) dc;
                db.connect(settings.getSetting("db_address"), settings.getSetting("db_username"), settings.getSetting("db_password"));
                TillSplashScreen.setLabel("Connected to database");
                TillSplashScreen.addBar(40);
                lblDatabase.setText("Connected to database");
                if (dc.getStaffCount() == 0) {
                    Staff s = StaffDialog.showNewStaffDialog(this, db);
                    if (s == null) {
                        System.exit(0);
                    }
                }
            } catch (SQLException ex) {
                initialSetup();
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, ex, "Server Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            lblDatabase.setText("Connected to JTill Server");
        }
    }

    public void setUpdateLabel(String text) {
        lblUpdate.setText(text);
    }

    public void increaceClientCount(String site) {
        connections.add(site);
        clientCounter++;
        lblClients.setText("Connections: " + clientCounter);
    }

    public void decreaseClientCount(String site) {
        connections.remove(site);
        clientCounter--;
        lblClients.setText("Connections: " + clientCounter);
    }

    @Override
    public void setClientLabel(String text) {
        lblClients.setText(text);
    }

    /**
     * Method to log something in the server log at the bottom of the screen.
     *
     * @param o the object to log.
     */
    @Override
    public void log(Object o) {
        txtLog.append(o.toString() + "\n");
    }

    @Override
    public void logWarning(Object o) {
        txtStockWarnings.append(o + "\n");
    }

    /**
     * Method to log a member of staff in to the server.
     */
    public void login() {
        staff = LoginDialog.showLoginDialog(this, dc);
        if (staff != null) {
            lblUser.setText(staff.getName());
            itemLogin.setText("Log Out");
            log.log(Level.INFO, staff.getName() + " has logged in");
            isLoggedOn = true;
        } else {
            if (dc instanceof DBConnect) {
                settings.saveProperties();
            }
            if (remote) {
                try {
                    ((ServerConnection) dc).close();
                } catch (IOException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
                System.exit(0);
            }
            if (SystemTray.isSupported()) {
                this.setVisible(false);
                TillServer.trayIcon.displayMessage("JTill Server is still running", "JTill Server is still running in the background, click this icon to bring it back up.", TrayIcon.MessageType.INFO);
            }
        }
    }

    /**
     * Method to log a member of staff out of the server and display the login
     * dialog.
     */
    private void logout() {
        try {
            lblUser.setText("Not Logged In");
            dc.logout(staff);
            log.log(Level.INFO, staff.getName() + " has logged out");
        } catch (StaffNotFoundException ex) {
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        isLoggedOn = false;
        staff = null;
        itemLogin.setText("Log In");
        login();
    }

    public boolean isLoggedOn() {
        return isLoggedOn;
    }

    @Override
    public void showMessage(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public boolean showYesNoMessage(String title, String message) {
        return JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    @Override
    public void showModalMessage(String title, String message) {
    }

    @Override
    public void hideModalMessage() {

    }

    @Override
    public void addTill(Till t) {

    }

    private class LogHandler extends Handler {

        @Override
        public void publish(LogRecord record) {
            if (record.getLevel() == Level.SEVERE) {
                if (GUI.SHOW_SEVERE == true) {
                    send(record);
                }
            } else if (record.getLevel() == Level.INFO) {
                if (GUI.SHOW_INFO == true) {
                    send(record);
                }
            } else if (record.getLevel() == Level.WARNING) {
                if (GUI.SHOW_WARNING == true) {
                    send(record);
                }
            } else {
                send(record);
            }
        }

        private void send(LogRecord record) {
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            GUI.this.log("[" + df.format(new Date(record.getMillis())) + "] [" + record.getLevel().toString() + "] " + record.getMessage());
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        btnManageStock = new javax.swing.JButton();
        btnManageCustomers = new javax.swing.JButton();
        btnManageStaff = new javax.swing.JButton();
        btnDiscounts = new javax.swing.JButton();
        btnCategorys = new javax.swing.JButton();
        btnReports = new javax.swing.JButton();
        btnScreens = new javax.swing.JButton();
        btnSettings = new javax.swing.JButton();
        statusBar = new javax.swing.JPanel();
        lblDatabase = new javax.swing.JLabel();
        lblUser = new javax.swing.JLabel();
        lblUpdate = new javax.swing.JLabel();
        lblClients = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        lblServerAddress = new javax.swing.JLabel();
        lblPort = new javax.swing.JLabel();
        lblProducts = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtStockWarnings = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        chkSevere = new javax.swing.JCheckBox();
        chkWarning = new javax.swing.JCheckBox();
        chkInfo = new javax.swing.JCheckBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        itemLogin = new javax.swing.JMenuItem();
        itemServerOptions = new javax.swing.JMenuItem();
        itemAbout = new javax.swing.JMenuItem();
        itemExit = new javax.swing.JMenuItem();
        menuStock = new javax.swing.JMenu();
        itemStock = new javax.swing.JMenuItem();
        itemReceive = new javax.swing.JMenuItem();
        itemWasteStock = new javax.swing.JMenuItem();
        itemWasteReports = new javax.swing.JMenuItem();
        itemDiscounts = new javax.swing.JMenuItem();
        itemCategorys = new javax.swing.JMenuItem();
        itemTaxes = new javax.swing.JMenuItem();
        menuStaff = new javax.swing.JMenu();
        itemStaff = new javax.swing.JMenuItem();
        menuCustomers = new javax.swing.JMenu();
        itemCustomers = new javax.swing.JMenuItem();
        menuItemReports = new javax.swing.JMenu();
        itemSales = new javax.swing.JMenuItem();
        itemResetSales = new javax.swing.JMenuItem();

        setTitle("JTill Server");

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setDoubleBuffered(true);

        btnManageStock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/product.png"))); // NOI18N
        btnManageStock.setToolTipText("Manage Products");
        btnManageStock.setFocusable(false);
        btnManageStock.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnManageStock.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnManageStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnManageStockActionPerformed(evt);
            }
        });
        jToolBar1.add(btnManageStock);

        btnManageCustomers.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/customer.png"))); // NOI18N
        btnManageCustomers.setToolTipText("Manage Customers");
        btnManageCustomers.setFocusable(false);
        btnManageCustomers.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnManageCustomers.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnManageCustomers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnManageCustomersActionPerformed(evt);
            }
        });
        jToolBar1.add(btnManageCustomers);

        btnManageStaff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/staff.png"))); // NOI18N
        btnManageStaff.setToolTipText("Manage Staff");
        btnManageStaff.setFocusable(false);
        btnManageStaff.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnManageStaff.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnManageStaff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnManageStaffActionPerformed(evt);
            }
        });
        jToolBar1.add(btnManageStaff);

        btnDiscounts.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/discount.png"))); // NOI18N
        btnDiscounts.setToolTipText("Manage Discounts");
        btnDiscounts.setFocusable(false);
        btnDiscounts.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDiscounts.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDiscounts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDiscountsActionPerformed(evt);
            }
        });
        jToolBar1.add(btnDiscounts);

        btnCategorys.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/category.png"))); // NOI18N
        btnCategorys.setToolTipText("Manage Categorys");
        btnCategorys.setFocusable(false);
        btnCategorys.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCategorys.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCategorys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCategorysActionPerformed(evt);
            }
        });
        jToolBar1.add(btnCategorys);

        btnReports.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/reports.png"))); // NOI18N
        btnReports.setToolTipText("Reports");
        btnReports.setFocusable(false);
        btnReports.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnReports.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnReports.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReportsActionPerformed(evt);
            }
        });
        jToolBar1.add(btnReports);

        btnScreens.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/screens.png"))); // NOI18N
        btnScreens.setToolTipText("Edit Screens");
        btnScreens.setFocusable(false);
        btnScreens.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnScreens.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnScreens.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnScreensActionPerformed(evt);
            }
        });
        jToolBar1.add(btnScreens);

        btnSettings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/settings.png"))); // NOI18N
        btnSettings.setToolTipText("Settings");
        btnSettings.setFocusable(false);
        btnSettings.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSettings.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSettingsActionPerformed(evt);
            }
        });
        jToolBar1.add(btnSettings);

        lblDatabase.setText("Not connected to database");
        lblDatabase.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblDatabase.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblDatabaseMouseClicked(evt);
            }
        });

        lblUser.setText("Not Logged In");
        lblUser.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblUser.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblUserMouseClicked(evt);
            }
        });

        lblUpdate.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblClients.setText("Connections: 0");
        lblClients.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblClients.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblClientsMouseClicked(evt);
            }
        });

        jLabel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout statusBarLayout = new javax.swing.GroupLayout(statusBar);
        statusBar.setLayout(statusBarLayout);
        statusBarLayout.setHorizontalGroup(
            statusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusBarLayout.createSequentialGroup()
                .addComponent(lblDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(lblUser, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(lblUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(lblClients, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        statusBarLayout.setVerticalGroup(
            statusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblDatabase, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblClients, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        txtLog.setEditable(false);
        txtLog.setColumns(20);
        txtLog.setRows(5);
        jScrollPane1.setViewportView(txtLog);

        jLabel2.setText("Event Log");

        lblServerAddress.setText("Local Server Address: 0.0.0.0");

        lblPort.setText("Port number: 0");

        lblProducts.setText("Products in database: 0");

        jButton1.setText("Log Staff Out");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        txtStockWarnings.setEditable(false);
        txtStockWarnings.setColumns(20);
        txtStockWarnings.setLineWrap(true);
        txtStockWarnings.setRows(5);
        jScrollPane2.setViewportView(txtStockWarnings);

        jLabel3.setText("Stock Warnings-");

        chkSevere.setSelected(true);
        chkSevere.setText("Show severe");
        chkSevere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkSevereActionPerformed(evt);
            }
        });

        chkWarning.setSelected(true);
        chkWarning.setText("Show warning");
        chkWarning.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkWarningActionPerformed(evt);
            }
        });

        chkInfo.setSelected(true);
        chkInfo.setText("Show info");
        chkInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkInfoActionPerformed(evt);
            }
        });

        menuFile.setText("File");

        itemLogin.setText("Log in");
        itemLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemLoginActionPerformed(evt);
            }
        });
        menuFile.add(itemLogin);

        itemServerOptions.setText("Server Options");
        itemServerOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemServerOptionsActionPerformed(evt);
            }
        });
        menuFile.add(itemServerOptions);

        itemAbout.setText("About");
        itemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemAboutActionPerformed(evt);
            }
        });
        menuFile.add(itemAbout);

        itemExit.setText("Exit");
        itemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemExitActionPerformed(evt);
            }
        });
        menuFile.add(itemExit);

        jMenuBar1.add(menuFile);

        menuStock.setText("Stock");

        itemStock.setText("Manage Stock");
        itemStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemStockActionPerformed(evt);
            }
        });
        menuStock.add(itemStock);

        itemReceive.setText("Receive Stock");
        itemReceive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemReceiveActionPerformed(evt);
            }
        });
        menuStock.add(itemReceive);

        itemWasteStock.setText("Waste Stock");
        itemWasteStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemWasteStockActionPerformed(evt);
            }
        });
        menuStock.add(itemWasteStock);

        itemWasteReports.setText("Waste Reports");
        itemWasteReports.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemWasteReportsActionPerformed(evt);
            }
        });
        menuStock.add(itemWasteReports);

        itemDiscounts.setText("Manage Discounts");
        itemDiscounts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemDiscountsActionPerformed(evt);
            }
        });
        menuStock.add(itemDiscounts);

        itemCategorys.setText("Manage Categorys");
        itemCategorys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemCategorysActionPerformed(evt);
            }
        });
        menuStock.add(itemCategorys);

        itemTaxes.setText("Manage Taxes");
        itemTaxes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemTaxesActionPerformed(evt);
            }
        });
        menuStock.add(itemTaxes);

        jMenuBar1.add(menuStock);

        menuStaff.setText("Staff");

        itemStaff.setText("Manage Staff");
        itemStaff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemStaffActionPerformed(evt);
            }
        });
        menuStaff.add(itemStaff);

        jMenuBar1.add(menuStaff);

        menuCustomers.setText("Customers");

        itemCustomers.setText("Manage Customers");
        itemCustomers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemCustomersActionPerformed(evt);
            }
        });
        menuCustomers.add(itemCustomers);

        jMenuBar1.add(menuCustomers);

        menuItemReports.setText("Reports");

        itemSales.setText("View Sales Data");
        itemSales.setEnabled(false);
        itemSales.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemSalesActionPerformed(evt);
            }
        });
        menuItemReports.add(itemSales);

        itemResetSales.setText("Reset Sales Data");
        itemResetSales.setEnabled(false);
        itemResetSales.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemResetSalesActionPerformed(evt);
            }
        });
        menuItemReports.add(itemResetSales);

        jMenuBar1.add(menuItemReports);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 935, Short.MAX_VALUE)
            .addComponent(statusBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPort)
                            .addComponent(lblProducts)
                            .addComponent(jButton1)
                            .addComponent(lblServerAddress))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chkSevere)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkWarning)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkInfo)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblServerAddress)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblPort)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblProducts)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(chkSevere)
                    .addComponent(chkWarning)
                    .addComponent(chkInfo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(statusBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnManageStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageStockActionPerformed
        ProductsWindow.showProductsListWindow(dc, icon);
    }//GEN-LAST:event_btnManageStockActionPerformed

    private void itemStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemStockActionPerformed
        ProductsWindow.showProductsListWindow(dc, icon);
    }//GEN-LAST:event_itemStockActionPerformed

    private void itemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemExitActionPerformed
        log.log(Level.INFO, "Stopping JTIll Server");
        if (dc instanceof DBConnect) {
            DBConnect db = (DBConnect) dc;
            log.log(Level.INFO, "Saving properties");
            settings.saveProperties();
            TillServer.removeSystemTrayIcon();
        }
        try {
            dc.close();
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        log.log(Level.INFO, "Stopping");
        System.exit(0);
    }//GEN-LAST:event_itemExitActionPerformed

    private void itemCustomersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemCustomersActionPerformed
        CustomersWindow.showCustomersListWindow(dc, icon);
    }//GEN-LAST:event_itemCustomersActionPerformed

    private void btnManageCustomersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageCustomersActionPerformed
        CustomersWindow.showCustomersListWindow(dc, icon);
    }//GEN-LAST:event_btnManageCustomersActionPerformed

    private void itemLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemLoginActionPerformed
        if (staff != null) {
            logout();
        } else {
            login();
        }
    }//GEN-LAST:event_itemLoginActionPerformed

    private void itemStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemStaffActionPerformed
        StaffWindow.showStaffListWindow(dc, icon);
    }//GEN-LAST:event_itemStaffActionPerformed

    private void btnManageStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageStaffActionPerformed
        StaffWindow.showStaffListWindow(dc, icon);
    }//GEN-LAST:event_btnManageStaffActionPerformed

    private void lblClientsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblClientsMouseClicked
        if (evt.getClickCount() == 2) {
            ConnectionsDialog.showConnectionsDialog(this, connections);
        }
    }//GEN-LAST:event_lblClientsMouseClicked

    private void lblUserMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblUserMouseClicked
        if (evt.getClickCount() == 2) {
            JOptionPane.showMessageDialog(this, staff, staff.getName(), JOptionPane.PLAIN_MESSAGE);
        }
    }//GEN-LAST:event_lblUserMouseClicked

    private void lblDatabaseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblDatabaseMouseClicked
        if (evt.getClickCount() == 2) {
            JOptionPane.showMessageDialog(this, dc, "Database Connection", JOptionPane.PLAIN_MESSAGE);
        }
    }//GEN-LAST:event_lblDatabaseMouseClicked

    private void itemDiscountsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemDiscountsActionPerformed
        DiscountsWindow.showDiscountListWindow(dc, icon);
    }//GEN-LAST:event_itemDiscountsActionPerformed

    private void itemCategorysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemCategorysActionPerformed
        CategorysWindow.showCategoryWindow(dc, icon);
    }//GEN-LAST:event_itemCategorysActionPerformed

    private void itemTaxesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemTaxesActionPerformed
        TaxWindow.showTaxWindow(dc, icon);
    }//GEN-LAST:event_itemTaxesActionPerformed

    private void btnDiscountsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiscountsActionPerformed
        DiscountsWindow.showDiscountListWindow(dc, icon);
    }//GEN-LAST:event_btnDiscountsActionPerformed

    private void btnCategorysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCategorysActionPerformed
        CategorysWindow.showCategoryWindow(dc, icon);
    }//GEN-LAST:event_btnCategorysActionPerformed

    private void itemServerOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemServerOptionsActionPerformed
        SettingsWindow.showSettingsWindow(dc, icon);
    }//GEN-LAST:event_itemServerOptionsActionPerformed

    private void itemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemAboutActionPerformed
        JOptionPane.showMessageDialog(null, "JTill Server is running on port number "
                + settings.getSetting("port") + " with " + clientCounter + " connections.\n"
                + dc.toString(), "JTill Server",
                JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_itemAboutActionPerformed

    private void itemSalesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemSalesActionPerformed
        SalesWindow.showSalesWindow(dc, icon);
    }//GEN-LAST:event_itemSalesActionPerformed

    private void itemResetSalesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemResetSalesActionPerformed
        JOptionPane.showMessageDialog(this, "Sales data reset", "Sales Data", JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_itemResetSalesActionPerformed

    private void btnReportsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReportsActionPerformed
        SalesWindow.showSalesWindow(dc, icon);
    }//GEN-LAST:event_btnReportsActionPerformed

    private void btnSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSettingsActionPerformed
        SettingsWindow.showSettingsWindow(dc, icon);
    }//GEN-LAST:event_btnSettingsActionPerformed

    private void btnScreensActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScreensActionPerformed
        ScreenEditWindow.showScreenEditWindow(dc, icon);
    }//GEN-LAST:event_btnScreensActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            for (Staff s : dc.getAllStaff()) {
                try {
                    dc.tillLogout(s);
                } catch (IOException | StaffNotFoundException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException | SQLException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void itemReceiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemReceiveActionPerformed
        ReceiveItemsWindow.showWindow(dc, icon);
    }//GEN-LAST:event_itemReceiveActionPerformed

    private void itemWasteStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemWasteStockActionPerformed
        WasteStockWindow.showWindow(dc, icon);
    }//GEN-LAST:event_itemWasteStockActionPerformed

    private void chkSevereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSevereActionPerformed
        GUI.SHOW_SEVERE = ((JCheckBox) evt.getSource()).isSelected();
    }//GEN-LAST:event_chkSevereActionPerformed

    private void chkWarningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkWarningActionPerformed
        GUI.SHOW_WARNING = ((JCheckBox) evt.getSource()).isSelected();
    }//GEN-LAST:event_chkWarningActionPerformed

    private void chkInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkInfoActionPerformed
        GUI.SHOW_INFO = ((JCheckBox) evt.getSource()).isSelected();
    }//GEN-LAST:event_chkInfoActionPerformed

    private void itemWasteReportsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemWasteReportsActionPerformed
        WasteReports.showWindow(dc);
    }//GEN-LAST:event_itemWasteReportsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCategorys;
    private javax.swing.JButton btnDiscounts;
    private javax.swing.JButton btnManageCustomers;
    private javax.swing.JButton btnManageStaff;
    private javax.swing.JButton btnManageStock;
    private javax.swing.JButton btnReports;
    private javax.swing.JButton btnScreens;
    private javax.swing.JButton btnSettings;
    private javax.swing.JCheckBox chkInfo;
    private javax.swing.JCheckBox chkSevere;
    private javax.swing.JCheckBox chkWarning;
    private javax.swing.JMenuItem itemAbout;
    private javax.swing.JMenuItem itemCategorys;
    private javax.swing.JMenuItem itemCustomers;
    private javax.swing.JMenuItem itemDiscounts;
    private javax.swing.JMenuItem itemExit;
    private javax.swing.JMenuItem itemLogin;
    private javax.swing.JMenuItem itemReceive;
    private javax.swing.JMenuItem itemResetSales;
    private javax.swing.JMenuItem itemSales;
    private javax.swing.JMenuItem itemServerOptions;
    private javax.swing.JMenuItem itemStaff;
    private javax.swing.JMenuItem itemStock;
    private javax.swing.JMenuItem itemTaxes;
    private javax.swing.JMenuItem itemWasteReports;
    private javax.swing.JMenuItem itemWasteStock;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblClients;
    private javax.swing.JLabel lblDatabase;
    private javax.swing.JLabel lblPort;
    private javax.swing.JLabel lblProducts;
    private javax.swing.JLabel lblServerAddress;
    private javax.swing.JLabel lblUpdate;
    private javax.swing.JLabel lblUser;
    private javax.swing.JMenu menuCustomers;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuItemReports;
    private javax.swing.JMenu menuStaff;
    private javax.swing.JMenu menuStock;
    private javax.swing.JPanel statusBar;
    private javax.swing.JTextArea txtLog;
    private javax.swing.JTextArea txtStockWarnings;
    // End of variables declaration//GEN-END:variables

    @Override
    public void allow() {

    }

    @Override
    public void disallow() {
    }

}
