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

    private BufferedReader in;
    private PrintWriter out;
    private ObjectInputStream obIn;
    private ObjectOutputStream obOut;

    private final Socket socket;

    private boolean conn_term = false;
    
    private String site;

    /**
     * Constructor for Connection thread.
     *
     * @param s the socket used for this connection.
     * @param productsSem the semaphore for the products list.
     * @param customersSem the semaphore for the customers list.
     * @param salesSem the semaphore for the sales list.
     * @param data the data object.
     */
    public ConnectionThread(Socket s, Semaphore productsSem, Semaphore customersSem, Semaphore salesSem, Data data) {
        this.socket = s;
        this.productsSem = productsSem;
        this.customersSem = customersSem;
        this.salesSem = salesSem;
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
            
            
            

            while (!conn_term) {
                String input = in.readLine();

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
                        } catch (InterruptedException | ProductNotFoundException e) {
                        }
                        productsSem.release();
                        break;
                    case "PURCHASE": //Purchase a product
                        try {
                            String code = inp[1];
                            productsSem.acquire();
                            data.purchaseProduct(code);
                        } catch (InterruptedException | ProductNotFoundException | OutOfStockException e) {
                        }
                        productsSem.release();
                        break;
                    case "GETPRODUCT": //Get a product
                        try {
                            String code = inp[1];
                            productsSem.acquire();
                            Product p = data.getProduct(code);
                            obOut.writeObject(p);
                        } catch (InterruptedException | ProductNotFoundException e) {
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
                        } catch (InterruptedException | CustomerNotFoundException ex) {
                        }
                        customersSem.release();
                        break;
                    case "GETCUSTOMER": //Get a customer
                        try {
                            String id = inp[1];
                            customersSem.acquire();
                            Customer c = data.getCustomer(id);
                            obOut.writeObject(c);
                        } catch (InterruptedException | CustomerNotFoundException ex) {
                        }
                        obOut.flush();
                        customersSem.release();
                        break;
                    case "GETCUSTOMERCOUNT": //Get customer count
                        try {
                            customersSem.acquire();
                            out.write(data.customerCount());
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
                    case "CONNTERM": //Terminate the connection
                        conn_term = true;
                        break;
                }
            }
            socket.close();
        } catch (IOException e) {

        }
    }
}
