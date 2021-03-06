/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class ConsolidatedReportingWindow extends javax.swing.JInternalFrame {

    private List<Sale> sales;

    private final Date start;
    private final Date end;
    private final Till till;

    private List<Department> departments;
    private List<Category> categorys;
    private List<Tax> taxes;

    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal cost = BigDecimal.ZERO;
    private BigDecimal tax = BigDecimal.ZERO;
    private BigDecimal refunds = BigDecimal.ZERO;
    private BigDecimal wastage = BigDecimal.ZERO;

    private final JTill jtill;

    /**
     * Creates new form ConsolodatedReportingWindow
     */
    public ConsolidatedReportingWindow(JTill jtill, Date start, Date end, Till till) {
        this.jtill = jtill;
        this.start = start;
        this.end = end;
        this.till = till;
        initComponents();
        setTitle("Consolidated Report - " + (till == null ? "All Terminals" : till.getName()));
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        final ModalDialog mDialog = new ModalDialog(GUI.gui, "Consolodated Report");
        final Runnable run = () -> {
            try {
                retrieve(start, end, till);
                init();
            } catch (IOException | SQLException ex) {
                mDialog.hide();
                JOptionPane.showMessageDialog(ConsolidatedReportingWindow.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                mDialog.hide();
            }
        };
        final Thread thread = new Thread(run, "Consolodate");
        thread.start();
        mDialog.show();
    }

    private void retrieve(Date start, Date end, Till till) throws IOException, SQLException {
        if (till == null) {
            sales = jtill.getDataConnection().consolidated(start, end, -1);
            refunds = jtill.getDataConnection().getRefunds(start, end, -1).setScale(2, 6);
        } else {
            sales = jtill.getDataConnection().consolidated(start, end, till.getId());
            refunds = jtill.getDataConnection().getRefunds(start, end, till.getId()).setScale(2, 6);
        }
        wastage = jtill.getDataConnection().getWastage(start, end).setScale(2, 6);
    }

    public static void showWindow(JTill jtill, Date start, Date end, Till till) {
        ConsolidatedReportingWindow window = new ConsolidatedReportingWindow(jtill, start, end, till);
        GUI.gui.internal.add(window);
        window.setVisible(true);
        try {
            window.setIcon(false);
            window.setSelected(true);
        } catch (PropertyVetoException ex) {
            GUI.LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void init() {
        try {
            int transactions = sales.size();

            departments = jtill.getDataConnection().getAllDepartments();
            categorys = jtill.getDataConnection().getAllCategorys();
            taxes = jtill.getDataConnection().getAllTax();

            for (Sale s : sales) {
                total = total.add(s.getTotal());
                for (SaleItem si : s.getSaleItems()) {
                    cost = cost.add(si.getTotalCost());
                    for (Department d : departments) {
                        final Product p = (Product) si.getProduct();
                        if (d.equals(p.getDepartment())) {
                            d.addToSales(si.getTotalPrice());
                        }
                    }
                    for (Category c : categorys) {
                        final Product p = (Product) si.getProduct();
                        if (c.equals(p.getCategory())) {
                            c.addToSales(si.getTotalPrice());
                        }
                    }
                    for (Tax t : taxes) {
                        final Product p = (Product) si.getProduct();
                        if (t.getName() == null ? p.getTax().getName() == null : t.getName().equals(p.getTax().getName())) {
                            t.addToSales(si.getTotalPrice());
                            t.addToPayable(si.getTotalTax());
                        }
                    }
                    tax = tax.add(si.getTotalTax());
                }
            }

            DefaultTableModel dModel = (DefaultTableModel) depsTable.getModel();
            depsTable.setModel(dModel);
            depsTable.setSelectionModel(new ForcedListSelectionModel());
            for (Department d : departments) {
                Object[] o = new Object[]{d.getName(), "£" + d.getSales().toString()};
                dModel.addRow(o);
            }

            DefaultTableModel cModel = (DefaultTableModel) catTable.getModel();
            catTable.setModel(cModel);
            catTable.setSelectionModel(new ForcedListSelectionModel());
            for (Category c : categorys) {
                Object[] o = new Object[]{c.getName(), "£" + c.getSales().toString()};
                cModel.addRow(o);
            }

            DefaultTableModel tModel = (DefaultTableModel) taxTable.getModel();
            taxTable.setModel(tModel);
            taxTable.setSelectionModel(new ForcedListSelectionModel());
            for (Tax t : taxes) {
                Object[] o = new Object[]{t.getName(), "£" + t.getSales().toString(), "£" + t.getPayable().toString()};
                tModel.addRow(o);
            }

            BigDecimal net = total.subtract(cost).subtract(tax).setScale(2, 6);

            txtTxn.setText(transactions + "");
            txtSales.setText("£" + total.setScale(2, 6).toString());
            txtCost.setText("£" + cost.toString());
            txtTax.setText("£" + tax.toString());
            txtNet.setText("£" + net.toString());
            txtRefunds.setText("£" + refunds.toString());
            txtWastage.setText("£" + wastage.toString());
        } catch (IOException | SQLException ex) {
            GUI.LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Inner class for printing a receipt from the sale.
     */
    private class ReportPrinter implements Printable {

        private final Date start;
        private final Date end;
        private final Till t;
        private final int x = 70;
        private final int tableLineSpace = 20;

        private int ci;
        private int cj;
        private boolean complete;

        public ReportPrinter(Date start, Date end, Till t) {
            this.start = start;
            this.end = end;
            this.t = t;
            ci = 0;
            cj = 0;
            complete = false;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0 && complete) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            Font font = graphics.getFont();

            int y = 60;
            int width = (int) (pageFormat.getWidth() - 140);

            g2.setFont(new Font("Arial", Font.BOLD, 20)); //Use a differnt font for the header.
            g2.drawString("Consolidated Report", x, y);
            y += 30;
            g2.setFont(font); //Change back to the old font.

            int lineSpace = g2.getFontMetrics(font).getHeight() + 5;

            String page = "Page " + (pageIndex + 1);
            g2.drawString(page, (int) (pageFormat.getWidth() / 2) - (g2.getFontMetrics(font).stringWidth(page) / 2), (int) pageFormat.getHeight() - 10);

            //Print report info.
            String ter = "";

            if (t == null) {
                ter = "all terminals";
            } else {
                ter = "terminal " + t.getName();
            }

            g2.drawString("Consolidated report for " + ter + ".", x, y);
            y += lineSpace;
            g2.drawString("Period " + start.toString() + " - " + end.toString(), x, y);
            y += lineSpace;
            g2.drawString("Staff Member: " + GUI.staff.getName(), x, y);

            y += lineSpace;
            y += lineSpace;
            g2.drawString("Department Breakdown:", x, y);
            y += lineSpace;

            //Print departments
            for (int i = ci; i < departments.size(); i++) {
                final Department d = departments.get(i);
                g2.setColor(Color.LIGHT_GRAY);
                g2.fillRect(x, y, width, tableLineSpace);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, y, width, tableLineSpace);
                g2.drawString(d.getName(), x + 10, y + 15);
                int y1 = y;
                int x2 = width / 2;
                y += tableLineSpace * 2;
                for (int j = cj; j < categorys.size(); j++) {
                    Category c = categorys.get(j);
                    if (c.getDepartment().equals(d)) {
                        g2.drawString(c.getName(), x + 10, y - 5);
                        g2.drawString("£" + c.getSales(), x2 + 10, y - 5);
                        g2.drawLine(x, y, x + width, y);
                        y += tableLineSpace;
                    }
                    if (y + 30 >= pageFormat.getHeight()) {
                        ci = i;
                        cj = j;
                        return PAGE_EXISTS;
                    }
                }
                g2.setFont(new Font("Arial", Font.BOLD, font.getSize()));
                g2.drawString("Total", x + 10, y - 5);
                g2.drawString("£" + d.getSales(), x2 + 10, y - 5);
                g2.setFont(font);
                g2.drawLine(x2, y1 + tableLineSpace, x2, y);
                g2.drawRect(x, y1, width, y - y1);
            }

            complete = true;

            y += tableLineSpace;
            g2.drawString("Tax Sales Breakdown:", x, y);
            y += tableLineSpace;

            //Print taxes
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillRect(x, y, width, tableLineSpace);
            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, width, tableLineSpace);
            g2.drawString("Tax", x + 10, y + 15);
            int y1 = y;
            int w = width / 3;
            int x2 = x + w;
            int x3 = x2 + w;
            g2.drawString("Sales", x2 + 10, y + 15);
            g2.drawString("Payable", x3 + 10, y + 15);
            y += tableLineSpace * 2;
            int y2 = y;
            for (Tax t : taxes) {
                g2.drawString(t.getName(), x + 10, y - 5);
                g2.drawString("£" + t.getSales(), x2 + 10, y - 5);
                g2.drawString("£" + t.getPayable(), x3 + 10, y - 5);
                g2.drawLine(x, y, x + width, y);
                y2 = y;
                y += tableLineSpace;
            }
            g2.drawLine(x2, y1, x2, y2);
            g2.drawLine(x3, y1, x3, y2);
            g2.drawRect(x, y1, width, y - y1 - tableLineSpace);

            y += tableLineSpace;

            g2.drawString("Total sales: £" + total.setScale(2, 6).toString(), x, y);
            if (y + 30 >= pageFormat.getHeight()) {
                return PAGE_EXISTS;
            }
            y += lineSpace;
            g2.drawString("Total cost: £" + cost.toString(), x, y);
            if (y + 30 >= pageFormat.getHeight()) {
                return PAGE_EXISTS;
            }
            y += lineSpace;
            g2.drawString("Total tax payable: £" + tax.toString(), x, y);
            if (y + 30 >= pageFormat.getHeight()) {
                return PAGE_EXISTS;
            }
            y += lineSpace;
            g2.drawString("Net Sales: £" + total.subtract(cost).subtract(tax).toString(), x, y);
            if (y + 30 >= pageFormat.getHeight()) {
                return PAGE_EXISTS;
            }
            y += lineSpace;
            g2.drawString("Refunds: £" + refunds, x, y);
            if (y + 30 >= pageFormat.getHeight()) {
                return PAGE_EXISTS;
            }
            if (t == null) {
                y += lineSpace;
                g2.drawString("Wastage: £" + wastage, x, y);
                if (y + 30 >= pageFormat.getHeight()) {
                    return PAGE_EXISTS;
                }
            }

            return PAGE_EXISTS;
        }

        private void printDepartmentHeader(Graphics g, Department d, int y, int width) {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(x, y, width, tableLineSpace);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, width, tableLineSpace);
            g.drawString(d.getName(), x + 10, y + 15);
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
        jPanel1 = new javax.swing.JPanel();
        txtCost = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        txtSales = new javax.swing.JTextField();
        txtNet = new javax.swing.JTextField();
        txtTxn = new javax.swing.JTextField();
        txtTax = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtRefunds = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtWastage = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taxTable = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        depsTable = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        catTable = new javax.swing.JTable();
        btnPrint = new javax.swing.JButton();
        btnPreivew = new javax.swing.JButton();

        setTitle("Consolidated");

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Summary"));

        txtCost.setEditable(false);

        jLabel1.setText("Transactions:");

        txtSales.setEditable(false);

        txtNet.setEditable(false);

        txtTxn.setEditable(false);

        txtTax.setEditable(false);

        jLabel4.setText("Tax:");

        jLabel3.setText("Cost:");

        jLabel2.setText("Sales:");

        jLabel5.setText("Net:");

        jLabel6.setText("Refunds:");

        txtRefunds.setEditable(false);

        jLabel7.setText("Wastage:");

        txtWastage.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtTxn)
                    .addComponent(txtSales)
                    .addComponent(txtCost)
                    .addComponent(txtTax)
                    .addComponent(txtNet, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                    .addComponent(txtRefunds)
                    .addComponent(txtWastage))
                .addContainerGap(74, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtTxn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtSales, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtCost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtNet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtRefunds, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtWastage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(73, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Tax Breakdown"));

        taxTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tax Class", "Sales", "Payable"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        taxTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(taxTable);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Departments Breakdown"));

        depsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Department", "Sales"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        depsTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(depsTable);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Categories Breakdown"));

        catTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Category", "Sales"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        catTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(catTable);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnPrint.setText("Print");
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });

        btnPreivew.setText("Preview");
        btnPreivew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreivewActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnPrint)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPreivew)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnPrint)
                    .addComponent(btnPreivew))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new ReportPrinter(start, end, till));
        boolean ok = job.printDialog();
        final ModalDialog mDialog = new ModalDialog(this, "Printing...", job);
        if (ok) {
            Runnable runnable = () -> {
                try {
                    job.print();
                    mDialog.hide();
                    JOptionPane.showMessageDialog(ConsolidatedReportingWindow.this, "Printing complete", "Print", JOptionPane.INFORMATION_MESSAGE);
                } catch (PrinterException ex) {
                    mDialog.hide();
                    JOptionPane.showMessageDialog(ConsolidatedReportingWindow.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    mDialog.hide();
                }
            };
            Thread th = new Thread(runnable);
            th.start();
            mDialog.show();
        }
    }//GEN-LAST:event_btnPrintActionPerformed

    private void btnPreivewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreivewActionPerformed
        PrintPreviewForm form = new PrintPreviewForm(new ReportPrinter(start, end, till));
    }//GEN-LAST:event_btnPreivewActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnPreivew;
    private javax.swing.JButton btnPrint;
    private javax.swing.JTable catTable;
    private javax.swing.JTable depsTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable taxTable;
    private javax.swing.JTextField txtCost;
    private javax.swing.JTextField txtNet;
    private javax.swing.JTextField txtRefunds;
    private javax.swing.JTextField txtSales;
    private javax.swing.JTextField txtTax;
    private javax.swing.JTextField txtTxn;
    private javax.swing.JTextField txtWastage;
    // End of variables declaration//GEN-END:variables
}
