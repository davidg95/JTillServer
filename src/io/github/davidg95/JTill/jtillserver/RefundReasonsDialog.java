/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author David
 */
public class RefundReasonsDialog extends javax.swing.JInternalFrame {

    private static RefundReasonsDialog dialog;

    private final DataConnect dc;

    private MyModel model;
    private final DefaultComboBoxModel cmbModel;

    private RefundReason reason;

    /**
     * Creates new form RefundReasonsDialog
     */
    public RefundReasonsDialog() {
        super();
        this.dc = GUI.gui.dc;
        initComponents();
        setTitle("Refund Reasons");
        super.setClosable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        cmbModel = (DefaultComboBoxModel) cmbPriviledge.getModel();
        cmbModel.addElement("Assisstant");
        cmbModel.addElement("Supervisor");
        cmbModel.addElement("Manager");
        cmbModel.addElement("Area Manager");
        tabbed.setEnabledAt(1, false);
        try {
            init();
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void showDialog() {
        if (dialog == null) {
            dialog = new RefundReasonsDialog();
            GUI.gui.internal.add(dialog);
        }
        dialog.setVisible(true);
        try {
            dialog.setIcon(false);
            dialog.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WasteReasonDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void init() throws IOException, SQLException {
        model = new MyModel(dc.getUsedRefundReasons());
        table.setModel(model);
        table.getColumnModel().getColumn(0).setMinWidth(40);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.setSelectionModel(new ForcedListSelectionModel());
        InputMap im = table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = table.getActionMap();

        KeyStroke deleteKey = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);

        im.put(deleteKey, "Action.delete");
        am.put("Action.delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {

            }
        });
    }

    private void setCurrent(RefundReason reason) {
        this.reason = reason;
        txtId.setText(reason.getId() + "");
        txtReason.setText(reason.getReason());
        cmbPriviledge.setSelectedIndex(reason.getPriviledgeLevel());
    }

    private class MyModel implements TableModel {

        private final List<RefundReason> reasons;
        private final List<TableModelListener> listeners;

        public MyModel(List<RefundReason> reasons) {
            this.reasons = reasons;
            this.listeners = new LinkedList<>();
        }

        public RefundReason addReason(RefundReason reason) throws IOException, SQLException {
            reason = dc.addRefundReason(reason);
            reasons.add(reason);
            alertAll();
            return reason;
        }

        public void removeReason(RefundReason reason) throws IOException, SQLException, JTillException {
            dc.removeRefundReason(reason);
            reasons.remove(reason);
            alertAll();
        }

        public RefundReason getReason(int i) {
            return reasons.get(i);
        }

        public void updateReason(RefundReason reason) throws IOException, SQLException, JTillException {
            dc.updateRefundReason(reason);
            alertAll();
        }

        @Override
        public int getRowCount() {
            return reasons.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0: {
                    return "ID";
                }
                case 1: {
                    return "Reason";
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
            RefundReason reason = reasons.get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return reason.getId();
                }
                case 1: {
                    return reason.getReason();
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

        tabbed = new javax.swing.JTabbedPane();
        panelView = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        btnReason = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        panelEdit = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        txtReason = new javax.swing.JTextField();
        cmbPriviledge = new javax.swing.JComboBox<>();
        btnClose2 = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Reason"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
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
            table.getColumnModel().getColumn(0).setMinWidth(40);
            table.getColumnModel().getColumn(0).setPreferredWidth(40);
            table.getColumnModel().getColumn(0).setMaxWidth(40);
            table.getColumnModel().getColumn(1).setResizable(false);
        }

        btnReason.setText("New Reason");
        btnReason.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReasonActionPerformed(evt);
            }
        });

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnEdit.setText("Edit");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelViewLayout = new javax.swing.GroupLayout(panelView);
        panelView.setLayout(panelViewLayout);
        panelViewLayout.setHorizontalGroup(
            panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelViewLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
                    .addGroup(panelViewLayout.createSequentialGroup()
                        .addComponent(btnReason)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEdit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        panelViewLayout.setVerticalGroup(
            panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelViewLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReason)
                    .addComponent(btnClose)
                    .addComponent(btnEdit))
                .addContainerGap())
        );

        tabbed.addTab("View", panelView);

        jLabel1.setText("ID:");

        jLabel2.setText("Reason:");

        jLabel3.setText("Privilage Level:");

        txtId.setEditable(false);

        btnClose2.setText("Close");
        btnClose2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClose2ActionPerformed(evt);
            }
        });

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelEditLayout = new javax.swing.GroupLayout(panelEdit);
        panelEdit.setLayout(panelEditLayout);
        panelEditLayout.setHorizontalGroup(
            panelEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEditLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelEditLayout.createSequentialGroup()
                        .addGroup(panelEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelEditLayout.createSequentialGroup()
                                .addGap(32, 32, 32)
                                .addGroup(panelEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtReason)
                                    .addComponent(txtId, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelEditLayout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbPriviledge, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelEditLayout.createSequentialGroup()
                        .addComponent(btnSave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelete)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 213, Short.MAX_VALUE)
                        .addComponent(btnClose2)))
                .addContainerGap())
        );
        panelEditLayout.setVerticalGroup(
            panelEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEditLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtReason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cmbPriviledge, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 282, Short.MAX_VALUE)
                .addGroup(panelEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose2)
                    .addComponent(btnSave)
                    .addComponent(btnDelete))
                .addContainerGap())
        );

        tabbed.addTab("Edit", panelEdit);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbed)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbed)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnClose2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClose2ActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnClose2ActionPerformed

    private void btnReasonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReasonActionPerformed
        String reason = JOptionPane.showInputDialog(this, "Enter new refund reason", "New Reason", JOptionPane.PLAIN_MESSAGE);

        if (reason == null) {
            return;
        }
        if (reason.equals("")) {
            JOptionPane.showMessageDialog(this, "A value must be entered", "New Refund Reason", JOptionPane.ERROR_MESSAGE);
            return;
        }

        RefundReason rr = new RefundReason(reason, 0);
        try {
            rr = model.addReason(rr);
            setCurrent(rr);
            tabbed.setEnabledAt(1, true);
            tabbed.setSelectedIndex(1);
        } catch (IOException | SQLException ex) {
            Logger.getLogger(RefundReasonsDialog.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnReasonActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        String name = txtReason.getText();
        int level = cmbPriviledge.getSelectedIndex();
        reason.setReason(name);
        reason.setPriviledgeLevel(level);
        try {
            reason.save();
            model.alertAll();
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        try {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this refund reason?\n" + reason.getReason(), "Remove Reason", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                model.removeReason(reason);
                tabbed.setSelectedIndex(0);
                reason = null;
                tabbed.setEnabledAt(1, false);
                JOptionPane.showMessageDialog(this, "Refund reason removed", "Refund Reason", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException | SQLException | JTillException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        int row = table.getSelectedRow();
        if (row == -1) {
            return;
        }
        btnEdit.setEnabled(true);
        tabbed.setEnabledAt(1, true);
        RefundReason r = model.getReason(row);
        setCurrent(r);
        if (evt.getClickCount() == 2) {
            tabbed.setSelectedIndex(1);
        }
    }//GEN-LAST:event_tableMouseClicked

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        tabbed.setSelectedIndex(1);
    }//GEN-LAST:event_btnEditActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnClose2;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnReason;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<String> cmbPriviledge;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panelEdit;
    private javax.swing.JPanel panelView;
    private javax.swing.JTabbedPane tabbed;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtReason;
    // End of variables declaration//GEN-END:variables
}
