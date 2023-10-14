package com.steincker.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Random;

@RestController
@RequestMapping(value = "/demo")
public class DemoController {

    @GetMapping("/test")
    @ResponseBody
    public String loop() {
       try {
           Date.parse("111");
       }catch (Exception e){
       }
       Random random = new Random();        // Noncompliant - new instance created with each invocation
       int rValue = random.nextInt();
       23432
      return "你好啊，这个是个演示的web项目1111111！";
    }


}
