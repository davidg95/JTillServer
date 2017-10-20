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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Dialog which allows a product to be selected.
 *
 * @author David
 */
public class ProductSelectDialog extends javax.swing.JDialog {

    private static Product product;

    private final DataConnect dc;

    private final DefaultTableModel model;
    private List<Product> allProducts;
    private List<Product> currentTableContents;

    private final boolean showOpen;

    protected boolean closedFlag;

    private MyTreeModel treeModel;

    /**
     * Creates new form ProductSelectDialog
     *
     * @param parent the parent window.
     * @param showOpen indicated whether open price products should show. new
     * product if a barcode is not found.
     */
    public ProductSelectDialog(Window parent, boolean showOpen) {
        super(parent);
        this.dc = GUI.gui.dc;
        this.showOpen = showOpen;
        closedFlag = false;
        initComponents();
        this.setIconImage(GUI.icon);
        setLocationRelativeTo(parent);
        setModal(true);
        currentTableContents = new ArrayList<>();
        allProducts = new ArrayList<>();
        model = (DefaultTableModel) table.getModel();
        showAllProducts();
        txtSearch.requestFocus();
        initTable();
        try {
            init();
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
        tree.setModel(treeModel);
    }

    private void initTable() {
        table.setSelectionModel(new ForcedListSelectionModel());
    }

    private void init() throws IOException, SQLException {
        RootNode top = new RootNode();
        treeModel = new MyTreeModel(top);
        createNodes();
    }

    private void createNodes() throws IOException, SQLException {
        List<Department> departments = dc.getAllDepartments();
        for (Department d : departments) {
            treeModel.insertDepartment(d);
            List<Category> categorys = dc.getCategoriesInDepartment(d.getId());
            for (Category c : categorys) {
                treeModel.insertCategory(c);
            }
        }
    }

    /**
     * Method to show the product select dialog.
     *
     * @param parent the parent component.
     * @param showOpen indicates whether open products should show or not.
     * @return the product selected by the user.
     */
    public static Product showDialog(Component parent, boolean showOpen) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        final ProductSelectDialog dialog = new ProductSelectDialog(window, showOpen);
        product = null;
        dialog.setVisible(true);
        return product;
    }

    /**
     * Method to show the product select dialog.
     *
     * @param parent the parent component.
     * @return the product selected by the user.
     */
    public static Product showDialog(Component parent) {
        return showDialog(parent, true);
    }

    /**
     * Method to update the contents of the table.
     */
    private void updateTable() {
        model.setRowCount(0);

        for (Product p : currentTableContents) {
            Object[] s = new Object[]{p.getId(), p.getLongName()};
            model.addRow(s);
        }

        table.setModel(model);
        ProductsWindow.update();
    }

    private void showAllProducts() {
        try {
            allProducts = dc.getAllProducts();
            currentTableContents = allProducts;
            setTable();
        } catch (IOException | SQLException ex) {
            showError(ex);
        }
    }

    /**
     * Method to show all products in the list.
     */
    private void setTable() {
        List<Product> newList = new ArrayList<>();
        if (!showOpen) {
            for (Product p : currentTableContents) {
                if (!p.isOpen()) {
                    newList.add(p);
                }
            }
            currentTableContents = newList;
        }
        updateTable();
    }

    /**
     * Method to show an error.
     *
     * @param e the exception to show.
     */
    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e, "Products", JOptionPane.ERROR_MESSAGE);
    }

    private class MyTreeModel implements TreeModel {

        private final RootNode root;

        private final List<TreeModelListener> listeners;

        public MyTreeModel(RootNode root) {
            this.root = root;
            listeners = new LinkedList<>();
        }

        public void insertDepartment(Department d) {
            DepartmentNode n = new DepartmentNode(root, d);
            root.addNode(n);
            alertAll();
        }

        public void insertCategory(Category c) {
            for (DepartmentNode n : root.getChildren()) {
                if (c.getDepartment().getId() == n.getDepartment().getId()) {
                    CategoryNode cn = new CategoryNode(n, c);
                    n.addCategory(cn);
                }
            }
            alertAll();
        }

        @Override
        public Object getRoot() {
            return root;
        }

        @Override
        public Object getChild(Object parent, int index) {
            if (parent instanceof RootNode) {
                return root.getChildAt(index);
            }
            if (parent instanceof DepartmentNode) {
                for (DepartmentNode n : root.getChildren()) {
                    if (n.equals(parent)) {
                        return n.getChildAt(index);
                    }
                }
            }
            return null;
        }

        @Override
        public int getChildCount(Object parent) {
            if (parent.equals(root)) {
                return root.getChildCount();
            }
            for (DepartmentNode d : root.getChildren()) {
                if (parent.equals(d)) {
                    return d.getChildCount();
                }
            }
            return -1;
        }

        @Override
        public boolean isLeaf(Object node) {
            return node instanceof CategoryNode;
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {

        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            for (int i = 0; i < root.getChildCount(); i++) {
                if (getChild(parent, i).equals(child)) {
                    return i;
                }
            }
            return -1;
        }

        private void alertAll() {
            for (TreeModelListener l : listeners) {
                l.treeNodesInserted(new TreeModelEvent(this, new TreePath(root)));
            }
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

    private class RootNode implements TreeNode {

        private final List<DepartmentNode> children;

        public RootNode() {
            this.children = new LinkedList<>();
        }

        public void addNode(DepartmentNode node) {
            children.add(node);
        }

        public List<DepartmentNode> getChildren() {
            return children;
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            return children.get(childIndex);
        }

        @Override
        public int getChildCount() {
            return children.size();
        }

        @Override
        public TreeNode getParent() {
            return null;
        }

        @Override
        public int getIndex(TreeNode node) {
            return children.indexOf(node);
        }

        @Override
        public boolean getAllowsChildren() {
            return true;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public Enumeration children() {
            return new Vector(children).elements();
        }

        @Override
        public String toString() {
            return "All";
        }
    }

    private class CategoryNode implements TreeNode {

        private final Category category;
        private final DepartmentNode parent;

        public CategoryNode(DepartmentNode parent, Category c) {
            this.category = c;
            this.parent = parent;
        }

        public Category getCategory() {
            return category;
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            return null;
        }

        @Override
        public int getChildCount() {
            return 0;
        }

        @Override
        public TreeNode getParent() {
            return parent;
        }

        @Override
        public int getIndex(TreeNode node) {
            return -1;
        }

        @Override
        public boolean getAllowsChildren() {
            return false;
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public Enumeration children() {
            return null;
        }

        @Override
        public String toString() {
            return category.getName();
        }
    }

    private class DepartmentNode implements TreeNode {

        private final Department department;
        private final TreeNode parent;

        private final List<CategoryNode> children;

        public DepartmentNode(TreeNode parent, Department d) {
            this.department = d;
            this.parent = parent;
            children = new LinkedList<>();
        }

        public Department getDepartment() {
            return department;
        }

        public List<CategoryNode> getChildren() {
            return children;
        }

        public void addCategory(CategoryNode cn) {
            children.add(cn);
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            return children.get(childIndex);
        }

        @Override
        public int getChildCount() {
            return children.size();
        }

        @Override
        public TreeNode getParent() {
            return parent;
        }

        @Override
        public int getIndex(TreeNode node) {
            return children.indexOf(node);
        }

        @Override
        public boolean getAllowsChildren() {
            return true;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public Enumeration children() {
            return new Vector(children).elements();
        }

        @Override
        public String toString() {
            return department.getName();
        }
    }

    private void filterCategory(Category c) {
        currentTableContents.clear();
        for (Product p : allProducts) {
            if (p.getCategory().equals(c)) {
                currentTableContents.add(p);
            }
        }
        setTable();
    }

    private void filterDepartment(Department d) {
        currentTableContents.clear();
        for (Product p : allProducts) {
            if (p.getCategory().getDepartment().equals(d)) {
                currentTableContents.add(p);
            }
        }
        setTable();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchButtonGroup = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        btnClose = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        radName = new javax.swing.JRadioButton();
        radBarcode = new javax.swing.JRadioButton();
        btnSelect = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();

        setTitle("Select Product - All");

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table.getTableHeader().setReorderingAllowed(false);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setMinWidth(40);
            table.getColumnModel().getColumn(0).setPreferredWidth(40);
            table.getColumnModel().getColumn(0).setMaxWidth(40);
            table.getColumnModel().getColumn(1).setResizable(false);
        }

        btnClose.setText("Cancel");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        jLabel1.setText("Search:");

        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        searchButtonGroup.add(radName);
        radName.setText("Name");
        radName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radNameActionPerformed(evt);
            }
        });

        searchButtonGroup.add(radBarcode);
        radBarcode.setSelected(true);
        radBarcode.setText("Barcode");
        radBarcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radBarcodeActionPerformed(evt);
            }
        });

        btnSelect.setText("Select");
        btnSelect.setEnabled(false);
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });

        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                treeMousePressed(evt);
            }
        });
        jScrollPane2.setViewportView(tree);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radBarcode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 184, Short.MAX_VALUE)
                        .addComponent(btnSelect)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(jLabel1)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)
                    .addComponent(radName)
                    .addComponent(radBarcode)
                    .addComponent(btnSelect))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        product = null;
        closedFlag = true;
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void tableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMousePressed
        product = currentTableContents.get(table.getSelectedRow());
        btnSelect.setEnabled(true);
        if (evt.getClickCount() == 2) {
            closedFlag = true;
            this.setVisible(false);
        }
    }//GEN-LAST:event_tableMousePressed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String search = txtSearch.getText();

        if (search.length() == 0) {
            return;
        }
        List<Product> newList = new ArrayList<>();

        for (Product p : currentTableContents) {
            if (!showOpen && p.isOpen()) {
                continue;
            }
            if (radName.isSelected()) {
                if (p.getLongName().toLowerCase().contains(search.toLowerCase())) {
                    newList.add(p);
                }
            } else {
                if (!Utilities.isNumber(search)) {
                    JOptionPane.showMessageDialog(this, "Barcode must be a number", "Search", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (p.getBarcode().equals(search)) {
                    newList.add(p);
                }
            }
        }
        if (newList.isEmpty()) {
            txtSearch.setSelectionStart(0);
            txtSearch.setSelectionEnd(txtSearch.getText().length());
            JOptionPane.showMessageDialog(this, "No Results", "Search", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        currentTableContents = newList;
        updateTable();
        if (currentTableContents.size() == 1) {
            product = currentTableContents.get(0);
            this.setVisible(false);
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        btnSearch.doClick();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void radNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radNameActionPerformed
        txtSearch.requestFocus();
    }//GEN-LAST:event_radNameActionPerformed

    private void radBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radBarcodeActionPerformed
        txtSearch.requestFocus();
    }//GEN-LAST:event_radBarcodeActionPerformed

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        closedFlag = true;
        setVisible(false);
    }//GEN-LAST:event_btnSelectActionPerformed

    private void treeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMouseClicked

    }//GEN-LAST:event_treeMouseClicked

    private void treeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMousePressed
        TreePath path = tree.getSelectionModel().getSelectionPath();
        if (path == null) {
            return;
        }
        TreeNode node = (TreeNode) path.getLastPathComponent();
        if (path.getPathCount() == 3) { //Category
            TreeNode par = (TreeNode) path.getPathComponent(path.getPathCount() - 2);
            setTitle("Select Product - " + par.toString() + " - " + node.toString());
            CategoryNode catNode = (CategoryNode) node;
            filterCategory(catNode.getCategory());
        } else if (path.getPathCount() == 2) { //Department
            setTitle("Select Product - " + node.toString());
            DepartmentNode depNode = (DepartmentNode) node;
            filterDepartment(depNode.getDepartment());
        } else { //All
            setTitle("Select Product - All");
            showAllProducts();
        }
    }//GEN-LAST:event_treeMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSelect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JRadioButton radBarcode;
    private javax.swing.JRadioButton radName;
    private javax.swing.ButtonGroup searchButtonGroup;
    private javax.swing.JTable table;
    private javax.swing.JTree tree;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
