/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.tillserver;

import io.github.davidg95.Till.till.Customer;
import io.github.davidg95.Till.till.CustomerNotFoundException;
import io.github.davidg95.Till.till.DBConnect;
import io.github.davidg95.Till.till.LoginException;
import io.github.davidg95.Till.till.OutOfStockException;
import io.github.davidg95.Till.till.Product;
import io.github.davidg95.Till.till.ProductNotFoundException;
import io.github.davidg95.Till.till.Sale;
import io.github.davidg95.Till.till.Staff;
import io.github.davidg95.Till.till.StaffNotFoundException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JOptionPane;

/**
 * Data class which stores all system data.
 *
 * @author David
 */
public class Data {

    private List<Product> products;
    private List<Customer> customers;
    private List<Staff> staff;
    private List<Sale> sales;

    private static int productCounter;
    private static int customerCounter;
    private static int staffCounter;

    private DBConnect dbConnection;

    /**
     * Blank constructor which initialises the product and customers data
     * structures. ArrayList data structures are used.
     */
    public Data(DBConnect db) {
        this.dbConnection = db;
        this.openFile();
        try {
            products = db.getAllProducts();
            customers = db.getAllCustomers();
            staff = db.getAllStaff();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
            products = new ArrayList<>();
            customers = new ArrayList<>();
            staff = new ArrayList<>();
        }
        sales = new ArrayList<>();

    }

    public void updateDatabase() throws SQLException {
        dbConnection.updateWholeProducts(products);
        dbConnection.updateWholeCustomers(customers);
        dbConnection.updateWholeStaff(staff);
        this.saveToFile();
    }

    public void close() throws SQLException {
        updateDatabase();
        dbConnection.close();
    }

    //Getters and setters
    public List<Product> getProductsList() {
        return this.products;
    }

    public List<Customer> getCustomersList() {
        return this.customers;
    }

    public List<Staff> getStaffList() {
        return this.staff;
    }

    public List<Sale> getSalesList() {
        return this.sales;
    }

    public void setProductsList(List<Product> products) {
        this.products = products;
    }

    public void setCustomersList(List<Customer> customers) {
        this.customers = customers;
    }

    public void setStaffList(List<Staff> staff) {
        this.staff = staff;
    }

    public void setSalesList(List<Sale> sales) {
        this.sales = sales;
    }

    //Product Methods
    /**
     * Method to add a new product to the system.
     *
     * @param p the new Product object to add.
     */
    public void addProduct(Product p) {
        p.setProductCode(generateProductCode());
        products.add(p);
    }

    /**
     * Method to remove a product from the system by passing in a Product
     * object.
     *
     * @param p the Product object to remove.
     */
    public void removeProduct(Product p) {
        products.remove(p);
    }

    /**
     * Method to remove a Product from the system by passing in its product
     * code.
     *
     * @param code the product code to remove.
     * @throws ProductNotFoundException if the product could not be found.
     */
    public void removeProduct(String code) throws ProductNotFoundException {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getProductCode().equalsIgnoreCase(code)) {
                products.remove(i);
                return;
            }
        }
        throw new ProductNotFoundException(code);
    }

    /**
     * Method to purchase a product.
     *
     * @param code the product to purchase.
     * @throws ProductNotFoundException if the product code could not be found.
     * @throws OutOfStockException if the product is out of stock.
     */
    public void purchaseProduct(String code) throws ProductNotFoundException, OutOfStockException {
        for (Product p : products) {
            if (p.getProductCode().equalsIgnoreCase(code)) {
                p.purchace();
                return;
            }
        }
        throw new ProductNotFoundException(code);
    }

    /**
     * Method to increase the stock level of a product.
     *
     * @param code the code of the product to increase the stock of.
     * @param stock the stock to add.
     * @throws ProductNotFoundException if the product code could not be found.
     */
    public void increaceStock(String code, int stock) throws ProductNotFoundException {
        for (Product p : products) {
            if (p.getProductCode().equalsIgnoreCase(code)) {
                p.addStock(stock);
                return;
            }
        }
        throw new ProductNotFoundException(code);
    }

    /**
     * Method to decrease the stock level of a product.
     *
     * @param code the code of the product to decrease the stock of.
     * @param stock the amount of stock to remove.
     * @throws ProductNotFoundException if the product code was not found.
     */
    public void decreaceStock(String code, int stock) throws ProductNotFoundException {
        for (Product p : products) {
            if (p.getProductCode().equalsIgnoreCase(code)) {
                p.removeStock(stock);
                return;
            }
        }
        throw new ProductNotFoundException(code);
    }

    /**
     * Method to search for a product by its product code.
     *
     * @param code the code to search for.
     * @return Product object that matches the code.
     * @throws ProductNotFoundException if the product code was not found on the
     * system.
     */
    public Product getProduct(String code) throws ProductNotFoundException {
        for (Product p : products) {
            if (p.getProductCode().equalsIgnoreCase(code)) {
                return p;
            }
        }
        throw new ProductNotFoundException(code);
    }

    /**
     * Method to get a product by the barcode.
     *
     * @param barcode the barcode to search.
     * @return Product object that matches the barcode.
     * @throws ProductNotFoundException if the barcode was not found.
     */
    public Product getProductByBarcode(String barcode) throws ProductNotFoundException {
        for (Product p : products) {
            if (p.getBarcode().equalsIgnoreCase(barcode)) {
                return p;
            }
        }
        throw new ProductNotFoundException(barcode);
    }

    /**
     * Method to get the total number of different products;
     *
     * @return the size of the product list as an int.
     */
    public int productCount() {
        return products.size();
    }

    public static String generateProductCode() {
        String no = "";
        String zeros = "";
        no = Integer.toString(productCounter);
        zeros = "";
        for (int i = no.length(); i < 5; i++) {
            zeros += "0";
        }
        productCounter++;

        return "P" + zeros + no;
    }

    //Customer Methods
    /**
     * Method to add a new customer to the system.
     *
     * @param c the Customer object to add.
     */
    public void addCustomer(Customer c) {
        customers.add(c);
        c.setId(generateCustomerCode());
    }

    /**
     * Method to remove a customer from the system by passing in a Customer
     * object.
     *
     * @param c the Customer object to remove.
     */
    public void removeCustomer(Customer c) {
        customers.remove(c);
    }

    /**
     * Method to remove a customer from the system by passing in their id.
     *
     * @param id the id of the customer to remove.
     * @throws CustomerNotFoundException if the id could not be found.
     */
    public void removeCustomer(String id) throws CustomerNotFoundException {
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getId().equalsIgnoreCase(id)) {
                customers.remove(i);
                return;
            }
        }
        throw new CustomerNotFoundException(id);
    }

    /**
     * Method to get a customer by passing in their id.
     *
     * @param id the id of the customer to search for.
     * @return the Customer object that matches the id.
     * @throws CustomerNotFoundException if the id could not be found.
     */
    public Customer getCustomer(String id) throws CustomerNotFoundException {
        for (Customer c : customers) {
            if (c.getId().equalsIgnoreCase(id)) {
                return c;
            }
        }
        throw new CustomerNotFoundException(id);
    }

    /**
     * Method to get the total number of customers.
     *
     * @return the size of the customer list.
     */
    public int customerCount() {
        return customers.size();
    }

    public static String generateCustomerCode() {
        String no = "";
        String zeros = "";
        no = Integer.toString(customerCounter);
        zeros = "";
        for (int i = no.length(); i < 5; i++) {
            zeros += "0";
        }
        customerCounter++;

        return "C" + zeros + no;
    }

    //Staff Methods
    /**
     * Method to add a new member of staff tot he system.
     *
     * @param s the new member of staff to add.
     */
    public void addStaff(Staff s) {
        s.setId(generateStaffID());
        staff.add(s);
    }

    /**
     * Method to remove a member of staff from the system.
     *
     * @param s the member of staff to remove.
     */
    public void removeStaff(Staff s) {
        staff.remove(s);
    }

    /**
     * Method to remove a member of staff from the system by passing in their
     * id.
     *
     * @param id the id of the staff to remove.
     * @throws StaffNotFoundException if the id could not be found.
     */
    public void removeStaff(String id) throws StaffNotFoundException {
        for (int i = 0; i < staff.size(); i++) {
            if (staff.get(i).getId().equalsIgnoreCase(id)) {
                staff.remove(i);
                return;
            }
        }
        throw new StaffNotFoundException(id);
    }

    /**
     * Method to get a member of staff by passing in their id.
     *
     * @param id the id of the staff to get.
     * @return Staff object that matches the id.
     * @throws StaffNotFoundException if the id could not be found.
     */
    public Staff getStaff(String id) throws StaffNotFoundException {
        for (Staff s : staff) {
            if (s.getId().equalsIgnoreCase(id)) {
                return s;
            }
        }
        throw new StaffNotFoundException(id);
    }

    /**
     * Method to log a member of staff in using a username and password. This
     * will be used for logging in to the server interface.
     *
     * @param username the username as a String.
     * @param password the password as a String.
     * @throws LoginException if there was an error logging in.
     */
    public void login(String username, String password) throws LoginException {
        for (Staff s : staff) {
            if (s.getUsername().equals(username)) {
                s.login(password);
                return;
            }
        }
        throw new LoginException("Bad Login");
    }

    /**
     * Method to log in using an id. This will be used for logging in to a till.
     *
     * @param id the id to log in.
     * @throws LoginException if they are already logged in on a till.
     * @throws StaffNotFoundException if the id was not found.
     */
    public void login(String id) throws LoginException, StaffNotFoundException {
        for (Staff s : staff) {
            if (s.getId().equals(id)) {
                s.login();
            }
        }
        throw new StaffNotFoundException(id);
    }

    /**
     * Method to get the total number of staff on the system.
     *
     * @return int value representing how many staff are on the system.
     */
    public int staffCount() {
        return staff.size();
    }

    /**
     * Method to generate a new 6-digit staff id.
     *
     * @return String value of new 6-digit staff id.
     */
    public static String generateStaffID() {
        String no = "";
        String zeros = "";
        no = Integer.toString(staffCounter);
        zeros = "";
        for (int i = no.length(); i < 6; i++) {
            zeros += "0";
        }
        staffCounter++;

        return zeros + no;
    }

    //Sale Methods
    /**
     * Method to add a sale.
     *
     * @param s the sale to add.
     */
    public void addSale(Sale s) {
        sales.add(s);
    }

    /**
     * Method to save the configs.
     */
    public void saveToFile() {
        try (PrintWriter writer = new PrintWriter("config.txt", "UTF-8")) {
            writer.println(productCounter);
            writer.println(customerCounter);
            writer.println(staffCounter);
        } catch (IOException ex) {

        }
    }

    /**
     * Method to load the configs.
     */
    public final void openFile() {
        try {
            Scanner fileReader = new Scanner(new File("config.txt"));

            if (fileReader.hasNext()) {
                productCounter = Integer.parseInt(fileReader.nextLine());
                customerCounter = Integer.parseInt(fileReader.nextLine());
                staffCounter = Integer.parseInt(fileReader.nextLine());
            }
        } catch (IOException e) {
            try {
                boolean createNewFile = new File("config.txt").createNewFile();
            } catch (IOException ex) {
            }
        }
    }
}
