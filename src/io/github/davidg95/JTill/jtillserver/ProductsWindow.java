/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * Window which allows for adding, editing and deleting products,
 *
 * @author David
 */
public class ProductsWindow extends javax.swing.JInternalFrame {

    public static ProductsWindow frame;

    private final DataConnect dc;

    private Product product;

    private MyModel model;

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
     * Method to init the discounts, categories and taxes.
     */
    private void init() {
        try {
            List<Product> products = dc.getAllProducts();
            model = new MyModel(products);
            tableProducts.setModel(model);
            tableProducts.setSelectionModel(new ForcedListSelectionModel());
            tableProducts.getColumnModel().getColumn(2).setMaxWidth(60);
            tableProducts.getColumnModel().getColumn(3).setMaxWidth(60);
        } catch (SQLException | IOException ex) {
            showError(ex);
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
            jLabel9.setText("Cost (£):");
            txtPackSize.setText("");
            txtStock.setText("");
            txtMinStock.setText("");
            txtMaxStock.setText("");
            txtComments.setText("");
            txtScaleName.setText("");
            txtScale.setText("");
            txtOrder.setText("");
            txtCat.setText("");
            txtTax.setText("");
            txtDepartment.setText("");
            chkScale.setSelected(false);
            chkIncVat.setSelected(false);
            product = null;
        } else { //Fill the fields with the product.
            this.product = p;
            txtName.setText(product.getLongName());
            txtShortName.setText(product.getShortName());
            txtOrder.setText(product.getOrderCode() + "");
            if (product.isOpen()) { //Check if price is open.
                jLabel3.setText("Price Limit (£):");
                txtPrice.setText(product.getPriceLimit().setScale(2, 6) + "");
                jLabel9.setText("Cost %:");
                chkScale.setSelected(product.getScale() != -1);
                if (product.getScale() == -1) {
                    txtScaleName.setText("");
                    txtScale.setText("");
                } else {
                    txtScaleName.setText(product.getScaleName());
                    txtScale.setText(product.getScale() + "");
                }
                chkIncVat.setSelected(false);
                txtBarcode.setText(product.getBarcode());
                txtCostPrice.setText(product.getCostPercentage() + "");
                txtPackSize.setText("0");
                txtScaleName.setText(p.getScaleName());
                txtScale.setText(p.getScale() + "");
                txtStock.setText("");
                txtMinStock.setText("");
                txtMaxStock.setText("");
            } else {
                jLabel9.setText("Cost (£):");
                txtScaleName.setText("");
                txtScale.setText("");
                chkScale.setSelected(false);
                chkIncVat.setSelected(product.isPriceIncVat());
                txtBarcode.setText(product.getBarcode());
                txtPrice.setText(product.getPrice().setScale(2) + "");
                txtCostPrice.setText(product.getCostPrice().setScale(2) + "");
                txtPackSize.setValue(product.getPackSize());
                txtStock.setText(product.getStock() + "");
                txtMinStock.setText(product.getMinStockLevel() + "");
                txtMaxStock.setText(product.getMaxStockLevel() + "");
            }
            txtComments.setText(product.getComments());
            txtCat.setText(product.getCategory().toString());
            txtTax.setText(product.getTax().toString());
            txtDepartment.setText(product.getDepartment().toString());
        }
    }

    private void removeProduct(Product p) {
        int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove the following product?\n" + p, "Remove Product", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                model.removeProduct(p);
                setCurrentProduct(null);
                txtName.requestFocus();
                JOptionPane.showMessageDialog(this, "Product has been removed", "Remove Product", JOptionPane.INFORMATION_MESSAGE);
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

    /**
     * Method to show an error.
     *
     * @param e the exception to show.
     */
    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e, "Products", JOptionPane.ERROR_MESSAGE);
    }

    private class MyModel implements TableModel {

        private Product allProducts[];
        private List<Product> products;
        private final List<TableModelListener> listeners;

        public MyModel(List<Product> products) {
            allProducts = new Product[products.size()];
            allProducts = products.toArray(allProducts);
            this.products = products;
            this.listeners = new LinkedList<>();
        }

        public void addProduct(Product p) throws IOException, SQLException {
            dc.addProduct(p);
            products.add(p);
            alertAll();
        }

        public void removeProduct(Product p) throws IOException, ProductNotFoundException, SQLException {
            dc.removeProduct(p);
            products.remove(p);
            alertAll();
        }

        public Product getProduct(String barcode) {
            for (Product p : products) {
                if (p.getBarcode().equals(barcode)) {
                    return p;
                }
            }
            return null;
        }

        public Product getSelected() {
            int row = tableProducts.getSelectedRow();
            if (row == -1) {
                return null;
            }
            return products.get(row);
        }

        public void showAll() {
            products.clear();
            for (Product p : allProducts) {
                products.add(p);
            }
            alertAll();
        }

        public void filterCategory(Category c) throws IOException, SQLException, JTillException {
            products = dc.getProductsInCategory(c.getId());
            alertAll();
        }

        public void filterDepartment(Department d) throws IOException, SQLException, JTillException {
            products = dc.getProductsInDepartment(d.getId());
            alertAll();
        }

        public List<Product> getAll() {
            return products;
        }

        public void setList(List<Product> products) {
            this.products = products;
            alertAll();
        }

        @Override
        public int getRowCount() {
            return products.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int i) {
            switch (i) {
                case 0: {
                    return "Barcode";
                }
                case 1: {
                    return "Name";
                }
                case 2: {
                    return "Price";
                }
                case 3: {
                    return "Stock";
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return Object.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Product p = products.get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return p.getBarcode();
                }
                case 1: {
                    return p.getLongName();
                }
                case 2: {
                    if (p.isOpen()) {
                        return "Open";
                    } else {
                        return "£" + new DecimalFormat("0.00").format(p.getPrice());
                    }
                }
                case 3: {
                    if (p.isTrackStock()) {
                        return p.getStock();
                    } else {
                        return "N/A";
                    }
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        }

        private void alertAll() {
            for (TableModelListener l : listeners) {
                l.tableChanged(new TableModelEvent(this));
            }
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            listeners.add(l);
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            listeners.remove(l);
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
        btnSearch = new javax.swing.JButton();
        btnWasteStock = new javax.swing.JButton();
        btnReceiveStock = new javax.swing.JButton();
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
        txtShortName = new javax.swing.JTextField();
        txtMinStock = new javax.swing.JTextField();
        txtCostPrice = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        btnDepartments = new javax.swing.JButton();
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
        btnEditProduct = new javax.swing.JButton();
        btnRemoveProduct = new javax.swing.JButton();
        txtDepartment = new javax.swing.JTextField();
        btnCondiments = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        txtOrder = new javax.swing.JTextField();
        txtCat = new javax.swing.JTextField();
        txtTax = new javax.swing.JTextField();
        btnAdvanced = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setResizable(true);
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
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableProductsMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableProductsMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(tableProducts);
        if (tableProducts.getColumnModel().getColumnCount() > 0) {
            tableProducts.getColumnModel().getColumn(0).setMinWidth(40);
            tableProducts.getColumnModel().getColumn(0).setPreferredWidth(40);
            tableProducts.getColumnModel().getColumn(0).setMaxWidth(40);
            tableProducts.getColumnModel().getColumn(1).setResizable(false);
            tableProducts.getColumnModel().getColumn(2).setResizable(false);
            tableProducts.getColumnModel().getColumn(3).setMinWidth(60);
            tableProducts.getColumnModel().getColumn(3).setPreferredWidth(60);
            tableProducts.getColumnModel().getColumn(3).setMaxWidth(60);
            tableProducts.getColumnModel().getColumn(4).setMinWidth(60);
            tableProducts.getColumnModel().getColumn(4).setPreferredWidth(60);
            tableProducts.getColumnModel().getColumn(4).setMaxWidth(60);
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
        jScrollPane2.setViewportView(txtComments);

        txtBarcode.setEditable(false);

        jLabel10.setText("Min Stock Level:");

        txtName.setEditable(false);
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

        txtMaxStock.setEditable(false);

        jLabel12.setText("Department:");

        jLabel1.setText("Product Name:");

        txtShortName.setEditable(false);

        txtMinStock.setEditable(false);

        txtCostPrice.setEditable(false);

        jLabel3.setText("Price (£):");

        jLabel9.setText("Cost (£):");

        btnDepartments.setText("Departments");
        btnDepartments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDepartmentsActionPerformed(evt);
            }
        });

        jLabel8.setText("Tax Class:");

        txtStock.setEditable(false);

        jLabel2.setText("Barcode:");

        jLabel11.setText("Max Stock Level:");

        jLabel6.setText("Short Name:");

        jLabel7.setText("Category:");

        jLabel4.setText("Stock:");

        txtPrice.setEditable(false);

        jLabel14.setText("Pack Size:");

        txtPackSize.setEditable(false);
        txtPackSize.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getIntegerInstance())));

        jLabel15.setText("Scale Name:");

        txtScaleName.setEditable(false);

        jLabel16.setText("Scale Factor:");

        txtScale.setEditable(false);

        chkScale.setText("Use Scale");
        chkScale.setEnabled(false);
        chkScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkScaleActionPerformed(evt);
            }
        });

        chkIncVat.setText("Inc. VAT");
        chkIncVat.setEnabled(false);

        btnEditProduct.setText("Edit Product");
        btnEditProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditProductActionPerformed(evt);
            }
        });

        btnRemoveProduct.setText("Remove Product");
        btnRemoveProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveProductActionPerformed(evt);
            }
        });

        txtDepartment.setEditable(false);

        btnCondiments.setText("Condiments");
        btnCondiments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCondimentsActionPerformed(evt);
            }
        });

        jLabel17.setText("Order Code:");

        txtOrder.setEditable(false);

        txtCat.setEditable(false);

        txtTax.setEditable(false);

        javax.swing.GroupLayout panelCurrentLayout = new javax.swing.GroupLayout(panelCurrent);
        panelCurrent.setLayout(panelCurrentLayout);
        panelCurrentLayout.setHorizontalGroup(
            panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                    .addComponent(jLabel12)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelCurrentLayout.createSequentialGroup()
                        .addComponent(txtCostPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPackSize, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelCurrentLayout.createSequentialGroup()
                        .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkIncVat)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCondiments))
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
                    .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelCurrentLayout.createSequentialGroup()
                            .addComponent(txtShortName, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel17)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtOrder))
                        .addComponent(txtBarcode, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE))
                    .addGroup(panelCurrentLayout.createSequentialGroup()
                        .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtDepartment)
                            .addComponent(txtCat)
                            .addComponent(txtTax, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnShowCategorys, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnDepartments, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                            .addComponent(btnShowTax, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCurrentLayout.createSequentialGroup()
                .addContainerGap(33, Short.MAX_VALUE)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelCurrentLayout.createSequentialGroup()
                        .addComponent(btnEditProduct)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveProduct))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(jLabel6)
                    .addComponent(jLabel17)
                    .addComponent(txtOrder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(chkIncVat)
                    .addComponent(btnCondiments))
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
                    .addComponent(btnShowCategorys)
                    .addComponent(jLabel7)
                    .addComponent(txtCat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDepartments)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel8)
                        .addComponent(txtTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnShowTax))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(32, 32, 32)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnEditProduct)
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
                            .addGap(249, 249, 249))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(btnNewProduct)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnReceiveStock)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnWasteStock)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panelCurrent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnShowAll, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radBarcode)
                        .addGap(93, 93, 93)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAdvanced)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 196, Short.MAX_VALUE)
                        .addComponent(btnClose))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE)
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
                        .addComponent(btnCSV)
                        .addGap(0, 40, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(jLabel13)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radName)
                    .addComponent(radBarcode)
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
        model.showAll();
    }//GEN-LAST:event_btnShowAllActionPerformed

    private void btnRemoveProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveProductActionPerformed
        Product p = model.getSelected();
        if (p == null) {
            return;
        }
        removeProduct(p);
    }//GEN-LAST:event_btnRemoveProductActionPerformed

    private void btnEditProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditProductActionPerformed
        ProductEntryDialog.showDialog(this, product);
    }//GEN-LAST:event_btnEditProductActionPerformed

    private void btnNewProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewProductActionPerformed
        ProductEntryDialog.showDialog(this);
        setCurrentProduct(null);
    }//GEN-LAST:event_btnNewProductActionPerformed

    private void tableProductsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableProductsMousePressed
        Product p = model.getSelected();
        setCurrentProduct(p);
    }//GEN-LAST:event_tableProductsMousePressed

    private void btnShowCategorysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowCategorysActionPerformed
        CategorysWindow.showCategoryWindow();
    }//GEN-LAST:event_btnShowCategorysActionPerformed

    private void btnShowTaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowTaxActionPerformed
        TaxWindow.showTaxWindow();
    }//GEN-LAST:event_btnShowTaxActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        btnSearch.doClick();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        int option;
        String terms = txtSearch.getText();

        if (terms.isEmpty()) {
            model.showAll();
            return;
        }

        if (radBarcode.isSelected()) {
            option = 1;
        } else {
            option = 2;
        }
        final List<Product> newList = new ArrayList<>();
        switch (option) {
            case 1:
                for (Product p : model.getAll()) {
                    if (p.getBarcode().equals(terms)) {
                        newList.add(p);
                    }
                }
                break;
            default:
                model.getAll().stream().filter((p) -> (p.getLongName().toLowerCase().contains(terms.toLowerCase()) || p.getShortName().toLowerCase().contains(terms.toLowerCase()))).forEachOrdered((p) -> {
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
            model.setList(newList);
            if (newList.size() == 1) {
                setCurrentProduct(newList.get(0));
            }
        }
        txtSearch.requestFocus();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnReceiveStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReceiveStockActionPerformed
        ReceiveItemsWindow.showWindow();
    }//GEN-LAST:event_btnReceiveStockActionPerformed

    private void btnWasteStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWasteStockActionPerformed
        WasteStockWindow.showWindow();
    }//GEN-LAST:event_btnWasteStockActionPerformed

    private void btnCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCSVActionPerformed
        final JFileChooser chooser = new JFileChooser();
        chooser.setApproveButtonText("Export CSV");
        chooser.setDialogTitle("Export CSV File");
        final int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            try {
                final PrintWriter pw = new PrintWriter(file);

                for (Product p : model.getAll()) {
                    pw.println(p.getLongName() + ","
                            + p.getCategory().getId() + ","
                            + p.getCostPrice() + ","
                            + p.getMaxStockLevel() + ","
                            + p.getMinStockLevel() + ","
                            + p.getPrice() + ","
                            + p.getStock() + ","
                            + p.getTax().getId());
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

    private void btnDepartmentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDepartmentsActionPerformed
        DepartmentsWindow.showWindow();
    }//GEN-LAST:event_btnDepartmentsActionPerformed

    private void btnAdvancedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdvancedActionPerformed
        final JLabel catLabel = new JLabel("Filter Category: ");
        final JButton catButton = new JButton("Select Category");

        final JLabel depLabel = new JLabel("Filter Department");
        final JButton depButton = new JButton("Select Department");

        JDialog dialog = new JDialog();

        depButton.addActionListener(event -> {
            try {
                Department d = (Department) DCSelectDialog.showDialog(this, DCSelectDialog.DEPARTMENT_SELECT);
                if (d == null) {
                    return;
                }
                model.filterDepartment(d);
                dialog.setVisible(false);
            } catch (JTillException | IOException | SQLException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        catButton.addActionListener(event -> {
            try {
                Category c = (Category) DCSelectDialog.showDialog(this, DCSelectDialog.CATEGORY_SELECT);
                if (c == null) {
                    return;
                }
                model.filterCategory(c);
                dialog.setVisible(false);
            } catch (IOException | SQLException | JTillException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        final JPanel panel = new JPanel();
        panel.add(catLabel);
        panel.add(catButton);
        panel.add(depLabel);
        panel.add(depButton);

        dialog.add(panel);
        dialog.setModal(closable);
        dialog.pack();
        dialog.setTitle("Filter");
        dialog.setIconImage(GUI.icon);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }//GEN-LAST:event_btnAdvancedActionPerformed

    private void chkScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkScaleActionPerformed
        txtScale.setEnabled(chkScale.isSelected());
        txtScaleName.setEnabled(chkScale.isSelected());
    }//GEN-LAST:event_chkScaleActionPerformed

    private void tableProductsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableProductsMouseClicked
        Product p = model.getSelected();
        if (p == null) {
            return;
        }
        if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem enquiry = new JMenuItem("Enquiry");
            JMenuItem edit = new JMenuItem("Edit");
            JMenuItem remove = new JMenuItem("Remove");
            final Font boldFont = new Font(enquiry.getFont().getFontName(), Font.BOLD, enquiry.getFont().getSize());
            enquiry.setFont(boldFont);
            enquiry.addActionListener((ActionEvent e) -> {
                ProductEnquiry.showWindow(p);
            });
            remove.addActionListener((ActionEvent e) -> {
                removeProduct(p);
            });
            edit.addActionListener((event) -> {
                ProductEntryDialog.showDialog(this, p);
            });
            menu.add(enquiry);
            menu.add(edit);
            menu.addSeparator();
            menu.add(remove);
            menu.show(tableProducts, evt.getX(), evt.getY());
        } else {
            if (evt.getClickCount() == 2) {
                ProductEnquiry.showWindow(p);
            }
        }
    }//GEN-LAST:event_tableProductsMouseClicked

    private void btnCondimentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCondimentsActionPerformed
        CondimentWindow.showWindow(product);
    }//GEN-LAST:event_btnCondimentsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdvanced;
    private javax.swing.JButton btnCSV;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnCondiments;
    private javax.swing.JButton btnDepartments;
    private javax.swing.JButton btnEditProduct;
    private javax.swing.JButton btnNewProduct;
    private javax.swing.JButton btnReceiveStock;
    private javax.swing.JButton btnRemoveProduct;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnShowAll;
    private javax.swing.JButton btnShowCategorys;
    private javax.swing.JButton btnShowTax;
    private javax.swing.JButton btnWasteStock;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox chkIncVat;
    private javax.swing.JCheckBox chkScale;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
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
    private javax.swing.JRadioButton radName;
    private javax.swing.JTable tableProducts;
    private javax.swing.JTextField txtBarcode;
    private javax.swing.JTextField txtCat;
    private javax.swing.JTextArea txtComments;
    private javax.swing.JTextField txtCostPrice;
    private javax.swing.JTextField txtDepartment;
    private javax.swing.JTextField txtMaxStock;
    private javax.swing.JTextField txtMinStock;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtOrder;
    private javax.swing.JFormattedTextField txtPackSize;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtScale;
    private javax.swing.JTextField txtScaleName;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtShortName;
    private javax.swing.JTextField txtStock;
    private javax.swing.JTextField txtTax;
    // End of variables declaration//GEN-END:variables
}
