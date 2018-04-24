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
public class DepartmentsWindow extends javax.swing.JInternalFrame {

    private static DepartmentsWindow window;

    private final JTill jtill;

    private Department department;

    private MyModel model;

    /**
     * Creates new form DepartmentWindow
     */
    public DepartmentsWindow(JTill jtill) {
        this.jtill = jtill;
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        initComponents();
        setTable();
    }

    public static void showWindow(JTill jtill) {
        if (window == null || window.isClosed()) {
            window = new DepartmentsWindow(jtill);
            GUI.gui.internal.add(window);
        }
        window.setVisible(true);
        try {
            window.setIcon(false);
            window.setSelected(true);
            window.setTable();
        } catch (PropertyVetoException ex) {
            Logger.getLogger(DepartmentsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setTable() {
        try {
            List<Department> deps = jtill.getDataConnection().getAllDepartments();
            model = new MyModel(deps);
            tblDep.setModel(model);
            tblDep.getColumnModel().getColumn(0).setMaxWidth(40);
            tblDep.setSelectionModel(new ForcedListSelectionModel());
        } catch (SQLException | IOException ex) {
            Logger.getLogger(DepartmentsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void removeDepartment(Department dep) {
        try {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove " + dep.getName() + "?", "Remove Department", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                model.removeDepartment(dep);
                JOptionPane.showMessageDialog(this, "Department " + dep.getName() + " removed", "Remove Department", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException | SQLException | JTillException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renameDepartment(Department dep) {
        String name = JOptionPane.showInputDialog(this, "Enter new name for " + dep.getName(), "Rename Department", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.isEmpty()) {
            return;
        }
        dep.setName(name);
        try {
            model.updateDepartment(dep);
        } catch (IOException | SQLException | JTillException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class MyModel implements TableModel {

        private final List<Department> departments;
        private final List<TableModelListener> listeners;

        public MyModel(List<Department> deps) {
            departments = deps;
            listeners = new LinkedList<>();
        }

        public Department getDepartment(int id) {
            for (Department d : departments) {
                if (d.getId() == id) {
                    return d;
                }
            }
            return null;
        }

        public Department getSelected() {
            int row = tblDep.getSelectedRow();
            if (row == -1) {
                return null;
            }
            return departments.get(row);
        }

        public void addDepartment(Department d) throws IOException, SQLException {
            d = jtill.getDataConnection().addDepartment(d);
            departments.add(d);
            alertAll();
        }

        public void removeDepartment(Department d) throws IOException, SQLException, JTillException {
            for (int i = 0; i < departments.size(); i++) {
                Department dep = departments.get(i);
                if (dep.getId() == d.getId()) {
                    jtill.getDataConnection().removeDepartment(d.getId());
                    departments.remove(i);
                    alertAll();
                    return;
                }
            }
        }

        public void updateDepartment(Department d) throws IOException, SQLException, JTillException {
            jtill.getDataConnection().updateDepartment(d);
            alertAll();
        }

        @Override
        public int getRowCount() {
            return departments.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int i) {
            switch (i) {
                case 0:
                    return "ID";
                case 1:
                    return "Name";
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0: {
                    return Integer.class;
                }
                case 1: {
                    return String.class;
                }
                default: {
                    return Object.class;
                }
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Department d = departments.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return d.getId();
                case 1:
                    return d.getName();
                default:
                    return "";
            }
        }

        private void alertAll() {
            for (TableModelListener l : listeners) {
                l.tableChanged(new TableModelEvent(this));
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 1) {
                Department d = departments.get(rowIndex);
                d.setName((String) aValue);
                try {
                    d.save();
                } catch (IOException | SQLException ex) {
                    JOptionPane.showMessageDialog(DepartmentsWindow.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
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
        tblDep = new javax.swing.JTable();
        btnNewDepartment = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setTitle("Edit Departments");

        tblDep.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

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
        tblDep.getTableHeader().setReorderingAllowed(false);
        tblDep.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblDepMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblDep);
        if (tblDep.getColumnModel().getColumnCount() > 0) {
            tblDep.getColumnModel().getColumn(0).setMaxWidth(40);
            tblDep.getColumnModel().getColumn(1).setResizable(false);
        }

        btnNewDepartment.setText("Create new department");
        btnNewDepartment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewDepartmentActionPerformed(evt);
            }
        });

        btnRemove.setText("Remove Selected");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnNewDepartment)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnNewDepartment)
                    .addComponent(btnRemove))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNewDepartmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewDepartmentActionPerformed
        String name = JOptionPane.showInputDialog(this, "Enter name for new department", "New Department", JOptionPane.PLAIN_MESSAGE);

        if (name == null) {
            return;
        }
        if (name.equals("")) {
            JOptionPane.showMessageDialog(this, "Cannot have a null value", "New Department", JOptionPane.ERROR_MESSAGE);
            return;
        }

        department = new Department(name);
        try {
            model.addDepartment(department);
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex);
        }
    }//GEN-LAST:event_btnNewDepartmentActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        Department dep = model.getSelected();
        removeDepartment(dep);
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void tblDepMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDepMouseClicked
        department = model.getSelected();
        if (SwingUtilities.isRightMouseButton(evt)) {
            if (department == null) {
                return;
            }
            JPopupMenu menu = new JPopupMenu();

            JMenuItem rename = new JMenuItem("Rename");
            JMenuItem remove = new JMenuItem("Remove");

            rename.addActionListener((event) -> {
                renameDepartment(department);
            });
            remove.addActionListener((event) -> {
                removeDepartment(department);
            });

            menu.add(rename);
            menu.addSeparator();
            menu.add(remove);
            menu.show(tblDep, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tblDepMouseClicked

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnNewDepartment;
    private javax.swing.JButton btnRemove;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblDep;
    // End of variables declaration//GEN-END:variables
}
