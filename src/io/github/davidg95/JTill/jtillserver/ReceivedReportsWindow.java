/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.DataConnect;
import io.github.davidg95.JTill.jtill.JTill;
import io.github.davidg95.JTill.jtill.ReceivedReport;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author David
 */
public class ReceivedReportsWindow extends javax.swing.JInternalFrame {

    private static ReceivedReportsWindow window;
    
    private final JTill jtill;
    
    private MyModel model;

    /**
     * Creates new form ReceivedReports
     */
    public ReceivedReportsWindow(JTill jtill) {
        this.jtill= jtill;
        initComponents();
        setTitle("Received Reports");
        super.setClosable(true);
        super.setIconifiable(true);
        super.setMaximizable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        reloadTable();
        txtInvoiceNo.requestFocus();
    }

    public static void showWindow(JTill jtill) {
        if (window == null || window.isClosed()) {
            window = new ReceivedReportsWindow(jtill);
            GUI.gui.internal.add(window);
        }
        window.setVisible(true);
        try {
            window.setSelected(true);
            window.setIcon(false);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WasteReports.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void reloadTable() {
        try {
            List<ReceivedReport> rrs = jtill.getDataConnection().getAllReceivedReports();
            model = new MyModel(rrs);
            tblReports.setModel(model);
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(WasteReports.class.getName()).log(Level.SEVERE, null, ex);
        }
        tblReports.getColumnModel().getColumn(0).setMaxWidth(40);
        tblReports.getColumnModel().getColumn(0).setMinWidth(40);
        tblReports.getColumnModel().getColumn(3).setMaxWidth(40);
        tblReports.getColumnModel().getColumn(3).setMinWidth(40);
        tblReports.setSelectionModel(new ForcedListSelectionModel());
    }

    private class MyModel implements TableModel {

        private final List<ReceivedReport> reports;
        private final List<TableModelListener> listeners;

        public MyModel(List<ReceivedReport> reports) {
            this.reports = reports;
            this.listeners = new LinkedList<>();
        }

        public ReceivedReport getReport(int i) {
            return reports.get(i);
        }

        @Override
        public int getRowCount() {
            return reports.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0: {
                    return "ID";
                }
                case 1: {
                    return "Invoice No.";
                }
                case 2: {
                    return "Supplier";
                }
                case 3: {
                    return "Paid";
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 3) {
                return Boolean.class;
            } else {
                return Object.class;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ReceivedReport report = reports.get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return report.getId();
                }
                case 1: {
                    return report.getInvoiceId();
                }
                case 2: {
                    return report.getSupplier();
                }
                case 3: {
                    return report.isPaid();
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 3) {
                ReceivedReport report = reports.get(rowIndex);
                report.setPaid((boolean) aValue);
                try {
                    report.save();
                } catch (IOException | SQLException ex) {
                    JOptionPane.showMessageDialog(ReceivedReportsWindow.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblReports = new javax.swing.JTable();
        btnClose = new javax.swing.JButton();
        chkShowUnpaid = new javax.swing.JCheckBox();
        btnMarkPaid = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        txtInvoiceNo = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tblReports.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Invoice No.", "Supplier", "Paid"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblReports.getTableHeader().setReorderingAllowed(false);
        tblReports.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblReportsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblReports);
        if (tblReports.getColumnModel().getColumnCount() > 0) {
            tblReports.getColumnModel().getColumn(0).setResizable(false);
            tblReports.getColumnModel().getColumn(0).setPreferredWidth(40);
            tblReports.getColumnModel().getColumn(1).setResizable(false);
            tblReports.getColumnModel().getColumn(2).setResizable(false);
            tblReports.getColumnModel().getColumn(3).setResizable(false);
            tblReports.getColumnModel().getColumn(3).setPreferredWidth(40);
        }

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        chkShowUnpaid.setText("Only show unpaid");
        chkShowUnpaid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowUnpaidActionPerformed(evt);
            }
        });

        btnMarkPaid.setText("Mark Selected As Paid");
        btnMarkPaid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMarkPaidActionPerformed(evt);
            }
        });

        jLabel1.setText("Invoice No.:");

        txtInvoiceNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtInvoiceNoActionPerformed(evt);
            }
        });

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 605, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(chkShowUnpaid)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMarkPaid)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtInvoiceNo, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(chkShowUnpaid)
                    .addComponent(btnMarkPaid)
                    .addComponent(jLabel1)
                    .addComponent(txtInvoiceNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        try {
            setClosed(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(ReceivedReportsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnCloseActionPerformed

    private void chkShowUnpaidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowUnpaidActionPerformed
        reloadTable();
    }//GEN-LAST:event_chkShowUnpaidActionPerformed

    private void tblReportsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblReportsMouseClicked
        if (SwingUtilities.isLeftMouseButton(evt)) {
            if (evt.getClickCount() == 2) {
                final ReceivedReport rr = model.getReport(tblReports.getSelectedRow());
                ReceiveItemsWindow.showWindow(jtill, rr);
            }
        } else if (SwingUtilities.isRightMouseButton(evt)) {
            final ReceivedReport rr = model.getReport(tblReports.getSelectedRow());
            JPopupMenu menu = new JPopupMenu();
            JMenuItem view = new JMenuItem("View");
            final Font boldFont = new Font(view.getFont().getFontName(), Font.BOLD, view.getFont().getSize());
            view.setFont(boldFont);
            view.addActionListener((ActionEvent e) -> {
                ReceiveItemsWindow.showWindow(jtill, rr);
            });
            JMenuItem markPaid = new JMenuItem("Mark Paid");
            markPaid.addActionListener((ActionEvent e) -> {
                rr.setPaid(true);
                try {
                    jtill.getDataConnection().updateReceivedReport(rr);
                    reloadTable();
                } catch (IOException | SQLException ex) {
                    JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            if (rr.isPaid()) {
                markPaid.setEnabled(false);
            }
            menu.add(view);
            menu.add(markPaid);
            menu.show(tblReports, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tblReportsMouseClicked

    private void btnMarkPaidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMarkPaidActionPerformed
        int[] selected = tblReports.getSelectedRows();
        ReceivedReport[] reps = new ReceivedReport[selected.length];
        for (int i = 0; i < selected.length; i++) {
            reps[i] = model.getReport(selected[i]);
        }
        for (ReceivedReport rr : reps) {
            try {
                if (rr.isPaid()) {
                    JOptionPane.showMessageDialog(this, "Already Paid", "Received Report", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                rr.setPaid(true);
                jtill.getDataConnection().updateReceivedReport(rr);
            } catch (IOException | SQLException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        reloadTable();
    }//GEN-LAST:event_btnMarkPaidActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        try {
            final String number = txtInvoiceNo.getText();

            for (ReceivedReport rr : jtill.getDataConnection().getAllReceivedReports()) {
                if (rr.getInvoiceId().equals(number)) {
                    ReceiveItemsWindow.showWindow(jtill, rr);
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Invoice " + number + " not found", "Invoice", JOptionPane.WARNING_MESSAGE);
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void txtInvoiceNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtInvoiceNoActionPerformed
        btnSearch.doClick();
    }//GEN-LAST:event_txtInvoiceNoActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnMarkPaid;
    private javax.swing.JButton btnSearch;
    private javax.swing.JCheckBox chkShowUnpaid;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblReports;
    private javax.swing.JTextField txtInvoiceNo;
    // End of variables declaration//GEN-END:variables
}
