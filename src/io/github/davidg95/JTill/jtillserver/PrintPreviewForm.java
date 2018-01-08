/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author David
 */
public class PrintPreviewForm {

    private JFrame frame;
    private JPanel container;
    private JToolBar bar;

    private final Printable print;

    public PrintPreviewForm(Printable print) {
        this.print = print;
        init();
    }

    private void init() {
        frame = new JFrame();
        
        container = new JPanel();
        container.setLayout(new CardLayout());
        
        JButton next = new JButton("Next");
        next.addActionListener((event) -> {
            ((CardLayout) container.getLayout()).next(container);
        });

        JButton prev = new JButton("Previous");
        prev.addActionListener((event) -> {
            ((CardLayout) container.getLayout()).last(container);
        });
        
        bar = new JToolBar();
        bar.add(next);
        bar.add(prev);
        
        frame.add(bar);
        frame.add(container);
//        frame.add(next);
//        frame.add(prev);

        frame.setBackground(Color.WHITE);
        frame.setSize((int) new PageFormat().getWidth(), (int) new PageFormat().getHeight());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        print();
        ((CardLayout) container.getLayout()).show(container, "0");
    }

    private void print() {
        int page = 0;
        try {
            while (true) {
                MyPanel panel = new MyPanel(print, page);
                container.add(panel, "" + page);
                if (panel.isPage() == Printable.NO_SUCH_PAGE) {
                    break;
                }
                page++;
            }
        } catch (PrinterException ex) {
            Logger.getLogger(PrintPreviewForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class MyPanel extends JPanel {

        private final Printable p;
        private final int pageNo;

        public MyPanel(Printable p, int pageNo) {
            super();
            this.p = p;
            this.pageNo = pageNo;
        }

        public int isPage() throws PrinterException {
            return p.print(frame.getGraphics(), new PageFormat(), pageNo);
        }

        @Override
        public void paintComponent(Graphics g) {
            try {
                p.print(g, new PageFormat(), pageNo);
            } catch (PrinterException ex) {
                Logger.getLogger(PrintPreviewForm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
