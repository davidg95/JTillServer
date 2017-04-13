/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David
 */
public class ReportingWindow extends javax.swing.JFrame {

    private final DataConnect dc;

    //Search options
    private Department dep = null;
    private Category cat = null;
    private Date startDate = new Date();
    private Date endDate = new Date();

    /**
     * Creates new form ReportingWindow
     */
    public ReportingWindow(DataConnect dc, Image icon) {
        this.dc = dc;
        initComponents();
        setIconImage(icon);
        setTitle("Sales reporting");
        btnStartDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(startDate));
        btnEndDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(endDate));
    }

    /**
     * Method to show the reporting window.
     *
     * @param dc the data connection.
     * @param icon the icon for the window.
     */
    public static void showWindow(DataConnect dc, Image icon) {
        new ReportingWindow(dc, icon).setVisible(true);
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
                try {
                    if (it.getType() == SaleItem.PRODUCT) {
                        final Product product = dc.getProduct(it.getItem());
                        g2.drawString(product.getName(), itemCol, y);
                    } else {
                        final Discount discount = dc.getDiscount(it.getItem());
                        g2.drawString(discount.getName(), itemCol, y);
                    }
                    g2.drawString("" + it.getQuantity(), quantityCol, y);
                    g2.drawString("£" + it.getPrice(), totalCol, y);
                    y += 30;
                } catch (IOException | ProductNotFoundException | SQLException | DiscountNotFoundException ex) {
                    Logger.getLogger(ReportingWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
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

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnDep = new javax.swing.JButton();
        btnCat = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btnStartDate = new javax.swing.JButton();
        btnEndDate = new javax.swing.JButton();
        chkBoth = new javax.swing.JCheckBox();
        btnGenerate = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        txtItemsSold = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtTotalSales = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtTotalTax = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtTotalTransactions = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Search By"));

        jLabel1.setText("Department:");

        jLabel2.setText("Category:");

        btnDep.setText("All");
        btnDep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDepActionPerformed(evt);
            }
        });

        btnCat.setText("All");
        btnCat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCatActionPerformed(evt);
            }
        });

        jLabel3.setText("Start Date:");

        jLabel4.setText("End Date:");

        btnStartDate.setText("Choose Start");
        btnStartDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartDateActionPerformed(evt);
            }
        });

        btnEndDate.setText("Choose End");
        btnEndDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEndDateActionPerformed(evt);
            }
        });

        chkBoth.setText("Items must belong to both");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkBoth)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnDep, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCat, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnStartDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnEndDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(btnDep)
                    .addComponent(jLabel3)
                    .addComponent(btnStartDate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(btnCat))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(btnEndDate)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkBoth)
                .addGap(0, 20, Short.MAX_VALUE))
        );

        btnGenerate.setText("Generate Report");
        btnGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateActionPerformed(evt);
            }
        });

        jLabel5.setText("Items Sold:");

        jLabel6.setText("Total Sales:");

        jLabel7.setText("Total Tax:");

        jLabel8.setText("Total Transactions:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(291, 291, 291)
                                .addComponent(btnGenerate, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtTotalTax, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                                    .addComponent(txtTotalSales)
                                    .addComponent(txtItemsSold))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtTotalTransactions, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 288, Short.MAX_VALUE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtItemsSold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(txtTotalTransactions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTotalSales, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtTotalTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 138, Short.MAX_VALUE)
                .addComponent(btnGenerate)
                .addGap(75, 75, 75))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDepActionPerformed
        dep = DepartmentSelectDialog.showDialog(this, dc);
        if (dep != null) {
            btnDep.setText(dep.getName());
        } else{
            btnDep.setText("All");
        }
    }//GEN-LAST:event_btnDepActionPerformed

    private void btnCatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCatActionPerformed
        cat = CategorySelectDialog.showDialog(this, dc);
        if (cat != null) {
            btnCat.setText(cat.getName());
        } else{
            btnCat.setText("All");
        }
    }//GEN-LAST:event_btnCatActionPerformed

    private void btnGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateActionPerformed
        try {
//            List<SaleItem> items = dc.getAllSaleItems();
//            List<SaleItem> fil = new ArrayList<>();
//            List<SaleItem> fil2 = new ArrayList<>();
//            String deps = "All";
//            if (dep != null) {
//                deps = dep.getName();
//            }
//            String cats = "All";
//            if (cat != null) {
//                cats = cat.getName();
//            }
//            for (SaleItem i : items) {
//                if (cat != null) {
//                    if (i.getType() == SaleItem.PRODUCT) {
//                        Product p = dc.getProduct(i.getItem());
//                        if (p.getCategory() == cat.getId()) {
//                            fil.add(i);
//                        }
//                    }
//                } else {
//                    fil.add(i);
//                }
//            }
//            for (SaleItem i : fil) {
//                if (dep != null) {
//                    if (i.getType() == SaleItem.PRODUCT) {
//                        Product p = dc.getProduct(i.getItem());
//                        if (p.getDepartment() == dep.getId()) {
//                            fil2.add(i);
//                        }
//                    }
//                } else {
//                    fil2.add(i);
//                }
//            }
            List<SaleItem> fil2 = dc.searchSaleItems(dep.getId(), cat.getId(), chkBoth.isSelected(), startDate, endDate);
            int itemsSold = 0;
            BigDecimal sales = BigDecimal.ZERO;
            BigDecimal tax = BigDecimal.ZERO;
            int transactions = 0;

            for (SaleItem i : fil2) {
                if (i.getType() == SaleItem.PRODUCT) {
                    itemsSold += i.getQuantity();
                    sales = sales.add(i.getPrice().multiply(new BigDecimal(i.getQuantity())));
                    final Product p = dc.getProduct(i.getItem());
                    final Tax t = dc.getTax(p.getTax());
                    double taxP = t.getValue() / 100;
                    tax = tax.add(i.getPrice().multiply(new BigDecimal(i.getQuantity()).multiply(new BigDecimal(taxP))));
                }
            }
            transactions = dc.getAllSales().size();
            txtItemsSold.setText(itemsSold + "");
            txtTotalSales.setText("£" + new DecimalFormat("0.00").format(sales));
            txtTotalTax.setText("£" + new DecimalFormat("0.00").format(tax));
            txtTotalTransactions.setText(transactions + "");

//            ReportPrinter p = new ReportPrinter(fil2, deps, cats);
//            p.printReport();
        } catch (IOException | SQLException | ProductNotFoundException | TaxNotFoundException | JTillException ex) {
            Logger.getLogger(ReportingWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnGenerateActionPerformed

    private void btnStartDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartDateActionPerformed
        startDate = DateSelectDialog.showDialog(this);
        if (startDate == null) {
            startDate = new Date();
            return;
        }
        btnStartDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(startDate));
    }//GEN-LAST:event_btnStartDateActionPerformed

    private void btnEndDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEndDateActionPerformed
        endDate = DateSelectDialog.showDialog(this);
        if (endDate == null) {
            endDate = new Date();
            return;
        }
        btnEndDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(endDate));
    }//GEN-LAST:event_btnEndDateActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCat;
    private javax.swing.JButton btnDep;
    private javax.swing.JButton btnEndDate;
    private javax.swing.JButton btnGenerate;
    private javax.swing.JButton btnStartDate;
    private javax.swing.JCheckBox chkBoth;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField txtItemsSold;
    private javax.swing.JTextField txtTotalSales;
    private javax.swing.JTextField txtTotalTax;
    private javax.swing.JTextField txtTotalTransactions;
    // End of variables declaration//GEN-END:variables
}
