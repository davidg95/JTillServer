/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
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
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author 1301480
 */
public class StaffWindow extends javax.swing.JInternalFrame {

    private final Logger log = Logger.getGlobal();

    public static StaffWindow frame;

    private final DataConnect dc;

    private Staff staff;

    private final DefaultTableModel model;
    private List<Staff> currentTableContents;

    /**
     * Creates new form StaffWindow
     */
    public StaffWindow() {
        this.dc = GUI.gui.dc;
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setClosable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        initComponents();
        currentTableContents = new ArrayList<>();
        model = (DefaultTableModel) tableStaff.getModel();
        showAllStaff();
        init();
    }

    private void init() {
        tableStaff.getColumnModel().getColumn(0).setMaxWidth(40);
        tableStaff.setSelectionModel(new ForcedListSelectionModel());
        InputMap im = tableStaff.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = tableStaff.getActionMap();

        KeyStroke deleteKey = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);

        im.put(deleteKey, "Action.delete");
        am.put("Action.delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                final int index = tableStaff.getSelectedRow();
                if (index == -1) {
                    return;
                }
                final Staff s = currentTableContents.get(index);
                removeStaff(s);
            }
        });
    }

    public static void showStaffListWindow() {
        if (frame == null || frame.isClosed()) {
            frame = new StaffWindow();
            GUI.gui.internal.add(frame);
        }
        if (frame.isVisible()) {
            frame.toFront();
        } else {
            update();
            frame.setCurrentStaff(null);
            frame.setVisible(true);
            frame.showInit();
        }
        try {
            frame.setIcon(false);
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(SettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void update() {
        if (frame != null) {
            frame.showAllStaff();
        }
    }

    private void showInit() {
        btnPassword.setEnabled(false);
    }

    private void updateTable() {
        model.setRowCount(0);

        for (Staff s : currentTableContents) {
            String position = "";
            switch (s.getPosition()) {
                case 1:
                    position = "Assisstant";
                    break;
                case 2:
                    position = "Supervisor";
                    break;
                case 3:
                    position = "Manager";
                    break;
                default:
                    position = "Area Manager";
                    break;
            }
            Object[] str = new Object[]{s.getId(), s.getName(), position, s.getUsername()};
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
        SwingUtilities.invokeLater(() -> {
            int selectedRow = tableStaff.getSelectedRow();
            if (selectedRow != -1) {
                Staff s = currentTableContents.get(selectedRow);
                StaffDialog.showEditStaffDialog(this, s);
                updateTable();
            }
        });
    }

    private void setCurrentStaff(Staff s) {
        if (s == null) {
            txtName.setText("");
            txtUsername.setText("");
            cmbPosition.setSelectedIndex(0);
            txtWage.setText("");
            staff = null;
            chkEnabled.setSelected(false);
        } else {
            this.staff = s;
            txtName.setText(s.getName());
            txtUsername.setText(s.getUsername());
            int p = s.getPosition();
            int index = p - 1;
            cmbPosition.setSelectedIndex(index);
            txtWage.setText(s.getWage() + "");
            chkEnabled.setSelected(staff.isEnabled());
        }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e, "Staff", JOptionPane.ERROR_MESSAGE);
    }

    private void changePassword(Staff s) {
        if (GUI.staff.getPosition() >= 3) {
            String password = PasswordDialog.showDialog(this, s);
            if (password != null) {
                if (!password.equals("")) {
                    s.setPassword(password);
                    try {
                        dc.updateStaff(s);
                        JOptionPane.showInternalMessageDialog(GUI.gui.internal, "Password successfully changed", "Password", JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException | StaffNotFoundException | SQLException ex) {
                        showError(ex);
                    }
                }
            }
        } else {
            JOptionPane.showInternalMessageDialog(GUI.gui.internal, "You cannot change users passwords", "Password", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeStaff(Staff s) {
        if (s.equals(GUI.staff)) {
            JOptionPane.showMessageDialog(this, "You cannot remove yourself as you are currently logged in.", "Remove", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            if (dc.isTillLoggedIn(s)) {
                JOptionPane.showMessageDialog(this, "You cannot remove this member of staff as they are currently logged in", "Remove", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (IOException | StaffNotFoundException | SQLException ex) {
            log.log(Level.INFO, null, ex);
        }
        int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove the following staff member?\n" + s, "Remove Staff", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                dc.removeStaff(s.getId());
                showAllStaff();
                setCurrentStaff(null);
                JOptionPane.showMessageDialog(this, "Staff member removed", "Remove Staff", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException | StaffNotFoundException | IOException ex) {
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
        lblPassword = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtWage = new javax.swing.JTextField();
        chkEnabled = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
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
        tableStaff.getTableHeader().setReorderingAllowed(false);
        tableStaff.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableStaffMouseClicked(evt);
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

        lblPassword.setText("Click to change password:");

        jLabel4.setText("Wage:");

        chkEnabled.setText("Enabled");

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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblPassword)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkEnabled)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(btnPassword)
                                .addComponent(txtName)
                                .addComponent(cmbPosition, 0, 139, Short.MAX_VALUE)
                                .addComponent(txtUsername)
                                .addComponent(txtWage)))))
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
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnPassword)
                            .addComponent(lblPassword))
                        .addGap(2, 2, 2)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtWage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(4, 4, 4)
                        .addComponent(chkEnabled)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
        Staff s = StaffDialog.showNewStaffDialog(this);
        if (s != null) {
            setCurrentStaff(null);
            showAllStaff();
        }
    }//GEN-LAST:event_btnAddStaffActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        String name = txtName.getText();
        String username = txtUsername.getText();
        int position = cmbPosition.getSelectedIndex() + 1;
        String w = txtWage.getText();
        if (!Utilities.isNumber(w) || name.length() == 0 || username.length() == 0) {
            JOptionPane.showMessageDialog(this, "Not all fields have been filled out correctly", "Save Changes", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double wage = Double.parseDouble(w);
        if (wage <= 0) {
            JOptionPane.showMessageDialog(this, "Wage must be greater than 0", "Save Changes", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            staff.setName(name);
            String lastUsername = staff.getUsername();
            if (!lastUsername.equals(username)) {
                if (dc.checkUsername(username)) {
                    JOptionPane.showMessageDialog(this, "Username is already in use", "Save Changes", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            staff.setUsername(username);
            staff.setPosition(position);
            staff.setWage(wage);
            staff.setEnabled(chkEnabled.isSelected());
            dc.updateStaff(staff);
        } catch (SQLException | StaffNotFoundException | IOException ex) {
            showError(ex);
        }
        updateTable();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnRemoveStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveStaffActionPerformed
        int index = tableStaff.getSelectedRow();
        Staff selected = currentTableContents.get(index);
        removeStaff(selected);
    }//GEN-LAST:event_btnRemoveStaffActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnShowAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowAllActionPerformed
        showAllStaff();
    }//GEN-LAST:event_btnShowAllActionPerformed

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
        changePassword(staff);
    }//GEN-LAST:event_btnPasswordActionPerformed

    private void tableStaffMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableStaffMouseClicked
        int index = tableStaff.getSelectedRow();
        if (index == -1) {
            return;
        }
        setCurrentStaff(currentTableContents.get(index));
        if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem view = new JMenuItem("View");
            final Font boldFont = new Font(view.getFont().getFontName(), Font.BOLD, view.getFont().getSize());
            view.setFont(boldFont);
            view.addActionListener((ActionEvent e) -> {
                editStaff();
            });
            JMenuItem pass = new JMenuItem("Change Password");
            pass.addActionListener((ActionEvent e) -> {
                changePassword(staff);
            });
            JMenuItem enable = new JMenuItem("Disable Account");
            if (!staff.isEnabled()) {
                enable.setText("Enable Account");
            }
            enable.addActionListener((ActionEvent e) -> {
                staff.setEnabled(!staff.isEnabled());
                try {
                    dc.updateStaff(staff);
                    JOptionPane.showInternalMessageDialog(this, "Account " + (staff.isEnabled() ? "enabled" : "disabled"), "Staff", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException | StaffNotFoundException | SQLException ex) {
                    JOptionPane.showInternalMessageDialog(this, ex.getMessage(), "Staff", JOptionPane.ERROR_MESSAGE);
                }
            });
            JMenuItem remove = new JMenuItem("Remove");
            remove.addActionListener((ActionEvent e) -> {
                removeStaff(staff);
            });
            menu.add(view);
            menu.add(pass);
            menu.add(enable);
            menu.addSeparator();
            menu.add(remove);
            menu.show(tableStaff, evt.getX(), evt.getY());
        } else if (SwingUtilities.isLeftMouseButton(evt)) {
            if (evt.getClickCount() == 2) {
                editStaff();
            }
        }
    }//GEN-LAST:event_tableStaffMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddStaff;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnPassword;
    private javax.swing.JButton btnRemoveStaff;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnShowAll;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox chkEnabled;
    private javax.swing.JComboBox<String> cmbPosition;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JRadioButton radID;
    private javax.swing.JRadioButton radName;
    private javax.swing.JTable tableStaff;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtUsername;
    private javax.swing.JTextField txtWage;
    // End of variables declaration//GEN-END:variables
}
