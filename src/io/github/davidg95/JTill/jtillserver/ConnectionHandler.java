/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import io.github.davidg95.jconn.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;

/**
 * Thread for handling incoming connections.
 *
 * @author David
 */
public class ConnectionHandler {

    private static final Logger LOG = Logger.getGlobal();

    private final DBConnect dc; //The main database connection.

    public Staff staff; //The staff member currently logged on.
    public Till till; //The till that is using this connection.

    /**
     * Constructor for Connection thread.
     */
    public ConnectionHandler() {
        this.dc = (DBConnect) DataConnect.get();
    }

    @JConnMethod("CONNECT")
    public Till initialConnection(@JConnParameter("UUID") UUID uuid, @JConnParameter("NAME") String name) throws JTillException {
        till = dc.connectTill(name, uuid, null);
        return till;
    }

    @JConnMethod("RECONNECT")
    public void reconnect(@JConnParameter("UUID") UUID uuid, @JConnParameter("SITE") String site, @JConnParameter("STAFF") Staff staff) throws JTillException {
        till = dc.connectTill(site, uuid, staff);
        this.staff = staff;
    }

    @JConnMethod("PROPERTIES")
    public Properties getProperties() {
        LOG.log(Level.INFO, till.getName() + " is retreiving settings");
        return Settings.getInstance().getProperties();
    }

    @JConnMethod("NEWPRODUCT")
    public void newProduct(@JConnParameter("PRODUCT") Product p) throws IOException, SQLException {
        dc.addProduct(p);
    }

    @JConnMethod("REMOVEPRODUCT")
    public void removeProduct(@JConnParameter("CODE") String code) throws ProductNotFoundException, IOException, SQLException {
        dc.removeProduct(code);
    }

    @JConnMethod("PURCHASE")
    public void purchase(@JConnParameter("PRODUCT") String p, @JConnParameter("AMOUNT") int amount) throws IOException, ProductNotFoundException, OutOfStockException, SQLException {
        dc.purchaseProduct(p, amount);
    }

    @JConnMethod("GETPRODUCT")
    public Product getProduct(@JConnParameter("CODE") String code) throws IOException, ProductNotFoundException, SQLException {
        return dc.getProduct(code);
    }

    @JConnMethod("UPDATEPRODUCT")
    public void updateProduct(@JConnParameter("PRODUCT") Product p) throws IOException, ProductNotFoundException, SQLException {
        dc.updateProduct(p);
    }

    @JConnMethod("GETPRODUCTBARCODE")
    public Product getProductByBarcode(@JConnParameter("BARCODE") String barcode) throws IOException, ProductNotFoundException, SQLException {
        return dc.getProductByBarcode(barcode);
    }

    @JConnMethod("CHECKBARCODE")
    public boolean checkBarcode(@JConnParameter("CHECKBARCODE") String barcode) throws IOException, SQLException {
        return dc.checkBarcode(barcode);
    }

    @JConnMethod("GETALLPRODUCTS")
    public List<Product> getAllProducts() throws IOException, SQLException {
        return dc.getAllProducts();
    }

    @JConnMethod("PRODUCTLOOKUP")
    public List<Product> productLookup(@JConnParameter("TERMS") String terms) throws IOException, SQLException {
        return dc.productLookup(terms);
    }

    @JConnMethod("customerid")
    public boolean isCustomerIDUsed(@JConnParameter("id") String id) throws IOException, SQLException {
        return dc.isCustomerIDUsed(id);
    }

    @JConnMethod("NEWCUSTOMER")
    public void newCustomer(@JConnParameter("CUSTOMER") Customer c) throws IOException, SQLException {
        dc.addCustomer(c);
    }

    @JConnMethod("REMOVECUSTOMER")
    public void removeCustomer(@JConnParameter("c") Customer c) throws IOException, JTillException, SQLException {
        dc.removeCustomer(c);
    }

    @JConnMethod("GETCUSTOMER")
    public Customer getCustomer(@JConnParameter("ID") String id) throws IOException, JTillException, SQLException {
        return dc.getCustomer(id);
    }

    @JConnMethod("GETCUSTOMERBYNAME")
    public List<Customer> getCustomerByName(@JConnParameter("NAME") String name) throws IOException, JTillException, SQLException {
        return dc.getCustomerByName(name);
    }

    @JConnMethod("UPDATECUSTOMER")
    public void updateCustomer(@JConnParameter("CUSTOMER") Customer c) throws IOException, JTillException, SQLException {
        dc.updateCustomer(c);
    }

    @JConnMethod("GETALLCUSTOMERS")
    public List<Customer> getAllCustomers() throws IOException, SQLException {
        return dc.getAllCustomers();
    }

    @JConnMethod("ADDSTAFF")
    public Staff addStaff(@JConnParameter("STAFF") Staff s, @JConnParameter("PASSWORD") String password) throws IOException, SQLException {
        s = dc.addStaff(s, password);
        return s;
    }

    @JConnMethod("REMOVESTAFF")
    public void removeStaff(@JConnParameter("S") Staff s) throws IOException, JTillException, SQLException {
        dc.removeStaff(s);
    }

    @JConnMethod("GETSTAFF")
    public Staff getStaff(@JConnParameter("ID") int id) throws IOException, JTillException, SQLException {
        final Staff s = dc.getStaff(id);
        return s;
    }

    @JConnMethod("UPDATESTAFF")
    public void updateStaff(@JConnParameter("STAFF") Staff s) throws IOException, JTillException, SQLException {
        dc.updateStaff(s);
    }

    @JConnMethod("GETALLSTAFF")
    public List<Staff> getAllStaff() throws IOException, SQLException, SQLException {
        final List<Staff> staffList = dc.getAllStaff();
        return staffList;
    }

    @JConnMethod("STAFFCOUNT")
    public int staffCount() throws IOException, SQLException {
        return dc.getStaffCount();
    }

    @JConnMethod("ADDSALE")
    public Sale addSale(@JConnParameter("SALE") Sale s) throws IOException, SQLException {
        return dc.addSale(s);
    }

    @JConnMethod("SENDSALES")
    public void sendSales(@JConnParameter("SALES") List<Sale> sales) throws IOException, SQLException {
        for (Sale s : sales) {
            dc.addSale(s);
        }
    }

    @JConnMethod("GETALLSALES")
    public List<Sale> getAllSales() throws IOException, SQLException {
        return dc.getAllSales();
    }

    @JConnMethod("GETSALE")
    public Sale getSale(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getSale(id);
    }

    @JConnMethod("UPDATESALE")
    public Sale updateSale(@JConnParameter("SALE") Sale sale) throws IOException, SQLException, JTillException {
        return dc.updateSale(sale);
    }

    @JConnMethod("GETSALEDATERANGE")
    public List<Sale> getSalesInRange(@JConnParameter("START") Date start, @JConnParameter("END") Date end) throws IOException, SQLException {
        return dc.getSalesInRange(start, end);
    }

    @JConnMethod("SUSPENDSALE")
    public void suspendSale(@JConnParameter("SALE") Sale sale, @JConnParameter("STAFF") Staff s) throws IOException {
        dc.suspendSale(sale, s);
    }

    @JConnMethod("RESUMESALE")
    public Sale resumeSale(@JConnParameter("STAFF") Staff s) throws IOException {
        return dc.resumeSale(s);
    }

    @JConnMethod("LOGIN")
    public Staff login(@JConnParameter("USERNAME") String username, @JConnParameter("PASSWORD") String password) throws IOException, LoginException, SQLException {
        password = Encryptor.decrypt(password);
        final Staff s = dc.login(username, password);
        ConnectionHandler.this.staff = s;
        LOG.log(Level.INFO, s.getName() + " has logged in");
        return s;
    }

    @JConnMethod("TILLLOGIN")
    public Staff tillLogin(@JConnParameter("ID") int id) throws IOException, LoginException, SQLException {
        final Staff s = dc.tillLogin(id);
        ConnectionHandler.this.staff = s;
        LOG.log(Level.INFO, staff.getName() + " has logged in from " + till.getName());
        return s;
    }

    @JConnMethod("LOGOUT")
    public void logout(@JConnParameter("STAFF") Staff s) throws IOException, JTillException {
        dc.logout(s);
        LOG.log(Level.INFO, staff.getName() + " has logged out");
        ConnectionHandler.this.staff = null;
    }

    @JConnMethod("TILLLOGOUT")
    public void tillLogout(@JConnParameter("STAFF") Staff s) throws IOException, JTillException {
        dc.tillLogout(s);
        LOG.log(Level.INFO, staff.getName() + " has logged out");
        ConnectionHandler.this.staff = null;
    }

    @JConnMethod("changepassword")
    public void changePassword(@JConnParameter("username") String username, @JConnParameter("newp") String newPassword) throws JTillException, SQLException {
        dc.changePassword(username, newPassword);
    }

    @JConnMethod("ADDCATEGORY")
    public void addCategory(@JConnParameter("CATEGORY") Category c) throws IOException, SQLException {
        dc.addCategory(c);
    }

    @JConnMethod("UPDATECATEGORY")
    public Category updateCategory(@JConnParameter("CATEGORY") Category c) throws IOException, SQLException, JTillException {
        return dc.updateCategory(c);
    }

    @JConnMethod("REMOVECATEGORY")
    public void removeCategory(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        dc.removeCategory(id);
    }

    @JConnMethod("GETCATEGORY")
    public Category getCategory(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getCategory(id);
    }

    @JConnMethod("GETALLCATEGORYS")
    public List<Category> getAllCategorys() throws IOException, SQLException, JTillException {
        return dc.getAllCategorys();
    }

    @JConnMethod("GETPRODUCTSINCATEGORY")
    public List<Product> getProductsInCategory(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getProductsInCategory(id);
    }

    @JConnMethod("ADDDISCOUNT")
    public Discount addDiscount(@JConnParameter("DISCOUNT") Discount d) throws IOException, SQLException {
        return dc.addDiscount(d);
    }

    @JConnMethod("UPDATEDISCOUNT")
    public Discount updateDiscount(@JConnParameter("DISCOUNT") Discount d) throws IOException, SQLException, DiscountNotFoundException {
        return dc.updateDiscount(d);
    }

    @JConnMethod("REMOVEDISCOUNT")
    public void removeDiscount(@JConnParameter("ID") int id) throws IOException, SQLException, DiscountNotFoundException {
        dc.removeDiscount(id);
    }

    @JConnMethod("GETDISCOUNT")
    public Discount getDiscount(@JConnParameter("ID") int id) throws IOException, SQLException, DiscountNotFoundException {
        return dc.getDiscount(id);
    }

    @JConnMethod("GETALLDISCOUNTS")
    public List<Discount> getAllDiscounts() throws IOException, SQLException {
        return dc.getAllDiscounts();
    }

    @JConnMethod("ADDTAX")
    public void addTax(@JConnParameter("TAX") Tax t) throws IOException, SQLException {
        dc.addTax(t);
    }

    @JConnMethod("REMOVETAX")
    public void removeTax(@JConnParameter("t") Tax t) throws IOException, SQLException, JTillException {
        dc.removeTax(t);
    }

    @JConnMethod("GETTAX")
    public Tax getTax(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getTax(id);
    }

    @JConnMethod("UPDATETAX")
    public Tax updateTax(@JConnParameter("TAX") Tax t) throws IOException, SQLException, JTillException {
        return dc.updateTax(t);
    }

    @JConnMethod("GETALLTAX")
    public List<Tax> getAllTax() throws IOException, SQLException, JTillException {
        return dc.getAllTax();
    }

    @JConnMethod("GETPRODUCTSINTAX")
    public List<Product> getProductsInTax(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getProductsInTax(id);
    }

    @JConnMethod("ADDSCREEN")
    public Screen addScreen(@JConnParameter("SCREEN") Screen s) throws IOException, SQLException, JTillException {
        return dc.addScreen(s);
    }

    @JConnMethod("ADDBUTTON")
    public TillButton addButton(@JConnParameter("BUTTON") TillButton b) throws IOException, SQLException, JTillException {
        return dc.addButton(b);
    }

    @JConnMethod("REMOVESCREEN")
    public void removeScreen(@JConnParameter("SCREEN") Screen s) throws IOException, SQLException, ScreenNotFoundException {
        dc.removeScreen(s);
    }

    @JConnMethod("REMOVEBUTTON")
    public void removeButton(@JConnParameter("BUTTON") TillButton b) throws IOException, SQLException, JTillException {
        dc.removeButton(b);
    }

    @JConnMethod("UPDATESCREEN")
    public Screen updateScreen(@JConnParameter("SCREEN") Screen s) throws IOException, SQLException, ScreenNotFoundException {
        return dc.updateScreen(s);
    }

    @JConnMethod("UPDATEBUTTON")
    public TillButton updateButton(@JConnParameter("BUTTON") TillButton b) throws IOException, SQLException, JTillException {
        return dc.updateButton(b);
    }

    @JConnMethod("GETSCREEN")
    public Screen getScreen(@JConnParameter("ID") int id) throws IOException, SQLException, ScreenNotFoundException {
        return dc.getScreen(id);
    }

    @JConnMethod("GETBUTTON")
    public TillButton getButton(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getButton(id);
    }

    @JConnMethod("GETALLSCREENS")
    public List<Screen> getAllScreens() throws IOException, SQLException {
        return dc.getAllScreens();
    }

    @JConnMethod("GETALLBUTTONS")
    public List<TillButton> getAllButtons() throws IOException, SQLException {
        return dc.getAllButtons();
    }

    @JConnMethod("GETBUTTONSONSCREEN")
    public List<TillButton> getButtonsOnScreen(@JConnParameter("SCREEN") Screen s) throws IOException, SQLException, ScreenNotFoundException {
        return dc.getButtonsOnScreen(s);
    }

    @JConnMethod("ASSISSTANCE")
    public void assisstance(@JConnParameter("MESSAGE") String message) throws IOException {
        dc.assisstance(staff.getName() + " on terminal " + till.getName() + " has requested assistance with message:\n" + message);
    }

    @JConnMethod("UNCASHEDSALES")
    public List<Sale> getUncashedSales(@JConnParameter("NAME") String terminal) throws IOException, SQLException {
        return dc.getUncashedSales(terminal);
    }

    @JConnMethod("SENDEMAIL")
    public void sendEmail(@JConnParameter("MESSAGE") String message) throws IOException, SQLException {
        dc.sendEmail(message);
    }

    @JConnMethod("SENDRECEIPT")
    public void sendReceipt(@JConnParameter("EMAIL") String email, @JConnParameter("SALE") Sale sale) throws IOException, MessagingException {
        dc.emailReceipt(email, sale);
    }

    @JConnMethod("ADDTILL")
    public Till addTill(@JConnParameter("TILL") Till t) throws IOException, SQLException {
        return dc.addTill(t);
    }

    @JConnMethod("REMOVETILL")
    public void removeTill(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        dc.removeTill(id);
    }

    @JConnMethod("GETTILL")
    public Till getTill(@JConnParameter("TERMINAL") int id) throws IOException, SQLException, JTillException {
        return dc.getTill(id);
    }

    @JConnMethod("UPDATETILL")
    public Till updateTill(@JConnParameter("TILL") Till t) throws IOException, SQLException, JTillException {
        return dc.updateTill(t);
    }

    @JConnMethod("GETALLTILLS")
    public List<Till> getAllTills() throws IOException, SQLException {
        return dc.getAllTills();
    }

    @JConnMethod("CONNECTTILL")
    public Till connectTill(@JConnParameter("NAME") String name, @JConnParameter("UUID") UUID uuid) throws IOException, SQLException, JTillException {
        return dc.connectTill(name, uuid, null);
    }

    @JConnMethod("GETCONNECTEDTILLS")
    public List<Till> getAllConnectedTills() throws IOException {
        return dc.getConnectedTills();
    }

    @JConnMethod("CHECKTILLNAME")
    public boolean isTillNameUsed(@JConnParameter("NAME") String name) throws IOException, SQLException {
        return dc.isTillNameUsed(name);
    }

    @JConnMethod("SETSETTING")
    public void setSetting(@JConnParameter("KEY") String key, @JConnParameter("VALUE") String value) throws IOException {
        dc.setSetting(key, value);
    }

    @JConnMethod("GETSETTING")
    public String getSetting(@JConnParameter("KEY") String key) throws IOException {
        return dc.getSetting(key);
    }

    @JConnMethod("GETSETTINGDEFAULT")
    public String getSettingDefault(@JConnParameter("KEY") String key, @JConnParameter("DEF") String def_value) throws IOException {
        return dc.getSetting(key, def_value);
    }

    @JConnMethod("ISTILLLOGGEDIN")
    public boolean isTillLoggedIn(@JConnParameter("STAFF") Staff s) throws IOException, JTillException, SQLException {
        return dc.isTillLoggedIn(s);
    }

    @JConnMethod("CHECKUSERNAME")
    public boolean checkUsername(@JConnParameter("USERNAME") String username) throws IOException, SQLException, JTillException {
        return dc.checkUsername(username);
    }

    @JConnMethod("ADDWASTEREPORT")
    public void addWasteReport(@JConnParameter("WASTE") List<WasteItem> items) throws IOException, SQLException, JTillException {
        dc.addWasteReport(items);
    }

    @JConnMethod("ADDWASTEITEM")
    public WasteItem addWasteItem(@JConnParameter("ITEM") WasteItem wi) throws IOException, SQLException, JTillException {
        return dc.addWasteItem(wi);
    }

    @JConnMethod("REMOVEWASTEITEM")
    public void removeWasteItem(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        dc.removeWasteItem(id);
    }

    @JConnMethod("GETWASTEITEM")
    public WasteItem getWasteItem(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getWasteItem(id);
    }

    @JConnMethod("GETALLWASTEITEMS")
    public List<WasteItem> getAllWasteItems() throws IOException, SQLException, JTillException {
        return dc.getAllWasteItems();
    }

    @JConnMethod("ADDWASTEREASON")
    public void addWasteReason(@JConnParameter("WASTE") WasteReason wr) throws IOException, SQLException, JTillException {
        dc.addWasteReason(wr);
    }

    @JConnMethod("REMOVEWASTEREASON")
    public void removeWasteReason(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        dc.removeWasteReason(id);
    }

    @JConnMethod("GETWASTEREASON")
    public WasteReason getWasteReason(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getWasteReason(id);
    }

    @JConnMethod("GETALLWASTEREASONS")
    public List<WasteReason> getAllWasteReasons() throws IOException, SQLException, JTillException {
        return dc.getAllWasteReasons();
    }

    @JConnMethod("UPDATEWASTEREASON")
    public WasteReason updateWasteReason(@JConnParameter("WASTE") WasteReason wr) throws IOException, SQLException, JTillException {
        return dc.updateWasteReason(wr);
    }

    @JConnMethod("ADDSUPPLIER")
    public void addSupplier(@JConnParameter("SUPPLIER") Supplier s) throws IOException, SQLException, JTillException {
        dc.addSupplier(s);
    }

    @JConnMethod("REMOVESUPPLIER")
    public void removeSupplier(@JConnParameter("s") Supplier s) throws IOException, SQLException, JTillException {
        dc.removeSupplier(s);
    }

    @JConnMethod("GETSUPPLIER")
    public Supplier getSupplier(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getSupplier(id);
    }

    @JConnMethod("GETALLSUPPLIERS")
    public List<Supplier> getAllSuppliers() throws IOException, SQLException {
        return dc.getAllSuppliers();
    }

    @JConnMethod("UPDATESUPPLIER")
    public Supplier updateSupplier(@JConnParameter("SUPPLIER") Supplier s) throws IOException, SQLException, JTillException {
        return dc.updateSupplier(s);
    }

    @JConnMethod("ADDDEPARTMENT")
    public void addDepartment(@JConnParameter("DEPARTMENT") Department d) throws IOException, SQLException, JTillException {
        dc.addDepartment(d);
    }

    @JConnMethod("REMOVEDEPARTMENT")
    public void removeDepartment(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        dc.removeDepartment(id);
    }

    @JConnMethod("GETDEPARTMENT")
    public Department getDepartment(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getDepartment(id);
    }

    @JConnMethod("GETALLDEPARTMENTS")
    public List<Department> getAllDepartments() throws IOException, SQLException {
        return dc.getAllDepartments();
    }

    @JConnMethod("UPDATEDEPARTMENT")
    public Department updateDepartment(@JConnParameter("DEPARTMENT") Department d) throws IOException, SQLException, JTillException {
        return dc.updateDepartment(d);
    }

    @JConnMethod("ADDSALEITEM")
    public SaleItem addSaleItem(@JConnParameter("SALE") Sale s, @JConnParameter("ITEM") SaleItem i) throws IOException, SQLException, JTillException {
        return dc.addSaleItem(s, i);
    }

    @JConnMethod("GETSALEITEM")
    public SaleItem getSaleItem(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getSaleItem(id);
    }

    @JConnMethod("GETALLSALEITEMS")
    public List<SaleItem> getAllSaleItems() throws IOException, SQLException {
        return dc.getAllSaleItems();
    }

    @JConnMethod("SUBMITSALEITEMQUERY")
    public List<SaleItem> subSaleItemQuery(@JConnParameter("QUERY") String q) throws IOException, SQLException {
        return dc.submitSaleItemQuery(q);
    }

    @JConnMethod("GETTOTALSOLDITEM")
    public int getTotalSoldItem(@JConnParameter("ID") String id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getTotalSoldOfItem(id);
    }

    @JConnMethod("GETVALUESOLDITEM")
    public BigDecimal getValueSoldItem(@JConnParameter("ID") String id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getTotalValueSold(id);
    }

    @JConnMethod("GETTOTALWASTEDITEM")
    public int getTotalWastedItem(@JConnParameter("ID") String id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getTotalWastedOfItem(id);
    }

    @JConnMethod("GETVALUEWASTEDITEM")
    public BigDecimal getValueWastedItem(@JConnParameter("ID") String id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getValueWastedOfItem(id);
    }

    @JConnMethod("ADDRECEIVEDITEM")
    public void addReceivedItem(@JConnParameter("ITEM") ReceivedItem i, @JConnParameter("REP") int report) throws IOException, SQLException {
        dc.addReceivedItem(i, report);
    }

    @JConnMethod("GETSPENTONITEM")
    public BigDecimal getValueSpentOnItem(@JConnParameter("ID") String id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getValueSpentOnItem(id);
    }

    @JConnMethod("CLOCKON")
    public void clockOn(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        dc.clockOn(id);
    }

    @JConnMethod("CLOCKOFF")
    public void clockOff(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        dc.clockOff(id);
    }

    @JConnMethod("GETALLCLOCKS")
    public List<ClockItem> getAllClocks(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getAllClocks(id);
    }

    @JConnMethod("CLEARCLOCKS")
    public void clearClocks(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        dc.clearClocks(id);
    }

    @JConnMethod("ADDTRIGGER")
    public Trigger addTrigger(@JConnParameter("TRIGGER") Trigger t) throws IOException, SQLException {
        return dc.addTrigger(t);
    }

    @JConnMethod("GETDISCOUNTBUCKETS")
    public List<DiscountBucket> getDiscountBuckets(@JConnParameter("ID") int id) throws IOException, SQLException, DiscountNotFoundException {
        return dc.getDiscountBuckets(id);
    }

    @JConnMethod("REMOVETRIGGER")
    public void removeTrigger(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        dc.removeTrigger(id);
    }

    @JConnMethod("GETVALIDDISCOUNTS")
    public List<Discount> getValidDiscounts() throws IOException, SQLException, JTillException {
        return dc.getValidDiscounts();
    }

    @JConnMethod("ADDBUCKET")
    public DiscountBucket addBucket(@JConnParameter("BUCKET") DiscountBucket b) throws IOException, SQLException, JTillException {
        return dc.addBucket(b);
    }

    @JConnMethod("REMOVEBUCKET")
    public void removeBucket(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        dc.removeBucket(id);
    }

    @JConnMethod("GETBUCKETTRIGGERS")
    public List<Trigger> getBucketTriggers(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getBucketTriggers(id);
    }

    @JConnMethod("UPDATETRIGGER")
    public Trigger updateTrigger(@JConnParameter("TRIGGER") Trigger t) throws IOException, SQLException, JTillException {
        return dc.updateTrigger(t);
    }

    @JConnMethod("UPDATEBUCKET")
    public DiscountBucket updateBucket(@JConnParameter("BUCKET") DiscountBucket b) throws IOException, SQLException, JTillException {
        return dc.updateBucket(b);
    }

    @JConnMethod("GETUNCASHEDTILLSALES")
    public List<Sale> getUncashedTerminalSales(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getUncachedTillSales(id);
    }

    @JConnMethod("SEARCHSALEITEMS")
    public List<SaleItem> searchSaleItems(@JConnParameter("DEP") int department, @JConnParameter("CAT") int category, @JConnParameter("START") Date start, @JConnParameter("END") Date end) throws IOException, SQLException, JTillException {
        return dc.searchSaleItems(department, category, start, end);
    }

    @JConnMethod("CONNTERM")
    public void terminateConnection() throws IOException, SQLException, JTillException {
        dc.logout(staff);
        dc.tillLogout(staff);
    }

    @JConnMethod("GETTERMINALSALES")
    public List<Sale> getTerminalSales(@JConnParameter("START") Date start, @JConnParameter("END") Date end, @JConnParameter("TERMINAL") int terminal, @JConnParameter("UNCASHEDFLAG") boolean uncashedOnly) throws IOException, SQLException, JTillException {
        return dc.getTerminalSales(start, end, terminal, uncashedOnly);
    }

    @JConnMethod("GETALLTERMINALSALES")
    public List<Sale> getAllTerminalSales(@JConnParameter("TERMINAL") int terminal, @JConnParameter("UNCASHEDFLAG") boolean uncashedOnly) throws IOException, SQLException, JTillException {
        return dc.getAllTerminalSales(terminal, uncashedOnly);
    }

    @JConnMethod("INTEGRITYCHECK")
    public void integrityCheck() throws IOException, SQLException {
        dc.integrityCheck();
    }

    @JConnMethod("GETPRODUCTSADVANCED")
    public List<Product> getProductsAdvanced(@JConnParameter("WHERE") String WHERE) throws IOException, SQLException {
        return dc.getProductsAdvanced(WHERE);
    }

    @JConnMethod("GETSTAFFSALES")
    public List<Sale> getStaffSales(@JConnParameter("STAFF") Staff s) throws IOException, SQLException, JTillException {
        return dc.getStaffSales(s);
    }

    @JConnMethod("DATABASEINFO")
    public Object[] databaseInfo() throws IOException, SQLException {
        return dc.databaseInfo();
    }

    @JConnMethod("GETBGIMAGE")
    public File getBackgroundImage() throws IOException, JTillException {
        return dc.getLoginBackground();
    }

    @JConnMethod("REINITTILLS")
    public void reinitTills() throws IOException, JTillException {
        dc.reinitialiseAllTills();
    }

    @JConnMethod("CLEARSALES")
    public int clearSalesData() throws IOException, SQLException {
        return dc.clearSalesData();
    }

    @JConnMethod("ADDRECREP")
    public void addReceivedReport(@JConnParameter("REP") ReceivedReport rep) throws IOException, SQLException {
        dc.addReceivedReport(rep);
    }

    @JConnMethod("GETRECREP")
    public List<ReceivedReport> getAllReceivedReports() throws IOException, SQLException {
        return dc.getAllReceivedReports();
    }

    @JConnMethod("UPDATERECREP")
    public ReceivedReport updateReceivedReport(@JConnParameter("RECREP") ReceivedReport rr) throws IOException, SQLException {
        return dc.updateReceivedReport(rr);
    }

    @JConnMethod("SENDDATA")
    public void sendData(@JConnParameter("ID") int id, @JConnParameter("DATA") String[] data) throws IOException, SQLException {
        dc.sendData(id, data);
    }

    @JConnMethod("DOWNLOADTER")
    public byte[] downloadTerminalUpdate() throws Exception {
        return dc.downloadTerminalUpdate();
    }

    @JConnMethod("LOGOUTTERMINAL")
    public void logoutTerminal(@JConnParameter("ID") int id) throws IOException, JTillException {
        dc.logoutTill(id);
    }

    @JConnMethod("ISINHERITED")
    public List<Screen> checkInheritance(@JConnParameter("SCREEN") Screen s) throws IOException, SQLException, JTillException {
        return dc.checkInheritance(s);
    }

    @JConnMethod("GETTILLSTAFF")
    public Staff getTillStaff(@JConnParameter("ID") int id) throws IOException, JTillException {
        return dc.getTillStaff(id);
    }

    @JConnMethod("ZREPORT")
    public TillReport zReport(@JConnParameter("TERMINAL") Till terminal, @JConnParameter("DECLARED") BigDecimal declared, @JConnParameter("STAFF") Staff staff) throws IOException, SQLException, JTillException {
        return dc.zReport(terminal, declared, staff);
    }

    @JConnMethod("XREPORT")
    public TillReport xReport(@JConnParameter("TERMINAL") Till terminal, @JConnParameter("DECLARED") BigDecimal declared, @JConnParameter("STAFF") Staff staff) throws IOException, SQLException, JTillException {
        return dc.xReport(terminal, declared, staff);
    }

    @JConnMethod("PURGE")
    public void purgeDatabase() throws IOException, SQLException {
        dc.purgeDatabase();
    }

    @JConnMethod("REMOVECASHED")
    public int removeCashedSales() throws IOException, SQLException {
        return dc.removeCashedSales();
    }

    @JConnMethod("DECLARATIONREPORTS")
    public List<TillReport> getDeclarationReports(@JConnParameter("TERMINAL") int terminal) throws IOException, SQLException {
        return dc.getDeclarationReports(terminal);
    }

    @JConnMethod("REMOVEDECLARATIONREPORT")
    public void removeDeclarationReport(@JConnParameter("ID") int id) throws IOException, SQLException {
        dc.removeDeclarationReport(id);
    }

    @JConnMethod("TOTALRECEIVED")
    public int getTotalReceivedOfItem(@JConnParameter("ID") String id) throws IOException, SQLException {
        return dc.getTotalReceivedOfItem(id);
    }

    @JConnMethod("CONSOLIDATED")
    public List<Sale> consolidated(@JConnParameter("START") Date start, @JConnParameter("END") Date end, @JConnParameter("TILL") int till) throws IOException, SQLException {
        return dc.consolidated(start, end, till);
    }

    @JConnMethod("GETREFUNDS")
    public BigDecimal refunds(@JConnParameter("START") Date start, @JConnParameter("END") Date end, @JConnParameter("TILL") int till) throws IOException, SQLException {
        return dc.getRefunds(start, end, till);
    }

    @JConnMethod("GETWASTAGE")
    public BigDecimal getWasage(@JConnParameter("START") Date start, @JConnParameter("END") Date end) throws IOException, SQLException {
        return dc.getWastage(start, end);
    }

    @JConnMethod("SUBMITSTOCKTAKE")
    public void submitStockTake(@JConnParameter("PRODUCTS") List<Product> products) throws IOException, SQLException {
        dc.submitStockTake(products);
    }

    @JConnMethod("CATSINDEP")
    public List<Category> getCategoriesInDepartment(@JConnParameter("DEP") int department) throws IOException, SQLException {
        return dc.getCategoriesInDepartment(department);
    }

    @JConnMethod("GETPRODUCTSINDEPARTMENT")
    public List<Product> getProductsInDepartment(@JConnParameter("ID") int id) throws IOException, SQLException {
        return dc.getProductsInDepartment(id);
    }

    @JConnMethod("ADDCONDIMENT")
    public Condiment addCondiment(@JConnParameter("C") Condiment c) throws IOException, SQLException {
        return dc.addCondiment(c);
    }

    @JConnMethod("GETPRODUCTSCONDIMENTS")
    public List<Condiment> getProductsCondiments(@JConnParameter("ID") String id) throws IOException, SQLException {
        return dc.getProductsCondiments(id);
    }

    @JConnMethod("UPDATECONDIMENT")
    public Condiment updateCondiment(@JConnParameter("C") Condiment c) throws IOException, SQLException, JTillException {
        return dc.updateCondiment(c);
    }

    @JConnMethod("REMOVECONDIMENT")
    public void removeCondiment(@JConnParameter("ID") int id) throws IOException, SQLException {
        dc.removeCondiment(id);
    }

    @JConnMethod("SALESBYDEPARTMENT")
    public List<SaleItem> getSalesByDepartment(@JConnParameter("ID") int id) throws IOException, SQLException {
        return dc.getSalesByDepartment(id);
    }

    @JConnMethod("ADDORDER")
    public Order addOrder(@JConnParameter("ORDER") Order o) throws IOException, SQLException {
        return dc.addOrder(o);
    }

    @JConnMethod("UPDATEORDER")
    public void updateOrder(@JConnParameter("ORDER") Order o) throws IOException, SQLException, JTillException {
        dc.updateOrder(o);
    }

    @JConnMethod("GETALLORDERS")
    public List<Order> getAllOrders() throws IOException, SQLException {
        return dc.getAllOrders();
    }

    @JConnMethod("DELETEORDER")
    public void deleteOrder(@JConnParameter("ID") int id) throws IOException, SQLException {
        dc.deleteOrder(id);
    }

    @JConnMethod("TERMINALINIT")
    public HashMap<String, Object> terminalInit(@JConnParameter("ID") int id, @JConnParameter("DATA") String[] data) throws IOException {
        return dc.terminalInit(id, data);
    }

    @JConnMethod("INITCOMPLETE")
    public void initComplete() throws IOException {
        dc.initComplete();
    }

    @JConnMethod("GETINITS")
    public int getInits() throws IOException {
        return dc.getInits();
    }

    @JConnMethod("ISTILLCONNECTED")
    public boolean isTillConnected(@JConnParameter("ID") int id) throws IOException {
        return dc.isTillConnected(id);
    }

    @JConnMethod("BACKUP")
    public String performBackup() throws IOException {
        return dc.performBackup();
    }

    @JConnMethod("LISTBACKUPS")
    public List<String> getBackupList() throws IOException {
        return dc.getBackupList();
    }

    @JConnMethod("DELETEBACKUP")
    public void clearBackup(@JConnParameter("NAME") String name) throws IOException {
        dc.clearBackup(name);
    }

    @JConnMethod("LICENSEINFO")
    public Object[] getLicenseInfo() throws IOException {
        return dc.getLicenseInfo();
    }

    @JConnMethod("SUBMITSQL")
    public void submitSQL(@JConnParameter("SQL") String SQL) throws IOException, SQLException {
        dc.submitSQL(SQL);
    }

    @JConnMethod("DELETEWASTEREASON")
    public void deleteWasteReason(@JConnParameter("WR") WasteReason wr) throws SQLException, JTillException, JTillException {
        dc.deleteWasteReason(wr);
    }

    @JConnMethod("GETUSEDWASTEREASONS")
    public List<WasteReason> getUsedWasteReasons() throws IOException, SQLException {
        return dc.getUsedWasteReasons();
    }

    @JConnMethod("ADDREFUNDREASON")
    public void addRefundReason(@JConnParameter("REASON") RefundReason reason) throws IOException, SQLException {
        dc.addRefundReason(reason);
    }

    @JConnMethod("REMOVEREFUNDREASON")
    public void removeRefundReason(@JConnParameter("REASON") RefundReason reason) throws IOException, SQLException, JTillException {
        dc.removeRefundReason(reason);
    }

    @JConnMethod("UPDATEREFUNDREASON")
    public void updateRefundReason(@JConnParameter("REASON") RefundReason reason) throws IOException, SQLException, JTillException {
        dc.updateRefundReason(reason);
    }

    @JConnMethod("GETREFUNDREASON")
    public RefundReason getRefundReason(@JConnParameter("REASON") int id) throws IOException, SQLException, JTillException {
        return dc.getRefundReason(id);
    }

    @JConnMethod("GETUSEDREFUNDREASONS")
    public List<RefundReason> getUsedRefundReasons() throws IOException, SQLException {
        return dc.getUsedRefundReasons();
    }

    @JConnMethod("GETSTAFFMEMBERSALES")
    public BigDecimal getStaffMemberSales(@JConnParameter("START") Date start, @JConnParameter("END") Date end, @JConnParameter("STAFF") Staff s) throws SQLException {
        return dc.getStaffMemberSales(start, end, s);
    }

    @JConnMethod("GETSCREENSWITHPRODUCT")
    public List<Screen> getScreensWithProduct(@JConnParameter("PRODUCT") Product p) throws SQLException {
        return dc.getScreensWithProduct(p);
    }

    @JConnMethod("GETSUPPLIERSPRODUCTS")
    public List<Product> getSuppliersProducts(@JConnParameter("SUPPLIER") Supplier s) throws IOException, SQLException {
        return dc.getProductsInSupplier(s);
    }

    @JConnMethod("BATCHPRODUCTUPDATE")
    public void batchProductUpdate(@JConnParameter("products") List<Product> products) throws SQLException {
        dc.batchProductUpdate(products);
    }

    @JConnMethod("BATCHSTOCKRECEIVE")
    public void batchStockReceive(@JConnParameter("updates") HashMap<String, Integer> updates) throws SQLException {
        dc.batchStockReceive(updates);
    }

    @JConnMethod("departmentid")
    public boolean isDepartmentIdIsed(@JConnParameter("id") int id) throws SQLException {
        return dc.isDepartmentIDUsed(id);
    }

    @JConnMethod("categoryid")
    public boolean isCategoryIdUsed(@JConnParameter("id") int id) throws SQLException {
        return dc.isCategoryIDUsed(id);
    }

    @JConnMethod("taxname")
    public boolean isTaxNameUsed(@JConnParameter("name") String name) throws SQLException {
        return dc.isTaxNameUsed(name);
    }
}
