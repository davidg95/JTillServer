/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author David
 */
public class TillDialog extends javax.swing.JDialog {

    private static JDialog dialog;

    private Till till;
    private final DataConnect dc;

//    private final DefaultTableModel model; //The model for the table.
//    private List<Sale> currentTableContents; //The current table contents.

    /**
     * Creates new form TillDialog
     *
     * @param parent the parent component.
     * @param t the till.
     */
    public TillDialog(Window parent, Till t) {
        super(parent);
        this.till = t;
        this.dc = GUI.gui.dc;
        initComponents();
        setTitle(till.getName());
        setModal(true);
        setLocationRelativeTo(parent);
        txtID.setText("" + till.getId());
        txtUUID.setText(till.getUuid().toString());
        txtName.setText(till.getName());
        txtUncashedTakings.setText("£" + till.getUncashedTakings());
        if (t.getLastContact() == null) {
            txtLastContact.setText("None");
        } else {
            txtLastContact.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(till.getLastContact()));
        }
        txtStaff.setText("Not logged in");
        for (ConnectionThread thread : ConnectionAcceptThread.connections) {
            if (thread.till.equals(t)) {
                Staff s = thread.staff;
                if (s == null) {
                    txtStaff.setText("Not logged in");
                } else {
                    txtStaff.setText(thread.staff.getName());
                }
            }
        }
//        model = (DefaultTableModel) table.getModel();
//        showAllSales();
    }

    public static void showDialog(Component parent, Till till) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        dialog = new TillDialog(window, till);
        dialog.setVisible(true);
    }

    /**
     * Sets the table to the contents of the <code>currentTableContents</code>
     * list.
     */
    private void updateTable() {
//        model.setRowCount(0);
//
//        currentTableContents.stream().map((s) -> new Object[]{s.getId(), s.getTotal(), s.getTotalItemCount(), s.getDate().toString()}).forEachOrdered((r) -> {
//            model.addRow(r);
//        });
//
//        table.setModel(model);
    }

    /**
     * Shows all sales in the database.
     */
    private void showAllSales() {

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblID = new javax.swing.JLabel();
        lblName = new javax.swing.JLabel();
        lblTakings = new javax.swing.JLabel();
        btnClose = new javax.swing.JButton();
        lblStaff = new javax.swing.JLabel();
        lblLastContact = new javax.swing.JLabel();
        lblUUID = new javax.swing.JLabel();
        txtID = new javax.swing.JTextField();
        txtUUID = new javax.swing.JTextField();
        txtName = new javax.swing.JTextField();
        txtUncashedTakings = new javax.swing.JTextField();
        txtStaff = new javax.swing.JTextField();
        txtLastContact = new javax.swing.JTextField();
        btnCashup = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        lblID.setText("Till ID: ");

        lblName.setText("Till Name: ");

        lblTakings.setText("Uncashed Takings: ");

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        lblStaff.setText("Staff:");

        lblLastContact.setText("Last Contact:");

        lblUUID.setText("UUID:");

        txtID.setEditable(false);

        txtUUID.setEditable(false);

        txtName.setEditable(false);

        txtUncashedTakings.setEditable(false);

        txtStaff.setEditable(false);

        txtLastContact.setEditable(false);

        btnCashup.setText("Cash Up");
        btnCashup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCashupActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblLastContact, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblStaff, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblTakings, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblName, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblUUID, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblID, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtID, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtUUID, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtUncashedTakings, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCashup))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(txtLastContact, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                        .addComponent(txtStaff, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtName, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(btnClose))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblID)
                    .addComponent(txtID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUUID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblUUID))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTakings)
                    .addComponent(txtUncashedTakings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCashup))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblStaff)
                    .addComponent(txtStaff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLastContact)
                    .addComponent(txtLastContact, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnClose)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnCashupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCashupActionPerformed
        try {
            BigDecimal takings = dc.getTillTakings(till.getId());
            if(takings.compareTo(BigDecimal.ZERO) < 0){
                JOptionPane.showMessageDialog(this, "That till currently has no declared takings", "Cash up till " + till.getName(), JOptionPane.PLAIN_MESSAGE);
                return;
            }
            takings = takings.setScale(2);
            String strVal = JOptionPane.showInputDialog(this, "Enter value of money counted", "Cash up till " + till.getName(), JOptionPane.PLAIN_MESSAGE);
            if(strVal == null || strVal.equals("")){
                return;
            }
            if(!Utilities.isNumber(strVal)){
                JOptionPane.showMessageDialog(this, "You must enter a number greater than zero", "Cash up till " + till.getName(), JOptionPane.ERROR_MESSAGE);
                return;
            }
            BigDecimal valueCounted = new BigDecimal(strVal);
            valueCounted = valueCounted.setScale(2);
            BigDecimal difference = valueCounted.subtract(takings);
            difference = difference.setScale(2);
            DecimalFormat df;
            if (takings.compareTo(BigDecimal.ONE) >= 1) {
                df = new DecimalFormat("#.00");
            } else {
                df = new DecimalFormat("0.00");
            }
            
            till = dc.getTill(till.getId());
            txtUncashedTakings.setText("£" + till.getUncashedTakings());
            
            JPanel panel = new JPanel();
            JLabel declared = new JLabel("Declared: £" + valueCounted);
            JLabel takingsLabel = new JLabel("Takings: £" + df.format(takings.doubleValue()));
            if (difference.compareTo(BigDecimal.ONE) >= 1) {
                df = new DecimalFormat("#.00");
            } else {
                df = new DecimalFormat("0.00");
            }
            JLabel differenceLabel = new JLabel("Difference: £" + df.format(difference.doubleValue()));
            panel.add(declared);
            panel.add(takingsLabel);
            panel.add(differenceLabel);
            JOptionPane.showMessageDialog(this, panel, "Cash up till " + till.getName(), JOptionPane.INFORMATION_MESSAGE);
            if (JOptionPane.showConfirmDialog(this, "Do you want the report emailed?" ,"Cash up", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                String message = "Cashup for terminal " + till.getName()
                        + "\nValue counted: £" + valueCounted.toString()
                        + "\nActual takings: £" + takings.toString()
                        + "\nDifference: £" + difference.toString();
                dc.sendEmail(message);
            }
        } catch (IOException | SQLException | TillNotFoundException ex) {
            Logger.getLogger(TillDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnCashupActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCashup;
    private javax.swing.JButton btnClose;
    private javax.swing.JLabel lblID;
    private javax.swing.JLabel lblLastContact;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblStaff;
    private javax.swing.JLabel lblTakings;
    private javax.swing.JLabel lblUUID;
    private javax.swing.JTextField txtID;
    private javax.swing.JTextField txtLastContact;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtStaff;
    private javax.swing.JTextField txtUUID;
    private javax.swing.JTextField txtUncashedTakings;
    // End of variables declaration//GEN-END:variables
}
