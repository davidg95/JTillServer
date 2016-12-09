/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.tillserver;

import io.github.davidg95.Till.till.Customer;
import io.github.davidg95.Till.till.CustomerNotFoundException;
import io.github.davidg95.Till.till.DBConnect;
import io.github.davidg95.Till.till.Discount;
import io.github.davidg95.Till.till.DiscountNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class CustomersWindow extends javax.swing.JFrame {

    public static CustomersWindow frame;

    private final Data data;
    private final DBConnect dbConn;

    private Customer customer;

    private final DefaultTableModel model;
    private List<Customer> currentTableContents;
    private DefaultComboBoxModel discountsModel;
    private List<Discount> discounts;

    /**
     * Creates new form CustomersWindow
     */
    public CustomersWindow() {
        this.data = TillServer.getData();
        this.dbConn = TillServer.getDBConnection();
        initComponents();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        currentTableContents = new ArrayList<>();
        model = (DefaultTableModel) table.getModel();
        showAllCustomers();
        init();
    }

    public static void showCustomersListWindow() {
        if (frame == null) {
            frame = new CustomersWindow();
        }
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.showAllCustomers();
        frame.init();
        frame.setVisible(true);
    }

    public static void update() {
        if (frame != null) {
            frame.showAllCustomers();
            frame.init();
        }
    }

    private void init() {
        try {
            discounts = dbConn.getAllDiscounts();
            discountsModel = new DefaultComboBoxModel(discounts.toArray());
            cmbDiscount.setModel(discountsModel);
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void updateTable() {
        model.setRowCount(0);

        for (Customer c : currentTableContents) {
            Object[] s = new Object[]{c.getId(), c.getName(), c.getTown(), c.getPhone()};
            model.addRow(s);
        }

        table.setModel(model);
    }

    private void showAllCustomers() {
        try {
            currentTableContents = dbConn.getAllCustomers();
            updateTable();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editCustomer() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            Customer c = currentTableContents.get(selectedRow);
            CustomerDialog.showEditCusomerDialog(this, data, c);
            updateTable();
        }
    }

    private void setCurrentCustomer(Customer c) {
        if (c == null) {
            txtName.setText("");
            txtPhone.setText("");
            txtMobile.setText("");
            txtEmail.setText("");
            txtLoyalty.setText("");
            txtNotes.setText("");
            txtAddress1.setText("");
            txtAddress2.setText("");
            txtTown.setText("");
            txtCounty.setText("");
            txtCountry.setText("");
            txtPostcode.setText("");
            cmbDiscount.setSelectedIndex(0);
            customer = null;
        } else {
            try {
                this.customer = c;
                txtName.setText(c.getName());
                txtPhone.setText(c.getPhone());
                txtMobile.setText(c.getMobile());
                txtEmail.setText(c.getEmail());
                txtLoyalty.setText(c.getLoyaltyPoints() + "");
                txtNotes.setText(c.getNotes());
                txtAddress1.setText(c.getAddressLine1());
                txtAddress2.setText(c.getAddressLine2());
                txtTown.setText(c.getTown());
                txtCounty.setText(c.getCounty());
                txtCountry.setText(c.getCountry());
                txtPostcode.setText(c.getPostcode());
                Discount d = dbConn.getDiscount(c.getDiscountID());
                int index = 0;
                for (int i = 0; i < discounts.size(); i++) {
                    if (discounts.get(i).getId() == d.getId()) {
                        index = i;
                        break;
                    }
                }
                cmbDiscount.setSelectedIndex(index);
            } catch (SQLException | DiscountNotFoundException ex) {
                showError(ex);
            }
        }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e, "Customers", JOptionPane.ERROR_MESSAGE);
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
        btnAdd = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnSearch = new javax.swing.JButton();
        btnShowAll = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        txtName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtMobile = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        txtLoyalty = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtNotes = new javax.swing.JTextArea();
        cmbDiscount = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        txtAddress1 = new javax.swing.JTextField();
        txtAddress2 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtTown = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtCounty = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtCountry = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtPostcode = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();

        setTitle("Manage Customers");
        setIconImage(TillServer.getIcon());

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "id", "Name", "Address", "Phone Number"
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
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(1).setResizable(false);
        }

        btnAdd.setText("Add Customer");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnSave.setText("Save Changes");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnRemove.setText("Remove Customer");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        btnSearch.setText("Search Customer");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
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

        jLabel5.setText("Email:");

        jLabel2.setText("Phone Number:");

        jLabel4.setText("Mobile:");

        jLabel1.setText("Name:");

        jLabel11.setText("Notes:");

        jLabel12.setText("Discount:");

        jLabel13.setText("Loyalty Points:");

        txtNotes.setColumns(20);
        txtNotes.setRows(5);
        jScrollPane2.setViewportView(txtNotes);

        cmbDiscount.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel1)
                                .addComponent(jLabel2))
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addComponent(jLabel12)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtLoyalty)
                    .addComponent(txtEmail)
                    .addComponent(txtMobile)
                    .addComponent(txtPhone)
                    .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)
                    .addComponent(cmbDiscount, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(44, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMobile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(cmbDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtLoyalty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Details", jPanel1);

        jLabel3.setText("Address Line 1:");

        jLabel6.setText("Address Line 2:");

        jLabel7.setText("Town:");

        jLabel8.setText("County:");

        jLabel9.setText("Country:");

        jLabel10.setText("Postcode:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtPostcode, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                    .addComponent(txtCountry)
                    .addComponent(txtCounty)
                    .addComponent(txtTown)
                    .addComponent(txtAddress1)
                    .addComponent(txtAddress2))
                .addContainerGap(52, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtAddress1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtAddress2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCounty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPostcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addContainerGap(113, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Address", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnAdd)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnSave)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRemove))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnSearch)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnShowAll)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE))
                    .addComponent(btnClose))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAdd)
                            .addComponent(btnSave)
                            .addComponent(btnRemove))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnSearch)
                            .addComponent(btnShowAll))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnClose)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        if (customer == null) {
            Customer c;
            try {
                String name = txtName.getText();
                String phone = txtPhone.getText();
                String mobile = txtMobile.getText();
                String email = txtEmail.getText();
                String notes = txtNotes.getText();
                int discount = 1;
                if (!discounts.isEmpty()) {
                    discount = discounts.get(cmbDiscount.getSelectedIndex()).getId();
                }
                int loyalty = Integer.parseInt(txtLoyalty.getText());

                String address1 = txtAddress1.getText();
                String address2 = txtAddress2.getText();
                String town = txtTown.getText();
                String county = txtCounty.getText();
                String country = txtCountry.getText();
                String postcode = txtPostcode.getText();
                if (name.equals("") || phone.equals("") || mobile.equals("") || email.equals("") || address1.equals("")) {
                    JOptionPane.showMessageDialog(this, "Fill out all required fields", "New Product", JOptionPane.ERROR_MESSAGE);
                } else {
                    c = new Customer(name, phone, mobile, email, discount, address1, address2, town, county, country, postcode, notes, loyalty);
                    try {
                        dbConn.addCustomer(c);
                        showAllCustomers();
                        setCurrentCustomer(null);
                        jTabbedPane1.setSelectedIndex(0);
                        txtName.requestFocus();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, ex, "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Fill out all required fields", "New Customer", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            setCurrentCustomer(null);
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        String name = txtName.getText();
        String phone = txtPhone.getText();
        String mobile = txtMobile.getText();
        String email = txtEmail.getText();
        String notes = txtNotes.getText();
        int discount = 1;
        if (!discounts.isEmpty()) {
            discount = discounts.get(cmbDiscount.getSelectedIndex()).getId();
        }
        int loyalty = Integer.parseInt(txtLoyalty.getText());

        String address1 = txtAddress1.getText();
        String address2 = txtAddress2.getText();
        String town = txtTown.getText();
        String county = txtCounty.getText();
        String country = txtCountry.getText();
        String postcode = txtPostcode.getText();

        customer.setName(name);
        customer.setPhone(phone);
        customer.setMobile(mobile);
        customer.setEmail(email);
        customer.setNotes(notes);
        customer.setDiscountID(discount);
        customer.setLoyaltyPoints(loyalty);

        customer.setAddressLine1(address1);
        customer.setAddressLine2(address2);
        customer.setTown(town);
        customer.setCounty(county);
        customer.setCountry(country);
        customer.setPostcode(postcode);

        try {
            dbConn.updateCustomer(customer);
        } catch (SQLException | CustomerNotFoundException ex) {
            showError(ex);
        }

        showAllCustomers();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        int index = table.getSelectedRow();
        if (index != -1) {
            int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove the following customer?\n" + currentTableContents.get(index), "Remove Customer", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    dbConn.removeCustomer(currentTableContents.get(index).getId());
                } catch (SQLException | CustomerNotFoundException ex) {
                    showError(ex);
                }
                showAllCustomers();
                setCurrentCustomer(null);
            }
        }
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnShowAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowAllActionPerformed
        showAllCustomers();
    }//GEN-LAST:event_btnShowAllActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        currentTableContents = CustomerSearchDialog.showSearchDialog(this, currentTableContents);
        updateTable();
        if (currentTableContents.size() == 1) {
            setCurrentCustomer(currentTableContents.get(0));
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void tableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMousePressed
        if (evt.getClickCount() == 2) {
            editCustomer();
        } else if (evt.getClickCount() == 1) {
            Customer c = currentTableContents.get(table.getSelectedRow());
            setCurrentCustomer(c);
        }
    }//GEN-LAST:event_tableMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnShowAll;
    private javax.swing.JComboBox<String> cmbDiscount;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtAddress1;
    private javax.swing.JTextField txtAddress2;
    private javax.swing.JTextField txtCountry;
    private javax.swing.JTextField txtCounty;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtLoyalty;
    private javax.swing.JTextField txtMobile;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextArea txtNotes;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextField txtPostcode;
    private javax.swing.JTextField txtTown;
    // End of variables declaration//GEN-END:variables
}
