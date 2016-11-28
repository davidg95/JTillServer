/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.tillserver;

import io.github.davidg95.Till.till.Customer;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author David
 */
public class CustomerSearchDialog extends javax.swing.JDialog {
    
    private static JDialog dialog;
    private static List<Customer> newList;
    
    private List<Customer> customers;
    

    /**
     * Creates new form CustomerSearchDialog
     */
    public CustomerSearchDialog(Window parent, List<Customer> customers) {
        super(parent);
        this.customers = customers;
        initComponents();
        this.setLocationRelativeTo(parent);
        this.setModal(true);
    }
    
    public static List<Customer> showSearchDialog(Component parent, List<Customer> customers){
        Window window = null;
        if(parent instanceof Dialog || parent instanceof Frame){
            window = (Window) parent;
        }
        dialog = new CustomerSearchDialog(window, customers);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        newList = new ArrayList<>();
        dialog.setVisible(true);
        return newList;
    }
    
    private enum Selection{
        ID,NAME
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
        radID = new javax.swing.JRadioButton();
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

        buttonGroup1.add(radID);
        radID.setText("ID");

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
                        .addComponent(txtSearchTerms, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addComponent(btnSearch))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(radName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radID)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(radID)
                    .addComponent(radName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSearch)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        Selection option;
        String terms = txtSearchTerms.getText();
        
        if(radID.isSelected()){
            option = Selection.ID;
        } else{
            option = Selection.NAME;
        }
        
        if(option == Selection.ID){
            for(Customer c: customers){
                if((c.getId() + "").equals(terms)){
                    newList.add(c);
                }
            }
        } else{
            for(Customer c: customers){
                if(c.getName().toLowerCase().contains(terms.toLowerCase())){
                    newList.add(c);
                }
            }
        }
        
        if(newList.isEmpty()){
            JOptionPane.showMessageDialog(this, "No records found", "Search", JOptionPane.PLAIN_MESSAGE);
            newList = customers;
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
    private javax.swing.JRadioButton radID;
    private javax.swing.JRadioButton radName;
    private javax.swing.JTextField txtSearchTerms;
    // End of variables declaration//GEN-END:variables
}
