/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Dialog which allows a product to be selected.
 *
 * @author David
 */
public class ProductSelectDialog extends javax.swing.JDialog {

    private static ProductSelectDialog dialog;

    private static Product product;

    private final DataConnect dc;

    private MyTableModel model;

    protected boolean closedFlag;

    private MyTreeModel treeModel;

    private final String filter;

    /**
     * Creates new form ProductSelectDialog
     *
     * @param parent the parent window.
     * @param filter a filter to apply.
     */
    public ProductSelectDialog(Window parent, String filter) {
        super(parent);
        this.dc = GUI.gui.dc;
        this.filter = filter;
        closedFlag = false;
        initComponents();
        this.setIconImage(GUI.icon);
        setLocationRelativeTo(parent);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        final ModalDialog mDialog = new ModalDialog(this, "Retrieving", "Retrieving...");
        final Runnable run = () -> {
            try {
                setTable();
                initTable();
                checkFilter();
                try {
                    init();
                } catch (IOException | SQLException ex) {
                    JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException | SQLException ex) {
                mDialog.hide();
                showError(ex);
            } finally {
                mDialog.hide();
            }
        };
        final Thread thread = new Thread(run, "GET_PRODUCTS");
        thread.start();
        mDialog.show();
        txtSearch.requestFocus();
        tree.setModel(treeModel);
        tree.setCellRenderer(new MyTreeCellRenderer());
    }

    private void checkFilter() {
        if (filter.isEmpty()) {
            return;
        }
        String[] params = filter.split(",");
        if (params.length == 0) {
            return;
        }
        for (String p : params) {
            String[] sPams = p.split(" ");
            if (sPams.length == 0) {
                return;
            }
            if (p.charAt(p.indexOf('-') + 1) == 'd') {
                Department d = model.filterDepartment(sPams[1]);
                setTitle("Select Product - " + d.getName());
            } else if (p.charAt(p.indexOf('-') + 1) == 'c') {
                Category c = model.filterCategory(sPams[1]);
                setTitle("Select Product - " + c.getDepartment().getName() + " - " + c.getDepartment().getName());
            }
        }
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
     * @return the product selected by the user.
     */
    public static Product showDialog(Component parent) {
        return showDialog(parent, "");
    }

    /**
     * Method to show the product select dialog.
     *
     * @param parent the parent component.
     * @param filter a filter to apply.
     * @return the product selected by the user.
     */
    public static Product showDialog(Component parent, String filter) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        dialog = new ProductSelectDialog(window, filter);
        product = null;
        dialog.setVisible(true);
        return product;
    }

    private void setTable() throws IOException, SQLException {
        List<Product> all = dc.getAllProducts();
        Product allProducts[] = new Product[]{};
        allProducts = all.toArray(allProducts);
        model = new MyTableModel(allProducts);
        table.setModel(model);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(2).setMaxWidth(120);
        table.getColumnModel().getColumn(0).setMinWidth(40);
        table.getColumnModel().getColumn(2).setMinWidth(120);
    }

    /**
     * Method to show an error.
     *
     * @param e the exception to show.
     */
    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e, "Products", JOptionPane.ERROR_MESSAGE);
    }

    private class MyTableModel implements TableModel {

        private final Product allProducts[];
        private List<Product> products;
        private final List<TableModelListener> listeners;

        public MyTableModel(Product all[]) {
            this.allProducts = all;
            this.products = new LinkedList<>();
            this.listeners = new LinkedList<>();
            showAll();
        }

        public void showAll() {
            products = new LinkedList<>(Arrays.asList(allProducts));
            alertAll();
        }

        public void filterCategory(Category c) {
            products.clear();
            for (Product p : allProducts) {
                if (p.getCategory().equals(c)) {
                    products.add(p);
                }
            }
            alertAll();
        }

        public void filterDepartment(Department d) {
            products.clear();
            for (Product p : allProducts) {
                if (p.getCategory().getDepartment().equals(d)) {
                    products.add(p);
                }
            }
            alertAll();
        }

        public Category filterCategory(String c) {
            products.clear();
            Category cat = null;
            for (Product p : allProducts) {
                if (p.getCategory().getName().equalsIgnoreCase(c)) {
                    cat = p.getCategory();
                    products.add(p);
                }
            }
            alertAll();
            return cat;
        }

        public Department filterDepartment(String d) {
            products.clear();
            Department dep = null;
            for (Product p : allProducts) {
                if (p.getCategory().getDepartment().getName().equalsIgnoreCase(d)) {
                    dep = p.getCategory().getDepartment();
                    products.add(p);
                }
            }
            alertAll();
            return dep;
        }

        public Product getSelected() {
            final int row = table.getSelectedRow();
            if (row == -1) {
                return null;
            }
            return products.get(row);
        }

        public List<Product> getCurrentProducts() {
            return products;
        }

        public void setCurrent(List<Product> products) {
            this.products = products;
            alertAll();
        }

        @Override
        public int getRowCount() {
            return products.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int i) {
            switch (i) {
                case 0:
                    return "ID";
                case 1:
                    return "Name";
                case 2:
                    return "Barcode";
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int i) {
            switch (i) {
                case 0:
                    return Object.class;
                case 1:
                    return String.class;
                case 2:
                    return String.class;
                default:
                    return Object.class;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Product p = products.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return p.getId();
                case 1:
                    return p.getLongName();
                case 2:
                    return p.getBarcode();
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        }

        private void alertAll() {
            for (TableModelListener l : listeners) {
                l.tableChanged(new TableModelEvent(this));
            }
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            listeners.add(l);
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            listeners.remove(l);
        }

    }

    private class MyTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree,
                Object value, boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            setIcon(null);
            return this;
        }
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
        radOrder = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Select Product - All");
        setResizable(false);

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name", "Barcode"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
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
            table.getColumnModel().getColumn(2).setMinWidth(120);
            table.getColumnModel().getColumn(2).setPreferredWidth(120);
            table.getColumnModel().getColumn(2).setMaxWidth(120);
        }

        btnClose.setText("Cancel");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        jLabel1.setText("Search:");

        txtSearch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtSearchMouseClicked(evt);
            }
        });
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

        tree.setModel(null);
        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                treeMousePressed(evt);
            }
        });
        jScrollPane2.setViewportView(tree);

        searchButtonGroup.add(radOrder);
        radOrder.setText("Order Code");

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
                        .addComponent(radOrder)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 103, Short.MAX_VALUE)
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
                    .addComponent(btnSelect)
                    .addComponent(radOrder))
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
        int row = table.getSelectedRow();
        if (row == -1) {
            return;
        }
        btnSelect.setEnabled(true);
        if (evt.getClickCount() == 2) {
            product = model.getSelected();
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

        for (Product p : model.getCurrentProducts()) {
            if (radName.isSelected()) {
                if (p.getLongName().toLowerCase().contains(search.toLowerCase())) {
                    newList.add(p);
                }
            } else if (radOrder.isSelected()) {
                if (!Utilities.isNumber(search)) {
                    JOptionPane.showMessageDialog(this, "Order code must be a number", "Search", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int order = Integer.parseInt(search);
                if (p.getOrder_code() == order) {
                    newList.add(p);
                }
            } else {
                if (!Utilities.isNumber(search)) {
                    txtSearch.setSelectionStart(0);
                    txtSearch.setSelectionEnd(txtSearch.getText().length());
                    JOptionPane.showMessageDialog(this, "Barcode must be a number", "Search", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!Utilities.validateBarcodeLenth(search)) {
                    txtSearch.setSelectionStart(0);
                    txtSearch.setSelectionEnd(txtSearch.getText().length());
                    JOptionPane.showMessageDialog(this, "Barcodes must be 8, 12, 13, or 14 digits long", "Search", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!Utilities.validateBarcode(search)) {
                    txtSearch.setSelectionStart(0);
                    txtSearch.setSelectionEnd(txtSearch.getText().length());
                    JOptionPane.showMessageDialog(this, "Invalid check digit", "Search", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (p.getBarcode().equals(search)) {
                    newList.add(p);
                    break;
                }
            }
        }
        if (newList.isEmpty()) {
            txtSearch.setSelectionStart(0);
            txtSearch.setSelectionEnd(txtSearch.getText().length());
            JOptionPane.showMessageDialog(this, "No Results", "Search", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        model.setCurrent(newList);
        if (model.getCurrentProducts().size() == 1) {
            product = model.getCurrentProducts().get(0);
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
        int row = table.getSelectedRow();
        if (row == -1) {
            return;
        }
        product = model.getSelected();
        closedFlag = true;
        setVisible(false);
    }//GEN-LAST:event_btnSelectActionPerformed

    private void treeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMousePressed
        TreePath path = tree.getSelectionModel().getSelectionPath();
        btnSelect.setEnabled(false);
        if (path == null) {
            return;
        }
        TreeNode node = (TreeNode) path.getLastPathComponent();
        if (path.getPathCount() == 3) { //Category
            TreeNode par = (TreeNode) path.getPathComponent(path.getPathCount() - 2);
            setTitle("Select Product - " + par.toString() + " - " + node.toString());
            CategoryNode catNode = (CategoryNode) node;
            model.filterCategory(catNode.getCategory());
        } else if (path.getPathCount() == 2) { //Department
            setTitle("Select Product - " + node.toString());
            DepartmentNode depNode = (DepartmentNode) node;
            model.filterDepartment(depNode.getDepartment());
        } else { //All
            setTitle("Select Product - All");
            try {
                setTable();
            } catch (IOException | SQLException ex) {
                showError(ex);
            }
        }
    }//GEN-LAST:event_treeMousePressed

    private void txtSearchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtSearchMouseClicked
        if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu menu = new JPopupMenu();

            JMenuItem search = new JMenuItem("Search");
            final Font boldFont = new Font(search.getFont().getFontName(), Font.BOLD, search.getFont().getSize());
            search.setFont(boldFont);
            JMenuItem copy = new JMenuItem("Copy");
            JMenuItem paste = new JMenuItem("Paste");
            JMenuItem clear = new JMenuItem("Clear");

            search.addActionListener((event) -> {
                btnSearch.doClick();
            });

            copy.addActionListener((event) -> {
                StringSelection stringSelection = new StringSelection(txtSearch.getText());
                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                clpbrd.setContents(stringSelection, null);
            });

            copy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));

            paste.addActionListener((event) -> {
                Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable t = c.getContents(this);
                if (t == null) {
                    return;
                }
                try {
                    txtSearch.setText((String) t.getTransferData(DataFlavor.stringFlavor));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            paste.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));

            clear.addActionListener((event) -> {
                txtSearch.setText("");
            });

            menu.add(search);
            menu.addSeparator();
            menu.add(copy);
            menu.add(paste);
            menu.addSeparator();
            menu.add(clear);

            menu.show(txtSearch, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_txtSearchMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSelect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JRadioButton radBarcode;
    private javax.swing.JRadioButton radName;
    private javax.swing.JRadioButton radOrder;
    private javax.swing.ButtonGroup searchButtonGroup;
    private javax.swing.JTable table;
    private javax.swing.JTree tree;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
