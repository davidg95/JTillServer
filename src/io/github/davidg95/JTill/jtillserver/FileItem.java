/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import io.github.davidg95.JTill.jtill.Product;

/**
 * Models an item in a data file.
 *
 * @author David
 */
public class FileItem {

    private String barcode;
    private Product product;
    private int quantity;

    public FileItem(String barcode, Product product, int quantity) {
        this.barcode = barcode;
        this.product = product;
        this.quantity = quantity;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return this.barcode + " - " + this.quantity;
    }
}
