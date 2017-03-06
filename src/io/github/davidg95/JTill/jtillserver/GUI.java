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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * The main GUI for the server.
 *
 * @author David
 */
public class GUI extends javax.swing.JFrame implements GUIInterface {

    private static GUI gui;

    private final DataConnect dbConn;
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
        this.dbConn = dataConnect;
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
        this.dbConn.setGUI(this);
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
            lblProducts.setText("Products in database: " + dbConn.getAllProducts().size());
        } catch (IOException | SQLException ex) {
            lblProducts.setText("Products in database: UNKNOWN");
        }
    }

    /**
     * Initial setup of the gui which creates the database.
     */
    private void initialSetup() {
        if (dbConn instanceof DBConnect) {
            try {
                DBConnect db = (DBConnect) dbConn;
                TillSplashScreen.setLabel("Creating database...");
                db.create(settings.getSetting("db_address") + "create=true;", settings.getSetting("db_username"), settings.getSetting("db_password"));
                TillSplashScreen.setLabel("Creating tables...");
                Staff s = StaffDialog.showNewStaffDialog(this, db);
                if (s == null) {
                    System.exit(0);
                }
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 40000) {
                    JOptionPane.showMessageDialog(this, "The database is already in use by another application. Program will now terminate.\nError Code " + ex.getErrorCode(), "Database in use", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                } else {
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
                DBConnect db = (DBConnect) dbConn;
                db.connect(settings.getSetting("db_address"), settings.getSetting("db_username"), settings.getSetting("db_password"));
                TillSplashScreen.setLabel("Connected to database");
                TillSplashScreen.addBar(40);
                lblDatabase.setText("Connected to database");
                if (dbConn.staffCount() == 0) {
                    Staff s = StaffDialog.showNewStaffDialog(this, db);
                    if (s == null) {
                        System.exit(0);
                    }
                }
            } catch (SQLException ex) {
                initialSetup();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex, "Server Error", JOptionPane.ERROR_MESSAGE);
            } catch (StaffNotFoundException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
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

    /**
     * Method to log a member of staff in to the server.
     */
    public void login() {
        staff = LoginDialog.showLoginDialog(this, dbConn);
        if (staff != null) {
            lblUser.setText(staff.getName());
            itemLogin.setText("Log Out");
            log(staff.getName() + " has logged in");
            isLoggedOn = true;
        } else {
            if (dbConn instanceof DBConnect) {
                DBConnect db = (DBConnect) dbConn;
                settings.saveProperties();
            }
            if (remote) {
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
            dbConn.logout(staff);
            log(staff.getName() + " has logged out");
        } catch (StaffNotFoundException ex) {
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
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
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        itemLogin = new javax.swing.JMenuItem();
        itemServerOptions = new javax.swing.JMenuItem();
        itemAbout = new javax.swing.JMenuItem();
        itemExit = new javax.swing.JMenuItem();
        menuStock = new javax.swing.JMenu();
        itemStock = new javax.swing.JMenuItem();
        itemDiscounts = new javax.swing.JMenuItem();
        itemVouchers = new javax.swing.JMenuItem();
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

        itemDiscounts.setText("Manage Discounts");
        itemDiscounts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemDiscountsActionPerformed(evt);
            }
        });
        menuStock.add(itemDiscounts);

        itemVouchers.setText("Manage Vouchers");
        itemVouchers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemVouchersActionPerformed(evt);
            }
        });
        menuStock.add(itemVouchers);

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
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(lblServerAddress)
                            .addComponent(lblPort)
                            .addComponent(lblProducts)
                            .addComponent(jButton1))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblServerAddress)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPort)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblProducts)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 191, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(statusBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnManageStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageStockActionPerformed
        ProductsWindow.showProductsListWindow(dbConn, icon);
    }//GEN-LAST:event_btnManageStockActionPerformed

    private void itemStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemStockActionPerformed
        ProductsWindow.showProductsListWindow(dbConn, icon);
    }//GEN-LAST:event_itemStockActionPerformed

    private void itemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemExitActionPerformed
        if (dbConn instanceof DBConnect) {
            DBConnect db = (DBConnect) dbConn;
            settings.saveProperties();
            TillServer.removeSystemTrayIcon();
        }
        try {
            dbConn.close();
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }//GEN-LAST:event_itemExitActionPerformed

    private void itemCustomersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemCustomersActionPerformed
        CustomersWindow.showCustomersListWindow(dbConn, icon);
    }//GEN-LAST:event_itemCustomersActionPerformed

    private void btnManageCustomersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageCustomersActionPerformed
        CustomersWindow.showCustomersListWindow(dbConn, icon);
    }//GEN-LAST:event_btnManageCustomersActionPerformed

    private void itemLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemLoginActionPerformed
        if (staff != null) {
            logout();
        } else {
            login();
        }
    }//GEN-LAST:event_itemLoginActionPerformed

    private void itemStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemStaffActionPerformed
        StaffWindow.showStaffListWindow(dbConn, icon);
    }//GEN-LAST:event_itemStaffActionPerformed

    private void btnManageStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageStaffActionPerformed
        StaffWindow.showStaffListWindow(dbConn, icon);
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
            JOptionPane.showMessageDialog(this, dbConn, "Database Connection", JOptionPane.PLAIN_MESSAGE);
        }
    }//GEN-LAST:event_lblDatabaseMouseClicked

    private void itemDiscountsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemDiscountsActionPerformed
        DiscountsWindow.showDiscountListWindow(dbConn, icon);
    }//GEN-LAST:event_itemDiscountsActionPerformed

    private void itemCategorysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemCategorysActionPerformed
        CategorysWindow.showCategoryWindow(dbConn, icon);
    }//GEN-LAST:event_itemCategorysActionPerformed

    private void itemTaxesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemTaxesActionPerformed
        TaxWindow.showTaxWindow(dbConn, icon);
    }//GEN-LAST:event_itemTaxesActionPerformed

    private void btnDiscountsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiscountsActionPerformed
        DiscountsWindow.showDiscountListWindow(dbConn, icon);
    }//GEN-LAST:event_btnDiscountsActionPerformed

    private void btnCategorysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCategorysActionPerformed
        CategorysWindow.showCategoryWindow(dbConn, icon);
    }//GEN-LAST:event_btnCategorysActionPerformed

    private void itemServerOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemServerOptionsActionPerformed
        SettingsWindow.showSettingsWindow(dbConn, icon);
    }//GEN-LAST:event_itemServerOptionsActionPerformed

    private void itemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemAboutActionPerformed
        JOptionPane.showMessageDialog(null, "JTill Server is running on port number "
                + settings.getSetting("port") + " with " + clientCounter + " connections.\n"
                + dbConn.toString(), "JTill Server",
                JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_itemAboutActionPerformed

    private void itemSalesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemSalesActionPerformed
//        JOptionPane.showMessageDialog(this, "Takings: £" + data.getTakings() + "\nSales: " + data.getSales(), "Sales Data", JOptionPane.PLAIN_MESSAGE);
        SalesWindow.showSalesWindow(dbConn, icon);
    }//GEN-LAST:event_itemSalesActionPerformed

    private void itemResetSalesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemResetSalesActionPerformed
        JOptionPane.showMessageDialog(this, "Sales data reset", "Sales Data", JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_itemResetSalesActionPerformed

    private void btnReportsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReportsActionPerformed
        SalesWindow.showSalesWindow(dbConn, icon);
    }//GEN-LAST:event_btnReportsActionPerformed

    private void btnSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSettingsActionPerformed
        SettingsWindow.showSettingsWindow(dbConn, icon);
    }//GEN-LAST:event_btnSettingsActionPerformed

    private void btnScreensActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScreensActionPerformed
        ScreenEditWindow.showScreenEditWindow(dbConn, icon);
    }//GEN-LAST:event_btnScreensActionPerformed

    private void itemVouchersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemVouchersActionPerformed
        VoucherWindow.showVoucherWindow(dbConn);
    }//GEN-LAST:event_itemVouchersActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            for (Staff s : dbConn.getAllStaff()) {
                try {
                    dbConn.tillLogout(s);
                } catch (IOException | StaffNotFoundException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException | SQLException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCategorys;
    private javax.swing.JButton btnDiscounts;
    private javax.swing.JButton btnManageCustomers;
    private javax.swing.JButton btnManageStaff;
    private javax.swing.JButton btnManageStock;
    private javax.swing.JButton btnReports;
    private javax.swing.JButton btnScreens;
    private javax.swing.JButton btnSettings;
    private javax.swing.JMenuItem itemAbout;
    private javax.swing.JMenuItem itemCategorys;
    private javax.swing.JMenuItem itemCustomers;
    private javax.swing.JMenuItem itemDiscounts;
    private javax.swing.JMenuItem itemExit;
    private javax.swing.JMenuItem itemLogin;
    private javax.swing.JMenuItem itemResetSales;
    private javax.swing.JMenuItem itemSales;
    private javax.swing.JMenuItem itemServerOptions;
    private javax.swing.JMenuItem itemStaff;
    private javax.swing.JMenuItem itemStock;
    private javax.swing.JMenuItem itemTaxes;
    private javax.swing.JMenuItem itemVouchers;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
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
    // End of variables declaration//GEN-END:variables

    @Override
    public void allow() {

    }

    @Override
    public void disallow() {
    }

}
