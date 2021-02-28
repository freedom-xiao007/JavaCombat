# 基于Servlet的MVC框架Dome（一）：初步搭建运行
***
## 简介
本系列将展示基于Java的Servlet，逐步搭建一个类似SpringMVC的Web框架

刚开始，我们首先跑起一个简单的hello程序

工程GitHub地址：[https://github.com/lw1243925457/JavaCombat](https://github.com/lw1243925457/JavaCombat)

## 相关知识
编写还是需要一些前置知识的，推荐的阅读如下：

- 《Servlet、JSP和Spring MVC初学指南》：详细展示了相关的基础概念，并从中能得到演化轨迹，利于我们的程序的编写

下面是自己学习过程写的一些记录，可能会有些帮助：

- [Servlet/JSP基础]()
- [如果Debug一个Tomcat程序]()

## 程序编写
### 工程目录结构
工程目录结构如下：web-mvc-core主要放置我们的框架代码，example相当于业务代码部分

```text
F:.
├─doc 文档及作业说明存放位置
└─project
    ├─example 自研MVC框架使用示例
    └─web-mvc-core 自研MVC框架核心代码
```

### Maven配置
#### project
总工程中配置相关的子工程及可能用到的依赖的版本信息，内容大致如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>project</groupId>
    <artifactId>project</artifactId>
    <packaging>pom</packaging>
    <version>1.0-SNAPSHOT</version>

    <modules>
        <module>web-mvc-core</module>
        <module>example</module>
    </modules>

    <properties>
        <!-- Artifacts 依赖版本 -->
        <javax.servlet-api.version>3.1.0</javax.servlet-api.version>
        <javax.ws.rs-api.version>2.0.1</javax.ws.rs-api.version>
    </properties>
</project>
```

#### web-mvc-core
配置Servlet相关依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>project</artifactId>
        <groupId>project</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>web-mvc-core</artifactId>

    <dependencies>
        <!-- Servlet API -->
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>${javax.servlet-api.version}</version>
        </dependency>

        <!-- 引入Maven的Tomcat -->
        <dependency>
            <groupId>org.apache.tomcat.maven</groupId>
            <artifactId>tomcat7-maven-plugin</artifactId>
            <version>${tomcat.version}</version>
        </dependency>
    </dependencies>

</project>
```

#### example
tomcat相关依赖和自己的MVC框架依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>project</artifactId>
        <groupId>project</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>example</artifactId>

    <dependencies>
        <dependency>
            <groupId>project</groupId>
            <artifactId>web-mvc-core</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>

</project>
```

### web-mvc-core 
#### hello类编写
写一个简单的hell类，返回hello信息给前端

```java
package com.my.mvc.web.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class FrontControllerServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String servletName = getServletConfig().getServletName();
        PrintWriter writer = response.getWriter();
        writer.print("<html><head></head>"
                        + "<body>Get Hello from " + servletName
                        + "</body></html>");
    }
}
```

#### 启动类编写
写一个简单的tomcat启动类，目前比较糙，后面再优化

```java
package com.my.mvc.web.run;

import com.my.mvc.web.servlet.FrontControllerServlet;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;

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
```

### example
#### 服务启动编写
类似我们使用SpringMVC时候，一行代码搞定

```java
import com.my.mvc.web.run.CustomBoot;
import org.apache.catalina.LifecycleException;

public class Main {

    public static void main(String[] args) throws LifecycleException {
        new CustomBoot().run();
    }
}
```

## 总结
通过上面的代码，我们初步跑起了一个简单hello的web程序，而且有一点点SpringMVC的味道

由于时间原因，但代码规范方面有些不足

目前存在的问题有，Servlet的需要手工代码注入，后面需要改成自动装载

后序还有JSP、Controllers相关的问题，留待后面探索

## 参考链接
### 自写的学习相关
- [Servlet/JSP基础]()
- [如果Debug一个Tomcat程序]()

### 借鉴
- [菜鸟教程之工具使用——Maven自动部署到Tomcat](https://blog.csdn.net/liushuijinger/article/details/39673093)
- [不用下载tomcat，maven插件直接运行war包，真香](https://juejin.cn/post/6844904099150823432)
- [Tomcat专题(二)-----Tomcat源码、嵌入式Tomcat](https://www.cnblogs.com/alimayun/p/12354704.html)
