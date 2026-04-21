package com.steincker.entity;

import java.util.concurrent.locks.ReentrantLock;

public class BankAccount {
    private double balance;
    private final ReentrantLock lock = new ReentrantLock();

    // 构造函数
    public BankAccount(double initialBalance) {
        this.balance = initialBalance;
    }

    // 存款方法
    public void deposit(double amount) {
        lock.lock();
        try {
            double newBalance = balance + amount;
            balance = newBalance;
            System.out.println(Thread.currentThread().getName() + " deposited " + amount + ". New balance is " + balance);
        } finally {
            lock.unlock();
        }
    }

    // 取款方法
    public void withdraw(double amount) {
        lock.lock();
        try {
            if (amount <= balance) {
                double newBalance = balance - amount;
                balance = newBalance;
                System.out.println(Thread.currentThread().getName() + " withdrew " + amount + ". New balance is " + balance);
            } else {
                System.out.println(Thread.currentThread().getName() + " withdrawal failed. Insufficient funds.");
            }
        } finally {
            lock.unlock();
        }
    }

    // 获取余额方法
    public double getBalance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }
}
