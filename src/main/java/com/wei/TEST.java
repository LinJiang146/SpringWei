package com.wei;

import com.SpringWei.WeiApplicationContext;
import com.wei.service.UserService;
import com.wei.service.UserServiceImpl;

import java.util.concurrent.locks.ReentrantLock;

public class TEST {
    public static void main(String[] args) {
        WeiApplicationContext weiApplicationContext = new WeiApplicationContext(AppConfig.class);
        UserService userService = (UserService)weiApplicationContext.getBean("userService");

//        System.out.println(userService);

//        userService.test();



    }
}
