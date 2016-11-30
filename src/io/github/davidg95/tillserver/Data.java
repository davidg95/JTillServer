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
import javax.swing.JOptionPane;

/**
 * Data class which stores all system data.
 *
 * @author David
 */
public class Data {
    
    private List<Staff> staff;

    private final DBConnect dbConnection;
    private final GUI g;

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
        staff = new ArrayList<>();

    }

    public void loadDatabase() {
        if (dbConnection.isConnected()) {
            try {
                staff = dbConnection.getAllStaff();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
                staff = new ArrayList<>();
            }
        } else {
            staff = new ArrayList<>();
        }
    }

    public void updateDatabase() throws SQLException {
        dbConnection.updateWholeStaff(staff);
    }

    public void close() throws SQLException {
        updateDatabase();
        dbConnection.close();
    }

    public List<Staff> getStaffList() {
        return this.staff;
    }

    public void setStaffList(List<Staff> staff) {
        this.staff = staff;
    }

    //Staff Methods
    /**
     * Method to add a new member of staff tot he system.
     *
     * @param s the new member of staff to add.
     */
    public void addStaff(Staff s) {
        staff.add(s);
    }

    /**
     * Method to remove a member of staff from the system.
     *
     * @param s the member of staff to remove.
     */
    public void removeStaff(Staff s) {
        staff.remove(s);
    }

    /**
     * Method to remove a member of staff from the system by passing in their
     * id.
     *
     * @param id the id of the staff to remove.
     * @throws StaffNotFoundException if the id could not be found.
     */
    public void removeStaff(int id) throws StaffNotFoundException {
        for (int i = 0; i < staff.size(); i++) {
            if (staff.get(i).getId()== id) {
                staff.remove(i);
                return;
            }
        }
        throw new StaffNotFoundException(id + "");
    }

    /**
     * Method to get a member of staff by passing in their id.
     *
     * @param id the id of the staff to get.
     * @return Staff object that matches the id.
     * @throws StaffNotFoundException if the id could not be found.
     */
    public Staff getStaff(int id) throws StaffNotFoundException {
        for (Staff s : staff) {
            if (s.getId() == id) {
                return s;
            }
        }
        throw new StaffNotFoundException(id + "");
    }

    /**
     * Method to log a member of staff in using a username and password. This
     * will be used for logging in to the server interface.
     *
     * @param username the username as a String.
     * @param password the password as a String.
     * @return the member of staff who has logged in.
     * @throws LoginException if there was an error logging in.
     */
    public Staff login(String username, String password) throws LoginException {
        for (Staff s : staff) {
            if (s.getUsername().equals(username)) {
                s.login(password);
                return s;
            }
        }
        throw new LoginException("Your credentials were not recognised");
    }

    /**
     * Method to log in using an id. This will be used for logging in to a till.
     *
     * @param id the id to log in.
     * @return the member of staff who has logged in.
     * @throws LoginException if they are already logged in on a till.
     * @throws StaffNotFoundException if the id was not found.
     */
    public Staff login(int id) throws LoginException, StaffNotFoundException {
        for (Staff s : staff) {
            if (s.getId() == id) {
                s.login();
                return s;
            }
        }
        throw new StaffNotFoundException(id + "");
    }

    /**
     * Method to log a member of staff out.
     *
     * @param id the id of the staff to log out.
     * @throws StaffNotFoundException if the staff member was not found.
     */
    public void logout(int id) throws StaffNotFoundException {
        for (Staff s : staff) {
            if (s.getId() == id) {
                s.logout();
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
        for (Staff s : staff) {
            if (s.getId() == id) {
                s.tillLogout();
                return;
            }
        }
        throw new StaffNotFoundException(id + "");
    }

    /**
     * Method to get the total number of staff on the system.
     *
     * @return int value representing how many staff are on the system.
     */
    public int staffCount() {
        return staff.size();
    }

    /**
     * Method to generate a new 6-digit staff id.
     *
     * @return String value of new 6-digit staff id.
     */
    public static String generateStaffID() {
        String no;
        String zeros;
        no = Integer.toString(DBConnect.staffCounter);
        zeros = "";
        for (int i = no.length(); i < 6; i++) {
            zeros += "0";
        }
        DBConnect.staffCounter++;

        return zeros + no;
    }
}
