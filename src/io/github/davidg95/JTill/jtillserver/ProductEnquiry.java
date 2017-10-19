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
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author David
 */
public final class ProductEnquiry extends javax.swing.JInternalFrame {

    private final DataConnect dc;
    private Product product;
    private int totalSold;
    private BigDecimal valueSold;
    private int totalWasted;
    private BigDecimal valueWasted;
    private int totalReceived;
    private BigDecimal valueSpent;

    /**
     * Creates new form ProductEnquiry
     *
     * @param p the product to show an enquiry for, can be null.
     */
    public ProductEnquiry(Product p) {
        this.dc = GUI.gui.dc;
        initComponents();
        super.setClosable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        product = p;
        if (p != null) {
            setProduct();
        }
    }

    /**
     * Method to show the Product Enquiry window.
     */
    public static void showWindow() {
        ProductEnquiry window = new ProductEnquiry(null);
        GUI.gui.internal.add(window);
        window.setVisible(true);
        try {
            window.setIcon(false);
            window.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(ProductEnquiry.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Method to show the Product Enquiry window.
     *
     * @param p the product to show an enquiry for.
     */
    public static void showWindow(Product p) {
        ProductEnquiry window = new ProductEnquiry(p);
        GUI.gui.internal.add(window);
        window.setVisible(true);
        try {
            window.setIcon(false);
            window.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(ProductEnquiry.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setProduct() {
        try {
            //Total sold and value sold
            totalSold = dc.getTotalSoldOfItem(product.getId());
            valueSold = dc.getTotalValueSold(product.getId()).setScale(2);

            //Total wasted and value wasted
            totalWasted = dc.getTotalWastedOfItem(product.getId());
            valueWasted = dc.getValueWastedOfItem(product.getId()).setScale(2);

            //Total and value received.
            valueSpent = dc.getValueSpentOnItem(product.getId()).setScale(2);
            totalReceived = dc.getTotalReceivedOfItem(product.getId());

            txtProduct.setText(product.getLongName());
            txtPlu.setText(product.getBarcode());
            if (product.getOrder_code() == 0) {
                txtOrderCode.setText("N/A");
            } else {
                txtOrderCode.setText(product.getOrder_code() + "");
            }
            DecimalFormat df = new DecimalFormat("0.00");
            txtName.setText(product.getLongName());
            txtShortName.setText(product.getName());
            txtDep.setText(product.getCategory().getDepartment().getName());
            txtCat.setText(product.getCategory().getName());
            txtStock.setText(product.getStock() + "");
            txtMinStock.setText(product.getMinStockLevel() + "");
            txtMaxStock.setText(product.getMaxStockLevel() + "");
            txtSold.setText(totalSold + "");
            txtValSold.setText("£" + valueSold.toString());
            txtWaste.setText(totalWasted + "");
            txtValWaste.setText("£" + valueWasted.toString());
            if (product.isOpen()) {
                txtPrice.setText("OPEN");
            } else {
                txtPrice.setText("£" + df.format(product.getPrice()));
            }
            txtCostPrice.setText("£" + df.format(product.getCostPrice()));
            txtPackSize.setText(product.getPackSize() + "");
            txtValReceived.setText("£" + valueSpent.toString());
            txtReceived.setText(totalReceived + "");
            txtProfit.setText("£" + valueSold.subtract(valueSpent));
            double margin = (valueSpent.doubleValue() / valueSold.doubleValue()) * 100;
            if (Double.isNaN(margin)) {
                txtMarginToDate.setText("---");
            } else if (Double.isInfinite(margin)) {
                txtMarginToDate.setText("INFINITE");
            } else {
                BigDecimal bMargin = new BigDecimal(margin);
                bMargin = bMargin.setScale(2, RoundingMode.HALF_UP);
                txtMarginToDate.setText(bMargin.toString());
            }
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(ProductEnquiry.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public class ProductEnquiryPrintout implements Printable {

        private final Product p;
        private final int ts;
        private final BigDecimal vs;
        private final int tw;
        private final BigDecimal vw;

        public ProductEnquiryPrintout(Product p, int ts, BigDecimal vs, int tw, BigDecimal vw) {
            this.p = p;
            this.ts = ts;
            this.vs = vs;
            this.tw = tw;
            this.vw = vw;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            String header = "Product Enquiry";

            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            Font oldFont = graphics.getFont();

            int y = 60;

            g2.setFont(new Font("Arial", Font.BOLD, 20)); //Use a differnt font for the header.
            g2.drawString(header, 70, y);
            y += 30;
            g2.setFont(oldFont); //Chagne back to the old font.

            int x = 70;
            int s = 30;

            //Print product info.
            g2.drawString("ID: " + p.getId(), x, y);
            y += s;
            g2.drawString("Barcode: " + p.getBarcode(), x, y);
            y += s;
            g2.drawString("Name: " + p.getLongName(), x, y);
            y += s;
            g2.drawString("Category: " + p.getCategory().getName(), x, y);
            y += s;
            g2.drawString("Department: " + p.getCategory().getDepartment().getName(), x, y);
            y += s;
            g2.drawString("Price: £" + p.getPrice(), x, y);
            y += s;
            g2.drawString("Cost Price: £" + p.getCostPrice(), x, y);
            y += s;
            g2.drawString("Stock: " + p.getStock(), x, y);
            y += s;
            g2.drawString("Total units sold: " + ts, x, y);
            y += s;
            g2.drawString("Total value sold: £" + vs, x, y);
            y += s;
            g2.drawString("Total units wasted: " + tw, x, y);
            y += s;
            g2.drawString("Total value wasted: £" + vw, x, y);

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
        txtProduct = new javax.swing.JTextField();
        btnProductSelect = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtPlu = new javax.swing.JTextField();
        txtName = new javax.swing.JTextField();
        txtShortName = new javax.swing.JTextField();
        txtDep = new javax.swing.JTextField();
        txtCat = new javax.swing.JTextField();
        txtStock = new javax.swing.JTextField();
        txtSold = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtWaste = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtValWaste = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtValSold = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtMinStock = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txtMaxStock = new javax.swing.JTextField();
        btnClose = new javax.swing.JButton();
        btnPrint = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        txtCostPrice = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        txtProfit = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        txtMarginToDate = new javax.swing.JTextField();
        txtOrderCode = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        txtReceived = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        txtValReceived = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        txtPackSize = new javax.swing.JTextField();

        setTitle("Product Enquiry");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Search Product"));

        jLabel1.setText("Choose Product:");

        txtProduct.setEditable(false);

        btnProductSelect.setText("Select Product");
        btnProductSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProductSelectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnProductSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(35, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(46, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnProductSelect))
                .addGap(43, 43, 43))
        );

        jLabel2.setText("Barcode:");

        jLabel3.setText("Name:");

        jLabel4.setText("Short Name:");

        jLabel5.setText("Category:");

        jLabel6.setText("Department:");

        jLabel7.setText("Stock:");

        jLabel8.setText("Sold to date:");

        txtPlu.setEditable(false);

        txtName.setEditable(false);

        txtShortName.setEditable(false);

        txtDep.setEditable(false);

        txtCat.setEditable(false);

        txtStock.setEditable(false);

        txtSold.setEditable(false);

        jLabel9.setText("Units Wasted:");

        txtWaste.setEditable(false);

        jLabel10.setText("Value Wasted:");

        txtValWaste.setEditable(false);

        jLabel11.setText("Value Sold:");

        txtValSold.setEditable(false);

        jLabel12.setText("Minimum Stock:");

        txtMinStock.setEditable(false);

        jLabel13.setText("Maximum Stock:");

        txtMaxStock.setEditable(false);

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnPrint.setText("Print");
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });

        jLabel14.setText("Unit Price:");

        txtPrice.setEditable(false);

        jLabel15.setText("Cost Price:");

        txtCostPrice.setEditable(false);

        jLabel17.setText("Profit to date:");

        txtProfit.setEditable(false);

        jLabel18.setText("Margin to date %:");

        txtMarginToDate.setEditable(false);

        txtOrderCode.setEditable(false);

        jLabel19.setText("Order Code:");

        jLabel20.setText("Units Received:");

        txtReceived.setEditable(false);

        jLabel21.setText("Value Received:");

        txtValReceived.setEditable(false);

        jLabel22.setText("Pack Size:");

        txtPackSize.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jLabel7)
                            .addComponent(jLabel14))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtPlu, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                                    .addComponent(txtName)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtDep, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel5)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                            .addComponent(jLabel4)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(txtShortName, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel19)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(txtOrderCode, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(0, 0, 0)))
                                    .addComponent(txtCat, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtPrice)
                                    .addComponent(txtStock, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel12)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtMinStock, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel15)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtCostPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel13)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtMaxStock, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel22)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtPackSize, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel20)
                            .addComponent(jLabel9)
                            .addComponent(jLabel8)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtProfit)
                            .addComponent(txtSold)
                            .addComponent(txtWaste, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtReceived, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jLabel21))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addGap(8, 8, 8)
                                            .addComponent(jLabel10)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGap(28, 28, 28)
                                        .addComponent(jLabel11)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtValReceived)
                                    .addComponent(txtValWaste, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                                    .addComponent(txtValSold)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(jLabel18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtMarginToDate, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnPrint)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnClose)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtPlu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtOrderCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(txtShortName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(txtCat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel12)
                    .addComponent(txtMinStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMaxStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(jLabel15)
                    .addComponent(txtCostPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22)
                    .addComponent(txtPackSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtValReceived, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21)
                    .addComponent(txtReceived, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtWaste, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(txtValWaste, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel11)
                    .addComponent(txtValSold, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtProfit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(jLabel18)
                    .addComponent(txtMarginToDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnPrint))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnProductSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProductSelectActionPerformed
        product = ProductSelectDialog.showDialog(this);
        if (product == null) {
            return;
        }
        setProduct();
    }//GEN-LAST:event_btnProductSelectActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new ProductEnquiryPrintout(product, totalSold, valueSold, totalWasted, valueWasted));
        boolean ok = job.printDialog();
        final ModalDialog mDialog = new ModalDialog(this, "Printing...", "Printing report...", job);
        if (ok) {
            Runnable runnable = () -> {
                try {
                    job.print();
                } catch (PrinterException ex) {
                    mDialog.hide();
                    JOptionPane.showMessageDialog(ProductEnquiry.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    mDialog.hide();
                }
            };
            Thread th = new Thread(runnable);
            th.start();
            mDialog.show();
            JOptionPane.showMessageDialog(this, "Printing complete", "Print", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btnPrintActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnProductSelect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField txtCat;
    private javax.swing.JTextField txtCostPrice;
    private javax.swing.JTextField txtDep;
    private javax.swing.JTextField txtMarginToDate;
    private javax.swing.JTextField txtMaxStock;
    private javax.swing.JTextField txtMinStock;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtOrderCode;
    private javax.swing.JTextField txtPackSize;
    private javax.swing.JTextField txtPlu;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtProduct;
    private javax.swing.JTextField txtProfit;
    private javax.swing.JTextField txtReceived;
    private javax.swing.JTextField txtShortName;
    private javax.swing.JTextField txtSold;
    private javax.swing.JTextField txtStock;
    private javax.swing.JTextField txtValReceived;
    private javax.swing.JTextField txtValSold;
    private javax.swing.JTextField txtValWaste;
    private javax.swing.JTextField txtWaste;
    // End of variables declaration//GEN-END:variables
}
