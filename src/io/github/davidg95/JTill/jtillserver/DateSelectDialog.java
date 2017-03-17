/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.jdatepicker.*;

/**
 *
 * @author David
 */
public class DateSelectDialog extends javax.swing.JDialog {
    
    private static DateSelectDialog dialog;
    
    private JDatePicker picker;
    private static Date date;

    /**
     * Creates new form DateSelectDialog
     */
    public DateSelectDialog(Window parent) {
        super(parent);
        picker = new JDateComponentFactory().createJDatePicker();
        setModal(true);
        setTitle("Select Date");
        setLocationRelativeTo(parent);
        JPanel pan = new JPanel();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        picker.setDoubleClickAction(true);
        picker.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                date = new Date();
                DateModel model = picker.getModel();
                Calendar c = Calendar.getInstance();
                c.set(model.getYear(), (model.getMonth()), model.getDay());
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                date.setTime(c.getTimeInMillis());
                dialog.setVisible(false);
            }

        });
        pan.add((JComponent) picker);
        add(pan);
        pack();
        initComponents();
    }
    
    public static Date showDialog(Component parent){
        Window window = null;
        if(parent instanceof Dialog || parent instanceof Frame){
            window = (Window) parent;
        }
        dialog = new DateSelectDialog(window);
        date = null;
        dialog.setVisible(true);
        return date;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
