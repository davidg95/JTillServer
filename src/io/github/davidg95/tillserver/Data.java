/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.tillserver;

import io.github.davidg95.Till.till.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data class which stores all system data.
 *
 * @author David
 */
public class Data {

    private final DBConnect dbConnection;
    private final GUI g;

    private final List<Staff> loggedIn;
    private final List<Staff> loggedInTill;
    private double takings;
    private int sales;

    private final Semaphore logSem;
    private final Semaphore tillLogSem;

    /**
     * Blank constructor which initialises the product and customers data
     * structures. ArrayList data structures are used.
     *
     * @param db DBConnect class.
     * @param g GUI class.
     */
    public Data(DBConnect db, GUI g) {
        this.dbConnection = db;
        this.g = g;
        loggedIn = new ArrayList<>();
        loggedInTill = new ArrayList<>();
        logSem = new Semaphore(1);
        tillLogSem = new Semaphore(1);
    }

    /**
     * Method to log a member of staff in using a username and password. This
     * will be used for logging in to the server interface.
     *
     * @param username the username as a String.
     * @param password the password as a String.
     * @return the member of staff who has logged in.
     * @throws LoginException if there was an error logging in.
     * @throws java.sql.SQLException if there was a database error.
     */
    public Staff login(String username, String password) throws LoginException, SQLException {
        Staff s = dbConnection.login(username, password);
        try {
            logSem.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (loggedIn.contains(s)) {
            throw new LoginException("You are already logged in elsewhere");
        }
        loggedIn.add(s);
        logSem.release();
        return s;
    }

    /**
     * Method to log in using an id. This will be used for logging in to a till.
     *
     * @param id the id to log in.
     * @return the member of staff who has logged in.
     * @throws LoginException if they are already logged in on a till.
     * @throws java.sql.SQLException if there was a database error.
     */
    public Staff login(int id) throws LoginException, SQLException {
        Staff s;
        try {
            s = dbConnection.getStaff(id);
            try {
                tillLogSem.acquire();
            } catch (InterruptedException ex) {
                Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (Staff st : loggedInTill) {
                if (st.getId() == s.getId()) {
                    throw new LoginException("You are already logged in else where");
                }
            }
            loggedInTill.add(s);
        } catch (StaffNotFoundException ex) {
            throw new LoginException(id + " could not be found");
        }
        tillLogSem.release();
        return s;
    }

    /**
     * Method to log a member of staff out.
     *
     * @param id the id of the staff to log out.
     * @throws StaffNotFoundException if the staff member was not found.
     */
    public void logout(int id) throws StaffNotFoundException {
        try {
            tillLogSem.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Staff s : loggedIn) {
            if (s.getId() == id) {
                loggedIn.remove(s);
                tillLogSem.release();
                return;
            }
        }
        throw new StaffNotFoundException(id + "");
    }

    /**
     * Method to log a member of staff out the till.
     *
     * @param id the id of the staff to log out.
     * @throws StaffNotFoundException if the staff member was not found.
     */
    public void tillLogout(int id) throws StaffNotFoundException {
        for (Staff s : loggedInTill) {
            if (s.getId() == id) {
                loggedInTill.remove(s);
                return;
            }
        }
        throw new StaffNotFoundException(id + "");
    }

    public void logAllTillsOut(){
        for(int i = 0; i < this.loggedInTill.size(); i++){
            this.loggedInTill.remove(i);
        }
    }

    public void addTakings(double val) {
        takings += val;
    }

    public void addSale() {
        sales++;
    }

    public double getTakings() {
        return takings;
    }

    public int getSales() {
        return sales;
    }

    public void reset() {
        takings = 0;
        sales = 0;
    }
}
