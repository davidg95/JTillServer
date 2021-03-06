/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.HeadlessException;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
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
public class TransactionViewerWindow extends javax.swing.JInternalFrame {

    private final JTill jtill;

    private MyModel model;

    /**
     * Creates new form TransactionViewerWindow
     */
    public TransactionViewerWindow(JTill jtill) {
        super();
        this.jtill = jtill;
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        initComponents();
        this.setFrameIcon(new ImageIcon(GUI.icon));
        pickStart.setDate(new Date(0));
        pickEnd.setDate(new Date());
        setVisible(true);
        GUI.gui.internal.add(this);
        init();
    }

    public static void showWindow(JTill jtill) {
        final TransactionViewerWindow window = new TransactionViewerWindow(jtill);
        try {
            window.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(TransactionViewerWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void init() {
        try {
            List<Sale> sales = jtill.getDataConnection().getAllSales();
            model = new MyModel(sales);
            table.setModel(model);
            final List<Staff> staff = jtill.getDataConnection().getAllStaff();
            final List<Till> tills = jtill.getDataConnection().getAllTills();
            cmbStaff.setModel(new DefaultComboBoxModel(staff.toArray()));
            cmbTerminal.setModel(new DefaultComboBoxModel(tills.toArray()));
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(0).setMinWidth(40);
        table.setSelectionModel(new ForcedListSelectionModel());
    }

    private class MyModel implements TableModel {

        private List<Sale> sales;
        private final List<TableModelListener> listeners;

        public MyModel(List<Sale> sales) {
            this.sales = sales;
            listeners = new LinkedList<>();
        }

        public void setContents(List<Sale> sales) {
            this.sales = sales;
            final int totalSales = sales.size();
            BigDecimal totalValue = BigDecimal.ZERO;
            BigDecimal totalTax = BigDecimal.ZERO;
            for (Sale s : sales) {
                totalValue = totalValue.add(s.getTotal());
                for (SaleItem si : s.getSaleItems()) {
                    totalTax = totalTax.add(si.getTotalTax());
                }
            }
            txtTotalSales.setValue(totalSales);
            txtTotalValue.setValue(totalValue);
            txtTax.setValue(totalTax);
            alertAll();
        }

        public Sale getSale(int i) {
            return sales.get(i);
        }

        private void alertAll() {
            for (TableModelListener l : listeners) {
                l.tableChanged(new TableModelEvent(this));
            }
        }

        @Override
        public int getRowCount() {
            return sales.size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int i) {
            switch (i) {
                case 0: {
                    return "ID";
                }
                case 1: {
                    return "Timestamp";
                }
                case 2: {
                    return "Value";
                }
                case 3: {
                    return "Staff";
                }
                case 4: {
                    return "Terminal";
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
        public boolean isCellEditable(int rowIndex, int i) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            final Sale sale = sales.get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return sale.getId();
                }
                case 1: {
                    return sale.getDate();
                }
                case 2: {
                    return "£" + sale.getTotal().setScale(2, 6);
                }
                case 3: {
                    return sale.getStaff();
                }
                case 4: {
                    return sale.getTill();
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
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
        jPanel1 = new javax.swing.JPanel();
        cmbTerminal = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        chkAllStaff = new javax.swing.JCheckBox();
        chkAllTills = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        btnClear = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        spinIdStart = new javax.swing.JSpinner();
        spinMoneyMin = new javax.swing.JSpinner();
        cmbStaff = new javax.swing.JComboBox<>();
        spinMoneyMax = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        spinIdEnd = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        btnSearch = new javax.swing.JButton();
        pickEnd = new org.jdesktop.swingx.JXDatePicker();
        pickStart = new org.jdesktop.swingx.JXDatePicker();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtTotalValue = new javax.swing.JFormattedTextField();
        txtTotalSales = new javax.swing.JFormattedTextField();
        txtTax = new javax.swing.JFormattedTextField();
        jLabel11 = new javax.swing.JLabel();
        btnRemoveCashed = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setTitle("Transaction Viewer");
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/tillIcon.png"))); // NOI18N

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Timestamp", "Value", "Staff", "Terminal"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table.getTableHeader().setReorderingAllowed(false);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setResizable(false);
            table.getColumnModel().getColumn(1).setResizable(false);
            table.getColumnModel().getColumn(2).setResizable(false);
            table.getColumnModel().getColumn(3).setResizable(false);
            table.getColumnModel().getColumn(4).setResizable(false);
        }

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Filter Options"));

        cmbTerminal.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbTerminal.setEnabled(false);

        jLabel6.setText("End date:");

        chkAllStaff.setSelected(true);
        chkAllStaff.setText("All");
        chkAllStaff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkAllStaffActionPerformed(evt);
            }
        });

        chkAllTills.setSelected(true);
        chkAllTills.setText("All");
        chkAllTills.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkAllTillsActionPerformed(evt);
            }
        });

        jLabel1.setText("Terminal:");

        btnClear.setText("Clear Search");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        jLabel2.setText("Staff:");

        jLabel8.setText("ID end");

        spinMoneyMin.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));
        spinMoneyMin.setValue(-99999.99);

        cmbStaff.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbStaff.setEnabled(false);

        spinMoneyMax.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));
        spinMoneyMax.setValue(99999.99);

        jLabel5.setText("Start date:");

        spinIdEnd.setValue(9999999);

        jLabel3.setText("Value from:");

        jLabel4.setText("Value to:");

        jLabel7.setText("ID start:");

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
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
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(cmbStaff, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cmbTerminal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(chkAllTills)
                                    .addComponent(chkAllStaff)))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(pickStart, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(pickEnd, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(spinIdEnd, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(spinIdStart, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(spinMoneyMax, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(spinMoneyMin, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClear)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbTerminal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkAllTills))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cmbStaff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkAllStaff))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(spinMoneyMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(spinMoneyMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(pickStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(pickEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spinIdStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(5, 5, 5)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spinIdEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSearch)
                    .addComponent(btnClear))
                .addContainerGap())
        );

        jLabel9.setText("Total Value:");

        jLabel10.setText("Total Sales:");

        txtTotalValue.setEditable(false);
        txtTotalValue.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getCurrencyInstance())));
        txtTotalValue.setText("0.00");

        txtTotalSales.setEditable(false);
        txtTotalSales.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        txtTax.setEditable(false);
        txtTax.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getCurrencyInstance())));

        jLabel11.setText("Total Tax:");

        btnRemoveCashed.setText("Remove Cashed Sales");
        btnRemoveCashed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveCashedActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel9)
                            .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnRemoveCashed)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtTax)
                                .addComponent(txtTotalValue, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                                .addComponent(txtTotalSales)))))
                .addGap(2, 2, 2)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 688, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(txtTotalValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(txtTotalSales, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRemoveCashed))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        final ModalDialog mDialog = new ModalDialog(this, "Search");
        //Create a runnable to pass into a worker thread
        final Runnable run = () -> {
            try {
                //Search options
                final Staff staff = (Staff) cmbStaff.getModel().getSelectedItem(); //Get the staff member to filter
                final Till terminal = (Till) cmbTerminal.getModel().getSelectedItem(); //Get the till to filter

                final boolean allStaff = chkAllStaff.isSelected(); //Check if the allStaff box is selected
                final boolean allTills = chkAllTills.isSelected(); //Check if the allTills box is selected

                final double minVal = (double) spinMoneyMin.getValue(); //Get the minimum sale value
                final double maxVal = (double) spinMoneyMax.getValue(); //Get the maximum sale value

                final Date startDate = pickStart.getDate();
                final Date endDate = new Date(pickEnd.getDate().getTime() + 86399999L);

                final int startID = (int) spinIdStart.getValue(); //Get the start ID
                final int endID = (int) spinIdEnd.getValue(); //Get the end ID

                //Check to make sure that the minimum value is less than the maximum value
                if (minVal > maxVal) {
                    mDialog.hide();
                    JOptionPane.showMessageDialog(this, "Minimum value must be greater or equal to the maximum value", "Transaction Search", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                //Check to make sure that the start time is afer then end time
                if (endDate.getTime() < startDate.getTime()) {
                    mDialog.hide();
                    JOptionPane.showMessageDialog(this, "Start date must be on or after then end date", "Transaction Search", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                //Check to make sure that the start ID is before the end ID
                if (startID > endID) {
                    mDialog.hide();
                    JOptionPane.showMessageDialog(this, "Start ID must be greater than or equal to the end ID", "Transaction Search", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    final List<Sale> sales = jtill.getDataConnection().getAllSales(); //Get all the sales
                    final List<Sale> newList = new LinkedList<>(); //Create a list for the filtered sales
                    //Check each sale and make sure it matches the criteria
                    for (Sale s : sales) {
                        if ((allTills || s.getTill().getId() == terminal.getId()) && (allStaff || s.getStaff().getId() == staff.getId()) && s.getTotal().compareTo(new BigDecimal(minVal)) >= 0 && s.getTotal().compareTo(new BigDecimal(maxVal)) <= 0 && s.getDate().after(startDate) && s.getDate().before(endDate) && (s.getId() >= startID && s.getId() <= endID)) {
                            newList.add(s);
                        }
                    }
                    //Display the sales in the table.
                    if (newList.isEmpty()) {
                        mDialog.hide();
                        JOptionPane.showMessageDialog(TransactionViewerWindow.this, "No results", "Transactions", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        model.setContents(newList);
                    }
                } catch (IOException | SQLException ex) {
                    mDialog.hide();
                    JOptionPane.showMessageDialog(TransactionViewerWindow.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
                this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                mDialog.hide();
            } catch (HeadlessException e) {
                this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                mDialog.hide();
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                mDialog.hide();
            }
        };
        final Thread thread = new Thread(run); //Create the worker thread
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        thread.start(); //Start the worker thread
        mDialog.show(); //Show the dialog to indicate that a search is in progress
    }//GEN-LAST:event_btnSearchActionPerformed

    private void chkAllTillsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAllTillsActionPerformed
        cmbTerminal.setEnabled(!chkAllTills.isSelected());
    }//GEN-LAST:event_chkAllTillsActionPerformed

    private void chkAllStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAllStaffActionPerformed
        cmbStaff.setEnabled(!chkAllStaff.isSelected());
    }//GEN-LAST:event_chkAllStaffActionPerformed

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        if (SwingUtilities.isLeftMouseButton(evt)) {
            if (evt.getClickCount() == 2) {
                final int index = table.getSelectedRow();
                if (index > -1) {
                    final Sale sale = model.getSale(index);
                    SaleDialog.showSaleDialog(jtill, sale);
                }
            }
        } else if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem view = new JMenuItem("View");
            final Font boldFont = new Font(view.getFont().getFontName(), Font.BOLD, view.getFont().getSize());
            view.setFont(boldFont);
            JMenuItem print = new JMenuItem("Print");

            view.addActionListener((event) -> {
                final int index = table.getSelectedRow();
                if (index > -1) {
                    final Sale sale = model.getSale(index);
                    SaleDialog.showSaleDialog(jtill, sale);
                }
            });

            print.addActionListener((event) -> {

            });

            menu.add(view);
            menu.add(print);
            menu.show(table, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tableMouseClicked

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        cmbTerminal.setEnabled(false);
        cmbStaff.setEnabled(false);
        chkAllTills.setSelected(true);
        chkAllStaff.setSelected(true);
        spinMoneyMin.setValue(-99999.99);
        spinMoneyMax.setValue(99999.99);
        pickStart.setDate(new Date(0));
        pickEnd.setDate(new Date());
        spinIdStart.setValue(0);
        spinIdEnd.setValue(9999999);
        init();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnRemoveCashedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveCashedActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove cashed sales?", "Remove Cashed Sales", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            final ModalDialog mDialog = new ModalDialog(this, "Remove Cashed Sales");
            final Runnable run = () -> {
                try {
                    int val = jtill.getDataConnection().removeCashedSales();
                    if (val == 0) {
                        mDialog.hide();
                        JOptionPane.showMessageDialog(TransactionViewerWindow.this, "No sales to remove", "Remove Cashed Sales", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    init();
                    mDialog.hide();
                    JOptionPane.showMessageDialog(TransactionViewerWindow.this, "Remove " + val + " sales", "Remove Cashed Sales", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException | SQLException ex) {
                    mDialog.hide();
                    JOptionPane.showMessageDialog(TransactionViewerWindow.this, ex, "Remove Cashed Sales", JOptionPane.ERROR_MESSAGE);
                } finally {
                    mDialog.hide();
                }
            };
            new Thread(run, "RemoveSales").start();
            mDialog.show();
        }
    }//GEN-LAST:event_btnRemoveCashedActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnRemoveCashed;
    private javax.swing.JButton btnSearch;
    private javax.swing.JCheckBox chkAllStaff;
    private javax.swing.JCheckBox chkAllTills;
    private javax.swing.JComboBox<String> cmbStaff;
    private javax.swing.JComboBox<String> cmbTerminal;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private org.jdesktop.swingx.JXDatePicker pickEnd;
    private org.jdesktop.swingx.JXDatePicker pickStart;
    private javax.swing.JSpinner spinIdEnd;
    private javax.swing.JSpinner spinIdStart;
    private javax.swing.JSpinner spinMoneyMax;
    private javax.swing.JSpinner spinMoneyMin;
    private javax.swing.JTable table;
    private javax.swing.JFormattedTextField txtTax;
    private javax.swing.JFormattedTextField txtTotalSales;
    private javax.swing.JFormattedTextField txtTotalValue;
    // End of variables declaration//GEN-END:variables
}
