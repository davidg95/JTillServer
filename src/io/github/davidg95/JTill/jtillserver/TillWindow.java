/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import io.github.davidg95.jconn.JConnData;
import io.github.davidg95.jconn.JConnThread;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class TillWindow extends javax.swing.JInternalFrame {

    private static TillWindow window;

    private final DataConnect dc;
    private final DefaultTableModel model;
    private List<Till> contents;

    /**
     * Creates new form TillWindow
     */
    public TillWindow(DataConnect dc) {
        this.dc = dc;
        initComponents();
        super.setClosable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        model = (DefaultTableModel) table.getModel();
        table.setModel(model);
    }

    public static void showWindow(DataConnect dc) {
        if (window == null) {
            window = new TillWindow(dc);
            GUI.gui.internal.add(window);
        }
        update();
        window.setVisible(true);
        try {
            window.setSelected(true);
            window.setIcon(false);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(TillWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void update() {
        window.getAllTills();
    }

    private void getAllTills() {
        try {
            contents = dc.getAllTills();
            model.setRowCount(0);
            for (JConnThread th : TillServer.server.getClientConnections()) {
                ConnectionHandler hand = (ConnectionHandler) th.getMethodClass();
                for (int i = 0; i < contents.size(); i++) {
                    final Till t = contents.get(i);
                    if (t.getId() == hand.till.getId()) {
                        contents.set(i, hand.till);
                    }
                }
            }
            for (Till t : contents) {
                model.addRow(new Object[]{t.getId(), t.getName(), new DecimalFormat("#.00").format(t.getUncashedTakings()), (t.isConnected() ? "Online" : "Offline")});
            }
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading form", "Error", JOptionPane.ERROR_MESSAGE);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        btnClose = new javax.swing.JButton();
        btnView = new javax.swing.JButton();
        btnReinit = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();

        setTitle("Tills");

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Terminal Name", "Uncashed Takings", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setResizable(false);
            table.getColumnModel().getColumn(1).setResizable(false);
            table.getColumnModel().getColumn(2).setResizable(false);
            table.getColumnModel().getColumn(3).setResizable(false);
        }

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnView.setText("View");
        btnView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewActionPerformed(evt);
            }
        });

        btnReinit.setText("Reinitialise all connected tills");
        btnReinit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReinitActionPerformed(evt);
            }
        });

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnView)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnReinit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRefresh)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnView)
                    .addComponent(btnReinit)
                    .addComponent(btnRefresh))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewActionPerformed
        int index = table.getSelectedRow();
        if (index == -1) {
            return;
        }
        Till t = contents.get(index);
        TillDialog.showDialog(this, t);
    }//GEN-LAST:event_btnViewActionPerformed

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        if (SwingUtilities.isLeftMouseButton(evt)) {
            if (evt.getClickCount() == 2) {
                int index = table.getSelectedRow();
                if (index == -1) {
                    return;
                }
                Till t = contents.get(index);
                TillDialog.showDialog(this, t);
            }
        } else if (SwingUtilities.isRightMouseButton(evt)) {
            int index = table.getSelectedRow();
            if (index == -1) {
                return;
            }
            Till t = contents.get(index);
            JPopupMenu menu = new JPopupMenu();
            JMenuItem view = new JMenuItem("View");
            view.addActionListener((ActionEvent e) -> {
                TillDialog.showDialog(this, t);
            });
            JMenuItem reinit = new JMenuItem("Reinitialise Terminal");
            reinit.addActionListener((ActionEvent e) -> {
                for (JConnThread th : TillServer.server.getClientConnections()) {
                    ConnectionHandler hand = (ConnectionHandler) th.getMethodClass();
                    if (hand.till.getId() == t.getId()) {
                        try {
                            th.sendData(JConnData.create("REINIT"));
                            return;
                        } catch (IOException ex) {
                            JOptionPane.showInternalMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                JOptionPane.showInternalMessageDialog(this, "Till offline", "Reinitalise", JOptionPane.WARNING_MESSAGE);
            });

            if (!t.isConnected()) {
                reinit.setEnabled(false);
            }

            menu.add(view);
            menu.add(reinit);
            menu.show(table, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tableMouseClicked

    private void btnReinitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReinitActionPerformed
        if (JOptionPane.showInternalConfirmDialog(this, "Warning! This will log all staff members out. Continue?", "Reinitalise", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            try {
                dc.reinitialiseAllTills();
            } catch (IOException ex) {
                JOptionPane.showInternalConfirmDialog(GUI.gui.internal, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnReinitActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        this.getAllTills();
    }//GEN-LAST:event_btnRefreshActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnReinit;
    private javax.swing.JButton btnView;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
