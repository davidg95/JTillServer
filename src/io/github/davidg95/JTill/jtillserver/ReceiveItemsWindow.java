/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTable.PrintMode;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author david
 */
public final class ReceiveItemsWindow extends javax.swing.JInternalFrame {

    private final DataConnect dc;
    private final List<Product> products;
    private final DefaultTableModel model;
    private final DefaultComboBoxModel cmbModel;

    /**
     * Creates new form ReceiveItemsWindow
     *
     * @param dc the data connection.
     * @param icon the frame icon.
     */
    public ReceiveItemsWindow(DataConnect dc, Image icon) {
        this.dc = dc;
        this.products = new ArrayList<>();
        initComponents();
        setTitle("Receive Stock");
//        setIconImage(icon);
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(icon));
        model = (DefaultTableModel) tblProducts.getModel();
        tblProducts.setModel(model);
        model.setRowCount(0);
        cmbModel = (DefaultComboBoxModel) cmbSuppliers.getModel();
        cmbSuppliers.setModel(cmbModel);
        init();
    }

    public static void showWindow(DataConnect dc, Image icon) {
        ReceiveItemsWindow window = new ReceiveItemsWindow(dc, icon);
        GUI.gui.internal.add(window);
        try {
            List<Supplier> suppliers = dc.getAllSuppliers();
            if (suppliers.isEmpty()) {
                JOptionPane.showMessageDialog(window, "You must set up at least one supplier before receiving stock. Go to Setup -> Edit Suppliers to do this", "No Suppliers Set", JOptionPane.WARNING_MESSAGE);
                return;
            }
            window.setVisible(true);
            window.setIcon(false);
            window.setSelected(true);
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(window, "Error connecting to database", "Receive Stock", JOptionPane.ERROR_MESSAGE);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(ReceiveItemsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void init() {
        cmbModel.removeAllElements();
        try {
            List<Supplier> suppliers = dc.getAllSuppliers();
            suppliers.forEach((s) -> {
                cmbModel.addElement(s);
            });
        } catch (IOException | SQLException ex) {
            Logger.getLogger(WasteStockWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        InputMap im = tblProducts.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = tblProducts.getActionMap();

        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);

        im.put(enterKey, "Action.enter");
        am.put("Action.enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                final int index = tblProducts.getSelectedRow();
                final Product p = products.get(index);
                if (index == -1) {
                    return;
                }
                if (JOptionPane.showInternalConfirmDialog(GUI.gui.internal, "Are you sure you want to remove this item?\n" + p, "Stock Item", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    model.removeRow(index);
                    products.remove(index);
                }
                updateTable();
            }
        });
    }

    private void updateTable() {
        model.setRowCount(0);
        BigDecimal val = BigDecimal.ZERO;
        val.setScale(2);
        for (Product pr : products) {
            try {
                final Plu p = dc.getPluByProduct(pr.getId());
                model.addRow(new Object[]{pr.getId(), pr.getLongName(), p.getCode(), pr.getStock()});
                val = val.add(pr.getCostPrice().multiply(new BigDecimal(pr.getStock())));
            } catch (IOException | JTillException ex) {
                Logger.getLogger(ReceiveItemsWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (val == BigDecimal.ZERO) {
            lblValue.setText("Total Value: £0.00");
        } else {
            lblValue.setText("Total Value: £" + new DecimalFormat("#.00").format(val));
        }
        if (products.isEmpty()) {
            btnReceive.setEnabled(false);
        } else {
            btnReceive.setEnabled(true);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        tblProducts = new javax.swing.JTable();
        btnReceive = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        btnAddProduct = new javax.swing.JButton();
        btnAddCSV = new javax.swing.JButton();
        lblValue = new javax.swing.JLabel();
        cmbSuppliers = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();

        tblProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

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

        btnReceive.setText("Receive");
        btnReceive.setEnabled(false);
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

        btnAddCSV.setText("Add CSV File");
        btnAddCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddCSVActionPerformed(evt);
            }
        });

        lblValue.setText("Total Value: £0.00");

        cmbSuppliers.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setText("Supplier:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 604, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lblValue)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAddCSV)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddProduct)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnReceive)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbSuppliers, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbSuppliers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReceive)
                    .addComponent(btnClose)
                    .addComponent(btnAddProduct)
                    .addComponent(btnAddCSV)
                    .addComponent(lblValue))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnReceiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReceiveActionPerformed
        if (products.isEmpty()) {
            return;
        }
        products.forEach((p) -> {
            try {
                Product product = dc.getProduct(p.getId());
                product.addStock(p.getStock());
                p = dc.updateProduct(product);
                dc.addReceivedItem(new ReceivedItem(p.getId(), p.getStock(), p.getCostPrice()));
            } catch (IOException | ProductNotFoundException | SQLException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        lblValue.setText("Total: £0.00");
        model.setRowCount(0);
        products.clear();
        btnReceive.setEnabled(false);
        JOptionPane.showMessageDialog(this, "All items have been received", "Received", JOptionPane.INFORMATION_MESSAGE);
        if (JOptionPane.showConfirmDialog(this, "Do you want to print the report?", "Print", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                BigDecimal val = BigDecimal.ZERO;
                for (Product p : products) {
                    val = val.add(p.getPrice());
                }
                MessageFormat header = new MessageFormat("Receive Stock " + new Date());
                MessageFormat footer = new MessageFormat("Page{0,number,integer}");
                tblProducts.print(PrintMode.FIT_WIDTH, header, footer);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnReceiveActionPerformed

    private void btnAddProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProductActionPerformed
        Product product = ProductSelectDialog.showDialog(this, false);

        if (product == null) {
            return;
        }

        product = (Product) product.clone();

        String str = JOptionPane.showInternalInputDialog(GUI.gui.internal, "Enter amount to receive", "Receive Stock", JOptionPane.INFORMATION_MESSAGE);

        if (str == null || str.isEmpty()) {
            return;
        }

        if (Utilities.isNumber(str)) {
            int amount = Integer.parseInt(str);
            if (amount <= 0) {
                JOptionPane.showInternalMessageDialog(GUI.gui.internal, "Value must be greater than zero", "Receive Items", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (product.getStock() + amount > product.getMaxStockLevel() && product.getMaxStockLevel() != 0) {
                JOptionPane.showMessageDialog(ReceiveItemsWindow.this, "Warning- this will take the product stock level higher than the maximum stock level defined for this product", "Stock", JOptionPane.WARNING_MESSAGE);
            }
            if (amount == 0) {
                return;
            }
            product.setStock(amount);

            products.add(product);
            updateTable();
        } else {
            JOptionPane.showInternalMessageDialog(GUI.gui.internal, "You must enter a number", "Receive Stock", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAddProductActionPerformed

    private void tblProductsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblProductsMouseClicked
        final int row = tblProducts.getSelectedRow();
        final Product product = products.get(row);
        if (evt.getClickCount() == 2) {
            if (evt.getClickCount() == 2) {
                String input = JOptionPane.showInternalInputDialog(GUI.gui.internal, "Enter new quantity", "Receive Items", JOptionPane.PLAIN_MESSAGE);
                if (!Utilities.isNumber(input)) {
                    JOptionPane.showInternalMessageDialog(GUI.gui.internal, "A number must be entered", "Receive Items", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int val = Integer.parseInt(input);
                if (val > 0) {
                    product.setStock(val);
                    updateTable();
                } else {
                    JOptionPane.showInternalMessageDialog(GUI.gui.internal, "Must be a value greater than zero", "Receive Items", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
        if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem it = new JMenuItem("Change Quantity");
            JMenuItem item = new JMenuItem("Remove");
            it.addActionListener((ActionEvent e) -> {
                if (evt.getClickCount() == 2) {
                    String input = JOptionPane.showInternalInputDialog(GUI.gui.internal, "Enter new quantity", "Receive Items", JOptionPane.PLAIN_MESSAGE);
                    if (!Utilities.isNumber(input)) {
                        JOptionPane.showInternalMessageDialog(GUI.gui.internal, "A number must be entered", "Receive Items", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    int val = Integer.parseInt(input);
                    if (val > 0) {
                        product.setStock(val);
                        updateTable();
                    } else {
                        JOptionPane.showInternalMessageDialog(GUI.gui.internal, "Must be a value greater than zero", "Receive Items", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
            item.addActionListener((ActionEvent e) -> {
                if (JOptionPane.showInternalConfirmDialog(GUI.gui.internal, "Remove this item?", "Remove Item", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    products.remove(tblProducts.getSelectedRow());
                    updateTable();
                }
            });
            menu.add(it);
            menu.add(item);
            menu.show(tblProducts, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tblProductsMouseClicked

    private void btnAddCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCSVActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Receive File");
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));

                while (true) {
                    String line = br.readLine();

                    if (line == null) {
                        break;
                    }

                    String[] items = line.split(",");

                    if (items.length != 2) {
                        JOptionPane.showInternalMessageDialog(GUI.gui.internal, "File is not recognised", "Add CSV", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String barcode = items[0];
                    int quantity = Integer.parseInt(items[1]);

                    Product product;
                    try {
                        product = dc.getProductByBarcode(barcode);

                        product.setStock(quantity);

                        products.add(product);
                        try {
                            final Plu plu = dc.getPluByProduct(product.getId());
                            model.addRow(new Object[]{product.getId(), product.getName(), plu.getCode(), product.getStock()});
                        } catch (JTillException ex) {
                            JOptionPane.showMessageDialog(this, ex);
                        }
                    } catch (ProductNotFoundException ex) {
                        if (JOptionPane.showInternalConfirmDialog(GUI.gui.internal, "Barcode not found, create new product?", "Not found", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            Plu p = new Plu(barcode, 0);
                            product = ProductDialog.showNewProductDialog(this, dc, p, quantity);
                            p.setProductID(product.getId());
                            JOptionPane.showInternalMessageDialog(GUI.gui.internal, product.getLongName() + " has now been added to the system with given stock level, there is no need to receive it here.", "Added", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                JOptionPane.showInternalMessageDialog(GUI.gui.internal, ex, "File Not Found", JOptionPane.ERROR_MESSAGE);
            } catch (IOException | SQLException ex) {
                JOptionPane.showInternalMessageDialog(GUI.gui.internal, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnAddCSVActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCSV;
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnReceive;
    private javax.swing.JComboBox<String> cmbSuppliers;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblValue;
    private javax.swing.JTable tblProducts;
    // End of variables declaration//GEN-END:variables
}
