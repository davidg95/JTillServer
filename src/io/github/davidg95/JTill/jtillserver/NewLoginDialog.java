/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.input.KeyCode;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.jdesktop.swingx.auth.LoginAdapter;
import org.jdesktop.swingx.auth.LoginEvent;
import org.jdesktop.swingx.auth.LoginListener;
import org.jdesktop.swingx.auth.LoginService;

/**
 *
 * @author David
 */
public class NewLoginDialog extends javax.swing.JDialog {

    private static Staff staff;

    private final DataConnect dc;

    /**
     * Creates new form NewLoginDialog
     */
    public NewLoginDialog(Window parent) {
        super(parent);
        dc = GUI.gui.dc;
        initComponents();
        login.setBannerText("Login to JTill Server");
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        init();
    }

    public static Staff showLoginDialog(Component parent) {
        Window window = null;
        if (parent instanceof Frame || parent instanceof Dialog) {
            window = (Window) parent;
        }
        NewLoginDialog dialog = new NewLoginDialog(window);
        staff = null;
        dialog.setVisible(true);
        return staff;
    }

    private void init() {
        LoginListener listener = new LoginAdapter() {

            @Override
            public void loginFailed(LoginEvent source) {
            }

            @Override
            public void loginSucceeded(LoginEvent source) {
                NewLoginDialog.this.setVisible(false);
            }

        };

        LoginService loginService = new LoginService() {
            @Override
            public boolean authenticate(String name, char[] pass, String server) throws Exception {
                final String username = name;
                final String password = new String(pass);

                if (username.equals("") || password.equals("")) {
                    login.setErrorMessage("Please enter both username and password");
                    return false;
                } else {
                    try {
                        staff = dc.login(username, password);
                        if (staff.getPosition() < Integer.parseInt(dc.getSetting("MINIMUM_SERVER_LOGIN"))) {
                            login.setErrorMessage("You do not have authority to log in");
                            try {
                                dc.logout(staff);
                            } catch (StaffNotFoundException ex) {
                            }
                            return false;
                        }
                        return true;
                    } catch (LoginException ex) {
                        login.setErrorMessage(ex.getMessage());
                        return false;
                    } catch (IOException | SQLException ex) {
                        login.setErrorMessage(ex.getMessage());
                        Logger.getGlobal().log(Level.WARNING, null, ex);
                        return false;
                    }
                }
            }
        };

        login.setLoginService(loginService);
        login.getLoginService().addLoginListener(listener);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        login = new org.jdesktop.swingx.JXLoginPane();
        btnLogin = new org.jdesktop.swingx.JXButton();
        btnCancel = new org.jdesktop.swingx.JXButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        btnLogin.setText("Login");
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(login, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(login, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        try {
            login.getLoginService().startAuthentication(login.getUserName(), login.getPassword(), "JTill");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnLoginActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXButton btnCancel;
    private org.jdesktop.swingx.JXButton btnLogin;
    private org.jdesktop.swingx.JXLoginPane login;
    // End of variables declaration//GEN-END:variables
}