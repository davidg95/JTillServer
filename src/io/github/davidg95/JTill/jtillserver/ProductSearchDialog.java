/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.Product;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * Dialog for searching a product.
 *
 * @author David
 */
public class ProductSearchDialog extends javax.swing.JDialog {

    private static JDialog dialog;
    private static List<Product> newList;

    private final List<Product> products;

    /**
     * Creates new form ProductSearchDialog
     *
     * @param parent the parent component.
     * @param products the products to search.
     */
    public ProductSearchDialog(Window parent, List<Product> products) {
        super(parent);
        this.products = products;
        initComponents();
        this.setLocationRelativeTo(parent);
        this.setModal(true);
    }

    /**
     * Method for showing the product search dialog.
     *
     * @param parent the parent component.
     * @param products the list of products which will be searched.
     * @return a list of products which matched the criteria.
     */
    public static List<Product> showSearchDialog(Component parent, List<Product> products) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        dialog = new ProductSearchDialog(window, products);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        newList = new ArrayList<>();
        dialog.setVisible(true);
        return newList;
    }

    private enum Selection {
        PRODUCT_CODE, BARCODE, NAME
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        txtSearchTerms = new javax.swing.JTextField();
        radCode = new javax.swing.JRadioButton();
        radBarcode = new javax.swing.JRadioButton();
        radName = new javax.swing.JRadioButton();
        btnSearch = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Enter Search Terms");
        setResizable(false);

        jLabel1.setText("Search Terms:");

        txtSearchTerms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchTermsActionPerformed(evt);
            }
        });

        buttonGroup1.add(radCode);
        radCode.setText("Product Code");

        buttonGroup1.add(radBarcode);
        radBarcode.setText("Barcode");

        buttonGroup1.add(radName);
        radName.setSelected(true);
        radName.setText("Name");

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
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
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSearchTerms, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(radName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radBarcode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radCode))))
            .addGroup(layout.createSequentialGroup()
                .addGap(75, 75, 75)
                .addComponent(btnSearch))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtSearchTerms, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radBarcode)
                    .addComponent(radName)
                    .addComponent(radCode))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        Selection option;
        String terms = txtSearchTerms.getText();

        if (radCode.isSelected()) {
            option = Selection.PRODUCT_CODE;
        } else if (radBarcode.isSelected()) {
            option = Selection.BARCODE;
        } else {
            option = Selection.NAME;
        }

        switch (option) {
            case PRODUCT_CODE:
                for (Product p : products) {
                    if ((p.getId() + "").equals(terms)) {
                        newList.add(p);
                    }
                }
                break;
            case BARCODE:
                for (Product p : products) {
                    if (p.getBarcode().equals(terms)) {
                        newList.add(p);
                    }
                }
                break;
            default:
                for (Product p : products) {
                    if (p.getLongName().toLowerCase().contains(terms.toLowerCase())) {
                        newList.add(p);
                    }
                }
                break;
        }

        if (newList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records found", "Search", JOptionPane.PLAIN_MESSAGE);
            newList = products;
        }

        this.setVisible(false);
    }//GEN-LAST:event_btnSearchActionPerformed

    private void txtSearchTermsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchTermsActionPerformed
        btnSearch.doClick();
    }//GEN-LAST:event_txtSearchTermsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSearch;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JRadioButton radBarcode;
    private javax.swing.JRadioButton radCode;
    private javax.swing.JRadioButton radName;
    private javax.swing.JTextField txtSearchTerms;
    // End of variables declaration//GEN-END:variables
}
