package com.steincker.controller;

import com.steincker.common.dto.TransactionRequest;
import com.steincker.entity.BankAccount;
import com.steincker.service.AsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName AsyncController
 * @Author ST000056
 * @Date 2024-04-09 14:57
 * @Version 1.0
 * @Description 异步任务测试
 **/
@RestController
@RequestMapping(value = "/async")
public class AsyncController {

    @Autowired
    private AsyncService asyncService;

    @GetMapping("/startAsync")
    public String startAsync() {
        asyncService.testAsyncTask();
        return "异步任务已启动";
    }

    private final BankAccount bankAccount = new BankAccount(1000); // Initial balance is 1000

    @PostMapping("/bank/transaction")
    public String processTransaction(@RequestBody TransactionRequest request) {
        for (double amount : request.getNums()) {
            switch (request.getType()) {
                case 1:
                    bankAccount.deposit(amount);
                    break;
                case 0:
                    try {
                        bankAccount.withdraw(amount);
                    } catch (RuntimeException e) {
                        return "Exception: " + e.getMessage();
                    }
                    break;
                default:
                    return "Invalid transaction type";
            }
        }
        return "Final balance is " + bankAccount.getBalance();
    }
}
