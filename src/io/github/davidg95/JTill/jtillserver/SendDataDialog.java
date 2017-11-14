/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

/**
 *
 * @author David
 */
public class SendDataDialog extends javax.swing.JDialog {

    private MyTillModel model;
    private final DataConnect dc;

    /**
     * Creates new form SendDataDialog
     */
    public SendDataDialog(Window parent) {
        super(parent);
        dc = GUI.gui.dc;
        initComponents();
        setModal(true);
        setLocationRelativeTo(parent);
        try {
            model = new MyTillModel(dc.getAllTills());
            init();
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void init() {
        tillTable.setSelectionModel(new ForcedListSelectionModel());
        tillTable.setModel(model);
        tillTable.getColumnModel().getColumn(0).setMaxWidth(40);
        tillTable.getColumnModel().getColumn(2).setMaxWidth(50);
    }

    public static void showDialog(Window parent) {
        SendDataDialog dialog = new SendDataDialog(parent);
        dialog.setVisible(true);
    }

    public class StatusColumnCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            //Cells are by default rendered as a JLabel.
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            if (value instanceof Boolean) {
                c = new JPanel();
                boolean v = (boolean) value;
                JCheckBox cb = new JCheckBox();
                cb.setSelected(v);
                ((JPanel) c).add(cb);
            }
            //Get the status for the current row.
            if (model.getOnline(row)) {
                c.setBackground(new Color(162, 255, 150));
//                c.setEnabled(true);
            } else {
                c.setBackground(new Color(255, 150, 150));
//                c.setEnabled(false);
            }

            //Return the JLabel which renders the cell.
            return c;

        }
    }

    private class MyTillModel implements TableModel {

        private final List<Till> tills;
        private final List<TableModelListener> listeners;

        public MyTillModel(List<Till> tills) {
            this.tills = tills;
            for (Till t : tills) {
                t.setSendData(t.isConnected());
            }
            listeners = new LinkedList<>();
        }

        public boolean getOnline(int row) {
            return tills.get(row).isConnected();
        }

        public void sendToCheckedTills(String[] data) {
            for (Till t : tills) {
                if (t.isSendData()) {
                    try {
                        t.sendData(data);
                    } catch (IOException | SQLException ex) {
                        JOptionPane.showMessageDialog(SendDataDialog.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        @Override
        public int getRowCount() {
            return tills.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "ID";
                case 1:
                    return "Name";
                case 2:
                    return "Send";
                default:
                    break;
            }
            return "";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Integer.class;
                case 1:
                    return String.class;
                case 2:
                    return Boolean.class;
                default:
                    break;
            }
            return Object.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 2 && tills.get(rowIndex).isConnected();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Till t = tills.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return t.getId();
                case 1:
                    return t.getName();
                case 2:
                    return t.isSendData();
                default:
                    break;
            }
            return "";
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Till t = tills.get(rowIndex);
            if (columnIndex == 2) {
                t.setSendData((boolean) aValue);
            }
        }

        private void alertAll() {
            for (TableModelListener l : listeners) {
                l.tableChanged(new TableModelEvent(this, 0, tills.size()));
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
        tillTable = new javax.swing.JTable();
        btnSend = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        chkSettings = new javax.swing.JCheckBox();
        chkDiscounts = new javax.swing.JCheckBox();
        chkScreens = new javax.swing.JCheckBox();
        chkBackground = new javax.swing.JCheckBox();
        chkStaff = new javax.swing.JCheckBox();
        chkProducts = new javax.swing.JCheckBox();
        chkTerminalSettings = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Send Data to Terminals");
        setResizable(false);

        tillTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tillTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tillTable);

        btnSend.setText("Send Data");
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Data to send"));

        chkSettings.setSelected(true);
        chkSettings.setText("Settings");

        chkDiscounts.setSelected(true);
        chkDiscounts.setText("Discounts");

        chkScreens.setSelected(true);
        chkScreens.setText("Screens");

        chkBackground.setSelected(true);
        chkBackground.setText("Background");

        chkStaff.setSelected(true);
        chkStaff.setText("Staff");

        chkProducts.setSelected(true);
        chkProducts.setText("Products");

        chkTerminalSettings.setSelected(true);
        chkTerminalSettings.setText("Terminal Settings");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(chkSettings)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkDiscounts)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkScreens)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkBackground))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(chkStaff)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkProducts)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkTerminalSettings)))
                .addContainerGap(65, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkSettings)
                    .addComponent(chkDiscounts)
                    .addComponent(chkScreens)
                    .addComponent(chkBackground))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkStaff)
                    .addComponent(chkProducts)
                    .addComponent(chkTerminalSettings)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSend))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSend)
                    .addComponent(btnCancel))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendActionPerformed
        List<String> data = new LinkedList<>();
        if (chkBackground.isSelected()) {
            data.add("background");
        }
        if (chkDiscounts.isSelected()) {
            data.add("discounts");
        }
        if (chkScreens.isSelected()) {
            data.add("screens");
        }
        if (chkSettings.isSelected()) {
            data.add("settings");
        }
        if (chkStaff.isSelected()) {
            data.add("staff");
        }
        if (chkProducts.isSelected()) {
            data.add("products");
        }
        if (chkTerminalSettings.isSelected()) {
            data.add("terminal");
        }
        String[] d = new String[data.size()];
        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Must select at least one item", "Send Data", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (int i = 0; i < data.size(); i++) {
            d[i] = data.get(i);
        }
        model.sendToCheckedTills(d);
        setVisible(false);
    }//GEN-LAST:event_btnSendActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSend;
    private javax.swing.JCheckBox chkBackground;
    private javax.swing.JCheckBox chkDiscounts;
    private javax.swing.JCheckBox chkProducts;
    private javax.swing.JCheckBox chkScreens;
    private javax.swing.JCheckBox chkSettings;
    private javax.swing.JCheckBox chkStaff;
    private javax.swing.JCheckBox chkTerminalSettings;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tillTable;
    // End of variables declaration//GEN-END:variables
}
