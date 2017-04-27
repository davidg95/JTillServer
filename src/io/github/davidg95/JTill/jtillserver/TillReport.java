/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import java.math.BigDecimal;

/**
 *
 * @author David
 */
public class TillReport {

    public BigDecimal declared;
    public BigDecimal actualTakings;
    public BigDecimal difference;
    public int transactions;
    public BigDecimal averageSpend;
    public BigDecimal tax;
}
