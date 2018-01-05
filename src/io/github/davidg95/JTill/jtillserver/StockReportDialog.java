/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

/**
 *
 * @author David
 */
public class StockReportDialog extends javax.swing.JDialog {

    private final DataConnect dc;

    private static StockReportDialog dialog;

    public static final int FULL = 0;
    public static final int MINIMUM = 1;
    public static final int ZERO = 2;

    /**
     * Creates new form StockReportDialog
     */
    public StockReportDialog(Window parent) {
        super(parent);
        dc = GUI.gui.dc;
        initComponents();
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        btnGenerate.requestFocus();
        Utilities.installEscapeCloseOperation(this);
    }

    public static void showDialog(Component parent) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        dialog = new StockReportDialog(window);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    private class StockPrintable implements Printable {

        private final List<Product> products;
        private List<Department> departments;
        private final String info;

        private final int x = 70;

        private int max_per_page = 20;

        public StockPrintable(List<Product> products, String info) {
            this.products = products;
            try {
                this.departments = dc.getAllDepartments();
            } catch (IOException | SQLException ex) {
                Logger.getLogger(StockReportDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.info = info;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex * max_per_page >= products.size()) {
                return NO_SUCH_PAGE;
            }
            Graphics2D g = (Graphics2D) graphics;
            g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            Font font = g.getFont();

            final int width = (int) (pageFormat.getWidth() - x - x);

            int y = 60;
            FontMetrics metrics = graphics.getFontMetrics(font);
            final int lineSpace = metrics.getHeight() + 5;

            g.setFont(new Font("Arial", Font.BOLD, 20)); //Use a differnt font for the header.

            g.drawString("Stock Report", x, y);
            g.setFont(font);
            String page = "Page " + (pageIndex + 1);
            g.drawString(page, (int) (pageFormat.getWidth() / 2) - (g.getFontMetrics(font).stringWidth(page) / 2), (int) pageFormat.getHeight() - 20);
            y += lineSpace;
            g.drawString(info, x, y);

            y += lineSpace;

            final int idCol = x + 10;
            final int nCol = x + 40;
            final int sCol = x + width - 50;

            final int topY = y;
            g.setColor(Color.lightGray);
            g.fillRect(x, y, width, lineSpace);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, width, lineSpace);

            g.drawString("ID", idCol, y + lineSpace - 5);
            g.drawString("Name", nCol, y + lineSpace - 5);
            g.drawString("In Stock", sCol, y + lineSpace - 5);

            y += lineSpace;

            max_per_page = (int) Math.floor((pageFormat.getHeight() - y - 40) / lineSpace);

//            for (int i = ci; i < departments.size(); i++) {
//                Department d = departments.get(i);
//                g.setColor(Color.LIGHT_GRAY);
//                g.fillRect(x, y, width, lineSpace);
//                g.setColor(Color.BLACK);
//                g.drawRect(x, y, width, lineSpace);
//                g.drawString(d.getName(), nCol, y + 15);
//                y += lineSpace;
            for (int i = max_per_page * pageIndex; i < products.size() && i < max_per_page * (pageIndex + 1); i++) {
                Product p = products.get(i);
                y += lineSpace;
                g.drawString(p.getId() + "", idCol, y);
                g.drawString(p.getLongName(), nCol, y);
                g.drawString(p.getStock() + "", sCol, y);
            }
            y += lineSpace / 2;
//            }

            g.drawRect(x, topY, width, y - topY);
            g.drawLine(nCol - 5, topY, nCol - 5, y);
            g.drawLine(sCol - 5, topY, sCol - 5, y);
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

        jLabel1 = new javax.swing.JLabel();
        cmbType = new javax.swing.JComboBox<>();
        btnGenerate = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Stock Report");
        setResizable(false);

        jLabel1.setText("Report Type:");

        cmbType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Full report", "Stock below minimum", "Stock below zero" }));

        btnGenerate.setText("Generate");
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
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnGenerate)
                    .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnGenerate)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void print(List<Product> products, String info, ModalDialog mDialog, PrinterJob job) {
        job.setPrintable(new StockPrintable(products, info));
        boolean ok = job.printDialog();
        if (ok) {
            try {
                job.print();
                mDialog.hide();
                JOptionPane.showMessageDialog(GUI.gui, "Printing complete", "Print", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                mDialog.hide();
                Logger.getLogger(StockReportDialog.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(GUI.gui, ex, "Error", JOptionPane.INFORMATION_MESSAGE);
            } finally {
                mDialog.hide();
            }
        } else {
            mDialog.hide();
        }
    }
    private void btnGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateActionPerformed
        final PrinterJob job = PrinterJob.getPrinterJob();
        final int type = cmbType.getSelectedIndex();
        final ModalDialog mDialog = new ModalDialog(GUI.gui, "Stock Report", "Generating...", job);
        final Runnable run = new Runnable() {
            @Override
            public void run() {
                if (type == FULL) {
                    try {
                        List<Product> products = dc.getAllProducts();
                        Iterator it = products.iterator();
                        while (it.hasNext()) {
                            Product p = (Product) it.next();
                            if (p.isOpen() || !p.isTrackStock()) {
                                it.remove();
                            }
                        }
                        if (products.isEmpty()) {
                            mDialog.hide();
                            JOptionPane.showMessageDialog(StockReportDialog.this, "No items in the report", "Stock Report", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                        print(products, "Full report", mDialog, job);
                    } catch (IOException | SQLException ex) {
                        JOptionPane.showMessageDialog(GUI.gui, ex, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (type == MINIMUM) {
                    try {
                        List<Product> products = dc.getAllProducts();
                        Iterator it = products.iterator();
                        while (it.hasNext()) {
                            Product p = (Product) it.next();
                            if (p.getStock() >= p.getMinStockLevel() || p.isOpen() || !p.isTrackStock()) {
                                it.remove();
                            }
                        }
                        if (products.isEmpty()) {
                            mDialog.hide();
                            JOptionPane.showMessageDialog(StockReportDialog.this, "No items in the report", "Stock Report", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                        print(products, "Below minimum report", mDialog, job);
                    } catch (IOException | SQLException ex) {
                        JOptionPane.showMessageDialog(GUI.gui, ex, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (type == ZERO) {
                    try {
                        List<Product> products = dc.getAllProducts();
                        Iterator it = products.iterator();
                        while (it.hasNext()) {
                            Product p = (Product) it.next();
                            if (p.getStock() > 0 || p.isOpen() || !p.isTrackStock()) {
                                it.remove();
                            }
                        }
                        if (products.isEmpty()) {
                            mDialog.hide();
                            JOptionPane.showMessageDialog(StockReportDialog.this, "No items in the report", "Stock Report", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                        print(products, "Beloew zero report", mDialog, job);
                    } catch (IOException | SQLException ex) {
                        JOptionPane.showMessageDialog(GUI.gui, ex, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        final Thread thread = new Thread(run, "StockReport");
        thread.start();
        setVisible(false);
        mDialog.show();
    }//GEN-LAST:event_btnGenerateActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGenerate;
    private javax.swing.JComboBox<String> cmbType;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
