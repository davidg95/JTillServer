/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import io.github.davidg95.JTill.jtillserver.salereportdialogs.SaleReportDialog;
import io.github.davidg95.jconn.JConnData;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.*;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * The main GUI for the server.
 *
 * @author David
 */
public class GUI extends JFrame implements GUIInterface {

    private static final Logger LOG = Logger.getGlobal();

    public static GUI gui; //The GUI.

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

    public final DataConnect dc; //The data connection.

    private boolean isLoggedOn; //Boolean to indicate whether someone is logged on or not.
    public static Staff staff; //The current logged on staff.

    private final boolean remote;

    public static Image icon; //The icon for the frame.

    private Settings settings; //The settings object.

    private static final String HELP_TEXT = "Press F1 for help";

    private int warningCount;
    private LinkedList<String> warningsList;

    private ModalDialog mDialog;

    public HashMap<String, List> savedReports = new HashMap<>();

    private Image image;

    /**
     * Creates new form GUI
     *
     * @param remote flag indicating whether this is a remote connection or not.
     * @param icon the icon for the frame.
     */
    public GUI(boolean remote, Image icon) throws Exception {
        super();
        try {
            javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); //Set the look and feel.
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        this.dc = DataConnect.dataconnect;
        this.remote = remote;
        GUI.icon = icon;
        if (!remote) {
            this.settings = Settings.getInstance();
        }
        initComponents();
        init();
        try {
            if (!remote) {
                lblServerAddress.setText("Local Server Address: " + InetAddress.getLocalHost().getHostAddress());
            } else {
                lblServerAddress.setText("Server Address: " + ((ServerConnection) dc).toString());
            }
        } catch (UnknownHostException ex) {
            lblServerAddress.setText("Local Server Address: UNKNOWN");
        }
        LOG.addHandler(new LogHandler());
        try {
            File file = dc.getLoginBackground();
            if (file != null) {
                image = ImageIO.read(dc.getLoginBackground());
            }
        } catch (IOException ex) {
        }
    }

    @Override
    public void setClientLabel(int count) {
        lblClients.setText("Connections: " + count);
    }

    /**
     * Class for adding the background image to the desktop pane.
     */
    private class MyInternal extends JDesktopPane {

        /**
         * Paints the background image.
         *
         * @param g the Graphics context.
         */
        @Override
        public void paintComponent(Graphics g) {
            try {
                Graphics2D g2 = (Graphics2D) g;
                if (!Boolean.parseBoolean(dc.getSetting("SHOWBACKGROUND"))) {
                    return;
                }
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                g2.drawImage(image, 0, 0, internal.getWidth(), internal.getHeight(), null);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void init() throws Exception {
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
        warningsList = new LinkedList<>();
        warningCount = 0;
    }

    public void setTaskLabel(String message) {
        lblTask.setText(message);
    }

    /**
     * Creates a new instance of the GUI.
     *
     * @param remote if it is a remote session.
     * @param icon the icon for the windows and dialogs.
     * @return the GUI.
     */
    public static GUI create(boolean remote, Image icon) throws Exception {
        gui = new GUI(remote, icon);
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
        lblPort.setText("Port Number: " + TillServer.PORT_IN_USE);
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
     * Initial setup which creates the database and prompts the user to create
     * the first member of staff.
     */
    private void initialSetup() {
        if (dc instanceof DBConnect) {
            try {
                DBConnect db = (DBConnect) dc;
                TillSplashScreen.setLabel("Creating database...");
                db.create(settings.getSetting("db_address") + "create=true;", settings.getSetting("db_username"), settings.getSetting("db_password")); //Create the database
                TillSplashScreen.setLabel("Populating database");
                db.addCustomer(new Customer("NONE", "", "", "", "", "", "", "", "", "", "", 0, BigDecimal.ZERO)); //Create a blank customer
                Staff s = StaffDialog.showNewStaffDialog(this); //Show the create staff dialog
                if (s == null) {
                    System.exit(0); // Exit if the user clicked cancel
                }
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 40000) { //If another application is already using the database.
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
        if (!remote) { //Test if this is a remote server connection
            try {
                DBConnect db = (DBConnect) dc;
                TillSplashScreen.setLabel("Connecting to database"); //Update the splash screen
                db.connect(settings.getSetting("db_address"), settings.getSetting("db_username"), settings.getSetting("db_password")); //Open a connection to the database
                setTitle("JTill Server - " + dc.getSetting("SITE_NAME")); //Set the window title
                if (dc.getStaffCount() == 0) { //Check to see if any staff members have been created
                    Staff s = new Staff("JTill Admin", Staff.AREA_MANAGER, "admin", "jtill", 0.01, true); //Create the admin member of staff if they do not already exists
                    try {
                        dc.addStaff(s); //Add the member of staff
                    } catch (SQLException | IOException ex) {
                        JOptionPane.showMessageDialog(this, ex, "Server Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                TillSplashScreen.addBar(20);
            } catch (SQLException ex) {
                initialSetup(); //If there was an issue connecting to the database, go to the initial setup.
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, ex, "Server Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void setUpdateLabel(String text) {
        lblWarnings.setText(text);
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
        if (TillServer.server != null) {
            TillServer.server.sendData(null, JConnData.create("LOG").addParam("MESSAGE", o.toString()));
        }
    }

    @Override
    public void logWarning(Object o) {
        warningsList.add(o.toString());
        warningCount++;
        lblWarnings.setText("Warnings: " + warningCount);
    }

    public void checkUpdate() {
        try {
            String latest = UpdateChecker.checkForUpdate();
            if (!latest.equals(TillServer.VERSION)) {
                if (JOptionPane.showConfirmDialog(this, "Version " + latest + " avaliable. Download now?", "Update", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    UpdateChecker.downloadServerUpdate();
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error checking for update", "Update", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Method to log a member of staff in to the server.
     */
    public void login() {
        staff = LoginDialog.showLoginDialog(this);
        if (staff != null) {
            lblUser.setText(staff.getName());
            itemLogin.setText("Log Out");
            LOG.log(Level.INFO, staff.getName() + " has logged in");
            isLoggedOn = true;
//            checkUserLevel();
        } else {
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

    private void checkUserLevel() {
        for (Component c : toolBar.getComponents()) {
            c.setEnabled(false);
        }
        switch (staff.getPosition()) {
            case 4: {

            }
            case 3: {
                for (Component c : toolBar.getComponents()) {
                    c.setEnabled(true);
                }
            }
            case 2: {
                btnManageStock.setEnabled(true);
            }
            case 1: {
                btnWasteStock.setEnabled(true);
                btnReceiveStock.setEnabled(true);
                btnEnquiry.setEnabled(true);
            }
        }
    }

    /**
     * Method to log a member of staff out of the server and display the login
     * dialog.
     */
    @Override
    public void logout() {
        try {
            dc.logout(staff);
            for (JInternalFrame f : internal.getAllFrames()) {
                try {
                    f.setClosed(true);
                } catch (PropertyVetoException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            lblUser.setText("Not Logged In");
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

    @Override
    public void connectionDrop() {
        mDialog = new ModalDialog(this, "Connection lost", "Connection lost");
        new Thread() {
            @Override
            public void run() {
                mDialog.show();
            }
        }.start();
    }

    @Override
    public Staff connectionReestablish() {
        mDialog.hide();
        return null;
    }

    @Override
    public void initTill() {
        //Nothing as remote clients do not need reinitialised.
    }

    @Override
    public Till showTillSetupWindow(String name, UUID uuid) throws JTillException {
        if (staff == null) {
            throw new JTillException("The server is not ready to accept new connections");
        }
        Till till = new Till(name, uuid, 1);
        till = TillInitialSetupDialog.showDialog(this, till);
        if (till == null) {
            throw new JTillException("Connection cancelled by server");
        }
        return till;
    }

    @Override
    public void renameTill(String name) {
        //Nothing
    }

    @Override
    public void requestUpdate() {
        //Nothing
    }

    @Override
    public void markNewData(String[] data) {
        //Nothing
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

        toolBar = new javax.swing.JToolBar();
        btnManageStock = new javax.swing.JButton();
        btnManageCustomers = new javax.swing.JButton();
        btnManageStaff = new javax.swing.JButton();
        btnAddStaff = new javax.swing.JButton();
        btnDiscounts = new javax.swing.JButton();
        btnCategorys = new javax.swing.JButton();
        btnSendData = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        btnNewProduct = new javax.swing.JButton();
        btnReceiveStock = new javax.swing.JButton();
        btnWasteStock = new javax.swing.JButton();
        btnEnquiry = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btnScreens = new javax.swing.JButton();
        btnTerminals = new javax.swing.JButton();
        btnSettings = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        btnReports = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
        lblServerAddress = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
        lblPort = new javax.swing.JLabel();
        statusBar = new javax.swing.JPanel();
        lblHelp = new javax.swing.JLabel();
        lblUser = new javax.swing.JLabel();
        lblWarnings = new javax.swing.JLabel();
        lblClients = new javax.swing.JLabel();
        lblTask = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        chkSevere = new javax.swing.JCheckBox();
        chkWarning = new javax.swing.JCheckBox();
        chkInfo = new javax.swing.JCheckBox();
        internal = new MyInternal();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        itemLogin = new javax.swing.JMenuItem();
        itemServerOptions = new javax.swing.JMenuItem();
        itemCheckDatabase = new javax.swing.JMenuItem();
        itemInfo = new javax.swing.JMenuItem();
        itemUpdate = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        itemExit = new javax.swing.JMenuItem();
        menuStock = new javax.swing.JMenu();
        itemCreateNewProduct = new javax.swing.JMenuItem();
        itemStock = new javax.swing.JMenuItem();
        itemEdit = new javax.swing.JMenuItem();
        itemReceive = new javax.swing.JMenuItem();
        itemOrdering = new javax.swing.JMenuItem();
        itemOrderingWizard = new javax.swing.JMenuItem();
        itemWasteStock = new javax.swing.JMenuItem();
        itemEnquiry = new javax.swing.JMenuItem();
        itemStockTake = new javax.swing.JMenuItem();
        menuSetup = new javax.swing.JMenu();
        itemReasons = new javax.swing.JMenuItem();
        itemRefundReasons = new javax.swing.JMenuItem();
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
        itemSendData = new javax.swing.JMenuItem();
        itemTerminals = new javax.swing.JMenuItem();
        itemSiteDetails = new javax.swing.JMenuItem();
        itemDatabase = new javax.swing.JMenuItem();
        itemConsolodated = new javax.swing.JMenu();
        itemSalesReporting = new javax.swing.JMenuItem();
        itemTransactionViewer = new javax.swing.JMenuItem();
        itemLabelPrinting = new javax.swing.JMenuItem();
        itemStaffClocking = new javax.swing.JMenuItem();
        itemStaffReporting = new javax.swing.JMenuItem();
        itemWasteReports = new javax.swing.JMenuItem();
        itemReceivedReports = new javax.swing.JMenuItem();
        itemDeclarations = new javax.swing.JMenuItem();
        itemConsolidated = new javax.swing.JMenuItem();
        itemStockReport = new javax.swing.JMenuItem();
        menuHelp = new javax.swing.JMenu();
        itemAbout = new javax.swing.JMenuItem();
        itemHelp = new javax.swing.JMenuItem();

        setTitle("JTill Server");
        setIconImage(GUI.icon);

        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setDoubleBuffered(true);

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
        toolBar.add(btnManageStock);

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
        toolBar.add(btnManageCustomers);

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
        toolBar.add(btnManageStaff);

        btnAddStaff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/addStaff.png"))); // NOI18N
        btnAddStaff.setToolTipText("Add a new staff");
        btnAddStaff.setFocusable(false);
        btnAddStaff.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddStaff.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddStaff.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnAddStaffMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnAddStaffMouseExited(evt);
            }
        });
        btnAddStaff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddStaffActionPerformed(evt);
            }
        });
        toolBar.add(btnAddStaff);

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
        toolBar.add(btnDiscounts);

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
        toolBar.add(btnCategorys);

        btnSendData.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/send.png"))); // NOI18N
        btnSendData.setToolTipText("Send data to terminals");
        btnSendData.setFocusable(false);
        btnSendData.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSendData.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSendData.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnSendDataMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnSendDataMouseExited(evt);
            }
        });
        btnSendData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendDataActionPerformed(evt);
            }
        });
        toolBar.add(btnSendData);
        toolBar.add(jSeparator1);

        btnNewProduct.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/add.png"))); // NOI18N
        btnNewProduct.setToolTipText("Create a new product");
        btnNewProduct.setFocusable(false);
        btnNewProduct.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNewProduct.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNewProduct.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnNewProductMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnNewProductMouseExited(evt);
            }
        });
        btnNewProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewProductActionPerformed(evt);
            }
        });
        toolBar.add(btnNewProduct);

        btnReceiveStock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/receive.png"))); // NOI18N
        btnReceiveStock.setToolTipText("Receive stock");
        btnReceiveStock.setFocusable(false);
        btnReceiveStock.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnReceiveStock.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnReceiveStock.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnReceiveStockMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnReceiveStockMouseExited(evt);
            }
        });
        btnReceiveStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReceiveStockActionPerformed(evt);
            }
        });
        toolBar.add(btnReceiveStock);

        btnWasteStock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/waste.png"))); // NOI18N
        btnWasteStock.setToolTipText("Waste stock");
        btnWasteStock.setFocusable(false);
        btnWasteStock.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnWasteStock.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnWasteStock.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnWasteStockMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnWasteStockMouseExited(evt);
            }
        });
        btnWasteStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWasteStockActionPerformed(evt);
            }
        });
        toolBar.add(btnWasteStock);

        btnEnquiry.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/enquiry.png"))); // NOI18N
        btnEnquiry.setToolTipText("Product enquiry");
        btnEnquiry.setFocusable(false);
        btnEnquiry.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnEnquiry.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnEnquiry.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnEnquiryMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnEnquiryMouseExited(evt);
            }
        });
        btnEnquiry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEnquiryActionPerformed(evt);
            }
        });
        toolBar.add(btnEnquiry);
        toolBar.add(jSeparator2);

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
        toolBar.add(btnScreens);

        btnTerminals.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/terminals.png"))); // NOI18N
        btnTerminals.setToolTipText("View and edit terminals");
        btnTerminals.setFocusable(false);
        btnTerminals.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnTerminals.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnTerminals.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnTerminalsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnTerminalsMouseExited(evt);
            }
        });
        btnTerminals.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTerminalsActionPerformed(evt);
            }
        });
        toolBar.add(btnTerminals);

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
        toolBar.add(btnSettings);
        toolBar.add(jSeparator3);

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
        toolBar.add(btnReports);
        toolBar.add(filler2);

        lblServerAddress.setText("Local Server Address: 0.0.0.0");
        toolBar.add(lblServerAddress);
        toolBar.add(filler1);

        lblPort.setText("Port number: 0");
        toolBar.add(lblPort);

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

        lblWarnings.setText("Warnings: 0");
        lblWarnings.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblWarnings.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblWarningsMouseClicked(evt);
            }
        });

        lblClients.setText("Connections: 0");
        lblClients.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblClients.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblClientsMouseClicked(evt);
            }
        });

        lblTask.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout statusBarLayout = new javax.swing.GroupLayout(statusBar);
        statusBar.setLayout(statusBarLayout);
        statusBarLayout.setHorizontalGroup(
            statusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusBarLayout.createSequentialGroup()
                .addComponent(lblHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(lblUser, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(lblWarnings, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(lblClients, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(lblTask, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE))
        );
        statusBarLayout.setVerticalGroup(
            statusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblWarnings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblHelp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblClients, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblTask, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

        internal.setBackground(null);
        internal.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        javax.swing.GroupLayout internalLayout = new javax.swing.GroupLayout(internal);
        internal.setLayout(internalLayout);
        internalLayout.setHorizontalGroup(
            internalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        internalLayout.setVerticalGroup(
            internalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 375, Short.MAX_VALUE)
        );

        menuFile.setText("File");

        itemLogin.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        itemLogin.setText("Log in");
        itemLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemLoginActionPerformed(evt);
            }
        });
        menuFile.add(itemLogin);

        itemServerOptions.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        itemServerOptions.setText("Server Options");
        itemServerOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemServerOptionsActionPerformed(evt);
            }
        });
        menuFile.add(itemServerOptions);

        itemCheckDatabase.setText("Check Database");
        itemCheckDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemCheckDatabaseActionPerformed(evt);
            }
        });
        menuFile.add(itemCheckDatabase);

        itemInfo.setText("Info");
        itemInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemInfoActionPerformed(evt);
            }
        });
        menuFile.add(itemInfo);

        itemUpdate.setText("Check For Update");
        itemUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemUpdateActionPerformed(evt);
            }
        });
        menuFile.add(itemUpdate);

        jMenuItem1.setText("Backup...");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        menuFile.add(jMenuItem1);

        jMenuItem2.setText("Print Test");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        menuFile.add(jMenuItem2);

        itemExit.setText("Exit");
        itemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemExitActionPerformed(evt);
            }
        });
        menuFile.add(itemExit);

        jMenuBar1.add(menuFile);

        menuStock.setText("Stock");

        itemCreateNewProduct.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        itemCreateNewProduct.setText("Create New Product");
        itemCreateNewProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemCreateNewProductActionPerformed(evt);
            }
        });
        menuStock.add(itemCreateNewProduct);

        itemStock.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        itemStock.setText("Manage Stock");
        itemStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemStockActionPerformed(evt);
            }
        });
        menuStock.add(itemStock);

        itemEdit.setText("Edit a Product");
        itemEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemEditActionPerformed(evt);
            }
        });
        menuStock.add(itemEdit);

        itemReceive.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        itemReceive.setText("Receive Stock");
        itemReceive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemReceiveActionPerformed(evt);
            }
        });
        menuStock.add(itemReceive);

        itemOrdering.setText("Ordering");
        itemOrdering.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemOrderingActionPerformed(evt);
            }
        });
        menuStock.add(itemOrdering);

        itemOrderingWizard.setText("Ordering Wizard");
        itemOrderingWizard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemOrderingWizardActionPerformed(evt);
            }
        });
        menuStock.add(itemOrderingWizard);

        itemWasteStock.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        itemWasteStock.setText("Waste Stock");
        itemWasteStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemWasteStockActionPerformed(evt);
            }
        });
        menuStock.add(itemWasteStock);

        itemEnquiry.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        itemEnquiry.setText("Product Enquiry");
        itemEnquiry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemEnquiryActionPerformed(evt);
            }
        });
        menuStock.add(itemEnquiry);

        itemStockTake.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
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

        itemRefundReasons.setText("Edit Refund Reasons");
        itemRefundReasons.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemRefundReasonsActionPerformed(evt);
            }
        });
        menuSetup.add(itemRefundReasons);

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

        itemPluSettings.setText("Edit Barcode Settings");
        itemPluSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemPluSettingsActionPerformed(evt);
            }
        });
        menuSetup.add(itemPluSettings);

        itemSendData.setText("Send Data to Terminals...");
        itemSendData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemSendDataActionPerformed(evt);
            }
        });
        menuSetup.add(itemSendData);

        itemTerminals.setText("Terminals");
        itemTerminals.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemTerminalsActionPerformed(evt);
            }
        });
        menuSetup.add(itemTerminals);

        itemSiteDetails.setText("Site Details");
        itemSiteDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemSiteDetailsActionPerformed(evt);
            }
        });
        menuSetup.add(itemSiteDetails);

        itemDatabase.setText("Database");
        itemDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemDatabaseActionPerformed(evt);
            }
        });
        menuSetup.add(itemDatabase);

        jMenuBar1.add(menuSetup);

        itemConsolodated.setText("Report");

        itemSalesReporting.setText("Sales Reporting");
        itemSalesReporting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemSalesReportingActionPerformed(evt);
            }
        });
        itemConsolodated.add(itemSalesReporting);

        itemTransactionViewer.setText("Transaction Viewer");
        itemTransactionViewer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemTransactionViewerActionPerformed(evt);
            }
        });
        itemConsolodated.add(itemTransactionViewer);

        itemLabelPrinting.setText("Label Printing");
        itemLabelPrinting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemLabelPrintingActionPerformed(evt);
            }
        });
        itemConsolodated.add(itemLabelPrinting);

        itemStaffClocking.setText("Staff Hours");
        itemStaffClocking.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemStaffClockingActionPerformed(evt);
            }
        });
        itemConsolodated.add(itemStaffClocking);

        itemStaffReporting.setText("Staff Reporting");
        itemStaffReporting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemStaffReportingActionPerformed(evt);
            }
        });
        itemConsolodated.add(itemStaffReporting);

        itemWasteReports.setText("Waste Reports");
        itemWasteReports.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemWasteReportsActionPerformed(evt);
            }
        });
        itemConsolodated.add(itemWasteReports);

        itemReceivedReports.setText("Received Reports");
        itemReceivedReports.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemReceivedReportsActionPerformed(evt);
            }
        });
        itemConsolodated.add(itemReceivedReports);

        itemDeclarations.setText("Declaration Reports");
        itemDeclarations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemDeclarationsActionPerformed(evt);
            }
        });
        itemConsolodated.add(itemDeclarations);

        itemConsolidated.setText("Consolidated Report");
        itemConsolidated.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemConsolidatedActionPerformed(evt);
            }
        });
        itemConsolodated.add(itemConsolidated);

        itemStockReport.setText("Stock Report");
        itemStockReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemStockReportActionPerformed(evt);
            }
        });
        itemConsolodated.add(itemStockReport);

        jMenuBar1.add(itemConsolodated);

        menuHelp.setText("Help");

        itemAbout.setText("About");
        itemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemAboutActionPerformed(evt);
            }
        });
        menuHelp.add(itemAbout);

        itemHelp.setText("Help Pages");
        itemHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemHelpActionPerformed(evt);
            }
        });
        menuHelp.add(itemHelp);

        jMenuBar1.add(menuHelp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(statusBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chkSevere)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkWarning)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkInfo)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
            .addComponent(internal, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(internal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
        ProductsWindow.showProductsListWindow();
    }//GEN-LAST:event_btnManageStockActionPerformed

    private void itemStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemStockActionPerformed
        ProductsWindow.showProductsListWindow();
    }//GEN-LAST:event_itemStockActionPerformed

    private void itemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemExitActionPerformed
        if (dc instanceof DBConnect) {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to stop JTill server?", "JTill Server", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }
            LOG.log(Level.INFO, "Stopping");
            LOG.log(Level.INFO, "Stopping JTIll Server");
            LOG.log(Level.INFO, "Saving properties");
            settings.saveProperties(); //Save the server properties
            TillServer.removeSystemTrayIcon(); //Remove the system tray icon
        } else {
            try {
                ((ServerConnection) dc).close(); //Close the Database/Server connection
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        System.exit(0); //Exit the application
    }//GEN-LAST:event_itemExitActionPerformed

    private void itemCustomersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemCustomersActionPerformed
        CustomersWindow.showCustomersListWindow();
    }//GEN-LAST:event_itemCustomersActionPerformed

    private void btnManageCustomersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageCustomersActionPerformed
        CustomersWindow.showCustomersListWindow();
    }//GEN-LAST:event_btnManageCustomersActionPerformed

    private void itemLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemLoginActionPerformed
        if (staff != null) {
            logout();
        } else {
            login();
        }
    }//GEN-LAST:event_itemLoginActionPerformed

    private void itemStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemStaffActionPerformed
        StaffWindow.showStaffListWindow();
    }//GEN-LAST:event_itemStaffActionPerformed

    private void btnManageStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageStaffActionPerformed
        StaffWindow.showStaffListWindow();
    }//GEN-LAST:event_btnManageStaffActionPerformed

    private void lblClientsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblClientsMouseClicked

    }//GEN-LAST:event_lblClientsMouseClicked

    private void lblUserMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblUserMouseClicked
        if (evt.getClickCount() == 2) {
            String position = "";
            switch (staff.getPosition()) {
                case Staff.ASSISSTANT: {
                    position = "Assisstant";
                    break;
                }
                case Staff.SUPERVISOR: {
                    position = "Supervisor";
                    break;
                }
                case Staff.MANAGER: {
                    position = "Manager";
                    break;
                }
                case Staff.AREA_MANAGER: {
                    position = "Area Manager";
                    break;
                }
            }
            JOptionPane.showInternalMessageDialog(GUI.gui.internal, "ID: " + staff.getName() + "\nName: " + staff.getName() + "\nPosition: " + position, staff.getName(), JOptionPane.PLAIN_MESSAGE);
        }
    }//GEN-LAST:event_lblUserMouseClicked

    private void lblHelpMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblHelpMouseClicked
        if (evt.getClickCount() == 2) {
            new HelpPage().setVisible(true);
        }
    }//GEN-LAST:event_lblHelpMouseClicked

    private void itemDiscountsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemDiscountsActionPerformed
        DiscountsWindow.showDiscountListWindow();
    }//GEN-LAST:event_itemDiscountsActionPerformed

    private void itemCategorysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemCategorysActionPerformed
        CategorysWindow.showCategoryWindow();
    }//GEN-LAST:event_itemCategorysActionPerformed

    private void itemTaxesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemTaxesActionPerformed
        TaxWindow.showTaxWindow();
    }//GEN-LAST:event_itemTaxesActionPerformed

    private void btnDiscountsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiscountsActionPerformed
        DiscountsWindow.showDiscountListWindow();
    }//GEN-LAST:event_btnDiscountsActionPerformed

    private void btnCategorysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCategorysActionPerformed
        CategorysWindow.showCategoryWindow();
    }//GEN-LAST:event_btnCategorysActionPerformed

    private void itemServerOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemServerOptionsActionPerformed
        SettingsWindow.showSettingsWindow();
    }//GEN-LAST:event_itemServerOptionsActionPerformed

    private void itemInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemInfoActionPerformed
        try {
            JOptionPane.showMessageDialog(this, "JTill Server is running on port number "
                    + dc.getSetting("port"), "JTill Server",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_itemInfoActionPerformed

    private void btnReportsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReportsActionPerformed
        ReportingWindow.showWindow();
    }//GEN-LAST:event_btnReportsActionPerformed

    private void btnSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSettingsActionPerformed
        SettingsWindow.showSettingsWindow();
    }//GEN-LAST:event_btnSettingsActionPerformed

    private void btnScreensActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScreensActionPerformed
        ScreenEditWindow.showScreenEditWindow();
    }//GEN-LAST:event_btnScreensActionPerformed

    private void itemReceiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemReceiveActionPerformed
        ReceiveItemsWindow.showWindow();
    }//GEN-LAST:event_itemReceiveActionPerformed

    private void itemWasteStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemWasteStockActionPerformed
        WasteStockWindow.showWindow();
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
        WasteReports.showWindow(this);
    }//GEN-LAST:event_itemWasteReportsActionPerformed

    private void itemReasonsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemReasonsActionPerformed
        WasteReasonDialog.showDialog();
    }//GEN-LAST:event_itemReasonsActionPerformed

    private void itemTillScreensActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemTillScreensActionPerformed
        ScreenEditWindow.showScreenEditWindow();
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
        SupplierWindow.showWindow();
    }//GEN-LAST:event_itemSuppliersActionPerformed

    private void itemDepartmentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemDepartmentsActionPerformed
        DepartmentsWindow.showWindow();
    }//GEN-LAST:event_itemDepartmentsActionPerformed

    private void itemSalesReportingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemSalesReportingActionPerformed
        int resp = SaleReportDialog.showDialog(this);
        switch (resp) {
            case SaleReportDialog.DEPARTMENT_REPORT:
                Department d = DepartmentSelectDialog.showDialog(this);
                break;
            case SaleReportDialog.CATEGORY_REPORT:
                Category c = CategorySelectDialog.showDialog(this);
                break;
            case SaleReportDialog.CLERK_REPORT:
                Staff s = StaffSelectDialog.showDialog(this);
                break;
            default:
                break;
        }
    }//GEN-LAST:event_itemSalesReportingActionPerformed

    private void itemEnquiryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemEnquiryActionPerformed
        ProductEnquiry.showWindow();
    }//GEN-LAST:event_itemEnquiryActionPerformed

    private void itemLabelPrintingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemLabelPrintingActionPerformed
        LabelPrintingWindow.showWindow();
    }//GEN-LAST:event_itemLabelPrintingActionPerformed

    private void itemCreateNewProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemCreateNewProductActionPerformed
        ProductEntryDialog.showDialog(this);
    }//GEN-LAST:event_itemCreateNewProductActionPerformed

    private void itemNewStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemNewStaffActionPerformed
        Staff s = StaffDialog.showNewStaffDialog(this);
        if (s != null) {
            JOptionPane.showMessageDialog(this, "New staff member " + s + " created");
        }
    }//GEN-LAST:event_itemNewStaffActionPerformed

    private void itemStaffClockingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemStaffClockingActionPerformed
        StaffClocking.showWindow();
    }//GEN-LAST:event_itemStaffClockingActionPerformed

    private void itemStockTakeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemStockTakeActionPerformed
        StockTakeWindow.showWindow();
    }//GEN-LAST:event_itemStockTakeActionPerformed

    private void itemPluSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemPluSettingsActionPerformed
        BarcodeSettings.showWindow();
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

    private void lblWarningsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblWarningsMouseClicked
        if (evt.getClickCount() == 2) {
            WarningDialog.showDialog(warningsList);
        }
    }//GEN-LAST:event_lblWarningsMouseClicked

    private void itemTerminalsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemTerminalsActionPerformed
        TillWindow.showWindow();
    }//GEN-LAST:event_itemTerminalsActionPerformed

    private void itemCheckDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemCheckDatabaseActionPerformed
        final ModalDialog mDialog = new ModalDialog(this, "Database Check", "Checking database integrity..."); //Create the dialog object
        final Runnable run = () -> {
            try {
                dc.integrityCheck(); //Perform the Database check
                mDialog.hide(); //Hide the dialog once the check completes
                JOptionPane.showInternalMessageDialog(GUI.gui.internal, "Check complete. No Issues.", "Database Check", JOptionPane.INFORMATION_MESSAGE); //Show success message
            } catch (IOException | SQLException ex) {
                mDialog.hide(); //Hide the dialog if there is an error
                JOptionPane.showInternalMessageDialog(GUI.gui.internal, ex, "Database Check", JOptionPane.ERROR_MESSAGE); //Show the error
            }
        }; //Create the runnable for performing the database check
        final Thread thread = new Thread(run); //Create the thread for running the integrity check
        thread.start(); //Start the thread
        mDialog.show(); //Show the running dialog
    }//GEN-LAST:event_itemCheckDatabaseActionPerformed

    private void itemUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemUpdateActionPerformed
        new Thread() {
            @Override
            public void run() {
                try {
                    String latest = UpdateChecker.checkForUpdate(); //Get the latest version of JTillServer
                    if (!latest.equals(TillServer.VERSION)) { //Check to see if this is the latest version
                        if (JOptionPane.showInternalConfirmDialog(GUI.gui.internal, "Version " + latest + " avaliable. Download now?", "Update", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            UpdateChecker.downloadServerUpdate();
                        }
                    } else {
                        JOptionPane.showInternalMessageDialog(GUI.gui.internal, "You are currently at the latest version", "Update", JOptionPane.INFORMATION_MESSAGE); //Display message to indicate the this is the latest version
                    }
                } catch (Exception ex) {
                    JOptionPane.showInternalMessageDialog(GUI.gui.internal, "Error checking for update", "Update", JOptionPane.INFORMATION_MESSAGE); //Display error message
                }
            }
        }.start(); //Start this thread
    }//GEN-LAST:event_itemUpdateActionPerformed

    private void itemTransactionViewerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemTransactionViewerActionPerformed
        final ModalDialog md = new ModalDialog(this, "Transactions", "Loading tranactions...");
        final Runnable run = () -> {
            try {
                TransactionViewerWindow.showWindow();
            } finally {
                md.hide();
                this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        };
        final Thread thread = new Thread(run, "TransactionWindow");
        thread.start();
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        md.show();
    }//GEN-LAST:event_itemTransactionViewerActionPerformed

    private void itemStaffReportingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemStaffReportingActionPerformed
        StaffReportingWindow.showWindow();
    }//GEN-LAST:event_itemStaffReportingActionPerformed

    private void itemSendDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemSendDataActionPerformed
//        try {
//            dc.reinitialiseAllTills();
//        } catch (IOException ex) {
//            JOptionPane.showMessageDialog(internal, ex, "Error", JOptionPane.ERROR_MESSAGE);
//        } catch (JTillException ex) {
//            JOptionPane.showMessageDialog(this, ex.getMessage(), "Active inits", JOptionPane.ERROR_MESSAGE);
//        }
        SendDataDialog.showDialog(this);
    }//GEN-LAST:event_itemSendDataActionPerformed

    private void itemDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemDatabaseActionPerformed
        DatabaseWindow.showDatabaseWindow();
    }//GEN-LAST:event_itemDatabaseActionPerformed

    private void itemReceivedReportsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemReceivedReportsActionPerformed
        ReceivedReportsWindow.showWindow();
    }//GEN-LAST:event_itemReceivedReportsActionPerformed

    private void btnSendDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendDataActionPerformed
        try {
            dc.reinitialiseAllTills();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(internal, ex, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (JTillException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Active inits", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSendDataActionPerformed

    private void btnSendDataMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSendDataMouseEntered
        lblHelp.setText("Send data to terminals");
    }//GEN-LAST:event_btnSendDataMouseEntered

    private void btnSendDataMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSendDataMouseExited
        lblHelp.setText(HELP_TEXT);
    }//GEN-LAST:event_btnSendDataMouseExited

    private void btnTerminalsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnTerminalsMouseEntered
        lblHelp.setText("View and edit terminals");
    }//GEN-LAST:event_btnTerminalsMouseEntered

    private void btnTerminalsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnTerminalsMouseExited
        lblHelp.setText(HELP_TEXT);
    }//GEN-LAST:event_btnTerminalsMouseExited

    private void btnTerminalsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTerminalsActionPerformed
        TillWindow.showWindow();
    }//GEN-LAST:event_btnTerminalsActionPerformed

    private void btnNewProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewProductActionPerformed
        ProductEntryDialog.showDialog(this);
    }//GEN-LAST:event_btnNewProductActionPerformed

    private void btnWasteStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWasteStockActionPerformed
        WasteStockWindow.showWindow();
    }//GEN-LAST:event_btnWasteStockActionPerformed

    private void btnNewProductMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnNewProductMouseEntered
        lblHelp.setText("Create a new product");
    }//GEN-LAST:event_btnNewProductMouseEntered

    private void btnNewProductMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnNewProductMouseExited
        lblHelp.setText(HELP_TEXT);
    }//GEN-LAST:event_btnNewProductMouseExited

    private void btnWasteStockMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnWasteStockMouseEntered
        lblHelp.setText("Waste stock");
    }//GEN-LAST:event_btnWasteStockMouseEntered

    private void btnWasteStockMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnWasteStockMouseExited
        lblHelp.setText(HELP_TEXT);
    }//GEN-LAST:event_btnWasteStockMouseExited

    private void btnAddStaffMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAddStaffMouseEntered
        lblHelp.setText("Add a member of staff");
    }//GEN-LAST:event_btnAddStaffMouseEntered

    private void btnAddStaffMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAddStaffMouseExited
        lblHelp.setText(HELP_TEXT);
    }//GEN-LAST:event_btnAddStaffMouseExited

    private void btnAddStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddStaffActionPerformed
        Staff s = StaffDialog.showNewStaffDialog(this);
        if (s != null) {
            JOptionPane.showMessageDialog(this, "New staff member " + s + " created");
        }
    }//GEN-LAST:event_btnAddStaffActionPerformed

    private void btnReceiveStockMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnReceiveStockMouseEntered
        lblHelp.setText("Receive stock");
    }//GEN-LAST:event_btnReceiveStockMouseEntered

    private void btnReceiveStockMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnReceiveStockMouseExited
        lblHelp.setText(HELP_TEXT);
    }//GEN-LAST:event_btnReceiveStockMouseExited

    private void btnReceiveStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReceiveStockActionPerformed
        ReceiveItemsWindow.showWindow();
    }//GEN-LAST:event_btnReceiveStockActionPerformed

    private void btnEnquiryMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEnquiryMouseEntered
        lblHelp.setText("Product enquiry");
    }//GEN-LAST:event_btnEnquiryMouseEntered

    private void btnEnquiryMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEnquiryMouseExited
        lblHelp.setText(HELP_TEXT);
    }//GEN-LAST:event_btnEnquiryMouseExited

    private void btnEnquiryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnquiryActionPerformed
        ProductEnquiry.showWindow();
    }//GEN-LAST:event_btnEnquiryActionPerformed

    private void itemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemAboutActionPerformed
        try {
            Object[] licenseInfo = dc.getLicenseInfo();
            String number = (String) licenseInfo[0];
            int connections = (int) licenseInfo[1];
            JOptionPane.showMessageDialog(this, "JTill Server version " + TillServer.VERSION + "\n"
                    + "License Number: " + number + "\n"
                    + "Maximum Connections: " + connections, "JTill Server", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_itemAboutActionPerformed

    private void itemHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemHelpActionPerformed
        new HelpPage().setVisible(true);
    }//GEN-LAST:event_itemHelpActionPerformed

    private void itemDeclarationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemDeclarationsActionPerformed
        DeclarationReportWindow.showWindow();
    }//GEN-LAST:event_itemDeclarationsActionPerformed

    private void itemConsolidatedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemConsolidatedActionPerformed
        ConsolidatedReportingDialog.showDialog(this);
    }//GEN-LAST:event_itemConsolidatedActionPerformed

    private void itemStockReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemStockReportActionPerformed
        StockReportDialog.showDialog(this);
    }//GEN-LAST:event_itemStockReportActionPerformed

    private void itemSiteDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemSiteDetailsActionPerformed
        CompanyDetailsDialog.showDialog(this);
    }//GEN-LAST:event_itemSiteDetailsActionPerformed

    private void itemOrderingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemOrderingActionPerformed
        OrdersViewer.showWindow();
    }//GEN-LAST:event_itemOrderingActionPerformed

    private void itemOrderingWizardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemOrderingWizardActionPerformed
        OrderingWizard.showDialog(this);
    }//GEN-LAST:event_itemOrderingWizardActionPerformed

    private void itemEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemEditActionPerformed
        ProductEditWindow.showWindow();
    }//GEN-LAST:event_itemEditActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        BackupDialog.showDialog(this);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void itemRefundReasonsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemRefundReasonsActionPerformed
        RefundReasonsDialog.showDialog();
    }//GEN-LAST:event_itemRefundReasonsActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        new PrintTest().setVisible(true);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddStaff;
    private javax.swing.JButton btnCategorys;
    private javax.swing.JButton btnDiscounts;
    private javax.swing.JButton btnEnquiry;
    private javax.swing.JButton btnManageCustomers;
    private javax.swing.JButton btnManageStaff;
    private javax.swing.JButton btnManageStock;
    private javax.swing.JButton btnNewProduct;
    private javax.swing.JButton btnReceiveStock;
    private javax.swing.JButton btnReports;
    private javax.swing.JButton btnScreens;
    private javax.swing.JButton btnSendData;
    private javax.swing.JButton btnSettings;
    private javax.swing.JButton btnTerminals;
    private javax.swing.JButton btnWasteStock;
    private javax.swing.JCheckBox chkInfo;
    private javax.swing.JCheckBox chkSevere;
    private javax.swing.JCheckBox chkWarning;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    public javax.swing.JDesktopPane internal;
    private javax.swing.JMenuItem itemAbout;
    private javax.swing.JMenuItem itemCategorys;
    private javax.swing.JMenuItem itemCheckDatabase;
    private javax.swing.JMenuItem itemConsolidated;
    private javax.swing.JMenu itemConsolodated;
    private javax.swing.JMenuItem itemCreateNewProduct;
    private javax.swing.JMenuItem itemCustomers;
    private javax.swing.JMenuItem itemDatabase;
    private javax.swing.JMenuItem itemDeclarations;
    private javax.swing.JMenuItem itemDepartments;
    private javax.swing.JMenuItem itemDiscounts;
    private javax.swing.JMenuItem itemEdit;
    private javax.swing.JMenuItem itemEnquiry;
    private javax.swing.JMenuItem itemExit;
    private javax.swing.JMenuItem itemHelp;
    private javax.swing.JMenuItem itemInfo;
    private javax.swing.JMenuItem itemLabelPrinting;
    private javax.swing.JMenuItem itemLogin;
    private javax.swing.JMenuItem itemNewStaff;
    private javax.swing.JMenuItem itemOrdering;
    private javax.swing.JMenuItem itemOrderingWizard;
    private javax.swing.JMenuItem itemPluSettings;
    private javax.swing.JMenuItem itemReasons;
    private javax.swing.JMenuItem itemReceive;
    private javax.swing.JMenuItem itemReceivedReports;
    private javax.swing.JMenuItem itemRefundReasons;
    private javax.swing.JMenuItem itemSalesReporting;
    private javax.swing.JMenuItem itemSendData;
    private javax.swing.JMenuItem itemServerOptions;
    private javax.swing.JMenuItem itemSiteDetails;
    private javax.swing.JMenuItem itemStaff;
    private javax.swing.JMenuItem itemStaffClocking;
    private javax.swing.JMenuItem itemStaffReporting;
    private javax.swing.JMenuItem itemStock;
    private javax.swing.JMenuItem itemStockReport;
    private javax.swing.JMenuItem itemStockTake;
    private javax.swing.JMenuItem itemSuppliers;
    private javax.swing.JMenuItem itemTaxes;
    private javax.swing.JMenuItem itemTerminals;
    private javax.swing.JMenuItem itemTillScreens;
    private javax.swing.JMenuItem itemTransactionViewer;
    private javax.swing.JMenuItem itemUpdate;
    private javax.swing.JMenuItem itemWasteReports;
    private javax.swing.JMenuItem itemWasteStock;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JLabel lblClients;
    private javax.swing.JLabel lblHelp;
    private javax.swing.JLabel lblPort;
    private javax.swing.JLabel lblServerAddress;
    private javax.swing.JLabel lblTask;
    private javax.swing.JLabel lblUser;
    private javax.swing.JLabel lblWarnings;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuHelp;
    private javax.swing.JMenu menuSetup;
    private javax.swing.JMenu menuStock;
    private javax.swing.JPanel statusBar;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JTextArea txtLog;
    // End of variables declaration//GEN-END:variables

    @Override
    public void allow(Till t) {

    }

    @Override
    public void disallow() {
    }

}
