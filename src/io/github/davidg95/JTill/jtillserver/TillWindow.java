/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
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
        if (window == null || window.isClosed()) {
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
            for (Till t : contents) {
                model.addRow(new Object[]{t.getId(), t.getName(), new DecimalFormat("0.00").format(t.getUncashedTakings()), (t.isConnected() ? "Online" : "Offline")});
            }
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading form", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xReport(Till t) {
        try {
            TillReport report = new TillReport();
            List<Sale> sales = dc.getTerminalSales(t.getId(), true);
            if (sales.isEmpty()) {
                JOptionPane.showMessageDialog(this, "This till has no sales since the last cash up", "Cash up till", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            for (Sale s : sales) {
                report.actualTakings = report.actualTakings.add(s.getTotal());
                for (SaleItem si : s.getSaleItems()) {
                    report.tax = report.tax.add(si.getTaxValue());
                }
            }
            report.actualTakings = report.actualTakings.setScale(2);
            report.tax = report.tax.setScale(2, RoundingMode.HALF_UP);
            String strVal = JOptionPane.showInputDialog(this, "Enter value of money counted", "Cash up till " + t.getName(), JOptionPane.PLAIN_MESSAGE);
            if (strVal == null) {
                return;
            }
            if (strVal.equals("")) {
                JOptionPane.showMessageDialog(this, "You must enter a value", "Cash up till", JOptionPane.ERROR_MESSAGE);
            }
            if (!Utilities.isNumber(strVal)) {
                JOptionPane.showInputDialog(this, "You must enter a number greater than zero", "Cash up till " + t.getName(), JOptionPane.ERROR_MESSAGE);
                return;
            }
            report.declared = new BigDecimal(strVal);
            report.declared = report.declared.setScale(2);
            report.difference = report.declared.subtract(report.actualTakings);
            report.difference = report.difference.setScale(2);
            report.transactions = sales.size();
            report.averageSpend = report.actualTakings.divide(new BigDecimal(report.transactions));

            if (JOptionPane.showConfirmDialog(this, "Do you want the report emailed?", "Cash up", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                String message = "Cashup for terminal " + t.getName()
                        + "\nValue counted: £" + report.declared.toString()
                        + "\nActual takings: £" + report.actualTakings.toString()
                        + "\nDifference: £" + report.difference.toString();
                final ModalDialog mDialog = new ModalDialog(this, "Email", "Emailing...");
                final Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            dc.sendEmail(message);
                            mDialog.hide();
                            JOptionPane.showInputDialog(TillWindow.this, "Email sent", "Email", JOptionPane.INFORMATION_MESSAGE);
                        } catch (IOException ex) {
                            mDialog.hide();
                            JOptionPane.showInputDialog(TillWindow.this, "Error sending email", "Email", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                final Thread thread = new Thread(run);
                thread.start();
                mDialog.show();
            }
            TillReportDialog.showDialog(this, report);
        } catch (Exception ex) {
            JOptionPane.showInternalMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void zReport(Till t) {
        try {
            xReport(t);
            dc.cashUncashedSales(t.getId());
        } catch (IOException | SQLException ex) {
            JOptionPane.showInternalMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        btnSendData = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnBuildUpdates = new javax.swing.JButton();
        btnZ = new javax.swing.JButton();

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

        btnSendData.setText("Send Data to Tills");
        btnSendData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendDataActionPerformed(evt);
            }
        });

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        btnBuildUpdates.setText("Send Build Updates");
        btnBuildUpdates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuildUpdatesActionPerformed(evt);
            }
        });

        btnZ.setText("Z Report");
        btnZ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZActionPerformed(evt);
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
                        .addComponent(btnSendData)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRefresh)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBuildUpdates)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnZ)
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
                    .addComponent(btnSendData)
                    .addComponent(btnRefresh)
                    .addComponent(btnBuildUpdates)
                    .addComponent(btnZ))
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

            JMenuItem sendData = new JMenuItem("Send Data");
            sendData.addActionListener((ActionEvent e) -> {
                try {
                    dc.sendData(t.getId());
                    return;
                } catch (SQLException | IOException ex) {
                    JOptionPane.showInternalMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
                JOptionPane.showInternalMessageDialog(this, "Till offline", "Send Data", JOptionPane.WARNING_MESSAGE);
            });

            JMenuItem xReport = new JMenuItem("X Report");
            xReport.addActionListener((ActionEvent e) -> {
                xReport(t);
            });

            JMenuItem zReport = new JMenuItem("Z Report");
            zReport.addActionListener((ActionEvent e) -> {
                zReport(t);
            });

            if (!t.isConnected()) {
                sendData.setEnabled(false);
            }

            menu.add(view);
            menu.add(sendData);
            menu.add(xReport);
            menu.add(zReport);
            menu.show(table, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tableMouseClicked

    private void btnSendDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendDataActionPerformed
        try {
            dc.reinitialiseAllTills();
        } catch (IOException ex) {
            JOptionPane.showInternalConfirmDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSendDataActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        this.getAllTills();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnBuildUpdatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuildUpdatesActionPerformed
        try {
            dc.sendBuildUpdates();
        } catch (IOException | SQLException ex) {
            JOptionPane.showInternalConfirmDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showInternalConfirmDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnBuildUpdatesActionPerformed

    private void btnZActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnZActionPerformed
        if (JOptionPane.showInternalConfirmDialog(this, "This will take a report for all tills and reset the session, continue?", "Z Report", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                long session = Long.parseLong(dc.getSetting("SESSION", Long.toString(new Date().getTime())));
                List<Sale> sales = dc.getZSales(session);
                BigDecimal takings = BigDecimal.ZERO;
                for (Sale s : sales) {
                    for (SaleItem si : s.getSaleItems()) {
                        takings = takings.add(si.getPrice().multiply(new BigDecimal(Integer.toString(si.getQuantity()))));
                    }
                }
                dc.setSetting("SESSION", Long.toString(new Date().getTime()));
                JOptionPane.showInternalMessageDialog(this, "Got " + sales.size() + " sales\nTotal takings: £" + takings, "Z Report", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | SQLException | JTillException ex) {
                JOptionPane.showInternalMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnZActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuildUpdates;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSendData;
    private javax.swing.JButton btnView;
    private javax.swing.JButton btnZ;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
