/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.DataConnect;
import java.awt.Image;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author David
 */
public class DatabaseWindow extends javax.swing.JInternalFrame {

    private static DatabaseWindow frame;

    private final DataConnect dc;
    private final Image icon;

    /**
     * Creates new form DatabaseWindow
     */
    public DatabaseWindow() {
        this.dc = GUI.getInstance().dc;
        this.icon = GUI.icon;
        initComponents();
        super.setFrameIcon(new ImageIcon(icon));
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setClosable(true);
        init();
    }

    /**
     * Method to showing the products list window. This will create the window
     * if needed.
     */
    public static void showDatabaseWindow() {
        if (frame == null || frame.isClosed()) {
            frame = new DatabaseWindow();
            GUI.gui.internal.add(frame);
        }
        if (frame.isVisible()) {
            frame.toFront();
        } else {
            frame.setVisible(true);
        }
        try {
            frame.setIcon(false);
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(SettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void init() {
        try {
            Object[] ob = dc.databaseInfo();
            String catalog = (String) ob[0];
            String schema = (String) ob[1];
            Properties info = (Properties) ob[2];
            String drivername = (String) ob[3];
            String driverversion = (String) ob[4];
            lblName.setText(lblName.getText() + drivername);
            lblVer.setText(lblVer.getText() + driverversion);
            lblCatalog.setText(lblCatalog.getText() + schema);
            lblSchema.setText(lblSchema.getText() + schema);
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Database Settings", JOptionPane.ERROR_MESSAGE);
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

        lblName = new javax.swing.JLabel();
        lblVer = new javax.swing.JLabel();
        lblCatalog = new javax.swing.JLabel();
        lblSchema = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        btnClearSales = new javax.swing.JButton();
        btnPurge = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        txtSQL = new javax.swing.JTextField();
        btnSubmit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setTitle("Database Settings");

        lblName.setText("Database Driver: ");

        lblVer.setText("Database Driver Version: ");

        lblCatalog.setText("Catalog: ");

        lblSchema.setText("Schema: ");

        jButton1.setText("Close");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Perform Integrity Check");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        btnClearSales.setText("Clear sales data");
        btnClearSales.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearSalesActionPerformed(evt);
            }
        });

        btnPurge.setText("Purge Database");
        btnPurge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPurgeActionPerformed(evt);
            }
        });

        jLabel1.setText("Submit SQL Statement:");

        txtSQL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSQLActionPerformed(evt);
            }
        });

        btnSubmit.setText("Submit");
        btnSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubmitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblName)
                            .addComponent(lblVer)
                            .addComponent(lblCatalog)
                            .addComponent(lblSchema))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 341, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnClearSales)
                            .addComponent(jButton2)
                            .addComponent(btnPurge)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSQL, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSubmit)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVer)
                    .addComponent(btnClearSales))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblCatalog)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSchema))
                    .addComponent(btnPurge))
                .addGap(37, 37, 37)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtSQL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSubmit))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 195, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        hide();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        final ModalDialog mDialog = new ModalDialog(this, "Database Check", "Checking database integrity..."); //Create the dialog object
        final Runnable run = () -> {
            try {
                dc.integrityCheck(); //Perform the Database check
                mDialog.hide(); //Hide the dialog once the check completes
                JOptionPane.showInternalMessageDialog(GUI.gui.internal, "Check complete. No Issues.", "Database Check", JOptionPane.INFORMATION_MESSAGE); //Show success message
            } catch (IOException | SQLException ex) {
                mDialog.hide(); //Hide the dialog if there is an error
                JOptionPane.showInternalMessageDialog(GUI.gui.internal, ex, "Database Check", JOptionPane.ERROR_MESSAGE); //Show the error
            }
        }; //Create the runnable for performing the database check
        final Thread thread = new Thread(run); //Create the thread for running the integrity check
        thread.start(); //Start the thread
        mDialog.show(); //Show the running dialog
    }//GEN-LAST:event_jButton2ActionPerformed

    private void btnClearSalesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearSalesActionPerformed
        if (JOptionPane.showInternalConfirmDialog(GUI.gui.internal, "This will clear ALL sales data, continue?", "Clear sales data", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            final ModalDialog mDialog = new ModalDialog(this, "Sales Data", "Clearing...");
            final Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        int val = dc.clearSalesData();
                        mDialog.hide();
                        JOptionPane.showMessageDialog(GUI.gui.internal, val + " records removed", "Sales Data", JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException | SQLException ex) {
                        mDialog.hide();
                        JOptionPane.showInternalMessageDialog(GUI.gui.internal, ex, "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        mDialog.hide();
                    }
                }
            };
            final Thread thread = new Thread(run);
            thread.start();
            mDialog.show();
        }
    }//GEN-LAST:event_btnClearSalesActionPerformed

    private void btnPurgeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPurgeActionPerformed
        if (JOptionPane.showInternalConfirmDialog(GUI.gui.internal, "This will clear ALL sales data, received reports and waste data. Continue?", "Purge Database", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            final ModalDialog mDialog = new ModalDialog(this, "Sales Data", "Clearing...");
            final Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        dc.purgeDatabase();
                        mDialog.hide();
                        JOptionPane.showMessageDialog(GUI.gui.internal, "Purge complete", "Purge Database", JOptionPane.INFORMATION_MESSAGE);
                        if (JOptionPane.showConfirmDialog(GUI.gui.internal, "Do you want to set all stock levels to 0?", "Purge database", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            dc.submitSQL("UPDATE PRODUCTS SET STOCK = 0");
                            JOptionPane.showMessageDialog(GUI.gui.internal, "Stock levels set to 0", "Purge Database", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (IOException | SQLException ex) {
                        mDialog.hide();
                        JOptionPane.showInternalMessageDialog(GUI.gui.internal, ex, "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        mDialog.hide();
                    }
                }
            };
            final Thread thread = new Thread(run);
            thread.start();
            mDialog.show();
        }
    }//GEN-LAST:event_btnPurgeActionPerformed

    private void txtSQLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSQLActionPerformed
        btnSubmit.doClick();
    }//GEN-LAST:event_txtSQLActionPerformed

    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        String query = txtSQL.getText();
        if (query.isEmpty()) {
            return;
        }
        final ModalDialog mDialog = new ModalDialog(this, "SQL", "Running...");
        final Runnable run = () -> {
            try {
                dc.submitSQL(query);
                mDialog.hide();
                JOptionPane.showMessageDialog(this, "Query successfully run", "SQL", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | SQLException ex) {
                mDialog.hide();
                JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                mDialog.hide();
            }
        };
        final Thread thread = new Thread(run, "SQL_THREAD");
        thread.start();
        mDialog.show();
        txtSQL.setSelectionStart(0);
        txtSQL.setSelectionEnd(txtSQL.getText().length());
    }//GEN-LAST:event_btnSubmitActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClearSales;
    private javax.swing.JButton btnPurge;
    private javax.swing.JButton btnSubmit;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblCatalog;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblSchema;
    private javax.swing.JLabel lblVer;
    private javax.swing.JTextField txtSQL;
    // End of variables declaration//GEN-END:variables
}
