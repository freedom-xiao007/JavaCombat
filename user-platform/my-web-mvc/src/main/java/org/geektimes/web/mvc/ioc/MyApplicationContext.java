/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.geektimes.web.mvc.ioc;

import org.apache.catalina.LifecycleException;
import org.geektimes.web.mvc.dbProxy.JdkRepositoryProxy;
import org.geektimes.web.mvc.ioc.annotation.MyAutowired;
import org.geektimes.web.mvc.ioc.annotation.MyComponent;
import org.geektimes.web.mvc.ioc.annotation.MyRepository;
import org.geektimes.web.mvc.log.MyLogger;
import org.geektimes.web.mvc.run.RunTomcat;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

/**
 * @author lw1243925457
 */
public class MyApplicationContext {

    private MyLogger log = new MyLogger(MyApplicationContext.class.getName());

    private final ClassLoader classLoader;

    private Set<String> beans = new HashSet<>();

    private Map<String, Object> beanMap = new HashMap<>();

    public MyApplicationContext(Class<?> clazz) {
        this.classLoader = clazz.getClassLoader();

        try {
            loadBeans(clazz.getPackage().getName());
            registerBeans();
            injection();
            log.info("init bean end");
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        log.info("Run tomcat");
        try {
            new RunTomcat().run();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }

    private void injection() throws IllegalAccessException {
        for (Object object: beanMap.values()) {
            doInjection(object);
        }
    }

    private void doInjection(Object object) throws IllegalAccessException {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field: fields) {
            MyAutowired autowired = field.getAnnotation(MyAutowired.class);
            if (autowired == null) {
                continue;
            }

            String beanName = autowired.name();
            if (!beanMap.containsKey(beanName)) {
                continue;
            }

            field.setAccessible(true);
            field.set(object, beanMap.get(beanName));
            log.info(object.getClass().getName() + " Autowired : " + beanName);

            doInjection(beanMap.get(beanName));
        }
    }

    private void registerBeans() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        for (String classBean: beans) {
            log.info("register: " + classBean);
            Class<?> clazz = classLoader.loadClass(classBean);
            if (clazz.getAnnotation(MyComponent.class) != null) {
                beanMap.put(clazz.getAnnotation(MyComponent.class).name(), clazz.newInstance());
                log.info("register bean of component: " + clazz.getName());
            } else if (clazz.getAnnotation(MyRepository.class) != null) {
                beanMap.put(clazz.getAnnotation(MyRepository.class).name(), JdkRepositoryProxy.create(clazz));
                log.info("register bean of repository: " + clazz.getName());
            }
        }
    }

    private void loadBeans(String packageName) {
        String filePath = packageName.replace(".", "/");
        URL url = this.getClass().getClassLoader().getResource(filePath);
        assert url != null;
        File root = new File(url.getFile());
        for (File file: Objects.requireNonNull(root.listFiles())) {
            if (file.isDirectory()) {
                loadBeans(packageName + "." + file.getName());
            } else {
                beans.add(packageName + "." + file.getName().replace(".class", ""));
            }
        }
    }

    public Object getBean(String name) {
        return beanMap.get(name);
    }

}
