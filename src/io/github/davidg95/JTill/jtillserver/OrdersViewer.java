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
import java.util.Date;
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
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author David
 */
public class OrdersViewer extends javax.swing.JInternalFrame {

    private final DataConnect dc;

    private final MyModel model;
    private final EditModel editModel;

    private Order currentOrder;

    /**
     * Creates new form OrdersViewer
     */
    public OrdersViewer() {
        this.dc = GUI.gui.dc;
        initComponents();
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        model = new MyModel();
        editModel = new EditModel();
        init();
    }

    private void init() {
        table.setSelectionModel(new ForcedListSelectionModel());
        table.setModel(model);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(2).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setMaxWidth(200);

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
            }
        });

        try {
            List<Order> os = dc.getAllOrders();
            for (Order o : os) {
                model.addOrder(o);
            }
        } catch (IOException | SQLException ex) {
            Logger.getLogger(OrdersViewer.class.getName()).log(Level.SEVERE, null, ex);
        }

        tableEdit.setSelectionModel(new ForcedListSelectionModel());
        tableEdit.setModel(editModel);
        tableEdit.getColumnModel().getColumn(0).setMaxWidth(70);
        tableEdit.getColumnModel().getColumn(2).setMaxWidth(40);
        tableEdit.getColumnModel().getColumn(3).setMaxWidth(70);

        InputMap ime = tableEdit.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap ame = tableEdit.getActionMap();

        ime.put(deleteKey, "Action.delete");
        ame.put("Action.delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                final int index = tableEdit.getSelectedRow();
                if (index == -1) {
                    return;
                }
            }
        });

    }

    public static void showWindow() {
        OrdersViewer window = new OrdersViewer();
        GUI.gui.internal.add(window);
        window.setVisible(true);
        try {
            window.setIcon(false);
            window.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WasteStockWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createOrder() {
        Supplier supplier = SupplierSelectDialog.showDialog(this);
        if (supplier == null) {
            return;
        }
        currentOrder = new Order(supplier, new LinkedList<>());
        editModel.setOrder(currentOrder);
        tabbed.setSelectedIndex(1);
    }

    private void saveOrder() {
        if (currentOrder.getId() == 0) {
            try {
                currentOrder = dc.addOrder(currentOrder);
            } catch (IOException | SQLException ex) {
                JOptionPane.showInternalMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            try {
                dc.updateOrder(currentOrder);
            } catch (IOException | SQLException ex) {
                JOptionPane.showInternalMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class MyModel implements TableModel {

        private final List<Order> orders;
        private final List<TableModelListener> listeners;

        public MyModel() {
            orders = new LinkedList<>();
            listeners = new LinkedList<>();
        }

        public void addOrder(Order p) {
            orders.add(p);
            alert(orders.size() - 1, orders.size() - 1);
        }

        public void removeOrder(int i) {
            orders.remove(i);
            alert(i, i);
        }

        public List<Order> getAllOrders() {
            return orders;
        }

        public void clear() {
            int size = orders.size();
            orders.clear();
            alert(0, size);
        }

        @Override
        public int getRowCount() {
            return orders.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int i) {
            switch (i) {
                case 0:
                    return "ID";
                case 1:
                    return "Supplier";
                case 2:
                    return "Sent";
                case 3:
                    return "Date";
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
                    return Boolean.class;
                case 3:
                    return Object.class;
                default:
                    break;
            }
            return Object.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int i) {
            Order o = orders.get(rowIndex);
            switch (i) {
                case 0:
                    return o.getId();
                case 1:
                    return o.getSupplier().getName();
                case 2:
                    return o.isSent();
                case 3:
                    return (o.isSent() ? o.getSendDate() : "");
                default:
                    break;
            }
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

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

    private class EditModel implements TableModel {

        private List<OrderItem> products;
        private final List<TableModelListener> listeners;

        public EditModel() {
            products = new LinkedList<>();
            listeners = new LinkedList<>();
        }

        public void addProduct(OrderItem p) {
            products.add(p);
            alert(products.size() - 1, products.size() - 1);
        }

        public void removeProduct(int i) {
            products.remove(i);
            alert(i, i);
        }

        public BigDecimal getTotal() {
            BigDecimal total = BigDecimal.ZERO;
            for (OrderItem i : products) {
                total = total.add(i.getPrice());
            }
            return total;
        }

        public List<OrderItem> getAllProducts() {
            return products;
        }

        public void setOrder(Order o) {
            products = o.getItems();
            alert(0, products.size() - 1);
            lblValue.setText("Total: £" + getTotal().setScale(2, 6));
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
            OrderItem p = products.get(rowIndex);
            switch (i) {
                case 0:
                    return p.getOrderCode();
                case 1:
                    return p.getName();
                case 2:
                    return p.getQuantity();
                case 3:
                    return "£" + p.getPrice().setScale(2, 6);
                default:
                    break;
            }
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 2) {
                products.get(rowIndex).setQuantity((int) aValue);
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

        tabbed = new javax.swing.JTabbedPane();
        panelOrders = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        btnCreate = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        panelEdit = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableEdit = new javax.swing.JTable();
        btnSend = new javax.swing.JButton();
        btnPrint = new javax.swing.JButton();
        btnAddProduct = new javax.swing.JButton();
        txtBarcode = new javax.swing.JTextField();
        lblValue = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Orders");

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        table.getTableHeader().setReorderingAllowed(false);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);

        btnCreate.setText("Create New Order");
        btnCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateActionPerformed(evt);
            }
        });

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelOrdersLayout = new javax.swing.GroupLayout(panelOrders);
        panelOrders.setLayout(panelOrdersLayout);
        panelOrdersLayout.setHorizontalGroup(
            panelOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOrdersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 763, Short.MAX_VALUE)
                    .addGroup(panelOrdersLayout.createSequentialGroup()
                        .addComponent(btnCreate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        panelOrdersLayout.setVerticalGroup(
            panelOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOrdersLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCreate)
                    .addComponent(btnClose))
                .addContainerGap())
        );

        tabbed.addTab("Orders", panelOrders);

        tableEdit.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(tableEdit);

        btnSend.setText("Send Order");
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }
        });

        btnPrint.setText("Print Order");

        btnAddProduct.setText("Add Product");
        btnAddProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddProductActionPerformed(evt);
            }
        });

        lblValue.setText("Total Value: £0.00");

        javax.swing.GroupLayout panelEditLayout = new javax.swing.GroupLayout(panelEdit);
        panelEdit.setLayout(panelEditLayout);
        panelEditLayout.setHorizontalGroup(
            panelEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEditLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 763, Short.MAX_VALUE)
                    .addGroup(panelEditLayout.createSequentialGroup()
                        .addComponent(btnSend)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPrint)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBarcode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddProduct)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblValue)))
                .addContainerGap())
        );
        panelEditLayout.setVerticalGroup(
            panelEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEditLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 428, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSend)
                    .addComponent(btnPrint)
                    .addComponent(btnAddProduct)
                    .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblValue))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbed.addTab("Edit Order", panelEdit);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbed)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbed)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateActionPerformed
        createOrder();
    }//GEN-LAST:event_btnCreateActionPerformed

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        int row = table.getSelectedRow();
        if (row == -1) {
            return;
        }
        if (SwingUtilities.isLeftMouseButton(evt)) {
            if (evt.getClickCount() == 2) {
                currentOrder = model.getAllOrders().get(row);
                editModel.setOrder(currentOrder);
                tabbed.setSelectedIndex(1);
            }
        }
    }//GEN-LAST:event_tableMouseClicked

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
        if (p.getStock() + quantity > p.getMaxStockLevel()) {
            if (JOptionPane.showInternalConfirmDialog(this, "Warning, this will take the item above its maximum stock level. Continue?", "Product Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }
        }
        OrderItem item = new OrderItem(p, quantity);
        editModel.addProduct(item);
        lblValue.setText("Total Value: £" + editModel.getTotal().setScale(2, 6));
        txtBarcode.setText("");
        saveOrder();
    }//GEN-LAST:event_btnAddProductActionPerformed

    private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendActionPerformed
        final ModalDialog dialog = new ModalDialog(this, "Send Order", "Sending order...");
        final Runnable run = () -> {
            try {
                currentOrder.setSent(true);
                currentOrder.setSendDate(new Date());
                saveOrder();
            } finally {
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
    private javax.swing.JButton btnCreate;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnSend;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblValue;
    private javax.swing.JPanel panelEdit;
    private javax.swing.JPanel panelOrders;
    private javax.swing.JTabbedPane tabbed;
    private javax.swing.JTable table;
    private javax.swing.JTable tableEdit;
    private javax.swing.JTextField txtBarcode;
    // End of variables declaration//GEN-END:variables
}
