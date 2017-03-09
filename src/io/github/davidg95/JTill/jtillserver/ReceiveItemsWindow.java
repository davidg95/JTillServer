/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Image;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author david
 */
public class ReceiveItemsWindow extends javax.swing.JFrame {

    private static ReceiveItemsWindow window;

    private final DataConnect dc;
    private final List<Product> products;
    private final DefaultTableModel model;

    /**
     * Creates new form ReceiveItemsWindow
     */
    public ReceiveItemsWindow(DataConnect dc, Image icon) {
        this.dc = dc;
        this.products = new ArrayList<>();
        initComponents();
        setTitle("Receive Stock");
        setIconImage(icon);
        model = (DefaultTableModel) tblProducts.getModel();
        tblProducts.setModel(model);
        model.setRowCount(0);
    }

    public static void showWindow(DataConnect dc, Image icon) {
        window = new ReceiveItemsWindow(dc, icon);
        window.setVisible(true);
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
        tblProducts = new javax.swing.JTable();
        btnAddPlu = new javax.swing.JButton();
        btnReceive = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        btnAddProduct = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tblProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Product", "Barcode", "Stock In"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblProducts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblProductsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblProducts);
        if (tblProducts.getColumnModel().getColumnCount() > 0) {
            tblProducts.getColumnModel().getColumn(0).setResizable(false);
            tblProducts.getColumnModel().getColumn(1).setResizable(false);
            tblProducts.getColumnModel().getColumn(2).setResizable(false);
            tblProducts.getColumnModel().getColumn(3).setResizable(false);
        }

        btnAddPlu.setText("Add Plu");
        btnAddPlu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddPluActionPerformed(evt);
            }
        });

        btnReceive.setText("Receive");
        btnReceive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReceiveActionPerformed(evt);
            }
        });

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnAddProduct.setText("Add Product");
        btnAddProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddProductActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnAddProduct)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddPlu)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnReceive)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReceive)
                    .addComponent(btnClose)
                    .addComponent(btnAddPlu)
                    .addComponent(btnAddProduct))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddPluActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddPluActionPerformed
        String barcode = JOptionPane.showInputDialog(this, "Enter barcode", "Receive Items", JOptionPane.PLAIN_MESSAGE);

        if (barcode != null) {
            Product product;
            try {
                product = dc.getProductByBarcode(barcode);
                String amount = JOptionPane.showInputDialog(this, "How many?", "Receive Items", JOptionPane.PLAIN_MESSAGE);
                if (amount == null || amount.equals("0") || amount.equals("")) {
                    return;
                }
                if (product.getStock() + Integer.parseInt(amount) > product.getMaxStockLevel()) {
                    JOptionPane.showMessageDialog(this, "Warning- this will take the product stock level higher than the maximum stock level defined for this product", "Stock", JOptionPane.WARNING_MESSAGE);
                }
                product.setStock(Integer.parseInt(amount));
                products.add(product);
                model.addRow(new Object[]{product.getId(), product.getName(), product.getPlu().getCode(), product.getStock()});
            } catch (IOException | SQLException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ProductNotFoundException ex) {
                if (JOptionPane.showConfirmDialog(this, "Barcode not found, create new product?", "Receive Items", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    Plu p = new Plu(barcode);
                    product = ProductDialog.showNewProductDialog(this, dc, p);
                    JOptionPane.showMessageDialog(this, product.getLongName() + " has now been added to the system with given stock level, there is no need to receive it here.", "Added", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_btnAddPluActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnReceiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReceiveActionPerformed
        if(products.isEmpty()){
            return;
        }
        for (Product p : products) {
            try {
                Product product = dc.getProduct(p.getId());
                product.addStock(p.getStock());
                p = dc.updateProduct(product);
            } catch (IOException | ProductNotFoundException | SQLException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        JOptionPane.showMessageDialog(this, "All items have been received", "Received", JOptionPane.INFORMATION_MESSAGE);
        model.setRowCount(0);
        products.clear();
    }//GEN-LAST:event_btnReceiveActionPerformed

    private void btnAddProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProductActionPerformed
        Product product = (Product) ProductSelectDialog.showDialog(this, dc).clone();

        if (product == null) {
            return;
        }

        String amount = JOptionPane.showInputDialog(this, "Enter amount to receive", "Receive Stock", JOptionPane.INFORMATION_MESSAGE);

        if (amount == null || amount.equals("0") || amount.equals("")) {
            return;
        }

        if (product.getStock() + Integer.parseInt(amount) > product.getMaxStockLevel()) {
            JOptionPane.showMessageDialog(this, "Warning- this will take the product stock level higher than the maximum stock level defined for this product", "Stock", JOptionPane.WARNING_MESSAGE);
        }

        product.setStock(Integer.parseInt(amount));

        products.add(product);
        model.addRow(new Object[]{product.getId(), product.getName(), product.getPlu().getCode(), product.getStock()});
    }//GEN-LAST:event_btnAddProductActionPerformed

    private void tblProductsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblProductsMouseClicked
        if (evt.getClickCount() == 2) {
            int row = tblProducts.getSelectedRow();
            Product p = products.get(row);
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this line?\n" + p.getLongName(), "Remove", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                products.remove(row);
                model.removeRow(row);
            }
        }
    }//GEN-LAST:event_tblProductsMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddPlu;
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnReceive;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblProducts;
    // End of variables declaration//GEN-END:variables
}
