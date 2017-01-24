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
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * NewProductDialog class.
 *
 * @author David
 */
public class ProductDialog extends javax.swing.JDialog {

    private static JDialog dialog;
    private static Product product;

    private final DataConnectInterface dbConn;

    private boolean editMode;

    private List<Discount> discounts;
    private List<Tax> taxes;
    private List<Category> categorys;

    private DefaultComboBoxModel discountsModel;
    private DefaultComboBoxModel taxesModel;
    private DefaultComboBoxModel categorysModel;

    /**
     * Creates new form NewProduct
     */
    public ProductDialog(Window parent) {
        super(parent);
        this.dbConn = TillServer.getDataConnection();
        initComponents();
        this.editMode = false;
        this.setLocationRelativeTo(parent);
        this.setModal(true);
        init();
    }

    public ProductDialog(Window parent, Product p) {
        super(parent);
        this.dbConn = TillServer.getDataConnection();
        initComponents();
        init();
        this.editMode = true;
        this.setLocationRelativeTo(parent);
        this.setModal(true);
        txtName.setText(p.getName());
        txtShortName.setText(p.getShortName());
        txtBarcode.setText(p.getBarcode());
        txtPrice.setText(p.getPrice() + "");
        txtCostPrice.setText(p.getCostPrice() + "");
        txtStock.setText(p.getStock() + "");
        txtMinStock.setText(p.getMinStockLevel() + "");
        txtMaxStock.setText(p.getMaxStockLevel() + "");
        txtComments.setText(p.getComments());
        try {
            Discount d = dbConn.getDiscount(p.getDiscountID());
            int index = 0;
            for (int i = 0; i < discounts.size(); i++) {
                if (discounts.get(i).getId() == d.getId()) {
                    index = i;
                    break;
                }
            }
            cmbDiscount.setSelectedIndex(index);
            Category c = dbConn.getCategory(p.getCategoryID());
            index = 0;
            for (int i = 0; i < categorys.size(); i++) {
                if (categorys.get(i).getID() == c.getID()) {
                    index = i;
                    break;
                }
            }
            cmbCategory.setSelectedIndex(index);
            Tax t = dbConn.getTax(p.getTaxID());
            index = 0;
            for (int i = 0; i < taxes.size(); i++) {
                if (taxes.get(i).getId() == t.getId()) {
                    index = i;
                    break;
                }
            }
            cmbTax.setSelectedIndex(index);
        } catch (CategoryNotFoundException | DiscountNotFoundException | TaxNotFoundException | IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        btnAddProduct.setText("Save Changes");
        this.setTitle("Edit Product " + p.getName());
    }

    private void init() {
        try {
            discounts = dbConn.getAllDiscounts();
            taxes = dbConn.getAllTax();
            categorys = dbConn.getAllCategorys();
            discountsModel = new DefaultComboBoxModel(discounts.toArray());
            taxesModel = new DefaultComboBoxModel(taxes.toArray());
            categorysModel = new DefaultComboBoxModel(categorys.toArray());
            cmbDiscount.setModel(discountsModel);
            cmbTax.setModel(taxesModel);
            cmbCategory.setModel(categorysModel);
        } catch (IOException | SQLException ex) {
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
    public static Product showNewProductDialog(Component parent) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        dialog = new ProductDialog(window);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        product = null;
        dialog.setVisible(true);
        return product;
    }

    public static Product showEditProductDialog(Component parent, Product p) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        dialog = new ProductDialog(window, p);
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
        cmbCategory = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        cmbTax = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtCostPrice = new javax.swing.JTextField();
        txtMinStock = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtMaxStock = new javax.swing.JTextField();
        cmbDiscount = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        chkOpen = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("New Product");
        setResizable(false);

        jLabel1.setText("Product Name:");

        jLabel2.setText("Barcode:");

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

        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel7.setText("Category:");

        cmbTax.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel8.setText("Tax Class:");

        jLabel9.setText("Cost Price (£):");

        jLabel10.setText("Min Stock Level:");

        jLabel11.setText("Max Stock Level:");

        cmbDiscount.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel12.setText("Discount:");

        chkOpen.setText("Open Price");
        chkOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkOpenActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAddProduct))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtShortName)
                            .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtStock, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtMinStock, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtMaxStock, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel7)
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cmbDiscount, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cmbTax, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addComponent(jLabel5)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkOpen)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtCostPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
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
                    .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel9)
                    .addComponent(txtCostPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkOpen)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMinStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11)
                    .addComponent(txtMaxStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnAddProduct, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProductActionPerformed
        String name = txtName.getText();
        String shortName = txtShortName.getText();
        int category = 1;
        if (!categorys.isEmpty()) {
            category = categorys.get(cmbCategory.getSelectedIndex()).getID();
        }
        int tax = 1;
        if (!taxes.isEmpty()) {
            tax = taxes.get(cmbTax.getSelectedIndex()).getId();
        }
        String comments = txtComments.getText();
        int discount = 1;
        if (!discounts.isEmpty()) {
            discount = discounts.get(cmbDiscount.getSelectedIndex()).getId();
        }
        try {
            if (chkOpen.isSelected()) {
                try {
                    product = new Product(name, shortName, category, comments, tax, discount, true);
                    dbConn.addProduct(product);
                    this.setVisible(false);
                } catch (IOException | SQLException ex) {
                    JOptionPane.showMessageDialog(this, ex, "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                String barcode = txtBarcode.getText();
                if (!dbConn.checkBarcode(barcode)) {
                    BigDecimal price = new BigDecimal(Double.parseDouble(txtPrice.getText()));
                    BigDecimal costPrice = new BigDecimal(txtCostPrice.getText());
                    int stock = Integer.parseInt(txtStock.getText());
                    int minStock = Integer.parseInt(txtMinStock.getText());
                    int maxStock = Integer.parseInt(txtMaxStock.getText());

                    if (!editMode) {
                        product = new Product(name, shortName, category, comments, tax, discount, false, price, costPrice, stock, minStock, maxStock, barcode);

                        dbConn.addProduct(product);
                    } else {
                        product.setName(name);
                        product.setName(shortName);
                        product.setCategoryID(category);
                        product.setBarcode(barcode);
                        product.setPrice(price);
                        product.setStock(stock);
                        product.setComments(comments);
                    }
                    this.setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(this, "Barcode already in use", "New Product", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (HeadlessException | IOException | NumberFormatException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAddProductActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void chkOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkOpenActionPerformed
        txtPrice.setEnabled(chkOpen.isSelected());
        txtCostPrice.setEnabled(chkOpen.isSelected());
        txtBarcode.setEnabled(chkOpen.isSelected());
        txtStock.setEnabled(chkOpen.isSelected());
        txtMinStock.setEnabled(chkOpen.isSelected());
        txtMaxStock.setEnabled(chkOpen.isSelected());
        jLabel3.setEnabled(chkOpen.isSelected());
        jLabel9.setEnabled(chkOpen.isSelected());
        jLabel2.setEnabled(chkOpen.isSelected());
        jLabel4.setEnabled(chkOpen.isSelected());
        jLabel10.setEnabled(chkOpen.isSelected());
        jLabel11.setEnabled(chkOpen.isSelected());

    }//GEN-LAST:event_chkOpenActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnClose;
    private javax.swing.JCheckBox chkOpen;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbDiscount;
    private javax.swing.JComboBox<String> cmbTax;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
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
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtShortName;
    private javax.swing.JTextField txtStock;
    // End of variables declaration//GEN-END:variables
}
