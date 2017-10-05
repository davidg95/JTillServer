/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class TillDialog extends javax.swing.JDialog {

    private Till till;
    private final DataConnect dc;
    private final DefaultTableModel model;
    private List<Sale> contents;

    /**
     * Creates new form TillDialog
     *
     * @param parent the parent window.
     * @param t the till.
     */
    public TillDialog(Window parent, Till t) {
        super(parent);
        this.till = t;
        this.dc = GUI.gui.dc;
        initComponents();
        setModal(true);
        setLocationRelativeTo(parent);
        setTitle(till.getName());
        setIconImage(GUI.icon);
        txtUUID.setText(till.getUuid().toString());
        txtID.setText("" + till.getId());
        txtName.setText(till.getName());
        txtStaff.setText("Not logged in");
        try {
            Staff s = dc.getTillStaff(till.getId());
            if (s == null) {
                txtStaff.setText("Not logged in");
                btnLogout.setEnabled(false);
            } else {
                txtStaff.setText(s.getName());
                btnLogout.setEnabled(true);
            }
        } catch (IOException | JTillException ex) {
        }

        try {
            Screen sc = dc.getScreen(till.getDefaultScreen());
            txtDefaultScreen.setText(sc.getName());
        } catch (IOException | SQLException | ScreenNotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
        model = (DefaultTableModel) table.getModel();
        table.setModel(model);
        getAllSales();
    }

    public static void showDialog(Component parent, Till till) {
        Window window = null;
        if (window instanceof Frame || window instanceof Dialog) {
            window = (Window) parent;
        }
        final TillDialog dialog = new TillDialog(window, till);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    private void getAllSales() {
        try {
            BigDecimal runningTotal = BigDecimal.ZERO;
            contents = dc.getTerminalSales(till.getId(), true);
            model.setRowCount(0);
            if (till.getUncashedTakings().compareTo(BigDecimal.ZERO) == 0) {
                txtUncashedTakings.setText("£0.00");
            }
            for (Sale s : contents) {
                model.addRow(new Object[]{s.getDate(), s.getTotalItemCount(), s.getTotal()});
                runningTotal = runningTotal.add(s.getTotal());
            }
            txtUncashedTakings.setText("£" + new DecimalFormat("0.00").format(runningTotal));
        } catch (IOException | SQLException | JTillException ex) {
            Logger.getLogger(TillDialog.class.getName()).log(Level.SEVERE, null, ex);
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

        lblTakings = new javax.swing.JLabel();
        btnClose = new javax.swing.JButton();
        lblStaff = new javax.swing.JLabel();
        txtUncashedTakings = new javax.swing.JTextField();
        txtStaff = new javax.swing.JTextField();
        btnXReport = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        btnLogout = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        txtName = new javax.swing.JTextField();
        lblID = new javax.swing.JLabel();
        txtID = new javax.swing.JTextField();
        lblName = new javax.swing.JLabel();
        btnChangeName = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtUUID = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        txtDefaultScreen = new javax.swing.JTextField();
        btnChange = new javax.swing.JButton();
        btnSendData = new javax.swing.JButton();
        btnZReport = new javax.swing.JButton();

        lblTakings.setText("Uncashed Takings: ");

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        lblStaff.setText("Staff:");

        txtUncashedTakings.setEditable(false);

        txtStaff.setEditable(false);

        btnXReport.setText("X Report");
        btnXReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXReportActionPerformed(evt);
            }
        });

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Timestamp", "Items", "Value"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setResizable(false);
            table.getColumnModel().getColumn(2).setResizable(false);
        }

        btnLogout.setText("Logout");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Terminal Info"));

        lblID.setText("Terminal ID:");

        txtID.setEditable(false);

        lblName.setText("Terminal Name:");

        btnChangeName.setText("Save");
        btnChangeName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeNameActionPerformed(evt);
            }
        });

        jLabel2.setText("Terminal UUID:");

        txtUUID.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(lblName)
                    .addComponent(lblID))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtID, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtUUID, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnChangeName)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtUUID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblID)
                    .addComponent(txtID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnChangeName))
                .addContainerGap())
        );

        jLabel1.setText("Default Screen:");

        txtDefaultScreen.setEditable(false);

        btnChange.setText("Change");
        btnChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeActionPerformed(evt);
            }
        });

        btnSendData.setText("Send Data to Terminal");
        btnSendData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendDataActionPerformed(evt);
            }
        });

        btnZReport.setText("Z Report");
        btnZReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZReportActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(97, 97, 97)
                        .addComponent(btnClose)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSendData)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnXReport))
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(lblStaff)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtStaff)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnLogout))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtDefaultScreen))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(lblTakings)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtUncashedTakings)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(btnChange))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnZReport)))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblStaff)
                            .addComponent(txtStaff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnLogout))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTakings)
                            .addComponent(txtUncashedTakings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnZReport))
                        .addGap(1, 1, 1)
                        .addComponent(btnXReport)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(txtDefaultScreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnChange))
                        .addGap(86, 86, 86)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnClose)
                            .addComponent(btnSendData))
                        .addGap(39, 39, 39))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnXReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXReportActionPerformed
        try {
            String strVal = JOptionPane.showInputDialog(this, "Enter value of money counted", "X Report for " + till.getName(), JOptionPane.PLAIN_MESSAGE);
            if (strVal == null) {
                return;
            }
            if (strVal.equals("")) {
                JOptionPane.showMessageDialog(this, "You must enter a value", "X Report for " + till.getName(), JOptionPane.ERROR_MESSAGE);
            }
            if (!Utilities.isNumber(strVal)) {
                JOptionPane.showInputDialog(this, "You must enter a number greater than zero", "X Report for " + till.getName(), JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal declared = new BigDecimal(strVal);
            final TillReport report = dc.xReport(this.till.getId(), declared);
            TillReportDialog.showDialog(this, report);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(TillDialog.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }//GEN-LAST:event_btnXReportActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        try {
            dc.logoutTill(till.getId());
        } catch (IOException | JTillException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeActionPerformed
        try {
            if (dc.getAllScreens().isEmpty()) {
                JOptionPane.showMessageDialog(this, "You have not configured any screens", "Screens", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            final Screen s = ScreenSelectDialog.showDialog(this);
            if (s != null) {
                till.setDefaultScreen(s.getId());
                txtDefaultScreen.setText(s.getName());
                dc.updateTill(till);
            }
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (JTillException ex) {

        }
    }//GEN-LAST:event_btnChangeActionPerformed

    private void btnChangeNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeNameActionPerformed
        String name = txtName.getText();
        if (name.equals("")) {
            JOptionPane.showMessageDialog(this, "You msut enter a name", "Name", JOptionPane.ERROR_MESSAGE);
            return;
        }
        till.setName(name);
        try {
            dc.updateTill(till);
            JOptionPane.showMessageDialog(this, "Rename succesful, restart till to take effect.", "Rename", JOptionPane.INFORMATION_MESSAGE);
        } catch (JTillException ex) {

        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnChangeNameActionPerformed

    private void btnSendDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendDataActionPerformed
        try {
            dc.sendData(till.getId());
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSendDataActionPerformed

    private void btnZReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnZReportActionPerformed
        try {
            String strVal = JOptionPane.showInputDialog(this, "Enter value of money counted", "Z Report for " + till.getName(), JOptionPane.PLAIN_MESSAGE);
            if (strVal == null) {
                return;
            }
            if (strVal.equals("")) {
                JOptionPane.showMessageDialog(this, "You must enter a value", "Z Report for " + till.getName(), JOptionPane.ERROR_MESSAGE);
            }
            if (!Utilities.isNumber(strVal)) {
                JOptionPane.showInputDialog(this, "You must enter a number greater than zero", "Z Report for " + till.getName(), JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal declared = new BigDecimal(strVal);
            final TillReport report = dc.zReport(this.till.getId(), declared);

            if (JOptionPane.showConfirmDialog(this, "Do you want the report emailed?", "Z Report for " + till.getName(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                String message = "Cashup for terminal " + till.getName()
                        + "\nValue counted: £" + report.getDeclared().toString()
                        + "\nActual takings: £" + report.getExpected().toString()
                        + "\nDifference: £" + report.getDifference().toString();
                final ModalDialog mDialog = new ModalDialog(this, "Email", "Emailing...");
                final Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            dc.sendEmail(message);
                            mDialog.hide();
                            JOptionPane.showInputDialog(TillDialog.this, "Email sent", "Email", JOptionPane.INFORMATION_MESSAGE);
                        } catch (IOException ex) {
                            mDialog.hide();
                            JOptionPane.showInputDialog(TillDialog.this, "Error sending email", "Email", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                final Thread thread = new Thread(run);
                thread.start();
                mDialog.show();
            }
            txtUncashedTakings.setText("£0.00");
            TillReportDialog.showDialog(this, report);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(TillDialog.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }//GEN-LAST:event_btnZReportActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChange;
    private javax.swing.JButton btnChangeName;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnSendData;
    private javax.swing.JButton btnXReport;
    private javax.swing.JButton btnZReport;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblID;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblStaff;
    private javax.swing.JLabel lblTakings;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtDefaultScreen;
    private javax.swing.JTextField txtID;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtStaff;
    private javax.swing.JTextField txtUUID;
    private javax.swing.JTextField txtUncashedTakings;
    // End of variables declaration//GEN-END:variables
}
