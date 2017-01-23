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
                String input = (String) obIn.readObject();

                TillServer.g.log("Contact from " + site);

                String inp[] = input.split(",");

                switch (inp[0]) {
                    case "NEWPRODUCT": //Add a new product
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Object o = obIn.readObject();
                                    Product p = (Product) o;
                                    dbConn.addProduct(p);
                                } catch (ClassNotFoundException | SQLException ex) {
                                } catch (IOException ex) {
                                    Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }.start();
                        break;
                    case "REMOVEPRODUCT": //Remove a product
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int code = Integer.parseInt(inp[1]);
                                        dbConn.removeProduct(code);
                                        obOut.writeObject("SUCC");
                                    } catch (SQLException | ProductNotFoundException ex) {
                                        obOut.writeObject("FAIL");
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "PURCHASE": //Purchase a product
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int code = Integer.parseInt(inp[1]);
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
                        }.start();

                        break;
                    case "GETPRODUCT": //Get a product
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int code = Integer.parseInt(inp[1]);
                                        Product p = dbConn.getProduct(code);
                                        obOut.writeObject(p);
                                    } catch (ProductNotFoundException | SQLException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "UPDATEPRODUCT": //Update a product
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Object o = obIn.readObject();
                                        dbConn.updateProduct((Product) o);
                                        obOut.writeObject((Product) o);
                                    } catch (ClassNotFoundException ex) {

                                    } catch (SQLException | ProductNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETPRODUCTBARCODE": //Get a product by barcode
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        String barcode = inp[1];
                                        Product p = dbConn.getProductByBarcode(barcode);
                                        obOut.writeObject(p);
                                    } catch (ProductNotFoundException | SQLException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "CHECKBARCODE": //Check if a barcode is in use
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        String barcode = inp[1];
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
                        }.start();
                        break;
                    case "SETSTOCK": //Set the stock level of a product
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
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
                        }.start();
                        break;
                    case "GETPRODUCTSDISCOUNT": //Gets all discounts for a product
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Object o = obIn.readObject();
                                        Product p = (Product) o;

                                        List<Discount> discounts = dbConn.getProductsDiscount(p);

                                        obOut.writeObject(discounts);
                                    } catch (ClassNotFoundException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (SQLException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETPRODUCTCOUNT": //Get product count
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        obOut.writeObject(dbConn.getProductCount());
                                    } catch (SQLException ex) {
                                        obOut.writeObject(-1);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETALLPRODUCTS": //Get all products
                        new Thread() {
                            @Override
                            public void run() {
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
                        }.start();
                        break;
                    case "NEWCUSTOMER": //Add a new customer
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Object o = obIn.readObject();
                                        Customer c = (Customer) o;
                                        dbConn.addCustomer(c);
                                    } catch (ClassNotFoundException | SQLException e) {
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "REMOVECUSTOMER": //Remove a customer
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
                                        dbConn.removeCustomer(id);
                                        obOut.writeObject("SUCC");
                                    } catch (SQLException | CustomerNotFoundException ex) {
                                        obOut.writeObject("FAIL");
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETCUSTOMER": //Get a customer
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
                                        Customer c = dbConn.getCustomer(id);
                                        obOut.writeObject(c);
                                    } catch (CustomerNotFoundException | SQLException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETCUSTOMERBYNAME": //Get a customer by name
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        String name = inp[1];
                                        List<Customer> customers = dbConn.getCustomerByName(name);
                                        obOut.writeObject(customers);
                                    } catch (SQLException | CustomerNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETCUSTOMERCOUNT": //Get customer count
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        obOut.writeObject(dbConn.getCustomerCount());
                                    } catch (SQLException ex) {
                                        obOut.writeObject(-1);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "UPDATECUSTOMER": //Update a customer
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Object o = obIn.readObject();
                                        Customer c = (Customer) o;
                                        Customer customer = dbConn.updateCustomer(c);
                                        obOut.writeObject(customer);
                                    } catch (ClassNotFoundException ex) {
                                    } catch (SQLException | CustomerNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETALLCUSTOMERS": //Get all customers
                        new Thread() {
                            @Override
                            public void run() {
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
                        }.start();
                        break;
                    case "ADDSTAFF": //Add a member of staff
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Object o = obIn.readObject();
                                        Staff s = (Staff) o;
                                        dbConn.addStaff(s);
                                    } catch (ClassNotFoundException | SQLException | StaffNotFoundException ex) {
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "REMOVESTAFF": //Remove a member of staff
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
                                        dbConn.removeStaff(id);
                                        obOut.writeObject("SUCC");
                                    } catch (SQLException | StaffNotFoundException ex) {
                                        obOut.writeObject("FAIL");
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETSTAFF": //Get a member of staff
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
                                        Staff s = dbConn.getStaff(id);
                                        obOut.writeObject(s);
                                    } catch (StaffNotFoundException | SQLException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "UPDATESTAFF": //Update a member of staff
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Object o = obIn.readObject();
                                        Staff s = (Staff) o;
                                        Staff updatedStaff = dbConn.updateStaff(s);
                                        obOut.writeObject(updatedStaff);
                                    } catch (ClassNotFoundException ex) {
                                    } catch (SQLException | StaffNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETALLSTAFF": //Get all the staff
                        new Thread() {
                            @Override
                            public void run() {
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
                        }.start();
                        break;
                    case "STAFFCOUNT": //Get the staff count
                        new Thread() {
                            @Override
                            public void run() {
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
                        }.start();
                        break;
                    case "ADDSALE": //Add a sale
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Object o = obIn.readObject();
                                        Sale s = (Sale) o;
                                        data.addSale(s);
                                    } catch (ClassNotFoundException ex) {

                                    } catch (SQLException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETALLSALES": //Get all sales
                        new Thread() {
                            @Override
                            public void run() {
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
                        }.start();
                        break;
                    case "GETSALE": //Get a sale
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
                                        Sale s = dbConn.getSale(id);
                                        obOut.writeObject(s);
                                    } catch (SQLException | SaleNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETSALEDATERANGE": //Get all sales within a date range
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Date start = (Date) obIn.readObject();
                                        Date end = (Date) obIn.readObject();
                                        List<Sale> sales = dbConn.getSalesInRange(start, end);
                                        obOut.writeObject(sales);
                                    } catch (ClassNotFoundException | IllegalArgumentException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (SQLException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "LOGIN": //Standard staff login
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        String username = inp[1];
                                        String password = inp[2];
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
                        }.start();
                        break;
                    case "TILLLOGIN": //Till login
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
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
                        }.start();
                        break;
                    case "LOGOUT": //Logout
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
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
                        }.start();
                        break;
                    case "TILLLOGOUT": //Till logout
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
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
                        }.start();
                        break;
                    case "ADDCATEGORY": //Add a category
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Category c = (Category) obIn.readObject();
                                        dbConn.addCategory(c);
                                    } catch (ClassNotFoundException | SQLException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "UPDATECATEGORY": //Update a category
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Category c = (Category) obIn.readObject();
                                        Category category = dbConn.updateCategory(c);
                                        obOut.writeObject(category);
                                    } catch (ClassNotFoundException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (SQLException | CategoryNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "REMOVECATEGORY": //Remove a category
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
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
                        }.start();
                        break;
                    case "GETCATEGORY": //Get a category
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
                                        Category c = dbConn.getCategory(id);
                                        obOut.writeObject(c);
                                    } catch (SQLException | CategoryNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETALLCATEGORYS": //Get all categorys
                        new Thread() {
                            @Override
                            public void run() {
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
                        }.start();
                        break;
                    case "GETPRODUCTSINCATEGORY": //Get all products in a category
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
                                        List<Product> products = dbConn.getProductsInCategory(id);
                                        obOut.writeObject(products);
                                    } catch (SQLException | CategoryNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "ADDDISCOUNT": //Add a discount
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Discount d = (Discount) obIn.readObject();
                                        dbConn.addDiscount(d);
                                    } catch (ClassNotFoundException | SQLException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "UPDATEDISCOUNT": //Update a discount
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Discount d = (Discount) obIn.readObject();
                                        Discount discount = dbConn.updateDiscount(d);
                                        obOut.writeObject(discount);
                                    } catch (ClassNotFoundException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (SQLException | DiscountNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "REMOVEDISCOUNT": //Remove a discount
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
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
                        }.start();
                        break;
                    case "GETDISCOUNT": //Get a discount
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
                                        Discount d = dbConn.getDiscount(id);
                                        obOut.writeObject(d);
                                    } catch (SQLException | DiscountNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETALLDISCOUNTS": //Get all discounts
                        new Thread() {
                            @Override
                            public void run() {
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
                        }.start();
                        break;
                    case "ADDTAX": //Add a new tax
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Tax t = (Tax) obIn.readObject();
                                        dbConn.addTax(t);
                                    } catch (ClassNotFoundException | SQLException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "REMOVETAX": //Remove a tax
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
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
                        }.start();
                        break;
                    case "GETTAX": //Get a tax
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
                                        Tax t = dbConn.getTax(id);
                                        obOut.writeObject(t);
                                    } catch (SQLException | TaxNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "UPDATETAX": //Update a tax
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Tax t = (Tax) obIn.readObject();
                                        Tax tax = dbConn.updateTax(t);
                                        obOut.writeObject(tax);
                                    } catch (ClassNotFoundException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (SQLException | TaxNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETALLTAX": //Get all tax
                        new Thread() {
                            @Override
                            public void run() {
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
                        }.start();
                        break;
                    case "ADDVOUCHER": //Add a new voucher
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Voucher v = (Voucher) obIn.readObject();
                                        dbConn.addVoucher(v);
                                    } catch (ClassNotFoundException | SQLException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "REMOVEVOUCHER": //Remove a voucher
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
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
                        }.start();
                        break;
                    case "GETVOUCHER": //Get a voucher
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
                                        Voucher v = dbConn.getVoucher(id);
                                        obOut.writeObject(v);
                                    } catch (SQLException | VoucherNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "UPDATEVOUCHER": //Update a voucher
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Voucher v = (Voucher) obIn.readObject();
                                        Voucher voucher = dbConn.updateVoucher(v);
                                        obOut.writeObject(voucher);
                                    } catch (ClassNotFoundException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (SQLException | VoucherNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETALLVOUCHERS": //Get all vouchers
                        new Thread() {
                            @Override
                            public void run() {
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
                        }.start();
                        break;
                    case "ADDSCREEN": //Add a new screen
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Screen s = (Screen) obIn.readObject();
                                        dbConn.addScreen(s);
                                    } catch (ClassNotFoundException | SQLException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "ADDBUTTON": //Add a new button
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Button b = (Button) obIn.readObject();
                                        dbConn.addButton(b);
                                    } catch (ClassNotFoundException | SQLException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "REMOVESCREEN": //Remove a screen
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Screen s = (Screen) obIn.readObject();
                                        dbConn.removeScreen(s);
                                        obOut.writeObject("SUCC");
                                    } catch (ClassNotFoundException | SQLException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (ScreenNotFoundException ex) {
                                        obOut.writeObject("FAIL");
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "REMOVEBUTTON": //Remove a button
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Button b = (Button) obIn.readObject();
                                        dbConn.removeButton(b);
                                        obOut.writeObject("SUCC");
                                    } catch (ClassNotFoundException | SQLException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (ButtonNotFoundException ex) {
                                        obOut.writeObject("FAIL");
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "UPDATESCREEN": //Update a screen
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Screen s = (Screen) obIn.readObject();
                                        dbConn.updateScreen(s);
                                        obOut.writeObject(s);
                                    } catch (ClassNotFoundException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (SQLException | ScreenNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "UPDATEBUTTON": //Update a button
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Button b = (Button) obIn.readObject();
                                        dbConn.updateButton(b);
                                        obOut.writeObject(b);
                                    } catch (ClassNotFoundException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (SQLException | ButtonNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETSCREEN": //Update a screen
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
                                        Screen s = dbConn.getScreen(id);
                                        obOut.writeObject(s);
                                    } catch (SQLException | ScreenNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETBUTTON": //Updatea a button
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        int id = Integer.parseInt(inp[1]);
                                        Button b = dbConn.getButton(id);
                                        obOut.writeObject(b);
                                    } catch (SQLException | ButtonNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        }.start();
                        break;
                    case "GETALLSCREENS": //Get all screens
                        new Thread() {
                            @Override
                            public void run() {
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
                        }.start();
                        break;
                    case "GETALLBUTTONS": //Get all buttons
                        new Thread() {
                            @Override
                            public void run() {
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
                        }.start();
                        break;
                    case "GETBUTTONSONSCREEN": //Get buttons on a screen
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        Screen s = (Screen) obIn.readObject();
                                        List<Button> buttons = dbConn.getButtonsOnScreen(s);
                                        obOut.writeObject(buttons);
                                    } catch (ClassNotFoundException ex) {
                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (SQLException | ScreenNotFoundException ex) {
                                        obOut.writeObject(ex);
                                    }
                                } catch (IOException e) {

                                }
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
}
