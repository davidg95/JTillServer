/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.DataConnect;
import io.github.davidg95.JTill.jtill.JTillException;
import io.github.davidg95.JTill.jtill.Plu;
import io.github.davidg95.JTill.jtill.Product;
import io.github.davidg95.JTill.jtill.Sale;
import io.github.davidg95.JTill.jtill.SaleItem;
import io.github.davidg95.JTill.jtill.Till;
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
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class ManualSaleWindow extends javax.swing.JInternalFrame {

    private final DataConnect dc;

    private Sale sale;

    private final DefaultTableModel model;

    private MyComboModel cmbModel;

    private final DecimalFormat df;

    /**
     * Creates new form ManualSaleDialog
     */
    public ManualSaleWindow() {
        dc = GUI.gui.dc;
        initComponents();
        setTitle("Manual Sale");
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        df = new DecimalFormat("0.00");
        model = (DefaultTableModel) table.getModel();
        init();
    }

    public static void showWindow() {
        ManualSaleWindow window = new ManualSaleWindow();
        GUI.gui.internal.add(window);
        window.setVisible(true);
        JOptionPane.showMessageDialog(window, "This is a beta feature", "BETA Feature!", JOptionPane.WARNING_MESSAGE);
    }

    private void init() {
        model.setRowCount(0);
        try {
            cmbModel = new MyComboModel(dc.getAllTills());
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex);
        }
        cmbTerminal.setModel(cmbModel);
        sale = new Sale(((Till) cmbTerminal.getSelectedItem()).getId(), GUI.staff.getId());
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

    private void addItem(Product p, int q) {
        sale.addItem(p, q);
        refreshTable();
    }

    private void refreshTable() {
        model.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (SaleItem si : sale.getSaleItems()) {
            try {
                final Product p = (Product) si.getItem();
                final Plu pl = dc.getPluByProduct(p.getId());
                si.setTotalPrice(p.getPrice().multiply(new BigDecimal(si.getQuantity())).setScale(2).toString());
                Object[] s = new Object[]{pl.getCode(), p.getLongName(), si.getQuantity(), si.getTotalPrice()};
                total = total.add(si.getPrice().multiply(new BigDecimal(si.getQuantity())));
                model.addRow(s);
            } catch (IOException | JTillException ex) {
                Logger.getLogger(ManualSaleWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        lblTotal.setText("Total: £" + total.setScale(2).toString());
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnComplete)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCancel))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnAdd)
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblTotal)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE))))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbTerminal, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbTerminal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
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
        final Product p = ProductSelectDialog.showDialog(this, true);
        if (p == null) {
            return;
        }
        if (p.isOpen()) {
            double price = Double.parseDouble(JOptionPane.showInternalInputDialog(GUI.gui.internal, "Enter price", "Sale", JOptionPane.PLAIN_MESSAGE));
            p.setPrice(new BigDecimal(Double.toString(price)));
        }
        int amount = Integer.parseInt((String) JOptionPane.showInternalInputDialog(GUI.gui.internal, "Enter quantity", "Sale", JOptionPane.PLAIN_MESSAGE, null, null, "1"));
        this.addItem(p, amount);
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnCompleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCompleteActionPerformed
        try {
            sale.complete();
            sale.setDate(new Date());
            sale.setCustomerID(1);
            sale.setStaff(GUI.staff);
            sale.setMop(Sale.MOP_CASH);
            dc.addSale(sale);
            init();
            JOptionPane.showInternalMessageDialog(this, "Sale complete", "Sale", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | SQLException ex) {
            JOptionPane.showInternalMessageDialog(this, ex, "Sale", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(ManualSaleWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnCompleteActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCancelActionPerformed

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        if (table.getSelectedRow() == -1) {
            return;
        }
        SaleItem item = sale.getSaleItems().get(table.getSelectedRow());
        if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem quantity = new JMenuItem("Change Quantity");
            JMenuItem remove = new JMenuItem("Remove");
            quantity.addActionListener((ActionEvent) -> {
                try {
                    int q = Integer.parseInt(JOptionPane.showInternalInputDialog(this, "Enter new quantity", "Quantity for " + item.getName(), JOptionPane.PLAIN_MESSAGE));
                    item.setQuantity(q);
                    refreshTable();
                } catch (NumberFormatException e) {
                    JOptionPane.showInternalMessageDialog(this, e, "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            remove.addActionListener((ActionEvent) -> {
                sale.voidItem(item);
                refreshTable();
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
