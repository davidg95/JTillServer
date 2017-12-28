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
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author David
 */
public class WasteReasonSelectDialog extends javax.swing.JDialog {

    private static WasteReasonSelectDialog dialog;

    private final DataConnect dc = DataConnect.dataconnect;

    private static WasteReason reason;

    private MyTableModel model;

    /**
     * Creates new form WasteReasonSelectDialog
     */
    public WasteReasonSelectDialog(Window parent) {
        super(parent);
        initComponents();
        setLocationRelativeTo(parent);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle("Select Reason");
        setIconImage(GUI.icon);
        txtID.requestFocus();
        try {
            List<WasteReason> reasons = dc.getUsedWasteReasons();
            model = new MyTableModel(reasons);
            table.setModel(model);
            table.setSelectionModel(new ForcedListSelectionModel());
            table.getColumnModel().getColumn(0).setMaxWidth(40);
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static WasteReason showDialog(Component parent) {
        Window window = null;
        if (parent instanceof Frame || parent instanceof Dialog) {
            window = (Window) parent;
        }
        dialog = new WasteReasonSelectDialog(window);
        reason = null;
        dialog.setVisible(true);
        return reason;
    }

    private class MyTableModel implements TableModel {

        private final List<WasteReason> reasons;
        private final List<TableModelListener> listeners;

        public MyTableModel(List<WasteReason> reasons) {
            this.reasons = reasons;
            this.listeners = new LinkedList<>();
        }

        public WasteReason getReason(int id) {
            for (WasteReason wr : reasons) {
                if (wr.getId() == id) {
                    return wr;
                }
            }
            return null;
        }

        public WasteReason getSelected() {
            if (table.getSelectedRow() == -1) {
                return null;
            }
            return reasons.get(table.getSelectedRow());
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
        public String getColumnName(int i) {
            switch (i) {
                case 0:
                    return "ID";
                case 1:
                    return "Reason";
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int i) {
            return Object.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int i) {
            WasteReason r = reasons.get(rowIndex);
            switch (i) {
                case 0:
                    return r.getId();
                case 1:
                    return r.getReason();
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
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

    private void select() {
        if ((reason.getPriviledgeLevel() + 1) > GUI.staff.getPosition()) {
            int resp = JOptionPane.showOptionDialog(this, "You are not authorised to use this reason", "Waste Reason", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[]{"Ok", "Get Authorisation Now"}, null);
            if (resp == 1) {
                Staff s = LoginDialog.showLoginDialog(this);
                if (s == null) {
                    return;
                }
                if (reason.getPriviledgeLevel() + 1 <= s.getPosition()) {
                    setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(this, "You do not have authority", "Waste Reason", JOptionPane.ERROR_MESSAGE);
                }
            }
            return;
        }
        setVisible(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        btnCancel = new javax.swing.JButton();
        btnSelect = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        txtID = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        btnSelect.setText("Select");
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });

        jLabel1.setText("ID:");

        txtID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIDActionPerformed(evt);
            }
        });

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
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
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtID, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSelect)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnSelect)
                    .addComponent(jLabel1)
                    .addComponent(txtID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        select();
    }//GEN-LAST:event_btnSelectActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        reason = null;
        setVisible(false);
    }//GEN-LAST:event_btnCancelActionPerformed

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        if (SwingUtilities.isLeftMouseButton(evt)) {
            reason = model.getSelected();
            if (reason == null) {
                return;
            }
            if (evt.getClickCount() == 2) {
                select();
            }
        }
    }//GEN-LAST:event_tableMouseClicked

    private void txtIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIDActionPerformed
        btnSearch.doClick();
    }//GEN-LAST:event_txtIDActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        try {
            int id = Integer.parseInt(txtID.getText());
            reason = model.getReason(id);
            if (reason == null) {
                JOptionPane.showMessageDialog(this, "Reason not found", "Search", JOptionPane.ERROR_MESSAGE);
            } else {
                select();
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Must enter a number", "Search", JOptionPane.ERROR_MESSAGE);
        }
        txtID.setSelectionStart(0);
        txtID.setSelectionEnd(txtID.getText().length());
    }//GEN-LAST:event_btnSearchActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSelect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtID;
    // End of variables declaration//GEN-END:variables
}
