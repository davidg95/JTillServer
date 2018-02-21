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
import java.text.DecimalFormat;
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

    private final String companyDetails = System.getenv("APPDATA") + "\\JTill Server\\company.details";

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
        try {
            if (!Boolean.getBoolean(dc.getSetting("USE_EMAIL"))) {
                btnEmail.setEnabled(false);
            }
        } catch (IOException ex) {
            Logger.getLogger(SaleDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        tableItems.setSelectionModel(new ForcedListSelectionModel());
        txtId.setText(sale.getId() + "");
        txtTime.setText(sale.getDate().toString());
        if (sale.getCustomer() != null) {
            txtCustomer.setText(sale.getCustomer().getName());
        } else {
            txtCustomer.setText("N/A");
        }
        txtTill.setText(sale.getTill().getName());
        txtStaff.setText(sale.getStaff().getName());
        lblTotal.setText("Total: £" + sale.getTotal().setScale(2, 6));

        model.setRowCount(0);

        BigDecimal taxValue = BigDecimal.ZERO;
        for (SaleItem item : sale.getSaleItems()) {
            taxValue = taxValue.add(item.getTaxValue());
            DecimalFormat df;
            if (item.getPrice().compareTo(BigDecimal.ZERO) > 1) {
                df = new DecimalFormat("#.00");
            } else {
                df = new DecimalFormat("0.00");
            }
            Object[] s = null;
            final Product p = (Product) item.getProduct();
            s = new Object[]{p.getShortName(), item.getQuantity(), "£" + df.format(item.getPrice().doubleValue() * item.getQuantity())};
            model.addRow(s);
        }
        lblTax.setText("Tax: £" + new DecimalFormat("0.00").format(taxValue.doubleValue()));
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

            if (header == null || header.isEmpty()) {
                header = "JTill Receipt";
            }
            if (footer == null || footer.isEmpty()) {
                footer = "Thank-you";
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
            final Staff staff = toPrint.getStaff();
            g2.drawString("Served by " + staff.getName(), 70, 180);

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
                final Product p = (Product) it.getProduct();
                g2.drawString(p.getShortName(), item, y);
                g2.drawString("" + it.getQuantity(), quantity, y);
                g2.drawString("£" + it.getPrice().setScale(2), total, y);
                y += 30;
            }
            g2.drawLine(item - 30, y - 20, total + 100, y - 20);
            g2.drawString("Total: £" + toPrint.getTotal().setScale(2, 6), total, y);

            //Print the footer.
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.drawString(footer, 150, y + 50);

            return PAGE_EXISTS;
        }

        private Image loadImage() {
            InputStream in;
            final Properties properties = new Properties();
            try {
                in = new FileInputStream(companyDetails);
                properties.load(in);

                final String logoURL = properties.getProperty("LOGO");
                if (logoURL == null || logoURL.isEmpty()) {
                    throw new FileNotFoundException("File Not Set");
                }
                final File file = new File(logoURL);

                final Image image = ImageIO.read(file);
                in.close();

                return image;
            } catch (FileNotFoundException | UnknownHostException ex) {
                OutputStream out;
                try {
                    out = new FileOutputStream(companyDetails);
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
        btnPrint = new javax.swing.JButton();
        lblStaff = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        txtTime = new javax.swing.JTextField();
        txtCustomer = new javax.swing.JTextField();
        txtTill = new javax.swing.JTextField();
        txtStaff = new javax.swing.JTextField();
        lblTotal = new javax.swing.JLabel();
        lblTax = new javax.swing.JLabel();

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        lblSaleID.setText("Sale ID: ");

        lblTime.setText("Time: ");

        lblCustomer.setText("Customer:");

        tableItems.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Item", "Qty.", "Price"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableItems.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(tableItems);
        if (tableItems.getColumnModel().getColumnCount() > 0) {
            tableItems.getColumnModel().getColumn(0).setResizable(false);
            tableItems.getColumnModel().getColumn(1).setMinWidth(40);
            tableItems.getColumnModel().getColumn(1).setMaxWidth(40);
            tableItems.getColumnModel().getColumn(2).setMinWidth(100);
            tableItems.getColumnModel().getColumn(2).setMaxWidth(100);
        }

        lblTerminal.setText("Terminal:");

        btnEmail.setText("Email Receipt");
        btnEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEmailActionPerformed(evt);
            }
        });

        btnPrint.setText("Print Receipt");
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });

        lblStaff.setText("Staff: ");

        txtId.setEditable(false);

        txtTime.setEditable(false);

        txtCustomer.setEditable(false);

        txtTill.setEditable(false);

        txtStaff.setEditable(false);

        lblTotal.setText("Total: £0.00");

        lblTax.setText("Tax: £0.00");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnPrint)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEmail)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 311, Short.MAX_VALUE)
                        .addComponent(btnClose))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lblTime)
                                    .addComponent(lblSaleID)
                                    .addComponent(lblTerminal)
                                    .addComponent(lblStaff)))
                            .addComponent(lblCustomer))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtCustomer, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                                .addComponent(txtTill)
                                .addComponent(txtStaff))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(txtId, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                                .addComponent(txtTime)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblTotal)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblTax))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSaleID)
                            .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTime)
                            .addComponent(txtTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCustomer))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTerminal)
                            .addComponent(txtTill, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblStaff)
                            .addComponent(txtStaff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 137, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTax)
                    .addComponent(lblTotal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnEmail)
                    .addComponent(btnPrint))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEmailActionPerformed
        String email;
        if (sale.getCustomer() != null) {
            final Customer c = sale.getCustomer();
            email = c.getEmail();
            if (email == null) {
                email = JOptionPane.showInputDialog(this, "Enter email address", "Email Receipt", JOptionPane.PLAIN_MESSAGE);
            }

        } else {
            email = JOptionPane.showInputDialog(this, "Enter email address", "Email Receipt", JOptionPane.PLAIN_MESSAGE);
        }
        if (email == null) {
            return;
        }
        final ModalDialog mDialog = new ModalDialog(this, "Email...");
        final String fEmail = email;
        final Runnable run = () -> {
            try {
                boolean result = dc.emailReceipt(fEmail, sale);
                mDialog.hide();
                if (result) {
                    JOptionPane.showMessageDialog(this, "Email sent", "Email Receipt", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Email not sent", "Email Receipt", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException | MessagingException ex) {
                mDialog.hide();
                JOptionPane.showMessageDialog(this, "Error sending email", "Email Receipt", JOptionPane.ERROR_MESSAGE);
            } finally {
                mDialog.hide();
            }
        };
        final Thread th = new Thread(run);
        th.start();
        mDialog.show();
    }//GEN-LAST:event_btnEmailActionPerformed

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        ReceiptPrinter prt = new ReceiptPrinter(sale);
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(prt);
        boolean ok = job.printDialog();
        final ModalDialog mDialog = new ModalDialog(this, "Printing...", job);
        if (ok) {
            Runnable print = new Runnable() {
                @Override
                public void run() {
                    try {
                        job.print();
                    } catch (PrinterAbortException ex) {
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
    private javax.swing.JLabel lblTax;
    private javax.swing.JLabel lblTerminal;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JTable tableItems;
    private javax.swing.JTextField txtCustomer;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtStaff;
    private javax.swing.JTextField txtTill;
    private javax.swing.JTextField txtTime;
    // End of variables declaration//GEN-END:variables
}
