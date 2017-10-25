/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Font;
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
import java.util.LinkedList;
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
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author david
 */
public final class ReceiveItemsWindow extends javax.swing.JInternalFrame {

    private final DataConnect dc;
    private ReceivedReport rr;
    private List<ReceivedItem> products;
    private final DefaultTableModel model;
    private final DefaultComboBoxModel cmbModel;

    private boolean viewMode = false;

    /**
     * Creates new form ReceiveItemsWindow
     */
    public ReceiveItemsWindow() {
        this.dc = GUI.gui.dc;
        this.products = new ArrayList<>();
        initComponents();
        setTitle("Receive Stock");
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        model = (DefaultTableModel) tblProducts.getModel();
        tblProducts.setModel(model);
        model.setRowCount(0);
        cmbModel = new DefaultComboBoxModel();
        cmbSuppliers.setModel(cmbModel);
        init();
        txtBarcode.requestFocus();
    }

    public ReceiveItemsWindow(ReceivedReport rr) {
        this.dc = GUI.gui.dc;
        this.rr = rr;
        this.products = new LinkedList<>();
        initComponents();
        setTitle(rr.getSupplier().getName() + " - " + rr.getInvoiceId());
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        model = (DefaultTableModel) tblProducts.getModel();
        tblProducts.setModel(model);
        model.setRowCount(0);
        cmbModel = (DefaultComboBoxModel) cmbSuppliers.getModel();
        cmbSuppliers.setModel(cmbModel);
        txtBarcode.setEnabled(false);
        setReport();
    }

    public static void showWindow() {
        ReceiveItemsWindow window = new ReceiveItemsWindow();
        GUI.gui.internal.add(window);
        try {
            List<Supplier> suppliers = GUI.gui.dc.getAllSuppliers();
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

    public static void showWindow(ReceivedReport rr) {
        ReceiveItemsWindow window = new ReceiveItemsWindow(rr);
        GUI.gui.internal.add(window);
        try {
            window.setVisible(true);
            window.setIcon(false);
            window.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(ReceiveItemsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setReport() {
        tblProducts.setSelectionModel(new ForcedListSelectionModel());
        products = rr.getItems();
        updateTable();
        txtInvoice.setText(rr.getInvoiceId());
        cmbModel.setSelectedItem(rr.getSupplier());
        chkPaid.setSelected(rr.isPaid());
        setViewMode();
    }

    private void init() {
        tblProducts.setSelectionModel(new ForcedListSelectionModel());
        this.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                if (!products.isEmpty()) {
                    int res = JOptionPane.showInternalConfirmDialog(ReceiveItemsWindow.this, "Do you want to save the current report?", "Save", JOptionPane.YES_NO_OPTION);
                    if (res == JOptionPane.YES_OPTION) {
                        GUI.gui.savedReports.put("REC", products);
                    }
                }
            }
        });
        if (GUI.gui.savedReports.containsKey("REC")) {
            products = GUI.gui.savedReports.get("REC");
            updateTable();
            GUI.gui.savedReports.remove("REC");
        }
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
                if (index == -1) {
                    return;
                }
                final ReceivedItem p = products.get(index);
                removeItem(p);
            }
        });
    }

    private void removeItem(ReceivedItem i) {
        if (JOptionPane.showInternalConfirmDialog(ReceiveItemsWindow.this, "Are you sure you want to remove this item?\n" + i, "Stock Item", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            products.remove(i);
        }
        updateTable();
    }

    private void updateTable() {
        model.setRowCount(0);
        BigDecimal val = BigDecimal.ZERO;
        val.setScale(2);
        for (ReceivedItem pr : products) {
            final Product p = pr.getProduct();
            model.addRow(new Object[]{p.getId(), p.getLongName(), pr.getQuantity()});
            val = val.add(pr.getPrice());
        }
        if (val == BigDecimal.ZERO) {
            lblValue.setText("Total Value: £0.00");
        } else {
            lblValue.setText("Total Value: £" + new DecimalFormat("0.00").format(val));
        }
        if (products.isEmpty()) {
            btnReceive.setEnabled(false);
        } else {
            btnReceive.setEnabled(true);
        }
    }

    private void setViewMode() {
        btnAddProduct.setEnabled(false);
        btnReceive.setEnabled(false);
        cmbSuppliers.setEnabled(false);
        txtInvoice.setEditable(false);
        viewMode = true;
    }

    private void addCSV() {
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
                        JOptionPane.showInternalMessageDialog(ReceiveItemsWindow.this, "File is not recognised", "Add CSV", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String barcode = items[0];
                    int quantity = Integer.parseInt(items[1]);

                    Product product;
                    try {
                        product = dc.getProductByBarcode(barcode);

                        product.setStock(quantity);

                        products.add(new ReceivedItem(product, quantity));
                        model.addRow(new Object[]{product.getId(), product.getName(), product.getBarcode(), product.getStock()});
                    } catch (ProductNotFoundException ex) {
                        if (JOptionPane.showInternalConfirmDialog(ReceiveItemsWindow.this, "Barcode not found, create new product?", "Not found", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            JOptionPane.showMessageDialog(this, "Feature not yet implemented, must add manually", "Not Found", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                JOptionPane.showInternalMessageDialog(ReceiveItemsWindow.this, ex, "File Not Found", JOptionPane.ERROR_MESSAGE);
            } catch (IOException | SQLException ex) {
                JOptionPane.showInternalMessageDialog(ReceiveItemsWindow.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
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
        lblValue = new javax.swing.JLabel();
        cmbSuppliers = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtInvoice = new javax.swing.JTextField();
        chkPaid = new javax.swing.JCheckBox();
        txtBarcode = new javax.swing.JTextField();

        setResizable(true);

        tblProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Product", "Qty."
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblProducts.getTableHeader().setReorderingAllowed(false);
        tblProducts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblProductsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblProducts);
        if (tblProducts.getColumnModel().getColumnCount() > 0) {
            tblProducts.getColumnModel().getColumn(0).setMinWidth(40);
            tblProducts.getColumnModel().getColumn(0).setPreferredWidth(40);
            tblProducts.getColumnModel().getColumn(0).setMaxWidth(40);
            tblProducts.getColumnModel().getColumn(1).setResizable(false);
            tblProducts.getColumnModel().getColumn(2).setMinWidth(40);
            tblProducts.getColumnModel().getColumn(2).setPreferredWidth(40);
            tblProducts.getColumnModel().getColumn(2).setMaxWidth(40);
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

        lblValue.setText("Total Value: £0.00");

        cmbSuppliers.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setText("Supplier:");

        jLabel2.setText("Invoice No.:");

        chkPaid.setText("Paid");
        chkPaid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkPaidActionPerformed(evt);
            }
        });

        txtBarcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBarcodeActionPerformed(evt);
            }
        });

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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkPaid)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddProduct)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnReceive)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(txtInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReceive)
                    .addComponent(btnClose)
                    .addComponent(btnAddProduct)
                    .addComponent(lblValue)
                    .addComponent(chkPaid)
                    .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        try {
            super.setClosed(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(ReceiveItemsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnReceiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReceiveActionPerformed
        if (products.isEmpty()) {
            return;
        }
        if (txtInvoice.getText().isEmpty()) {
            JOptionPane.showInternalMessageDialog(this, "You must enter an invoice number", "Receive", JOptionPane.WARNING_MESSAGE);
            txtInvoice.requestFocus();
            return;
        }
        Supplier supplier = (Supplier) cmbSuppliers.getSelectedItem();
        ReceivedReport report = new ReceivedReport(txtInvoice.getText(), supplier);
        products.forEach((p) -> {
            try {
                Product product = p.getProduct();
                product.addStock(p.getQuantity());
                dc.updateProduct(product);
            } catch (IOException | ProductNotFoundException | SQLException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        report.setPaid(chkPaid.isSelected());
        report.setItems(products);
        try {
            dc.addReceivedReport(report);
            lblValue.setText("Total: £0.00");
            model.setRowCount(0);
            products.clear();
            btnReceive.setEnabled(false);
            txtInvoice.setText("");
            chkPaid.setSelected(false);
            JOptionPane.showMessageDialog(this, "All items have been received", "Received", JOptionPane.INFORMATION_MESSAGE);
            if (JOptionPane.showConfirmDialog(this, "Do you want to print the report?", "Print", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    BigDecimal val = BigDecimal.ZERO;
                    for (ReceivedItem p : products) {
                        val = val.add(p.getPrice());
                    }
                    MessageFormat header = new MessageFormat("Receive Stock " + new Date());
                    MessageFormat footer = new MessageFormat("Page{0,number,integer}");
                    tblProducts.print(PrintMode.FIT_WIDTH, header, footer);
                } catch (PrinterException ex) {
                    JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (IOException | SQLException ex) {
            JOptionPane.showInternalMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnReceiveActionPerformed

    private void btnAddProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProductActionPerformed
        Product product;
        if (txtBarcode.getText().isEmpty()) {
            product = ProductSelectDialog.showDialog(this, false);
        } else {
            if (!Utilities.isNumber(txtBarcode.getText())) {
                txtBarcode.setSelectionStart(0);
                txtBarcode.setSelectionEnd(txtBarcode.getText().length());
                JOptionPane.showInternalMessageDialog(this, "Not a number", "Add Product", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                product = dc.getProductByBarcode(txtBarcode.getText());
            } catch (IOException | ProductNotFoundException | SQLException ex) {
                txtBarcode.setSelectionStart(0);
                txtBarcode.setSelectionEnd(txtBarcode.getText().length());
                JOptionPane.showInternalMessageDialog(this, ex, "Add Product", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (product == null) {
            return;
        }

        String str = JOptionPane.showInternalInputDialog(ReceiveItemsWindow.this, "Enter amount to receive", "Receive Stock", JOptionPane.INFORMATION_MESSAGE);

        if (str == null || str.isEmpty()) {
            return;
        }

        if (Utilities.isNumber(str)) {
            int amount = Integer.parseInt(str);
            if (amount <= 0) {
                JOptionPane.showInternalMessageDialog(ReceiveItemsWindow.this, "Value must be greater than zero", "Receive Items", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (product.getStock() + amount > product.getMaxStockLevel() && product.getMaxStockLevel() != 0) {
                int response = JOptionPane.showConfirmDialog(this, "Warning- this will take the product stock level higher than the maximum stock level defined for this product, Continue?", "Stock", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            if (amount == 0) {
                return;
            }

            products.add(new ReceivedItem(product, amount));
            txtBarcode.setText("");
            updateTable();
        } else {
            JOptionPane.showInternalMessageDialog(ReceiveItemsWindow.this, "You must enter a number", "Receive Stock", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAddProductActionPerformed

    private void tblProductsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblProductsMouseClicked
        if (viewMode) {
            return;
        }
        final int row = tblProducts.getSelectedRow();
        final ReceivedItem product = products.get(row);
        if (evt.getClickCount() == 2) {
            if (evt.getClickCount() == 2) {
                String input = JOptionPane.showInternalInputDialog(ReceiveItemsWindow.this, "Enter new quantity", "Receive Items", JOptionPane.PLAIN_MESSAGE);
                if (!Utilities.isNumber(input)) {
                    JOptionPane.showInternalMessageDialog(ReceiveItemsWindow.this, "A number must be entered", "Receive Items", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int val = Integer.parseInt(input);
                if (val > 0) {
                    product.setQuantity(val);
                    updateTable();
                } else {
                    JOptionPane.showInternalMessageDialog(ReceiveItemsWindow.this, "Must be a value greater than zero", "Receive Items", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
        if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem qu = new JMenuItem("Change Quantity");
            final Font boldFont = new Font(qu.getFont().getFontName(), Font.BOLD, qu.getFont().getSize());
            qu.setFont(boldFont);
            JMenuItem rem = new JMenuItem("Remove");
            qu.addActionListener((ActionEvent e) -> {
                String input = JOptionPane.showInternalInputDialog(ReceiveItemsWindow.this, "Enter new quantity", "Receive Items", JOptionPane.PLAIN_MESSAGE);
                if (!Utilities.isNumber(input)) {
                    JOptionPane.showInternalMessageDialog(ReceiveItemsWindow.this, "A number must be entered", "Receive Items", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int val = Integer.parseInt(input);
                if (val > 0) {
                    product.setQuantity(val);
                    updateTable();
                } else {
                    JOptionPane.showInternalMessageDialog(ReceiveItemsWindow.this, "Must be a value greater than zero", "Receive Items", JOptionPane.WARNING_MESSAGE);
                }
            });
            rem.addActionListener((ActionEvent e) -> {
                removeItem(product);
            });
            menu.add(qu);
            menu.add(rem);
            menu.show(tblProducts, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tblProductsMouseClicked

    private void chkPaidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkPaidActionPerformed
        if (viewMode) {
            rr.setPaid(chkPaid.isSelected());
            try {
                dc.updateReceivedReport(rr);
                JOptionPane.showMessageDialog(this, (chkPaid.isSelected() ? "Marked as paid" : "Marked as not paid"), "Invoice " + rr.getInvoiceId(), JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | SQLException ex) {
                JOptionPane.showInternalMessageDialog(ReceiveItemsWindow.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_chkPaidActionPerformed

    private void txtBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBarcodeActionPerformed
        btnAddProduct.doClick();
    }//GEN-LAST:event_txtBarcodeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnReceive;
    private javax.swing.JCheckBox chkPaid;
    private javax.swing.JComboBox<String> cmbSuppliers;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblValue;
    private javax.swing.JTable tblProducts;
    private javax.swing.JTextField txtBarcode;
    private javax.swing.JTextField txtInvoice;
    // End of variables declaration//GEN-END:variables
}
