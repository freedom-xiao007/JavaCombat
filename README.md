# JavaCombat 实战项目
***
*极客时间 小马哥训练营*

## 工程说明
```text
F:.
├─doc 文档及作业说明存放位置
└─project
    ├─example 自研MVC框架使用示例
    └─web-mvc-core 自研MVC框架核心代码
```

## 运行说明
运行命令如下：

```shell script
# 进入user-platform目录
cd user-platform
mvn clean package -U
java -jar .\user-web\target\user-web-v1-SNAPSHOT-war-exec.jar
```

## 相关学习记录总结
- [Servlet/JSP基础]()
- [如果Debug一个Tomcat程序]()
- [基于Servlet的MVC框架Dome（一）：初步搭建运行]()

## 参考链接
- [菜鸟教程之工具使用——Maven自动部署到Tomcat](https://blog.csdn.net/liushuijinger/article/details/39673093)
- [不用下载tomcat，maven插件直接运行war包，真香](https://juejin.cn/post/6844904099150823432)
- [Tomcat专题(二)-----Tomcat源码、嵌入式Tomcat](https://www.cnblogs.com/alimayun/p/12354704.html)