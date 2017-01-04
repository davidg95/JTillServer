/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.DBConnect;
import java.awt.Component;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author David
 */
public class SettingsWindow extends javax.swing.JFrame {

    public static final SettingsWindow frame;
    private static Properties properties;

    //Static Settings
    public static int PORT = 600;
    public static int MAX_CONNECTIONS = 10;
    public static int MAX_QUEUE = 10;
    public static String hostName;
    public static boolean autoLogout = false;
    public static int logoutTimeout = 30;
    public static String DB_ADDRESS = "jdbc:derby:TillEmbedded;";
    public static String DB_USERNAME = "APP";
    public static String DB_PASSWORD = "App";
    private final String defaultAddress = "jdbc:derby:TillEmbedded;";
    private final String defaultUsername = "APP";
    private final String defaultPassword = "App";

    private final DBConnect dbConn;

    private boolean editNetwork = false;

    private boolean editDatabase = false;

    /**
     * Creates new form Settings
     */
    public SettingsWindow() {
        this.dbConn = TillServer.getDBConnection();
        initComponents();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    static {
        frame = new SettingsWindow();
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    public static void showSettingsWindow() {
        update();
        frame.setVisible(true);
    }

    public static void loadProperties() {
        properties = new Properties();
        InputStream in;

        try {
            in = new FileInputStream("server.properties");

            properties.load(in);

            hostName = properties.getProperty("host");
            PORT = Integer.parseInt(properties.getProperty("port", Integer.toString(PORT)));
            MAX_CONNECTIONS = Integer.parseInt(properties.getProperty("max_conn", Integer.toString(MAX_CONNECTIONS)));
            MAX_QUEUE = Integer.parseInt(properties.getProperty("max_queue", Integer.toString(MAX_QUEUE)));
            autoLogout = Boolean.parseBoolean(properties.getProperty("autoLogout", "false"));
            logoutTimeout = Integer.parseInt(properties.getProperty("logoutTimeout", "30"));
            DB_ADDRESS = properties.getProperty("db_address", "jdbc:derby:TillEmbedded;");
            DB_USERNAME = properties.getProperty("db_username", "APP");
            DB_PASSWORD = properties.getProperty("db_password", "App");

            frame.init();

            TillSplashScreen.addBar(10);

            in.close();
        } catch (FileNotFoundException | UnknownHostException ex) {
            saveProperties();
        } catch (IOException ex) {
        }
    }

    public static void saveProperties() {
        properties = new Properties();
        OutputStream out;

        try {
            out = new FileOutputStream("server.properties");

            hostName = InetAddress.getLocalHost().getHostName();

            properties.setProperty("host", hostName);
            properties.setProperty("port", Integer.toString(PORT));
            properties.setProperty("max_conn", Integer.toString(MAX_CONNECTIONS));
            properties.setProperty("max_queue", Integer.toString(MAX_QUEUE));
            properties.setProperty("autoLogout", Boolean.toString(autoLogout));
            properties.setProperty("logoutTimeout", Integer.toString(logoutTimeout));
            properties.setProperty("db_address", DB_ADDRESS);
            properties.setProperty("db_username", DB_USERNAME);
            properties.setProperty("db_password", DB_PASSWORD);

            properties.store(out, null);
            out.close();
        } catch (FileNotFoundException | UnknownHostException ex) {
        } catch (IOException ex) {
        }
    }

    public static void update() {
        frame.init();
    }

    private void init() {
        txtPort.setText(PORT + "");
        txtMaxConn.setText(MAX_CONNECTIONS + "");
        txtMaxQueued.setText(MAX_QUEUE + "");
        txtAddress.setText(DB_ADDRESS);
        txtUsername.setText(DB_USERNAME);
        txtPassword.setText(DB_PASSWORD);
        chkLogOut.setSelected(autoLogout);
        if (logoutTimeout == -1) {
            chkLogoutTimeout.setSelected(false);
            txtLogoutTimeout.setText("");
            txtLogoutTimeout.setEnabled(false);
        } else {
            chkLogoutTimeout.setSelected(true);
            txtLogoutTimeout.setText(logoutTimeout + "");
            txtLogoutTimeout.setEnabled(true);
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

        panelNetwork = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtPort = new javax.swing.JTextField();
        txtMaxConn = new javax.swing.JTextField();
        txtMaxQueued = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        btnEditNetwork = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        panelDatabase = new javax.swing.JPanel();
        txtAddress = new javax.swing.JTextField();
        txtUsername = new javax.swing.JTextField();
        txtPassword = new javax.swing.JPasswordField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        btnEditDatabase = new javax.swing.JButton();
        panelSecurity = new javax.swing.JPanel();
        txtLogoutTimeout = new javax.swing.JTextField();
        chkLogOut = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        chkLogoutTimeout = new javax.swing.JCheckBox();
        btnSaveSecurity = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        btnDatabaseDefault = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JTill Settings");
        setIconImage(TillServer.getIcon());

        panelNetwork.setBorder(javax.swing.BorderFactory.createTitledBorder("Network Options"));

        jLabel1.setText("Port Number:");
        jLabel1.setEnabled(false);

        txtPort.setEnabled(false);

        txtMaxConn.setEnabled(false);

        txtMaxQueued.setEnabled(false);

        jLabel2.setText("Maximum Connections:");
        jLabel2.setEnabled(false);

        jLabel3.setText("Maximum Queued Connections:");
        jLabel3.setEnabled(false);

        javax.swing.GroupLayout panelNetworkLayout = new javax.swing.GroupLayout(panelNetwork);
        panelNetwork.setLayout(panelNetworkLayout);
        panelNetworkLayout.setHorizontalGroup(
            panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNetworkLayout.createSequentialGroup()
                .addGroup(panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtMaxQueued)
                    .addComponent(txtMaxConn)
                    .addComponent(txtPort, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        panelNetworkLayout.setVerticalGroup(
            panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNetworkLayout.createSequentialGroup()
                .addGroup(panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMaxConn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMaxQueued, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)))
        );

        btnEditNetwork.setText("Edit Network Options");
        btnEditNetwork.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditNetworkActionPerformed(evt);
            }
        });

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        panelDatabase.setBorder(javax.swing.BorderFactory.createTitledBorder("Database Options"));
        panelDatabase.setEnabled(false);

        txtAddress.setEnabled(false);

        txtUsername.setEnabled(false);

        txtPassword.setEnabled(false);
        txtPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPasswordActionPerformed(evt);
            }
        });

        jLabel4.setText("Address:");
        jLabel4.setEnabled(false);

        jLabel5.setText("Username:");
        jLabel5.setEnabled(false);

        jLabel6.setText("Password:");
        jLabel6.setEnabled(false);

        javax.swing.GroupLayout panelDatabaseLayout = new javax.swing.GroupLayout(panelDatabase);
        panelDatabase.setLayout(panelDatabaseLayout);
        panelDatabaseLayout.setHorizontalGroup(
            panelDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDatabaseLayout.createSequentialGroup()
                .addGroup(panelDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtAddress)
                    .addComponent(txtUsername)
                    .addComponent(txtPassword)))
        );
        panelDatabaseLayout.setVerticalGroup(
            panelDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDatabaseLayout.createSequentialGroup()
                .addGroup(panelDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)))
        );

        btnEditDatabase.setText("Edit Database Options");
        btnEditDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditDatabaseActionPerformed(evt);
            }
        });

        panelSecurity.setBorder(javax.swing.BorderFactory.createTitledBorder("Security"));

        txtLogoutTimeout.setText("30");

        chkLogOut.setText("Log Out After Sale Is Complete");

        jLabel8.setText("seconds");

        chkLogoutTimeout.setSelected(true);
        chkLogoutTimeout.setText("Enabled");
        chkLogoutTimeout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkLogoutTimeoutActionPerformed(evt);
            }
        });

        btnSaveSecurity.setText("Save Changes");
        btnSaveSecurity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveSecurityActionPerformed(evt);
            }
        });

        jLabel7.setText("Automatic Logout Timeout:");

        javax.swing.GroupLayout panelSecurityLayout = new javax.swing.GroupLayout(panelSecurity);
        panelSecurity.setLayout(panelSecurityLayout);
        panelSecurityLayout.setHorizontalGroup(
            panelSecurityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chkLogOut)
            .addGroup(panelSecurityLayout.createSequentialGroup()
                .addGroup(panelSecurityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnSaveSecurity)
                    .addGroup(panelSecurityLayout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtLogoutTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkLogoutTimeout))
        );
        panelSecurityLayout.setVerticalGroup(
            panelSecurityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSecurityLayout.createSequentialGroup()
                .addComponent(chkLogOut)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSecurityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtLogoutTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(chkLogoutTimeout))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSaveSecurity))
        );

        btnDatabaseDefault.setText("Reset To Default");
        btnDatabaseDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDatabaseDefaultActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnClose))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelSecurity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnEditDatabase)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnDatabaseDefault, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(panelNetwork, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnEditNetwork)
                            .addComponent(panelDatabase, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 384, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelNetwork, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelSecurity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditNetwork)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEditDatabase)
                    .addComponent(btnDatabaseDefault))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 246, Short.MAX_VALUE)
                .addComponent(btnClose)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnEditNetworkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditNetworkActionPerformed
        if (editNetwork) {
            int port = Integer.parseInt(txtPort.getText());
            int max = Integer.parseInt(txtMaxConn.getText());
            int queue = Integer.parseInt(txtMaxQueued.getText());
            if (port < 0 || port > 65535) {
                JOptionPane.showMessageDialog(this, "Please enter a port value between 0 and 65535", "Server Options", JOptionPane.PLAIN_MESSAGE);
            } else if (max < 0 || queue < 0) {
                JOptionPane.showMessageDialog(this, "Please enter a value greater than 0", "Server Options", JOptionPane.PLAIN_MESSAGE);
            } else {
                SettingsWindow.PORT = port;
                SettingsWindow.MAX_CONNECTIONS = max;
                SettingsWindow.MAX_QUEUE = queue;
                SettingsWindow.saveProperties();
            }
            for (Component c : panelNetwork.getComponents()) {
                c.setEnabled(false);
            }
            btnEditNetwork.setText("Edit Network Options");
            editNetwork = false;
            JOptionPane.showMessageDialog(this, "Changes will take place next time server restarts", "Server Options", JOptionPane.PLAIN_MESSAGE);
        } else {
            for (Component c : panelNetwork.getComponents()) {
                c.setEnabled(true);
            }
            btnEditNetwork.setText("Save");
            editNetwork = true;
        }
    }//GEN-LAST:event_btnEditNetworkActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void txtPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPasswordActionPerformed

    }//GEN-LAST:event_txtPasswordActionPerformed

    private void btnEditDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditDatabaseActionPerformed
        if (editDatabase) {
            String address = txtAddress.getText();
            String username = txtUsername.getText();
            String password = new String(txtPassword.getPassword());
            DB_ADDRESS = address;
            DB_USERNAME = username;
            DB_PASSWORD = password;
            SettingsWindow.saveProperties();
            for (Component c : panelDatabase.getComponents()) {
                c.setEnabled(false);
            }
            btnEditDatabase.setText("Edit Database Options");
            editDatabase = false;
            JOptionPane.showMessageDialog(this, "Changes will take place next time server restarts", "Server Options", JOptionPane.PLAIN_MESSAGE);
        } else {
            for (Component c : panelDatabase.getComponents()) {
                c.setEnabled(true);
            }
            btnEditDatabase.setText("Save");
            editDatabase = true;
        }
    }//GEN-LAST:event_btnEditDatabaseActionPerformed

    private void btnSaveSecurityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveSecurityActionPerformed
        autoLogout = chkLogOut.isSelected();
        if (chkLogoutTimeout.isSelected()) {
            logoutTimeout = Integer.parseInt(txtLogoutTimeout.getText());
        } else {
            logoutTimeout = -1;
        }
        saveProperties();
    }//GEN-LAST:event_btnSaveSecurityActionPerformed

    private void chkLogoutTimeoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkLogoutTimeoutActionPerformed
        txtLogoutTimeout.setEnabled(chkLogoutTimeout.isSelected());
    }//GEN-LAST:event_chkLogoutTimeoutActionPerformed

    private void btnDatabaseDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDatabaseDefaultActionPerformed
        DB_ADDRESS = defaultAddress;
        DB_USERNAME = defaultUsername;
        DB_PASSWORD = defaultPassword;
        txtAddress.setText(DB_ADDRESS);
        txtUsername.setText(DB_USERNAME);
        txtPassword.setText(DB_PASSWORD);
        saveProperties();
        JOptionPane.showMessageDialog(this, "Changes will take place next time server restarts", "Server Options", JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_btnDatabaseDefaultActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnDatabaseDefault;
    private javax.swing.JButton btnEditDatabase;
    private javax.swing.JButton btnEditNetwork;
    private javax.swing.JButton btnSaveSecurity;
    private javax.swing.JCheckBox chkLogOut;
    private javax.swing.JCheckBox chkLogoutTimeout;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel panelDatabase;
    private javax.swing.JPanel panelNetwork;
    private javax.swing.JPanel panelSecurity;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtLogoutTimeout;
    private javax.swing.JTextField txtMaxConn;
    private javax.swing.JTextField txtMaxQueued;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtPort;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
