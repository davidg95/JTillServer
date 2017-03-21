/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author David
 */
public class ModalDialog {
    
    private JDialog dialog;
    private JPanel panel;
    private JLabel label;
    
    private final String title;
    private String text;
    
    public ModalDialog(String title, String text){
        this.title = title;
        this.text = text;
        init();
    }
    
    private void init(){
        panel = new JPanel();
        label = new JLabel(text);
        panel.add(label);
        dialog = new JDialog();
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);
        dialog.setTitle(title);
        dialog.add(panel);
        dialog.setModal(true);
        dialog.setMinimumSize(new Dimension(100,50));
    }
    
    public void setText(String text){
        this.text = text;
        SwingUtilities.invokeLater(() -> {
            label.setText(text);
        });
    }
    
    public void show(){
        dialog.setVisible(true);
    }
    
    public void hide(){
        dialog.setVisible(false);
    }
    
}
