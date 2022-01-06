package com.agent.interceptor.Impl;

import com.agent.interceptor.AroundInterceptor;


public class MyInterceptor implements AroundInterceptor {

    @Override
    public void before(Object target, Object[] args) {
        String serviceName = target.getClass().getSimpleName();
        System.out.println("serviceName: " + serviceName);
        for (Object arg : args) {
            System.out.println(arg);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

    }
}
