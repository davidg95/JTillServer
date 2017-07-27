/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public final class ReportingWindow extends javax.swing.JInternalFrame {

    private final DataConnect dc;

    //Table
    private final DefaultTableModel model;
    private List<SaleItem> items;

    /**
     * Creates new form ReportingWindow
     *
     * @param dc the data connection.
     * @param icon the frame icon.
     */
    public ReportingWindow(DataConnect dc, Image icon) {
        this.dc = dc;
        initComponents();
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        setTitle("Sales reporting");
        items = new ArrayList<>();
        model = (DefaultTableModel) table.getModel();
        table.setModel(model);
        updateTable();
        init();
    }

    private void init() {
        try {
            final List<Department> departments = dc.getAllDepartments();
            final List<Category> categories = dc.getAllCategorys();
            cmbDepartment.setModel(new DefaultComboBoxModel(departments.toArray()));
            cmbCategory.setModel(new DefaultComboBoxModel(categories.toArray()));
        } catch (IOException | SQLException ex) {
            Logger.getLogger(ReportingWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sortList(List<SaleItem> saleItems) {
        items.clear();
        for (SaleItem item : saleItems) {
            boolean found = false;
            for (SaleItem i : items) {
                if (i.equals(item)) {
                    found = true;
                    i.increaseQuantity(item.getQuantity());
                    i.getPrice().add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                    break;
                }
            }
            if (!found) {
                items.add(item);
            }
        }
    }

    private void updateTable() {
        model.setRowCount(0);
        for (SaleItem i : items) {
            final Product p = (Product) i.getItem();
            model.addRow(new Object[]{i.getId(), p.getName(), i.getQuantity(), new DecimalFormat("#.00").format(i.getPrice().multiply(new BigDecimal(i.getQuantity())))});
        }
    }

    /**
     * Method to show the reporting window.
     *
     * @param dc the data connection.
     * @param icon the icon for the window.
     */
    public static void showWindow(DataConnect dc, Image icon) {
        ReportingWindow window = new ReportingWindow(dc, icon);
        GUI.gui.internal.add(window);
        window.setVisible(true);
        try {
            window.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(ReportingWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public class ReportPrinter implements Printable {

        private final List<SaleItem> items;
        public PrinterJob job;
        public boolean ready;
        private final String dep;
        private final String cat;

        public ReportPrinter(List<SaleItem> items, String d, String c) {
            this.items = items;
            this.dep = d;
            this.cat = c;
        }

        public void printReport() {
            job = PrinterJob.getPrinterJob();
            ready = job.printDialog();
            if (ready) {
                job.setPrintable(this);
                try {
                    job.print();
                } catch (PrinterException ex) {
                    Logger.getLogger(ReportingWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            String header = "Sales Report";

            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            Font oldFont = graphics.getFont();

            g2.setFont(new Font("Arial", Font.BOLD, 20)); //Use a differnt font for the header.
            g2.drawString(header, 70, 60);
            g2.setFont(oldFont); //Chagne back to the old font.

            //Print sale info.
            g2.drawString("Items: " + items.size(), 70, 90);
            g2.drawString("Department: " + dep, 70, 110);
            g2.drawString("Category: " + cat, 70, 130);

            final int itemCol = 100;
            final int quantityCol = 300;
            final int totalCol = 420;
            int y = 170;

            //Print collumn headers.
            g2.drawString("Item", itemCol, y);
            g2.drawString("Quantity", quantityCol, y);
            g2.drawString("Total", totalCol, y);
            g2.drawLine(itemCol - 30, y + 10, totalCol + 100, y + 10);

            y += 30;

            //Print the sale items.
            for (SaleItem it : items) {
                if (it.getType() == SaleItem.PRODUCT) {
                    final Product product = (Product) it.getItem();
                    g2.drawString(product.getName(), itemCol, y);
                } else {
                    final Discount discount = (Discount) it.getItem();
                    g2.drawString(discount.getName(), itemCol, y);
                }
                g2.drawString("" + it.getQuantity(), quantityCol, y);
                g2.drawString("£" + it.getPrice(), totalCol, y);
                y += 30;
            }
            g2.drawLine(itemCol - 30, y - 20, totalCol + 100, y - 20);

            return PAGE_EXISTS;
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

        panelSearch = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btnGenerate = new javax.swing.JButton();
        cmbDepartment = new javax.swing.JComboBox<>();
        cmbCategory = new javax.swing.JComboBox<>();
        spinStart = new javax.swing.JSpinner();
        spinEnd = new javax.swing.JSpinner();
        chkAllDep = new javax.swing.JCheckBox();
        chkAllCat = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtItems = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtSales = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtTax = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtNet = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtProfitPreTax = new javax.swing.JTextField();

        panelSearch.setBorder(javax.swing.BorderFactory.createTitledBorder("Search By"));

        jLabel1.setText("Department:");

        jLabel2.setText("Category:");

        jLabel3.setText("Start Date:");

        jLabel4.setText("End Date:");

        btnGenerate.setText("Generate Report");
        btnGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateActionPerformed(evt);
            }
        });

        cmbDepartment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        spinStart.setModel(new javax.swing.SpinnerDateModel());

        spinEnd.setModel(new javax.swing.SpinnerDateModel());

        chkAllDep.setText("All");
        chkAllDep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkAllDepActionPerformed(evt);
            }
        });

        chkAllCat.setText("All");
        chkAllCat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkAllCatActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelSearchLayout = new javax.swing.GroupLayout(panelSearch);
        panelSearch.setLayout(panelSearchLayout);
        panelSearchLayout.setHorizontalGroup(
            panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbDepartment, 0, 146, Short.MAX_VALUE)
                    .addComponent(cmbCategory, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(chkAllCat, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkAllDep, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(spinStart)
                    .addComponent(spinEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnGenerate, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(121, Short.MAX_VALUE))
        );
        panelSearchLayout.setVerticalGroup(
            panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSearchLayout.createSequentialGroup()
                .addGroup(panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(btnGenerate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(panelSearchLayout.createSequentialGroup()
                            .addGroup(panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel3)
                                .addComponent(spinStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel4)
                                .addComponent(spinEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(panelSearchLayout.createSequentialGroup()
                        .addGroup(panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(cmbDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(chkAllDep))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(chkAllCat))))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Quantity", "Total Value"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

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
            table.getColumnModel().getColumn(0).setResizable(false);
            table.getColumnModel().getColumn(1).setResizable(false);
            table.getColumnModel().getColumn(2).setResizable(false);
        }

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Breakdown"));

        jLabel5.setText("Total Items Sold:");

        txtItems.setEditable(false);
        txtItems.setText("0");

        jLabel6.setText("Total Sales:");

        txtSales.setEditable(false);
        txtSales.setText("£0.00");

        jLabel7.setText("Tax Payable:");

        txtTax.setEditable(false);
        txtTax.setText("£0.00");

        jLabel8.setText("Net Profit");

        txtNet.setEditable(false);
        txtNet.setText("£0.00");

        jLabel9.setText("Profit Before Tax:");

        txtProfitPreTax.setEditable(false);
        txtProfitPreTax.setText("£0.00");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtItems)
                    .addComponent(txtSales, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
                    .addComponent(txtProfitPreTax))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtTax)
                    .addComponent(txtNet, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtItems, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtSales, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtProfitPreTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtNet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelSearch, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        if (SwingUtilities.isLeftMouseButton(evt)) {
            if (evt.getClickCount() == 2) {

            }
        }
    }//GEN-LAST:event_tableMouseClicked

    private void btnGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateActionPerformed
        final ModalDialog mDialog = new ModalDialog(this, "Sales Report", "Generating report...");
        final Runnable run = () -> {
            try {
                int depid = ((Department) cmbDepartment.getSelectedItem()).getId(); //Get the selected Department
                int catid = ((Category) cmbCategory.getSelectedItem()).getId();  //Get the selected Category
                if (chkAllDep.isSelected()) { //Check if all departments was selected
                    depid = -1;
                }
                if (chkAllCat.isSelected()) { //Check if all categories was selected
                    catid = -1;
                }
                final Date startDate = (Date) spinStart.getValue(); //Get the selected start date
                final Date endDate = (Date) spinEnd.getValue(); //Get the selected end date

                final List<SaleItem> saleItems = dc.searchSaleItems(depid, catid, startDate, endDate); //Get a list of all the sale items that match the search

                if (saleItems.isEmpty()) {
                    mDialog.hide();
                    JOptionPane.showInternalMessageDialog(GUI.gui.internal, "No results", "Sales Reporting", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                int itemsSold = 0;
                BigDecimal sales = BigDecimal.ZERO;
                BigDecimal expenses = BigDecimal.ZERO;
                BigDecimal tax = BigDecimal.ZERO;

                for (SaleItem i : saleItems) {
                    if (i.getType() == SaleItem.PRODUCT) {
                        final Product product = (Product) i.getItem();
                        final BigDecimal in = i.getPrice().multiply(new BigDecimal(i.getQuantity()));
                        final BigDecimal out = product.getCostPrice().multiply(new BigDecimal(i.getQuantity()));
                        expenses = expenses.add(out);
                        itemsSold += i.getQuantity();
                        sales = sales.add(in);
                        tax = tax.add(i.getTaxValue());
                    }
                }

                final BigDecimal profitBeforeTax = sales.subtract(expenses);
                sortList(saleItems);
                updateTable();
                txtItems.setText(itemsSold + "");
                txtSales.setText("£" + new DecimalFormat("0.00").format(sales));
                txtProfitPreTax.setText("£" + new DecimalFormat("0.00").format(profitBeforeTax));
                txtTax.setText("£" + new DecimalFormat("0.00").format(tax));
                txtNet.setText("£" + new DecimalFormat("0.00").format(profitBeforeTax.subtract(tax)));
            } catch (IOException | SQLException | JTillException ex) {
                Logger.getLogger(ReportingWindow.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                panelSearch.setEnabled(true);
                for (Component comp : panelSearch.getComponents()) {
                    comp.setEnabled(true);
                }
                if (chkAllCat.isSelected()) {
                    cmbCategory.setEnabled(false);
                }
                if (chkAllDep.isSelected()) {
                    cmbDepartment.setEnabled(false);
                }
                mDialog.hide();
            }
        };
        final Thread thread = new Thread(run);
        thread.start();
        panelSearch.setEnabled(false);
        for (Component comp : panelSearch.getComponents()) {
            comp.setEnabled(false);
        }
        mDialog.show();
    }//GEN-LAST:event_btnGenerateActionPerformed

    private void chkAllDepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAllDepActionPerformed
        cmbDepartment.setEnabled(!chkAllDep.isSelected());
    }//GEN-LAST:event_chkAllDepActionPerformed

    private void chkAllCatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAllCatActionPerformed
        cmbCategory.setEnabled(!chkAllCat.isSelected());
    }//GEN-LAST:event_chkAllCatActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGenerate;
    private javax.swing.JCheckBox chkAllCat;
    private javax.swing.JCheckBox chkAllDep;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbDepartment;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panelSearch;
    private javax.swing.JSpinner spinEnd;
    private javax.swing.JSpinner spinStart;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtItems;
    private javax.swing.JTextField txtNet;
    private javax.swing.JTextField txtProfitPreTax;
    private javax.swing.JTextField txtSales;
    private javax.swing.JTextField txtTax;
    // End of variables declaration//GEN-END:variables
}
