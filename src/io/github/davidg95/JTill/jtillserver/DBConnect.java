/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import io.github.davidg95.jconn.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.StampedLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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
public class DBConnect extends DataConnect {

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

    //Concurrent locks
    private final StampedLock susL;
    private final StampedLock supL;

    private volatile HashMap<Staff, Sale> suspendedSales; //A hash map of suspended sales.
    private final Settings systemSettings; //The system settings.

    private final List<Staff> loggedIn; //A list of logged in staff.
    private final Semaphore loggedInSem; //Semaphore for the list of logged in staff.

    private final LogFileHandler handler; //Handler object for the logger.

    private final List<Integer> clockedOn;
    private final StampedLock clockLock;

    private JConnServer server;

    private Connection dbConn;

    private int inits = 0;

    private String licenseNo;
    private int conn_limit;

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
        handler = LogFileHandler.getInstance();
        Logger.getGlobal().addHandler(handler);
        clockedOn = new LinkedList<>();
        clockLock = new StampedLock();
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
        try (final Connection con = getConnection()) {
            TillSplashScreen.addBar(10);
            con.commit();
        }
        updates();
    }

    private void updates() {
        try {
            Connection con = getConnection();
            Statement stmt = con.createStatement();
            TillSplashScreen.setLabel("Updating database");
            try {
                int res = stmt.executeUpdate("ALTER TABLE STAFF ADD ENABLED BOOLEAN");
                LOG.log(Level.INFO, "New fields added to staff table, " + res + " rows affected");
                con.commit();
                try {
                    int res2 = stmt.executeUpdate("UPDATE STAFF SET STAFF.ENABLED = TRUE");
                    LOG.log(Level.INFO, "Staff enabled fields set to TRUE, " + res2 + " rows affected");
                    con.commit();
                } catch (SQLException ex) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
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
            }
            try {
                int res = stmt.executeUpdate("ALTER TABLE RECEIVEDITEMS ADD RECEIVED_REPORT INT");
                LOG.log(Level.INFO, "New fields added to RECEIVEDITEMS table, " + res + " rows affected");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                int res = stmt.executeUpdate("ALTER TABLE RECEIVED_REPORTS ADD PAID BOOLEAN");
                LOG.log(Level.INFO, "New fields added to RECEIVED_REPORTS table, " + res + " rows affected");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                int res = stmt.executeUpdate("ALTER TABLE SCREENS ADD INHERITS INT");
                LOG.log(Level.INFO, "New fields added to SCREENS table, " + res + " rows affected");
                con.commit();
                try {
                    int res2 = stmt.executeUpdate("UPDATE STAFF SET INHERITS = -1");
                    LOG.log(Level.INFO, "Set inherits to -1, " + res2 + " rows affected");
                    con.commit();
                } catch (SQLException ex) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                int res = stmt.executeUpdate("ALTER TABLE BUTTONS ADD FONT_COLOR VARCHAR(7)");
                LOG.log(Level.INFO, "New field added to BUTTONS table, " + res + " rows affected");
                con.commit();
                try {
                    int res2 = stmt.executeUpdate("UPDATE BUTTONS SET FONT_COLOR = '000000'");
                    LOG.log(Level.INFO, "Set FONT_COLOR to 'ffffff', " + res2 + " rows affected");
                    con.commit();
                } catch (SQLException ex) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                int res = stmt.executeUpdate("ALTER TABLE PRODUCTS ADD PACK_SIZE INT");
                LOG.log(Level.INFO, "New field added to PRODUCTS table, " + res + " rows affected");
                con.commit();
                try {
                    int res2 = stmt.executeUpdate("UPDATE PRODUCTS SET PACK_SIZE = 1");
                    LOG.log(Level.INFO, "Set PACK_SIZE to 1, " + res2 + " rows affected");
                    con.commit();
                } catch (SQLException ex) {
                    con.rollback();
                    throw ex;
                }
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                int res = stmt.executeUpdate("ALTER TABLE BUTTONS ADD ACCESS_LEVEL INT");
                LOG.log(Level.INFO, "New field added to BUTTONS table, " + res + " rows affected");
                con.commit();
                try {
                    int res2 = stmt.executeUpdate("UPDATE BUTTONS SET ACCESS_LEVEL = 1");
                    LOG.log(Level.INFO, "Set ACCESS_LEVEL to 1, " + res2 + " rows affected");
                    con.commit();
                } catch (SQLException ex) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                int res = stmt.executeUpdate("ALTER TABLE PRODUCTS ADD BARCODE VARCHAR(15)");
                LOG.log(Level.INFO, "BARCODE added to PRODUCTS table, " + res + " rows affected");
                int res2 = stmt.executeUpdate("UPDATE PRODUCTS SET BARCODE = (SELECT CODE FROM PLUS WHERE PLUS.PRODUCT = PRODUCTS.ID)");
                LOG.log(Level.INFO, "Updated ," + res2 + " rows affected");
                int res3 = stmt.executeUpdate("DROP TABLES PLUS");
                LOG.log(Level.INFO, "Removed PLUS table");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                int res = stmt.executeUpdate("ALTER TABLE BUTTONS ADD LINK VARCHAR(50)");
                LOG.log(Level.INFO, "Added LINK to BUTTONS, " + res + " rows affected");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                String declarations = "create table \"APP\".DECLARATIONS\n"
                        + "(\n"
                        + "     ID INT not null primary key\n"
                        + "         GENERATED ALWAYS AS IDENTITY\n"
                        + "         (START WITH 1, INCREMENT BY 1),\n"
                        + "     TERMINAL INT not null references TILLS(ID),\n"
                        + "     DECLARED DOUBLE,\n"
                        + "     EXPECTED DOUBLE,\n"
                        + "     TRANSACTIONS int,\n"
                        + "     TAX DOUBLE,\n"
                        + "     STAFF INT not null references STAFF(ID),\n"
                        + "     TIME bigint\n"
                        + ")";
                stmt.executeUpdate(declarations);
                LOG.log(Level.INFO, "Created table declarations");
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                stmt.executeUpdate("ALTER TABLE PRODUCTS ADD SCALE DOUBLE");
                stmt.executeUpdate("UPDATE PRODUCTS SET SCALE=1");
                int res = stmt.executeUpdate("ALTER TABLE PRODUCTS ADD SCALE_NAME VARCHAR(20)");
                stmt.executeUpdate("UPDATE PRODUCTS SET SCALE_NAME='PRICE'");
                con.commit();
                LOG.log(Level.INFO, "Added SCALE and SCALE_NAME to PRODUCTS, " + res + " rows affected");
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                int res = stmt.executeUpdate("ALTER TABLE SALEITEMS ADD COST DOUBLE");
                con.commit();
                LOG.log(Level.INFO, "Added COST to SALEITEMS, " + res + " rows affected");
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                stmt.executeUpdate("DROP TABLE PLUS");
                con.commit();
                LOG.log(Level.INFO, "Removed PLUS table");
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                stmt.executeUpdate("ALTER TABLE PRODUCTS ADD INCVAT BOOLEAN");
                stmt.executeUpdate("UPDATE PRODUCTS SET INCVAT = FALSE");
                con.commit();
                LOG.log(Level.INFO, "Added INCVAT to PRODUCTS table and set to FALSE");
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                int r1 = stmt.executeUpdate("ALTER TABLE PRODUCTS DROP COLUMN DEPARTMENT_ID");
                int r2 = stmt.executeUpdate("ALTER TABLE CATEGORYS ADD DEPARTMENT INT REFERENCES DEPARTMENTS(ID)");
                stmt.executeUpdate("UPDATE CATEGORYS SET DEPARTMENT = 1");
                con.commit();
                LOG.log(Level.INFO, "Removed DEPARTMENT column from PRODUCTS, " + r1 + " rows affected");
                LOG.log(Level.INFO, "Added column DEPARTMENT to CATEGORYS, " + r2 + " rows affected");
                LOG.log(Level.INFO, "Set DEPARTMENT in CATEGORYS to 1, " + r2 + " rows affected");
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                int r1 = stmt.executeUpdate("ALTER TABLE WASTEREPORTS DROP COLUMN VALUE");
                int r2 = stmt.executeUpdate("ALTER TABLE WASTEITEMS ADD VALUE DOUBLE");
                con.commit();
                LOG.log(Level.INFO, "Removed VALUE from WASTEREPORTS, " + r1 + " records affected");
                LOG.log(Level.INFO, "Added VALUE to WASTEITEMS, " + r2 + " records affected");
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                String condiments = "create table \"APP\".CONDIMENTS\n"
                        + "(\n"
                        + "     ID INT not null primary key\n"
                        + "         GENERATED ALWAYS AS IDENTITY\n"
                        + "         (START WITH 1, INCREMENT BY 1),\n"
                        + "     NAME VARCHAR(30),\n"
                        + "     PRODUCT INT not null references PRODUCTS(ID),\n"
                        + "     VALUE DOUBLE,\n"
                        + "     STOCK int\n"
                        + ")";
                stmt.executeUpdate(condiments);
                con.commit();
                LOG.log(Level.INFO, "Created table CONDIMENTS");
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                int r1 = stmt.executeUpdate("ALTER TABLE PRODUCTS ADD COLUMN MAXCON INT");
                int r2 = stmt.executeUpdate("ALTER TABLE PRODUCTS ADD COLUMN MINCON INT");
                con.commit();
                LOG.log(Level.INFO, "Added MAXCON to PRODUCTS, " + r1 + " records affected");
                LOG.log(Level.INFO, "Added MINCON to PRODUCTS, " + r2 + " records affected");
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                int r1 = stmt.executeUpdate("ALTER TABLE CONDIMENTS DROP COLUMN VALUE");
                stmt.executeUpdate("ALTER TABLE CONDIMENTS DROP COLUMN STOCK");
                stmt.executeUpdate("ALTER TABLE CONDIMENTS DROP COLUMN NAME");
                stmt.executeUpdate("ALTER TABLE CONDIMENTS ADD COLUMN PRODUCT_CON INT not null references PRODUCTS(ID) DEFAULT 1");
                con.commit();
                LOG.log(Level.INFO, "Removed column VALUE from CONDIMENTS, " + r1 + " records affected");
                LOG.log(Level.INFO, "Removed column STOCK from CONDIMENTS, " + r1 + " records affected");
                LOG.log(Level.INFO, "Removed column NAME from CONDIMENTS, " + r1 + " records affected");
                LOG.log(Level.INFO, "Added column PRODUCT_CON to CONDIMENTS, " + r1 + " records affected");
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                int i1 = stmt.executeUpdate("ALTER TABLE WASTEITEMS ADD COLUMN TIMESTAMP BIGINT");
                int i2 = stmt.executeUpdate("UPDATE WASTEITEMS SET TIMESTAMP=(SELECT TIMESTAMP FROM WASTEREPORTS WHERE ID=WASTEITEMS.REPORT_ID)");
                int i3 = stmt.executeUpdate("ALTER TABLE WASTEITEMS DROP COLUMN REPORT_ID");
                int i4 = stmt.executeUpdate("DROP TABLE WASTEREPORTS");
                con.commit();
                log("Added TIMESTAMP to WASTEITEMS, " + i1 + " rows affected");
                log("Set TIMESTAMP in WASTEITEMS, " + i2 + " rows affected");
                log("Dropped column REPORT_ID in WASTEITEMS, " + i3 + " rows affected");
                log("Dropped table WASTEREPORTS, " + i4 + " rows affected");
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                String orders = "create table ORDERS\n"
                        + "(\n"
                        + "     ID INT not null primary key\n"
                        + "         GENERATED ALWAYS AS IDENTITY\n"
                        + "         (START WITH 1, INCREMENT BY 1),\n"
                        + "     SUPPLIER int not null references SUPPLIERS(ID),\n"
                        + "     SENT BOOLEAN,\n"
                        + "     SENDDATE BIGINT\n"
                        + ")";
                String orderItems = "create table ORDERITEMS\n"
                        + "(\n"
                        + "     ID INT not null primary key\n"
                        + "         GENERATED ALWAYS AS IDENTITY\n"
                        + "         (START WITH 1, INCREMENT BY 1),\n"
                        + "     PRODUCT int not null references PRODUCTS(ID),\n"
                        + "     ORDER_ID int not null references ORDERS(ID),\n"
                        + "     QUANTITY INT,\n"
                        + "     PRICE DOUBLE\n"
                        + ")";
                stmt.executeUpdate(orders);
                stmt.executeUpdate(orderItems);
                con.commit();
                log("Created table ORDERS");
                log("Created table ORDERITEMS");
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                stmt = con.createStatement();
                stmt.executeUpdate("ALTER TABLE SCREENS ADD COLUMN VGAP INT");
                stmt.executeUpdate("ALTER TABLE SCREENS ADD COLUMN HGAP INT");
                con.commit();
                log("Added VGAP to SCREENS");
                log("Added HGAP to SCREEN");
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                stmt = con.createStatement();
                stmt.executeUpdate("ALTER TABLE PRODUCTS ADD COLUMN LIMIT BIGINT");
                stmt.executeUpdate("UPDATE PRODUCTS SET LIMIT=0");
                con.commit();
                log("Added LIMIT to PRODUCTS");
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                stmt = con.createStatement();
                stmt.executeUpdate("ALTER TABLE ORDERS ADD COLUMN RECEIVED BOOLEAN");
                con.commit();
                log("Added RECEIVED to ORDERS");
            } catch (SQLException ex) {
                con.rollback();
            }
//            try {
//                stmt = con.createStatement();
//                stmt.executeUpdate("ALTER TABLE PRODUCTS ADD COLUMN SUPPLIER INTEGER");
//                stmt.executeUpdate("UPDATE PRODUCTS SET SUPPLIER = -1");
//                con.commit();
//                log("Added SUPPLIER to PRODUCTS and set to -1");
//            } catch (SQLException ex) {
//                con.rollback();
//            }
            try {
                stmt = con.createStatement();
                stmt.executeUpdate("ALTER TABLE PRODUCTS ADD COLUMN TRACK_STOCK BOOLEAN");
                stmt.executeUpdate("UPDATE PRODUCTS SET TRACK_STOCK = TRUE WHERE OPEN_PRICE = FALSE");
                stmt.executeUpdate("UPDATE PRODUCTS SET TRACK_STOCK = FALSE WHERE OPEN_PRICE = TRUE");
                con.commit();
                log("Added TRACK_STOCK to PRODUCTS");
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                stmt = con.createStatement();
                stmt.executeUpdate("ALTER TABLE WASTEREASONS ADD COLUMN PRIVILEDGE_LEVEL INT");
                stmt.executeUpdate("UPDATE WASTEREASONS SET PRIVILEDGE_LEVEL = 0");
                con.commit();
                log("Added PRIVILEDGE_LEVEL to WASTEREASONS");
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                stmt = con.createStatement();
                stmt.executeUpdate("ALTER TABLE WASTEREASONS ADD COLUMN DELETED BOOLEAN");
                stmt.executeUpdate("UPDATE WASTEREASONS SET DELETED = false");
                con.commit();
                log("Added DELETED to WASTEREASONS");
            } catch (SQLException ex) {
                con.rollback();
            }
            try {
                stmt = con.createStatement();
                stmt.executeUpdate("CREATE TABLE REFUND_REASONS\n"
                        + "(\n"
                        + "     ID INT not null primary key\n"
                        + "        GENERATED ALWAYS AS IDENTITY\n"
                        + "        (START WITH 1, INCREMENT BY 1),\n"
                        + "     REASON VARCHAR(30) NOT NULL,\n"
                        + "     LEVEL INT NOT NULL,\n"
                        + "     DELETED BOOLEAN NOT NULL\n"
                        + ")");
                con.commit();
                log("Created table REFUND_REASONS");
            } catch (SQLException ex) {
                con.rollback();
            }
            TillSplashScreen.addBar(20);
        } catch (SQLException ex) {
        }
    }

    private void log(String message) {
        LOG.log(Level.INFO, message);
    }

    /**
     * Returns a new connection to the database with auto commit disabled.
     *
     * @return new Connection.
     * @throws SQLException if there was an error getting the connection.
     */
    private Connection getConnection() throws SQLException {
        if (dbConn != null && !dbConn.isClosed()) {
            return dbConn;
        }
        dbConn = DriverManager.getConnection(address, username, password);
        dbConn.setAutoCommit(false);
        return dbConn;
    }

    @Override
    public HashMap integrityCheck() throws SQLException {
        final String query = "SELECT schemaname, tablename,\n"
                + "SYSCS_UTIL.SYSCS_CHECK_TABLE(schemaname, tablename)\n"
                + "FROM sys.sysschemas s, sys.systables t\n"
                + "WHERE s.schemaid = t.schemaid";
        final Connection con = getConnection();
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
        getConnection();
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
                + "	MAX_PRODUCT_LEVEL INTEGER,\n"
                + "     PACK_SIZE INT,\n"
                + "     BARCODE VARCHAR(15)\n"
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
                + "     INHERITS INT not null,\n"
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
                + "     COLOR VARCHAR(7),\n"
                + "     FONT_COLOR VARCHAR(7),\n"
                + "     WIDTH INT,\n"
                + "     HEIGHT INT,\n"
                + "     XPOS INT,\n"
                + "     YPOS INT,\n"
                + "     ACCESS_LEVEL INT,\n"
                + "     SCREEN_ID INT not null references SCREENS(ID),\n"
                + "     LINK VARCHAR(50)\n"
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
        String declarations = "create table \"APP\".DECLARATIONS\n"
                + "(\n"
                + "     ID INT not null primary key\n"
                + "         GENERATED ALWAYS AS IDENTITY\n"
                + "         (START WITH 1, INCREMENT BY 1),\n"
                + "     TERMINAL INT not null references TILLS(ID),\n"
                + "     DECLARED DOUBLE,\n"
                + "     EXPECTED DOUBLE,\n"
                + "     TRANSACTIONS int,\n"
                + "     TAX DOUBLE,\n"
                + "     STAFF INT not null references STAFF(ID),\n"
                + "     TIME bigint\n"
                + ")";
        Connection con = getConnection();
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
            stmt.execute(declarations);
            LOG.log(Level.INFO, "Created table declarations");
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            error(ex);
        }

        updates();

        try {
            String addDepartment = "INSERT INTO DEPARTMENTS (NAME) VALUES ('DEFAULT')";
            String addCategory = "INSERT INTO CATEGORYS (NAME, TIME_RESTRICT, MINIMUM_AGE, DEPARTMENT) VALUES ('Default','FALSE',0, 1)";
            String addTax = "INSERT INTO TAX (NAME, VALUE) VALUES ('ZERO',0.0)";
            String addReason = "INSERT INTO WASTEREASONS (REASON) VALUES ('DEFAULT')";
            stmt.executeUpdate(addDepartment);
            stmt.executeUpdate(addCategory);
            stmt.executeUpdate(addTax);
            stmt.executeUpdate(addReason);
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
        }
        Screen s = new Screen("DEFAULT", 5, 10, -1, 0, 0);
        addScreen(s);
        int x = 1;
        int y = 1;
        for (int i = 0; i < 50; i++) {
            TillButton bu = addButton(new TillButton("[SPACE]", 0, TillButton.SPACE, s.getId(), "000000", "ffffff", 1, 1, x, y, 1, ""));
            x++;
            if (x == 6) {
                x = 1;
                y++;
            }
        }
        updates();
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

    @Override
    public List<Product> getAllProducts() throws SQLException, IOException {
        String query = "SELECT * FROM PRODUCTS, CATEGORYS, DEPARTMENTS, TAX WHERE PRODUCTS.CATEGORY_ID = CATEGORYS.ID AND CATEGORYS.DEPARTMENT = DEPARTMENTS.ID AND PRODUCTS.TAX_ID = TAX.ID ORDER BY PRODUCTS.ID";
        List<Product> products;
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        try {
            ResultSet set = stmt.executeQuery(query);
            products = getProductsFromResultSet(set);
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
        for (Product p : products) {
            p.setCondiments(getProductsCondiments(p.getId()));
        }
        return products;
    }

    private List<Product> getProductsFromResultSet(ResultSet set) throws SQLException {
        List<Product> products = new LinkedList<>();
        while (set.next()) {
            int code = set.getInt(1);
            int order_code = set.getInt(2);
            String name = set.getString(3);
            boolean open = set.getBoolean(4);
            BigDecimal price = set.getBigDecimal(5);
            int stock = set.getInt(6);
            String comments = set.getString(7);
            String shortName = set.getString(8);
            int cId = set.getInt(9);
            int taxID = set.getInt(10);
            BigDecimal costPrice = set.getBigDecimal(11);
            int minStock = set.getInt(12);
            int maxStock = set.getInt(13);
            int packSize = set.getInt(14);
            String barcode = set.getString(15);
            double scale = set.getDouble(16);
            String scaleName = set.getString(17);
            boolean incVat = set.getBoolean(18);
            int maxCon = set.getInt(19);
            int minCon = set.getInt(20);
            BigDecimal limit = set.getBigDecimal(21);
            boolean trackStock = set.getBoolean(22);

            String cName = set.getString(24);
            Time start = set.getTime(25);
            Time end = set.getTime(26);
            boolean restrict = set.getBoolean(27);
            int age = set.getInt(28);
            int department = set.getInt(29);

            String dName = set.getString(31);

            Department d = new Department(department, dName);

            Category c = new Category(cId, cName, start, end, restrict, age, d);

            String tName = set.getString(33);
            double value = set.getDouble(34);

            Tax t = new Tax(taxID, tName, value);

            Product p;
            if (!open) {
                p = new Product(name, shortName, barcode, order_code, c, comments, t, price, costPrice, incVat, packSize, stock, minStock, maxStock, code, maxCon, minCon, trackStock);
            } else {
                p = new Product(name, shortName, barcode, order_code, c, comments, t, scale, scaleName, costPrice, limit, code);
            }

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
        String query = "INSERT INTO PRODUCTS (ORDER_CODE, NAME, OPEN_PRICE, PRICE, STOCK, COMMENTS, SHORT_NAME, CATEGORY_ID, TAX_ID, COST_PRICE, PACK_SIZE, MIN_PRODUCT_LEVEL, MAX_PRODUCT_LEVEL, BARCODE, SCALE, SCALE_NAME, INCVAT, LIMIT, TRACK_STOCK) VALUES (" + p.getSQLInsertString() + ")";
        Connection con = getConnection();
        try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.executeUpdate();
            ResultSet set = stmt.getGeneratedKeys();
            while (set.next()) {
                int id = set.getInt(1);
                p.setId(id);
            }
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
        return p;
    }

    @Override
    public Product updateProduct(Product p) throws SQLException, ProductNotFoundException {
        String query = p.getSQlUpdateString();
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        int value;
        try {
            value = stmt.executeUpdate(query);
            if (value == 0) {
                throw new ProductNotFoundException("Product id " + p.getId() + " could not be found");
            }
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
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
        String query = "SELECT BARCODE FROM PRODUCTS WHERE BARCODE = '" + barcode + "'";
        ResultSet res;
        Connection con = getConnection();
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
        Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("DELETE FROM WASTEITEMS WHERE PRODUCT = " + id);
            stmt.executeUpdate("DELETE FROM TRIGGERS WHERE PRODUCT=" + id);
            stmt.executeUpdate("DELETE FROM PRODUCTS WHERE PRODUCTS.ID = " + id);
            con.commit();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
            con.rollback();
            throw ex;
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
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        try {
            ResultSet res = stmt.executeQuery("SELECT ID, STOCK, MIN_PRODUCT_LEVEL FROM PRODUCTS WHERE PRODUCTS.ID=" + id);
            while (res.next()) {
                int stock = res.getInt("STOCK");
                stock -= amount;
                int minStock = res.getInt("MIN_PRODUCT_LEVEL");
                res.close();
                String update = "UPDATE PRODUCTS SET STOCK=" + stock + " WHERE PRODUCTS.ID=" + id;
                stmt.executeUpdate(update);
                if (stock < minStock) {
                    LOG.log(Level.WARNING, id + " is below minimum stock level");
                    g.logWarning("WARNING- Product " + id + " is below is minimum level!");
                }
                con.commit();
                return stock;
            }
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
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
    public Product getProduct(int code) throws SQLException, ProductNotFoundException, IOException {
        String query = "SELECT * FROM PRODUCTS, CATEGORYS, DEPARTMENTS, TAX WHERE PRODUCTS.CATEGORY_ID = CATEGORYS.ID AND CATEGORYS.DEPARTMENT = DEPARTMENTS.ID AND PRODUCTS.TAX_ID = TAX.ID AND PRODUCTS.ID=" + code;
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        List<Product> products = new LinkedList<>();
        try {
            ResultSet res = stmt.executeQuery(query);
            products = getProductsFromResultSet(res);
            con.commit();
            if (products.isEmpty()) {
                throw new ProductNotFoundException("Product " + code + " could not be found");
            }
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
        products.get(0).setCondiments(getProductsCondiments(products.get(0).getId()));
        return products.get(0);
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
        String query = "SELECT * FROM PRODUCTS, CATEGORYS, DEPARTMENTS, TAX WHERE PRODUCTS.CATEGORY_ID = CATEGORYS.ID AND CATEGORYS.DEPARTMENT = DEPARTMENTS.ID AND PRODUCTS.TAX_ID = TAX.ID AND BARCODE='" + barcode + "'";
        List<Product> products = new LinkedList<>();
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        try {
            ResultSet res = stmt.executeQuery(query);
            products = getProductsFromResultSet(res);
            con.commit();
            if (products.isEmpty()) {
                throw new ProductNotFoundException(barcode + " could not be found");
            }
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
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
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        List<Customer> customers = new LinkedList<>();
        try {
            ResultSet set = stmt.executeQuery(query);
            customers = getCustomersFromResultSet(set);
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
        return customers;
    }

    public List<Customer> getCustomersFromResultSet(ResultSet set) throws SQLException {
        List<Customer> customers = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt(1);
            String name = set.getString(2);
            String phone = set.getString(3);
            String mobile = set.getString(4);
            String email = set.getString(5);
            String address1 = set.getString(6);
            String address2 = set.getString(7);
            String town = set.getString(8);
            String county = set.getString(9);
            String country = set.getString(10);
            String postcode = set.getString(11);
            String notes = set.getString(12);
            int loyaltyPoints = set.getInt(13);
            BigDecimal moneyDue = set.getBigDecimal(14);
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
        Connection con = getConnection();
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

    @Override
    public Customer updateCustomer(Customer c) throws SQLException, CustomerNotFoundException {
        c = (Customer) Encryptor.encrypt(c);
        String query = c.getSQLUpdateString();
        Connection con = getConnection();
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

    @Override
    public void removeCustomer(Customer c) throws SQLException, CustomerNotFoundException {
        removeCustomer(c.getId());
    }

    @Override
    public void removeCustomer(int id) throws SQLException, CustomerNotFoundException {
        String query = "DELETE FROM CUSTOMERS WHERE CUSTOMERS.ID = " + id;
        Connection con = getConnection();
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

    @Override
    public Customer getCustomer(int id) throws SQLException, CustomerNotFoundException {
        String query = "SELECT * FROM CUSTOMERS WHERE CUSTOMERS.ID = " + id;
        Connection con = getConnection();
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

    @Override
    public List<Customer> getCustomerByName(String name) throws SQLException, CustomerNotFoundException {
        String query = "SELECT * FROM CUSTOMERS WHERE CUSTOMERS.NAME = " + name;
        Connection con = getConnection();
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

    @Override
    public List<Customer> customerLookup(String terms) throws IOException, SQLException {
        String query = "SELECT * FROM CUSTOMERS";
        Connection con = getConnection();
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

    //Staff Methods
    @Override
    public List<Staff> getAllStaff() throws SQLException {
        String query = "SELECT * FROM STAFF";
        Connection con = getConnection();
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

        return staff;
    }

    public List<Staff> getStaffFromResultSet(ResultSet set) throws SQLException {
        List<Staff> staff = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt(1);
            String name = set.getString(2);
            int position = set.getInt(3);
            String uname = set.getString(4);
            String pword = set.getString(5);
            String dPass = Encryptor.decrypt(pword);
            boolean enabled = set.getBoolean(6);
            double wage = set.getDouble(7);
            Staff s = new Staff(id, name, position, uname, dPass, wage, enabled);

            staff.add(s);
        }
        return staff;
    }

    @Override
    public Staff addStaff(Staff s) throws SQLException {
        String query = "INSERT INTO STAFF (NAME, POSITION, USERNAME, PASSWORD, ENABLED, WAGE) VALUES (" + s.getSQLInsertString() + ")";
        Connection con = getConnection();
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

    @Override
    public Staff updateStaff(Staff s) throws SQLException, StaffNotFoundException {
        String query = s.getSQLUpdateString();
        Connection con = getConnection();
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

    @Override
    public void removeStaff(Staff s) throws SQLException, StaffNotFoundException {
        removeStaff(s.getId());
    }

    @Override
    public void removeStaff(int id) throws SQLException, StaffNotFoundException {
        String query = "DELETE FROM STAFF WHERE STAFF.ID = " + id;
        Connection con = getConnection();
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

    @Override
    public Staff getStaff(int id) throws SQLException, StaffNotFoundException {
        String query = "SELECT * FROM STAFF WHERE STAFF.ID = " + id;
        Connection con = getConnection();
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

    @Override
    public Staff login(String username, String password) throws SQLException, LoginException {
        String query = "SELECT * FROM STAFF WHERE STAFF.USERNAME = '" + username.toLowerCase() + "'";
        Connection con = getConnection();
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

    @Override
    public int getStaffCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM STAFF";
        Connection con = getConnection();
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

    //Discount Methods
    @Override
    public List<Discount> getAllDiscounts() throws SQLException {
        String query = "SELECT * FROM DISCOUNTS";
        Connection con = getConnection();
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
        return discounts;
    }

    public List<Discount> getDiscountsFromResultSet(ResultSet set) throws SQLException {
        List<Discount> discounts = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt(1);
            String name = set.getString(2);
            double percentage = set.getDouble(3);
            BigDecimal price = new BigDecimal(Double.toString(set.getDouble(4)));
            int a = set.getInt(5);
            int c = set.getInt(6);
            long start = set.getLong(7);
            long end = set.getLong(8);
            Discount d = new Discount(id, name, percentage, price, a, c, start, end);

            discounts.add(d);
        }
        return discounts;
    }

    @Override
    public Discount addDiscount(Discount d) throws SQLException {
        String query = "INSERT INTO DISCOUNTS (NAME, PERCENTAGE, PRICE, ACTION, CONDITION, STARTT, ENDT) VALUES (" + d.getSQLInsertString() + ")";
        Connection con = getConnection();
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

    @Override
    public Discount updateDiscount(Discount d) throws SQLException, DiscountNotFoundException {
        String query = d.getSQLUpdateString();
        Connection con = getConnection();
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

    @Override
    public void removeDiscount(Discount d) throws SQLException, DiscountNotFoundException {
        removeDiscount(d.getId());
    }

    @Override
    public void removeDiscount(int id) throws SQLException, DiscountNotFoundException {
        String query = "DELETE FROM DISCOUNTS WHERE DISCOUNTS.ID = " + id;
        Connection con = getConnection();
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

    @Override
    public Discount getDiscount(int id) throws SQLException, DiscountNotFoundException {
        String query = "SELECT * FROM DISCOUNTS WHERE DISCOUNTS.ID = " + id;
        Connection con = getConnection();
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

    //Tax Methods
    @Override
    public List<Tax> getAllTax() throws SQLException {
        String query = "SELECT * FROM TAX";
        Connection con = getConnection();
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

        return tax;
    }

    public List<Tax> getTaxFromResultSet(ResultSet set) throws SQLException {
        List<Tax> tax = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt(1);
            String name = set.getString(2);
            double value = set.getDouble(3);
            Tax t = new Tax(id, name, value);

            tax.add(t);
        }
        return tax;
    }

    @Override
    public Tax addTax(Tax t) throws SQLException {
        String query = "INSERT INTO TAX (NAME, VALUE) VALUES (" + t.getSQLInsertString() + ")";
        Connection con = getConnection();
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
        return t;
    }

    @Override
    public Tax updateTax(Tax t) throws SQLException, JTillException {
        String query = t.getSQLUpdateString();
        Connection con = getConnection();
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
        return t;
    }

    @Override
    public void removeTax(Tax t) throws SQLException, JTillException {
        removeTax(t.getId());
    }

    @Override
    public void removeTax(int id) throws SQLException, JTillException {
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        int value;
        try {
            stmt.executeUpdate("UPDATE PRODUCTS SET TAX_ID = 1 WHERE TAX_ID = " + id);
            value = stmt.executeUpdate("DELETE FROM TAX WHERE TAX.ID = " + id);
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

    @Override
    public Tax getTax(int id) throws SQLException, JTillException {
        String query = "SELECT * FROM TAX WHERE TAX.ID = " + id;
        Connection con = getConnection();
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

    @Override
    public List<Product> getProductsInTax(int id) throws SQLException {
        String query = "SELECT * FROM PRODUCTS, CATEGORYS, DEPARTMENTS, TAX WHERE PRODUCTS.CATEGORY_ID = CATEGORYS.ID AND CATEGORYS.DEPARTMENT = DEPARTMENTS.ID AND PRODUCTS.TAX_ID = TAX.ID AND TAX_ID = " + id;
        Connection con = getConnection();
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

    //Category Methods
    @Override
    public List<Category> getAllCategorys() throws SQLException {
        String query = "SELECT * FROM CATEGORYS, DEPARTMENTS WHERE CATEGORYS.DEPARTMENT = DEPARTMENTS.ID";
        Connection con = getConnection();
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
        return categorys;
    }

    public List<Category> getCategorysFromResultSet(ResultSet set) throws SQLException {
        List<Category> categorys = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt(1);
            String name = set.getString(2);
            Time startSell = set.getTime(3);
            Time endSell = set.getTime(4);
            boolean timeRestrict = set.getBoolean(5);
            int minAge = set.getInt(6);
            int department = set.getInt(7);

            String dName = set.getString(9);

            Department d = new Department(department, dName);
            Category c = new Category(id, name, startSell, endSell, timeRestrict, minAge, d);
            categorys.add(c);
        }
        return categorys;
    }

    @Override
    public Category addCategory(Category c) throws SQLException {
        String query = "INSERT INTO CATEGORYS (NAME, SELL_START, SELL_END, TIME_RESTRICT, MINIMUM_AGE, DEPARTMENT) VALUES (" + c.getSQLInsertString() + ")";
        Connection con = getConnection();
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
        return c;
    }

    @Override
    public Category updateCategory(Category c) throws SQLException, JTillException {
        String query = c.getSQLUpdateString();
        Connection con = getConnection();
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
        return c;
    }

    @Override
    public void removeCategory(Category c) throws SQLException, JTillException {
        removeCategory(c.getId());
    }

    @Override
    public void removeCategory(int id) throws SQLException, JTillException {
        if (id == 1) {
            throw new JTillException("Cannot remove the Default category");
        }
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        int value;
        try {
            stmt.executeUpdate("UPDATE PRODUCTS SET CATEGORY_ID=1 WHERE CATEGORY_ID=" + id);
            value = stmt.executeUpdate("DELETE FROM CATEGORYS WHERE CATEGORYS.ID = " + id);
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

    @Override
    public Category getCategory(int id) throws SQLException, JTillException {
        String query = "SELECT * FROM CATEGORYS, DEPARTMENTS WHERE CATEGORYS.DEPARTMENT = DEPARTMENTS.ID AND CATEGORYS.ID = " + id;
        Connection con = getConnection();
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

    @Override
    public List<Product> getProductsInCategory(int id) throws SQLException {
        String query = "SELECT * FROM PRODUCTS, CATEGORYS, DEPARTMENTS, TAX WHERE PRODUCTS.CATEGORY_ID = CATEGORYS.ID AND CATEGORYS.DEPARTMENT = DEPARTMENTS.ID AND PRODUCTS.TAX_ID = TAX.ID AND CATEGORYS.ID = " + id;
        Connection con = getConnection();
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

    private List<Sale> getSalesFromResultSet(ResultSet set) throws SQLException {
        List<Sale> sales = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt(1);
            BigDecimal price = new BigDecimal(Double.toString(set.getDouble(2)));
            int cid = set.getInt(3);
            Date date = new Date(set.getLong(4));
            boolean cashed = set.getBoolean(6);

            int tid = set.getInt(9);
            UUID uuid = UUID.fromString(set.getString(10));
            String tname = set.getString(11);
            BigDecimal uncashed = set.getBigDecimal(12);
            int sc = set.getInt(13);
            final Till t = new Till(tname, uncashed, tid, uuid, sc);

            int stid = set.getInt(14);
            String stname = set.getString(15);
            int position = set.getInt(16);
            String uname = set.getString(17);
            String pword = set.getString(18);
            String dPass = Encryptor.decrypt(pword);
            boolean enabled = set.getBoolean(19);
            double wage = set.getDouble(20);
            final Staff st = new Staff(stid, stname, position, uname, dPass, wage, enabled);

            final Sale s = new Sale(id, price, null, date, t, cashed, st);
            sales.add(s);
        }
        return sales;
    }

    @Override
    public Sale addSale(Sale s) throws SQLException, IOException {
        String query = "INSERT INTO SALES (PRICE, CUSTOMER, TIMESTAMP, TERMINAL, CASHED, STAFF, MOP) VALUES (" + s.getSQLInsertStatement() + ")";
        Connection con = getConnection();
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
                final Customer c = s.getCustomer();
                chargeCustomerAccount(c, s.getTotal());
            }
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
        for (SaleItem p : s.getSaleItems()) {
            addSaleItem(s, p);
            try {
                if (p.getType() == SaleItem.PRODUCT) {
                    final Product pr = (Product) p.getItem();
                    if (!pr.getSaleCondiments().isEmpty()) {
                        for (Condiment c : pr.getSaleCondiments()) {
                            purchaseProduct(c.getProduct(), 1);
                        }
                    }
                    if (!pr.isOpen() || pr.isTrackStock()) {
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
        String query = "SELECT * FROM SALES s, TILLS t, STAFF st WHERE st.ID = s.STAFF AND s.TERMINAL = t.ID";
        List<Sale> sales = new LinkedList<>();
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        try {
            ResultSet set = stmt.executeQuery(query);
            sales = getSalesFromResultSet(set);
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
        for (Sale s : sales) {
            s.setProducts(getItemsInSale(s));
        }
        return sales;
    }

    @Override
    public List<Sale> getUncashedSales(String t) throws SQLException {
        final String query = "SELECT * FROM SALES s, TILLS t, STAFF st , SaleItems si WHERE st.ID = s.STAFF AND CASHED = FALSE AND si.SALE_ID = s.ID AND s.TERMINAL = t.ID AND t.NAME = '" + t + "'";
        Connection con = getConnection();
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

    private List<SaleItem> getItemsInSale(Sale sale) throws SQLException {
        final String query = "SELECT * FROM SALEITEMS, PRODUCTS, CATEGORYS, DEPARTMENTS, TAX WHERE SALEITEMS.PRODUCT_ID = PRODUCTS.ID AND CATEGORYS.DEPARTMENT = DEPARTMENTS.ID AND PRODUCTS.CATEGORY_ID = CATEGORYS.ID AND PRODUCTS.TAX_ID = TAX.ID AND SALEITEMS.SALE_ID = " + sale.getId();
        Connection con = getConnection();
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

    private List<SaleItem> getSaleItemsFromResultSet(ResultSet set) throws SQLException {
        final List<SaleItem> sales = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt(1);
            int item = set.getInt(2);
            int type = set.getInt(3);
            int quantity = set.getInt(4);
            BigDecimal price = set.getBigDecimal(5);
            BigDecimal tax = set.getBigDecimal(6);
            int saleId = set.getInt(7);
            BigDecimal cost = set.getBigDecimal(8);

            int code = set.getInt(9);
            int order_code = set.getInt(10);
            String name = set.getString(11);
            boolean open = set.getBoolean(12);
            BigDecimal pPrice = set.getBigDecimal(13);
            int stock = set.getInt(14);
            String comments = set.getString(15);
            String shortName = set.getString(16);
            int cId = set.getInt(17);
            int taxID = set.getInt(18);
            BigDecimal costPrice = set.getBigDecimal(19);
            int minStock = set.getInt(20);
            int maxStock = set.getInt(21);
            int packSize = set.getInt(22);
            String barcode = set.getString(23);
            double scale = set.getDouble(24);
            String scaleName = set.getString(25);
            boolean incVat = set.getBoolean(26);
            int maxCon = set.getInt(27);
            int minCon = set.getInt(28);
            BigDecimal limit = set.getBigDecimal(29);
            boolean trackStock = set.getBoolean(30);

            String cName = set.getString(32);
            Time start = set.getTime(33);
            Time end = set.getTime(34);
            boolean restrict = set.getBoolean(35);
            int age = set.getInt(36);
            int department = set.getInt(37);

            String dName = set.getString(39);

            Department d = new Department(department, dName);

            Category c = new Category(cId, cName, start, end, restrict, age, d);

            String tName = set.getString(41);
            double value = set.getDouble(42);

            Tax t = new Tax(taxID, tName, value);

            Product p;
            if (!open) {
                p = new Product(name, shortName, barcode, order_code, c, comments, t, price, costPrice, incVat, packSize, stock, minStock, maxStock, code, maxCon, minCon, trackStock);
            } else {
                p = new Product(name, shortName, barcode, order_code, c, comments, t, scale, scaleName, costPrice, limit, code);
            }

            SaleItem s = new SaleItem(saleId, p, quantity, id, price, type, tax, cost);
            sales.add(s);
        }
        return sales;
    }

    @Override
    public Sale getSale(int id) throws SQLException, JTillException {
        String query = "SELECT * FROM SALES s, CUSTOMERS c, TILLS t, STAFF st, SaleItems si WHERE c.ID = s.CUSTOMER AND st.ID = s.STAFF AND CASHED = FALSE AND si.SALE_ID = s.ID AND s.TERMINAL = t.ID AND s.ID = " + id;
        Connection con = getConnection();
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

    @Override
    public Sale updateSale(Sale s) throws SQLException, JTillException {
        String query = s.getSQLUpdateStatement();
        Connection con = getConnection();
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
        return s;
    }

    public Sale updateSaleNoSem(Sale s) throws SQLException, JTillException {
        String query = s.getSQLUpdateStatement();
        Connection con = getConnection();
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
        try {
            if (dbConn != null && !dbConn.isClosed()) {
                return "Connected to database " + this.address + "\nOn user " + this.username;
            } else {
                return "Not connected to database";
            }
        } catch (SQLException ex) {
            return "NOT CONNECTED";
        }
    }

    @Override
    public Staff tillLogin(int id) throws IOException, LoginException, SQLException {
        String query = "SELECT * FROM STAFF WHERE STAFF.ID = " + id;
        Connection con = getConnection();
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
            int inherits = set.getInt("INHERITS");
            int vgap = set.getInt("VGAP");
            int hgap = set.getInt("HGAP");
            Screen s = new Screen(name, id, width, height, inherits, vgap, hgap);

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
            String color = set.getString("COLOR");
            String fontColor = set.getString("FONT_COLOR");
            int width = set.getInt("WIDTH");
            int height = set.getInt("HEIGHT");
            int x = set.getInt("XPOS");
            int y = set.getInt("YPOS");
            int accessLevel = set.getInt("ACCESS_LEVEL");
            String link = set.getString("LINK");
            TillButton b = new TillButton(name, p, type, s, color, fontColor, id, width, height, x, y, accessLevel, link);

            buttons.add(b);
        }

        return buttons;
    }

    @Override
    public Screen addScreen(Screen s) throws SQLException {
        String query = "INSERT INTO SCREENS (NAME, WIDTH, HEIGHT, INHERITS, VGAP, HGAP) VALUES (" + s.getSQLInsertString() + ")";
        Connection con = getConnection();
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

    @Override
    public TillButton addButton(TillButton b) throws SQLException {
        String query = "INSERT INTO BUTTONS (NAME, PRODUCT, TYPE, COLOR, FONT_COLOR, SCREEN_ID, WIDTH, HEIGHT, XPOS, YPOS, ACCESS_LEVEL, LINK) VALUES (" + b.getSQLInsertString() + ")";
        Connection con = getConnection();
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
        return b;
    }

    @Override
    public void removeScreen(Screen s) throws SQLException, ScreenNotFoundException {
        String query = "DELETE FROM SCREENS WHERE SCREENS.ID = " + s.getId();
        String buttonsQuery = "DELETE FROM BUTTONS WHERE BUTTONS.SCREEN_ID = " + s.getId();
        String bq2 = "UPDATE BUTTONS SET BUTTONS.TYPE=" + TillButton.SPACE + " WHERE BUTTONS.TYPE=" + TillButton.SCREEN + " AND BUTTONS.PRODUCT=" + s.getId();
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        int value;
        try {
            stmt.executeUpdate(bq2);
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

    @Override
    public void removeButton(TillButton b) throws SQLException, JTillException {
        String query = "DELETE FROM BUTTONS WHERE BUTTONS.ID = " + b.getId();
        Connection con = getConnection();
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

    @Override
    public Screen getScreen(int s) throws SQLException, ScreenNotFoundException {
        String query = "SELECT * FROM SCREENS WHERE SCREENS.ID = " + s;
        Connection con = getConnection();
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

    @Override
    public TillButton getButton(int b) throws SQLException, JTillException {
        String query = "SELECT * FROM SCREENS WHERE BUTTONS.ID = " + b;
        Connection con = getConnection();
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

    @Override
    public Screen updateScreen(Screen s) throws SQLException, ScreenNotFoundException {
        String query = s.getSQLUpdateString();
        Connection con = getConnection();
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
        return s;
    }

    @Override
    public TillButton updateButton(TillButton b) throws SQLException, JTillException {
        String query = b.getSQLUpdateString();
        Connection con = getConnection();
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
        return b;
    }

    @Override
    public List<Screen> getAllScreens() throws SQLException {
        String query = "SELECT * FROM SCREENS";
        Connection con = getConnection();
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
                int inherits = set.getInt("INHERITS");
                int vgap = set.getInt("VGAP");
                int hgap = set.getInt("HGAP");
                Screen s = new Screen(name, id, width, height, inherits, vgap, hgap);

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

    @Override
    public List<TillButton> getAllButtons() throws SQLException {
        String query = "SELECT * FROM BUTTONS";
        Connection con = getConnection();
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
                String color = set.getString("COLOR");
                String fontColor = set.getString("FONT_COLOR");
                int s = set.getInt("SCREEN_ID");
                int width = set.getInt("WIDTH");
                int height = set.getInt("HEIGHT");
                int x = set.getInt("XPOS");
                int y = set.getInt("YPOS");
                String link = set.getString("LINK");
                TillButton b = new TillButton(name, p, type, s, color, fontColor, id, width, height, x, y, link);
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

    @Override
    public List<TillButton> getButtonsOnScreen(Screen s) throws IOException, SQLException, ScreenNotFoundException {
        String query = "SELECT * FROM BUTTONS WHERE BUTTONS.SCREEN_ID=" + s.getId();
        Connection con = getConnection();
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
                String color = set.getString("COLOR");
                String fontColor = set.getString("FONT_COLOR");
                int width = set.getInt("WIDTH");
                int height = set.getInt("HEIGHT");
                int x = set.getInt("XPOS");
                int y = set.getInt("YPOS");
                int accessLevel = set.getInt("ACCESS_LEVEL");
                String link = set.getString("LINK");

                TillButton b = new TillButton(name, i, type, s.getId(), color, fontColor, id, width, height, x, y, accessLevel, link);

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
        final Staff staff = sale.getStaff();
        text += "You were served by " + staff.getName() + "\n";
        text += "Thank you for your custom";

        message.setFrom(new InternetAddress(outgoing_email));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
        message.setSubject("Receipt for sale " + sale.getId());
        message.setText(text);
        Transport.send(message);
        return true;
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
        Connection con = getConnection();
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
        return t;
    }

    @Override
    public void removeTill(int id) throws IOException, SQLException, JTillException {
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        int value;
        try {
            stmt.executeUpdate("DELETE FROM DECLARATIONS WHERE TERMINAL=" + id);
            stmt.executeUpdate("DELETE FROM SALEITEMS WHERE SALE_ID IN(SELECT ID FROM SALES WHERE ID=SALEITEMS.SALE_ID)");
            stmt.executeUpdate("DELETE FROM SALES WHERE TERMINAL=" + id);
            value = stmt.executeUpdate("DELETE FROM TILLS WHERE TILLS.ID=" + id);
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

    @Override
    public Till getTill(int id) throws IOException, SQLException, JTillException {
        String query = "SELECT * FROM TILLS WHERE TILLS.ID = " + id;
        Connection con = getConnection();
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

    @Override
    public List<Till> getAllTills() throws SQLException {
        String query = "SELECT * FROM TILLS";
        Connection con = getConnection();
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

    @Override
    public boolean isTillConnected(int id) {
        for (JConnThread th : TillServer.server.getClientConnections()) {
            ConnectionHandler hand = (ConnectionHandler) th.getMethodClass();
            if (hand.till != null && id == hand.till.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Till connectTill(String name, UUID uuid, Staff staff) throws JTillException {
        try {
            if (isTillConnected(uuid)) {
                LOG.log(Level.WARNING, "Terminal already connected");
                throw new JTillException("This till is already connected to the server");
            }
            Till till;
            try {
                till = this.getTillByUUID(uuid);
            } catch (JTillException ex) {
                LOG.log(Level.INFO, "New terminal connected");
                till = newTill(name, uuid);
            }
            LOG.log(Level.INFO, "Terminal " + name + " has connected");
            till.setConnected(true);
            g.addTill(till);
            return till;
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "There has been an error adding a till to the database", ex);
        }
        return null;
    }

    private Till newTill(String name, UUID uuid) throws JTillException {
        Till till = g.showTillSetupWindow(name, uuid);
        if (till != null) { //If the connection was allowed
            try {
                addTill(till);
            } catch (IOException | SQLException ex) {
                LOG.log(Level.SEVERE, "There has been an error connecting a till the server", ex);
            }
            return till;
        }
        return null;
    }

    private boolean isTillConnected(UUID uuid) {
        for (Till t : getConnectedTills()) {
            if (t.getUuid().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Till> getConnectedTills() {
        List<Till> tills = new LinkedList<>();
        for (JConnThread th : TillServer.server.getClientConnections()) {
            ConnectionHandler h = (ConnectionHandler) th.getMethodClass();
            if (h.till == null) {
                continue;
            }
            tills.add(h.till);
        }
        return tills;
    }

    private Till getTillByUUID(UUID uuid) throws SQLException, JTillException {
        String query = "SELECT * FROM TILLS WHERE TILLS.UUID = '" + uuid.toString() + "'";
        Connection con = getConnection();
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

    @Override
    public void setSetting(String key, String value) {
        systemSettings.setSetting(key, value);
    }

    @Override
    public String getSetting(String key) {
        return systemSettings.getSetting(key);
    }

    @Override
    public boolean isTillLoggedIn(Staff s) throws IOException, StaffNotFoundException, SQLException {
        return loggedIn.contains(s);
    }

    @Override
    public boolean checkUsername(String username) throws IOException, SQLException {
        String query = "SELECT * FROM STAFF WHERE USERNAME='" + username.toLowerCase() + "'";
        Connection con = getConnection();
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

    @Override
    public String getSetting(String key, String value) throws IOException {
        return systemSettings.getSetting(key, value);
    }

    @Override
    public void addWasteReport(List<WasteItem> items) throws IOException, SQLException, JTillException {
        Connection con = getConnection();
        try {
            for (WasteItem i : items) {
                PreparedStatement stmt = con.prepareStatement("INSERT INTO APP.WASTEITEMS (PRODUCT, QUANTITY, REASON, VALUE, TIMESTAMP) values (" + i.getProduct().getId() + "," + i.getQuantity() + "," + i.getReason().getId() + "," + i.getTotalValue() + "," + i.getTimestamp().getTime() + ")", Statement.RETURN_GENERATED_KEYS);
                stmt.executeUpdate();
                ResultSet set = stmt.getGeneratedKeys();
                while (set.next()) {
                    int id = set.getInt(1);
                    i.setId(id);
                }
            }
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    private List<WasteItem> getWasteItemsFromResultSet(ResultSet set) throws SQLException, IOException {
        List<WasteItem> wis = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt(1);
            int pid = set.getInt(2);
            int quantity = set.getInt(3);
            int wreason = set.getInt(4);
            BigDecimal value = set.getBigDecimal(5);
            Date date = new Date(set.getLong(6));

            String reason = set.getString(8);
            int level = set.getInt(9);

            int code = set.getInt(11);
            int order_code = set.getInt(12);
            String name = set.getString(13);
            boolean open = set.getBoolean(14);
            BigDecimal price = set.getBigDecimal(15);
            int stock = set.getInt(16);
            String comments = set.getString(17);
            String shortName = set.getString(18);
            int cId = set.getInt(19);
            int taxID = set.getInt(20);
            BigDecimal costPrice = set.getBigDecimal(21);
            int minStock = set.getInt(22);
            int maxStock = set.getInt(23);
            int packSize = set.getInt(24);
            String barcode = set.getString(25);
            double scale = set.getDouble(26);
            String scaleName = set.getString(27);
            boolean incVat = set.getBoolean(28);
            int maxCon = set.getInt(29);
            int minCon = set.getInt(30);
            BigDecimal limit = set.getBigDecimal(31);
            boolean trackStock = set.getBoolean(32);

            String cName = set.getString(34);
            Time start = set.getTime(35);
            Time end = set.getTime(36);
            boolean restrict = set.getBoolean(37);
            int age = set.getInt(38);
            int department = set.getInt(39);

            String dName = set.getString(41);

            Department d = new Department(department, dName);

            Category c = new Category(cId, cName, start, end, restrict, age, d);

            String tName = set.getString(43);
            double tValue = set.getDouble(44);

            Tax t = new Tax(taxID, tName, tValue);

            Product p;
            if (!open) {
                p = new Product(name, shortName, barcode, order_code, c, comments, t, price, costPrice, incVat, packSize, stock, minStock, maxStock, code, maxCon, minCon, trackStock);
            } else {
                p = new Product(name, shortName, barcode, order_code, c, comments, t, scale, scaleName, costPrice, limit, code);
            }

            WasteReason wr = new WasteReason(wreason, reason, level);
            wis.add(new WasteItem(id, p, quantity, wr, value, date));
        }
        return wis;
    }

    @Override
    public WasteItem addWasteItem(WasteItem wi) throws IOException, SQLException, JTillException {
        String query = "INSERT INTO APP.WASTEITEMS (PRODUCT, QUANTITY, REASON, VALUE, TIMESTAMP) values (" + wi.getProduct().getId() + "," + wi.getQuantity() + "," + wi.getReason() + "," + wi.getTotalValue() + "," + wi.getTimestamp().getTime() + ")";
        Connection con = getConnection();
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
        return wi;
    }

    @Override
    public void removeWasteItem(int id) throws IOException, SQLException, JTillException {
        String query = "DELETE FROM WASTEITEMS WHERE ID=" + id;
        Connection con = getConnection();
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

    @Override
    public WasteItem getWasteItem(int id) throws IOException, SQLException, JTillException {
        String query = "SELECT * FROM WASTEITEMS, WASTEREASONS WHERE WASTEITEMS.REASON = WASTEREASONS.ID AND WASTEITEMS.ID=" + id;
        Connection con = getConnection();
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

    @Override
    public List<WasteItem> getAllWasteItems() throws IOException, SQLException {
        String query = "SELECT * FROM WASTEITEMS, WASTEREASONS, PRODUCTS, CATEGORYS, DEPARTMENTS, TAX WHERE PRODUCTS.CATEGORY_ID = CATEGORYS.ID AND CATEGORYS.DEPARTMENT = DEPARTMENTS.ID AND PRODUCTS.TAX_ID = TAX.ID AND WASTEITEMS.REASON = WASTEREASONS.ID AND WASTEITEMS.PRODUCT = PRODUCTS.ID";
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        List<WasteItem> wis = new LinkedList<>();
        try {
            ResultSet set = stmt.executeQuery(query);
            wis = getWasteItemsFromResultSet(set);
            con.commit();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
            con.rollback();
            throw ex;
        }

        return wis;
    }

    private List<WasteReason> getWasteReasonsFromResultSet(ResultSet set) throws SQLException {
        List<WasteReason> wrs = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt("ID");
            String reason = set.getString("REASON");
            int level = set.getInt("PRIVILEDGE_LEVEL");
            wrs.add(new WasteReason(id, reason, level));
        }
        return wrs;
    }

    @Override
    public WasteReason addWasteReason(WasteReason wr) throws IOException, SQLException {
        String query = "INSERT INTO APP.WASTEREASONS (REASON, DELETED) values ('" + wr.getReason() + "', false)";
        Connection con = getConnection();
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
        return wr;
    }

    @Override
    public void removeWasteReason(int id) throws IOException, SQLException, JTillException {
        String query = "DELETE FROM WATSEREASONS WHERE ID=" + id;
        Connection con = getConnection();
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

    @Override
    public WasteReason getWasteReason(int id) throws IOException, SQLException, JTillException {
        String query = "SELECT * FROM WASTEREASONS WHERE ID=" + id;
        Connection con = getConnection();
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

    @Override
    public List<WasteReason> getAllWasteReasons() throws IOException, SQLException {
        String query = "SELECT * FROM WASTEREASONS";
        Connection con = getConnection();
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

    @Override
    public List<WasteReason> getUsedWasteReasons() throws IOException, SQLException {
        String query = "SELECT * FROM WASTEREASONS WHERE DELETED = false";
        Connection con = getConnection();
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

    @Override
    public WasteReason updateWasteReason(WasteReason wr) throws IOException, SQLException, JTillException {
        String query = "UPDATE WASTEREASONS SET REASON='" + wr.getReason() + "', PRIVILEDGE_LEVEL=" + wr.getPriviledgeLevel() + " WHERE ID=" + wr.getId();
        Connection con = getConnection();
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
        return wr;
    }

    @Override
    public Supplier addSupplier(Supplier s) throws IOException, SQLException {
        String query = "INSERT INTO SUPPLIERS (NAME, ADDRESS, PHONE) VALUES ('" + s.getName() + "','" + s.getAddress() + "','" + s.getContactNumber() + "')";
        Connection con = getConnection();
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

    @Override
    public void removeSupplier(int id) throws IOException, SQLException, JTillException {
        String query = "DELETE FROM SUPPLIERS WHERE ID=" + id;
        Connection con = getConnection();
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

    @Override
    public Supplier getSupplier(int id) throws IOException, SQLException, JTillException {
        String query = "SELECT * FROM SUPPLIERS WHERE ID=" + id;
        Connection con = getConnection();
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

    @Override
    public List<Supplier> getAllSuppliers() throws IOException, SQLException {
        String query = "SELECT * FROM SUPPLIERS";
        Connection con = getConnection();
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

    @Override
    public Supplier updateSupplier(Supplier s) throws IOException, SQLException, JTillException {
        String query = "UPDATE SUPPLIERS SET NAME='" + s.getName() + "', ADDRESS='" + s.getAddress() + "', PHONE='" + s.getContactNumber() + "' WHERE ID=" + s.getId();
        Connection con = getConnection();
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

    @Override
    public Department addDepartment(Department d) throws IOException, SQLException {
        String query = "INSERT INTO DEPARTMENTS (NAME) VALUES('" + d.getName() + "')";
        Connection con = getConnection();
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

    @Override
    public void removeDepartment(int id) throws IOException, SQLException, JTillException {
        if (id == 1) {
            throw new JTillException("Cannot remove the Default department");
        }
        Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("UPDATE CATEGORYS SET DEPARTMENT = 1 WHERE DEPARTMENT = " + id);
            int value = stmt.executeUpdate("DELETE FROM DEPARTMENTS WHERE ID=" + id);
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

    @Override
    public Department getDepartment(int id) throws SQLException, JTillException {
        String query = "SELECT * FROM DEPARTMENTS WHERE ID=" + id;
        Connection con = getConnection();
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

    @Override
    public List<Department> getAllDepartments() throws IOException, SQLException {
        String query = "SELECT * FROM DEPARTMENTS";
        Connection con = getConnection();
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

    @Override
    public Department updateDepartment(Department d) throws IOException, SQLException, JTillException {
        String query = "UPDATE DEPARTMENTS SET NAME='" + d.getName() + "' WHERE ID=" + d.getId();
        Connection con = getConnection();
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

    @Override
    public SaleItem addSaleItem(Sale s, SaleItem i) throws IOException, SQLException {
        i.setSale(s.getId());
        String query = "INSERT INTO SALEITEMS (PRODUCT_ID, TYPE, QUANTITY, PRICE, TAX, SALE_ID, COST) VALUES(" + i.getSQLInsertStatement() + ")";
        Connection con = getConnection();
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

    @Override
    public void removeSaleItem(int id) throws IOException, SQLException, JTillException {
        String query = "DELETE FROM SALEITEMS WHERE ID=" + id;
        Connection con = getConnection();
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

    @Override
    public SaleItem getSaleItem(int id) throws IOException, SQLException, JTillException {
        String query = "SELECT * FROM SALEITEMS, PRODUCTS, CATEGORYS, DEPARTMENTS, TAX WHERE SALEITEMS.PRODUCT = PRODUCTS.ID AND PRODUCTS.DEPARTNENT_ID = DEPARTMENTS.ID AND PRODUCTS.CATEGORY_ID = CATEGORYS.ID AND PRODUCTS.TAX_ID = TAX.ID AND ID = " + id;
        SaleItem i = null;
        Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery(query);
            List<SaleItem> items = getSaleItemsFromResultSet(set);
            con.commit();
            if (items.isEmpty()) {
                throw new JTillException("Item not found");
            }
            return items.get(0);
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public List<SaleItem> getAllSaleItems() throws IOException, SQLException {
        String query = "SELECT * FROM SALEITEMS, PRODUCTS, CATEGORYS, DEPARTMENTS, TAX WHERE SALEITEMS.PRODUCT = PRODUCTS.ID AND PRODUCTS.DEPARTNENT_ID = DEPARTMENTS.ID AND PRODUCTS.CATEGORY_ID = CATEGORYS.ID AND PRODUCTS.TAX_ID = TAX.ID AND SALEITEMS.TYPE = 1";
        List<SaleItem> items = new LinkedList<>();
        Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery(query);
            items = getSaleItemsFromResultSet(set);
            con.commit();
            return items;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            if (ex instanceof SQLException) {
                throw (SQLException) ex;
            } else {
                throw new SQLException(ex.getMessage());
            }
        }
    }

    @Override
    public List<SaleItem> submitSaleItemQuery(String q) throws SQLException {
        String query = "SELECT * FROM SALEITEMS, PRODUCTS, CATEGORYS, DEPARTMENTS, TAX WHERE SALEITEMS.PRODUCT = PRODUCTS.ID AND PRODUCTS.DEPARTNENT_ID = DEPARTMENTS.ID AND PRODUCTS.CATEGORY_ID = CATEGORYS.ID AND PRODUCTS.TAX_ID = TAX.ID AND " + q;
        List<SaleItem> items = new LinkedList<>();
        int product_id;
        int sale_id;
        int type;
        Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery(query);
            items = getSaleItemsFromResultSet(set);
            con.commit();
            return items;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            if (ex instanceof SQLException) {
                throw (SQLException) ex;
            } else {
                throw new SQLException(ex.getMessage());
            }
        }
    }

    @Override
    public SaleItem updateSaleItem(SaleItem i) throws IOException, SQLException, JTillException {
        String query = "UPDATE SALEITEMS SET PRODUCT_ID=" + i.getId() + ",TYPE=" + i.getType() + ",QUANTITY=" + i.getQuantity() + ",PRICE=" + i.getPrice() + ",SALE_ID=" + i.getSale();
        Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate(query);
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
        return i;
    }

    @Override
    public int getTotalSoldOfItem(int id) throws IOException, SQLException {
        String query = "SELECT QUANTITY, PRODUCT_ID FROM SALEITEMS WHERE PRODUCT_ID=" + id;
        int quantity = 0;
        Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery(query);
            while (set.next()) {
                quantity += set.getInt("QUANTITY");
            }
            con.commit();
            return quantity;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public BigDecimal getTotalValueSold(int id) throws IOException, SQLException {
        String query = "SELECT PRICE, PRODUCT_ID, QUANTITY FROM SALEITEMS WHERE PRODUCT_ID=" + id;
        BigDecimal val = BigDecimal.ZERO;
        Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery(query);
            while (set.next()) {
                String value = Double.toString(set.getDouble("PRICE"));
                int quantity = set.getInt("QUANTITY");
                val = val.add(new BigDecimal(value).multiply(new BigDecimal(quantity)));
            }
            con.commit();
            return val;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public int getTotalWastedOfItem(int id) throws IOException, SQLException {
        String query = "SELECT PRODUCT, QUANTITY FROM WASTEITEMS WHERE PRODUCT=" + id;
        int quantity = 0;
        Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery(query);
            while (set.next()) {
                quantity += set.getInt("QUANTITY");
            }
            con.commit();
            return quantity;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public BigDecimal getValueWastedOfItem(int id) throws IOException, SQLException {
        String query = "SELECT WASTEITEMS.ID AS wId, PRODUCT, QUANTITY, PRODUCTS.ID as pId, PRICE FROM PRODUCTS, WASTEITEMS WHERE WASTEITEMS.PRODUCT = PRODUCTS.ID AND WASTEITEMS.PRODUCT=" + id;
        BigDecimal val = BigDecimal.ZERO;
        Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery(query);
            while (set.next()) {
                double dval = set.getDouble("PRICE");
                dval *= set.getInt("QUANTITY");
                String value = Double.toString(dval);
                val = val.add(new BigDecimal(value));
            }
            con.commit();
            return val;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public void addReceivedItem(ReceivedItem i, int report) throws IOException, SQLException {
        String query = "INSERT INTO RECEIVEDITEMS (PRODUCT, QUANTITY, PRICE, RECEIVED_REPORT) VALUES (" + i.getProduct().getId() + "," + i.getQuantity() + "," + i.getPrice().doubleValue() + "," + report + ")";
        Connection con = getConnection();
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

    @Override
    public BigDecimal getValueSpentOnItem(int id) throws IOException, SQLException {
        String query = "SELECT * FROM RECEIVEDITEMS WHERE PRODUCT=" + id;
        BigDecimal value = BigDecimal.ZERO;
        Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery(query);
            while (set.next()) {
                BigDecimal p = set.getBigDecimal("PRICE");
                value = value.add(p);
            }
            con.commit();
            return value;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public List<SaleItem> getSaleItemsSearchTerms(int depId, int catId, Date start, Date end) throws IOException, SQLException {
        String query = "SELECT i.ID as iId, PRODUCT_ID, QUANTITY, i.PRICE as iPrice, SALE_ID, s.ID as sId, s.PRICE as sPrice, CUSTOMER, DISCOUNT, TIMESTAMP, TERMINAL, CASHED, STAFF, CHARGE_ACCOUNT FROM SALEITEMS i, SALES s, DEPARTMENTS d, Products p, Categorys c WHERE c.ID = p.CATEGORY_ID AND d.ID = p.DEPARTMENT_ID AND i.PRODUCT = p.ID AND i.SALE_ID = s.ID AND d.ID = " + depId + " AND c.ID = " + catId;
        Connection con = getConnection();
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
        Connection con = getConnection();
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

    @Override
    public void clockOff(int id) throws IOException, SQLException, StaffNotFoundException {
        String query = "INSERT INTO CLOCKONOFF(STAFF, TIMESTAMP, ONOFF) VALUES (" + id + "," + new Date().getTime() + "," + 1 + ")";
        Connection con = getConnection();
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

    @Override
    public List<ClockItem> getAllClocks(int sid) throws IOException, SQLException, StaffNotFoundException {
        String query = "SELECT * FROM CLOCKONOFF WHERE STAFF=" + sid;
        Connection con = getConnection();
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

    @Override
    public void clearClocks(int id) throws IOException, SQLException, StaffNotFoundException {
        String query = "DELETE FROM CLOCKONOFF WHERE STAFF=" + id;
        Connection con = getConnection();
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

    @Override
    public Trigger addTrigger(Trigger t) throws IOException, SQLException {
        String query = "INSERT INTO TRIGGERS (BUCKET, PRODUCT, QUANTITYREQUIRED) VALUES (" + t.getBucket() + "," + t.getProduct() + "," + t.getQuantityRequired() + ")";
        Connection con = getConnection();
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

    @Override
    public List<DiscountBucket> getDiscountBuckets(int id) throws IOException, SQLException, DiscountNotFoundException {
        String query = "SELECT * FROM BUCKETS WHERE DISCOUNT=" + id;
        Connection con = getConnection();
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

    @Override
    public void removeTrigger(int id) throws IOException, SQLException, JTillException {
        String query = "DELETE FROM TRIGGERS WHERE ID=" + id;
        Connection con = getConnection();
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

    @Override
    public List<Discount> getValidDiscounts() throws IOException, SQLException {
        String query = "SELECT * FROM DISCOUNTS";
        Date date = new Date();
        Connection con = getConnection();
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

    @Override
    public DiscountBucket addBucket(DiscountBucket b) throws IOException, SQLException {
        String query = "INSERT INTO BUCKETS (DISCOUNT, TRIGGERSREQUIRED, REQUIREDTRIGGER) VALUES(" + b.getDiscount() + "," + b.getRequiredTriggers() + "," + b.isRequiredTrigger() + ")";
        Connection con = getConnection();
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

    @Override
    public void removeBucket(int id) throws IOException, SQLException, JTillException {
        String query = "DELETE FROM BUCKETS WHERE ID=" + id;
        Connection con = getConnection();
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

    @Override
    public List<Trigger> getBucketTriggers(int id) throws IOException, SQLException, JTillException {
        String query = "SELECT * FROM TRIGGERS WHERE BUCKET=" + id;
        Connection con = getConnection();
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

    @Override
    public Trigger updateTrigger(Trigger t) throws IOException, SQLException, JTillException {
        String query = "UPDATE TRIGGERS SET BUCKET=" + t.getBucket() + ", PRODUCT=" + t.getProduct() + ", QUANTITYREQUIRED=" + t.getQuantityRequired();
        Connection con = getConnection();
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

    @Override
    public DiscountBucket updateBucket(DiscountBucket b) throws IOException, SQLException, JTillException {
        String query = "UPDATE BUCKETS SET TRIGGERSREQUIRED=" + b.getRequiredTriggers() + ", REQUIREDTRIGGER=" + b.isRequiredTrigger() + " WHERE ID=" + b.getId();
        Connection con = getConnection();
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

    @Override
    public List<Sale> getUncachedTillSales(int id) throws IOException, JTillException, SQLException {
        String query = "SELECT * FROM SALES s, TILLS t, STAFF st WHERE st.ID = s.STAFF AND s.TERMINAL = t.ID AND TERMINAL=" + id + " AND CASHED=FALSE";
        Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery(query);
            List<Sale> sales = getSalesFromResultSet(set);
            con.commit();
            return sales;
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
            con.rollback();
            throw ex;
        }
    }

    @Override
    public List<SaleItem> searchSaleItems(int department, int category, Date start, Date end) throws IOException, SQLException, JTillException {
        long startL = start.getTime();
        long endL = end.getTime();
        String pQuery = "SELECT * FROM SALEITEMS i, PRODUCTS p, CATEGORYS c, DEPARTMENTS d, TAX t, SALES s WHERE i.PRODUCT_ID = p.ID AND p.CATEGORY_ID = c.ID AND c.DEPARTMENT = d.ID AND p.TAX_ID = t.ID AND p.ID = i.PRODUCT_ID AND i.SALE_ID = s.ID AND i.TYPE = 1 AND s.TIMESTAMP >= " + startL + " AND s.TIMESTAMP <= " + endL;
        if (department > -1) {
            pQuery = pQuery.concat(" AND d.ID = " + department);
        }
        if (category > -1) {
            pQuery = pQuery.concat(" AND p.CATEGORY_ID = " + category);
        }
        Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery(pQuery);
            List<SaleItem> items = getSaleItemsFromResultSet(set);
            con.commit();
            return items;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            if (ex instanceof SQLException) {
                throw (SQLException) ex;
            } else {
                throw new JTillException(ex.getMessage());
            }
        }
    }

    @Override
    public List<Sale> getTerminalSales(int terminal, boolean uncashedOnly) throws IOException, SQLException {
        String query = "SELECT * FROM SALES s, TILLS t, STAFF st WHERE st.ID = s.STAFF AND s.TERMINAL = t.ID AND s.TERMINAL = " + terminal + (uncashedOnly ? " AND s.CASHED = FALSE" : "");
        Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery(query);
            List<Sale> sales = getSalesFromResultSet(set);
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

    @Override
    public List<Product> getProductsAdvanced(String WHERE) throws IOException, SQLException {
        String query = "SELECT * FROM PRODUCTS, CATEGORYS, DEPARTMENTS, TAX " + WHERE + " AND PRODUCTS.CATEGORY_ID = CATEGORYS.ID AND CATEGORYS.DEPARTMENT = DEPARTMENTS.ID AND PRODUCTS.TAX_ID = TAX.ID";
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        try {
            ResultSet set = stmt.executeQuery(query);
            List<Product> products = getProductsFromResultSet(set);
            con.commit();
            return products;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public List<Sale> getStaffSales(Staff s) throws IOException, SQLException, StaffNotFoundException {
        final String query = "SELECT * FROM SALES s, TILLS t, STAFF st WHERE st.ID = s.STAFF AND s.TERMINAL = t.ID AND s.STAFF = " + s.getId();
        final Connection con = getConnection();
        try {
            final Statement stmt = con.createStatement();
            final ResultSet set = stmt.executeQuery(query);
            final List<Sale> sales = getSalesFromResultSet(set);
            con.commit();
            for (Sale sale : sales) {
                sale.setProducts(getItemsInSale(sale));
            }
            return sales;
        } catch (SQLException ex) {
            con.rollback();
            throw ex;
        }
    }

    @Override
    public Object[] databaseInfo() throws IOException, SQLException {
        final Connection con = getConnection();
        final Object[] info = new Object[5];
        info[0] = con.getCatalog();
        info[1] = con.getSchema();
        info[2] = con.getClientInfo();
        info[3] = con.getMetaData().getDriverName();
        info[4] = con.getMetaData().getDriverVersion();
        return info;
    }

    @Override
    public Till updateTill(Till t) throws IOException, SQLException, JTillException {
        String query = t.getSQLUpdateString();
        Connection con = getConnection();
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
        return t;
    }

    @Override
    public File getLoginBackground() throws IOException {
        try {
            if (getSetting("bg_url").equals("NONE")) {
                return null;
            }
            return new File(getSetting("bg_url"));
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Override
    public void reinitialiseAllTills() throws IOException, JTillException {
        if (inits == 0) {
            server.sendData(null, JConnData.create("SENDDATA"));
        } else {
            throw new JTillException("There " + (inits > 1 ? "are " + inits + " terminals" : "is 1 terminal") + " receiving data, these must finish first");
        }
    }

    @Override
    public int clearSalesData() throws IOException, SQLException {
        String q1 = "DELETE FROM SALEITEMS";
        String q2 = "DELETE FROM SALES";
        Connection con = getConnection();
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

    @Override
    public void addReceivedReport(ReceivedReport rep) throws SQLException, IOException {
        String query = "INSERT INTO RECEIVED_REPORTS (INVOICE_NO, SUPPLIER_ID, PAID) VALUES ('" + rep.getInvoiceId() + "'," + rep.getSupplier().getId() + "," + rep.isPaid() + ")";
        Connection con = getConnection();
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
        for (ReceivedItem item : rep.getItems()) {
            this.addReceivedItem(item, rep.getId());
        }
    }

    @Override
    public List<ReceivedReport> getAllReceivedReports() throws IOException, SQLException {
        String query = "SELECT * FROM RECEIVED_REPORTS, SUPPLIERS WHERE RECEIVED_REPORTS.SUPPLIER_ID = SUPPLIERS.ID ORDER BY RECEIVED_REPORTS.ID";
        List<ReceivedReport> reports;
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        try {
            ResultSet set = stmt.executeQuery(query);
            reports = new LinkedList<>();
            while (set.next()) {
                int id = set.getInt(1);
                String inv = set.getString(2);
                int supp = set.getInt(3);
                boolean paid = set.getBoolean(4);

                String name = set.getString(6);
                String addrs = set.getString(7);
                String phone = set.getString(8);
                Supplier sup = new Supplier(supp, name, addrs, phone);

                ReceivedReport rr = new ReceivedReport(id, inv, sup, paid);

                reports.add(rr);
            }
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }

        for (ReceivedReport rr : reports) {
            rr.setItems(getItemsInReport(rr.getId()));
        }
        return reports;
    }

    private List<ReceivedItem> getItemsInReport(int id) throws SQLException {
        String query = "SELECT * FROM PRODUCTS, CATEGORYS, DEPARTMENTS, TAX, RECEIVEDITEMS WHERE PRODUCTS.CATEGORY_ID = CATEGORYS.ID AND CATEGORYS.DEPARTMENT = DEPARTMENTS.ID AND PRODUCTS.TAX_ID = TAX.ID AND RECEIVEDITEMS.PRODUCT = PRODUCTS.ID AND RECEIVED_REPORT=" + id;

        List<ReceivedItem> items;
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        try {
            ResultSet set = stmt.executeQuery(query);
            items = new LinkedList<>();
            while (set.next()) {
                int code = set.getInt(1);
                int order_code = set.getInt(2);
                String name = set.getString(3);
                boolean open = set.getBoolean(4);
                BigDecimal price = set.getBigDecimal(5);
                int stock = set.getInt(6);
                String comments = set.getString(7);
                String shortName = set.getString(8);
                int cId = set.getInt(9);
                int taxID = set.getInt(10);
                BigDecimal costPrice = set.getBigDecimal(11);
                int minStock = set.getInt(12);
                int maxStock = set.getInt(13);
                int packSize = set.getInt(14);
                String barcode = set.getString(15);
                double scale = set.getDouble(16);
                String scaleName = set.getString(17);
                boolean incVat = set.getBoolean(18);
                int maxCon = set.getInt(19);
                int minCon = set.getInt(20);
                BigDecimal limit = set.getBigDecimal(21);
                boolean trackStock = set.getBoolean(22);

                String cName = set.getString(24);
                Time start = set.getTime(25);
                Time end = set.getTime(26);
                boolean restrict = set.getBoolean(27);
                int age = set.getInt(28);
                int department = set.getInt(29);

                String dName = set.getString(31);

                Department d = new Department(department, dName);

                Category c = new Category(cId, cName, start, end, restrict, age, d);

                String tName = set.getString(33);
                double value = set.getDouble(34);

                Tax t = new Tax(taxID, tName, value);

                Product p;
                if (!open) {
                    p = new Product(name, shortName, barcode, order_code, c, comments, t, price, costPrice, incVat, packSize, stock, minStock, maxStock, code, maxCon, minCon, trackStock);
                } else {
                    p = new Product(name, shortName, barcode, order_code, c, comments, t, scale, scaleName, costPrice, limit, code);
                }

                int iid = set.getInt(35);
                BigDecimal riPrice = set.getBigDecimal(36);
                int quantity = set.getInt(37);

                ReceivedItem ri = new ReceivedItem(iid, p, quantity, riPrice);

                items.add(ri);
            }
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
        return items;
    }

    @Override
    public ReceivedReport updateReceivedReport(ReceivedReport rr) throws IOException, SQLException {
        String query = "UPDATE RECEIVED_REPORTS SET PAID = " + rr.isPaid() + " WHERE ID=" + rr.getId();
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        try {
            stmt.executeUpdate(query);
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
        return rr;
    }

    @Override
    public void sendData(int id, String[] data) throws IOException {
        for (JConnThread th : server.getClientConnections()) {
            ConnectionHandler hand = (ConnectionHandler) th.getMethodClass();
            if (hand.till != null && hand.till.getId() == id) {
                th.sendData(JConnData.create("SENDDATA").addParam("DATA", data));
            }
        }
    }

    @Override
    public void sendBuildUpdates() throws IOException, SQLException {
        for (JConnThread th : server.getClientConnections()) {
            ConnectionHandler hand = (ConnectionHandler) th.getMethodClass();
            if (hand.till != null) {
                th.sendData(JConnData.create("REQUPDATE"));
            }
        }
    }

    @Override
    public byte[] downloadTerminalUpdate() throws Exception {
        byte[] bytes = UpdateChecker.downloadTerminalUpdate();
        return bytes;
    }

    @Override
    public void logoutTill(int id) throws IOException, JTillException {
        for (JConnThread th : server.getClientConnections()) {
            ConnectionHandler hand = (ConnectionHandler) th.getMethodClass();
            if (hand.till != null) {
                if (hand.till.getId() == id) {
                    th.sendData(JConnData.create("LOGOUT"));
                    return;
                }
            }
        }
    }

    @Override
    public List<Screen> checkInheritance(Screen s) throws IOException, SQLException, JTillException {
        String query = "SELECT * FROM SCREENS WHERE INHERITS = " + s.getId();
        Connection con = getConnection();
        Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        try {
            ResultSet set = stmt.executeQuery(query);
            List<Screen> screens = this.getScreensFromResultSet(set);
            con.commit();
            return screens;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public Staff getTillStaff(int id) throws IOException, JTillException {
        for (JConnThread thread : TillServer.server.getClientConnections()) {
            final ConnectionHandler th = (ConnectionHandler) thread.getMethodClass();
            if (th.till == null) {
                continue;
            }
            if (th.till.getId() == id) {
                if (th.staff == null) {
                    return null;
                } else {
                    return th.staff;
                }
            }
        }
        return null;
    }

    @Override
    public TillReport zReport(Till terminal, BigDecimal declared, Staff staff) throws IOException, SQLException, JTillException {
        final TillReport report = xReport(terminal, declared, staff);
        final String query = "UPDATE SALES SET CASHED=TRUE WHERE TERMINAL=" + terminal.getId();
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        try {
            stmt.executeUpdate(query);
            stmt.executeUpdate("INSERT INTO DECLARATIONS (TERMINAL, DECLARED, EXPECTED, TRANSACTIONS, TAX, STAFF, TIME) VALUES (" + report.getInsert() + ")");
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
        return report;
    }

    @Override
    public TillReport xReport(Till terminal, BigDecimal declared, Staff staff) throws IOException, SQLException, JTillException {
        final String query = "SELECT * FROM SALES s, TILLS t, STAFF st WHERE st.ID = s.STAFF AND s.TERMINAL = t.ID AND s.CASHED = FALSE AND s.TERMINAL = " + terminal.getId();
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        List<Sale> sales = new LinkedList<>();
        try {
            ResultSet set = stmt.executeQuery(query);
            sales = getSalesFromResultSet(set);
            if (sales.isEmpty()) {
                throw new JTillException("No sales since last Z report.");
            }
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
        for (Sale s : sales) {
            s.setProducts(getItemsInSale(s));
        }
        return new TillReport(terminal, sales, declared, staff, new Date().getTime());
    }

    @Override
    public void purgeDatabase() throws IOException, SQLException {
        String r1 = "DELETE FROM RECEIVEDITEMS";
        String r2 = "DELETE FROM RECEIVED_REPORTS";
        String w1 = "DELETE FROM WASTEITEMS";
        String d1 = "DELETE FROM DECLARATIONS";
        String o1 = "DELETE FROM ORDERITEMS";
        String o2 = "DELETE FROM ORDERS";
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        try {
            stmt.executeUpdate(r1);
            stmt.executeUpdate(r2);
            stmt.executeUpdate(w1);
            stmt.executeUpdate(d1);
            stmt.executeUpdate(o1);
            stmt.executeUpdate(o2);
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public int removeCashedSales() throws IOException, SQLException {
        String query = "SELECT ID FROM SALES WHERE CASHED = TRUE";
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        try {
            ResultSet set = stmt.executeQuery(query);
            List<Integer> ids = new LinkedList<>();
            while (set.next()) {
                ids.add(set.getInt("ID"));
            }
            for (int id : ids) {
                String query2 = "DELETE FROM SALEITEMS WHERE SALE_ID = " + id;
                stmt.executeUpdate(query2);
            }
            String query3 = "DELETE FROM SALES WHERE CASHED = TRUE";
            int val = stmt.executeUpdate(query3);
            con.commit();
            return val;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public List<TillReport> getDeclarationReports(int terminal) throws SQLException {
        String query;
        if (terminal == -1) {
            query = "SELECT * FROM DECLARATIONS d, STAFF s, TILLS t WHERE d.TERMINAL = t.ID AND d.STAFF = s.ID";
        } else {
            query = "SELECT * FROM DECLARATIONS d, STAFF s, TILLS t WHERE d.TERMINAL = t.ID AND d.STAFF = s.ID AND d.TERMINAL = " + terminal;
        }
        final Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery(query);
            List<TillReport> reports = new LinkedList<>();
            while (set.next()) {
                int tId = set.getInt(16);
                UUID uuid = UUID.fromString(set.getString(17));
                String name = set.getString(18);
                BigDecimal uncashed = set.getBigDecimal(19);
                int defaultScreen = set.getInt(20);

                Till till = new Till(name, uncashed, tId, uuid, defaultScreen);

                int sId = set.getInt(9);
                String sName = set.getString(10);
                int pos = set.getInt(11);
                String un = set.getString(12);
                String pw = set.getString(13);
                boolean enabled = set.getBoolean(14);
                double wage = set.getDouble(15);

                Staff staff = new Staff(sId, sName, pos, un, pw, wage, enabled);

                int rid = set.getInt(1);
                BigDecimal declared = set.getBigDecimal(3);
                BigDecimal expected = set.getBigDecimal(4);
                int transactions = set.getInt(5);
                BigDecimal tax = set.getBigDecimal(6);
                long time = set.getLong(8);

                TillReport report = new TillReport(rid, till, declared, expected, transactions, tax, staff, time);
                reports.add(report);
            }
            con.commit();
            return reports;
        } catch (SQLException ex) {
            con.rollback();
            throw ex;
        }
    }

    @Override
    public boolean isTillNameUsed(String name) throws IOException, SQLException {
        String query = "SELECT NAME FROM TILLS WHERE NAME='" + name + "'";
        final Connection con = getConnection();
        try {
            Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet set = stmt.executeQuery(query);
            return set.first();
        } finally {
            con.commit();
        }
    }

    @Override
    public void removeDeclarationReport(int id) throws IOException, SQLException {
        final Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("DELETE FROM DECLARATIONS WHERE ID=" + id);
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public int getTotalReceivedOfItem(int id) throws IOException, SQLException {
        final Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery("SELECT PRODUCT, QUANTITY FROM RECEIVEDITEMS WHERE PRODUCT =" + id);
            int quantity = 0;
            while (set.next()) {
                quantity += set.getInt(2);
            }
            con.commit();
            return quantity;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public List<Sale> consolidated(Date start, Date end, int t) throws IOException, SQLException {
        long s = start.getTime();
        long e = end.getTime();
        List<Sale> sales;
        final Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery("SELECT * FROM SALES s, TILLS t, STAFF st WHERE st.ID = s.STAFF AND s.TERMINAL = t.ID AND TIMESTAMP >= " + s + " AND TIMESTAMP <= " + e + (t != -1 ? "AND TERMINAL = " + t : ""));
            sales = getSalesFromResultSet(set);
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
        for (Sale sale : sales) {
            sale.setProducts(getItemsInSale(sale));
        }
        return sales;
    }

    @Override
    public BigDecimal getRefunds(Date start, Date end, int t) throws IOException, SQLException {
        long s = start.getTime();
        long e = end.getTime();
        final Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery("SELECT SALEITEMS.PRICE, SALES.TIMESTAMP FROM SALEITEMS, SALES WHERE SALEITEMS.SALE_ID = SALES.ID AND SALES.TIMESTAMP >= " + s + " AND SALES.TIMESTAMP <= " + e + " AND SALEITEMS.PRICE < 0");
            BigDecimal total = BigDecimal.ZERO;
            while (set.next()) {
                total = total.add(set.getBigDecimal(1));
            }
            con.commit();
            return total;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public BigDecimal getWastage(Date start, Date end) throws IOException, SQLException {
        long s = start.getTime();
        long e = end.getTime();
        final Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery("SELECT TIMESTAMP, VALUE FROM WASTEITEMS WHERE TIMESTAMP >= " + s + " AND TIMESTAMP <= " + e);
            BigDecimal total = BigDecimal.ZERO;
            while (set.next()) {
                total = total.add(set.getBigDecimal(2));
            }
            con.commit();
            return total;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public void submitStockTake(List<Product> products, boolean zeroRest) throws IOException, SQLException {
        final Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            if (zeroRest) {
                stmt.executeUpdate("UPDATE PRODUCTS SET STOCK = 0");
            }
            for (Product p : products) {
                stmt.executeUpdate("UPDATE PRODUCTS SET STOCK=" + p.getStock() + " WHERE ID=" + p.getId());
            }
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public List<Category> getCategoriesInDepartment(int department) throws IOException, SQLException {
        final Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery("SELECT * FROM CATEGORYS, DEPARTMENTS WHERE CATEGORYS.DEPARTMENT = DEPARTMENTS.ID AND DEPARTMENTS.ID = " + department);
            List<Category> c = getCategorysFromResultSet(set);
            con.commit();
            return c;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public List<Product> getProductsInDepartment(int id) throws IOException, SQLException {
        final Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery("SELECT * FROM PRODUCTS, CATEGORYS, DEPARTMENTS, TAX WHERE PRODUCTS.CATEGORY_ID = CATEGORYS.ID AND CATEGORYS.DEPARTMENT = DEPARTMENTS.ID AND PRODUCTS.TAX_ID = TAX.ID AND DEPARTMENTS.ID = " + id);
            List<Product> products = getProductsFromResultSet(set);
            con.commit();
            return products;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.INFO, null, ex);
            throw ex;
        }
    }

    @Override
    public Condiment addCondiment(Condiment c) throws IOException, SQLException {
        String query = "INSERT INTO CONDIMENTS (PRODUCT, PRODUCT_CON) VALUES (" + c.getProduct() + "," + c.getProduct_con().getId() + ")";
        final Connection con = getConnection();
        try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.executeUpdate();
            ResultSet set = stmt.getGeneratedKeys();
            while (set.next()) {
                int id = set.getInt(1);
                c.setId(id);
            }
            con.commit();
            return c;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public List<Condiment> getProductsCondiments(int product) throws IOException, SQLException {
        final Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery("SELECT * FROM PRODUCTS, CATEGORYS, DEPARTMENTS, TAX, CONDIMENTS WHERE PRODUCTS.CATEGORY_ID = CATEGORYS.ID AND CATEGORYS.DEPARTMENT = DEPARTMENTS.ID AND PRODUCTS.TAX_ID = TAX.ID AND PRODUCTS.ID = CONDIMENTS.PRODUCT_CON AND CONDIMENTS.PRODUCT = " + product);
            List<Condiment> condiments = new LinkedList<>();
            while (set.next()) {
                int code = set.getInt(1);
                int order_code = set.getInt(2);
                String name = set.getString(3);
                boolean open = set.getBoolean(4);
                BigDecimal price = set.getBigDecimal(5);
                int stock = set.getInt(6);
                String comments = set.getString(7);
                String shortName = set.getString(8);
                int cId = set.getInt(9);
                int taxID = set.getInt(10);
                BigDecimal costPrice = set.getBigDecimal(11);
                int minStock = set.getInt(12);
                int maxStock = set.getInt(13);
                int packSize = set.getInt(14);
                String barcode = set.getString(15);
                double scale = set.getDouble(16);
                String scaleName = set.getString(17);
                boolean incVat = set.getBoolean(18);
                int maxCon = set.getInt(19);
                int minCon = set.getInt(20);
                BigDecimal limit = set.getBigDecimal(21);
                boolean trackStock = set.getBoolean(22);

                String cName = set.getString(24);
                Time start = set.getTime(25);
                Time end = set.getTime(26);
                boolean restrict = set.getBoolean(27);
                int age = set.getInt(28);
                int department = set.getInt(29);

                String dName = set.getString(31);

                Department d = new Department(department, dName);

                Category c = new Category(cId, cName, start, end, restrict, age, d);

                String tName = set.getString(33);
                double value = set.getDouble(34);

                Tax t = new Tax(taxID, tName, value);

                int conId = set.getInt(35);

                Product p;
                if (!open) {
                    p = new Product(name, shortName, barcode, order_code, c, comments, t, price, costPrice, incVat, packSize, stock, minStock, maxStock, code, maxCon, minCon, trackStock);
                } else {
                    p = new Product(name, shortName, barcode, order_code, c, comments, t, scale, scaleName, costPrice, limit, code);
                }
                condiments.add(new Condiment(conId, code, p));
            }
            con.commit();
            return condiments;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public Condiment updateCondiment(Condiment c) throws IOException, SQLException {
        final Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("UPDATE CONDIMENTS SET PRODUCT=" + c.getProduct() + ", PRODUCT_CON=" + c.getProduct_con().getId() + " WHERE ID=" + c.getId());
            con.commit();
            return c;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public void removeCondiment(int id) throws IOException, SQLException {
        final Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("DELETE FROM CONDIMENTS WHERE ID=" + id);
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public List<SaleItem> getSalesByDepartment(int id) throws IOException, SQLException {
        final Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery("SELECT * FROM SALEITEMS, PRODUCTS, CATEGORYS, DEPARTMENTS, TAX WHERE SALEITEMS.PRODUCT_ID = PRODUCTS.ID AND CATEGORYS.DEPARTMENT = DEPARTMENTS.ID AND PRODUCTS.CATEGORY_ID = CATEGORYS.ID AND PRODUCTS.TAX_ID = TAX.ID AND DEPARTMENTS.ID=" + id);
            List<SaleItem> items = getSaleItemsFromResultSet(set);
            List<SaleItem> is = new LinkedList<>();
            Main:
            for (SaleItem item : items) {
                for (SaleItem i : is) {
                    if (item.getItem().equals(i.getItem())) {
                        i.increaseQuantity(item.getQuantity());
                        continue Main;
                    }
                }
                is.add(item);
            }
            con.commit();
            return is;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public Order addOrder(Order o) throws IOException, SQLException {
        String query = "INSERT INTO ORDERS (SUPPLIER, SENT, SENDDATE, RECEIVED) VALUES (" + o.getSupplier().getId() + "," + o.isSent() + "," + o.getSendDate().getTime() + ", FALSE)";
        final Connection con = getConnection();
        try (PreparedStatement pstmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.executeUpdate();
            ResultSet set = pstmt.getGeneratedKeys();
            while (set.next()) {
                int id = set.getInt(1);
                o.setId(id);
            }
            for (OrderItem i : o.getItems()) {
                String query2 = "INSERT INTO ORDERITEMS (PRODUCT, ORDER_ID, QUANTITY, PRICE) VALUES (" + i.getProduct().getId() + "," + o.getId() + "," + i.getQuantity() + "," + i.getPrice().doubleValue() + ")";
                try (PreparedStatement pstmt2 = con.prepareStatement(query2, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt2.executeUpdate();
                    ResultSet set2 = pstmt2.getGeneratedKeys();
                    while (set2.next()) {
                        int id = set2.getInt(1);
                        i.setId(id);
                    }
                }
            }
            con.commit();
            return o;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public void updateOrder(Order o) throws IOException, SQLException {
        final Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("UPDATE ORDERS SET SENDDATE=" + o.getSendDate().getTime() + ", SENT=" + o.isSent() + ", RECEIVED=" + o.isReceived() + " WHERE ID=" + o.getId());
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public List<Order> getAllOrders() throws IOException, SQLException {
        final Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery("SELECT * FROM ORDERS, SUPPLIERS WHERE ORDERS.SUPPLIER = SUPPLIERS.ID");
            List<Order> orders = new LinkedList<>();
            while (set.next()) {
                int id = set.getInt(1);
                int supplier = set.getInt(2);
                boolean sent = set.getBoolean(3);
                Date sendDate = new Date(set.getLong(4));
                boolean received = set.getBoolean(5);

                String name = set.getString(7);
                String s_address = set.getString(8);
                String phone = set.getString(9);

                Supplier s = new Supplier(supplier, name, s_address, phone);

                Order o = new Order(id, s, sent, sendDate, null, received);
                orders.add(o);
            }

            for (Order o : orders) {
                ResultSet set2 = stmt.executeQuery("SELECT * FROM PRODUCTS, CATEGORYS, DEPARTMENTS, TAX, ORDERITEMS WHERE PRODUCTS.CATEGORY_ID = CATEGORYS.ID AND CATEGORYS.DEPARTMENT = DEPARTMENTS.ID AND PRODUCTS.TAX_ID = TAX.ID AND ORDERITEMS.PRODUCT = PRODUCTS.ID AND ORDERITEMS.ORDER_ID = " + o.getId());
                List<OrderItem> items = new LinkedList<>();
                while (set2.next()) {
                    int code = set2.getInt(1);
                    int order_code = set2.getInt(2);
                    String name = set2.getString(3);
                    boolean open = set2.getBoolean(4);
                    BigDecimal price = set2.getBigDecimal(5);
                    int stock = set2.getInt(6);
                    String comments = set2.getString(7);
                    String shortName = set2.getString(8);
                    int cId = set2.getInt(9);
                    int taxID = set2.getInt(10);
                    BigDecimal costPrice = set2.getBigDecimal(11);
                    int minStock = set2.getInt(12);
                    int maxStock = set2.getInt(13);
                    int packSize = set2.getInt(14);
                    String barcode = set2.getString(15);
                    double scale = set2.getDouble(16);
                    String scaleName = set2.getString(17);
                    boolean incVat = set2.getBoolean(18);
                    int maxCon = set2.getInt(19);
                    int minCon = set2.getInt(20);
                    BigDecimal limit = set2.getBigDecimal(21);
                    boolean trackStock = set2.getBoolean(22);

                    String cName = set2.getString(24);
                    Time start = set2.getTime(25);
                    Time end = set2.getTime(26);
                    boolean restrict = set2.getBoolean(27);
                    int age = set2.getInt(28);
                    int department = set2.getInt(29);

                    String dName = set2.getString(31);

                    Department d = new Department(department, dName);

                    Category c = new Category(cId, cName, start, end, restrict, age, d);

                    String tName = set2.getString(33);
                    double value = set2.getDouble(34);

                    Tax t = new Tax(taxID, tName, value);

                    Product p;
                    if (!open) {
                        p = new Product(name, shortName, barcode, order_code, c, comments, t, price, costPrice, incVat, packSize, stock, minStock, maxStock, code, maxCon, minCon, trackStock);
                    } else {
                        p = new Product(name, shortName, barcode, order_code, c, comments, t, scale, scaleName, costPrice, limit, code);
                    }
                    int o_id = set2.getInt(34);
                    int quantity = set2.getInt(37);
                    BigDecimal o_price = set2.getBigDecimal(38);

                    OrderItem i = new OrderItem(o_id, p, quantity, o_price);
                    items.add(i);
                }
                o.setItems(items);
            }
            con.commit();
            return orders;
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public void deleteOrder(int id) throws IOException, SQLException {
        final Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("DELETE FROM ORDERITEMS WHERE ORDER_ID = " + id);
            stmt.executeUpdate("DELETE FROM ORDERS WHERE ID=" + id);
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public HashMap<String, Object> terminalInit(int id, String[] data) throws IOException {
        try {
            inits++;
            HashMap<String, Object> init = new HashMap<>();
            if (data == null) {
                init.put("background", getLoginBackground());
                init.put("products", getAllProducts());
                init.put("discounts", getValidDiscounts());
                init.put("settings", systemSettings.getProperties());
                init.put("screens", getAllScreens());
                init.put("staff", getAllStaff());
                init.put("terminal", getTill(id));
            } else {
                for (String s : data) {
                    if (s.equals("background")) {
                        init.put(s, getLoginBackground());
                    } else if (s.equals("products")) {
                        init.put(s, getAllProducts());
                    } else if (s.equals("discounts")) {
                        init.put(s, getValidDiscounts());
                    } else if (s.equals("settings")) {
                        init.put(s, systemSettings.getProperties());
                    } else if (s.equals("screens")) {
                        init.put(s, getAllScreens());
                    } else if (s.equals("staff")) {
                        init.put(s, getAllStaff());
                    } else if (s.equals("terminal")) {
                        init.put(s, getTill(id));
                    }
                }
            }
            return init;
        } catch (SQLException | JTillException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    @Override
    public void initComplete() throws IOException {
        inits--;
    }

    @Override
    public int getInits() throws IOException {
        return inits;
    }

    private List<String> fileList;
    private String SOURCE_FOLDER = "TillEmbedded";
    private IOException backupException = null;
    private String path = "";
    private ModalDialog mDialog;

    @Override
    public String performBackup() throws IOException {
        try {
            mDialog = new ModalDialog(GUI.gui, "Backup");
            final Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        fileList = new ArrayList<>();
                        generateFileList(new File(SOURCE_FOLDER));
                        path = zipIt();
                    } catch (IOException ex) {
                        backupException = ex;
                    } finally {
                        mDialog.hide();
                    }
                }
            };
            final Thread thread = new Thread(run, "BACKUP_THREAD");
            thread.start();
            mDialog.show();
            if (backupException != null) {
                throw backupException;
            }
            return path;
        } finally {
            backupException = null;
        }
    }

    private String zipIt() throws IOException {
        File dir = new File("backups");
        if (!dir.exists()) {
            LOG.log(Level.INFO, "Creating backups directory");
            dir.mkdir();
            LOG.log(Level.INFO, "Directory backups created at " + dir.getAbsolutePath());
        }
        Date now = new Date();
        SimpleDateFormat simpleDateformat = new SimpleDateFormat("ddMMyyyy");
        String date = simpleDateformat.format(now);
        String zipFile = "backups" + File.separator + "jtillbackup_" + date + ".zip";
        byte[] buffer = new byte[1024];
        String source = new File(SOURCE_FOLDER).getName();
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);

            LOG.log(Level.INFO, "Output to Zip : " + zipFile);
            FileInputStream in = null;

            for (String file : this.fileList) {
                LOG.log(Level.INFO, "File Added : " + file);
                ZipEntry ze = new ZipEntry(source + File.separator + file);
                zos.putNextEntry(ze);
                try {
                    in = new FileInputStream(SOURCE_FOLDER + File.separator + file);
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } finally {
                    in.close();
                }
            }

            zos.closeEntry();
            LOG.log(Level.INFO, "Folder successfully compressed");

        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return zipFile;
    }

    public void generateFileList(File node) {
        // add file only
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.toString()));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(new File(node, filename));
            }
        }
    }

    private String generateZipEntry(String file) {
        return file.substring(SOURCE_FOLDER.length() + 1, file.length());
    }

    @Override
    public List<String> getBackupList() throws IOException {
        File dir = new File("backups");
        ArrayList<String> backups = new ArrayList<>();
        LOG.log(Level.INFO, "Retrieving backup list");
        if (dir.exists() && dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                backups.add(f.getName());
            }
        }
        return backups;
    }

    @Override
    public void clearBackup(String name) throws IOException {
        File dir = new File("backups");
        if (dir.exists() && dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                if (name == null) {
                    LOG.log(Level.INFO, "Removing backup " + f.getName());
                    f.delete();
                    continue;
                }
                if (f.getName().equals(name)) {
                    LOG.log(Level.INFO, "Removing backup " + f.getName());
                    f.delete();
                    return;
                }
            }
        }
    }

    @Override
    public Object[] getLicenseInfo() throws IOException {
        return new Object[]{licenseNo, conn_limit};
    }

    public void setLicenseInfo(String no, int connections) {
        this.licenseNo = no;
        this.conn_limit = connections;
    }

    @Override
    public void submitSQL(String SQL) throws IOException, SQLException {
        final Connection con = getConnection();
        Statement stmt = con.createStatement();
        try {
            stmt.execute(SQL);
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, "Error submitting custom SQl statement", ex);
            throw ex;
        }
    }

    @Override
    public void deleteWasteReason(WasteReason wr) throws SQLException, JTillException {
        String query = "UPDATE WASTEREASONS SET DELETED = true WHERE ID=" + wr.getId();
        Connection con = getConnection();
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

    @Override
    public RefundReason addRefundReason(RefundReason r) throws IOException, SQLException {
        String query = "INSERT INTO APP.REFUND_REASONS (REASON, LEVEL, DELETED) values ('" + r.getReason() + "'," + r.getPriviledgeLevel() + ", false)";
        Connection con = getConnection();
        PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        try {
            stmt.executeUpdate();
            ResultSet set = stmt.getGeneratedKeys();
            while (set.next()) {
                int id = set.getInt(1);
                r.setId(id);
            }
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
        return r;
    }

    @Override
    public void removeRefundReason(RefundReason r) throws IOException, SQLException, JTillException {
        String query = "UPDATE REFUND_REASONS SET DELETED = true WHERE ID=" + r.getId();
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        int value;
        try {
            value = stmt.executeUpdate(query);
            con.commit();
            if (value == 0) {
                throw new JTillException("Refund Reason " + r.getId() + " not found");
            }
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public void updateRefundReason(RefundReason r) throws IOException, SQLException, JTillException {
        String query = "UPDATE REFUND_REASONS SET REASON='" + r.getReason() + "', LEVEL=" + r.getPriviledgeLevel() + " WHERE ID=" + r.getId();
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        int value;
        try {
            value = stmt.executeUpdate(query);
            con.commit();
            if (value == 0) {
                throw new JTillException("Refund Reason " + r.getId() + " not found");
            }
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    @Override
    public RefundReason getRefundReason(int id) throws IOException, SQLException, JTillException {
        String query = "SELECT * FROM WASTEREASONS WHERE ID=" + id;
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        List<RefundReason> rrs = new LinkedList<>();
        try {
            ResultSet set = stmt.executeQuery(query);
            rrs = getRefundReasonsFromResultSet(set);
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }

        if (rrs.isEmpty()) {
            throw new JTillException(id + " could not be found");
        }
        return rrs.get(0);
    }

    private List<RefundReason> getRefundReasonsFromResultSet(ResultSet set) throws SQLException {
        List<RefundReason> rrs = new LinkedList<>();
        while (set.next()) {
            int id = set.getInt("ID");
            String reason = set.getString("REASON");
            int level = set.getInt("LEVEL");
            rrs.add(new RefundReason(id, reason, level));
        }
        return rrs;
    }

    @Override
    public List<RefundReason> getUsedRefundReasons() throws IOException, SQLException {
        String query = "SELECT * FROM REFUND_REASONS WHERE DELETED = false";
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        List<RefundReason> rrs = new LinkedList<>();
        try {
            ResultSet set = stmt.executeQuery(query);
            rrs = getRefundReasonsFromResultSet(set);
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        }

        return rrs;
    }

    @Override
    public BigDecimal getStaffMemberSales(Date start, Date end, Staff s) throws SQLException {
        List<Sale> sales = this.getAllSales();
        BigDecimal value = BigDecimal.ZERO;
        for (Sale sale : sales) {
            if (sale.getStaff() == s && sale.getDate().before(end) && sale.getDate().after(start)) {
                value = value.add(sale.getTotal());
            }
        }
        return value;
    }

    @Override
    public List<Screen> getScreensWithProduct(Product p) throws SQLException {
        String query = "SELECT * FROM SCREENS, BUTTONS WHERE SCREENS.ID = BUTTONS.SCREEN_ID AND BUTTONS.PRODUCT = " + p.getId();
        Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet set = stmt.executeQuery(query);
            List<Screen> screens = getScreensFromResultSet(set);
            con.commit();
            return screens;
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
            con.rollback();
            throw ex;
        }
    }
}
