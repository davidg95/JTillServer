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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class DepartmentSelectDialog extends javax.swing.JDialog {

    private static Department department;

    private final DataConnect dc;

    private final DefaultTableModel model;
    private List<Department> ctc;

    /**
     * Creates new form DepartmentSelectDialog
     */
    public DepartmentSelectDialog(Window parent) {
        super(parent);
        this.dc = GUI.gui.dc;
        initComponents();
        setLocationRelativeTo(parent);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setIconImage(GUI.icon);
        ctc = new ArrayList<>();
        model = (DefaultTableModel) tblDep.getModel();
        init();
    }

    public static Department showDialog(Component parent) {
        Window window = JOptionPane.getFrameForComponent(parent);
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        final DepartmentSelectDialog dialog = new DepartmentSelectDialog(window);
        department = null;
        dialog.setVisible(true);
        return department;
    }

    private void init() {
        tblDep.setSelectionModel(new ForcedListSelectionModel());
        showAllDepartments();
    }

    private void showAllDepartments() {
        try {
            ctc = dc.getAllDepartments();
            updateTable();
        } catch (IOException | SQLException ex) {
            Logger.getLogger(DepartmentSelectDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateTable() {
        model.setRowCount(0);
        for (Department d : ctc) {
            Object[] row = new Object[]{d.getId(), d.getName()};
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
        tblDep = new javax.swing.JTable();
        btnClose = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Select Department");
        setResizable(false);

        tblDep.setModel(new javax.swing.table.DefaultTableModel(
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
        tblDep.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblDepMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblDep);
        if (tblDep.getColumnModel().getColumnCount() > 0) {
            tblDep.getColumnModel().getColumn(0).setMinWidth(40);
            tblDep.getColumnModel().getColumn(0).setPreferredWidth(40);
            tblDep.getColumnModel().getColumn(0).setMaxWidth(40);
            tblDep.getColumnModel().getColumn(1).setResizable(false);
        }

        btnClose.setText("Cancel");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        jButton1.setText("Select");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        department = null;
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void tblDepMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDepMouseClicked
        int row = tblDep.getSelectedRow();
        if (row == -1) {
            return;
        }
        if (SwingUtilities.isLeftMouseButton(evt)) {
            department = ctc.get(row);
            if (evt.getClickCount() == 2) {
                setVisible(false);
            }
        }
    }//GEN-LAST:event_tblDepMouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (department != null) {
            setVisible(false);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblDep;
    // End of variables declaration//GEN-END:variables
}
