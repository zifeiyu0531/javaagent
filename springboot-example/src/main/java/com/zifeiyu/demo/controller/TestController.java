package com.zifeiyu.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class TestController {
    @GetMapping("/call")
    public String call() {
        String str = "call is called";
        System.out.println(str);
        return str;
    }
}
