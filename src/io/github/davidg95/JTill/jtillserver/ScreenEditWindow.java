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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JLabel;
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
    private Button currentButton;
    private List<Button> currentButtons;

    /**
     * Creates new form ScreenEditWindow
     */
    public ScreenEditWindow(DataConnectInterface dc) {
        dbConn = dc;
        initComponents();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        categoryCards = (CardLayout) panelProducts.getLayout();
        cardsButtonGroup = new ButtonGroup();
    }

    public static void showScreenEditWindow(DataConnectInterface dc) {
        if (frame == null) {
            frame = new ScreenEditWindow(dc);
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        update();
        frame.setVisible(true);
    }

    public static void update() {
        frame.setButtons();
    }

    public void setButtons() {
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
        cButton.addActionListener((ActionEvent e) -> {
            categoryCards.show(panelProducts, s.getName());
            currentScreen = s;
            try {
                if (dbConn.getButtonsOnScreen(s).size() == 50) {
                    btnNewProduct.setEnabled(false);
                    btnSpace.setEnabled(false);
                } else {
                    btnNewProduct.setEnabled(true);
                    btnSpace.setEnabled(true);
                }
            } catch (IOException | SQLException | ScreenNotFoundException ex) {
                showError(ex);
            }
        });
        cardsButtonGroup.add(cButton);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(10, 5));

        try {
            currentButtons = dbConn.getButtonsOnScreen(s);
            for (int i = 0; i < currentButtons.size(); i++) {
                for (Button b : currentButtons) {
                    if (b.getOrder() == i) {
                        JButton pButton = new JButton(b.getName());
                        if (b.getColorValue() != 0) {
                            pButton.setBackground(new Color(b.getColorValue()));
                        }
                        if (b.getName().equals("[SPACE]")) {
                            JPanel pan = new JPanel();
                            pan.setBackground(Color.WHITE);
                            pan.setLayout(new GridLayout(1, 1));
                            pan.add(new JLabel("<Space>"));
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
                            pButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    currentButton = b;
                                    showButtonOptions();
                                }

                            });
                            panel.add(pButton);
                        }
                        break;
                    }
                }
            }

            for (int i = currentButtons.size() - 1; i < 49; i++) {
                JPanel blankPanel = new JPanel();
                blankPanel.setBackground(Color.WHITE);
                panel.add(blankPanel);
            }

            panelProducts.add(panel, s.getName());
            panelCategories.add(cButton);
        } catch (SQLException | IOException | ScreenNotFoundException ex) {
            showError(ex);
        }
    }

    private void showButtonOptions() {
        int originalOrder = currentButton.getOrder(); //The order of the button before being changed
        Button but = currentButton; //The button that is getting changed
        currentButton = ButtonOptionDialog.showDialog(this, currentButton, currentButtons.size() - 1);
        if (currentButton == null) { //If it is null then the button is getting removed.
            try {
                dbConn.removeButton(but);
            } catch (IOException | SQLException | ButtonNotFoundException ex) {
                showError(ex);
            }
        } else { //If it is not null then it is being edited or nothing has happening to it
            try {
                if (currentButton.getOrder() != originalOrder) { //If the order has changed then the button is getting moved
                    int newOrder = currentButton.getOrder(); //Get the buttons new position
                    Button otherButton = changeButtonPosition(newOrder, originalOrder); //This will move the other button into the current buttons old position
                    dbConn.updateButton(otherButton); //This will update the other button in the database
                }
                dbConn.updateButton(currentButton); //This will update the ucrrent button in the database
            } catch (IOException | SQLException | ButtonNotFoundException ex) {
                showError(ex);
            }
        }
        setButtons(); //This will update the view to reflect any changes
    }

    /**
     * This method will move a button at one position to another position,
     *
     * @param pos the position of the button getting moved.
     * @param newPos the new position of the button.
     * @return the updated button.
     */
    private Button changeButtonPosition(int pos, int newPos) {
        for (Button b : currentButtons) {
            if (b.getOrder() == pos) {
                if (b.getName().equals(currentButton.getName())) {
                    continue;
                }
                b.setOrder(newPos);
                return b;
            }
        }
        return null;
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e, "Staff", JOptionPane.ERROR_MESSAGE);
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
        btnNewProduct = new javax.swing.JButton();
        btnSpace = new javax.swing.JButton();
        btnWipeAll = new javax.swing.JButton();

        setTitle("Screen Editor");
        setIconImage(TillServer.getIcon());

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

        btnNewProduct.setText("Add Product");
        btnNewProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewProductActionPerformed(evt);
            }
        });

        btnSpace.setText("Add Space");
        btnSpace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSpaceActionPerformed(evt);
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
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnNewProduct)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSpace))
                    .addComponent(btnWipeAll))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
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
                        .addGap(106, 106, 106)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnNewProduct)
                            .addComponent(btnSpace))
                        .addGap(63, 63, 63)
                        .addComponent(btnWipeAll)
                        .addGap(0, 333, Short.MAX_VALUE))
                    .addComponent(panelEditor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNewScreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewScreenActionPerformed
        try {
            String name = JOptionPane.showInputDialog("Enter Name");
            int position = dbConn.getAllScreens().size() - 1;
            Screen s = new Screen(name, position, 0);
            dbConn.addScreen(s);
            setButtons();
        } catch (IOException | SQLException ex) {
            showError(ex);
        }
    }//GEN-LAST:event_btnNewScreenActionPerformed

    private void btnNewProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewProductActionPerformed
        new Thread() {
            @Override
            public void run() {
                try {
                    if (currentScreen == null) {
                        JOptionPane.showMessageDialog(ScreenEditWindow.this, "Select a screen", "New Button", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Product p = ProductSelectDialog.showDialog(ScreenEditWindow.this, dbConn);
                    if (p != null) {
                        int position = dbConn.getButtonsOnScreen(currentScreen).size();
                        Button b = new Button(p.getShortName(), p.getProductCode(), position, currentScreen.getId(), 0);
                        dbConn.addButton(b);
                        setButtons();
                    }
                } catch (IOException | SQLException | ScreenNotFoundException ex) {
                    showError(ex);
                }
            }
        }.start();
    }//GEN-LAST:event_btnNewProductActionPerformed

    private void btnSpaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSpaceActionPerformed
        new Thread() {
            @Override
            public void run() {
                try {
                    if (currentScreen == null) {
                        JOptionPane.showMessageDialog(ScreenEditWindow.this, "Select a screen", "Space", JOptionPane.ERROR_MESSAGE);
                    } else {
                        int position = dbConn.getButtonsOnScreen(currentScreen).size();
                        Button b = new Button("[SPACE]", 1, position, currentScreen.getId(), 0);
                        dbConn.addButton(b);
                        setButtons();
                    }
                } catch (IOException | SQLException | ScreenNotFoundException ex) {
                    showError(ex);
                }
            }
        }.start();
    }//GEN-LAST:event_btnSpaceActionPerformed

    private void btnWipeAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWipeAllActionPerformed
        new Thread() {
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
    private javax.swing.JButton btnNewProduct;
    private javax.swing.JButton btnNewScreen;
    private javax.swing.JButton btnSpace;
    private javax.swing.JButton btnWipeAll;
    private javax.swing.JPanel panelCategories;
    private javax.swing.JPanel panelEditor;
    private javax.swing.JPanel panelProducts;
    // End of variables declaration//GEN-END:variables
}
