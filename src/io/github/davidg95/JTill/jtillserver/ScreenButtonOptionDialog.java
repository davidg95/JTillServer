/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.Screen;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author David
 */
public class ScreenButtonOptionDialog extends JDialog {
    private static JDialog dialog;
    private static Screen screen;
    
    
    public ScreenButtonOptionDialog(Window parent, Screen screen){
        super(parent);
        init();
        setTitle(screen.getName());
        setLocationRelativeTo(parent);
        setModal(true);
    }
    
    public static Screen showDialog(Component parent, Screen s){
        Window window = null;
        if(parent instanceof Dialog || parent instanceof Frame){
            window = (Window) parent;
        }
        dialog = new ScreenButtonOptionDialog(window, s);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        screen = s;
        dialog.setVisible(true);
        return screen;
    }
    
    private void init(){
        JButton rename = new JButton();
        rename.setText("Rename button");
        rename.addActionListener((ActionEvent event) ->{
            String name = JOptionPane.showInputDialog(this, "Enter new name", "Screen Button", JOptionPane.PLAIN_MESSAGE);
            screen.setName(name);
            setVisible(false);
        });
        
        JButton remove = new JButton();
        remove.setText("Remove");
        remove.addActionListener((ActionEvent event) ->{
            screen = null;
            setVisible(false);
        });
        
        JButton cancel = new JButton();
        cancel.setText("Cancel");
        cancel.addActionListener((ActionEvent event) ->{
            setVisible(false);
        });
        
        JPanel panel = new JPanel();
        panel.add(rename);
        panel.add(remove);
        panel.add(cancel);
        
        add(panel);
        pack();
    }
}
