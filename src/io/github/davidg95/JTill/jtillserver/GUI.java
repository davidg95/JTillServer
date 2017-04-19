/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.TillSplashScreen;
import io.github.davidg95.JTill.jtill.*;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 * The main GUI for the server.
 *
 * @author David
 */
public class GUI extends JFrame implements GUIInterface {

    private static final Logger LOG = Logger.getGlobal();

    private static GUI gui; //The GUI.

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

    private final DataConnect dc; //The data connection.
    private boolean isLoggedOn; //Boolean to indicate whether someone is logged on or not.

    public static Staff staff; //The current logged on staff.

    public int clientCounter = 0;
    private final ArrayList<String> connections;

    private final boolean remote;

    private final Image icon; //The icon for the frame.

    private final Settings settings; //The settings object.

    private static final String HELP_TEXT = "Press F1 for help";

    /**
     * Creates new form GUI
     *
     * @param dataConnect
     * @param remote flag indicating whether this is a remote connection or not.
     * @param icon the icon for the frame.
     */
    public GUI(DataConnect dataConnect, boolean remote, Image icon) {
        try {
            javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        this.dc = dataConnect;
        this.remote = remote;
        this.icon = icon;
        this.settings = Settings.getInstance();
        initComponents();
        init();
        try {
            lblServerAddress.setText("Local Server Address: " + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException ex) {
            lblServerAddress.setText("Local Server Address: UNKNOWN");
        }
        connections = new ArrayList<>();
        LOG.addHandler(new LogHandler());
    }

    private void init() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setFocusable(true);
        setIconImage(icon);
        dc.setGUI(this);
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F1) {
                    new HelpPage().setVisible(true);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
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
    }

    @Override
    public void updateTills() {
        try {
            lblClients.setText("Connections: " + dc.getConnectedTills().size());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
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
                TillSplashScreen.setLabel("Populating database");
                db.addCustomer(new Customer("NONE", "", "", "", "", "", "", "", "", "", "", 0, BigDecimal.ZERO));
                Staff s = StaffDialog.showNewStaffDialog(this, db);
                if (s == null) {
                    System.exit(0);
                }
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 40000) {
                    LOG.log(Level.SEVERE, "The database is already in use. The program will now terminate");
                    JOptionPane.showMessageDialog(this, "The database is already in use by another application. Program will now terminate.\nError Code " + ex.getErrorCode(), "Database in use", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                } else {
                    LOG.log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(this, ex, "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Method to log in to the database. this will not attempt login if the
     * remote flag has indicated this this is a remote connection to the main
     * server.
     */
    public void databaseLogin() {
        try {
            setTitle("JTill Server - " + dc.getSetting("SITE_NAME"));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        if (!remote) {
            try {
                DBConnect db = (DBConnect) dc;
                TillSplashScreen.setLabel("Connecting to database");
                db.connect(settings.getSetting("db_address"), settings.getSetting("db_username"), settings.getSetting("db_password"));
                if (dc.getStaffCount() == 0) {
                    Staff s = StaffDialog.showNewStaffDialog(this, db);
                    if (s == null) {
                        System.exit(0);
                    }
                }
                TillSplashScreen.addBar(56);
            } catch (SQLException ex) {
                initialSetup();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, ex, "Server Error", JOptionPane.ERROR_MESSAGE);
            }
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
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
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
            LOG.log(Level.INFO, staff.getName() + " has logged in");
            isLoggedOn = true;
        } else {
            if (dc instanceof DBConnect) {
                settings.saveProperties();
            }
            if (remote) {
                try {
                    ((ServerConnection) dc).close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
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
            LOG.log(Level.INFO, staff.getName() + " has logged out");
        } catch (StaffNotFoundException ex) {
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
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
            GUI.this.log("[" + df.format(new Date(record.getMillis())) + " - " + record.getLevel().toString() + "] " + record.getMessage());
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
        lblHelp = new javax.swing.JLabel();
        lblUser = new javax.swing.JLabel();
        lblUpdate = new javax.swing.JLabel();
        lblClients = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        lblServerAddress = new javax.swing.JLabel();
        lblPort = new javax.swing.JLabel();
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
        itemCreateNewProduct = new javax.swing.JMenuItem();
        itemStock = new javax.swing.JMenuItem();
        itemReceive = new javax.swing.JMenuItem();
        itemWasteStock = new javax.swing.JMenuItem();
        itemWasteReports = new javax.swing.JMenuItem();
        itemEnquiry = new javax.swing.JMenuItem();
        itemStockTake = new javax.swing.JMenuItem();
        menuSetup = new javax.swing.JMenu();
        itemReasons = new javax.swing.JMenuItem();
        itemSuppliers = new javax.swing.JMenuItem();
        itemTillScreens = new javax.swing.JMenuItem();
        itemCustomers = new javax.swing.JMenuItem();
        itemStaff = new javax.swing.JMenuItem();
        itemNewStaff = new javax.swing.JMenuItem();
        itemDiscounts = new javax.swing.JMenuItem();
        itemCategorys = new javax.swing.JMenuItem();
        itemDepartments = new javax.swing.JMenuItem();
        itemTaxes = new javax.swing.JMenuItem();
        itemPluSettings = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        itemDatabase = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        itemLabelPrinting = new javax.swing.JMenuItem();
        itemStaffClocking = new javax.swing.JMenuItem();

        setTitle("JTill Server");

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setDoubleBuffered(true);

        btnManageStock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/product.png"))); // NOI18N
        btnManageStock.setToolTipText("Manage Products");
        btnManageStock.setFocusable(false);
        btnManageStock.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnManageStock.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnManageStock.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnManageStockMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnManageStockMouseExited(evt);
            }
        });
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
        btnManageCustomers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnManageCustomersMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnManageCustomersMouseExited(evt);
            }
        });
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
        btnManageStaff.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnManageStaffMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnManageStaffMouseExited(evt);
            }
        });
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
        btnDiscounts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnDiscountsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnDiscountsMouseExited(evt);
            }
        });
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
        btnCategorys.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCategorysMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCategorysMouseExited(evt);
            }
        });
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
        btnReports.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnReportsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnReportsMouseExited(evt);
            }
        });
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
        btnScreens.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnScreensMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnScreensMouseExited(evt);
            }
        });
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
        btnSettings.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnSettingsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnSettingsMouseExited(evt);
            }
        });
        btnSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSettingsActionPerformed(evt);
            }
        });
        jToolBar1.add(btnSettings);

        lblHelp.setText("Press F1 for help");
        lblHelp.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblHelp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblHelpMouseClicked(evt);
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
                .addComponent(lblHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(lblUser, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(lblUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(lblClients, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        statusBarLayout.setVerticalGroup(
            statusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblHelp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblClients, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        txtLog.setEditable(false);
        txtLog.setColumns(20);
        txtLog.setRows(5);
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
        txtLog.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtLogMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(txtLog);

        jLabel2.setText("Event Log");

        lblServerAddress.setText("Local Server Address: 0.0.0.0");

        lblPort.setText("Port number: 0");

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

        itemCreateNewProduct.setText("Create New Product");
        itemCreateNewProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemCreateNewProductActionPerformed(evt);
            }
        });
        menuStock.add(itemCreateNewProduct);

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

        itemEnquiry.setText("Product Enquiry");
        itemEnquiry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemEnquiryActionPerformed(evt);
            }
        });
        menuStock.add(itemEnquiry);

        itemStockTake.setText("Stock Take");
        itemStockTake.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemStockTakeActionPerformed(evt);
            }
        });
        menuStock.add(itemStockTake);

        jMenuBar1.add(menuStock);

        menuSetup.setText("Setup");

        itemReasons.setText("Edit Waste Reasons");
        itemReasons.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemReasonsActionPerformed(evt);
            }
        });
        menuSetup.add(itemReasons);

        itemSuppliers.setText("Edit Suppliers");
        itemSuppliers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemSuppliersActionPerformed(evt);
            }
        });
        menuSetup.add(itemSuppliers);

        itemTillScreens.setText("Edit Till Screens");
        itemTillScreens.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemTillScreensActionPerformed(evt);
            }
        });
        menuSetup.add(itemTillScreens);

        itemCustomers.setText("Edit Customers");
        itemCustomers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemCustomersActionPerformed(evt);
            }
        });
        menuSetup.add(itemCustomers);

        itemStaff.setText("Edit Staff");
        itemStaff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemStaffActionPerformed(evt);
            }
        });
        menuSetup.add(itemStaff);

        itemNewStaff.setText("Add New Staff");
        itemNewStaff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemNewStaffActionPerformed(evt);
            }
        });
        menuSetup.add(itemNewStaff);

        itemDiscounts.setText("Edit Discounts");
        itemDiscounts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemDiscountsActionPerformed(evt);
            }
        });
        menuSetup.add(itemDiscounts);

        itemCategorys.setText("Edit Categorys");
        itemCategorys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemCategorysActionPerformed(evt);
            }
        });
        menuSetup.add(itemCategorys);

        itemDepartments.setText("Edit Departments");
        itemDepartments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemDepartmentsActionPerformed(evt);
            }
        });
        menuSetup.add(itemDepartments);

        itemTaxes.setText("Edit Taxes");
        itemTaxes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemTaxesActionPerformed(evt);
            }
        });
        menuSetup.add(itemTaxes);

        itemPluSettings.setText("Edit Plu Settings");
        itemPluSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemPluSettingsActionPerformed(evt);
            }
        });
        menuSetup.add(itemPluSettings);
        menuSetup.add(jSeparator2);

        itemDatabase.setText("Database Settings");
        itemDatabase.setEnabled(false);
        menuSetup.add(itemDatabase);

        jMenuBar1.add(menuSetup);

        jMenu1.setText("Report");

        jMenuItem1.setText("Sales Reporting");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        itemLabelPrinting.setText("Label Printing");
        itemLabelPrinting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemLabelPrintingActionPerformed(evt);
            }
        });
        jMenu1.add(itemLabelPrinting);

        itemStaffClocking.setText("Staff Hours");
        itemStaffClocking.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemStaffClockingActionPerformed(evt);
            }
        });
        jMenu1.add(itemStaffClocking);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(statusBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblServerAddress)
                            .addComponent(lblPort))
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
                    .addComponent(lblPort)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
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
        if (dc instanceof DBConnect) {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to stop JTill server?", "JTill Server", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }
            LOG.log(Level.INFO, "Stopping JTIll Server");
            DBConnect db = (DBConnect) dc;
            LOG.log(Level.INFO, "Saving properties");
            settings.saveProperties();
            TillServer.removeSystemTrayIcon();
        }
        try {
            dc.close();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        LOG.log(Level.INFO, "Stopping");
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
            ConnectionsDialog.showConnectionsDialog(this, dc);
        }
    }//GEN-LAST:event_lblClientsMouseClicked

    private void lblUserMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblUserMouseClicked
        if (evt.getClickCount() == 2) {
            JOptionPane.showMessageDialog(this, staff, staff.getName(), JOptionPane.PLAIN_MESSAGE);
        }
    }//GEN-LAST:event_lblUserMouseClicked

    private void lblHelpMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblHelpMouseClicked
        if (evt.getClickCount() == 2) {
            new HelpPage().setVisible(true);
        }
    }//GEN-LAST:event_lblHelpMouseClicked

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

    private void btnReportsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReportsActionPerformed
        SalesWindow.showSalesWindow(dc, icon);
    }//GEN-LAST:event_btnReportsActionPerformed

    private void btnSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSettingsActionPerformed
        SettingsWindow.showSettingsWindow(dc, icon);
    }//GEN-LAST:event_btnSettingsActionPerformed

    private void btnScreensActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScreensActionPerformed
        ScreenEditWindow.showScreenEditWindow(dc, icon);
    }//GEN-LAST:event_btnScreensActionPerformed

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
        WasteReports.showWindow(dc, icon);
    }//GEN-LAST:event_itemWasteReportsActionPerformed

    private void itemReasonsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemReasonsActionPerformed
        WasteReasonDialog.showDialog(this, dc);
    }//GEN-LAST:event_itemReasonsActionPerformed

    private void itemTillScreensActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemTillScreensActionPerformed
        ScreenEditWindow.showScreenEditWindow(dc, icon);
    }//GEN-LAST:event_itemTillScreensActionPerformed

    private void txtLogMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtLogMouseClicked
        if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem item = new JMenuItem("Copy");
            JMenuItem item2 = new JMenuItem("Open Log File");
            item.addActionListener((ActionEvent e) -> {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(txtLog.getSelectedText()), null);
            });
            item2.addActionListener((ActionEvent e) -> {
                try {
                    Desktop.getDesktop().open(new File("log.txt"));
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(this, "Error opening log file", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            menu.add(item);
            menu.add(item2);
            menu.show(txtLog, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_txtLogMouseClicked

    private void itemSuppliersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemSuppliersActionPerformed
        SupplierWindow.showWindow(dc, icon);
    }//GEN-LAST:event_itemSuppliersActionPerformed

    private void itemDepartmentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemDepartmentsActionPerformed
        DepartmentsWindow.showWindow(dc, icon);
    }//GEN-LAST:event_itemDepartmentsActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        ReportingWindow.showWindow(dc, icon);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void itemEnquiryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemEnquiryActionPerformed
        ProductEnquiry.showWindow(dc, icon);
    }//GEN-LAST:event_itemEnquiryActionPerformed

    private void itemLabelPrintingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemLabelPrintingActionPerformed
        LabelPrintingWindow.showWindow(dc, icon);
    }//GEN-LAST:event_itemLabelPrintingActionPerformed

    private void itemCreateNewProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemCreateNewProductActionPerformed
        ProductEntryDialog.showDialog(this, dc, icon);
    }//GEN-LAST:event_itemCreateNewProductActionPerformed

    private void itemNewStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemNewStaffActionPerformed
        Staff s = StaffDialog.showNewStaffDialog(this, dc);
        if (s != null) {
            JOptionPane.showMessageDialog(this, "New staff member " + s + " created");
        }
    }//GEN-LAST:event_itemNewStaffActionPerformed

    private void itemStaffClockingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemStaffClockingActionPerformed
        StaffClocking.showWindow(dc, icon);
    }//GEN-LAST:event_itemStaffClockingActionPerformed

    private void itemStockTakeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemStockTakeActionPerformed
        StockTakeWindow.showWindow(dc, icon);
    }//GEN-LAST:event_itemStockTakeActionPerformed

    private void itemPluSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemPluSettingsActionPerformed
        PluSettings.showWindow(dc, icon);
    }//GEN-LAST:event_itemPluSettingsActionPerformed

    private void btnManageStockMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnManageStockMouseEntered
        lblHelp.setText("Manage the stock inventory");
    }//GEN-LAST:event_btnManageStockMouseEntered

    private void btnManageStockMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnManageStockMouseExited
        lblHelp.setText(HELP_TEXT);
    }//GEN-LAST:event_btnManageStockMouseExited

    private void btnManageCustomersMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnManageCustomersMouseEntered
        lblHelp.setText("Manage customers");
    }//GEN-LAST:event_btnManageCustomersMouseEntered

    private void btnManageCustomersMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnManageCustomersMouseExited
        lblHelp.setText(HELP_TEXT);
    }//GEN-LAST:event_btnManageCustomersMouseExited

    private void btnManageStaffMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnManageStaffMouseEntered
        lblHelp.setText("Manage staff");
    }//GEN-LAST:event_btnManageStaffMouseEntered

    private void btnManageStaffMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnManageStaffMouseExited
        lblHelp.setText(HELP_TEXT);
    }//GEN-LAST:event_btnManageStaffMouseExited

    private void btnDiscountsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDiscountsMouseEntered
        lblHelp.setText("Manage discounts");
    }//GEN-LAST:event_btnDiscountsMouseEntered

    private void btnDiscountsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDiscountsMouseExited
        lblHelp.setText(HELP_TEXT);
    }//GEN-LAST:event_btnDiscountsMouseExited

    private void btnCategorysMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCategorysMouseEntered
        lblHelp.setText("Manage categories");
    }//GEN-LAST:event_btnCategorysMouseEntered

    private void btnCategorysMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCategorysMouseExited
        lblHelp.setText(HELP_TEXT);
    }//GEN-LAST:event_btnCategorysMouseExited

    private void btnReportsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnReportsMouseEntered
        lblHelp.setText("View reports");
    }//GEN-LAST:event_btnReportsMouseEntered

    private void btnReportsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnReportsMouseExited
        lblHelp.setText(HELP_TEXT);
    }//GEN-LAST:event_btnReportsMouseExited

    private void btnScreensMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnScreensMouseEntered
        lblHelp.setText("Edit till screens");
    }//GEN-LAST:event_btnScreensMouseEntered

    private void btnScreensMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnScreensMouseExited
        lblHelp.setText(HELP_TEXT);
    }//GEN-LAST:event_btnScreensMouseExited

    private void btnSettingsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSettingsMouseEntered
        lblHelp.setText("Edit system settings");
    }//GEN-LAST:event_btnSettingsMouseEntered

    private void btnSettingsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSettingsMouseExited
        lblHelp.setText(HELP_TEXT);
    }//GEN-LAST:event_btnSettingsMouseExited

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
    private javax.swing.JMenuItem itemCreateNewProduct;
    private javax.swing.JMenuItem itemCustomers;
    private javax.swing.JMenuItem itemDatabase;
    private javax.swing.JMenuItem itemDepartments;
    private javax.swing.JMenuItem itemDiscounts;
    private javax.swing.JMenuItem itemEnquiry;
    private javax.swing.JMenuItem itemExit;
    private javax.swing.JMenuItem itemLabelPrinting;
    private javax.swing.JMenuItem itemLogin;
    private javax.swing.JMenuItem itemNewStaff;
    private javax.swing.JMenuItem itemPluSettings;
    private javax.swing.JMenuItem itemReasons;
    private javax.swing.JMenuItem itemReceive;
    private javax.swing.JMenuItem itemServerOptions;
    private javax.swing.JMenuItem itemStaff;
    private javax.swing.JMenuItem itemStaffClocking;
    private javax.swing.JMenuItem itemStock;
    private javax.swing.JMenuItem itemStockTake;
    private javax.swing.JMenuItem itemSuppliers;
    private javax.swing.JMenuItem itemTaxes;
    private javax.swing.JMenuItem itemTillScreens;
    private javax.swing.JMenuItem itemWasteReports;
    private javax.swing.JMenuItem itemWasteStock;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblClients;
    private javax.swing.JLabel lblHelp;
    private javax.swing.JLabel lblPort;
    private javax.swing.JLabel lblServerAddress;
    private javax.swing.JLabel lblUpdate;
    private javax.swing.JLabel lblUser;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuSetup;
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
