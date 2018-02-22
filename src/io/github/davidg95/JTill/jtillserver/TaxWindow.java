/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author David
 */
public class TaxWindow extends javax.swing.JInternalFrame {

    public static TaxWindow frame;

    private final DataConnect dc;
    private Tax tax;

    private MyModel model;

    /**
     * Creates new form TaxWindow
     */
    public TaxWindow() {
        this.dc = GUI.gui.dc;
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        initComponents();
        showAllTaxes();
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.setSelectionModel(new ForcedListSelectionModel());
    }

    public static void showTaxWindow() {
        if (frame == null || frame.isClosed()) {
            frame = new TaxWindow();
            GUI.gui.internal.add(frame);
        }
        frame.setVisible(true);
        try {
            frame.setIcon(false);
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(TaxWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showAllTaxes() {
        try {
            model = new MyModel(Tax.getAll());
            table.setModel(model);
        } catch (IOException | SQLException ex) {
            showError(ex);
        }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e, "Tax", JOptionPane.ERROR_MESSAGE);
    }

    private class MyModel implements TableModel {

        private List<Tax> taxes;
        private final List<TableModelListener> listeners;

        public MyModel(List<Tax> taxes) {
            this.taxes = taxes;
            this.listeners = new LinkedList<>();
        }

        public void addTax(Tax t) throws IOException, SQLException {
            dc.addTax(t);
            taxes.add(t);
            alertAll();
        }

        public void removeTax(Tax t) throws IOException, SQLException, JTillException {
            dc.removeTax(t);
            taxes.remove(t);
            alertAll();
        }

        public Tax getTax(int i) {
            return taxes.get(i);
        }

        public List<Tax> getAll() {
            return taxes;
        }

        public void filter(String terms) {
            List<Tax> newList = new LinkedList<>();
            for (Tax t : taxes) {
                if (t.getName().toLowerCase().contains(terms.toLowerCase())) {
                    newList.add(t);
                }
            }
            taxes = newList;
            alertAll();
        }

        public void showAll() throws IOException, SQLException {
            taxes = Tax.getAll();
            alertAll();
        }

        @Override
        public int getRowCount() {
            return taxes.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "ID";
                case 1:
                    return "Name";
                case 2:
                    return "Value";
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Object.class;
                case 1:
                    return String.class;
                case 2:
                    return Double.class;
                default:
                    return Object.class;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex != 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            final Tax t = taxes.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return t.getId();
                case 1:
                    return t.getName();
                case 2:
                    return t.getValue();
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Tax t = taxes.get(rowIndex);
            switch (columnIndex) {
                case 1:
                    t.setName((String) aValue);
                    break;
                case 2:
                    double value = (double) aValue;
                    if (value < 0 || value > 100) {
                        JOptionPane.showMessageDialog(TaxWindow.this, "Value msut be between 0 and 100", "Tax", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    t.setValue(value);
                    break;
                default:
                    return;
            }
            try {
                t.save();
            } catch (IOException | SQLException ex) {
                JOptionPane.showMessageDialog(TaxWindow.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        public void alertAll() {
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

    private void delete(Tax t) {
        int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove the following tax?\n-" + t + "\nAll products in this tax will be set to the default tax (0%)", "Remove Tax", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                model.removeTax(t);
            } catch (IOException | SQLException | JTillException ex) {
                showError(ex);
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
        table = new javax.swing.JTable();
        btnAdd = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setTitle("Tax");

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
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
        table.getTableHeader().setReorderingAllowed(false);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setMinWidth(40);
            table.getColumnModel().getColumn(0).setPreferredWidth(40);
            table.getColumnModel().getColumn(0).setMaxWidth(40);
            table.getColumnModel().getColumn(1).setResizable(false);
            table.getColumnModel().getColumn(2).setResizable(false);
        }

        btnAdd.setText("Add New");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        jLabel3.setText("Search:");

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

        btnRemove.setText("Remove");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemove)
                        .addGap(76, 76, 76)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(jLabel3)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)
                    .addComponent(btnAdd)
                    .addComponent(btnRemove))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        Tax t;
        try {
            String name = JOptionPane.showInputDialog(this, "Enter name for new tax class", "New Tax Class", JOptionPane.PLAIN_MESSAGE);
            if (name == null || name.isEmpty()) {
                return;
            }
            String val = JOptionPane.showInputDialog(this, "Enter value for new tax class", "New Tax Class", JOptionPane.PLAIN_MESSAGE);
            if (val == null || val.isEmpty()) {
                return;
            }
            if (!Utilities.isNumber(val)) {
                JOptionPane.showMessageDialog(this, "Must enter a number for value", "New Tax", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double value = Double.parseDouble(val);
            if (value < 0 || value > 100) {
                JOptionPane.showMessageDialog(this, "Value must be between 0 and 100", "New Tax Class", JOptionPane.ERROR_MESSAGE);
                return;
            }
            t = new Tax(name, value);
            try {
                model.addTax(t);
            } catch (IOException | SQLException ex) {
                showError(ex);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Fill out all required fields", "New Tax", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String terms = txtSearch.getText();

        if (terms.isEmpty()) {
            try {
                model.showAll();
            } catch (IOException | SQLException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        model.filter(terms);
    }//GEN-LAST:event_btnSearchActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        btnSearch.doClick();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        int index = table.getSelectedRow();
        if (index == -1) {
            return;
        }
        tax = model.getTax(index);
        if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem remove = new JMenuItem("Remove");
            remove.addActionListener((event) -> {
                delete(tax);
            });

            if (tax.getId() == 1) {
                remove.setEnabled(false);
            }

            menu.add(remove);
            menu.show(table, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tableMouseClicked

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        if (tax == null) {
            return;
        }
        delete(tax);
    }//GEN-LAST:event_btnRemoveActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnSearch;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
