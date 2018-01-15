/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author David
 */
public class LabelPrintingWindow extends javax.swing.JInternalFrame {

    private final DataConnect dc;

    private final MyModel model;

    /**
     * Creates new form LabelPrintingWindow
     */
    public LabelPrintingWindow() {
        this.dc = GUI.gui.dc;
        initComponents();
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        model = new MyModel();
        table.setModel(model);
        init();
    }

    /**
     * Shows the label printing window.
     */
    public static void showWindow() {
        LabelPrintingWindow window = new LabelPrintingWindow();
        GUI.gui.internal.add(window);
        window.setVisible(true);
        try {
            window.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(LabelPrintingWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void init() {
        table.getColumnModel().getColumn(1).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setMinWidth(40);
        table.setSelectionModel(new ForcedListSelectionModel());
        this.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                GUI.gui.savedReports.put("LAB", model.getAllLabels());
            }
        }
        );
        if (GUI.gui.savedReports.containsKey(
                "LAB")) {
            model.setLabels(GUI.gui.savedReports.get("LAB"));
            GUI.gui.savedReports.remove("LAB");
        }
        InputMap im = table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = table.getActionMap();

        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);

        im.put(enterKey, "Action.enter");
        am.put("Action.enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                final int index = table.getSelectedRow();
                if (index == -1) {
                    return;
                }
                if (JOptionPane.showConfirmDialog(LabelPrintingWindow.this, "Are you sure you want to remove this item?", "Label Item", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    model.removeLabel(index);
                }
            }
        }
        );
    }

    private class MyModel implements TableModel {

        private List<Label> labels;
        private final List<TableModelListener> listeners;

        public MyModel() {
            this.labels = new LinkedList<>();
            listeners = new LinkedList<>();
        }

        public void addLabel(Product p, int amount) {
            labels.add(new Label(p, amount));
            alertAll();
        }

        public void removeLabel(int i) {
            labels.remove(i);
            alertAll();
        }

        public List<Label> getAllLabels() {
            return labels;
        }

        public void setLabels(List<Label> labels) {
            this.labels = labels;
        }

        public void clear() {
            labels.clear();
            alertAll();
        }

        public void setAmount(int index, int amount) {
            labels.get(index).amount = amount;
            alertAll();
        }

        @Override
        public int getRowCount() {
            return labels.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "Product";
                case 1:
                    return "Qty";
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return Integer.class;
                default:
                    return Object.class;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Label label = labels.get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return label.p.getLongName();
                }
                case 1: {
                    return label.amount;
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 1) {
                Label label = labels.get(rowIndex);
                label.amount = (int) aValue;
            }
            alertAll();
        }

        private void alertAll() {
            for (TableModelListener l : listeners) {
                l.tableChanged(new TableModelEvent(this));
            }
            int amount = 0;
            for (Label l : labels) {
                amount += l.amount;
            }
            lblAmount.setText("Lables to print: " + amount);
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

    /**
     * Class which models a label.
     */
    public class Label {

        private final Product p; //The product to print.
        private int amount; //The amount of the label to print.

        /**
         * Create a new label.
         *
         * @param p the product for the label.
         * @param amount the amount to print.
         */
        public Label(Product p, int amount) {
            this.p = p;
            this.amount = amount;
        }

        /**
         * Method to print the label.
         *
         * @param g the 2D graphics context.
         * @param x the x position.
         * @param y the y position.
         */
        public void print(Graphics2D g, int x, int y) {
            g.drawString(p.getName(), x + 1, y + 20); //Print the name.
            g.drawString("£" + p.getPrice(), x + 1, y + 45); //Print the price.
            g.drawString(p.getBarcode(), x + 1, y + 65); //Print the barcode.
        }

        @Override
        public String toString() {
            return "Item: " + p.getName() + " Quantity: " + amount;
        }
    }

    /**
     * Class which prints the labels.
     */
    public class LabelPrintout implements Printable {

        private final List<Label> l; //The labels to print.

        /**
         * Create a new label printout.
         *
         * @param l the labels to print.
         */
        public LabelPrintout(List<Label> l) {
            this.l = l;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            int width = 200;
            int height = 100;

            int x = 0;
            int y = 0;

            for (Label la : l) {
                for (int i = 1; i <= la.amount; i++) {
                    la.print(g2, x, y);
                    x += width;
                    if (x == width * 3) {
                        x = 0;
                        y += height;
                    }
                }
            }

            return PAGE_EXISTS;
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
        table = new javax.swing.JTable();
        btnClose = new javax.swing.JButton();
        btnPrint = new javax.swing.JButton();
        btnAddProduct = new javax.swing.JButton();
        btnCSV = new javax.swing.JButton();
        lblAmount = new javax.swing.JLabel();

        setTitle("Print Labels");

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product", "Qty."
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table.getTableHeader().setReorderingAllowed(false);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setResizable(false);
            table.getColumnModel().getColumn(1).setMinWidth(40);
            table.getColumnModel().getColumn(1).setPreferredWidth(40);
            table.getColumnModel().getColumn(1).setMaxWidth(40);
        }

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnPrint.setText("Print");
        btnPrint.setEnabled(false);
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });

        btnAddProduct.setText("Add Product");
        btnAddProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddProductActionPerformed(evt);
            }
        });

        btnCSV.setText("Add CSV File");
        btnCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCSVActionPerformed(evt);
            }
        });

        lblAmount.setText("Labels to print: 0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lblAmount)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCSV)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddProduct)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPrint)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnPrint)
                    .addComponent(btnAddProduct)
                    .addComponent(btnCSV)
                    .addComponent(lblAmount))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        try {
            setClosed(true);

        } catch (PropertyVetoException ex) {
            Logger.getLogger(LabelPrintingWindow.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnAddProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProductActionPerformed
        Product p = ProductSelectDialog.showDialog(this);

        if (p == null) {
            return;
        }

        model.addLabel(p, 1);
    }//GEN-LAST:event_btnAddProductActionPerformed

    private void btnCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCSVActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Labels File");
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                boolean errors = false;
                while (true) {
                    try {
                        String line = br.readLine();

                        if (line == null) {
                            break;
                        }

                        String[] item = line.split(",");

                        if (item.length != 2) {
                            JOptionPane.showMessageDialog(this, "File is not recognised", "Add CSV", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        Product product = dc.getProductByBarcode(item[0]);
                        int a = Integer.parseInt(item[1]);
                        model.addLabel(product, a);
                    } catch (ProductNotFoundException ex) {
                        errors = true;
                    }
                }
                if (errors) {
                    JOptionPane.showMessageDialog(this, "Not all products could be found", "Labels", JOptionPane.ERROR_MESSAGE);
                }
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "The file could not be found", "Open File", JOptionPane.ERROR_MESSAGE);
            } catch (IOException | SQLException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnCSVActionPerformed

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new LabelPrintout(model.getAllLabels()));
        boolean ok = job.printDialog();
        final ModalDialog mDialog = new ModalDialog(this, "Printing...", job);
        if (ok) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        job.print();
                        mDialog.hide();
                        JOptionPane.showMessageDialog(LabelPrintingWindow.this, "Printing complete", "Print", JOptionPane.INFORMATION_MESSAGE);
                        if (JOptionPane.showConfirmDialog(LabelPrintingWindow.this, "Do you want to clear the labels?", "Labels", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            model.clear();
                        }
                    } catch (PrinterException ex) {
                        mDialog.hide();
                        JOptionPane.showMessageDialog(LabelPrintingWindow.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        mDialog.hide();
                    }
                }
            };
            Thread th = new Thread(runnable);
            th.start();
            mDialog.show();
        }
    }//GEN-LAST:event_btnPrintActionPerformed

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        final int index = table.getSelectedRow();
        if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu m = new JPopupMenu();
            JMenuItem quantity = new JMenuItem("Change Quantity");
            JMenuItem remove = new JMenuItem("Remove Label");
            quantity.addActionListener((ActionEvent e) -> {
                String input = JOptionPane.showInputDialog(this, "Enter quantity to print", "Quantity", JOptionPane.PLAIN_MESSAGE);
                if (!Utilities.isNumber(input)) {
                    JOptionPane.showMessageDialog(this, "A number must be entered", "Quantity", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int val = Integer.parseInt(input);
                if (val > 0) {
                    model.setAmount(index, val);
                } else {
                    JOptionPane.showMessageDialog(this, "Must be a value greater than zero", "Quantity", JOptionPane.WARNING_MESSAGE);
                }
            });
            remove.addActionListener((ActionEvent e) -> {
                if (index == -1) {
                    return;
                }
                if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this item?", "Remove Label", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    model.removeLabel(index);
                }
            });
            m.add(quantity);
            m.addSeparator();
            m.add(remove);
            m.show(table, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tableMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnCSV;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnPrint;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAmount;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
