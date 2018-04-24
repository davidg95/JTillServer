/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver.printables;

import io.github.davidg95.JTill.jtill.DataConnect;
import io.github.davidg95.JTill.jtill.JTill;
import io.github.davidg95.JTill.jtill.OrderItem;
import io.github.davidg95.JTill.jtill.Supplier;
import io.github.davidg95.JTill.jtillserver.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 *
 * @author David
 */
public class OrderPrintable implements Printable {

    private final List<OrderItem> items;
    private final Supplier supplier;

    private final JTill jtill;

    private String name;
    private String address;

    private final String companyDetails = System.getenv("APPDATA") + "\\JTill Server\\company.details";

    public OrderPrintable(JTill jtill, Supplier s, List<OrderItem> items) {
        this.items = items;
        this.supplier = s;
        this.jtill = jtill;
        loadFile();
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return Printable.NO_SUCH_PAGE;
        }

        final Graphics2D g = (Graphics2D) graphics;

        final int x = 70;
        int y = 70;
        final int width = (int) (pageFormat.getWidth() - x - x);
        final int lineSpace = g.getFontMetrics().getHeight();

        String to = "Delivery to:";
        g.drawString("Delivery to:", x, y);

        int x1 = x + g.getFontMetrics().stringWidth(to);

        g.drawString(name, x1, y);
        y += lineSpace;
        g.drawString(address, x1, y);
        y += 20;

        int topy = y;
        x1 = x + 10;
        int x2 = x1 + 70;
        int x4 = x + width - 70;
        int x3 = x4 - 40;

        g.drawString("Order Code", x1, y);
        g.drawString("Description", x2, y);
        g.drawString("Qty.", x3, y);
        g.drawString("Total", x4, y);
        g.drawLine(x, y + 5, x + width, y + 5);
        y += lineSpace;

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem i : items) {
            g.drawString(i.getOrderCode() + "", x1, y);
            g.drawString(i.getName(), x2, y);
            g.drawString(i.getQuantity() + "", x3, y);
            g.drawString("£" + i.getPrice(), x4, y);
            y += lineSpace;
            total = total.add(i.getPrice());
        }
        y -= lineSpace;
        g.drawLine(x, y + 5, x + width, y + 5);
        y += lineSpace;
        g.drawString("Total", x1, y);
        g.drawString("£" + total, x4, y);
        return Printable.PAGE_EXISTS;
    }

    private void loadFile() {
        InputStream in;
        try {
            in = new FileInputStream(companyDetails);
            Properties properties = new Properties();
            properties.load(in);

            name = (properties.getProperty("NAME"));
            address = (properties.getProperty("ADDRESS"));

            in.close();
        } catch (FileNotFoundException | UnknownHostException ex) {
            JOptionPane.showMessageDialog(GUI.gui, "Failed to load company details", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(GUI.gui, "Failed to load company details", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
