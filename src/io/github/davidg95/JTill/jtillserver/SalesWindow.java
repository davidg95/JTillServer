/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Image;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Time;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author 1301480
 */
public class SalesWindow extends javax.swing.JFrame {

    private static SalesWindow frame;

    private final DataConnectInterface dbConn;

    private final DefaultTableModel model;
    private List<Sale> currentTableContents;

    /**
     * Creates new form SalesWindow
     */
    public SalesWindow(DataConnectInterface dc, Image icon) {
        this.dbConn = TillServer.getDataConnection();
        this.setIconImage(icon);
        initComponents();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        model = (DefaultTableModel) tableSales.getModel();
    }

    public static void showSalesWindow(DataConnectInterface dc, Image icon) {
        if (frame == null) {
            frame = new SalesWindow(dc, icon);
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        frame.setCurrentSale(null);
        update();
        frame.setVisible(true);
    }

    public static void update() {
        if (frame != null) {
            frame.showAllSales();
            frame.setTakings();
        }
    }

    private void updateTable() {
        model.setRowCount(0);

        for (Sale s : currentTableContents) {
            Object[] r = new Object[]{s.getCode(), s.getTotal(), 0, new Time(s.getTime()).toString()};
            model.addRow(r);
        }

        tableSales.setModel(model);
    }

    private void setTakings() {
        try {
            List<Sale> sales = dbConn.getAllSales();
            BigDecimal val = new BigDecimal("0");
            int count = sales.size();
            for (Sale s : sales) {
                val.add(s.getTotal());
            }
            if (val.longValueExact() > 1) {
                DecimalFormat df = new DecimalFormat("#.00"); // Set your desired format here.
                lblCurrentTakings.setText("Current Takings: £" + df.format(val));
            } else {
                DecimalFormat df = new DecimalFormat("0.00"); // Set your desired format here.
                lblCurrentTakings.setText("Current Takings: £" + df.format(val));
            }
            lblSaleCount.setText("Current Sale Count: " + count);

            if (val.longValueExact() > 1) {
                DecimalFormat df = new DecimalFormat("#.00"); // Set your desired format here.
                lblDailyTakings.setText("Daily Takings: £" + df.format(val));
            } else {
                DecimalFormat df = new DecimalFormat("0.00"); // Set your desired format here.
                lblDailyTakings.setText("Daily Takings: £" + df.format(val));
            }
            lblDailySales.setText("Daily Sales: " + count);

        } catch (SQLException | IOException ex) {
            showError(ex);
        }

    }

    private void showAllSales() {
        try {
            currentTableContents = dbConn.getAllSales();
        } catch (SQLException | IOException ex) {
            showError(ex);
        }
        updateTable();
    }

    private void setCurrentSale(Sale s) {

    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e, "Products", JOptionPane.ERROR_MESSAGE);
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
        tableSales = new javax.swing.JTable();
        btnClose = new javax.swing.JButton();
        lblCurrentTakings = new javax.swing.JLabel();
        lblSaleCount = new javax.swing.JLabel();
        btnReset = new javax.swing.JButton();
        lblDailyTakings = new javax.swing.JLabel();
        lblDailySales = new javax.swing.JLabel();

        setTitle("Sales");
        setIconImage(TillServer.getIcon());

        tableSales.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Amount", "Items", "Time Stamp"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableSales.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableSalesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tableSales);
        if (tableSales.getColumnModel().getColumnCount() > 0) {
            tableSales.getColumnModel().getColumn(0).setResizable(false);
            tableSales.getColumnModel().getColumn(1).setResizable(false);
            tableSales.getColumnModel().getColumn(2).setResizable(false);
            tableSales.getColumnModel().getColumn(3).setResizable(false);
        }

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        lblCurrentTakings.setText("Current Takings: £0.00");

        lblSaleCount.setText("Current Sale Count: 0");

        btnReset.setText("Rest Daily Counter");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        lblDailyTakings.setText("Daily Takings: £0.00");

        lblDailySales.setText("Daily Sales: 0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnClose))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCurrentTakings)
                            .addComponent(lblSaleCount)
                            .addComponent(btnReset)
                            .addComponent(lblDailyTakings)
                            .addComponent(lblDailySales))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 761, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addComponent(lblDailyTakings)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDailySales)
                        .addGap(66, 66, 66)
                        .addComponent(lblCurrentTakings)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSaleCount)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnReset)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        this.setTakings();
    }//GEN-LAST:event_btnResetActionPerformed

    private void tableSalesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableSalesMouseClicked
        if (evt.getClickCount() == 2) {
            SaleDialog.showSaleDialog(this, currentTableContents.get(tableSales.getSelectedRow()));
        }
    }//GEN-LAST:event_tableSalesMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnReset;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCurrentTakings;
    private javax.swing.JLabel lblDailySales;
    private javax.swing.JLabel lblDailyTakings;
    private javax.swing.JLabel lblSaleCount;
    private javax.swing.JTable tableSales;
    // End of variables declaration//GEN-END:variables
}
