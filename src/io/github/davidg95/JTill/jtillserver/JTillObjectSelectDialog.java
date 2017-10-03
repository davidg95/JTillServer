/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class JTillObjectSelectDialog extends javax.swing.JDialog {

    private static JTillObjectSelectDialog dialog;
    private static final Logger LOG = Logger.getGlobal();
    private static JTillObject object;

    private final Collection<Class<?>> filter;

    private final DataConnect dc;
    private List<JTillObject> objects;
    private final DefaultTableModel model;

    /**
     * Creates new form JTillObjectSelectDialog
     */
    public JTillObjectSelectDialog(Window parent, DataConnect dc, String title, Class<?> E) {
        super(parent);
        this.dc = dc;
        filter = new LinkedList<Class<?>>();
        filter.add(E);
        objects = new ArrayList<>();
        initComponents();
        this.setIconImage(GUI.icon);
        setTitle(title);
        setModal(true);
        setLocationRelativeTo(parent);
        model = (DefaultTableModel) tblObjects.getModel();
        tblObjects.setModel(model);
        init();
    }

    public JTillObjectSelectDialog(Window parent, DataConnect dc, String title, Collection<Class<?>> filter) {
        super(parent);
        this.dc = dc;
        this.filter = filter;
        objects = new ArrayList<>();
        initComponents();
        setTitle(title);
        setModal(true);
        setLocationRelativeTo(parent);
        model = (DefaultTableModel) tblObjects.getModel();
        tblObjects.setModel(model);
        init();
    }

    public static JTillObject showDialog(Component parent, DataConnect dc, String title, Class<?> E) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        dialog = new JTillObjectSelectDialog(window, dc, title, E);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        object = null;
        dialog.setVisible(true);
        return object;
    }

    public static JTillObject showDialog(Component parent, DataConnect dc, String title, Collection<Class<?>> filter) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        dialog = new JTillObjectSelectDialog(window, dc, title, filter);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        object = null;
        dialog.setVisible(true);
        return object;
    }

    private void init() {
        try {
            for (Class<?> E : filter) {
                if (Product.class.equals(E)) {
                    objects.addAll(dc.getAllProducts());
                    continue;
                }
                if (Category.class.equals(E)) {
                    objects.addAll(dc.getAllCategorys());
                    continue;
                }
                if (Customer.class.equals(E)) {
                    objects.addAll(dc.getAllCustomers());
                    continue;
                }
                if (Tax.class.equals(E)) {
                    objects.addAll(dc.getAllTax());
                    continue;
                }
                if (Sale.class.equals(E)) {
                    objects.addAll(dc.getAllSales());
                    continue;
                }
                if (Staff.class.equals(E)) {
                    objects.addAll(dc.getAllStaff());
                    continue;
                }
                if (Plu.class.equals(E)) {
                    objects.addAll(dc.getAllPlus());
                    continue;
                }
                if (Discount.class.equals(E)) {
                    objects.addAll(dc.getAllDiscounts());
                    continue;
                }
                if (WasteItem.class.equals(E)) {
                    objects.addAll(dc.getAllWasteItems());
                    continue;
                }
                if (WasteReason.class.equals(E)) {
                    objects.addAll(dc.getAllWasteReasons());
                    continue;
                }
                if (WasteReport.class.equals(E)) {
                    objects.addAll(dc.getAllWasteReports());
                }
            }
            reloadTable();
        } catch (IOException | SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void reloadTable() {
        model.setRowCount(0);
        for (JTillObject j : objects) {
            Object[] row = new Object[]{j.getId(), j.getName()};
            model.addRow(row);
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
        tblObjects = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tblObjects.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "ID", "Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblObjects.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblObjectsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblObjects);
        if (tblObjects.getColumnModel().getColumnCount() > 0) {
            tblObjects.getColumnModel().getColumn(0).setResizable(false);
            tblObjects.getColumnModel().getColumn(1).setResizable(false);
        }

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 115, Short.MAX_VALUE)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnSearch)
                    .addComponent(jLabel1)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String terms = txtSearch.getText();

        if (terms.equals("")) {
            init();
            return;
        }

        List<JTillObject> newList = new ArrayList<>();

        for (JTillObject j : objects) {
            if (j.getName().toLowerCase().contains(terms.toLowerCase())) {
                newList.add(j);
            }
        }
        objects = newList;
        reloadTable();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void tblObjectsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblObjectsMouseClicked
        object = objects.get(tblObjects.getSelectedRow());
        if (evt.getClickCount() == 2) {
            this.setVisible(false);
        }
    }//GEN-LAST:event_tblObjectsMouseClicked

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        btnSearch.doClick();
    }//GEN-LAST:event_txtSearchActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSearch;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblObjects;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
