/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author David
 */
public class TillWindow extends javax.swing.JInternalFrame {

    private static TillWindow window;

    private final JTill jtill;
    private MyModel model;

    /**
     * Creates new form TillWindow
     */
    public TillWindow(JTill jtill) {
        this.jtill = jtill;
        initComponents();
        super.setClosable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        getAllTills();
        table.setModel(model);
        init();
    }

    public static void showWindow(JTill jtill) {
        if (window == null || window.isClosed()) {
            window = new TillWindow(jtill);
            GUI.gui.internal.add(window);
        }
        update();
        window.setVisible(true);
        try {
            window.setSelected(true);
            window.setIcon(false);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(TillWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void init() {
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(0).setMinWidth(40);
        table.getColumnModel().getColumn(2).setMaxWidth(100);
        table.getColumnModel().getColumn(2).setMinWidth(100);
        table.setSelectionModel(new ForcedListSelectionModel());
        InputMap im = table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = table.getActionMap();

        KeyStroke deleteKey = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);

        im.put(deleteKey, "Action.delete");
        am.put("Action.delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                final int index = table.getSelectedRow();
                if (index == -1) {
                    return;
                }
                final Till t = model.get(index);
                removeTill(t);
            }
        });
    }

    private static void update() {
        window.getAllTills();
    }

    private void getAllTills() {
        try {
            model = new MyModel(Till.getAll());
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading form", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xReport(Till t) {
        try {
            String strVal = JOptionPane.showInputDialog(this, "Enter value of money counted", "Cash up till " + t.getName(), JOptionPane.PLAIN_MESSAGE);
            if (strVal == null) {
                return;
            }
            if (strVal.equals("")) {
                JOptionPane.showMessageDialog(this, "You must enter a value", "Cash up till", JOptionPane.ERROR_MESSAGE);
            }
            if (!Utilities.isNumber(strVal)) {
                JOptionPane.showInputDialog(this, "You must enter a number greater than zero", "Cash up till " + t.getName(), JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal declared = new BigDecimal(strVal);

            final TillReport report = jtill.getDataConnection().xReport(t, declared, GUI.staff);
            TillReportDialog.showDialog(report);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void zReport(Till t) {
        try {
            String strVal = JOptionPane.showInputDialog(this, "Enter value of money counted", "Cash up till " + t.getName(), JOptionPane.PLAIN_MESSAGE);
            if (strVal == null) {
                return;
            }
            if (strVal.equals("")) {
                JOptionPane.showMessageDialog(this, "You must enter a value", "Cash up till", JOptionPane.ERROR_MESSAGE);
            }
            if (!Utilities.isNumber(strVal)) {
                JOptionPane.showInputDialog(this, "You must enter a number greater than zero", "Cash up till " + t.getName(), JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal declared = new BigDecimal(strVal);

            final TillReport report = jtill.getDataConnection().zReport(t, declared, GUI.staff);
            TillReportDialog.showDialog(report);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isTerminalOnline(Till t) throws IOException {
        List<Till> tills = jtill.getDataConnection().getConnectedTills();
        for (Till till : tills) {
            if (till.getId() == t.getId()) {
                return true;
            }
        }
        return false;
    }

    private void removeTill(Till t) {
        try {
            if (isTerminalOnline(t)) {
                JOptionPane.showMessageDialog(this, "This terminal is currently online. The terminal must be disconnected before it can be removed.", "Remove terminal " + t.getName(), JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!jtill.getDataConnection().getUncachedTillSales(t.getId()).isEmpty()) {
                JOptionPane.showMessageDialog(this, "This terminal has uncashed transactions, these must be cashed before this temrinal can be removed", "Remove terminal " + t.getName(), JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this terminal? This will also remove any sales data associated with this terminal along with declaration reports?", "Remove terminal " + t.getName(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                jtill.getDataConnection().removeTill(t.getId());
                update();
                JOptionPane.showMessageDialog(this, "Terminal " + t.getName() + " has been removed from the system", "Remove Terminal", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException | JTillException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Remove Terminal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class MyModel implements TableModel {

        private final List<Till> tills;
        private final List<TableModelListener> listeners;

        public MyModel(List<Till> tills) {
            this.tills = tills;
            this.listeners = new LinkedList<>();
        }

        public void removeTill(Till t) {
            tills.remove(t);
            alertAll();
        }

        public Till get(int i) {
            return tills.get(i);
        }

        public List<Till> getAll() {
            return tills;
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
        public String getColumnName(int i) {
            switch (i) {
                case 0: {
                    return "ID";
                }
                case 1: {
                    return "Terminal Name";
                }
                case 2: {
                    return "Status";
                }
                default: {
                    return "";
                }
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return Object.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Till till = tills.get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return till.getId();
                }
                case 1: {
                    return till.getName();
                }
                case 2: {
                    return (till.isConnected() ? "Online" : "Offline");
                }
                default: {
                    return "";
                }
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
        btnView = new javax.swing.JButton();
        btnSendData = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnBuildUpdates = new javax.swing.JButton();
        btnZ = new javax.swing.JButton();

        setTitle("Tills");

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Terminal Name", "Status"
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
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setMinWidth(40);
            table.getColumnModel().getColumn(0).setPreferredWidth(40);
            table.getColumnModel().getColumn(0).setMaxWidth(40);
            table.getColumnModel().getColumn(1).setResizable(false);
            table.getColumnModel().getColumn(2).setMinWidth(100);
            table.getColumnModel().getColumn(2).setPreferredWidth(100);
            table.getColumnModel().getColumn(2).setMaxWidth(100);
        }

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnView.setText("View");
        btnView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewActionPerformed(evt);
            }
        });

        btnSendData.setText("Send Data to Tills");
        btnSendData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendDataActionPerformed(evt);
            }
        });

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        btnBuildUpdates.setText("Send Build Updates");
        btnBuildUpdates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuildUpdatesActionPerformed(evt);
            }
        });

        btnZ.setText("Z Report");
        btnZ.setEnabled(false);
        btnZ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnView)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSendData)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRefresh)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBuildUpdates)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnZ)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnView)
                    .addComponent(btnSendData)
                    .addComponent(btnRefresh)
                    .addComponent(btnBuildUpdates)
                    .addComponent(btnZ))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewActionPerformed
        int index = table.getSelectedRow();
        if (index == -1) {
            return;
        }
        Till t = model.get(index);
        TillDialog.showDialog(jtill, t);
    }//GEN-LAST:event_btnViewActionPerformed

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        if (SwingUtilities.isLeftMouseButton(evt)) {
            if (evt.getClickCount() == 2) {
                int index = table.getSelectedRow();
                if (index == -1) {
                    return;
                }
                Till t = model.get(index);
                TillDialog.showDialog(jtill, t);
            }
        } else if (SwingUtilities.isRightMouseButton(evt)) {
            int index = table.getSelectedRow();
            if (index == -1) {
                return;
            }
            Till t = model.get(index);
            JPopupMenu menu = new JPopupMenu();

            JMenuItem view = new JMenuItem("View");
            final Font boldFont = new Font(view.getFont().getFontName(), Font.BOLD, view.getFont().getSize());
            view.setFont(boldFont);
            view.addActionListener((ActionEvent e) -> {
                TillDialog.showDialog(jtill, t);
            });

            JMenuItem sendData = new JMenuItem("Send Data");
            sendData.addActionListener((ActionEvent e) -> {
                try {
                    jtill.getDataConnection().sendData(t.getId(), null);
                    return;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
                JOptionPane.showMessageDialog(this, "Till offline", "Send Data", JOptionPane.WARNING_MESSAGE);
            });

            JMenuItem xReport = new JMenuItem("X Report");
            xReport.addActionListener((ActionEvent e) -> {
                xReport(t);
            });

            JMenuItem zReport = new JMenuItem("Z Report");
            zReport.addActionListener((ActionEvent e) -> {
                zReport(t);
            });

            JMenuItem remove = new JMenuItem("Remove");
            remove.addActionListener((ActionEvent e) -> {
                removeTill(t);
            });

            if (!t.isConnected()) {
                sendData.setEnabled(false);
            }

            menu.add(view);
            menu.addSeparator();
            menu.add(sendData);
            menu.add(xReport);
            menu.add(zReport);
            menu.addSeparator();
            menu.add(remove);
            menu.show(table, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tableMouseClicked

    private void btnSendDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendDataActionPerformed
        try {
            jtill.getDataConnection().reinitialiseAllTills();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (JTillException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Active inits", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSendDataActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        this.getAllTills();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnBuildUpdatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuildUpdatesActionPerformed
        try {
            jtill.getDataConnection().sendBuildUpdates();
        } catch (IOException | SQLException ex) {
            JOptionPane.showConfirmDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showConfirmDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnBuildUpdatesActionPerformed

    private void btnZActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnZActionPerformed
        if (JOptionPane.showConfirmDialog(this, "This will take a report for all tills and reset the session, continue?", "Z Report", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

        }
    }//GEN-LAST:event_btnZActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuildUpdates;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSendData;
    private javax.swing.JButton btnView;
    private javax.swing.JButton btnZ;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
