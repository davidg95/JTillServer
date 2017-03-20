/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.DataConnect;
import io.github.davidg95.JTill.jtill.Product;
import io.github.davidg95.JTill.jtill.WasteItem;
import io.github.davidg95.JTill.jtill.WasteReason;
import io.github.davidg95.JTill.jtill.WasteReport;
import java.awt.Image;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import org.jdatepicker.JDatePicker;

/**
 *
 * @author David
 */
public class WasteReports extends javax.swing.JFrame {

    private final Logger log = Logger.getGlobal();

    private static WasteReports window;

    private final DataConnect dc;
    private List<WasteReport> wasteReports;
    private DefaultTableModel model;
    private final Image icon;

    private Product p;
    private WasteReason wasteReason;
    private Date date;

    private JDatePicker picker;

    private static final int CONTAINING = 0;
    private static final int REASON = 1;
    private static final int GREATER = 2;
    private static final int LESS = 3;
    private static final int DAY = 4;

    /**
     * Creates new form WasteReports
     */
    public WasteReports(DataConnect dc, Image icon) {
        this.dc = dc;
        this.icon = icon;
        initComponents();
        setTitle("Waste Reports");
        setIconImage(icon);
        model = (DefaultTableModel) tblReports.getModel();
        tblReports.setModel(model);
        try {
            wasteReports = dc.getAllWasteReports();
            reloadTable();
        } catch (IOException | SQLException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

    public static void showWindow(DataConnect dc, Image icon) {
        window = new WasteReports(dc, icon);
        window.setVisible(true);
    }

    private void reloadTable() {
        model.setRowCount(0);
        BigDecimal val = BigDecimal.ZERO;
        for (WasteReport wr : wasteReports) {
            Object[] row = new Object[]{wr.getId(), wr.getTotalValue(), wr.getDate()};
            model.addRow(row);
            val = val.add(wr.getTotalValue());
        }
        lblValue.setText("Total Value: £" + val);
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
        jLabel1 = new javax.swing.JLabel();
        cmbSearch = new javax.swing.JComboBox<>();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnShowAll = new javax.swing.JButton();
        lblValue = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tblReports.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "Value", "Date"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblReports.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblReportsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblReports);
        if (tblReports.getColumnModel().getColumnCount() > 0) {
            tblReports.getColumnModel().getColumn(0).setResizable(false);
            tblReports.getColumnModel().getColumn(1).setResizable(false);
            tblReports.getColumnModel().getColumn(2).setResizable(false);
        }

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        jLabel1.setText("Search for reports");

        cmbSearch.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "containing item", "with reason", "with value greater than", "with value less than", "from a specific day" }));
        cmbSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSearchActionPerformed(evt);
            }
        });

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

        btnShowAll.setText("Show All");
        btnShowAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowAllActionPerformed(evt);
            }
        });

        lblValue.setText("Total Value: £0.00");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                    .addComponent(txtSearch))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblValue)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnShowAll)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(71, 71, 71)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(cmbSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnShowAll)
                    .addComponent(lblValue))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void tblReportsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblReportsMouseClicked
        int index = tblReports.getSelectedRow();
        if (evt.getClickCount() == 2) {
            if (index != -1) {
                WasteReport wr = wasteReports.get(index);
                WasteStockWindow.showWindow(dc, wr, icon);
            }
        }
    }//GEN-LAST:event_tblReportsMouseClicked

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        int reason = cmbSearch.getSelectedIndex();

        List<WasteReport> newList = new ArrayList<>();

        switch (reason) {
            case CONTAINING: {
                if (p != null) {
                    for (WasteReport wr : wasteReports) {
                        for (WasteItem wi : wr.getItems()) {
                            if (wi.getProduct().equals(p)) {
                                newList.add(wr);
                                break;
                            }
                        }
                    }
                }
                break;
            }
            case REASON: {
                if (this.wasteReason != null) {
                    for (WasteReport wr : wasteReports) {
                        for (WasteItem wi : wr.getItems()) {
                            if (wi.getReason().equals(wasteReason)) {
                                newList.add(wr);
                                break;
                            }
                        }
                    }
                }
                break;
            }
            case GREATER: {
                BigDecimal value = new BigDecimal(txtSearch.getText());
                for (WasteReport wr : wasteReports) {
                    if (value.compareTo(wr.getTotalValue()) <= 0) {
                        newList.add(wr);
                    }
                }
                break;
            }
            case LESS: {
                BigDecimal value = new BigDecimal(txtSearch.getText());
                for (WasteReport wr : wasteReports) {
                    if (value.compareTo(wr.getTotalValue()) >= 0) {
                        newList.add(wr);
                    }
                }
                break;
            }
            case DAY: {
                for (WasteReport wr : wasteReports) {
                    Date d = wr.getDate();
                    Calendar c = Calendar.getInstance();
                    c.setTime(d);
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    c.set(Calendar.MINUTE, 0);
                    c.set(Calendar.SECOND, 0);
                    c.set(Calendar.MILLISECOND, 0);
                    d = c.getTime();
                    if (d.equals(date)) {
                        newList.add(wr);
                    }
                }
                break;
            }
            default: {
                break;
            }
        }
        wasteReports = newList;
        reloadTable();
        if (wasteReports.size() == 1) {
            WasteStockWindow.showWindow(dc, wasteReports.get(0), icon);
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnShowAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowAllActionPerformed
        try {
            wasteReports = dc.getAllWasteReports();
            reloadTable();
        } catch (IOException | SQLException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnShowAllActionPerformed

    private void txtSearchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtSearchMouseClicked
        switch (cmbSearch.getSelectedIndex()) {
            case CONTAINING:
                p = (Product) JTillObjectSelectDialog.showDialog(this, dc, "Select a Product", Product.class);
                if (p != null) {
                    txtSearch.setText(p.getName());
                }
                break;
            case REASON:
                wasteReason = (WasteReason) JTillObjectSelectDialog.showDialog(jLabel1, dc, "Select a WasteReason", WasteReason.class);
                if (wasteReason != null) {
                    txtSearch.setText(wasteReason.getName());
                }
                break;
            case DAY:
                date = DateSelectDialog.showDialog(this);
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                txtSearch.setText(df.format(date));
                break;
            default:
                break;
        }
    }//GEN-LAST:event_txtSearchMouseClicked

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        btnSearch.doClick();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void cmbSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSearchActionPerformed
        txtSearch.setText("");
    }//GEN-LAST:event_cmbSearchActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnShowAll;
    private javax.swing.JComboBox<String> cmbSearch;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblValue;
    private javax.swing.JTable tblReports;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
