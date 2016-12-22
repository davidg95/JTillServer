/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.ButtonFunction;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 *
 * @author 1301480
 */
public class CustomButton extends JButton{
    
    private ButtonFunction function;
    private JFrame parent;
    
    public CustomButton(){
        super();
    }
    
    public CustomButton(JFrame parent, String name, ButtonFunction function){
        super(name);
        this.parent = parent;
        this.function = function;
    }

    public ButtonFunction getFunction() {
        return function;
    }

    public void setFunction(ButtonFunction function) {
        this.function = function;
    }
}
