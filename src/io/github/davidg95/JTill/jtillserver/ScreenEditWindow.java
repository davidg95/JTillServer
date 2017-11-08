/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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

    private Screen currentScreen; //The current selected screen.
    private TillButton currentButton; //The current selected button.
    private List<TillButton> currentButtons; //The current buttons.

    //Progress bar stuff
    private int amount;

    private final MyListModel model;

    /**
     * Creates new form ScreenEditWindow
     */
    public ScreenEditWindow() {
        this.dc = GUI.gui.dc;
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setClosable(true);
        super.setFrameIcon(new ImageIcon(GUI.icon));
        super.setResizable(true);
        initComponents();
        categoryCards = (CardLayout) panelProducts.getLayout();
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
     */
    public static void showScreenEditWindow() {
        if (frame == null || frame.isClosed()) {
            frame = new ScreenEditWindow();
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
        } catch (SQLException | IOException ex) {
            showError(ex);
        }
        bar.setValue(0);
    }

    private void checkInheritance(JPanel panel, Screen s) throws IOException, SQLException, ScreenNotFoundException {
        if (s.getInherits() == -1) {
            return;
        }
        Screen parent = dc.getScreen(s.getInherits());
        checkInheritance(panel, parent);
        List<TillButton> buttons = dc.getButtonsOnScreen(parent);
        for (TillButton b : buttons) {
            if (b.getType() != TillButton.SPACE) {
                JButton pButton = new JButton(b.getName()); //Creat a new button for the button.
                pButton.setPreferredSize(new Dimension(panel.getWidth() / s.getWidth(), panel.getHeight() / s.getHeight()));
                pButton.setBackground(TillButton.hex2Rgb(b.getColorValue()));
                pButton.setOpaque(true);
                pButton.setBorderPainted(false);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = b.getX() - 1;
                gbc.gridy = b.getY() - 1;
                gbc.gridwidth = b.getWidth();
                gbc.gridheight = b.getHeight();
                gbc.weightx = 1;
                gbc.weighty = 1;
                gbc.fill = GridBagConstraints.BOTH;
                pButton.addActionListener((ActionEvent e) -> {
                    currentButton = b;
                    showButtonOptions(); //Show the button options dialog.
                });
                pButton.setEnabled(false);
                panel.add(pButton, gbc); //Add the button to the panel.
            }
        }
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
            checkInheritance(panel, s);
            for (TillButton b : currentButtons) {
                JButton pButton = new JButton(b.getName()); //Creat a new button for the button.
                pButton.setBackground(TillButton.hex2Rgb(b.getColorValue()));
                pButton.setOpaque(true);
                pButton.setBorderPainted(false);
                pButton.setPreferredSize(new Dimension(panel.getWidth() / s.getWidth(), panel.getHeight() / s.getHeight()));
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
                    pan.setBackground(Color.GRAY);
                    pan.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, false));
                    pan.addMouseListener(new MouseListener() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            currentButton = b;
                            if (SwingUtilities.isLeftMouseButton(e)) {
                                showButtonOptions(); //Show the button options dialog.
                            } else if (SwingUtilities.isRightMouseButton(e)) {
                                showPopup(pan, e, b);
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
                            pan.setBackground(Color.LIGHT_GRAY); //Set the panel to turn grey when the mouse hovers over it.
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            pan.setBackground(Color.GRAY); //Set the panel back to white when the mouse leaves.
                        }

                    });
                    pan.setPreferredSize(new Dimension(panel.getWidth() / s.getWidth(), panel.getHeight() / s.getHeight()));
                    panel.add(pan, gbc); //Add the panel to the screen panel.
                } else { //If the button is a button.
                    pButton.addActionListener((ActionEvent e) -> {
                        currentButton = b;
                        showButtonOptions(); //Show the button options dialog.
                    });
                    pButton.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                            if (SwingUtilities.isRightMouseButton(evt)) {
                                currentButton = b;
                                showPopup(pButton, evt, b);
                            }
                        }
                    });
                    pButton.setPreferredSize(new Dimension(panel.getWidth() / s.getWidth(), panel.getHeight() / s.getHeight()));
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

    private void showPopup(Component c, MouseEvent evt, TillButton b) {
        final JPopupMenu menu = new JPopupMenu();
        final JMenuItem edit = new JMenuItem("Edit");
        final JMenu position = new JMenu("Position");
        final Font boldFont = new Font(edit.getFont().getFontName(), Font.BOLD, edit.getFont().getSize());
        edit.setFont(boldFont);
        final JMenuItem remove = new JMenuItem("Remove");

        edit.addActionListener((ActionEvent e) -> {
            showButtonOptions();
        });

        if (c instanceof JPanel) {
            position.setEnabled(false);
            remove.setEnabled(false);
            edit.setText("Add button");
        }
        JMenuItem up = new JMenuItem("Move Up");
        JMenuItem down = new JMenuItem("Move Down");
        JMenuItem left = new JMenuItem("Move Left");
        JMenuItem right = new JMenuItem("Move Right");

        up.addActionListener((ActionEvent e) -> {
            b.setY(b.getY() - 1);
            saveAndUpdate();
        });
        down.addActionListener((ActionEvent e) -> {
            b.setY(b.getY() + 1);
            saveAndUpdate();
        });
        left.addActionListener((ActionEvent e) -> {
            b.setX(b.getX() - 1);
            saveAndUpdate();
        });
        right.addActionListener((ActionEvent e) -> {
            b.setX(b.getX() + 1);
            saveAndUpdate();
        });

        position.add(up);
        position.add(down);
        position.add(left);
        position.add(right);

        remove.addActionListener((ActionEvent e) -> {
            b.setWidth(1);
            b.setHeight(1);
            b.setType(TillButton.SPACE);
            b.setColorValue("000000");
            b.setFontColor("ffffff");
            saveAndUpdate();
        });

        menu.add(edit);
        menu.add(position);
        menu.add(remove);
        menu.show(c, evt.getX(), evt.getY());
    }

    private void saveAndUpdate() {
        try {
            dc.updateButton(currentButton); //This will update the ucrrent button in the database
        } catch (IOException | SQLException | JTillException ex) {
            showError(ex);
        }
        new Thread() {
            @Override
            public void run() {
                setButtons(); //This will update the view to reflect any changes
                categoryCards.show(panelProducts, currentScreen.getName());
                repaint();
            }
        }.start();
    }

    /**
     * Method to show the buttons options dialog for a particular button.
     */
    private void showButtonOptions() {
        currentButton = ButtonOptionDialog.showDialog(this, currentButton, currentScreen.getWidth() - currentButton.getX() + 1, currentScreen.getHeight() - currentButton.getY() + 1);
        if (currentButton == null) { //If it is null then the button is getting removed.
            return;
        } else { //If it is not null then it is being edited or nothing has happening to it
            try {
                dc.updateButton(currentButton); //This will update the ucrrent button in the database
            } catch (IOException | SQLException | JTillException ex) {
                showError(ex);
            }
        }
        final String name = currentScreen.getName();
        new Thread() {
            @Override
            public void run() {
                setButtons(); //This will update the view to reflect any changes
                categoryCards.show(panelProducts, name);
                repaint();
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

        btnNewScreen = new javax.swing.JButton();
        bar = new javax.swing.JProgressBar();
        btnClose = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList<>();
        panelProducts = new javax.swing.JPanel();
        panelScreenProperties = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        btnInherits = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtVGap = new javax.swing.JTextField();
        txtHGap = new javax.swing.JTextField();
        btnSave = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setTitle("Screen Editor");

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

        panelProducts.setBorder(javax.swing.BorderFactory.createTitledBorder("Buttons"));
        panelProducts.setLayout(new java.awt.CardLayout());

        panelScreenProperties.setBorder(javax.swing.BorderFactory.createTitledBorder("Screen Settings"));

        jLabel3.setText("Inherits from:");

        btnInherits.setText("NONE");
        btnInherits.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInheritsActionPerformed(evt);
            }
        });

        jLabel1.setText("V Gap:");

        jLabel2.setText("H Gap:");

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelScreenPropertiesLayout = new javax.swing.GroupLayout(panelScreenProperties);
        panelScreenProperties.setLayout(panelScreenPropertiesLayout);
        panelScreenPropertiesLayout.setHorizontalGroup(
            panelScreenPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScreenPropertiesLayout.createSequentialGroup()
                .addGroup(panelScreenPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelScreenPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnInherits, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                    .addComponent(txtVGap)
                    .addComponent(txtHGap)))
            .addGroup(panelScreenPropertiesLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(btnSave)
                .addContainerGap(64, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelScreenPropertiesLayout.setVerticalGroup(
            panelScreenPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScreenPropertiesLayout.createSequentialGroup()
                .addGroup(panelScreenPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(btnInherits))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelScreenPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtVGap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelScreenPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtHGap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSave))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelScreenProperties, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNewScreen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bar, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 624, Short.MAX_VALUE)
                        .addComponent(btnClose))
                    .addComponent(panelProducts, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNewScreen)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelScreenProperties, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 136, Short.MAX_VALUE))
                    .addComponent(panelProducts, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnClose)
                    .addComponent(bar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNewScreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewScreenActionPerformed
        String name = JOptionPane.showInternalInputDialog(this, "Enter Name", "New Screen", JOptionPane.PLAIN_MESSAGE);
        if (name == null) {
            return;
        }
        int inherit = -1;
        int width;
        int height;
        if (JOptionPane.showInternalConfirmDialog(this, "Inherit from screen?", "New Screen", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Screen s = ScreenSelectDialog.showDialog(this);
            inherit = s.getId();
            width = s.getWidth();
            height = s.getHeight();
        } else {
            try {
                width = Integer.parseInt(JOptionPane.showInternalInputDialog(this, "Enter width for screen", "New Screen", JOptionPane.PLAIN_MESSAGE));
                height = Integer.parseInt(JOptionPane.showInternalInputDialog(this, "Enter height for screen", "New Screen", JOptionPane.PLAIN_MESSAGE));
                if (width <= 0 || height <= 0) {
                    throw new Exception("You must enter a number greater than 0");
                }
            } catch (Exception e) {
                JOptionPane.showInternalMessageDialog(this, e.getMessage(), "New Screen", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (name.equalsIgnoreCase("default")) {
            JOptionPane.showInternalMessageDialog(this, "That name is not allowed", "New Screen", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final int inh = inherit;
        final int fw = width;
        final int fh = height;
        if (!name.equals("")) {
            if (checkName(name)) { //Check if the name is already being used
                new Thread("New Screen") {
                    @Override
                    public void run() {
                        try {
                            Screen s = new Screen(name, fw, fh, inh, 3, 3);
                            currentScreen = dc.addScreen(s);
                            int x = 1;
                            int y = 1;
                            for (int i = 0; i < (fw * fh); i++) {
                                TillButton bu = dc.addButton(new TillButton("[SPACE]", 0, TillButton.SPACE, s.getId(), "000000", "ffffff", 1, 1, x, y, 1, ""));
                                x++;
                                if (x == (fw + 1)) {
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
                JOptionPane.showInternalMessageDialog(this, "Name already in use", "New Screen", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showInternalMessageDialog(this, "Must enter a value", "New Screen", JOptionPane.ERROR_MESSAGE);
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
            JMenuItem duplicate = new JMenuItem("Duplicate");
            rename.addActionListener((ActionEvent e) -> {
                String name = JOptionPane.showInternalInputDialog(this, "Enter new screen name", "Screen name for " + sc.getName(), JOptionPane.OK_CANCEL_OPTION);
                if (name != null) {
                    sc.setName(name);
                    try {
                        dc.updateScreen(sc);
                    } catch (IOException | SQLException | ScreenNotFoundException ex) {
                        JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            remove.addActionListener((ActionEvent e) -> {
                try {
                    if (JOptionPane.showInternalConfirmDialog(this, "Are you sure you want to remove " + sc.getName() + "?", "Remove Screen " + sc.getName(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        if (!dc.checkInheritance(sc).isEmpty()) {
                            if (JOptionPane.showInternalConfirmDialog(this, "Warning! This screen is being inherited. Are you sure you want to remove it?", "Remove Screen " + sc.getName(), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                                return;
                            }
                        }
                        dc.removeScreen(sc);
                        model.removeScreen(sc);
                    }
                } catch (IOException | SQLException | ScreenNotFoundException | JTillException ex) {
                    JOptionPane.showInternalMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            duplicate.addActionListener((ActionEvent e) -> {
                try {
                    String name = JOptionPane.showInternalInputDialog(this, "Enter name for duplicate", "Duplicate " + sc.getName(), JOptionPane.OK_CANCEL_OPTION);
                    if (name != null) {
                        Screen dup = sc.clone();
                        dup.setName(name);
                        model.addScreen(dc.addScreen(dup));
                    }
                } catch (IOException | SQLException ex) {
                    JOptionPane.showInternalMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            if (sc.getName().equals("DEFAULT")) {
                remove.setEnabled(false);
                rename.setEnabled(false);
            }

            JMenuItem inherit = new JMenuItem("Check Inheritance");
            inherit.addActionListener((ActionEvent e) -> {
                try {
                    List<Screen> screens = dc.checkInheritance(sc);
                    if (!screens.isEmpty()) {
                        String scList = "";
                        for (Screen s : screens) {
                            scList += "\n" + s.getName();
                        }
                        JOptionPane.showInternalMessageDialog(this, "This screen is inherited by-" + scList);
                    } else {
                        JOptionPane.showInternalMessageDialog(this, "This screen is not inherited");
                    }
                } catch (IOException | SQLException | JTillException ex) {
                    JOptionPane.showInternalMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            menu.add(rename);
            menu.add(remove);
//            menu.add(duplicate);
            menu.add(inherit);
            menu.show(evt.getComponent(), evt.getX(), evt.getY());
        } else {
            categoryCards.show(panelProducts, sc.getName());
            currentScreen = sc;
            txtVGap.setText(currentScreen.getvGap() + "");
            txtHGap.setText(currentScreen.gethGap() + "");
            if (sc.getInherits() != -1) {
                try {
                    Screen parent = dc.getScreen(sc.getInherits());
                    btnInherits.setText(parent.getName());
                } catch (IOException | SQLException | ScreenNotFoundException ex) {
                    sc.setInherits(-1);
                    try {
                        dc.updateScreen(sc);
                    } catch (IOException | SQLException | ScreenNotFoundException ex1) {
                        JOptionPane.showInternalMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    btnInherits.setText("NONE");
                }
            } else {
                btnInherits.setText("NONE");
            }
        }
    }//GEN-LAST:event_listMouseClicked

    private void btnInheritsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInheritsActionPerformed
        Screen par = ScreenSelectDialog.showDialog(this);
        if (par == null) {
            currentScreen.setInherits(-1);
            try {
                dc.updateScreen(currentScreen);
                btnInherits.setText("NONE");
            } catch (IOException | SQLException | ScreenNotFoundException ex) {
                JOptionPane.showInternalMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
            setButtons();
            return;
        }
        if (par.equals(currentScreen)) {
            JOptionPane.showInternalMessageDialog(this, "A screen cannot inherit itself", "Inheritance", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (par.getWidth() != currentScreen.getWidth() || par.getHeight() != currentScreen.getHeight()) {
            JOptionPane.showInternalMessageDialog(this, "Parent screens must have the same width and height", "Inheritance", JOptionPane.ERROR_MESSAGE);
            return;
        }
        currentScreen.setInherits(par.getId());
        try {
            dc.updateScreen(currentScreen);
            btnInherits.setText(par.getName());
        } catch (IOException | SQLException | ScreenNotFoundException ex) {
            JOptionPane.showInternalMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
        setButtons();
    }//GEN-LAST:event_btnInheritsActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        if (!Utilities.isNumber(txtVGap.getText()) || !Utilities.isNumber(txtHGap.getText())) {
            JOptionPane.showMessageDialog(this, "Must enter a number", "Save", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int vgap = Integer.parseInt(txtVGap.getText());
        int hgap = Integer.parseInt(txtHGap.getText());
        currentScreen.setvGap(vgap);
        currentScreen.sethGap(hgap);
        try {
            dc.updateScreen(currentScreen);
        } catch (IOException | SQLException | ScreenNotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex, "Save", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar bar;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnInherits;
    private javax.swing.JButton btnNewScreen;
    private javax.swing.JButton btnSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<String> list;
    private javax.swing.JPanel panelProducts;
    private javax.swing.JPanel panelScreenProperties;
    private javax.swing.JTextField txtHGap;
    private javax.swing.JTextField txtVGap;
    // End of variables declaration//GEN-END:variables
}
