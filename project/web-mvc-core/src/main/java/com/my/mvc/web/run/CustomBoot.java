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

package com.my.mvc.web.run;

import com.my.mvc.web.servlet.FrontControllerServlet;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;

/**
 * @author lw1243925457
 */
public class CustomBoot {

    public void run() throws LifecycleException {
        String classPath = System.getProperty("user.dir");
        System.out.println(classPath);

        // new 一个 tomcat，设置相应的ip和端口信息
        Tomcat tomcat = new Tomcat();
        Connector connector = tomcat.getConnector();
        connector.setPort(8080);
        Host host = tomcat.getHost();
        host.setName("localhost");
        host.setAppBase("my-mvc-web");

        // 把启动工程和class加载进去
        Context context = tomcat.addContext(host, "/", classPath);

        if (context instanceof StandardContext) {
            StandardContext standardContext = (StandardContext) context;
            standardContext.setDefaultContextXml("F:\\Code\\Java\\JavaCombat\\project\\example\\src\\main\\webapp\\WEB-INF\\web.xml");
            // 设置Servlet
            Wrapper wrapper = tomcat.addServlet("/", "FrontControllerServlet", new FrontControllerServlet());
            wrapper.addMapping("/");
        }

        // 跑起来并等待
        tomcat.start();
        tomcat.getServer().await();
    }

}
