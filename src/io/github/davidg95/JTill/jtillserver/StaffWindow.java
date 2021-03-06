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
import java.util.LinkedList;
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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author 1301480
 */
public class StaffWindow extends javax.swing.JInternalFrame {

    private final Logger log = Logger.getGlobal();

    public static StaffWindow frame;

    private final JTill jtill;

    private Staff staff;

    private MyModel model;

    /**
     * Creates new form StaffWindow
     */
    public StaffWindow(JTill jtill) {
        this.jtill = jtill;
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setClosable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        initComponents();
        showAllStaff();
        init();
    }

    private void init() {
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
                final Staff s = model.getSelected();
                if (s == null) {
                    return;
                }
                removeStaff(s);
            }
        });
    }

    public static void showStaffListWindow(JTill jtill) {
        if (frame == null || frame.isClosed()) {
            frame = new StaffWindow(jtill);
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

    private void showAllStaff() {
        try {
            List<Staff> list = jtill.getDataConnection().getAllStaff();
            model = new MyModel(list);
            tableStaff.setModel(model);
            tableStaff.getColumnModel().getColumn(0).setMaxWidth(40);
        } catch (IOException | SQLException ex) {
            showError(ex);
        }
    }

    private void editStaff() {
        SwingUtilities.invokeLater(() -> {
            int selectedRow = tableStaff.getSelectedRow();
            if (selectedRow != -1) {
                Staff s = model.getSelected();
                if (s == null) {
                    return;
                }
                StaffDialog.showEditStaffDialog(jtill, this, s);
                model.alertAll();
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
            tabbed.setEnabledAt(1, false);
        } else {
            this.staff = s;
            txtName.setText(s.getName());
            txtUsername.setText(s.getUsername());
            int p = s.getPosition();
            int index = p - 1;
            cmbPosition.setSelectedIndex(index);
            txtWage.setText(s.getWage() + "");
            chkEnabled.setSelected(staff.isEnabled());
            tabbed.setEnabledAt(1, true);
        }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e, "Staff", JOptionPane.ERROR_MESSAGE);
    }

    private void changePassword(Staff s) {
        if (GUI.staff.getPosition() >= 3) {
            String password = PasswordDialog.showDialog(this, s);
            if (password == null) {
                return;
            }
            if (!password.isEmpty()) {
                try {
                    jtill.getDataConnection().changePassword(s.getUsername(), password);
                    JOptionPane.showMessageDialog(this, "Password successfully changed", "Password", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException | JTillException | SQLException ex) {
                    showError(ex);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "You cannot change users passwords", "Password", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeStaff(Staff s) {
        if (s.equals(GUI.staff)) {
            JOptionPane.showMessageDialog(this, "You cannot remove yourself as you are currently logged in.", "Remove", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            if (jtill.getDataConnection().isTillLoggedIn(s)) {
                JOptionPane.showMessageDialog(this, "You cannot remove this member of staff as they are currently logged in", "Remove", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (IOException | JTillException | SQLException ex) {
            log.log(Level.INFO, null, ex);
        }
        int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove the following staff member?\n" + s, "Remove Staff", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                jtill.getDataConnection().removeStaff(s);
                showAllStaff();
                setCurrentStaff(null);
                JOptionPane.showMessageDialog(this, "Staff member removed", "Remove Staff", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException | JTillException | IOException ex) {
                showError(ex);
            }
        }
    }

    private class MyModel implements TableModel {

        private List<Staff> staff;
        private final List<TableModelListener> listeners;

        public MyModel(List<Staff> staff) {
            this.staff = staff;
            this.listeners = new LinkedList<>();
        }

        public void addStaff(Staff s, String password) throws IOException, SQLException {
            s = jtill.getDataConnection().addStaff(s, password);
            staff.add(s);
            alertAll();
        }

        public void removeStaff(Staff s) throws IOException, JTillException, SQLException {
            jtill.getDataConnection().removeStaff(s);
            staff.remove(s);
            alertAll();
        }

        public Staff getStaffById(int id) {
            for (Staff s : staff) {
                if (s.getId() == id) {
                    return s;
                }
            }
            return null;
        }

        public Staff getStaffByName(String name) {
            for (Staff s : staff) {
                if (s.getName().equalsIgnoreCase(name)) {
                    return s;
                }
            }
            return null;
        }

        public Staff getSelected() {
            int row = tableStaff.getSelectedRow();
            if (row == -1) {
                return null;
            }
            return staff.get(row);
        }

        public List<Staff> getAll() {
            return staff;
        }

        public void setList(List<Staff> staff) {
            this.staff = staff;
            alertAll();
        }

        @Override
        public int getRowCount() {
            return staff.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int i) {
            switch (i) {
                case 0: {
                    return "ID";
                }
                case 1: {
                    return "Name";
                }
                case 2: {
                    return "Position";
                }
                case 3: {
                    return "Username";
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return Object.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Staff s = staff.get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return s.getId();
                }
                case 1: {
                    return s.getName();
                }
                case 2: {
                    switch (s.getPosition()) {
                        case 1:
                            return "Assisstant";
                        case 2:
                            return "Supervisor";
                        case 3:
                            return "Manager";
                        default:
                            return "Area Manager";
                    }
                }
                case 3: {
                    return s.getUsername();
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        tabbed = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        btnSearch = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableStaff = new javax.swing.JTable();
        radID = new javax.swing.JRadioButton();
        jLabel6 = new javax.swing.JLabel();
        radName = new javax.swing.JRadioButton();
        btnAddStaff = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        cmbPosition = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        txtWage = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        btnRemoveStaff = new javax.swing.JButton();
        chkEnabled = new javax.swing.JCheckBox();
        btnSave = new javax.swing.JButton();
        btnPassword = new javax.swing.JButton();
        lblPassword = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        txtName = new javax.swing.JTextField();
        btnBack = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setTitle("Manage Staff");

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        tableStaff.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

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

        buttonGroup1.add(radID);
        radID.setText("ID");

        jLabel6.setText("Search:");

        buttonGroup1.add(radName);
        radName.setSelected(true);
        radName.setText("Name");

        btnAddStaff.setText("Add Staff");
        btnAddStaff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddStaffActionPerformed(evt);
            }
        });

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 681, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(btnAddStaff)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnAddStaff)
                    .addComponent(jLabel6)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radName)
                    .addComponent(radID)
                    .addComponent(btnSearch))
                .addContainerGap())
        );

        tabbed.addTab("Browse", jPanel2);

        jLabel3.setText("Username:");

        jLabel1.setText("Name:");

        cmbPosition.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Assisstant", "Supervisor", "Manager", "Area Manager"}));

        jLabel4.setText("Wage:");

        jLabel2.setText("Position:");

        btnRemoveStaff.setText("Delete Staff");
        btnRemoveStaff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveStaffActionPerformed(evt);
            }
        });

        chkEnabled.setText("Enabled");

        btnSave.setText("Save Changes");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnPassword.setText("Password");
        btnPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPasswordActionPerformed(evt);
            }
        });

        lblPassword.setText("Click to change password:");

        txtName.setNextFocusableComponent(cmbPosition);

        btnBack.setText("Back");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblPassword)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkEnabled)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(btnPassword)
                                .addComponent(txtName)
                                .addComponent(cmbPosition, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtUsername)
                                .addComponent(txtWage, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 413, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnSave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveStaff)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnBack)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbPosition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnPassword)
                    .addComponent(lblPassword))
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtWage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addComponent(chkEnabled)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 262, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(btnRemoveStaff)
                    .addComponent(btnBack))
                .addContainerGap())
        );

        tabbed.addTab("Details", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbed)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbed)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddStaffActionPerformed
        Staff s = StaffDialog.showNewStaffDialog(jtill, this);
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
                if (jtill.getDataConnection().checkUsername(username)) {
                    JOptionPane.showMessageDialog(this, "Username is already in use", "Save Changes", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            staff.setUsername(username);
            staff.setPosition(position);
            staff.setWage(wage);
            staff.setEnabled(chkEnabled.isSelected());
            jtill.getDataConnection().updateStaff(staff);
            model.alertAll();
        } catch (SQLException | JTillException | IOException ex) {
            showError(ex);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnRemoveStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveStaffActionPerformed
        Staff selected = model.getSelected();
        if (selected == null) {
            return;
        }
        removeStaff(selected);
    }//GEN-LAST:event_btnRemoveStaffActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        int option;
        String terms = txtSearch.getText();

        if (radID.isSelected()) {
            option = 1;
        } else {
            option = 2;
        }

        if (option == 1) {
            Staff s = model.getStaffById(Integer.parseInt(terms));
            if (s != null) {
                setCurrentStaff(s);
                tabbed.setSelectedIndex(1);
            } else {
                JOptionPane.showMessageDialog(this, "Staff not found", "Search", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            Staff s = model.getStaffByName(terms);
            if (s != null) {
                setCurrentStaff(s);
                tabbed.setSelectedIndex(1);
            } else {
                JOptionPane.showMessageDialog(this, "Staff not found", "Search", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPasswordActionPerformed
        changePassword(staff);
    }//GEN-LAST:event_btnPasswordActionPerformed

    private void tableStaffMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableStaffMouseClicked
        int index = tableStaff.getSelectedRow();
        if (index == -1) {
            return;
        }
        setCurrentStaff(model.getSelected());
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
                    jtill.getDataConnection().updateStaff(staff);
                    JOptionPane.showMessageDialog(this, "Account " + (staff.isEnabled() ? "enabled" : "disabled"), "Staff", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException | JTillException | SQLException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Staff", JOptionPane.ERROR_MESSAGE);
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
                tabbed.setSelectedIndex(1);
            }
        }
    }//GEN-LAST:event_tableStaffMouseClicked

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        tabbed.setSelectedIndex(0);
    }//GEN-LAST:event_btnBackActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        btnSearch.doClick();
    }//GEN-LAST:event_txtSearchActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddStaff;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnPassword;
    private javax.swing.JButton btnRemoveStaff;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox chkEnabled;
    private javax.swing.JComboBox<String> cmbPosition;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JRadioButton radID;
    private javax.swing.JRadioButton radName;
    private javax.swing.JTabbedPane tabbed;
    private javax.swing.JTable tableStaff;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtUsername;
    private javax.swing.JTextField txtWage;
    // End of variables declaration//GEN-END:variables
}
