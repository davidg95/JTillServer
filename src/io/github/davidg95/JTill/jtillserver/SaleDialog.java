/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterAbortException;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class SaleDialog extends javax.swing.JDialog {

    private static JDialog dialog;

    private final Sale sale;
    private final DataConnect dc;

    private final DefaultTableModel model;

    /**
     * Creates new form SaleDialog
     */
    public SaleDialog(Window parent, Sale sale, DataConnect dc) {
        super(parent);
        this.sale = sale;
        this.dc = dc;
        initComponents();
        model = (DefaultTableModel) tableItems.getModel();
        setLocationRelativeTo(parent);
        setModal(true);
        setTitle("Sale " + sale.getId());
        init();
    }

    public static void showSaleDialog(Component parent, Sale sale, DataConnect dc) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        dialog = new SaleDialog(window, sale, dc);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    private void init() {
        lblSaleID.setText("Sale ID: " + sale.getId());
        Calendar c = Calendar.getInstance();
        c.setTime(sale.getDate());
        lblTime.setText("Time: " + c.getTime().toString());
        if (sale.getCustomer() != null) {
            lblCustomer.setText("Customer: " + sale.getCustomer().getName());
        }
        lblTerminal.setText("Terminal: " + sale.getTerminal());
        lblTotal.setText("Sale Total: £" + sale.getTotal());

        model.setRowCount(0);

        for (SaleItem item : sale.getSaleItems()) {
            DecimalFormat df;
            if (item.getPrice().compareTo(BigDecimal.ZERO) > 1) {
                df = new DecimalFormat("#.00");
            } else {
                df = new DecimalFormat("0.00");
            }
            Object[] s = new Object[]{item.getQuantity(), item.getItem().getName(), df.format(item.getPrice().doubleValue())};
            model.addRow(s);
        }

        tableItems.setModel(model);
    }

    /**
     * Inner class for printing a receipt from the sale.
     */
    private class ReceiptPrinter implements Printable {
        
        private final Sale toPrint; //The Sale to print.

        /**
         * Create a new receipt for printing.
         *
         * @param s the Sale to print.
         */
        public ReceiptPrinter(Sale s) {
            toPrint = s;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            String header = "Sale Receipt";
            String footer = "Thank you for your custom";
            try {
                header = dc.getSetting("RECEIPT_HEADER"); //Get the receipt header for the receipt.
                footer = dc.getSetting("RECEIPT_FOOTER"); //Get the receipt footer for ther receipt.
            } catch (IOException ex) {
                Logger.getLogger(SaleDialog.class.getName()).log(Level.SEVERE, null, ex);
            }

            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            Font oldFont = graphics.getFont();

            g2.setFont(new Font("Arial", Font.BOLD, 20)); //Use a differnt font for the header.
            g2.drawString(header, 70, 60);
            g2.setFont(oldFont); //Chagne back to the old font.

            //Print sale info.
            g2.drawString("Receipt for sale: " + toPrint.getId(), 70, 90);
            g2.drawString("Time: " + toPrint.getDate(), 70, 110);
            g2.drawString("Served by " + toPrint.getStaff(), 70, 130);

            final int item = 100;
            final int quantity = 300;
            final int total = 420;
            int y = 170;

            //Print collumn headers.
            g2.drawString("Item", item, y);
            g2.drawString("Quantity", quantity, y);
            g2.drawString("Total", total, y);
            g2.drawLine(item - 30, y + 10, total + 100, y + 10);

            y += 30;

            //Print the sale items.
            for (SaleItem it : toPrint.getSaleItems()) {
                g2.drawString(it.getName(), item, y);
                g2.drawString("" + it.getQuantity(), quantity, y);
                g2.drawString("£" + it.getPrice(), total, y);
                y += 30;
            }
            g2.drawLine(item - 30, y - 20, total + 100, y - 20);
            g2.drawString("Total: £" + toPrint.getTotal(), total, y);

            //Print the footer.
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.drawString(footer, 150, y + 50);

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

        btnClose = new javax.swing.JButton();
        lblSaleID = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();
        lblCustomer = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableItems = new javax.swing.JTable();
        lblTerminal = new javax.swing.JLabel();
        btnEmail = new javax.swing.JButton();
        lblTotal = new javax.swing.JLabel();
        btnPrint = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        lblSaleID.setText("Sale ID: ");

        lblTime.setText("Time: ");

        lblCustomer.setText("Customer: NO CUSTOMER");

        tableItems.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Qty.", "Item", "Price"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(tableItems);

        lblTerminal.setText("Terminal:");

        btnEmail.setText("Email Receipt");
        btnEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEmailActionPerformed(evt);
            }
        });

        lblTotal.setText("Sale Total: ");

        btnPrint.setText("Print Receipt");
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(lblSaleID)
                                .addComponent(lblTime)
                                .addComponent(lblCustomer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblTerminal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(lblTotal))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 110, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnPrint)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEmail)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblSaleID)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTime)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCustomer)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTerminal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotal)
                .addGap(193, 193, 193))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnEmail)
                    .addComponent(btnPrint))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEmailActionPerformed
        String email;
        if (sale.getCustomer() != null) {
            email = sale.getCustomer().getEmail();
        } else {
            email = JOptionPane.showInputDialog(this, "Enter email address", "Email Receipt", JOptionPane.PLAIN_MESSAGE);
        }
        if (email.equals("")) {
            return;
        }
        final ModalDialog mDialog = new ModalDialog("Email...", "Sending email...");
        Runnable run = new Runnable() {
            @Override
            public void run() {
                try {
                    dc.emailReceipt(email, sale);
                    mDialog.hide();
                    JOptionPane.showMessageDialog(SaleDialog.this, "Email sent", "Email Receipt", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException | MessagingException ex) {
                    mDialog.hide();
                    JOptionPane.showMessageDialog(SaleDialog.this, "Error sending email", "Email Receipt", JOptionPane.ERROR_MESSAGE);
                } finally {
                    mDialog.hide();
                }
            }
        };
        Thread th = new Thread(run);
        th.start();
        mDialog.show();
    }//GEN-LAST:event_btnEmailActionPerformed

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        ReceiptPrinter prt = new ReceiptPrinter(sale);
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(prt);
        boolean ok = job.printDialog();
        final ModalDialog mDialog = new ModalDialog("Printing...", "Printing...", job);
        if (ok) {
            Runnable print = new Runnable() {
                @Override
                public void run() {
                    try {
                        job.print();
                    } catch (PrinterAbortException ex) {
                        mDialog.setText("Print aborted");
                    } catch (PrinterException ex) {
                        Logger.getLogger(SaleDialog.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        mDialog.hide();
                    }
                }
            };

            Thread th = new Thread(print);
            th.start();
            mDialog.show();
            if (job.isCancelled()) {
                JOptionPane.showMessageDialog(this, "Printing cancelled", "Print", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Printing complete", "Print", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnPrintActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnEmail;
    private javax.swing.JButton btnPrint;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCustomer;
    private javax.swing.JLabel lblSaleID;
    private javax.swing.JLabel lblTerminal;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JTable tableItems;
    // End of variables declaration//GEN-END:variables
}
