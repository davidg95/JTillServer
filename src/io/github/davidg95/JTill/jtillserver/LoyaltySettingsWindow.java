/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Image;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author David
 */
public class LoyaltySettingsWindow extends javax.swing.JInternalFrame {

    private static LoyaltySettingsWindow window;

    private final DataConnect dc;

    private final DefaultListModel model;
    private final List<JTillObject> contents;

    /**
     * Creates new form LoyaltySettingsWindow
     */
    public LoyaltySettingsWindow(Image icon) {
        dc = GUI.gui.dc;
        contents = new ArrayList<>();
        initComponents();
//        setIconImage(GUI.gui.icon);
        super.setClosable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(icon));
        setTitle("Loyalty points settings");
        model = new DefaultListModel();
        list.setModel(model);
        readFiles();
        init();
    }

    public static void showWindow(Image icon) {
        if (window == null) {
            window = new LoyaltySettingsWindow(icon);
            GUI.gui.internal.add(window);
        }
        window.setVisible(true);
        try {
            window.setIcon(false);
            window.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(LoyaltySettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void init() {
        model.setSize(0);
        try {
            txtSpent.setText(dc.getSetting("LOYALTY_VALUE"));
            txtSpendValue.setText(dc.getSetting("LOYALTY_SPEND_VALUE"));
            for (JTillObject o : contents) {
                if (o instanceof Product) {
                    model.addElement("Product: " + o);
                } else if (o instanceof Department) {
                    model.addElement("Department: " + o);
                } else if (o instanceof Category) {
                    model.addElement("Category: " + o);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(LoyaltySettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readFiles() {
        try (Scanner inDep = new Scanner(new File("departments.loyalty"))) {
            while (inDep.hasNext()) {
                try {
                    String line = inDep.nextLine();
                    int id = Integer.parseInt(line);
                    final Department d = dc.getDepartment(id);
                    contents.add(d);
                } catch (IOException | SQLException | JTillException ex) {
                    Logger.getLogger(LoyaltySettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (FileNotFoundException e) {
            try {
                new File("departments.loyalty").createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(LoyaltySettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try (Scanner inCat = new Scanner(new File("categorys.loyalty"))) {
            while (inCat.hasNext()) {
                try {
                    String line = inCat.nextLine();
                    int id = Integer.parseInt(line);
                    final Category c = dc.getCategory(id);
                    contents.add(c);
                } catch (IOException | SQLException | JTillException ex) {
                    Logger.getLogger(LoyaltySettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (FileNotFoundException e) {
            try {
                new File("categorys.loyalty").createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(LoyaltySettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try (Scanner inPro = new Scanner(new File("products.loyalty"))) {
            while (inPro.hasNext()) {
                try {
                    String line = inPro.nextLine();
                    int id = Integer.parseInt(line);
                    final Product p = dc.getProduct(id);
                    contents.add(p);
                } catch (IOException | ProductNotFoundException | SQLException ex) {
                    Logger.getLogger(LoyaltySettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (FileNotFoundException e) {
            try {
                new File("products.loyalty").createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(LoyaltySettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void writeFile() {

        final File fDep = new File("departments.loyalty");
        final File fCat = new File("categorys.loyalty");
        final File fPro = new File("products.loyalty");

        try {
            if (!fDep.exists()) {
                fDep.createNewFile();
            }
            if (!fCat.exists()) {
                fCat.createNewFile();
            }
            if (!fPro.exists()) {
                fPro.createNewFile();
            }

            final FileOutputStream dep = new FileOutputStream(fDep);
            final FileOutputStream cat = new FileOutputStream(fCat);
            final FileOutputStream pro = new FileOutputStream(fPro);

            try {
                for (JTillObject o : contents) {
                    if (o instanceof Department) {
                        dep.write((o.getId() + "\n").getBytes());
                    } else if (o instanceof Category) {
                        cat.write((o.getId() + "\n").getBytes());
                    } else if (o instanceof Product) {
                        pro.write((o.getId() + "\n").getBytes());
                    }
                }
            } finally {
                dep.close();
                cat.close();
                pro.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(LoyaltySettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
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
        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList<>();
        addDCP = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtSpent = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        btnSave = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        txtSpendValue = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

        jLabel1.setText("Earn loyalty points from the following:");

        jScrollPane1.setViewportView(list);

        addDCP.setText("Add D/C/P");
        addDCP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDCPActionPerformed(evt);
            }
        });

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Loyalty Settings")));

        jLabel2.setText("One loyalty point for every ");

        txtSpent.setText("0");

        jLabel3.setText(" spent.");

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        jLabel4.setText("One point is worth ");

        txtSpendValue.setText("0");

        jLabel5.setText(" when spent");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtSpent, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtSpendValue, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel5)))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(btnSave)
                        .addGap(48, 48, 48))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtSpent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtSpendValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSave))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane1)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 11, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addDCP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addDCP)
                    .addComponent(btnClose))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        try {
            String val = txtSpent.getText();
            String spendVal = txtSpendValue.getText();
            if (val.length() == 0 || !Utilities.isNumber(val) || spendVal.length() == 0 || !Utilities.isNumber(spendVal)) {
                JOptionPane.showMessageDialog(this, "A number must be entered", "Loyalty Settings", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int value = Integer.parseInt(val);
            int spendValue = Integer.parseInt(spendVal);
            if (value < 0 || spendValue < 0) {
                JOptionPane.showMessageDialog(this, "Negatives are not allowed", "Loyalty Settings", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dc.setSetting("LOYALTY_VALUE", txtSpent.getText());
            dc.setSetting("LOYALTY_SPEND_VALUE", txtSpendValue.getText());
            JOptionPane.showMessageDialog(this, "Loyalty settings saved", "Loyalty Settings", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            Logger.getLogger(LoyaltySettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void addDCPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDCPActionPerformed
        LinkedList<Class<?>> filter = new LinkedList<>();
        filter.add(Product.class);
        filter.add(Category.class);
        filter.add(Department.class);
        JTillObject o = JTillObjectSelectDialog.showDialog(this, dc, "Select Department, Category or Product", filter);
        contents.add(o);
        writeFile();
        init();
    }//GEN-LAST:event_addDCPActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addDCP;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<String> list;
    private javax.swing.JTextField txtSpendValue;
    private javax.swing.JTextField txtSpent;
    // End of variables declaration//GEN-END:variables
}
