/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author David
 */
public class ScreenEditWindow extends javax.swing.JInternalFrame {

    private final Logger LOG = Logger.getGlobal();

    private static ScreenEditWindow frame;

    private final DataConnect dc;

    private final CardLayout categoryCards; //Cards for each screen
    private final ButtonGroup cardsButtonGroup; //Button group for the screen buttons

    private Screen currentScreen; //The current selected screen.
    private TillButton currentButton; //The current selected button.
    private List<TillButton> currentButtons; //The current buttons.

    //Progress bar stuff
    private int amount;

    /**
     * The amount of buttons each screen has horizontally.
     */
    private final int BUTTONS_GRID_X = 5;
    /**
     * The amount of buttons each screen has vertically.
     */
    private final int BUTTONS_GRID_Y = 10;

    private final MyListModel model;

    /**
     * Creates new form ScreenEditWindow
     *
     * @param dc reference to the data connect class.
     * @param icon the icon for the window.
     */
    public ScreenEditWindow(DataConnect dc, Image icon) {
        this.dc = dc;
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setClosable(true);
        super.setFrameIcon(new ImageIcon(icon));
        initComponents();
        categoryCards = (CardLayout) panelProducts.getLayout();
        cardsButtonGroup = new ButtonGroup();
        model = new MyListModel();
        init();
    }

    private void init() {
        list.setModel(model);
    }

    class MyListModel implements ListModel {

        private final List<Screen> screens = new ArrayList<>();
        private final List<ListDataListener> listeners = new LinkedList<>();

        public void addScreen(Screen s) {
            screens.add(s);
            alertListenersChanged(screens.size() - 1, screens.size() - 1);
        }

        public void removeScreen(Screen s) {
            screens.remove(s);
            int index = 0;
            for (int i = 0; i < screens.size(); i++) {
                if (screens.get(i).equals(s)) {
                    index = i;
                    break;
                }
            }
            alertListenersChanged(index, index);
        }

        public void removeScreen(int i) {
            screens.remove(i);
            alertListenersChanged(i, i);
        }

        public void empty() {
            screens.clear();
            alertListenersChanged(0, 0);
        }

        @Override
        public int getSize() {
            return screens.size();
        }

        @Override
        public Object getElementAt(int index) {
            return screens.get(index);
        }

        private void alertListenersChanged(int i1, int i2) {
            for (ListDataListener l : listeners) {
                l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, i1, i2));
            }
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            listeners.add(l);
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            listeners.remove(l);
        }
    }

    /**
     * Show the screen edit window.
     *
     * @param dc the database connection.
     * @param icon the icon for the window.
     */
    public static void showScreenEditWindow(DataConnect dc, Image icon) {
        if (frame == null) {
            frame = new ScreenEditWindow(dc, icon);
            GUI.gui.internal.add(frame);
        }
        if (frame.isVisible()) {
            frame.toFront();
        } else {
            update();
            frame.setVisible(true);
        }
        try {
            frame.setIcon(false);
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(SettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void update() {
        frame.setButtons();
    }

    /**
     * Sets the buttons and screens for editing.
     */
    public synchronized void setButtons() {
        try {
            List<Screen> screens = dc.getAllScreens(); //Get all the screens on the server.
            amount = dc.getAllButtons().size();
            bar.setMaximum(amount);
            model.empty();
            for (Screen s : screens) {
                addScreenButton(s);
                model.addScreen(s);
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
        bar.setValue(0);
        repaint();
        revalidate();
    }

    /**
     * Creates a new button for the screen.
     *
     * @param s the screen to create a button for.
     */
    public void addScreenButton(Screen s) {
        JPanel panel = new JPanel(); //Create a panel for the buttons on this screen.
        panel.setLayout(new GridBagLayout());

        try {
            currentButtons = dc.getButtonsOnScreen(s); //Get all the buttons on the screen.
            for (TillButton b : currentButtons) {
                JButton pButton = new JButton(b.getName()); //Creat a new button for the button.
                if (b.getColorValue() != 0) {
                    pButton.setBackground(new Color(b.getColorValue()));
                }
                pButton.setMinimumSize(new Dimension(0, 0));
                pButton.setMaximumSize(new Dimension(panel.getWidth() / this.BUTTONS_GRID_X, panel.getHeight() / this.BUTTONS_GRID_Y));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = b.getX() - 1;
                gbc.gridy = b.getY() - 1;
                gbc.gridwidth = b.getWidth();
                gbc.gridheight = b.getHeight();
                gbc.weightx = 1;
                gbc.weighty = 1;
                gbc.fill = GridBagConstraints.BOTH;
                if (b.getType() == TillButton.SPACE) { //If it is a space, create a panel for the button.
                    JPanel pan = new JPanel();
                    pan.setBackground(Color.WHITE);
                    //pan.setLayout(new GridLayout(1, 1));
                    pan.addMouseListener(new MouseListener() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            currentButton = b;
                            showButtonOptions(); //Show the button options dialog.
                        }

                        @Override
                        public void mousePressed(MouseEvent e) {

                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {

                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {
                            pan.setBackground(Color.GRAY); //Set the panel to turn grey when the mouse hovers over it.
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            pan.setBackground(Color.WHITE); //Set the panel back to white when the mouse leaves.
                        }

                    });
                    panel.add(pan, gbc); //Add the panel to the screen panel.
                } else { //If the button is a button.
                    pButton.addActionListener((ActionEvent e) -> {
                        currentButton = b;
                        showButtonOptions(); //Show the button options dialog.
                    });
                    panel.add(pButton, gbc); //Add the button to the panel.
                }
                bar.setValue(bar.getValue() + 1);
                bar.repaint();
            }
            panelProducts.add(panel, s.getName()); //Add the screen panel to the container panel for all screens.
        } catch (SQLException | IOException | ScreenNotFoundException ex) {
            showError(ex);
        }
    }

    /**
     * Method to show the buttons options dialog for a particular button.
     */
    private void showButtonOptions() {
        currentButton = ButtonOptionDialog.showDialog(this, currentButton, 5 - currentButton.getX() + 1, 10 - currentButton.getY() + 1);
        if (currentButton == null) { //If it is null then the button is getting removed.
            try {
                currentButton.setName("[SPACE]");
                dc.updateButton(currentButton);
            } catch (IOException | SQLException | JTillException ex) {
                showError(ex);
            }
        } else { //If it is not null then it is being edited or nothing has happening to it
            try {
                dc.updateButton(currentButton); //This will update the ucrrent button in the database
            } catch (IOException | SQLException | JTillException ex) {
                showError(ex);
            }
        }
        new Thread() {
            @Override
            public void run() {
                setButtons(); //This will update the view to reflect any changes
            }
        }.start();
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e, "Staff", JOptionPane.ERROR_MESSAGE);
    }

    private boolean checkName(String name) {
        try {
            List<Screen> screens = dc.getAllScreens();
            for (Screen s : screens) {
                if (s.getName().equalsIgnoreCase(name)) {
                    return false;
                }
            }
        } catch (IOException | SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
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
        btnNewScreen = new javax.swing.JButton();
        bar = new javax.swing.JProgressBar();
        btnClose = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setTitle("Screen Editor");

        panelEditor.setBorder(javax.swing.BorderFactory.createTitledBorder("Editor"));

        panelProducts.setBorder(javax.swing.BorderFactory.createTitledBorder("Buttons"));
        panelProducts.setLayout(new java.awt.CardLayout());

        javax.swing.GroupLayout panelEditorLayout = new javax.swing.GroupLayout(panelEditor);
        panelEditor.setLayout(panelEditorLayout);
        panelEditorLayout.setHorizontalGroup(
            panelEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelProducts, javax.swing.GroupLayout.DEFAULT_SIZE, 1145, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelEditorLayout.setVerticalGroup(
            panelEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelProducts, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnNewScreen.setText("Add Screen");
        btnNewScreen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewScreenActionPerformed(evt);
            }
        });

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        list.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(list);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnNewScreen, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 66, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bar, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose))
                    .addComponent(panelEditor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnNewScreen)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 420, Short.MAX_VALUE))
                    .addComponent(panelEditor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnClose)
                    .addComponent(bar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNewScreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewScreenActionPerformed
        String name = JOptionPane.showInternalInputDialog(GUI.gui.internal, "Enter Name", "New Screen", JOptionPane.PLAIN_MESSAGE);
        if (name == null) {
            return;
        }
        if (!name.equals("")) {
            if (checkName(name)) { //Check if the name is already being used
                new Thread("New Screen") {
                    @Override
                    public void run() {
                        try {
                            Screen s = new Screen(name);
                            s = dc.addScreen(s);
                            int x = 1;
                            int y = 1;
                            for (int i = 0; i < 50; i++) {
                                TillButton bu = dc.addButton(new TillButton("[SPACE]", 0, TillButton.SPACE, s.getId(), 1, 1, 1, x, y));
                                dc.addButton(bu);
                                x++;
                                if (x == 6) {
                                    x = 1;
                                    y++;
                                }
                            }
                            setButtons();
                        } catch (IOException | SQLException ex) {
                            showError(ex);
                        }
                    }
                }.start();
            } else {
                JOptionPane.showInternalMessageDialog(GUI.gui.internal, "Name already in use", "New Screen", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showInternalMessageDialog(GUI.gui.internal, "Must enter a value", "New Screen", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnNewScreenActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void listMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMouseClicked
        Screen sc = (Screen) model.getElementAt(list.getSelectedIndex());
        if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem rename = new JMenuItem("Rename");
            JMenuItem remove = new JMenuItem("Remove");
            rename.addActionListener((ActionEvent e) -> {
                String name = JOptionPane.showInternalInputDialog(GUI.gui.internal, "Enter new screen name", "Screen name for " + sc.getName(), JOptionPane.OK_CANCEL_OPTION);
                if (name != null) {
                    sc.setName(name);
                    try {
                        dc.updateScreen(sc);
                    } catch (IOException | SQLException | ScreenNotFoundException ex) {
                        JOptionPane.showMessageDialog(GUI.gui.internal, ex, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            remove.addActionListener((ActionEvent e) -> {
                try {
                    if (JOptionPane.showInternalConfirmDialog(GUI.gui.internal, "Are you sure you want to remove " + sc.getName() + "?", "Remove Screen " + sc.getName(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        dc.removeScreen(sc);
                        model.removeScreen(sc);
                    }
                } catch (IOException | SQLException | ScreenNotFoundException ex) {
                    JOptionPane.showMessageDialog(GUI.gui.internal, ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            menu.add(rename);
            menu.add(remove);
            menu.show(this, evt.getX(), evt.getY());
        } else {
            categoryCards.show(panelProducts, sc.getName());
            currentScreen = sc;
        }
    }//GEN-LAST:event_listMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar bar;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnNewScreen;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<String> list;
    private javax.swing.JPanel panelEditor;
    private javax.swing.JPanel panelProducts;
    // End of variables declaration//GEN-END:variables
}
