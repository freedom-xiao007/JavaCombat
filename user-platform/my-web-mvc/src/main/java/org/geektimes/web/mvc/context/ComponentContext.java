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

package org.geektimes.web.mvc.context;

import org.geektimes.web.mvc.dbProxy.JdkRepositoryProxy;
import org.geektimes.web.mvc.function.ThrowableAction;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.naming.*;
import javax.servlet.ServletContext;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author lw1243925457
 */
public class ComponentContext {

    private static final String CONTEXT_NAME = ComponentContext.class.getName();

    private static final String COMPONENT_ENV_CONTEXT_NAME = "java:comp/env";

    private static ServletContext servletContext;

    private ClassLoader classLoader;

    private Context envContext;

    private Map<String, Object> componentsMap = new LinkedHashMap<>();

    /**
     * 获取 ComponentContext
     *
     * @return
     */
    public static ComponentContext getInstance() {
        return (ComponentContext) servletContext.getAttribute(CONTEXT_NAME);
    }

    public void init(ServletContext servletContext) {
        ComponentContext.servletContext = servletContext;
        servletContext.setAttribute(CONTEXT_NAME, this);
        // 获取当前 ServletContext（WebApp）ClassLoader
        this.classLoader = servletContext.getClassLoader();
        initEnvContext();
        try {
            instantiateComponents();
        } catch (ClassNotFoundException | NamingException e) {
            e.printStackTrace();
        }
        initializeComponents();
    }

    private void initEnvContext() {
        if (this.envContext != null) {
            return;
        }
        Context context = null;
        try {
            context = new InitialContext();
            this.envContext = (Context) context.lookup(COMPONENT_ENV_CONTEXT_NAME);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            close(context);
        }
    }

    private static void close(Context context) {
        if (context != null) {
            ThrowableAction.execute(context::close);
        }
    }

    private void instantiateComponents() throws ClassNotFoundException, NamingException {
        // 遍历获取所有的组件名称
        List<String> componentNames = listAllComponentNames("/");
        // 通过依赖查找，实例化对象（ Tomcat BeanFactory setter 方法的执行，仅支持简单类型）
        componentNames.forEach(name -> componentsMap.put(name, lookupComponent(name)));
    }

    private Object lookupComponent(String name) {
        try {
            Object o = envContext.lookup(name);
            if (name.contains("Repository")) {
                return JdkRepositoryProxy.create(o.getClass());
            }
            return o;
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<String> listAllComponentNames(String name) throws NamingException, ClassNotFoundException {
        NamingEnumeration<NameClassPair> e = envContext.list(name);

        // 目录 - Context
        // 节点 -
        // 当前 JNDI 名称下没有子节点
        if (e == null) {
            return Collections.emptyList();
        }

        List<String> fullNames = new ArrayList<>();
        while (e.hasMoreElements()) {
            NameClassPair element = e.nextElement();
            String className = element.getClassName();
            Class<?> targetClass = classLoader.loadClass(className);
            if (Context.class.isAssignableFrom(targetClass)) {
                // 如果当前名称是目录（Context 实现类）的话，递归查找
                fullNames.addAll(listAllComponentNames(element.getName()));
            } else {
                // 否则，当前名称绑定目标类型的话话，添加该名称到集合中
                String fullName = name.startsWith("/") ?
                        element.getName() : name + "/" + element.getName();
                fullNames.add(fullName);
            }
        }
        return fullNames;
    }

    private void initializeComponents() {
        componentsMap.values().forEach(component -> {
            Class<?> componentClass = component.getClass();
            // 注入阶段 - {@link Resource}
            injectComponents(component, componentClass);
            // 初始阶段 - {@link PostConstruct}
            processPostConstruct(component, componentClass);
            // TODO 实现销毁阶段 - {@link PreDestroy}
            processPreDestroy();
        });
    }

    private void injectComponents(Object component, Class<?> componentClass) {
        Stream.of(componentClass.getDeclaredFields())
                .filter(field -> {
                    int mods = field.getModifiers();
                    return !Modifier.isStatic(mods) &&
                            field.isAnnotationPresent(Resource.class);
                }).forEach(field -> {
            Resource resource = field.getAnnotation(Resource.class);
            String resourceName = resource.name();
            Object injectedObject = lookupComponent(resourceName);
            field.setAccessible(true);
            try {
                // 注入目标对象
                field.set(component, injectedObject);
            } catch (IllegalAccessException e) {
            }
        });
    }

    private void processPostConstruct(Object component, Class<?> componentClass) {
        Stream.of(componentClass.getMethods())
                .filter(method ->
                        // 非 static
                        !Modifier.isStatic(method.getModifiers()) &&
                                // 没有参数
                                method.getParameterCount() == 0 &&
                                // 标注 @PostConstruct
                                method.isAnnotationPresent(PostConstruct.class)
                ).forEach(method -> {
            try {
                // 执行目标方法
                method.invoke(component);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void processPreDestroy() {

    }
}
