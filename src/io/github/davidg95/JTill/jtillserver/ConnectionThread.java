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
import java.net.Socket;
import java.sql.SQLException;
import java.util.Date;
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
                    case "GETIMAGE":
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getImage();
                            }
                        }.start();
                        break;
                    case "GETFXIMAGE":
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getFXImage();
                            }
                        }.start();
                        break;
                    case "SETIMAGEPATH":
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                setImagePath(data);
                            }
                        }.start();
                        break;
                    case "ASSISSTANCE":
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                assisstance(data);
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
            ConnectionData clone = data.clone();
            Product p = (Product) clone.getData();
            dbConn.addProduct(p);
        } catch (SQLException ex) {
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void removeProduct(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int code = (int) clone.getData();
                dbConn.removeProduct(code);
                obOut.writeObject("SUCC");
            } catch (SQLException | ProductNotFoundException ex) {
                obOut.writeObject("FAIL");
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void purchase(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int code = (int) clone.getData();
                int amount = (int) clone.getData2();
                int stock = dbConn.purchaseProduct(code, amount);
                obOut.writeObject(stock);
            } catch (ProductNotFoundException | SQLException | OutOfStockException ex) {
                TillServer.g.log(ex);
                obOut.writeObject("FAIL");
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getProduct(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int code = (int) clone.getData();
                Product p = dbConn.getProduct(code);
                obOut.writeObject(p);
            } catch (ProductNotFoundException | SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void updateProduct(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Product p = (Product) clone.getData();
                dbConn.updateProduct(p);
                obOut.writeObject(p);
            } catch (SQLException | ProductNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getProductByBarcode(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                String barcode = (String) clone.getData();
                Product p = dbConn.getProductByBarcode(barcode);
                obOut.writeObject(p);
            } catch (ProductNotFoundException | SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void checkBarcode(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                String barcode = (String) clone.getData();
                boolean inUse = dbConn.checkBarcode(barcode);
                if (inUse) {
                    obOut.writeObject("USED");
                } else {
                    obOut.writeObject("NOTUSED");
                }
            } catch (SQLException ex) {
                obOut.writeObject(ex.getMessage());
            }
        } catch (IOException e) {

        }
    }

    private void setStock(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                String[] inp = ((String) clone.getData()).split(",");
                int id = Integer.parseInt(inp[1]);
                int stock = Integer.parseInt(inp[2]);
                dbConn.setStock(id, stock);
                obOut.writeObject("SUCC");
            } catch (SQLException ex) {
                obOut.writeObject(ex.getMessage());
            } catch (ProductNotFoundException ex) {
                obOut.writeObject("FAIL");
            }
        } catch (IOException e) {

        }
    }

    private void getProductsDiscount(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Product p = (Product) clone.getData();

                List<Discount> discounts = dbConn.getProductsDiscount(p);

                obOut.writeObject(discounts);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
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
                String terms = (String) clone.getData();
                List<Product> products = dbConn.productLookup(terms);
                obOut.writeObject(products);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void newCustomer(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Customer c = (Customer) clone.getData();
                dbConn.addCustomer(c);
            } catch (SQLException e) {
            }
        } catch (IOException e) {

        }
    }

    private void removeCustomer(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int id = (int) clone.getData();
                dbConn.removeCustomer(id);
                obOut.writeObject("SUCC");
            } catch (SQLException | CustomerNotFoundException ex) {
                obOut.writeObject("FAIL");
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getCustomer(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int id = (int) clone.getData();
                Customer c = dbConn.getCustomer(id);
                obOut.writeObject(c);
            } catch (CustomerNotFoundException | SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getCustomerByName(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                String name = (String) clone.getData();
                List<Customer> customers = dbConn.getCustomerByName(name);
                obOut.writeObject(customers);
            } catch (SQLException | CustomerNotFoundException ex) {
                obOut.writeObject(ex);
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
                Customer c = (Customer) clone.getData();
                Customer customer = dbConn.updateCustomer(c);
                obOut.writeObject(customer);
            } catch (SQLException | CustomerNotFoundException ex) {
                obOut.writeObject(ex);
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
                String terms = (String) clone.getData();
                List<Customer> customers = dbConn.customerLookup(terms);
                obOut.writeObject(customers);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void addStaff(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Staff s = (Staff) clone.getData();
                dbConn.addStaff(s);
            } catch (SQLException | StaffNotFoundException ex) {
            }
        } catch (IOException e) {

        }
    }

    private void removeStaff(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int id = (int) clone.getData();
                dbConn.removeStaff(id);
                obOut.writeObject("SUCC");
            } catch (SQLException | StaffNotFoundException ex) {
                obOut.writeObject("FAIL");
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getStaff(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int id = (int) clone.getData();
                Staff s = dbConn.getStaff(id);
                obOut.writeObject(s);
            } catch (StaffNotFoundException | SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void updateStaff(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Staff s = (Staff) clone.getData();
                Staff updatedStaff = dbConn.updateStaff(s);
                obOut.writeObject(updatedStaff);
            } catch (SQLException | StaffNotFoundException ex) {
                obOut.writeObject(ex);
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
                Sale s = (Sale) clone.getData();
                dbConn.addSale(s);
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
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
                int id = (int) clone.getData();
                Sale s = dbConn.getSale(id);
                obOut.writeObject(s);
            } catch (SQLException | SaleNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void updateSale(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Sale sale = (Sale) clone.getData();
                Sale s = dbConn.updateSale(sale);
            } catch (SQLException | SaleNotFoundException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {

        }
    }

    private void getSaleDateRange(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Date start = (Date) clone.getData();
                Date end = (Date) clone.getData2();
                List<Sale> sales = dbConn.getSalesInRange(start, end);
                obOut.writeObject(sales);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void suspendSale(ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            Sale sale = (Sale) clone.getData();
            Staff s = (Staff) clone.getData2();
            dbConn.suspendSale(sale, s);
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void resumeSale(ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            Staff s = (Staff) clone.getData();
            Sale sale = dbConn.resumeSale(s);
            obOut.writeObject(sale);
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void login(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                String username = (String) clone.getData();
                String password = (String) clone.getData2();
                Staff s = dbConn.login(username, password);
                ConnectionThread.this.staff = s;
                TillServer.g.log(staff.getName() + " has logged in");
                obOut.writeObject(s);
            } catch (SQLException | LoginException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void tillLogin(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int id = (int) clone.getData();
                Staff s = dbConn.tillLogin(id);
                ConnectionThread.this.staff = s;
                TillServer.g.log(staff.getName() + " has logged in from " + site);
                obOut.writeObject(s);
            } catch (SQLException | LoginException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void logout(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Staff s = (Staff) clone.getData();
                dbConn.logout(s);
                TillServer.g.log(staff.getName() + " has logged out");
                ConnectionThread.this.staff = null;
                obOut.writeObject("SUCC");
            } catch (StaffNotFoundException ex) {
                obOut.writeObject("FAIL");
            }
        } catch (IOException e) {

        }
    }

    private void tillLogout(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Staff s = (Staff) clone.getData();
                dbConn.tillLogout(s);
                TillServer.g.log(staff.getName() + " has logged out");
                ConnectionThread.this.staff = null;
                obOut.writeObject("SUCC");
            } catch (StaffNotFoundException ex) {
                obOut.writeObject("FAIL");
            }
        } catch (IOException e) {

        }
    }

    private void addCategory(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Category c = (Category) clone.getData();
                dbConn.addCategory(c);
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {

        }
    }

    private void updateCategory(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Category c = (Category) clone.getData();
                Category category = dbConn.updateCategory(c);
                obOut.writeObject(category);
            } catch (SQLException | CategoryNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void removeCategory(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int id = (int) clone.getData();
                dbConn.removeCategory(id);
                obOut.writeObject("SUCC");
            } catch (SQLException ex) {
                obOut.writeObject(ex.getErrorCode());
            } catch (CategoryNotFoundException ex) {
                obOut.writeObject("FAIL");
            }
        } catch (IOException e) {

        }
    }

    private void getCategory(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int id = (int) clone.getData();
                Category c = dbConn.getCategory(id);
                obOut.writeObject(c);
            } catch (SQLException | CategoryNotFoundException ex) {
                obOut.writeObject(ex);
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
                int id = (int) clone.getData();
                List<Product> products = dbConn.getProductsInCategory(id);
                obOut.writeObject(products);
            } catch (SQLException | CategoryNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void addDiscount(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Discount d = (Discount) clone.getData();
                dbConn.addDiscount(d);
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {

        }
    }

    private void updateDiscount(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Discount d = (Discount) clone.getData();
                Discount discount = dbConn.updateDiscount(d);
                obOut.writeObject(discount);
            } catch (SQLException | DiscountNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void removeDiscount(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int id = (int) clone.getData();
                dbConn.removeDiscount(id);
                obOut.writeObject("SUCC");
            } catch (SQLException ex) {
                obOut.writeObject(ex.getErrorCode());
            } catch (DiscountNotFoundException ex) {
                obOut.writeObject("FAIL");
            }
        } catch (IOException e) {

        }
    }

    private void getDiscount(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int id = (int) clone.getData();
                Discount d = dbConn.getDiscount(id);
                obOut.writeObject(d);
            } catch (SQLException | DiscountNotFoundException ex) {
                obOut.writeObject(ex);
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
                Tax t = (Tax) clone.getData();
                dbConn.addTax(t);
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {

        }
    }

    private void removeTax(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int id = (int) clone.getData();
                dbConn.removeTax(id);
                obOut.writeObject("SUCC");
            } catch (SQLException ex) {
                obOut.writeObject(ex.getErrorCode());
            } catch (TaxNotFoundException ex) {
                obOut.writeObject("FAIL");
            }
        } catch (IOException e) {

        }
    }

    private void getTax(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int id = (int) clone.getData();
                Tax t = dbConn.getTax(id);
                obOut.writeObject(t);
            } catch (SQLException | TaxNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void updateTax(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Tax t = (Tax) clone.getData();
                Tax tax = dbConn.updateTax(t);
                obOut.writeObject(tax);
            } catch (SQLException | TaxNotFoundException ex) {
                obOut.writeObject(ex);
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
                Voucher v = (Voucher) clone.getData();
                dbConn.addVoucher(v);
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {

        }
    }

    private void removeVoucher(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int id = (int) clone.getData();
                dbConn.removeVoucher(id);
                obOut.writeObject("SUCC");
            } catch (SQLException ex) {
                obOut.writeObject(ex.getErrorCode());
            } catch (VoucherNotFoundException ex) {
                obOut.writeObject("FAIL");
            }
        } catch (IOException e) {

        }
    }

    private void getVoucher(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int id = (int) clone.getData();
                Voucher v = dbConn.getVoucher(id);
                obOut.writeObject(v);
            } catch (SQLException | VoucherNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void updateVoucher(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Voucher v = (Voucher) clone.getData();
                Voucher voucher = dbConn.updateVoucher(v);
                obOut.writeObject(voucher);
            } catch (SQLException | VoucherNotFoundException ex) {
                obOut.writeObject(ex);
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
                Screen s = (Screen) clone.getData();
                dbConn.addScreen(s);
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {

        }
    }

    private void addButton(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Button b = (Button) clone.getData();
                dbConn.addButton(b);
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {

        }
    }

    private void removeScreen(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Screen s = (Screen) clone.getData();
                dbConn.removeScreen(s);
                obOut.writeObject("SUCC");
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ScreenNotFoundException ex) {
                obOut.writeObject("FAIL");
            }
        } catch (IOException e) {

        }
    }

    private void removeButton(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Button b = (Button) clone.getData();
                dbConn.removeButton(b);
                obOut.writeObject("SUCC");
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ButtonNotFoundException ex) {
                obOut.writeObject("FAIL");
            }
        } catch (IOException e) {

        }
    }

    private void updateScreen(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Screen s = (Screen) clone.getData();
                dbConn.updateScreen(s);
                obOut.writeObject(s);
            } catch (SQLException | ScreenNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void updateButton(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                Button b = (Button) clone.getData();
                dbConn.updateButton(b);
                obOut.writeObject(b);
            } catch (SQLException | ButtonNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getScreen(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int id = (int) clone.getData();
                Screen s = dbConn.getScreen(id);
                obOut.writeObject(s);
            } catch (SQLException | ScreenNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getButton(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int id = (int) clone.getData();
                Button b = dbConn.getButton(id);
                obOut.writeObject(b);
            } catch (SQLException | ButtonNotFoundException ex) {
                obOut.writeObject(ex);
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
                List<Button> buttons = dbConn.getAllButtons();
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
                Screen s = (Screen) clone.getData();
                List<Button> buttons = dbConn.getButtonsOnScreen(s);
                obOut.writeObject(buttons);
            } catch (SQLException | ScreenNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getImage() {
        try {
            java.awt.Image image = dbConn.getImage();
            obOut.writeObject(image);
        } catch (IOException ex) {

        }
    }

    private void getFXImage() {
        try {
            javafx.scene.image.Image image = dbConn.getFXImage();
            obOut.writeObject(image);
        } catch (IOException ex) {

        }
    }

    private void setImagePath(ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            String path = (String) clone.getData();
            dbConn.setImagePath(path);
        } catch (IOException ex) {
        }
    }

    private void assisstance(ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            String message = (String) clone.getData();
            dbConn.assisstance(staff.getName() + " on terminal " + site + " has requested assisstance with message:\n" + message);
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
