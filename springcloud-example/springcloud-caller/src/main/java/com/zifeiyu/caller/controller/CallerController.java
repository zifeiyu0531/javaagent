package com.zifeiyu.caller.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping("/springcloud/service")
public class CallerController {

    @Autowired
    private RestTemplate restTemplate;
    private String serviceName = "SPRINGCLOUD-CLIENT";
    private String infoUrlFmt = "http://%s/springcloud/client/info?name=%s";
    private String sumUrlFmt = "http://%s/springcloud/client/sum?a=%d&b=%d";

    @GetMapping("/caller")
    public String caller() {
        String strRtn = restTemplate.getForObject(String.format(infoUrlFmt, serviceName, "zifeiyu"), String.class);
        Integer intRtn = restTemplate.getForObject(String.format(sumUrlFmt, serviceName, 1, 2), Integer.class);
        return strRtn + "\n" + intRtn;
    }
}
