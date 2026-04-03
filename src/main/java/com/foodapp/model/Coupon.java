package com.foodapp.model;

import java.io.Serializable;
import java.util.Objects;

public class Coupon implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private int discountPercent;
    private boolean active;

    public Coupon() {}

    public Coupon(String code, int discountPercent, boolean active) {
        this.code = code;
        this.discountPercent = discountPercent;
        this.active = active;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coupon)) return false;
        Coupon coupon = (Coupon) o;
        return Objects.equals(code, coupon.code);
    }

    @Override
    public int hashCode() { return Objects.hash(code); }

    @Override
    public String toString() {
        return "Coupon{code='" + code + "', discountPercent=" + discountPercent + ", active=" + active + "}";
    }
}
