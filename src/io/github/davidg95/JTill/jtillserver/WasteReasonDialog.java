/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class WasteReasonDialog extends javax.swing.JInternalFrame {

    private static WasteReasonDialog dialog;
    private static final Logger LOG = Logger.getGlobal();

    private final DataConnect dc;
    private List<WasteReason> reasons;
    private final DefaultTableModel model;

    /**
     * Creates new form WasteReasonDialog
     */
    public WasteReasonDialog(Image icon) {
        super();
        this.dc = GUI.gui.dc;
        initComponents();
        setTitle("Waste Reasons");
//        setModal(true);
//        setLocationRelativeTo(parent);
        super.setClosable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(icon));
        model = (DefaultTableModel) table.getModel();
        table.setModel(model);
        init();
    }

    public static void showDialog(Image icon) {
        if (dialog == null) {
            dialog = new WasteReasonDialog(icon);
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

    private void init() {
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
        reloadList();
    }

    private void reloadList() {
        try {
            reasons = dc.getAllWasteReasons();
            model.setRowCount(0);
            for (WasteReason wr : reasons) {
                model.addRow(new Object[]{wr.getId(), wr.getReason()});
            }
        } catch (IOException | SQLException ex) {
            Logger.getLogger(WasteReasonDialog.class.getName()).log(Level.SEVERE, null, ex);
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
        btnNew = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnNew.setText("New Reason");
        btnNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewActionPerformed(evt);
            }
        });

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
        jScrollPane2.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setMinWidth(40);
            table.getColumnModel().getColumn(0).setPreferredWidth(40);
            table.getColumnModel().getColumn(0).setMaxWidth(40);
            table.getColumnModel().getColumn(1).setResizable(false);
        }

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnNew)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNew)
                    .addComponent(btnClose))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
        String reason = JOptionPane.showInternalInputDialog(this, "Enter new waste reason", "New Reason", JOptionPane.PLAIN_MESSAGE);

        if (reason == null) {
            return;
        }
        if (reason.equals("")) {
            JOptionPane.showMessageDialog(this, "A value must be entered", "New Waste Reason", JOptionPane.ERROR_MESSAGE);
            return;
        }

        WasteReason wr = new WasteReason(reason);
        try {
            wr = dc.addWasteReason(wr);
            JOptionPane.showInternalMessageDialog(this, "New reason added\nID-" + wr.getId() + " Reason-" + wr.getReason(), "Reason Added", JOptionPane.INFORMATION_MESSAGE);
            init();
        } catch (IOException | SQLException | JTillException ex) {
            LOG.log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnNewActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnNew;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
