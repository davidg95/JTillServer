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
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;
import java.util.HashMap;
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

    private final DataConnect dc; //The main database connection.

    private ObjectInputStream obIn; //InputStream for receiving data.
    private ObjectOutputStream obOut; //OutputStream for sending data

    private final Socket socket; //The main socket

    private boolean conn_term = false;

    public Staff staff; //The staff member currently logged on.
    public Till till; //The till that is using this connection.

    private ConnectionData currentData;

    private final Semaphore sem; //Semaphore for the output stream.

    /**
     * Constructor for Connection thread.
     *
     * @param name the name of the thread.
     * @param dc the data connection.
     * @param s the socket used for this connection.
     */
    public ConnectionThread(String name, DataConnect dc, Socket s) {
        super(name);
        this.socket = s;
        this.dc = dc;
        sem = new Semaphore(1);
    }

    /**
     * Main run method for the connection thread. This method initialises the
     * input and output streams and performs the client-server handshake. It
     * will check if the connection is allowed and block if it is not. It will
     * then enter a while loop where it will wait for data from the client. It
     * uses a switch statement to analyse the flag on the connection data object
     * and decide what the request is for. The switch statement then spawns a
     * new thread for dealing with the request, freeing up the main thread to
     * handle further requests.
     */
    @Override
    public void run() {
        try {
            obIn = new ObjectInputStream(socket.getInputStream());
            obOut = new ObjectOutputStream(socket.getOutputStream());
            obOut.flush();

            final ConnectionData firstCon = (ConnectionData) obIn.readObject();

            final String site = (String) firstCon.getData();
            UUID uuid = null;
            if (firstCon.getData2() != null) {
                uuid = (UUID) firstCon.getData2();
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
                String input;

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
                input = currentData.getFlag();

                final String inp[] = input.split(",");
                final ConnectionData data = currentData.clone();

                LOG.log(Level.INFO, "Received " + data.getFlag() + " from client", data.getFlag());

                final Method[] methods = ConnectionThread.class.getDeclaredMethods();

                for (Method m : methods) { //Loop through every method in this class
                    final Annotation[] annos = m.getAnnotations(); //Get all the annotations
                    for (Annotation a : annos) { //Loop througha ll the annotations on that method
                        if (a.annotationType() == JConnMethod.class) { //Check if it as the JConnMethod annotation
                            final JConnMethod ja = (JConnMethod) a; //Get the JConnMethod annotation object to find out the flag name
                            final String flag = inp[0]; //Get the flag from the connection object
                            if(flag.equals("GETPLUBYPRODUCT") || flag.equals("GETALLPRODUCTS")){
                                System.out.println("Cheese");
                            }
                            if (ja.value().equals(flag)) { //Check if the current flag matches the flag definted on the annotation
                                try {
                                    Runnable run; //Runnable which will invoke the method
                                    final ConnectionData clone = data.clone(); //Take a clone of the connection data object
                                    m.setAccessible(true); //Set the access to public
                                    if (m.getParameterCount() == 0) { //Check if it has any parameters
                                        run = () -> {
                                            try {
                                                final Object ret = m.invoke(this); //Invoke the method
                                                obOut.writeObject(ConnectionData.create("SUCC", ret)); //Return the result
                                            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException ex) {
                                                try {
                                                    obOut.writeObject(ConnectionData.create("FAIL", ex));
                                                } catch (IOException ex1) {
                                                    Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex1);
                                                }
                                            }
                                        };
                                    } else {
                                        final Parameter[] params = m.getParameters();
                                        if (params.length == 1) {
                                            run = () -> {
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
                                        } else {
                                            run = () -> {
                                                try {
                                                    final Object ret = m.invoke(this, clone.getData(), clone.getData2()); //Invoke the method
                                                    obOut.writeObject(ConnectionData.create("SUCC", ret)); //Return the result
                                                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException ex) {
                                                    try {
                                                        obOut.writeObject(ConnectionData.create("FAIL", ex));
                                                    } catch (IOException ex1) {
                                                        Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex1);
                                                    }
                                                }
                                            };
                                        }
                                    }
                                    final Thread thread = new Thread(run); //The thread which will run the runnable
                                    thread.start(); //Run the thread which will invoke the method
                                } catch (IllegalArgumentException ex) {
                                    Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }
                }
                sem.release();
            }
            LOG.log(Level.INFO, till.getName() + " has disconnected");
        } catch (IOException | ClassNotFoundException ex) {
            if (till == null) {
                LOG.log(Level.SEVERE, "There was an error with the conenction to a client. Client information could not be retrieved. The connection will be forecfully terminated", ex);
            } else {
                LOG.log(Level.SEVERE, "There was an error with the conenction to " + till.getName() + ". The connection will be forecfully terminated", ex);
            }
        } finally {
            try {
                dc.disconnectTill(till);
                socket.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            ConnectionAcceptThread.removeConnection(this);
        }
    }

    @JConnMethod("NEWPRODUCT")
    private Product newProduct(Product p) throws IOException, SQLException {
        return dc.addProduct(p);
    }

    @JConnMethod("REMOVEPRODUCT")
    private void removeProduct(int code) throws ProductNotFoundException, IOException, SQLException {
        dc.removeProduct(code);
    }

    @JConnMethod("PURCHASE")
    private int purchase(Product p, int amount) throws IOException, ProductNotFoundException, OutOfStockException, SQLException {
        return dc.purchaseProduct(p.getId(), amount);
    }

    @JConnMethod("GETPRODUCT")
    private Product getProduct(int code) throws IOException, ProductNotFoundException, SQLException {
        return dc.getProduct(code);
    }

    @JConnMethod("UPDATEPRODUCT")
    private Product updateProduct(Product p) throws IOException, ProductNotFoundException, SQLException {
        return dc.updateProduct(p);
    }

    @JConnMethod("GETPRODUCTBARCODE")
    private Product getProductByBarcode(String barcode) throws IOException, ProductNotFoundException, SQLException {
        return dc.getProductByBarcode(barcode);
    }

    @JConnMethod("CHECKBARCODE")
    private boolean checkBarcode(String barcode) throws IOException, SQLException {
        return dc.checkBarcode(barcode);
    }

    @JConnMethod("GETALLPRODUCTS")
    private List<Product> getAllProducts() throws IOException, SQLException {
        return dc.getAllProducts();
    }

    @JConnMethod("PRODUCTLOOKUP")
    private List<Product> productLookup(String terms) throws IOException, SQLException {
        return dc.productLookup(terms);
    }

    @JConnMethod("NEWCUSTOMER")
    private Customer newCustomer(Customer c) throws IOException, SQLException {
        return dc.addCustomer(c);
    }

    @JConnMethod("REMOVECUSTOMER")
    private void removeCustomer(int id) throws IOException, CustomerNotFoundException, SQLException {
        dc.removeCustomer(id);
    }

    @JConnMethod("GETCUSTOMER")
    private Customer getCustomer(int id) throws IOException, CustomerNotFoundException, SQLException {
        return dc.getCustomer(id);
    }

    @JConnMethod("GETCUSTOMERBYNAME")
    private List<Customer> getCustomerByName(String name) throws IOException, CustomerNotFoundException, SQLException {
        return dc.getCustomerByName(name);
    }

    @JConnMethod("UPDATECUSTOMER")
    private Customer updateCustomer(Customer c) throws IOException, CustomerNotFoundException, SQLException {
        return dc.updateCustomer(c);
    }

    @JConnMethod("GETALLCUSTOMERS")
    private List<Customer> getAllCustomers() throws IOException, SQLException {
        return dc.getAllCustomers();
    }

    @JConnMethod("CUSTOMERLOOKUP")
    private List<Customer> customerLookup(String terms) throws IOException, SQLException {
        return dc.customerLookup(terms);
    }

    @JConnMethod("ADDSTAFF")
    private Staff addStaff(Staff s) throws IOException, SQLException {
        s.setPassword(Encryptor.decrypt(s.getPassword()));
        Staff newS = dc.addStaff(s);
        s.setPassword(Encryptor.encrypt(s.getPassword()));
        return newS;
    }

    @JConnMethod("REMOVESTAFF")
    private void removeStaff(int id) throws IOException, StaffNotFoundException, SQLException {
        dc.removeStaff(id);
    }

    @JConnMethod("GETSTAFF")
    private Staff getStaff(int id) throws IOException, StaffNotFoundException, SQLException {
        Staff s = dc.getStaff(id);
        s.setPassword(Encryptor.encrypt(s.getPassword()));
        return s;
    }

    @JConnMethod("UPDATESTAFF")
    private Staff updateStaff(Staff s) throws IOException, StaffNotFoundException, SQLException {
        s.setPassword(Encryptor.decrypt(s.getPassword()));
        Staff updatedStaff = dc.updateStaff(s);
        s.setPassword(Encryptor.encrypt(s.getPassword()));
        return s;
    }

    @JConnMethod("GETALLSTAFF")
    private List<Staff> getAllStaff() throws IOException, SQLException, SQLException {
        List<Staff> staffList = dc.getAllStaff();
        staffList.forEach((s) -> {
            s.setPassword(Encryptor.encrypt(s.getPassword()));
        });
        return staffList;
    }

    @JConnMethod("STAFFCOUNT")
    private int staffCount() throws IOException, SQLException {
        return dc.getStaffCount();
    }

    @JConnMethod("ADDSALE")
    private Sale addSale(Sale s) throws IOException, SQLException {
        return dc.addSale(s);
    }

    @JConnMethod("GETALLSALES")
    private List<Sale> getAllSales() throws IOException, SQLException {
        return dc.getAllSales();
    }

    @JConnMethod("GETSALE")
    private Sale getSale(int id) throws IOException, SQLException, JTillException {
        return dc.getSale(id);
    }

    @JConnMethod("UPDATESALE")
    private Sale updateSale(Sale sale) throws IOException, SQLException, JTillException {
        return dc.updateSale(sale);
    }

    @JConnMethod("GETSALEDATERANGE")
    private List<Sale> getSaleDateRange(Time start, Time end) throws IOException, SQLException {
        return dc.getSalesInRange(start, end);
    }

    @JConnMethod("SUSPENDSALE")
    private void suspendSale(Sale sale, Staff s) throws IOException {
        dc.suspendSale(sale, s);
    }

    @JConnMethod("RESUMESALE")
    private Sale resumeSale(Staff s) throws IOException {
        return dc.resumeSale(s);
    }

    @JConnMethod("LOGIN")
    private Staff login(String username, String password) throws IOException, LoginException, SQLException {
        password = Encryptor.decrypt(password);
        final Staff s = dc.login(username, password);
        ConnectionThread.this.staff = s;
        LOG.log(Level.INFO, s.getName() + " has logged in");
        s.setPassword(Encryptor.encrypt(s.getPassword()));
        return s;
    }

    @JConnMethod("TILLLOGIN")
    private Staff tillLogin(int id) throws IOException, LoginException, SQLException {
        final Staff s = dc.tillLogin(id);
        ConnectionThread.this.staff = s;
        LOG.log(Level.INFO, staff.getName() + " has logged in from " + till.getName());
        s.setPassword(Encryptor.encrypt(s.getPassword()));
        return s;
    }

    @JConnMethod("LOGOUT")
    private void logout(Staff s) throws IOException, StaffNotFoundException {
        dc.logout(s);
        LOG.log(Level.INFO, staff.getName() + " has logged out");
        ConnectionThread.this.staff = null;
    }

    @JConnMethod("TILLLOGOUT")
    private void tillLogout(Staff s) throws IOException, StaffNotFoundException {
        dc.tillLogout(s);
        LOG.log(Level.INFO, staff.getName() + " has logged out");
        ConnectionThread.this.staff = null;
    }

    @JConnMethod("ADDCATEGORY")
    private Category addCategory(Category c) throws IOException, SQLException {
        return dc.addCategory(c);
    }

    @JConnMethod("UPDATECATEGORY")
    private Category updateCategory(Category c) throws IOException, SQLException, JTillException {
        return dc.updateCategory(c);
    }

    @JConnMethod("REMOVECATEGORY")
    private void removeCategory(int id) throws IOException, SQLException, JTillException {
        dc.removeCategory(id);
    }

    @JConnMethod("GETCATEGORY")
    private Category getCategory(int id) throws IOException, SQLException, JTillException {
        return dc.getCategory(id);
    }

    @JConnMethod("GETALLCATEGORYS")
    private List<Category> getAllCategorys() throws IOException, SQLException, JTillException {
        return dc.getAllCategorys();
    }

    @JConnMethod("GETPRODUCTSINCATEGORY")
    private List<Product> getProductsInCategory(int id) throws IOException, SQLException, JTillException {
        return dc.getProductsInCategory(id);
    }

    @JConnMethod("ADDDISCOUNT")
    private Discount addDiscount(Discount d) throws IOException, SQLException {
        return dc.addDiscount(d);
    }

    @JConnMethod("UPDATEDISCOUNT")
    private Discount updateDiscount(Discount d) throws IOException, SQLException, DiscountNotFoundException {
        return dc.updateDiscount(d);
    }

    @JConnMethod("REMOVEDISCOUNT")
    private void removeDiscount(int id) throws IOException, SQLException, DiscountNotFoundException {
        dc.removeDiscount(id);
    }

    @JConnMethod("GETDISCOUNT")
    private Discount getDiscount(int id) throws IOException, SQLException, DiscountNotFoundException {
        return dc.getDiscount(id);
    }

    @JConnMethod("GETALLDISCOUNTS")
    private List<Discount> getAllDiscounts() throws IOException, SQLException {
        return dc.getAllDiscounts();
    }

    @JConnMethod("ADDTAX")
    private Tax addTax(Tax t) throws IOException, SQLException {
        return dc.addTax(t);
    }

    @JConnMethod("REMOVETAX")
    private void removeTax(int id) throws IOException, SQLException, JTillException {
        dc.removeTax(id);
    }

    @JConnMethod("GETTAX")
    private Tax getTax(int id) throws IOException, SQLException, JTillException {
        return dc.getTax(id);
    }

    @JConnMethod("UPDATETAX")
    private Tax updateTax(Tax t) throws IOException, SQLException, JTillException {
        return dc.updateTax(t);
    }

    @JConnMethod("GETALLTAX")
    private List<Tax> getAllTax() throws IOException, SQLException, JTillException {
        return dc.getAllTax();
    }

    @JConnMethod("GETPRODUCTSINTAX")
    private List<Product> getProductsInTax(int id) throws IOException, SQLException, JTillException {
        return dc.getProductsInTax(id);
    }

    @JConnMethod("ADDSCREEN")
    private Screen addScreen(Screen s) throws IOException, SQLException, JTillException {
        return dc.addScreen(s);
    }

    @JConnMethod("ADDBUTTON")
    private TillButton addButton(TillButton b) throws IOException, SQLException, JTillException {
        return dc.addButton(b);
    }

    @JConnMethod("REMOVESCREEN")
    private void removeScreen(Screen s) throws IOException, SQLException, ScreenNotFoundException {
        dc.removeScreen(s);
    }

    @JConnMethod("REMOVEBUTTON")
    private void removeButton(TillButton b) throws IOException, SQLException, JTillException {
        dc.removeButton(b);
    }

    @JConnMethod("UPDATESCREEN")
    private Screen updateScreen(Screen s) throws IOException, SQLException, ScreenNotFoundException {
        return dc.updateScreen(s);
    }

    @JConnMethod("UPDATEBUTTON")
    private TillButton updateButton(TillButton b) throws IOException, SQLException, JTillException {
        return dc.updateButton(b);
    }

    @JConnMethod("GETSCREEN")
    private Screen getScreen(int id) throws IOException, SQLException, ScreenNotFoundException {
        return dc.getScreen(id);
    }

    @JConnMethod("GETBUTTON")
    private TillButton getButton(int id) throws IOException, SQLException, JTillException {
        return dc.getButton(id);
    }

    @JConnMethod("GETALLSCREENS")
    private List<Screen> getAllScreens() throws IOException, SQLException {
        return dc.getAllScreens();
    }

    @JConnMethod("GETALLBUTTONS")
    private List<TillButton> getAllButtons() throws IOException, SQLException {
        return dc.getAllButtons();
    }

    @JConnMethod("GETBUTTONSONSCREEN")
    private List<TillButton> getButtonsOnScreen(Screen s) throws IOException, SQLException, ScreenNotFoundException {
        return dc.getButtonsOnScreen(s);
    }

    @JConnMethod("ASSISSTANCE")
    private void assisstance(String message) throws IOException {
        dc.assisstance(staff.getName() + " on terminal " + till.getName() + " has requested assistance with message:\n" + message);
    }

    @JConnMethod("GETTAKINGS")
    private BigDecimal getTakings(int terminal) throws IOException, SQLException {
        return dc.getTillTakings(terminal);
    }

    @JConnMethod("GETUNCASHEDSALES")
    private List<Sale> getUncashedSales(String terminal) throws IOException, SQLException {
        return dc.getUncashedSales(terminal);
    }

    @JConnMethod("SENDEMAIL")
    private void sendEmail(String message) throws IOException, SQLException {
        dc.sendEmail(message);
    }

    @JConnMethod("SENDRECEIPT")
    private void sendReceipt(String email, Sale sale) throws IOException, MessagingException {
        dc.emailReceipt(email, sale);
    }

    @JConnMethod("ADDTILL")
    private Till addTill(Till t) throws IOException, SQLException {
        return dc.addTill(t);
    }

    @JConnMethod("REMOVETILL")
    private void removeTill(int id) throws IOException, SQLException, JTillException {
        dc.removeTill(id);
    }

    @JConnMethod("GETTILL")
    private Till getTill(int id) throws IOException, SQLException, JTillException {
        return dc.getTill(id);
    }

    @JConnMethod("GETALLTILLS")
    private List<Till> getAllTills() throws IOException, SQLException {
        return dc.getAllTills();
    }

    @JConnMethod("CONNECTTILL")
    private Till connectTill(String name, UUID uuid) throws IOException, SQLException {
        return dc.connectTill(name, uuid);
    }

    @JConnMethod("DISCONNECTTILL")
    private void disconnectTill(Till t) {
        dc.disconnectTill(t);
    }

    @JConnMethod("GETALLCONNECTEDTILLS")
    private List<Till> getAllConnectedTills() throws IOException {
        return dc.getConnectedTills();
    }

    @JConnMethod("SETSETTING")
    private void setSetting(String key, String value) throws IOException {
        dc.setSetting(key, value);
    }

    @JConnMethod("GETSETTING")
    private String getSetting(String key) throws IOException {
        return dc.getSetting(key);
    }

    @JConnMethod("GETSETTINGDEFAULT")
    private String getSettingDefault(String key, String def_value) throws IOException {
        return dc.getSetting(key, def_value);
    }

    private Settings getSettingsInstance() throws IOException {
        return dc.getSettingsInstance();
    }

    @JConnMethod("ADDPLU")
    private Plu addPlu(Plu p) throws IOException, SQLException {
        return dc.addPlu(p);
    }

    @JConnMethod("REMOVEPLU")
    private void removePlu(Plu p) throws IOException, SQLException, JTillException {
        dc.removePlu(p);
    }

    @JConnMethod("GETPLU")
    private Plu getPlu(int id) throws IOException, SQLException, JTillException {
        return dc.getPlu(id);
    }

    @JConnMethod("GETPLUBYCODE")
    private Plu getPluByCode(String code) throws IOException, SQLException, JTillException {
        return dc.getPluByCode(code);
    }

    @JConnMethod("GETALLPLUS")
    private List<Plu> getAllPlus() throws IOException, SQLException {
        return dc.getAllPlus();
    }

    @JConnMethod("UPDATEPLU")
    private Plu updatePlu(Plu plu) throws IOException, SQLException, JTillException {
        return dc.updatePlu(plu);
    }

    @JConnMethod("ISTILLLOGGEDIN")
    private boolean isTillLoggedIn(Staff s) throws IOException, StaffNotFoundException, SQLException {
        return dc.isTillLoggedIn(s);
    }

    @JConnMethod("CHECKUSER")
    private boolean checkUsername(String username) throws IOException, SQLException, JTillException {
        return dc.checkUsername(username);
    }

    @JConnMethod("ADDWASTEREPORT")
    private WasteReport addWasteReport(WasteReport wr) throws IOException, SQLException, JTillException {
        return dc.addWasteReport(wr);
    }

    @JConnMethod("REMOVEWASTEREPORT")
    private void removeWasteReport(int id) throws IOException, SQLException, JTillException {
        dc.removeWasteReport(id);
    }

    @JConnMethod("GETWASTEREPORT")
    private WasteReport getWasteReport(int id) throws IOException, SQLException, JTillException {
        return dc.getWasteReport(id);
    }

    @JConnMethod("GETALLWASTEREPORTS")
    private List<WasteReport> getAllWasteReports() throws IOException, SQLException, JTillException {
        return dc.getAllWasteReports();
    }

    @JConnMethod("UPDATEWASTEREPORT")
    private WasteReport updateWasteReport(WasteReport wr) throws IOException, SQLException, JTillException {
        return dc.updateWasteReport(wr);
    }

    @JConnMethod("ADDWASTEITEM")
    private WasteItem addWasteItem(WasteReport wr, WasteItem wi) throws IOException, SQLException, JTillException {
        return dc.addWasteItem(wr, wi);
    }

    @JConnMethod("REMOVEWASTEITEM")
    private void removeWasteItem(int id) throws IOException, SQLException, JTillException {
        dc.removeWasteItem(id);
    }

    @JConnMethod("GETWASTEITEM")
    private WasteItem getWasteItem(int id) throws IOException, SQLException, JTillException {
        return dc.getWasteItem(id);
    }

    @JConnMethod("GETALLWASTEITEMS")
    private List<WasteItem> getAllWasteItems() throws IOException, SQLException, JTillException {
        return dc.getAllWasteItems();
    }

    @JConnMethod("UPDATEWASTEITEM")
    private WasteItem updateWasteItem(WasteItem wi) throws IOException, SQLException, JTillException {
        return dc.updateWasteItem(wi);
    }

    @JConnMethod("ADDWASTEREASON")
    private WasteReason addWasteReason(WasteReason wr) throws IOException, SQLException, JTillException {
        return dc.addWasteReason(wr);
    }

    @JConnMethod("REMOVEWASTEREASON")
    private void removeWasteReason(int id) throws IOException, SQLException, JTillException {
        dc.removeWasteReason(id);
    }

    @JConnMethod("GETWASTEREASON")
    private WasteReason getWasteReason(int id) throws IOException, SQLException, JTillException {
        return dc.getWasteReason(id);
    }

    @JConnMethod("GETALLWASTEREASONS")
    private List<WasteReason> getAllWasteReasons() throws IOException, SQLException, JTillException {
        return dc.getAllWasteReasons();
    }

    @JConnMethod("UPDATEWASTEREASON")
    private WasteReason updateWasteReason(WasteReason wr) throws IOException, SQLException, JTillException {
        return dc.updateWasteReason(wr);
    }

    @JConnMethod("ADDSUPPLIER")
    private Supplier addSupplier(Supplier s) throws IOException, SQLException, JTillException {
        return dc.addSupplier(s);
    }

    @JConnMethod("REMOVESUPPLIER")
    private void removeSupplier(int id) throws IOException, SQLException, JTillException {
        dc.removeSupplier(id);
    }

    @JConnMethod("GETSUPPLIER")
    private Supplier getSupplier(int id) throws IOException, SQLException, JTillException {
        return dc.getSupplier(id);
    }

    @JConnMethod("GETALLSUPPLIERS")
    private List<Supplier> getAllSuppliers() throws IOException, SQLException {
        return dc.getAllSuppliers();
    }

    @JConnMethod("UPDATESUPPLIER")
    private Supplier updateSupplier(Supplier s) throws IOException, SQLException, JTillException {
        return dc.updateSupplier(s);
    }

    @JConnMethod("ADDDEPARTMENT")
    private Department addDepartment(Department d) throws IOException, SQLException, JTillException {
        return dc.addDepartment(d);
    }

    @JConnMethod("REMOVEDEPARTMENT")
    private void removeDepartment(int id) throws IOException, SQLException, JTillException {
        dc.removeDepartment(id);
    }

    @JConnMethod("GETDEPARTMENT")
    private Department getDepartment(int id) throws IOException, SQLException, JTillException {
        return dc.getDepartment(id);
    }

    @JConnMethod("GETALLDEPARTMENTS")
    private List<Department> getAllDepartments() throws IOException, SQLException {
        return dc.getAllDepartments();
    }

    @JConnMethod("UPDATEDEPARTMENT")
    private Department updateDepartment(Department d) throws IOException, SQLException, JTillException {
        return dc.updateDepartment(d);
    }

    @JConnMethod("ADDSALEITEM")
    private SaleItem addSaleItem(Sale s, SaleItem i) throws IOException, SQLException, JTillException {
        return dc.addSaleItem(s, i);
    }

    @JConnMethod("REMOVESALEITEM")
    private void removeSaleItem(int id) throws IOException, SQLException, JTillException {
        dc.removeSaleItem(id);
    }

    @JConnMethod("GETSALEITEM")
    private SaleItem getSaleItem(int id) throws IOException, SQLException, JTillException {
        return dc.getSaleItem(id);
    }

    @JConnMethod("GETALLSALEITEMS")
    private List<SaleItem> getAllSaleItems() throws IOException, SQLException {
        return dc.getAllSaleItems();
    }

    private List<SaleItem> subSaleItemQuery(String q) throws IOException, SQLException {
        return dc.submitSaleItemQuery(q);
    }

    @JConnMethod("UPDATESALEITEM")
    private SaleItem updateSaleItem(SaleItem i) throws IOException, SQLException, JTillException {
        return dc.updateSaleItem(i);
    }

    @JConnMethod("GETTOTALSOLDITEM")
    private int getTotalSoldItem(int id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getTotalSoldOfItem(id);
    }

    @JConnMethod("GETVALUESOLDITEM")
    private BigDecimal getValueSoldItem(int id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getTotalValueSold(id);
    }

    @JConnMethod("GETTOTALWASTEDITEM")
    private int getTotalWastedItem(int id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getTotalWastedOfItem(id);
    }

    @JConnMethod("GETVALUEWASTEDITEM")
    private BigDecimal getValueWastedItem(int id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getValueWastedOfItem(id);
    }

    @JConnMethod("ADDRECEIVEDITEM")
    private void addReceivedItem(ReceivedItem i) throws IOException, SQLException {
        dc.addReceivedItem(i);
    }

    @JConnMethod("GETSPENTONITEM")
    private BigDecimal getValueSpentOnItem(int id) throws IOException, SQLException, ProductNotFoundException {
        return dc.getValueSpentOnItem(id);
    }

    @JConnMethod("CLOCKON")
    private void clockOn(int id) throws IOException, SQLException, StaffNotFoundException {
        dc.clockOn(id);
    }

    @JConnMethod("CLOCKOFF")
    private void clockOff(int id) throws IOException, SQLException, StaffNotFoundException {
        dc.clockOff(id);
    }

    @JConnMethod("GETALLCLOCKS")
    private List<ClockItem> getAllClocks(int id) throws IOException, SQLException, StaffNotFoundException {
        return dc.getAllClocks(id);
    }

    @JConnMethod("CLEARCLOCKS")
    private void clearClocks(int id) throws IOException, SQLException, StaffNotFoundException {
        dc.clearClocks(id);
    }

    @JConnMethod("ADDTRIGGER")
    private Trigger addTrigger(Trigger t) throws IOException, SQLException {
        return dc.addTrigger(t);
    }

    @JConnMethod("GETDISCOUNTBUCKETS")
    private List<DiscountBucket> getDiscountBuckets(int id) throws IOException, SQLException, DiscountNotFoundException {
        return dc.getDiscountBuckets(id);
    }

    @JConnMethod("REMOVETRIGGER")
    private void removeTrigger(int id) throws IOException, SQLException, JTillException {
        dc.removeTrigger(id);
    }

    @JConnMethod("GETVALIDDISCOUNTS")
    private List<Discount> getValidDiscounts() throws IOException, SQLException, JTillException {
        return dc.getValidDiscounts();
    }

    @JConnMethod("ADDBUCKET")
    private DiscountBucket addBucket(DiscountBucket b) throws IOException, SQLException, JTillException {
        return dc.addBucket(b);
    }

    @JConnMethod("REMOVEBUCKET")
    private void removeBucket(int id) throws IOException, SQLException, JTillException {
        dc.removeBucket(id);
    }

    @JConnMethod("GETBUCKETTRIGGERS")
    private List<Trigger> getBucketTriggers(int id) throws IOException, SQLException, JTillException {
        return dc.getBucketTriggers(id);
    }

    @JConnMethod("UPDATETRIGGER")
    private Trigger updateTrigger(Trigger t) throws IOException, SQLException, JTillException {
        return dc.updateTrigger(t);
    }

    @JConnMethod("UPDATEBUCKET")
    private DiscountBucket updateBucket(DiscountBucket b) throws IOException, SQLException, JTillException {
        return dc.updateBucket(b);
    }

    @JConnMethod("GETUNCASHEDTERMINALSALES")
    private List<Sale> getUncashedTerminalSales(int id) throws IOException, SQLException, JTillException {
        return dc.getUncachedTillSales(id);
    }

    @JConnMethod("ADDPRODUCTANDPLU")
    private Product addProductAndPlu(Product p, Plu pl) throws IOException, SQLException, JTillException {
        return dc.addProductAndPlu(p, pl);
    }

    @JConnMethod("GETPLUBYPRODUCT")
    private Plu getPluByProduct(int id) throws IOException, SQLException, JTillException {
        return dc.getPluByProduct(id);
    }

    @JConnMethod("SEARCHSALEITEMS")
    private List<SaleItem> searchSaleItems(Object[] object) throws IOException, SQLException, JTillException {
        final int department = (int) object[0];
        final int category = (int) object[1];
        final Date start = (Date) object[2];
        final Date end = (Date) object[3];
        return dc.searchSaleItems(department, category, start, end);
    }

    @JConnMethod("CONNTERM")
    private void terminateConnection() throws IOException, SQLException, StaffNotFoundException {
        dc.logout(staff);
        dc.tillLogout(staff);
        conn_term = true;
    }

    @JConnMethod("GETTERMINALSALES")
    private List<Sale> getTerminalSales(int terminal, boolean uncashedOnly) throws IOException, SQLException, JTillException {
        return dc.getTerminalSales(terminal, uncashedOnly);
    }

    @JConnMethod("INTEGRITYCHECK")
    private HashMap integrityCheck() throws IOException, SQLException {
        return dc.integrityCheck();
    }

    @JConnMethod("CASHUNCASHEDSALES")
    private void cashUncashedSales(int t) throws IOException, SQLException {
        dc.cashUncashedSales(t);
    }

    @JConnMethod("GETPRODUCTSADVANCED")
    private List<Product> getProductsAdvanced(String WHERE) throws IOException, SQLException {
        return dc.getProductsAdvanced(WHERE);
    }

    @JConnMethod("GETSTAFFSALES")
    private List<Sale> getStaffSales(Staff s) throws IOException, SQLException, StaffNotFoundException {
        return dc.getStaffSales(s);
    }
}
