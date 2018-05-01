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

/**
 *
 * @author David
 */
public class DerbyDB extends DBConnect {

    String tills = "create table TILLS\n"
            + "(\n"
            + "     tiID INT not null primary key\n"
            + "        GENERATED ALWAYS AS IDENTITY\n"
            + "        (START WITH 1, INCREMENT BY 1),\n"
            + "     tiUUID VARCHAR(50) not null,\n"
            + "     tiNAME VARCHAR(20) not null,\n"
            + "     tiUNCASHED DOUBLE not null,\n"
            + "     tiDEFAULT_SCREEN INT not null references screens(scid)\n"
            + ")";
    String departments = "create table DEPARTMENTS\n"
            + "(\n"
            + "     dID INT not null primary key\n,"
            + "     dNAME VARCHAR(30) not null\n"
            + ")";
    String categorys = "create table CATEGORYS\n"
            + "(\n"
            + "     cID INT not null primary key,\n"
            + "     cNAME VARCHAR(20) not null,\n"
            + "     cDEPARTMENT INT references DEPARTMENTS(dID)\n"
            + ")";
    String tax = "create table TAX\n"
            + "(\n"
            + "	tNAME VARCHAR(20) not null primary key,\n"
            + "	tVALUE DOUBLE not null\n"
            + ")";
    String sales = "create table SALES\n"
            + "(\n"
            + "     saID INT not null primary key\n"
            + "        GENERATED ALWAYS AS IDENTITY\n"
            + "        (START WITH 1, INCREMENT BY 1),\n"
            + "     saPRICE DOUBLE,\n"
            + "     saCUSTOMER varchar(20),\n"
            + "     saTIMESTAMP bigint,\n"
            + "     saTERMINAL int not null references TILLS(tiID),\n"
            + "     saCASHED boolean not null,\n"
            + "     saSTAFF int,\n"
            + "     saENABLED boolean,\n"
            + "     saMOP int\n"
            + ")";
    String saleItems = "create table SALEITEMS\n"
            + "(\n"
            + "     siID INT not null primary key\n"
            + "        GENERATED ALWAYS AS IDENTITY\n"
            + "        (START WITH 1, INCREMENT BY 1),\n"
            + "     siproduct VARCHAR(15) not null references PRODUCTS(BARCODE),\n"
            + "     siquantity INT not null,\n"
            + "     siprice double not null,\n"
            + "     sitax double not null,\n"
            + "     sisale INT not null references SALES(saID),\n"
            + "     sicost DOUBLE\n"
            + ")";
    String customers = "create table CUSTOMERS\n"
            + "(\n"
            + "	ID varchar(20) not null primary key,\n"
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
            + "     MONEY_DUE DOUBLE,\n"
            + "     MAX_DEBT BIGINT\n"
            + ")";
    String products = "create table PRODUCTS\n"
            + "(\n"
            + "     barcode VARCHAR(15) not null primary key,\n"
            + "     porder_code VARCHAR(15),\n"
            + "     pNAME VARCHAR(50) not null,\n"
            + "     pSHORT_NAME VARCHAR(50) not null,\n"
            + "     OPEN_PRICE BOOLEAN not null,\n"
            + "     pPRICE DOUBLE,\n"
            + "     pCOST_PRICE DOUBLE,\n"
            + "     pPACK_SIZE INT,\n"
            + "     pSTOCK INTEGER,\n"
            + "     pCategory INT not null references CATEGORYS(cID),\n"
            + "     pTax varchar(20) not null references TAX(tname),\n"
            + "     pmin_level INTEGER,\n"
            + "     pmax_level INTEGER,\n"
            + "     pSCALE DOUBLE,\n"
            + "     pSCALE_NAME VARCHAR(20),\n"
            + "     pcost_percentage double,\n"
            + "     pINCVAT BOOLEAN,\n"
            + "     pMAXCON INT,\n"
            + "     pMINCON INT,\n"
            + "     pLIMIT BIGINT,\n"
            + "     pComments VARCHAR(200),\n"
            + "     pIngredients VARCHAR(200),\n"
            + "     pSupplier int,\n"
            + "     pTRACK_STOCK BOOLEAN\n"
            + ")";
    String discounts = "create table DISCOUNTS\n"
            + "(\n"
            + "	ID INT not null primary key,\n"
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
            + "     PRODUCT VARCHAR(15) not null references PRODUCTS(BARCODE),\n"
            + "     QUANTITYREQUIRED INT\n"
            + ")";
    String staff = "create table STAFF\n"
            + "(\n"
            + "     stID INT not null primary key\n"
            + "        GENERATED ALWAYS AS IDENTITY\n"
            + "        (START WITH 1, INCREMENT BY 1),\n"
            + "     stNAME VARCHAR(50) not null,\n"
            + "     stPOSITION INTEGER not null,\n"
            + "     stUSERNAME VARCHAR(20) not null unique,\n"
            + "     stPASSWORD VARCHAR(200) not null,\n"
            + "     stENABLED BOOLEAN,\n"
            + "     stWAGE DOUBLE\n"
            + ")";
    String screens = "create table SCREENS\n"
            + "(\n"
            + "     scID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     scNAME VARCHAR(50) not null,\n"
            + "     scWIDTH INT not null,\n"
            + "     scINHERITS INT not null,\n"
            + "     scHEIGHT INT not null,\n"
            + "     scVGAP INT,\n"
            + "     scHGAP INT\n"
            + ")";
    String buttons = "create table BUTTONS\n"
            + "(\n"
            + "     bID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     bNAME VARCHAR(50) not null,\n"
            + "     bPRODUCT VARCHAR(15) not null,\n"
            + "     bTYPE INT not null,\n"
            + "     bCOLOR VARCHAR(7),\n"
            + "     bFONT_COLOR VARCHAR(7),\n"
            + "     bWIDTH INT,\n"
            + "     bHEIGHT INT,\n"
            + "     bXPOS INT,\n"
            + "     bYPOS INT,\n"
            + "     bACCESS_LEVEL INT,\n"
            + "     bSCREEN_ID INT not null references SCREENS(scID),\n"
            + "     bLINK VARCHAR(50)\n"
            + ")";
    String wasteReasons = "create table WASTEREASONS\n"
            + "(\n"
            + "     wrID INT not null primary key,\n"
            + "     wrREASON VARCHAR(30),\n"
            + "     wrpriv INT,\n"
            + "     wrDELETED BOOLEAN\n"
            + ")";
    String refundReason = "CREATE TABLE REFUND_REASONS\n"
            + "(\n"
            + "     refID INT not null primary key,\n"
            + "     refREASON VARCHAR(30) NOT NULL,\n"
            + "     refLEVEL INT NOT NULL,\n"
            + "     refDELETED BOOLEAN NOT NULL\n"
            + ")";
    String wasteItems = "create table WASTEITEMS\n"
            + "(\n"
            + "     wiID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     wiPRODUCT VARCHAR(15) not null references PRODUCTS(BARCODE),\n"
            + "     wiQUANTITY INT,\n"
            + "     wiREASON INT not null references WASTEREASONS(wrID),\n"
            + "     wiVALUE DOUBLE,\n"
            + "     wiTIMESTAMP BIGINT\n"
            + ")";
    String suppliers = "create table SUPPLIERS\n"
            + "(\n"
            + "     sID INT not null primary key,\n"
            + "     sNAME VARCHAR(30),\n"
            + "     sADDRESS VARCHAR(100),\n"
            + "     sPHONE VARCHAR(20),\n"
            + "     sACCOUNT_NUMBER VARCHAR(20),\n"
            + "     sEMAIL VARCHAR(30)\n"
            + ")";
    String receivedItems = "create table RECEIVEDITEMS\n"
            + "(\n"
            + "     riID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     riPRODUCT VARCHAR(15) not null references PRODUCTS(BARCODE),\n"
            + "     ritotal DOUBLE,\n"
            + "     riQUANTITY INT,\n"
            + "     ripacks int,\n"
            + "     riRECEIVED_REPORT INT not null references RECEIVED_REPORTS(rrID)\n"
            + ")";
    String clockOnOff = "create table CLOCKONOFF\n"
            + "(\n"
            + "     ID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     STAFF int not null references STAFF(stID),\n"
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
            + "     TERMINAL INT not null references TILLS(tiID),\n"
            + "     DECLARED DOUBLE,\n"
            + "     EXPECTED DOUBLE,\n"
            + "     TRANSACTIONS int,\n"
            + "     TAX DOUBLE,\n"
            + "     STAFF INT not null references STAFF(stID),\n"
            + "     TIME bigint\n"
            + ")";
    String receivedReports = "create table RECEIVED_REPORTS\n"
            + "(\n"
            + "     rrID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),"
            + "     rrINVOICE_NO VARCHAR(30) not null,\n"
            + "     rrSUPPLIER_ID INT not null references SUPPLIERS(sID),\n"
            + "     rrPAID BOOLEAN\n"
            + ")";
    String condiments = "create table CONDIMENTS\n"
            + "(\n"
            + "     ID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     PRODUCT VARCHAR(15) not null references PRODUCTS(BARCODE),\n"
            + "     PRODUCT_CON VARCHAR(15) not null references PRODUCTS(BARCODE)\n"
            + ")";
    String orders = "create table ORDERS\n"
            + "(\n"
            + "     oID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     oSUPPLIER int not null references SUPPLIERS(sID),\n"
            + "     oSENT BOOLEAN,\n"
            + "     oSENDDATE BIGINT,\n"
            + "     oRECEIVED BOOLEAN\n"
            + ")";
    String orderItems = "create table ORDERITEMS\n"
            + "(\n"
            + "     oiID INT not null primary key\n"
            + "         GENERATED ALWAYS AS IDENTITY\n"
            + "         (START WITH 1, INCREMENT BY 1),\n"
            + "     oiPRODUCT VARCHAR(15) not null references PRODUCTS(BARCODE),\n"
            + "     oiORDER_ID int not null references ORDERS(oID),\n"
            + "     oiQUANTITY INT,\n"
            + "     oiPRICE DOUBLE\n"
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
            s.executeUpdate(tax);
            s.executeUpdate(departments);
            s.executeUpdate(categorys);
            s.executeUpdate(customers);
            s.executeUpdate(products);
            s.executeUpdate(discounts);
            s.executeUpdate(buckets);
            s.executeUpdate(triggers);
            s.executeUpdate(staff);
            s.executeUpdate(screens);
            s.executeUpdate(tills);
            s.executeUpdate(sales);
            s.executeUpdate(saleItems);
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
            s.executeUpdate(refundReason);
            String addDepartment = "INSERT INTO DEPARTMENTS (did, dNAME) VALUES (1,'Default')";
            String addCategory = "INSERT INTO CATEGORYS (cid, cNAME, cDEPARTMENT) VALUES (1, 'Default', 1)";
            String addTax = "INSERT INTO TAX (tid, tNAME, tVALUE) VALUES (1, 'ZERO',0.0)";
            String addWasteReason = "INSERT INTO WASTEREASONS (wrid, wrREASON, wrDELETED) VALUES (1, 'Default', 'FALSE')";
            String addRefundReason = "insert into REFUND_REASONS (refid, refreason, reflevel, refdeleted) values(1, 'Default', 1, FALSE)";
            s.executeUpdate(addDepartment);
            s.executeUpdate(addCategory);
            s.executeUpdate(addTax);
            s.executeUpdate(addWasteReason);
            s.executeUpdate(addRefundReason);
            s.close();
        } catch (SQLException ex) {
            LOG.info("Tables already exists, so they do not need created");
        }
    }
}
