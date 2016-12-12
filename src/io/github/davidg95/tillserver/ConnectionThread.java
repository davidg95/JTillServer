/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.tillserver;

import io.github.davidg95.Till.till.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
/**
 * Thread for handling incoming connections.
 *
 * @author David
 */
public class ConnectionThread extends Thread {

    private final Data data;
    private final DBConnect dbConn;

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
        this.dbConn = TillServer.getDBConnection();
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
                            dbConn.purchaseProduct(code);
                            out.println("SUCC");
                        } catch (ProductNotFoundException | SQLException | OutOfStockException ex) {
                            TillServer.g.log(ex);
                            out.println("FAIL");
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "GETPRODUCT": //Get a product
                        try {
                            String code = inp[1];
                            Product p = dbConn.getProduct(code);
                            obOut.writeObject(p);
                        } catch (ProductNotFoundException | SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
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
                    case "GETCUSTOMERCOUNT": //Get customer count
                        try {
                            out.println(dbConn.getCustomerCount());
                        } catch (SQLException ex) {
                            out.println(-1);
                        }
                        out.flush();
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
                        } catch (ClassNotFoundException | SQLException ex) {
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
                    case "GETALLSTAFF": //Get all the staff
                        try {
                            List<Staff> staffList = dbConn.getAllStaff();
                            obOut.writeObject(staffList);
                        } catch (SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        break;
                    case "ADDSALE":
                        try {
                            Object o = obIn.readObject();
                            Sale s = (Sale) o;
                            data.addSale();
                            data.addTakings(s.getTotal());
                        } catch (ClassNotFoundException ex) {

                        }
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
                    case "GETINIT": //Load till configuration
                        try {
                            List<Category> categorys = dbConn.getAllCategorys();
                            for (int i = 0; i < categorys.size(); i++) {
                                if (!categorys.get(i).isButton()) {
                                    categorys.remove(i);
                                }
                            }
                            out.print(categorys.size());
                            for (Category c : categorys) {
                                List<Product> products = dbConn.getProductsInCategory(c.getID());
                                obOut.writeObject(products);
                            }
                        } catch (SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        break;
                    case "GETCATBUTTONS": //Load category buttons
                        try {
                            List<Category> categorys = dbConn.getAllCategorys();
                            for (int i = 0; i < categorys.size(); i++) {
                                if (!categorys.get(i).isButton()) {
                                    categorys.remove(i);
                                }
                            }
                            obOut.writeObject(categorys);
                        } catch (SQLException ex) {
                            obOut.writeObject(ex);
                        }
                        break;
                    case "GETPRODUCTBUTTONS": //Load product buttons
                        try {
                            int id = Integer.parseInt(inp[1]);
                            List<Product> products = dbConn.getProductsInCategory(id);
                            for (int i = 0; i < products.size(); i++) {
                                if (!products.get(i).isButton()) {
                                    products.remove(i);
                                }
                            }
                            obOut.writeObject(products);
                        } catch (SQLException ex) {
                            obOut.writeObject(ex);
                        }
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
