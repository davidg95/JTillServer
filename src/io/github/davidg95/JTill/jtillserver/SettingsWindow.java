/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author David
 */
public final class SettingsWindow extends javax.swing.JInternalFrame {

    private static final Logger LOG = Logger.getGlobal();

    public static SettingsWindow frame;

    private final JTill jtill;

    private boolean editNetwork = false;

    private boolean editDatabase = false;

    /**
     * Creates new form Settings
     */
    public SettingsWindow(JTill jtill) {
        this.jtill = jtill;
        super.setIconifiable(true);
        super.setClosable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        initComponents();
    }

    public static void showSettingsWindow(JTill jtill) {
        if (frame == null || frame.isClosed()) {
            frame = new SettingsWindow(jtill);
            GUI.gui.internal.add(frame);
        }
        if (frame.isVisible()) {
            frame.toFront();
        } else {
            update();
            frame.setVisible(true);
        }
        try {
            frame.setIcon(false);
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(SettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void update() {
        frame.init();
    }

    private void init() {
        try {
            txtPort.setText(jtill.getDataConnection().getSetting("port"));
            txtMaxConn.setText(jtill.getDataConnection().getSetting("max_conn"));
            txtMaxQueued.setText(jtill.getDataConnection().getSetting("max_queue"));
            txtAddress.setText(jtill.getDataConnection().getSetting("db_address"));
            txtUsername.setText(jtill.getDataConnection().getSetting("db_username"));
            txtPassword.setText(jtill.getDataConnection().getSetting("db_password"));
            chkLogOut.setSelected(jtill.getDataConnection().getSetting("AUTO_LOGOUT").equals("TRUE"));
            spinSaleCache.setValue(Integer.parseInt(jtill.getDataConnection().getSetting("MAX_CACHE_SALES")));
            boolean useEmail = Boolean.getBoolean(jtill.getDataConnection().getSetting("USE_EMAIL", Boolean.toString(false)));
            chkEmail.setSelected(useEmail);
            if (!useEmail) {
                for (Component c : panelEmail.getComponents()) {
                    if (c == btnSave || c == chkEmail) {
                        continue;
                    }
                    c.setEnabled(false);
                }
            }
            txtOutMail.setText(jtill.getDataConnection().getSetting("mail.smtp.host"));
            txtOutgoingAddress.setText(jtill.getDataConnection().getSetting("OUTGOING_MAIL_ADDRESS"));
            txtMailAddress.setText(jtill.getDataConnection().getSetting("MAIL_ADDRESS"));
            txtSymbol.setText(jtill.getDataConnection().getSetting("CURRENCY_SYMBOL"));
            txtSiteName.setText(jtill.getDataConnection().getSetting("SITE_NAME"));
            txtReceiptHeader.setText(jtill.getDataConnection().getSetting("RECEIPT_HEADER"));
            txtReceiptFooter.setText(jtill.getDataConnection().getSetting("RECEIPT_FOOTER"));
            chkBorderScreen.setSelected(jtill.getDataConnection().getSetting("BORDER_SCREEN_BUTTON", "false").equals("true"));
            chkAddress.setSelected(jtill.getDataConnection().getSetting("SHOW_ADDRESS_RECEIPT").equals("true"));
            chkStaff.setSelected(jtill.getDataConnection().getSetting("SHOW_STAFF_RECEIPT").equals("true"));
            chkTerminal.setSelected(jtill.getDataConnection().getSetting("SHOW_TERMINAL_RECEIPT").equals("true"));
            chkEmailPrompt.setSelected(jtill.getDataConnection().getSetting("PROMPT_EMAIL_RECEIPT").equals("true"));
            chkUpdate.setSelected(jtill.getDataConnection().getSetting("UPDATE_STARTUP").equals("true"));
            txtLogoutTimeout.setValue(Integer.parseInt(jtill.getDataConnection().getSetting("LOGOUT_TIMEOUT")));
            String unlockCode = jtill.getDataConnection().getSetting("UNLOCK_CODE");
            chkShowBack.setSelected(Boolean.parseBoolean(jtill.getDataConnection().getSetting("SHOWBACKGROUND")));
            if (unlockCode.equals("OFF")) {
                chkUnlock.setSelected(false);
                txtUnlockCode.setEnabled(false);
            } else {
                chkUnlock.setSelected(true);
                txtUnlockCode.setText(unlockCode);
                txtUnlockCode.setEnabled(true);
            }
        } catch (IOException ex) {

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
        btnDatabaseDefault = new javax.swing.JButton();
        btnEditDatabase = new javax.swing.JButton();
        panelSecurity = new javax.swing.JPanel();
        chkLogOut = new javax.swing.JCheckBox();
        btnSaveSecurity = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        chkUnlock = new javax.swing.JCheckBox();
        txtLogoutTimeout = new javax.swing.JSpinner();
        txtUnlockCode = new javax.swing.JTextField();
        panelEmail = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtOutMail = new javax.swing.JTextField();
        txtMailAddress = new javax.swing.JTextField();
        btnSave = new javax.swing.JButton();
        txtOutgoingAddress = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        chkEmail = new javax.swing.JCheckBox();
        panelGeneral = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        spinSaleCache = new javax.swing.JSpinner();
        btnSaveCache = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        txtSymbol = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        txtSiteName = new javax.swing.JTextField();
        btnCompanyDetails = new javax.swing.JButton();
        chkEmailPrompt = new javax.swing.JCheckBox();
        chkUpdate = new javax.swing.JCheckBox();
        btnColor = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        btnImage = new javax.swing.JButton();
        chkShowBack = new javax.swing.JCheckBox();
        btnRemoveImage = new javax.swing.JButton();
        chkBorderScreen = new javax.swing.JCheckBox();
        btnBorderColor = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        txtReceiptHeader = new javax.swing.JTextField();
        txtReceiptFooter = new javax.swing.JTextField();
        btnReceiptSave = new javax.swing.JButton();
        chkAddress = new javax.swing.JCheckBox();
        chkStaff = new javax.swing.JCheckBox();
        chkTerminal = new javax.swing.JCheckBox();
        btnCompanyDetails2 = new javax.swing.JButton();
        btnDatabaseCancel = new javax.swing.JButton();
        btnNetworkCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setTitle("JTill Settings");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setMinimumSize(null);

        panelNetwork.setBorder(javax.swing.BorderFactory.createTitledBorder("Network Options"));
        panelNetwork.setEnabled(false);

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
                .addGroup(panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtPort)
                    .addComponent(txtMaxQueued)
                    .addComponent(txtMaxConn))
                .addContainerGap())
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

        btnDatabaseDefault.setText("Reset To Default");
        btnDatabaseDefault.setEnabled(false);
        btnDatabaseDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDatabaseDefaultActionPerformed(evt);
            }
        });

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
                    .addGroup(panelDatabaseLayout.createSequentialGroup()
                        .addComponent(btnDatabaseDefault, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
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
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDatabaseDefault))
        );

        btnEditDatabase.setText("Edit Database Options");
        btnEditDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditDatabaseActionPerformed(evt);
            }
        });

        panelSecurity.setBorder(javax.swing.BorderFactory.createTitledBorder("Security"));

        chkLogOut.setText("Log Out After Sale Is Complete");

        btnSaveSecurity.setText("Save Changes");
        btnSaveSecurity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveSecurityActionPerformed(evt);
            }
        });

        jLabel8.setText("Logout Timeout:");

        jLabel7.setText("s");

        jLabel9.setText("Till Unlock Code:");

        chkUnlock.setText("Enable");
        chkUnlock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkUnlockActionPerformed(evt);
            }
        });

        txtLogoutTimeout.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        javax.swing.GroupLayout panelSecurityLayout = new javax.swing.GroupLayout(panelSecurity);
        panelSecurity.setLayout(panelSecurityLayout);
        panelSecurityLayout.setHorizontalGroup(
            panelSecurityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSecurityLayout.createSequentialGroup()
                .addGroup(panelSecurityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelSecurityLayout.createSequentialGroup()
                        .addGap(74, 74, 74)
                        .addComponent(btnSaveSecurity))
                    .addGroup(panelSecurityLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(chkLogOut))
                    .addGroup(panelSecurityLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(panelSecurityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelSecurityLayout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtUnlockCode))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelSecurityLayout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtLogoutTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelSecurityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(chkUnlock))))
                .addContainerGap(75, Short.MAX_VALUE))
        );
        panelSecurityLayout.setVerticalGroup(
            panelSecurityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSecurityLayout.createSequentialGroup()
                .addComponent(chkLogOut)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSecurityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel7)
                    .addComponent(txtLogoutTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSecurityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(chkUnlock)
                    .addComponent(txtUnlockCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(btnSaveSecurity))
        );

        panelEmail.setBorder(javax.swing.BorderFactory.createTitledBorder("Email Settings"));

        jLabel10.setText("Outgoing mail server:");

        jLabel11.setText("Address to send reports to:");

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        jLabel12.setText("Outgoing address:");

        chkEmail.setText("Use email");
        chkEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkEmailActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelEmailLayout = new javax.swing.GroupLayout(panelEmail);
        panelEmail.setLayout(panelEmailLayout);
        panelEmailLayout.setHorizontalGroup(
            panelEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEmailLayout.createSequentialGroup()
                .addGap(102, 102, 102)
                .addComponent(btnSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(chkEmail)
                .addContainerGap())
            .addGroup(panelEmailLayout.createSequentialGroup()
                .addGroup(panelEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11)
                    .addComponent(jLabel10)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtOutMail)
                    .addComponent(txtOutgoingAddress)
                    .addComponent(txtMailAddress)))
        );
        panelEmailLayout.setVerticalGroup(
            panelEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEmailLayout.createSequentialGroup()
                .addGroup(panelEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(txtOutMail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtOutgoingAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txtMailAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(chkEmail)))
        );

        panelGeneral.setBorder(javax.swing.BorderFactory.createTitledBorder("General Settings"));

        jLabel13.setText("Max sales to cache on terminal:");

        btnSaveCache.setText("Save");
        btnSaveCache.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveCacheActionPerformed(evt);
            }
        });

        jLabel14.setText("Currency Symbol:");

        jLabel15.setText("Site Name:");

        btnCompanyDetails.setText("Company Details");
        btnCompanyDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCompanyDetailsActionPerformed(evt);
            }
        });

        chkEmailPrompt.setText("Prompt to email customer receipt");
        chkEmailPrompt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                chkEmailPromptMouseClicked(evt);
            }
        });
        chkEmailPrompt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkEmailPromptActionPerformed(evt);
            }
        });

        chkUpdate.setText("Check for updates on startup");

        btnColor.setText("Terminal Background Color");
        btnColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnColorActionPerformed(evt);
            }
        });

        jButton1.setText("Clear Color");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        btnImage.setText("Login Screen Image");
        btnImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImageActionPerformed(evt);
            }
        });

        chkShowBack.setText("Show image on server");

        btnRemoveImage.setText("Remove Image");
        btnRemoveImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveImageActionPerformed(evt);
            }
        });

        chkBorderScreen.setText("Border round selected screen button");
        chkBorderScreen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkBorderScreenActionPerformed(evt);
            }
        });

        btnBorderColor.setText("Color");
        btnBorderColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBorderColorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelGeneralLayout = new javax.swing.GroupLayout(panelGeneral);
        panelGeneral.setLayout(panelGeneralLayout);
        panelGeneralLayout.setHorizontalGroup(
            panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelGeneralLayout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSymbol, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSiteName))
                    .addGroup(panelGeneralLayout.createSequentialGroup()
                        .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnCompanyDetails)
                            .addGroup(panelGeneralLayout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinSaleCache, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelGeneralLayout.createSequentialGroup()
                                .addComponent(btnImage)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRemoveImage))
                            .addComponent(chkUpdate)
                            .addGroup(panelGeneralLayout.createSequentialGroup()
                                .addGap(118, 118, 118)
                                .addComponent(btnSaveCache))
                            .addComponent(chkEmailPrompt)
                            .addComponent(chkShowBack)
                            .addGroup(panelGeneralLayout.createSequentialGroup()
                                .addComponent(btnColor)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1))
                            .addGroup(panelGeneralLayout.createSequentialGroup()
                                .addComponent(chkBorderScreen)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnBorderColor)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelGeneralLayout.setVerticalGroup(
            panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkEmailPrompt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnColor)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkBorderScreen)
                    .addComponent(btnBorderColor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnImage)
                    .addComponent(btnRemoveImage))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkShowBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(spinSaleCache, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(txtSymbol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(txtSiteName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCompanyDetails)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkUpdate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSaveCache)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Receipt Settings")));

        jLabel16.setText("Receipt Header:");

        jLabel17.setText("Receipt Footer:");

        btnReceiptSave.setText("Save");
        btnReceiptSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReceiptSaveActionPerformed(evt);
            }
        });

        chkAddress.setText("Show address");

        chkStaff.setText("Show staff name");

        chkTerminal.setText("Show terminal");

        btnCompanyDetails2.setText("Company Details");
        btnCompanyDetails2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCompanyDetails2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnReceiptSave)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(txtReceiptHeader, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtReceiptFooter))
                                .addContainerGap())))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(chkAddress)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnCompanyDetails2))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(chkTerminal)
                                    .addComponent(chkStaff))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(txtReceiptHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(txtReceiptFooter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkAddress)
                    .addComponent(btnCompanyDetails2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkStaff)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkTerminal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnReceiptSave))
        );

        btnDatabaseCancel.setText("Cancel Changes");
        btnDatabaseCancel.setEnabled(false);
        btnDatabaseCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDatabaseCancelActionPerformed(evt);
            }
        });

        btnNetworkCancel.setText("Cancel Changes");
        btnNetworkCancel.setEnabled(false);
        btnNetworkCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNetworkCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(panelSecurity, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelEmail, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelGeneral, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelNetwork, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelDatabase, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(btnEditNetwork)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnNetworkCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(btnEditDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnDatabaseCancel)))
                    .addComponent(btnClose, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelNetwork, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnEditNetwork)
                            .addComponent(btnNetworkCancel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnEditDatabase)
                            .addComponent(btnDatabaseCancel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelSecurity, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelGeneral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnEditNetworkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditNetworkActionPerformed
        if (editNetwork) {
            String portVal = txtPort.getText();
            String maxVal = txtMaxConn.getText();
            String queueVal = txtMaxQueued.getText();
            if (portVal.length() == 0 || maxVal.length() == 0 || queueVal.length() == 0) {
                JOptionPane.showMessageDialog(this, "Must enter value for network settings", "Network Options", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Utilities.isNumber(portVal) || !Utilities.isNumber(maxVal) || !Utilities.isNumber(queueVal)) {
                JOptionPane.showMessageDialog(this, "Must enter numerical values for network settings", "Network Options", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int port = Integer.parseInt(portVal);
            int max = Integer.parseInt(maxVal);
            int queue = Integer.parseInt(queueVal);
            if (port < 0 || port > 65535) {
                JOptionPane.showMessageDialog(this, "Please enter a port value between 0 and 65535", "Server Options", JOptionPane.PLAIN_MESSAGE);
                return;
            } else if (max < 0 || queue < 0) {
                JOptionPane.showMessageDialog(this, "Please enter a value greater than 0", "Server Options", JOptionPane.PLAIN_MESSAGE);
                return;
            } else {
                try {
                    jtill.getDataConnection().setSetting("port", portVal);
                    jtill.getDataConnection().setSetting("max_conn", maxVal);
                    jtill.getDataConnection().setSetting("max_queue", queueVal);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
            for (Component c : panelNetwork.getComponents()) {
                c.setEnabled(false);
            }
            panelNetwork.setEnabled(false);
            btnNetworkCancel.setEnabled(false);
            btnEditNetwork.setText("Edit Network Options");
            editNetwork = false;
            JOptionPane.showMessageDialog(this, "Changes will take place next time server restarts", "Server Options", JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Component c : panelNetwork.getComponents()) {
                c.setEnabled(true);
            }
            panelNetwork.setEnabled(true);
            btnNetworkCancel.setEnabled(true);
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
            try {
                String address = txtAddress.getText();
                String username = txtUsername.getText();
                String password = new String(txtPassword.getPassword());
                if (address.length() == 0 || username.length() == 0 || password.length() == 0) {
                    JOptionPane.showMessageDialog(this, "Must enter values for database options", "Database Settings", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                jtill.getDataConnection().setSetting("db_address", address);
                jtill.getDataConnection().setSetting("db_username", username);
                jtill.getDataConnection().setSetting("db_password", password);
                for (Component c : panelDatabase.getComponents()) {
                    c.setEnabled(false);
                }
                panelDatabase.setEnabled(false);
                btnEditDatabase.setText("Edit Database Options");
                btnDatabaseCancel.setEnabled(false);
                editDatabase = false;
                JOptionPane.showMessageDialog(this, "Changes will take place next time server restarts", "Server Options", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        } else {
            for (Component c : panelDatabase.getComponents()) {
                c.setEnabled(true);
            }
            panelDatabase.setEnabled(true);
            btnDatabaseCancel.setEnabled(true);
            btnEditDatabase.setText("Save");
            editDatabase = true;
        }
    }//GEN-LAST:event_btnEditDatabaseActionPerformed

    private void btnSaveSecurityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveSecurityActionPerformed
        try {
            if (chkLogOut.isSelected()) {
                jtill.getDataConnection().setSetting("AUTO_LOGOUT", "TRUE");
            } else {
                jtill.getDataConnection().setSetting("AUTO_LOGOUT", "FALSE");
            }
            jtill.getDataConnection().setSetting("LOGOUT_TIMEOUT", txtLogoutTimeout.getValue().toString());
            if (chkUnlock.isSelected()) {
                if (txtUnlockCode.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Must enter a value for Unlock Code", "Security Settings", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!Utilities.isNumber(txtUnlockCode.getText())) {
                    JOptionPane.showMessageDialog(this, "Must enter a numerical value for unlock code", "Security Settings", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                jtill.getDataConnection().setSetting("UNLOCK_CODE", txtUnlockCode.getText());
            } else {
                jtill.getDataConnection().setSetting("UNLOCK_CODE", "OFF");
            }
            JOptionPane.showMessageDialog(this, "Security settings have been saved", "Security Settings", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Error saving security settings, changes have been rolled back", "Security Settings", JOptionPane.ERROR_MESSAGE);
            try {
                boolean old = jtill.getDataConnection().getSetting("AUTO_LOGOUT").equals("TRUE");
                chkLogOut.setSelected(old);
            } catch (IOException ex1) {
                LOG.log(Level.SEVERE, null, ex1);
            }
        }
    }//GEN-LAST:event_btnSaveSecurityActionPerformed

    private void btnDatabaseDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDatabaseDefaultActionPerformed
        try {
            txtAddress.setText(jtill.getDataConnection().getSetting("db_address"));
            txtUsername.setText(jtill.getDataConnection().getSetting("db_username"));
            txtPassword.setText(jtill.getDataConnection().getSetting("db_password"));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnDatabaseDefaultActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        try {
            jtill.getDataConnection().setSetting("USE_EMAIL", Boolean.toString(chkEmail.isSelected()));
            if (chkEmail.isSelected()) {
                if (txtOutMail.getText().isEmpty() || txtOutgoingAddress.getText().isEmpty() || txtMailAddress.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Must fill out all fields", "Email settings", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            jtill.getDataConnection().setSetting("mail.smtp.host", txtOutMail.getText());
            jtill.getDataConnection().setSetting("OUTGOING_MAIL_ADDRESS", txtOutgoingAddress.getText());
            jtill.getDataConnection().setSetting("MAIL_ADDRESS", txtMailAddress.getText());
            JOptionPane.showMessageDialog(this, "Email settings saved", "Email Settings", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving settings", "Email Settings", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnSaveCacheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveCacheActionPerformed
        boolean error = false;
        try {
            jtill.getDataConnection().setSetting("MAX_CACHE_SALES", "" + (int) spinSaleCache.getValue());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            error = true;
        }
        try {
            if (txtSymbol.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No currency symbol set, will use last set symbol", "Settings", JOptionPane.WARNING_MESSAGE);
            } else {
                jtill.getDataConnection().setSetting("CURRENCY_SYMBOL", txtSymbol.getText());
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            error = true;
        }
        try {
            jtill.getDataConnection().setSetting("SITE_NAME", txtSiteName.getText());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            error = true;
        }
        try {
            jtill.getDataConnection().setSetting("PROMPT_EMAIL_RECEIPT", Boolean.toString(chkEmailPrompt.isSelected()));
        } catch (IOException ex) {
            error = true;
        }
        try {
            jtill.getDataConnection().setSetting("UPDATE_STARTUP", Boolean.toString(chkUpdate.isSelected()));
        } catch (IOException ex) {
            error = true;
        }
        try {
            jtill.getDataConnection().setSetting("SHOWBACKGROUND", Boolean.toString(chkShowBack.isSelected()));
        } catch (IOException ex) {
            error = true;
        }
        if (error) {
            JOptionPane.showMessageDialog(this, "There was an error saving some settings", "General Settings", JOptionPane.WARNING_MESSAGE);
        }

        JOptionPane.showMessageDialog(this, "General settings saved", "General Settings", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_btnSaveCacheActionPerformed

    private void btnReceiptSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReceiptSaveActionPerformed
        try {
            jtill.getDataConnection().setSetting("RECEIPT_HEADER", txtReceiptHeader.getText());
            jtill.getDataConnection().setSetting("RECEIPT_FOOTER", txtReceiptFooter.getText());
            jtill.getDataConnection().setSetting("SHOW_ADDRESS_RECEIPT", Boolean.toString(chkAddress.isSelected()));
            jtill.getDataConnection().setSetting("SHOW_STAFF_RECEIPT", Boolean.toString(chkStaff.isSelected()));
            jtill.getDataConnection().setSetting("SHOW_TERMINAL_RECEIPT", Boolean.toString(chkStaff.isSelected()));
            JOptionPane.showMessageDialog(this, "Receipt settings saved", "Receipt Settings", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            Logger.getLogger(SettingsWindow.class
                    .getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Error saving settings", "Receipt Settings", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnReceiptSaveActionPerformed

    private void btnCompanyDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCompanyDetailsActionPerformed
        CompanyDetailsDialog.showDialog(this);
    }//GEN-LAST:event_btnCompanyDetailsActionPerformed

    private void btnCompanyDetails2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCompanyDetails2ActionPerformed
        CompanyDetailsDialog.showDialog(this);
    }//GEN-LAST:event_btnCompanyDetails2ActionPerformed

    private void chkEmailPromptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkEmailPromptActionPerformed
        if (chkEmailPrompt.isSelected()) {
            try {
                String i = jtill.getDataConnection().getSetting("mail.smtp.host");
                String j = jtill.getDataConnection().getSetting("OUTGOING_MAIL_ADDRESS");
                String k = jtill.getDataConnection().getSetting("MAIL_ADDRESS");

                if ("".equals(i) || "".equals(j) || "".equals(k)) {
                    JOptionPane.showMessageDialog(this, "You have not specified any mail server settings, this must be done before you can send emails", "Email", JOptionPane.ERROR_MESSAGE);
                    chkEmailPrompt.setSelected(false);

                }
            } catch (IOException ex) {
                Logger.getLogger(SettingsWindow.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_chkEmailPromptActionPerformed

    private void chkEmailPromptMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chkEmailPromptMouseClicked
        if (chkEmailPrompt.isSelected()) {
            try {
                String i = jtill.getDataConnection().getSetting("mail.smtp.host");
                String j = jtill.getDataConnection().getSetting("OUTGOING_MAIL_ADDRESS");
                String k = jtill.getDataConnection().getSetting("MAIL_ADDRESS");

                if ("".equals(i) || "".equals(j) || "".equals(k)) {
                    JOptionPane.showMessageDialog(this, "You have not specified any mail server settings, this must be done before you can send emails", "Email", JOptionPane.ERROR_MESSAGE);
                    chkEmailPrompt.setSelected(false);

                }
            } catch (IOException ex) {
                Logger.getLogger(SettingsWindow.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_chkEmailPromptMouseClicked

    private void btnNetworkCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNetworkCancelActionPerformed
        try {
            txtPort.setText(jtill.getDataConnection().getSetting("port"));
            txtMaxConn.setText(jtill.getDataConnection().getSetting("max_conn"));
            txtMaxQueued.setText(jtill.getDataConnection().getSetting("max_queue"));
            for (Component c : panelNetwork.getComponents()) {
                c.setEnabled(false);
            }
            panelNetwork.setEnabled(false);
            btnNetworkCancel.setEnabled(false);
            btnEditNetwork.setText("Edit Network Options");
            editNetwork = false;

        } catch (IOException ex) {
            Logger.getLogger(SettingsWindow.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnNetworkCancelActionPerformed

    private void btnDatabaseCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDatabaseCancelActionPerformed
        try {
            txtAddress.setText(jtill.getDataConnection().getSetting("db_address"));
            txtUsername.setText(jtill.getDataConnection().getSetting("db_username"));
            txtPassword.setText(jtill.getDataConnection().getSetting("db_password"));
            for (Component c : panelDatabase.getComponents()) {
                c.setEnabled(false);
            }
            panelDatabase.setEnabled(false);
            btnEditDatabase.setText("Edit Database Options");
            btnDatabaseCancel.setEnabled(false);
            editDatabase = false;

        } catch (IOException ex) {
            Logger.getLogger(SettingsWindow.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnDatabaseCancelActionPerformed

    private void btnColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnColorActionPerformed
        Color c = JColorChooser.showDialog(this, "Terminal Screen Background Color", null);
        try {
            jtill.getDataConnection().setSetting("TERMINAL_BG", Integer.toString(c.getRGB()));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnColorActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            jtill.getDataConnection().setSetting("TERMINAL_BG", Integer.toString(0));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImageActionPerformed
        JFileChooser chooser = new JFileChooser();
        int option = chooser.showDialog(this, "Select Image");
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                jtill.getDataConnection().setSetting("bg_url", file.getAbsolutePath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "File is not an image", "Background Image", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnImageActionPerformed

    private void chkUnlockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkUnlockActionPerformed
        txtUnlockCode.setEnabled(chkUnlock.isSelected());
    }//GEN-LAST:event_chkUnlockActionPerformed

    private void btnRemoveImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveImageActionPerformed
        try {
            jtill.getDataConnection().setSetting("bg_url", "NONE");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnRemoveImageActionPerformed

    private void chkEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkEmailActionPerformed
        for (Component c : panelEmail.getComponents()) {
            if (c == btnSave || c == chkEmail) {
                continue;
            }
            c.setEnabled(chkEmail.isSelected());
        }
    }//GEN-LAST:event_chkEmailActionPerformed

    private void chkBorderScreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkBorderScreenActionPerformed
        btnBorderColor.setEnabled(chkBorderScreen.isSelected());
        try {
            jtill.getDataConnection().setSetting("BORDER_SCREEN_BUTTON", Boolean.toString(chkBorderScreen.isSelected()));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_chkBorderScreenActionPerformed

    private void btnBorderColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBorderColorActionPerformed
        Color c = JColorChooser.showDialog(this, "Screen Button Border Color", null);
        try {
            jtill.getDataConnection().setSetting("BORDER_COLOR", TillButton.rbg2Hex(c));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnBorderColorActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBorderColor;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnColor;
    private javax.swing.JButton btnCompanyDetails;
    private javax.swing.JButton btnCompanyDetails2;
    private javax.swing.JButton btnDatabaseCancel;
    private javax.swing.JButton btnDatabaseDefault;
    private javax.swing.JButton btnEditDatabase;
    private javax.swing.JButton btnEditNetwork;
    private javax.swing.JButton btnImage;
    private javax.swing.JButton btnNetworkCancel;
    private javax.swing.JButton btnReceiptSave;
    private javax.swing.JButton btnRemoveImage;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSaveCache;
    private javax.swing.JButton btnSaveSecurity;
    private javax.swing.JCheckBox chkAddress;
    private javax.swing.JCheckBox chkBorderScreen;
    private javax.swing.JCheckBox chkEmail;
    private javax.swing.JCheckBox chkEmailPrompt;
    private javax.swing.JCheckBox chkLogOut;
    private javax.swing.JCheckBox chkShowBack;
    private javax.swing.JCheckBox chkStaff;
    private javax.swing.JCheckBox chkTerminal;
    private javax.swing.JCheckBox chkUnlock;
    private javax.swing.JCheckBox chkUpdate;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel panelDatabase;
    private javax.swing.JPanel panelEmail;
    private javax.swing.JPanel panelGeneral;
    private javax.swing.JPanel panelNetwork;
    private javax.swing.JPanel panelSecurity;
    private javax.swing.JSpinner spinSaleCache;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JSpinner txtLogoutTimeout;
    private javax.swing.JTextField txtMailAddress;
    private javax.swing.JTextField txtMaxConn;
    private javax.swing.JTextField txtMaxQueued;
    private javax.swing.JTextField txtOutMail;
    private javax.swing.JTextField txtOutgoingAddress;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtPort;
    private javax.swing.JTextField txtReceiptFooter;
    private javax.swing.JTextField txtReceiptHeader;
    private javax.swing.JTextField txtSiteName;
    private javax.swing.JTextField txtSymbol;
    private javax.swing.JTextField txtUnlockCode;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
