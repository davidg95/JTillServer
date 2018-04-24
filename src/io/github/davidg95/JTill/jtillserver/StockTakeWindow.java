/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import com.sun.glass.events.KeyEvent;
import io.github.davidg95.JTill.jtill.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

/**
 *
 * @author David
 */
public class StockTakeWindow extends javax.swing.JInternalFrame {

    private static StockTakeWindow window;

    private final JTill jtill;
    private MyModel model;

    /**
     * Creates new form StockTakeWindow
     */
    public StockTakeWindow(JTill jtill) {
        this.jtill = jtill;
        initComponents();
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        init();
    }

    public static void showWindow(JTill jtill) {
        window = new StockTakeWindow(jtill);
        GUI.gui.internal.add(window);
        window.setVisible(true);
        try {
            window.setIcon(false);
            window.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(StockTakeWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void init() {
        model = new MyModel();
        table.setModel(model);
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusColumnCellRenderer());
        table.getColumnModel().getColumn(2).setMinWidth(80);
        table.getColumnModel().getColumn(2).setMaxWidth(80);
        table.getColumnModel().getColumn(3).setMinWidth(80);
        table.getColumnModel().getColumn(3).setMaxWidth(80);
        table.getColumnModel().getColumn(4).setMinWidth(80);
        table.getColumnModel().getColumn(4).setMaxWidth(80);
        table.setSelectionModel(new ForcedListSelectionModel());
        InputMap im = table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = table.getActionMap();
        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        im.put(enterKey, "Action.enter");
        am.put("Action.enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                final int index = table.getSelectedRow();
                final Product p = model.get(index).getProduct();
                if (index == -1) {
                    return;
                }
                if (JOptionPane.showConfirmDialog(StockTakeWindow.this, "Are you sure you want to remove this item?\n" + p, "Stock Item", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    model.removeItem(index);
                }
            }
        });
    }

    public class StatusColumnCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (((int) value) == 0) {
                l.setBackground(Color.GREEN);
            } else if (((int) value) < 0) {
                l.setBackground(Color.RED);
            } else {
                l.setBackground(Color.ORANGE);
            }
            return l;
        }
    }

    private class MyModel implements TableModel {

        private final List<ProductStockPair> products;
        private final List<TableModelListener> listeners;

        public MyModel() {
            this.products = new LinkedList<>();
            this.listeners = new LinkedList<>();
        }

        public void addItem(Product p, int amount) {
            for (ProductStockPair psp : products) {
                Product pr = psp.getProduct();
                if (pr.equals(p)) {
                    String option = (String) JOptionPane.showInputDialog(StockTakeWindow.this, "Product already in report, add to quantity ot set to quantity?", "Add Product", JOptionPane.QUESTION_MESSAGE, null, new String[]{"Add To", "Set To"}, "Add To");
                    if ("Add To".equals(option)) {
                        psp.setNewStock(((int) psp.getNewStock()) + amount);
                    } else {
                        psp.setNewStock(amount);
                    }
                    alertAll();
                    return;
                }
            }
            products.add(new ProductStockPair(p, amount));
            alertAll();
        }

        public void addItems(List<Product> ps) {
            Main:
            for (Product pr : ps) {
                for (ProductStockPair psp : products) {
                    Product p = psp.getProduct();
                    if (p.equals(pr)) {
                        continue Main;
                    }
                }
                products.add(new ProductStockPair(pr, 0));
            }
            alertAll();
        }

        public void removeItem(Product p) {
            for (int i = 0; i < products.size(); i++) {
                ProductStockPair psp = products.get(i);
                if (psp.getProduct().equals(p)) {
                    products.remove(i);
                    alertAll();
                    return;
                }
            }
        }

        public void removeItem(int i) {
            products.remove(i);
            alertAll();
        }

        public ProductStockPair get(int i) {
            return products.get(i);
        }

        public int indexOf(ProductStockPair p) {
            return products.indexOf(p);
        }

        public List<ProductStockPair> getAll() {
            return products;
        }

        public void clear() {
            products.clear();
            alertAll();
        }

        @Override
        public int getRowCount() {
            return products.size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0: {
                    return "Barcode";
                }
                case 1: {
                    return "Product";
                }
                case 2: {
                    return "Current Stock";
                }
                case 3: {
                    return "New Qty.";
                }
                case 4: {
                    return "Discrepency";
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex >= 2) {
                return Integer.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ProductStockPair psp = products.get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return psp.getProduct().getBarcode();
                }
                case 1: {
                    return psp.getProduct().getLongName();
                }
                case 2: {
                    return psp.getProduct().getStock();
                }
                case 3: {
                    return psp.getNewStock();
                }
                case 4: {
                    return psp.getNewStock() - psp.getProduct().getStock();
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            ProductStockPair psp = products.get(rowIndex);
            if (columnIndex == 3) {
                psp.setNewStock((int) aValue);
            }
        }

        private void alertAll() {
            for (TableModelListener l : listeners) {
                l.tableChanged(new TableModelEvent(this));
            }
            btnSubmit.setEnabled(!products.isEmpty());
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

    private class ProductStockPair {

        private final Product product;
        private int newStock;

        public ProductStockPair(Product product, int newStock) {
            this.product = product;
            this.newStock = newStock;
        }

        public void setNewStock(int newStock) {
            this.newStock = newStock;
        }

        public Product getProduct() {
            return product;
        }

        public int getNewStock() {
            return newStock;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 83 * hash + Objects.hashCode(this.product);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ProductStockPair other = (ProductStockPair) obj;
            return Objects.equals(this.product, other.product);
        }

        @Override
        public String toString() {
            return product.getLongName() + " - " + newStock;
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
        table = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        btnAddProduct = new javax.swing.JButton();
        btnAddCSV = new javax.swing.JButton();
        btnSubmit = new javax.swing.JButton();
        addDc = new javax.swing.JButton();

        setTitle("Stock Take");

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        table.getTableHeader().setReorderingAllowed(false);
        table.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                tableMouseWheelMoved(evt);
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);

        jLabel1.setText("Search:");

        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
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

        btnAddCSV.setText("Add CSV");
        btnAddCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddCSVActionPerformed(evt);
            }
        });

        btnSubmit.setText("Submit");
        btnSubmit.setEnabled(false);
        btnSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubmitActionPerformed(evt);
            }
        });

        addDc.setText("Add D/C");
        addDc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDcActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 80, Short.MAX_VALUE)
                        .addComponent(btnAddCSV)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddProduct)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addDc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSubmit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)
                    .addComponent(btnClose)
                    .addComponent(btnAddProduct)
                    .addComponent(btnAddCSV)
                    .addComponent(btnSubmit)
                    .addComponent(addDc))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        try {
            setClosed(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(StockTakeWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnAddProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProductActionPerformed
        Product p = ProductSelectDialog.showDialog(this, jtill);
        if (p != null) {
            String input = JOptionPane.showInputDialog(this, "Enter new quantity", "Stock Take", JOptionPane.PLAIN_MESSAGE);
            if (!Utilities.isNumber(input)) {
                JOptionPane.showMessageDialog(this, "Must enter a number", "Stock Take", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int val = Integer.parseInt(input);
            if (val > 0) {
                model.addItem(p, val);
                return;
            } else {
                JOptionPane.showMessageDialog(this, "Must enter a value greater than zero", "Stock Take", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else {
            return;
        }
    }//GEN-LAST:event_btnAddProductActionPerformed

    private void btnAddCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCSVActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Stock File");
        int returnVal = chooser.showOpenDialog(this);
        int nFound = 0;
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
                        JOptionPane.showMessageDialog(StockTakeWindow.this, "File is not recognised", "Add CSV", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String barcode = items[0];
                    int quantity = Integer.parseInt(items[1]);

                    Product product;
                    try {
                        product = jtill.getDataConnection().getProductByBarcode(barcode);

                        model.addItem(product, quantity);
                    } catch (ProductNotFoundException ex) {
                        nFound++;
                    }
                }
                if (nFound > 0) {
                    JOptionPane.showMessageDialog(StockTakeWindow.this, nFound + " barcodes could not be found", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(StockTakeWindow.this, ex, "File Not Found", JOptionPane.ERROR_MESSAGE);
            } catch (IOException | SQLException ex) {
                JOptionPane.showMessageDialog(StockTakeWindow.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnAddCSVActionPerformed

    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        if (model.getAll().isEmpty()) {
            return;
        }
        if (JOptionPane.showConfirmDialog(StockTakeWindow.this, "Are you sure you want to submit this stock take?", "Stock Take", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            final ModalDialog mDialog = new ModalDialog(this, "Stock Take");
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        List<Product> products = new LinkedList<>();
                        for (ProductStockPair psp : model.getAll()) {
                            psp.getProduct().setStock(psp.getNewStock());
                            products.add(psp.getProduct());
                        }
                        jtill.getDataConnection().submitStockTake(products);
                        model.clear();
                        mDialog.hide();
                        JOptionPane.showMessageDialog(StockTakeWindow.this, "Stock take submitted", "Complete", JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException | SQLException ex) {
                        mDialog.hide();
                        JOptionPane.showMessageDialog(StockTakeWindow.this, ex + "\nStock take not submitted", "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        mDialog.hide();
                    }
                }
            };
            final Thread thread = new Thread(runnable, "STOCK_TAKE");
            thread.start();
            mDialog.show();
        }
    }//GEN-LAST:event_btnSubmitActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        btnSearch.doClick();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        if (txtSearch.getText().equals("")) {
            return;
        }
        table.getSelectionModel().removeSelectionInterval(0, model.getRowCount());
        for (ProductStockPair p : model.getAll()) {
            if (p.getProduct().getBarcode().equals(txtSearch.getText())) {
                int index = model.indexOf(p);
                table.getSelectionModel().setSelectionInterval(index, index);
            }
        }
        txtSearch.setSelectionStart(0);
        txtSearch.setSelectionEnd(txtSearch.getText().length());
    }//GEN-LAST:event_btnSearchActionPerformed

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        final int index = table.getSelectedRow();
        if (index == -1) {
            return;
        }
        final ProductStockPair p = model.get(index);
        if (SwingUtilities.isLeftMouseButton(evt)) {
            if (evt.getClickCount() == 2) {
                String input = JOptionPane.showInputDialog(StockTakeWindow.this, "Enter new quantity", "Stock Take", JOptionPane.PLAIN_MESSAGE);
                if (input == null || input.isEmpty()) {
                    return;
                }
                if (!Utilities.isNumber(input)) {
                    JOptionPane.showMessageDialog(StockTakeWindow.this, "A number must be entered", "Stock Take", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int val = Integer.parseInt(input);
                if (val > 0) {
                    p.setNewStock(val);
                    model.alertAll();
                } else {
                    JOptionPane.showMessageDialog(StockTakeWindow.this, "Must be a value greater than zero", "Stock Take", JOptionPane.WARNING_MESSAGE);
                }
            }
        } else if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu m = new JPopupMenu();
            JMenuItem changeQuantity = new JMenuItem("Change Quantity");
            JMenuItem removeItem = new JMenuItem("Remove Item");
            changeQuantity.addActionListener((ActionEvent e) -> {
                String input = JOptionPane.showInputDialog(StockTakeWindow.this, "Enter new quantity", "Stock Take", JOptionPane.PLAIN_MESSAGE);
                if (!Utilities.isNumber(input)) {
                    JOptionPane.showMessageDialog(StockTakeWindow.this, "A number must be entered", "Stock Take", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int val = Integer.parseInt(input);
                if (val > 0) {
                    p.setNewStock(val);
                    model.alertAll();
                } else {
                    JOptionPane.showMessageDialog(StockTakeWindow.this, "Must be a value greater than zero", "Stock Take", JOptionPane.WARNING_MESSAGE);
                }
            });
            removeItem.addActionListener((ActionEvent e) -> {
                if (index == -1) {
                    return;
                }
                if (JOptionPane.showConfirmDialog(StockTakeWindow.this, "Are you sure you want to remove this item?\n" + p, "Stock Item", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    model.removeItem(index);
                }
            });
            m.add(changeQuantity);
            m.add(removeItem);
            m.show(table, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tableMouseClicked

    private void addDcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDcActionPerformed
        Object selection = DCSelectDialog.showDialog(this, DCSelectDialog.ANY_SELECT);
        if(selection == null){
            return;
        }
        List<Product> products;
        try {
            if (selection instanceof Department) {
                Department d = (Department) selection;
                products = jtill.getDataConnection().getProductsInDepartment(d.getId());
            } else if (selection instanceof Category) {
                Category c = (Category) selection;
                products = jtill.getDataConnection().getProductsInCategory(c.getId());
            } else {
                products = jtill.getDataConnection().getAllProducts();
            }
            model.addItems(products);
        } catch (IOException | SQLException | JTillException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_addDcActionPerformed

    private void tableMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_tableMouseWheelMoved
        int row = table.getSelectedRow();
        if (evt.getWheelRotation() < 0) { //Up
            if (row == 0) {
                return;
            }
            table.getSelectionModel().setSelectionInterval(row - 1, row - 1);
        } else if (evt.getWheelRotation() > 0) { //Down
            if (table.getRowCount() - 1 == row) {
                return;
            }
            table.getSelectionModel().setSelectionInterval(row + 1, row + 1);
        }
    }//GEN-LAST:event_tableMouseWheelMoved

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addDc;
    private javax.swing.JButton btnAddCSV;
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSubmit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
