package com.SpringWei;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WeiApplicationContext {

    private Class configClass;

    private ConcurrentHashMap<String,Object> singletonObjects = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();
    public WeiApplicationContext(Class configClass)  {
        this.configClass = configClass;

        //解析配置类
        //获取扫描地址,扫描类来生成beanDefinition对象,放入到beanDefinitionMap中
        scan(configClass);

        //依据单例非懒加载的beanDefinition生成bean放入singletonObjects中
        for (String beanName : beanDefinitionMap.keySet()){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if ("singleton".equals(beanDefinition.getScope())){
                Object bean = creatBean(beanName,beanDefinition);
                singletonObjects.put(beanName,bean);
            }
        }

    }

    private Object creatBean(String beanName,BeanDefinition beanDefinition){
        Class clazz = beanDefinition.getClazz();
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();

            //依赖注入
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)){
                    Object bean = getBean(field.getName());
                    field.setAccessible(true);
                    field.set(instance,bean);
                }
            }

            //实现BeanNameAware接口有效  Aware 初始化
            if (instance instanceof BeanNameAware){
                ((BeanNameAware)instance).setBeanName(beanName);
            }

            //全局有效 初始化前
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance,beanName);
            }

            //实现Initializing接口有效 Initializing初始化
            if (instance instanceof InitializingBean){
                ((InitializingBean)instance).afterPropertiesSet();
            }

            //全局有效 初始化后  这里可以生成aop代理对象
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance,beanName);
            }

            return instance;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void scan(Class configClass) {
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.value();
        path = path.replace(".","/");
        //获取路径下的类
        ClassLoader classLoader = WeiApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(path);
        File file = new File(resource.getFile());

        if (file.isDirectory()){
            for (File listFile : file.listFiles()) {
                String fileName = listFile.getAbsolutePath();
                if (fileName.endsWith(".class")){
                    fileName = fileName.substring(fileName.indexOf("com"),fileName.indexOf(".class"));
                    fileName = fileName.replace("\\",".");

                    try {
                        Class<?> clazz = classLoader.loadClass(fileName);
                        if (clazz.isAnnotationPresent(Component.class)) {
                            //当前类是一个bean





                            Component component = clazz.getDeclaredAnnotation(Component.class);
                            String beanName = component.value();
                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setClazz(clazz);

                            if (BeanPostProcessor.class.isAssignableFrom(clazz)){
                                BeanPostProcessor beanPostProcessor = (BeanPostProcessor) clazz.newInstance();
                                beanPostProcessorList.add(beanPostProcessor);
                            }else {
                                if (clazz.isAnnotationPresent(Scope.class)){
                                    Scope scope = clazz.getDeclaredAnnotation(Scope.class);
                                    beanDefinition.setScope(scope.value());
                                }else {
                                    beanDefinition.setScope("singleton");
                                }
                                beanDefinitionMap.put(beanName,beanDefinition);
                            }




                        }
                    }
                    catch (ClassNotFoundException e){
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public Object getBean(String beanName){
        if (beanDefinitionMap.containsKey(beanName)){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if ("singleton".equals(beanDefinition.getScope())){
                return singletonObjects.get(beanName);
            }else {
                Object bean = creatBean(beanName,beanDefinition);
                return bean;
            }
        }else {
            throw new NullPointerException();
        }
    }
}
