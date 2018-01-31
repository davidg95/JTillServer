/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver.printables;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * @author David
 */
public class TransactionReportPrintable implements Printable {

    private final Till till;
    private final Date start;
    private final Date end;
    private final List<Sale> sales;
    private final int transactionCount;
    private BigDecimal totalValue;
    private final BigDecimal average;
    private int negativeSales;
    private BigDecimal negativeSaleValue;
    private BigDecimal refundsValue;
    private BigDecimal taxValue;
    private BigDecimal netSales;

    private final int x = 100;

    public TransactionReportPrintable(List<Sale> sales, Till till, Date start, Date end) {
        this.sales = sales;
        this.transactionCount = sales.size();
        init();
        if (transactionCount == 0) {
            average = new BigDecimal(0);
        } else {
            average = totalValue.divide(new BigDecimal(transactionCount), 2, RoundingMode.HALF_UP);
        }
        this.till = till;
        this.start = start;
        this.end = end;
    }

    private void init() {
        negativeSales = 0;
        totalValue = new BigDecimal(0);
        negativeSaleValue = new BigDecimal(0);
        refundsValue = new BigDecimal(0);
        taxValue = new BigDecimal(0);
        netSales = new BigDecimal(0);
        for (Sale s : sales) {
            totalValue = totalValue.add(s.getTotal());
            if (s.getTotal().compareTo(BigDecimal.ZERO) == -1) {
                negativeSales++;
                negativeSaleValue = negativeSaleValue.add(s.getTotal());
            }
            for (SaleItem si : s.getSaleItems()) {
                taxValue = taxValue.add(si.getTaxValue());
                if (si.getPrice().compareTo(BigDecimal.ZERO) == -1) {
                    refundsValue = refundsValue.add(si.getPrice());
                }
            }
        }
        netSales = totalValue.subtract(taxValue);
    }

    @Override
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        int y = 100;
        g.setFont(new Font(g.getFont().getName(), g.getFont().getStyle(), 18));
        int lineSpace = g.getFontMetrics().getHeight();
        if (till == null) {
            g.drawString("Report for all terminals", x, y);
        } else {
            g.drawString("Report for Terminal " + till.getName(), x, y);
        }
        y += lineSpace;
        g.drawString("From " + start.toString() + " to " + end.toString(), x, y);
        y += lineSpace * 2;
        int x2 = 300;
        int startY = y;
        DecimalFormat df = new DecimalFormat("0.00");
        g.drawLine(x, y + 4 - lineSpace, x2 + 100, y + 4 - lineSpace);
        g.drawLine(x, y + 4, x2 + 100, y + 4);
        g.drawString("Transaction Count", x + 5, y);
        g.drawString("" + transactionCount, x2 + 5, y);
        y += lineSpace;
        g.drawLine(x, y + 4, x2 + 100, y + 4);
        g.drawString("Total Sales Value", x + 5, y);
        g.drawString("£" + df.format(totalValue), x2 + 5, y);
        y += lineSpace;
        g.drawLine(x, y + 4, x2 + 100, y + 4);
        g.drawString("Average Spend", x + 5, y);
        g.drawString("£" + df.format(average), x2 + 5, y);
        y += lineSpace;
        g.drawLine(x, y + 4, x2 + 100, y + 4);
        g.drawString("Negative Sales", x + 5, y);
        g.drawString(negativeSales + "", x2 + 5, y);
        y += lineSpace;
        g.drawLine(x, y + 4, x2 + 100, y + 4);
        g.drawString("Negative Sales Value", x + 5, y);
        g.drawString("£" + df.format(negativeSaleValue), x2 + 5, y);
        y += lineSpace;
        g.drawLine(x, y + 4, x2 + 100, y + 4);
        g.drawString("Refunds", x + 5, y);
        g.drawString("£" + df.format(refundsValue), x2 + 5, y);
        y += lineSpace;
        g.drawLine(x, y + 4, x2 + 100, y + 4);
        g.drawString("Tax Value", x + 5, y);
        g.drawString("£" + df.format(taxValue), x2 + 5, y);
        y += lineSpace;
        g.drawLine(x, y + 4, x2 + 100, y + 4);
        g.drawString("Net Sales", x + 5, y);
        g.drawString("£" + df.format(netSales), x2 + 5, y);
        g.drawLine(x, startY + 4 - lineSpace, x, y + 4);
        g.drawLine(x2, startY + 4 - lineSpace, x2, y + 4);
        g.drawLine(x2 + 100, startY + 4 - lineSpace, x2 + 100, y + 4);
        return PAGE_EXISTS;
    }

}
