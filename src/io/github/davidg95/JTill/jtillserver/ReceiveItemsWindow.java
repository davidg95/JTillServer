/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTable.PrintMode;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author david
 */
public final class ReceiveItemsWindow extends javax.swing.JInternalFrame {

    private final DataConnect dc;
    private ReceivedReport rr;
    private final MyModel model;
    private Supplier supplier;

    private boolean viewMode = false;

    private Order order;

    /**
     * Creates new form ReceiveItemsWindow
     */
    public ReceiveItemsWindow() {
        this.dc = GUI.gui.dc;
        initComponents();
        setTitle("Receive Stock");
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        model = new MyModel();
        tblProducts.setModel(model);
        init();
        txtBarcode.requestFocus();
    }

    public ReceiveItemsWindow(ReceivedReport rr) {
        this.dc = GUI.gui.dc;
        this.rr = rr;
        initComponents();
        setTitle(rr.getSupplier().getName() + " - " + rr.getInvoiceId());
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        model = new MyModel();
        tblProducts.setModel(model);
        txtBarcode.setEnabled(false);
        btnAddOrder.setEnabled(false);
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
            window.setSupplier();
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(window, "Error connecting to database", "Receive Stock", JOptionPane.ERROR_MESSAGE);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(ReceiveItemsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setSupplier() {
        supplier = SupplierSelectDialog.showDialog(this);
        if (supplier == null) {
            setVisible(false);
            return;
        }
        txtSupplier.setText(supplier.getName());
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
        model.setItems(rr.getItems());
        txtInvoice.setText(rr.getInvoiceId());
        supplier = rr.getSupplier();
        txtSupplier.setText(supplier.getName());
        chkPaid.setSelected(rr.isPaid());
        setViewMode();
    }

    private void init() {
        tblProducts.getColumnModel().getColumn(0).setMaxWidth(40);
        tblProducts.getColumnModel().getColumn(2).setMaxWidth(40);
        tblProducts.getColumnModel().getColumn(0).setMinWidth(40);
        tblProducts.getColumnModel().getColumn(2).setMinWidth(40);
        tblProducts.setSelectionModel(new ForcedListSelectionModel());
        this.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                if (!model.getItems().isEmpty()) {
                    int res = JOptionPane.showInternalConfirmDialog(ReceiveItemsWindow.this, "Do you want to save the current report?", "Save", JOptionPane.YES_NO_OPTION);
                    if (res == JOptionPane.YES_OPTION) {
                        GUI.gui.savedReports.put("REC", model.getItems());
                    }
                }
            }
        });
        if (GUI.gui.savedReports.containsKey("REC")) {
            model.setItems(GUI.gui.savedReports.get("REC"));
            GUI.gui.savedReports.remove("REC");
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
                final ReceivedItem p = model.getItem(index);
                removeItem(p);
            }
        });
    }

    private void removeItem(ReceivedItem i) {
        if (JOptionPane.showInternalConfirmDialog(ReceiveItemsWindow.this, "Are you sure you want to remove this item?\n" + i, "Stock Item", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            model.removeItem(i);
            if (model.getRowCount() == 0) {
                btnReceive.setEnabled(false);
            }
        }
    }

    private void setViewMode() {
        btnAddProduct.setEnabled(false);
        btnReceive.setEnabled(false);
        txtInvoice.setEditable(false);
        btnAddOrder.setSelected(false);
        viewMode = true;
    }

    private class MyModel implements TableModel {

        private List<ReceivedItem> items;
        private final List<TableModelListener> listeners;

        public MyModel() {
            this.items = new LinkedList<>();
            this.listeners = new LinkedList<>();
        }

        public void addItem(ReceivedItem item) {
            items.add(item);
            alertAll();
        }

        public void removeItem(int i) {
            items.remove(i);
            alertAll();
        }

        public void removeItem(ReceivedItem item) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).equals(item)) {
                    items.remove(i);
                    alertAll();
                    return;
                }
            }
        }

        public ReceivedItem getItem(int i) {
            return items.get(i);
        }

        public List<ReceivedItem> getItems() {
            return items;
        }

        public void setItems(List<ReceivedItem> items) {
            this.items = items;
            alertAll();
        }

        public void clear() {
            items.clear();
            alertAll();
        }

        @Override
        public int getRowCount() {
            return items.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int i) {
            switch (i) {
                case 0: {
                    return "ID";
                }
                case 1: {
                    return "Product";
                }
                case 2: {
                    return "Qty.";
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
        public boolean isCellEditable(int rowIndex, int i) {
            return i == 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int i) {
            final ReceivedItem item = items.get(rowIndex);
            switch (i) {
                case 0: {
                    return item.getProduct().getId();
                }
                case 1: {
                    return item.getProduct().getLongName();
                }
                case 2: {
                    return item.getQuantity();
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            final ReceivedItem item = items.get(rowIndex);
            if (columnIndex == 2) {
                int quantity = Integer.parseInt((String) aValue);
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(ReceiveItemsWindow.this, "Value must be greater than 0", "New Quantity", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                item.setQuantity(quantity);
            }
            alertAll();
        }

        public void alertAll() {
            for (TableModelListener l : listeners) {
                l.tableChanged(new TableModelEvent(this));
            }
            BigDecimal val = BigDecimal.ZERO;
            for (ReceivedItem ri : items) {
                val = val.add(ri.getPrice());
            }
            if (val == BigDecimal.ZERO) {
                lblValue.setText("Total Value: £0.00");
            } else {
                lblValue.setText("Total Value: £" + new DecimalFormat("0.00").format(val));
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

        jScrollPane1 = new javax.swing.JScrollPane();
        tblProducts = new javax.swing.JTable();
        btnReceive = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        btnAddProduct = new javax.swing.JButton();
        lblValue = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtInvoice = new javax.swing.JTextField();
        chkPaid = new javax.swing.JCheckBox();
        txtBarcode = new javax.swing.JTextField();
        btnAddOrder = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        txtSupplier = new javax.swing.JTextField();

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

        btnAddOrder.setText("Add Order");
        btnAddOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddOrderActionPerformed(evt);
            }
        });

        jLabel1.setText("Supplier:");

        txtSupplier.setEditable(false);
        txtSupplier.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtSupplierMouseClicked(evt);
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBarcode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddProduct)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAddOrder)
                        .addGap(18, 18, 18)
                        .addComponent(btnReceive)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(165, 165, 165)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSupplier)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(txtSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReceive)
                    .addComponent(btnClose)
                    .addComponent(btnAddProduct)
                    .addComponent(lblValue)
                    .addComponent(chkPaid)
                    .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddOrder))
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
        if (model.getItems().isEmpty()) {
            return;
        }
        if (txtInvoice.getText().isEmpty()) {
            JOptionPane.showInternalMessageDialog(this, "You must enter an invoice number", "Receive", JOptionPane.WARNING_MESSAGE);
            txtInvoice.requestFocus();
            return;
        }
        ReceivedReport report = new ReceivedReport(txtInvoice.getText(), supplier);
        model.getItems().forEach((p) -> {
            try {
                Product product = p.getProduct();
                product.addStock(p.getQuantity());
                dc.updateProduct(product);
            } catch (IOException | ProductNotFoundException | SQLException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        report.setPaid(chkPaid.isSelected());
        report.setItems(model.getItems());
        try {
            dc.addReceivedReport(report);
            if (order != null) {
                order.setReceived(true);
                dc.updateOrder(order);
            }
            lblValue.setText("Total: £0.00");
            model.clear();
            btnReceive.setEnabled(false);
            txtInvoice.setText("");
            chkPaid.setSelected(false);
            JOptionPane.showMessageDialog(this, "All items have been received", "Received", JOptionPane.INFORMATION_MESSAGE);
            if (JOptionPane.showConfirmDialog(this, "Do you want to print the report?", "Print", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    BigDecimal val = BigDecimal.ZERO;
                    for (ReceivedItem p : model.getItems()) {
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
        try {
            Product product;
            if (txtBarcode.getText().isEmpty()) {
                product = ProductSelectDialog.showDialog(this);
            } else {
                if (!Utilities.isNumber(txtBarcode.getText())) {
                    txtBarcode.setSelectionStart(0);
                    txtBarcode.setSelectionEnd(txtBarcode.getText().length());
                    JOptionPane.showMessageDialog(this, "Not a number", "Add Product", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!Utilities.validateBarcodeLenth(txtBarcode.getText())) {
                    txtBarcode.setSelectionStart(0);
                    txtBarcode.setSelectionEnd(txtBarcode.getText().length());
                    JOptionPane.showMessageDialog(this, "Barcodes must be 8, 12, 13 or 14 digits long", "Add Product", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!Utilities.validateBarcode(txtBarcode.getText())) {
                    txtBarcode.setSelectionStart(0);
                    txtBarcode.setSelectionEnd(txtBarcode.getText().length());
                    JOptionPane.showMessageDialog(this, "Invalid check digit", "Add Product", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    product = dc.getProductByBarcode(txtBarcode.getText());
                } catch (IOException | ProductNotFoundException | SQLException ex) {
                    txtBarcode.setSelectionStart(0);
                    txtBarcode.setSelectionEnd(txtBarcode.getText().length());
                    JOptionPane.showMessageDialog(this, ex, "Add Product", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (product == null) {
                return;
            }

            if (product.isOpen() || !product.isTrackStock()) {
                JOptionPane.showMessageDialog(this, "This product cannot be received", "Add Product", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String str = JOptionPane.showInputDialog(this, "Enter amount to receive", "Receive Stock", JOptionPane.INFORMATION_MESSAGE);

            if (str == null || str.isEmpty()) {
                return;
            }

            if (Utilities.isNumber(str)) {
                int amount = Integer.parseInt(str);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Value must be greater than zero", "Receive Items", JOptionPane.ERROR_MESSAGE);
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

                model.addItem(new ReceivedItem(product, amount));
                txtBarcode.setText("");
                btnReceive.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(ReceiveItemsWindow.this, "You must enter a number", "Receive Stock", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input detected", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAddProductActionPerformed

    private void tblProductsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblProductsMouseClicked
        if (viewMode) {
            return;
        }
        final int row = tblProducts.getSelectedRow();
        final ReceivedItem product = model.getItem(row);
        if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem qu = new JMenuItem("Change Quantity");
            JMenuItem rem = new JMenuItem("Remove");
            qu.addActionListener((ActionEvent e) -> {
                String input = JOptionPane.showInputDialog(this, "Enter new quantity", "Receive Items", JOptionPane.PLAIN_MESSAGE);
                if (!Utilities.isNumber(input)) {
                    JOptionPane.showMessageDialog(this, "A number must be entered", "Receive Items", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int val = Integer.parseInt(input);
                if (val > 0) {
                    product.setQuantity(val);
                    model.alertAll();
                } else {
                    JOptionPane.showMessageDialog(this, "Must be a value greater than zero", "Receive Items", JOptionPane.WARNING_MESSAGE);
                }
            });
            rem.addActionListener((ActionEvent e) -> {
                removeItem(product);
            });
            menu.add(qu);
            menu.addSeparator();
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

    private void btnAddOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddOrderActionPerformed
        if (!model.getItems().isEmpty()) {
            JOptionPane.showInternalMessageDialog(this, "You cannot add an order with other items", "Add Order", JOptionPane.ERROR_MESSAGE);
            return;
        }
        order = OrderSelectDialog.showDialog(this);
        if (order == null) {
            return;
        }

        if (!order.isSent()) {
            if (JOptionPane.showConfirmDialog(this, "This order has not been sent, continue?", "Add Order", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }
        }

        for (OrderItem oi : order.getItems()) {
            model.addItem(new ReceivedItem(oi.getProduct(), oi.getQuantity() * oi.getProduct().getPackSize()));
        }
        supplier = order.getSupplier();
        txtSupplier.setText(supplier.getName());
        btnAddProduct.setEnabled(false);
        txtBarcode.setEnabled(false);
        txtBarcode.setText("");
    }//GEN-LAST:event_btnAddOrderActionPerformed

    private void txtSupplierMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtSupplierMouseClicked
        if (SwingUtilities.isLeftMouseButton(evt)) {
            if (evt.getClickCount() == 2) {
                Supplier sup = SupplierSelectDialog.showDialog(this);
                if (sup == null) {
                    return;
                }
                supplier = sup;
                txtSupplier.setText(supplier.getName());
            }
        }
    }//GEN-LAST:event_txtSupplierMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddOrder;
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnReceive;
    private javax.swing.JCheckBox chkPaid;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblValue;
    private javax.swing.JTable tblProducts;
    private javax.swing.JTextField txtBarcode;
    private javax.swing.JTextField txtInvoice;
    private javax.swing.JTextField txtSupplier;
    // End of variables declaration//GEN-END:variables
}
