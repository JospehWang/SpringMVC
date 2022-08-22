package com.springmvc.handler;

import java.lang.reflect.Method;

public class MyHandler {
    private String url;
    private Object Controller;
    private Method method;

    public MyHandler() {
    }

    public MyHandler(String url, Object controller, Method method) {
        this.url = url;
        Controller = controller;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Object getController() {
        return Controller;
    }

    public void setController(Object controller) {
        Controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "MyHandler{" +
                "url='" + url + '\'' +
                ", Controller=" + Controller +
                ", method=" + method +
                '}';
    }
}
