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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread for handling incoming connections.
 *
 * @author David
 */
public class ConnectionThread extends Thread {

    private final Data data;
    private final DataConnectInterface dbConn;

    private ObjectInputStream obIn;
    private ObjectOutputStream obOut;

    private final Socket socket;

    private boolean conn_term = false;

    private String site;
    private Staff staff;

    private ConnectionData currentData;

    /**
     * Constructor for Connection thread.
     *
     * @param name the name of the thread.
     * @param s the socket used for this connection.
     * @param data the data object.
     */
    public ConnectionThread(String name, Socket s, Data data) {
        super(name);
        this.socket = s;
        this.data = data;
        this.dbConn = TillServer.getDataConnection();
    }

    @Override
    public void run() {
        try {
            obIn = new ObjectInputStream(socket.getInputStream());
            obOut = new ObjectOutputStream(socket.getOutputStream());
            obOut.flush();

            site = (String) obIn.readObject();

            //data.addConnection(site);
            TillServer.g.increaceClientCount(site);
            TillServer.g.log(site + " has connected");

            while (!conn_term) {
//                String input = (String) obIn.readObject();
                String input;

                Object o = obIn.readObject();
                if (o instanceof ConnectionData) {
                    currentData = (ConnectionData) o;
                    input = currentData.getFlag();
                } else {
                    input = (String) o;
                }

                TillServer.g.log("Contact from " + site);

                String inp[] = input.split(",");

                switch (inp[0]) {
                    case "NEWPRODUCT": //Add a new product
                        new Thread() {
                            @Override
                            public void run() {
                                newProduct();
                            }
                        }.start();
                        break;
                    case "REMOVEPRODUCT": //Remove a product
                        new Thread() {
                            @Override
                            public void run() {
                                removeProduct();
                            }
                        }.start();
                        break;
                    case "PURCHASE": //Purchase a product
                        new Thread() {
                            @Override
                            public void run() {
                                purchase();
                            }
                        }.start();
                        break;
                    case "GETPRODUCT": //Get a product
                        new Thread() {
                            @Override
                            public void run() {
                                getProduct();
                            }
                        }.start();
                        break;
                    case "UPDATEPRODUCT": //Update a product
                        new Thread() {
                            @Override
                            public void run() {
                                updateProduct();
                            }
                        }.start();
                        break;
                    case "GETPRODUCTBARCODE": //Get a product by barcode
                        new Thread() {
                            @Override
                            public void run() {
                                getProductByBarcode();
                            }
                        }.start();
                        break;
                    case "CHECKBARCODE": //Check if a barcode is in use
                        new Thread() {
                            @Override
                            public void run() {
                                checkBarcode();
                            }
                        }.start();
                        break;
                    case "SETSTOCK": //Set the stock level of a product
                        new Thread() {
                            @Override
                            public void run() {
                                setStock();
                            }
                        }.start();
                        break;
                    case "GETPRODUCTSDISCOUNT": //Gets all discounts for a product
                        new Thread() {
                            @Override
                            public void run() {
                                getProductsDiscount();
                            }
                        }.start();
                        break;
                    case "GETPRODUCTCOUNT": //Get product count
                        new Thread() {
                            @Override
                            public void run() {
                                getProductCount();
                            }
                        }.start();
                        break;
                    case "GETALLPRODUCTS": //Get all products
                        new Thread() {
                            @Override
                            public void run() {
                                getAllProducts();
                            }
                        }.start();
                        break;
                    case "NEWCUSTOMER": //Add a new customer
                        new Thread() {
                            @Override
                            public void run() {
                                newCustomer();
                            }
                        }.start();
                        break;
                    case "REMOVECUSTOMER": //Remove a customer
                        new Thread() {
                            @Override
                            public void run() {
                                removeCustomer();
                            }
                        }.start();
                        break;
                    case "GETCUSTOMER": //Get a customer
                        new Thread() {
                            @Override
                            public void run() {
                                getCustomer();
                            }
                        }.start();
                        break;
                    case "GETCUSTOMERBYNAME": //Get a customer by name
                        new Thread() {
                            @Override
                            public void run() {
                                getCustomerByName();
                            }
                        }.start();
                        break;
                    case "GETCUSTOMERCOUNT": //Get customer count
                        new Thread() {
                            @Override
                            public void run() {
                                getCustomerCount();
                            }
                        }.start();
                        break;
                    case "UPDATECUSTOMER": //Update a customer
                        new Thread() {
                            @Override
                            public void run() {
                                updateCustomer();
                            }
                        }.start();
                        break;
                    case "GETALLCUSTOMERS": //Get all customers
                        new Thread() {
                            @Override
                            public void run() {
                                getAllCustomers();
                            }
                        }.start();
                        break;
                    case "ADDSTAFF": //Add a member of staff
                        new Thread() {
                            @Override
                            public void run() {
                                addStaff();
                            }
                        }.start();
                        break;
                    case "REMOVESTAFF": //Remove a member of staff
                        new Thread() {
                            @Override
                            public void run() {
                                removeStaff();
                            }
                        }.start();
                        break;
                    case "GETSTAFF": //Get a member of staff
                        new Thread() {
                            @Override
                            public void run() {
                                getStaff();
                            }
                        }.start();
                        break;
                    case "UPDATESTAFF": //Update a member of staff
                        new Thread() {
                            @Override
                            public void run() {
                                updateStaff();
                            }
                        }.start();
                        break;
                    case "GETALLSTAFF": //Get all the staff
                        new Thread() {
                            @Override
                            public void run() {
                                getAllStaff();
                            }
                        }.start();
                        break;
                    case "STAFFCOUNT": //Get the staff count
                        new Thread() {
                            @Override
                            public void run() {
                                staffCount();
                            }
                        }.start();
                        break;
                    case "ADDSALE": //Add a sale
                        new Thread() {
                            @Override
                            public void run() {
                                addSale();
                            }
                        }.start();
                        break;
                    case "GETALLSALES": //Get all sales
                        new Thread() {
                            @Override
                            public void run() {
                                getAllSales();
                            }
                        }.start();
                        break;
                    case "GETSALE": //Get a sale
                        new Thread() {
                            @Override
                            public void run() {
                                getSale();
                            }
                        }.start();
                        break;
                    case "GETSALEDATERANGE": //Get all sales within a date range
                        new Thread() {
                            @Override
                            public void run() {
                                getSaleDateRange();
                            }
                        }.start();
                        break;
                    case "LOGIN": //Standard staff login
                        new Thread() {
                            @Override
                            public void run() {
                                login();
                            }
                        }.start();
                        break;
                    case "TILLLOGIN": //Till login
                        new Thread() {
                            @Override
                            public void run() {
                                tillLogin();
                            }
                        }.start();
                        break;
                    case "LOGOUT": //Logout
                        new Thread() {
                            @Override
                            public void run() {
                                logout();
                            }
                        }.start();
                        break;
                    case "TILLLOGOUT": //Till logout
                        new Thread() {
                            @Override
                            public void run() {
                                tillLogout();
                            }
                        }.start();
                        break;
                    case "ADDCATEGORY": //Add a category
                        new Thread() {
                            @Override
                            public void run() {
                                addCategory();
                            }
                        }.start();
                        break;
                    case "UPDATECATEGORY": //Update a category
                        new Thread() {
                            @Override
                            public void run() {
                                updateCategory();
                            }
                        }.start();
                        break;
                    case "REMOVECATEGORY": //Remove a category
                        new Thread() {
                            @Override
                            public void run() {
                                removeCategory();
                            }
                        }.start();
                        break;
                    case "GETCATEGORY": //Get a category
                        new Thread() {
                            @Override
                            public void run() {
                                getCategory();
                            }
                        }.start();
                        break;
                    case "GETALLCATEGORYS": //Get all categorys
                        new Thread() {
                            @Override
                            public void run() {
                                getAllCategorys();
                            }
                        }.start();
                        break;
                    case "GETPRODUCTSINCATEGORY": //Get all products in a category
                        new Thread() {
                            @Override
                            public void run() {
                                getProductsInCategory();
                            }
                        }.start();
                        break;
                    case "ADDDISCOUNT": //Add a discount
                        new Thread() {
                            @Override
                            public void run() {
                                addDiscount();
                            }
                        }.start();
                        break;
                    case "UPDATEDISCOUNT": //Update a discount
                        new Thread() {
                            @Override
                            public void run() {
                                updateDiscount();
                            }
                        }.start();
                        break;
                    case "REMOVEDISCOUNT": //Remove a discount
                        new Thread() {
                            @Override
                            public void run() {
                                removeDiscount();
                            }
                        }.start();
                        break;
                    case "GETDISCOUNT": //Get a discount
                        new Thread() {
                            @Override
                            public void run() {
                                getDiscount();
                            }
                        }.start();
                        break;
                    case "GETALLDISCOUNTS": //Get all discounts
                        new Thread() {
                            @Override
                            public void run() {
                                getAllDiscounts();
                            }
                        }.start();
                        break;
                    case "ADDTAX": //Add a new tax
                        new Thread() {
                            @Override
                            public void run() {
                                addTax();
                            }
                        }.start();
                        break;
                    case "REMOVETAX": //Remove a tax
                        new Thread() {
                            @Override
                            public void run() {
                                removeTax();
                            }
                        }.start();
                        break;
                    case "GETTAX": //Get a tax
                        new Thread() {
                            @Override
                            public void run() {
                                getTax();
                            }
                        }.start();
                        break;
                    case "UPDATETAX": //Update a tax
                        new Thread() {
                            @Override
                            public void run() {
                                updateTax();
                            }
                        }.start();
                        break;
                    case "GETALLTAX": //Get all tax
                        new Thread() {
                            @Override
                            public void run() {
                                getAllTax();
                            }
                        }.start();
                        break;
                    case "ADDVOUCHER": //Add a new voucher
                        new Thread() {
                            @Override
                            public void run() {
                                addVoucher();
                            }
                        }.start();
                        break;
                    case "REMOVEVOUCHER": //Remove a voucher
                        new Thread() {
                            @Override
                            public void run() {
                                removeVoucher();
                            }
                        }.start();
                        break;
                    case "GETVOUCHER": //Get a voucher
                        new Thread() {
                            @Override
                            public void run() {
                                getVoucher();
                            }
                        }.start();
                        break;
                    case "UPDATEVOUCHER": //Update a voucher
                        new Thread() {
                            @Override
                            public void run() {
                                updateVoucher();
                            }
                        }.start();
                        break;
                    case "GETALLVOUCHERS": //Get all vouchers
                        new Thread() {
                            @Override
                            public void run() {
                                getAllVouchers();
                            }
                        }.start();
                        break;
                    case "ADDSCREEN": //Add a new screen
                        new Thread() {
                            @Override
                            public void run() {
                                addScreen();
                            }
                        }.start();
                        break;
                    case "ADDBUTTON": //Add a new button
                        new Thread() {
                            @Override
                            public void run() {
                                addButton();
                            }
                        }.start();
                        break;
                    case "REMOVESCREEN": //Remove a screen
                        new Thread() {
                            @Override
                            public void run() {
                                removeScreen();
                            }
                        }.start();
                        break;
                    case "REMOVEBUTTON": //Remove a button
                        new Thread() {
                            @Override
                            public void run() {
                                removeButton();
                            }
                        }.start();
                        break;
                    case "UPDATESCREEN": //Update a screen
                        new Thread() {
                            @Override
                            public void run() {
                                updateScreen();
                            }
                        }.start();
                        break;
                    case "UPDATEBUTTON": //Update a button
                        new Thread() {
                            @Override
                            public void run() {
                                updateButton();
                            }
                        }.start();
                        break;
                    case "GETSCREEN": //Update a screen
                        new Thread() {
                            @Override
                            public void run() {
                                getScreen();
                            }
                        }.start();
                        break;
                    case "GETBUTTON": //Updatea a button
                        new Thread() {
                            @Override
                            public void run() {
                                getButton();
                            }
                        }.start();
                        break;
                    case "GETALLSCREENS": //Get all screens
                        new Thread() {
                            @Override
                            public void run() {
                                getAllScreens();
                            }
                        }.start();
                        break;
                    case "GETALLBUTTONS": //Get all buttons
                        new Thread() {
                            @Override
                            public void run() {
                                getAllButtons();
                            }
                        }.start();
                        break;
                    case "GETBUTTONSONSCREEN": //Get buttons on a screen
                        new Thread() {
                            @Override
                            public void run() {
                                getButtonsOnScreen();
                            }
                        }.start();
                        break;
                    case "DROPSCREENSANDBUTTONS":
                        try {
                            dbConn.deleteAllScreensAndButtons();
                        } catch (SQLException ex) {
                        }
                    case "CONNTERM": //Terminate the connection
                        conn_term = true;
                        if (staff != null) {
                            try {
                                data.logout(staff.getId());
                                data.tillLogout(staff.getId());
                            } catch (StaffNotFoundException ex) {
                            }
                        }
                        break;
                }
            }
//            data.removeConnection(site);
            TillServer.g.decreaseClientCount(site);
            TillServer.g.log(site + " has disconnected");
            socket.close();
        } catch (IOException e) {

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void newProduct() {
        try {
            Product p = (Product) currentData.getData();
            dbConn.addProduct(p);
        } catch (SQLException ex) {
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void removeProduct() {
        try {
            try {
                int code = (int) currentData.getData();
                dbConn.removeProduct(code);
                obOut.writeObject("SUCC");
            } catch (SQLException | ProductNotFoundException ex) {
                obOut.writeObject("FAIL");
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void purchase() {
        try {
            try {
                int code = (int) currentData.getData();
                int stock = dbConn.purchaseProduct(code);
                obOut.writeObject(stock);
            } catch (ProductNotFoundException | SQLException | OutOfStockException ex) {
                TillServer.g.log(ex);
                obOut.writeObject("FAIL");
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getProduct() {
        try {
            try {
                int code = (int) currentData.getData();
                Product p = dbConn.getProduct(code);
                obOut.writeObject(p);
            } catch (ProductNotFoundException | SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void updateProduct() {
        try {
            try {
                Product p = (Product) currentData.getData();
                dbConn.updateProduct(p);
                obOut.writeObject(p);
            } catch (SQLException | ProductNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getProductByBarcode() {
        try {
            try {
                String barcode = (String) currentData.getData();
                Product p = dbConn.getProductByBarcode(barcode);
                obOut.writeObject(p);
            } catch (ProductNotFoundException | SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void checkBarcode() {
        try {
            try {
                String barcode = (String) currentData.getData();
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

    private void setStock() {
        try {
            try {
                String[] inp = ((String) currentData.getData()).split(",");
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

    private void getProductsDiscount() {
        try {
            try {
                Product p = (Product) currentData.getData();

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

    private void newCustomer() {
        try {
            try {
                Customer c = (Customer) currentData.getData();
                dbConn.addCustomer(c);
            } catch (SQLException e) {
            }
        } catch (IOException e) {

        }
    }

    private void removeCustomer() {
        try {
            try {
                int id = (int) currentData.getData();
                dbConn.removeCustomer(id);
                obOut.writeObject("SUCC");
            } catch (SQLException | CustomerNotFoundException ex) {
                obOut.writeObject("FAIL");
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getCustomer() {
        try {
            try {
                int id = (int) currentData.getData();
                Customer c = dbConn.getCustomer(id);
                obOut.writeObject(c);
            } catch (CustomerNotFoundException | SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getCustomerByName() {
        try {
            try {
                String name = (String) currentData.getData();
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

    private void updateCustomer() {
        try {
            try {
                Customer c = (Customer) currentData.getData();
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

    private void addStaff() {
        try {
            try {
                Staff s = (Staff) currentData.getData();
                dbConn.addStaff(s);
            } catch (SQLException | StaffNotFoundException ex) {
            }
        } catch (IOException e) {

        }
    }

    private void removeStaff() {
        try {
            try {
                int id = (int) currentData.getData();
                dbConn.removeStaff(id);
                obOut.writeObject("SUCC");
            } catch (SQLException | StaffNotFoundException ex) {
                obOut.writeObject("FAIL");
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getStaff() {
        try {
            try {
                int id = (int) currentData.getData();
                Staff s = dbConn.getStaff(id);
                obOut.writeObject(s);
            } catch (StaffNotFoundException | SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void updateStaff() {
        try {
            try {
                Staff s = (Staff) currentData.getData();
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

    private void addSale() {
        try {
            try {
                Sale s = (Sale) currentData.getData();
                data.addSale(s);
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

    private void getSale() {
        try {
            try {
                int id = (int) currentData.getData();
                Sale s = dbConn.getSale(id);
                obOut.writeObject(s);
            } catch (SQLException | SaleNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getSaleDateRange() {
        try {
            try {
                Date start = (Date) currentData.getData();
                Date end = (Date) currentData.getData2();
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

    private void login() {
        try {
            try {
                String username = (String) currentData.getData();
                String password = (String) currentData.getData2();
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

    private void tillLogin() {
        try {
            try {
                int id = (int) currentData.getData();
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

    private void logout() {
        try {
            try {
                int id = (int) currentData.getData();
                dbConn.logout(id);
                TillServer.g.log(staff.getName() + " has logged out");
                ConnectionThread.this.staff = null;
                obOut.writeObject("SUCC");
            } catch (StaffNotFoundException ex) {
                obOut.writeObject("FAIL");
            }
        } catch (IOException e) {

        }
    }

    private void tillLogout() {
        try {
            try {
                int id = (int) currentData.getData();
                dbConn.tillLogout(id);
                TillServer.g.log(staff.getName() + " has logged out");
                ConnectionThread.this.staff = null;
                obOut.writeObject("SUCC");
            } catch (StaffNotFoundException ex) {
                obOut.writeObject("FAIL");
            }
        } catch (IOException e) {

        }
    }

    private void addCategory() {
        try {
            try {
                Category c = (Category) currentData.getData();
                dbConn.addCategory(c);
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {

        }
    }

    private void updateCategory() {
        try {
            try {
                Category c = (Category) currentData.getData();
                Category category = dbConn.updateCategory(c);
                obOut.writeObject(category);
            } catch (SQLException | CategoryNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void removeCategory() {
        try {
            try {
                int id = (int) currentData.getData();
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

    private void getCategory() {
        try {
            try {
                int id = (int) currentData.getData();
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

    private void getProductsInCategory() {
        try {
            try {
                int id = (int) currentData.getData();
                List<Product> products = dbConn.getProductsInCategory(id);
                obOut.writeObject(products);
            } catch (SQLException | CategoryNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void addDiscount() {
        try {
            try {
                Discount d = (Discount) currentData.getData();
                dbConn.addDiscount(d);
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {

        }
    }

    private void updateDiscount() {
        try {
            try {
                Discount d = (Discount) currentData.getData();
                Discount discount = dbConn.updateDiscount(d);
                obOut.writeObject(discount);
            } catch (SQLException | DiscountNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void removeDiscount() {
        try {
            try {
                int id = (int) currentData.getData();
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

    private void getDiscount() {
        try {
            try {
                int id = (int) currentData.getData();
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

    private void addTax() {
        try {
            try {
                Tax t = (Tax) currentData.getData();
                dbConn.addTax(t);
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {

        }
    }

    private void removeTax() {
        try {
            try {
                int id = (int) currentData.getData();
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

    private void getTax() {
        try {
            try {
                int id = (int) currentData.getData();
                Tax t = dbConn.getTax(id);
                obOut.writeObject(t);
            } catch (SQLException | TaxNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void updateTax() {
        try {
            try {
                Tax t = (Tax) currentData.getData();
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

    private void addVoucher() {
        try {
            try {
                Voucher v = (Voucher) currentData.getData();
                dbConn.addVoucher(v);
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {

        }
    }

    private void removeVoucher() {
        try {
            try {
                int id = (int) currentData.getData();
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

    private void getVoucher() {
        try {
            try {
                int id = (int) currentData.getData();
                Voucher v = dbConn.getVoucher(id);
                obOut.writeObject(v);
            } catch (SQLException | VoucherNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void updateVoucher() {
        try {
            try {
                Voucher v = (Voucher) currentData.getData();
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

    private void addScreen() {
        try {
            try {
                Screen s = (Screen) currentData.getData();
                dbConn.addScreen(s);
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {

        }
    }

    private void addButton() {
        try {
            try {
                Button b = (Button) currentData.getData();
                dbConn.addButton(b);
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {

        }
    }

    private void removeScreen() {
        try {
            try {
                Screen s = (Screen) currentData.getData();
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

    private void removeButton() {
        try {
            try {
                Button b = (Button) currentData.getData();
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

    private void updateScreen() {
        try {
            try {
                Screen s = (Screen) currentData.getData();
                dbConn.updateScreen(s);
                obOut.writeObject(s);
            } catch (SQLException | ScreenNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void updateButton() {
        try {
            try {
                Button b = (Button) currentData.getData();
                dbConn.updateButton(b);
                obOut.writeObject(b);
            } catch (SQLException | ButtonNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getScreen() {
        try {
            try {
                int id = (int) currentData.getData();
                Screen s = dbConn.getScreen(id);
                obOut.writeObject(s);
            } catch (SQLException | ScreenNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }

    private void getButton() {
        try {
            try {
                int id = (int) currentData.getData();
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

    private void getButtonsOnScreen() {
        try {
            try {
                Screen s = (Screen) currentData.getData();
                List<Button> buttons = dbConn.getButtonsOnScreen(s);
                obOut.writeObject(buttons);
            } catch (SQLException | ScreenNotFoundException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {

        }
    }
}
