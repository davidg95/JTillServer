/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.tillserver;

import io.github.davidg95.Till.till.Customer;
import io.github.davidg95.Till.till.CustomerNotFoundException;
import io.github.davidg95.Till.till.OutOfStockException;
import io.github.davidg95.Till.till.Product;
import io.github.davidg95.Till.till.ProductNotFoundException;
import io.github.davidg95.Till.till.Sale;
import java.util.ArrayList;
import java.util.List;

/**
 * Data class which stores all system data.
 *
 * @author David
 */
public class Data {

    private List<Product> products;
    private List<Customer> customers;
    private List<Sale> sales;
    
    private int productCounter;
    private int CustomerCounter;

    /**
     * Blank constructor which initialises the product and customers data
     * structures. ArrayList data structures are used.
     */
    public Data() {
        products = new ArrayList<>();
        customers = new ArrayList<>();
        sales = new ArrayList<>();
    }

    //Getters and setters
    public List<Product> getProductsList() {
        return this.products;
    }

    public List<Customer> getCustomersList() {
        return this.customers;
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
     * Method to get the total number of different products;
     *
     * @return the size of the product list as an int.
     */
    public int productCount() {
        return products.size();
    }

    private String generateProductCode() {
        String no = "";
        String zeros = "";
        no = Integer.toString(productCounter);
        zeros = "";
        for (int i = no.length(); i < 6; i++) {
            zeros += "0";
        }
        productCounter++;

        return zeros + no;
    }

    //Customer Methods
    /**
     * Method to add a new customer to the system.
     *
     * @param c the Customer object to add.
     */
    public void addCustomer(Customer c) {
        customers.add(c);
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

    //Sale Methods
    /**
     * Method to add a sale.
     *
     * @param s the sale to add.
     */
    public void addSale(Sale s) {
        sales.add(s);
    }
}
