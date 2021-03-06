/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author David
 */
public class TillDialog extends javax.swing.JInternalFrame {

    private Till till;
    private final JTill jtill;
    private SalesModel salesModel;
    private ScreensModel screensModel;

    /**
     * Creates new form TillDialog
     *
     * @param t the till.
     */
    public TillDialog(JTill jtill, Till t) {
        this.till = t;
        this.jtill = jtill;
        initComponents();
        super.setClosable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        setTitle(till.getName());
        init();
    }

    private void init() {
        try {
            List<Screen> screens = jtill.getDataConnection().getAllScreens();
            screensModel = new ScreensModel(screens);
            cmbScreen.setModel(screensModel);

            txtUUID.setText(till.getUuid().toString());
            txtID.setText("" + till.getId());
            txtName.setText(till.getName());
            txtStaff.setText("Not logged in");
            try {
                Staff s = jtill.getDataConnection().getTillStaff(till.getId());
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
                Screen sc = jtill.getDataConnection().getScreen(till.getDefaultScreen());
                screensModel.setSelectedItem(sc);
            } catch (IOException | SQLException | ScreenNotFoundException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
            getAllSales();
            if (till.isConnected()) {
                btnSendData.setEnabled(true);
            } else {
                btnSendData.setEnabled(false);
            }
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void showDialog(JTill jtill, Till till) {
        final TillDialog dialog = new TillDialog(jtill, till);
        GUI.gui.internal.add(dialog);
        dialog.setVisible(true);
        try {
            dialog.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(SaleDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getAllSales() {
        try {
            BigDecimal runningTotal = BigDecimal.ZERO;
            List<Sale> contents = till.getAllTerminalSales(true);
            salesModel = new SalesModel(contents);
            table.setModel(salesModel);
            table.setSelectionModel(new ForcedListSelectionModel());
            table.getColumnModel().getColumn(1).setMinWidth(40);
            table.getColumnModel().getColumn(1).setMaxWidth(40);
            lblTotal.setText("Total: £" + new DecimalFormat("0.00").format(runningTotal));
        } catch (IOException | SQLException ex) {
            Logger.getLogger(TillDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class ScreensModel implements ComboBoxModel {

        private final List<Screen> screens;
        private Screen selected;

        public ScreensModel(List<Screen> screens) {
            this.screens = screens;
            if (!screens.isEmpty()) {
                selected = screens.get(0);
            } else {
                selected = null;
            }
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selected = (Screen) anItem;
        }

        @Override
        public Object getSelectedItem() {
            return selected;
        }

        @Override
        public int getSize() {
            return screens.size();
        }

        @Override
        public Object getElementAt(int index) {
            return screens.get(index);
        }

        @Override
        public void addListDataListener(ListDataListener l) {
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
        }

    }

    private class SalesModel implements TableModel {

        private final List<Sale> sales;
        private final List<TableModelListener> listeners;

        public SalesModel(List<Sale> sales) {
            this.sales = sales;
            listeners = new LinkedList<>();
        }

        public Sale get(int i) {
            return sales.get(i);
        }

        @Override
        public int getRowCount() {
            return sales.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0: {
                    return "Timestamp";
                }
                case 1: {
                    return "Items";
                }
                case 2: {
                    return "Value";
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return Object.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Sale s = sales.get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return s.getDate();
                }
                case 1: {
                    return s.getTotalItemCount();
                }
                case 2: {
                    return "£" + s.getTotal();
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        }

        private void alertAll() {
            for (TableModelListener l : listeners) {
                l.tableChanged(new TableModelEvent(this));
            }
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            listeners.add(l);
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            listeners.remove(l);
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

        btnClose = new javax.swing.JButton();
        btnXReport = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        lblID = new javax.swing.JLabel();
        txtID = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtUUID = new javax.swing.JTextField();
        txtStaff = new javax.swing.JTextField();
        lblStaff = new javax.swing.JLabel();
        btnLogout = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        txtName = new javax.swing.JTextField();
        btnChangeName = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        cmbScreen = new javax.swing.JComboBox<>();
        btnSaveScreen = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        btnSendData = new javax.swing.JButton();
        btnZReport = new javax.swing.JButton();
        lblTotal = new javax.swing.JLabel();

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

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
        table.getTableHeader().setReorderingAllowed(false);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setResizable(false);
            table.getColumnModel().getColumn(1).setMinWidth(40);
            table.getColumnModel().getColumn(1).setMaxWidth(40);
            table.getColumnModel().getColumn(2).setMinWidth(80);
            table.getColumnModel().getColumn(2).setMaxWidth(80);
        }

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Terminal Info"));

        lblID.setText("Terminal ID:");

        txtID.setEditable(false);

        jLabel2.setText("Terminal UUID:");

        txtUUID.setEditable(false);

        txtStaff.setEditable(false);

        lblStaff.setText("Staff:");

        btnLogout.setText("Logout");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Name"));

        txtName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameActionPerformed(evt);
            }
        });

        btnChangeName.setText("Save");
        btnChangeName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeNameActionPerformed(evt);
            }
        });

        jLabel3.setText("Name:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnChangeName)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnChangeName)
                    .addComponent(jLabel3))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Screen"));

        cmbScreen.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnSaveScreen.setText("Save");
        btnSaveScreen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveScreenActionPerformed(evt);
            }
        });

        jLabel1.setText("Default Screen:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbScreen, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSaveScreen)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbScreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSaveScreen))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblStaff)
                            .addComponent(jLabel2))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnLogout))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtUUID))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblID)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtID, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtStaff, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 96, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtUUID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblID))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtStaff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnLogout))
                    .addComponent(lblStaff))
                .addGap(28, 28, 28)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(39, Short.MAX_VALUE))
        );

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

        lblTotal.setText("Total: £0.00");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSendData, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnXReport)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnZReport))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTotal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 110, Short.MAX_VALUE)
                        .addComponent(btnClose))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnSendData)
                    .addComponent(btnXReport)
                    .addComponent(btnZReport)
                    .addComponent(lblTotal))
                .addContainerGap())
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
                return;
            }
            if (!Utilities.isNumber(strVal)) {
                JOptionPane.showMessageDialog(this, "You must enter a number greater than zero", "X Report for " + till.getName(), JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal declared = new BigDecimal(strVal);
            final TillReport report = jtill.getDataConnection().xReport(this.till, declared, GUI.staff);
            TillReportDialog.showDialog(report);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(TillDialog.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }//GEN-LAST:event_btnXReportActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        try {
            jtill.getDataConnection().logoutTill(till.getId());
            btnLogout.setEnabled(false);
        } catch (IOException | JTillException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnChangeNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeNameActionPerformed
        String name = txtName.getText();
        if (name.equals("")) {
            JOptionPane.showMessageDialog(this, "You must enter a name", "Name", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (name.equals(till.getName())) {
            return;
        }
        try {
            if (jtill.getDataConnection().isTillNameUsed(name)) {
                JOptionPane.showMessageDialog(this, "That name is already in use", "Till Rename", JOptionPane.ERROR_MESSAGE);
                return;
            }
            till.setName(name);
            jtill.getDataConnection().updateTill(till);
            JOptionPane.showMessageDialog(this, "Rename successful, data must be sent to terminal", "Rename", JOptionPane.INFORMATION_MESSAGE);
        } catch (JTillException | IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnChangeNameActionPerformed

    private void btnSendDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendDataActionPerformed
        try {
            till.sendData(null);
        } catch (IOException ex) {
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
                return;
            }
            if (!Utilities.isNumber(strVal)) {
                JOptionPane.showMessageDialog(this, "You must enter a number greater than zero", "Z Report for " + till.getName(), JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal declared = new BigDecimal(strVal);
            final TillReport report = jtill.getDataConnection().zReport(this.till, declared, GUI.staff);

            if (Boolean.getBoolean(jtill.getDataConnection().getSetting("USE_EMAIL"))) {
                if (JOptionPane.showConfirmDialog(this, "Do you want the report emailed?", "Z Report for " + till.getName(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    String message = "Cashup for terminal " + till.getName()
                            + "\nValue counted: £" + report.getDeclared().toString()
                            + "\nActual takings: £" + report.getExpected().toString()
                            + "\nDifference: £" + report.getDifference().toString();
                    final ModalDialog mDialog = new ModalDialog(this, "Email");
                    final Runnable run = () -> {
                        try {
                            jtill.getDataConnection().sendEmail(message);
                            mDialog.hide();
                            JOptionPane.showInputDialog(TillDialog.this, "Email sent", "Email", JOptionPane.INFORMATION_MESSAGE);
                        } catch (IOException ex) {
                            mDialog.hide();
                            JOptionPane.showInputDialog(TillDialog.this, "Error sending email", "Email", JOptionPane.ERROR_MESSAGE);
                        }
                    };
                    final Thread thread = new Thread(run);
                    thread.start();
                    mDialog.show();
                }
            }
            lblTotal.setText("Total: £0.00");
            getAllSales();
            TillReportDialog.showDialog(report);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(TillDialog.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }//GEN-LAST:event_btnZReportActionPerformed

    private void txtNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNameActionPerformed
        btnChangeName.doClick();
    }//GEN-LAST:event_txtNameActionPerformed

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        int row = table.getSelectedRow();
        if (row == -1) {
            return;
        }
        Sale s = salesModel.get(row);
        if (SwingUtilities.isLeftMouseButton(evt)) {
            if (evt.getClickCount() == 2) {
                SaleDialog.showSaleDialog(jtill, s);
            }
        }
    }//GEN-LAST:event_tableMouseClicked

    private void btnSaveScreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveScreenActionPerformed
        try {
            till.setDefaultScreen(((Screen) screensModel.getSelectedItem()).getId());
            jtill.getDataConnection().updateTill(till);
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (JTillException ex) {

        }
    }//GEN-LAST:event_btnSaveScreenActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChangeName;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnSaveScreen;
    private javax.swing.JButton btnSendData;
    private javax.swing.JButton btnXReport;
    private javax.swing.JButton btnZReport;
    private javax.swing.JComboBox<String> cmbScreen;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblID;
    private javax.swing.JLabel lblStaff;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtID;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtStaff;
    private javax.swing.JTextField txtUUID;
    // End of variables declaration//GEN-END:variables
}
