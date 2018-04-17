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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
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

    private static Object lastSelection;

    private final DataConnect dc;

    private MyTableModel model;

    protected boolean closedFlag;

    private MyTreeModel treeModel;

    private final Supplier supplier;
    private final String suppString;

    /**
     * Creates new form ProductSelectDialog
     *
     * @param parent the parent window.
     * @param supplier the supplier to show.
     */
    public ProductSelectDialog(Window parent, Supplier supplier) {
        super(parent);
        this.dc = GUI.gui.dc;
        this.supplier = supplier;
        if(supplier == null){
            suppString = "";
        } else{
            suppString = " for supplier " + supplier.getName();
        }
        closedFlag = false;
        initComponents();
        this.setIconImage(GUI.icon);
        setLocationRelativeTo(parent);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        final ModalDialog mDialog = new ModalDialog(this, "Retrieving");
        final Runnable run = () -> {
            try {
                setTable();
                initTable();
                if (table.getRowCount() > 0) {
                    table.getSelectionModel().setSelectionInterval(0, 0);
                }
                try {
                    init();
                } catch (IOException | SQLException ex) {
                    mDialog.hide();
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
        selectNode(lastSelection);
    }

    private void selectNode(Object o) {
        RootNode root = (RootNode) treeModel.getRoot();
        if (o == null) {
            model.showAll();
            tree.setSelectionRow(0);
        } else if (o instanceof Department) {
            model.filterDepartment((Department) lastSelection);
            for (int i = 0; i < root.getChildCount(); i++) {
                DepartmentNode node = (DepartmentNode) root.getChildAt(i);
                if (node.getDepartment().equals(o)) {
                    //Department Found - Code goes here
                }
            }
        } else {
            model.filterCategory((Category) lastSelection);
            for (int i = 0; i < root.getChildCount(); i++) {
                DepartmentNode dNode = (DepartmentNode) root.getChildAt(i);
                for (int j = 0; j < dNode.getChildCount(); j++) {
                    CategoryNode cNode = (CategoryNode) dNode.getChildAt(j);
                    if (cNode.getCategory().equals(o)) {
                        //Category Found - Code goes here
                    }
                }
            }
        }
    }

    private static final String solve = "Solve";

    private void initTable() {
        table.setSelectionModel(new ForcedListSelectionModel());
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, solve);
        table.getActionMap().put(solve, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnSelect.doClick();
            }
        });
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
        return showDialog(parent, null);
    }

    /**
     * Method to show the product select dialog.
     *
     * @param parent the parent component.
     * @param supplier the supplier to show.
     * @return the product selected by the user.
     */
    public static Product showDialog(Component parent, Supplier supplier) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        dialog = new ProductSelectDialog(window, supplier);
        product = null;
        dialog.setVisible(true);
        return product;
    }

    private void setTable() throws IOException, SQLException {
        List<Product> all;
        if (supplier == null) {
            all = dc.getAllProducts();
        } else {
            all = dc.getProductsInSupplier(supplier);
        }
        Product allProducts[] = new Product[]{};
        allProducts = all.toArray(allProducts);
        model = new MyTableModel(allProducts);
        table.setModel(model);
        table.getColumnModel().getColumn(1).setMaxWidth(120);
        table.getColumnModel().getColumn(1).setMinWidth(120);
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
            setTitle("Select Product - All" + suppString);
        }

        public void filterCategory(Category c) {
            products.clear();
            for (Product p : allProducts) {
                if (p.getCategory().equals(c)) {
                    products.add(p);
                }
            }
            setTitle("Select Product - " + c.getDepartment().getName() + " - " + c.getName() + suppString);
            alertAll();
        }

        public void filterDepartment(Department d) {
            products.clear();
            for (Product p : allProducts) {
                if (p.getDepartment().equals(d)) {
                    products.add(p);
                }
            }
            setTitle("Select Product - " + d.getName() + suppString);
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
                if (p.getDepartment().getName().equalsIgnoreCase(d)) {
                    dep = p.getDepartment();
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

        public Product[] getAllProducts() {
            return allProducts;
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
            return 2;
        }

        @Override
        public String getColumnName(int i) {
            switch (i) {
                case 0:
                    return "Name";
                case 1:
                    return "Barcode";
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int i) {
            switch (i) {
                case 0:
                    return String.class;
                case 1:
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
                    return p.getLongName();
                case 1:
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
        scroll = new javax.swing.JScrollPane();
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
        btnRefresh = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Select Products - All" + suppString);

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
        table.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                tableMouseWheelMoved(evt);
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableMousePressed(evt);
            }
        });
        scroll.setViewportView(table);
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
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
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

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

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
                        .addComponent(scroll, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRefresh)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
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
                    .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
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
                    .addComponent(radOrder)
                    .addComponent(btnRefresh))
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
            int row = table.getSelectedRow();
            if (row == -1) {
                return;
            }
            product = model.getSelected();
            closedFlag = true;
            setVisible(false);
            return;
        }
        List<Product> newList = new ArrayList<>();

        for (Product p : model.getAllProducts()) {
            if (radName.isSelected()) {
                if (p.getLongName().toLowerCase().contains(search.toLowerCase())) {
                    newList.add(p);
                }
            } else if (radOrder.isSelected()) {
                if (!Utilities.isNumber(search)) {
                    JOptionPane.showMessageDialog(this, "Order code must be a number", "Search", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (p.getOrderCode().equals(search)) {
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
                    JOptionPane.showMessageDialog(this, "Barcodes must be 1-8, 12, 13, or 14 digits long", "Search", JOptionPane.ERROR_MESSAGE);
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
            lastSelection = catNode.getCategory();
        } else if (path.getPathCount() == 2) { //Department
            setTitle("Select Product - " + node.toString());
            DepartmentNode depNode = (DepartmentNode) node;
            model.filterDepartment(depNode.getDepartment());
            lastSelection = depNode.getDepartment();
        } else { //All
            setTitle("Select Product - All");
            model.showAll();
            lastSelection = null;
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

            if (txtSearch.getText().isEmpty()) {
                search.setEnabled(false);
            }

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

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        final ModalDialog mDialog = new ModalDialog(this, "Refreshing");
        final Runnable run = new Runnable() {
            @Override
            public void run() {
                try {
                    setTable();
                    setTitle("Select Product - All");
                    model.showAll();
                    tree.setSelectionRow(0);
                } catch (IOException | SQLException ex) {
                    mDialog.hide();
                    JOptionPane.showMessageDialog(ProductSelectDialog.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    mDialog.hide();
                }
            }
        };
        final Thread thread = new Thread(run, "Refresh");
        thread.start();
        mDialog.show();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void tableMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_tableMouseWheelMoved
        int row = table.getSelectedRow();
        if (evt.getWheelRotation() < 0) { //Up
            if (row == 0) {
                return;
            }
            table.getSelectionModel().setSelectionInterval(row - 1, row - 1);
        } else if (evt.getWheelRotation() > 0) { //Down
            if (table.getRowCount() - 1 == row) {
                return;
            }
            table.getSelectionModel().setSelectionInterval(row + 1, row + 1);
        }
    }//GEN-LAST:event_tableMouseWheelMoved

    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased
        int row = table.getSelectedRow();
        if (evt.getKeyCode() == KeyEvent.VK_UP) { //Up
            if (row == 0) {
                return;
            }
            table.getSelectionModel().setSelectionInterval(row - 1, row - 1);
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) { //Down
            if (table.getRowCount() - 1 == row) {
                return;
            }
            table.getSelectionModel().setSelectionInterval(row + 1, row + 1);
        }
    }//GEN-LAST:event_txtSearchKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSelect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JRadioButton radBarcode;
    private javax.swing.JRadioButton radName;
    private javax.swing.JRadioButton radOrder;
    private javax.swing.JScrollPane scroll;
    private javax.swing.ButtonGroup searchButtonGroup;
    private javax.swing.JTable table;
    private javax.swing.JTree tree;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
