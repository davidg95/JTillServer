/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

/**
 *
 * @author David
 */
public class DerbyDB extends DBConnect {

    String tills = "create table TILLS\n"
            + "(\n"
            + "	ID INT not null primary key\n"
            + "        GENERATED ALWAYS AS IDENTITY\n"
            + "        (START WITH 1, INCREMENT BY 1),\n"
            + "     UUID VARCHAR(50) not null,\n"
            + "	NAME VARCHAR(20) not null,\n"
            + "     UNCASHED DOUBLE not null,\n"
            + "     DEFAULT_SCREEN INT not null\n"
            + ")";
    String departments = "create table DEPARTMENTS\n"
            + "(\n"
            + "     ID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),"
            + "     NAME VARCHAR(30) not null\n"
            + ")";
    String categorys = "create table CATEGORYS\n"
            + "(\n"
            + "	ID INT not null primary key\n"
            + "        GENERATED ALWAYS AS IDENTITY\n"
            + "        (START WITH 1, INCREMENT BY 1),\n"
            + "	NAME VARCHAR(20) not null,\n"
            + "     SELL_START TIME,\n"
            + "     SELL_END TIME,\n"
            + "     TIME_RESTRICT BOOLEAN not null,\n"
            + "     MINIMUM_AGE INT not null,\n"
            + "     DEPARTMENT INT references DEPARTMENTS(ID)\n"
            + ")";
    String tax = "create table TAX\n"
            + "(\n"
            + "	ID INT not null primary key\n"
            + "        GENERATED ALWAYS AS IDENTITY\n"
            + "        (START WITH 1, INCREMENT BY 1),\n"
            + "	NAME VARCHAR(20) not null,\n"
            + "	VALUE DOUBLE not null\n"
            + ")";
    String configs = "create table CONFIGS\n"
            + "(\n"
            + "	NAME INT not null primary key\n"
            + "        GENERATED ALWAYS AS IDENTITY\n"
            + "        (START WITH 1, INCREMENT BY 1),\n"
            + "	VALUE VARCHAR(20) not null\n"
            + ")";
    String sales = "create table SALES\n"
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
            + "     ENABLED boolean,\n"
            + "     MOP int\n"
            + ")";
    String saleItems = "create table SALEITEMS\n"
            + "(\n"
            + "     ID INT not null primary key\n"
            + "        GENERATED ALWAYS AS IDENTITY\n"
            + "        (START WITH 1, INCREMENT BY 1),\n"
            + "     PRODUCT_ID INT not null references PRODUCTS(ID),\n"
            + "	TYPE INT,\n"
            + "     QUANTITY INT not null,\n"
            + "     PRICE double not null,\n"
            + "     TAX double not null,\n"
            + "     SALE_ID INT not null references SALES(ID),\n"
            + "     COST DOUBLE\n"
            + ")";
    String customers = "create table CUSTOMERS\n"
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
            + "     MONEY_DUE DOUBLE,\n"
            + "     MAX_DEBT BIGINT\n"
            + ")";
    String products = "create table PRODUCTS\n"
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
            + "	TAX_ID INT not null references TAX(ID),\n"
            + "	COST_PRICE DOUBLE,\n"
            + "	MIN_PRODUCT_LEVEL INTEGER,\n"
            + "	MAX_PRODUCT_LEVEL INTEGER,\n"
            + "     PACK_SIZE INT,\n"
            + "     BARCODE VARCHAR(15),\n"
            + "     SCALE DOUBLE,\n"
            + "     SCALE_NAME VARCHAR(20),\n"
            + "     INCVAT BOOLEAN,\n"
            + "     MAXCON INT,\n"
            + "     MINCON INT,\n"
            + "     LIMIT BIGINT,\n"
            + "     TRACK_STOCK BOOLEAN\n"
            + ")";
    String discounts = "create table DISCOUNTS\n"
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
    String buckets = "create table BUCKETS\n"
            + "(\n"
            + "     ID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     DISCOUNT INT not null references DISCOUNTS(ID),\n"
            + "     TRIGGERSREQUIRED INT,\n"
            + "     REQUIREDTRIGGER BOOLEAN\n"
            + ")";
    String triggers = "create table TRIGGERS\n"
            + "(\n"
            + "     ID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     BUCKET INT not null references BUCKETS(ID),\n"
            + "     PRODUCT INT not null references PRODUCTS(ID),\n"
            + "     QUANTITYREQUIRED INT\n"
            + ")";
    String staff = "create table STAFF\n"
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
    String screens = "create table SCREENS\n"
            + "(\n"
            + "     ID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     NAME VARCHAR(50) not null,\n"
            + "     WIDTH INT not null,\n"
            + "     INHERITS INT not null,\n"
            + "     HEIGHT INT not null,\n"
            + "     VGAP INT,\n"
            + "     HGAP INT\n"
            + ")";
    String buttons = "create table BUTTONS\n"
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
    String wasteReasons = "create table WASTEREASONS\n"
            + "(\n"
            + "     ID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     REASON VARCHAR(30),\n"
            + "     PRIVILEDGE_LEVEL INT,\n"
            + "     DELETED BOOLEAN\n"
            + ")";
    String refundReason = "CREATE TABLE REFUND_REASONS\n"
            + "(\n"
            + "     ID INT not null primary key\n"
            + "        GENERATED ALWAYS AS IDENTITY\n"
            + "        (START WITH 1, INCREMENT BY 1),\n"
            + "     REASON VARCHAR(30) NOT NULL,\n"
            + "     LEVEL INT NOT NULL,\n"
            + "     DELETED BOOLEAN NOT NULL\n"
            + ")";
    String wasteItems = "create table WASTEITEMS\n"
            + "(\n"
            + "     ID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     PRODUCT INT not null references PRODUCTS(ID),\n"
            + "     QUANTITY INT,\n"
            + "     REASON INT not null references WASTEREASONS(ID),\n"
            + "     VALUE DOUBLE,\n"
            + "     TIMESTAMP BIGINT\n"
            + ")";
    String suppliers = "create table SUPPLIERS\n"
            + "(\n"
            + "     ID INT not null primary key\n"
            + "       GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     NAME VARCHAR(30),\n"
            + "     ADDRESS VARCHAR(100),\n"
            + "     PHONE VARCHAR(20),\n"
            + "     ACCOUNT_NUMBER VARCHAR(20),\n"
            + "     EMAIL VARCHAR(30)\n"
            + ")";
    String receivedItems = "create table RECEIVEDITEMS\n"
            + "(\n"
            + "     ID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     PRODUCT INT not null references PRODUCTS(ID),\n"
            + "     PRICE DOUBLE,\n"
            + "     QUANTITY INT,\n"
            + "     RECEIVED_REPORT INT not null references RECEIVED_REPORTS(ID)\n"
            + ")";
    String clockOnOff = "create table CLOCKONOFF\n"
            + "(\n"
            + "     ID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     STAFF int not null references STAFF(ID),\n"
            + "     TIMESTAMP BIGINT,\n"
            + "     ONOFF int\n"
            + ")";
    String images = "create table IMAGES\n"
            + "(\n"
            + "     ID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     NAME VARCHAR(50),\n"
            + "     URL VARCHAR(200)\n"
            + ")";
    String declarations = "create table DECLARATIONS\n"
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
    String receivedReports = "create table RECEIVED_REPORTS\n"
            + "(\n"
            + "     ID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),"
            + "     INVOICE_NO VARCHAR(30) not null,\n"
            + "     SUPPLIER_ID INT not null references SUPPLIERS(ID),\n"
            + "     PAID BOOLEAN\n"
            + ")";
    String condiments = "create table CONDIMENTS\n"
            + "(\n"
            + "     ID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     PRODUCT INT not null references PRODUCTS(ID),\n"
            + "     PRODUCT_CON INT not null references PRODUCTS(ID)\n"
            + ")";
    String orders = "create table ORDERS\n"
            + "(\n"
            + "     ID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     SUPPLIER int not null references SUPPLIERS(ID),\n"
            + "     SENT BOOLEAN,\n"
            + "     SENDDATE BIGINT,\n"
            + "     RECEIVED BOOLEAN\n"
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

    public DerbyDB() {
        super();
    }

    /**
     * Returns a new connection to the database with auto commit disabled.
     *
     * @return new Connection.
     * @throws SQLException if there was an error getting the connection.
     */
    @Override
    public Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
        connection = DriverManager.getConnection(address, username, password);
        connection.setAutoCommit(false);
        return connection;
    }

    @Override
    public void load() throws SQLException {
        connection = getConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(tills);
            s.executeUpdate(tax);
            s.executeUpdate(departments);
            s.executeUpdate(categorys);
            s.executeUpdate(configs);
            s.executeUpdate(sales);
            s.executeUpdate(customers);
            s.executeUpdate(products);
            s.executeUpdate(discounts);
            s.executeUpdate(buckets);
            s.executeUpdate(triggers);
            s.executeUpdate(saleItems);
            s.executeUpdate(staff);
            s.executeUpdate(screens);
            s.executeUpdate(buttons);
            s.executeUpdate(wasteReasons);
            s.executeUpdate(wasteItems);
            s.executeUpdate(suppliers);
            s.executeUpdate(receivedReports);
            s.executeUpdate(receivedItems);
            s.executeUpdate(clockOnOff);
            s.executeUpdate(images);
            s.executeUpdate(declarations);
            s.executeUpdate(condiments);
            s.executeUpdate(orders);
            s.executeUpdate(orderItems);
            String addDepartment = "INSERT INTO DEPARTMENTS (NAME) VALUES ('Default')";
            String addCategory = "INSERT INTO CATEGORYS (NAME, TIME_RESTRICT, MINIMUM_AGE, DEPARTMENT) VALUES ('Default','FALSE',0, 1)";
            String addTax = "INSERT INTO TAX (NAME, VALUE) VALUES ('ZERO',0.0)";
            String addWasteReason = "INSERT INTO WASTEREASONS (REASON, DELETED) VALUES ('Default', 'FALSE')";
            s.executeUpdate(addDepartment);
            s.executeUpdate(addCategory);
            s.executeUpdate(addTax);
            s.executeUpdate(addWasteReason);
            s.close();
        } catch (SQLException ex) {
            LOG.info("Tables already exists, so they do not need created");
        }
    }
}
