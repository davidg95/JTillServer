/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.table.DefaultTableModel;

/**
 * Dialog which allows a product to be selected.
 *
 * @author David
 */
public class ProductSelectDialog extends javax.swing.JInternalFrame {
    
    private static Product product;

    private final DataConnect dc;

    private final DefaultTableModel model;
    private List<Product> currentTableContents;

    private final boolean showOpen;

    protected boolean closedFlag;

    /**
     * Creates new form ProductSelectDialog
     *
     * @param showOpen indicated whether open price products should show. new
     * product if a barcode is not found.
     */
    public ProductSelectDialog(boolean showOpen) {
        super();
        this.dc = GUI.gui.dc;
        this.showOpen = showOpen;
        closedFlag = false;
        initComponents();
        super.setClosable(true);
        super.setIconifiable(true);
        currentTableContents = new ArrayList<>();
        model = (DefaultTableModel) table.getModel();
        showAllProducts();
        this.addInternalFrameListener(new InternalFrameListener() {
            @Override
            public void internalFrameOpened(InternalFrameEvent e) {

            }

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                closedFlag = true;
            }

            @Override
            public void internalFrameClosed(InternalFrameEvent e) {

            }

            @Override
            public void internalFrameIconified(InternalFrameEvent e) {

            }

            @Override
            public void internalFrameDeiconified(InternalFrameEvent e) {

            }

            @Override
            public void internalFrameActivated(InternalFrameEvent e) {

            }

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {

            }
        });
        txtSearch.requestFocus();
    }

    /**
     * Method to show the product select dialog.
     *
     * @param showOpen indicates whether open products should show or not.
     * @return the product selected by the user.
     */
    public static Product showDialog(boolean showOpen) {
        final ProductSelectDialog dialog = new ProductSelectDialog(showOpen);
        product = null;
        GUI.gui.internal.add(dialog);
        final Runnable run = new Runnable() {
            @Override
            public void run() {
                dialog.setVisible(true);
                try {
                    dialog.setIcon(false);
                    dialog.setSelected(true);
                } catch (PropertyVetoException ex) {
                    Logger.getLogger(ProductSelectDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        final Thread thread = new Thread(run);
        thread.start();
        while (!dialog.closedFlag) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                Logger.getLogger(ProductSelectDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return product;
    }

    /**
     * Method to show the product select dialog.
     *
     * @return the product selected by the user.
     */
    public static Product showDialog() {
        return showDialog(true);
    }

    /**
     * Method to update the contents of the table.
     */
    private void updateTable() {
        model.setRowCount(0);

        for (Product p : currentTableContents) {
            Object[] s = new Object[]{p.getId(), p.getLongName()};
            model.addRow(s);
        }

        table.setModel(model);
        ProductsWindow.update();
    }

    /**
     * Method to show all products in the list.
     */
    private void showAllProducts() {
        try {
            currentTableContents = dc.getAllProducts();
            List<Product> newList = new ArrayList<>();
            if (!showOpen) {
                for (Product p : currentTableContents) {
                    if (!p.isOpen()) {
                        newList.add(p);
                    }
                }
                currentTableContents = newList;
            }
            updateTable();
        } catch (IOException | SQLException ex) {
            showError(ex);
        }
    }

    /**
     * Method to show an error.
     *
     * @param e the exception to show.
     */
    private void showError(Exception e) {
        JOptionPane.showInternalMessageDialog(GUI.gui.internal, e, "Products", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchButtonGroup = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        btnClose = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        radName = new javax.swing.JRadioButton();
        radBarcode = new javax.swing.JRadioButton();
        btnSelect = new javax.swing.JButton();

        setTitle("Select Product");

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "ID", "Name"
            }
        ));
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setResizable(false);
            table.getColumnModel().getColumn(1).setResizable(false);
        }

        btnClose.setText("Cancel");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        jLabel1.setText("Search:");

        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        searchButtonGroup.add(radName);
        radName.setText("Name");
        radName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radNameActionPerformed(evt);
            }
        });

        searchButtonGroup.add(radBarcode);
        radBarcode.setSelected(true);
        radBarcode.setText("Barcode");
        radBarcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radBarcodeActionPerformed(evt);
            }
        });

        btnSelect.setText("Select");
        btnSelect.setEnabled(false);
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radBarcode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSelect)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(jLabel1)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)
                    .addComponent(radName)
                    .addComponent(radBarcode)
                    .addComponent(btnSelect))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        product = null;
        closedFlag = true;
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void tableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMousePressed
        product = currentTableContents.get(table.getSelectedRow());
        btnSelect.setEnabled(true);
        if (evt.getClickCount() == 2) {
            closedFlag = true;
            this.setVisible(false);
        }
    }//GEN-LAST:event_tableMousePressed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String search = txtSearch.getText();

        if (search.length() == 0) {
            return;
        }
        List<Product> newList = new ArrayList<>();

        for (Product p : currentTableContents) {
            if (!showOpen && p.isOpen()) {
                continue;
            }
            if (radName.isSelected()) {
                if (p.getLongName().toLowerCase().contains(search.toLowerCase())) {
                    newList.add(p);
                }
            } else {
                try {
                    if (!Utilities.isNumber(search)) {
                        JOptionPane.showInternalMessageDialog(GUI.gui.internal, "Barcode must be a number", "Search", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    final Plu plu = dc.getPluByProduct(p.getId());
                    if (plu.getCode().equals(search)) {
                        newList.add(p);
                    }
                } catch (IOException | JTillException ex) {
                    Logger.getLogger(ProductSelectDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (newList.isEmpty()) {
            txtSearch.setSelectionStart(0);
            txtSearch.setSelectionEnd(txtSearch.getText().length());
            JOptionPane.showInternalMessageDialog(GUI.gui.internal, "No Results", "Search", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        currentTableContents = newList;
        updateTable();
        if (currentTableContents.size() == 1) {
            product = currentTableContents.get(0);
            this.setVisible(false);
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        btnSearch.doClick();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void radNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radNameActionPerformed
        txtSearch.requestFocus();
    }//GEN-LAST:event_radNameActionPerformed

    private void radBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radBarcodeActionPerformed
        txtSearch.requestFocus();
    }//GEN-LAST:event_radBarcodeActionPerformed

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        closedFlag = true;
        setVisible(false);
    }//GEN-LAST:event_btnSelectActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSelect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton radBarcode;
    private javax.swing.JRadioButton radName;
    private javax.swing.ButtonGroup searchButtonGroup;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
