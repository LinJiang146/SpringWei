package com.wei.service;


import com.SpringWei.*;

@Component("userService")
public class UserServiceImpl implements UserService,
//        BeanNameAware ,
        InitializingBean {

    @Autowired
    private OrderService orderService;


    private String beanName;

    @Override
    public void test(){
        System.out.println(orderService);
        System.out.println(beanName);
    }

//    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("执行初始化");
    }
}
