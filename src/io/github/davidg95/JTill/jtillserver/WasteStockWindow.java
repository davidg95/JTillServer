/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 *
 * @author David
 */
public class WasteStockWindow extends javax.swing.JInternalFrame {

    private final DataConnect dc;
    private Date date;

    private final MyModel model;

    /**
     * Creates new form WasteStockWindow
     */
    public WasteStockWindow() {
        this.dc = GUI.gui.dc;
        initComponents();
        setTitle("Waste Stock");
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.YEAR, c.get(Calendar.YEAR));
        ca.set(Calendar.MONTH, c.get(Calendar.MONTH));
        ca.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
        ca.set(Calendar.HOUR_OF_DAY, 0);
        ca.set(Calendar.MINUTE, 0);
        ca.set(Calendar.SECOND, 0);
        ca.set(Calendar.MILLISECOND, 0);
        Date d = ca.getTime();
        Calendar cb = Calendar.getInstance();
        cb.setTime(new Date(0));
        cb.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
        cb.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
        cb.set(Calendar.SECOND, c.get(Calendar.SECOND));
        cb.set(Calendar.MILLISECOND, c.get(Calendar.MILLISECOND));
        Date t = cb.getTime();
        pickDate.setDate(new Date());
        timeSpin.setValue(t);
        model = new MyModel();
        tblProducts.setModel(model);
        init();
        txtBarcode.requestFocus();
    }

    public static void showWindow() {
        WasteStockWindow window = new WasteStockWindow();
        GUI.gui.internal.add(window);
        window.setVisible(true);
        try {
            window.setIcon(false);
            window.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WasteStockWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                    int res = JOptionPane.showConfirmDialog(WasteStockWindow.this, "Do you want to save the current report?", "Save", JOptionPane.YES_NO_OPTION);
                    if (res == JOptionPane.YES_OPTION) {
                        GUI.gui.savedReports.put("WAS", model.getItems());
                    }
                }
            }
        });
        if (GUI.gui.savedReports.containsKey("WAS")) {
            model.setItems(GUI.gui.savedReports.get("WAS"));
            GUI.gui.savedReports.remove("WAS");
        }
        InputMap im = tblProducts.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = tblProducts.getActionMap();

        KeyStroke deleteKey = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);

        im.put(deleteKey, "Action.delete");
        am.put("Action.delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                final int index = tblProducts.getSelectedRow();
                final WasteItem it = model.getWasteItem(index);
                final Product p = it.getProduct();
                if (index == -1) {
                    return;
                }
                if (JOptionPane.showConfirmDialog(WasteStockWindow.this, "Are you sure you want to remove this item?\n" + p.getLongName(), "Stock Item", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    model.removeWasteItem(index);
                    if (model.getRowCount() == 0) {
                        btnWaste.setEnabled(false);
                    }
                }
            }
        });
        JComboBox box = new JComboBox();
        try {
            List<WasteReason> wasteReasons = dc.getUsedWasteReasons();
            for (WasteReason wr : wasteReasons) {
                box.addItem(wr);
            }
            TableColumn wrCol = tblProducts.getColumnModel().getColumn(4);
            wrCol.setCellEditor(new DefaultCellEditor(box));
        } catch (IOException | SQLException ex) {
            Logger.getLogger(WasteStockWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Date getSelectedDate() {
        long d = pickDate.getDate().getTime();
        long t = ((Date) timeSpin.getValue()).getTime();
        date = new Date(d + t);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + 1);
        date = c.getTime();
        return date;
    }

    private class MyModel implements TableModel {

        private List<WasteItem> items;
        private final List<TableModelListener> listeners;

        public MyModel() {
            items = new LinkedList<>();
            listeners = new LinkedList<>();
        }

        public void addWasteItem(WasteItem wi) {
            items.add(wi);
            alertAll();
        }

        public void removeWasteItem(int i) {
            items.remove(i);
            alertAll();
        }

        public WasteItem getWasteItem(int i) {
            return items.get(i);
        }

        public void alertAll() {
            for (TableModelListener l : listeners) {
                l.tableChanged(new TableModelEvent(this));
            }
            BigDecimal val = BigDecimal.ZERO;
            for (WasteItem wi : items) {
                val = val.add(wi.getTotalValue());
            }
            if (val == BigDecimal.ZERO) {
                lblValue.setText("Total Value: £0.00");
            } else {
                lblValue.setText("Total Value: £" + new DecimalFormat("0.00").format(val));
            }
        }

        public void setItems(List<WasteItem> items) {
            this.items = items;
            alertAll();
        }

        public List<WasteItem> getItems() {
            return items;
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
            return 5;
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
                case 3: {
                    return "Total Value";
                }
                case 4: {
                    return "Reason";
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public Class<?> getColumnClass(int i) {
            switch (i) {
                case 0: {
                    return String.class;
                }
                case 1: {
                    return String.class;
                }
                case 2: {
                    return Integer.class;
                }
                case 3: {
                    return Object.class;
                }
                case 4: {
                    return String.class;
                }
                default: {
                    return String.class;
                }
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 2 || columnIndex == 4;
        }

        @Override
        public Object getValueAt(int rowIndex, int i) {
            WasteItem item = items.get(rowIndex);
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
                case 3: {
                    return "£" + new DecimalFormat("0.00").format(item.getTotalValue());
                }
                case 4: {
                    return item.getReason().getName();
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if(rowIndex == -1){
                return;
            }
            WasteItem item = items.get(rowIndex);
            if (columnIndex == 2) {
                int value = (int) aValue;
                if (value < 0) {
                    JOptionPane.showMessageDialog(WasteStockWindow.this, "Value must be greater than or equal to 0", "Set Value", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                item.setQuantity(value);
            } else if (columnIndex == 4) {
                WasteReason reason = (WasteReason) aValue;
                if ((reason.getPriviledgeLevel() + 1) > GUI.staff.getPosition()) {
                    int resp = JOptionPane.showOptionDialog(WasteStockWindow.this, "You are not authorised to use this reason", "Waste Reason", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[]{"Ok", "Get Authorisation Now"}, null);
                    if (resp == 1) {
                        Staff s = LoginDialog.showLoginDialog(WasteStockWindow.this);
                        if (s == null) {
                            return;
                        }
                        if (reason.getPriviledgeLevel() + 1 <= s.getPosition()) {
                            item.setReason(reason);
                        } else {
                            JOptionPane.showMessageDialog(WasteStockWindow.this, "You do not have authority", "Waste Reason", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    return;
                }
                item.setReason(reason);
            }
            alertAll();
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
        btnClose = new javax.swing.JButton();
        btnWaste = new javax.swing.JButton();
        btnAddProduct = new javax.swing.JButton();
        lblValue = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        timeSpin = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        pickDate = new org.jdesktop.swingx.JXDatePicker();
        txtBarcode = new javax.swing.JTextField();
        btnAddfile = new javax.swing.JButton();

        setResizable(true);
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/tillIcon.png"))); // NOI18N

        tblProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Product", "Qty.", "Total Value", "Reason"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true
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
            tblProducts.getColumnModel().getColumn(3).setResizable(false);
            tblProducts.getColumnModel().getColumn(4).setResizable(false);
        }

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnWaste.setText("Waste");
        btnWaste.setEnabled(false);
        btnWaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWasteActionPerformed(evt);
            }
        });

        btnAddProduct.setText("Add Product");
        btnAddProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddProductActionPerformed(evt);
            }
        });

        lblValue.setText("Total Value: £0.00");

        jLabel1.setText("Time:");

        SpinnerDateModel model = new SpinnerDateModel();
        model.setCalendarField(Calendar.MINUTE);

        timeSpin = new JSpinner();
        timeSpin.setModel(model);
        timeSpin.setEditor(new JSpinner.DateEditor(timeSpin, "h:mm a"));

        jLabel2.setText("Date:");

        txtBarcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBarcodeActionPerformed(evt);
            }
        });

        btnAddfile.setText("Add File");
        btnAddfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddfileActionPerformed(evt);
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
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pickDate, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timeSpin)
                        .addGap(277, 277, 277))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(lblValue)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                                .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnAddProduct)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnAddfile, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnWaste)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnClose)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(pickDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnWaste)
                    .addComponent(btnAddProduct)
                    .addComponent(lblValue)
                    .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddfile))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        try {
            this.setClosed(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WasteStockWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnCloseActionPerformed

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

            String str = JOptionPane.showInputDialog(this, "Enter amount to waste", "Waste", JOptionPane.INFORMATION_MESSAGE);

            if (str == null || str.isEmpty()) {
                return;
            }

            if (Utilities.isNumber(str)) {
                int amount = Integer.parseInt(str);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Value must be greater than zero", "Waste Item", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (product.isTrackStock() && product.getStock() - amount < 0) {
                    if (JOptionPane.showConfirmDialog(this, "Item does not have that much in stock. Continue?", "Waste", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                if (amount == 0) {
                    return;
                }
                WasteReason wr = WasteReasonSelectDialog.showDialog(this);
                if (wr == null) {
                    return;
                }
                WasteItem wi = new WasteItem(product, amount, wr, getSelectedDate());
                model.addWasteItem(wi);
                txtBarcode.setText("");
                btnWaste.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "You must enter a number", "Waste Stock", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input detected", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAddProductActionPerformed

    private void btnWasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWasteActionPerformed
        if (model.getItems().isEmpty()) {
            return;
        }
        final ModalDialog mDialog = new ModalDialog(this, "Waste");
        final Runnable run = () -> {
            BigDecimal total = BigDecimal.ZERO;
            for (WasteItem wi : model.getItems()) {
                try {
                    Product product = dc.getProduct(wi.getProduct().getId());
                    product.removeStock(wi.getQuantity());
                    dc.updateProduct(product);
                    total = total.add(product.getIndividualCost().multiply(new BigDecimal(wi.getQuantity())));
                } catch (IOException | ProductNotFoundException | SQLException ex) {
                    mDialog.hide();
                    JOptionPane.showMessageDialog(WasteStockWindow.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            total.setScale(2);
            try {
                dc.addWasteReport(model.getItems());
                model.clear();
                lblValue.setText("Total Value: £0.00");
                btnWaste.setEnabled(false);
                mDialog.hide();
                JOptionPane.showMessageDialog(WasteStockWindow.this, "All items have been wasted", "Waste", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | SQLException | JTillException ex) {
                mDialog.hide();
                JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
            mDialog.hide();
        };
        final Thread thread = new Thread(run, "WASTE_THREAD");
        thread.start();
        mDialog.show();
    }//GEN-LAST:event_btnWasteActionPerformed

    private void tblProductsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblProductsMouseClicked
        final int index = tblProducts.getSelectedRow();
        final WasteItem wi = model.getWasteItem(index);

        if (tblProducts.getSelectedRow() == -1) {
            return;
        }
        if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem changeQuantity = new JMenuItem("Change Quantity");
            JMenuItem changeReason = new JMenuItem("Change Reason");
            JMenuItem item = new JMenuItem("Remove");
            changeQuantity.addActionListener((ActionEvent e) -> {
                boolean loop = true;
                while (loop) {
                    String input = JOptionPane.showInputDialog(this, "Enter new quantity", "Waste Stock", JOptionPane.PLAIN_MESSAGE);
                    if (!Utilities.isNumber(input)) {
                        JOptionPane.showMessageDialog(this, "A number must be entered", "Waste Stock", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    int val = Integer.parseInt(input);
                    if (val > 0) {
                        wi.setQuantity(val);
                        model.alertAll();
                        loop = false;
                    } else {
                        JOptionPane.showMessageDialog(this, "Must be a value greater than zero", "Waste Stock", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
            changeReason.addActionListener((ActionEvent e) -> {
                WasteReason reason = WasteReasonSelectDialog.showDialog(this);
                if (reason == null) {
                    return;
                }
                wi.setReason(reason);
                model.alertAll();
            });
            item.addActionListener((ActionEvent e) -> {
                final Product p = wi.getProduct();
                if (index == -1) {
                    return;
                }
                if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this item?\n" + p.getLongName(), "Stock Item", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    model.removeWasteItem(index);
                    if (model.getRowCount() == 0) {
                        btnWaste.setEnabled(false);
                    }
                }
            });
            menu.add(changeQuantity);
            menu.add(changeReason);
            menu.addSeparator();
            menu.add(item);
            menu.show(tblProducts, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tblProductsMouseClicked

    private void txtBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBarcodeActionPerformed
        btnAddProduct.doClick();
    }//GEN-LAST:event_txtBarcodeActionPerformed

    private void btnAddfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddfileActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.txt files", "txt");
        chooser.setFileFilter(filter);
        int result = chooser.showDialog(this, "Select File");
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                Scanner in = new Scanner(file);
                int unknowns = 0;
                int count = 0;
                while (in.hasNext()) {
                    count++;
                    String line = in.nextLine();
                    Scanner inLine = new Scanner(line);
                    inLine.useDelimiter(",");
                    String barcode = inLine.next();
                    int quantity = Integer.parseInt(inLine.next());
                    int reasonID = Integer.parseInt(inLine.next());
                    try {
                        Product p = dc.getProductByBarcode(barcode);
                        WasteReason reason;
                        try {
                            reason = dc.getWasteReason(reasonID);
                        } catch (IOException | SQLException | JTillException ex) {
                            try {
                                reason = dc.getWasteReason(1);
                            } catch (IOException | SQLException | JTillException ex1) {
                                reason = null;
                            }
                        }
                        WasteItem item = new WasteItem(p, quantity, reason, new Date());
                        model.addWasteItem(item);
                    } catch (IOException | ProductNotFoundException | SQLException ex) {
                        unknowns++;
                    }
                }
                JOptionPane.showMessageDialog(this, "Loaded " + count + " items with " + unknowns + " unknown items", "Load File", JOptionPane.INFORMATION_MESSAGE);
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error reading file, are you sure this is a waste session?", "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnAddfileActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnAddfile;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnWaste;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblValue;
    private org.jdesktop.swingx.JXDatePicker pickDate;
    private javax.swing.JTable tblProducts;
    private javax.swing.JSpinner timeSpin;
    private javax.swing.JTextField txtBarcode;
    // End of variables declaration//GEN-END:variables
}
