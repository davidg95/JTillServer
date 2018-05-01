/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class StaffClocking extends javax.swing.JInternalFrame {

    private Staff staff;
    private final JTill jtill;

    private List<ClockItem> currentTableContents;
    private final DefaultTableModel model;

    private double clocked;

    /**
     * Creates new form StaffClocking
     */
    public StaffClocking(JTill jtill) {
        this.jtill = jtill;
        initComponents();
        super.setClosable(true);
        super.setIconifiable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        currentTableContents = new ArrayList<>();
        model = (DefaultTableModel) table.getModel();
        table.setModel(model);
        clocked = 0;
    }

    public static void showWindow(JTill jtill) {
        StaffClocking window = new StaffClocking(jtill);
        GUI.gui.internal.add(window);
        window.setVisible(true);
        try {
            window.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(StaffClocking.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setTable() {
        model.setRowCount(0);
        for (ClockItem item : currentTableContents) {
            Object[] row;
            if (item.getType() == ClockItem.CLOCK_ON) {
                row = new Object[]{new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(item.getTime()), "ON"};
            } else {
                row = new Object[]{new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(item.getTime()), "OFF"};
            }
            model.addRow(row);
        }
    }

    private class WageReport implements Printable {

        private final List<Staff> staffList;

        public WageReport(List<Staff> staff) {
            this.staffList = staff;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            String header = "Wage Report";

            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            Font oldFont = graphics.getFont();

            int y = 60;

            g2.setFont(new Font("Arial", Font.BOLD, 20)); //Use a differnt font for the header.
            g2.drawString(header, 70, y);
            y += 30;
            g2.setFont(oldFont); //Chagne back to the old font.

            //Print report info.
            g2.drawString("Time: " + new Date(), 70, y);

            final int staff = 100;
            final int hours = 300;
            final int pay = 400;

            y += 30;

            //Print collumn headers.
            g2.drawString("Staff", staff, y);
            g2.drawString("Hours", hours, y);
            g2.drawString("Pay", pay, y);
            g2.drawLine(staff - 30, y + 10, pay + 100, y + 10);

            y += 30;

            //Print the sale items.
            for (Staff s : staffList) {
                g2.drawString(s.toString(), staff, y);
                g2.drawString(s.getHours() + "", hours, y);
                g2.drawString("£" + s.getPay(), pay, y);
                y += 30;
            }
            g2.drawLine(staff - 30, y - 20, pay + 100, y - 20);

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

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtStaff = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnAllStaff = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        calPanel = new javax.swing.JPanel();
        btnCalculate = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtWage = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtRate = new javax.swing.JTextField();
        txtHours = new javax.swing.JTextField();
        btnClose = new javax.swing.JButton();

        setTitle("Staff Hours");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Filter"));

        jLabel1.setText("Staff Member:");

        txtStaff.setEditable(false);

        btnSearch.setText("Search...");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        btnAllStaff.setText("Generate report for all staff");
        btnAllStaff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAllStaffActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtStaff)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAllStaff)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtStaff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)
                    .addComponent(btnAllStaff))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Time", "On/Off"
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
        jScrollPane1.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setResizable(false);
            table.getColumnModel().getColumn(1).setResizable(false);
        }

        btnCalculate.setText("Calculate");
        btnCalculate.setEnabled(false);
        btnCalculate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCalculateActionPerformed(evt);
            }
        });

        jLabel3.setText("Enter Hourly Rate:");
        jLabel3.setEnabled(false);

        jLabel2.setText("Total Clocked Hours:");
        jLabel2.setEnabled(false);

        txtWage.setEditable(false);
        txtWage.setEnabled(false);

        jLabel4.setText("Wage:");
        jLabel4.setEnabled(false);

        txtRate.setEnabled(false);
        txtRate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRateActionPerformed(evt);
            }
        });

        txtHours.setEditable(false);
        txtHours.setEnabled(false);

        javax.swing.GroupLayout calPanelLayout = new javax.swing.GroupLayout(calPanel);
        calPanel.setLayout(calPanelLayout);
        calPanelLayout.setHorizontalGroup(
            calPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(calPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(calPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(calPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtRate)
                    .addComponent(txtHours, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCalculate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtWage, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        calPanelLayout.setVerticalGroup(
            calPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(calPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(calPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtHours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(calPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(btnCalculate)
                    .addComponent(txtWage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap())
        );

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnClose))
                    .addComponent(calPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(calPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnClose)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        staff = StaffSelectDialog.showDialog(jtill, this);
        if (staff == null) {
            return;
        }
        txtStaff.setText(staff.toString());
        try {
            currentTableContents = jtill.getDataConnection().getAllClocks(staff.getId());
            clocked = 0;
            long on = 0;
            int last = -1;
            for (ClockItem i : currentTableContents) {
                if (i.getType() == ClockItem.CLOCK_ON) {
                    if (last != ClockItem.CLOCK_ON) {
                        last = ClockItem.CLOCK_ON;
                        on = i.getTime().getTime();
                    }
                } else {
                    if (on != 0) {
                        long off = i.getTime().getTime();
                        double duration = off - on;
                        double minutes = (duration / 1000) / 60;
                        double hours = minutes / 60;
                        clocked += hours;
                        last = ClockItem.CLOCK_OFF;
                    }
                }
            }
            BigDecimal bClocked = new BigDecimal(clocked).setScale(2, RoundingMode.HALF_UP);
            txtHours.setText(bClocked + "");
            txtRate.setText(staff.getWage() + "");
            setTable();
            for (Component comp : calPanel.getComponents()) {
                comp.setEnabled(true);
            }
        } catch (IOException | SQLException | JTillException ex) {
            Logger.getLogger(StaffClocking.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnCalculateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCalculateActionPerformed
        String val = txtRate.getText();
        double rate = staff.getWage();
        if (val.length() != 0) {
            if (!Utilities.isNumber(val)) {
                JOptionPane.showMessageDialog(this, "Must enter either a numerical value for rate or leave it blank to use the default wage", "Hours", JOptionPane.ERROR_MESSAGE);
                return;
            }
            rate = Double.parseDouble(val);
            if (rate <= 0) {
                JOptionPane.showMessageDialog(this, "Rate must be greater than zero", "Hours", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        double wage = clocked * rate;
        BigDecimal bWage = new BigDecimal(wage).setScale(2, RoundingMode.HALF_UP);
        txtWage.setText("£" + bWage);
        if (JOptionPane.showConfirmDialog(this, "Do you want to clear the current clock entries for this member of staff?", "Hours", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                jtill.getDataConnection().clearClocks(staff.getId());
                currentTableContents = new ArrayList<>();
                setTable();
                JOptionPane.showMessageDialog(this, "Hours cleared", "Hours", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | SQLException | JTillException ex) {
                JOptionPane.showMessageDialog(this, ex);
            }
        }
    }//GEN-LAST:event_btnCalculateActionPerformed

    private void txtRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRateActionPerformed
        btnCalculate.doClick();
    }//GEN-LAST:event_txtRateActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnAllStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAllStaffActionPerformed
        try {
            List<Staff> staffList = new ArrayList<>();
            for (Staff s : jtill.getDataConnection().getAllStaff()) {
                try {
                    List<ClockItem> items = jtill.getDataConnection().getAllClocks(s.getId());
                    double cl = 0;
                    long on = 0;
                    for (ClockItem i : items) {
                        if (i.getType() == ClockItem.CLOCK_ON) {
                            on = i.getTime().getTime();
                        } else {
                            if (on != 0) {
                                long off = i.getTime().getTime();
                                double duration = off - on;
                                double minutes = (duration / 1000) / 60;
                                double hours = minutes / 60;
                                cl += hours;
                            }
                        }
                    }
                    s.setHours(cl);
                    s.setPay(cl * s.getWage());
                    staffList.add(s);
                    PrinterJob job = PrinterJob.getPrinterJob();
                    job.setPrintable(new WageReport(staffList));
                    boolean ok = job.printDialog();
                    final ModalDialog mDialog = new ModalDialog(this, "Printing...", job);
                    if (ok) {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    job.print();
                                } catch (PrinterException ex) {
                                    mDialog.hide();
                                    JOptionPane.showMessageDialog(StaffClocking.this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                                } finally {
                                    mDialog.hide();
                                }
                            }
                        };
                        Thread th = new Thread(runnable);
                        th.start();
                        mDialog.show();
                        JOptionPane.showMessageDialog(this, "Printing complete", "Wage Report", JOptionPane.INFORMATION_MESSAGE);

                    }
                } catch (JTillException ex) {
                    Logger.getLogger(StaffClocking.class
                            .getName()).log(Level.SEVERE, null, ex);

                }
            }

        } catch (IOException ex) {
            Logger.getLogger(StaffClocking.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (SQLException ex) {
            Logger.getLogger(StaffClocking.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAllStaffActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAllStaff;
    private javax.swing.JButton btnCalculate;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSearch;
    private javax.swing.JPanel calPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtHours;
    private javax.swing.JTextField txtRate;
    private javax.swing.JTextField txtStaff;
    private javax.swing.JTextField txtWage;
    // End of variables declaration//GEN-END:variables
}
