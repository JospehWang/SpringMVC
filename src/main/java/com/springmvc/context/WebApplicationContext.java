package com.springmvc.context;

import com.bruce.controller.UserController;
import com.bruce.pojo.User;
import com.bruce.service.UserService;
import com.springmvc.annotation.AutoWired;
import com.springmvc.annotation.Controller;
import com.springmvc.annotation.Service;
import com.springmvc.exception.ContextException;
import com.springmvc.xml.XmlPaser;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebApplicationContext {
    //classpath:springmvc.xml
    String contextConfigLocation;
    List<String> classNameList = new ArrayList<String>();

    //Spring的IOC容器
    public Map<String, Object> iocMap = new ConcurrentHashMap<String, Object>();

    public WebApplicationContext(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

    /**
     * 初始化Spring容器
     */
    public void refresh() {
        //1、解析classpath:springmvc.xml文件。dom4j
        String basePackage = XmlPaser.getBasePackage(contextConfigLocation.split(":")[1]);
        String[] basePackages = basePackage.split(",");
        if (basePackages.length > 0) {
            for (String aPackage : basePackages) {
                //com.bruce.service,com.bruce.controller
                excuteScanPackage(aPackage);

            }
            //扫描到[com.bruce.service.impl.UserServiceImpl, com.bruce.service.UserService, com.bruce.controller.UserController]
            System.out.println("扫描到配置文件中的类" + classNameList);

            //扫描到xml配置的类后进行实例化
            excuteInstance();
            //IOC容器的对象是
            System.out.println("SpringIOC容器的对象" + iocMap);
            //实现Spring容器中对象注入
            excuteAutoWired();
        }
    }

    /**
     * 扫描包
     */
    public void excuteScanPackage(String pack) {
        URL resource = this.getClass().getClassLoader().getResource("/" + pack.replaceAll("\\.", "/"));
        String url = resource.getFile();
        File file = new File(url);
        for (File listFile : file.listFiles()) {
            if (listFile.isDirectory()) {
                //文件目录
                //com.bruce.service.impl
                excuteScanPackage(pack + "." + listFile.getName());
            } else {
                //com.bruce.service.impl.userServiceImpl com.bruce.service.userService
                String className = pack + "." + listFile.getName().replaceAll(".class", "");
                classNameList.add(className);
            }
        }
    }

    /**
     * 实例化Spring容器的bean对象
     */
    public void excuteInstance() {
        if (classNameList.size() == 0) {
            //没有需要实例化的类
            throw new ContextException("找不到需要实例化的Class");
        }
        try {
            for (String className : classNameList) {
                Class<?> claszz = Class.forName(className);
                if (claszz.isAnnotationPresent(Controller.class)) {
                    //控制层的类UserController
                    String beanName = claszz.getSimpleName().substring(0, 1).toLowerCase() + claszz.getSimpleName().substring(1);
                    iocMap.put(beanName, claszz.newInstance());
                } else if (claszz.isAnnotationPresent(Service.class)) {
                    //业务逻辑层Impl
                    Service serviceAnnotation = claszz.getAnnotation(Service.class);
                    String beanName = serviceAnnotation.value();
                    if ("".equals(beanName)) {
                        Class<?>[] interfaces = claszz.getInterfaces();
                        for (Class<?> c1 : interfaces) {
                            String serviceName = c1.getSimpleName().substring(0, 1).toLowerCase() + c1.getSimpleName().substring(1);
                            iocMap.put(serviceName, claszz.newInstance());
                        }
                    } else {
                        iocMap.put(beanName, claszz.newInstance());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 实现Spring容器中的对象的依赖注入
     */
    public void excuteAutoWired() {
        try {
            if (iocMap.isEmpty()) {
                throw new ContextException("没有找到初始化的对象！");
            }
            for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
                String key = entry.getKey();
                Object bean = entry.getValue();
                Field[] declaredFields = bean.getClass().getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    if (declaredField.isAnnotationPresent(AutoWired.class)) {
                        AutoWired autoWiredAnnotation = declaredField.getAnnotation(AutoWired.class);
                        String beanName = autoWiredAnnotation.value();
                        if ("".equals(beanName)) {
                            Class<?> type = declaredField.getType();
                            beanName = type.getSimpleName().substring(0, 1).toLowerCase() + type.getSimpleName().substring(1);
                            System.out.println(beanName + "ahahah");
                            System.out.println("===================");
                        }
//                        Field userService = bean.getClass().getDeclaredField("userService");
//                        userService.setAccessible(true);
//                        userService.set(bean,iocMap.get("userService"));
                        declaredField.setAccessible(true);
                        //属性注入
                        System.out.println("容器完成");
                        //iocMap.get("userService");
                        System.out.println("-----------------");
                        declaredField.set(bean, iocMap.get(beanName));
                        System.out.println("dongxi__>" + bean + ":" + beanName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
