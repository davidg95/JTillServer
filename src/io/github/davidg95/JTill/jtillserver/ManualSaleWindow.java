/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author David
 */
public class ManualSaleWindow extends javax.swing.JInternalFrame {

    private final JTill jtill;

    private MyModel model;

    private MyComboModel cmbModel;

    private final DecimalFormat df;

    /**
     * Creates new form ManualSaleDialog
     */
    public ManualSaleWindow(JTill jtill) {
        this.jtill = jtill;
        initComponents();
        setTitle("Manual Sale");
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        df = new DecimalFormat("0.00");
        init();
    }

    public static void showWindow(JTill jtill) {
        ManualSaleWindow window = new ManualSaleWindow(jtill);
        GUI.gui.internal.add(window);
        window.setVisible(true);
        JOptionPane.showMessageDialog(window, "This is a beta feature", "BETA Feature!", JOptionPane.WARNING_MESSAGE);
    }

    private void init() {
        try {
            List<Till> tills = jtill.getDataConnection().getAllTills();
            if (tills.isEmpty()) {
                JOptionPane.showMessageDialog(this, "You have not set up any terminals yet", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            cmbModel = new MyComboModel(jtill.getDataConnection().getAllTills());
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex);
        }
        cmbTerminal.setModel(cmbModel);
        Sale sale = new Sale(((Till) cmbTerminal.getSelectedItem()), GUI.staff);
        model = new MyModel(sale);
        table.setModel(model);
        lblTotal.setText("Total: £0.00");
    }

    private class MyComboModel implements ComboBoxModel {

        private final List<Till> terminals;

        private int currentItem;

        private final List<ListDataListener> listeners;

        public MyComboModel(List<Till> t) {
            terminals = t;
            currentItem = 0;
            listeners = new LinkedList<>();
        }

        @Override
        public void setSelectedItem(Object anItem) {
            for (int i = 0; i < terminals.size(); i++) {
                if (terminals.get(i).equals((Till) anItem)) {
                    currentItem = i;
                    return;
                }
            }
        }

        @Override
        public Object getSelectedItem() {
            return terminals.get(currentItem);
        }

        @Override
        public int getSize() {
            return terminals.size();
        }

        @Override
        public Object getElementAt(int index) {
            return terminals.get(index);
        }

        private void alertAll(int type, int i1, int i2) {
            for (ListDataListener l : listeners) {
                l.contentsChanged(new ListDataEvent(this, type, i1, i2));
            }
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            listeners.add(l);
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            listeners.remove(l);
        }
    }

    private class MyModel implements TableModel {

        private final Sale sale;
        private final List<TableModelListener> listeners;

        public MyModel(Sale s) {
            this.sale = s;
            listeners = new LinkedList<>();
        }

        public void addItem(Product p, BigDecimal price, int quantity) {
            sale.addItem(p, price, quantity);
            alertAll();
        }

        public void removeItem(int i) {
            sale.getSaleItems().remove(i);
            alertAll();
        }

        public void removeItem(SaleItem i) {
            sale.voidItem(i);
            alertAll();
        }

        public SaleItem getItem(int i) {
            return sale.getSaleItems().get(i);
        }

        public boolean isEmpty() {
            return sale.getSaleItems().isEmpty();
        }

        public Sale getSale() {
            return sale;
        }

        @Override
        public int getRowCount() {
            return sale.getSaleItems().size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0: {
                    return "Barcode";
                }
                case 1: {
                    return "Name";
                }
                case 2: {
                    return "Quantity";
                }
                case 3: {
                    return "Total";
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if(columnIndex == 2){
                return Integer.class;
            }
            return Object.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SaleItem item = sale.getSaleItems().get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return item.getProduct().getBarcode();
                }
                case 1: {
                    return item.getProduct().getLongName();
                }
                case 2: {
                    return item.getQuantity();
                }
                case 3: {
                    return "£" + item.getTotalPrice();
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 2) {
                sale.getSaleItems().get(rowIndex).setQuantity((int) aValue);
                alertAll();
            }
        }

        private void alertAll() {
            for (TableModelListener l : listeners) {
                l.tableChanged(new TableModelEvent(this));
            }
            BigDecimal total = BigDecimal.ZERO;
            for (SaleItem si : sale.getSaleItems()) {
                final Product p = (Product) si.getProduct();
                total = total.add(si.getIndividualPrice().multiply(new BigDecimal(si.getQuantity())));
            }
            lblTotal.setText("Total: £" + total.setScale(2).toString());
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
        btnAdd = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        btnComplete = new javax.swing.JButton();
        lblTotal = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        cmbTerminal = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Barcode", "Name", "Quantity", "Total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table.getTableHeader().setReorderingAllowed(false);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setResizable(false);
            table.getColumnModel().getColumn(1).setResizable(false);
            table.getColumnModel().getColumn(2).setResizable(false);
            table.getColumnModel().getColumn(3).setResizable(false);
        }

        btnAdd.setText("Add Item");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        btnComplete.setText("Complete Sale");
        btnComplete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCompleteActionPerformed(evt);
            }
        });

        lblTotal.setText("Total: £0.00");

        jLabel1.setText("Terminal:");

        cmbTerminal.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnComplete))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAdd)
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblTotal)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbTerminal, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbTerminal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdd))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnComplete))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        final Product p = ProductSelectDialog.showDialog(this, jtill);
        if (p == null) {
            return;
        }
        BigDecimal price;
        if (p.isOpen()) {
            price = new BigDecimal(JOptionPane.showInputDialog(this, "Enter price", "Sale", JOptionPane.PLAIN_MESSAGE));
        } else{
            price = p.getPrice();
        }
        int amount = Integer.parseInt((String) JOptionPane.showInputDialog(this, "Enter quantity", "Sale", JOptionPane.PLAIN_MESSAGE, null, null, "1"));
        model.addItem(p, price, amount);
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnCompleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCompleteActionPerformed
        if (model.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items in sale", "Complete Sale", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Sale s = model.getSale();
            s.complete();
            s.setDate(new Date());
            s.setCustomer(null);
            s.setStaff(GUI.staff);
            s.setMop(Sale.MOP_CASH);
            jtill.getDataConnection().addSale(model.getSale());
            init();
            JOptionPane.showMessageDialog(this, "Sale complete", "Sale", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Sale", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(ManualSaleWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnCompleteActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        if (model.isEmpty()) {
            if (JOptionPane.showConfirmDialog(this, "Warning! This will abandon the current sale, continue?", "Abandon Sale", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                setVisible(false);
            }
        }
    }//GEN-LAST:event_btnCancelActionPerformed

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        if (table.getSelectedRow() == -1) {
            return;
        }
        SaleItem item = model.getSale().getSaleItems().get(table.getSelectedRow());
        if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem quantity = new JMenuItem("Change Quantity");
            JMenuItem remove = new JMenuItem("Remove");
            quantity.addActionListener((ActionEvent) -> {
                try {
                    int q = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter new quantity", "Quantity for " + item.getProduct().getLongName(), JOptionPane.PLAIN_MESSAGE));
                    item.setQuantity(q);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, e, "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            remove.addActionListener((ActionEvent) -> {
                model.removeItem(item);
            });

            menu.add(quantity);
            menu.add(remove);
            menu.show(table, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tableMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnComplete;
    private javax.swing.JComboBox<String> cmbTerminal;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
