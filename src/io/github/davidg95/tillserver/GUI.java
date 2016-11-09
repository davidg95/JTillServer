/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.tillserver;

import io.github.davidg95.Till.till.DBConnect;
import io.github.davidg95.Till.till.Staff;
import io.github.davidg95.Till.till.StaffNotFoundException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author David
 */
public class GUI extends javax.swing.JFrame {

//    private String database_address = "jdbc:derby://localhost:1527/TillTest";
//    private String username = "davidg95";
//    private String password = "adventures";
    private String database_address;
    private String username;
    private String password;

    private Properties connectionProperties;

    private final Data data;
    private final DBConnect dbConnection;

    private Staff staff;

    private int clientCounter = 0;
    private final ArrayList<String> connections;

    /**
     * Creates new form GUI
     *
     * @param data
     * @param dbConnection
     */
    public GUI(Data data, DBConnect dbConnection) {
        this.dbConnection = dbConnection;
        this.data = data;
        initComponents();
        setClientLabel("Connections: 0");
        connections = new ArrayList<>();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public void databaseLogin() {
        openFile();
        try {
            if (database_address == null || database_address.equals("null")) {
                InitialSetupWindow.showInitWindow(this, data, dbConnection);
                this.database_address = dbConnection.getAddress();
                this.username = dbConnection.getUsername();
                this.password = dbConnection.getPassword();
            } else {
                //DatabaseConnectionDialog.showConnectionDialog(this, this.dbConnection);
                dbConnection.connect(database_address, username, password);
            }
            try {
                dbConnection.initDatabase();
                data.loadDatabase();
                lblDatabase.setText("Connected to database");
                saveToFile();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Initialisation Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            itemDatabaseConnect.setEnabled(true);
            itemDatabaseConnect.setText("Connect To Database");
            itemUpdate.setEnabled(false);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Connection Error", JOptionPane.ERROR_MESSAGE);
            newDatabaseLogin();
        }
    }
    
    public void newDatabaseLogin(){
        DatabaseConnectionDialog.showConnectionDialog(this, dbConnection);
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

    public void setClientLabel(String text) {
        lblClients.setText(text);
    }

    public void login() {
        staff = LoginDialog.showLoginDialog(this, data);
        if (staff != null) {
            lblUser.setText(staff.getName());
            itemLogin.setText("Log Out");
        } else {
            System.exit(0);
        }
    }

    private void logout() {
        try {
            lblUser.setText("Not Logged On");
            data.logout(staff.getId());
        } catch (StaffNotFoundException ex) {
        }
        staff = null;
        itemLogin.setText("Log In");
        login();
    }

    /**
     * Method to save the configs.
     */
    private void saveToFile() {
        try (PrintWriter writer = new PrintWriter("database.txt", "UTF-8")) {
            writer.println(database_address);
            writer.println(username);
            writer.println(password);
            writer.println(TillServer.updateInterval);
        } catch (IOException ex) {

        }
    }

    /**
     * Method to load the configs.
     */
    private final void openFile() {
        try {
            Scanner fileReader = new Scanner(new File("database.txt"));

            if (fileReader.hasNext()) {
                database_address = fileReader.nextLine();
                username = fileReader.nextLine();
                password = fileReader.nextLine();
                TillServer.updateInterval = Long.parseLong(fileReader.nextLine());
            }
        } catch (IOException e) {
            try {
                boolean createNewFile = new File("database.txt").createNewFile();
            } catch (IOException ex) {
            }
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
        statusBar = new javax.swing.JPanel();
        lblDatabase = new javax.swing.JLabel();
        lblUser = new javax.swing.JLabel();
        lblUpdate = new javax.swing.JLabel();
        lblClients = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        itemLogin = new javax.swing.JMenuItem();
        itemDatabaseConnect = new javax.swing.JMenuItem();
        itemUpdate = new javax.swing.JMenuItem();
        itemInterval = new javax.swing.JMenuItem();
        itemExit = new javax.swing.JMenuItem();
        menuStock = new javax.swing.JMenu();
        itemStock = new javax.swing.JMenuItem();
        itemPromotions = new javax.swing.JMenuItem();
        menuStaff = new javax.swing.JMenu();
        itemStaff = new javax.swing.JMenuItem();
        menuCustomers = new javax.swing.JMenu();
        itemCustomers = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Till Server");

        jToolBar1.setRollover(true);

        btnManageStock.setText("Manage Stock");
        btnManageStock.setFocusable(false);
        btnManageStock.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnManageStock.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnManageStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnManageStockActionPerformed(evt);
            }
        });
        jToolBar1.add(btnManageStock);

        btnManageCustomers.setText("Manage Customers");
        btnManageCustomers.setFocusable(false);
        btnManageCustomers.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnManageCustomers.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnManageCustomers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnManageCustomersActionPerformed(evt);
            }
        });
        jToolBar1.add(btnManageCustomers);

        btnManageStaff.setText("Manage Staff");
        btnManageStaff.setFocusable(false);
        btnManageStaff.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnManageStaff.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnManageStaff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnManageStaffActionPerformed(evt);
            }
        });
        jToolBar1.add(btnManageStaff);

        lblDatabase.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblDatabase.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblDatabaseMouseClicked(evt);
            }
        });

        lblUser.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblUser.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblUserMouseClicked(evt);
            }
        });

        lblUpdate.setBorder(javax.swing.BorderFactory.createEtchedBorder());

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
            .addComponent(lblUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, 18, Short.MAX_VALUE)
            .addComponent(lblDatabase, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblClients, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        menuFile.setText("File");

        itemLogin.setText("Log in");
        itemLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemLoginActionPerformed(evt);
            }
        });
        menuFile.add(itemLogin);

        itemDatabaseConnect.setText("Disconnect Database");
        itemDatabaseConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemDatabaseConnectActionPerformed(evt);
            }
        });
        menuFile.add(itemDatabaseConnect);

        itemUpdate.setText("Update Database");
        itemUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemUpdateActionPerformed(evt);
            }
        });
        menuFile.add(itemUpdate);

        itemInterval.setText("Update Interval");
        itemInterval.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemIntervalActionPerformed(evt);
            }
        });
        menuFile.add(itemInterval);

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

        itemPromotions.setText("Manage Promotions");
        menuStock.add(itemPromotions);

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

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 935, Short.MAX_VALUE)
            .addComponent(statusBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 459, Short.MAX_VALUE)
                .addComponent(statusBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnManageStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageStockActionPerformed
        ProductsWindow.showProductsListWindow(data);
    }//GEN-LAST:event_btnManageStockActionPerformed

    private void itemStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemStockActionPerformed
        ProductsWindow.showProductsListWindow(data);
    }//GEN-LAST:event_itemStockActionPerformed

    private void itemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_itemExitActionPerformed

    private void itemUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemUpdateActionPerformed
        TillServer.updateTask.run();
    }//GEN-LAST:event_itemUpdateActionPerformed

    private void itemCustomersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemCustomersActionPerformed
        CustomersWindow.showCustomersListWindow(data);
    }//GEN-LAST:event_itemCustomersActionPerformed

    private void btnManageCustomersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageCustomersActionPerformed
        CustomersWindow.showCustomersListWindow(data);
    }//GEN-LAST:event_btnManageCustomersActionPerformed

    private void itemDatabaseConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemDatabaseConnectActionPerformed
        if (dbConnection.isConnected()) {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to disconnect from the database?", "Disconnect Database", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                dbConnection.close();
                this.itemDatabaseConnect.setText("Connect To Database");
                this.itemUpdate.setEnabled(false);
                lblDatabase.setText("Not Connected To Database");
            }
        } else {
            if (DatabaseConnectionDialog.showConnectionDialog(this, dbConnection)) {
                JOptionPane.showMessageDialog(this, "Connected to database " + dbConnection, "Connect to database", JOptionPane.PLAIN_MESSAGE);
                this.itemDatabaseConnect.setText("Disconnect Database");
                this.itemUpdate.setEnabled(true);
                lblDatabase.setText("Connected To Database");
            }
        }
    }//GEN-LAST:event_itemDatabaseConnectActionPerformed

    private void itemLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemLoginActionPerformed
        if (staff != null) {
            logout();
        } else {
            login();
        }
    }//GEN-LAST:event_itemLoginActionPerformed

    private void itemStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemStaffActionPerformed
        StaffWindow.showStaffListWindow(data);
    }//GEN-LAST:event_itemStaffActionPerformed

    private void btnManageStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageStaffActionPerformed
        StaffWindow.showStaffListWindow(data);
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
            JOptionPane.showMessageDialog(this, dbConnection, "Database Connection", JOptionPane.PLAIN_MESSAGE);
        }
    }//GEN-LAST:event_lblDatabaseMouseClicked

    private void itemIntervalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemIntervalActionPerformed
        TillServer.updateInterval = Long.parseLong((String) JOptionPane.showInputDialog(this, "Enter value for update interval in seconds", "Database Update Interval", JOptionPane.PLAIN_MESSAGE, null, null, TillServer.updateInterval/1000)) * 1000;
        TillServer.resetUpdateTimer();
    }//GEN-LAST:event_itemIntervalActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnManageCustomers;
    private javax.swing.JButton btnManageStaff;
    private javax.swing.JButton btnManageStock;
    private javax.swing.JMenuItem itemCustomers;
    private javax.swing.JMenuItem itemDatabaseConnect;
    private javax.swing.JMenuItem itemExit;
    private javax.swing.JMenuItem itemInterval;
    private javax.swing.JMenuItem itemLogin;
    private javax.swing.JMenuItem itemPromotions;
    private javax.swing.JMenuItem itemStaff;
    private javax.swing.JMenuItem itemStock;
    private javax.swing.JMenuItem itemUpdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblClients;
    private javax.swing.JLabel lblDatabase;
    private javax.swing.JLabel lblUpdate;
    private javax.swing.JLabel lblUser;
    private javax.swing.JMenu menuCustomers;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuStaff;
    private javax.swing.JMenu menuStock;
    private javax.swing.JPanel statusBar;
    // End of variables declaration//GEN-END:variables
}
