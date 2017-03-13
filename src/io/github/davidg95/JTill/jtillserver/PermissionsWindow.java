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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author David
 */
public class PermissionsWindow extends javax.swing.JDialog {

    private final Logger log = Logger.getGlobal();

    private static PermissionsWindow dialog;

    private Settings settings;

    private TreeModel model;

    private final DataConnect dc;

    private DefaultMutableTreeNode current;

    /**
     * Creates new form PermissionsWindow
     */
    public PermissionsWindow(Window parent, DataConnect dc) {
        super(parent);
        this.dc = dc;
        try {
            settings = dc.getSettingsInstance();
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        initComponents();
        setLocationRelativeTo(parent);
        setModal(true);
        setTitle("Permissions");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        init();
    }

    public static void showDialog(Component parent, DataConnect dc) {
        Window window = null;
        if (parent instanceof Frame || parent instanceof Dialog) {
            window = (Window) parent;
        }
        dialog = new PermissionsWindow(window, dc);
        dialog.setVisible(true);
    }

    private void init() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("JTill Terminal");

        Iterator it = settings.getProperties().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            root.add(new DefaultMutableTreeNode(pair.getKey()));
        }

        model = new PermissionTreeModel(root);
        jTree1.setModel(model);
        jTree1.setRootVisible(false);

        jTree1.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
                txtValue.setText(settings.getProperties().get(node.toString()).toString());
                current = node;
            }
        });
        revalidate();
    }

    private class PermissionTreeModel implements TreeModel {

        DefaultMutableTreeNode root;
        List<TreeModelListener> listeners;

        public PermissionTreeModel(DefaultMutableTreeNode root) {
            this.root = root;
            listeners = new ArrayList<>();
        }

        @Override
        public Object getRoot() {
            return root;
        }

        @Override
        public Object getChild(Object parent, int index) {
            if (parent instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode c = (DefaultMutableTreeNode) parent;
                return c.getChildAt(index);
            }
            return null;
        }

        @Override
        public int getChildCount(Object parent) {
            if (parent instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode c = (DefaultMutableTreeNode) parent;
                return c.getChildCount();
            }
            return 0;
        }

        @Override
        public boolean isLeaf(Object node) {
            if (!(node instanceof DefaultMutableTreeNode)) {
                return true;
            }
            DefaultMutableTreeNode c = (DefaultMutableTreeNode) node;
            return c.getChildCount() == 0;
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            if (!(parent instanceof DefaultMutableTreeNode)) {
                return -1;
            }
            DefaultMutableTreeNode c = (DefaultMutableTreeNode) parent;
            if (c.getChildCount() == 0) {
                return -1;
            }
            for (int i = 0; i < c.getChildCount(); i++) {
                if (c.getChildAt(i) == child) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {
            listeners.add(l);
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
            listeners.remove(l);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jButton1 = new javax.swing.JButton();
        txtValue = new javax.swing.JTextField();
        btnSave = new javax.swing.JButton();
        btnCreateSetting = new javax.swing.JButton();
        btnRemoveSetting = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jScrollPane1.setViewportView(jTree1);

        jButton1.setText("Close");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnCreateSetting.setText("Create Setting");
        btnCreateSetting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateSettingActionPerformed(evt);
            }
        });

        btnRemoveSetting.setText("Remove Setting");
        btnRemoveSetting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveSettingActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtValue, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnSave, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnCreateSetting)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRemoveSetting)))
                        .addGap(0, 55, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSave)
                        .addGap(177, 177, 177)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnCreateSetting)
                            .addComponent(btnRemoveSetting))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                        .addComponent(jButton1))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        for (Map.Entry pair : settings.getProperties().entrySet()) {
            try {
                dc.setSetting(pair.getKey().toString(), (String) pair.getValue());
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        }
        setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        settings.getProperties().put(current.toString(), txtValue.getText());
        settings.saveProperties();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnCreateSettingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateSettingActionPerformed
        String key = JOptionPane.showInputDialog(this, "Enter name for new setting", "New Setting", JOptionPane.PLAIN_MESSAGE);
        if (key != null) {
            String value = JOptionPane.showInputDialog(this, "Enter value for new setting", key, JOptionPane.PLAIN_MESSAGE);
            if (value != null) {
                settings.setSetting(key, value);
                settings.saveProperties();
                init();
                JOptionPane.showMessageDialog(this, "Setting " + key + " created with value " + value, "New Settings", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnCreateSettingActionPerformed

    private void btnRemoveSettingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveSettingActionPerformed
        if (current != null) {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove the setting " + current, "Remove " + current, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                settings.removeSetting(current.toString());
                settings.saveProperties();
                init();
            }
        } else {
            JOptionPane.showMessageDialog(this, "You must select a setting to remove", "Remove Setting " + current, JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnRemoveSettingActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCreateSetting;
    private javax.swing.JButton btnRemoveSetting;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTree1;
    private javax.swing.JTextField txtValue;
    // End of variables declaration//GEN-END:variables
}
