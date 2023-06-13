package com.wei.service;

import com.SpringWei.BeanPostProcessor;
import com.SpringWei.Component;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


@Component("beanPostProcessor")
public class WeiBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("初始化前操作");
        //全体bean有效
        //针对单个bean
        if (beanName.equals("userService")){
            ((UserServiceImpl)bean).setBeanName("userService");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {

        if (beanName.equals("userService")){
            Object proxyInstance = Proxy.newProxyInstance(WeiBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("代理逻辑");
                    //执行代理逻辑
                    Object invoke = method.invoke(bean, args);//调用原本方法
                    //执行代理逻辑
                    return invoke;
                }
            });
            return proxyInstance;
        }
        System.out.println("初始化后操作");
        return bean;
    }
}
