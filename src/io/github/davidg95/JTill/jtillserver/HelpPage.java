/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author David
 */
public class HelpPage extends javax.swing.JFrame {

    private final Map map;
    private TreeModel model;

    private String text;

    /**
     * Creates new form HelpPage
     */
    public HelpPage() {
        initComponents();
        map = new HashMap();
        /*editPane.setContentType("text/html");
        loadFile();
        editPane.setText(text);
        editPane.setEditable(false);
        init();*/
    }

    private void loadFile() {
        ClassLoader classLoader = getClass().getClassLoader();
        File f = new File(classLoader.getResource("/io/github/davidg95/JTill/resources/help/help_index.html").getFile());

        try {
            Scanner in = new Scanner(f);
            while (in.hasNext()) {
                text += in.nextLine() + "\n";
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HelpPage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initMap() {
        map.put("Products", "The products window contains all the products you have on your till system. Here you can add new products, delete products, and make changes to ecisting products. To add a product, simply click the \"Add Product\" button, you will then see a dialog asking you to scan or enter the barcode of the new product.");
        map.put("Staff", "The staff window contains all the staff that have access to the system. Here you can add new staff, delete staff or edit existing staff.");
    }

    private void init() {
        initMap();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("JTill");

        DefaultMutableTreeNode server = new DefaultMutableTreeNode("Server");
        DefaultMutableTreeNode terminal = new DefaultMutableTreeNode("Terminal");

        root.add(server);
        root.add(terminal);

        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            server.add(new DefaultMutableTreeNode(pair.getKey()));
        }
        model = new HelpModel(root);
        helpTree.setModel(model);
        helpTree.setRootVisible(false);

        helpTree.getSelectionModel().addTreeSelectionListener((TreeSelectionEvent e) -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
            if (map.containsKey(node.toString())) {

            }
        });
        revalidate();
    }

    private class HelpModel implements TreeModel {

        DefaultMutableTreeNode root;
        List<TreeModelListener> listeners;

        public HelpModel(DefaultMutableTreeNode root) {
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

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        helpTree = new javax.swing.JTree();
        jScrollPane3 = new javax.swing.JScrollPane();
        editPane = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Help");

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel1.setText("JTill Server");

        jScrollPane1.setViewportView(helpTree);

        jScrollPane3.setViewportView(editPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3)
                    .addComponent(jScrollPane1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane editPane;
    private javax.swing.JTree helpTree;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    // End of variables declaration//GEN-END:variables
}
