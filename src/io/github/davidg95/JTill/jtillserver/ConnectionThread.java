/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
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

    private BufferedReader in;
    private PrintWriter out;
    private ObjectInputStream obIn;
    private ObjectOutputStream obOut;

    private final Socket socket;

    private boolean conn_term = false;

    private String site;
    private Staff staff;

    /**
     * Constructor for Connection thread.
     *
     * @param s the socket used for this connection.
     * @param data the data object.
     */
    public ConnectionThread(Socket s, Data data) {
        this.socket = s;
        this.data = data;
        this.dbConn = TillServer.getDataConnection();
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            obIn = new ObjectInputStream(socket.getInputStream());
            obOut = new ObjectOutputStream(socket.getOutputStream());
            obOut.flush();

            site = in.readLine();

            //data.addConnection(site);
            TillServer.g.increaceClientCount(site);
            TillServer.g.log(site + " has connected");

            while (!conn_term) {
                String input = in.readLine();

                TillServer.g.log("Contact from " + site);

                String inp[] = input.split(",");

                switch (inp[0]) {
                    case "NEWPRODUCT": //Add a new product
                        try {
                            Object o = obIn.readObject();
                            Product p = (Product) o;
                            dbConn.addProduct(p);
                        } catch (ClassNotFoundException | SQLException ex) {
                        }
                        break;
                    case "REMOVEPRODUCT": //Remove a product
                        try {
                            int code = Integer.parseInt(inp[1]);
                            dbConn.removeProduct(code);
                            out.println("SUCC");
                        } catch (SQLException | ProductNotFoundException ex) {
                            out.println("FAIL");
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "PURCHASE": //Purchase a product
                        try {
                            int code = Integer.parseInt(inp[1]);
                            int stock = dbConn.purchaseProduct(code);
                            out.println(stock);
                        } catch (ProductNotFoundException | SQLException | OutOfStockException ex) {
                            TillServer.g.log(ex);
                            out.println("FAIL");
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETPRODUCT": //Get a product
                        try {
                            int code = Integer.parseInt(inp[1]);
                            Product p = dbConn.getProduct(code);
                            obOut.writeObject(p);
                        } catch (ProductNotFoundException | SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "UPDATEPRODUCT": //Update a product
                        try {
                            Object o = obIn.readObject();
                            dbConn.updateProduct((Product) o);
                            obOut.writeObject((Product) o);
                        } catch (ClassNotFoundException ex) {

                        } catch (SQLException | ProductNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        break;
                    case "GETPRODUCTBARCODE": //Get a product by barcode
                        try {
                            String barcode = inp[1];
                            Product p = dbConn.getProductByBarcode(barcode);
                            obOut.writeObject(p);
                        } catch (ProductNotFoundException | SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "CHECKBARCODE": //Check if a barcode is in use
                        try {
                            String barcode = inp[1];
                            boolean inUse = dbConn.checkBarcode(barcode);
                            if (inUse) {
                                out.println("USED");
                            } else {
                                out.println("NOTUSED");
                            }
                        } catch (SQLException ex) {
                            out.println(ex.getMessage());
                        }
                        break;
                    case "SETSTOCK": //Set the stock level of a product
                        try {
                            int id = Integer.parseInt(inp[1]);
                            int stock = Integer.parseInt(inp[2]);
                            dbConn.setStock(id, stock);
                            out.println("SUCC");
                        } catch (SQLException ex) {
                            out.println(ex.getMessage());
                        } catch (ProductNotFoundException ex) {
                            out.println("FAIL");
                        }
                        break;
                    case "GETPRODUCTSDISCOUNT": //Gets all discounts for a product
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
                        break;
                    case "GETPRODUCTCOUNT": //Get product count
                        try {
                            out.write(dbConn.getProductCount());
                        } catch (SQLException ex) {
                            out.println(-1);
                        }
                        out.flush();
                        break;
                    case "GETALLPRODUCTS": //Get all products
                        try {
                            List<Product> products = dbConn.getAllProducts();
                            obOut.writeObject(products);
                        } catch (SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "NEWCUSTOMER": //Add a new customer
                        try {
                            Object o = obIn.readObject();
                            Customer c = (Customer) o;
                            dbConn.addCustomer(c);
                        } catch (ClassNotFoundException | SQLException e) {
                        }
                        break;
                    case "REMOVECUSTOMER": //Remove a customer
                        try {
                            int id = Integer.parseInt(inp[1]);
                            dbConn.removeCustomer(id);
                            out.println("SUCC");
                        } catch (SQLException | CustomerNotFoundException ex) {
                            out.println("FAIL");
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETCUSTOMER": //Get a customer
                        try {
                            int id = Integer.parseInt(inp[1]);
                            Customer c = dbConn.getCustomer(id);
                            obOut.writeObject(c);
                        } catch (CustomerNotFoundException | SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETCUSTOMERBYNAME": //Get a customer by name
                        try {
                            String name = inp[1];
                            List<Customer> customers = dbConn.getCustomerByName(name);
                            obOut.writeObject(customers);
                        } catch (SQLException | CustomerNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETCUSTOMERCOUNT": //Get customer count
                        try {
                            out.println(dbConn.getCustomerCount());
                        } catch (SQLException ex) {
                            out.println(-1);
                        }
                        out.flush();
                        break;
                    case "UPDATECUSTOMER": //Update a customer
                        try {
                            Object o = obIn.readObject();
                            Customer c = (Customer) o;
                            Customer customer = dbConn.updateCustomer(c);
                            obOut.writeObject(customer);
                        } catch (ClassNotFoundException ex) {
                        } catch (SQLException | CustomerNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETALLCUSTOMERS": //Get all customers
                        try {
                            List<Customer> customers = dbConn.getAllCustomers();
                            obOut.writeObject(customers);
                        } catch (SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "ADDSTAFF": //Add a member of staff
                        try {
                            Object o = obIn.readObject();
                            Staff s = (Staff) o;
                            dbConn.addStaff(s);
                        } catch (ClassNotFoundException | SQLException | StaffNotFoundException ex) {
                        }
                        break;
                    case "REMOVESTAFF": //Remove a member of staff
                        try {
                            int id = Integer.parseInt(inp[1]);
                            dbConn.removeStaff(id);
                            out.println("SUCC");
                        } catch (SQLException | StaffNotFoundException ex) {
                            out.println("FAIL");
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETSTAFF": //Get a member of staff
                        try {
                            int id = Integer.parseInt(inp[1]);
                            Staff s = dbConn.getStaff(id);
                            obOut.writeObject(s);
                        } catch (StaffNotFoundException | SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "UPDATESTAFF": //Update a member of staff
                        try {
                            Object o = obIn.readObject();
                            Staff s = (Staff) o;
                            Staff updatedStaff = dbConn.updateStaff(s);
                            obOut.writeObject(updatedStaff);
                        } catch (ClassNotFoundException ex) {
                        } catch (SQLException | StaffNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETALLSTAFF": //Get all the staff
                        try {
                            List<Staff> staffList = dbConn.getAllStaff();
                            obOut.writeObject(staffList);
                        } catch (SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "STAFFCOUNT": //Get the staff count
                        try {
                            out.println(dbConn.staffCount());
                        } catch (SQLException ex) {
                            out.println("FAIL");
                        } catch (StaffNotFoundException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "ADDSALE": //Add a sale
                        try {
                            Object o = obIn.readObject();
                            Sale s = (Sale) o;
                            data.addSale(s);
                        } catch (ClassNotFoundException ex) {

                        } catch (SQLException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "GETALLSALES": //Get all sales
                        try {
                            List<Sale> sales = dbConn.getAllSales();
                            obOut.writeObject(sales);
                        } catch (SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETSALE": //Get a sale
                        try {
                            int id = Integer.parseInt(inp[1]);
                            Sale s = dbConn.getSale(id);
                            obOut.writeObject(s);
                        } catch (SQLException | SaleNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETSALEDATERANGE": //Get all sales within a date range
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
                        obOut.flush();
                        break;
                    case "LOGIN": //Standard staff login
                        try {
                            String username = inp[1];
                            String password = inp[2];
                            Staff s = data.login(username, password);
                            this.staff = s;
                            TillServer.g.log(staff.getName() + " has logged in");
                            obOut.writeObject(s);
                        } catch (SQLException | LoginException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "TILLLOGIN": //Till login
                        try {
                            int id = Integer.parseInt(inp[1]);
                            Staff s = data.login(id);
                            this.staff = s;
                            TillServer.g.log(staff.getName() + " has logged in from " + site);
                            obOut.writeObject(s);
                        } catch (SQLException | LoginException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "LOGOUT": //Logout
                        try {
                            int id = Integer.parseInt(inp[1]);
                            data.logout(id);
                            TillServer.g.log(staff.getName() + " has logged out");
                            this.staff = null;
                            out.println("SUCC");
                        } catch (StaffNotFoundException ex) {
                            out.println("FAIL");
                        }
                        break;
                    case "TILLLOGOUT": //Till logout
                        try {
                            int id = Integer.parseInt(inp[1]);
                            data.tillLogout(id);
                            TillServer.g.log(staff.getName() + " has logged out");
                            this.staff = null;
                            out.println("SUCC");
                        } catch (StaffNotFoundException ex) {
                            out.println("FAIL");
                        }
                        break;
                    case "ADDCATEGORY": //Add a category
                        try {
                            Category c = (Category) obIn.readObject();
                            dbConn.addCategory(c);
                        } catch (ClassNotFoundException | SQLException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "UPDATECATEGORY": //Update a category
                        try {
                            Category c = (Category) obIn.readObject();
                            Category category = dbConn.updateCategory(c);
                            obOut.writeObject(category);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SQLException | CategoryNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "REMOVECATEGORY": //Remove a category
                        try {
                            int id = Integer.parseInt(inp[1]);
                            dbConn.removeCategory(id);
                            out.println("SUCC");
                        } catch (SQLException ex) {
                            out.println(ex.getErrorCode());
                        } catch (CategoryNotFoundException ex) {
                            out.println("FAIL");
                        }
                        break;
                    case "GETCATEGORY": //Get a category
                        try {
                            int id = Integer.parseInt(inp[1]);
                            Category c = dbConn.getCategory(id);
                            obOut.writeObject(c);
                        } catch (SQLException | CategoryNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETALLCATEGORYS": //Get all categorys
                        try {
                            List<Category> categorys = dbConn.getAllCategorys();
                            obOut.writeObject(categorys);
                        } catch (SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETPRODUCTSINCATEGORY": //Get all products in a category
                        try {
                            int id = Integer.parseInt(inp[1]);
                            List<Product> products = dbConn.getProductsInCategory(id);
                            obOut.writeObject(products);
                        } catch (SQLException | CategoryNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "ADDDISCOUNT": //Add a discount
                        try {
                            Discount d = (Discount) obIn.readObject();
                            dbConn.addDiscount(d);
                        } catch (ClassNotFoundException | SQLException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "UPDATEDISCOUNT": //Update a discount
                        try {
                            Discount d = (Discount) obIn.readObject();
                            Discount discount = dbConn.updateDiscount(d);
                            obOut.writeObject(discount);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SQLException | DiscountNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "REMOVEDISCOUNT": //Remove a discount
                        try {
                            int id = Integer.parseInt(inp[1]);
                            dbConn.removeDiscount(id);
                            out.println("SUCC");
                        } catch (SQLException ex) {
                            out.println(ex.getErrorCode());
                        } catch (DiscountNotFoundException ex) {
                            out.println("FAIL");
                        }
                        break;
                    case "GETDISCOUNT": //Get a discount
                        try {
                            int id = Integer.parseInt(inp[1]);
                            Discount d = dbConn.getDiscount(id);
                            obOut.writeObject(d);
                        } catch (SQLException | DiscountNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETALLDISCOUNTS": //Get all discounts
                        try {
                            List<Discount> discounts = dbConn.getAllDiscounts();
                            obOut.writeObject(discounts);
                        } catch (SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "ADDTAX": //Add a new tax
                        try {
                            Tax t = (Tax) obIn.readObject();
                            dbConn.addTax(t);
                        } catch (ClassNotFoundException | SQLException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "REMOVETAX": //Remove a tax
                        try {
                            int id = Integer.parseInt(inp[1]);
                            dbConn.removeTax(id);
                            out.print("SUCC");
                        } catch (SQLException ex) {
                            out.println(ex.getErrorCode());
                        } catch (TaxNotFoundException ex) {
                            out.println("FAIL");
                        }
                        break;
                    case "GETTAX": //Get a tax
                        try {
                            int id = Integer.parseInt(inp[1]);
                            Tax t = dbConn.getTax(id);
                            obOut.writeObject(t);
                        } catch (SQLException | TaxNotFoundException ex) {
                            obOut.writeObject(out);
                        }
                        obOut.flush();
                        break;
                    case "UPDATETAX": //Update a tax
                        try {
                            Tax t = (Tax) obIn.readObject();
                            Tax tax = dbConn.updateTax(t);
                            obOut.writeObject(tax);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SQLException | TaxNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETALLTAX": //Get all tax
                        try {
                            List<Tax> tax = dbConn.getAllTax();
                            obOut.writeObject(tax);
                        } catch (SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "ADDVOUCHER": //Add a new voucher
                        try {
                            Voucher v = (Voucher) obIn.readObject();
                            dbConn.addVoucher(v);
                        } catch (ClassNotFoundException | SQLException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "REMOVEVOUCHER": //Remove a voucher
                        try {
                            int id = Integer.parseInt(inp[1]);
                            dbConn.removeVoucher(id);
                            out.print("SUCC");
                        } catch (SQLException ex) {
                            out.println(ex.getErrorCode());
                        } catch (VoucherNotFoundException ex) {
                            out.println("FAIL");
                        }
                        break;
                    case "GETVOUCHER": //Get a voucher
                        try {
                            int id = Integer.parseInt(inp[1]);
                            Voucher v = dbConn.getVoucher(id);
                            obOut.writeObject(v);
                        } catch (SQLException | VoucherNotFoundException ex) {
                            obOut.writeObject(out);
                        }
                        obOut.flush();
                        break;
                    case "UPDATEVOUCHER": //Update a voucher
                        try {
                            Voucher v = (Voucher) obIn.readObject();
                            Voucher voucher = dbConn.updateVoucher(v);
                            obOut.writeObject(voucher);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SQLException | VoucherNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETALLVOUCHERS": //Get all vouchers
                        try {
                            List<Voucher> voucher = dbConn.getAllVouchers();
                            obOut.writeObject(voucher);
                        } catch (SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "INIT": //Get Init Data
                        obOut.writeObject(TillInitData.initData);
                        break;
                    case "SENDINIT": //Send init data to the server
                        try {
                            TillInitData initData = (TillInitData) obIn.readObject();
                            dbConn.setInitData(initData);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "GETINIT": //Load till configuration
                        try {
                            List<Category> categorys = dbConn.getAllCategorys();
                            out.print(categorys.size());
                            for (Category c : categorys) {
                                List<Product> products = dbConn.getProductsInCategory(c.getID());
                                obOut.writeObject(products);
                            }
                        } catch (SQLException ex) {
                            obOut.writeObject(ex);
                        } catch (CategoryNotFoundException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "ADDSCREEN": //Add a new screen
                        try {
                            Screen s = (Screen) obIn.readObject();
                            dbConn.addScreen(s);
                        } catch (ClassNotFoundException | SQLException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "ADDBUTTON": //Add a new button
                        try {
                            Button b = (Button) obIn.readObject();
                            dbConn.addButton(b);
                        } catch (ClassNotFoundException | SQLException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "REMOVESCREEN": //Remove a screen
                        try {
                            Screen s = (Screen) obIn.readObject();
                            dbConn.removeScreen(s);
                            out.println("SUCC");
                        } catch (ClassNotFoundException | SQLException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ScreenNotFoundException ex) {
                            out.println("FAIL");
                        }
                        break;
                    case "REMOVEBUTTON": //Remove a button
                        try {
                            Button b = (Button) obIn.readObject();
                            dbConn.removeButton(b);
                            out.println("SUCC");
                        } catch (ClassNotFoundException | SQLException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ButtonNotFoundException ex) {
                            out.println("FAIL");
                        }
                        break;
                    case "UPDATESCREEN": //Update a screen
                        try {
                            Screen s = (Screen) obIn.readObject();
                            dbConn.updateScreen(s);
                            obOut.writeObject(s);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SQLException | ScreenNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "UPDATEBUTTON": //Update a button
                        try {
                            Button b = (Button) obIn.readObject();
                            dbConn.updateButton(b);
                            obOut.writeObject(b);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SQLException | ButtonNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETSCREEN": //Update a screen
                        try {
                            int id = Integer.parseInt(inp[1]);
                            Screen s = dbConn.getScreen(id);
                            obOut.writeObject(s);
                        } catch (SQLException | ScreenNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETBUTTON": //Updatea a button
                        try {
                            int id = Integer.parseInt(inp[1]);
                            Button b = dbConn.getButton(id);
                            obOut.writeObject(b);
                        } catch (SQLException | ButtonNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETALLSCREENS": //Get all screens
                        try {
                            List<Screen> screens = dbConn.getAllScreens();
                            obOut.writeObject(screens);
                        } catch (SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETALLBUTTONS": //Get all buttons
                        try {
                            List<Button> buttons = dbConn.getAllButtons();
                            obOut.writeObject(buttons);
                        } catch (SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETBUTTONSONSCREEN": //Get buttons on a screen
                        try {
                            Screen s = (Screen) obIn.readObject();
                            List<Button> buttons = dbConn.getButtonsOnScreen(s);
                            obOut.writeObject(buttons);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SQLException | ScreenNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
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

        }
    }
}
