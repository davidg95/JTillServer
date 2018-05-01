/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import com.sun.glass.events.KeyEvent;
import io.github.davidg95.JTill.jtill.*;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private final JTill jtill;

    private Product product;

    private MyModel model;

    /**
     * Creates new form ProductsWindow
     */
    public ProductsWindow(JTill jtill) {
        this.jtill = jtill;
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
    public static void showProductsListWindow(JTill jtill) {
        if (frame != null && !frame.isClosed && frame.isVisible()) {
            frame.toFront();
        } else {
            frame = new ProductsWindow(jtill);
            GUI.gui.internal.add(frame);
            frame.setCurrentProduct(null);
            try {
                frame.setIcon(false);
                frame.setSelected(true);
                frame.setVisible(true);
            } catch (PropertyVetoException ex) {
                Logger.getLogger(SettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            List<Product> products = jtill.getDataConnection().getAllProducts();
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
            txtBarcode.setText("");
            txtPrice.setText("");
            txtCostPrice.setText("");
            jLabel9.setText("Cost (£):");
            txtPackSize.setText("");
            txtStock.setText("");
            txtComments.setText("");
            txtIngredients.setText("");
            txtCat.setText("");
            txtTax.setText("");
            txtDepartment.setText("");
            product = null;
        } else { //Fill the fields with the product.
            this.product = p;
            txtName.setText(product.getLongName());
            if (product.isOpen()) { //Check if price is open.
                jLabel3.setText("Price Limit (£):");
                txtPrice.setText(product.getPriceLimit().setScale(2, 6) + "");
                jLabel9.setText("Cost %:");
                if (product.getScale() == -1) {
                } else {
                }
                txtBarcode.setText(product.getBarcode());
                txtCostPrice.setText(product.getCostPercentage() + "");
                txtPackSize.setText("0");
                txtStock.setText("");
            } else {
                jLabel9.setText("Cost (£):");
                txtBarcode.setText(product.getBarcode());
                txtPrice.setText(product.getPrice().setScale(2) + "");
                txtCostPrice.setText(product.getCostPrice().setScale(2) + "");
                txtPackSize.setValue(product.getPackSize());
                txtStock.setText(product.getStock() + "");
            }
            txtComments.setText(product.getComments());
            txtIngredients.setText(product.getIngredients());
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
            jtill.getDataConnection().addProduct(p);
            products.add(p);
            alertAll();
        }

        public void removeProduct(Product p) throws IOException, ProductNotFoundException, SQLException {
            jtill.getDataConnection().removeProduct(p);
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
            products = jtill.getDataConnection().getProductsInCategory(c.getId());
            alertAll();
        }

        public void filterDepartment(Department d) throws IOException, SQLException, JTillException {
            products = jtill.getDataConnection().getProductsInDepartment(d.getId());
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
        btnEditProduct = new javax.swing.JButton();
        btnRemoveProduct = new javax.swing.JButton();
        btnCondiments = new javax.swing.JButton();
        btnEnquiry = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        txtName = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtCat = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtBarcode = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtDepartment = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        txtPackSize = new javax.swing.JFormattedTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtCostPrice = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        txtWithTax = new javax.swing.JTextField();
        txtPrice = new javax.swing.JTextField();
        txtUnitPrice = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        btnShowTax = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        txtTax = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtStock = new javax.swing.JTextField();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtComments = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtIngredients = new javax.swing.JTextArea();
        btnAdvanced = new javax.swing.JButton();

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
        tableProducts.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                tableProductsMouseWheelMoved(evt);
            }
        });
        tableProducts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableProductsMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableProductsMousePressed(evt);
            }
        });
        tableProducts.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tableProductsKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tableProductsKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tableProductsKeyTyped(evt);
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

        btnCondiments.setText("Condiments");
        btnCondiments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCondimentsActionPerformed(evt);
            }
        });

        btnEnquiry.setText("Enquiry");
        btnEnquiry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEnquiryActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Basic Settings"));

        txtName.setEditable(false);

        jLabel12.setText("Department:");

        txtCat.setEditable(false);

        jLabel7.setText("Category:");

        txtBarcode.setEditable(false);

        jLabel2.setText("Barcode:");

        jLabel1.setText("Product Name:");

        txtDepartment.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtCat, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtDepartment))
                            .addComponent(txtName)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(32, 32, 32)
                        .addComponent(txtBarcode)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtCat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(txtDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Price"));

        jLabel14.setText("Pack Size:");

        txtPackSize.setEditable(false);
        txtPackSize.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getIntegerInstance())));

        jLabel6.setText("Unit:");

        jLabel3.setText("Price (£):");

        txtCostPrice.setEditable(false);

        jLabel18.setText("With Tax");

        txtWithTax.setEditable(false);

        txtPrice.setEditable(false);

        txtUnitPrice.setEditable(false);

        jLabel9.setText("Cost (£):");

        btnShowTax.setText("Tax");
        btnShowTax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowTaxActionPerformed(evt);
            }
        });

        jLabel8.setText("Tax Class:");

        txtTax.setEditable(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtCostPrice, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                            .addComponent(txtPrice))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtWithTax, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtPackSize, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtUnitPrice))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTax)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnShowTax)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel18)
                    .addComponent(txtWithTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtCostPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(txtPackSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtUnitPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnShowTax))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Stock Options"));

        jLabel4.setText("Stock:");

        txtStock.setEditable(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtStock, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        txtComments.setEditable(false);
        txtComments.setColumns(20);
        txtComments.setRows(5);
        jScrollPane2.setViewportView(txtComments);

        jTabbedPane1.addTab("Comments", jScrollPane2);

        txtIngredients.setEditable(false);
        txtIngredients.setColumns(20);
        txtIngredients.setRows(5);
        jScrollPane3.setViewportView(txtIngredients);

        jTabbedPane1.addTab("Ingredients", jScrollPane3);

        javax.swing.GroupLayout panelCurrentLayout = new javax.swing.GroupLayout(panelCurrent);
        panelCurrent.setLayout(panelCurrentLayout);
        panelCurrentLayout.setHorizontalGroup(
            panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCurrentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelCurrentLayout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(panelCurrentLayout.createSequentialGroup()
                        .addComponent(btnEditProduct)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEnquiry)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCondiments)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveProduct)
                        .addGap(105, 105, 105))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCurrentLayout.createSequentialGroup()
                        .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTabbedPane1))
                        .addContainerGap())
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        panelCurrentLayout.setVerticalGroup(
            panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCurrentLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurrentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRemoveProduct)
                    .addComponent(btnEditProduct)
                    .addComponent(btnEnquiry)
                    .addComponent(btnCondiments)))
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
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 696, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelCurrent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnNewProduct)
                            .addComponent(btnReceiveStock)
                            .addComponent(btnWasteStock))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnShowAll)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCSV)))
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
        ProductEntryDialog.showDialog(this, jtill, product);
    }//GEN-LAST:event_btnEditProductActionPerformed

    private void btnNewProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewProductActionPerformed
        ProductEntryDialog.showDialog(this, jtill);
        setCurrentProduct(null);
    }//GEN-LAST:event_btnNewProductActionPerformed

    private void tableProductsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableProductsMousePressed
        Product p = model.getSelected();
        setCurrentProduct(p);
    }//GEN-LAST:event_tableProductsMousePressed

    private void btnShowTaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowTaxActionPerformed
        TaxWindow.showTaxWindow(jtill);
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
        ReceiveItemsWindow.showWindow(jtill);
    }//GEN-LAST:event_btnReceiveStockActionPerformed

    private void btnWasteStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWasteStockActionPerformed
        WasteStockWindow.showWindow(jtill);
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
                            + p.getTax().getName());
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

    private void tableProductsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableProductsMouseClicked
        Product p = model.getSelected();
        if (p == null) {
            return;
        }
        if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem edit = new JMenuItem("Edit");
            JMenuItem enquiry = new JMenuItem("Enquiry");
            JMenuItem condiments = new JMenuItem("Condiemnts");
            JMenuItem remove = new JMenuItem("Remove");
            final Font boldFont = new Font(enquiry.getFont().getFontName(), Font.BOLD, enquiry.getFont().getSize());
            edit.setFont(boldFont);
            edit.addActionListener((event) -> {
                ProductEntryDialog.showDialog(this, jtill, p);
            });
            enquiry.addActionListener((ActionEvent e) -> {
                ProductEnquiry.showWindow(p, jtill);
            });
            condiments.addActionListener((ActionEvent e) -> {
                CondimentWindow.showWindow(jtill, p);
            });
            remove.addActionListener((ActionEvent e) -> {
                removeProduct(p);
            });
            menu.add(edit);
            menu.add(enquiry);
            menu.add(condiments);
            menu.addSeparator();
            menu.add(remove);
            menu.show(tableProducts, evt.getX(), evt.getY());
        } else {
            if (evt.getClickCount() == 2) {
                ProductEntryDialog.showDialog(this, jtill, p);
            }
        }
    }//GEN-LAST:event_tableProductsMouseClicked

    private void btnCondimentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCondimentsActionPerformed
        CondimentWindow.showWindow(jtill, product);
    }//GEN-LAST:event_btnCondimentsActionPerformed

    private void btnEnquiryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnquiryActionPerformed
        ProductEnquiry.showWindow(product, jtill);
    }//GEN-LAST:event_btnEnquiryActionPerformed

    private void tableProductsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableProductsKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            ProductEntryDialog.showDialog(this, jtill, product);
        }
    }//GEN-LAST:event_tableProductsKeyPressed

    private void tableProductsKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableProductsKeyTyped

    }//GEN-LAST:event_tableProductsKeyTyped

    private void tableProductsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableProductsKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_UP || evt.getKeyCode() == KeyEvent.VK_DOWN) {
            Product p = model.getSelected();
            setCurrentProduct(p);
        }
    }//GEN-LAST:event_tableProductsKeyReleased

    private void tableProductsMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_tableProductsMouseWheelMoved
        int row = tableProducts.getSelectedRow();
        if (evt.getWheelRotation() < 0) { //Up
            if (row == 0) {
                return;
            }
            tableProducts.getSelectionModel().setSelectionInterval(row - 1, row - 1);
        } else if (evt.getWheelRotation() > 0) { //Down
            if (tableProducts.getRowCount() - 1 == row) {
                return;
            }
            tableProducts.getSelectionModel().setSelectionInterval(row + 1, row + 1);

        }
        Product p = model.getSelected();
        setCurrentProduct(p);
    }//GEN-LAST:event_tableProductsMouseWheelMoved

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdvanced;
    private javax.swing.JButton btnCSV;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnCondiments;
    private javax.swing.JButton btnEditProduct;
    private javax.swing.JButton btnEnquiry;
    private javax.swing.JButton btnNewProduct;
    private javax.swing.JButton btnReceiveStock;
    private javax.swing.JButton btnRemoveProduct;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnShowAll;
    private javax.swing.JButton btnShowTax;
    private javax.swing.JButton btnWasteStock;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel panelCurrent;
    private javax.swing.JRadioButton radBarcode;
    private javax.swing.JRadioButton radName;
    private javax.swing.JTable tableProducts;
    private javax.swing.JTextField txtBarcode;
    private javax.swing.JTextField txtCat;
    private javax.swing.JTextArea txtComments;
    private javax.swing.JTextField txtCostPrice;
    private javax.swing.JTextField txtDepartment;
    private javax.swing.JTextArea txtIngredients;
    private javax.swing.JTextField txtName;
    private javax.swing.JFormattedTextField txtPackSize;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtStock;
    private javax.swing.JTextField txtTax;
    private javax.swing.JTextField txtUnitPrice;
    private javax.swing.JTextField txtWithTax;
    // End of variables declaration//GEN-END:variables
}
