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
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author David
 */
public class WasteReports extends javax.swing.JDialog {

    private final Logger log = Logger.getGlobal();

    private final DataConnect dc;

    private static final int ALL = 0;
    private static final int CONTAINING = 1;
    private static final int DEPARTMENT = 2;
    private static final int CATEGORY = 3;
    private static final int REASON = 4;

    /**
     * Creates new form WasteReports
     */
    public WasteReports(Window parent) {
        this.dc = GUI.gui.dc;
        initComponents();
        dateStart.setDate(new Date(0));
        dateEnd.setDate(new Date());
        setIconImage(GUI.icon);
        setLocationRelativeTo(parent);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        btnSearch.requestFocus();
    }

    public static void showWindow(Component parent) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        WasteReports wr = new WasteReports(window);
        wr.setVisible(true);
    }

    private void print(Date start, Date end, List<WasteItem> items) throws IOException, SQLException {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new WastePrinter(start, end, items));
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
                        JOptionPane.showMessageDialog(WasteReports.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        mDialog.hide();
                    }
                }
            };
            Thread th = new Thread(runnable);
            th.start();
            mDialog.show();
            JOptionPane.showMessageDialog(this, "Printing complete", "Print", JOptionPane.INFORMATION_MESSAGE);
            setVisible(false);
        }
    }

    private class WastePrinter implements Printable {

        private final Date start;
        private final Date end;
        private final List<WasteItem> items;

        private final List<Department> departments;
        private final List<Category> categorys;

        public WastePrinter(Date start, Date end, List<WasteItem> items) throws IOException, SQLException {
            this.start = start;
            this.end = end;
            this.items = items;
            departments = dc.getAllDepartments();
            categorys = dc.getAllCategorys();
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }
            final int x = 70;

            final int width = (int) (pageFormat.getWidth() - x - x);

            Font font = graphics.getFont();

            int y = 60;

            Graphics2D g = (Graphics2D) graphics;

            g.setFont(new Font("Arial", Font.BOLD, 20)); //Use a differnt font for the header.
            g.drawString("Waste Report", 70, y);
            y += 30;
            g.setFont(font); //Chagne back to the old font.

            String page = "Page " + (pageIndex + 1);
            g.drawString(page, (int) (pageFormat.getWidth() / 2) - (g.getFontMetrics(font).stringWidth(page) / 2), (int) pageFormat.getHeight() - 10);

            int lineSpace = g.getFontMetrics(font).getHeight();

            g.drawString("Start date: " + start.toString(), x, y);
            y += lineSpace;
            g.drawString("End date: " + end.toString(), x, y);
            y += 30;

            final int topX = x;
            final int topY = y;
            final int pCol = x + 10;
            final int qCol = x + 200;
            final int rCol = x + 240;
            final int vCol = x + 320;

            g.setColor(Color.lightGray);
            g.fillRect(x, y, width, lineSpace);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, width, lineSpace);
            g.drawString("Product", pCol, y + lineSpace - 4);
            g.drawString("Qty.", qCol, y + lineSpace - 4);
            g.drawString("Reason", rCol, y + lineSpace - 4);
            g.drawString("Total Value", vCol, y + lineSpace - 4);
            BigDecimal total = BigDecimal.ZERO;
            y += lineSpace;
            for (WasteItem i : items) {
                y += lineSpace;
                g.drawString(i.getProduct().getLongName(), pCol, y - 4);
                g.drawString(i.getQuantity() + "", qCol, y - 4);
                g.drawString(i.getReason().getReason(), rCol, y - 4);
                g.drawString("£" + i.getTotalValue().setScale(2, 6).toString(), vCol, y - 4);
                total = total.add(i.getTotalValue());
            }

            g.setColor(Color.lightGray);
            g.fillRect(x, y, width, lineSpace);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, width, lineSpace);

            g.setFont(new Font(font.getFamily(), Font.BOLD, font.getSize()));
            g.drawString("Total", pCol, y + lineSpace - 4);
            g.drawString("£" + total.setScale(2, 6).toString(), vCol, y + lineSpace - 4);
            g.setFont(font);

            y += lineSpace;

            g.drawRect(topX, topY, width, y - topY);
            g.drawLine(qCol - 5, topY, qCol - 5, y - lineSpace);
            g.drawLine(rCol - 5, topY, rCol - 5, y - lineSpace);
            g.drawLine(vCol - 5, topY, vCol - 5, y);

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
        jLabel1 = new javax.swing.JLabel();
        cmbSearch = new javax.swing.JComboBox<>();
        btnSearch = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        dateStart = new org.jdesktop.swingx.JXDatePicker();
        dateEnd = new org.jdesktop.swingx.JXDatePicker();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Waste Reports");
        setResizable(false);

        btnClose.setText("Cancel");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        jLabel1.setText("Search for reports");

        cmbSearch.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "all", "containing item", "department", "category", "with reason" }));

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        jLabel2.setText("Start date:");

        jLabel3.setText("End date:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dateStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dateEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(dateStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(dateEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnSearch))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        try {
            int reason = cmbSearch.getSelectedIndex();

            Date start = dateStart.getDate();
            Date end = new Date(dateEnd.getDate().getTime() + 86399999L);

            List<WasteItem> allItems = dc.getAllWasteItems();
            List<WasteItem> items = new LinkedList<>();

            switch (reason) {
                case CONTAINING: {
                    Product p = ProductSelectDialog.showDialog(this);
                    if (p == null) {
                        return;
                    }
                    for (WasteItem i : allItems) {
                        if (i.getTimestamp().before(end) && i.getTimestamp().after(start)) {
                            if (i.getProduct().equals(p)) {
                                items.add(i);
                            }
                        }
                    }
                    print(start, end, items);
                    break;
                }
                case DEPARTMENT: {
                    Department d = DepartmentSelectDialog.showDialog(this);
                    if (d == null) {
                        return;
                    }
                    for (WasteItem i : allItems) {
                        if (i.getTimestamp().before(end) && i.getTimestamp().after(start)) {
                            if (i.getProduct().getCategory().getDepartment().equals(d)) {
                                items.add(i);
                            }
                        }
                    }
                    print(start, end, items);
                    break;
                }
                case CATEGORY: {
                    Category c = CategorySelectDialog.showDialog(this);
                    if (c == null) {
                        return;
                    }
                    for (WasteItem i : allItems) {
                        if (i.getTimestamp().before(end) && i.getTimestamp().after(start)) {
                            if (i.getProduct().getCategory().equals(c)) {
                                items.add(i);
                            }
                        }
                    }
                    print(start, end, items);
                    break;
                }
                case REASON: {
                    WasteReason wr = WasteReasonSelectDialog.showDialog(this);
                    if (wr == null) {
                        return;
                    }
                    for (WasteItem i : allItems) {
                        if (i.getTimestamp().before(end) && i.getTimestamp().after(start)) {
                            if (i.getReason().equals(wr)) {
                                items.add(i);
                            }
                        }
                    }
                    print(start, end, items);
                    break;
                }
                case ALL: {
                    for (WasteItem i : allItems) {
                        if (i.getTimestamp().before(end) && i.getTimestamp().after(start)) {
                            items.add(i);
                        }
                    }
                    print(start, end, items);
                    break;
                }
                default: {
                    break;
                }
            }
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox<String> cmbSearch;
    private org.jdesktop.swingx.JXDatePicker dateEnd;
    private org.jdesktop.swingx.JXDatePicker dateStart;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    // End of variables declaration//GEN-END:variables
}
