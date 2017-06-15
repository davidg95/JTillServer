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

            ConnectionData firstCon = (ConnectionData) obIn.readObject();

            String site = (String) firstCon.getData();
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
                            final JConnMethod ja = (JConnMethod) a;
                            String asd = inp[0];
                            if (asd.equals("GETPRODUCT")) {
                                System.out.println("Cheese");
                            }
                            if (ja.value().equals(inp[0])) { //Check if the current flag matches the flag definted on the annotation
                                try {
                                    Runnable run; //Runnable which will invoke the method
                                    m.setAccessible(true); //Set the access to public
                                    final Class returnType = m.getReturnType(); //Get the return typoe of the method
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
                                                    final Object ret = m.invoke(this, data.getData()); //Invoke the method
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
                                                    final Object ret = m.invoke(this, data.getData(), data.getData2()); //Invoke the method
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
                /*switch (inp[0]) {
                    case "NEWPRODUCT": { //Add a new product
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                newProduct(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMOVEPRODUCT": { //Remove a product
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeProduct(data);
                            }
                        }.start();
                        break;
                    }
                    case "PURCHASE": { //Purchase a product
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                purchase(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETPRODUCT": { //Get a product
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getProduct(data);
                            }
                        }.start();
                        break;
                    }
                    case "UPDATEPRODUCT": { //Update a product
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateProduct(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETPRODUCTBARCODE": { //Get a product by barcode
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getProductByBarcode(data);
                            }
                        }.start();
                        break;
                    }
                    case "CHECKBARCODE": { //Check if a barcode is in use
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                checkBarcode(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETALLPRODUCTS": { //Get all products
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllProducts();
                            }
                        }.start();
                        break;
                    }
                    case "PRODUCTLOOKUP": { //Product lookup
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                productLookup(data);
                            }
                        }.start();
                        break;
                    }
                    case "NEWCUSTOMER": { //Add a new customer
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                newCustomer(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMOVECUSTOMER": { //Remove a customer
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeCustomer(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETCUSTOMER": { //Get a customer
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getCustomer(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETCUSTOMERBYNAME": { //Get a customer by name
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getCustomerByName(data);
                            }
                        }.start();
                        break;
                    }
                    case "UPDATECUSTOMER": { //Update a customer
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateCustomer(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETALLCUSTOMERS": { //Get all customers
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllCustomers();
                            }
                        }.start();
                        break;
                    }
                    case "CUSTOMERLOOKUP": { //Search for a customer
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                customerLookup(data);
                            }
                        }.start();
                        break;
                    }
                    case "ADDSTAFF": { //Add a member of staff
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addStaff(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMOVESTAFF": { //Remove a member of staff
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeStaff(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETSTAFF": { //Get a member of staff
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getStaff(data);
                            }
                        }.start();
                        break;
                    }
                    case "UPDATESTAFF": { //Update a member of staff
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateStaff(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETALLSTAFF": { //Get all the staff
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllStaff();
                            }
                        }.start();
                        break;
                    }
                    case "STAFFCOUNT": { //Get the staff count
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                staffCount();
                            }
                        }.start();
                        break;
                    }
                    case "ADDSALE": { //Add a sale
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addSale(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETALLSALES": { //Get all sales
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllSales();
                            }
                        }.start();
                        break;
                    }
                    case "GETSALE": { //Get a sale
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getSale(data);
                            }
                        }.start();
                        break;
                    }
                    case "UPDATESALE": { //Update a sale
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateSale(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETSALEDATERANGE": { //Get all sales within a date range
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getSaleDateRange(data);
                            }
                        }.start();
                        break;
                    }
                    case "SUSPENDSALE": { //Suspend a sale
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                suspendSale(data);
                            }
                        }.start();
                        break;
                    }
                    case "RESUMESALE": { //Resume a sale
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                resumeSale(data);
                            }
                        }.start();
                        break;
                    }
                    case "LOGIN": { //Standard staff login
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                login(data);
                            }
                        }.start();
                        break;
                    }
                    case "TILLLOGIN": { //Till login
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                tillLogin(data);
                            }
                        }.start();
                        break;
                    }
                    case "LOGOUT": { //Logout
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                logout(data);
                            }
                        }.start();
                        break;
                    }
                    case "TILLLOGOUT": { //Till logout
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                tillLogout(data);
                            }
                        }.start();
                        break;
                    }
                    case "ADDCATEGORY": { //Add a category
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addCategory(data);
                            }
                        }.start();
                        break;
                    }
                    case "UPDATECATEGORY": { //Update a category
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateCategory(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMOVECATEGORY": { //Remove a category
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeCategory(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETCATEGORY": { //Get a category
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getCategory(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETALLCATEGORYS": { //Get all categorys
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllCategorys();
                            }
                        }.start();
                        break;
                    }
                    case "GETPRODUCTSINCATEGORY": { //Get all products in a category
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getProductsInCategory(data);
                            }
                        }.start();
                        break;
                    }
                    case "ADDDISCOUNT": { //Add a discount
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addDiscount(data);
                            }
                        }.start();
                        break;
                    }
                    case "UPDATEDISCOUNT": { //Update a discount
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateDiscount(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMOVEDISCOUNT": { //Remove a discount
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeDiscount(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETDISCOUNT": { //Get a discount
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getDiscount(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETALLDISCOUNTS": { //Get all discounts
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllDiscounts();
                            }
                        }.start();
                        break;
                    }
                    case "ADDTAX": { //Add a new tax
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addTax(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMOVETAX": { //Remove a tax
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeTax(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETTAX": { //Get a tax
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getTax(data);
                            }
                        }.start();
                        break;
                    }
                    case "UPDATETAX": { //Update a tax
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateTax(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETALLTAX": { //Get all tax
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllTax();
                            }
                        }.start();
                        break;
                    }
                    case "GETPRODUCTSINTAX": { //Get products in tax
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getProductsInTax(data);
                            }
                        }.start();
                    }
                    case "ADDSCREEN": { //Add a new screen
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addScreen(data);
                            }
                        }.start();
                        break;
                    }
                    case "ADDBUTTON": { //Add a new button
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addButton(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMOVESCREEN": { //Remove a screen
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeScreen(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMOVEBUTTON": { //Remove a button
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeButton(data);
                            }
                        }.start();
                        break;
                    }
                    case "UPDATESCREEN": { //Update a screen
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateScreen(data);
                            }
                        }.start();
                        break;
                    }
                    case "UPDATEBUTTON": { //Update a button
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateButton(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETSCREEN": { //Update a screen
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getScreen(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETBUTTON": { //Updatea a button
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getButton(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETALLSCREENS": { //Get all screens
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllScreens();
                            }
                        }.start();
                        break;
                    }
                    case "GETALLBUTTONS": { //Get all buttons
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllButtons();
                            }
                        }.start();
                        break;
                    }
                    case "GETBUTTONSONSCREEN": { //Get buttons on a screen
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getButtonsOnScreen(data);
                            }
                        }.start();
                        break;
                    }
                    case "DROPSCREENSANDBUTTONS": {
                        try {
                            dc.deleteAllScreensAndButtons();
                        } catch (SQLException ex) {
                        }
                        break;
                    }
                    case "ASSISSTANCE": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                assisstance(data);
                            }
                        }.start();
                        break;
                    }
                    case "TAKINGS": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getTakings(data);
                            }
                        }.start();
                        break;
                    }
                    case "UNCASHEDSALES": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getUncashedSales(data);
                            }
                        }.start();
                        break;
                    }
                    case "EMAIL": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                sendEmail(data);
                            }
                        }.start();
                        break;
                    }
                    case "EMAILRECEIPT": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                sendReceipt(data);
                            }
                        }.start();
                        break;
                    }
                    case "ADDTILL": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addTill(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMOVETILL": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeTill(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETTILL": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getTill(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETALLTILLS": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllTills();
                            }
                        }.start();
                        break;
                    }
                    case "CONNECTTILL": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                connectTill(data);
                            }
                        }.start();
                        break;
                    }
                    case "DISCONNECTTILL": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                disconnectTill(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETCONNECTEDTILLS": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllConnectedTills();
                            }
                        }.start();
                        break;
                    }
                    case "SETSETTING": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                setSetting(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETSETTING": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getSetting(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETSETTINGDEFAULT": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getSettingDefault(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETSETTINGSINSTANCE": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getSettingsInstance();
                            }
                        }.start();
                        break;
                    }
                    case "ADDPLU": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addPlu(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMOVEPLU": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removePlu(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETPLU": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getPlu(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETPLUBYCODE": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getPluByCode(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETALLPLUS": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllPlus();
                            }
                        }.start();
                        break;
                    }
                    case "UPDATEPLU": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updatePlu(data);
                            }
                        }.start();
                        break;
                    }
                    case "ISLOGGEDTILL": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                isTillLoggedIn(data);
                            }
                        }.start();
                        break;
                    }
                    case "CHECKUSER": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                checkUsername(data);
                            }
                        }.start();
                        break;
                    }
                    case "ADDWASTEREPORT": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addWasteReport(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMOVEWASTEREPORT": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeWasteReport(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETWASTEREPORT": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getWasteReport(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETALLWASTEREPORTS": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllWasteReports();
                            }
                        }.start();
                        break;
                    }
                    case "UPDATEWASTEREPORT": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateWasteReport(data);
                            }
                        }.start();
                        break;
                    }
                    case "ADDWASTEITEM": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addWasteItem(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMOVEWASTEITEM": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeWasteItem(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETWASTEITEM": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getWasteItem(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETALLWASTEITEMS": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllWasteItems();
                            }
                        }.start();
                        break;
                    }
                    case "UPDATEWASTEITEM": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateWasteItem(data);
                            }
                        }.start();
                        break;
                    }
                    case "ADDWASTEREASON": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addWasteReason(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMOVEWASTEREASON": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeWasteReason(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETWASTEREASON": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getWasteReason(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETALLWASTEREASONS": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllWasteReasons();
                            }
                        }.start();
                        break;
                    }
                    case "UPDATEWASTEREASON": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateWasteReason(data);
                            }
                        }.start();
                        break;
                    }
                    case "ADDSUPPLIER": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addSupplier(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMOVESUPPLIER": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeSupplier(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETSUPPLIER": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getSupplier(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETALLSUPPLIERS": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllSuppliers();
                            }
                        }.start();
                        break;
                    }
                    case "UPDATESUPPLIER": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateSupplier(data);
                            }
                        }.start();
                        break;
                    }
                    case "ADDDEPARTMENT": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addDepartment(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMOVEDEPARTMENT": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeDepartment(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETDEPARTMENT": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getDepartment(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETALLDEPARTMENTS": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllDepartments();
                            }
                        }.start();
                        break;
                    }
                    case "UPDATEDEPARTMENT": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateDepartment(data);
                            }
                        }.start();
                        break;
                    }
                    case "ADDSALEITEM": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addSaleItem(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMVOESALEITEM": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeSaleItem(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETSALEITEM": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getSaleItem(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETALLSALEITEMS": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllSaleItems();
                            }
                        }.start();
                        break;
                    }
                    case "UPDATESALEITEM": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateSaleItem(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETTOTALSOLDITEM": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getTotalSoldItem(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETVALUESOLDITEM": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getValueSoldItem(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETTOTALWASTEDITEM": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getTotalWastedItem(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETVALUEWASTEDITEM": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getValueWastedItem(data);
                            }
                        }.start();
                        break;
                    }
                    case "ADDRECEIVEDITEM": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addReceivedItem(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETSPENTONITEM": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getValueSpentOnItem(data);
                            }
                        }.start();
                        break;
                    }
                    case "CLOCKON": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                clockOn(data);
                            }
                        }.start();
                        break;
                    }
                    case "CLOCKOFF": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                clockOff(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETCLOCKS": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getAllClocks(data);
                            }
                        }.start();
                        break;
                    }
                    case "CLEARCLOCKS": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                clearClocks(data);
                            }
                        }.start();
                        break;
                    }
                    case "ADDTRIGGER": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addTrigger(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETDISCOUNTBUCKETS": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getDiscountBuckets(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMOVETRIGGER": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeTrigger(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETVALIDDISCOUNTS": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getValidDiscounts();
                            }
                        }.start();
                        break;
                    }
                    case "ADDBUCKET": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addBucket(data);
                            }
                        }.start();
                        break;
                    }
                    case "REMOVEBUCKET": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                removeBucket(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETBUCKETTRIGGERES": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getBucketTriggers(data);
                            }
                        }.start();
                        break;
                    }
                    case "UPDATETRIGGER": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateTrigger(data);
                            }
                        }.start();
                        break;
                    }
                    case "UPDATEBUCKET": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                updateBucket(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETUNCASHEDTERMINALSALES": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getUncashedTerminalSales(data);
                            }
                        }.start();
                        break;
                    }
                    case "ADDPRODUCTANDPLU": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                addProductAndPlu(data);
                            }
                        }.start();
                        break;
                    }
                    case "GETPLUBYPRODUCT": {
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getPluByProduct(data);
                            }
                        }.start();
                        break;
                    }
                    case "CONNTERM": { //Terminate the connection
                        conn_term = true;
                        if (staff != null) {
                            try {
                                dc.logout(staff);
                                dc.tillLogout(staff);
                                LOG.log(Level.INFO, till.getName() + " has terminated their connection to the server");
                            } catch (StaffNotFoundException ex) {
                            }
                        }
                        break;
                    }
                    default: {
                        LOG.log(Level.WARNING, "An unknown flag " + data.getFlag() + " was received from " + till.getName());
                        break;
                    }
                }*/
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
        Staff s = dc.login(username, password);
        ConnectionThread.this.staff = s;
        LOG.log(Level.INFO, s.getName() + " has logged in");
        s.setPassword(Encryptor.encrypt(s.getPassword()));
        return s;
    }

    @JConnMethod("TILLLOGIN")
    private Staff tillLogin(int id) throws IOException, LoginException, SQLException {
        Staff s = dc.tillLogin(id);
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

    @JConnMethod("GETPRODUCTANDPLU")
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

    @JConnMethod("IntegrityCheck")
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
