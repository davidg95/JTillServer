/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author David
 */
public final class ProductEntryDialog extends javax.swing.JDialog {

    private final DataConnect dc = DataConnect.get();
    private String barcode;
    private static Product product;

    private String nextBarcode;

    private final DefaultComboBoxModel taxModel;
    private final DefaultComboBoxModel catModel;

    private boolean autoClose = false;

    private boolean editMode = false;

    /**
     * Creates new form ProductEntryDialog.
     *
     * @param parent the parent component.
     */
    public ProductEntryDialog(Window parent) {
        super(parent);
        taxModel = new DefaultComboBoxModel();
        catModel = new DefaultComboBoxModel();
        initCombos();
        initComponents();
        txtDepartment.setText("1 - Default");
        this.setIconImage(GUI.icon);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);
        setModal(true);
        showBarcodePanel();
    }

    public ProductEntryDialog(Window parent, String barcode) {
        this(parent);
        this.barcode = barcode;
        setTitle("Edit Product - " + barcode);
        CardLayout c = (CardLayout) container.getLayout();
        c.show(container, "card3");
        btnBack.setEnabled(false);
        txtName.requestFocus();
        autoClose = true;
    }

    public ProductEntryDialog(Window parent, Product product) {
        this(parent);
        editMode = true;
        btnCreate.setText("Save");
        btnBack.setText("Close");
        ProductEntryDialog.product = product;
        setProduct();
        setTitle("Edit Product - " + product.getBarcode());
        showProductPanel();
    }

    private void showBarcodePanel() {
        CardLayout c = (CardLayout) container.getLayout();
        c.show(container, "card2");
        txtBarcode.requestFocus();
    }

    private void showProductPanel() {
        CardLayout c = (CardLayout) container.getLayout();
        c.show(container, "card3");
        txtName.requestFocus();
    }

    private void initCombos() {
        try {
            List<Tax> taxes = dc.getAllTax();
            for (Tax t : taxes) {
                taxModel.addElement(t);
            }
            List<Category> cats = dc.getAllCategorys();
            for (Category c : cats) {
                catModel.addElement(c);
            }
        } catch (IOException | SQLException ex) {
            Logger.getLogger(ProductEntryDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void showDialog(Component parent) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        product = null;
        ProductEntryDialog dialog = new ProductEntryDialog(window);
        dialog.setVisible(true);
    }

    public static Product showDialog(Component parent, String barcode) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        product = null;
        ProductEntryDialog dialog = new ProductEntryDialog(window, barcode);
        dialog.setVisible(true);
        return product;
    }

    public static Product showDialog(Component parent, Product product) {
        Window window = null;
        if (parent instanceof Dialog || parent instanceof Frame) {
            window = (Window) parent;
        }
        ProductEntryDialog dialog = new ProductEntryDialog(window, product);
        dialog.setVisible(true);
        return product;
    }

    private void resetPanels() {
        txtBarcode.setText("");

        txtName.setText("");
        txtShortName.setText("");
        txtOrderCode.setText("0");
        txtPrice.setText("");
        txtCostPrice.setText("");
        txtPackSize.setText("");
        txtMin.setText("0");
        txtMax.setText("0");
        txtComments.setText("");
        txtIngredients.setText("");
        radStandard.doClick();
        chkIncVat.setSelected(false);
        chkTrackStock.setSelected(true);
        txtScale.setText("1");
        txtScaleName.setText("");
        txtCostPercentage.setText("0");
        txtPriceLimit.setText("0");

        cmbVat.setSelectedIndex(0);
        cmbCat.setSelectedIndex(0);
        tabbed.setSelectedIndex(0);
    }

    public void setProduct() {
        barcode = product.getBarcode();
        txtName.setText(product.getLongName());
        txtShortName.setText(product.getShortName());
        txtOrderCode.setText("" + product.getOrderCode());
        cmbCat.setSelectedItem(product.getCategory());
        txtDepartment.setText(product.getCategory().getDepartment().toString());
        cmbVat.setSelectedItem(product.getTax());
        chkTrackStock.setSelected(product.isTrackStock());
        if (product.isTrackStock()) {
            txtMin.setText(product.getMinStockLevel() + "");
            txtMax.setText(product.getMaxStockLevel() + "");
        }
        txtIngredients.setText(product.getIngredients());
        txtComments.setText(product.getComments());
        if (product.isOpen()) {
            radOpen.doClick();
            if (product.getScaleName().equals("Price")) {
                chkScale.setSelected(true);
                txtScaleName.setEnabled(true);
                txtScaleName.setText(product.getScaleName());
                txtScale.setEnabled(true);
                txtScale.setText(product.getScale() + "");
            }
            txtCostPercentage.setText(product.getCostPercentage() + "");
            txtPriceLimit.setText(product.getPriceLimit().toString());
        } else {
            radStandard.doClick();
            txtPrice.setText(product.getPrice().toString());
            txtCostPrice.setText(product.getCostPrice().toString());
            txtPackSize.setText(product.getPackSize() + "");
            calculateUnitCost();
            calculateGP();
            chkIncVat.setSelected(product.isPriceIncVat());
        }
    }

    private void sortBarcode() throws IOException, SQLException, JTillException {
        if (chkNext.isSelected()) {
            String upc = dc.getSetting("UPC_PREFIX"); //Get the UPC Prefix
            int length = Integer.parseInt(dc.getSetting("BARCODE_LENGTH")); //Get the barcode length
            if (!upc.equals("")) { //Check that the UPC has been set
                while (true) {
                    int lengthToAdd = length - upc.length() - 1; //Work out how many more digits need added
                    String ref = dc.getSetting("NEXT_PLU"); //Get the next PLU number
                    int n = Integer.parseInt(ref);
                    int max = (int) Math.pow(10, length - upc.length() - 1);
                    int remaining = max - n;
                    if (remaining == 0) {
                        chkNext.setSelected(false);
                        txtBarcode.setEnabled(true);
                        throw new JTillException("There are no more avaliable barcodes for this UPC Prefix");
                    }
                    n++;
                    nextBarcode = Integer.toString(n); // Increase it then convert it to a String

                    lengthToAdd -= ref.length(); //Subtract the length to find out how many digits need added
                    for (int i = 1; i <= lengthToAdd; i++) {
                        ref = 0 + ref; //Pad it out with zero's to make up the length
                    }
                    String barcode = upc + ref; //Join them all together
                    int checkDigit = Utilities.calculateCheckDigit(barcode);
                    barcode = barcode + checkDigit;
                    if (!dc.checkBarcode(barcode)) { //Check the barcode is not already int use
                        this.barcode = barcode;
                        break; //break from the while loop
                    } else {
                        dc.setSetting("NEXT_PLU", nextBarcode);
                    }
                }
            } else {
                throw new JTillException("You have not specified a UPC Company Prefix. This must be done before generating your own barcodes. Go to Setup -> Plu Settings to do this.");
            }
            //If excecution reaches here, it means an unused barcode as been generated
        } else if (chkAssignNextPrivate.isSelected()) {
            while (true) {
                String nextPrivate = dc.getSetting("NEXT_PRIVATE"); //Get the next barcode
                int n = Integer.parseInt(nextPrivate);
                n++;
                nextBarcode = Integer.toString(n); // Increase it then convert it to a String

                String barcode = nextPrivate; //Join them all together
                if (!dc.checkBarcode(barcode)) { //Check the barcode is not already int use
                    this.barcode = barcode;
                    break; //break from the while loop
                } else {
                    dc.setSetting("NEXT_PRIVATE", nextBarcode);
                }
            }
        } else {
            //Get the barcode from the user, check what they entered is valid
            String barcode = txtBarcode.getText();
            if (barcode.equals("")) {
                txtBarcode.setSelectionStart(0);
                txtBarcode.setSelectionEnd(barcode.length());
                throw new JTillException("You must enter a barcode");
            } else if (!Utilities.validateBarcodeLenth(barcode)) {
                txtBarcode.setSelectionStart(0);
                txtBarcode.setSelectionEnd(barcode.length());
                throw new JTillException("Barcode must be 8, 12, 13 or 14 digits long");
            } else if (!barcode.matches("[0-9]+")) {
                txtBarcode.setSelectionStart(0);
                txtBarcode.setSelectionEnd(barcode.length());
                throw new JTillException("Must only contain numbers");
            }
            if (!Utilities.validateBarcode(barcode)) {
                txtBarcode.setSelectionStart(0);
                txtBarcode.setSelectionEnd(barcode.length());
                throw new JTillException("Invalid check digit");
            }
            if (dc.checkBarcode(barcode)) {
                txtBarcode.setSelectionStart(0);
                txtBarcode.setSelectionEnd(barcode.length());
                throw new JTillException("Barcode is already in use");
            }
            this.barcode = txtBarcode.getText();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        typeGroup = new javax.swing.ButtonGroup();
        container = new javax.swing.JPanel();
        panBarcode = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        chkAssignNextPrivate = new javax.swing.JCheckBox();
        chkNext = new javax.swing.JCheckBox();
        txtBarcode = new javax.swing.JTextField();
        btnCopyDetails = new javax.swing.JButton();
        btnEnter = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        panProduct = new javax.swing.JPanel();
        btnCreate = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        radStandard = new javax.swing.JRadioButton();
        radOpen = new javax.swing.JRadioButton();
        panelStandard = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        txtCostPrice = new javax.swing.JTextField();
        txtUnitCost = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        chkIncVat = new javax.swing.JCheckBox();
        btnCalculate = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtGP = new javax.swing.JTextField();
        txtPackSize = new javax.swing.JTextField();
        panelOpen = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        txtScale = new javax.swing.JTextField();
        txtPriceLimit = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        chkScale = new javax.swing.JCheckBox();
        txtCostPercentage = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtScaleName = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        cmbVat = new javax.swing.JComboBox<>();
        jPanel4 = new javax.swing.JPanel();
        lblMin = new javax.swing.JLabel();
        chkTrackStock = new javax.swing.JCheckBox();
        lblMax = new javax.swing.JLabel();
        txtMax = new javax.swing.JTextField();
        txtMin = new javax.swing.JTextField();
        tabbed = new javax.swing.JTabbedPane();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtComments = new javax.swing.JTextArea();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtIngredients = new javax.swing.JTextArea();
        jPanel8 = new javax.swing.JPanel();
        txtOrderCode = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        cmbCat = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        txtShortName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtDepartment = new javax.swing.JTextField();

        setTitle("Create New Product");
        setResizable(false);

        container.setLayout(new java.awt.CardLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Barcode Settings")));

        jLabel1.setText("Enter Barcode:");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Auto-Generate"));

        chkAssignNextPrivate.setText("Assign Next Private");
        chkAssignNextPrivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkAssignNextPrivateActionPerformed(evt);
            }
        });

        chkNext.setText("Assign next UPC");
        chkNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkNextActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkNext)
                    .addComponent(chkAssignNextPrivate))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(chkNext)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkAssignNextPrivate)
                .addContainerGap())
        );

        txtBarcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBarcodeActionPerformed(evt);
            }
        });
        txtBarcode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtBarcodeKeyTyped(evt);
            }
        });

        btnCopyDetails.setText("Copy Details");
        btnCopyDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCopyDetailsActionPerformed(evt);
            }
        });

        btnEnter.setText("Create");
        btnEnter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEnterActionPerformed(evt);
            }
        });

        btnClose.setText("Cancel");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnEdit.setText("Edit Existing");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnEdit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 62, Short.MAX_VALUE)
                        .addComponent(btnEnter, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCopyDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(132, 132, 132)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnCopyDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnEnter, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnClose, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnEdit)))
                .addContainerGap())
        );

        javax.swing.GroupLayout panBarcodeLayout = new javax.swing.GroupLayout(panBarcode);
        panBarcode.setLayout(panBarcodeLayout);
        panBarcodeLayout.setHorizontalGroup(
            panBarcodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBarcodeLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panBarcodeLayout.setVerticalGroup(
            panBarcodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBarcodeLayout.createSequentialGroup()
                .addGap(234, 234, 234)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(276, Short.MAX_VALUE))
        );

        container.add(panBarcode, "card2");

        btnCreate.setText("Create");
        btnCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateActionPerformed(evt);
            }
        });

        btnBack.setText("Back");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Price Settings"));

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Price Type"));

        typeGroup.add(radStandard);
        radStandard.setSelected(true);
        radStandard.setText("Standard");
        radStandard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radStandardActionPerformed(evt);
            }
        });

        typeGroup.add(radOpen);
        radOpen.setText("Open");
        radOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radOpenActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radStandard)
                    .addComponent(radOpen))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(radStandard)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radOpen)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelStandard.setBorder(javax.swing.BorderFactory.createTitledBorder("Standard"));

        jLabel19.setText("Pack Size:");

        txtCostPrice.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtCostPriceFocusLost(evt);
            }
        });

        txtUnitCost.setEditable(false);

        jLabel9.setText("Cost Price (£):");

        jLabel14.setText("Price (£):");

        chkIncVat.setText("Price includes VAT");

        btnCalculate.setText("Calculate based on GP");
        btnCalculate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCalculateActionPerformed(evt);
            }
        });

        jLabel3.setText("Unit Cost (£):");

        txtPrice.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtPriceFocusLost(evt);
            }
        });

        jLabel4.setText("GP:");

        txtGP.setEditable(false);

        javax.swing.GroupLayout panelStandardLayout = new javax.swing.GroupLayout(panelStandard);
        panelStandard.setLayout(panelStandardLayout);
        panelStandardLayout.setHorizontalGroup(
            panelStandardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStandardLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelStandardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelStandardLayout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtGP, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(panelStandardLayout.createSequentialGroup()
                        .addGroup(panelStandardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelStandardLayout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtCostPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(4, 4, 4)
                                .addComponent(jLabel19))
                            .addComponent(btnCalculate))
                        .addGap(2, 2, 2)
                        .addGroup(panelStandardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelStandardLayout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(txtPackSize)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3))
                            .addComponent(chkIncVat))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtUnitCost, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE))))
        );
        panelStandardLayout.setVerticalGroup(
            panelStandardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStandardLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelStandardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelStandardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel14)
                        .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelStandardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(txtGP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelStandardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtCostPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)
                    .addComponent(jLabel3)
                    .addComponent(txtUnitCost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPackSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelStandardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCalculate, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkIncVat))
                .addContainerGap())
        );

        panelOpen.setBorder(javax.swing.BorderFactory.createTitledBorder("Open"));

        jLabel21.setText("Scale Factor:");
        jLabel21.setEnabled(false);

        txtScale.setText("1");
        txtScale.setEnabled(false);

        txtPriceLimit.setEnabled(false);

        jLabel20.setText("Scale Name:");
        jLabel20.setEnabled(false);

        chkScale.setText("Use Scale");
        chkScale.setEnabled(false);
        chkScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkScaleActionPerformed(evt);
            }
        });

        txtCostPercentage.setEnabled(false);

        jLabel12.setText("Price Limit:");
        jLabel12.setEnabled(false);

        txtScaleName.setText("Price");
        txtScaleName.setEnabled(false);

        jLabel17.setText("Cost Percentage");
        jLabel17.setEnabled(false);

        javax.swing.GroupLayout panelOpenLayout = new javax.swing.GroupLayout(panelOpen);
        panelOpen.setLayout(panelOpenLayout);
        panelOpenLayout.setHorizontalGroup(
            panelOpenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOpenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelOpenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel17)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelOpenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelOpenLayout.createSequentialGroup()
                        .addComponent(txtScaleName, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtScale, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkScale))
                    .addGroup(panelOpenLayout.createSequentialGroup()
                        .addComponent(txtCostPercentage, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPriceLimit, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelOpenLayout.setVerticalGroup(
            panelOpenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOpenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelOpenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(txtScaleName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21)
                    .addComponent(txtScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkScale))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelOpenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCostPercentage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(jLabel12)
                    .addComponent(txtPriceLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabel22.setText("VAT:");

        cmbVat.setModel(taxModel);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbVat, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelOpen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelStandard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(panelStandard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelOpen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel22)
                            .addComponent(cmbVat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Stock Settings"));

        lblMin.setText("Min Stock Level:");

        chkTrackStock.setSelected(true);
        chkTrackStock.setText("Track Stock");
        chkTrackStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkTrackStockActionPerformed(evt);
            }
        });

        lblMax.setText("Max Stock Level:");

        txtMax.setText("0");

        txtMin.setText("0");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMin)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMin, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMax)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMax, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkTrackStock)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMin)
                    .addComponent(txtMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMax)
                    .addComponent(txtMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkTrackStock))
                .addContainerGap())
        );

        txtComments.setColumns(20);
        txtComments.setRows(5);
        jScrollPane1.setViewportView(txtComments);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 613, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabbed.addTab("Comments", jPanel6);

        txtIngredients.setColumns(20);
        txtIngredients.setRows(5);
        jScrollPane3.setViewportView(txtIngredients);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 613, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabbed.addTab("Ingredients", jPanel7);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Main Settings"));

        txtOrderCode.setText("0");

        jLabel13.setText("Order Code:");

        jLabel6.setText("Short Name:");

        jLabel7.setText("Category:");

        cmbCat.setModel(catModel);
        cmbCat.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbCatItemStateChanged(evt);
            }
        });

        jLabel2.setText("Product Name:");

        jLabel5.setText("Department:");

        txtDepartment.setEditable(false);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(275, 275, 275)
                        .addComponent(txtShortName, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbCat, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(6, 6, 6)
                        .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6)
                        .addGap(106, 106, 106)
                        .addComponent(jLabel13)
                        .addGap(6, 6, 6)
                        .addComponent(txtOrderCode, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtShortName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(txtOrderCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(cmbCat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(txtDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout panProductLayout = new javax.swing.GroupLayout(panProduct);
        panProduct.setLayout(panProductLayout);
        panProductLayout.setHorizontalGroup(
            panProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panProductLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(panProductLayout.createSequentialGroup()
                        .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCreate))
                    .addComponent(tabbed, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panProductLayout.setVerticalGroup(
            panProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panProductLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCreate))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        container.add(panProduct, "card3");

        getContentPane().add(container, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateActionPerformed
        String name = txtName.getText();
        String shortName = txtShortName.getText();
        String orderCode = txtOrderCode.getText();
        Category category = (Category) cmbCat.getSelectedItem();
        if (!Utilities.isNumber(txtMin.getText())) {
            JOptionPane.showMessageDialog(this, "Not all fields have been filled out correctly", "Create New Product", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int minStock = Integer.parseInt(txtMin.getText());
        if (minStock < 0) {
            JOptionPane.showMessageDialog(this, "Not all fields have been filled out correctly", "Create New Product", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!Utilities.isNumber(txtMax.getText())) {
            JOptionPane.showMessageDialog(this, "Not all fields have been filled out correctly", "Create New Product", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int maxStock = Integer.parseInt(txtMax.getText());
        if (maxStock < 0) {
            JOptionPane.showMessageDialog(this, "Not all fields have been filled out correctly", "Create New Product", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean trackStock = chkTrackStock.isSelected();
        Tax tax = (Tax) cmbVat.getSelectedItem();
        String comments = txtComments.getText();
        String ingredients = txtIngredients.getText();

        if (radStandard.isSelected()) {
            if (!Utilities.isNumber(txtPrice.getText())) {
                JOptionPane.showMessageDialog(this, "Not all fields have been filled out correctly", "Create New Product", JOptionPane.ERROR_MESSAGE);
                return;
            }
            BigDecimal price = new BigDecimal(txtPrice.getText());
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Not all fields have been filled out correctly", "Create New Product", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Utilities.isNumber(txtCostPrice.getText())) {
                JOptionPane.showMessageDialog(this, "Not all fields have been filled out correctly", "Create New Product", JOptionPane.ERROR_MESSAGE);
                return;
            }
            BigDecimal costPrice = new BigDecimal(txtCostPrice.getText());
            if (costPrice.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Not all fields have been filled out correctly", "Create New Product", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int packSize = Integer.parseInt(txtPackSize.getText());
            if (packSize < 1) {
                JOptionPane.showMessageDialog(this, "Pack size must be 1 or greater", "Create New Product", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean incVat = chkIncVat.isSelected();
            product = new Product(name, shortName, barcode, orderCode, category, comments, tax, price, costPrice, incVat, packSize, 0, minStock, maxStock, 0, 0, trackStock, ingredients);
        } else {
            String strCost = txtCostPercentage.getText();
            if (!Utilities.isNumber(strCost)) {
                JOptionPane.showMessageDialog(this, "Must enter a number for cost", "Open Product", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double costPercentage = Double.parseDouble(strCost);
            if (costPercentage < 0 || costPercentage > 100) {
                JOptionPane.showMessageDialog(this, "Cost percentage must be between 0 and 100", "Create Product", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Utilities.isNumber(txtPriceLimit.getText())) {
                JOptionPane.showMessageDialog(this, "Must enter a number", "Create Product", JOptionPane.ERROR_MESSAGE);
                return;
            }
            BigDecimal priceLimit = new BigDecimal(txtPriceLimit.getText());
            String scaleName = "Price";
            double scale = 1;
            if (chkScale.isSelected()) {
                scaleName = txtScaleName.getText();
                String strScale = txtScale.getText();
                if (!Utilities.isNumber(strScale)) {
                    JOptionPane.showMessageDialog(this, "Must enter a number for scale factor", "Open Product", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                scale = Double.parseDouble(strScale);
                if (scale <= 0) {
                    JOptionPane.showMessageDialog(this, "Scale must be greater than 0", "Open Product", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            product = new Product(name, shortName, barcode, orderCode, category, comments, tax, scale, scaleName, costPercentage, priceLimit, ingredients);
        }
        try {
            if (editMode) {
                product.save();
                JOptionPane.showMessageDialog(this, "Product has been saved", "Edit Product", JOptionPane.INFORMATION_MESSAGE);
            } else {
                product = dc.addProduct(product);
                if (chkAssignNextPrivate.isSelected()) {
                    if (nextBarcode != null) {
                        dc.setSetting("NEXT_PRIVATE", nextBarcode);
                        nextBarcode = null;
                    }
                } else {
                    if (nextBarcode != null) {
                        dc.setSetting("NEXT_PLU", nextBarcode);
                        nextBarcode = null;
                    }
                }
                JOptionPane.showMessageDialog(this, "New product has been created", "New Product", JOptionPane.INFORMATION_MESSAGE);
                resetPanels();
                txtBarcode.setText("");
                setTitle("Create New Product");
                CardLayout c = (CardLayout) container.getLayout();
                c.show(container, "card2");
                txtBarcode.requestFocus();
                if (autoClose) {
                    setVisible(false);
                }
            }
        } catch (HeadlessException | IOException | NumberFormatException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnCreateActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        if (editMode) {
            setVisible(false);
        } else {
            CardLayout c = (CardLayout) container.getLayout();
            c.show(container, "card2");
        }
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnEnterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnterActionPerformed
        try {
            sortBarcode();
            setTitle("Create New Product - " + barcode);
            CardLayout c = (CardLayout) container.getLayout();
            c.show(container, "card3");
            txtName.requestFocus();
        } catch (IOException | SQLException | JTillException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnEnterActionPerformed

    private void txtBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBarcodeActionPerformed
        btnEnter.doClick();
    }//GEN-LAST:event_txtBarcodeActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void chkNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkNextActionPerformed
        txtBarcode.setEnabled(!chkNext.isSelected());
        if (chkNext.isSelected()) {
            chkAssignNextPrivate.setSelected(false);
        }
    }//GEN-LAST:event_chkNextActionPerformed

    private void chkScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkScaleActionPerformed
        txtScale.setEnabled(chkScale.isSelected());
        txtScaleName.setEnabled(chkScale.isSelected());
    }//GEN-LAST:event_chkScaleActionPerformed

    private void txtBarcodeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBarcodeKeyTyped
        char c = evt.getKeyChar();
        String s = Character.toString(c);
        if (!(s.matches("[0-9]+"))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtBarcodeKeyTyped

    private void chkAssignNextPrivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAssignNextPrivateActionPerformed
        txtBarcode.setEnabled(!chkAssignNextPrivate.isSelected());
        if (chkAssignNextPrivate.isSelected()) {
            chkNext.setSelected(false);
        }
    }//GEN-LAST:event_chkAssignNextPrivateActionPerformed

    private void btnCalculateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCalculateActionPerformed
        if (txtCostPrice.getText().isEmpty() || txtPackSize.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter values for cost price and pack size", "Calculate GP", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String input = JOptionPane.showInputDialog(this, "Enter GP %", "Calculate Price", JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.isEmpty()) {
            return;
        }
        BigDecimal gp = new BigDecimal(input).divide(new BigDecimal(100));
        if (gp.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Value must be greater than 0", "Calculate Price", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BigDecimal cost = new BigDecimal(txtCostPrice.getText());
        int packSize = Integer.parseInt(txtPackSize.getText());

        BigDecimal indCost = cost.divide(new BigDecimal(packSize), 2, 6);
        BigDecimal price = indCost.multiply(gp.add(BigDecimal.ONE));
        txtPrice.setText("" + price.setScale(2, 6));
        txtGP.setText(gp.multiply(new BigDecimal(100)).toString() + "%");
    }//GEN-LAST:event_btnCalculateActionPerformed

    private void btnCopyDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCopyDetailsActionPerformed
        try {
            sortBarcode();
            Product toCopy = ProductSelectDialog.showDialog(this); //Copy details from an existing product
            if (toCopy != null) {
                toCopy.setStock(0);
                toCopy.setBarcode(this.barcode);
                try {
                    dc.addProduct(toCopy);
                    JOptionPane.showMessageDialog(this, "New Product created", "New Product", JOptionPane.INFORMATION_MESSAGE);
                    txtBarcode.setText("");
                    txtBarcode.requestFocus();
                } catch (IOException | SQLException ex) {
                    JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (IOException | SQLException | JTillException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnCopyDetailsActionPerformed

    private void chkTrackStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTrackStockActionPerformed
        lblMin.setEnabled(chkTrackStock.isSelected());
        lblMax.setEnabled(chkTrackStock.isSelected());
        txtMin.setEnabled(chkTrackStock.isSelected());
        txtMax.setEnabled(chkTrackStock.isSelected());
    }//GEN-LAST:event_chkTrackStockActionPerformed

    private void txtCostPriceFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCostPriceFocusLost
        calculateUnitCost();
        calculateGP();
    }//GEN-LAST:event_txtCostPriceFocusLost

    private void radStandardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radStandardActionPerformed
        for (Component c : panelStandard.getComponents()) {
            c.setEnabled(true);
        }
        for (Component c : panelOpen.getComponents()) {
            c.setEnabled(false);
        }
    }//GEN-LAST:event_radStandardActionPerformed

    private void radOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radOpenActionPerformed
        for (Component c : panelStandard.getComponents()) {
            c.setEnabled(false);
        }
        for (Component c : panelOpen.getComponents()) {
            c.setEnabled(true);
        }
        txtScaleName.setEnabled(chkScale.isSelected());
        txtScale.setEnabled(chkScale.isSelected());
    }//GEN-LAST:event_radOpenActionPerformed

    private void txtPriceFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPriceFocusLost
        calculateGP();
    }//GEN-LAST:event_txtPriceFocusLost

    private void cmbCatItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbCatItemStateChanged
        txtDepartment.setText((((Category) cmbCat.getSelectedItem()).getDepartment()).toString());
    }//GEN-LAST:event_cmbCatItemStateChanged

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        product = ProductSelectDialog.showDialog(this);
        if (product == null) {
            return;
        }
        setProduct();
        editMode = true;
        btnCreate.setText("Save");
        btnBack.setText("Close");
        setTitle("Edit Product - " + product.getBarcode());
        showProductPanel();
    }//GEN-LAST:event_btnEditActionPerformed

    private void calculateUnitCost() {
        try {
            if (txtCostPrice.getText().isEmpty()) {
                return;
            }
            if (txtPackSize.getText().isEmpty()) {
                return;
            }

            BigDecimal cost = new BigDecimal(txtCostPrice.getText());
            int packSize = Integer.parseInt(txtPackSize.getText());

            BigDecimal unit = cost.divide(new BigDecimal(packSize), 2, 6);
            txtUnitCost.setText(unit.toString());
        } catch (NumberFormatException e) {
            txtUnitCost.setText("INVALID INPUT");
        }
    }

    private void calculateGP() {
        try {
            if (txtCostPrice.getText().isEmpty()) {
                return;
            }
            if (txtPackSize.getText().isEmpty()) {
                return;
            }
            if (txtPrice.getText().isEmpty()) {
                return;
            }

            BigDecimal price = new BigDecimal(txtPrice.getText());
            BigDecimal cost = new BigDecimal(txtCostPrice.getText());
            int packSize = Integer.parseInt(txtPackSize.getText());

            BigDecimal unit = cost.divide(new BigDecimal(packSize), 2, 6);

            if (unit.compareTo(BigDecimal.ZERO) == 0) {
                txtGP.setText("---");
                return;
            }
            BigDecimal gp = ((price.divide(unit, 2, 6)).subtract(BigDecimal.ONE)).multiply(new BigDecimal(100));
            txtGP.setText(gp.toString() + "%");
        } catch (NumberFormatException e) {
            txtGP.setText("INVALID INPUT");
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnCalculate;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnCopyDetails;
    private javax.swing.JButton btnCreate;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnEnter;
    private javax.swing.JCheckBox chkAssignNextPrivate;
    private javax.swing.JCheckBox chkIncVat;
    private javax.swing.JCheckBox chkNext;
    private javax.swing.JCheckBox chkScale;
    private javax.swing.JCheckBox chkTrackStock;
    private javax.swing.JComboBox<String> cmbCat;
    private javax.swing.JComboBox<String> cmbVat;
    private javax.swing.JPanel container;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblMax;
    private javax.swing.JLabel lblMin;
    private javax.swing.JPanel panBarcode;
    private javax.swing.JPanel panProduct;
    private javax.swing.JPanel panelOpen;
    private javax.swing.JPanel panelStandard;
    private javax.swing.JRadioButton radOpen;
    private javax.swing.JRadioButton radStandard;
    private javax.swing.JTabbedPane tabbed;
    private javax.swing.JTextField txtBarcode;
    private javax.swing.JTextArea txtComments;
    private javax.swing.JTextField txtCostPercentage;
    private javax.swing.JTextField txtCostPrice;
    private javax.swing.JTextField txtDepartment;
    private javax.swing.JTextField txtGP;
    private javax.swing.JTextArea txtIngredients;
    private javax.swing.JTextField txtMax;
    private javax.swing.JTextField txtMin;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtOrderCode;
    private javax.swing.JTextField txtPackSize;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtPriceLimit;
    private javax.swing.JTextField txtScale;
    private javax.swing.JTextField txtScaleName;
    private javax.swing.JTextField txtShortName;
    private javax.swing.JTextField txtUnitCost;
    private javax.swing.ButtonGroup typeGroup;
    // End of variables declaration//GEN-END:variables
}
