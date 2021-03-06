/*
 * Copyright 2016 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.bootstrap.interceptor;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class ExceptionHandleAroundInterceptor0 implements AroundInterceptor0 {

    private final AroundInterceptor0 delegate;
    private final ExceptionHandler exceptionHandler;

    public ExceptionHandleAroundInterceptor0(AroundInterceptor0 delegate, ExceptionHandler exceptionHandler) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "exceptionHandler");
    }

    @Override
    public void before(Object target) {
        try {
            this.delegate.before(target);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }

    }

    @Override
    public void after(Object target, Object result, Throwable throwable) {
        try {
            this.delegate.after(target, result, throwable);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }
}