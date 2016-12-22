/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

/**
 *
 * @author 1301480
 */
public class TillSplashScreen extends JWindow {

    private static JWindow window;
    private static JLabel prgLabel;
    
    public TillSplashScreen() {
        JLabel label = new JLabel();
        prgLabel = new JLabel();
        prgLabel.setText("Starting JTill Server...");
        label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/splashIcon.png")));
        JPanel pan = new JPanel();
        pan.add(label);
        pan.add(prgLabel);
        add(pan);
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        setBounds(500, 150, 500, 320);
        setLocationRelativeTo(null);
    }

    public static void showSplashScreen() {
        window = new TillSplashScreen();
        window.setVisible(true);
    }
    
    public static void hideSplashScreen(){
        window.setVisible(false);
    }
    
    public static void setLabel(String text){
        prgLabel.setText(text);
    }

}
