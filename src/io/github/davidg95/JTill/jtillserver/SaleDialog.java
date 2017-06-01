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
import java.awt.Image;
import java.awt.Window;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterAbortException;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class SaleDialog extends javax.swing.JInternalFrame {

    private final Sale sale;
    private final DataConnect dc;

    private final DefaultTableModel model;

    /**
     * Creates new form SaleDialog
     */
    public SaleDialog(Sale sale) {
        super();
        this.sale = sale;
        this.dc = GUI.gui.dc;
        initComponents();
        super.setClosable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        model = (DefaultTableModel) tableItems.getModel();
        setTitle("Sale " + sale.getId());
        init();
    }

    public static void showSaleDialog(Sale sale) {
        SaleDialog dialog = new SaleDialog(sale);
        GUI.gui.internal.add(dialog);
        dialog.setVisible(true);
        try {
            dialog.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(SaleDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void init() {
        lblSaleID.setText("Sale ID: " + sale.getId());
        Calendar c = Calendar.getInstance();
        c.setTime(sale.getDate());
        lblTime.setText("Time: " + c.getTime().toString());
        if (sale.getCustomer() != 0) {
            try {
                final Customer cus = dc.getCustomer(sale.getCustomer());
                lblCustomer.setText("Customer: " + cus.getName());
            } catch (IOException | CustomerNotFoundException | SQLException ex) {
                Logger.getLogger(SaleDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            final Till till = dc.getTill(sale.getTerminal());
            lblTerminal.setText("Terminal: " + till.getName());
        } catch (IOException | SQLException | JTillException ex) {
            lblTerminal.setText("Terminal: " + sale.getTerminal());
        }
        try {
            final Staff staff = dc.getStaff(sale.getStaff());
            lblStaff.setText("Staff: " + staff);
        } catch (IOException | StaffNotFoundException | SQLException ex) {
            Logger.getLogger(SaleDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        lblTotal.setText("Sale Total: £" + sale.getTotal().setScale(2));

        model.setRowCount(0);

        for (SaleItem item : sale.getSaleItems()) {
            try {
                DecimalFormat df;
                if (item.getPrice().compareTo(BigDecimal.ZERO) > 1) {
                    df = new DecimalFormat("#.00");
                } else {
                    df = new DecimalFormat("0.00");
                }
                Object[] s;
                if (item.getType() == SaleItem.PRODUCT) {
                    final Product p = dc.getProduct(item.getItem());
                    s = new Object[]{item.getQuantity(), p.getName(), df.format(item.getPrice().doubleValue() * item.getQuantity())};
                } else {
                    final Discount d = dc.getDiscount(item.getItem());
                    s = new Object[]{item.getQuantity(), d.getName(), df.format(item.getPrice().doubleValue() * item.getQuantity())};
                }
                model.addRow(s);
            } catch (IOException | ProductNotFoundException | SQLException | DiscountNotFoundException ex) {
                Logger.getLogger(SaleDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            Image img = loadImage();
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
            //g2.drawString(header, 70, 60);
            if (img == null) {
                g2.drawString(header, 70, 30);
            } else {
                g2.drawImage(img, 70, 30, 400, 100, null);
            }
            g2.setFont(oldFont); //Chagne back to the old font.

            //Print sale info.
            g2.drawString("Receipt for sale: " + toPrint.getId(), 70, 140);
            g2.drawString("Time: " + toPrint.getDate(), 70, 160);
            g2.drawString("Served by " + toPrint.getStaff(), 70, 180);

            final int item = 100;
            final int quantity = 300;
            final int total = 420;
            int y = 220;

            //Print collumn headers.
            g2.drawString("Item", item, y);
            g2.drawString("Quantity", quantity, y);
            g2.drawString("Total", total, y);
            g2.drawLine(item - 30, y + 10, total + 100, y + 10);

            y += 30;

            //Print the sale items.
            for (SaleItem it : toPrint.getSaleItems()) {
                try {
                    if (it.getType() == SaleItem.PRODUCT) {
                        final Product p = dc.getProduct(it.getItem());
                        g2.drawString(p.getName(), item, y);
                    } else {
                        final Discount d = dc.getDiscount(it.getItem());
                        g2.drawString(d.getName(), item, y);
                    }
                    g2.drawString("" + it.getQuantity(), quantity, y);
                    g2.drawString("£" + it.getPrice(), total, y);
                    y += 30;
                } catch (IOException | ProductNotFoundException | SQLException | DiscountNotFoundException ex) {
                    Logger.getLogger(SaleDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            g2.drawLine(item - 30, y - 20, total + 100, y - 20);
            g2.drawString("Total: £" + toPrint.getTotal(), total, y);

            //Print the footer.
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.drawString(footer, 150, y + 50);

            return PAGE_EXISTS;
        }

        private Image loadImage() {
            InputStream in;
            Properties properties = new Properties();
            try {
                in = new FileInputStream("company.details");
                properties.load(in);

                String logoURL = properties.getProperty("LOGO");
                File file = new File(logoURL);

                Image image = ImageIO.read(file);
                in.close();

                return image;
            } catch (FileNotFoundException | UnknownHostException ex) {
                OutputStream out;
                try {
                    out = new FileOutputStream("company.details");
                    properties.store(out, null);
                    out.close();
                } catch (FileNotFoundException | UnknownHostException e) {
                } catch (IOException e) {
                }
            } catch (IOException ex) {
            }
            return null;
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
        lblStaff = new javax.swing.JLabel();

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

        lblStaff.setText("Staff: ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblSaleID)
                            .addComponent(lblTime)
                            .addComponent(lblCustomer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTerminal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTotal)
                            .addComponent(lblStaff, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblSaleID)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTime)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCustomer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTerminal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblStaff)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTotal)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnEmail)
                    .addComponent(btnPrint))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEmailActionPerformed
        String email;
        try {
            if (sale.getCustomer() != 1) {
                final Customer c = dc.getCustomer(sale.getCustomer());
                email = c.getEmail();
                if (email == null) {
                    email = JOptionPane.showInternalInputDialog(GUI.gui.internal, "Enter email address", "Email Receipt", JOptionPane.PLAIN_MESSAGE);
                }

            } else {
                email = JOptionPane.showInternalInputDialog(GUI.gui.internal, "Enter email address", "Email Receipt", JOptionPane.PLAIN_MESSAGE);
            }
            if (email == null) {
                return;
            }
            final ModalDialog mDialog = new ModalDialog(this, "Email...", "Sending email...");
            final String fEmail = email;
            final Runnable run = () -> {
                try {
                    boolean result = dc.emailReceipt(fEmail, sale);
                    mDialog.hide();
                    if (result) {
                        JOptionPane.showInternalMessageDialog(GUI.gui.internal, "Email sent", "Email Receipt", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showInternalMessageDialog(GUI.gui.internal, "Email not sent", "Email Receipt", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException | MessagingException ex) {
                    mDialog.hide();
                    JOptionPane.showInternalMessageDialog(GUI.gui.internal, "Error sending email", "Email Receipt", JOptionPane.ERROR_MESSAGE);
                } finally {
                    mDialog.hide();
                }
            };
            final Thread th = new Thread(run);
            th.start();
            mDialog.show();
        } catch (IOException | CustomerNotFoundException | SQLException ex) {
            Logger.getLogger(SaleDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnEmailActionPerformed

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        ReceiptPrinter prt = new ReceiptPrinter(sale);
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(prt);
        boolean ok = job.printDialog();
        final ModalDialog mDialog = new ModalDialog(this, "Printing...", "Printing...", job);
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
    private javax.swing.JLabel lblStaff;
    private javax.swing.JLabel lblTerminal;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JTable tableItems;
    // End of variables declaration//GEN-END:variables
}
