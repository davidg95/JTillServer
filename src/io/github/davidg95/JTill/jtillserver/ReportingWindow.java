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
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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

    /**
     * Creates new form ReportingWindow
     */
    public ReportingWindow(DataConnect dc) {
        this.dc = dc;
        initComponents();
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
                g2.drawString(it.getName(), itemCol, y);
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

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnDep = new javax.swing.JButton();
        btnCat = new javax.swing.JButton();
        btnGenerate = new javax.swing.JButton();

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnDep, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                    .addComponent(btnCat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(btnDep))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(btnCat))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnGenerate.setText("Generate Report");
        btnGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateActionPerformed(evt);
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
                        .addGap(291, 291, 291)
                        .addComponent(btnGenerate, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(298, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 244, Short.MAX_VALUE)
                .addComponent(btnGenerate)
                .addGap(75, 75, 75))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDepActionPerformed
        dep = DepartmentSelectDialog.showDialog(this, dc);
        if (dep != null) {
            btnDep.setText(dep.getName());
        }
    }//GEN-LAST:event_btnDepActionPerformed

    private void btnCatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCatActionPerformed
        cat = CategorySelectDialog.showDialog(this, dc);
        if (cat != null) {
            btnCat.setText(cat.getName());
        }
    }//GEN-LAST:event_btnCatActionPerformed

    private void btnGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateActionPerformed
        try {
            List<SaleItem> items = dc.getAllSaleItems();
            List<SaleItem> fil = new ArrayList<>();
            List<SaleItem> fil2 = new ArrayList<>();
            String deps = "All";
            if (dep != null) {
                deps = dep.getName();
            }
            String cats = "All";
            if (cat != null) {
                cats = cat.getName();
            }
            for (SaleItem i : items) {
                if (cat != null) {
                    if (i.getItem() instanceof Product) {
                        Product p = (Product) i.getItem();
                        if (p.getCategory() == cat.getId()) {
                            fil.add(i);
                        }
                    }
                } else {
                    fil.add(i);
                }
            }
            for (SaleItem i : fil) {
                if (dep != null) {
                    if (i.getItem() instanceof Product) {
                        Product p = (Product) i.getItem();
                        if (p.getDepartment() == dep.getId()) {
                            fil2.add(i);
                        }
                    }
                } else {
                    fil2.add(i);
                }
            }

            ReportPrinter p = new ReportPrinter(fil2, deps, cats);
            p.printReport();
        } catch (IOException | SQLException ex) {
            Logger.getLogger(ReportingWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnGenerateActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCat;
    private javax.swing.JButton btnDep;
    private javax.swing.JButton btnGenerate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
