/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.tillserver;

import io.github.davidg95.Till.till.Category;
import io.github.davidg95.Till.till.CategoryNotFoundException;
import io.github.davidg95.Till.till.DBConnect;
import io.github.davidg95.Till.till.Discount;
import io.github.davidg95.Till.till.DiscountNotFoundException;
import io.github.davidg95.Till.till.Product;
import io.github.davidg95.Till.till.Tax;
import io.github.davidg95.Till.till.TaxNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class ProductsWindow extends javax.swing.JFrame {

    private static JFrame frame;

    private final Data data;
    private final DBConnect dbConn;

    private Product product;

    private final DefaultTableModel model;
    private List<Product> currentTableContents;

    private List<Discount> discounts;
    private List<Tax> taxes;
    private List<Category> categorys;

    private DefaultComboBoxModel discountsModel;
    private DefaultComboBoxModel taxesModel;
    private DefaultComboBoxModel categorysModel;

    /**
     * Creates new form ProductsWindow
     *
     * @param data the List of the products.
     */
    public ProductsWindow(Data data) {
        this.data = data;
        this.dbConn = TillServer.getDBConnection();
        initComponents();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        currentTableContents = new ArrayList<>();
        model = (DefaultTableModel) tableProducts.getModel();
        showAllProducts();
        init();
    }

    public static void showProductsListWindow(Data data) {
        frame = new ProductsWindow(data);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
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
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void updateTable() {
        model.setRowCount(0);

        for (Product p : currentTableContents) {
            Object[] s = new Object[]{p.getProductCode(), p.getBarcode(), p.getName(), p.getPrice(), p.getStock()};
            model.addRow(s);
        }

        tableProducts.setModel(model);
    }

    private void showAllProducts() {
        try {
            currentTableContents = dbConn.getAllProducts();
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
        updateTable();
    }

    private void editProduct() {
        int selectedRow = tableProducts.getSelectedRow();
        if (selectedRow != -1) {
            Product p = currentTableContents.get(selectedRow);
            ProductDialog.showEditProductDialog(this, data, p);
            updateTable();
        }
    }

    private void setCurrentProduct(Product p) {
        if (p == null) {
            txtName.setText("");
            txtShortName.setText("");
            txtBarcode.setText("");
            txtPrice.setText("");
            txtCostPrice.setText("");
            txtStock.setText("");
            txtMinStock.setText("");
            txtMaxStock.setText("");
            txtComments.setText("");
            chkOpen.setSelected(false);
            cmbDiscount.setSelectedIndex(0);
            cmbTax.setSelectedIndex(0);
            cmbCategory.setSelectedIndex(0);
            product = null;
        } else {
            try {
                this.product = p;
                txtName.setText(p.getName());
                txtShortName.setText(p.getShortName());
                txtBarcode.setText(p.getBarcode());
                txtPrice.setText(p.getPrice() + "");
                txtCostPrice.setText(p.getCostPrice() + "");
                txtStock.setText(p.getStock() + "");
                txtMinStock.setText(p.getMinStockLevel() + "");
                txtMaxStock.setText(p.getMaxStockLevel() + "");
                txtComments.setText(p.getComments());
                Discount d = dbConn.getDiscount(p.getDiscountID());
                int index = 0;
                for(int i = 0; i < discounts.size(); i++){
                    if(discounts.get(i).getId() == d.getId()){
                        index = i;
                        break;
                    }
                }
                cmbDiscount.setSelectedIndex(index);
                Category c = dbConn.getCategory(p.getCategoryID());
                index = 0;
                for(int i = 0; i < categorys.size(); i++){
                    if(categorys.get(i).getID() == c.getID()){
                        index = i;
                        break;
                    }
                }
                cmbCategory.setSelectedIndex(index);
                Tax t = dbConn.getTax(p.getTaxID());
                index = 0;
                for(int i = 0; i < taxes.size(); i++){
                    if(taxes.get(i).getId()== t.getId()){
                        index = i;
                        break;
                    }
                }
                cmbTax.setSelectedIndex(index);
            } catch (SQLException | DiscountNotFoundException | CategoryNotFoundException | TaxNotFoundException ex) {
                showDatabaseError(ex);
            }
        }
    }

    private void showDatabaseError(Exception e) {
        JOptionPane.showMessageDialog(this, e, "Database Error", JOptionPane.ERROR_MESSAGE);
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
        tableProducts = new javax.swing.JTable();
        btnClose = new javax.swing.JButton();
        btnRemoveProduct = new javax.swing.JButton();
        btnSearch = new javax.swing.JButton();
        btnShowAll = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtComments = new javax.swing.JTextArea();
        txtShortName = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        cmbCategory = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        cmbTax = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtCostPrice = new javax.swing.JTextField();
        txtMinStock = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtMaxStock = new javax.swing.JTextField();
        txtBarcode = new javax.swing.JTextField();
        cmbDiscount = new javax.swing.JComboBox<>();
        txtPrice = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        txtStock = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        btnSaveChanges = new javax.swing.JButton();
        chkOpen = new javax.swing.JCheckBox();
        btnNewProduct = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Stock Managment");
        setIconImage(TillServer.getIcon());

        tableProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Product Code", "Barcode", "Name", "Price", "Stock"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableProducts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableProductsMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(tableProducts);

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnRemoveProduct.setText("Remove Product");
        btnRemoveProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveProductActionPerformed(evt);
            }
        });

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        btnShowAll.setText("Show All Products");
        btnShowAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowAllActionPerformed(evt);
            }
        });

        jLabel5.setText("Comments:");

        txtComments.setColumns(20);
        txtComments.setRows(5);
        jScrollPane2.setViewportView(txtComments);

        jLabel6.setText("Short Name:");

        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel7.setText("Category:");

        cmbTax.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel8.setText("Tax Class:");

        jLabel9.setText("Cost Price (£):");

        jLabel1.setText("Product Name:");

        jLabel10.setText("Min Stock Level:");

        txtName.setNextFocusableComponent(txtShortName);

        jLabel11.setText("Max Stock Level:");

        jLabel2.setText("Barcode:");

        cmbDiscount.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel3.setText("Price (£):");

        jLabel12.setText("Discount:");

        jLabel4.setText("Stock:");

        btnSaveChanges.setText("Save Changes");
        btnSaveChanges.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveChangesActionPerformed(evt);
            }
        });

        chkOpen.setText("Open Price");
        chkOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkOpenActionPerformed(evt);
            }
        });

        btnNewProduct.setText("Add Product");
        btnNewProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewProductActionPerformed(evt);
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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel12))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cmbCategory, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(chkOpen)
                                    .addComponent(txtBarcode)
                                    .addComponent(txtName)
                                    .addComponent(txtShortName)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(33, 33, 33)
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtCostPrice))
                                    .addComponent(txtStock)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtMinStock, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel11)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtMaxStock))
                                    .addComponent(cmbTax, javax.swing.GroupLayout.Alignment.TRAILING, 0, 278, Short.MAX_VALUE)
                                    .addComponent(cmbDiscount, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnSearch)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnShowAll))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnNewProduct)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRemoveProduct)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnSaveChanges)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 816, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtShortName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)
                            .addComponent(txtCostPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkOpen)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtMinStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11)
                            .addComponent(txtMaxStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnSaveChanges)
                            .addComponent(btnRemoveProduct)
                            .addComponent(btnNewProduct))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnShowAll)
                            .addComponent(btnSearch))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnClose)
                        .addGap(6, 6, 6))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnShowAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowAllActionPerformed
        showAllProducts();
    }//GEN-LAST:event_btnShowAllActionPerformed

    private void btnRemoveProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveProductActionPerformed
        int index = tableProducts.getSelectedRow();
        if (index != -1) {
            int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove the following product?\n" + currentTableContents.get(index), "Remove Product", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    dbConn.removeProduct(currentTableContents.get(index).getProductCode());
                } catch (SQLException ex) {
                    showDatabaseError(ex);
                }
                this.updateTable();
                setCurrentProduct(null);
            }
        }
    }//GEN-LAST:event_btnRemoveProductActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        currentTableContents = ProductSearchDialog.showSearchDialog(this, currentTableContents);
        updateTable();
        if (currentTableContents.size() == 1) {
            setCurrentProduct(currentTableContents.get(0));
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnSaveChangesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveChangesActionPerformed
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
        if (chkOpen.isSelected()) {
            product.setName(name);
            product.setShortName(shortName);
            product.setCategoryID(category);
            product.setTaxID(tax);
            product.setDiscountID(discount);
            product.setComments(comments);
        } else {
            String barcode = txtBarcode.getText();
            double price = Double.parseDouble(txtPrice.getText());
            double costPrice = Double.parseDouble(txtCostPrice.getText());
            int stock = Integer.parseInt(txtStock.getText());
            int minStock = Integer.parseInt(txtMinStock.getText());
            int maxStock = Integer.parseInt(txtMaxStock.getText());
            product.setName(name);
            product.setShortName(shortName);
            product.setCategoryID(category);
            product.setTaxID(tax);
            product.setDiscountID(discount);
            product.setBarcode(barcode);
            product.setPrice(price);
            product.setCostPrice(costPrice);
            product.setStock(stock);
            product.setMinStockLevel(minStock);
            product.setMaxStockLevel(maxStock);
            product.setComments(comments);
        }
        updateTable();
    }//GEN-LAST:event_btnSaveChangesActionPerformed

    private void chkOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkOpenActionPerformed
        if (chkOpen.isSelected()) {
            txtPrice.setEnabled(false);
            txtCostPrice.setEnabled(false);
        } else {
            txtPrice.setEnabled(true);
            txtCostPrice.setEnabled(true);
        }
    }//GEN-LAST:event_chkOpenActionPerformed

    private void btnNewProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewProductActionPerformed
        if (product == null) {
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
            Product p;
            if (chkOpen.isSelected()) {
                if (name == null || shortName == null || comments == null) {
                    JOptionPane.showMessageDialog(this, "Fill out all required fields", "New Product", JOptionPane.ERROR_MESSAGE);
                } else {
                    p = new Product(name, shortName, category, comments, tax, discount);
                    try {
                        dbConn.addProduct(p);
                    } catch (SQLException ex) {
                        showDatabaseError(ex);
                    }
                }
            } else {
                try {
                    String barcode = txtBarcode.getText();
                    double price = Double.parseDouble(txtPrice.getText());
                    double costPrice = Double.parseDouble(txtCostPrice.getText());
                    int stock = Integer.parseInt(txtStock.getText());
                    int minStock = Integer.parseInt(txtMinStock.getText());
                    int maxStock = Integer.parseInt(txtMaxStock.getText());
                    if (name.equals("") || shortName.equals("") || barcode.equals("")) {
                        JOptionPane.showMessageDialog(this, "Fill out all required fields", "New Product", JOptionPane.ERROR_MESSAGE);
                    } else {
                        p = new Product(name, shortName, category, comments, tax, discount, price, costPrice, stock, minStock, maxStock, barcode);
                        try {
                            dbConn.addProduct(p);
                            showAllProducts();
                            setCurrentProduct(null);
                            txtName.requestFocus();
                        } catch (SQLException ex) {
                            showDatabaseError(ex);
                        }
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Fill out all required fields", "New Product", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            setCurrentProduct(null);
        }
    }//GEN-LAST:event_btnNewProductActionPerformed

    private void tableProductsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableProductsMousePressed
        if (evt.getClickCount() == 2) {
            editProduct();
        } else if (evt.getClickCount() == 1) {
            Product p = currentTableContents.get(tableProducts.getSelectedRow());
            setCurrentProduct(p);
        }
    }//GEN-LAST:event_tableProductsMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnNewProduct;
    private javax.swing.JButton btnRemoveProduct;
    private javax.swing.JButton btnSaveChanges;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnShowAll;
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
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tableProducts;
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
