/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.Utilities;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterJob;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * Class which shows a modal dialog that can be used for showing progress.
 *
 * @author David
 */
public class ModalDialog {

    private JDialog dialog; //The dialog.
    private Component parent; //The parent component.
    private JPanel panel; //The panel for the components.

    private final String title; //The title of the window.
    private PrinterJob job; //The printer job (if any) associated with this message.
    private boolean hidden;

    private boolean modal;

    /**
     * Constructor which creates the dialog.
     *
     * @param parent the parent component.
     * @param title the title.
     */
    public ModalDialog(Component parent, String title) {
        this.title = title;
        this.hidden = false;
        this.parent = null;
        this.parent = parent;
        this.modal = true;
        init();
    }

    /**
     * Constructor which created the dialog. This constructor assigns a
     * <code>PrinterJob</code> to the dialog which can be cancelled by clicking
     * the cancel button.
     *
     * @param parent the parent component.
     * @param title the title.
     * @param job the PrinterJob to assign.
     */
    public ModalDialog(Component parent, String title, PrinterJob job) {
        this(parent, title);
        this.job = job;
        this.hidden = false;
        final JButton button = new JButton("Cancel");
        button.addActionListener((ActionEvent e) -> {
            this.job.cancel();
        });
        panel.add(button);
//        dialog.setMinimumSize(new Dimension(100, 100));
        dialog.setResizable(false);
    }

    private void init() {
        panel = new ModalPanel();
        final Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        panel.setBorder(padding);
        dialog = new JDialog(Utilities.getParentWindow(parent));
        dialog.setAlwaysOnTop(true);
        dialog.setResizable(false);
        dialog.setTitle(title);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setModal(modal);
        dialog.setIconImage(GUI.icon);
    }

    /**
     * Method to show the dialog.
     */
    public void show() {
        if (!hidden) {
            dialog.setVisible(true);
        }
    }

    /**
     * Method to hide the dialog.
     */
    public void hide() {
        this.hidden = true;
        dialog.setVisible(false);
    }

}
