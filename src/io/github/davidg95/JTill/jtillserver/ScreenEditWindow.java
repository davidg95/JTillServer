/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 *
 * @author David
 */
public class ScreenEditWindow extends javax.swing.JFrame {

    private static ScreenEditWindow frame;

    private final DataConnectInterface dbConn;

    private final CardLayout categoryCards;
    private final ButtonGroup cardsButtonGroup;

    private Screen currentScreen;
    private TillButton currentButton;
    private List<TillButton> currentButtons;

    /**
     * Creates new form ScreenEditWindow
     *
     * @param dc reference to the data connect class.
     */
    public ScreenEditWindow(DataConnectInterface dc, Image icon) {
        dbConn = dc;
        this.setIconImage(icon);
        initComponents();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        categoryCards = (CardLayout) panelProducts.getLayout();
        cardsButtonGroup = new ButtonGroup();
    }

    public static void showScreenEditWindow(DataConnectInterface dc, Image icon) {
        if (frame == null) {
            frame = new ScreenEditWindow(dc, icon);
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        update();
        frame.setVisible(true);
    }

    public static void update() {
        frame.setButtons();
    }

    public synchronized void setButtons() {
        try {
            List<Screen> screens = dbConn.getAllScreens();
            panelCategories.removeAll();
            panelCategories.setLayout(new GridLayout(2, 5));
            for (Screen s : screens) {
                addScreenButton(s);
            }

            for (int i = screens.size() - 1; i < 9; i++) {
                JPanel panel = new JPanel();
                panel.setBackground(Color.WHITE);
                panelCategories.add(panel);
            }
            repaint();
            revalidate();

            if (currentScreen != null) {
                if (cardsButtonGroup.getButtonCount() > 0) {
                    Enumeration<AbstractButton> abButs = cardsButtonGroup.getElements();
                    while (abButs.hasMoreElements()) {
                        JToggleButton button = (JToggleButton) abButs.nextElement();
                        if (button.getText().equals(currentScreen.getName())) {
                            button.doClick();
                        }
                    }
                }
            }
        } catch (SQLException | IOException ex) {
            showError(ex);
        }
    }

    public void addScreenButton(Screen s) {
        JToggleButton cButton = new JToggleButton(s.getName());
        if (s.getColorValue() != 0) {
            cButton.setBackground(new Color(s.getColorValue()));
        }
        cButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Screen s = ScreenButtonOptionDialog.showDialog(ScreenEditWindow.this, currentScreen);
                    if (s == null) {
                        try {
                            dbConn.removeScreen(currentScreen);
                        } catch (IOException | SQLException | ScreenNotFoundException ex) {
                            showError(ex);
                        }
                    } else {
                        try {
                            dbConn.updateScreen(s);
                        } catch (IOException | SQLException | ScreenNotFoundException ex) {
                            showError(ex);
                        }
                    }
                    setButtons();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }

        });
        cButton.addActionListener((ActionEvent e) -> {
            categoryCards.show(panelProducts, s.getName());
            currentScreen = s;
        });
        cardsButtonGroup.add(cButton);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(10, 5));

        try {
            currentButtons = dbConn.getButtonsOnScreen(s);
            for (TillButton b : currentButtons) {
                JButton pButton = new JButton(b.getName());
                if (b.getColorValue() != 0) {
                    pButton.setBackground(new Color(b.getColorValue()));
                }
                if (b.getName().equals("[SPACE]")) {
                    JPanel pan = new JPanel();
                    pan.setBackground(Color.WHITE);
                    pan.setLayout(new GridLayout(1, 1));
                    pan.addMouseListener(new MouseListener() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            currentButton = b;
                            showButtonOptions();
                        }

                        @Override
                        public void mousePressed(MouseEvent e) {

                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {

                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {
                            pan.setBackground(Color.GRAY);
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            pan.setBackground(Color.WHITE);
                        }

                    });
                    panel.add(pan);
                } else {
                    pButton.addActionListener((ActionEvent e) -> {
                        currentButton = b;
                        showButtonOptions();
                    });
                    panel.add(pButton);
                }
            }
            panelProducts.add(panel, s.getName());
            panelCategories.add(cButton);
        } catch (SQLException | IOException | ScreenNotFoundException ex) {
            showError(ex);
        }
    }

    private void showButtonOptions() {
        TillButton but = currentButton; //The button that is getting changed
        currentButton = ButtonOptionDialog.showDialog(this, currentButton, dbConn);
        if (currentButton == null) { //If it is null then the button is getting removed.
            try {
                currentButton.setName("[SPACE]");
                dbConn.updateButton(currentButton);
            } catch (IOException | SQLException | ButtonNotFoundException ex) {
                showError(ex);
            }
        } else { //If it is not null then it is being edited or nothing has happening to it
            try {
                dbConn.updateButton(currentButton); //This will update the ucrrent button in the database
            } catch (IOException | SQLException | ButtonNotFoundException ex) {
                showError(ex);
            }
        }
        setButtons(); //This will update the view to reflect any changes
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e, "Staff", JOptionPane.ERROR_MESSAGE);
    }

    private boolean checkName(String name) {
        try {
            List<Screen> screens = dbConn.getAllScreens();
            for (Screen s : screens) {
                if (s.getName().equalsIgnoreCase(name)) {
                    return false;
                }
            }
        } catch (IOException | SQLException ex) {
            Logger.getLogger(ScreenEditWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelEditor = new javax.swing.JPanel();
        panelProducts = new javax.swing.JPanel();
        panelCategories = new javax.swing.JPanel();
        btnNewScreen = new javax.swing.JButton();
        btnWipeAll = new javax.swing.JButton();

        setTitle("Screen Editor");

        panelProducts.setLayout(new java.awt.CardLayout());

        panelCategories.setLayout(new java.awt.CardLayout());

        javax.swing.GroupLayout panelEditorLayout = new javax.swing.GroupLayout(panelEditor);
        panelEditor.setLayout(panelEditorLayout);
        panelEditorLayout.setHorizontalGroup(
            panelEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelProducts, javax.swing.GroupLayout.DEFAULT_SIZE, 844, Short.MAX_VALUE)
                    .addComponent(panelCategories, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelEditorLayout.setVerticalGroup(
            panelEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelCategories, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelProducts, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnNewScreen.setText("Add Screen");
        btnNewScreen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewScreenActionPerformed(evt);
            }
        });

        btnWipeAll.setText("Wipe All");
        btnWipeAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWipeAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnNewScreen)
                    .addComponent(btnWipeAll))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 106, Short.MAX_VALUE)
                .addComponent(panelEditor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnNewScreen)
                        .addGap(192, 192, 192)
                        .addComponent(btnWipeAll)
                        .addGap(0, 333, Short.MAX_VALUE))
                    .addComponent(panelEditor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNewScreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewScreenActionPerformed
        String name = JOptionPane.showInputDialog("Enter Name");
        if (!name.equals("")) {
            if (checkName(name)) {
                new Thread("New Screen") {
                    @Override
                    public void run() {
                        try {
                            int position = dbConn.getAllScreens().size() - 1;
                            Screen s = new Screen(name, position, 0);
                            Screen sc = dbConn.addScreen(s);
                            for (int i = 0; i < 50; i++) {
                                TillButton bu = dbConn.addButton(new TillButton("[SPACE]", null, s, 0));
                            }
                            setButtons();
                        } catch (IOException | SQLException ex) {
                            showError(ex);
                        }
                    }
                }.start();
            } else {
                JOptionPane.showMessageDialog(this, "Name already in use", "New Screen", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnNewScreenActionPerformed

    private void btnWipeAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWipeAllActionPerformed
        new Thread("WpieAllButton") {
            @Override
            public void run() {
                try {
                    dbConn.deleteAllScreensAndButtons();
                    setButtons();
                } catch (IOException | SQLException ex) {
                    showError(ex);
                }
            }
        }.start();
    }//GEN-LAST:event_btnWipeAllActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnNewScreen;
    private javax.swing.JButton btnWipeAll;
    private javax.swing.JPanel panelCategories;
    private javax.swing.JPanel panelEditor;
    private javax.swing.JPanel panelProducts;
    // End of variables declaration//GEN-END:variables
}
