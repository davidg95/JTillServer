/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.ImageView;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListDataListener;

/**
 *
 * @author David
 */
public final class SettingsWindow extends javax.swing.JInternalFrame {

    private static final Logger LOG = Logger.getGlobal();

    public static SettingsWindow frame;

    private final DataConnect dc;

    private boolean editNetwork = false;

    private boolean editDatabase = false;

    private final MyModel model;

    /**
     * Creates new form Settings
     *
     * @param dc
     * @param icon
     */
    public SettingsWindow(DataConnect dc, Image icon) {
        this.dc = dc;
//        this.setIconImage(icon);
        super.setIconifiable(true);
        super.setClosable(true);
        super.setFrameIcon(new ImageIcon(icon));
        initComponents();
        model = new MyModel();
        model.setSelectedItem(0);
        cmbLaf.setModel(model);
        model.setSelectedItem(UIManager.getLookAndFeel());
//        setLocationRelativeTo(null);
    }

    private class MyModel implements ComboBoxModel {

        private final LookAndFeelInfo[] info;

        private LookAndFeelInfo selected;

        private final LinkedList<ListDataListener> listeners;

        public MyModel() {
            info = UIManager.getInstalledLookAndFeels();
            listeners = new LinkedList<>();
        }

        @Override
        public void setSelectedItem(Object anItem) {
            for (LookAndFeelInfo lafi : info) {
                if (lafi.getName().equals(anItem)) {
                    selected = lafi;
                }
            }
        }

        public void setSelectedItem(int index) {
            selected = info[index];
        }

        public void setSelectedItem(String name) {
            for (LookAndFeelInfo lafi : info) {
                if (lafi.getName().equals(name)) {
                    selected = lafi;
                }
            }
        }

        @Override
        public Object getSelectedItem() {
            return selected.getName();
        }

        @Override
        public int getSize() {
            return info.length;
        }

        @Override
        public Object getElementAt(int index) {
            return info[index].getName();
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            listeners.add(l);
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            listeners.remove(l);
        }

    }

    public static void showSettingsWindow(DataConnect dc, Image icon) {
        if (frame == null) {
            frame = new SettingsWindow(dc, icon);
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
            txtPort.setText(dc.getSetting("port"));
            txtMaxConn.setText(dc.getSetting("max_conn"));
            txtMaxQueued.setText(dc.getSetting("max_queue"));
            txtAddress.setText(dc.getSetting("db_address"));
            txtUsername.setText(dc.getSetting("db_username"));
            txtPassword.setText(dc.getSetting("db_password"));
            chkLogOut.setSelected(dc.getSetting("AUTO_LOGOUT").equals("TRUE"));
            spinSaleCache.setValue(Integer.parseInt(dc.getSetting("MAX_CACHE_SALES")));
            if (dc.getSetting("SEND_PRODUCTS_START").equals("TRUE")) {
                chkSendProducts.setSelected(true);
            } else {
                chkSendProducts.setSelected(false);
            }
            txtOutMail.setText(dc.getSetting("mail.smtp.host"));
            txtOutgoingAddress.setText(dc.getSetting("OUTGOING_MAIL_ADDRESS"));
            txtMailAddress.setText(dc.getSetting("MAIL_ADDRESS"));
            txtSymbol.setText(dc.getSetting("CURRENCY_SYMBOL"));
            txtSiteName.setText(dc.getSetting("SITE_NAME"));
            txtReceiptHeader.setText(dc.getSetting("RECEIPT_HEADER"));
            txtReceiptFooter.setText(dc.getSetting("RECEIPT_FOOTER"));
            chkApproveNew.setSelected(dc.getSetting("APPROVE_NEW_CONNECTIONS").equals("TRUE"));
            chkAddress.setSelected(dc.getSetting("SHOW_ADDRESS_RECEIPT").equals("true"));
            chkStaff.setSelected(dc.getSetting("SHOW_STAFF_RECEIPT").equals("true"));
            chkTerminal.setSelected(dc.getSetting("SHOW_TERMINAL_RECEIPT").equals("true"));
            chkEmailPrompt.setSelected(dc.getSetting("PROMPT_EMAIL_RECEIPT", "false").equals("true"));
            chkUpdate.setSelected(dc.getSetting("UPDATE_STARTUP", "true").equals("true"));
            chkCode.setSelected(dc.getSetting("LOGINTYPE", "CODE").equals("CODE"));
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
        chkCode = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtOutMail = new javax.swing.JTextField();
        txtMailAddress = new javax.swing.JTextField();
        btnSave = new javax.swing.JButton();
        txtOutgoingAddress = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        btnPermissions = new javax.swing.JButton();
        panelGeneral = new javax.swing.JPanel();
        chkSendProducts = new javax.swing.JCheckBox();
        jLabel13 = new javax.swing.JLabel();
        spinSaleCache = new javax.swing.JSpinner();
        btnSaveCache = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        txtSymbol = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        txtSiteName = new javax.swing.JTextField();
        btnCompanyDetails = new javax.swing.JButton();
        chkApproveNew = new javax.swing.JCheckBox();
        chkEmailPrompt = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        cmbLaf = new javax.swing.JComboBox<>();
        btnSetLAF = new javax.swing.JButton();
        chkUpdate = new javax.swing.JCheckBox();
        btnColor = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        btnImage = new javax.swing.JButton();
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

        chkCode.setText("Use code for login");

        javax.swing.GroupLayout panelSecurityLayout = new javax.swing.GroupLayout(panelSecurity);
        panelSecurity.setLayout(panelSecurityLayout);
        panelSecurityLayout.setHorizontalGroup(
            panelSecurityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSecurityLayout.createSequentialGroup()
                .addGroup(panelSecurityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelSecurityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(btnSaveSecurity)
                        .addComponent(chkLogOut))
                    .addComponent(chkCode))
                .addContainerGap(146, Short.MAX_VALUE))
        );
        panelSecurityLayout.setVerticalGroup(
            panelSecurityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSecurityLayout.createSequentialGroup()
                .addComponent(chkLogOut)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkCode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSaveSecurity))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Email Settings"));

        jLabel10.setText("Outgoing mail server:");

        jLabel11.setText("Address to send reports to:");

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        jLabel12.setText("Outgoing address:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(102, 102, 102)
                .addComponent(btnSave)
                .addContainerGap(162, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11)
                    .addComponent(jLabel10)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtOutMail)
                    .addComponent(txtOutgoingAddress)
                    .addComponent(txtMailAddress)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(txtOutMail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtOutgoingAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txtMailAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSave))
        );

        btnPermissions.setText("Edit Setting Values");
        btnPermissions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPermissionsActionPerformed(evt);
            }
        });

        panelGeneral.setBorder(javax.swing.BorderFactory.createTitledBorder("General Settings"));

        chkSendProducts.setText("Send all products to terminal on startup");

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

        chkApproveNew.setText("New connections require approval");

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

        jLabel7.setText("Look and Feel:");

        cmbLaf.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnSetLAF.setText("Set");
        btnSetLAF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetLAFActionPerformed(evt);
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

        javax.swing.GroupLayout panelGeneralLayout = new javax.swing.GroupLayout(panelGeneral);
        panelGeneral.setLayout(panelGeneralLayout);
        panelGeneralLayout.setHorizontalGroup(
            panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGeneralLayout.createSequentialGroup()
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                                    .addComponent(chkEmailPrompt)
                                    .addComponent(chkSendProducts)
                                    .addGroup(panelGeneralLayout.createSequentialGroup()
                                        .addComponent(btnColor)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton1))
                                    .addComponent(chkUpdate)
                                    .addComponent(chkApproveNew)
                                    .addComponent(btnCompanyDetails)
                                    .addGroup(panelGeneralLayout.createSequentialGroup()
                                        .addComponent(jLabel7)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cmbLaf, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnSetLAF))
                                    .addGroup(panelGeneralLayout.createSequentialGroup()
                                        .addComponent(jLabel13)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(spinSaleCache, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 27, Short.MAX_VALUE))))
                    .addGroup(panelGeneralLayout.createSequentialGroup()
                        .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelGeneralLayout.createSequentialGroup()
                                .addGap(128, 128, 128)
                                .addComponent(btnSaveCache))
                            .addGroup(panelGeneralLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(btnImage)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelGeneralLayout.setVerticalGroup(
            panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkApproveNew)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkSendProducts)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkEmailPrompt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnColor)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnImage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(cmbLaf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSetLAF))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkUpdate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelGeneral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelSecurity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnPermissions)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose)))
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
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnClose)
                            .addComponent(btnPermissions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(34, 34, 34))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelSecurity, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelGeneral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
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
                    dc.setSetting("port", portVal);
                    dc.setSetting("max_conn", maxVal);
                    dc.setSetting("max_queue", queueVal);
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
                dc.setSetting("db_address", address);
                dc.setSetting("db_username", username);
                dc.setSetting("db_password", password);
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
                dc.setSetting("AUTO_LOGOUT", "TRUE");
            } else {
                dc.setSetting("AUTO_LOGOUT", "FALSE");
            }
            if (chkCode.isSelected()) {
                dc.setSetting("LOGINTYPE", "CODE");
            } else {
                dc.setSetting("LOGINTYPE", "BUTTONS");
            }
            JOptionPane.showMessageDialog(this, "Security settings have been saved", "Security Settings", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Error saving security settings, changes have been rolled back", "Security Settings", JOptionPane.ERROR_MESSAGE);
            try {
                boolean old = dc.getSetting("AUTO_LOGOUT").equals("TRUE");
                chkLogOut.setSelected(old);
            } catch (IOException ex1) {
                LOG.log(Level.SEVERE, null, ex1);
            }
        }
    }//GEN-LAST:event_btnSaveSecurityActionPerformed

    private void btnDatabaseDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDatabaseDefaultActionPerformed
        try {
            txtAddress.setText(dc.getSetting("db_address"));
            txtUsername.setText(dc.getSetting("db_username"));
            txtPassword.setText(dc.getSetting("db_password"));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnDatabaseDefaultActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        try {
            dc.setSetting("mail.smtp.host", txtOutMail.getText());
            dc.setSetting("OUTGOING_MAIL_ADDRESS", txtOutgoingAddress.getText());
            dc.setSetting("MAIL_ADDRESS", txtMailAddress.getText());
            JOptionPane.showMessageDialog(this, "Email settings saved", "Email Settings", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving settings", "Email Settings", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnPermissionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPermissionsActionPerformed
        try {
            if (GUI.staff.getPosition() < Integer.parseInt(dc.getSetting("SETTINGS_EDIT"))) {
                JOptionPane.showInternalMessageDialog(GUI.gui.internal, "You do not have authority to use this screen", "Settings", JOptionPane.ERROR_MESSAGE);
                return;
            }
            PermissionsWindow.showDialog(dc);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnPermissionsActionPerformed

    private void btnSaveCacheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveCacheActionPerformed
        boolean error = false;
        if (chkSendProducts.isSelected()) {
            try {
                dc.setSetting("SEND_PRODUCTS_START", "TRUE");
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
                error = true;
            }
        } else {
            try {
                dc.setSetting("SEND_PRODUCTS_START", "FALSE");
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
                error = true;
            }
        }
        try {
            dc.setSetting("MAX_CACHE_SALES", "" + (int) spinSaleCache.getValue());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            error = true;
        }
        try {
            if (txtSymbol.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No currency symbol set, will use last set symbol", "Settings", JOptionPane.WARNING_MESSAGE);
            } else {
                dc.setSetting("CURRENCY_SYMBOL", txtSymbol.getText());
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            error = true;
        }
        try {
            dc.setSetting("SITE_NAME", txtSiteName.getText());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            error = true;
        }
        try {
            if (chkApproveNew.isSelected()) {
                dc.setSetting("APPROVE_NEW_CONNECTIONS", "TRUE");
            } else {
                dc.setSetting("APPROVE_NEW_CONNECTIONS", "FALSE");
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            error = true;
        }
        try {
            dc.setSetting("PROMPT_EMAIL_RECEIPT", Boolean.toString(chkEmailPrompt.isSelected()));
        } catch (IOException ex) {
            error = true;
        }
        try {
            dc.setSetting("UPDATE_STARTUP", Boolean.toString(chkUpdate.isSelected()));
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
            dc.setSetting("RECEIPT_HEADER", txtReceiptHeader.getText());
            dc.setSetting("RECEIPT_FOOTER", txtReceiptFooter.getText());
            dc.setSetting("SHOW_ADDRESS_RECEIPT", Boolean.toString(chkAddress.isSelected()));
            dc.setSetting("SHOW_STAFF_RECEIPT", Boolean.toString(chkStaff.isSelected()));
            dc.setSetting("SHOW_TERMINAL_RECEIPT", Boolean.toString(chkStaff.isSelected()));
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
                String i = dc.getSetting("mail.smtp.host");
                String j = dc.getSetting("OUTGOING_MAIL_ADDRESS");
                String k = dc.getSetting("MAIL_ADDRESS");

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
                String i = dc.getSetting("mail.smtp.host");
                String j = dc.getSetting("OUTGOING_MAIL_ADDRESS");
                String k = dc.getSetting("MAIL_ADDRESS");

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
            txtPort.setText(dc.getSetting("port"));
            txtMaxConn.setText(dc.getSetting("max_conn"));
            txtMaxQueued.setText(dc.getSetting("max_queue"));
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
            txtAddress.setText(dc.getSetting("db_address"));
            txtUsername.setText(dc.getSetting("db_username"));
            txtPassword.setText(dc.getSetting("db_password"));
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

    private void btnSetLAFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetLAFActionPerformed
        final String sel = (String) model.getSelectedItem();

        for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
            if (laf.getName().equals(sel)) {
                final LookAndFeelInfo lafi = laf;
                SwingUtilities.invokeLater(() -> {
                    try {
                        UIManager.setLookAndFeel(lafi.getClassName());
                        SwingUtilities.updateComponentTreeUI(GUI.gui);

                    } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                        Logger.getLogger(SettingsWindow.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                });
            }
        }
    }//GEN-LAST:event_btnSetLAFActionPerformed

    private void btnColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnColorActionPerformed
        Color c = JColorChooser.showDialog(this, "Terminal Screen Background Color", null);
        try {
            dc.setSetting("TERMINAL_BG", Integer.toString(c.getRGB()));
        } catch (IOException ex) {
            JOptionPane.showInternalMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnColorActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            dc.setSetting("TERMINAL_BG", Integer.toString(0));
        } catch (IOException ex) {
            JOptionPane.showInternalMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImageActionPerformed
        JFileChooser chooser = new JFileChooser();
        int option = chooser.showDialog(this, "Select Image");
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                dc.setSetting("bg_url", file.getAbsolutePath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "File is not an image", "Background Image", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnImageActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
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
    private javax.swing.JButton btnPermissions;
    private javax.swing.JButton btnReceiptSave;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSaveCache;
    private javax.swing.JButton btnSaveSecurity;
    private javax.swing.JButton btnSetLAF;
    private javax.swing.JCheckBox chkAddress;
    private javax.swing.JCheckBox chkApproveNew;
    private javax.swing.JCheckBox chkCode;
    private javax.swing.JCheckBox chkEmailPrompt;
    private javax.swing.JCheckBox chkLogOut;
    private javax.swing.JCheckBox chkSendProducts;
    private javax.swing.JCheckBox chkStaff;
    private javax.swing.JCheckBox chkTerminal;
    private javax.swing.JCheckBox chkUpdate;
    private javax.swing.JComboBox<String> cmbLaf;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel panelDatabase;
    private javax.swing.JPanel panelGeneral;
    private javax.swing.JPanel panelNetwork;
    private javax.swing.JPanel panelSecurity;
    private javax.swing.JSpinner spinSaleCache;
    private javax.swing.JTextField txtAddress;
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
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
