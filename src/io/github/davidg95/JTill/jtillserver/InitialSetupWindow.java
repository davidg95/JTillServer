/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author David
 */
public class InitialSetupWindow extends javax.swing.JDialog {

    private static JDialog dialog;

    private static boolean complete;

    private final Settings s;

    /**
     * Creates new form InitialSetupWindow
     */
    public InitialSetupWindow() {
        super();
        this.s = Settings.getInstance();
        initComponents();
        setModal(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setIconImage(TillServer.icon);
    }

    public static boolean showWindow() {
        dialog = new InitialSetupWindow();
        complete = false;
        dialog.setVisible(true);
        return complete;
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
        txtSiteName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtPort = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtCurrencySymbol = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtCompanyName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtAddress = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();
        txtVATNumber = new javax.swing.JTextField();
        btnStart = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Initial Setup");

        jLabel1.setText("Site name:");

        jLabel2.setText("Server port number:");

        txtPort.setText("52341");

        jLabel3.setText("Currency symbol:");

        txtCurrencySymbol.setText("£");

        jLabel4.setText("Company name:");

        jLabel5.setText("Company address:");

        txtAddress.setColumns(20);
        txtAddress.setRows(5);
        jScrollPane1.setViewportView(txtAddress);

        jLabel6.setText("VAT No.:");

        btnStart.setText("Confirm");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPort)
                            .addComponent(txtCurrencySymbol)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                            .addComponent(txtCompanyName)
                            .addComponent(txtSiteName)
                            .addComponent(txtVATNumber)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnStart)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtSiteName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtCurrencySymbol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtCompanyName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtVATNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        final String siteName = txtSiteName.getText();
        if (siteName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You must enter a site name", "Initial Setup", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final String strPort = txtPort.getText();
        if (strPort.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You must enter a port number", "Initial Setup", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!Utilities.isNumber(strPort)) {
            JOptionPane.showMessageDialog(this, "You must enter a number for port number", "Initial Setup", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final int port = Integer.parseInt(txtPort.getText());
        if (port <= 0 || port > 65535) {
            JOptionPane.showMessageDialog(this, "Port must be in range 1-65535", "Initial Setup", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final String currency = txtCurrencySymbol.getText();
        if (currency.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You must enter a currency symbol", "Initial Setup", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final String companyName = txtCompanyName.getText();
        if (companyName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You must enter a company name", "Initial Setup", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final String companyAddress = txtAddress.getText();
        if (companyAddress.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You must enter a company address", "Initial Setup", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final String vat = txtVATNumber.getText();
        if (vat.isEmpty()) {
            if (JOptionPane.showConfirmDialog(this, "You have not entered a VAT number, this is required to print a VAT receipt, continue?", "Initial Setup", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }
        }

        try {
            s.setSetting("SITE_NAME", siteName);
            s.setSetting("port", strPort);
            s.setSetting("CURRENCY_SYMBOL", currency);

            Properties properties = new Properties();
            OutputStream out;
            out = new FileOutputStream("company.details");
            properties.setProperty("NAME", companyName);
            properties.setProperty("ADDRESS", companyAddress);
            properties.setProperty("VAT", vat);
            properties.store(out, null);
            out.close();
            complete = true;
            this.setVisible(false);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving some settings\n" + ex, "Initial Setup", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        File file = new File("company.details");
        File file2 = new File("server.properties");
        file.delete();
        file2.delete();
        setVisible(false);
    }//GEN-LAST:event_btnCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnStart;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea txtAddress;
    private javax.swing.JTextField txtCompanyName;
    private javax.swing.JTextField txtCurrencySymbol;
    private javax.swing.JTextField txtPort;
    private javax.swing.JTextField txtSiteName;
    private javax.swing.JTextField txtVATNumber;
    // End of variables declaration//GEN-END:variables
}
