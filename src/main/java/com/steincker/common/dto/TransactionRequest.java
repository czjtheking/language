package com.steincker.common.dto;

/**
 * @ClassName TransactionRequest
 * @Author ST000056
 * @Date 2024-12-20 18:13
 * @Version 1.0
 * @Description
 **/
public class TransactionRequest {
    private int type; // 0 for withdrawal, 1 for deposit
    private double[] nums; // Array of transaction amounts

    // Getters and setters
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double[] getNums() {
        return nums;
    }

    public void setNums(double[] nums) {
        this.nums = nums;
    }
}