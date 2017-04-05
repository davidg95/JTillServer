/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Image;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;

/**
 * Window for adding, editing and remove discounts.
 *
 * @author David
 */
public class DiscountsWindow extends javax.swing.JFrame {

    public static DiscountsWindow frame;

    private final DataConnect dc;

    private Discount discount;

    private final DefaultTableModel model;
    private List<Discount> currentTableContents;

    private final DefaultTableModel trigModel;
    private List<Trigger> currentTriggerContents;

    /**
     * Creates new form DiscountsWindow
     */
    public DiscountsWindow(DataConnect dc, Image icon) {
        this.dc = dc;
        this.setIconImage(icon);
        initComponents();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        currentTableContents = new ArrayList<>();
        model = (DefaultTableModel) table.getModel();
        table.setModel(model);
        currentTriggerContents = new ArrayList<>();
        trigModel = (DefaultTableModel) tableTrig.getModel();
        tableTrig.setModel(trigModel);
        showAllDiscounts();

    }

    /**
     * Method to show the discount window.
     *
     * @param dc the data source.
     */
    public static void showDiscountListWindow(DataConnect dc, Image icon) {
        if (frame == null) {
            frame = new DiscountsWindow(dc, icon);
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        update();
        frame.setCurrentDiscount(null);
        frame.setVisible(true);
    }

    /**
     * Method to update the contents of the discounts, this can be called from
     * another class if a change is made to the discount data while the window
     * is open.
     */
    public static void update() {
        if (frame != null) {
            frame.showAllDiscounts();
        }
    }

    /**
     * Method to update the contents of the discounts window.
     */
    private void updateTable() {
        model.setRowCount(0);

        for (Discount d : currentTableContents) {
            Object[] s;
            if (d.getAction() == Discount.PERCENTAGE_OFF) {
                s = new Object[]{d.getId(), d.getName(), "PERCENTAGE OFF"};
            } else {
                s = new Object[]{d.getId(), d.getName(), "MONEY OFF"};
            }
            model.addRow(s);
        }
        ProductsWindow.update();
    }

    /**
     * Method to show all the discounts in the database.
     */
    private void showAllDiscounts() {
        try {
            currentTableContents = dc.getAllDiscounts();
            updateTable();
        } catch (SQLException | IOException ex) {
            showError(ex);
        }
    }

    /**
     * Method to set the fields to show a certain discount.
     *
     * @param d
     */
    private void setCurrentDiscount(Discount d) {
        if (d == null) {
            txtName.setText("");
            txtPercentage.setText("");
            txtMoney.setText("");
            txtOfEach.setText("");
            txtTotal.setText("");
            spinStart.setValue(new Date());
            spinEnd.setValue(new Date());
            discount = null;
        } else {
            this.discount = d;
            txtName.setText(d.getName());
            cmbAction.setSelectedIndex(d.getAction());
            if (d.getAction() == Discount.PERCENTAGE_OFF) {
                txtPercentage.setEnabled(true);
                lblPercentage.setEnabled(true);
                txtPercentage.setText(d.getPercentage() + "");
                txtMoney.setEnabled(false);
                lblMoney.setEnabled(false);
                txtMoney.setText("");
            } else {
                txtPercentage.setEnabled(false);
                lblPercentage.setEnabled(false);
                txtPercentage.setText("");
                txtMoney.setEnabled(true);
                lblMoney.setEnabled(true);
                txtMoney.setText(d.getPrice() + "");
            }
            if (d.getCondition() == 1) {
                rad1.setSelected(true);
                txtOfEach.setText(d.getConditionValue() + "");
                lblOfEach.setEnabled(true);
                txtTotal.setText("");
                lblAtLeast.setEnabled(false);
                lblTotal.setEnabled(false);
            } else {
                rad2.setSelected(true);
                txtOfEach.setText("");
                lblOfEach.setEnabled(false);
                txtTotal.setText(d.getConditionValue() + "");
                lblAtLeast.setEnabled(true);
                lblTotal.setEnabled(true);
            }
            spinStart.setValue(new Date(d.getStart()));
            spinEnd.setValue(new Date(d.getEnd()));
            getTriggers();
        }
    }

    //***TRIGGER TABLE METHODS***//
    private void getTriggers() {
        try {
            currentTriggerContents = dc.getDiscountTriggers(discount.getId());
            updateTriggerTable();
        } catch (IOException | SQLException | DiscountNotFoundException ex) {
            Logger.getLogger(DiscountsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateTriggerTable() {
        trigModel.setRowCount(0);
        for (Trigger t : currentTriggerContents) {
            try {
                final Product p = dc.getProduct(t.getProduct());
                final Plu plu = dc.getPlu(p.getPlu());
                Object[] row = new Object[]{p.getName(), plu.getCode()};
                trigModel.addRow(row);
            } catch (ProductNotFoundException | JTillException | IOException | SQLException ex) {
                Logger.getLogger(DiscountsWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Method to show an error.
     *
     * @param e the exception to show.
     */
    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e, "Database error", JOptionPane.ERROR_MESSAGE);
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
        jLabel2 = new javax.swing.JLabel();
        lblPercentage = new javax.swing.JLabel();
        txtPercentage = new javax.swing.JTextField();
        btnNew = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnShowAll = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        txtName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableTrig = new javax.swing.JTable();
        btnAddTrigger = new javax.swing.JButton();
        btnRemoveTrigger = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        cmbAction = new javax.swing.JComboBox<>();
        lblMoney = new javax.swing.JLabel();
        txtMoney = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtOfEach = new javax.swing.JTextField();
        lblOfEach = new javax.swing.JLabel();
        txtTotal = new javax.swing.JTextField();
        lblAtLeast = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        rad1 = new javax.swing.JRadioButton();
        rad2 = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        spinStart = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        spinEnd = new javax.swing.JSpinner();

        setTitle("Discounts");

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "Name", "Type"
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
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(1).setResizable(false);
            table.getColumnModel().getColumn(2).setResizable(false);
        }

        jLabel2.setText("Name:");

        lblPercentage.setText("Percentage:");

        btnNew.setText("Add Discount");
        btnNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewActionPerformed(evt);
            }
        });

        btnSave.setText("Save Discount");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete Discount");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnShowAll.setText("Show All");
        btnShowAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowAllActionPerformed(evt);
            }
        });

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

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

        tableTrig.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product", "Barcode"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(tableTrig);
        if (tableTrig.getColumnModel().getColumnCount() > 0) {
            tableTrig.getColumnModel().getColumn(0).setResizable(false);
            tableTrig.getColumnModel().getColumn(1).setResizable(false);
        }

        btnAddTrigger.setText("Add Product Trigger");
        btnAddTrigger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTriggerActionPerformed(evt);
            }
        });

        btnRemoveTrigger.setText("Remove Selected Trigger");
        btnRemoveTrigger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveTriggerActionPerformed(evt);
            }
        });

        jLabel5.setText("Action:");

        cmbAction.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Percentage Off", "Money Off" }));
        cmbAction.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                cmbActionPropertyChange(evt);
            }
        });

        lblMoney.setText("Money:");
        lblMoney.setEnabled(false);

        txtMoney.setEnabled(false);

        jLabel3.setText("Conditions:");

        lblOfEach.setText("of each");

        lblAtLeast.setText("At least ");

        lblTotal.setText("in the sale");

        buttonGroup1.add(rad1);
        rad1.setText("Condition 1");

        buttonGroup1.add(rad2);
        rad2.setText("Condition 2");

        jLabel4.setText("Discount Period:");

        SpinnerDateModel modelStart = new SpinnerDateModel();
        modelStart.setCalendarField(Calendar.MINUTE);

        spinStart = new JSpinner();
        spinStart.setModel(modelStart);
        spinStart.setEditor(new JSpinner.DateEditor(spinStart, "dd/MM/yyyy"));

        jLabel6.setText("-");

        SpinnerDateModel modelEnd = new SpinnerDateModel();
        modelEnd.setCalendarField(Calendar.MINUTE);

        spinEnd = new JSpinner();
        spinEnd.setModel(modelEnd);
        spinEnd.setEditor(new JSpinner.DateEditor(spinEnd, "dd/MM/yyyy"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnNew, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnShowAll, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(btnSave)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnDelete))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel2)
                            .addComponent(lblPercentage)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(btnAddTrigger)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRemoveTrigger))
                            .addComponent(txtName)
                            .addComponent(cmbAction, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtPercentage, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblMoney)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtMoney))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(rad1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(rad2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtOfEach, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblOfEach))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lblAtLeast)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblTotal))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(spinStart, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 642, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(cmbAction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblPercentage)
                            .addComponent(txtPercentage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblMoney)
                            .addComponent(txtMoney, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAddTrigger)
                            .addComponent(btnRemoveTrigger))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3)
                                    .addComponent(rad1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rad2))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtOfEach, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblOfEach))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblAtLeast)
                                    .addComponent(lblTotal))))
                        .addGap(2, 2, 2)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(spinStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(spinEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(41, 41, 41)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnNew)
                            .addComponent(btnSave)
                            .addComponent(btnDelete))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnClose)
                            .addComponent(btnShowAll))))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnShowAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowAllActionPerformed
        showAllDiscounts();
    }//GEN-LAST:event_btnShowAllActionPerformed

    private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
        if (discount == null) {
            Discount d;
            try {
                String name = txtName.getText();
                double percentage = 0;
                double price = 0;
                int action;
                if (cmbAction.getSelectedIndex() == Discount.PERCENTAGE_OFF) {
                    percentage = Double.parseDouble(txtPercentage.getText());
                    action = Discount.PERCENTAGE_OFF;
                } else {
                    price = Double.parseDouble(txtMoney.getText());
                    action = Discount.MONEY_OFF;
                }
                int condition;
                int value;
                if (rad1.isSelected()) {
                    condition = 1;
                    value = Integer.parseInt(txtOfEach.getText());
                } else {
                    condition = 2;
                    value = Integer.parseInt(txtTotal.getText());
                }
                if (name.equals("")) {
                    JOptionPane.showMessageDialog(this, "Fill out all required fields", "New Product", JOptionPane.ERROR_MESSAGE);
                } else if (percentage > 100 || percentage < 0) {
                    JOptionPane.showMessageDialog(this, "Please enter a value between 0 and 100", "Discount", JOptionPane.ERROR_MESSAGE);
                } else {
                    Date start = (Date) spinStart.getValue();
                    Date end = (Date) spinEnd.getValue();
                    d = new Discount(name, percentage, new BigDecimal(Double.toString(price)), action, condition, value, start.getTime(), end.getTime());
                    try {
                        Discount dis = dc.addDiscount(d);
                        showAllDiscounts();
                        setCurrentDiscount(null);
                    } catch (SQLException | IOException ex) {
                        showError(ex);
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Fill out all required fields", "New Product", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnNewActionPerformed

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        if (evt.getClickCount() == 1) {
            setCurrentDiscount(currentTableContents.get(table.getSelectedRow()));
        }
    }//GEN-LAST:event_tableMouseClicked

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        try {
            String name = txtName.getText();
            if (name == null) {
                JOptionPane.showMessageDialog(this, "Fill out all required fields", "Discount", JOptionPane.ERROR_MESSAGE);
            } else {
                discount.setType(name);
                if (discount.getAction() == Discount.PERCENTAGE_OFF) {
                    discount.setPercentage(Double.parseDouble(txtPercentage.getText()));
                } else {
                    discount.setPrice(new BigDecimal(txtMoney.getText()));
                }
                if (rad1.isSelected()) {
                    discount.setCondition(1);
                    discount.setConditionValue(Integer.parseInt(txtOfEach.getText()));
                } else {
                    discount.setCondition(2);
                    discount.setConditionValue(Integer.parseInt(txtTotal.getText()));
                }
                discount.setStart(((Date) spinStart.getValue()).getTime());
                discount.setEnd(((Date) spinEnd.getValue()).getTime());
                try {
                    dc.updateDiscount(discount);
                } catch (SQLException | DiscountNotFoundException | IOException ex) {
                    showError(ex);
                }

                showAllDiscounts();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Fill out all required fields", "Discount", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int index = table.getSelectedRow();
        if (index != -1) {
            int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove the following discount?\n" + currentTableContents.get(index), "Remove Discount", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    dc.removeDiscount(currentTableContents.get(index).getId());
                } catch (SQLException | DiscountNotFoundException | IOException ex) {
                    showError(ex);
                }
                showAllDiscounts();
                setCurrentDiscount(null);
            }
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String terms = txtSearch.getText();

        if (terms.isEmpty()) {
            showAllDiscounts();
            return;
        }

        List<Discount> newList = new ArrayList<>();

        for (Discount d : currentTableContents) {
            if (d.getName().toLowerCase().contains(terms.toLowerCase())) {
                newList.add(d);
            }
        }

        if (newList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records found", "Search", JOptionPane.PLAIN_MESSAGE);
        } else {
            currentTableContents = newList;
            if (newList.size() == 1) {
                setCurrentDiscount(newList.get(0));
            }
        }
        updateTable();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        btnSearch.doClick();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnAddTriggerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTriggerActionPerformed
        Product p = ProductSelectDialog.showDialog(this, dc);
        if (p == null) {
            return;
        }
        Trigger t = new Trigger(discount.getId(), p.getId());
        try {
            dc.addTrigger(t);
            currentTriggerContents.add(t);
            updateTriggerTable();
        } catch (IOException | SQLException ex) {
            showError(ex);
        }
    }//GEN-LAST:event_btnAddTriggerActionPerformed

    private void btnRemoveTriggerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveTriggerActionPerformed
        if (tableTrig.getSelectedRow() == -1) {
            return;
        }
        Trigger t = currentTriggerContents.get(tableTrig.getSelectedRow());
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this trigger?", "Remove Trigger", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                dc.removeTrigger(t.getId());
                getTriggers();
            } catch (IOException | SQLException | JTillException ex) {
                showError(ex);
            }
        }
    }//GEN-LAST:event_btnRemoveTriggerActionPerformed

    private void cmbActionPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_cmbActionPropertyChange
        if (cmbAction.getSelectedIndex() == Discount.PERCENTAGE_OFF) {
            lblPercentage.setEnabled(true);
            txtPercentage.setEnabled(true);
            lblMoney.setEnabled(false);
            txtMoney.setEnabled(false);
        } else {
            lblPercentage.setEnabled(false);
            txtPercentage.setEnabled(false);
            lblMoney.setEnabled(true);
            txtMoney.setEnabled(true);
        }
    }//GEN-LAST:event_cmbActionPropertyChange

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddTrigger;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnRemoveTrigger;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnShowAll;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cmbAction;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblAtLeast;
    private javax.swing.JLabel lblMoney;
    private javax.swing.JLabel lblOfEach;
    private javax.swing.JLabel lblPercentage;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JRadioButton rad1;
    private javax.swing.JRadioButton rad2;
    private javax.swing.JSpinner spinEnd;
    private javax.swing.JSpinner spinStart;
    private javax.swing.JTable table;
    private javax.swing.JTable tableTrig;
    private javax.swing.JTextField txtMoney;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtOfEach;
    private javax.swing.JTextField txtPercentage;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables
}
