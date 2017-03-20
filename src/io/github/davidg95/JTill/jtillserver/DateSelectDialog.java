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
import org.jdatepicker.DateModel;
import org.jdatepicker.*;

/**
 *
 * @author David
 */
public class DateSelectDialog extends JDialog {

    private static DateSelectDialog dialog;
    private static Date date;

    private final JDatePicker picker;

    public DateSelectDialog(Window parent) {
        picker = new JDateComponentFactory().createJDatePicker();
        setModal(true);
        setTitle("Select Date");
        setLocationRelativeTo(parent);
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
        add((JComponent) picker);
        pack();
    }

    public static Date showDialog(Component parent) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        dialog = new DateSelectDialog(window);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        date = null;
        dialog.setVisible(true);
        return date;
    }
}
