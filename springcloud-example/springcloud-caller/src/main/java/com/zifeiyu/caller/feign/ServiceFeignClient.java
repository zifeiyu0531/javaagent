package com.zifeiyu.caller.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "SPRINGCLOUD-CLIENT")
public interface ServiceFeignClient {

    @GetMapping(value = "/springcloud/client/info")
    String info(@RequestParam("name") String name);

    @GetMapping(value = "/springcloud/client/sum")
    int sum(@RequestParam("a") final int a, @RequestParam("b") final int b);
}
