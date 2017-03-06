/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Image;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author 1301480
 */
public class StaffWindow extends javax.swing.JFrame {

    public static StaffWindow frame;

    private final DataConnect dc;

    private Staff staff;

    private final DefaultTableModel model;
    private List<Staff> currentTableContents;

    /**
     * Creates new form StaffWindow
     */
    public StaffWindow(DataConnect dc, Image icon) {
        this.dc = dc;
        this.setIconImage(icon);
        initComponents();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        currentTableContents = new ArrayList<>();
        model = (DefaultTableModel) tableStaff.getModel();
        showAllStaff();
    }

    public static void showStaffListWindow(DataConnect dc, Image icon) {
        if (frame == null) {
            frame = new StaffWindow(dc, icon);
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        update();
        frame.setCurrentStaff(null);
        frame.setVisible(true);
    }

    public static void update() {
        if (frame != null) {
            frame.showAllStaff();
        }
    }

    private void updateTable() {
        model.setRowCount(0);

        for (Staff s : currentTableContents) {
            Object[] str = new Object[]{s.getId(), s.getName(), s.getPosition(), s.getUsername()};
            model.addRow(str);
        }

        tableStaff.setModel(model);
    }

    private void showAllStaff() {
        try {
            currentTableContents = dc.getAllStaff();
        } catch (IOException | SQLException ex) {
            showError(ex);
        }
        updateTable();
    }

    private void editStaff() {
        int selectedRow = tableStaff.getSelectedRow();
        if (selectedRow != -1) {
            Staff s = currentTableContents.get(selectedRow);
            StaffDialog.showEditStaffDialog(this, dc, s);
            updateTable();
        }
    }

    private void setCurrentStaff(Staff s) {
        if (s == null) {
            txtName.setText("");
            txtUsername.setText("");
            cmbPosition.setSelectedIndex(0);
            staff = null;
        } else {
            this.staff = s;
            txtName.setText(s.getName());
            txtUsername.setText(s.getUsername());
            int p = s.getPosition();
            int index = p - 1;
            cmbPosition.setSelectedIndex(index);
        }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e, "Staff", JOptionPane.ERROR_MESSAGE);
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
        tableStaff = new javax.swing.JTable();
        btnAddStaff = new javax.swing.JButton();
        btnRemoveStaff = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        btnShowAll = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        cmbPosition = new javax.swing.JComboBox<>();
        txtUsername = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        radName = new javax.swing.JRadioButton();
        radID = new javax.swing.JRadioButton();
        btnSearch = new javax.swing.JButton();
        btnPassword = new javax.swing.JButton();

        setTitle("Manage Staff");

        tableStaff.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Position", "Username"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
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
        tableStaff.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableStaffMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(tableStaff);

        btnAddStaff.setText("Add Staff");
        btnAddStaff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddStaffActionPerformed(evt);
            }
        });

        btnRemoveStaff.setText("Remove Staff");
        btnRemoveStaff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveStaffActionPerformed(evt);
            }
        });

        btnSave.setText("Save Changes");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnShowAll.setText("Show All Staff");
        btnShowAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowAllActionPerformed(evt);
            }
        });

        jLabel3.setText("Username:");

        txtName.setNextFocusableComponent(cmbPosition);

        cmbPosition.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Assisstant", "Supervisor", "Manager", "Area Manager"}));

        jLabel1.setText("Name:");

        jLabel2.setText("Position:");

        jLabel6.setText("Search:");

        buttonGroup1.add(radName);
        radName.setSelected(true);
        radName.setText("Name");

        buttonGroup1.add(radID);
        radID.setText("ID");

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        btnPassword.setText("Password");
        btnPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPasswordActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAddStaff)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSave))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnRemoveStaff)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnShowAll))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnPassword)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtName)
                                .addComponent(cmbPosition, 0, 139, Short.MAX_VALUE)
                                .addComponent(txtUsername)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radID)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 597, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbPosition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPassword)
                        .addGap(86, 86, 86)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAddStaff)
                            .addComponent(btnSave))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnRemoveStaff)
                            .addComponent(btnShowAll))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(jLabel6)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radName)
                    .addComponent(radID)
                    .addComponent(btnSearch))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddStaffActionPerformed
//        if (staff == null) {
//            Staff s;
//            String name = txtName.getText();
//            String username = txtUsername.getText();
//            String password = new String(txtPassword.getPassword());
//            int position = cmbPosition.getSelectedIndex() + 1;
//            if (name.equals("") || username.equals("")) {
//                JOptionPane.showMessageDialog(this, "Fill out all required fields", "New Staff", JOptionPane.ERROR_MESSAGE);
//            } else if (new String(txtPassword.getPassword()).equals(new String(txtPasswordConfirm.getPassword()))) {
//                try {
//                    s = new Staff(name, position, username, password);
//                    Staff st = dc.addStaff(s);
//                    setCurrentStaff(null);
//                    showAllStaff();
//                    txtName.requestFocus();
//                } catch (IOException | SQLException | StaffNotFoundException ex) {
//                    showError(ex);
//                }
//            } else {
//                JOptionPane.showMessageDialog(this, "Passwords do not match", "New Staff", JOptionPane.ERROR_MESSAGE);
//            }
//        } else {
//            setCurrentStaff(null);
//        }

        Staff s = StaffDialog.showNewStaffDialog(this, dc);

        if (s != null) {
            try {
                dc.addStaff(s);
                setCurrentStaff(null);
                showAllStaff();
            } catch (IOException | StaffNotFoundException | SQLException ex) {
                showError(ex);
            }
        }
    }//GEN-LAST:event_btnAddStaffActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        String name = txtName.getText();
        String username = txtUsername.getText();
        int position = cmbPosition.getSelectedIndex() + 1;
        staff.setName(name);
        staff.setUsername(username);
        staff.setPosition(position);
        try {
            dc.updateStaff(staff);
        } catch (SQLException | StaffNotFoundException | IOException ex) {
            showError(ex);
        }
        updateTable();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnRemoveStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveStaffActionPerformed
        int index = tableStaff.getSelectedRow();
        if (index != -1) {
            int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove the following staff member?\n" + currentTableContents.get(index), "Remove Staff", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    dc.removeStaff(currentTableContents.get(index).getId());
                } catch (SQLException | StaffNotFoundException | IOException ex) {
                    showError(ex);
                }
                showAllStaff();
                setCurrentStaff(null);
            }
        }
    }//GEN-LAST:event_btnRemoveStaffActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnShowAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowAllActionPerformed
        showAllStaff();
    }//GEN-LAST:event_btnShowAllActionPerformed

    private void tableStaffMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableStaffMousePressed
        if (evt.getClickCount() == 2) {
            editStaff();
        } else if (evt.getClickCount() == 1) {
            setCurrentStaff(currentTableContents.get(tableStaff.getSelectedRow()));
        }
    }//GEN-LAST:event_tableStaffMousePressed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        int option;
        String terms = txtSearch.getText();

        if (radID.isSelected()) {
            option = 1;
        } else {
            option = 2;
        }

        List<Staff> newList = new ArrayList<>();

        if (option == 1) {
            for (Staff s : currentTableContents) {
                if ((s.getId() + "").equals(terms)) {
                    newList.add(s);
                }
            }
        } else {
            for (Staff s : currentTableContents) {
                if (s.getName().toLowerCase().contains(terms.toLowerCase())) {
                    newList.add(s);
                }
            }
        }

        if (newList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records found", "Search", JOptionPane.PLAIN_MESSAGE);
        } else {
            currentTableContents = newList;
            if (newList.size() == 1) {
                setCurrentStaff(newList.get(0));
            }
        }
        updateTable();

    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPasswordActionPerformed
        if(GUI.staff.getPosition() >= 3){
        String password = PasswordDialog.showDialog(this, dc, staff);
        if (password != null) {
            staff.setPassword(password);
            try {
                dc.updateStaff(staff);
            } catch (IOException | StaffNotFoundException | SQLException ex) {
                showError(ex);
            }
        }
        } else{
            JOptionPane.showMessageDialog(this, "You cannot change users passwords", "Password", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnPasswordActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddStaff;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnPassword;
    private javax.swing.JButton btnRemoveStaff;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnShowAll;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cmbPosition;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton radID;
    private javax.swing.JRadioButton radName;
    private javax.swing.JTable tableStaff;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
