/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import io.github.davidg95.jconn.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.StampedLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;
import org.apache.derby.jdbc.EmbeddedDriver;

/**
 * Database connection class which handles communication with the database.
 *
 * @author David
 */
public class DBConnect implements DataConnect {

    private static final Logger LOG = Logger.getGlobal();

    /**
     * The static reference to the DBConnect object.
     */
    private static final DBConnect CONNECTION;

    /**
     * The database driver.
     */
    private Driver embedded;

    //Database credentials
    public String address; //The database address.
    public String username; //the database username.
    public String password; //The database password.

    private boolean connected; //Connection flag

    //Concurrent locks
    private final StampedLock susL;
    private final StampedLock supL;
    private final StampedLock productLock;

    private GUIInterface g; //A reference to the GUI.

    private volatile HashMap<Staff, Sale> suspendedSales; //A hash map of suspended sales.
    private final Settings systemSettings; //The system settings.

    private final List<Staff> loggedIn; //A list of logged in staff.
    private final Semaphore loggedInSem; //Semaphore for the list of logged in staff.

    private final LogFileHandler handler; //Handler object for the logger.

    private final ObservableList<Till> connectedTills; //List of connected tills.

    private final List<Integer> clockedOn;
    private final StampedLock clockLock;

    private JConnServer server;

    static {
        CONNECTION = new DBConnect();
    }

    /**
     * Returns an instance of the DBConnect object.
     *
     * @return the DBConnect object.
     */
    public static DBConnect getInstance() {
        return CONNECTION;
    }

    /**
     * Constructor which initialises the concurrent locks.
     */
    public DBConnect() {
        susL = new StampedLock();
        supL = new StampedLock();
        suspendedSales = new HashMap<>();
        systemSettings = Settings.getInstance();
        loggedIn = new LinkedList<>();
        loggedInSem = new Semaphore(1);
        connectedTills = FXCollections.observableArrayList();
        connectedTills.addListener((ListChangeListener.Change<? extends Till> c) -> {
            g.updateTills();
        });
        handler = LogFileHandler.getInstance();
        Logger.getGlobal().addHandler(handler);
        clockedOn = new LinkedList<>();
        clockLock = new StampedLock();
        productLock = new StampedLock();
    }

    public void setServer(JConnServer server) {
        this.server = server;
    }

    /**
     * Method to make a new connection with the database.
     *
     * @param database_address the url of the database.
     * @param username username to log on to the database.
     * @param password password to log on to the database.
     * @throws SQLException if there was a log on error.
     */
    public void connect(String database_address, String username, String password) throws SQLException {
        LOG.log(Level.INFO, "Connecting to database " + database_address);
        this.address = database_address;
        this.username = username;
        this.password = password;
        connected = true;
        updates();
    }

    private void updates() {
        try (final Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                int res = stmt.executeUpdate("ALTER TABLE STAFF ADD ENABLED BOOLEAN");
                LOG.log(Level.INFO, "New fields added to staff table, " + res + " rows affected");
                con.commit();
                try {
                    Statement stmt2 = con.createStatement();
                    int res2 = stmt2.executeUpdate("UPDATE STAFF SET STAFF.ENABLED = TRUE");
                    LOG.log(Level.INFO, "Staff enabled fields set to TRUE, " + res2 + " rows affected");
                    con.commit();
                } catch (SQLException ex) {
                    con.rollback();
                    throw ex;
                }
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            LOG.log(Level.INFO, "Column ENABLED already exists in STAFF, no need to change database.", ex);
        }

        try (final Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                String received_reports = "create table APP.RECEIVED_REPORTS\n"
                        + "(\n"
                        + "     ID INT not null primary key\n"
                        + "         GENERATED ALWAYS AS IDENTITY\n"
                        + "         (START WITH 1, INCREMENT BY 1),"
                        + "     INVOICE_NO VARCHAR(30) not null,\n"
                        + "     SUPPLIER_ID INT not null references SUPPLIERS(ID)\n"
                        + ")";
                boolean res = stmt.execute(received_reports);
                if (res) {
                    LOG.log(Level.INFO, "New table RECEIVED_REPORTS created");
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            LOG.log(Level.INFO, "Table RECEIVED_REPORTS already exists, no need to change database.", ex);
        }

        try (final Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                int res = stmt.executeUpdate("ALTER TABLE RECEIVEDITEMS ADD RECEIVED_REPORT INT");
                LOG.log(Level.INFO, "New fields added to RECEIVEDITEMS table, " + res + " rows affected");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            LOG.log(Level.INFO, "Column RECEIVED_REPORT already exists in RECEIVEDITEMS, no need to change database.", ex);
        }

        try (final Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                int res = stmt.executeUpdate("ALTER TABLE RECEIVED_REPORTS ADD PAID BOOLEAN");
                LOG.log(Level.INFO, "New fields added to RECEIVED_REPORTS table, " + res + " rows affected");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            LOG.log(Level.INFO, "Column PAID already exists in RECEIVED_REPORTS, no need to change database.", ex);
        }
    }

    /**
     * Returns a new connection to the database with auto commit disabled.
     *
     * @return new Connection.
     * @throws SQLException if there was an error getting the connection.
     */
    public Connection getNewConnection() throws SQLException {
        final Connection conn = DriverManager.getConnection(address, username, password);
        conn.setAutoCommit(false);
        connected = true;
        return conn;
    }

    @Override
    public HashMap integrityCheck() throws SQLException {
        final String query = "SELECT schemaname, tablename,\n"
                + "SYSCS_UTIL.SYSCS_CHECK_TABLE(schemaname, tablename)\n"
                + "FROM sys.sysschemas s, sys.systables t\n"
                + "WHERE s.schemaid = t.schemaid";
        try (final Connection con = getNewConnection()) {
            final Statement stmt = con.createStatement();
            try {
                final ResultSet set = stmt.executeQuery(query); //Execute the check
                final HashMap<String, HashMap> map = new HashMap();
                while (set.next()) {

                }
                con.commit();
                return map;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /**
     * Method which creates the database.
     *
     * @param address the database address.
     * @param username the database username.
     * @param password the database password.
     * @throws SQLException if there was a creation error.
     */
    public void create(String address, String username, String password) throws SQLException {
        LOG.log(Level.INFO, "The database does not exists, so it is getting created");
        embedded = new EmbeddedDriver();
        TillSplashScreen.setLabel("Registering database driver");
        DriverManager.registerDriver(embedded);
        TillSplashScreen.addBar(10);
        this.address = address;
        this.username = username;
        this.password = password;
        connected = true;
        TillSplashScreen.setLabel("Creating tables");
        createTables();
    }

    /**
     * Create the database tables.
     *
     * @throws SQLException if there was an error in creating any tables.
     */
    private void createTables() throws SQLException {
        LOG.log(Level.INFO, "Creating tables");
        String tills = "create table APP.TILLS\n"
                + "(\n"
                + "	ID INT not null primary key\n"
                + "        GENERATED ALWAYS AS IDENTITY\n"
                + "        (START WITH 1, INCREMENT BY 1),\n"
                + "     UUID VARCHAR(50) not null,\n"
                + "	NAME VARCHAR(20) not null,\n"
                + "     UNCASHED DOUBLE not null,\n"
                + "     DEFAULT_SCREEN INT not null\n"
                + ")";
        String categorys = "create table APP.CATEGORYS\n"
                + "(\n"
                + "	ID INT not null primary key\n"
                + "        GENERATED ALWAYS AS IDENTITY\n"
                + "        (START WITH 1, INCREMENT BY 1),\n"
                + "	NAME VARCHAR(20) not null,\n"
                + "     SELL_START TIME,\n"
                + "     SELL_END TIME,\n"
                + "     TIME_RESTRICT BOOLEAN not null,\n"
                + "     MINIMUM_AGE INT not null\n"
                + ")";
        String departments = "create table APP.DEPARTMENTS\n"
                + "(\n"
                + "     ID INT not null primary key\n"
                + "         GENERATED ALWAYS AS IDENTITY\n"
                + "         (START WITH 1, INCREMENT BY 1),"
                + "     NAME VARCHAR(30) not null\n"
                + ")";
        String tax = "create table \"APP\".TAX\n"
                + "(\n"
                + "	ID INT not null primary key\n"
                + "        GENERATED ALWAYS AS IDENTITY\n"
                + "        (START WITH 1, INCREMENT BY 1),\n"
                + "	NAME VARCHAR(20) not null,\n"
                + "	VALUE DOUBLE not null\n"
                + ")";
        String configs = "create table APP.CONFIGS\n"
                + "(\n"
                + "	NAME INT not null primary key\n"
                + "        GENERATED ALWAYS AS IDENTITY\n"
                + "        (START WITH 1, INCREMENT BY 1),\n"
                + "	VALUE VARCHAR(20) not null\n"
                + ")";
        String sales = "create table APP.SALES\n"
                + "(\n"
                + "     ID INT not null primary key\n"
                + "        GENERATED ALWAYS AS IDENTITY\n"
                + "        (START WITH 1, INCREMENT BY 1),\n"
                + "     PRICE DOUBLE,\n"
                + "     CUSTOMER int,\n"
                + "     TIMESTAMP bigint,\n"
                + "     TERMINAL int not null references TILLS(ID),\n"
                + "     CASHED boolean not null,\n"
                + "     STAFF int,\n"
                + "     MOP int\n"
                + ")";
        String saleItems = "create table APP.SALEITEMS\n"
                + "(\n"
                + "     ID INT not null primary key\n"
                + "        GENERATED ALWAYS AS IDENTITY\n"
                + "        (START WITH 1, INCREMENT BY 1),\n"
                + "     PRODUCT_ID INT not null references PRODUCTS(ID),\n"
                + "	TYPE INT,\n"
                + "     QUANTITY INT not null,\n"
                + "     PRICE double not null,\n"
                + "     TAX double not null,\n"
                + "     SALE_ID INT not null references SALES(ID)\n"
                + ")";
        String customers = "create table \"APP\".CUSTOMERS\n"
                + "(\n"
                + "	ID INT not null primary key\n"
                + "        GENERATED ALWAYS AS IDENTITY\n"
                + "        (START WITH 1, INCREMENT BY 1),\n"
                + "	NAME VARCHAR(200) not null,\n"
                + "	PHONE VARCHAR(200),\n"
                + "	MOBILE VARCHAR(200),\n"
                + "	EMAIL VARCHAR(200),\n"
                + "	ADDRESS_LINE_1 VARCHAR(300),\n"
                + "	ADDRESS_LINE_2 VARCHAR(300),\n"
                + "	TOWN VARCHAR(200),\n"
                + "	COUNTY VARCHAR(200),\n"
                + "	COUNTRY VARCHAR(200),\n"
                + "	POSTCODE VARCHAR(200),\n"
                + "	NOTES VARCHAR(1000),\n"
                + "	LOYALTY_POINTS INTEGER,\n"
                + "     MONEY_DUE DOUBLE\n"
                + ")";
        String products = "create table \"APP\".PRODUCTS\n"
                + "(\n"
                + "	ID INT not null primary key\n"
                + "        GENERATED ALWAYS AS IDENTITY\n"
                + "        (START WITH 1, INCREMENT BY 1),\n"
                + "     ORDER_CODE INTEGER,\n"
                + "	NAME VARCHAR(50) not null,\n"
                + "     OPEN_PRICE BOOLEAN not null,\n"
                + "	PRICE DOUBLE,\n"
                + "	STOCK INTEGER,\n"
                + "	COMMENTS VARCHAR(200),\n"
                + "	SHORT_NAME VARCHAR(50) not null,\n"
                + "	CATEGORY_ID INT not null references CATEGORYS(ID),\n"
                + "     DEPARTMENT_ID INT not null references DEPARTMENTS(ID),\n"
                + "	TAX_ID INT not null references TAX(ID),\n"
                + "	COST_PRICE DOUBLE,\n"
                + "	MIN_PRODUCT_LEVEL INTEGER,\n"
                + "	MAX_PRODUCT_LEVEL INTEGER\n"
                + ")";
        String plus = "create table APP.PLUS\n"
                + "(\n"
                + "	ID INT not null primary key\n"
                + "        GENERATED ALWAYS AS IDENTITY\n"
                + "        (START WITH 1, INCREMENT BY 1),\n"
                + "     CODE VARCHAR(20),\n"
                + "     PRODUCT INT not null references PRODUCTS(ID)\n"
                + ")";
        String discounts = "create table \"APP\".DISCOUNTS\n"
                + "(\n"
                + "	ID INT not null primary key\n"
                + "        GENERATED ALWAYS AS IDENTITY\n"
                + "        (START WITH 1, INCREMENT BY 1),\n"
                + "	NAME VARCHAR(20) not null,\n"
                + "	PLU INTEGER,\n"
                + "	PERCENTAGE DOUBLE not null,\n"
                + "	PRICE DOUBLE not null,\n"
                + "     ACTION INTEGER,\n"
                + "     CONDITION INTEGER,\n"
                + "     STARTT BIGINT,\n"
                + "     ENDT BIGINT\n"
                + ")";
        String buckets = "create table \"APP\".BUCKETS\n"
                + "(\n"
                + "     ID INT not null primary key\n"
                + "         GENERATED ALWAYS AS IDENTITY\n"
                + "         (START WITH 1, INCREMENT BY 1),\n"
                + "     DISCOUNT INT not null references DISCOUNTS(ID),\n"
                + "     TRIGGERSREQUIRED INT,\n"
                + "     REQUIREDTRIGGER BOOLEAN\n"
                + ")";
        String triggers = "create table \"APP\".TRIGGERS\n"
                + "(\n"
                + "     ID INT not null primary key\n"
                + "         GENERATED ALWAYS AS IDENTITY\n"
                + "         (START WITH 1, INCREMENT BY 1),\n"
                + "     BUCKET INT not null references BUCKETS(ID),\n"
                + "     PRODUCT INT not null references PRODUCTS(ID),\n"
                + "     QUANTITYREQUIRED INT\n"
                + ")";
        String staff = "create table \"APP\".STAFF\n"
                + "(\n"
                + "	ID INT not null primary key\n"
                + "        GENERATED ALWAYS AS IDENTITY\n"
                + "        (START WITH 1, INCREMENT BY 1),\n"
                + "	NAME VARCHAR(50) not null,\n"
                + "	POSITION INTEGER not null,\n"
                + "	USERNAME VARCHAR(20) not null,\n"
                + "	PASSWORD VARCHAR(200) not null,\n"
                + "     ENABLED BOOLEAN,\n"
                + "     WAGE DOUBLE\n"
                + ")";
        String screens = "create table \"APP\".SCREENS\n"
                + "(\n"
                + "     ID INT not null primary key\n"
                + "         GENERATED ALWAYS AS IDENTITY\n"
                + "         (START WITH 1, INCREMENT BY 1),\n"
                + "     NAME VARCHAR(50) not null,\n"
                + "     WIDTH INT not null,\n"
                + "     HEIGHT INT not null\n"
                + ")";
        String buttons = "create table \"APP\".BUTTONS\n"
                + "(\n"
                + "     ID INT not null primary key\n"
                + "         GENERATED ALWAYS AS IDENTITY\n"
                + "         (START WITH 1, INCREMENT BY 1),\n"
                + "     NAME VARCHAR(50) not null,\n"
                + "     PRODUCT INT not null,\n"
                + "     TYPE INT not null,\n"
                + "     COLOR INT,\n"
                + "     WIDTH INT,\n"
                + "     HEIGHT INT,\n"
                + "     XPOS INT,\n"
                + "     YPOS INT,\n"
                + "     SCREEN_ID INT not null references SCREENS(ID)\n"
                + ")";
        String wasteReports = "create table \"APP\".WASTEREPORTS\n"
                + "(\n"
                + "     ID INT not null primary key\n"
                + "         GENERATED ALWAYS AS IDENTITY\n"
                + "         (START WITH 1, INCREMENT BY 1),\n"
                + "     VALUE DOUBLE,\n"
                + "     TIMESTAMP bigint\n"
                + ")";
        String wasteReasons = "create table \"APP\".WASTEREASONS\n"
                + "(\n"
                + "     ID INT not null primary key\n"
                + "         GENERATED ALWAYS AS IDENTITY\n"
                + "         (START WITH 1, INCREMENT BY 1),\n"
                + "     REASON VARCHAR(30)\n"
                + ")";
        String wasteItems = "create table \"APP\".WASTEITEMS\n"
                + "(\n"
                + "     ID INT not null primary key\n"
                + "         GENERATED ALWAYS AS IDENTITY\n"
                + "         (START WITH 1, INCREMENT BY 1),\n"
                + "     REPORT_ID INT not null references WASTEREPORTS(ID),\n"
                + "     PRODUCT INT not null references PRODUCTS(ID),\n"
                + "     QUANTITY INT,\n"
                + "     REASON INT not null references WASTEREASONS(ID)\n"
                + ")";
        String suppliers = "create table \"APP\".SUPPLIERS\n"
                + "(\n"
                + "     ID INT not null primary key\n"
                + "       GENERATED ALWAYS AS IDENTITY\n"
                + "         (START WITH 1, INCREMENT BY 1),\n"
                + "     NAME VARCHAR(30),\n"
                + "     ADDRESS VARCHAR(100),\n"
                + "     PHONE VARCHAR(20)\n"
                + ")";
        String receivedItems = "create table \"APP\".RECEIVEDITEMS\n"
                + "(\n"
                + "     ID INT not null primary key\n"
                + "         GENERATED ALWAYS AS IDENTITY\n"
                + "         (START WITH 1, INCREMENT BY 1),\n"
                + "     PRODUCT INT not null references PRODUCTS(ID),\n"
                + "     PRICE DOUBLE,\n"
                + "     QUANTITY INT\n"
                + ")";
        String clockOnOff = "create table \"APP\".CLOCKONOFF\n"
                + "(\n"
                + "     ID INT not null primary key\n"
                + "         GENERATED ALWAYS AS IDENTITY\n"
                + "         (START WITH 1, INCREMENT BY 1),\n"
                + "     STAFF int not null references STAFF(ID),\n"
                + "     TIMESTAMP BIGINT,\n"
                + "     ONOFF int\n"
                + ")";
        String images = "create table \"APP\".IMAGES\n"
                + "(\n"
                + "     ID INT not null primary key\n"
                + "         GENERATED ALWAYS AS IDENTITY\n"
                + "         (START WITH 1, INCREMENT BY 1),\n"
                + "     NAME VARCHAR(50),\n"
                + "     URL VARCHAR(200)\n"
                + ")";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            try {
                stmt.execute(tills);
                LOG.log(Level.INFO, "Created tills table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(tax);
                LOG.log(Level.INFO, "Created tax table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(categorys);
                LOG.log(Level.INFO, "Created categorys table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(departments);
                LOG.log(Level.INFO, "Created departments table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(configs);
                LOG.log(Level.INFO, "Created configs table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(sales);
                LOG.log(Level.INFO, "Created sales table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(customers);
                LOG.log(Level.INFO, "Created customers table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(products);
                LOG.log(Level.INFO, "Created products table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(plus);
                LOG.log(Level.INFO, "Created plus table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(discounts);
                LOG.log(Level.INFO, "Created discounts table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(buckets);
                LOG.log(Level.INFO, "Created buckets table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(triggers);
                LOG.log(Level.INFO, "Create triggers table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(saleItems);
                LOG.log(Level.INFO, "Created saleItems table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(staff);
                LOG.log(Level.INFO, "Created staff table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(screens);
                LOG.log(Level.INFO, "Created screens table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(buttons);
                LOG.log(Level.INFO, "Created buttons table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(wasteReports);
                LOG.log(Level.INFO, "Created waste reports table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(wasteReasons);
                LOG.log(Level.INFO, "Created table waste reasons");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(wasteItems);
                LOG.log(Level.INFO, "Created table waste items");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(suppliers);
                LOG.log(Level.INFO, "Created table suppliers");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(receivedItems);
                LOG.log(Level.INFO, "Created table recevied items");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(clockOnOff);
                LOG.log(Level.INFO, "Created table clockonoff");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);
            try {
                stmt.execute(images);
                LOG.log(Level.INFO, "Created table images");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                error(ex);
            }
            TillSplashScreen.addBar(2);

            try {
                String addCategory = "INSERT INTO CATEGORYS (NAME, TIME_RESTRICT, MINIMUM_AGE) VALUES ('Default','FALSE',0)";
                String addDepartment = "INSERT INTO DEPARTMENTS (NAME) VALUES ('DEFAULT')";
                String addTax = "INSERT INTO TAX (NAME, VALUE) VALUES ('ZERO',0.0)";
                String addReason = "INSERT INTO WASTEREASONS (REASON) VALUES ('DEFAULT')";
                stmt.executeUpdate(addCategory);
                stmt.executeUpdate(addDepartment);
                stmt.executeUpdate(addTax);
                stmt.executeUpdate(addReason);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
            }
        }
    }

    private void error(SQLException ex) {
        JOptionPane.showMessageDialog(null, ex, "Database error", JOptionPane.ERROR_MESSAGE);
        LOG.log(Level.SEVERE, null, ex);
    }

    public String getAddress() {
        return address;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Method to close the database connection. This will close the data sets
     * and close the connection.
     */
    @Override
    @Deprecated
    public void close() {

    }

    @Override
    public List<Product> getAllProducts() throws SQLException {
        String query = "SELECT ID as pId, ORDER_CODE, p.NAME as pName, OPEN_PRICE, PRICE, STOCK, COMMENTS, SHORT_NAME, CATEGORY_ID, DEPARTMENT_ID, TAX_ID, COST_PRICE, MIN_PRODUCT_LEVEL, MAX_PRODUCT_LEVEL FROM PRODUCTS p";
        List<Product> products;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            try {
                ResultSet set = stmt.executeQuery(query);
                products = new LinkedList<>();
                while (set.next()) {
                    int code = set.getInt("pId");
                    int order_code = set.getInt("ORDER_CODE");
                    String name = set.getString("pName");
                    boolean open = set.getBoolean("OPEN_PRICE");
                    BigDecimal price = new BigDecimal(Double.toString(set.getDouble("PRICE")));
                    int stock = set.getInt("STOCK");
                    String comments = set.getString("COMMENTS");
                    String shortName = set.getString("SHORT_NAME");
                    int cid = set.getInt("CATEGORY_ID");
                    int dId = set.getInt("DEPARTMENT_ID");
                    int taxID = set.getInt("TAX_ID");
                    BigDecimal costPrice = new BigDecimal(Double.toString(set.getDouble("COST_PRICE")));
                    int minStock = set.getInt("MIN_PRODUCT_LEVEL");
                    int maxStock = set.getInt("MAX_PRODUCT_LEVEL");

                    Product p = new Product(name, shortName, order_code, cid, dId, comments, taxID, open, price, costPrice, stock, minStock, maxStock, code);
                    p.setCategory(this.getCategory(p.getCategoryID()));
                    p.setDepartment(this.getDepartment(p.getDepartmentID()));
                    p.setTax(this.getTax(p.getTaxID()));

                    products.add(p);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            } catch (JTillException ex) {
                Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
                throw new SQLException(ex.getMessage());
            }
        }
        return products;
    }

    private List<Product> getProductsFromResultSet(ResultSet set) throws SQLException {
        List<Product> products = new LinkedList<>();
        while (set.next()) {
            int code = set.getInt("pId");
            int order_code = set.getInt("ORDER_CODE");
            String name = set.getString("pName");
            boolean open = set.getBoolean("OPEN_PRICE");
            BigDecimal price = new BigDecimal(Double.toString(set.getDouble("PRICE")));
            int stock = set.getInt("STOCK");
            String comments = set.getString("COMMENTS");
            String shortName = set.getString("SHORT_NAME");
            int categoryID = set.getInt("CATEGORY_ID");
            int dId = set.getInt("DEPARTMENT_ID");
            int taxID = set.getInt("TAX_ID");
            BigDecimal costPrice = new BigDecimal(Double.toString(set.getDouble("COST_PRICE")));
            int minStock = set.getInt("MIN_PRODUCT_LEVEL");
            int maxStock = set.getInt("MAX_PRODUCT_LEVEL");

            Product p = new Product(name, shortName, order_code, categoryID, dId, comments, taxID, open, price, costPrice, stock, minStock, maxStock, code);

            products.add(p);
        }
        return products;
    }

    /**
     * Method to add a new product to the database.
     *
     * @param p the new product to add.
     * @return the product that was added.
     * @throws SQLException if there was an error adding the product to the
     * database.
     */
    @Override
    public Product addProduct(Product p) throws SQLException {
        String query = "INSERT INTO PRODUCTS (ORDER_CODE, NAME, OPEN_PRICE, PRICE, STOCK, COMMENTS, SHORT_NAME, CATEGORY_ID, DEPARTMENT_ID, TAX_ID, COST_PRICE, MIN_PRODUCT_LEVEL, MAX_PRODUCT_LEVEL) VALUES (" + p.getSQLInsertString() + ")";
        try (Connection con = getNewConnection()) {
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                long stamp = productLock.writeLock();
                try {
                    stmt.executeUpdate();
                    ResultSet set = stmt.getGeneratedKeys();
                    while (set.next()) {
                        int id = set.getInt(1);
                        p.setId(id);
                    }
                    con.commit();
                } finally {
                    productLock.unlockWrite(stamp);
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return p;
    }

    @Override
    public Product updateProduct(Product p) throws SQLException, ProductNotFoundException {
        String query = p.getSQlUpdateString();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                long stamp = productLock.writeLock();
                try {
                    value = stmt.executeUpdate(query);
                    if (value == 0) {
                        throw new ProductNotFoundException("Product id " + p.getId() + " could not be found");
                    }
                    con.commit();
                } finally {
                    productLock.unlockWrite(stamp);
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return p;
    }

    /**
     * Method to check if a barcode already exists in the database.
     *
     * @param barcode the barcode to check.
     * @return true or false indicating whether the barcode already exists.
     * @throws SQLException if there was an error checking the barcode.
     */
    @Override
    public boolean checkBarcode(String barcode) throws SQLException {
        String query = "SELECT * FROM PLUS WHERE CODE = '" + barcode + "'";
        ResultSet res;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            try {
                res = stmt.executeQuery(query);
                if (res.next()) {
                    res.close();
                    con.commit();
                    return true;
                }
                res.close();
                con.commit();
                return false;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    /**
     * Method to remove a product from the database.
     *
     * @param p the product to remove.
     * @throws SQLException if there was an error removing the product.
     * @throws ProductNotFoundException if the product was not found.
     */
    @Override
    public void removeProduct(Product p) throws SQLException, ProductNotFoundException {
        removeProduct(p.getId());
    }

    /**
     * Method to remove a product from the database.
     *
     * @param id the product to remove.
     * @throws SQLException if there was an error removing the product.
     * @throws ProductNotFoundException if the product code was not found.
     */
    @Override
    public void removeProduct(int id) throws SQLException, ProductNotFoundException {
        String query = "DELETE FROM PRODUCTS WHERE PRODUCTS.ID = " + id + "";
        String iQuery = "DELETE FROM WASTEITEMS WHERE PRODUCT = " + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                stmt.executeUpdate(iQuery);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            try {
                long stamp = productLock.writeLock();
                try {
                    value = stmt.executeUpdate(query);
                    if (value == 0) {
                        throw new ProductNotFoundException(id + "");
                    }
                    con.commit();
                } finally {
                    productLock.unlockWrite(stamp);
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    /**
     * Method to purchase a product and reduce its stock level by 1.
     *
     * @param id the product to purchase.
     * @param amount the amount of the product to purchase.
     * @return the new stock level.
     * @throws SQLException if there was an error purchasing the product.
     * @throws OutOfStockException if the product is out of stock.
     * @throws ProductNotFoundException if the product was not found.
     */
    @Override
    public int purchaseProduct(int id, int amount) throws SQLException, OutOfStockException, ProductNotFoundException {
        String query = "SELECT * FROM PRODUCTS WHERE PRODUCTS.ID=" + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            try {
                long stamp = productLock.writeLock();
                try {
                    ResultSet res = stmt.executeQuery(query);
                    while (res.next()) {
                        int stock = res.getInt("STOCK");
                        stock -= amount;
                        int minStock = res.getInt("MIN_PRODUCT_LEVEL");
                        res.close();
                        String update = "UPDATE PRODUCTS SET STOCK=" + stock + " WHERE PRODUCTS.ID=" + id;
                        stmt = con.createStatement();
                        stmt.executeUpdate(update);
                        if (stock < minStock) {
                            LOG.log(Level.WARNING, id + " is below minimum stock level");
                            g.logWarning("WARNING- Product " + id + " is below is minimum level!");
                        }
                        con.commit();
                        return stock;
                    }
                } finally {
                    productLock.unlockWrite(stamp);
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        throw new ProductNotFoundException(id + " could not be found");
    }

    /**
     * Method to get a product by its code.
     *
     * @param code the product to get.
     * @return the Product that matches the code.
     * @throws SQLException if there was an error getting the product.
     * @throws ProductNotFoundException if the product could not be found.
     */
    @Override
    public Product getProduct(int code) throws SQLException, ProductNotFoundException {
        String query = "SELECT p.ID as pId, p.ORDER_CODE, p.NAME as pName, p.OPEN_PRICE, p.PRICE, p.STOCK, p.COMMENTS, p.SHORT_NAME, p.CATEGORY_ID, p.DEPARTMENT_ID, p.TAX_ID, p.COST_PRICE, p.MIN_PRODUCT_LEVEL, p.MAX_PRODUCT_LEVEL FROM PRODUCTS p WHERE p.ID=" + code;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Product> products = new LinkedList<>();
            try {
                long stamp = productLock.readLock();
                try {
                    ResultSet res = stmt.executeQuery(query);
                    products = getProductsFromResultSet(res);
                    con.commit();
                    if (products.isEmpty()) {
                        throw new ProductNotFoundException("Product " + code + " could not be found");
                    }
                } finally {
                    productLock.unlockRead(stamp);
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            final Product product = products.get(0);
            try {
                product.setTax(this.getTax(product.getTaxID()));
                product.setCategory(this.getCategory(product.getCategoryID()));
                product.setDepartment(this.getDepartment(product.getDepartmentID()));
            } catch (JTillException ex) {
                Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
            }
            return products.get(0);
        }
    }

    /**
     * Method to get a product by its barcode.
     *
     * @param barcode the barcode to search.
     * @return the product that matches the barcode.
     * @throws SQLException if there was an error getting the product.
     * @throws ProductNotFoundException if the product could not be found.
     */
    @Override
    public Product getProductByBarcode(String barcode) throws SQLException, ProductNotFoundException {
        String query = "SELECT p.ID as pId, p.ORDER_CODE, p.NAME as pName, p.OPEN_PRICE, p.PRICE, p.STOCK, p.COMMENTS, p.SHORT_NAME, p.DEPARTMENT_ID, p.CATEGORY_ID, p.TAX_ID, p.COST_PRICE, p.MIN_PRODUCT_LEVEL, p.MAX_PRODUCT_LEVEL, pl.ID as plId, pl.CODE as plCode, pl.PRODUCT as plProduct FROM PRODUCTS p, PLUS pl WHERE p.ID = pl.PRODUCT AND pl.CODE='" + barcode + "'";
        List<Product> products = new LinkedList<>();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            try {
                long stamp = productLock.readLock();
                try {
                    ResultSet res = stmt.executeQuery(query);
                    products = getProductsFromResultSet(res);
                    con.commit();
                    if (products.isEmpty()) {
                        throw new ProductNotFoundException(barcode + " could not be found");
                    }
                    products.get(0).setCategory(this.getCategory(products.get(0).getCategoryID()));
                    products.get(0).setTax(this.getTax(products.get(0).getTaxID()));
                } catch (JTillException ex) {
                    Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    productLock.unlockRead(stamp);
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return products.get(0);
    }

    @Override
    public List<Product> productLookup(String terms) throws IOException, SQLException {
        List<Product> products = this.getAllProducts();
        List<Product> newList = new LinkedList<>();
        products.stream().filter((p) -> (p.getLongName().toLowerCase().contains(terms.toLowerCase()) || p.getName().toLowerCase().contains(terms.toLowerCase()))).forEachOrdered((p) -> {
            newList.add(p);
        });
        return newList;
    }

    //Customer Methods
    @Override
    public List<Customer> getAllCustomers() throws SQLException {
        String query = "SELECT * FROM CUSTOMERS";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Customer> customers = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                customers = new LinkedList<>();
                while (set.next()) {
                    int id = set.getInt("ID");
                    String name = set.getString("NAME");
                    String phone = set.getString("PHONE");
                    String mobile = set.getString("MOBILE");
                    String email = set.getString("EMAIL");
                    String address1 = set.getString("ADDRESS_LINE_1");
                    String address2 = set.getString("ADDRESS_LINE_2");
                    String town = set.getString("TOWN");
                    String county = set.getString("COUNTY");
                    String country = set.getString("COUNTRY");
                    String postcode = set.getString("POSTCODE");
                    String notes = set.getString("NOTES");
                    int loyaltyPoints = set.getInt("LOYALTY_POINTS");
                    BigDecimal moneyDue = new BigDecimal(Double.toString(set.getDouble("MONEY_DUE")));
                    Customer c = new Customer(id, name, phone, mobile, email, address1, address2, town, county, country, postcode, notes, loyaltyPoints, moneyDue);
                    c = (Customer) Encryptor.decrypt(c);
                    customers.add(c);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            return customers;
        }
    }

    public List<Customer> getCustomersFromResultSet(ResultSet set) throws SQLException {
        List<Customer> customers = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt("ID");
            String name = set.getString("NAME");
            String phone = set.getString("PHONE");
            String mobile = set.getString("MOBILE");
            String email = set.getString("EMAIL");
            String address1 = set.getString("ADDRESS_LINE_1");
            String address2 = set.getString("ADDRESS_LINE_2");
            String town = set.getString("TOWN");
            String county = set.getString("COUNTY");
            String country = set.getString("COUNTRY");
            String postcode = set.getString("POSTCODE");
            String notes = set.getString("NOTES");
            int loyaltyPoints = set.getInt("LOYALTY_POINTS");
            BigDecimal moneyDue = new BigDecimal(set.getDouble("MONEY_DUE"));

            Customer c = new Customer(id, name, phone, mobile, email, address1, address2, town, county, country, postcode, notes, loyaltyPoints, moneyDue);

            c = (Customer) Encryptor.decrypt(c);
            customers.add(c);
        }
        return customers;
    }

    /**
     * Method to add a new product to the database.
     *
     * @param c the new customer to add.
     * @return the customer that was added.
     * @throws SQLException if there was an error adding the customer to the
     * database.
     */
    @Override
    public Customer addCustomer(Customer c) throws SQLException {
        c = (Customer) Encryptor.encrypt(c);
        String query = "INSERT INTO CUSTOMERS (NAME, PHONE, MOBILE, EMAIL, ADDRESS_LINE_1, ADDRESS_LINE_2, TOWN, COUNTY, COUNTRY, POSTCODE, NOTES, LOYALTY_POINTS, MONEY_DUE) VALUES (" + c.getSQLInsertString() + ")";
        try (Connection con = getNewConnection()) {
            PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    c.setId(id);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            c = (Customer) Encryptor.decrypt(c);
            return c;
        }
    }

    @Override
    public Customer updateCustomer(Customer c) throws SQLException, CustomerNotFoundException {
        c = (Customer) Encryptor.encrypt(c);
        String query = c.getSQLUpdateString();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new CustomerNotFoundException(c.getId() + "");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            c = (Customer) Encryptor.decrypt(c);
            return c;
        }
    }

    @Override
    public void removeCustomer(Customer c) throws SQLException, CustomerNotFoundException {
        removeCustomer(c.getId());
    }

    @Override
    public void removeCustomer(int id) throws SQLException, CustomerNotFoundException {
        String query = "DELETE FROM CUSTOMERS WHERE CUSTOMERS.ID = " + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new CustomerNotFoundException(id + "");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public Customer getCustomer(int id) throws SQLException, CustomerNotFoundException {
        String query = "SELECT * FROM CUSTOMERS WHERE CUSTOMERS.ID = " + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Customer> customers = new LinkedList<>();
            try {
                ResultSet res = stmt.executeQuery(query);
                customers = getCustomersFromResultSet(res);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            if (customers.isEmpty()) {
                throw new CustomerNotFoundException("Customer " + id + " could not be found");
            }
            return customers.get(0);
        }
    }

    @Override
    public List<Customer> getCustomerByName(String name) throws SQLException, CustomerNotFoundException {
        String query = "SELECT * FROM CUSTOMERS WHERE CUSTOMERS.NAME = " + name;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Customer> customers = new LinkedList<>();
            try {
                ResultSet res = stmt.executeQuery(query);
                customers = getCustomersFromResultSet(res);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            if (customers.isEmpty()) {
                throw new CustomerNotFoundException("Customer " + name + " could not be found");
            }
            return customers;
        }
    }

    @Override
    public List<Customer> customerLookup(String terms) throws IOException, SQLException {
        String query = "SELECT * FROM CUSTOMERS";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Customer> customers = new LinkedList<>();
            try {
                ResultSet res = stmt.executeQuery(query);
                customers = getCustomersFromResultSet(res);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            List<Customer> newList = new LinkedList<>();

            customers.stream().filter((c) -> (c.getName().toLowerCase().contains(terms.toLowerCase()))).forEachOrdered((c) -> {
                newList.add(c);
            });

            return newList;
        }
    }

    //Staff Methods
    @Override
    public List<Staff> getAllStaff() throws SQLException {
        String query = "SELECT * FROM STAFF";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Staff> staff = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                staff = new LinkedList<>();
                while (set.next()) {
                    int id = set.getInt("ID");
                    String name = set.getString("NAME");
                    int position = set.getInt("POSITION");
                    String uname = set.getString("USERNAME");
                    String pword = set.getString("PASSWORD");
                    String dPword = Encryptor.decrypt(pword);
                    boolean enabled = set.getBoolean("ENABLED");
                    double wage = set.getDouble("WAGE");
                    Staff s = new Staff(id, name, position, uname, dPword, wage, enabled);
                    staff.add(s);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            return staff;
        }
    }

    public List<Staff> getStaffFromResultSet(ResultSet set) throws SQLException {
        List<Staff> staff = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt("ID");
            String name = set.getString("NAME");
            int position = set.getInt("POSITION");
            String uname = set.getString("USERNAME");
            String pword = set.getString("PASSWORD");
            String dPass = Encryptor.decrypt(pword);
            boolean enabled = set.getBoolean("ENABLED");
            double wage = set.getDouble("WAGE");
            Staff s = new Staff(id, name, position, uname, dPass, wage, enabled);

            staff.add(s);
        }
        return staff;
    }

    @Override
    public Staff addStaff(Staff s) throws SQLException {
        String query = "INSERT INTO STAFF (NAME, POSITION, USERNAME, PASSWORD, ENABLED, WAGE) VALUES (" + s.getSQLInsertString() + ")";
        try (Connection con = getNewConnection()) {
            PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    s.setId(id);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            return s;
        }
    }

    @Override
    public Staff updateStaff(Staff s) throws SQLException, StaffNotFoundException {
        String query = s.getSQLUpdateString();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new StaffNotFoundException(s.getId() + "");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            return s;
        }
    }

    @Override
    public void removeStaff(Staff s) throws SQLException, StaffNotFoundException {
        removeStaff(s.getId());
    }

    @Override
    public void removeStaff(int id) throws SQLException, StaffNotFoundException {
        String query = "DELETE FROM STAFF WHERE STAFF.ID = " + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new StaffNotFoundException(id + "");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public Staff getStaff(int id) throws SQLException, StaffNotFoundException {
        String query = "SELECT * FROM STAFF WHERE STAFF.ID = " + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Staff> staff = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                staff = getStaffFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            if (staff.isEmpty()) {
                throw new StaffNotFoundException(id + "");
            }

            return staff.get(0);
        }
    }

    @Override
    public Staff login(String username, String password) throws SQLException, LoginException {
        String query = "SELECT * FROM STAFF WHERE STAFF.USERNAME = '" + username.toLowerCase() + "'";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Staff> staff = new LinkedList<>();
            try {
                ResultSet res = stmt.executeQuery(query);
                staff = getStaffFromResultSet(res);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            if (staff.isEmpty()) {
                throw new LoginException(username + " could not be found");
            }

            Staff s = staff.get(0);

            if (!s.isEnabled()) {
                throw new LoginException("Account not enabled");
            }

            if (s.getPassword().equals(password)) {
                return s;
            }

            throw new LoginException("Incorrect Password");
        }
    }

    @Override
    public int getStaffCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM STAFF";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            try {
                ResultSet res = stmt.executeQuery(query);
                while (res.next()) {
                    int count = res.getInt(1);
                    con.commit();
                    return count;
                }
                con.commit();
                return 0;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    //Discount Methods
    @Override
    public List<Discount> getAllDiscounts() throws SQLException {
        String query = "SELECT * FROM DISCOUNTS";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Discount> discounts = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                discounts = new LinkedList<>();
                while (set.next()) {
                    int id = set.getInt("ID");
                    String name = set.getString("NAME");
                    double percentage = set.getDouble("PERCENTAGE");
                    BigDecimal price = new BigDecimal(Double.toString(set.getDouble("PRICE")));
                    int a = set.getInt("ACTION");
                    int c = set.getInt("CONDITION");
                    long start = set.getLong("STARTT");
                    long end = set.getLong("ENDT");
                    Discount d = new Discount(id, name, percentage, price, a, c, start, end);

                    discounts.add(d);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            return discounts;
        }
    }

    public List<Discount> getDiscountsFromResultSet(ResultSet set) throws SQLException {
        List<Discount> discounts = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt("ID");
            String name = set.getString("NAME");
            double percentage = set.getDouble("PERCENTAGE");
            BigDecimal price = new BigDecimal(Double.toString(set.getDouble("PRICE")));
            int a = set.getInt("ACTION");
            int c = set.getInt("CONDITION");
            long start = set.getLong("STARTT");
            long end = set.getLong("ENDT");
            Discount d = new Discount(id, name, percentage, price, a, c, start, end);

            discounts.add(d);
        }
        return discounts;
    }

    @Override
    public Discount addDiscount(Discount d) throws SQLException {
        String query = "INSERT INTO DISCOUNTS (NAME, PERCENTAGE, PRICE, ACTION, CONDITION, STARTT, ENDT) VALUES (" + d.getSQLInsertString() + ")";
        try (Connection con = getNewConnection()) {
            PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    d.setId(id);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            return d;
        }
    }

    @Override
    public Discount updateDiscount(Discount d) throws SQLException, DiscountNotFoundException {
        String query = d.getSQLUpdateString();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new DiscountNotFoundException(d.getId() + "");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            return d;
        }
    }

    @Override
    public void removeDiscount(Discount d) throws SQLException, DiscountNotFoundException {
        removeDiscount(d.getId());
    }

    @Override
    public void removeDiscount(int id) throws SQLException, DiscountNotFoundException {
        String query = "DELETE FROM DISCOUNTS WHERE DISCOUNTS.ID = " + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new DiscountNotFoundException(id + "");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public Discount getDiscount(int id) throws SQLException, DiscountNotFoundException {
        String query = "SELECT * FROM DISCOUNTS WHERE DISCOUNTS.ID = " + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Discount> discounts = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                discounts = getDiscountsFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            if (discounts.isEmpty()) {
                throw new DiscountNotFoundException(id + "");
            }

            return discounts.get(0);
        }
    }

    //Tax Methods
    @Override
    public List<Tax> getAllTax() throws SQLException {
        String query = "SELECT * FROM TAX";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Tax> tax = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                tax = new LinkedList<>();
                while (set.next()) {
                    int id = set.getInt("ID");
                    String name = set.getString("NAME");
                    double value = set.getDouble("VALUE");
                    Tax t = new Tax(id, name, value);
                    tax.add(t);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            return tax;
        }
    }

    public List<Tax> getTaxFromResultSet(ResultSet set) throws SQLException {
        List<Tax> tax = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt("ID");
            String name = set.getString("NAME");
            double value = set.getDouble("VALUE");
            Tax t = new Tax(id, name, value);

            tax.add(t);
        }
        return tax;
    }

    @Override
    public Tax addTax(Tax t) throws SQLException {
        String query = "INSERT INTO TAX (NAME, VALUE) VALUES (" + t.getSQLInsertString() + ")";
        try (Connection con = getNewConnection()) {
            PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    t.setId(id);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return t;
    }

    @Override
    public Tax updateTax(Tax t) throws SQLException, JTillException {
        String query = t.getSQLUpdateString();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new JTillException(t.getId() + "");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return t;
    }

    @Override
    public void removeTax(Tax t) throws SQLException, JTillException {
        removeTax(t.getId());
    }

    @Override
    public void removeTax(int id) throws SQLException, JTillException {
        List<Product> products = this.getProductsInTax(id);
        final Tax DEFAULT_TAX = this.getTax(1);
        for (Product p : products) {
            p.setTaxID(DEFAULT_TAX.getId());
            try {
                this.updateProduct(p);
            } catch (ProductNotFoundException ex) {
            }
        }
        String query = "DELETE FROM TAX WHERE TAX.ID = " + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new JTillException(id + "");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public Tax getTax(int id) throws SQLException, JTillException {
        String query = "SELECT * FROM TAX WHERE TAX.ID = " + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Tax> tax = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                tax = getTaxFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            if (tax.isEmpty()) {
                throw new JTillException(id + "");
            }

            return tax.get(0);
        }
    }

    @Override
    public List<Product> getProductsInTax(int id) throws SQLException {
        String query = "SELECT * FROM PRODUCTS WHERE TAX_ID = " + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Product> products = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                products = getProductsFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            return products;
        }
    }

    //Category Methods
    @Override
    public List<Category> getAllCategorys() throws SQLException {
        String query = "SELECT * FROM CATEGORYS";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Category> categorys = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                categorys = new LinkedList<>();
                while (set.next()) {
                    int id = set.getInt("ID");
                    String name = set.getString("NAME");
                    Time startSell = set.getTime("SELL_START");
                    Time endSell = set.getTime("SELL_END");
                    boolean timeRestrict = set.getBoolean("TIME_RESTRICT");
                    int minAge = set.getInt("MINIMUM_AGE");
                    Category c = new Category(id, name, startSell, endSell, timeRestrict, minAge);
                    categorys.add(c);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            return categorys;
        }
    }

    public List<Category> getCategorysFromResultSet(ResultSet set) throws SQLException {
        List<Category> categorys = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt("ID");
            String name = set.getString("NAME");
            Time startSell = set.getTime("SELL_START");
            Time endSell = set.getTime("SELL_END");
            boolean timeRestrict = set.getBoolean("TIME_RESTRICT");
            int minAge = set.getInt("MINIMUM_AGE");
            Category c = new Category(id, name, startSell, endSell, timeRestrict, minAge);
            categorys.add(c);
        }
        return categorys;
    }

    @Override
    public Category addCategory(Category c) throws SQLException {
        String query = "INSERT INTO CATEGORYS (NAME, SELL_START, SELL_END, TIME_RESTRICT, MINIMUM_AGE) VALUES (" + c.getSQLInsertString() + ")";
        try (Connection con = getNewConnection()) {
            PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    c.setID(id);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return c;
    }

    @Override
    public Category updateCategory(Category c) throws SQLException, JTillException {
        String query = c.getSQLUpdateString();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new JTillException(c.getId() + "");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return c;
    }

    @Override
    public void removeCategory(Category c) throws SQLException, JTillException {
        removeCategory(c.getId());
    }

    @Override
    public void removeCategory(int id) throws SQLException, JTillException {
        List<Product> products = getProductsInCategory(id);
        final Category DEFAULT_CATEGORY = getCategory(1);
        for (Product p : products) {
            p.setCategoryID(DEFAULT_CATEGORY.getId());
            try {
                updateProduct(p);
            } catch (ProductNotFoundException ex) {
            }
        }
        String query = "DELETE FROM CATEGORYS WHERE CATEGORYS.ID = " + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new JTillException(id + "");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public Category getCategory(int id) throws SQLException, JTillException {
        String query = "SELECT * FROM CATEGORYS WHERE CATEGORYS.ID = " + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Category> categorys = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                categorys = getCategorysFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            if (categorys.isEmpty()) {
                throw new JTillException(id + "");
            }

            return categorys.get(0);
        }
    }

    @Override
    public List<Product> getProductsInCategory(int id) throws SQLException {
        String query = "SELECT * FROM PRODUCTS WHERE PRODUCTS.CATEGORY_ID = " + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Product> products = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                products = getProductsFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            return products;
        }
    }

    private List<Sale> getSalesFromResultSet(ResultSet set) throws SQLException {
        List<Sale> sales = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt(1);
            BigDecimal price = new BigDecimal(Double.toString(set.getDouble(2)));
            int customerid = set.getInt(3);
            Date date = new Date(set.getLong(4));
            int terminal = set.getInt(5);
            boolean cashed = set.getBoolean(6);
            int sId = set.getInt(7);
            final Sale s = new Sale(id, price, customerid, date, terminal, cashed, sId);

            int cid = set.getInt(9);
            String name = set.getString(10);
            String phone = set.getString(11);
            String mobile = set.getString(12);
            String email = set.getString(13);
            String address1 = set.getString(14);
            String address2 = set.getString(15);
            String town = set.getString(16);
            String county = set.getString(17);
            String country = set.getString(18);
            String postcode = set.getString(19);
            String notes = set.getString(20);
            int loyaltyPoints = set.getInt(21);
            BigDecimal moneyDue = new BigDecimal(set.getDouble(22));
            final Customer c = new Customer(cid, name, phone, mobile, email, address1, address2, town, county, country, postcode, notes, loyaltyPoints, moneyDue);
            s.setCustomer(c);

            int tid = set.getInt(23);
            UUID uuid = UUID.fromString(set.getString(24));
            String tname = set.getString(25);
            double d = set.getDouble(26);
            int sc = set.getInt(27);
            BigDecimal uncashed = new BigDecimal(Double.toString(d));
            final Till t = new Till(tname, uncashed, tid, uuid, sc);
            s.setTerminal(t);

            int stid = set.getInt(28);
            String stname = set.getString(29);
            int position = set.getInt(30);
            String uname = set.getString(31);
            String pword = set.getString(32);
            String dPass = Encryptor.decrypt(pword);
            boolean enabled = set.getBoolean(33);
            double wage = set.getDouble(34);
            final Staff st = new Staff(stid, stname, position, uname, dPass, wage, enabled);
            s.setStaff(st);

            s.setProducts(getItemsInSale(s));
            sales.add(s);
        }
        return sales;
    }

    @Override
    public Sale addSale(Sale s) throws SQLException, IOException {
        String query = "INSERT INTO SALES (PRICE, CUSTOMER, TIMESTAMP, TERMINAL, CASHED, STAFF, MOP) VALUES (" + s.getSQLInsertStatement() + ")";
        try (Connection con = getNewConnection()) {
            PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    s.setId(id);
                }
                con.commit();
                if (s.getMop() == Sale.MOP_CHARGEACCOUNT) {
                    try {
                        final Customer c = DBConnect.this.getCustomer(s.getCustomerID());
                        chargeCustomerAccount(c, s.getTotal());
                    } catch (SQLException | CustomerNotFoundException ex) {
                        Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        Runnable run = () -> {
            try {
                final Customer cus = getCustomer(s.getCustomerID());
                s.getSaleItems().forEach((i) -> {
                    if (i.getItem() instanceof Product) {
                        final Product p = (Product) i.getItem();
                        if (checkLoyalty(p)) {
                            String value = getSetting("LOYALTY_VALUE");
                            int points = p.getPrice().divide(new BigDecimal(value)).intValue();
                            points = points * i.getQuantity();
                            cus.addLoyaltyPoints(points);
                        }
                    }
                });
                updateCustomer(cus);
            } catch (SQLException | CustomerNotFoundException ex) {
                Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        Thread thread = new Thread(run);
        if (s.getCustomerID() > 1) {
            thread.start();
        }
        for (SaleItem p : s.getSaleItems()) {
            addSaleItem(s, p);
            try {
                if (p.getType() == SaleItem.PRODUCT) {
                    final Product pr = (Product) p.getItem();
                    if (!pr.isOpen()) {
                        purchaseProduct(pr.getId(), p.getQuantity());
                    }
                }
            } catch (OutOfStockException ex) {
                g.log(ex);
            } catch (ProductNotFoundException ex) {
            }
        }
        return s;
    }

    private boolean checkLoyalty(Product pr) {
        final List<JTillObject> contents = new ArrayList<>();
        try (Scanner inDep = new Scanner(new File("departments.loyalty"))) {
            while (inDep.hasNext()) {
                try {
                    String line = inDep.nextLine();
                    int id = Integer.parseInt(line);
                    final Department d = getDepartment(id);
                    contents.add(d);
                } catch (SQLException | JTillException ex) {
                    Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (FileNotFoundException e) {
            try {
                new File("departments.loyalty").createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try (Scanner inCat = new Scanner(new File("categorys.loyalty"))) {
            while (inCat.hasNext()) {
                try {
                    String line = inCat.nextLine();
                    int id = Integer.parseInt(line);
                    final Category c = getCategory(id);
                    contents.add(c);
                } catch (SQLException | JTillException ex) {
                    Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (FileNotFoundException e) {
            try {
                new File("categorys.loyalty").createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try (Scanner inPro = new Scanner(new File("products.loyalty"))) {
            while (inPro.hasNext()) {
                try {
                    String line = inPro.nextLine();
                    int id = Integer.parseInt(line);
                    final Product p = getProduct(id);
                    contents.add(p);
                } catch (ProductNotFoundException | SQLException ex) {
                    Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (FileNotFoundException e) {
            try {
                new File("products.loyalty").createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        for (JTillObject o : contents) {
            if (o instanceof Product) {
                if (((Product) o).equals(pr)) {
                    return true;
                }
            } else if (o instanceof Department) {
                try {
                    final Department dep = getDepartment(pr.getDepartmentID());
                    if (((Department) o).equals(dep)) {
                        return true;
                    }
                } catch (SQLException | JTillException ex) {
                    Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (o instanceof Category) {
                try {
                    final Category cat = getCategory(pr.getCategoryID());
                    if (((Category) o).equals(cat)) {
                        return true;
                    }
                } catch (SQLException | JTillException ex) {
                    Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return false;
    }

    private void chargeCustomerAccount(Customer c, BigDecimal amount) {
        c.setMoneyDue(c.getMoneyDue().add(amount));
        try {
            updateCustomer(c);
        } catch (SQLException | CustomerNotFoundException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public List<Sale> getAllSales() throws SQLException {
        String query = "SELECT * FROM SALES s, CUSTOMERS c, TILLS t, STAFF st WHERE c.ID = s.CUSTOMER AND st.ID = s.STAFF AND s.TERMINAL = t.ID";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Sale> sales = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                sales = getSalesFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            return sales;
        }
    }

    @Override
    public BigDecimal getTillTakings(int t) throws SQLException {
        String query = "SELECT * FROM SALES WHERE SALES.CASHED = FALSE AND SALES.TERMINAL = " + t + "";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            BigDecimal result = new BigDecimal("0");
            try {
                ResultSet set = stmt.executeQuery(query);
                while (set.next()) {
                    int id = set.getInt("ID");
                    BigDecimal price = new BigDecimal(Double.toString(set.getDouble("PRICE")));
                    int customerid = set.getInt("CUSTOMER");
                    Date date = new Date(set.getLong("TIMESTAMP"));
                    int terminal = set.getInt("TERMINAL");
                    boolean cashed = set.getBoolean("CASHED");
                    int sId = set.getInt("STAFF");
                    Sale s = new Sale(id, price, customerid, date, terminal, cashed, sId);
                    s.setProducts(getItemsInSale(s));
                    if (!s.isCashed()) {
                        result = result.add(s.getTotal());
                    }
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            return result;
        }
    }

    @Override
    public List<Sale> getUncashedSales(String t) throws SQLException {
        final String query = "SELECT * FROM SALES s, CUSTOMERS c, TILLS t, STAFF st , SaleItems si WHERE c.ID = s.CUSTOMER AND st.ID = s.STAFF AND CASHED = FALSE AND si.SALE_ID = s.ID AND s.TERMINAL = t.ID AND t.NAME = '" + t + "'";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Sale> sales = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                sales = getSalesFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            return sales;
        }
    }

    private List<SaleItem> getItemsInSale(Sale sale) throws SQLException {
        final String query = "SELECT * FROM APP.SALEITEMS WHERE SALEITEMS.SALE_ID = " + sale.getId();
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                List<SaleItem> items;
                ResultSet set = stmt.executeQuery(query);
                items = getSaleItemsFromResultSet(set);
                con.commit();
                return items;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    private List<SaleItem> getSaleItemsFromResultSet(ResultSet set) throws SQLException {
        final List<SaleItem> sales = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt("ID");
            int item = set.getInt("PRODUCT_ID");
            int type = set.getInt("TYPE");
            int quantity = set.getInt("QUANTITY");
            int saleId = set.getInt("SALE_ID");
            BigDecimal price = new BigDecimal(Double.toString(set.getDouble("PRICE")));
            BigDecimal tax = new BigDecimal(Double.toString(set.getDouble("TAX")));
            Item i;
            try {
                if (type == SaleItem.PRODUCT) {
                    i = this.getProduct(item);
                } else {
                    i = this.getDiscount(item);
                }
                SaleItem s = new SaleItem(saleId, i, quantity, id, price, type, tax);
                sales.add(s);
            } catch (ProductNotFoundException | DiscountNotFoundException ex) {
                Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return sales;
    }

    @Override
    public Sale getSale(int id) throws SQLException, JTillException {
        String query = "SELECT * FROM SALES s, CUSTOMERS c, TILLS t, STAFF st , SaleItems si WHERE c.ID = s.CUSTOMER AND st.ID = s.STAFF AND CASHED = FALSE AND si.SALE_ID = s.ID AND s.TERMINAL = t.ID AND s.ID = " + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Sale> sales = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                sales = getSalesFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            if (sales.isEmpty()) {
                throw new JTillException(id + "");
            }
            return sales.get(0);
        }
    }

    @Override
    public Sale updateSale(Sale s) throws SQLException, JTillException {
        String query = s.getSQLUpdateStatement();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new JTillException(s.getId() + "");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return s;
    }

    public Sale updateSaleNoSem(Sale s) throws SQLException, JTillException {
        String query = s.getSQLUpdateStatement();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            if (value == 0) {
                throw new JTillException(s.getId() + "");
            }
        }
        return s;
    }

    @Override
    public List<Sale> getSalesInRange(Time start, Time end) throws SQLException, IllegalArgumentException {
        if (start.after(end)) {
            throw new IllegalArgumentException("Start date needs to be before end date");
        }
        List<Sale> s = getAllSales();
        List<Sale> sales = new LinkedList<>();

        s.stream().filter((sale) -> (sale.getDate().after(start) && sale.getDate().before(start))).forEachOrdered((sale) -> {
            sales.add(sale);
        });

        return sales;
    }

    @Override
    public String toString() {
        if (connected) {
            return "Connected to database " + this.address + "\nOn user " + this.username;
        } else {
            return "Not connected to database";
        }
    }

    @Override
    public Staff tillLogin(int id) throws IOException, LoginException, SQLException {
        String query = "SELECT * FROM STAFF WHERE STAFF.ID = " + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Staff> staff = new LinkedList<>();
            try {
                ResultSet res = stmt.executeQuery(query);
                staff = getStaffFromResultSet(res);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            if (staff.isEmpty()) {
                throw new LoginException(id + " could not be found");
            }

            Staff s = staff.get(0);

            if (!s.isEnabled()) {
                throw new LoginException("Account not enabled");
            }

            try {
                loggedInSem.acquire();

                if (loggedIn.contains(s)) {
                    loggedInSem.release();
                    throw new LoginException("You are already logged in elsewhere");
                }

                loggedIn.add(s);
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, "There has been an error loggin " + id + " into the system", ex);
            } finally {
                loggedInSem.release();
            }

            return s;
        }
    }

    @Override
    public void logout(Staff s) throws IOException, StaffNotFoundException {

    }

    @Override
    public void tillLogout(Staff s) throws IOException, StaffNotFoundException {
        try {
            loggedInSem.acquire();
            loggedIn.remove(s);
            return;
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, "There has been an error logging " + s.getId() + " out of the system", ex);
        } finally {
            loggedInSem.release();
        }
        throw new IOException("Error logging out");
    }

    private List<Screen> getScreensFromResultSet(ResultSet set) throws SQLException {
        List<Screen> screens = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt("ID");
            String name = set.getString("NAME");
            int width = set.getInt("WIDTH");
            int height = set.getInt("HEIGHT");
            Screen s = new Screen(name, id, width, height);

            screens.add(s);
        }

        return screens;
    }

    private List<TillButton> getButtonsFromResultSet(ResultSet set) throws SQLException {
        List<TillButton> buttons = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt("ID");
            String name = set.getString("NAME");
            int p = set.getInt("PRODUCT");
            int type = set.getInt("TYPE");
            int s = set.getInt("SCREEN_ID");
            int color = set.getInt("COLOR");
            int width = set.getInt("WIDTH");
            int height = set.getInt("HEIGHT");
            int x = set.getInt("XPOS");
            int y = set.getInt("YPOS");
            TillButton b = new TillButton(name, p, type, s, color, id, width, height, x, y);

            buttons.add(b);
        }

        return buttons;
    }

    @Override
    public Screen addScreen(Screen s) throws SQLException {
        String query = "INSERT INTO SCREENS (NAME, WIDTH, HEIGHT) VALUES (" + s.getSQLInsertString() + ")";
        try (Connection con = getNewConnection()) {
            PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    s.setId(id);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return s;
    }

    @Override
    public TillButton addButton(TillButton b) throws SQLException {
        String query = "INSERT INTO BUTTONS (NAME, PRODUCT, TYPE, COLOR, SCREEN_ID, WIDTH, HEIGHT, XPOS, YPOS) VALUES (" + b.getSQLInsertString() + ")";
        try (Connection con = getNewConnection()) {
            PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    b.setId(id);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return b;
    }

    @Override
    public void removeScreen(Screen s) throws SQLException, ScreenNotFoundException {
        String query = "DELETE FROM SCREENS WHERE SCREENS.ID = " + s.getId();
        String buttonsQuery = "DELETE FROM BUTTONS WHERE BUTTONS.SCREEN_ID = " + s.getId();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                stmt.executeUpdate(buttonsQuery);
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new ScreenNotFoundException("Screen " + s + " could not be found");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public void removeButton(TillButton b) throws SQLException, JTillException {
        String query = "DELETE FROM BUTTONS WHERE BUTTONS.ID = " + b.getId();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new JTillException("Button " + b + " could not be found");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public Screen getScreen(int s) throws SQLException, ScreenNotFoundException {
        String query = "SELECT * FROM SCREENS WHERE SCREENS.ID = " + s;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Screen> screens = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                screens = getScreensFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            if (screens.isEmpty()) {
                throw new ScreenNotFoundException("Screen " + s + " could not be found");
            }
            return screens.get(0);
        }
    }

    @Override
    public TillButton getButton(int b) throws SQLException, JTillException {
        String query = "SELECT * FROM SCREENS WHERE BUTTONS.ID = " + b;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<TillButton> buttons = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                buttons = getButtonsFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            if (buttons.isEmpty()) {
                throw new JTillException("Button " + b + " could not be found");
            }
            return buttons.get(0);
        }
    }

    @Override
    public Screen updateScreen(Screen s) throws SQLException, ScreenNotFoundException {
        String query = s.getSQLUpdateString();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new ScreenNotFoundException("Screen " + s + " could not be found");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return s;
    }

    @Override
    public TillButton updateButton(TillButton b) throws SQLException, JTillException {
        String query = b.getSQLUpdateString();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new JTillException("Button " + b + " could not be found");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return b;
    }

    @Override
    public List<Screen> getAllScreens() throws SQLException {
        String query = "SELECT * FROM SCREENS";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Screen> screens = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                screens = new LinkedList<>();
                while (set.next()) {
                    int id = set.getInt("ID");
                    String name = set.getString("NAME");
                    int width = set.getInt("WIDTH");
                    int height = set.getInt("HEIGHT");
                    Screen s = new Screen(name, id, width, height);

                    screens.add(s);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            return screens;
        }
    }

    @Override
    public List<TillButton> getAllButtons() throws SQLException {
        String query = "SELECT * FROM BUTTONS";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<TillButton> buttons = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                buttons = new LinkedList<>();
                while (set.next()) {
                    int id = set.getInt("ID");
                    String name = set.getString("NAME");
                    int p = set.getInt("PRODUCT");
                    int type = set.getInt("TYPE");
                    int color = set.getInt("COLOR");
                    int s = set.getInt("SCREEN_ID");
                    int width = set.getInt("WIDTH");
                    int height = set.getInt("HEIGHT");
                    int x = set.getInt("XPOS");
                    int y = set.getInt("YPOS");
                    TillButton b = new TillButton(name, p, type, s, color, id, width, height, x, y);
                    buttons.add(b);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            return buttons;
        }
    }

    @Override
    public List<TillButton> getButtonsOnScreen(Screen s) throws IOException, SQLException, ScreenNotFoundException {
        String query = "SELECT * FROM BUTTONS WHERE BUTTONS.SCREEN_ID=" + s.getId();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<TillButton> buttons = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                buttons = new LinkedList<>();
                while (set.next()) {
                    int id = set.getInt("ID");
                    String name = set.getString("NAME");
                    int i = 0;
                    if (!name.equals("[SPACE]")) {
                        i = set.getInt("PRODUCT");
                    }
                    int type = set.getInt("TYPE");
                    int color = set.getInt("COLOR");
                    int width = set.getInt("WIDTH");
                    int height = set.getInt("HEIGHT");
                    int x = set.getInt("XPOS");
                    int y = set.getInt("YPOS");

                    TillButton b = new TillButton(name, i, type, s.getId(), color, id, width, height, x, y);

                    buttons.add(b);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            return buttons;
        }
    }

    @Override
    public void deleteAllScreensAndButtons() throws IOException, SQLException {
        try (Connection con = getNewConnection()) {
            try {
                String buttons = "DROP TABLE BUTTONS";
                String screens = "DROP TABLE SCREENS";
                Statement stmt = con.createStatement();
                stmt.execute(buttons);
                stmt.execute(screens);
                String cscreens = "create table \"APP\".SCREENS\n"
                        + "(\n"
                        + "     ID INT not null primary key\n"
                        + "         GENERATED ALWAYS AS IDENTITY\n"
                        + "         (START WITH 1, INCREMENT BY 1),\n"
                        + "     NAME VARCHAR(50) not null,\n"
                        + "     POSITION INTEGER,\n"
                        + "     COLOR INT\n"
                        + ")";
                String cbuttons = "create table \"APP\".BUTTONS\n"
                        + "(\n"
                        + "     ID INT not null primary key\n"
                        + "         GENERATED ALWAYS AS IDENTITY\n"
                        + "         (START WITH 1, INCREMENT BY 1),\n"
                        + "     NAME VARCHAR(50) not null,\n"
                        + "     PRODUCT INT not null,\n"
                        + "     COLOR INT,\n"
                        + "     SCREEN_ID INT not null references SCREENS(ID)\n"
                        + ")";
                stmt.execute(cscreens);
                LOG.log(Level.INFO, "Created screens table");
                stmt.execute(cbuttons);
                LOG.log(Level.INFO, "Created buttons table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    @Override
    public void setGUI(GUIInterface g) {
        this.g = g;
    }

    @Override
    public void suspendSale(Sale sale, Staff staff) throws IOException {
        long stamp = susL.writeLock();
        try {
            suspendedSales.put(staff, sale);
        } finally {
            susL.unlockWrite(stamp);
        }
    }

    @Override
    public Sale resumeSale(Staff s) throws IOException {
        long stamp = susL.writeLock();
        try {
            Sale sale = suspendedSales.get(s);
            suspendedSales.remove(s);
            return sale;
        } finally {
            susL.unlockWrite(stamp);
        }
    }

    @Override
    public void assisstance(String message) throws IOException {
        new Thread("Assisstance Message") {
            @Override
            public void run() {
                g.showMessage("Assisstance", message);
            }
        }.start();
        g.log(message);
    }

    @Override
    public void sendEmail(String email) throws IOException {
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("jtill", "honorsproject");
            }
        };
        Session session = Session.getDefaultInstance(systemSettings.getProperties(), auth);

        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(systemSettings.getSetting("OUTGOING_MAIL_ADDRESS")));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(systemSettings.getSetting("MAIL_ADDRESS")));
            message.setSubject("TILL REPORT");
            message.setText(email);
            Transport.send(message);
        } catch (AddressException ex) {
            System.out.println(ex);
        } catch (MessagingException ex) {
            System.out.println(ex);
        }
    }

    @Override
    public boolean emailReceipt(String email, Sale sale) throws IOException, AddressException, MessagingException {
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("jtill", "honorsproject");
            }
        };
        Session session = Session.getDefaultInstance(systemSettings.getProperties(), auth);

        final String outgoing_email = systemSettings.getSetting("OUTGOING_MAIL_ADDRESS");

        if (outgoing_email == null || outgoing_email.isEmpty()) {
            return false;
        }
        MimeMessage message = new MimeMessage(session);

        String text = "";

        text += "Here is your receipt from your recent purchase\n";
        text += "Sale ID: " + sale.getId() + "\n";
        text += "Time: " + sale.getDate().toString() + "\n";
        String symbol = Settings.getInstance().getSetting("CURRENCY_SYMBOL");
        text += "Total: " + symbol + sale.getTotal() + "\n";
        if (sale.getMop() == Sale.MOP_CHARGEACCOUNT) {
            text += "You will be invoiced for this sale\n";
        }
        try {
            final Staff staff = getStaff(sale.getStaffID());
            text += "You were served by " + staff.getName() + "\n";
            text += "Thank you for your custom";

            message.setFrom(new InternetAddress(outgoing_email));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("Receipt for sale " + sale.getId());
            message.setText(text);
            Transport.send(message);
            return true;
        } catch (SQLException | StaffNotFoundException ex) {
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private List<Till> getTillsFromResultSet(ResultSet set) throws SQLException {
        List<Till> tills = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt("ID");
            UUID uuid = UUID.fromString(set.getString("UUID"));
            String name = set.getString("NAME");
            double d = set.getDouble("UNCASHED");
            int sc = set.getInt("DEFAULT_SCREEN");
            BigDecimal uncashed = new BigDecimal(Double.toString(d));

            Till t = new Till(name, uncashed, id, uuid, sc);

            tills.add(t);
        }

        return tills;
    }

    @Override
    public Till addTill(Till t) throws IOException, SQLException {
        String query = "INSERT INTO TILLS (NAME, UUID, UNCASHED, DEFAULT_SCREEN) VALUES (" + t.getSQLInsertString() + ")";
        try (Connection con = getNewConnection()) {
            PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    t.setId(id);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return t;
    }

    @Override
    public void removeTill(int id) throws IOException, SQLException, JTillException {
        String query = "DELETE FROM TILLS WHERE TILLS.ID = " + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new JTillException(id + " could not be found");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public Till getTill(int id) throws IOException, SQLException, JTillException {
        String query = "SELECT * FROM TILLS WHERE TILLS.ID = " + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Till> tills = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                tills = getTillsFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            if (tills.isEmpty()) {
                throw new JTillException(id + " could not be found");
            }
            return tills.get(0);
        }
    }

    @Override
    public List<Till> getAllTills() throws SQLException {
        String query = "SELECT * FROM TILLS";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Till> tills = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                tills = getTillsFromResultSet(set);
                con.commit();
                for (Till t : tills) {
                    try {
                        List<Sale> sales = this.getUncachedTillSales(t.getId());
                        BigDecimal uncashedValue = BigDecimal.ZERO;
                        for (Sale s : sales) {
                            uncashedValue = uncashedValue.add(s.getTotal());
                        }
                        t.setUncashedTakings(uncashedValue);
                    } catch (IOException | JTillException ex) {
                        Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            for (JConnThread th : TillServer.server.getClientConnections()) {
                ConnectionHandler hand = (ConnectionHandler) th.getMethodClass();
                for (int i = 0; i < tills.size(); i++) {
                    final Till t = tills.get(i);
                    if (hand.till != null && t.getId() == hand.till.getId()) {
                        tills.set(i, hand.till);
                    }
                }
            }
            return tills;
        }
    }

    @Override
    public Till connectTill(String name, UUID uuid) {
        try {
            if (uuid == null) {
                throw new JTillException("No UUID");
            }
            Till till = this.getTillByUUID(uuid);
            till.setConnected(true);
            g.addTill(till);
            connectedTills.add(till);
            return till;
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "There has been an error adding a till to the database", ex);
        } catch (JTillException ex) {
            Till till = g.showTillSetupWindow(name);
            if (till != null) { //If the connection was allowed
                try {
                    addTill(till);
                } catch (IOException | SQLException ex1) {
                    LOG.log(Level.SEVERE, "There has been an error connecting a till the server", ex);
                }
                connectedTills.add(till);
                return till;
            }
        }
        return null;
    }

    @Override
    public List<Till> getConnectedTills() {
        return connectedTills;
    }

    private Till getTillByUUID(UUID uuid) throws SQLException, JTillException {
        String query = "SELECT * FROM TILLS WHERE TILLS.UUID = '" + uuid.toString() + "'";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Till> tills = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                tills = getTillsFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            if (tills.isEmpty()) {
                throw new JTillException(uuid.toString() + " could not be found");
            }

            return tills.get(0);
        }
    }

    @Override
    public void setSetting(String key, String value) {
        systemSettings.setSetting(key, value);
    }

    @Override
    public String getSetting(String key) {
        return systemSettings.getSetting(key);
    }

    private List<Plu> getPlusFromResultSet(ResultSet set) throws SQLException {
        List<Plu> plus = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt(1);
            String code = set.getString(2);
            int product = set.getInt(3);
            final Plu plu = new Plu(id, code, product);
            int pid = set.getInt(4);
            int order_code = set.getInt(5);
            String name = set.getString(6);
            boolean open = set.getBoolean(7);
            BigDecimal price = new BigDecimal(Double.toString(set.getDouble(8)));
            int stock = set.getInt(9);
            String comments = set.getString(10);
            String shortName = set.getString(11);
            int categoryID = set.getInt(12);
            int dId = set.getInt(13);
            int taxID = set.getInt(14);
            BigDecimal costPrice = new BigDecimal(Double.toString(set.getDouble(15)));
            int minStock = set.getInt(16);
            int maxStock = set.getInt(17);
            final Product p = new Product(name, shortName, order_code, categoryID, dId, comments, taxID, open, price, costPrice, stock, minStock, maxStock, pid);
            plu.setProduct(p);
            plus.add(plu);
        }
        return plus;
    }

    @Override
    public Plu addPlu(Plu plu) throws IOException, SQLException {
        String query = "INSERT INTO APP.PLUS (CODE, PRODUCT) values ('" + plu.getCode() + "'," + plu.getProductID() + ")";
        try (Connection con = getNewConnection()) {
            PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    plu.setId(id);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return plu;
    }

    @Override
    public void removePlu(int id) throws IOException, JTillException, SQLException {
        String query = "DELETE FROM PLUS WHERE ID=" + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value = 0;
            try {
                stmt.executeUpdate(query);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            if (value == 0) {
                throw new JTillException(id + " could not be found");
            }
        }
    }

    @Override
    public void removePlu(Plu p) throws IOException, JTillException, SQLException {
        removePlu(p.getId());
    }

    @Override
    public Plu getPlu(int id) throws IOException, JTillException, SQLException {
        String query = "SELECT * FROM PLUS pl, PRODUCTS p WHERE pl.PRODUCT = p.ID AND ID=" + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Plu> plus = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                plus = getPlusFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            if (plus.isEmpty()) {
                throw new JTillException(id + " could not be found");
            }
            return plus.get(0);
        }
    }

    @Override
    public Plu getPluByCode(String code) throws IOException, JTillException, SQLException {
        String query = "SELECT * FROM PLUS WHERE CODE='" + code + "'";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<Plu> plus = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                plus = getPlusFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            if (plus.isEmpty()) {
                throw new JTillException("Plu " + code + " not found");
            }

            return plus.get(0);
        }
    }

    @Override
    public List<Plu> getAllPlus() throws IOException, SQLException {
        final String query = "SELECT * FROM PLUS pl, PRODUCTS p WHERE pl.PRODUCT = p.ID";
        try (Connection con = getNewConnection()) {
            final Statement stmt = con.createStatement();
            List<Plu> plus = new LinkedList<>();
            try {
                final ResultSet set = stmt.executeQuery(query);
                plus = getPlusFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            for (Plu p : plus) {
                try {
                    p.setProduct(this.getProductByBarcode(p.getCode()));
                } catch (ProductNotFoundException ex) {
                    Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            return plus;
        }
    }

    @Override
    public Plu updatePlu(Plu p) throws IOException, JTillException, SQLException {
        String query = "UPDATE PLUS SET CODE='" + p.getCode() + "', PRODUCT=" + p.getProductID() + " WHERE ID=" + p.getId();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new JTillException("Plu " + p.getId() + " not found");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return p;
    }

    @Override
    public boolean isTillLoggedIn(Staff s) throws IOException, StaffNotFoundException, SQLException {
        return loggedIn.contains(s);
    }

    @Override
    public boolean checkUsername(String username) throws IOException, SQLException {
        String query = "SELECT * FROM STAFF WHERE USERNAME='" + username.toLowerCase() + "'";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            try {
                ResultSet set = stmt.executeQuery(query);
                boolean used = set.next();
                con.commit();
                return used;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public String getSetting(String key, String value) throws IOException {
        return systemSettings.getSetting(key, value);
    }

    @Override
    public GUIInterface getGUI() {
        return this.g;
    }

    private List<WasteReport> getWasteReportsFromResultSet(ResultSet set) throws SQLException {
        List<WasteReport> wrs = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt("ID");
            BigDecimal value = new BigDecimal(Double.toString(set.getDouble("VALUE")));
            Date date = new Date(set.getLong("TIMESTAMP"));
            wrs.add(new WasteReport(id, value, date));
        }
        return wrs;
    }

    @Override
    public WasteReport addWasteReport(WasteReport wr) throws IOException, SQLException, JTillException {
        String query = "INSERT INTO APP.WASTEREPORTS (VALUE, TIMESTAMP) values (" + wr.getTotalValue().doubleValue() + "," + wr.getDate().getTime() + ")";
        try (Connection con = getNewConnection()) {
            PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    wr.setId(id);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        for (WasteItem wi : wr.getItems()) {
            addWasteItem(wr, wi);
        }
        return wr;
    }

    @Override
    public void removeWasteReport(int id) throws IOException, SQLException, JTillException {
        String query = "DELETE FROM WASTEREPORTS WHERE ID=" + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value = 0;
            try {
                stmt.executeUpdate(query);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            if (value == 0) {
                throw new JTillException(id + " could not be found");
            }
        }
    }

    @Override
    public WasteReport getWasteReport(int id) throws IOException, SQLException, JTillException {
        String query = "SELECT * FROM WASTEREPORTS WHERE ID=" + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<WasteReport> wrs = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                wrs = getWasteReportsFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            if (wrs.isEmpty()) {
                throw new JTillException(id + " could not be found");
            }

            WasteReport wr = wrs.get(0);
            wr.setItems(getWasteItemsInReport(id));
            return wr;
        }
    }

    private List<WasteItem> getWasteItemsInReport(int id) throws SQLException {
        String query = "SELECT * FROM WASTEITEMS WHERE REPORT_ID=" + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<WasteItem> wis = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                wis = getWasteItemsFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            return wis;
        }
    }

    @Override
    public List<WasteReport> getAllWasteReports() throws IOException, SQLException {
        String query = "SELECT * FROM WASTEREPORTS";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<WasteReport> wrs = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                wrs = getWasteReportsFromResultSet(set);
                for (WasteReport wr : wrs) {
                    wr.setItems(getWasteItemsInReport(wr.getId()));
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            return wrs;
        }
    }

    @Override
    public WasteReport updateWasteReport(WasteReport wr) throws IOException, SQLException, JTillException {
        String query = "UPDATE WASTEREPORTS SET VALUE=" + wr.getTotalValue().doubleValue() + ", TIMESTAMP=" + wr.getDate().getTime() + " WHERE ID=" + wr.getId();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new JTillException("Waste Report " + wr.getId() + " not found");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return wr;
    }

    private List<WasteItem> getWasteItemsFromResultSet(ResultSet set) throws SQLException {
        List<WasteItem> wis = new LinkedList<>();
        while (set.next()) {
            try {
                int id = set.getInt("ID");
                Product p = this.getProduct(set.getInt("PRODUCT"));
                int quantity = set.getInt("QUANTITY");
                int wreason = set.getInt("REASON");
                wis.add(new WasteItem(id, p, quantity, wreason));
            } catch (ProductNotFoundException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        return wis;
    }

    @Override
    public WasteItem addWasteItem(WasteReport wr, WasteItem wi) throws IOException, SQLException, JTillException {
        String query = "INSERT INTO APP.WASTEITEMS (REPORT_ID, PRODUCT, QUANTITY, REASON) values (" + wr.getId() + "," + wi.getProduct().getId() + "," + wi.getQuantity() + "," + wi.getReason() + ")";
        try (Connection con = getNewConnection()) {
            PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    wi.setId(id);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return wi;
    }

    @Override
    public void removeWasteItem(int id) throws IOException, SQLException, JTillException {
        String query = "DELETE FROM WASTEITEMS WHERE ID=" + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value = 0;
            try {
                stmt.executeUpdate(query);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            if (value == 0) {
                throw new JTillException(id + " could not be found");
            }
        }
    }

    @Override
    public WasteItem getWasteItem(int id) throws IOException, SQLException, JTillException {
        String query = "SELECT * FROM WASTEITEMS WHERE ID=" + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<WasteItem> wis = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                wis = getWasteItemsFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            if (wis.isEmpty()) {
                throw new JTillException(id + " could not be found");
            }

            return wis.get(0);
        }
    }

    @Override
    public List<WasteItem> getAllWasteItems() throws IOException, SQLException {
        String query = "SELECT * FROM WASTEITEMS";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<WasteItem> wis = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                wis = getWasteItemsFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            return wis;
        }
    }

    @Override
    public WasteItem updateWasteItem(WasteItem wi) throws IOException, SQLException, JTillException {
        String query = "UPDATE WASTEREPORTS SET PRODUCT=" + wi.getProduct().getId() + ", quantity=" + wi.getQuantity() + ", REASON='" + wi.getReason() + "', WHERE ID=" + wi.getId();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new JTillException("Waste Report " + wi.getId() + " not found");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return wi;
    }

    private List<WasteReason> getWasteReasonsFromResultSet(ResultSet set) throws SQLException {
        List<WasteReason> wrs = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt("ID");
            String reason = set.getString("REASON");
            wrs.add(new WasteReason(id, reason));
        }
        return wrs;
    }

    @Override
    public WasteReason addWasteReason(WasteReason wr) throws IOException, SQLException, JTillException {
        String query = "INSERT INTO APP.WASTEREASONS (REASON) values ('" + wr.getReason() + "')";
        try (Connection con = getNewConnection()) {
            PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    wr.setId(id);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return wr;
    }

    @Override
    public void removeWasteReason(int id) throws IOException, SQLException, JTillException {
        String query = "DELETE FROM WATSEREASONS WHERE ID=" + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value = 0;
            try {
                stmt.executeUpdate(query);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
            if (value == 0) {
                throw new JTillException(id + " could not be found");
            }
        }
    }

    @Override
    public WasteReason getWasteReason(int id) throws IOException, SQLException, JTillException {
        String query = "SELECT * FROM WASTEREASONS WHERE ID=" + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<WasteReason> wrs = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                wrs = getWasteReasonsFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            if (wrs.isEmpty()) {
                throw new JTillException(id + " could not be found");
            }
            return wrs.get(0);
        }
    }

    @Override
    public List<WasteReason> getAllWasteReasons() throws IOException, SQLException {
        String query = "SELECT * FROM WASTEREASONS";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            List<WasteReason> wrs = new LinkedList<>();
            try {
                ResultSet set = stmt.executeQuery(query);
                wrs = getWasteReasonsFromResultSet(set);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }

            return wrs;
        }
    }

    @Override
    public WasteReason updateWasteReason(WasteReason wr) throws IOException, SQLException, JTillException {
        String query = "UPDATE WASTEREASONS SET REASON='" + wr.getReason() + "'";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new JTillException("Waste Reason " + wr.getId() + " not found");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return wr;
    }

    @Override
    public Supplier addSupplier(Supplier s) throws IOException, SQLException, JTillException {
        String query = "INSERT INTO SUPPLIERS (NAME, ADDRESS, PHONE) VALUES ('" + s.getName() + "','" + s.getAddress() + "','" + s.getContactNumber() + "')";
        try (Connection con = getNewConnection()) {
            PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            long stamp = supL.writeLock();
            try {
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    s.setId(id);
                }
                con.commit();
                return s;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            } finally {
                supL.unlockWrite(stamp);
            }
        }
    }

    @Override
    public void removeSupplier(int id) throws IOException, SQLException, JTillException {
        String query = "DELETE FROM SUPPLIERS WHERE ID=" + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            long stamp = supL.writeLock();
            try {
                stmt.executeUpdate(query);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            } finally {
                supL.unlockWrite(stamp);
            }
        }
    }

    @Override
    public Supplier getSupplier(int id) throws IOException, SQLException, JTillException {
        String query = "SELECT * FROM SUPPLIERS WHERE ID=" + id;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            long stamp = supL.readLock();
            try {
                ResultSet set = stmt.executeQuery(query);
                Supplier sup = null;
                while (set.next()) {
                    String name = set.getString("NAME");
                    String addrs = set.getString("ADDRESS");
                    String phone = set.getString("PHONE");
                    sup = new Supplier(id, name, addrs, phone);
                }
                con.commit();
                return sup;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            } finally {
                supL.unlockRead(stamp);
            }
        }
    }

    @Override
    public List<Supplier> getAllSuppliers() throws IOException, SQLException {
        String query = "SELECT * FROM SUPPLIERS";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            long stamp = supL.readLock();
            try {
                ResultSet set = stmt.executeQuery(query);
                List<Supplier> suppliers = new LinkedList<>();
                while (set.next()) {
                    int id = set.getInt("ID");
                    String name = set.getString("NAME");
                    String addrs = set.getString("ADDRESS");
                    String phone = set.getString("PHONE");
                    Supplier sup = new Supplier(id, name, addrs, phone);
                    suppliers.add(sup);
                }
                con.commit();
                return suppliers;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            } finally {
                supL.unlockRead(stamp);
            }
        }
    }

    @Override
    public Supplier updateSupplier(Supplier s) throws IOException, SQLException, JTillException {
        String query = "UPDATE SUPPLIERS SET NAME='" + s.getName() + "' ADDRESS='" + s.getAddress() + "', PHONE='" + s.getContactNumber() + "'";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            long stamp = supL.writeLock();
            try {
                stmt.executeUpdate(query);
                con.commit();
                return s;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            } finally {
                supL.unlockWrite(stamp);
            }
        }
    }

    @Override
    public Department addDepartment(Department d) throws IOException, SQLException {
        String query = "INSERT INTO DEPARTMENTS (NAME) VALUES('" + d.getName() + "')";
        try (Connection con = getNewConnection()) {
            try {
                PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    d.setId(id);
                }
                con.commit();
                return d;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public void removeDepartment(int id) throws IOException, SQLException, JTillException {
        String query = "DELETE FROM DEPARTMENTS WHERE ID='" + id + "'";
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                int value = stmt.executeUpdate(query);
                con.commit();
                if (value == 0) {
                    throw new JTillException("Department " + id + " not found");
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public Department getDepartment(int id) throws SQLException, JTillException {
        String query = "SELECT * FROM DEPARTMENTS WHERE ID=" + id;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                List<Department> departments = new LinkedList<>();
                while (set.next()) {
                    String name = set.getString("NAME");
                    departments.add(new Department(id, name));
                }
                con.commit();
                return departments.get(0);
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public List<Department> getAllDepartments() throws IOException, SQLException {
        String query = "SELECT * FROM DEPARTMENTS";
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                List<Department> departments = new LinkedList<>();
                while (set.next()) {
                    int id = set.getInt("ID");
                    String name = set.getString("NAME");
                    departments.add(new Department(id, name));
                }
                con.commit();
                return departments;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.INFO, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public Department updateDepartment(Department d) throws IOException, SQLException, JTillException {
        String query = "UPDATE DEPARTMENTS SET NAME='" + d.getName() + "' WHERE ID=" + d.getId();
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                con.commit();
                return d;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.INFO, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public SaleItem addSaleItem(Sale s, SaleItem i) throws IOException, SQLException {
        i.setSale(s.getId());
        String query = "INSERT INTO SALEITEMS (PRODUCT_ID, TYPE, QUANTITY, PRICE, TAX, SALE_ID) VALUES(" + i.getSQLInsertStatement() + ")";
        try (Connection con = getNewConnection()) {
            try {
                PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    i.setId(id);
                }
                con.commit();
                return i;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public void removeSaleItem(int id) throws IOException, SQLException, JTillException {
        String query = "DELETE FROM SALEITEMS WHERE ID=" + id;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public SaleItem getSaleItem(int id) throws IOException, SQLException, JTillException {
        String query = "SELECT * FROM SALEITEMS WHERE ID = " + id;
        SaleItem i = null;
        int product_id;
        int sale_id;
        int type;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                while (set.next()) {
                    product_id = set.getInt("PRODUCT_ID");
                    type = set.getInt("TYPE");
                    int quantity = set.getInt("QUANTITY");
                    BigDecimal price = new BigDecimal(set.getDouble("PRICE"));
                    sale_id = set.getInt("SALE_ID");
                    BigDecimal tax = new BigDecimal(Double.toString(set.getDouble("TAX")));
                    Item it;
                    try {
                        if (type == SaleItem.PRODUCT) {
                            it = this.getProduct(product_id);
                        } else {
                            it = this.getDiscount(product_id);
                        }
                        i = new SaleItem(sale_id, it, quantity, id, price, type, tax);
                    } catch (ProductNotFoundException | DiscountNotFoundException ex) {
                        Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                con.commit();
                return i;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public List<SaleItem> getAllSaleItems() throws IOException, SQLException {
        String query = "SELECT * FROM SALEITEMS i, PRODUCTS p WHERE i.TYPE = 1 AND i.ITEM = p.ID";
        List<SaleItem> items = new LinkedList<>();
        int product_id;
        int sale_id;
        int type;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                while (set.next()) {
                    int id = set.getInt(1);
                    product_id = set.getInt(2);
                    type = set.getInt(3);
                    int quantity = set.getInt(4);
                    BigDecimal price = new BigDecimal(Double.toString(set.getDouble(5)));
                    BigDecimal tax = new BigDecimal(Double.toString(set.getDouble(6)));
                    sale_id = set.getInt(7);
                    Item it;
                    if (type == SaleItem.PRODUCT) {
                        it = this.getProduct(product_id);
                    } else {
                        it = this.getDiscount(product_id);
                    }
                    SaleItem i = new SaleItem(sale_id, it, quantity, id, price, type, tax);
                    int pcode = set.getInt(8);
                    int order_code = set.getInt(9);
                    String name = set.getString(10);
                    boolean open = set.getBoolean(11);
                    BigDecimal pprice = new BigDecimal(Double.toString(set.getDouble(12)));
                    int stock = set.getInt(13);
                    String comments = set.getString(14);
                    String shortName = set.getString(15);
                    int categoryID = set.getInt(16);
                    int dId = set.getInt(17);
                    int taxID = set.getInt(18);
                    BigDecimal costPrice = new BigDecimal(Double.toString(set.getDouble(19)));
                    int minStock = set.getInt(20);
                    int maxStock = set.getInt(21);

                    Product p = new Product(name, shortName, order_code, categoryID, dId, comments, taxID, open, pprice, costPrice, stock, minStock, maxStock, pcode);
                    i.setItem(p);
                    items.add(i);
                }
                con.commit();
                return items;
            } catch (SQLException | ProductNotFoundException | DiscountNotFoundException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                } else {
                    throw new SQLException(ex.getMessage());
                }
            }
        }
    }

    @Override
    public List<SaleItem> submitSaleItemQuery(String q) throws SQLException {
        String query = "SELECT * FROM SALEITEMS " + q;
        List<SaleItem> items = new LinkedList<>();
        int product_id;
        int sale_id;
        int type;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                while (set.next()) {
                    int id = set.getInt("ID");
                    product_id = set.getInt("PRODUCT_ID");
                    type = set.getInt("TYPE");
                    int quantity = set.getInt("QUANTITY");
                    BigDecimal price = new BigDecimal(set.getDouble("PRICE"));
                    sale_id = set.getInt("SALE_ID");
                    BigDecimal tax = new BigDecimal(Double.toString(set.getDouble("TAX")));
                    Item it;
                    if (type == SaleItem.PRODUCT) {
                        it = this.getProduct(product_id);
                    } else {
                        it = this.getDiscount(product_id);
                    }
                    SaleItem i = new SaleItem(sale_id, it, quantity, id, price, type, tax);
                    items.add(i);
                }
                con.commit();
                return items;
            } catch (ProductNotFoundException | DiscountNotFoundException | SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                } else {
                    throw new SQLException(ex.getMessage());
                }
            }
        }
    }

    @Override
    public SaleItem updateSaleItem(SaleItem i) throws IOException, SQLException, JTillException {
        String query = "UPDATE SALEITEMS SET PRODUCT_ID=" + i.getId() + ",TYPE=" + i.getType() + ",QUANTITY=" + i.getQuantity() + ",PRICE=" + i.getPrice() + ",SALE_ID=" + i.getSale();
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return i;
    }

    @Override
    public int getTotalSoldOfItem(int id) throws IOException, SQLException, ProductNotFoundException {
        String query = "SELECT QUANTITY, PRODUCT_ID FROM SALEITEMS WHERE PRODUCT_ID=" + id;
        int quantity = 0;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                boolean found = false;
                while (set.next()) {
                    found = true;
                    quantity += set.getInt("QUANTITY");
                }
                con.commit();
                if (!found) {
                    throw new ProductNotFoundException("Product " + id + " not found");
                }
                return quantity;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public BigDecimal getTotalValueSold(int id) throws IOException, SQLException, ProductNotFoundException {
        String query = "SELECT PRICE, PRODUCT_ID, QUANTITY FROM SALEITEMS WHERE PRODUCT_ID=" + id;
        BigDecimal val = BigDecimal.ZERO;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                boolean found = false;
                while (set.next()) {
                    found = true;
                    String value = Double.toString(set.getDouble("PRICE"));
                    int quantity = set.getInt("QUANTITY");
                    val = val.add(new BigDecimal(value).multiply(new BigDecimal(quantity)));
                }
                if (!found) {
                    throw new ProductNotFoundException("Product " + id + " not found");
                }
                con.commit();
                return val;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public int getTotalWastedOfItem(int id) throws IOException, SQLException, ProductNotFoundException {
        String query = "SELECT PRODUCT, QUANTITY FROM WASTEITEMS WHERE PRODUCT=" + id;
        int quantity = 0;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                boolean found = false;
                while (set.next()) {
                    found = true;
                    quantity += set.getInt("QUANTITY");
                }
                if (!found) {
                    throw new ProductNotFoundException("Product " + id + " not found");
                }
                con.commit();
                return quantity;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public BigDecimal getValueWastedOfItem(int id) throws IOException, SQLException, ProductNotFoundException {
        String query = "SELECT WASTEITEMS.ID AS wId, PRODUCT, QUANTITY, PRODUCTS.ID as pId, PRICE FROM PRODUCTS, WASTEITEMS WHERE WASTEITEMS.PRODUCT = PRODUCTS.ID AND WASTEITEMS.PRODUCT=" + id;
        BigDecimal val = BigDecimal.ZERO;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                boolean found = false;
                while (set.next()) {
                    found = true;
                    double dval = set.getDouble("PRICE");
                    dval *= set.getInt("QUANTITY");
                    String value = Double.toString(dval);
                    val = val.add(new BigDecimal(value));
                }
                if (!found) {
                    throw new ProductNotFoundException("Product " + id + " not found");
                }
                con.commit();
                return val;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public void addReceivedItem(ReceivedItem i, int report) throws IOException, SQLException {
        String query = "INSERT INTO RECEIVEDITEMS (PRODUCT, QUANTITY, PRICE, RECEIVED_REPORT) VALUES (" + i.getProduct() + "," + i.getQuantity() + "," + i.getPrice().doubleValue() + "," + report + ")";
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public BigDecimal getValueSpentOnItem(int id) throws IOException, SQLException, ProductNotFoundException {
        String query = "SELECT * FROM RECEIVEDITEMS WHERE PRODUCT=" + id;
        BigDecimal value = BigDecimal.ZERO;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                boolean found = false;
                while (set.next()) {
                    found = true;
                    String p = Double.toString(set.getDouble("PRICE"));
                    value = value.add(new BigDecimal(p).multiply(new BigDecimal(set.getInt("QUANTITY"))));
                }
                con.commit();
                if (!found) {
                    throw new ProductNotFoundException("Product " + id + " not found");
                }
                return value;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public List<SaleItem> getSaleItemsSearchTerms(int depId, int catId, Date start, Date end) throws IOException, SQLException {
        String query = "SELECT i.ID as iId, PRODUCT_ID, QUANTITY, i.PRICE as iPrice, SALE_ID, s.ID as sId, s.PRICE as sPrice, CUSTOMER, DISCOUNT, TIMESTAMP, TERMINAL, CASHED, STAFF, CHARGE_ACCOUNT FROM SALEITEMS i, SALES s, DEPARTMENTS d, Products p, Categorys c WHERE c.ID = p.CATEGORY_ID AND d.ID = p.DEPARTMENT_ID AND i.PRODUCT = p.ID AND i.SALE_ID = s.ID AND d.ID = " + depId + " AND c.ID = " + catId;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                List<SaleItem> items = new LinkedList<>();
                while (set.next()) {
                    int id = set.getInt("iId");
                    int pId = set.getInt("pId");
                    int quantity = set.getInt("QUANTITY");
                    BigDecimal price = new BigDecimal(Double.toString(set.getDouble("iPrice")));
                    int saleId = set.getInt("SALE_ID");
//                    items.add(new SaleItem(saleId, pId, quantity, id, price));
                }
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.INFO, null, ex);
                throw ex;
            }
        }
        return null;
    }

    @Override
    public void clockOn(int id) throws IOException, SQLException, StaffNotFoundException {
        long stamp = clockLock.readLock();
        try {
            for (int i : clockedOn) {
                if (i == id) {
                    return;
                }
            }
        } finally {
            clockLock.unlockRead(stamp);
        }
        String query = "INSERT INTO CLOCKONOFF(STAFF, TIMESTAMP, ONOFF) VALUES (" + id + "," + new Date().getTime() + "," + 0 + ")";
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                con.commit();
                stamp = clockLock.writeLock();
                clockedOn.add(id);
                clockLock.unlockWrite(stamp);
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public void clockOff(int id) throws IOException, SQLException, StaffNotFoundException {
        String query = "INSERT INTO CLOCKONOFF(STAFF, TIMESTAMP, ONOFF) VALUES (" + id + "," + new Date().getTime() + "," + 1 + ")";
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                con.commit();
                long stamp = clockLock.writeLock();
                for (int i = 0; i < clockedOn.size(); i++) {
                    int in = clockedOn.get(i);
                    if (in == id) {
                        clockedOn.remove(i);
                    }
                }
                clockLock.unlockWrite(stamp);
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public List<ClockItem> getAllClocks(int sid) throws IOException, SQLException, StaffNotFoundException {
        String query = "SELECT * FROM CLOCKONOFF WHERE STAFF=" + sid;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                List<ClockItem> items = new LinkedList<>();
                while (set.next()) {
                    int id = set.getInt("ID");
                    long timestamp = set.getLong("TIMESTAMP");
                    int type = set.getInt("ONOFF");
                    Date date = new Date(timestamp);
                    items.add(new ClockItem(id, sid, date, type));
                }
                con.commit();
                return items;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public void clearClocks(int id) throws IOException, SQLException, StaffNotFoundException {
        String query = "DELETE FROM CLOCKONOFF WHERE STAFF=" + id;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public Trigger addTrigger(Trigger t) throws IOException, SQLException {
        String query = "INSERT INTO TRIGGERS (BUCKET, PRODUCT, QUANTITYREQUIRED) VALUES (" + t.getBucket() + "," + t.getProduct() + "," + t.getQuantityRequired() + ")";
        try (Connection con = getNewConnection()) {
            try {
                PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    t.setId(id);
                }
                con.commit();
                return t;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public List<DiscountBucket> getDiscountBuckets(int id) throws IOException, SQLException, DiscountNotFoundException {
        String query = "SELECT * FROM BUCKETS WHERE DISCOUNT=" + id;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                List<DiscountBucket> buckets = new LinkedList<>();
                while (set.next()) {
                    int bId = set.getInt("ID");
                    int triggers = set.getInt("TRIGGERSREQUIRED");
                    boolean required = set.getBoolean("REQUIREDTRIGGER");
                    buckets.add(new DiscountBucket(bId, id, triggers, required));
                }
                con.commit();
                return buckets;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public void removeTrigger(int id) throws IOException, SQLException, JTillException {
        String query = "DELETE FROM TRIGGERS WHERE ID=" + id;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public List<Discount> getValidDiscounts() throws IOException, SQLException {
        String query = "SELECT * FROM DISCOUNTS";
        Date date = new Date();
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                List<Discount> discounts = new LinkedList<>();
                while (set.next()) {
                    int id = set.getInt("ID");
                    String name = set.getString("NAME");
                    double percentage = set.getDouble("PERCENTAGE");
                    BigDecimal price = new BigDecimal(Double.toString(set.getDouble("PRICE")));
                    int a = set.getInt("ACTION");
                    int c = set.getInt("CONDITION");
                    Date start = new Date(set.getLong("STARTT"));
                    Date end = new Date(set.getLong("ENDT"));
                    Discount d = new Discount(id, name, percentage, price, a, c, start.getTime(), end.getTime());
                    if (start.before(date) && end.after(date)) {
                        discounts.add(d);
                    }
                }
                con.commit();
                return discounts;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public DiscountBucket addBucket(DiscountBucket b) throws IOException, SQLException {
        String query = "INSERT INTO BUCKETS (DISCOUNT, TRIGGERSREQUIRED, REQUIREDTRIGGER) VALUES(" + b.getDiscount() + "," + b.getRequiredTriggers() + "," + b.isRequiredTrigger() + ")";
        try (Connection con = getNewConnection()) {
            try {
                PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    b.setId(set.getInt(1));
                }
                con.commit();
                return b;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public void removeBucket(int id) throws IOException, SQLException, JTillException {
        String query = "DELETE FROM BUCKETS WHERE ID=" + id;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public List<Trigger> getBucketTriggers(int id) throws IOException, SQLException, JTillException {
        String query = "SELECT * FROM TRIGGERS WHERE BUCKET=" + id;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                List<Trigger> triggers = new LinkedList<>();
                while (set.next()) {
                    int tId = set.getInt("ID");
                    int product = set.getInt("PRODUCT");
                    int req = set.getInt("QUANTITYREQUIRED");
                    triggers.add(new Trigger(tId, id, product, req));
                }
                con.commit();
                return triggers;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public Trigger updateTrigger(Trigger t) throws IOException, SQLException, JTillException {
        String query = "UPDATE TRIGGERS SET BUCKET=" + t.getBucket() + ", PRODUCT=" + t.getProduct() + ", QUANTITYREQUIRED=" + t.getQuantityRequired();
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                con.commit();
                return t;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public DiscountBucket updateBucket(DiscountBucket b) throws IOException, SQLException, JTillException {
        String query = "UPDATE BUCKETS SET TRIGGERSREQUIRED=" + b.getRequiredTriggers() + ", REQUIREDTRIGGER=" + b.isRequiredTrigger() + " WHERE ID=" + b.getId();
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                con.commit();
                return b;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public List<Sale> getUncachedTillSales(int id) throws IOException, JTillException {
        String query = "SELECT * FROM SALES WHERE TERMINAL=" + id;// + " AND CASHED=" + false;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                List<Sale> sales = new LinkedList<>();
                while (set.next()) {
                    int sId = set.getInt("ID");
                    double price = set.getDouble("PRICE");
                    int cId = set.getInt("CUSTOMER");
                    long timestamp = set.getLong("TIMESTAMP");
                    boolean cashed = set.getBoolean("CASHED");
                    if (cashed) {
                        continue;
                    }
                    int staff = set.getInt("STAFF");
                    int mop = set.getInt("MOP");
                    Sale s = new Sale(sId, new BigDecimal(Double.toString(price)), cId, new Date(timestamp), id, false, staff);
                    s.setMop(mop);
                    sales.add(s);
                }
                con.commit();
                return sales;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw new JTillException("Error getting sales");
        }
    }

    @Override
    public Product addProductAndPlu(Product p, Plu pl) throws IOException, SQLException {
        try (Connection con = getNewConnection()) {
            try {
                String productAdd = "INSERT INTO PRODUCTS (ORDER_CODE, NAME, OPEN_PRICE, PRICE, STOCK, COMMENTS, SHORT_NAME, CATEGORY_ID, DEPARTMENT_ID, TAX_ID, COST_PRICE, MIN_PRODUCT_LEVEL, MAX_PRODUCT_LEVEL) VALUES (" + p.getSQLInsertString() + ")";
                PreparedStatement productStmt = con.prepareStatement(productAdd, Statement.RETURN_GENERATED_KEYS);
                productStmt.executeUpdate();
                ResultSet product = productStmt.getGeneratedKeys();
                while (product.next()) {
                    int id = product.getInt(1);
                    p.setId(id);
                    pl.setProductID(id);
                }
                String pluAdd = "INSERT INTO PLUS (CODE, PRODUCT) VALUES ('" + pl.getCode() + "'," + pl.getProductID() + ")";
                PreparedStatement pluStmt = con.prepareStatement(pluAdd, Statement.RETURN_GENERATED_KEYS);
                pluStmt.executeUpdate();
                ResultSet plu = pluStmt.getGeneratedKeys();
                while (plu.next()) {
                    int id = plu.getInt(1);
                    pl.setId(id);
                }
                con.commit();
                return p;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public Plu getPluByProduct(int product) throws IOException, JTillException {
        String query = "SELECT * FROM PLUS WHERE PRODUCT=" + product;
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                Plu p = null;
                while (set.next()) {
                    int id = set.getInt("ID");
                    String code = set.getString("CODE");
                    p = new Plu(id, code, product);
                }
                con.commit();
                if (p == null) {
                    throw new JTillException("No matcing plu found");
                }
                return p;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;

            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnect.class
                    .getName()).log(Level.SEVERE, null, ex);
            throw new JTillException("Error getting Plu");
        }
    }

    @Override
    public List<SaleItem> searchSaleItems(int department, int category, Date start, Date end) throws IOException, SQLException, JTillException {
        long startL = start.getTime();
        long endL = end.getTime();
        String pQuery = "SELECT * FROM SALEITEMS i, PRODUCTS p, SALES s WHERE p.ID = i.PRODUCT_ID AND i.SALE_ID = s.ID AND i.TYPE = 1 AND s.TIMESTAMP >= " + startL + " AND s.TIMESTAMP <= " + endL;
        if (department > -1) {
            pQuery = pQuery.concat(" AND p.DEPARTMENT_ID = " + department);
        }
        if (category > -1) {
            pQuery = pQuery.concat(" AND p.CATEGORY_ID = " + category);
        }
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(pQuery);
                List<SaleItem> items = new LinkedList<>();
                while (set.next()) {
                    int id = set.getInt(1);
                    int pid = set.getInt(2);
                    int qu = set.getInt(4);
                    BigDecimal price = new BigDecimal(Double.toString(set.getDouble(5)));
                    int iSa = set.getInt(7);
                    BigDecimal tax = new BigDecimal(Double.toString(set.getDouble(6)));
                    final Item it = this.getProduct(pid);
                    SaleItem i = new SaleItem(iSa, it, qu, id, price, 1, tax);
                    int pcode = set.getInt(8);
                    int order_code = set.getInt(9);
                    String name = set.getString(10);
                    boolean open = set.getBoolean(11);
                    BigDecimal pprice = new BigDecimal(Double.toString(set.getDouble(12)));
                    int stock = set.getInt(13);
                    String comments = set.getString(14);
                    String shortName = set.getString(15);
                    int categoryID = set.getInt(16);
                    int dId = set.getInt(17);
                    int taxID = set.getInt(18);
                    BigDecimal costPrice = new BigDecimal(Double.toString(set.getDouble(19)));
                    int minStock = set.getInt(20);
                    int maxStock = set.getInt(21);

                    Product p = new Product(name, shortName, order_code, categoryID, dId, comments, taxID, open, pprice, costPrice, stock, minStock, maxStock, pcode);
                    i.setItem(p);
                    items.add(i);
                }
                con.commit();
                return items;
            } catch (ProductNotFoundException | SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                } else {
                    throw new JTillException(ex.getMessage());
                }
            }
        }
    }

    @Override
    public List<Sale> getTerminalSales(int terminal, boolean uncashedOnly) throws IOException, SQLException, JTillException {
        String query = "SELECT * FROM SALES WHERE TERMINAL = " + terminal + (uncashedOnly ? " AND CASHED = FALSE" : "");
        try (Connection con = getNewConnection()) {
            try {
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query);
                List<Sale> sales = new ArrayList<>();
                while (set.next()) {
                    int id = set.getInt("ID");
                    BigDecimal price = new BigDecimal(Double.toString(set.getDouble("PRICE")));
                    int customerid = set.getInt("CUSTOMER");
                    Date date = new Date(set.getLong("TIMESTAMP"));
                    boolean cashed = set.getBoolean("CASHED");
                    int sId = set.getInt("STAFF");
                    Sale s = new Sale(id, price, customerid, date, terminal, cashed, sId);
                    sales.add(s);
                }
                con.commit();
                for (Sale s : sales) {
                    s.setProducts(getItemsInSale(s));
                }
                return sales;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public void cashUncashedSales(int t) throws IOException, SQLException {
        String query = "SELECT * FROM SALES WHERE SALES.CASHED = FALSE AND SALES.TERMINAL = " + t + "";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            BigDecimal result = new BigDecimal("0");
            try {
                ResultSet set = stmt.executeQuery(query);
                while (set.next()) {
                    int id = set.getInt("ID");
                    BigDecimal price = new BigDecimal(Double.toString(set.getDouble("PRICE")));
                    int customerid = set.getInt("CUSTOMER");
                    Date date = new Date(set.getLong("TIMESTAMP"));
                    int terminal = set.getInt("TERMINAL");
                    boolean cashed = set.getBoolean("CASHED");
                    int sId = set.getInt("STAFF");
                    Sale s = new Sale(id, price, customerid, date, terminal, cashed, sId);
                    s.setProducts(getItemsInSale(s));
                    if (!s.isCashed()) {
                        result = result.add(s.getTotal());
                        s.setCashed(true);
                        try {
                            updateSaleNoSem(s);
                        } catch (JTillException ex) {
                            LOG.log(Level.WARNING, null, ex);
                        }
                    }
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public List<Product> getProductsAdvanced(String WHERE) throws IOException, SQLException {
        String query = "SELECT * FROM PRODUCTS " + WHERE;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            try {
                ResultSet set = stmt.executeQuery(query);
                List<Product> products = new ArrayList<>();
                while (set.next()) {
                    int code = set.getInt("id");
                    int order_code = set.getInt("ORDER_CODE");
                    String name = set.getString("name");
                    boolean open = set.getBoolean("OPEN_PRICE");
                    BigDecimal price = new BigDecimal(Double.toString(set.getDouble("PRICE")));
                    int stock = set.getInt("STOCK");
                    String comments = set.getString("COMMENTS");
                    String shortName = set.getString("SHORT_NAME");
                    int categoryID = set.getInt("CATEGORY_ID");
                    int dId = set.getInt("DEPARTMENT_ID");
                    int taxID = set.getInt("TAX_ID");
                    BigDecimal costPrice = new BigDecimal(Double.toString(set.getDouble("COST_PRICE")));
                    int minStock = set.getInt("MIN_PRODUCT_LEVEL");
                    int maxStock = set.getInt("MAX_PRODUCT_LEVEL");

                    Product p = new Product(name, shortName, order_code, categoryID, dId, comments, taxID, open, price, costPrice, stock, minStock, maxStock, code);

                    products.add(p);
                }
                con.commit();
                return products;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public List<Sale> getStaffSales(Staff s) throws IOException, StaffNotFoundException {
        final String query = "SELECT * FROM SALES WHERE STAFF = " + s.getId();
        try (final Connection con = getNewConnection()) {
            final Statement stmt = con.createStatement();
            final ResultSet set = stmt.executeQuery(query);
            final List<Sale> sales = new ArrayList<>();
            while (set.next()) {
                final int id = set.getInt("ID");
                final BigDecimal price = new BigDecimal(Double.toString(set.getDouble("PRICE")));
                final int customerId = set.getInt("CUSTOMER");
                final Date time = new Date(set.getLong("TIMESTAMP"));
                final int tillId = set.getInt("TERMINAL");
                final boolean cashed = set.getBoolean("CASHED");

                final Sale sale = new Sale(id, price, customerId, time, tillId, cashed, s.getId(), null);
                sales.add(sale);
            }
            con.commit();
            for (Sale sale : sales) {
                sale.setProducts(getItemsInSale(sale));
            }
            return sales;
        } catch (SQLException ex) {
            throw new IOException("Error");
        }
    }

    @Override
    public Object[] databaseInfo() throws IOException, SQLException {
        try (final Connection con = getNewConnection()) {
            final Object[] info = new Object[5];
            info[0] = con.getCatalog();
            info[1] = con.getSchema();
            info[2] = con.getClientInfo();
            info[3] = con.getMetaData().getDriverName();
            info[4] = con.getMetaData().getDriverVersion();
            return info;
        }
    }

    @Override
    public Till updateTill(Till t) throws IOException, SQLException, JTillException {
        String query = t.getSQLUpdateString();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value;
            try {
                value = stmt.executeUpdate(query);
                if (value == 0) {
                    throw new JTillException("Till id " + t.getId() + " could not be found");
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return t;
    }

    @Override
    public File getLoginBackground() throws IOException {
        return new File(getSetting("bg_url"));
    }

    @Override
    public void reinitialiseAllTills() throws IOException {
        server.sendData(null, JConnData.create("REINIT"));
    }

    @Override
    public int clearSalesData() throws IOException, SQLException {
        String q1 = "DELETE FROM SALEITEMS";
        String q2 = "DELETE FROM SALES";
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            int value = 0;
            try {
                stmt.executeUpdate(q1);
                value += stmt.executeUpdate(q2);
                con.commit();
                return value;
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }

    @Override
    public void addReceivedReport(ReceivedReport rep) throws SQLException, IOException {
        String query = "INSERT INTO RECEIVED_REPORTS (INVOICE_NO, SUPPLIER_ID, PAID) VALUES ('" + rep.getInvoiceId() + "'," + rep.getSupplierId() + "," + rep.isPaid() + ")";
        try (Connection con = getNewConnection()) {
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    rep.setId(id);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        for (ReceivedItem item : rep.getItems()) {
            this.addReceivedItem(item, rep.getId());
        }
    }

    @Override
    public List<ReceivedReport> getAllReceivedReports() throws IOException, SQLException {
        String query = "SELECT ID, SUPPLIER_ID, INVOICE_NO, PAID FROM RECEIVED_REPORTS";
        List<ReceivedReport> reports;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            try {
                ResultSet set = stmt.executeQuery(query);
                reports = new LinkedList<>();
                while (set.next()) {
                    int id = set.getInt(1);
                    int supp = set.getInt(2);
                    String inv = set.getString(3);
                    boolean paid = set.getBoolean(4);

                    ReceivedReport rr = new ReceivedReport(id, inv, supp, paid);

                    reports.add(rr);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }

        for (ReceivedReport rr : reports) {
            rr.setItems(getItemsInReport(rr.getId()));
        }
        return reports;
    }

    private List<ReceivedItem> getItemsInReport(int id) throws SQLException {
        String query = "SELECT * FROM RECEIVEDITEMS WHERE RECEIVED_REPORT=" + id;
        List<ReceivedItem> items;
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            try {
                ResultSet set = stmt.executeQuery(query);
                items = new LinkedList<>();
                while (set.next()) {
                    int iid = set.getInt(1);
                    int pro = set.getInt(2);
                    BigDecimal price = set.getBigDecimal(3);
                    int quantity = set.getInt(4);

                    ReceivedItem ri = new ReceivedItem(iid, pro, quantity, price);

                    items.add(ri);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return items;
    }

    @Override
    public ReceivedReport updateReceivedReport(ReceivedReport rr) throws IOException, SQLException {
        String query = "UPDATE RECEIVED_REPORTS SET PAID = " + rr.isPaid() + " WHERE ID=" + rr.getId();
        try (Connection con = getNewConnection()) {
            Statement stmt = con.createStatement();
            try {
                stmt.executeUpdate(query);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                LOG.log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        return rr;
    }

    @Override
    public void reinitTill(int id) throws IOException, SQLException {
        for (JConnThread th : server.getClientConnections()) {
            ConnectionHandler hand = (ConnectionHandler) th.getMethodClass();
            if (hand.till != null && hand.till.getId() == id) {
                th.sendData(JConnData.create("REINIT"));
            }
        }
    }

    @Override
    public void sendBuildUpdates() throws IOException, SQLException {
        for (JConnThread th : server.getClientConnections()) {
            ConnectionHandler hand = (ConnectionHandler) th.getMethodClass();
            if (hand.till != null) {
                th.sendData(JConnData.create("BUILDUPDATE"));
            }
        }
    }
}
