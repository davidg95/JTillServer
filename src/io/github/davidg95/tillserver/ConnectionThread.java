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
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Thread for handling incoming connections.
 *
 * @author David
 */
public class ConnectionThread extends Thread {

    private final Data data;

    private final Semaphore productsSem;
    private final Semaphore customersSem;
    private final Semaphore salesSem;
    private final Semaphore staffSem;

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
     * @param productsSem the semaphore for the products list.
     * @param customersSem the semaphore for the customers list.
     * @param salesSem the semaphore for the sales list.
     * @param staffSem the semaphore for the staff list.
     * @param data the data object.
     */
    public ConnectionThread(Socket s, Semaphore productsSem, Semaphore customersSem, Semaphore salesSem, Semaphore staffSem, Data data) {
        this.socket = s;
        this.productsSem = productsSem;
        this.customersSem = customersSem;
        this.salesSem = salesSem;
        this.staffSem = staffSem;
        this.data = data;
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
                            productsSem.acquire();
                            data.addProduct(p);
                        } catch (ClassNotFoundException | InterruptedException ex) {
                        }
                        productsSem.release();
                        break;
                    case "REMOVEPRODUCT": //Remove a product
                        try {
                            String code = inp[1];
                            productsSem.acquire();
                            data.removeProduct(code);
                            out.println("SUCC");
                        } catch (InterruptedException e) {

                        } catch (ProductNotFoundException ex) {
                            out.println("FAIL");
                        }
                        productsSem.release();
                        break;
                    case "PURCHASE": //Purchase a product
                        try {
                            String code = inp[1];
                            productsSem.acquire();
                            data.purchaseProduct(code);
                            out.println("SUCC");
                        } catch (InterruptedException e) {
                        } catch (ProductNotFoundException ex) {
                            out.println("NOTFOUND");
                        } catch (OutOfStockException ex) {
                            out.println("STOCK");
                        }
                        productsSem.release();
                        break;
                    case "GETPRODUCT": //Get a product
                        try {
                            String code = inp[1];
                            productsSem.acquire();
                            Product p = data.getProduct(code);
                            obOut.writeObject(p);
                        } catch (InterruptedException ex) {
                        } catch (ProductNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        productsSem.release();
                        break;
                    case "GETPRODUCTBARCODE": //Get a product by barcode
                        try {
                            String barcode = inp[1];
                            productsSem.acquire();
                            Product p = data.getProductByBarcode(barcode);
                            obOut.writeObject(p);
                        } catch (InterruptedException ex) {

                        } catch (ProductNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        productsSem.release();
                        break;
                    case "GETPRODUCTCOUNT": //Get product count
                        try {
                            productsSem.acquire();
                            out.write(data.productCount());
                        } catch (InterruptedException ex) {
                        }
                        out.flush();
                        productsSem.release();
                        break;
                    case "GETALLPRODUCTS": //Get all products
                        try {
                            productsSem.acquire();
                            List<Product> products = data.getProductsList();
                            obOut.writeObject(products);
                        } catch (InterruptedException ex) {
                        }
                        obOut.flush();
                        productsSem.release();
                        break;
                    case "NEWCUSTOMER": //Add a new customer
                        try {
                            Object o = obIn.readObject();
                            Customer c = (Customer) o;
                            customersSem.acquire();
                            data.addCustomer(c);
                        } catch (InterruptedException | ClassNotFoundException e) {
                        }
                        customersSem.release();
                        break;
                    case "REMOVECUSTOMER": //Remove a customer
                        try {
                            String id = inp[1];
                            customersSem.acquire();
                            data.removeCustomer(id);
                            out.println("SUCC");
                        } catch (InterruptedException ex) {
                        } catch (CustomerNotFoundException ex) {
                            out.println("FAIL");
                        }
                        customersSem.release();
                        break;
                    case "GETCUSTOMER": //Get a customer
                        try {
                            String id = inp[1];
                            customersSem.acquire();
                            Customer c = data.getCustomer(id);
                            obOut.writeObject(c);
                        } catch (InterruptedException ex) {
                        } catch (CustomerNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        customersSem.release();
                        break;
                    case "GETCUSTOMERCOUNT": //Get customer count
                        try {
                            customersSem.acquire();
                            out.println(data.customerCount());
                        } catch (InterruptedException ex) {
                        }
                        out.flush();
                        customersSem.release();
                        break;
                    case "GETALLCUSTOMERS": //Get all customers
                        try {
                            customersSem.acquire();
                            List<Customer> customers = data.getCustomersList();
                            obOut.writeObject(customers);
                        } catch (InterruptedException ex) {
                        }
                        obOut.flush();
                        customersSem.release();
                        break;
                    case "ADDSALE": //Add a sale
                        try {
                            Object o = obIn.readObject();
                            Sale s = (Sale) o;
                            salesSem.acquire();
                            data.addSale(s);
                        } catch (ClassNotFoundException | InterruptedException ex) {
                        }
                        salesSem.release();
                        break;
                    case "ADDSTAFF": //Add a member of staff
                        try {
                            Object o = obIn.readObject();
                            Staff s = (Staff) o;
                            staffSem.acquire();
                            data.addStaff(s);
                        } catch (ClassNotFoundException | InterruptedException ex) {
                        }
                        staffSem.release();
                        break;
                    case "REMOVESTAFF": //Remove a member of staff
                        try {
                            String id = inp[1];
                            staffSem.acquire();
                            data.removeStaff(id);
                            out.println("SUCC");
                        } catch (InterruptedException ex) {
                        } catch (StaffNotFoundException ex) {
                            out.println("FAIL");
                        }
                        staffSem.release();
                        break;
                    case "GETSTAFF": //Get a member of staff
                        try {
                            String id = inp[1];
                            staffSem.acquire();
                            Staff s = data.getStaff(id);
                            obOut.writeObject(s);
                        } catch (InterruptedException ex) {
                        } catch (StaffNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        staffSem.release();
                        break;
                    case "GETSTAFFCOUNT": //Get the total number of staff
                        try {
                            staffSem.acquire();
                            out.println(data.staffCount());
                        } catch (InterruptedException ex) {
                        }
                        staffSem.release();
                        break;
                    case "GETALLSTAFF": //Get all the staff
                        try {
                            staffSem.acquire();
                            List<Staff> staffList = data.getStaffList();
                            obOut.writeObject(staffList);
                        } catch (InterruptedException ex) {
                        }
                        obOut.flush();
                        staffSem.release();
                        break;
                    case "LOGIN": //Standard staff login
                        try {
                            String username = inp[1];
                            String password = inp[2];
                            staffSem.acquire();
                            Staff s = data.login(username, password);
                            this.staff = s;
                            TillServer.g.log(staff.getName() + " has logged in");
                            obOut.writeObject(s);
                        } catch (InterruptedException ex) {
                        } catch (LoginException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        staffSem.release();
                        break;
                    case "TILLLOGIN": //Till login
                        try {
                            String id = inp[1];
                            staffSem.acquire();
                            Staff s = data.login(id);
                            this.staff = s;
                            TillServer.g.log(staff.getName() + " has logged in");
                            obOut.writeObject(s);
                        } catch (InterruptedException ex) {
                        } catch (LoginException | StaffNotFoundException ex) {
                            obOut.writeObject(ex);
                        }
                        obOut.flush();
                        staffSem.release();
                        break;
                    case "LOGOUT": //Logout
                        try {
                            String id = inp[1];
                            staffSem.acquire();
                            data.logout(id);
                            TillServer.g.log(staff.getName() + " has logged out");
                            this.staff = null;
                            out.println("SUCC");
                        } catch (InterruptedException ex) {
                        } catch (StaffNotFoundException ex) {
                            out.println("FAIL");
                        }
                        staffSem.release();
                        break;
                    case "TILLLOGOUT": //Till logout
                        try {
                            String id = inp[1];
                            staffSem.acquire();
                            data.tillLogout(id);
                            TillServer.g.log(staff.getName() + " has logged out");
                            this.staff = null;
                            out.println("SUCC");
                        } catch (InterruptedException ex) {
                        } catch (StaffNotFoundException ex) {
                            out.println("FAIL");
                        }
                        staffSem.release();
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
            TillServer.g.log(site + " hsa disconnected");
            socket.close();
        } catch (IOException e) {

        }
    }
}
