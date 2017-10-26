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
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author David
 */
public class OrderingWindow extends javax.swing.JInternalFrame {

    private final DataConnect dc;

    private final MyModel model;

    /**
     * Creates new form OrderingWindow
     */
    public OrderingWindow() {
        this.dc = GUI.gui.dc;
        initComponents();
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        model = new MyModel();
        init();
    }

    private void init() {
        txtBarcode.requestFocus();
        table.setSelectionModel(new ForcedListSelectionModel());
        table.setModel(model);
        InputMap im = table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = table.getActionMap();

        KeyStroke deleteKey = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);

        im.put(deleteKey, "Action.delete");
        am.put("Action.delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                final int index = table.getSelectedRow();
                if (index == -1) {
                    return;
                }
                if (JOptionPane.showInternalConfirmDialog(OrderingWindow.this, "Are you sure you want to remove this item?", "Remove Item", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    model.removeProduct(index);
                }
            }
        });
    }

    public static void showWindow() {
        OrderingWindow window = new OrderingWindow();
        GUI.gui.internal.add(window);
        window.setVisible(true);
        try {
            window.setIcon(false);
            window.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WasteStockWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class MyModel implements TableModel {

        private final List<Product> products;
        private final List<TableModelListener> listeners;

        public MyModel() {
            products = new LinkedList<>();
            listeners = new LinkedList<>();
        }

        public void addProduct(Product p) {
            products.add(p);
            alert(products.size() - 1, products.size() - 1);
        }

        public void removeProduct(int i) {
            products.remove(i);
            alert(i, i);
        }

        public List<Product> getAllProducts() {
            return products;
        }

        public void clear() {
            int size = products.size();
            products.clear();
            alert(0, size);
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
                case 0:
                    return "Order Code";
                case 1:
                    return "Name";
                case 2:
                    return "Qty.";
                case 3:
                    return "Price";
                default:
                    break;
            }
            return "";
        }

        @Override
        public Class<?> getColumnClass(int i) {
            switch (i) {
                case 0:
                    return Integer.class;
                case 1:
                    return String.class;
                case 2:
                    return Integer.class;
                case 3:
                    return String.class;
                default:
                    break;
            }
            return Object.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int i) {
            Product p = products.get(rowIndex);
            switch (i) {
                case 0:
                    return p.getOrder_code();
                case 1:
                    return p.getLongName();
                case 2:
                    return p.getStock();
                case 3:
                    return "£" + p.getPrice().multiply(new BigDecimal(p.getStock()));
                default:
                    break;
            }
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 2) {
                products.get(rowIndex).setStock((int) aValue);
                alert(rowIndex, rowIndex);
                alert(rowIndex, rowIndex);
            }
        }

        private void alert(int row1, int row2) {
            for (TableModelListener l : listeners) {
                l.tableChanged(new TableModelEvent(this, row1, row2,
                        TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
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
        table = new javax.swing.JTable();
        btnClose = new javax.swing.JButton();
        btnAddProduct = new javax.swing.JButton();
        btnSend = new javax.swing.JButton();
        txtBarcode = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Ordering");
        setToolTipText("");

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Order Code", "Name", "Qty.", "Total Value"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setMinWidth(70);
            table.getColumnModel().getColumn(0).setPreferredWidth(70);
            table.getColumnModel().getColumn(0).setMaxWidth(70);
            table.getColumnModel().getColumn(2).setMinWidth(40);
            table.getColumnModel().getColumn(2).setPreferredWidth(40);
            table.getColumnModel().getColumn(2).setMaxWidth(40);
            table.getColumnModel().getColumn(3).setMinWidth(70);
            table.getColumnModel().getColumn(3).setPreferredWidth(70);
            table.getColumnModel().getColumn(3).setMaxWidth(70);
        }

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

        btnSend.setText("Send Order");
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
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
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnSend)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddProduct)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 285, Short.MAX_VALUE)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnAddProduct)
                    .addComponent(btnSend)
                    .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnAddProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProductActionPerformed
        Product p;
        if (txtBarcode.getText().isEmpty()) {
            p = ProductSelectDialog.showDialog(this);
        } else {
            if (!Utilities.isNumber(txtBarcode.getText())) {
                JOptionPane.showInternalMessageDialog(this, "Invalid input", "Add Product", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                p = dc.getProductByBarcode(txtBarcode.getText());
            } catch (IOException | ProductNotFoundException | SQLException ex) {
                JOptionPane.showInternalMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (p == null) {
            return;
        }

        String str = JOptionPane.showInternalInputDialog(this, "Enter quantity", "Add Product", JOptionPane.PLAIN_MESSAGE);
        int quantity = Integer.parseInt(str);
        p.setStock(quantity);
        model.addProduct(p);
        txtBarcode.setText("");
    }//GEN-LAST:event_btnAddProductActionPerformed

    private void txtBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBarcodeActionPerformed
        btnAddProduct.doClick();
    }//GEN-LAST:event_txtBarcodeActionPerformed

    private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendActionPerformed
        final ModalDialog dialog = new ModalDialog(this, "Send Order", "Sending order...");
        final Runnable run = () -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(OrderingWindow.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                model.clear();
                dialog.hide();
                JOptionPane.showMessageDialog(this, "Order sent", "Send Order", JOptionPane.INFORMATION_MESSAGE);
            }
        };
        final Thread thread = new Thread(run, "Order");
        thread.start();
        dialog.show();
    }//GEN-LAST:event_btnSendActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSend;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtBarcode;
    // End of variables declaration//GEN-END:variables
}
