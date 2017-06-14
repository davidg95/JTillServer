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
                            if (asd.equals("GETALLSCREENS")) {
                                System.out.println("Here we go! :D");
                            }
                            if (ja.value().equals(inp[0])) { //Check if the current flag matches the flag definted on the annotation
                                try {
                                    boolean data1valid = false;
                                    boolean data2valid = false;
                                    if (data.getData2() == null) { //If no second value was apssed, then set the second value valid to true
                                        data2valid = true;
                                    }
                                    if (m.getParameterCount() == 0) { //check if it has any parameters
                                        m.invoke(this); //Invoke the method
                                    } else {
                                        final Annotation[] ans = m.getParameters()[0].getAnnotations(); //Get the annotations for the parameter
                                        for (Annotation ano : ans) {
                                            if (ano.annotationType() == JConnValue.class) { //Check for the JConnValue annotation
                                                final JConnValue jv = (JConnValue) ano;
                                                if (data.getData().getClass().equals(jv.value())) {
                                                    data1valid = true;
                                                }
                                            }
                                            if (ano.annotationType() == JConnValue2.class) { //Check for the JConnVAlue2 annotation
                                                final JConnValue2 jv = (JConnValue2) ano;
                                                if (data.getData2().getClass().equals(jv.value())) {
                                                    data2valid = true;
                                                }
                                            }
                                        }
                                        if (data1valid && data2valid) { //If the data type are correct, then invoke the method
                                            m.invoke(this, data);
                                        } else { //If the are incorrect, then send a reply back indicating this
                                            obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type"));
                                        }
                                    }
                                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
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
    private void newProduct(@JConnValue(Product.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (clone.getData() == null) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A null value was received"));
                    return;
                }
                Product p = (Product) clone.getData();
                Product newP = dc.addProduct(p);
                obOut.writeObject(ConnectionData.create("SUCC", newP));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("REMOVEPRODUCT")
    private void removeProduct(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (clone.getData() == null) {
                    obOut.writeObject(ConnectionData.create("FAIL", "No value was received"));
                    return;
                }
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An int value mussed be passed in"));
                    return;
                }
                int code = (int) clone.getData();
                dc.removeProduct(code);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | ProductNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("PURCHASE")
    private void purchase(@JConnValue(Integer.class) @JConnValue2(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (clone.getData() == null || clone.getData2() == null) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A null value was received"));
                    LOG.log(Level.SEVERE, "A null was received");
                    return;
                }
                if (!(clone.getData() instanceof Integer) || !(clone.getData2() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An integer must be passed in"));
                    LOG.log(Level.SEVERE, "An unexpected value type was received");
                    return;
                }
                Product p = (Product) clone.getData();
                int amount = (int) clone.getData2();
                int stock = dc.purchaseProduct(p.getId(), amount);
                obOut.writeObject(ConnectionData.create("SUCC", stock));
            } catch (ProductNotFoundException | SQLException | OutOfStockException ex) {
                LOG.log(Level.WARNING, null, ex);
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETPRODUCT")
    private void getProduct(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (clone.getData() == null) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A null value was received"));
                    return;
                }
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An integerp must be passed here"));
                    return;
                }
                int code = (int) clone.getData();
                Product p = dc.getProduct(code);
                obOut.writeObject(ConnectionData.create("SUCC", p));
            } catch (ProductNotFoundException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("UPDATEPRODUCT")
    private void updateProduct(@JConnValue(Product.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Product)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A product must be passed here"));
                    return;
                }
                Product p = (Product) clone.getData();
                dc.updateProduct(p);
                obOut.writeObject(ConnectionData.create("SUCC", p));
            } catch (SQLException | ProductNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETPRODUCTBARCODE")
    private void getProductByBarcode(@JConnValue(String.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received"));
                    return;
                }
                String barcode = (String) clone.getData();
                Product p = dc.getProductByBarcode(barcode);
                obOut.writeObject(ConnectionData.create("SUCC", p));
            } catch (ProductNotFoundException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("CHECKBARCODE")
    private void checkBarcode(@JConnValue(String.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                String barcode = (String) clone.getData();
                boolean inUse = dc.checkBarcode(barcode);
                obOut.writeObject(ConnectionData.create("SUCC", inUse));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETALLPRODUCTS")
    private void getAllProducts() {
        try {
            try {
                List<Product> products = dc.getAllProducts();
                obOut.writeObject(products);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("PRODUCTLOOKUP")
    private void productLookup(@JConnValue(String.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                String terms = (String) clone.getData();
                List<Product> products = dc.productLookup(terms);
                obOut.writeObject(ConnectionData.create("SUCC", products));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("NEWCUSTOMER")
    private void newCustomer(@JConnValue(Customer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Customer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Customer must be received here"));
                    return;
                }
                Customer c = (Customer) clone.getData();
                Customer newC = dc.addCustomer(c);
                obOut.writeObject(ConnectionData.create("SUCC", newC));
            } catch (SQLException e) {
                obOut.writeObject(ConnectionData.create("FAIL", e));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("REMOVECUSTOMER")
    private void removeCustomer(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                dc.removeCustomer(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | CustomerNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETCUSTOMER")
    private void getCustomer(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Customer c = dc.getCustomer(id);
                obOut.writeObject(ConnectionData.create("SUCC", c));
            } catch (CustomerNotFoundException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETCUSTOMERBYNAME")
    private void getCustomerByName(@JConnValue(String.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                String name = (String) clone.getData();
                List<Customer> customers = dc.getCustomerByName(name);
                obOut.writeObject(ConnectionData.create("SUCC", customers));
            } catch (SQLException | CustomerNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("UPDATECUSTOMER")
    private void updateCustomer(@JConnValue(Customer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Customer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Customer must be received here"));
                    return;
                }
                Customer c = (Customer) clone.getData();
                Customer customer = dc.updateCustomer(c);
                obOut.writeObject(ConnectionData.create("SUCC", customer));
            } catch (SQLException | CustomerNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETALLCUSTOMERS")
    private void getAllCustomers() {
        try {
            try {
                List<Customer> customers = dc.getAllCustomers();
                obOut.writeObject(customers);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("CUSTOMERLOOKUP")
    private void customerLookup(@JConnValue(String.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (clone.getData() == null || !(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be passed here"));
                }
                String terms = (String) clone.getData();
                List<Customer> customers = dc.customerLookup(terms);
                obOut.writeObject(ConnectionData.create("SUCC", customers));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("ADDSTAFF")
    private void addStaff(@JConnValue(Staff.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Staff)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Staff must be received here"));
                    return;
                }
                Staff s = (Staff) clone.getData();
                s.setPassword(Encryptor.decrypt(s.getPassword()));
                Staff newS = dc.addStaff(s);
                s.setPassword(Encryptor.encrypt(s.getPassword()));
                obOut.writeObject(ConnectionData.create("SUCC", newS));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("REMOVESTAFF")
    private void removeStaff(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                dc.removeStaff(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | StaffNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETSTAFF")
    private void getStaff(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Staff s = dc.getStaff(id);
                s.setPassword(Encryptor.encrypt(s.getPassword()));
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (StaffNotFoundException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("UPDATESTAFF")
    private void updateStaff(@JConnValue(Staff.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Staff)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Staff must be received here"));
                    return;
                }
                Staff s = (Staff) clone.getData();
                s.setPassword(Encryptor.decrypt(s.getPassword()));
                Staff updatedStaff = dc.updateStaff(s);
                s.setPassword(Encryptor.encrypt(s.getPassword()));
                obOut.writeObject(ConnectionData.create("SUCC", updatedStaff));
            } catch (SQLException | StaffNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETALLSTAFF")
    private void getAllStaff() {
        try {
            try {
                List<Staff> staffList = dc.getAllStaff();
                staffList.forEach((s) -> {
                    s.setPassword(Encryptor.encrypt(s.getPassword()));
                });
                obOut.writeObject(staffList);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("STAFFCOUNT")
    private void staffCount() {
        try {
            try {
                obOut.writeObject(dc.getStaffCount());
            } catch (SQLException ex) {
                obOut.writeObject("FAIL");
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("ADDSALE")
    private void addSale(@JConnValue(Sale.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Sale)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Sale must be received here"));
                    return;
                }
                Sale s = (Sale) clone.getData();
                Sale newS = dc.addSale(s);
                obOut.writeObject(ConnectionData.create("SUCC", newS));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETALLSALES")
    private void getAllSales() {
        try {
            try {
                List<Sale> sales = dc.getAllSales();
                obOut.writeObject(sales);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETSALE")
    private void getSale(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Sale s = dc.getSale(id);
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("UPDATESALE")
    private void updateSale(@JConnValue(Sale.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Sale)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Sale must be received here"));
                    return;
                }
                Sale sale = (Sale) clone.getData();
                Sale s = dc.updateSale(sale);
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETSALEDATERANGE")
    private void getSaleDateRange(@JConnValue(Time.class) @JConnValue2(Time.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Time)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Time must be received here"));
                    return;
                }
                if (!(clone.getData2() instanceof Time)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Time must be received here"));
                    return;
                }
                Time start = (Time) clone.getData();
                Time end = (Time) clone.getData2();
                List<Sale> sales = dc.getSalesInRange(start, end);
                obOut.writeObject(ConnectionData.create("SUCC", sales));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("SUSPENDSALE")
    private void suspendSale(@JConnValue(Sale.class) @JConnValue2(Staff.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Sale)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A Sale must be received here"));
                return;
            }
            if (!(clone.getData2() instanceof Staff)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A Staff must be received here"));
                return;
            }
            Sale sale = (Sale) clone.getData();
            Staff s = (Staff) clone.getData2();
            dc.suspendSale(sale, s);
            obOut.writeObject(ConnectionData.create("SUSPEND", "SUCCESS"));

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("RESUMESALE")
    private void resumeSale(@JConnValue(Staff.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Staff)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A Staff must be received here"));
                return;
            }
            Staff s = (Staff) clone.getData();
            Sale sale = dc.resumeSale(s);
            obOut.writeObject(ConnectionData.create("RESUME", sale));

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("LOGIN")
    private void login(@JConnValue(String.class) @JConnValue2(String.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                if (!(clone.getData2() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                String username = (String) clone.getData();
                String password = (String) clone.getData2();
                password = Encryptor.decrypt(password);
                Staff s = dc.login(username, password);
                ConnectionThread.this.staff = s;
                LOG.log(Level.INFO, s.getName() + " has logged in");
                s.setPassword(Encryptor.encrypt(s.getPassword()));
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (SQLException | LoginException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("TILLLOGIN")
    private void tillLogin(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Staff s = dc.tillLogin(id);
                ConnectionThread.this.staff = s;
                LOG.log(Level.INFO, staff.getName() + " has logged in from " + till.getName());
                s.setPassword(Encryptor.encrypt(s.getPassword()));
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (SQLException | LoginException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("LOGOUT")
    private void logout(@JConnValue(Staff.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Staff)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Staff must be received here"));
                    return;
                }
                Staff s = (Staff) clone.getData();
                dc.logout(s);
                LOG.log(Level.INFO, staff.getName() + " has logged out");
                ConnectionThread.this.staff = null;
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (StaffNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("TILLOGOUT")
    private void tillLogout(@JConnValue(Staff.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Staff)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Staff must be received here"));
                    return;
                }
                Staff s = (Staff) clone.getData();
                dc.tillLogout(s);
                LOG.log(Level.INFO, staff.getName() + " has logged out");
                ConnectionThread.this.staff = null;
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (StaffNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("ADDCATEGORY")
    private void addCategory(@JConnValue(Category.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Category)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Category must be received here"));
                    return;
                }
                Category c = (Category) clone.getData();
                Category newC = dc.addCategory(c);
                obOut.writeObject(ConnectionData.create("SUCC", newC));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("UPDATECATEGORY")
    private void updateCategory(@JConnValue(Category.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Category)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Category must be received here"));
                    return;
                }
                Category c = (Category) clone.getData();
                Category category = dc.updateCategory(c);
                obOut.writeObject(ConnectionData.create("SUCC", category));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("REMOVECATEGORY")
    private void removeCategory(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                dc.removeCategory(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETCATEGORY")
    private void getCategory(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Category c = dc.getCategory(id);
                obOut.writeObject(ConnectionData.create("SUCC", c));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETALLCATEGORYS")
    private void getAllCategorys() {
        try {
            try {
                List<Category> categorys = dc.getAllCategorys();
                obOut.writeObject(categorys);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETPRODUCTSINCATEGORY")
    private void getProductsInCategory(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                List<Product> products = dc.getProductsInCategory(id);
                obOut.writeObject(ConnectionData.create("SUCC", products));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("ADDDISCOUNT")
    private void addDiscount(@JConnValue(Discount.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Discount)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Discount must be received here"));
                    return;
                }
                Discount d = (Discount) clone.getData();
                Discount newD = dc.addDiscount(d);
                obOut.writeObject(ConnectionData.create("SUCC", newD));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("UPDATEDISCOUNT")
    private void updateDiscount(@JConnValue(Discount.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Discount)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Discount must be received here"));
                    return;
                }
                Discount d = (Discount) clone.getData();
                Discount discount = dc.updateDiscount(d);
                obOut.writeObject(ConnectionData.create("SUCC", discount));
            } catch (SQLException | DiscountNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("REMOVEDISCOUNT")
    private void removeDiscount(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                dc.removeDiscount(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | DiscountNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETDISCOUNT")
    private void getDiscount(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Discount d = dc.getDiscount(id);
                obOut.writeObject(ConnectionData.create("SUCC", d));
            } catch (SQLException | DiscountNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETALLDISCOUNTS")
    private void getAllDiscounts() {
        try {
            try {
                List<Discount> discounts = dc.getAllDiscounts();
                obOut.writeObject(discounts);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("ADDTAX")
    private void addTax(@JConnValue(Tax.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Tax)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Tax must be received here"));
                    return;
                }
                Tax t = (Tax) clone.getData();
                Tax newT = dc.addTax(t);
                obOut.writeObject(ConnectionData.create("SUCC", newT));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("REMOVETAX")
    private void removeTax(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                dc.removeTax(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETTAX")
    private void getTax(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Tax t = dc.getTax(id);
                obOut.writeObject(ConnectionData.create("SUCC", t));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("UPDATETAX")
    private void updateTax(@JConnValue(Tax.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Tax)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Tax must be received here"));
                    return;
                }
                Tax t = (Tax) clone.getData();
                Tax tax = dc.updateTax(t);
                obOut.writeObject(ConnectionData.create("SUCC", tax));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETALLTAX")
    private void getAllTax() {
        try {
            try {
                List<Tax> tax = dc.getAllTax();
                obOut.writeObject(tax);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETPRODUCTSINTAX")
    private void getProductsInTax(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int id = (int) clone.getData();
                List<Product> products = dc.getProductsInTax(id);
                obOut.writeObject(ConnectionData.create("SUCC", products));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("ADDSCREEN")
    private void addScreen(@JConnValue(Screen.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Screen)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Screen must be received here"));
                    return;
                }
                Screen s = (Screen) clone.getData();
                Screen newS = dc.addScreen(s);
                obOut.writeObject(ConnectionData.create("SUCC", newS));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("ADDBUTTON")
    private void addButton(@JConnValue(TillButton.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof TillButton)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A TillButton must be received here"));
                    return;
                }
                TillButton b = (TillButton) clone.getData();
                TillButton newB = dc.addButton(b);
                obOut.writeObject(ConnectionData.create("SUCC", newB));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("REMOVESCREEN")
    private void removeScreen(@JConnValue(Screen.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Screen)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Screen must be received here"));
                    return;
                }
                Screen s = (Screen) clone.getData();
                dc.removeScreen(s);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | ScreenNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("SUCC", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("REMOVEBUTTON")
    private void removeButton(@JConnValue(TillButton.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof TillButton)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A TillButton must be received here"));
                    return;
                }
                TillButton b = (TillButton) clone.getData();
                dc.removeButton(b);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | JTillException ex) {
                ConnectionData.create("FAIL", ex);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("UPDATESCREEN")
    private void updateScreen(@JConnValue(Screen.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Screen)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Screen must be received here"));
                    return;
                }
                Screen s = (Screen) clone.getData();
                dc.updateScreen(s);
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (SQLException | ScreenNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("UPDATEBUTTON")
    private void updateButton(@JConnValue(TillButton.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof TillButton)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A TillButton must be received here"));
                    return;
                }
                TillButton b = (TillButton) clone.getData();
                dc.updateButton(b);
                obOut.writeObject(ConnectionData.create("SUCC", b));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETSCREEN")
    private void getScreen(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Screen s = dc.getScreen(id);
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (SQLException | ScreenNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETBUTTON")
    private void getButton(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                TillButton b = dc.getButton(id);
                obOut.writeObject(ConnectionData.create("SUCC", b));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETALLSCREENS")
    private void getAllScreens() {
        try {
            try {
                List<Screen> screens = dc.getAllScreens();
                obOut.writeObject(ConnectionData.create("SUCC", screens));
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETALLBUTTONS")
    private void getAllButtons() {
        try {
            try {
                List<TillButton> buttons = dc.getAllButtons();
                obOut.writeObject(buttons);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("GETBUTTONSONSCREEN")
    private void getButtonsOnScreen(@JConnValue(Screen.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Screen)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Screen must be received here"));
                    return;
                }
                Screen s = (Screen) clone.getData();
                List<TillButton> buttons = dc.getButtonsOnScreen(s);
                obOut.writeObject(ConnectionData.create("SUCC", buttons));
            } catch (SQLException | ScreenNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("ASSISSTANCE")
    private void assisstance(@JConnValue(String.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof String)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                return;
            }
            String message = (String) clone.getData();
            dc.assisstance(staff.getName() + " on terminal " + till.getName() + " has requested assistance with message:\n" + message);
            obOut.writeObject(ConnectionData.create("SUCC"));

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETTAKINGS")
    private void getTakings(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int terminal = (int) clone.getData();
                BigDecimal t = dc.getTillTakings(terminal);
                obOut.writeObject(ConnectionData.create("GET", t));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));

            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETUNCASHEDSALES")
    private void getUncashedSales(@JConnValue(String.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                String terminal = (String) clone.getData();
                List<Sale> sales = dc.getUncashedSales(terminal);
                obOut.writeObject(ConnectionData.create("GET", sales));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));

            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("SENDEMAIL")
    private void sendEmail(@JConnValue(String.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof String)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                return;
            }
            String message = (String) clone.getData();
            dc.sendEmail(message);
            obOut.writeObject(ConnectionData.create("SUCC"));

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("SENDRECEIPT")
    private void sendReceipt(@JConnValue(String.class) @JConnValue2(Sale.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                if (!(clone.getData2() instanceof Sale)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Sale must be received here"));
                    return;
                }
                String email = (String) clone.getData();
                Sale sale = (Sale) clone.getData2();
                dc.emailReceipt(email, sale);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (IOException | MessagingException ex) {
                obOut.writeObject(ConnectionData.create("FAIL"));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @JConnMethod("ADDTILL")
    private void addTill(@JConnValue(Till.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Till)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Till must be received here"));
                    return;
                }
                Till t = (Till) clone.getData();
                Till newT = dc.addTill(t);
                obOut.writeObject(ConnectionData.create("ADD", newT));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));

            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("REMOVETILL")
    private void removeTill(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                dc.removeTill(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));

            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETTILL")
    private void getTill(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Till t = dc.getTill(id);
                obOut.writeObject(ConnectionData.create("GET", t));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETALLTILLS")
    private void getAllTills() {
        try {
            try {
                List<Till> tills = dc.getAllTills();
                obOut.writeObject(tills);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("CONNECTTILL")
    private void connectTill(@JConnValue(UUID.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof UUID)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A UUID must be received here"));
                return;
            }
            String name = (String) clone.getData();
            UUID uuid = (UUID) clone.getData2();
            Till ti = dc.connectTill(name, uuid);
            obOut.writeObject(ConnectionData.create("CONNECT", ti));

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("DISCONNECTTILL")
    private void disconnectTill(@JConnValue(Till.class) ConnectionData data) {
        ConnectionData clone = data.clone();
        Till t = (Till) clone.getData();
        dc.disconnectTill(t);
    }

    @JConnMethod("GETALLCONNECTEDTILLS")
    private void getAllConnectedTills() {
        try {
            try {
                List<Till> ts = dc.getConnectedTills();
                obOut.writeObject(ConnectionData.create("SUCC", ts));
            } catch (IOException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("SETSETTING")
    private void setSetting(@JConnValue(String.class) @JConnValue2(String.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof String) || !(clone.getData() instanceof String)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                return;
            }
            String key = (String) clone.getData();
            String value = (String) clone.getData2();
            dc.setSetting(key, value);
            obOut.writeObject(ConnectionData.create("SUCC"));

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETSETTING")
    private void getSetting(@JConnValue(String.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof String)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                return;
            }
            String key = (String) clone.getData();
            String value = dc.getSetting(key);
            obOut.writeObject(ConnectionData.create("SUCC", value));

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETSETTINGDEFAULT")
    private void getSettingDefault(@JConnValue(String.class) @JConnValue2(String.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof String || !(clone.getData() instanceof String))) {
                obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                return;
            }
            String key = (String) clone.getData();
            String def_value = (String) clone.getData2();
            String value = dc.getSetting(key, def_value);
            obOut.writeObject(ConnectionData.create("SUCC", value));

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void getSettingsInstance() {
        try {
            Settings settings = dc.getSettingsInstance();
            obOut.writeObject(ConnectionData.create("SUCC", settings));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("ADDPLU")
    private void addPlu(@JConnValue(Plu.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Plu)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Plu must be received here"));
                    return;
                }
                Plu p = (Plu) clone.getData();
                Plu newP = dc.addPlu(p);
                obOut.writeObject(ConnectionData.create("SUCC", newP));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));

            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("REMOVEPLU")
    private void removePlu(@JConnValue(Plu.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Plu)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Plu must be received here"));
                    return;
                }
                Plu p = (Plu) clone.getData();
                dc.removePlu(p);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (JTillException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));

            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETPLU")
    private void getPlu(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An int must be received here"));
                    return;
                }
                int id = (int) data.getData();
                Plu p = dc.getPlu(id);
                obOut.writeObject(ConnectionData.create("SUCC", p));
            } catch (JTillException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));

            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETPLUBYCODE")
    private void getPluByCode(@JConnValue(String.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                String code = (String) data.getData();
                Plu p = dc.getPluByCode(code);
                obOut.writeObject(ConnectionData.create("SUCC", p));
            } catch (JTillException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));

            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETALLPLUS")
    private void getAllPlus() {
        try {
            try {
                List<Plu> p = dc.getAllPlus();
                obOut.writeObject(ConnectionData.create("SUCC", p));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));

            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("UPDATEPLU")
    private void updatePlu(@JConnValue(Plu.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Plu)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Plu must be received here"));
                    return;
                }
                Plu plu = (Plu) data.getData();
                Plu p = dc.updatePlu(plu);
                obOut.writeObject(ConnectionData.create("SUCC", p));
            } catch (JTillException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));

            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("ISTILLLOGGEDIN")
    private void isTillLoggedIn(@JConnValue(Staff.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Staff)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Staff must be received here"));
                    return;
                }
                Staff s = (Staff) data.getData();
                boolean logged = dc.isTillLoggedIn(s);
                obOut.writeObject(ConnectionData.create("SUCC", logged));
            } catch (StaffNotFoundException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));

            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("CHECKUSER")
    private void checkUsername(@JConnValue(String.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                String username = (String) data.getData();
                boolean used = dc.checkUsername(username);
                obOut.writeObject(ConnectionData.create("SUCC", used));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));

            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("ADDWASTEREPORT")
    private void addWasteReport(@JConnValue(WasteReport.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof WasteReport)) {
                    LOG.log(Level.SEVERE, "Unexpected data type adding waste report");
                    obOut.writeObject(ConnectionData.create("FAIL", "Unexpected data type"));
                    return;
                }
                WasteReport wr = (WasteReport) clone.getData();
                wr = dc.addWasteReport(wr);
                obOut.writeObject(ConnectionData.create("SUCC", wr));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("REMOVEWASTEREPORT")
    private void removeWasteReport(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    LOG.log(Level.SEVERE, "Unexpected data type removing waste report");
                    obOut.writeObject(ConnectionData.create("FAIL", "Unexpected data type"));
                    return;
                }
                int id = (int) clone.getData();
                dc.removeWasteReport(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETWASTEREPORT")
    private void getWasteReport(@JConnValue(Integer.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    LOG.log(Level.SEVERE, "Unexpected data type getting waste report");
                    obOut.writeObject(ConnectionData.create("FAIL", "Int expected"));
                    return;
                }
                int id = (int) clone.getData();
                WasteReport wr = dc.getWasteReport(id);
                obOut.writeObject(ConnectionData.create("SUCC", wr));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETALLWASTEREPORTS")
    private void getAllWasteReports() {
        try {
            try {
                List<WasteReport> wrs = dc.getAllWasteReports();
                obOut.writeObject(ConnectionData.create("SUCC", wrs));
            } catch (IOException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("UPDATEWASTEREPORT")
    private void updateWasteReport(@JConnValue(WasteReport.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof WasteReport)) {
                    LOG.log(Level.SEVERE, "Unexpected data type");
                    obOut.writeObject(ConnectionData.create("FAIL", "WasteReport expected"));
                    return;
                }
                WasteReport wr = (WasteReport) clone.getData();
                wr = dc.updateWasteReport(wr);
                obOut.writeObject(ConnectionData.create("SUCC", wr));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("ADDWASTEITEM")
    private void addWasteItem(@JConnValue(WasteReport.class) @JConnValue2(WasteItem.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof WasteReport) || !(clone.getData2() instanceof WasteItem)) {
                LOG.log(Level.SEVERE, "Unexpected data type adding a waste item");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            WasteReport wr = (WasteReport) clone.getData();
            WasteItem wi = (WasteItem) clone.getData2();
            try {
                wi = dc.addWasteItem(wr, wi);
                obOut.writeObject(ConnectionData.create("SUCC", wi));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("REMOVEWASTEITEM")
    private void removeWasteItem(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data value removing a waste item");
                obOut.writeObject(ConnectionData.create("FAIL", "Unexpected data value"));
            }
            int id = (int) data.getData();
            try {
                dc.removeWasteItem(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETWASTEITEM")
    private void getWasteItem(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type received getting waste items");
                obOut.writeObject(ConnectionData.create("FAIL", "Unexpected data type received"));
                return;
            }
            int id = (int) clone.getData();
            try {
                WasteItem wi = dc.getWasteItem(id);
                obOut.writeObject(ConnectionData.create("SUCC", wi));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETALLWASTEITEMS")
    private void getAllWasteItems() {
        try {
            try {
                List<WasteItem> wis = dc.getAllWasteItems();
                obOut.writeObject(ConnectionData.create("SUCC", wis));
            } catch (SQLException | IOException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("UPDATEWASTEITEM")
    private void updateWasteItem(@JConnValue(WasteItem.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof WasteItem)) {
                LOG.log(Level.SEVERE, "Unexpected data type updating a waste item");
                obOut.writeObject(ConnectionData.create("FAIL", "Unexpected data type"));
                return;
            }
            WasteItem wi = (WasteItem) clone.getData();
            try {
                wi = dc.updateWasteItem(wi);
                obOut.writeObject(ConnectionData.create("SUCC", wi));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("ADDWASTEREASON")
    private void addWasteReason(@JConnValue(WasteReason.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof WasteReason)) {
                LOG.log(Level.SEVERE, "Unexpected data type adding a waste reaspm");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            WasteReason wr = (WasteReason) clone.getData();
            try {
                wr = dc.addWasteReason(wr);
                obOut.writeObject(ConnectionData.create("SUCC", wr));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("REMOVEWASTEREASON")
    private void removeWasteReason(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data value removing a waste reason");
                obOut.writeObject(ConnectionData.create("FAIL", "Unexpected data value"));
            }
            int id = (int) data.getData();
            try {
                dc.removeWasteReason(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETWASTEREASON")
    private void getWasteReason(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type received getting waste reason");
                obOut.writeObject(ConnectionData.create("FAIL", "Unexpected data type received"));
                return;
            }
            int id = (int) clone.getData();
            try {
                WasteReason wr = dc.getWasteReason(id);
                obOut.writeObject(ConnectionData.create("SUCC", wr));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETALLWASTEREASONS")
    private void getAllWasteReasons() {
        try {
            try {
                List<WasteReason> wrs = dc.getAllWasteReasons();
                obOut.writeObject(ConnectionData.create("SUCC", wrs));
            } catch (SQLException | IOException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("UPDATEWASTEREASON")
    private void updateWasteReason(@JConnValue(WasteItem.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof WasteItem)) {
                LOG.log(Level.SEVERE, "Unexpected data type updating a waste reason");
                obOut.writeObject(ConnectionData.create("FAIL", "Unexpected data type"));
                return;
            }
            WasteReason wr = (WasteReason) clone.getData();
            try {
                wr = dc.updateWasteReason(wr);
                obOut.writeObject(ConnectionData.create("SUCC", wr));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("ADDSUPPLIER")
    private void addSupplier(@JConnValue(Supplier.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Supplier)) {
                LOG.log(Level.SEVERE, "Unexpected data type adding a supplier");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            Supplier s = (Supplier) clone.getData();
            try {
                s = dc.addSupplier(s);
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("REMOVESUPPLIER")
    private void removeSupplier(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type removing a supplier");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) clone.getData();
            try {
                dc.removeSupplier(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETSUPPLIER")
    private void getSupplier(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type getting a supplier");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) clone.getData();
            try {
                Supplier s = dc.getSupplier(id);
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETALLSUPPLIERS")
    private void getAllSuppliers() {
        try {
            try {
                List<Supplier> s = dc.getAllSuppliers();
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (IOException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("UPDATESUPPLIER")
    private void updateSupplier(@JConnValue(Supplier.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Supplier)) {
                LOG.log(Level.SEVERE, "Unexpected data type updating a supplier");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            Supplier s = (Supplier) clone.getData();
            try {
                s = dc.updateSupplier(s);
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("ADDDEPARTMENT")
    private void addDepartment(@JConnValue(Department.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Department)) {
                LOG.log(Level.SEVERE, "Unexpected data type adding a department");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            Department d = (Department) clone.getData();
            try {
                d = dc.addDepartment(d);
                obOut.writeObject(ConnectionData.create("SUCC", d));
            } catch (IOException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("REMOVEDEPARTMENT")
    private void removeDepartment(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type removing a department");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) clone.getData();
            try {
                dc.removeDepartment(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETDEPARTMENT")
    private void getDepartment(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type getting a department");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) clone.getData();
            try {
                Department d = dc.getDepartment(id);
                obOut.writeObject(ConnectionData.create("SUCC", d));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETALLDEPARTMENTS")
    private void getAllDepartments() {
        try {
            try {
                List<Department> d = dc.getAllDepartments();
                obOut.writeObject(ConnectionData.create("SUCC", d));
            } catch (IOException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("UPDATEDEPARTMENT")
    private void updateDepartment(@JConnValue(Department.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Department)) {
                LOG.log(Level.SEVERE, "Unexpected data type updating a department");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            Department d = (Department) clone.getData();
            try {
                d = dc.updateDepartment(d);
                obOut.writeObject(ConnectionData.create("SUCC", d));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("ADDSALEITEM")
    private void addSaleItem(@JConnValue(Sale.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Sale)) {
                LOG.log(Level.SEVERE, "Unexpected data type adding a saleitem");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            if (!(clone.getData2() instanceof SaleItem)) {
                LOG.log(Level.SEVERE, "Unexpected data type adding a saleitem");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            Sale s = (Sale) clone.getData();
            SaleItem i = (SaleItem) clone.getData2();
            try {
                i = dc.addSaleItem(s, i);
                obOut.writeObject(ConnectionData.create("SUCC", i));
            } catch (IOException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("REMOVESALEITEM")
    private void removeSaleItem(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type removing a saleitem");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) clone.getData();
            try {
                dc.removeSaleItem(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETSALEITEM")
    private void getSaleItem(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type getting a saleitem");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) clone.getData();
            try {
                SaleItem i = dc.getSaleItem(id);
                obOut.writeObject(ConnectionData.create("SUCC", i));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETALLSALEITEMS")
    private void getAllSaleItems() {
        try {
            try {
                List<SaleItem> i = dc.getAllSaleItems();
                obOut.writeObject(ConnectionData.create("SUCC", i));
            } catch (IOException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void subSaleItemQuery(@JConnValue(String.class) ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                String q = (String) clone.getData();
                List<SaleItem> i = dc.submitSaleItemQuery(q);
                obOut.writeObject(ConnectionData.create("SUCC", i));
            } catch (IOException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("UPDATESALEITEM")
    private void updateSaleItem(@JConnValue(SaleItem.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof SaleItem)) {
                LOG.log(Level.SEVERE, "Unexpected data type updating a department");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            SaleItem i = (SaleItem) clone.getData();
            try {
                i = dc.updateSaleItem(i);
                obOut.writeObject(ConnectionData.create("SUCC", i));
            } catch (IOException | SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETTOTALSOLDITEM")
    private void getTotalSoldItem(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type getting items");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) data.getData();
            try {
                int value = dc.getTotalSoldOfItem(id);
                obOut.writeObject(ConnectionData.create("SUCC", value));
            } catch (SQLException | ProductNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETVALUESOLDITEM")
    private void getValueSoldItem(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type getting items");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) data.getData();
            try {
                BigDecimal value = dc.getTotalValueSold(id);
                obOut.writeObject(ConnectionData.create("SUCC", value));
            } catch (SQLException | ProductNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETTOTALWASTEDITEM")
    private void getTotalWastedItem(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type getting items");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) data.getData();
            try {
                int value = dc.getTotalWastedOfItem(id);
                obOut.writeObject(ConnectionData.create("SUCC", value));
            } catch (SQLException | ProductNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETVALUEWASTEDITEM")
    private void getValueWastedItem(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type getting items");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) data.getData();
            try {
                BigDecimal value = dc.getValueWastedOfItem(id);
                obOut.writeObject(ConnectionData.create("SUCC", value));
            } catch (SQLException | ProductNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("ADDRECEIVEDITEM")
    private void addReceivedItem(@JConnValue(ReceivedItem.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof ReceivedItem)) {
                LOG.log(Level.SEVERE, "Unexpected data type receiving item");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            ReceivedItem i = (ReceivedItem) data.getData();
            try {
                dc.addReceivedItem(i);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETSPENTONITEM")
    private void getValueSpentOnItem(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type getting item");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) data.getData();
            try {
                BigDecimal val = dc.getValueSpentOnItem(id);
                obOut.writeObject(ConnectionData.create("SUCC", val));
            } catch (ProductNotFoundException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("CLOCKON")
    private void clockOn(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type clocking on");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) data.getData();
            try {
                dc.clockOn(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (StaffNotFoundException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("CLOCKOFF")
    private void clockOff(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type clocking off");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) data.getData();
            try {
                dc.clockOff(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (StaffNotFoundException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETALLCLOCKS")
    private void getAllClocks(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type getting clocks");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) data.getData();
            try {
                List<ClockItem> items = dc.getAllClocks(id);
                obOut.writeObject(ConnectionData.create("SUCC", items));
            } catch (StaffNotFoundException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("CLEARCLOCKS")
    private void clearClocks(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type clearing clocks");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) data.getData();
            try {
                dc.clearClocks(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (StaffNotFoundException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("ADDTRIGGER")
    private void addTrigger(@JConnValue(Trigger.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Trigger)) {
                LOG.log(Level.SEVERE, "Unexpected data type adding trigger");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            Trigger t = (Trigger) data.getData();
            try {
                t = dc.addTrigger(t);
                obOut.writeObject(ConnectionData.create("SUCC", t));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETDISCOUNTBUCKETS")
    private void getDiscountBuckets(@JConnValue(Trigger.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type getting discount buckets");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) data.getData();
            try {
                List<DiscountBucket> buckets = dc.getDiscountBuckets(id);
                obOut.writeObject(ConnectionData.create("SUCC", buckets));
            } catch (DiscountNotFoundException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("REMOVETRIGGER")
    private void removeTrigger(@JConnValue(Trigger.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type removing a trigger");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) data.getData();
            try {
                dc.removeTrigger(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (JTillException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETVALIDDISCOUNTS")
    private void getValidDiscounts() {
        try {
            try {
                List<Discount> discounts = dc.getValidDiscounts();
                obOut.writeObject(ConnectionData.create("SUCC", discounts));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("ADDBUCKET")
    private void addBucket(@JConnValue(DiscountBucket.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof DiscountBucket)) {
                LOG.log(Level.SEVERE, "Unexpected data type adding a bucket");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            DiscountBucket b = (DiscountBucket) data.getData();
            try {
                b = dc.addBucket(b);
                obOut.writeObject(ConnectionData.create("SUCC", b));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("REMOVEBUCKET")
    private void removeBucket(@JConnValue(DiscountBucket.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof DiscountBucket)) {
                LOG.log(Level.SEVERE, "Unexpected data type removing a bucket");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) data.getData();
            try {
                dc.removeBucket(id);
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (JTillException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETBUCKETTRIGGERS")
    private void getBucketTriggers(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type getting bucket triggers");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) data.getData();
            try {
                List<Trigger> triggers = dc.getBucketTriggers(id);
                obOut.writeObject(ConnectionData.create("SUCC", triggers));
            } catch (JTillException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("UPDATETRIGGER")
    private void updateTrigger(@JConnValue(Trigger.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Trigger)) {
                LOG.log(Level.SEVERE, "Unexpected data type updating bucket triggers");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            Trigger t = (Trigger) data.getData();
            try {
                t = dc.updateTrigger(t);
                obOut.writeObject(ConnectionData.create("SUCC", t));
            } catch (JTillException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("UPDATEBUCKET")
    private void updateBucket(@JConnValue(DiscountBucket.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof DiscountBucket)) {
                LOG.log(Level.SEVERE, "Unexpected data type updating bucket");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            DiscountBucket b = (DiscountBucket) data.getData();
            try {
                b = dc.updateBucket(b);
                obOut.writeObject(ConnectionData.create("SUCC", b));
            } catch (JTillException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETUNCASHEDTERMINALSALES")
    private void getUncashedTerminalSales(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type getting uncashed terminal sales");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) data.getData();
            try {
                List<Sale> sales = dc.getUncachedTillSales(id);
                obOut.writeObject(ConnectionData.create("SUCC", sales));
            } catch (JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("ADDPRODUCTANDPLU")
    private void addProductAndPlu(@JConnValue(Product.class) @JConnValue2(Plu.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Product) || !(clone.getData2() instanceof Plu)) {
                LOG.log(Level.SEVERE, "Unexpected data type adding new plu and product");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            Product p = (Product) data.getData();
            Plu pl = (Plu) data.getData2();
            try {
                p = dc.addProductAndPlu(p, pl);
                obOut.writeObject(ConnectionData.create("SUCC", p));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("GETPRODUCTANDPLU")
    private void getPluByProduct(@JConnValue(Integer.class) ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof Integer)) {
                LOG.log(Level.SEVERE, "Unexpected data type getting plu");
                obOut.writeObject(ConnectionData.create("FAIL", "Invalid data type received"));
                return;
            }
            int id = (int) data.getData();
            try {
                Plu p = dc.getPluByProduct(id);
                obOut.writeObject(ConnectionData.create("SUCC", p));
            } catch (JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("SEARCHSALEITEMS")
    private void searchSaleItems(@JConnValue(Object[].class) ConnectionData data) {
        try {
            final Object[] object = (Object[]) data.getData();
            final int department = (int) object[0];
            final int category = (int) object[1];
            final Date start = (Date) object[2];
            final Date end = (Date) object[3];
            try {
                final List<SaleItem> saleItems = dc.searchSaleItems(department, category, start, end);
                obOut.writeObject(ConnectionData.create("SUCC", saleItems));
            } catch (SQLException | JTillException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @JConnMethod("CONNTERM")
    private void terminateConnection() {
        conn_term = true;
        if (staff != null) {
            try {
                dc.logout(staff);
                dc.tillLogout(staff);
                LOG.log(Level.INFO, till.getName() + " has terminated their connection to the server");
            } catch (StaffNotFoundException | IOException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
