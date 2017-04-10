/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Window which allows for adding, editing and deleting products,
 *
 * @author David
 */
public class ProductsWindow extends javax.swing.JFrame {

    public static ProductsWindow frame;

    private final DataConnect dc;

    private Product product;

    private final DefaultTableModel model;
    private List<Product> currentTableContents;

    private List<Department> departments;
    private List<Tax> taxes;
    private List<Category> categorys;
    private DefaultComboBoxModel departmentsModel;
    private DefaultComboBoxModel taxesModel;
    private DefaultComboBoxModel categorysModel;

    private final Image icon;

    /**
     * Creates new form ProductsWindow
     *
     * @param dc the data source.
     */
    public ProductsWindow(DataConnect dc, Image icon) {
        this.dc = dc;
        this.icon = icon;
        this.setIconImage(icon);
        initComponents();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        currentTableContents = new ArrayList<>();
        model = (DefaultTableModel) tableProducts.getModel();
        showAllProducts();
        init();
    }

    /**
     * Method to showing the products list window. This will create the window
     * if needed.
     *
     * @param dc the data source.
     */
    public static void showProductsListWindow(DataConnect dc, Image icon) {
        if (frame == null) {
            frame = new ProductsWindow(dc, icon);
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        update();
        frame.setCurrentProduct(null);
        frame.setVisible(true);
    }

    /**
     * Method to update the contents of the window if a change has been made
     * from another class.
     */
    public static void update() {
        if (frame != null) {
            frame.showAllProducts();
            frame.init();
        }
    }

    /**
     * Method to init the discounts, categories and taxes.
     */
    private void init() {
        try {
            departments = dc.getAllDepartments();
            taxes = dc.getAllTax();
            categorys = dc.getAllCategorys();
            departmentsModel = new DefaultComboBoxModel(departments.toArray());
            taxesModel = new DefaultComboBoxModel(taxes.toArray());
            categorysModel = new DefaultComboBoxModel(categorys.toArray());
            cmbDepartments.setModel(departmentsModel);
            cmbTax.setModel(taxesModel);
            cmbCategory.setModel(categorysModel);
        } catch (SQLException | IOException ex) {
            showError(ex);
        }
    }

    /**
     * Method to update the contents of the table with the currentTableContents
     * list.
     */
    private void updateTable() {
        model.setRowCount(0);

        for (Product p : currentTableContents) {
            try {
                final Plu plu = dc.getPlu(p.getPlu());
                Object[] s = new Object[]{p.getId(), plu.getCode(), p.getLongName(), (p.isOpen() ? "Open Price" : p.getPrice()), (p.isOpen() ? "N/A" : p.getStock())};
                model.addRow(s);
            } catch (IOException | JTillException | SQLException ex) {
                Logger.getLogger(ProductsWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        tableProducts.setModel(model);
    }

    /**
     * Method to show all the products in the database.
     */
    private void showAllProducts() {
        try {
            currentTableContents = dc.getAllProducts();
        } catch (IOException | SQLException ex) {
            showError(ex);
        }
        updateTable();
    }

    /**
     * Method to call the EditProduct dialog on a product.
     */
    private void editProduct() {
        int selectedRow = tableProducts.getSelectedRow();
        if (selectedRow != -1) {
            Product p = currentTableContents.get(selectedRow);
            ProductDialog.showEditProductDialog(this, dc, p);
            updateTable();
        }
    }

    /**
     * Method to set the fields with a current product,
     *
     * @param p the product to show.
     */
    private void setCurrentProduct(Product p) {
        if (p == null) { //If product is null then clear all the fields.
            txtName.setText("");
            txtShortName.setText("");
            txtBarcode.setText("");
            txtPrice.setText("");
            txtCostPrice.setText("");
            chkOpen.setSelected(false);
            txtStock.setText("");
            txtMinStock.setText("");
            txtMaxStock.setText("");
            txtComments.setText("");
            txtPrice.setEnabled(true);
            txtCostPrice.setEnabled(true);
            txtBarcode.setEnabled(true);
            txtMinStock.setEnabled(true);
            txtMaxStock.setEnabled(true);
            jLabel3.setEnabled(true);
            jLabel9.setEnabled(true);
            jLabel2.setEnabled(true);
            jLabel4.setEnabled(true);
            jLabel10.setEnabled(true);
            jLabel11.setEnabled(true);
            chkOpen.setSelected(false);
            cmbTax.setSelectedIndex(0);
            cmbCategory.setSelectedIndex(0);
            cmbDepartments.setSelectedIndex(0);
            product = null;
        } else { //Fill the fields with the product.
            try {
                this.product = p;
                txtName.setText(p.getLongName());
                txtShortName.setText(p.getName());
                if (p.isOpen()) { //Check if price is open.
                    txtPrice.setEnabled(false);
                    txtCostPrice.setEnabled(false);
                    txtBarcode.setEnabled(true);
                    txtStock.setEnabled(false);
                    txtMinStock.setEnabled(false);
                    txtMaxStock.setEnabled(false);
                    jLabel3.setEnabled(false);
                    jLabel9.setEnabled(false);
                    jLabel2.setEnabled(false);
                    jLabel4.setEnabled(false);
                    jLabel10.setEnabled(false);
                    jLabel11.setEnabled(false);
                    final Plu plu = dc.getPlu(p.getPlu());
                    txtBarcode.setText(plu.getCode());
                    txtPrice.setText("");
                    txtCostPrice.setText("");
                    chkOpen.setSelected(true);
                    txtStock.setText("");
                    txtMinStock.setText("");
                    txtMaxStock.setText("");
                } else {
                    txtPrice.setEnabled(true);
                    txtCostPrice.setEnabled(true);
                    txtBarcode.setEnabled(true);
                    txtStock.setEnabled(true);
                    txtMinStock.setEnabled(true);
                    txtMaxStock.setEnabled(true);
                    jLabel3.setEnabled(true);
                    jLabel9.setEnabled(true);
                    jLabel2.setEnabled(true);
                    jLabel4.setEnabled(true);
                    jLabel10.setEnabled(true);
                    jLabel11.setEnabled(true);
                    final Plu plu = dc.getPlu(p.getPlu());
                    txtBarcode.setText(plu.getCode());
                    txtPrice.setText(p.getPrice() + "");
                    txtCostPrice.setText(p.getCostPrice() + "");
                    chkOpen.setSelected(false);
                    txtStock.setText(p.getStock() + "");
                    txtMinStock.setText(p.getMinStockLevel() + "");
                    txtMaxStock.setText(p.getMaxStockLevel() + "");
                }
                txtComments.setText(p.getComments());
                int index = 0;
                //Set the Category combo box.
                int c = p.getCategory();
                index = 0;
                for (int i = 0; i < categorys.size(); i++) {
                    if (categorys.get(i).getId() == c) {
                        index = i;
                        break;
                    }
                }
                cmbCategory.setSelectedIndex(index);
                //Set the Tax combo box.
                int t = p.getTax();
                index = 0;
                for (int i = 0; i < taxes.size(); i++) {
                    if (taxes.get(i).getId() == t) {
                        index = i;
                        break;
                    }
                }
                cmbTax.setSelectedIndex(index);
                //Set the Department combo box.
                int d = p.getDepartment();
                index = 0;
                for (int i = 0; i < departments.size(); i++) {
                    if (departments.get(i).getId() == d) {
                        index = i;
                        break;
                    }
                }
                cmbDepartments.setSelectedIndex(index);
            } catch (IOException | JTillException | SQLException ex) {
                showError(ex);
            }
        }
    }

    /**
     * Method to show an error.
     *
     * @param e the exception to show.
     */
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableProducts = new javax.swing.JTable();
        btnClose = new javax.swing.JButton();
        btnRemoveProduct = new javax.swing.JButton();
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
        txtPrice = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtStock = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        btnSaveChanges = new javax.swing.JButton();
        chkOpen = new javax.swing.JCheckBox();
        btnNewProduct = new javax.swing.JButton();
        btnShowCategorys = new javax.swing.JButton();
        btnShowTax = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        radName = new javax.swing.JRadioButton();
        radBarcode = new javax.swing.JRadioButton();
        radCode = new javax.swing.JRadioButton();
        btnSearch = new javax.swing.JButton();
        btnWasteStock = new javax.swing.JButton();
        btnReceiveStock = new javax.swing.JButton();
        btnChart = new javax.swing.JButton();
        btnCSV = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        cmbDepartments = new javax.swing.JComboBox<>();
        btnDepartments = new javax.swing.JButton();

        setTitle("Stock Managment");

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
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class
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

        txtBarcode.setEditable(false);

        jLabel3.setText("Price (£):");

        txtStock.setEditable(false);

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

        btnShowCategorys.setText("Categories");
        btnShowCategorys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowCategorysActionPerformed(evt);
            }
        });

        btnShowTax.setText("Tax");
        btnShowTax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowTaxActionPerformed(evt);
            }
        });

        jLabel13.setText("Search:");

        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        buttonGroup1.add(radName);
        radName.setSelected(true);
        radName.setText("Name");
        radName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radNameActionPerformed(evt);
            }
        });

        buttonGroup1.add(radBarcode);
        radBarcode.setText("Barcode");
        radBarcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radBarcodeActionPerformed(evt);
            }
        });

        buttonGroup1.add(radCode);
        radCode.setText("Product Code");
        radCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radCodeActionPerformed(evt);
            }
        });

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        btnWasteStock.setText("Waste Stock");
        btnWasteStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWasteStockActionPerformed(evt);
            }
        });

        btnReceiveStock.setText("Receive Stock");
        btnReceiveStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReceiveStockActionPerformed(evt);
            }
        });

        btnChart.setText("Show Bar Chart");
        btnChart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChartActionPerformed(evt);
            }
        });

        btnCSV.setText("Export Current Products as CSV");
        btnCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCSVActionPerformed(evt);
            }
        });

        jLabel12.setText("Department:");

        cmbDepartments.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnDepartments.setText("Departments");
        btnDepartments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDepartmentsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(chkOpen)
                                .addComponent(txtBarcode)
                                .addComponent(txtName)
                                .addComponent(txtShortName)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel9)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(txtCostPrice))
                                .addComponent(txtStock)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(cmbDepartments, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(txtMinStock)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jLabel11))
                                        .addComponent(cmbCategory, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                            .addGap(0, 0, Short.MAX_VALUE)
                                            .addComponent(cmbTax, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtMaxStock)
                                        .addComponent(btnShowCategorys, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                                        .addComponent(btnShowTax, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnDepartments, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                        .addComponent(btnShowAll, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(jLabel5)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(btnNewProduct)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnRemoveProduct)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnSaveChanges)))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnReceiveStock)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnWasteStock))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCSV)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnChart)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radBarcode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radCode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 268, Short.MAX_VALUE)
                        .addComponent(btnClose))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 791, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE)
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
                            .addComponent(jLabel7)
                            .addComponent(btnShowCategorys))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnShowTax)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(cmbDepartments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDepartments))
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnSaveChanges)
                            .addComponent(btnRemoveProduct)
                            .addComponent(btnNewProduct))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnShowAll)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnWasteStock)
                            .addComponent(btnReceiveStock))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnChart)
                            .addComponent(btnCSV))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(jLabel13)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radName)
                    .addComponent(radBarcode)
                    .addComponent(radCode)
                    .addComponent(btnSearch))
                .addGap(6, 6, 6))
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
                    dc.removeProduct(currentTableContents.get(index).getId());
                    GUI.getInstance().updateLables();
                } catch (ProductNotFoundException | IOException | SQLException ex) {
                    showError(ex);
                }
                showAllProducts();
                setCurrentProduct(null);
                txtName.requestFocus();
            }
        }
    }//GEN-LAST:event_btnRemoveProductActionPerformed

    private void btnSaveChangesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveChangesActionPerformed
        String name = txtName.getText();
        String shortName = txtShortName.getText();
        Category category = null;
        if (!categorys.isEmpty()) {
            category = categorys.get(cmbCategory.getSelectedIndex());
        }
        Tax tax = null;
        if (!taxes.isEmpty()) {
            tax = taxes.get(cmbTax.getSelectedIndex());
        }
        Department dep = null;
        if (!departments.isEmpty()) {
            dep = departments.get(cmbDepartments.getSelectedIndex());
        }
        String comments = txtComments.getText();
        if (product.isOpen()) {
            product.setLongName(name);
            product.setName(shortName);
            product.setCategory(category.getId());
            product.setTax(tax.getId());
            product.setComments(comments);
        } else {
            BigDecimal price = new BigDecimal(txtPrice.getText());
            BigDecimal costPrice = new BigDecimal(txtCostPrice.getText());
            int stock = Integer.parseInt(txtStock.getText());
            int minStock = Integer.parseInt(txtMinStock.getText());
            int maxStock = Integer.parseInt(txtMaxStock.getText());
            product.setLongName(name);
            product.setName(shortName);
            product.setCategory(category.getId());
            product.setDepartment(dep.getId());
            product.setTax(tax.getId());
            product.setPrice(price);
            product.setCostPrice(costPrice);
            product.setStock(stock);
            product.setMinStockLevel(minStock);
            product.setMaxStockLevel(maxStock);
            product.setComments(comments);
        }
        try {
            dc.updateProduct(product);
        } catch (ProductNotFoundException | IOException | SQLException ex) {
            showError(ex);
        }
        updateTable();
    }//GEN-LAST:event_btnSaveChangesActionPerformed

    private void chkOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkOpenActionPerformed
        txtPrice.setEnabled(!chkOpen.isSelected());
        txtCostPrice.setEnabled(!chkOpen.isSelected());
        txtBarcode.setEnabled(!chkOpen.isSelected());
        txtStock.setEnabled(!chkOpen.isSelected());
        txtMinStock.setEnabled(!chkOpen.isSelected());
        txtMaxStock.setEnabled(!chkOpen.isSelected());
        jLabel3.setEnabled(!chkOpen.isSelected());
        jLabel9.setEnabled(!chkOpen.isSelected());
        jLabel2.setEnabled(!chkOpen.isSelected());
        jLabel4.setEnabled(!chkOpen.isSelected());
        jLabel10.setEnabled(!chkOpen.isSelected());
        jLabel11.setEnabled(!chkOpen.isSelected());
    }//GEN-LAST:event_chkOpenActionPerformed

    private void btnNewProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewProductActionPerformed
        String barcode = JOptionPane.showInputDialog(this, "Enter or scan barcode", "New Product", JOptionPane.INFORMATION_MESSAGE);
        if (barcode != null && !barcode.equals("")) {
            Plu plu = new Plu(barcode);
            Product p = ProductDialog.showNewProductDialog(this, dc, plu, 0);
            showAllProducts();
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

    private void btnShowCategorysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowCategorysActionPerformed
        CategorysWindow.showCategoryWindow(dc, icon);
        init();
    }//GEN-LAST:event_btnShowCategorysActionPerformed

    private void btnShowTaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowTaxActionPerformed
        TaxWindow.showTaxWindow(dc, icon);
        init();
    }//GEN-LAST:event_btnShowTaxActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        btnSearch.doClick();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        int option;
        String terms = txtSearch.getText();

        if (terms.isEmpty()) {
            showAllProducts();
            return;
        }

        if (radCode.isSelected()) {
            option = 1;
        } else if (radBarcode.isSelected()) {
            option = 2;
        } else {
            option = 3;
        }
        List<Product> newList = new ArrayList<>();
        try {
            switch (option) {
                case 1:
                    for (Product p : currentTableContents) {
                        if ((p.getId() + "").contains(terms)) {
                            newList.add(p);
                        }
                    }
                    break;
                case 2:
                    for (Product p : currentTableContents) {
                        final Plu plu = dc.getPlu(p.getPlu());
                        if (plu.getCode().equals(terms)) {
                            newList.add(p);
                        }
                    }
                    break;
                default:
                    currentTableContents.stream().filter((p) -> (p.getLongName().toLowerCase().contains(terms.toLowerCase()) || p.getName().toLowerCase().contains(terms.toLowerCase()))).forEachOrdered((p) -> {
                        newList.add(p);
                    });
                    break;
            }

            if (newList.isEmpty()) {
                txtSearch.setSelectionStart(0);
                txtSearch.setSelectionEnd(txtSearch.getText().length());
                JOptionPane.showMessageDialog(this, "No records found", "Search", JOptionPane.ERROR_MESSAGE);
            } else {
                txtSearch.setText("");
                currentTableContents = newList;
                if (newList.size() == 1) {
                    setCurrentProduct(newList.get(0));
                }
            }
            updateTable();
        } catch (IOException | JTillException | SQLException ex) {
            Logger.getLogger(ProductsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnReceiveStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReceiveStockActionPerformed
        ReceiveItemsWindow.showWindow(dc, icon);
    }//GEN-LAST:event_btnReceiveStockActionPerformed

    private void btnWasteStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWasteStockActionPerformed
        WasteStockWindow.showWindow(dc, icon);
    }//GEN-LAST:event_btnWasteStockActionPerformed

    private void btnChartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChartActionPerformed
        StockGraphWindow.showWindow(dc, icon);
    }//GEN-LAST:event_btnChartActionPerformed

    private void btnCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCSVActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setApproveButtonText("Export CSV");
        chooser.setDialogTitle("Export CSV File");
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            try {
                PrintWriter pw = new PrintWriter(file);

                for (Product p : currentTableContents) {
                    pw.println(p.getId() + ","
                            + p.getLongName() + ","
                            + p.getCategory() + ","
                            + p.getCostPrice() + ","
                            + p.getMaxStockLevel() + ","
                            + p.getMinStockLevel() + ","
                            + p.getPlu() + ","
                            + p.getPrice() + ","
                            + p.getStock() + ","
                            + p.getTax());
                }
                pw.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ProductsWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btnCSVActionPerformed

    private void radNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radNameActionPerformed
        txtSearch.requestFocus();
    }//GEN-LAST:event_radNameActionPerformed

    private void radBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radBarcodeActionPerformed
        txtSearch.requestFocus();
    }//GEN-LAST:event_radBarcodeActionPerformed

    private void radCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radCodeActionPerformed
        txtSearch.requestFocus();
    }//GEN-LAST:event_radCodeActionPerformed

    private void btnDepartmentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDepartmentsActionPerformed
        DepartmentsWindow.showWindow(dc, icon);
    }//GEN-LAST:event_btnDepartmentsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCSV;
    private javax.swing.JButton btnChart;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnDepartments;
    private javax.swing.JButton btnNewProduct;
    private javax.swing.JButton btnReceiveStock;
    private javax.swing.JButton btnRemoveProduct;
    private javax.swing.JButton btnSaveChanges;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnShowAll;
    private javax.swing.JButton btnShowCategorys;
    private javax.swing.JButton btnShowTax;
    private javax.swing.JButton btnWasteStock;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox chkOpen;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbDepartments;
    private javax.swing.JComboBox<String> cmbTax;
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
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JRadioButton radBarcode;
    private javax.swing.JRadioButton radCode;
    private javax.swing.JRadioButton radName;
    private javax.swing.JTable tableProducts;
    private javax.swing.JTextField txtBarcode;
    private javax.swing.JTextArea txtComments;
    private javax.swing.JTextField txtCostPrice;
    private javax.swing.JTextField txtMaxStock;
    private javax.swing.JTextField txtMinStock;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtShortName;
    private javax.swing.JTextField txtStock;
    // End of variables declaration//GEN-END:variables
}
