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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
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
        getAllTills();
    }

    public static void showWindow(DataConnect dc) {
        if (window == null) {
            window = new TillWindow(dc);
            GUI.gui.internal.add(window);
        }
        window.setVisible(true);
        try {
            window.setSelected(true);
            window.setIcon(false);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(TillWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getAllTills() {
        try {
            contents = dc.getAllTills();
            model.setRowCount(0);
            for (Till t : contents) {
                model.addRow(new Object[]{t.getId(), t.getName(), new DecimalFormat("#.00").format(t.getUncashedTakings())});
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
        btnCashup = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Tills");

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Terminal Name", "Uncashed Takings"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
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

        btnCashup.setText("Cash Up");
        btnCashup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCashupActionPerformed(evt);
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
                        .addComponent(btnCashup)
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
                    .addComponent(btnCashup))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void btnCashupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCashupActionPerformed
        int index = table.getSelectedRow();
        if (index == -1) {
            return;
        }
        try {
            Till till = contents.get(index);
            till.setUncashedTakings(dc.getTillTakings(till.getId()));
            if (till.getUncashedTakings().doubleValue() <= 0) {
                JOptionPane.showInternalMessageDialog(GUI.gui.internal, "No uncashed sales", "Till", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (JOptionPane.showInternalConfirmDialog(GUI.gui.internal, "Cash up till " + till.getName() + "?", "Cash up", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }
            TillReport report = new TillReport();
            report.actualTakings = dc.getTillTakings(till.getId());
            if (report.actualTakings.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showInternalMessageDialog(GUI.gui.internal, "That till currently has no declared takings", "Cash up till " + till.getName(), JOptionPane.PLAIN_MESSAGE);
                return;
            }
            report.actualTakings = report.actualTakings.setScale(2);
            String strVal = JOptionPane.showInternalInputDialog(GUI.gui.internal, "Enter value of money counted", "Cash up till " + till.getName(), JOptionPane.PLAIN_MESSAGE);
            if (strVal == null) {
                return;
            }
            if (strVal.equals("")) {
                JOptionPane.showInternalMessageDialog(GUI.gui.internal, "A Value must be entered", "Cash up till " + till.getName(), JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Utilities.isNumber(strVal)) {
                JOptionPane.showInternalMessageDialog(GUI.gui.internal, "You must enter a number greater than zero", "Cash up till " + till.getName(), JOptionPane.ERROR_MESSAGE);
                return;
            }
            double value = Double.parseDouble(strVal);
            if (value <= 0) {
                JOptionPane.showInternalMessageDialog(GUI.gui.internal, "You must enter a value greater than zero", "Cash up till " + till.getName(), JOptionPane.ERROR_MESSAGE);
                return;
            }
            report.declared = new BigDecimal(strVal);
            report.declared = report.declared.setScale(2);
            report.difference = report.declared.subtract(report.actualTakings);
            report.difference = report.difference.setScale(2);
            DecimalFormat df;
            if (report.actualTakings.compareTo(BigDecimal.ONE) >= 1) {
                df = new DecimalFormat("#.00");
            } else {
                df = new DecimalFormat("0.00");
            }
            DecimalFormat df2;
            if (report.difference.compareTo(BigDecimal.ONE) >= 1) {
                df2 = new DecimalFormat("#.00");
            } else {
                df2 = new DecimalFormat("0.00");
            }
            report.averageSpend = report.actualTakings.divide(new BigDecimal(1));
            till = dc.getTill(till.getId());
            report.tax = BigDecimal.ZERO;

            dc.cashUncashedSales(till.getId());

            TillReportDialog.showDialog(report);

            if (JOptionPane.showInternalConfirmDialog(GUI.gui.internal, "Do you want the report emailed?", "Cash up", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                String message = "Cashup for terminal " + till.getName()
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
                            JOptionPane.showInternalMessageDialog(GUI.gui.internal, "Email sent", "Email", JOptionPane.INFORMATION_MESSAGE);
                        } catch (IOException ex) {
                            mDialog.hide();
                            JOptionPane.showInternalMessageDialog(GUI.gui.internal, "Error sending email", "Email", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                final Thread thread = new Thread(run);
                thread.start();
                mDialog.show();
            }
        } catch (IOException | SQLException | JTillException ex) {
            Logger.getLogger(TillWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnCashupActionPerformed

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        if (evt.getClickCount() == 2) {
            int index = table.getSelectedRow();
            Till t = contents.get(index);
            TillDialog.showDialog(this, t);
        }
    }//GEN-LAST:event_tableMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCashup;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnView;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
