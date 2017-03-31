/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;
import java.util.List;
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

    private final DataConnect dc;

    private ObjectInputStream obIn;
    private ObjectOutputStream obOut;

    private final Socket socket;

    private boolean conn_term = false;

    private String site;
    public Staff staff;
    public Till till;

    private ConnectionData currentData;

    private final Semaphore sem;

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

    public void sendLog(String message) throws IOException {
        try {
            sem.acquire();
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        obOut.writeObject(new ConnectionData("LOG", message));
        sem.release();
    }

    @Override
    public void run() {
        try {
            obIn = new ObjectInputStream(socket.getInputStream());
            obOut = new ObjectOutputStream(socket.getOutputStream());
            obOut.flush();

            site = (String) obIn.readObject();

            if (!site.equals("NOPERM")) {

                till = dc.connectTill(site);

                if (till == null) {
                    obOut.writeObject(ConnectionData.create("DISALLOW"));
                    conn_term = true;
                } else {
                    obOut.writeObject(ConnectionData.create("ALLOW"));
                }
            }

            LOG.log(Level.INFO, "{0} has connected", site);

            while (!conn_term) {
                String input;

                Object o;

                try {
                    o = obIn.readObject();

                    till.setLastContact(new Date());
                } catch (SocketException ex) {
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

                String inp[] = input.split(",");
                final ConnectionData data = currentData.clone();

                LOG.log(Level.INFO, "Received {0} from server", data.getFlag());

                switch (inp[0]) {
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
                    case "GETPRODUCTSDISCOUNT": { //Gets all discounts for a product
                        new Thread(inp[0]) {
                            @Override
                            public void run() {
                                getProductsDiscount(data);
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
                    case "CONNTERM": { //Terminate the connection
                        conn_term = true;
                        if (staff != null) {
                            try {
                                dc.logout(staff);
                                dc.tillLogout(staff);
                                LOG.log(Level.INFO, "{0} has terminated their connection to the server", site);
                            } catch (StaffNotFoundException ex) {
                            }
                        }
                        break;
                    }
                    default: {
                        LOG.log(Level.WARNING, "An unknown flag {0} was received from {1}", new Object[]{inp[0], site});
                        break;
                    }
                }
                sem.release();
            }
            LOG.log(Level.INFO, "{0} has disconnected", site);
        } catch (IOException | ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, "There was an error with the conenction to " + site + ". The connection will be forecfully terminated", ex);
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

    private void newProduct(ConnectionData data) {
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

    private void removeProduct(ConnectionData data) {
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

    private void purchase(ConnectionData data) {
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

    private void getProduct(ConnectionData data) {
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

    private void updateProduct(ConnectionData data) {
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

    private void getProductByBarcode(ConnectionData data) {
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

    private void checkBarcode(ConnectionData data) {
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

    private void getProductsDiscount(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Product)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Product must be received here"));
                    return;
                }
                Product p = (Product) clone.getData();
                List<Discount> discounts = dc.getProductsDiscount(p);
                obOut.writeObject(ConnectionData.create("SUCC", discounts));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

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

    private void productLookup(ConnectionData data) {
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

    private void newCustomer(ConnectionData data) {
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

    private void removeCustomer(ConnectionData data) {
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

    private void getCustomer(ConnectionData data) {
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

    private void getCustomerByName(ConnectionData data) {
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

    private void updateCustomer(ConnectionData data) {
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

    private void customerLookup(ConnectionData data) {
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

    private void addStaff(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Staff)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Staff must be received here"));
                    return;
                }
                Staff s = (Staff) clone.getData();
                Staff newS = dc.addStaff(s);
                obOut.writeObject(ConnectionData.create("SUCC", newS));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void removeStaff(ConnectionData data) {
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

    private void getStaff(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Integer)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "An Integer must be received here"));
                    return;
                }
                int id = (int) clone.getData();
                Staff s = dc.getStaff(id);
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (StaffNotFoundException | SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void updateStaff(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Staff)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Staff must be received here"));
                    return;
                }
                Staff s = (Staff) clone.getData();
                Staff updatedStaff = dc.updateStaff(s);
                obOut.writeObject(ConnectionData.create("SUCC", updatedStaff));
            } catch (SQLException | StaffNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void getAllStaff() {
        try {
            try {
                List<Staff> staffList = dc.getAllStaff();
                obOut.writeObject(staffList);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

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

    private void addSale(ConnectionData data) {
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

    private void getSale(ConnectionData data) {
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
            } catch (SQLException | SaleNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void updateSale(ConnectionData data) {
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
            } catch (SQLException | SaleNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void getSaleDateRange(ConnectionData data) {
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

    private void suspendSale(ConnectionData data) {
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

    private void resumeSale(ConnectionData data) {
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

    private void login(ConnectionData data) {
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
                Staff s = dc.login(username, password);
                ConnectionThread.this.staff = s;
                LOG.log(Level.INFO, "{0} has logged in", s.getName());
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (SQLException | LoginException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void tillLogin(ConnectionData data) {
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
                LOG.log(Level.INFO, "{0} has logged in from {1}", new Object[]{staff.getName(), site});
                obOut.writeObject(ConnectionData.create("SUCC", s));
            } catch (SQLException | LoginException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void logout(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Staff)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Staff must be received here"));
                    return;
                }
                Staff s = (Staff) clone.getData();
                dc.logout(s);
                LOG.log(Level.INFO, "{0} has logged out", staff.getName());
                ConnectionThread.this.staff = null;
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (StaffNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void tillLogout(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof Staff)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A Staff must be received here"));
                    return;
                }
                Staff s = (Staff) clone.getData();
                dc.tillLogout(s);
                LOG.log(Level.INFO, "{0} has logged out", staff.getName());
                ConnectionThread.this.staff = null;
                obOut.writeObject(ConnectionData.create("SUCC"));
            } catch (StaffNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void addCategory(ConnectionData data) {
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

    private void updateCategory(ConnectionData data) {
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
            } catch (SQLException | CategoryNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void removeCategory(ConnectionData data) {
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
            } catch (SQLException | CategoryNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void getCategory(ConnectionData data) {
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
            } catch (SQLException | CategoryNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

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

    private void getProductsInCategory(ConnectionData data) {
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
            } catch (SQLException | CategoryNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void addDiscount(ConnectionData data) {
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

    private void updateDiscount(ConnectionData data) {
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

    private void removeDiscount(ConnectionData data) {
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

    private void getDiscount(ConnectionData data) {
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

    private void addTax(ConnectionData data) {
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

    private void removeTax(ConnectionData data) {
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
            } catch (SQLException | TaxNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void getTax(ConnectionData data) {
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
            } catch (SQLException | TaxNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void updateTax(ConnectionData data) {
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
            } catch (SQLException | TaxNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

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

    private void getProductsInTax(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                int id = (int) clone.getData();
                List<Product> products = dc.getProductsInTax(id);
                obOut.writeObject(ConnectionData.create("SUCC", products));
            } catch (SQLException | TaxNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void addScreen(ConnectionData data) {
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

    private void addButton(ConnectionData data) {
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

    private void removeScreen(ConnectionData data) {
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

    private void removeButton(ConnectionData data) {
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
            } catch (SQLException | ButtonNotFoundException ex) {
                ConnectionData.create("FAIL", ex);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void updateScreen(ConnectionData data) {
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

    private void updateButton(ConnectionData data) {
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
            } catch (SQLException | ButtonNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void getScreen(ConnectionData data) {
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

    private void getButton(ConnectionData data) {
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
            } catch (SQLException | ButtonNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private void getAllScreens() {
        try {
            try {
                List<Screen> screens = dc.getAllScreens();
                obOut.writeObject(screens);
            } catch (SQLException ex) {
                obOut.writeObject(ex);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

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

    private void getButtonsOnScreen(ConnectionData data) {
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

    private void assisstance(ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof String)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                return;
            }
            String message = (String) clone.getData();
            dc.assisstance(staff.getName() + " on terminal " + site + " has requested assistance with message:\n" + message);
            obOut.writeObject(ConnectionData.create("SUCC"));

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void getTakings(ConnectionData data) {
        try {
            try {
                ConnectionData clone = data.clone();
                if (!(clone.getData() instanceof String)) {
                    obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                    return;
                }
                String terminal = (String) clone.getData();
                BigDecimal t = dc.getTillTakings(terminal);
                obOut.writeObject(ConnectionData.create("GET", t));
            } catch (SQLException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));

            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void getUncashedSales(ConnectionData data) {
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

    private void sendEmail(ConnectionData data) {
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

    private void sendReceipt(ConnectionData data) {
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

    private void addTill(ConnectionData data) {
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

    private void removeTill(ConnectionData data) {
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
            } catch (SQLException | TillNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));

            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void getTill(ConnectionData data) {
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
            } catch (SQLException | TillNotFoundException ex) {
                obOut.writeObject(ConnectionData.create("FAIL", ex));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

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

    private void connectTill(ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof String)) {
                obOut.writeObject(ConnectionData.create("FAIL", "A String must be received here"));
                return;
            }
            String t = (String) clone.getData();
            Till ti = dc.connectTill(t);
            obOut.writeObject(ConnectionData.create("CONNECT", ti));

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void disconnectTill(ConnectionData data) {
        ConnectionData clone = data.clone();
        Till t = (Till) clone.getData();
        dc.disconnectTill(t);
    }

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

    private void setSetting(ConnectionData data) {
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

    private void getSetting(ConnectionData data) {
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

    private void getSettingDefault(ConnectionData data) {
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

    private void addPlu(ConnectionData data) {
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

    private void removePlu(ConnectionData data) {
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

    private void getPlu(ConnectionData data) {
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

    private void getPluByCode(ConnectionData data) {
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

    private void updatePlu(ConnectionData data) {
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

    private void isTillLoggedIn(ConnectionData data) {
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

    private void checkUsername(ConnectionData data) {
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

    private void addWasteReport(ConnectionData data) {
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

    private void removeWasteReport(ConnectionData data) {
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

    private void getWasteReport(ConnectionData data) {
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

    private void updateWasteReport(ConnectionData data) {
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

    private void addWasteItem(ConnectionData data) {
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

    private void removeWasteItem(ConnectionData data) {
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

    private void getWasteItem(ConnectionData data) {
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

    private void updateWasteItem(ConnectionData data) {
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

    private void addWasteReason(ConnectionData data) {
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

    private void removeWasteReason(ConnectionData data) {
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

    private void getWasteReason(ConnectionData data) {
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

    private void updateWasteReason(ConnectionData data) {
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

    private void addSupplier(ConnectionData data) {
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

    private void removeSupplier(ConnectionData data) {
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

    private void getSupplier(ConnectionData data) {
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

    private void updateSupplier(ConnectionData data) {
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

    private void addDepartment(ConnectionData data) {
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

    private void removeDepartment(ConnectionData data) {
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

    private void getDepartment(ConnectionData data) {
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

    private void updateDepartment(ConnectionData data) {
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

    private void addSaleItem(ConnectionData data) {
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

    private void removeSaleItem(ConnectionData data) {
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

    private void getSaleItem(ConnectionData data) {
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

    private void subSaleItemQuery(ConnectionData data) {
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

    private void updateSaleItem(ConnectionData data) {
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

    private void getTotalSoldItem(ConnectionData data) {
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

    private void getValueSoldItem(ConnectionData data) {
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

    private void getTotalWastedItem(ConnectionData data) {
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

    private void getValueWastedItem(ConnectionData data) {
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

    private void addReceivedItem(ConnectionData data) {
        try {
            ConnectionData clone = data.clone();
            if (!(clone.getData() instanceof ReceivedItem)) {
                LOG.log(Level.SEVERE, "Unexpected data type receiving items");
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
}
