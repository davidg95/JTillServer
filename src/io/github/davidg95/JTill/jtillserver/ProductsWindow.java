/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
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
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Window which allows for adding, editing and deleting products,
 *
 * @author David
 */
public class ProductsWindow extends javax.swing.JInternalFrame {

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

    private Category searchC;
    private Department searchD;

    /**
     * Creates new form ProductsWindow
     */
    public ProductsWindow() {
        this.dc = GUI.gui.dc;
        initComponents();
        super.setFrameIcon(new ImageIcon(GUI.icon));
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setClosable(true);
        currentTableContents = new ArrayList<>();
        model = (DefaultTableModel) tableProducts.getModel();
        showAllProducts();
        init();
    }

    /**
     * Method to showing the products list window. This will create the window
     * if needed.
     */
    public static void showProductsListWindow() {
        if (frame == null || frame.isClosed()) {
            frame = new ProductsWindow();
            GUI.gui.internal.add(frame);
        }
        if (frame.isVisible()) {
            frame.toFront();
        } else {
            update();
            frame.setCurrentProduct(null);
            frame.setVisible(true);
        }
        try {
            frame.setIcon(false);
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(SettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get the instance of the window.
     *
     * @return the window instance.
     */
    public static ProductsWindow getWindow() {
        return frame;
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
        tableProducts.setSelectionModel(new ForcedListSelectionModel());
        tableProducts.getColumnModel().getColumn(0).setMaxWidth(40);
        tableProducts.getColumnModel().getColumn(3).setMaxWidth(60);
        tableProducts.getColumnModel().getColumn(4).setMaxWidth(60);
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
            Object[] s = new Object[]{p.getId(), p.getBarcode(), p.getLongName(), (p.isOpen() ? "Open Price" : p.getPrice().setScale(2)), (p.isOpen() ? "N/A" : p.getStock())};
            model.addRow(s);
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
            txtPackSize.setText("");
            txtStock.setText("");
            txtMinStock.setText("");
            txtMaxStock.setText("");
            txtComments.setText("");
            txtScaleName.setText("");
            txtScale.setText("");
            cmbTax.setSelectedIndex(0);
            cmbCategory.setSelectedIndex(0);
            cmbDepartments.setSelectedIndex(0);
            chkScale.setEnabled(false);
            chkScale.setSelected(false);
            chkIncVat.setSelected(false);
            product = null;
            for (Component c : panelCurrent.getComponents()) {
                c.setEnabled(false);
            }
        } else { //Fill the fields with the product.
            for (Component c : panelCurrent.getComponents()) {
                c.setEnabled(true);
            }
            this.product = p;
            txtName.setText(product.getLongName());
            txtShortName.setText(product.getName());
            if (product.isOpen()) { //Check if price is open.
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
                jLabel14.setEnabled(false);
                jLabel15.setEnabled(true);
                jLabel16.setEnabled(true);
                txtScaleName.setEnabled(true);
                txtScale.setEnabled(true);
                chkScale.setEnabled(true);
                chkScale.setSelected(product.getScale() != -1);
                if (product.getScale() == -1) {
                    txtScaleName.setEnabled(false);
                    txtScale.setEnabled(false);
                    txtScaleName.setText("");
                    txtScale.setText("");
                } else {
                    txtScaleName.setText(product.getScaleName());
                    txtScale.setText(product.getScale() + "");
                }
                chkIncVat.setSelected(false);
                chkIncVat.setEnabled(false);
                txtBarcode.setText(product.getBarcode());
                txtPrice.setText("OPEN");
                txtCostPrice.setText("OPEN");
                txtPackSize.setText("0");
                txtScaleName.setText(p.getScaleName());
                txtScale.setText(p.getScale() + "");
                txtPackSize.setEnabled(false);
                txtPrice.setEditable(false);
                txtCostPrice.setEditable(false);
                txtStock.setText("");
                txtMinStock.setText("");
                txtMaxStock.setText("");
            } else {
                txtPrice.setEnabled(true);
                txtCostPrice.setEnabled(true);
                txtPackSize.setEnabled(true);
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
                jLabel14.setEnabled(true);
                jLabel15.setEnabled(false);
                jLabel16.setEnabled(false);
                txtScaleName.setEnabled(false);
                txtScale.setEnabled(false);
                txtScaleName.setText("");
                txtScale.setText("");
                chkScale.setSelected(false);
                chkScale.setEnabled(false);
                chkIncVat.setEnabled(true);
                chkIncVat.setSelected(product.isPriceIncVat());
                txtBarcode.setText(product.getBarcode());
                txtPrice.setText(product.getPrice().setScale(2) + "");
                txtCostPrice.setText(product.getCostPrice().setScale(2) + "");
                txtPackSize.setValue(product.getPackSize());
                txtStock.setText(product.getStock() + "");
                txtMinStock.setText(product.getMinStockLevel() + "");
                txtMaxStock.setText(product.getMaxStockLevel() + "");
                txtPrice.setEditable(true);
                txtCostPrice.setEditable(true);
            }
            txtComments.setText(product.getComments());
            int index = 0;
            //Set the Category combo box.
            int c = product.getCategory().getId();
            index = 0;
            for (int i = 0; i < categorys.size(); i++) {
                if (categorys.get(i).getId() == c) {
                    index = i;
                    break;
                }
            }
            cmbCategory.setSelectedIndex(index);
            //Set the Tax combo box.
            int t = product.getTax().getId();
            index = 0;
            for (int i = 0; i < taxes.size(); i++) {
                if (taxes.get(i).getId() == t) {
                    index = i;
                    break;
                }
            }
            cmbTax.setSelectedIndex(index);
            //Set the Department combo box.
            int d = product.getDepartment().getId();
            index = 0;
            for (int i = 0; i < departments.size(); i++) {
                if (departments.get(i).getId() == d) {
                    index = i;
                    break;
                }
            }
            cmbDepartments.setSelectedIndex(index);
        }
    }

    /**
     * Method to show an error.
     *
     * @param e the exception to show.
     */
    private void showError(Exception e) {
        JOptionPane.showInternalMessageDialog(this, e, "Products", JOptionPane.ERROR_MESSAGE);
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
        btnShowAll = new javax.swing.JButton();
        btnNewProduct = new javax.swing.JButton();
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
        panelCurrent = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtComments = new javax.swing.JTextArea();
        txtBarcode = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        btnShowCategorys = new javax.swing.JButton();
        btnShowTax = new javax.swing.JButton();
        txtMaxStock = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        cmbTax = new javax.swing.JComboBox<>();
        txtShortName = new javax.swing.JTextField();
        txtMinStock = new javax.swing.JTextField();
        txtCostPrice = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        cmbDepartments = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        btnDepartments = new javax.swing.JButton();
        cmbCategory = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        txtStock = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        txtPackSize = new javax.swing.JFormattedTextField();
        jLabel15 = new javax.swing.JLabel();
        txtScaleName = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        txtScale = new javax.swing.JTextField();
        chkScale = new javax.swing.JCheckBox();
        chkIncVat = new javax.swing.JCheckBox();
        btnSaveChanges = new javax.swing.JButton();
        btnRemoveProduct = new javax.swing.JButton();
        btnAdvanced = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setTitle("Stock Managment");

        tableProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Barcode", "Name", "Price", "Stock"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class
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
        tableProducts.getTableHeader().setReorderingAllowed(false);
        tableProducts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableProductsMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(tableProducts);
        if (tableProducts.getColumnModel().getColumnCount() > 0) {
            tableProducts.getColumnModel().getColumn(0).setResizable(false);
            tableProducts.getColumnModel().getColumn(1).setResizable(false);
            tableProducts.getColumnModel().getColumn(2).setResizable(false);
            tableProducts.getColumnModel().getColumn(3).setResizable(false);
            tableProducts.getColumnModel().getColumn(4).setResizable(false);
        }

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnShowAll.setText("Show All Products");
        btnShowAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowAllActionPerformed(evt);
            }
        });

        btnNewProduct.setText("Create New Product");
        btnNewProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewProductActionPerformed(evt);
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

        panelCurrent.setBorder(javax.swing.BorderFactory.createTitledBorder("Current Product"));

        jLabel5.setText("Comments:");

        txtComments.setColumns(20);
        txtComments.setRows(5);
        txtComments.setEnabled(false);
        jScrollPane2.setViewportView(txtComments);

        txtBarcode.setEditable(false);

        jLabel10.setText("Min Stock Level:");

        txtName.setNextFocusableComponent(txtShortName);

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

        jLabel12.setText("Department:");

        jLabel1.setText("Product Name:");

        cmbTax.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel3.setText("Price (£):");

        cmbDepartments.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel9.setText("Cost Price (£):");

        btnDepartments.setText("Departments");
        btnDepartments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDepartmentsActionPerformed(evt);
            }
        });

        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel8.setText("Tax Class:");

        txtStock.setEditable(false);

        jLabel2.setText("Barcode:");

        jLabel11.setText("Max Stock Level:");

        jLabel6.setText("Short Name:");

        jLabel7.setText("Category:");

        jLabel4.setText("Stock:");

        jLabel14.setText("Pack Size:");

        txtPackSize.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getIntegerInstance())));

        jLabel15.setText("Scale Name:");

        jLabel16.setText("Scale Factor:");

        chkScale.setText("Use Scale");
        chkScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkScaleActionPerformed(evt);
            }
        });

        chkIncVat.setText("Inc. VAT");

        btnSaveChanges.setText("Save Changes");
        btnSaveChanges.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveChangesActionPerformed(evt);
            }
        });

        btnRemoveProduct.setText("Remove Product");
        btnRemoveProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveProductActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelCurrentLayout = new javax.swing.GroupLayout(panelCurrent);
        panelCurrent.setLayout(panelCurrentLayout);
        panelCurrentLayout.setHorizontalGroup(
            panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCurrentLayout.createSequentialGroup()
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelCurrentLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel6)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel9)
                            .addComponent(jLabel4)
                            .addComponent(jLabel15)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelCurrentLayout.createSequentialGroup()
                                .addComponent(txtCostPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtPackSize, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtBarcode, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                                .addComponent(txtName)
                                .addComponent(txtShortName)
                                .addGroup(panelCurrentLayout.createSequentialGroup()
                                    .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(chkIncVat)))
                            .addGroup(panelCurrentLayout.createSequentialGroup()
                                .addComponent(txtStock, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtMinStock, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtMaxStock, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelCurrentLayout.createSequentialGroup()
                                .addComponent(txtScaleName, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtScale, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chkScale))
                            .addGroup(panelCurrentLayout.createSequentialGroup()
                                .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnShowCategorys, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelCurrentLayout.createSequentialGroup()
                                .addComponent(cmbTax, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnShowTax, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 1, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCurrentLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelCurrentLayout.createSequentialGroup()
                                .addComponent(btnSaveChanges)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRemoveProduct))
                            .addGroup(panelCurrentLayout.createSequentialGroup()
                                .addComponent(cmbDepartments, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnDepartments, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        panelCurrentLayout.setVerticalGroup(
            panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCurrentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtShortName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(chkIncVat))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCostPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(txtPackSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(txtMinStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(txtMaxStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtScaleName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16)
                    .addComponent(txtScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkScale))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnShowCategorys)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnShowTax)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbDepartments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(btnDepartments))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSaveChanges)
                    .addComponent(btnRemoveProduct))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnAdvanced.setText("Advanced");
        btnAdvanced.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdvancedActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(btnCSV)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnChart, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(btnNewProduct)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnReceiveStock)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnWasteStock)))
                    .addComponent(panelCurrent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnShowAll, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAdvanced)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 195, Short.MAX_VALUE)
                        .addComponent(btnClose))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelCurrent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnNewProduct)
                            .addComponent(btnReceiveStock)
                            .addComponent(btnWasteStock))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnShowAll)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnCSV)
                            .addComponent(btnChart))
                        .addGap(0, 44, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(jLabel13)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radName)
                    .addComponent(radBarcode)
                    .addComponent(radCode)
                    .addComponent(btnSearch)
                    .addComponent(btnAdvanced))
                .addGap(6, 6, 6))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.hide();
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnShowAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowAllActionPerformed
        showAllProducts();
    }//GEN-LAST:event_btnShowAllActionPerformed

    private void btnRemoveProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveProductActionPerformed
        int index = tableProducts.getSelectedRow();
        if (index != -1) {
            int opt = JOptionPane.showInternalConfirmDialog(this, "Are you sure you want to remove the following product?\n" + currentTableContents.get(index), "Remove Product", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    dc.removeProduct(currentTableContents.get(index).getId());
                    GUI.getInstance().updateLables();
                    showAllProducts();
                    setCurrentProduct(null);
                    txtName.requestFocus();
                    JOptionPane.showInternalMessageDialog(this, "Product has been removed", "Remove Product", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    if (ex.getErrorCode() == 20000) {
                        JOptionPane.showMessageDialog(this, "This product is still being refernced in either a received report or a sale report, these reports must be cleared before the product can be deleted", "Remove Product", JOptionPane.ERROR_MESSAGE);
                    } else {
                        showError(ex);
                    }
                } catch (ProductNotFoundException | IOException ex) {
                    showError(ex);
                }
            }
        }
    }//GEN-LAST:event_btnRemoveProductActionPerformed

    private void btnSaveChangesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveChangesActionPerformed
        String name = txtName.getText();
        String shortName = txtShortName.getText();
        if (name.length() == 0 || shortName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Must enter a product name", "Save Changes", JOptionPane.ERROR_MESSAGE);
            return;
        }
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
            product.setCategory(category);
            product.setDepartment(dep);
            product.setTax(tax);
            product.setComments(comments);
            if (chkScale.isSelected()) {
                if (txtScaleName.getText().isEmpty() || txtScale.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "You msut fill out all fields", "Save Changes", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String scaleName = txtScaleName.getText();
                String strScale = txtScale.getText();
                if (!Utilities.isNumber(strScale)) {
                    JOptionPane.showMessageDialog(this, "Must enter a number for scale", "Save Changes", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double scale = Double.parseDouble(strScale);
                if (scale <= 0) {
                    JOptionPane.showMessageDialog(this, "Scale must be greater than 0", "Save Changes", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                product.setScaleName(scaleName);
                product.setScale(scale);
            } else {
                product.setScaleName("Price");
                product.setScale(-1);
            }
        } else {
            String pr = txtPrice.getText();
            String costPr = txtCostPrice.getText();
            int packSize = Integer.parseInt(txtPackSize.getText());
            if (packSize < 1) {
                JOptionPane.showInternalMessageDialog(this, "Pack size must be 1 or greater", "Save Changes", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Utilities.isNumber(pr) || !Utilities.isNumber(costPr)) {
                JOptionPane.showInternalMessageDialog(this, "Must enter a number for price and cost price", "Save Changes", JOptionPane.ERROR_MESSAGE);
                return;
            }
            BigDecimal price = new BigDecimal(pr);
            BigDecimal costPrice = new BigDecimal(costPr);
            String st = txtStock.getText();
            String minSt = txtMinStock.getText();
            String maxSt = txtMaxStock.getText();
            if (!Utilities.isNumber(st) || !Utilities.isNumber(minSt) || !Utilities.isNumber(maxSt)) {
                JOptionPane.showInternalMessageDialog(this, "Must enter a number for stock levels", "Save Changes", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int stock = Integer.parseInt(st);
            int minStock = Integer.parseInt(minSt);
            int maxStock = Integer.parseInt(maxSt);
            boolean incVat = chkIncVat.isSelected();
            product.setLongName(name);
            product.setName(shortName);
            product.setCategory(category);
            product.setDepartment(dep);
            product.setTax(tax);
            product.setPrice(price);
            product.setCostPrice(costPrice);
            product.setPackSize(packSize);
            product.setStock(stock);
            product.setMinStockLevel(minStock);
            product.setMaxStockLevel(maxStock);
            product.setComments(comments);
            product.setPriceIncVat(incVat);
        }
        try {
            dc.updateProduct(product);
        } catch (ProductNotFoundException | IOException | SQLException ex) {
            showError(ex);
        }
        updateTable();
    }//GEN-LAST:event_btnSaveChangesActionPerformed

    private void btnNewProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewProductActionPerformed
        ProductEntryDialog.showDialog(this);
        showAllProducts();
        setCurrentProduct(null);
    }//GEN-LAST:event_btnNewProductActionPerformed

    private void tableProductsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableProductsMousePressed
        Product p = currentTableContents.get(tableProducts.getSelectedRow());
        setCurrentProduct(p);
    }//GEN-LAST:event_tableProductsMousePressed

    private void btnShowCategorysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowCategorysActionPerformed
        CategorysWindow.showCategoryWindow(dc);
        init();
    }//GEN-LAST:event_btnShowCategorysActionPerformed

    private void btnShowTaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowTaxActionPerformed
        TaxWindow.showTaxWindow(dc);
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
        final List<Product> newList = new ArrayList<>();
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
                    if (p.getBarcode().equals(terms)) {
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
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnReceiveStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReceiveStockActionPerformed
        ReceiveItemsWindow.showWindow(dc);
    }//GEN-LAST:event_btnReceiveStockActionPerformed

    private void btnWasteStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWasteStockActionPerformed
        WasteStockWindow.showWindow(dc);
    }//GEN-LAST:event_btnWasteStockActionPerformed

    private void btnChartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChartActionPerformed
        StockGraphWindow.showWindow(dc);
    }//GEN-LAST:event_btnChartActionPerformed

    private void btnCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCSVActionPerformed
        final JFileChooser chooser = new JFileChooser();
        chooser.setApproveButtonText("Export CSV");
        chooser.setDialogTitle("Export CSV File");
        final int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            try {
                final PrintWriter pw = new PrintWriter(file);

                for (Product p : currentTableContents) {
                    pw.println(p.getId() + ","
                            + p.getLongName() + ","
                            + p.getCategory().getId() + ","
                            + p.getCostPrice() + ","
                            + p.getMaxStockLevel() + ","
                            + p.getMinStockLevel() + ","
                            + p.getPrice() + ","
                            + p.getStock() + ","
                            + p.getTax().getId() + ","
                            + p.getDepartment().getId());
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
        DepartmentsWindow.showWindow(dc);
    }//GEN-LAST:event_btnDepartmentsActionPerformed

    private void btnAdvancedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdvancedActionPerformed
        final JLabel label = new JLabel("Select Category: ");

        final JTextField field = new JTextField();

        final JRadioButton b1 = new JRadioButton("Category");
        b1.addActionListener((ActionEvent e) -> {
            label.setText("Select Category: ");
            field.setText("");
            searchC = null;
            searchD = null;
        });
        final JRadioButton b2 = new JRadioButton("Department");
        b2.addActionListener((ActionEvent e) -> {
            label.setText("Select Department: ");
            field.setText("");
            searchC = null;
            searchD = null;
        });
        final ButtonGroup bg = new ButtonGroup();
        bg.add(b1);
        bg.add(b2);
        field.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (b1.isSelected()) {
                    searchFieldClick(1, field);
                } else {
                    searchFieldClick(2, field);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

        });

        final JPanel panel = new JPanel();
        panel.add(b1);
        panel.add(b2);
        panel.add(label);
        panel.add(field);
        b1.setSelected(true);
        JOptionPane.showInternalMessageDialog(GUI.gui.internal, panel, "Advanced Search", JOptionPane.PLAIN_MESSAGE);
        if (searchC == null && searchD == null) {
            return;
        }
    }//GEN-LAST:event_btnAdvancedActionPerformed

    private void chkScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkScaleActionPerformed
        txtScale.setEnabled(chkScale.isSelected());
        txtScaleName.setEnabled(chkScale.isSelected());
    }//GEN-LAST:event_chkScaleActionPerformed

    private void searchFieldClick(int opt, JTextField f) {
        if (opt == 1) {
            searchC = CategorySelectDialog.showDialog(this);
            if (searchC != null) {
                f.setText(searchC.getName());
            }
        } else {
            searchD = DepartmentSelectDialog.showDialog(this);
            if (searchD != null) {
                f.setText(searchD.getName());
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdvanced;
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
    private javax.swing.JCheckBox chkIncVat;
    private javax.swing.JCheckBox chkScale;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbDepartments;
    private javax.swing.JComboBox<String> cmbTax;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
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
    private javax.swing.JPanel panelCurrent;
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
    private javax.swing.JFormattedTextField txtPackSize;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtScale;
    private javax.swing.JTextField txtScaleName;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtShortName;
    private javax.swing.JTextField txtStock;
    // End of variables declaration//GEN-END:variables
}
