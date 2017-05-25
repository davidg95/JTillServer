/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;

/**
 * Window for adding, editing and remove discounts.
 *
 * @author David
 */
public class DiscountsWindow extends javax.swing.JInternalFrame {

    public static DiscountsWindow frame;

    private final DataConnect dc;

    private Discount discount;

    private DiscountBucket currentBucket;

    private final DefaultTableModel model;
    private List<Discount> currentTableContents;

    private final DefaultTableModel trigModel;
    private List<Trigger> currentTriggerContents;

    private final DefaultListModel listModel;
    private List<DiscountBucket> currentBuckets;

    /**
     * Creates new form DiscountsWindow
     */
    public DiscountsWindow(DataConnect dc, Image icon) {
        this.dc = dc;
//        this.setIconImage(icon);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setClosable(true);
        super.setFrameIcon(new ImageIcon(icon));
        initComponents();
        currentTableContents = new ArrayList<>();
        model = (DefaultTableModel) table.getModel();
        table.setModel(model);
        currentTriggerContents = new ArrayList<>();
        trigModel = (DefaultTableModel) tableTrig.getModel();
        tableTrig.setModel(trigModel);
        currentBuckets = new ArrayList<>();
        listModel = new DefaultListModel();
        listBuckets.setModel(listModel);
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
            GUI.gui.internal.add(frame);
        }
        if (frame.isVisible()) {
            frame.toFront();
        } else {
            update();
            frame.setCurrentDiscount(null);
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
            txtTriggers.setText("");
            lblBuckets.setText("");
            spinStart.setValue(new Date());
            spinEnd.setValue(new Date());
            discount = null;
            panelBuckets.setEnabled(false);
            listBuckets.setEnabled(false);
            for (Component c : panelBuckets.getComponents()) {
                c.setEnabled(false);
            }
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
            lblBuckets.setText(d.getCondition() + "");
            spinStart.setValue(new Date(d.getStart()));
            spinEnd.setValue(new Date(d.getEnd()));
            currentBucket = null;
            getBuckets();
            getTriggers();
            panelBuckets.setEnabled(true);
            listBuckets.setEnabled(true);
            for (Component c : panelBuckets.getComponents()) {
                c.setEnabled(true);
            }
        }
    }

    //***TRIGGER TABLE METHODS***//
    private void getTriggers() {
        if (currentBucket == null) {
            trigModel.setRowCount(0);
            return;
        }
        try {
            currentTriggerContents = dc.getBucketTriggers(currentBucket.getId());
            updateTriggerTable();
        } catch (IOException | SQLException | JTillException ex) {
            Logger.getLogger(DiscountsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateTriggerTable() {
        trigModel.setRowCount(0);
        for (Trigger t : currentTriggerContents) {
            try {
                final Product p = dc.getProduct(t.getProduct());
                final Plu plu = dc.getPluByProduct(p.getId());
                Object[] row = new Object[]{p.getName(), plu.getCode(), t.getQuantityRequired()};
                trigModel.addRow(row);
            } catch (ProductNotFoundException | JTillException | IOException | SQLException ex) {
                Logger.getLogger(DiscountsWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //***BUCKET LIST METHODS***//
    private void getBuckets() {
        try {
            currentBuckets = dc.getDiscountBuckets(discount.getId());
            updateBucketList();
        } catch (IOException | SQLException | DiscountNotFoundException ex) {
            Logger.getLogger(DiscountsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateBucketList() {
        listModel.setSize(0);
        for (DiscountBucket b : currentBuckets) {
            listModel.addElement("Bucket " + b.getId());
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
        jLabel5 = new javax.swing.JLabel();
        cmbAction = new javax.swing.JComboBox<>();
        lblMoney = new javax.swing.JLabel();
        txtMoney = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        spinStart = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        spinEnd = new javax.swing.JSpinner();
        panelBuckets = new javax.swing.JPanel();
        btnAddBucket = new javax.swing.JButton();
        btnRemoveBucket = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        listBuckets = new javax.swing.JList<>();
        lblAtLeast = new javax.swing.JLabel();
        lblBuckets = new javax.swing.JTextField();
        lblTotal = new javax.swing.JLabel();
        panelTriggers = new javax.swing.JPanel();
        txtTriggers = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableTrig = new javax.swing.JTable();
        chkRequire = new javax.swing.JCheckBox();
        btnSaveBucket = new javax.swing.JButton();
        btnAddTrigger = new javax.swing.JButton();
        btnRemoveTrigger = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        lblOfEach = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
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

        panelBuckets.setBorder(javax.swing.BorderFactory.createTitledBorder("Bucket Settings"));
        panelBuckets.setEnabled(false);

        btnAddBucket.setText("Add Bucket");
        btnAddBucket.setEnabled(false);
        btnAddBucket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddBucketActionPerformed(evt);
            }
        });

        btnRemoveBucket.setText("Remove Bucket");
        btnRemoveBucket.setEnabled(false);
        btnRemoveBucket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveBucketActionPerformed(evt);
            }
        });

        listBuckets.setEnabled(false);
        listBuckets.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listBucketsMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(listBuckets);

        lblAtLeast.setText("Require");
        lblAtLeast.setEnabled(false);

        lblBuckets.setText("1");
        lblBuckets.setEnabled(false);

        lblTotal.setText(" optional buckets to activate");
        lblTotal.setEnabled(false);

        panelTriggers.setBorder(javax.swing.BorderFactory.createTitledBorder("Triggers"));
        panelTriggers.setEnabled(false);

        txtTriggers.setEnabled(false);

        tableTrig.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product", "Barcode", "Required"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableTrig.setEnabled(false);
        tableTrig.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableTrigMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tableTrig);
        if (tableTrig.getColumnModel().getColumnCount() > 0) {
            tableTrig.getColumnModel().getColumn(0).setResizable(false);
            tableTrig.getColumnModel().getColumn(1).setResizable(false);
            tableTrig.getColumnModel().getColumn(2).setResizable(false);
        }

        chkRequire.setText("Require this bucket");
        chkRequire.setEnabled(false);

        btnSaveBucket.setText("Save");
        btnSaveBucket.setEnabled(false);
        btnSaveBucket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveBucketActionPerformed(evt);
            }
        });

        btnAddTrigger.setText("Add Product Trigger");
        btnAddTrigger.setEnabled(false);
        btnAddTrigger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTriggerActionPerformed(evt);
            }
        });

        btnRemoveTrigger.setText("Remove Selected Trigger");
        btnRemoveTrigger.setEnabled(false);
        btnRemoveTrigger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveTriggerActionPerformed(evt);
            }
        });

        jLabel3.setText("Require ");
        jLabel3.setEnabled(false);

        lblOfEach.setText(" from this bucket");
        lblOfEach.setEnabled(false);

        javax.swing.GroupLayout panelTriggersLayout = new javax.swing.GroupLayout(panelTriggers);
        panelTriggers.setLayout(panelTriggersLayout);
        panelTriggersLayout.setHorizontalGroup(
            panelTriggersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTriggersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTriggersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelTriggersLayout.createSequentialGroup()
                        .addComponent(btnAddTrigger)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveTrigger))
                    .addGroup(panelTriggersLayout.createSequentialGroup()
                        .addComponent(chkRequire)
                        .addGap(43, 43, 43)
                        .addComponent(btnSaveBucket))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelTriggersLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTriggers, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblOfEach)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelTriggersLayout.setVerticalGroup(
            panelTriggersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTriggersLayout.createSequentialGroup()
                .addGroup(panelTriggersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddTrigger)
                    .addComponent(btnRemoveTrigger))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelTriggersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTriggers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblOfEach)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelTriggersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkRequire)
                    .addComponent(btnSaveBucket))
                .addContainerGap())
        );

        javax.swing.GroupLayout panelBucketsLayout = new javax.swing.GroupLayout(panelBuckets);
        panelBuckets.setLayout(panelBucketsLayout);
        panelBucketsLayout.setHorizontalGroup(
            panelBucketsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBucketsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBucketsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelBucketsLayout.createSequentialGroup()
                        .addComponent(btnAddBucket, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveBucket))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBucketsLayout.createSequentialGroup()
                        .addComponent(lblAtLeast)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblBuckets, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTotal)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelTriggers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelBucketsLayout.setVerticalGroup(
            panelBucketsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBucketsLayout.createSequentialGroup()
                .addGroup(panelBucketsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelTriggers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelBucketsLayout.createSequentialGroup()
                        .addGroup(panelBucketsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAddBucket)
                            .addComponent(btnRemoveBucket))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelBucketsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblBuckets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblAtLeast)
                            .addComponent(lblTotal))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel2)
                            .addComponent(lblPercentage)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(spinStart, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtName)
                            .addComponent(cmbAction, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtPercentage, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblMoney)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtMoney, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnNew, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelete)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnShowAll, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(panelBuckets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                            .addComponent(spinStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(spinEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelBuckets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)
                    .addComponent(btnClose)
                    .addComponent(btnNew)
                    .addComponent(btnSave)
                    .addComponent(btnShowAll)
                    .addComponent(btnDelete))
                .addContainerGap())
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
                int condition = 1;
                if (name.equals("")) {
                    JOptionPane.showMessageDialog(this, "Fill out all required fields", "New Product", JOptionPane.ERROR_MESSAGE);
                } else if (percentage > 100 || percentage < 0) {
                    JOptionPane.showMessageDialog(this, "Please enter a value between 0 and 100", "Discount", JOptionPane.ERROR_MESSAGE);
                } else {
                    Date start = (Date) spinStart.getValue();
                    Date end = (Date) spinEnd.getValue();
                    d = new Discount(name, percentage, new BigDecimal(Double.toString(price)), action, condition, start.getTime(), end.getTime());
                    try {
                        Discount dis = dc.addDiscount(d);
                        showAllDiscounts();
                        panelBuckets.setEnabled(true);
                        listBuckets.setEnabled(true);
                        for (Component c : panelBuckets.getComponents()) {
                            c.setEnabled(true);
                        }
                    } catch (SQLException | IOException ex) {
                        showError(ex);
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Fill out all required fields", "New Product", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            setCurrentDiscount(null);
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
            if (name == null || name.length() == 0) {
                JOptionPane.showMessageDialog(this, "Fill out all required fields", "Discount", JOptionPane.ERROR_MESSAGE);
                txtName.setText(discount.getName());
            } else {
                discount.setType(name);
                if (discount.getAction() == Discount.PERCENTAGE_OFF) {
                    discount.setPercentage(Double.parseDouble(txtPercentage.getText()));
                } else {
                    discount.setPrice(new BigDecimal(txtMoney.getText()));
                }
                discount.setCondition(Integer.parseInt(lblBuckets.getText()));
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
        Product p = ProductSelectDialog.showDialog(this);
        if (p == null) {
            return;
        }
        String val = JOptionPane.showInternalInputDialog(GUI.gui.internal, "Enter amount required", "Trigger", JOptionPane.INFORMATION_MESSAGE);
        if (!Utilities.isNumber(val)) {
            JOptionPane.showInternalMessageDialog(GUI.gui.internal, "Must enter a number", "New Trigger", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int value = Integer.parseInt(val);
        if (value <= 0) {
            JOptionPane.showInternalMessageDialog(GUI.gui.internal, "Must enter a value greater than zero", "New Trigger", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Trigger t = new Trigger(currentBucket.getId(), p.getId(), value);
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

    private void btnAddBucketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddBucketActionPerformed
        DiscountBucket db = new DiscountBucket(discount.getId(), 1, false);
        try {
            db = dc.addBucket(db);
            currentBuckets.add(db);
            updateBucketList();
        } catch (IOException | SQLException ex) {
            Logger.getLogger(DiscountsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddBucketActionPerformed

    private void listBucketsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listBucketsMouseClicked
        int index = listBuckets.getSelectedIndex();
        if (index == -1) {
            return;
        }
        currentBucket = currentBuckets.get(index);
        txtTriggers.setText(currentBucket.getRequiredTriggers() + "");
        chkRequire.setSelected(currentBucket.isRequiredTrigger());
        getTriggers();
        tableTrig.setEnabled(true);
        for (Component c : panelTriggers.getComponents()) {
            c.setEnabled(true);
        }
    }//GEN-LAST:event_listBucketsMouseClicked

    private void tableTrigMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableTrigMouseClicked
        if (evt.getClickCount() == 2) {
            int value = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter quantity required", "Chagne quantity", JOptionPane.INFORMATION_MESSAGE));
            if (value > 0) {
                Trigger t = currentTriggerContents.get(tableTrig.getSelectedRow());
                t.setQuantityRequired(value);
                try {
                    dc.updateTrigger(t);
                } catch (IOException | SQLException | JTillException ex) {
                    Logger.getLogger(DiscountsWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_tableTrigMouseClicked

    private void btnRemoveBucketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveBucketActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRemoveBucketActionPerformed

    private void btnSaveBucketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveBucketActionPerformed
        String val = txtTriggers.getText();
        if (!Utilities.isNumber(val)) {
            JOptionPane.showMessageDialog(this, "Must enter a number for triggers", "Triggers", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int value = Integer.parseInt(val);
        if (value <= 0) {
            JOptionPane.showMessageDialog(this, "Must enter a value greater than zero", "Triggers", JOptionPane.ERROR_MESSAGE);
            return;
        }
        currentBucket.setRequiredTriggers(Integer.parseInt(txtTriggers.getText()));
        currentBucket.setRequiredTrigger(chkRequire.isSelected());
        try {
            dc.updateBucket(currentBucket);
        } catch (IOException | SQLException | JTillException ex) {
            Logger.getLogger(DiscountsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnSaveBucketActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddBucket;
    private javax.swing.JButton btnAddTrigger;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnRemoveBucket;
    private javax.swing.JButton btnRemoveTrigger;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSaveBucket;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnShowAll;
    private javax.swing.JCheckBox chkRequire;
    private javax.swing.JComboBox<String> cmbAction;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblAtLeast;
    private javax.swing.JTextField lblBuckets;
    private javax.swing.JLabel lblMoney;
    private javax.swing.JLabel lblOfEach;
    private javax.swing.JLabel lblPercentage;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JList<String> listBuckets;
    private javax.swing.JPanel panelBuckets;
    private javax.swing.JPanel panelTriggers;
    private javax.swing.JSpinner spinEnd;
    private javax.swing.JSpinner spinStart;
    private javax.swing.JTable table;
    private javax.swing.JTable tableTrig;
    private javax.swing.JTextField txtMoney;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPercentage;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtTriggers;
    // End of variables declaration//GEN-END:variables
}
