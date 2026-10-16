package com.leaseapp.model.homeowner;

import java.math.BigDecimal;

// Section 13 - Rent - filled by homeowner
public class RentDetails {
    private BigDecimal amount;
    private String frequency;          // WEEK / MONTH
    private String paymentMethod;      // CASH / AUTO_WITHDRAWAL / POST_DATED_CHEQUES / CHEQUE / OTHER
    private String paymentMethodOther;
    private int dueDay;
    private String payableTo;

    public RentDetails() {}

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentMethodOther() { return paymentMethodOther; }
    public void setPaymentMethodOther(String paymentMethodOther) { this.paymentMethodOther = paymentMethodOther; }

    public int getDueDay() { return dueDay; }
    public void setDueDay(int dueDay) { this.dueDay = dueDay; }

    public String getPayableTo() { return payableTo; }
    public void setPayableTo(String payableTo) { this.payableTo = payableTo; }
}