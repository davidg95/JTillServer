/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver.printables;

import io.github.davidg95.JTill.jtill.Product;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.DecimalFormat;
import java.util.List;

/**
 *
 * @author David
 */
public class ProductReportPrintable implements Printable {

    private final List<Product> products;
    private final String dateRange;
    private int current;
    private final int xMargin = 30;
    private final int max_per_page = 20;

    public ProductReportPrintable(List<Product> products, String dateRange) {
        this.products = products;
        current = 0;
        this.dateRange = dateRange;
    }

    @Override
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if ((pageIndex * max_per_page) - max_per_page >= products.size()) {
            return NO_SUCH_PAGE;
        }
        int y = 30;
        int lineHeight = g.getFontMetrics().getHeight();
        if (pageIndex == 0) {
            g.drawString("Product Report", xMargin, y);
            y += lineHeight;
            g.drawString("From " + dateRange, xMargin, y);
        }
        y += lineHeight;
        int col1 = xMargin;
        int col2 = (int) (pageFormat.getWidth() / 2);
        int col3 = (int) (col2 + (pageFormat.getWidth() / 4));
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(xMargin, y, (int) (pageFormat.getWidth() - (xMargin * 2)), lineHeight + 10);
        g.setColor(Color.BLACK);
        g.drawRect(xMargin, y, (int) (pageFormat.getWidth() - (xMargin * 2)), lineHeight + 10);
        y += lineHeight;
        g.drawString("Product", col1, y);
        g.drawString("Value Sold", col2, y);
        g.drawString("Amount Sold", col3, y);
        y += lineHeight + 10;
        DecimalFormat df = new DecimalFormat("0.00");
        for (int i = max_per_page * pageIndex; i < products.size() && i < max_per_page; i++) {
            Product p = products.get(i);
            g.drawString(p.getLongName(), col1, y);
            g.drawString("£" + df.format(p.getPrice()), col2, y);
            g.drawString(p.getStock() + "", col3, y);
            y += lineHeight;
            current++;
        }
        String pageNumber = "Page " + (pageIndex + 1);
        g.drawString(pageNumber, (int) (pageFormat.getWidth() / 2) - (g.getFontMetrics().stringWidth(pageNumber) / 2), (int) pageFormat.getHeight() - 20);
        return PAGE_EXISTS;
    }

}
