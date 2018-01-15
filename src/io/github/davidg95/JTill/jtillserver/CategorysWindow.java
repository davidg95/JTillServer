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
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * Window which allows categorys to be added, edited or deleted.
 *
 * @author David
 */
public final class CategorysWindow extends javax.swing.JInternalFrame {

    public static CategorysWindow frame;

    private final DataConnect dc;
    private Category category;

    private MyModel model;

    private final DefaultComboBoxModel depsModel;

    /**
     * Creates new form CategoryWindow
     */
    public CategorysWindow() {
        this.dc = GUI.gui.dc;
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setClosable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        initComponents();
        depsModel = new DefaultComboBoxModel();
        init();
    }

    /**
     * Method to show the category window. If this is the first time it is being
     * called, it will first construct the window.
     */
    public static void showCategoryWindow() {
        if (frame == null || frame.isClosed()) {
            frame = new CategorysWindow();
            GUI.gui.internal.add(frame);
        }
        if (frame.isVisible()) {
            frame.toFront();
        } else {
            update();
            frame.setCurrentCategory(null);
            frame.setVisible(true);
        }
        try {
            frame.setIcon(false);
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(SettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Method to update the contents of the window. This can be called from
     * another class if change is made to any categorys elsewhere.
     */
    public static void update() {
        if (frame != null) {
            frame.init();
        }
    }

    private void init() {
        try {
            model = new MyModel(dc.getAllCategorys());
        } catch (IOException | SQLException ex) {
            showError(ex);
        }
        table.setModel(model);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.setSelectionModel(new ForcedListSelectionModel());
        try {
            depsModel.removeAllElements();
            List<Department> deps = dc.getAllDepartments();
            for (Department d : deps) {
                depsModel.addElement(d);
            }
            cmbDepartment.setModel(depsModel);
        } catch (IOException | SQLException ex) {
            showError(ex);
        }
        JComboBox box = new JComboBox();
        try {
            List<Department> departments = dc.getAllDepartments();
            for (Department d : departments) {
                box.addItem(d);
            }
            TableColumn depCol = table.getColumnModel().getColumn(2);
            depCol.setCellEditor(new DefaultCellEditor(box));
        } catch (IOException | SQLException ex) {
            Logger.getLogger(WasteStockWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Method to set the current selected category and fill the fields
     * accordingly.
     *
     * @param c the category to show.
     */
    private void setCurrentCategory(Category c) {
        if (c == null) {
            txtName.setText("");
            chkTime.setSelected(false);
            lblTime.setEnabled(false);
            startH.setEnabled(false);
            startM.setEnabled(false);
            endH.setEnabled(false);
            endM.setEnabled(false);
            lblS.setEnabled(false);
            lblE.setEnabled(false);
            lblStart.setEnabled(false);
            lblEnd.setEnabled(false);
            startM.setValue(0);
            startH.setValue(0);
            endM.setValue(0);
            endH.setValue(0);
            spinAge.setValue(0);
            category = null;
        } else {
            category = c;
            txtName.setText(c.getName());
            chkTime.setSelected(c.isTimeRestrict());
            if (c.isTimeRestrict()) {
                chkTime.setSelected(true);
                lblTime.setEnabled(true);
                startH.setEnabled(true);
                startM.setEnabled(true);
                endH.setEnabled(true);
                endM.setEnabled(true);
                lblS.setEnabled(true);
                lblE.setEnabled(true);
                lblStart.setEnabled(true);
                lblEnd.setEnabled(true);
                startM.setValue(c.getStartSell().getMinutes());
                startH.setValue(c.getStartSell().getHours());
                endM.setValue(c.getEndSell().getMinutes());
                endH.setValue(c.getEndSell().getHours());
                cmbDepartment.setEnabled(true);
                cmbDepartment.setSelectedItem(category.getDepartment());
            } else {
                chkTime.setSelected(false);
                lblTime.setEnabled(false);
                startH.setEnabled(false);
                startM.setEnabled(false);
                endH.setEnabled(false);
                endM.setEnabled(false);
                lblS.setEnabled(false);
                lblE.setEnabled(false);
                lblStart.setEnabled(false);
                lblEnd.setEnabled(false);
                startM.setValue(0);
                startH.setValue(0);
                endM.setValue(0);
                endH.setValue(0);
                cmbDepartment.setEnabled(true);
                cmbDepartment.setSelectedItem(category.getDepartment());
            }
            spinAge.setValue(c.getMinAge());
        }
        txtName.requestFocus();
    }

    /**
     * Method to show an error message.
     *
     * @param e the exception to show.
     */
    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e, "Categorys", JOptionPane.ERROR_MESSAGE);
    }

    private class MyModel implements TableModel {

        private final List<Category> categories;
        private final List<TableModelListener> listeners;

        public MyModel(List<Category> categories) {
            this.categories = categories;
            this.listeners = new LinkedList<>();
        }

        public void addCategory(Category c) {
            categories.add(c);
            alertAll();
        }

        public void removeCategory(int i) {
            categories.remove(i);
        }

        public List<Category> getAllCategories() {
            return categories;
        }

        public Category getCategories(int i) {
            return categories.get(i);
        }

        @Override
        public int getRowCount() {
            return categories.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0: {
                    return "ID";
                }
                case 1: {
                    return "Name";
                }
                case 2: {
                    return "Department";
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
            return columnIndex != 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Category category = categories.get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return category.getId();
                }
                case 1: {
                    return category.getName();
                }
                case 2: {
                    return category.getDepartment();
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Category c = categories.get(rowIndex);
            if (columnIndex == 1) {
                c.setName((String) aValue);
            } else if (columnIndex == 2) {
                c.setDepartment((Department) aValue);
            }
            try {
                c.save();
            } catch (IOException | SQLException ex) {
                JOptionPane.showMessageDialog(CategorysWindow.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void alertAll() {
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

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        btnClose = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        spinAge = new javax.swing.JSpinner();
        txtName = new javax.swing.JTextField();
        lblE = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();
        startM = new javax.swing.JSpinner();
        lblEnd = new javax.swing.JLabel();
        chkTime = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        startH = new javax.swing.JSpinner();
        lblStart = new javax.swing.JLabel();
        endH = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        btnSave = new javax.swing.JButton();
        endM = new javax.swing.JSpinner();
        lblS = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cmbDepartment = new javax.swing.JComboBox<>();
        btnDepartments = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setTitle("Categorys");

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "Name", "Department"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Object.class
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
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setMinWidth(40);
            table.getColumnModel().getColumn(0).setPreferredWidth(40);
            table.getColumnModel().getColumn(0).setMaxWidth(40);
            table.getColumnModel().getColumn(1).setResizable(false);
        }

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnAdd.setText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnRemove.setText("Remove");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
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

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Category Edit"));

        lblE.setText(":");

        lblTime.setText("Time Restriction:");
        lblTime.setEnabled(false);

        startM.setModel(new javax.swing.SpinnerNumberModel(0, 0, 59, 1));
        startM.setEnabled(false);

        lblEnd.setText("End");

        chkTime.setText("Time Restricted");
        chkTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkTimeActionPerformed(evt);
            }
        });

        jLabel2.setText("Minimum Age:");

        startH.setModel(new javax.swing.SpinnerNumberModel(0, 0, 23, 1));
        startH.setEnabled(false);

        lblStart.setText("Start");

        endH.setModel(new javax.swing.SpinnerNumberModel(0, 0, 23, 1));
        endH.setEnabled(false);

        jLabel1.setText("Name:");

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        endM.setModel(new javax.swing.SpinnerNumberModel(0, 0, 59, 1));
        endM.setEnabled(false);

        lblS.setText(":");

        jLabel4.setText("Department:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(70, 70, 70)
                            .addComponent(jLabel1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(chkTime)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addComponent(lblTime)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(endH)
                                .addComponent(startH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(lblS)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(startM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(lblE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(endM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblStart)
                                .addComponent(lblEnd))
                            .addGap(37, 37, 37)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbDepartment, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnSave)
                                    .addComponent(spinAge, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkTime)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTime)
                    .addComponent(startH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblS)
                    .addComponent(startM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblStart))
                .addGap(3, 3, 3)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(endH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblE)
                    .addComponent(endM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEnd))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(spinAge, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cmbDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(btnSave)
                .addGap(20, 20, 20))
        );

        btnDepartments.setText("Departments");
        btnDepartments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDepartmentsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(70, 70, 70)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnDepartments, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnAdd)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRemove)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 438, Short.MAX_VALUE)
                        .addComponent(btnClose))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAdd)
                            .addComponent(btnRemove))
                        .addGap(64, 64, 64)
                        .addComponent(btnDepartments)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(jLabel3)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        if (category == null) {
            Category c;
            try {
                String name = txtName.getText();
                boolean time = chkTime.isSelected();
                Time startSell = null;
                Time endSell = null;
                if (time) {
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
                    startSell = new Time(sdf.parse(startH.getValue() + ":" + startM.getValue() + ":00").getTime());
                    endSell = new Time(sdf.parse(endH.getValue() + ":" + endM.getValue() + ":00").getTime());
                }
                int minAge = (int) spinAge.getValue();
                Department dep = (Department) cmbDepartment.getSelectedItem();
                if (name.equals("")) {
                    JOptionPane.showMessageDialog(this, "Fill out all required fields", "New Category", JOptionPane.ERROR_MESSAGE);
                } else {
                    c = new Category(name, startSell, endSell, time, minAge, dep);
                    try {
                        Category cat = dc.addCategory(c);
                        setCurrentCategory(null);
                    } catch (SQLException | IOException ex) {
                        showError(ex);
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Fill out all required fields", "New Category", JOptionPane.ERROR_MESSAGE);
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid time format, user HH:mm:ss", "New Category", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            category = null;
            setCurrentCategory(null);
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        try {
            String name = txtName.getText();
            boolean time = chkTime.isSelected();
            Time startSell = null;
            Time endSell = null;
            if (time) {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
                startSell = new Time(sdf.parse(startH.getValue() + ":" + startM.getValue() + ":00").getTime());
                endSell = new Time(sdf.parse(endH.getValue() + ":" + endM.getValue() + ":00").getTime());
            }
            int minAge = (int) spinAge.getValue();
            Department dep = (Department) cmbDepartment.getSelectedItem();
            if (name.equals("")) {
                JOptionPane.showMessageDialog(this, "Fill out all required fields", "New Category", JOptionPane.ERROR_MESSAGE);
            } else {
                category.setName(name);
                category.setStartSell(startSell);
                category.setEndSell(endSell);
                category.setTimeRestrict(time);
                category.setMinAge(minAge);
                category.setDepartment(dep);

                try {
                    category.save();
                } catch (SQLException | IOException ex) {
                    showError(ex);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Fill out all required fields", "Category", JOptionPane.ERROR_MESSAGE);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid time format, user HH:mm:ss", "New Category", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        int index = table.getSelectedRow();
        if (index != -1) {
            if (model.getCategories(index).getId() == 1) {
                JOptionPane.showMessageDialog(this, "You cannot remote the default category", "Remove Category", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove the following category?\n-" + model.getCategories(index) + "\nAll products in this category will be moved to the DEFAULT category.", "Remove Category", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    dc.removeCategory(model.getCategories(index).getId());
                    model.removeCategory(index);
                } catch (SQLException | JTillException | IOException ex) {
                    showError(ex);
                }
                setCurrentCategory(null);
            }
        }
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void tableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMousePressed
        if (evt.getClickCount() == 1) {
            setCurrentCategory(model.getCategories(table.getSelectedRow()));
        }
    }//GEN-LAST:event_tableMousePressed

    private void chkTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTimeActionPerformed
        lblTime.setEnabled(chkTime.isSelected());
        startH.setEnabled(chkTime.isSelected());
        startM.setEnabled(chkTime.isSelected());
        endH.setEnabled(chkTime.isSelected());
        endM.setEnabled(chkTime.isSelected());
        lblS.setEnabled(chkTime.isSelected());
        lblE.setEnabled(chkTime.isSelected());
        lblStart.setEnabled(chkTime.isSelected());
        lblEnd.setEnabled(chkTime.isSelected());
    }//GEN-LAST:event_chkTimeActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String terms = txtSearch.getText();

        if (terms.isEmpty()) {
            return;
        }

        List<Category> newList = new ArrayList<>();

        for (Category c : model.getAllCategories()) {
            if (c.getName().toLowerCase().contains(terms.toLowerCase())) {
                newList.add(c);
            }
        }

        if (newList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records found", "Search", JOptionPane.PLAIN_MESSAGE);
        } else {
            if (newList.size() == 1) {
                setCurrentCategory(newList.get(0));
            }
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        btnSearch.doClick();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnDepartmentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDepartmentsActionPerformed
        DepartmentsWindow.showWindow();
    }//GEN-LAST:event_btnDepartmentsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnDepartments;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JCheckBox chkTime;
    private javax.swing.JComboBox<String> cmbDepartment;
    private javax.swing.JSpinner endH;
    private javax.swing.JSpinner endM;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblE;
    private javax.swing.JLabel lblEnd;
    private javax.swing.JLabel lblS;
    private javax.swing.JLabel lblStart;
    private javax.swing.JLabel lblTime;
    private javax.swing.JSpinner spinAge;
    private javax.swing.JSpinner startH;
    private javax.swing.JSpinner startM;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
