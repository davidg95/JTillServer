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
        if (frame == null) {
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
        JOptionPane.showInternalMessageDialog(frame, "WARNING! Changing the settings here can damage your database beyond repair! Please ensure you know exactly what you are doing before making changes here!", "Database Settings", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showInternalMessageDialog(frame, ex, "Database Settings", JOptionPane.ERROR_MESSAGE);
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
                            .addComponent(jButton2))))
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
                .addComponent(lblCatalog)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSchema)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 247, Short.MAX_VALUE)
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClearSales;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel lblCatalog;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblSchema;
    private javax.swing.JLabel lblVer;
    // End of variables declaration//GEN-END:variables
}
