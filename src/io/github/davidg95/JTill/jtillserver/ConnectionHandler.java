/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.jconn.*;
import io.github.davidg95.JTill.jtill.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
        this.dc = DBConnect.getInstance();
    }

    @JConnMethod("UUID")
    public Till initialConnection(@JConnParameter("UUID") UUID uuid, @JConnParameter("SITE") String site) {
        till = dc.connectTill(site, uuid);
        return till;
    }

    @JConnMethod("NEWPRODUCT")
    public Product newProduct(@JConnParameter("PRODUCT") Product p) throws IOException, SQLException {
        return dc.addProduct(p);
    }

    @JConnMethod("REMOVEPRODUCT")
    public void removeProduct(@JConnParameter("CODE") int code) throws ProductNotFoundException, IOException, SQLException {
        dc.removeProduct(code);
    }

    @JConnMethod("PURCHASE")
    public int purchase(@JConnParameter("PRODUCT") int p, @JConnParameter("AMOUNT") int amount) throws IOException, ProductNotFoundException, OutOfStockException, SQLException {
        return dc.purchaseProduct(p, amount);
    }

    @JConnMethod("GETPRODUCT")
    public Product getProduct(@JConnParameter("CODE") int code) throws IOException, ProductNotFoundException, SQLException {
        return dc.getProduct(code);
    }

    @JConnMethod("UPDATEPRODUCT")
    public Product updateProduct(@JConnParameter("PRODUCT") Product p) throws IOException, ProductNotFoundException, SQLException {
        return dc.updateProduct(p);
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

    @JConnMethod("NEWCUSTOMER")
    public Customer newCustomer(@JConnParameter("CUSTOMER") Customer c) throws IOException, SQLException {
        return dc.addCustomer(c);
    }

    @JConnMethod("REMOVECUSTOMER")
    public void removeCustomer(@JConnParameter("ID") int id) throws IOException, CustomerNotFoundException, SQLException {
        dc.removeCustomer(id);
    }

    @JConnMethod("GETCUSTOMER")
    public Customer getCustomer(@JConnParameter("ID") int id) throws IOException, CustomerNotFoundException, SQLException {
        return dc.getCustomer(id);
    }

    @JConnMethod("GETCUSTOMERBYNAME")
    public List<Customer> getCustomerByName(@JConnParameter("NAME") String name) throws IOException, CustomerNotFoundException, SQLException {
        return dc.getCustomerByName(name);
    }

    @JConnMethod("UPDATECUSTOMER")
    public Customer updateCustomer(@JConnParameter("CUSTOMER") Customer c) throws IOException, CustomerNotFoundException, SQLException {
        return dc.updateCustomer(c);
    }

    @JConnMethod("GETALLCUSTOMERS")
    public List<Customer> getAllCustomers() throws IOException, SQLException {
        return dc.getAllCustomers();
    }

    @JConnMethod("CUSTOMERLOOKUP")
    public List<Customer> customerLookup(@JConnParameter("TERMS") String terms) throws IOException, SQLException {
        return dc.customerLookup(terms);
    }

    @JConnMethod("ADDSTAFF")
    public Staff addStaff(@JConnParameter("STAFF") Staff s) throws IOException, SQLException {
        s.setPassword(Encryptor.decrypt(s.getPassword()));
        s = dc.addStaff(s);
        s.setPassword(Encryptor.encrypt(s.getPassword()));
        return s;
    }

    @JConnMethod("REMOVESTAFF")
    public void removeStaff(@JConnParameter("ID") int id) throws IOException, StaffNotFoundException, SQLException {
        dc.removeStaff(id);
    }

    @JConnMethod("GETSTAFF")
    public Staff getStaff(@JConnParameter("ID") int id) throws IOException, StaffNotFoundException, SQLException {
        final Staff s = dc.getStaff(id);
        s.setPassword(Encryptor.encrypt(s.getPassword()));
        return s;
    }

    @JConnMethod("UPDATESTAFF")
    public Staff updateStaff(@JConnParameter("STAFF") Staff s) throws IOException, StaffNotFoundException, SQLException {
        s.setPassword(Encryptor.decrypt(s.getPassword()));
        s = dc.updateStaff(s);
        s.setPassword(Encryptor.encrypt(s.getPassword()));
        return s;
    }

    @JConnMethod("GETALLSTAFF")
    public List<Staff> getAllStaff() throws IOException, SQLException, SQLException {
        final List<Staff> staffList = dc.getAllStaff();
        staffList.forEach((s) -> {
            s.setPassword(Encryptor.encrypt(s.getPassword()));
        });
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
    public List<Sale> getSaleDateRange(@JConnParameter("START") Time start, @JConnParameter("END") Time end) throws IOException, SQLException {
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
        s.setPassword(Encryptor.encrypt(s.getPassword()));
        return s;
    }

    @JConnMethod("TILLLOGIN")
    public Staff tillLogin(@JConnParameter("ID") int id) throws IOException, LoginException, SQLException {
        final Staff s = dc.tillLogin(id);
        ConnectionHandler.this.staff = s;
        LOG.log(Level.INFO, staff.getName() + " has logged in from " + till.getName());
        s.setPassword(Encryptor.encrypt(s.getPassword()));
        return s;
    }

    @JConnMethod("LOGOUT")
    public void logout(@JConnParameter("STAFF") Staff s) throws IOException, StaffNotFoundException {
        dc.logout(s);
        LOG.log(Level.INFO, staff.getName() + " has logged out");
        ConnectionHandler.this.staff = null;
    }

    @JConnMethod("TILLLOGOUT")
    public void tillLogout(@JConnParameter("STAFF") Staff s) throws IOException, StaffNotFoundException {
        dc.tillLogout(s);
        LOG.log(Level.INFO, staff.getName() + " has logged out");
        ConnectionHandler.this.staff = null;
    }

    @JConnMethod("ADDCATEGORY")
    public Category addCategory(@JConnParameter("CATEGORY") Category c) throws IOException, SQLException {
        return dc.addCategory(c);
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
    public Tax addTax(@JConnParameter("TAX") Tax t) throws IOException, SQLException {
        return dc.addTax(t);
    }

    @JConnMethod("REMOVETAX")
    public void removeTax(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        dc.removeTax(id);
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

    @JConnMethod("GETTAKINGS")
    public BigDecimal getTakings(@JConnParameter("TERMINAL") int terminal) throws IOException, SQLException {
        return dc.getTillTakings(terminal);
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
    public Till getTill(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getTill(id);
    }

    @JConnMethod("GETALLTILLS")
    public List<Till> getAllTills() throws IOException, SQLException {
        return dc.getAllTills();
    }

    @JConnMethod("CONNECTTILL")
    public Till connectTill(@JConnParameter("NAME") String name, @JConnParameter("UUID") UUID uuid) throws IOException, SQLException {
        return dc.connectTill(name, uuid);
    }

    @JConnMethod("GETCONNECTEDTILLS")
    public List<Till> getAllConnectedTills() throws IOException {
        return dc.getConnectedTills();
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

    @JConnMethod("ADDPLU")
    public Plu addPlu(@JConnParameter("PLU") Plu p) throws IOException, SQLException {
        return dc.addPlu(p);
    }

    @JConnMethod("REMOVEPLU")
    public void removePlu(@JConnParameter("PLU") Plu p) throws IOException, SQLException, JTillException {
        dc.removePlu(p);
    }

    @JConnMethod("GETPLU")
    public Plu getPlu(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getPlu(id);
    }

    @JConnMethod("GETPLUBYCODE")
    public Plu getPluByCode(@JConnParameter("CODE") String code) throws IOException, SQLException, JTillException {
        return dc.getPluByCode(code);
    }

    @JConnMethod("GETALLPLUS")
    public List<Plu> getAllPlus() throws IOException, SQLException {
        return dc.getAllPlus();
    }

    @JConnMethod("UPDATEPLU")
    public Plu updatePlu(@JConnParameter("PLU") Plu plu) throws IOException, SQLException, JTillException {
        return dc.updatePlu(plu);
    }

    @JConnMethod("ISTILLLOGGEDIN")
    public boolean isTillLoggedIn(@JConnParameter("STAFF") Staff s) throws IOException, StaffNotFoundException, SQLException {
        return dc.isTillLoggedIn(s);
    }

    @JConnMethod("CHECKUSER")
    public boolean checkUsername(@JConnParameter("USERNAME") String username) throws IOException, SQLException, JTillException {
        return dc.checkUsername(username);
    }

    @JConnMethod("ADDWASTEREPORT")
    public WasteReport addWasteReport(@JConnParameter("WASTE") WasteReport wr) throws IOException, SQLException, JTillException {
        return dc.addWasteReport(wr);
    }

    @JConnMethod("REMOVEWASTEREPORT")
    public void removeWasteReport(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        dc.removeWasteReport(id);
    }

    @JConnMethod("GETWASTEREPORT")
    public WasteReport getWasteReport(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getWasteReport(id);
    }

    @JConnMethod("GETALLWASTEREPORTS")
    public List<WasteReport> getAllWasteReports() throws IOException, SQLException, JTillException {
        return dc.getAllWasteReports();
    }

    @JConnMethod("UPDATEWASTEREPORT")
    public WasteReport updateWasteReport(@JConnParameter("WASTE") WasteReport wr) throws IOException, SQLException, JTillException {
        return dc.updateWasteReport(wr);
    }

    @JConnMethod("ADDWASTEITEM")
    public WasteItem addWasteItem(@JConnParameter("WASTE") WasteReport wr, @JConnParameter("ITEM") WasteItem wi) throws IOException, SQLException, JTillException {
        return dc.addWasteItem(wr, wi);
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

    @JConnMethod("UPDATEWASTEITEM")
    public WasteItem updateWasteItem(@JConnParameter("WASTE") WasteItem wi) throws IOException, SQLException, JTillException {
        return dc.updateWasteItem(wi);
    }

    @JConnMethod("ADDWASTEREASON")
    public WasteReason addWasteReason(@JConnParameter("WASTE") WasteReason wr) throws IOException, SQLException, JTillException {
        return dc.addWasteReason(wr);
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
    public Supplier addSupplier(@JConnParameter("SUPPLIER") Supplier s) throws IOException, SQLException, JTillException {
        return dc.addSupplier(s);
    }

    @JConnMethod("REMOVESUPPLIER")
    public void removeSupplier(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        dc.removeSupplier(id);
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
    public Department addDepartment(@JConnParameter("DEPARTMENT") Department d) throws IOException, SQLException, JTillException {
        return dc.addDepartment(d);
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

    @JConnMethod("REMOVESALEITEM")
    public void removeSaleItem(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        dc.removeSaleItem(id);
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

    @JConnMethod("UPDATESALEITEM")
    public SaleItem updateSaleItem(@JConnParameter("ITEM") SaleItem i) throws IOException, SQLException, JTillException {
        return dc.updateSaleItem(i);
    }

    @JConnMethod("GETTOTALSOLDITEM")
    public int getTotalSoldItem(@JConnParameter("ID") int id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getTotalSoldOfItem(id);
    }

    @JConnMethod("GETVALUESOLDITEM")
    public BigDecimal getValueSoldItem(@JConnParameter("ID") int id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getTotalValueSold(id);
    }

    @JConnMethod("GETTOTALWASTEDITEM")
    public int getTotalWastedItem(@JConnParameter("ID") int id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getTotalWastedOfItem(id);
    }

    @JConnMethod("GETVALUEWASTEDITEM")
    public BigDecimal getValueWastedItem(@JConnParameter("ID") int id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getValueWastedOfItem(id);
    }

    @JConnMethod("ADDRECEIVEDITEM")
    public void addReceivedItem(@JConnParameter("ITEM") ReceivedItem i) throws IOException, SQLException {
        dc.addReceivedItem(i);
    }

    @JConnMethod("GETSPENTONITEM")
    public BigDecimal getValueSpentOnItem(@JConnParameter("ID") int id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getValueSpentOnItem(id);
    }

    @JConnMethod("CLOCKON")
    public void clockOn(@JConnParameter("ID") int id) throws IOException, SQLException, StaffNotFoundException {
        dc.clockOn(id);
    }

    @JConnMethod("CLOCKOFF")
    public void clockOff(@JConnParameter("ID") int id) throws IOException, SQLException, StaffNotFoundException {
        dc.clockOff(id);
    }

    @JConnMethod("GETCLOCKS")
    public List<ClockItem> getAllClocks(@JConnParameter("ID") int id) throws IOException, SQLException, StaffNotFoundException {
        return dc.getAllClocks(id);
    }

    @JConnMethod("CLEARCLOCKS")
    public void clearClocks(@JConnParameter("ID") int id) throws IOException, SQLException, StaffNotFoundException {
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

    @JConnMethod("GETUNCASHEDTERMINALSALES")
    public List<Sale> getUncashedTerminalSales(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getUncachedTillSales(id);
    }

    @JConnMethod("ADDPRODUCTANDPLU")
    public Product addProductAndPlu(@JConnParameter("PRODUCT") Product p, @JConnParameter("PLU") Plu pl) throws IOException, SQLException, JTillException {
        return dc.addProductAndPlu(p, pl);
    }

    @JConnMethod("GETPLUBYPRODUCT")
    public Plu getPluByProduct(@JConnParameter("ID") int id) throws IOException, SQLException, JTillException {
        return dc.getPluByProduct(id);
    }

    @JConnMethod("SEARCHSALEITEMS")
    public List<SaleItem> searchSaleItems(@JConnParameter("DEP") int department, @JConnParameter("CAT") int category, @JConnParameter("START") Date start, @JConnParameter("END") Date end) throws IOException, SQLException, JTillException {
        return dc.searchSaleItems(department, category, start, end);
    }

    @JConnMethod("CONNTERM")
    public void terminateConnection() throws IOException, SQLException, StaffNotFoundException {
        dc.logout(staff);
        dc.tillLogout(staff);
    }

    @JConnMethod("GETTERMINALSALES")
    public List<Sale> getTerminalSales(@JConnParameter("TERMINAL") int terminal, @JConnParameter("UNCASHEDFLAG") boolean uncashedOnly) throws IOException, SQLException, JTillException {
        return dc.getTerminalSales(terminal, uncashedOnly);
    }

    @JConnMethod("INTEGRITYCHECK")
    public HashMap integrityCheck() throws IOException, SQLException {
        return dc.integrityCheck();
    }

    @JConnMethod("CASHUNCASHEDSALES")
    public void cashUncashedSales(@JConnParameter("T") int t) throws IOException, SQLException {
        dc.cashUncashedSales(t);
    }

    @JConnMethod("GETPRODUCTSADVANCED")
    public List<Product> getProductsAdvanced(@JConnParameter("WHERE") String WHERE) throws IOException, SQLException {
        return dc.getProductsAdvanced(WHERE);
    }

    @JConnMethod("GETSTAFFSALES")
    public List<Sale> getStaffSales(@JConnParameter("STAFF") Staff s) throws IOException, SQLException, StaffNotFoundException {
        return dc.getStaffSales(s);
    }
}
