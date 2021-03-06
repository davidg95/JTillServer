/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.Window;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;

/**
 * ButtonOptionsDialog for editing a till button.
 *
 * @author David
 */
public final class ButtonOptionDialog extends javax.swing.JDialog {

    private final JTill jtill;

    private static TillButton button;

    private final int maxWidth;
    private final int maxHeight;

    /**
     * Creates new form ButtonOptionDialog.
     *
     * @param jtill the JTill reference.
     * @param parent the parent component.
     * @param maxWidth the maximum width of the button.
     * @param maxHeight the maximum height of the button.
     */
    public ButtonOptionDialog(JTill jtill, Window parent, int maxWidth, int maxHeight) {
        super(parent);

        this.jtill = jtill;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.setIconImage(GUI.icon);
        initComponents();
        setLocationRelativeTo(parent);
        setModal(true);
        int x = (int) MouseInfo.getPointerInfo().getLocation().getX();
        int y = (int) MouseInfo.getPointerInfo().getLocation().getY();
        this.setLocation(x - getWidth() / 2, y - getHeight() / 2);
        if (button == null) {
            setTitle("New Button");
            button = new TillButton("", "", TillButton.SPACE, -1, TillButton.rbg2Hex(Color.BLACK),TillButton.rbg2Hex(Color.WHITE), 1, 1, 1, 1, 1, "");
        } else {
            setTitle(button.getName());
            txtItem.setText(button.getName());
            if (button.getType() == TillButton.SPACE) {
                btnChangeButton.setEnabled(false);
                cmbFunction.setSelectedItem("None");
                txtItem.setEnabled(false);
                txtItem.setText("");
            } else if (button.getType() == TillButton.ITEM) {
                btnChangeButton.setText("Change Product");
                cmbFunction.setSelectedItem("Product");
                txtItem.setText(button.getName());
            } else if (button.getType() == TillButton.BACK) {
                btnChangeButton.setEnabled(false);
                cmbFunction.setSelectedItem("Back");
                txtItem.setText(button.getName());
            } else if (button.getType() == TillButton.MAIN) {
                btnChangeButton.setEnabled(false);
                cmbFunction.setSelectedItem("Main");
                txtItem.setText(button.getName());
            } else if (button.getType() == TillButton.LOGOFF) {
                btnChangeButton.setEnabled(false);
                cmbFunction.setSelectedItem("Logoff");
                txtItem.setText(button.getName());
            } else if (button.getType() == TillButton.PAYMENT) {
                btnChangeButton.setEnabled(false);
                cmbFunction.setSelectedItem("Payment");
                txtItem.setText(button.getName());
            } else if (button.getType() == TillButton.VOID) {
                btnChangeButton.setEnabled(false);
                cmbFunction.setSelectedItem("Void");
                txtItem.setText(button.getName());
            } else if (button.getType() == TillButton.LINK) {
                btnChangeButton.setEnabled(true);
                cmbFunction.setSelectedItem("Link");
                txtItem.setText(button.getName());
            } else {
                btnChangeButton.setText("Change Screen");
                cmbFunction.setSelectedItem("Screen");
                txtItem.setText(button.getName());
            }
        }
        txtWidth.setText(button.getWidth() + "");
        txtHeight.setText(button.getHeight() + "");
        txtColor.setBackground(TillButton.hex2Rgb(button.getColorValue()));
        txtColor.setText(button.getColorValue());
        txtFontColor.setBackground(TillButton.hex2Rgb(button.getFontColor()));
        txtFontColor.setText(button.getFontColor());
        cmbAccess.setSelectedIndex(button.getAccessLevel() - 1);
    }

    /**
     * Shows the ButtonOptionsDialog. Returns null if remove button was
     * selected, otherwise it will return an updated button object.
     *
     * @param jtill the jtill reference.
     * @param parent the parent component.
     * @param b the button object.
     * @param maxWidth the maximum width of the button.
     * @param maxHeight the maximum height of the button.
     * @return the button with any changed applied.
     */
    public static TillButton showDialog(JTill jtill, Component parent, TillButton b, int maxWidth, int maxHeight) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        button = b;
        ButtonOptionDialog dialog = new ButtonOptionDialog(jtill, window, maxWidth, maxHeight);
        dialog.setVisible(true);
        return button;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnClose = new javax.swing.JButton();
        btnChangeButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtWidth = new javax.swing.JTextField();
        txtHeight = new javax.swing.JTextField();
        txtItem = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        btnSave = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        txtColor = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtFontColor = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        cmbFunction = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        cmbAccess = new javax.swing.JComboBox<>();
        btnColorButton = new javax.swing.JButton();
        btnColorFont = new javax.swing.JButton();

        setResizable(false);

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnChangeButton.setText("Change Product");
        btnChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Width:");

        jLabel2.setText("Height:");

        txtWidth.setText("1");
        txtWidth.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtWidthKeyReleased(evt);
            }
        });

        txtHeight.setText("1");
        txtHeight.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtHeightKeyReleased(evt);
            }
        });

        txtItem.setMaximumSize(new java.awt.Dimension(6, 20));

        jLabel3.setText("Label Text:");

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        jLabel4.setText("Color:");

        txtColor.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtColorFocusLost(evt);
            }
        });
        txtColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtColorActionPerformed(evt);
            }
        });

        jLabel5.setText("Font Color:");

        txtFontColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFontColorActionPerformed(evt);
            }
        });

        jLabel6.setText("Function:");

        cmbFunction.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "Product", "Screen", "Back", "Main", "Logoff", "Payment", "Void", "Link" }));
        cmbFunction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFunctionActionPerformed(evt);
            }
        });

        jLabel7.setText("Access:");

        cmbAccess.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Assisstant", "Supervisor", "Manager", "Area Manager" }));

        btnColorButton.setText("Color Picker");
        btnColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnColorButtonActionPerformed(evt);
            }
        });

        btnColorFont.setText("Color Picker");
        btnColorFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnColorFontActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel5)
                                .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel2))
                                    .addComponent(txtColor, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtFontColor, javax.swing.GroupLayout.Alignment.LEADING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtHeight)
                                    .addComponent(btnColorFont, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)))
                            .addComponent(cmbAccess, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtItem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbFunction, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnChangeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(cmbFunction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnChangeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtItem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(btnColorButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFontColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(btnColorFont))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1)
                        .addComponent(jLabel2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbAccess, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(btnClose))
                .addGap(13, 13, 13))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        button = null;
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeButtonActionPerformed
        String func = (String) cmbFunction.getSelectedItem();
        if (func.equals("Product")) {
            Product p = ProductSelectDialog.showDialog(this, jtill);
            if (p != null) {
                button.setItem(p.getBarcode());
                button.setName(p.getShortName());
                txtItem.setText(p.getShortName());
            }
            button.setType(TillButton.ITEM);
        } else if (func.equals("Screen")) {
            Screen s = ScreenSelectDialog.showDialog(jtill, this);
            if (s != null) {
                button.setItem("" + s.getId());
                button.setName(s.getName());
                txtItem.setText(s.getName());
            }
            button.setType(TillButton.SCREEN);
        } else if (func.equals("Link")) {
            String link = (String) JOptionPane.showInputDialog(this, "Enter URL", "Link", JOptionPane.PLAIN_MESSAGE, null, null, button.getLink());
            if (link == null) {
                return;
            }
            if (link.length() > 50) {
                JOptionPane.showMessageDialog(this, "Link must be 50 characters or less", "Link", JOptionPane.ERROR_MESSAGE);
                return;
            }
            button.setLink(link);
        }
    }//GEN-LAST:event_btnChangeButtonActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        String w = txtWidth.getText();
        String h = txtHeight.getText();
        if (w.length() == 0 || h.length() == 0) {
            JOptionPane.showMessageDialog(this, "Must enter a value for width and height", "Button Options", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!Utilities.isNumber(w) || !Utilities.isNumber(h)) {
            JOptionPane.showMessageDialog(this, "A number must be entered", "Button Options", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int width = Integer.parseInt(w);
        int height = Integer.parseInt(h);
        if (width <= 0 || height <= 0) {
            JOptionPane.showMessageDialog(this, "Width and height must be greater than zero", "Button Options", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String func = (String) cmbFunction.getSelectedItem();

        button.setWidth(width);
        button.setHeight(height);
        button.setColorValue(TillButton.rbg2Hex(txtColor.getBackground()));
        button.setFontColor(TillButton.rbg2Hex(txtFontColor.getBackground()));
        if (!func.equals("Space")) {
            if (txtItem.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "You must enter button text", "Button", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (func.equals("None")) {
            button.setName("[SPACE]");
            button.setType(TillButton.SPACE);
        } else if (func.equals("Back")) {
            button.setName(txtItem.getText());
            button.setType(TillButton.BACK);
        } else if (func.equals("Main")) {
            button.setName(txtItem.getText());
            button.setType(TillButton.MAIN);
        } else if (func.equals("Logoff")) {
            button.setName(txtItem.getText());
            button.setType(TillButton.LOGOFF);
        } else if (func.equals("Payment")) {
            button.setName(txtItem.getText());
            button.setType(TillButton.PAYMENT);
        } else if (func.equals("Void")) {
            button.setName(txtItem.getText());
            button.setType(TillButton.VOID);
        } else if (func.equals("Link")) {
            button.setName(txtItem.getText());
            button.setType(TillButton.LINK);
        } else {
            button.setName(txtItem.getText());
        }

        button.setAccessLevel(cmbAccess.getSelectedIndex() + 1);
        setVisible(false);
    }//GEN-LAST:event_btnSaveActionPerformed

    private void txtHeightKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtHeightKeyReleased
        if (Utilities.isNumber(txtHeight.getText())) {
            int val = Integer.parseInt(txtHeight.getText());
            if (val < 1 || val > maxHeight) {
                JOptionPane.showMessageDialog(this, "This must be greater than 0 and less than " + maxHeight + ", which is the maximum width for this item", "Button", JOptionPane.ERROR_MESSAGE);
                txtHeight.setSelectionStart(0);
                txtHeight.setSelectionEnd(txtHeight.getText().length());
            }
        } else {
            JOptionPane.showMessageDialog(this, "You must enter a number", "Button", JOptionPane.ERROR_MESSAGE);
            txtHeight.setSelectionStart(0);
            txtHeight.setSelectionEnd(txtHeight.getText().length());
        }
    }//GEN-LAST:event_txtHeightKeyReleased

    private void cmbFunctionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFunctionActionPerformed
        String func = (String) cmbFunction.getSelectedItem();
        if (func.equals("None")) {
            txtItem.setEnabled(false);
            txtItem.setText("");
        } else {
            txtItem.setEnabled(true);
            txtItem.setText("");
        }

        if (func.equals("Main") || func.equals("Back") || func.equals("Logoff") || func.equals("Payment") || func.equals("Void")) {
            txtItem.setText(func);
        }

        if (func.equals("Product") || func.equals("Screen") || func.equals("Link")) {
            btnChangeButton.setEnabled(true);
            cmbAccess.setEnabled(true);

        } else {
            btnChangeButton.setEnabled(false);
            cmbAccess.setEnabled(false);
        }

        if (func.equals("Product")) {
            btnChangeButton.setText("Change Product");
        } else if (func.equals("Screen")) {
            btnChangeButton.setText("Change Screen");
        } else if (func.equals("Link")) {
            btnChangeButton.setText("Change Link");
        }
    }//GEN-LAST:event_cmbFunctionActionPerformed

    private void txtWidthKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtWidthKeyReleased
        if (Utilities.isNumber(txtWidth.getText())) {
            int val = Integer.parseInt(txtWidth.getText());
            if (val < 1 || val > maxWidth) {
                JOptionPane.showMessageDialog(this, "This must be greater than 0 and less than " + maxWidth + ", which is the maximum height for this item", "Button", JOptionPane.ERROR_MESSAGE);
                txtWidth.setSelectionStart(0);
                txtWidth.setSelectionEnd(txtWidth.getText().length());
            }
        } else {
            JOptionPane.showMessageDialog(this, "You must enter a number", "Button", JOptionPane.ERROR_MESSAGE);
            txtWidth.setSelectionStart(0);
            txtWidth.setSelectionEnd(txtWidth.getText().length());
        }
    }//GEN-LAST:event_txtWidthKeyReleased

    private void btnColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnColorButtonActionPerformed
        Color c = JColorChooser.showDialog(this, "Select background color", TillButton.hex2Rgb(button.getColorValue()));
        if (c == null) {
            return;
        }
        txtColor.setBackground(c);
        txtColor.setText(TillButton.rbg2Hex(c));
    }//GEN-LAST:event_btnColorButtonActionPerformed

    private void btnColorFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnColorFontActionPerformed
        Color c = JColorChooser.showDialog(this, "Select font color", TillButton.hex2Rgb(button.getFontColor()));
        if (c == null) {
            return;
        }
        txtFontColor.setBackground(c);
        txtFontColor.setText(TillButton.rbg2Hex(c));
    }//GEN-LAST:event_btnColorFontActionPerformed

    private void txtColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtColorActionPerformed
        txtColorSet();
    }//GEN-LAST:event_txtColorActionPerformed

    private void txtFontColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFontColorActionPerformed
        txtFontSet();
    }//GEN-LAST:event_txtFontColorActionPerformed

    private void txtColorFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtColorFocusLost
        txtColorSet();
    }//GEN-LAST:event_txtColorFocusLost

    private void txtColorSet() {
        String hex = txtColor.getText();
        if (validateHex(hex)) {
            txtColor.setBackground(TillButton.hex2Rgb(hex));
        } else {
            JOptionPane.showMessageDialog(this, "Not a valid hex number", "Color", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void txtFontSet() {
        String hex = txtFontColor.getText();
        if (validateHex(hex)) {
            txtFontColor.setBackground(TillButton.hex2Rgb(hex));
        } else {
            JOptionPane.showMessageDialog(this, "Not a valid hex number", "Font Color", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateHex(String hex) {
        if (hex.length() != 6) {
            return false;
        }
        if (hex.matches("[0-9a-fA-F]+")) {
            return true;
        }
        return false;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChangeButton;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnColorButton;
    private javax.swing.JButton btnColorFont;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<String> cmbAccess;
    private javax.swing.JComboBox<String> cmbFunction;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTextField txtColor;
    private javax.swing.JTextField txtFontColor;
    private javax.swing.JTextField txtHeight;
    private javax.swing.JTextField txtItem;
    private javax.swing.JTextField txtWidth;
    // End of variables declaration//GEN-END:variables
}
