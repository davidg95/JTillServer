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
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Window which allows categorys to be added, edited or deleted.
 *
 * @author David
 */
public class CategorysWindow extends javax.swing.JFrame {

    public static CategorysWindow frame;

    private final DataConnect dbConn;
    private Category category;

    private final DefaultTableModel model;
    private List<Category> currentTableContents;

    /**
     * Creates new form CategoryWindow
     */
    public CategorysWindow(DataConnect dc, Image icon) {
        dbConn = dc;
        this.setIconImage(icon);
        initComponents();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        currentTableContents = new ArrayList<>();
        model = (DefaultTableModel) table.getModel();
        showAllCategorys();
    }

    /**
     * Method to show the category window. If this is the first time it is being
     * called, it will first construct the window.
     *
     * @param dc the reference to the data source.
     */
    public static void showCategoryWindow(DataConnect dc, Image icon) {
        if (frame == null) {
            frame = new CategorysWindow(dc, icon);
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        update();
        frame.setCurrentCategory(null);
        frame.setVisible(true);
    }

    /**
     * Method to update the contents of the window. This can be called from
     * another class if change is made to any categorys elsewhere.
     */
    public static void update() {
        if (frame != null) {
            frame.showAllCategorys();
        }
    }

    /**
     * Method to update the contents of the table with whatever is in the
     * currentTableContents list.
     */
    private void updateTable() {
        model.setRowCount(0);

        for (Category c : currentTableContents) {
            Object[] s = new Object[]{c.getID(), c.getName()};
            model.addRow(s);
        }

        table.setModel(model);
        ProductsWindow.update();
    }

    /**
     * Method to show all the categorys in the database.
     */
    private void showAllCategorys() {
        try {
            currentTableContents = dbConn.getAllCategorys();
            updateTable();
        } catch (IOException | SQLException ex) {
            showError(ex);
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
            }
            spinAge.setValue(c.getMinAge());
        }
    }

    /**
     * Method to show an error message.
     *
     * @param e the exception to show.
     */
    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e, "Categorys", JOptionPane.ERROR_MESSAGE);
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
        jLabel1 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        btnAdd = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        chkTime = new javax.swing.JCheckBox();
        lblTime = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        spinAge = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        startH = new javax.swing.JSpinner();
        startM = new javax.swing.JSpinner();
        lblS = new javax.swing.JLabel();
        endH = new javax.swing.JSpinner();
        lblE = new javax.swing.JLabel();
        endM = new javax.swing.JSpinner();
        lblStart = new javax.swing.JLabel();
        lblEnd = new javax.swing.JLabel();

        setTitle("Categorys");

        table.setModel(new javax.swing.table.DefaultTableModel(
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
                java.lang.String.class, java.lang.String.class
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
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setResizable(false);
            table.getColumnModel().getColumn(1).setResizable(false);
        }

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        jLabel1.setText("Name:");

        btnAdd.setText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnRemove.setText("Remove");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        chkTime.setText("Time Restricted");
        chkTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkTimeActionPerformed(evt);
            }
        });

        lblTime.setText("Time Restriction:");
        lblTime.setEnabled(false);

        jLabel2.setText("Minimum Age:");

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

        startH.setModel(new javax.swing.SpinnerNumberModel(0, 0, 23, 1));
        startH.setEnabled(false);

        startM.setModel(new javax.swing.SpinnerNumberModel(0, 0, 59, 1));
        startM.setEnabled(false);

        lblS.setText(":");

        endH.setModel(new javax.swing.SpinnerNumberModel(0, 0, 23, 1));
        endH.setEnabled(false);

        lblE.setText(":");

        endM.setModel(new javax.swing.SpinnerNumberModel(0, 0, 59, 1));
        endM.setEnabled(false);

        lblStart.setText("Start");

        lblEnd.setText("End");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(70, 70, 70)
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(chkTime)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(btnAdd)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnSave)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnRemove)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(lblTime)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(endH)
                                    .addComponent(startH))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lblS)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(startM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lblE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(endM)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblStart)
                                    .addComponent(lblEnd))
                                .addGap(77, 77, 77))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinAge, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 430, Short.MAX_VALUE)
                        .addComponent(btnClose))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chkTime)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTime)
                            .addComponent(startH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblS)
                            .addComponent(startM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblStart))
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(endH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblE)
                            .addComponent(endM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblEnd))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(spinAge, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(40, 40, 40)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAdd)
                            .addComponent(btnSave)
                            .addComponent(btnRemove))
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
                if (name.equals("")) {
                    JOptionPane.showMessageDialog(this, "Fill out all required fields", "New Category", JOptionPane.ERROR_MESSAGE);
                } else {
                    c = new Category(name, startSell, endSell, time, minAge);
                    try {
                        Category cat = dbConn.addCategory(c);
                        showAllCategorys();
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
            if (name.equals("")) {
                JOptionPane.showMessageDialog(this, "Fill out all required fields", "New Category", JOptionPane.ERROR_MESSAGE);
            } else {
                category.setName(name);
                category.setStartSell(startSell);
                category.setEndSell(endSell);
                category.setTimeRestrict(time);
                category.setMinAge(minAge);

                try {
                    dbConn.updateCategory(category);
                } catch (SQLException | CategoryNotFoundException | IOException ex) {
                    showError(ex);
                }

                showAllCategorys();
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
            if (currentTableContents.get(index).getID() == 1) {
                JOptionPane.showMessageDialog(this, "You cannot remote the default category", "Remove Category", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove the following category?\n-" + currentTableContents.get(index) + "\nAll products in this category will be moved to the DEFAULT category.", "Remove Category", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    dbConn.removeCategory(currentTableContents.get(index).getID());
                } catch (SQLException | CategoryNotFoundException | IOException ex) {
                    showError(ex);
                }
                showAllCategorys();
                setCurrentCategory(null);
            }
        }
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void tableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMousePressed
        if (evt.getClickCount() == 1) {
            setCurrentCategory(currentTableContents.get(table.getSelectedRow()));
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
            showAllCategorys();
            return;
        }

        List<Category> newList = new ArrayList<>();

        for (Category c : currentTableContents) {
            if (c.getName().toLowerCase().contains(terms.toLowerCase())) {
                newList.add(c);
            }
        }

        if (newList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records found", "Search", JOptionPane.PLAIN_MESSAGE);
        } else {
            currentTableContents = newList;
            if (newList.size() == 1) {
                setCurrentCategory(newList.get(0));
            }
        }
        updateTable();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        btnSearch.doClick();
    }//GEN-LAST:event_txtSearchActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JCheckBox chkTime;
    private javax.swing.JSpinner endH;
    private javax.swing.JSpinner endM;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
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
