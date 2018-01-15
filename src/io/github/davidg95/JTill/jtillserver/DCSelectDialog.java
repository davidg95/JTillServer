/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.Category;
import io.github.davidg95.JTill.jtill.DataConnect;
import io.github.davidg95.JTill.jtill.Department;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author David
 */
public class DCSelectDialog extends javax.swing.JDialog {

    private static DCSelectDialog dialog;

    private static Object object;

    private MyTreeModel model;

    private DataConnect dc;

    public static final int CATEGORY_SELECT = 1;
    public static final int DEPARTMENT_SELECT = 2;
    public static final int ANY_SELECT = 3;

    private final int mode;

    /**
     * Creates new form DCSelectDialog
     *
     * @param parent
     * @param mode
     */
    public DCSelectDialog(Window parent, int mode) {
        super(parent);
        dc = DataConnect.dataconnect;
        this.mode = mode;
        try {
            initComponents();
            setModal(true);
            setLocationRelativeTo(parent);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            if (mode == 1) {
                setTitle("Select a Category");
            } else if (mode == 2) {
                setTitle("Select a Department");
            } else if (mode == 3) {
                setTitle("Select a Department or Category");
            }
            init();
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void init() throws IOException, SQLException {
        RootNode top = new RootNode();
        model = new MyTreeModel(top);
        createNodes();
        tree.setModel(model);
        tree.setCellRenderer(new MyTreeCellRenderer());
    }

    private void createNodes() throws IOException, SQLException {
        List<Department> departments = dc.getAllDepartments();
        for (Department d : departments) {
            model.insertDepartment(d);
            if (mode != DEPARTMENT_SELECT) {
                List<Category> categorys = dc.getCategoriesInDepartment(d.getId());
                for (Category c : categorys) {
                    model.insertCategory(c);
                }
            }
        }
    }

    public static Object showDialog(Component parent, int mode) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        dialog = new DCSelectDialog(window, mode);
        object = null;
        dialog.setVisible(true);
        return object;
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
            if (mode == DEPARTMENT_SELECT) {
                return node instanceof DepartmentNode;
            }
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
            if (mode == DEPARTMENT_SELECT) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public boolean isLeaf() {
            if (mode == DEPARTMENT_SELECT) {
                return true;
            } else {
                return false;
            }
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

        jScrollPane1 = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        btnClose = new javax.swing.JButton();
        btnSelect = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                treeMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(tree);

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnSelect.setText("Select");
        btnSelect.setEnabled(false);
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnClose)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 166, Short.MAX_VALUE)
                        .addComponent(btnSelect)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnSelect))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        object = null;
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnSelectActionPerformed

    private void treeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMousePressed
        TreePath path = tree.getSelectionModel().getSelectionPath();
        if (path == null) {
            return;
        }
        TreeNode node = (TreeNode) path.getLastPathComponent();
        if (path.getPathCount() == 3) { //Category
            if (mode == 2) {
                return;
            }
            CategoryNode catNode = (CategoryNode) node;
            object = catNode.getCategory();
        } else if (path.getPathCount() == 2) { //Department
            if (mode == 1) {
                return;
            }
            DepartmentNode depNode = (DepartmentNode) node;
            object = depNode.getDepartment();
        } else { //All
            if (mode == 3) {
                object = "All";
            } else {
                return;
            }
        }
        btnSelect.setEnabled(true);
        if (evt.getClickCount() == 2) {
            setVisible(false);
        }
    }//GEN-LAST:event_treeMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSelect;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree tree;
    // End of variables declaration//GEN-END:variables
}
