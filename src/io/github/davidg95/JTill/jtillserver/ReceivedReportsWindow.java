/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.DataConnect;
import io.github.davidg95.JTill.jtill.JTillException;
import io.github.davidg95.JTill.jtill.ReceivedReport;
import io.github.davidg95.JTill.jtill.Supplier;
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
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class ReceivedReportsWindow extends javax.swing.JInternalFrame {

    private final Logger log = Logger.getGlobal();

    private static ReceivedReportsWindow window;

    private final DataConnect dc;
    private List<ReceivedReport> receivedReports;
    private final DefaultTableModel model;

    /**
     * Creates new form ReceivedReports
     */
    public ReceivedReportsWindow() {
        this.dc = GUI.gui.dc;
        initComponents();
        setTitle("Received Reports");
        super.setClosable(true);
        super.setIconifiable(true);
        super.setMaximizable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        model = (DefaultTableModel) tblReports.getModel();
        tblReports.setModel(model);
        reloadTable();
        txtInvoiceNo.requestFocus();
        tblReports.setSelectionModel(new ForcedListSelectionModel());
    }

    public static void showWindow() {
        if (window == null || window.isClosed()) {
            window = new ReceivedReportsWindow();
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
            List<ReceivedReport> rrs = dc.getAllReceivedReports();
            model.setRowCount(0);
            receivedReports = new LinkedList<>();
            for (ReceivedReport rr : rrs) {
                Object[] row = new Object[]{rr.getId(), rr.getInvoiceId(), rr.getSupplier().getName(), rr.isPaid()};
                if (chkShowUnpaid.isSelected() && !rr.isPaid()) {
                    model.addRow(row);
                    receivedReports.add(rr);
                }
                if (!chkShowUnpaid.isSelected()) {
                    model.addRow(row);
                    receivedReports.add(rr);
                }
            }
        } catch (IOException | SQLException ex) {
            JOptionPane.showInternalMessageDialog(GUI.gui.internal, ex, "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(WasteReports.class.getName()).log(Level.SEVERE, null, ex);
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
            tblReports.getColumnModel().getColumn(0).setMinWidth(40);
            tblReports.getColumnModel().getColumn(0).setPreferredWidth(40);
            tblReports.getColumnModel().getColumn(0).setMaxWidth(40);
            tblReports.getColumnModel().getColumn(1).setResizable(false);
            tblReports.getColumnModel().getColumn(2).setResizable(false);
            tblReports.getColumnModel().getColumn(3).setMinWidth(40);
            tblReports.getColumnModel().getColumn(3).setPreferredWidth(40);
            tblReports.getColumnModel().getColumn(3).setMaxWidth(40);
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
                final ReceivedReport rr = receivedReports.get(tblReports.getSelectedRow());
                ReceiveItemsWindow.showWindow(rr);
            }
        } else if (SwingUtilities.isRightMouseButton(evt)) {
            final ReceivedReport rr = receivedReports.get(tblReports.getSelectedRow());
            JPopupMenu menu = new JPopupMenu();
            JMenuItem view = new JMenuItem("View");
            final Font boldFont = new Font(view.getFont().getFontName(), Font.BOLD, view.getFont().getSize());
            view.setFont(boldFont);
            view.addActionListener((ActionEvent e) -> {
                ReceiveItemsWindow.showWindow(rr);
            });
            JMenuItem markPaid = new JMenuItem("Mark Paid");
            markPaid.addActionListener((ActionEvent e) -> {
                rr.setPaid(true);
                try {
                    dc.updateReceivedReport(rr);
                    reloadTable();
                } catch (IOException | SQLException ex) {
                    JOptionPane.showInternalMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
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
            reps[i] = receivedReports.get(selected[i]);
        }
        for (ReceivedReport rr : reps) {
            try {
                if (rr.isPaid()) {
                    JOptionPane.showInternalMessageDialog(this, "Already Paid", "Received Report", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                rr.setPaid(true);
                dc.updateReceivedReport(rr);
            } catch (IOException | SQLException ex) {
                JOptionPane.showInternalMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        reloadTable();
    }//GEN-LAST:event_btnMarkPaidActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        try {
            final String number = txtInvoiceNo.getText();

            for (ReceivedReport rr : dc.getAllReceivedReports()) {
                if (rr.getInvoiceId().equals(number)) {
                    ReceiveItemsWindow.showWindow(rr);
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
