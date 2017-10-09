/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author David
 */
public class StockGraphWindow extends JFrame {

    private final Logger log = Logger.getGlobal();

    private static StockGraphWindow window;

    private final DataConnect dc;

    private GraphPanel graph;

    public StockGraphWindow(DataConnect dc) {
        super();
        this.dc = dc;
        init();
        setIconImage(GUI.icon);
        setTitle("Stock Graph");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public static void showWindow(DataConnect dc) {
        window = new StockGraphWindow(dc);
        window.setVisible(true);
    }

    private void init() {
        try {
            graph = new GraphPanel(1500, 768);
            graph.setList(dc.getAllProducts());
            this.add(graph);
            pack();
            validate();
        } catch (IOException | SQLException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

    private class GraphPanel extends JPanel {

        private List<Product> products;

        private int bar_width;
        private int no_bars;

        private final int GRAPH_WIDTH;
        private final int GRAPH_HEIGHT;

        public GraphPanel(int width, int height) {
            super();
            super.setSize(width, height);
            this.GRAPH_WIDTH = width - 20;
            this.GRAPH_HEIGHT = height - 20;
            products = new ArrayList<>();
        }

        @Override
        public void paintComponent(Graphics g) {
            for (int i = 0; i < products.size(); i++) {
                if (products.get(i).isOpen()) {
                    products.remove(i);
                }
            }
            no_bars = products.size();
            bar_width = GRAPH_WIDTH / no_bars;
            int maxStock = 0;
            int currentPos = 0;
            for (Product p : products) {
                if (p.getStock() > maxStock) {
                    maxStock = p.getStock();
                }
            }
            int pixlesPerStock = GRAPH_HEIGHT / maxStock;
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, GRAPH_WIDTH, GRAPH_HEIGHT);
            g.drawString("-" + maxStock, GRAPH_WIDTH, 10);
            g.drawString("-" + (maxStock / 2), GRAPH_WIDTH, GRAPH_HEIGHT / 2);
            g.drawLine(0, GRAPH_HEIGHT / 2, GRAPH_WIDTH, GRAPH_HEIGHT / 2);
            for (Product p : products) {
                int h = p.getStock() * pixlesPerStock;
                if (p.getMinStockLevel() > 0 && (p.getStock() < p.getMinStockLevel())) {
                    g.setColor(Color.ORANGE);
                } else if (p.getStock() <= 0) {
                    g.setColor(Color.RED);
                } else {
                    g.setColor(Color.BLUE);
                }
                g.fillRect(currentPos, GRAPH_HEIGHT - h, bar_width - 2, h);
                g.setColor(Color.BLACK);
                g.drawString(p.getName(), currentPos + 2, GRAPH_HEIGHT + 20);
                g.drawString("" + p.getStock(), currentPos + 2, GRAPH_HEIGHT + 40);
                currentPos += bar_width;
            }
        }

        public void addProduct(Product p) {
            products.add(p);
        }

        public void setList(List<Product> products) {
            this.products = products;
        }
    }

}
