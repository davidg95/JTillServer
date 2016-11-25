/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.tillserver;

import io.github.davidg95.Till.till.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private List<Discount> discounts;
    private List<Sale> sales;
    private List<Tax> tax;
    private List<Category> categorys;

    private static int productCounter;
    private static int customerCounter;
    private static int staffCounter;
    private static int discountCounter;
    private static int taxCounter;
    private static int categoryCounter;

    private final DBConnect dbConnection;
    private final GUI g;

    /**
     * Blank constructor which initialises the product and customers data
     * structures. ArrayList data structures are used.
     *
     * @param db DBConnect class.
     * @param g GUI class.
     */
    public Data(DBConnect db, GUI g) {
        this.dbConnection = db;
        this.g = g;
        products = new ArrayList<>();
        customers = new ArrayList<>();
        staff = new ArrayList<>();
        discounts = new ArrayList<>();
        tax = new ArrayList<>();
        categorys = new ArrayList<>();

    }

    public void loadDatabase() {
        if (dbConnection.isConnected()) {
            try {
                products = dbConnection.getAllProducts();
                customers = dbConnection.getAllCustomers();
                staff = dbConnection.getAllStaff();
                discounts = dbConnection.getAllDiscounts();
                tax = dbConnection.getAllTax();
                categorys = dbConnection.getAllCategorys();
                this.openFile();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
                products = new ArrayList<>();
                customers = new ArrayList<>();
                staff = new ArrayList<>();
                discounts = new ArrayList<>();
                tax = new ArrayList<>();
                categorys = new ArrayList<>();
            }
        } else {
            products = new ArrayList<>();
            customers = new ArrayList<>();
            staff = new ArrayList<>();
            discounts = new ArrayList<>();
            sales = new ArrayList<>();
            tax = new ArrayList<>();
            categorys = new ArrayList<>();
        }
    }

    public void updateDatabase() throws SQLException {
        dbConnection.updateWholeProducts(products);
        dbConnection.updateWholeCustomers(customers);
        dbConnection.updateWholeStaff(staff);
        dbConnection.updateWholeDiscounts(discounts);
        dbConnection.updateWholeTax(tax);
        dbConnection.updateWholeCategorys(categorys);
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

    public List<Discount> getDiscountList() {
        return this.discounts;
    }

    public List<Sale> getSalesList() {
        return this.sales;
    }

    public List<Tax> getTaxList() {
        return this.tax;
    }

    public List<Category> getCategorysList() {
        return this.categorys;
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

    public void setDiscountList(List<Discount> discounts) {
        this.discounts = discounts;
    }

    public void setSalesList(List<Sale> sales) {
        this.sales = sales;
    }

    public void setTaxList(List<Tax> tax) {
        this.tax = tax;
    }

    public void setCategorysList(List<Category> categorys) {
        this.categorys = categorys;
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
     * Method to check if a barcode is already in use.
     *
     * @param barcode the barcode to check.
     * @return true if the barcode is already in use, false otherwise.
     */
    public boolean checkBarcode(String barcode) {
        for (Product p : products) {
            if (p.getBarcode().equals(barcode)) {
                return true;
            }
        }
        return false;
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

    public Discount getProductsDiscount(Product p) throws DiscountNotFoundException {
        for (Discount d : discounts) {
            if (d.getId().equalsIgnoreCase(p.getDiscountID())) {
                return d;
            }
        }
        throw new DiscountNotFoundException(p.getDiscountID());
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
        String no;
        String zeros;
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

    public Discount getCustomersDiscount(Customer c) throws DiscountNotFoundException {
        String discountid = c.getDiscountID();
        for (Discount d : discounts) {
            if (d.getId().equalsIgnoreCase(discountid)) {
                return d;
            }
        }
        throw new DiscountNotFoundException(c.getDiscountID());
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
        String no;
        String zeros;
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
     * @return the member of staff who has logged in.
     * @throws LoginException if there was an error logging in.
     */
    public Staff login(String username, String password) throws LoginException {
        for (Staff s : staff) {
            if (s.getUsername().equals(username)) {
                s.login(password);
                return s;
            }
        }
        throw new LoginException("Your credentials were not recognised");
    }

    /**
     * Method to log in using an id. This will be used for logging in to a till.
     *
     * @param id the id to log in.
     * @return the member of staff who has logged in.
     * @throws LoginException if they are already logged in on a till.
     * @throws StaffNotFoundException if the id was not found.
     */
    public Staff login(String id) throws LoginException, StaffNotFoundException {
        for (Staff s : staff) {
            if (s.getId().equals(id)) {
                s.login();
                return s;
            }
        }
        throw new StaffNotFoundException(id);
    }

    /**
     * Method to log a member of staff out.
     *
     * @param id the id of the staff to log out.
     * @throws StaffNotFoundException if the staff member was not found.
     */
    public void logout(String id) throws StaffNotFoundException {
        for (Staff s : staff) {
            if (s.getId().equals(id)) {
                s.logout();
                return;
            }
        }
        throw new StaffNotFoundException(id);
    }

    /**
     * Method to log a member of staff out the till.
     *
     * @param id the id of the staff to log out.
     * @throws StaffNotFoundException if the staff member was not found.
     */
    public void tillLogout(String id) throws StaffNotFoundException {
        for (Staff s : staff) {
            if (s.getId().equals(id)) {
                s.tillLogout();
                return;
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
        String no;
        String zeros;
        no = Integer.toString(staffCounter);
        zeros = "";
        for (int i = no.length(); i < 6; i++) {
            zeros += "0";
        }
        staffCounter++;

        return zeros + no;
    }

    //Discount Methods
    /**
     * Method to add a new discount.
     *
     * @param d the discount to add.
     */
    public void addDiscount(Discount d) {
        d.setId(generateDiscountID());
        discounts.add(d);
    }

    /**
     * Method to remove a discount.
     *
     * @param d the discount to remove.
     */
    public void removeDiscount(Discount d) {
        discounts.remove(d);
    }

    /**
     * Method to remove a discount by id.
     *
     * @param id the id of the discount to remove.
     * @throws DiscountNotFoundException if the id was not found.
     */
    public void removeDiscount(String id) throws DiscountNotFoundException {
        for (int i = 0; i < discounts.size(); i++) {
            if (discounts.get(i).getId().equalsIgnoreCase(id)) {
                discounts.remove(i);
                return;
            }
        }
        throw new DiscountNotFoundException(id);
    }

    /**
     * Method to get a discount by id.
     *
     * @param id the id to find.
     * @return Discount that matches the id.
     * @throws DiscountNotFoundException if the id was not found.
     */
    public Discount getDiscount(String id) throws DiscountNotFoundException {
        for (Discount d : discounts) {
            if (d.getId().equalsIgnoreCase(id)) {
                return d;
            }
        }
        throw new DiscountNotFoundException(id);
    }

    /**
     * Method to get the amount of discounts.
     *
     * @return int value representing the amount of discounts.
     */
    public int discountCount() {
        return discounts.size();
    }

    /**
     * Method to generate a new discount id.
     *
     * @return new discount id.
     */
    public static String generateDiscountID() {
        String no;
        String zeros;
        no = Integer.toString(discountCounter);
        zeros = "";
        for (int i = no.length(); i < 5; i++) {
            zeros += "0";
        }
        discountCounter++;

        return "D" + zeros + no;
    }

    //Tax Methods
    /**
     * Method to add a new tax class.
     *
     * @param t the new tax class.
     */
    public void addTax(Tax t) {
        tax.add(t);
    }

    /**
     * Method to remove a tax class.
     *
     * @param t the taax class to remove.
     */
    public void removeTax(Tax t) {
        tax.remove(t);
    }

    /**
     * Method to remove a tax class by ID.
     *
     * @param id the ID to remove.
     * @throws TaxNotFoundException if the ID could not be found.
     */
    public void removeTax(String id) throws TaxNotFoundException {
        for (int i = 0; i < tax.size(); i++) {
            if (tax.get(i).getId().equalsIgnoreCase(id)) {
                tax.remove(i);
                return;
            }
        }
        throw new TaxNotFoundException(id);
    }

    /**
     * Method to get a tax class by ID.
     *
     * @param id the tax class to get.
     * @return the Tax class that matches the ID.
     * @throws TaxNotFoundException if the ID was not found.
     */
    public Tax getTax(String id) throws TaxNotFoundException {
        for (Tax t : tax) {
            if (t.getId().equalsIgnoreCase(id)) {
                return t;
            }
        }
        throw new TaxNotFoundException(id);
    }

    /**
     * Method to get a tax class by name.
     *
     * @param name the name to search.
     * @return the Tax class.
     * @throws TaxNotFoundException if the name was not found.
     */
    public Tax getTaxByName(String name) throws TaxNotFoundException {
        for (Tax t : tax) {
            if (t.getName().equalsIgnoreCase(name)) {
                return t;
            }
        }
        throw new TaxNotFoundException(name);
    }

    /**
     * Method to get the Tax count
     *
     * @return the Tax count as an int.
     */
    public int taxCount() {
        return tax.size();
    }

    public static String generateTaxID() {
        String no;
        String zeros;
        no = Integer.toString(taxCounter);
        zeros = "";
        for (int i = no.length(); i < 5; i++) {
            zeros += "0";
        }
        taxCounter++;

        return "T" + zeros + no;
    }

    //Category Methods
    /**
     * Method to add a category.
     *
     * @param c the category to add.
     */
    public void addCategory(Category c) {
        categorys.add(c);
    }

    /**
     * Method to remove a category.
     *
     * @param c the category to remove.
     */
    public void removeCategory(Category c) {
        categorys.remove(c);
    }

    /**
     * Method to remove a category based on its id.
     *
     * @param id the id of the category to remove.
     * @throws CategoryNotFoundException if the id was not found.
     */
    public void removeCategory(String id) throws CategoryNotFoundException {
        for (int i = 0; i < categorys.size(); i++) {
            if (categorys.get(i).getID().equalsIgnoreCase(id)) {
                categorys.remove(i);
            }
        }
        throw new CategoryNotFoundException("Category " + id + " could not be found");
    }

    /**
     * Method to get a category based on its id.
     *
     * @param id the id of the category to get.
     * @return the category matching the id if any exists.
     * @throws CategoryNotFoundException if the category could not be found
     * matching the id.
     */
    public Category getCategory(String id) throws CategoryNotFoundException {
        for (Category c : categorys) {
            if (c.getID().equalsIgnoreCase(id)) {
                return c;
            }
        }
        throw new CategoryNotFoundException("Category " + id + " could not be found");
    }

    /**
     * Get the amount of categorys.
     *
     * @return int value representing the amount of categorys.
     */
    public int categoryCount() {
        return categorys.size();
    }

    public static String generateCategoryID() {
        String no;
        String zeros;
        no = Integer.toString(categoryCounter);
        zeros = "";
        for (int i = no.length(); i < 5; i++) {
            zeros += "0";
        }
        categoryCounter++;

        return "C" + zeros + no;
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
        HashMap<String, String> configs = new HashMap<>();

        configs.put("products", "" + productCounter);
        configs.put("customers", "" + customerCounter);
        configs.put("staff", "" + staffCounter);
        configs.put("discounts", "" + discountCounter);
        configs.put("tax", "" + taxCounter);
        configs.put("categorys", "" + categoryCounter);

        try {
            dbConnection.updateWholeConfigs(configs);
        } catch (SQLException ex) {
        }
    }

    /**
     * Method to load the configs.
     */
    public final void openFile() {
        try {
            HashMap<String, String> configs = dbConnection.getAllConfigs();

            productCounter = Integer.parseInt(configs.get("products"));
            customerCounter = Integer.parseInt(configs.get("customers"));
            staffCounter = Integer.parseInt(configs.get("staff"));
            discountCounter = Integer.parseInt(configs.get("discounts"));
            taxCounter = Integer.parseInt(configs.get("tax"));
            categoryCounter = Integer.parseInt(configs.get("categorys"));
        } catch (SQLException ex) {
        }
    }
}
