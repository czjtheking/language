package com.steincker.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/demo")
public class DemoController {

    @GetMapping("/test")
    @ResponseBody
    public String loop() {
        return "你好啊，这个是个web项目！ 我再次提交了代码！";
    }


}
