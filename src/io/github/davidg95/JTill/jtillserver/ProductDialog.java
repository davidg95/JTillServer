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
import java.awt.HeadlessException;
import java.awt.Window;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * NewProductDialog class.
 *
 * @author David
 */
public class ProductDialog extends javax.swing.JDialog {

    private final Logger log = Logger.getGlobal();
    private static JDialog dialog;

    private static Product product; //The product that is getting returned.

    private final DataConnect dc;
    private Plu plu;

    private final boolean editMode;

    private Category selectedCategory; //The selected category for the product.
    private Tax selectedTax; //The selected tax for the product.
    private Department selectedDepartment; //The selected Department for the product.

    /**
     * Creates new form NewProduct
     *
     * @param parent
     * @param dc
     * @param p
     * @param stock stock level to set the field to.
     */
    public ProductDialog(Window parent, DataConnect dc, Plu p, int stock) {
        super(parent);
        this.dc = dc;
        this.plu = p;
        initComponents();
        try {
            selectedCategory = dc.getCategory(1);
            selectedTax = dc.getTax(1);
            selectedDepartment = dc.getDepartment(1);
            btnSelectCategory.setText(selectedCategory.getName());
            btnSelectTax.setText(selectedTax.getName());
        } catch (IOException | SQLException | JTillException ex) {
            log.log(Level.WARNING, null, ex);
        }
        this.editMode = false;
        this.setLocationRelativeTo(parent);
        this.setModal(true);
        txtStock.setText(stock + "");
        txtMinStock.setText("0");
        txtMaxStock.setText("0");
        txtBarcode.setText(p.getCode());
    }

    public ProductDialog(Window parent, DataConnect dc, Product p) {
        super(parent);
        this.dc = dc;
        this.editMode = true;
        try {
            initComponents();
            setLocationRelativeTo(parent);
            setModal(true);
            txtName.setText(p.getLongName());
            txtShortName.setText(p.getName());
            plu = dc.getPluByProduct(p.getId());
            txtOrderCode.setText(p.getOrder_code() + "");
            txtBarcode.setText(plu.getCode());
            txtPrice.setText(p.getPrice() + "");
            txtCostPrice.setText(p.getCostPrice() + "");
            txtStock.setText(p.getStock() + "");
            txtMinStock.setText(p.getMinStockLevel() + "");
            txtMaxStock.setText(p.getMaxStockLevel() + "");
            txtComments.setText(p.getComments());
            final Category c = dc.getCategory(p.getCategory());
            btnSelectCategory.setText(c.getName());
            final Tax t = dc.getTax(p.getTax());
            btnSelectTax.setText(t.getName());
            final Department d = dc.getDepartment(p.getDepartment());
            btnSelectDepartment.setText(d.getName());
            btnAddProduct.setText("Save Changes");
            setTitle("Edit Product " + p.getName());
        } catch (IOException | SQLException | JTillException ex) {
            showError(ex);
        }
    }

    /**
     * Method which shows the dialog to create a new product and returns a new
     * product object.
     *
     * @param parent the parent component.
     * @return new Product object.
     */
    public static Product showNewProductDialog(Component parent, DataConnect dc, Plu p, int stock) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        dialog = new ProductDialog(window, dc, p, stock);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        product = null;
        dialog.setVisible(true);
        return product;
    }

    public static Product showEditProductDialog(Component parent, DataConnect dc, Product p) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        dialog = new ProductDialog(window, dc, p);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        product = p;
        dialog.setVisible(true);
        return product;
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e, "Product", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtBarcode = new javax.swing.JTextField();
        txtPrice = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtStock = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtComments = new javax.swing.JTextArea();
        btnAddProduct = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        txtShortName = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtCostPrice = new javax.swing.JTextField();
        txtMinStock = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtMaxStock = new javax.swing.JTextField();
        chkOpen = new javax.swing.JCheckBox();
        btnSelectCategory = new javax.swing.JButton();
        btnSelectTax = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        btnSelectDepartment = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        txtOrderCode = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("New Product");
        setResizable(false);

        jLabel1.setText("Product Name:");

        jLabel2.setText("Barcode:");

        txtBarcode.setEditable(false);

        jLabel3.setText("Price (£):");

        jLabel4.setText("Stock:");

        jLabel5.setText("Comments:");

        txtComments.setColumns(20);
        txtComments.setRows(5);
        jScrollPane1.setViewportView(txtComments);

        btnAddProduct.setText("Add Product");
        btnAddProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddProductActionPerformed(evt);
            }
        });

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        jLabel6.setText("Short Name:");

        jLabel7.setText("Category:");

        jLabel8.setText("Tax Class:");

        jLabel9.setText("Cost Price (£):");

        jLabel10.setText("Min Stock Level:");

        jLabel11.setText("Max Stock Level:");

        chkOpen.setText("Open Price");
        chkOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkOpenActionPerformed(evt);
            }
        });

        btnSelectCategory.setText("Select Category");
        btnSelectCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectCategoryActionPerformed(evt);
            }
        });

        btnSelectTax.setText("Select Tax");
        btnSelectTax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectTaxActionPerformed(evt);
            }
        });

        jLabel12.setText("Department:");

        btnSelectDepartment.setText("Select Department");
        btnSelectDepartment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectDepartmentActionPerformed(evt);
            }
        });

        jLabel13.setText("Order Code:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(179, 179, 179)
                        .addComponent(btnAddProduct))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtMinStock, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtMaxStock))
                            .addComponent(txtStock)
                            .addComponent(chkOpen)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtCostPrice))
                            .addComponent(txtBarcode, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                            .addComponent(txtShortName, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                            .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                            .addComponent(btnSelectTax, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnSelectCategory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnSelectDepartment, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1)
                            .addComponent(txtOrderCode))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtShortName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(txtOrderCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel9)
                    .addComponent(txtCostPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkOpen)
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(txtStock, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMinStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(txtMaxStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(btnSelectDepartment))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSelectCategory)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSelectTax)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProductActionPerformed
        String name = txtName.getText();
        String shortName = txtShortName.getText();
        int orderCode = Integer.parseInt(txtOrderCode.getText());
        Category category = selectedCategory;
        Tax tax = selectedTax;
        Department dep = selectedDepartment;
        String comments = txtComments.getText();
        try {
            Plu p = dc.addPlu(plu);
            if (chkOpen.isSelected()) {
                try {
                    product = new Product(name, shortName, orderCode, category.getId(), dep.getId(), comments, tax.getId(), true);
                    product = dc.addProduct(product);

                    dc.addReceivedItem(new ReceivedItem(product.getId(), product.getStock(), product.getCostPrice()));
                    this.setVisible(false);
                } catch (IOException | SQLException ex) {
                    JOptionPane.showMessageDialog(this, ex, "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                BigDecimal price = new BigDecimal(txtPrice.getText());
                BigDecimal costPrice = new BigDecimal(txtCostPrice.getText());
                int stock = Integer.parseInt(txtStock.getText());
                int minStock = Integer.parseInt(txtMinStock.getText());
                int maxStock = Integer.parseInt(txtMaxStock.getText());

                if (!editMode) {
                    product = new Product(name, shortName, orderCode, category.getId(), dep.getId(), comments, tax.getId(), false, price, costPrice, stock, minStock, maxStock, p.getId());
                    dc.addReceivedItem(new ReceivedItem(product.getId(), product.getStock(), product.getCostPrice()));
                    product = dc.addProduct(product);
                } else {
                    product.setLongName(name);
                    product.setName(shortName);
                    product.setCategory(category.getId());
                    product.setPrice(price);
                    product.setStock(stock);
                    product.setComments(comments);
                    product.setTax(tax.getId());
                    product.setMinStockLevel(minStock);
                    product.setMaxStockLevel(maxStock);
                    product.setCostPrice(costPrice);
                    dc.updateProduct(product);
                }
                this.setVisible(false);
            }
        } catch (HeadlessException | IOException | NumberFormatException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (ProductNotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Product", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAddProductActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void chkOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkOpenActionPerformed
        txtPrice.setEnabled(!chkOpen.isSelected());
        txtCostPrice.setEnabled(!chkOpen.isSelected());
        txtStock.setEnabled(!chkOpen.isSelected());
        txtMinStock.setEnabled(!chkOpen.isSelected());
        txtMaxStock.setEnabled(!chkOpen.isSelected());
        jLabel3.setEnabled(!chkOpen.isSelected());
        jLabel9.setEnabled(!chkOpen.isSelected());
        jLabel4.setEnabled(!chkOpen.isSelected());
        jLabel10.setEnabled(!chkOpen.isSelected());
        jLabel11.setEnabled(!chkOpen.isSelected());

    }//GEN-LAST:event_chkOpenActionPerformed

    private void btnSelectCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectCategoryActionPerformed
        selectedCategory = CategorySelectDialog.showDialog(this, dc);
        btnSelectCategory.setText(selectedCategory.getName());
    }//GEN-LAST:event_btnSelectCategoryActionPerformed

    private void btnSelectTaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectTaxActionPerformed
        selectedTax = TaxSelectDialog.showDialog(this, dc);
        btnSelectTax.setText(selectedTax.getName());
    }//GEN-LAST:event_btnSelectTaxActionPerformed

    private void btnSelectDepartmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectDepartmentActionPerformed
        selectedDepartment = DepartmentSelectDialog.showDialog(this, dc);
        btnSelectDepartment.setText(selectedDepartment.getName());
    }//GEN-LAST:event_btnSelectDepartmentActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSelectCategory;
    private javax.swing.JButton btnSelectDepartment;
    private javax.swing.JButton btnSelectTax;
    private javax.swing.JCheckBox chkOpen;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField txtBarcode;
    private javax.swing.JTextArea txtComments;
    private javax.swing.JTextField txtCostPrice;
    private javax.swing.JTextField txtMaxStock;
    private javax.swing.JTextField txtMinStock;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtOrderCode;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtShortName;
    private javax.swing.JTextField txtStock;
    // End of variables declaration//GEN-END:variables
}
