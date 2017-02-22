/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread for handling incoming connections.
 *
 * @author David
 */
public class ConnectionThread extends Thread {

    private final DataConnectInterface dbConn;

    private ObjectInputStream obIn;
    private ObjectOutputStream obOut;

    private final Socket socket;

    private boolean conn_term = false;

    private String site;
    private Staff staff;

    private ConnectionData currentData;

    private final Semaphore sem;

    /**
     * Constructor for Connection thread.
     *
     * @param name the name of the thread.
     * @param s the socket used for this connection.
     */
    public ConnectionThread(String name, Socket s) {
        super(name);
        this.socket = s;
        this.dbConn = TillServer.getDataConnection();
        sem = new Semaphore(1);
    }

    public void sendLog(String message) throws IOException {
        try {
            sem.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        obOut.writeObject(new ConnectionData("LOG", message));
        sem.release();
    }

    @Override
    public void run() {
        try {
            obIn = new ObjectInputStream(socket.getInputStream());
            obOut = new ObjectOutputStream(socket.getOutputStream());
            obOut.flush();

            site = (String) obIn.readObject();

            if (!site.equals("NOPERM")) {

                boolean allow = dbConn.connectTill(site);

                if (!allow) {
                    obOut.writeObject(ConnectionData.create("DISALLOW"));
                    conn_term = true;
                } else {
                    obOut.writeObject(ConnectionData.create("ALLOW"));
                }
            }

            TillServer.g.increaceClientCount(site);
            TillServer.g.log(site + " has connected");

            while (!conn_term) {
                String input;

                Object o = obIn.readObject();
                try {
                    sem.acquire();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                currentData = (ConnectionData) o;
                input = currentData.getFlag();

                TillServer.g.log("Contact from " + site);

                String inp[] = input.split(",");
                final ConnectionData data = currentData.clone();

                switch (inp[0]) {
                    case "NEWPRODUCT": //Add a new product
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                newProduct(data);
                            }
                        }.start();
                        break;
                    case "REMOVEPRODUCT": //Remove a product
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeProduct(data);
                            }
                        }.start();
                        break;
                    case "PURCHASE": //Purchase a product
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                purchase(data);
                            }
                        }.start();
                        break;
                    case "GETPRODUCT": //Get a product
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getProduct(data);
                            }
                        }.start();
                        break;
                    case "UPDATEPRODUCT": //Update a product
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateProduct(data);
                            }
                        }.start();
                        break;
                    case "GETPRODUCTBARCODE": //Get a product by barcode
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getProductByBarcode(data);
                            }
                        }.start();
                        break;
                    case "CHECKBARCODE": //Check if a barcode is in use
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                checkBarcode(data);
                            }
                        }.start();
                        break;
                    case "SETSTOCK": //Set the stock level of a product
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                setStock(data);
                            }
                        }.start();
                        break;
                    case "GETPRODUCTSDISCOUNT": //Gets all discounts for a product
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getProductsDiscount(data);
                            }
                        }.start();
                        break;
                    case "GETPRODUCTCOUNT": //Get product count
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getProductCount();
                            }
                        }.start();
                        break;
                    case "GETALLPRODUCTS": //Get all products
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllProducts();
                            }
                        }.start();
                        break;
                    case "PRODUCTLOOKUP": //Product lookup
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                productLookup(data);
                            }
                        }.start();
                        break;
                    case "NEWCUSTOMER": //Add a new customer
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                newCustomer(data);
                            }
                        }.start();
                        break;
                    case "REMOVECUSTOMER": //Remove a customer
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeCustomer(data);
                            }
                        }.start();
                        break;
                    case "GETCUSTOMER": //Get a customer
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getCustomer(data);
                            }
                        }.start();
                        break;
                    case "GETCUSTOMERBYNAME": //Get a customer by name
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getCustomerByName(data);
                            }
                        }.start();
                        break;
                    case "GETCUSTOMERCOUNT": //Get customer count
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getCustomerCount();
                            }
                        }.start();
                        break;
                    case "UPDATECUSTOMER": //Update a customer
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateCustomer(data);
                            }
                        }.start();
                        break;
                    case "GETALLCUSTOMERS": //Get all customers
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllCustomers();
                            }
                        }.start();
                        break;
                    case "CUSTOMERLOOKUP": //Search for a customer
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                customerLookup(data);
                            }
                        }.start();
                        break;
                    case "ADDSTAFF": //Add a member of staff
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addStaff(data);
                            }
                        }.start();
                        break;
                    case "REMOVESTAFF": //Remove a member of staff
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeStaff(data);
                            }
                        }.start();
                        break;
                    case "GETSTAFF": //Get a member of staff
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getStaff(data);
                            }
                        }.start();
                        break;
                    case "UPDATESTAFF": //Update a member of staff
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateStaff(data);
                            }
                        }.start();
                        break;
                    case "GETALLSTAFF": //Get all the staff
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllStaff();
                            }
                        }.start();
                        break;
                    case "STAFFCOUNT": //Get the staff count
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                staffCount();
                            }
                        }.start();
                        break;
                    case "ADDSALE": //Add a sale
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addSale(data);
                            }
                        }.start();
                        break;
                    case "GETALLSALES": //Get all sales
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllSales();
                            }
                        }.start();
                        break;
                    case "GETSALE": //Get a sale
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getSale(data);
                            }
                        }.start();
                        break;
                    case "UPDATESALE": //Update a sale
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateSale(data);
                            }
                        }.start();
                        break;
                    case "GETSALEDATERANGE": //Get all sales within a date range
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getSaleDateRange(data);
                            }
                        }.start();
                        break;
                    case "SUSPENDSALE": //Suspend a sale
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                suspendSale(data);
                            }
                        }.start();
                        break;
                    case "RESUMESALE": //Resume a sale
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                resumeSale(data);
                            }
                        }.start();
                        break;
                    case "LOGIN": //Standard staff login
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                login(data);
                            }
                        }.start();
                        break;
                    case "TILLLOGIN": //Till login
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                tillLogin(data);
                            }
                        }.start();
                        break;
                    case "LOGOUT": //Logout
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                logout(data);
                            }
                        }.start();
                        break;
                    case "TILLLOGOUT": //Till logout
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                tillLogout(data);
                            }
                        }.start();
                        break;
                    case "ADDCATEGORY": //Add a category
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addCategory(data);
                            }
                        }.start();
                        break;
                    case "UPDATECATEGORY": //Update a category
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateCategory(data);
                            }
                        }.start();
                        break;
                    case "REMOVECATEGORY": //Remove a category
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeCategory(data);
                            }
                        }.start();
                        break;
                    case "GETCATEGORY": //Get a category
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getCategory(data);
                            }
                        }.start();
                        break;
                    case "GETALLCATEGORYS": //Get all categorys
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllCategorys();
                            }
                        }.start();
                        break;
                    case "GETPRODUCTSINCATEGORY": //Get all products in a category
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getProductsInCategory(data);
                            }
                        }.start();
                        break;
                    case "ADDDISCOUNT": //Add a discount
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addDiscount(data);
                            }
                        }.start();
                        break;
                    case "UPDATEDISCOUNT": //Update a discount
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateDiscount(data);
                            }
                        }.start();
                        break;
                    case "REMOVEDISCOUNT": //Remove a discount
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeDiscount(data);
                            }
                        }.start();
                        break;
                    case "GETDISCOUNT": //Get a discount
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getDiscount(data);
                            }
                        }.start();
                        break;
                    case "GETALLDISCOUNTS": //Get all discounts
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllDiscounts();
                            }
                        }.start();
                        break;
                    case "ADDTAX": //Add a new tax
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addTax(data);
                            }
                        }.start();
                        break;
                    case "REMOVETAX": //Remove a tax
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeTax(data);
                            }
                        }.start();
                        break;
                    case "GETTAX": //Get a tax
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getTax(data);
                            }
                        }.start();
                        break;
                    case "UPDATETAX": //Update a tax
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateTax(data);
                            }
                        }.start();
                        break;
                    case "GETALLTAX": //Get all tax
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllTax();
                            }
                        }.start();
                        break;
                    case "ADDVOUCHER": //Add a new voucher
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addVoucher(data);
                            }
                        }.start();
                        break;
                    case "REMOVEVOUCHER": //Remove a voucher
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeVoucher(data);
                            }
                        }.start();
                        break;
                    case "GETVOUCHER": //Get a voucher
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getVoucher(data);
                            }
                        }.start();
                        break;
                    case "UPDATEVOUCHER": //Update a voucher
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateVoucher(data);
                            }
                        }.start();
                        break;
                    case "GETALLVOUCHERS": //Get all vouchers
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllVouchers();
                            }
                        }.start();
                        break;
                    case "ADDSCREEN": //Add a new screen
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addScreen(data);
                            }
                        }.start();
                        break;
                    case "ADDBUTTON": //Add a new button
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addButton(data);
                            }
                        }.start();
                        break;
                    case "REMOVESCREEN": //Remove a screen
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeScreen(data);
                            }
                        }.start();
                        break;
                    case "REMOVEBUTTON": //Remove a button
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeButton(data);
                            }
                        }.start();
                        break;
                    case "UPDATESCREEN": //Update a screen
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateScreen(data);
                            }
                        }.start();
                        break;
                    case "UPDATEBUTTON": //Update a button
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateButton(data);
                            }
                        }.start();
                        break;
                    case "GETSCREEN": //Update a screen
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getScreen(data);
                            }
                        }.start();
                        break;
                    case "GETBUTTON": //Updatea a button
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getButton(data);
                            }
                        }.start();
                        break;
                    case "GETALLSCREENS": //Get all screens
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllScreens();
                            }
                        }.start();
                        break;
                    case "GETALLBUTTONS": //Get all buttons
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllButtons();
                            }
                        }.start();
                        break;
                    case "GETBUTTONSONSCREEN": //Get buttons on a screen
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getButtonsOnScreen(data);
                            }
                        }.start();
                        break;
                    case "DROPSCREENSANDBUTTONS":
                        try {
                            dbConn.deleteAllScreensAndButtons();
                        } catch (SQLException ex) {
                        }
                        break;
                    case "ASSISSTANCE":
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                assisstance(data);
                            }
                        }.start();
                        break;
                    case "TAKINGS":
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getTakings(data);
                            }
                        }.start();
                        break;
                    case "UNCASHEDSALES":
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getUncashedSales(data);
                            }
                        }.start();
                        break;
                    case "EMAIL":
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                sendEmail(data);
                            }
                        }.start();
                        break;
                    case "EMAILRECEIPT":
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                sendReceipt(data);
                            }
                        }.start();
                        break;
                    case "ADDTILL":
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addTill(data);
                            }
                        }.start();
                        break;
                    case "REMOVETILL":
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeTill(data);
                            }
                        }.start();
                        break;
                    case "GETTILL":
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getTill(data);
                            }
                        }.start();
                        break;
                    case "GETALLTILLS":
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllTills();
                            }
                        }.start();
                        break;
                    case "CONNECTTILL":
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                connectTill(data);
                            }
                        }.start();
                        break;
                    case "SETSETTING":
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                setSetting(data);
                            }
                        }.start();
                        break;
                    case "GETSETTING":
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getSetting(data);
                            }
                        }.start();
                        break;
                    case "CONNTERM": //Terminate the connection
                        conn_term = true;
                        if (staff != null) {
                            try {
                                dbConn.logout(staff);
                                dbConn.tillLogout(staff);
                            } catch (StaffNotFoundException ex) {
                            }
                        }
                        break;
                }
                sem.release();
            }
            TillServer.g.decreaseClientCount(site);
            TillServer.g.log(site + " has disconnected");
            socket.close();
        } catch (IOException e) {

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void newProduct(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (clone.getData() == null) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A null value was received"));
                    return;
                }
                Product p = (Product) clone.getData();
                Product newP = dbConn.addProduct(p);
                obOut.writeObject(ConnectionData.create("SUCC", newP));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void removeProduct(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (clone.getData() == null) {
                    obOut.writeObject(ConnectionData.create("FAIL", "No value was received"));
                    return;
                }
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An int value mussed be passed in"));
                    return;
                }
                int code = (int) clone.getData();
                dbConn.removeProduct(code);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | ProductNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void purchase(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (clone.getData() == null || clone.getData2() == null) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A null value was received"));
                    return;
                }
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An integer must be passed in"));
                    return;
                }
                if (!(clone.getData2() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An integer must be passed in"));
                    return;
                }
                int code = (int) clone.getData();
                int amount = (int) clone.getData2();
                int stock = dbConn.purchaseProduct(code, amount);
                obOut.writeObject(ConnectionData.create("SUCC", stock));
            } catch (ProductNotFoundException | SQLException | OutOfStockException ex) {
                TillServer.g.log(ex);
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getProduct(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (clone.getData() == null) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A null value was received"));
                    return;
                }
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An integerp must be passed here"));
                    return;
                }
                int code = (int) clone.getData();
                Product p = dbConn.getProduct(code);
                obOut.writeObject(ConnectionData.create("SUCC", p));
            } catch (ProductNotFoundException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void updateProduct(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Product)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A product must be passed here"));
                    return;
                }
                Product p = (Product) clone.getData();
                dbConn.updateProduct(p);
                obOut.writeObject(ConnectionData.create("SUCC", p));
            } catch (SQLException | ProductNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getProductByBarcode(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received"));
                    return;
                }
                String barcode = (String) clone.getData();
                Product p = dbConn.getProductByBarcode(barcode);
                obOut.writeObject(ConnectionData.create("SUCC", p));
            } catch (ProductNotFoundException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void checkBarcode(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                String barcode = (String) clone.getData();
                boolean inUse = dbConn.checkBarcode(barcode);
                obOut.writeObject(ConnectionData.create("SUCC", inUse));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void setStock(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                String[] inp = ((String) clone.getData()).split(",");
                int id = Integer.parseInt(inp[1]);
                int stock = Integer.parseInt(inp[2]);
                dbConn.setStock(id, stock);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | ProductNotFoundException ex) {
                ConnectionData.create("FAIL", ex);
            }
        } catch (IOException e) {

        }
    }

    private void getProductsDiscount(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Product)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Product must be received here"));
                    return;
                }
                Product p = (Product) clone.getData();
                List<Discount> discounts = dbConn.getProductsDiscount(p);
                obOut.writeObject(ConnectionData.create("SUCC", discounts));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getProductCount() {
        try {
            try {
                obOut.writeObject(dbConn.getProductCount());
            } catch (SQLException ex) {
                obOut.writeObject(-1);
            }
        } catch (IOException e) {

        }
    }

    private void getAllProducts() {
        try {
            try {
                List<Product> products = dbConn.getAllProducts();
                obOut.writeObject(products);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void productLookup(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                String terms = (String) clone.getData();
                List<Product> products = dbConn.productLookup(terms);
                obOut.writeObject(ConnectionData.create("SUCC", products));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void newCustomer(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Customer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Customer must be received here"));
                    return;
                }
                Customer c = (Customer) clone.getData();
                Customer newC = dbConn.addCustomer(c);
                obOut.writeObject(ConnectionData.create("SUCC", newC));
            } catch (SQLException e) {
                obOut.writeObject(ConnectionData.create("FAIL", e));
            }
        } catch (IOException e) {

        }
    }

    private void removeCustomer(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                dbConn.removeCustomer(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | CustomerNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getCustomer(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Customer c = dbConn.getCustomer(id);
                obOut.writeObject(ConnectionData.create("SUCC", c));
            } catch (CustomerNotFoundException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getCustomerByName(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                String name = (String) clone.getData();
                List<Customer> customers = dbConn.getCustomerByName(name);
                obOut.writeObject(ConnectionData.create("SUCC", customers));
            } catch (SQLException | CustomerNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getCustomerCount() {
        try {
            try {
                obOut.writeObject(dbConn.getCustomerCount());
            } catch (SQLException ex) {
                obOut.writeObject(-1);
            }
        } catch (IOException e) {

        }
    }

    private void updateCustomer(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Customer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Customer must be received here"));
                    return;
                }
                Customer c = (Customer) clone.getData();
                Customer customer = dbConn.updateCustomer(c);
                obOut.writeObject(ConnectionData.create("SUCC", customer));
            } catch (SQLException | CustomerNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getAllCustomers() {
        try {
            try {
                List<Customer> customers = dbConn.getAllCustomers();
                obOut.writeObject(customers);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void customerLookup(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (clone.getData() == null || !(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be passed here"));
                }
                String terms = (String) clone.getData();
                List<Customer> customers = dbConn.customerLookup(terms);
                obOut.writeObject(ConnectionData.create("SUCC", customers));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void addStaff(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Staff)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Staff must be received here"));
                    return;
                }
                Staff s = (Staff) clone.getData();
                Staff newS = dbConn.addStaff(s);
                obOut.writeObject(ConnectionData.create("NEW", newS));
            } catch (SQLException | StaffNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void removeStaff(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                dbConn.removeStaff(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | StaffNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getStaff(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Staff s = dbConn.getStaff(id);
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (StaffNotFoundException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void updateStaff(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Staff)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Staff must be received here"));
                    return;
                }
                Staff s = (Staff) clone.getData();
                Staff updatedStaff = dbConn.updateStaff(s);
                obOut.writeObject(ConnectionData.create("SUCC", updatedStaff));
            } catch (SQLException | StaffNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getAllStaff() {
        try {
            try {
                List<Staff> staffList = dbConn.getAllStaff();
                obOut.writeObject(staffList);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void staffCount() {
        try {
            try {
                obOut.writeObject(dbConn.staffCount());
            } catch (SQLException ex) {
                obOut.writeObject("FAIL");
            } catch (StaffNotFoundException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {

        }
    }

    private void addSale(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Sale)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Sale must be received here"));
                    return;
                }
                Sale s = (Sale) clone.getData();
                Sale newS = dbConn.addSale(s);
                obOut.writeObject(ConnectionData.create("SUCC", newS));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getAllSales() {
        try {
            try {
                List<Sale> sales = dbConn.getAllSales();
                obOut.writeObject(sales);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getSale(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Sale s = dbConn.getSale(id);
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (SQLException | SaleNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void updateSale(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Sale)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Sale must be received here"));
                    return;
                }
                Sale sale = (Sale) clone.getData();
                Sale s = dbConn.updateSale(sale);
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (SQLException | SaleNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getSaleDateRange(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Time)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Time must be received here"));
                    return;
                }
                if (!(clone.getData2() instanceof Time)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Time must be received here"));
                    return;
                }
                Time start = (Time) clone.getData();
                Time end = (Time) clone.getData2();
                List<Sale> sales = dbConn.getSalesInRange(start, end);
                obOut.writeObject(ConnectionData.create("SUCC", sales));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void suspendSale(ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Sale)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A Sale must be received here"));
                return;
            }
            if (!(clone.getData2() instanceof Staff)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A Staff must be received here"));
                return;
            }
            Sale sale = (Sale) clone.getData();
            Staff s = (Staff) clone.getData2();
            dbConn.suspendSale(sale, s);
            obOut.writeObject(ConnectionData.create("SUSPEND", "SUCCESS"));
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void resumeSale(ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Staff)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A Staff must be received here"));
                return;
            }
            Staff s = (Staff) clone.getData();
            Sale sale = dbConn.resumeSale(s);
            obOut.writeObject(ConnectionData.create("RESUME", sale));
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void login(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                if (!(clone.getData2() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                String username = (String) clone.getData();
                String password = (String) clone.getData2();
                Staff s = dbConn.login(username, password);
                ConnectionThread.this.staff = s;
                TillServer.g.log(staff.getName() + " has logged in");
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (SQLException | LoginException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void tillLogin(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Staff s = dbConn.tillLogin(id);
                ConnectionThread.this.staff = s;
                TillServer.g.log(staff.getName() + " has logged in from " + site);
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (SQLException | LoginException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void logout(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Staff)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Staff must be received here"));
                    return;
                }
                Staff s = (Staff) clone.getData();
                dbConn.logout(s);
                TillServer.g.log(staff.getName() + " has logged out");
                ConnectionThread.this.staff = null;
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (StaffNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void tillLogout(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Staff)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Staff must be received here"));
                    return;
                }
                Staff s = (Staff) clone.getData();
                dbConn.tillLogout(s);
                TillServer.g.log(staff.getName() + " has logged out");
                ConnectionThread.this.staff = null;
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (StaffNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void addCategory(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Category)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Category must be received here"));
                    return;
                }
                Category c = (Category) clone.getData();
                Category newC = dbConn.addCategory(c);
                obOut.writeObject(ConnectionData.create("SUCC", newC));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void updateCategory(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Category)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Category must be received here"));
                    return;
                }
                Category c = (Category) clone.getData();
                Category category = dbConn.updateCategory(c);
                obOut.writeObject(ConnectionData.create("SUCC", category));
            } catch (SQLException | CategoryNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void removeCategory(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                dbConn.removeCategory(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | CategoryNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getCategory(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Category c = dbConn.getCategory(id);
                obOut.writeObject(ConnectionData.create("SUCC", c));
            } catch (SQLException | CategoryNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getAllCategorys() {
        try {
            try {
                List<Category> categorys = dbConn.getAllCategorys();
                obOut.writeObject(categorys);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getProductsInCategory(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                List<Product> products = dbConn.getProductsInCategory(id);
                obOut.writeObject(ConnectionData.create("SUCC", products));
            } catch (SQLException | CategoryNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void addDiscount(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Discount)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Discount must be received here"));
                    return;
                }
                Discount d = (Discount) clone.getData();
                Discount newD = dbConn.addDiscount(d);
                obOut.writeObject(ConnectionData.create("SUCC", newD));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void updateDiscount(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Discount)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Discount must be received here"));
                    return;
                }
                Discount d = (Discount) clone.getData();
                Discount discount = dbConn.updateDiscount(d);
                obOut.writeObject(ConnectionData.create("SUCC", discount));
            } catch (SQLException | DiscountNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void removeDiscount(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                dbConn.removeDiscount(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | DiscountNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getDiscount(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Discount d = dbConn.getDiscount(id);
                obOut.writeObject(ConnectionData.create("SUCC", d));
            } catch (SQLException | DiscountNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getAllDiscounts() {
        try {
            try {
                List<Discount> discounts = dbConn.getAllDiscounts();
                obOut.writeObject(discounts);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void addTax(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Tax)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Tax must be received here"));
                    return;
                }
                Tax t = (Tax) clone.getData();
                Tax newT = dbConn.addTax(t);
                obOut.writeObject(ConnectionData.create("SUCC", newT));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void removeTax(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                dbConn.removeTax(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | TaxNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getTax(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Tax t = dbConn.getTax(id);
                obOut.writeObject(ConnectionData.create("SUCC", t));
            } catch (SQLException | TaxNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void updateTax(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Tax)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Tax must be received here"));
                    return;
                }
                Tax t = (Tax) clone.getData();
                Tax tax = dbConn.updateTax(t);
                obOut.writeObject(ConnectionData.create("SUCC", tax));
            } catch (SQLException | TaxNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getAllTax() {
        try {
            try {
                List<Tax> tax = dbConn.getAllTax();
                obOut.writeObject(tax);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void addVoucher(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Voucher)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Voucher must be received here"));
                    return;
                }
                Voucher v = (Voucher) clone.getData();
                Voucher newV = dbConn.addVoucher(v);
                obOut.writeObject(ConnectionData.create("SUCC", newV));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void removeVoucher(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                dbConn.removeVoucher(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | VoucherNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getVoucher(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Voucher v = dbConn.getVoucher(id);
                obOut.writeObject(ConnectionData.create("SUCC", v));
            } catch (SQLException | VoucherNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void updateVoucher(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Voucher)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Voucher must be received here"));
                    return;
                }
                Voucher v = (Voucher) clone.getData();
                Voucher voucher = dbConn.updateVoucher(v);
                obOut.writeObject(ConnectionData.create("SUCC", voucher));
            } catch (SQLException | VoucherNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getAllVouchers() {
        try {
            try {
                List<Voucher> voucher = dbConn.getAllVouchers();
                obOut.writeObject(voucher);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void addScreen(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Screen)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Screen must be received here"));
                    return;
                }
                Screen s = (Screen) clone.getData();
                Screen newS = dbConn.addScreen(s);
                obOut.writeObject(ConnectionData.create("SUCC", newS));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void addButton(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof TillButton)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A TillButton must be received here"));
                    return;
                }
                TillButton b = (TillButton) clone.getData();
                TillButton newB = dbConn.addButton(b);
                obOut.writeObject(ConnectionData.create("SUCC", newB));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void removeScreen(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Screen)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Screen must be received here"));
                    return;
                }
                Screen s = (Screen) clone.getData();
                dbConn.removeScreen(s);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | ScreenNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("SUCC", ex));
            }
        } catch (IOException e) {

        }
    }

    private void removeButton(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof TillButton)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A TillButton must be received here"));
                    return;
                }
                TillButton b = (TillButton) clone.getData();
                dbConn.removeButton(b);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | ButtonNotFoundException ex) {
                ConnectionData.create("FAIL", ex);
            }
        } catch (IOException e) {

        }
    }

    private void updateScreen(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Screen)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Screen must be received here"));
                    return;
                }
                Screen s = (Screen) clone.getData();
                dbConn.updateScreen(s);
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (SQLException | ScreenNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void updateButton(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof TillButton)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A TillButton must be received here"));
                    return;
                }
                TillButton b = (TillButton) clone.getData();
                dbConn.updateButton(b);
                obOut.writeObject(ConnectionData.create("SUCC", b));
            } catch (SQLException | ButtonNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getScreen(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Screen s = dbConn.getScreen(id);
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (SQLException | ScreenNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getButton(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                TillButton b = dbConn.getButton(id);
                obOut.writeObject(ConnectionData.create("SUCC", b));
            } catch (SQLException | ButtonNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void getAllScreens() {
        try {
            try {
                List<Screen> screens = dbConn.getAllScreens();
                obOut.writeObject(screens);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getAllButtons() {
        try {
            try {
                List<TillButton> buttons = dbConn.getAllButtons();
                obOut.writeObject(buttons);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getButtonsOnScreen(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Screen)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Screen must be received here"));
                    return;
                }
                Screen s = (Screen) clone.getData();
                List<TillButton> buttons = dbConn.getButtonsOnScreen(s);
                obOut.writeObject(ConnectionData.create("SUCC", buttons));
            } catch (SQLException | ScreenNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {

        }
    }

    private void assisstance(ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof String)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                return;
            }
            String message = (String) clone.getData();
            dbConn.assisstance(staff.getName() + " on terminal " + site + " has requested assistance with message:\n" + message);
            obOut.writeObject(ConnectionData.create("SUCC"));
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getTakings(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                String terminal = (String) clone.getData();
                BigDecimal t = dbConn.getTillTakings(terminal);
                obOut.writeObject(ConnectionData.create("GET", t));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getUncashedSales(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                String terminal = (String) clone.getData();
                List<Sale> sales = dbConn.getUncashedSales(terminal);
                obOut.writeObject(ConnectionData.create("GET", sales));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendEmail(ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof String)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                return;
            }
            String message = (String) clone.getData();
            dbConn.sendEmail(message);
            obOut.writeObject(ConnectionData.create("SUCC"));
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendReceipt(ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof String)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                return;
            }
            if (!(clone.getData2() instanceof Sale)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A Sale must be received here"));
                return;
            }
            String email = (String) clone.getData();
            Sale sale = (Sale) clone.getData2();
            dbConn.emailReceipt(email, sale);
            obOut.writeObject(ConnectionData.create("SUCC"));
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addTill(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Till)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Till must be received here"));
                    return;
                }
                Till t = (Till) clone.getData();
                Till newT = dbConn.addTill(t);
                obOut.writeObject(ConnectionData.create("ADD", newT));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void removeTill(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                dbConn.removeTill(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | TillNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getTill(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Till till = dbConn.getTill(id);
                obOut.writeObject(ConnectionData.create("GET", till));
            } catch (SQLException | TillNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getAllTills() {
        try {
            try {
                List<Till> tills = dbConn.getAllTills();
                obOut.writeObject(tills);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void connectTill(ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof String)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                return;
            }
            String t = (String) clone.getData();
            boolean allowed = dbConn.connectTill(t);
            obOut.writeObject(ConnectionData.create("CONNECT", allowed));
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setSetting(ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof String) || !(clone.getData() instanceof String)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                return;
            }
            String key = (String) clone.getData();
            String value = (String) clone.getData2();
            dbConn.setSetting(key, value);
            obOut.writeObject(ConnectionData.create("SUCC"));
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getSetting(ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof String)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                return;
            }
            String key = (String) clone.getData();
            String value = dbConn.getSettings(key);
            obOut.writeObject(ConnectionData.create("SUCC", value));
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
