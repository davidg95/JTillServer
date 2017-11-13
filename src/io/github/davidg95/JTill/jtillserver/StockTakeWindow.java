/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import com.sun.glass.events.KeyEvent;
import io.github.davidg95.JTill.jtill.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class StockTakeWindow extends javax.swing.JInternalFrame {

    private static StockTakeWindow window;

    private final DataConnect dc;
    private List<Product> currentTableContents;
    private final DefaultTableModel model;

    /**
     * Creates new form StockTakeWindow
     */
    public StockTakeWindow() {
        this.dc = GUI.gui.dc;
        initComponents();
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        currentTableContents = new ArrayList<>();
        model = (DefaultTableModel) table.getModel();
        table.setModel(model);
        init();
    }

    public static void showWindow() {
        window = new StockTakeWindow();
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
        this.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                if (!currentTableContents.isEmpty()) {
                    int res = JOptionPane.showInternalConfirmDialog(StockTakeWindow.this, "Do you want to save the current report?", "Save", JOptionPane.YES_NO_OPTION);
                    if (res == JOptionPane.YES_OPTION) {
                        GUI.gui.savedReports.put("STO", currentTableContents);
                    }
                }
            }
        });
        if (GUI.gui.savedReports.containsKey("STO")) {
            currentTableContents = GUI.gui.savedReports.get("STO");
            updateTable();
            GUI.gui.savedReports.remove("STO");
        }
        InputMap im = table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = table.getActionMap();

        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);

        im.put(enterKey, "Action.enter");
        am.put("Action.enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                final int index = table.getSelectedRow();
                final Product p = currentTableContents.get(index);
                if (index == -1) {
                    return;
                }
                if (JOptionPane.showInternalConfirmDialog(StockTakeWindow.this, "Are you sure you want to remove this item?\n" + p, "Stock Item", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    currentTableContents.remove(index);
                    updateTable();
                }
            }
        });
    }

    public void updateTable() {
        model.setRowCount(0);
        for (Product p : currentTableContents) {
            Object[] row = new Object[]{p.getId(), p.getLongName(), p.getBarcode(), p.getStock()};
            model.addRow(row);
        }
        if (currentTableContents.isEmpty()) {
            btnSubmit.setEnabled(false);
        } else {
            btnSubmit.setEnabled(true);
        }
    }

    public void setTable(List<Product> list) {
        model.setRowCount(0);
        for (Product p : list) {
            Object[] row = new Object[]{p.getId(), p.getLongName(), p.getBarcode(), p.getStock()};
            model.addRow(row);
        }
    }

    public void addRow(Product p) {
        currentTableContents.add(p);
        updateTable();
    }

    private Product checkProductAlreadyExists(Product p) {
        for (Product pr : currentTableContents) {
            if (pr.equals(p)) {
                return pr;
            }
        }
        return null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        radName = new javax.swing.JRadioButton();
        radBarcode = new javax.swing.JRadioButton();
        btnSearch = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        btnAddProduct = new javax.swing.JButton();
        btnAddCSV = new javax.swing.JButton();
        btnSubmit = new javax.swing.JButton();

        setTitle("Stock Take");

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Product", "Barcode", "Qty."
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Integer.class
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
            table.getColumnModel().getColumn(3).setMinWidth(40);
            table.getColumnModel().getColumn(3).setPreferredWidth(40);
            table.getColumnModel().getColumn(3).setMaxWidth(40);
        }

        jLabel1.setText("Search:");

        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        buttonGroup1.add(radName);
        radName.setText("Name");

        buttonGroup1.add(radBarcode);
        radBarcode.setSelected(true);
        radBarcode.setText("Barcode");

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
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radBarcode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                        .addComponent(btnAddCSV)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddProduct)
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radName)
                    .addComponent(radBarcode)
                    .addComponent(btnSearch)
                    .addComponent(btnClose)
                    .addComponent(btnAddProduct)
                    .addComponent(btnAddCSV)
                    .addComponent(btnSubmit))
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
        Product p = ProductSelectDialog.showDialog(this);
        Product pr = checkProductAlreadyExists(p);
        if (pr != null) {
            String input = JOptionPane.showInternalInputDialog(StockTakeWindow.this, "Enter new quantity", "Stock Take", JOptionPane.PLAIN_MESSAGE);
            if (!Utilities.isNumber(input)) {
                JOptionPane.showInternalMessageDialog(StockTakeWindow.this, "Must enter a number", "Stock Take", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int val = Integer.parseInt(input);
            if (val > 0) {
                pr.setStock(val);
                updateTable();
                return;
            } else {
                JOptionPane.showInternalMessageDialog(StockTakeWindow.this, "Must enter a value greater than zero", "Stock Take", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        if (p == null) {
            return;
        }
        String val = JOptionPane.showInternalInputDialog(StockTakeWindow.this, "Enter new stock level", "Enter stock level", JOptionPane.PLAIN_MESSAGE);
        if (val == null || val.isEmpty()) {
            return;
        }
        if (Utilities.isNumber(val)) {
            int stock = Integer.parseInt(val);
            if (stock < 0) {
                JOptionPane.showInternalMessageDialog(StockTakeWindow.this, "Value must be zero or greater", "Stock Take", JOptionPane.ERROR_MESSAGE);
                return;
            }
            p.setStock(stock);
            addRow(p);
        } else {
            JOptionPane.showInternalMessageDialog(StockTakeWindow.this, "You must enter a number", "Stock Take", JOptionPane.ERROR_MESSAGE);
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
                        JOptionPane.showInternalMessageDialog(StockTakeWindow.this, "File is not recognised", "Add CSV", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String barcode = items[0];
                    int quantity = Integer.parseInt(items[1]);

                    Product product;
                    try {
                        product = dc.getProductByBarcode(barcode);

                        product.setStock(quantity);

                        currentTableContents.add(product);
                    } catch (ProductNotFoundException ex) {
                        nFound++;
                    }
                }
                updateTable();
                if (nFound > 0) {
                    JOptionPane.showInternalMessageDialog(StockTakeWindow.this, nFound + " barcodes could not be found", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (FileNotFoundException ex) {
                JOptionPane.showInternalMessageDialog(StockTakeWindow.this, ex, "File Not Found", JOptionPane.ERROR_MESSAGE);
            } catch (IOException | SQLException ex) {
                JOptionPane.showInternalMessageDialog(StockTakeWindow.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnAddCSVActionPerformed

    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        if (currentTableContents.isEmpty()) {
            return;
        }
        if (JOptionPane.showInternalConfirmDialog(StockTakeWindow.this, "Are you sure you want to submit this stock take?", "Stock Take", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            boolean zeroRest = JOptionPane.showInternalConfirmDialog(this, "Do you want unadded items to have the stock level set to zero?", "Stock Take", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
            try {
                dc.submitStockTake(currentTableContents, zeroRest);
                currentTableContents.clear();
                updateTable();
                JOptionPane.showInternalMessageDialog(StockTakeWindow.this, "Stock take submitted", "Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | SQLException ex) {
                JOptionPane.showMessageDialog(this, ex + "\nStock take not submitted", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnSubmitActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        btnSearch.doClick();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        if (txtSearch.getText().equals("")) {
            updateTable();
            return;
        }
        List<Product> newList = new ArrayList<>();
        for (Product p : currentTableContents) {
            if (radName.isSelected()) {
                if (p.getName().contains(txtSearch.getText()) || p.getLongName().contains(txtSearch.getText())) {
                    newList.add(p);
                }
            } else {
                if (p.getBarcode().equals(txtSearch.getText())) {
                    newList.add(p);
                }
            }
        }
        if (newList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results", "Search", JOptionPane.WARNING_MESSAGE);
        }
        setTable(newList);
        txtSearch.setSelectionStart(0);
        txtSearch.setSelectionEnd(txtSearch.getText().length());
    }//GEN-LAST:event_btnSearchActionPerformed

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        final int index = table.getSelectedRow();
        final Product p = currentTableContents.get(index);
        if (SwingUtilities.isLeftMouseButton(evt)) {
            if (evt.getClickCount() == 2) {
                String input = JOptionPane.showInternalInputDialog(StockTakeWindow.this, "Enter new quantity", "Stock Take", JOptionPane.PLAIN_MESSAGE);
                if (!Utilities.isNumber(input)) {
                    JOptionPane.showInternalMessageDialog(StockTakeWindow.this, "A number must be entered", "Stock Take", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int val = Integer.parseInt(input);
                if (val > 0) {
                    p.setStock(val);
                    updateTable();
                } else {
                    JOptionPane.showInternalMessageDialog(StockTakeWindow.this, "Must be a value greater than zero", "Stock Take", JOptionPane.WARNING_MESSAGE);
                }
            }
        } else if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu m = new JPopupMenu();
            JMenuItem i = new JMenuItem("Change Quantity");
            JMenuItem i2 = new JMenuItem("Remove Item");
            i.addActionListener((ActionEvent e) -> {
                String input = JOptionPane.showInternalInputDialog(StockTakeWindow.this, "Enter new quantity", "Stock Take", JOptionPane.PLAIN_MESSAGE);
                if (!Utilities.isNumber(input)) {
                    JOptionPane.showInternalMessageDialog(StockTakeWindow.this, "A number must be entered", "Stock Take", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int val = Integer.parseInt(input);
                if (val > 0) {
                    p.setStock(val);
                    updateTable();
                } else {
                    JOptionPane.showInternalMessageDialog(StockTakeWindow.this, "Must be a value greater than zero", "Stock Take", JOptionPane.WARNING_MESSAGE);
                }
            });
            i2.addActionListener((ActionEvent e) -> {
                if (index == -1) {
                    return;
                }
                if (JOptionPane.showInternalConfirmDialog(StockTakeWindow.this, "Are you sure you want to remove this item?\n" + p, "Stock Item", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    model.removeRow(index);
                    currentTableContents.remove(index);
                }
            });
            m.add(i);
            m.add(i2);
            m.show(table, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tableMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCSV;
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSubmit;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton radBarcode;
    private javax.swing.JRadioButton radName;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
