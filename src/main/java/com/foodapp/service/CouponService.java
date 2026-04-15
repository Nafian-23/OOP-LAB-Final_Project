package com.foodapp.service;

import com.foodapp.model.Coupon;

import java.util.HashMap;
import java.util.Map;

public class CouponService {
    private final Map<String, Coupon> coupons = new HashMap<>();

    public CouponService() {
        coupons.put("SAVE10", new Coupon("SAVE10", 10, true));
        coupons.put("SAVE20", new Coupon("SAVE20", 20, true));
    }

    public Coupon validate(String code) {
        if (code == null) {
            throw new CouponException("Invalid coupon code: null");
        }
        Coupon coupon = coupons.get(code.toUpperCase());
        if (coupon == null || !coupon.isActive()) {
            throw new CouponException("Invalid or expired coupon code: " + code);
        }
        return coupon;
    }

    public double apply(Coupon coupon, double subtotal) {
        return subtotal * (1 - coupon.getDiscountPercent() / 100.0);
    }

    public java.util.List<Coupon> getAllCoupons() {
        return new java.util.ArrayList<>(coupons.values());
    }
}
