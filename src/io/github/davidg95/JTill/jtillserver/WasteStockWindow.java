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
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable.PrintMode;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class WasteStockWindow extends javax.swing.JFrame {

    private static WasteStockWindow window;

    private final DataConnect dc;
    private WasteReport report;
    private final List<WasteItem> wasteItems;
    private final DefaultTableModel model;
    private final DefaultComboBoxModel cmbModel;
    private Date date;

    /**
     * Creates new form WasteStockWindow
     */
    public WasteStockWindow(DataConnect dc, Image icon) {
        this.dc = dc;
        wasteItems = new ArrayList<>();
        initComponents();
        btnPrint.setEnabled(false);
        setTitle("Waste Stock");
        setIconImage(icon);
        date = new Date();
        lblTime.setText("Time: " + date);
        model = (DefaultTableModel) tblProducts.getModel();
        tblProducts.setModel(model);
        model.setRowCount(0);
        cmbModel = (DefaultComboBoxModel) cmbReason.getModel();
        cmbReason.setModel(cmbModel);
        init();
    }

    public WasteStockWindow(DataConnect dc, WasteReport wr, Image icon) {
        this.report = wr;
        this.dc = dc;
        wasteItems = new ArrayList<>();
        initComponents();
        btnAddProduct.setEnabled(false);
        btnWaste.setEnabled(false);
        cmbReason.setEnabled(false);
        lblReason.setEnabled(false);
        btnCSV.setEnabled(false);
        btnDate.setEnabled(false);
        lblTime.setText("Time: " + wr.getDate());
        setTitle("Waste Report " + report.getId());
        setIconImage(icon);
        model = (DefaultTableModel) tblProducts.getModel();
        tblProducts.setModel(model);
        lblValue.setText("Total Value: £" + wr.getTotalValue());
        setTable();
        cmbModel = (DefaultComboBoxModel) cmbReason.getModel();
        cmbReason.setModel(cmbModel);
        init();
    }

    public static void showWindow(DataConnect dc, Image icon) {
        window = new WasteStockWindow(dc, icon);
        window.setVisible(true);
    }

    public static void showWindow(DataConnect dc, WasteReport wr, Image icon) {
        window = new WasteStockWindow(dc, wr, icon);
        window.setVisible(true);
    }

    private void init() {
        try {
            List<WasteReason> reasons = dc.getAllWasteReasons();
            for (WasteReason r : reasons) {
                cmbModel.addElement(r);
            }
        } catch (IOException | SQLException ex) {
            Logger.getLogger(WasteStockWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setTable() {
        String symbol = "";
        try {
            symbol = dc.getSetting("CURRENCY_SYMBOL");
        } catch (IOException ex) {
            Logger.getLogger(WasteStockWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        model.setRowCount(0);
        for (WasteItem wi : report.getItems()) {
            Object[] row = new Object[]{wi.getId(), wi.getProduct().getLongName(), wi.getQuantity(), symbol + wi.getProduct().getPrice().multiply(new BigDecimal(wi.getQuantity())), wi.getReason()};
            model.addRow(row);
        }
    }

    private class WasteReportPrintout implements Printable {

        private final WasteReport wr;

        public WasteReportPrintout(WasteReport wr) {
            this.wr = wr;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            String header = "Waste Report";

            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            Font oldFont = graphics.getFont();

            g2.setFont(new Font("Arial", Font.BOLD, 20)); //Use a differnt font for the header.
            g2.drawString(header, 70, 60);
            g2.setFont(oldFont); //Chagne back to the old font.

            //Print sale info.
            g2.drawString("Time: " + wr.getDate(), 70, 110);

            final int item = 100;
            final int quantity = 200;
            final int total = 300;
            final int reason = 400;
            int y = 130;

            //Print collumn headers.
            g2.drawString("Item", item, y);
            g2.drawString("Quantity", quantity, y);
            g2.drawString("Total", total, y);
            g2.drawString("Reason", reason, y);
            g2.drawLine(item - 30, y + 10, reason + 100, y + 10);

            y += 30;

            //Print the sale items.
            for (WasteItem wi : wr.getItems()) {
                g2.drawString(wi.getName(), item, y);
                g2.drawString("" + wi.getQuantity(), quantity, y);
                g2.drawString("£" + wi.getProduct().getPrice().multiply(new BigDecimal(wi.getQuantity())), total, y);
                g2.drawString(wi.getReason().getName(), reason, y);
                y += 30;
            }
            g2.drawLine(item - 30, y - 20, reason + 100, y - 20);
            g2.drawString("Total: £" + wr.getTotalValue().setScale(2), total, y);

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

        jScrollPane1 = new javax.swing.JScrollPane();
        tblProducts = new javax.swing.JTable();
        btnClose = new javax.swing.JButton();
        btnWaste = new javax.swing.JButton();
        btnAddProduct = new javax.swing.JButton();
        lblTime = new javax.swing.JLabel();
        lblReason = new javax.swing.JLabel();
        lblValue = new javax.swing.JLabel();
        cmbReason = new javax.swing.JComboBox<>();
        btnCSV = new javax.swing.JButton();
        btnDate = new javax.swing.JButton();
        btnPrint = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tblProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Product", "Quantity", "Total Value", "Reason"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblProducts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblProductsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblProducts);
        if (tblProducts.getColumnModel().getColumnCount() > 0) {
            tblProducts.getColumnModel().getColumn(0).setResizable(false);
            tblProducts.getColumnModel().getColumn(1).setResizable(false);
            tblProducts.getColumnModel().getColumn(2).setResizable(false);
            tblProducts.getColumnModel().getColumn(3).setResizable(false);
            tblProducts.getColumnModel().getColumn(4).setResizable(false);
        }

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnWaste.setText("Waste");
        btnWaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWasteActionPerformed(evt);
            }
        });

        btnAddProduct.setText("Add Product");
        btnAddProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddProductActionPerformed(evt);
            }
        });

        lblReason.setText("Reason:");

        lblValue.setText("Total Value: £0.00");

        btnCSV.setText("Add CSV File");
        btnCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCSVActionPerformed(evt);
            }
        });

        btnDate.setText("Change Date");
        btnDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDateActionPerformed(evt);
            }
        });

        btnPrint.setText("Print");
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
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lblValue)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnPrint)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCSV)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddProduct)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnWaste)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lblTime, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblReason)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbReason, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblReason)
                        .addComponent(cmbReason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnDate))
                    .addComponent(lblTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnWaste)
                    .addComponent(btnAddProduct)
                    .addComponent(lblValue)
                    .addComponent(btnCSV)
                    .addComponent(btnPrint))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnAddProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProductActionPerformed
        Product product = ProductSelectDialog.showDialog(this, dc);

        if (product == null) {
            return;
        }

        product = (Product) product.clone();

        String amount = JOptionPane.showInputDialog(this, "Enter amount to waste", "Waste", JOptionPane.INFORMATION_MESSAGE);

        if (amount == null || amount.equals("0") || amount.equals("")) {
            return;
        }

        if (product.getStock() - Integer.parseInt(amount) < 0) {
            if (JOptionPane.showConfirmDialog(this, "Item does not have that much in stock. Continue?", "Waste", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
        }
        int reason = cmbReason.getSelectedIndex() + 1;
        WasteReason wr = null;
        try {
            wr = dc.getWasteReason(reason);
            WasteItem wi = new WasteItem(product, Integer.parseInt(amount), wr);
            wasteItems.add(wi);
            BigDecimal val = BigDecimal.ZERO;
            for (WasteItem w : wasteItems) {
                val = val.add(w.getProduct().getPrice().setScale(2).multiply(new BigDecimal(w.getQuantity())));
            }
            lblValue.setText("Total Value: £" + val);
            model.addRow(new Object[]{wi.getProduct().getId(), wi.getProduct().getLongName(), Integer.parseInt(amount), product.getPrice().multiply(new BigDecimal(wi.getQuantity())), wi.getReason()});
        } catch (IOException | SQLException | JTillException ex) {
            Logger.getLogger(WasteStockWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddProductActionPerformed

    private void btnWasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWasteActionPerformed
        if (wasteItems.isEmpty()) {
            return;
        }
        WasteReport wr = new WasteReport(date);
        BigDecimal total = BigDecimal.ZERO;
        for (WasteItem wi : wasteItems) {
            try {
                Product product = dc.getProduct(wi.getProduct().getId());
                product.removeStock(wi.getQuantity());
                dc.updateProduct(product);
                total = total.add(product.getPrice().multiply(new BigDecimal(wi.getQuantity())));
            } catch (IOException | ProductNotFoundException | SQLException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        total.setScale(2);
        wr.setTotalValue(total);
        wr.setItems(wasteItems);
        try {
            dc.addWasteReport(wr);
            JOptionPane.showMessageDialog(this, "All items have been wasted", "Waste", JOptionPane.INFORMATION_MESSAGE);
            if (JOptionPane.showConfirmDialog(this, "Do you want to print this report?", "Print", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setPrintable(new WasteReportPrintout(wr));
                boolean ok = job.printDialog();
                final ModalDialog mDialog = new ModalDialog(this, "Printing...", "Printing report...", job);
                if (ok) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                job.print();
                            } catch (PrinterException ex) {
                                mDialog.hide();
                                JOptionPane.showMessageDialog(WasteStockWindow.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                            } finally {
                                mDialog.hide();
                            }
                        }
                    };
                    Thread th = new Thread(runnable);
                    th.start();
                    mDialog.show();
                }
            }
            model.setRowCount(0);
            wasteItems.clear();
            lblValue.setText("Total: £0.00");
            this.setVisible(false);
        } catch (IOException | SQLException | JTillException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnWasteActionPerformed

    private void tblProductsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblProductsMouseClicked
        if (evt.getClickCount() == 2) {
            if (!wasteItems.isEmpty()) {
                int row = tblProducts.getSelectedRow();
                WasteItem wi = wasteItems.get(row);
                if (JOptionPane.showConfirmDialog(this, "\"Are you sure you want to remove this line?\n" + wi.getProduct().getLongName(), "Remove", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    wasteItems.remove(row);
                    model.removeRow(row);
                }
            }
        }

        if (tblProducts.getSelectedRow() == -1) {
            return;
        }
        if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem item = new JMenuItem("Remove");
            item.addActionListener((ActionEvent e) -> {
                if (JOptionPane.showConfirmDialog(this, "Remove this item?", "Remove Item", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    wasteItems.remove(tblProducts.getSelectedRow());
                    BigDecimal val = BigDecimal.ZERO;
                    val.setScale(2);
                    for (WasteItem w : wasteItems) {
                        val = val.add(w.getProduct().getPrice().multiply(new BigDecimal(w.getQuantity())));
                    }
                    lblValue.setText("Total Value: £" + val);
                    model.removeRow(tblProducts.getSelectedRow());
                }
            });
            menu.add(item);
            menu.show(tblProducts, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tblProductsMouseClicked

    private void btnCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCSVActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Waste File");
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                boolean errors = false;
                while (true) {
                    try {
                        String line = br.readLine();

                        if (line == null) {
                            break;
                        }

                        String[] item = line.split(",");

                        if (item.length != 3) {
                            JOptionPane.showMessageDialog(this, "File is not recognised", "Add CSV", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        Product product = dc.getProductByBarcode(item[0]);
                        int amount = Integer.parseInt(item[1]);
                        int reason = Integer.parseInt(item[2]);

                        WasteReason wr;

                        try {
                            wr = dc.getWasteReason(reason);
                        } catch (JTillException ex) {
                            wr = dc.getWasteReason(0);
                        }
                        WasteItem wi = new WasteItem(product, amount, wr);
                        wasteItems.add(wi);
                        BigDecimal val = BigDecimal.ZERO;
                        val.setScale(2);
                        for (WasteItem w : wasteItems) {
                            val = val.add(w.getProduct().getPrice().multiply(new BigDecimal(w.getQuantity())));
                        }
                        lblValue.setText("Total Value: £" + val);
                        model.addRow(new Object[]{product.getId(), product.getName(), product.getPlu().getCode(), amount, wi.getReason()});
                    } catch (ProductNotFoundException ex) {
                        errors = true;
                    }
                }
                if (errors) {
                    JOptionPane.showMessageDialog(this, "Not all products could be found", "Waste", JOptionPane.ERROR_MESSAGE);
                }
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "The file could not be found", "Open File", JOptionPane.ERROR_MESSAGE);
            } catch (IOException | SQLException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            } catch (JTillException ex) {
                Logger.getGlobal().log(Level.WARNING, null, ex);
            }
        }
    }//GEN-LAST:event_btnCSVActionPerformed

    private void btnDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDateActionPerformed
        Date d = DateSelectDialog.showDialog(this);
        if (d != null) {
            date = d;
            lblTime.setText("Time: " + date);
        }
    }//GEN-LAST:event_btnDateActionPerformed

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new WasteReportPrintout(report));
        boolean ok = job.printDialog();
        final ModalDialog mDialog = new ModalDialog(this, "Printing...", "Printing report...", job);
        if (ok) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        job.print();
                    } catch (PrinterException ex) {
                        mDialog.hide();
                        JOptionPane.showMessageDialog(WasteStockWindow.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        mDialog.hide();
                    }
                }
            };
            Thread th = new Thread(runnable);
            th.start();
            mDialog.show();
            JOptionPane.showMessageDialog(this, "Printing complete", "Print", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btnPrintActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnCSV;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnDate;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnWaste;
    private javax.swing.JComboBox<String> cmbReason;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblReason;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblValue;
    private javax.swing.JTable tblProducts;
    // End of variables declaration//GEN-END:variables
}
