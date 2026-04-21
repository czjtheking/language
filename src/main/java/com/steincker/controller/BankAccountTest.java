package com.steincker.controller;

import com.steincker.entity.BankAccount;

/**
 * @ClassName BankAccountTest
 * @Author ST000056
 * @Date 2024-12-20 17:44
 * @Version 1.0
 * @Description
 **/
public class BankAccountTest {
    public static void main(String[] args) {
        BankAccount account = new BankAccount(1000); // 初始余额为1000

        Thread depositThread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                account.deposit(100);
            }
        });

        Thread withdrawThread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                account.withdraw(50);
            }
        });

        depositThread.start();
        withdrawThread.start();

        try {
            depositThread.join();
            withdrawThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Final balance is " + account.getBalance());
    }
}
