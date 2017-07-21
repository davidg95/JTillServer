/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.JConnMethod;
import io.github.davidg95.JTill.jtill.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;

/**
 * Thread for handling incoming connections.
 *
 * @author David
 */
public class ConnectionThread extends Thread {

    private static final Logger LOG = Logger.getGlobal();

    private final DBConnect dc; //The main database connection.

    private ObjectInputStream obIn; //InputStream for receiving data.
    private ObjectOutputStream obOut; //OutputStream for sending data

    private final Socket socket; //The main socket

    private boolean conn_term = false;

    public Staff staff; //The staff member currently logged on.
    public Till till; //The till that is using this connection.

    private ConnectionData currentData; //The current data object that has been recevied

    private final Semaphore sem; //Semaphore for the output stream.

    /**
     * All the detected method which have the @JConnMethod annotation.
     */
    private static final LinkedList<Method> JCONNMETHODS;

    /**
     * Constructor for Connection thread.
     *
     * @param name the name of the thread.
     * @param s the socket used for this connection.
     */
    public ConnectionThread(String name, Socket s) {
        super(name);
        this.socket = s;
        this.dc = DBConnect.getInstance();
        sem = new Semaphore(1);
    }

    /**
     * Static initialiser which creates the list of methods and scans for
     * JConnMethod methods.
     */
    static {
        JCONNMETHODS = new LinkedList<>();
        scanClass();
    }

    /**
     * Scans this class and finds all method with the JConnMethod annotation.
     */
    private static void scanClass() {
        final Method[] methods = ConnectionThread.class.getDeclaredMethods(); //Get all the methods in this class
        for (Method m : methods) { //Loop through each method
            if (m.isAnnotationPresent(JConnMethod.class)) { //Check if the annotation is a JConnMethod annotation
                JCONNMETHODS.add(m);
            }
        }
    }

    /**
     * Main run method for the connection thread. This method initialises the
     * input and output streams and performs the client-server handshake. It
     * will check if the connection is allowed and block if it is not. It will
     * then enter a while loop where it will wait for data from the client. It
     * uses reflection to analyse the methods in this class and decides what
     * method to send the request to based on the annotation value and the flag.
     */
    @Override
    public void run() {
        try {
            obIn = new ObjectInputStream(socket.getInputStream());
            obOut = new ObjectOutputStream(socket.getOutputStream());
            obOut.flush();

            final ConnectionData firstCon = (ConnectionData) obIn.readObject();

            final String site = (String) firstCon.getData()[0];
            UUID uuid = null;
            if (firstCon.getData()[1] != null) {
                uuid = (UUID) firstCon.getData()[1];
            }

            till = dc.connectTill(site, uuid);

            if (till == null) {
                obOut.writeObject(ConnectionData.create("DISALLOW"));
                socket.close();
                ConnectionAcceptThread.removeConnection(this);
                return;
            } else {
                obOut.writeObject(ConnectionData.create("ALLOW", till));
            }

            LOG.log(Level.INFO, till.getName() + " has connected");

            while (!conn_term) {
                Object o;
                try {
                    o = obIn.readObject();

                    till.setLastContact(new Date());
                } catch (SocketException ex) { //If the users ends the connection suddenly, this catch clause will detect it on the readObject() method on the input stream.
                    LOG.log(Level.WARNING, "The connection to the terminal was shut down forcefully");
                    try {
                        LOG.log(Level.INFO, "Logging staff out");
                        dc.tillLogout(staff);
                    } catch (StaffNotFoundException ex1) {
                        LOG.log(Level.WARNING, null, ex1);
                    }
                    return;
                }
                try {
                    sem.acquire();
                } catch (InterruptedException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
                currentData = (ConnectionData) o;

                final ConnectionData data = currentData.clone(); //Take a clone of the ConnectionData object

                LOG.log(Level.INFO, "Received " + data.getFlag() + " from client", data.getFlag());

                for (Method m : JCONNMETHODS) { //Loop through every method in this class
                    final Annotation a = m.getAnnotation(JConnMethod.class); //Get the JConnMethod annotation
                    if (a.annotationType() == JConnMethod.class) { //Check if it as the JConnMethod annotation
                        final JConnMethod ja = (JConnMethod) a; //Get the JConnMethod annotation object to find out the flag name
                        final String flag = data.getFlag(); //Get the flag from the connection object
                        if (ja.value().equals(flag)) { //Check if the current flag matches the flag definted on the annotation
                            try {
                                if (flag.equals("ADDRECEIVEDITEM")) {
                                    System.out.println("Cheese");
                                }
                                final ConnectionData clone = data.clone(); //Take a clone of the connection data object
                                m.setAccessible(true); //Set the access to public
                                final Runnable run = () -> {
                                    try {
                                        final Object ret = m.invoke(this, clone.getData()); //Invoke the method
                                        obOut.writeObject(ConnectionData.create("SUCC", ret)); //Return the result
                                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException ex) {
                                        try {
                                            obOut.writeObject(ConnectionData.create("FAIL", ex));
                                        } catch (IOException ex1) {
                                            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex1);
                                        }
                                    }
                                };
                                new Thread(run, flag).start(); //Run the thread which will invoke the method
                                break;
                            } catch (IllegalArgumentException ex) {
                                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
                sem.release();
            }
            LOG.log(Level.INFO, till.getName() + " has disconnected");
        } catch (CloneNotSupportedException | IOException | ClassNotFoundException ex) {
            if (till == null) {
                LOG.log(Level.SEVERE, "There was an error with the conenction to a client. Client information could not be retrieved. The connection will be forecfully terminated", ex);
            } else {
                LOG.log(Level.SEVERE, "There was an error with the conenction to " + till.getName() + ". The connection will be forecfully terminated", ex);
            }
        } catch (Exception ex) {
            System.out.println(ex);
        } finally {
            try {
                dc.disconnectTill(till); //Set the till to disconnected
                socket.close(); //Close the socket
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            ConnectionAcceptThread.removeConnection(this); //Remove the connection from the list
        }
    }

    @JConnMethod("NEWPRODUCT")
    public Product newProduct(Product p) throws IOException, SQLException {
        return dc.addProduct(p);
    }

    @JConnMethod("REMOVEPRODUCT")
    public void removeProduct(int code) throws ProductNotFoundException, IOException, SQLException {
        dc.removeProduct(code);
    }

    @JConnMethod("PURCHASE")
    public int purchase(Product p, int amount) throws IOException, ProductNotFoundException, OutOfStockException, SQLException {
        return dc.purchaseProduct(p.getId(), amount);
    }

    @JConnMethod("GETPRODUCT")
    public Product getProduct(int code) throws IOException, ProductNotFoundException, SQLException {
        return dc.getProduct(code);
    }

    @JConnMethod("UPDATEPRODUCT")
    public Product updateProduct(Product p) throws IOException, ProductNotFoundException, SQLException {
        return dc.updateProduct(p);
    }

    @JConnMethod("GETPRODUCTBARCODE")
    public Product getProductByBarcode(String barcode) throws IOException, ProductNotFoundException, SQLException {
        return dc.getProductByBarcode(barcode);
    }

    @JConnMethod("CHECKBARCODE")
    public boolean checkBarcode(String barcode) throws IOException, SQLException {
        return dc.checkBarcode(barcode);
    }

    @JConnMethod("GETALLPRODUCTS")
    public List<Product> getAllProducts() throws IOException, SQLException {
        return dc.getAllProducts();
    }

    @JConnMethod("PRODUCTLOOKUP")
    public List<Product> productLookup(String terms) throws IOException, SQLException {
        return dc.productLookup(terms);
    }

    @JConnMethod("NEWCUSTOMER")
    public Customer newCustomer(Customer c) throws IOException, SQLException {
        return dc.addCustomer(c);
    }

    @JConnMethod("REMOVECUSTOMER")
    public void removeCustomer(int id) throws IOException, CustomerNotFoundException, SQLException {
        dc.removeCustomer(id);
    }

    @JConnMethod("GETCUSTOMER")
    public Customer getCustomer(int id) throws IOException, CustomerNotFoundException, SQLException {
        return dc.getCustomer(id);
    }

    @JConnMethod("GETCUSTOMERBYNAME")
    public List<Customer> getCustomerByName(String name) throws IOException, CustomerNotFoundException, SQLException {
        return dc.getCustomerByName(name);
    }

    @JConnMethod("UPDATECUSTOMER")
    public Customer updateCustomer(Customer c) throws IOException, CustomerNotFoundException, SQLException {
        return dc.updateCustomer(c);
    }

    @JConnMethod("GETALLCUSTOMERS")
    public List<Customer> getAllCustomers() throws IOException, SQLException {
        return dc.getAllCustomers();
    }

    @JConnMethod("CUSTOMERLOOKUP")
    public List<Customer> customerLookup(String terms) throws IOException, SQLException {
        return dc.customerLookup(terms);
    }

    @JConnMethod("ADDSTAFF")
    public Staff addStaff(Staff s) throws IOException, SQLException {
        s.setPassword(Encryptor.decrypt(s.getPassword()));
        s = dc.addStaff(s);
        s.setPassword(Encryptor.encrypt(s.getPassword()));
        return s;
    }

    @JConnMethod("REMOVESTAFF")
    public void removeStaff(int id) throws IOException, StaffNotFoundException, SQLException {
        dc.removeStaff(id);
    }

    @JConnMethod("GETSTAFF")
    public Staff getStaff(int id) throws IOException, StaffNotFoundException, SQLException {
        final Staff s = dc.getStaff(id);
        s.setPassword(Encryptor.encrypt(s.getPassword()));
        return s;
    }

    @JConnMethod("UPDATESTAFF")
    public Staff updateStaff(Staff s) throws IOException, StaffNotFoundException, SQLException {
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
    public Sale addSale(Sale s) throws IOException, SQLException {
        return dc.addSale(s);
    }

    @JConnMethod("GETALLSALES")
    public List<Sale> getAllSales() throws IOException, SQLException {
        return dc.getAllSales();
    }

    @JConnMethod("GETSALE")
    public Sale getSale(int id) throws IOException, SQLException, JTillException {
        return dc.getSale(id);
    }

    @JConnMethod("UPDATESALE")
    public Sale updateSale(Sale sale) throws IOException, SQLException, JTillException {
        return dc.updateSale(sale);
    }

    @JConnMethod("GETSALEDATERANGE")
    public List<Sale> getSaleDateRange(Time start, Time end) throws IOException, SQLException {
        return dc.getSalesInRange(start, end);
    }

    @JConnMethod("SUSPENDSALE")
    public void suspendSale(Sale sale, Staff s) throws IOException {
        dc.suspendSale(sale, s);
    }

    @JConnMethod("RESUMESALE")
    public Sale resumeSale(Staff s) throws IOException {
        return dc.resumeSale(s);
    }

    @JConnMethod("LOGIN")
    public Staff login(String username, String password) throws IOException, LoginException, SQLException {
        password = Encryptor.decrypt(password);
        final Staff s = dc.login(username, password);
        ConnectionThread.this.staff = s;
        LOG.log(Level.INFO, s.getName() + " has logged in");
        s.setPassword(Encryptor.encrypt(s.getPassword()));
        return s;
    }

    @JConnMethod("TILLLOGIN")
    public Staff tillLogin(int id) throws IOException, LoginException, SQLException {
        final Staff s = dc.tillLogin(id);
        ConnectionThread.this.staff = s;
        LOG.log(Level.INFO, staff.getName() + " has logged in from " + till.getName());
        s.setPassword(Encryptor.encrypt(s.getPassword()));
        return s;
    }

    @JConnMethod("LOGOUT")
    public void logout(Staff s) throws IOException, StaffNotFoundException {
        dc.logout(s);
        LOG.log(Level.INFO, staff.getName() + " has logged out");
        ConnectionThread.this.staff = null;
    }

    @JConnMethod("TILLLOGOUT")
    public void tillLogout(Staff s) throws IOException, StaffNotFoundException {
        dc.tillLogout(s);
        LOG.log(Level.INFO, staff.getName() + " has logged out");
        ConnectionThread.this.staff = null;
    }

    @JConnMethod("ADDCATEGORY")
    public Category addCategory(Category c) throws IOException, SQLException {
        return dc.addCategory(c);
    }

    @JConnMethod("UPDATECATEGORY")
    public Category updateCategory(Category c) throws IOException, SQLException, JTillException {
        return dc.updateCategory(c);
    }

    @JConnMethod("REMOVECATEGORY")
    public void removeCategory(int id) throws IOException, SQLException, JTillException {
        dc.removeCategory(id);
    }

    @JConnMethod("GETCATEGORY")
    public Category getCategory(int id) throws IOException, SQLException, JTillException {
        return dc.getCategory(id);
    }

    @JConnMethod("GETALLCATEGORYS")
    public List<Category> getAllCategorys() throws IOException, SQLException, JTillException {
        return dc.getAllCategorys();
    }

    @JConnMethod("GETPRODUCTSINCATEGORY")
    public List<Product> getProductsInCategory(int id) throws IOException, SQLException, JTillException {
        return dc.getProductsInCategory(id);
    }

    @JConnMethod("ADDDISCOUNT")
    public Discount addDiscount(Discount d) throws IOException, SQLException {
        return dc.addDiscount(d);
    }

    @JConnMethod("UPDATEDISCOUNT")
    public Discount updateDiscount(Discount d) throws IOException, SQLException, DiscountNotFoundException {
        return dc.updateDiscount(d);
    }

    @JConnMethod("REMOVEDISCOUNT")
    public void removeDiscount(int id) throws IOException, SQLException, DiscountNotFoundException {
        dc.removeDiscount(id);
    }

    @JConnMethod("GETDISCOUNT")
    public Discount getDiscount(int id) throws IOException, SQLException, DiscountNotFoundException {
        return dc.getDiscount(id);
    }

    @JConnMethod("GETALLDISCOUNTS")
    public List<Discount> getAllDiscounts() throws IOException, SQLException {
        return dc.getAllDiscounts();
    }

    @JConnMethod("ADDTAX")
    public Tax addTax(Tax t) throws IOException, SQLException {
        return dc.addTax(t);
    }

    @JConnMethod("REMOVETAX")
    public void removeTax(int id) throws IOException, SQLException, JTillException {
        dc.removeTax(id);
    }

    @JConnMethod("GETTAX")
    public Tax getTax(int id) throws IOException, SQLException, JTillException {
        return dc.getTax(id);
    }

    @JConnMethod("UPDATETAX")
    public Tax updateTax(Tax t) throws IOException, SQLException, JTillException {
        return dc.updateTax(t);
    }

    @JConnMethod("GETALLTAX")
    public List<Tax> getAllTax() throws IOException, SQLException, JTillException {
        return dc.getAllTax();
    }

    @JConnMethod("GETPRODUCTSINTAX")
    public List<Product> getProductsInTax(int id) throws IOException, SQLException, JTillException {
        return dc.getProductsInTax(id);
    }

    @JConnMethod("ADDSCREEN")
    public Screen addScreen(Screen s) throws IOException, SQLException, JTillException {
        return dc.addScreen(s);
    }

    @JConnMethod("ADDBUTTON")
    public TillButton addButton(TillButton b) throws IOException, SQLException, JTillException {
        return dc.addButton(b);
    }

    @JConnMethod("REMOVESCREEN")
    public void removeScreen(Screen s) throws IOException, SQLException, ScreenNotFoundException {
        dc.removeScreen(s);
    }

    @JConnMethod("REMOVEBUTTON")
    public void removeButton(TillButton b) throws IOException, SQLException, JTillException {
        dc.removeButton(b);
    }

    @JConnMethod("UPDATESCREEN")
    public Screen updateScreen(Screen s) throws IOException, SQLException, ScreenNotFoundException {
        return dc.updateScreen(s);
    }

    @JConnMethod("UPDATEBUTTON")
    public TillButton updateButton(TillButton b) throws IOException, SQLException, JTillException {
        return dc.updateButton(b);
    }

    @JConnMethod("GETSCREEN")
    public Screen getScreen(int id) throws IOException, SQLException, ScreenNotFoundException {
        return dc.getScreen(id);
    }

    @JConnMethod("GETBUTTON")
    public TillButton getButton(int id) throws IOException, SQLException, JTillException {
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
    public List<TillButton> getButtonsOnScreen(Screen s) throws IOException, SQLException, ScreenNotFoundException {
        return dc.getButtonsOnScreen(s);
    }

    @JConnMethod("ASSISSTANCE")
    public void assisstance(String message) throws IOException {
        dc.assisstance(staff.getName() + " on terminal " + till.getName() + " has requested assistance with message:\n" + message);
    }

    @JConnMethod("GETTAKINGS")
    public BigDecimal getTakings(int terminal) throws IOException, SQLException {
        return dc.getTillTakings(terminal);
    }

    @JConnMethod("GETUNCASHEDSALES")
    public List<Sale> getUncashedSales(String terminal) throws IOException, SQLException {
        return dc.getUncashedSales(terminal);
    }

    @JConnMethod("SENDEMAIL")
    public void sendEmail(String message) throws IOException, SQLException {
        dc.sendEmail(message);
    }

    @JConnMethod("SENDRECEIPT")
    public void sendReceipt(String email, Sale sale) throws IOException, MessagingException {
        dc.emailReceipt(email, sale);
    }

    @JConnMethod("ADDTILL")
    public Till addTill(Till t) throws IOException, SQLException {
        return dc.addTill(t);
    }

    @JConnMethod("REMOVETILL")
    public void removeTill(int id) throws IOException, SQLException, JTillException {
        dc.removeTill(id);
    }

    @JConnMethod("GETTILL")
    public Till getTill(int id) throws IOException, SQLException, JTillException {
        return dc.getTill(id);
    }

    @JConnMethod("GETALLTILLS")
    public List<Till> getAllTills() throws IOException, SQLException {
        return dc.getAllTills();
    }

    @JConnMethod("CONNECTTILL")
    public Till connectTill(String name, UUID uuid) throws IOException, SQLException {
        return dc.connectTill(name, uuid);
    }

    @JConnMethod("DISCONNECTTILL")
    public void disconnectTill(Till t
    ) {
        dc.disconnectTill(t);
    }

    @JConnMethod("GETALLCONNECTEDTILLS")
    public List<Till> getAllConnectedTills() throws IOException {
        return dc.getConnectedTills();
    }

    @JConnMethod("SETSETTING")
    public void setSetting(String key, String value) throws IOException {
        dc.setSetting(key, value);
    }

    @JConnMethod("GETSETTING")
    public String getSetting(String key) throws IOException {
        return dc.getSetting(key);
    }

    @JConnMethod("GETSETTINGDEFAULT")
    public String getSettingDefault(String key, String def_value) throws IOException {
        return dc.getSetting(key, def_value);
    }

    @Deprecated
    public Settings getSettingsInstance() throws IOException {
        return dc.getSettingsInstance();
    }

    @JConnMethod("ADDPLU")
    public Plu addPlu(Plu p) throws IOException, SQLException {
        return dc.addPlu(p);
    }

    @JConnMethod("REMOVEPLU")
    public void removePlu(Plu p) throws IOException, SQLException, JTillException {
        dc.removePlu(p);
    }

    @JConnMethod("GETPLU")
    public Plu getPlu(int id) throws IOException, SQLException, JTillException {
        return dc.getPlu(id);
    }

    @JConnMethod("GETPLUBYCODE")
    public Plu getPluByCode(String code) throws IOException, SQLException, JTillException {
        return dc.getPluByCode(code);
    }

    @JConnMethod("GETALLPLUS")
    public List<Plu> getAllPlus() throws IOException, SQLException {
        return dc.getAllPlus();
    }

    @JConnMethod("UPDATEPLU")
    public Plu updatePlu(Plu plu) throws IOException, SQLException, JTillException {
        return dc.updatePlu(plu);
    }

    @JConnMethod("ISTILLLOGGEDIN")
    public boolean isTillLoggedIn(Staff s) throws IOException, StaffNotFoundException, SQLException {
        return dc.isTillLoggedIn(s);
    }

    @JConnMethod("CHECKUSER")
    public boolean checkUsername(String username) throws IOException, SQLException, JTillException {
        return dc.checkUsername(username);
    }

    @JConnMethod("ADDWASTEREPORT")
    public WasteReport addWasteReport(WasteReport wr) throws IOException, SQLException, JTillException {
        return dc.addWasteReport(wr);
    }

    @JConnMethod("REMOVEWASTEREPORT")
    public void removeWasteReport(int id) throws IOException, SQLException, JTillException {
        dc.removeWasteReport(id);
    }

    @JConnMethod("GETWASTEREPORT")
    public WasteReport getWasteReport(int id) throws IOException, SQLException, JTillException {
        return dc.getWasteReport(id);
    }

    @JConnMethod("GETALLWASTEREPORTS")
    public List<WasteReport> getAllWasteReports() throws IOException, SQLException, JTillException {
        return dc.getAllWasteReports();
    }

    @JConnMethod("UPDATEWASTEREPORT")
    public WasteReport updateWasteReport(WasteReport wr) throws IOException, SQLException, JTillException {
        return dc.updateWasteReport(wr);
    }

    @JConnMethod("ADDWASTEITEM")
    public WasteItem addWasteItem(WasteReport wr, WasteItem wi) throws IOException, SQLException, JTillException {
        return dc.addWasteItem(wr, wi);
    }

    @JConnMethod("REMOVEWASTEITEM")
    public void removeWasteItem(int id) throws IOException, SQLException, JTillException {
        dc.removeWasteItem(id);
    }

    @JConnMethod("GETWASTEITEM")
    public WasteItem getWasteItem(int id) throws IOException, SQLException, JTillException {
        return dc.getWasteItem(id);
    }

    @JConnMethod("GETALLWASTEITEMS")
    public List<WasteItem> getAllWasteItems() throws IOException, SQLException, JTillException {
        return dc.getAllWasteItems();
    }

    @JConnMethod("UPDATEWASTEITEM")
    public WasteItem updateWasteItem(WasteItem wi) throws IOException, SQLException, JTillException {
        return dc.updateWasteItem(wi);
    }

    @JConnMethod("ADDWASTEREASON")
    public WasteReason addWasteReason(WasteReason wr) throws IOException, SQLException, JTillException {
        return dc.addWasteReason(wr);
    }

    @JConnMethod("REMOVEWASTEREASON")
    public void removeWasteReason(int id) throws IOException, SQLException, JTillException {
        dc.removeWasteReason(id);
    }

    @JConnMethod("GETWASTEREASON")
    public WasteReason getWasteReason(int id) throws IOException, SQLException, JTillException {
        return dc.getWasteReason(id);
    }

    @JConnMethod("GETALLWASTEREASONS")
    public List<WasteReason> getAllWasteReasons() throws IOException, SQLException, JTillException {
        return dc.getAllWasteReasons();
    }

    @JConnMethod("UPDATEWASTEREASON")
    public WasteReason updateWasteReason(WasteReason wr) throws IOException, SQLException, JTillException {
        return dc.updateWasteReason(wr);
    }

    @JConnMethod("ADDSUPPLIER")
    public Supplier addSupplier(Supplier s) throws IOException, SQLException, JTillException {
        return dc.addSupplier(s);
    }

    @JConnMethod("REMOVESUPPLIER")
    public void removeSupplier(int id) throws IOException, SQLException, JTillException {
        dc.removeSupplier(id);
    }

    @JConnMethod("GETSUPPLIER")
    public Supplier getSupplier(int id) throws IOException, SQLException, JTillException {
        return dc.getSupplier(id);
    }

    @JConnMethod("GETALLSUPPLIERS")
    public List<Supplier> getAllSuppliers() throws IOException, SQLException {
        return dc.getAllSuppliers();
    }

    @JConnMethod("UPDATESUPPLIER")
    public Supplier updateSupplier(Supplier s) throws IOException, SQLException, JTillException {
        return dc.updateSupplier(s);
    }

    @JConnMethod("ADDDEPARTMENT")
    public Department addDepartment(Department d) throws IOException, SQLException, JTillException {
        return dc.addDepartment(d);
    }

    @JConnMethod("REMOVEDEPARTMENT")
    public void removeDepartment(int id) throws IOException, SQLException, JTillException {
        dc.removeDepartment(id);
    }

    @JConnMethod("GETDEPARTMENT")
    public Department getDepartment(int id) throws IOException, SQLException, JTillException {
        return dc.getDepartment(id);
    }

    @JConnMethod("GETALLDEPARTMENTS")
    public List<Department> getAllDepartments() throws IOException, SQLException {
        return dc.getAllDepartments();
    }

    @JConnMethod("UPDATEDEPARTMENT")
    public Department updateDepartment(Department d) throws IOException, SQLException, JTillException {
        return dc.updateDepartment(d);
    }

    @JConnMethod("ADDSALEITEM")
    public SaleItem addSaleItem(Sale s, SaleItem i) throws IOException, SQLException, JTillException {
        return dc.addSaleItem(s, i);
    }

    @JConnMethod("REMOVESALEITEM")
    public void removeSaleItem(int id) throws IOException, SQLException, JTillException {
        dc.removeSaleItem(id);
    }

    @JConnMethod("GETSALEITEM")
    public SaleItem getSaleItem(int id) throws IOException, SQLException, JTillException {
        return dc.getSaleItem(id);
    }

    @JConnMethod("GETALLSALEITEMS")
    public List<SaleItem> getAllSaleItems() throws IOException, SQLException {
        return dc.getAllSaleItems();
    }

    public List<SaleItem> subSaleItemQuery(String q) throws IOException, SQLException {
        return dc.submitSaleItemQuery(q);
    }

    @JConnMethod("UPDATESALEITEM")
    public SaleItem updateSaleItem(SaleItem i) throws IOException, SQLException, JTillException {
        return dc.updateSaleItem(i);
    }

    @JConnMethod("GETTOTALSOLDITEM")
    public int getTotalSoldItem(int id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getTotalSoldOfItem(id);
    }

    @JConnMethod("GETVALUESOLDITEM")
    public BigDecimal getValueSoldItem(int id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getTotalValueSold(id);
    }

    @JConnMethod("GETTOTALWASTEDITEM")
    public int getTotalWastedItem(int id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getTotalWastedOfItem(id);
    }

    @JConnMethod("GETVALUEWASTEDITEM")
    public BigDecimal getValueWastedItem(int id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getValueWastedOfItem(id);
    }

    @JConnMethod("ADDRECEIVEDITEM")
    public void addReceivedItem(ReceivedItem i) throws IOException, SQLException {
        dc.addReceivedItem(i);
    }

    @JConnMethod("GETSPENTONITEM")
    public BigDecimal getValueSpentOnItem(int id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getValueSpentOnItem(id);
    }

    @JConnMethod("CLOCKON")
    public void clockOn(int id) throws IOException, SQLException, StaffNotFoundException {
        dc.clockOn(id);
    }

    @JConnMethod("CLOCKOFF")
    public void clockOff(int id) throws IOException, SQLException, StaffNotFoundException {
        dc.clockOff(id);
    }

    @JConnMethod("GETCLOCKS")
    public List<ClockItem> getAllClocks(int id) throws IOException, SQLException, StaffNotFoundException {
        return dc.getAllClocks(id);
    }

    @JConnMethod("CLEARCLOCKS")
    public void clearClocks(int id) throws IOException, SQLException, StaffNotFoundException {
        dc.clearClocks(id);
    }

    @JConnMethod("ADDTRIGGER")
    public Trigger addTrigger(Trigger t) throws IOException, SQLException {
        return dc.addTrigger(t);
    }

    @JConnMethod("GETDISCOUNTBUCKETS")
    public List<DiscountBucket> getDiscountBuckets(int id) throws IOException, SQLException, DiscountNotFoundException {
        return dc.getDiscountBuckets(id);
    }

    @JConnMethod("REMOVETRIGGER")
    public void removeTrigger(int id) throws IOException, SQLException, JTillException {
        dc.removeTrigger(id);
    }

    @JConnMethod("GETVALIDDISCOUNTS")
    public List<Discount> getValidDiscounts() throws IOException, SQLException, JTillException {
        return dc.getValidDiscounts();
    }

    @JConnMethod("ADDBUCKET")
    public DiscountBucket addBucket(DiscountBucket b) throws IOException, SQLException, JTillException {
        return dc.addBucket(b);
    }

    @JConnMethod("REMOVEBUCKET")
    public void removeBucket(int id) throws IOException, SQLException, JTillException {
        dc.removeBucket(id);
    }

    @JConnMethod("GETBUCKETTRIGGERS")
    public List<Trigger> getBucketTriggers(int id) throws IOException, SQLException, JTillException {
        return dc.getBucketTriggers(id);
    }

    @JConnMethod("UPDATETRIGGER")
    public Trigger updateTrigger(Trigger t) throws IOException, SQLException, JTillException {
        return dc.updateTrigger(t);
    }

    @JConnMethod("UPDATEBUCKET")
    public DiscountBucket updateBucket(DiscountBucket b) throws IOException, SQLException, JTillException {
        return dc.updateBucket(b);
    }

    @JConnMethod("GETUNCASHEDTERMINALSALES")
    public List<Sale> getUncashedTerminalSales(int id) throws IOException, SQLException, JTillException {
        return dc.getUncachedTillSales(id);
    }

    @JConnMethod("ADDPRODUCTANDPLU")
    public Product addProductAndPlu(Product p, Plu pl) throws IOException, SQLException, JTillException {
        return dc.addProductAndPlu(p, pl);
    }

    @JConnMethod("GETPLUBYPRODUCT")
    public Plu getPluByProduct(int id) throws IOException, SQLException, JTillException {
        return dc.getPluByProduct(id);
    }

    @JConnMethod("SEARCHSALEITEMS")
    public List<SaleItem> searchSaleItems(int department, int category, Date start, Date end) throws IOException, SQLException, JTillException {
        return dc.searchSaleItems(department, category, start, end);
    }

    @JConnMethod("CONNTERM")
    public void terminateConnection() throws IOException, SQLException, StaffNotFoundException {
        dc.logout(staff);
        dc.tillLogout(staff);
        conn_term = true;
    }

    @JConnMethod("GETTERMINALSALES")
    public List<Sale> getTerminalSales(int terminal, boolean uncashedOnly) throws IOException, SQLException, JTillException {
        return dc.getTerminalSales(terminal, uncashedOnly);
    }

    @JConnMethod("INTEGRITYCHECK")
    public HashMap integrityCheck() throws IOException, SQLException {
        return dc.integrityCheck();
    }

    @JConnMethod("CASHUNCASHEDSALES")
    public void cashUncashedSales(int t) throws IOException, SQLException {
        dc.cashUncashedSales(t);
    }

    @JConnMethod("GETPRODUCTSADVANCED")
    public List<Product> getProductsAdvanced(String WHERE) throws IOException, SQLException {
        return dc.getProductsAdvanced(WHERE);
    }

    @JConnMethod("GETSTAFFSALES")
    public List<Sale> getStaffSales(Staff s) throws IOException, SQLException, StaffNotFoundException {
        return dc.getStaffSales(s);
    }
}
