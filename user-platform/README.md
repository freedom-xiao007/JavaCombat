# 课程工程
***
## 运行
```shell script
pom.xml -- add as maven project

cd user-platform

mvn clean package -U
java -jar .\user-web\target\user-web-v1-SNAPSHOT-war-exec.jar

http://localhost:8080/
```

## 工程结构
- my-web-mvc ： 框架代码，核心代码都写在这里
- user-web ： 用户业务部分，也相当于测试

```xml
├─my-web-mvc
│  └─src
│      └─main
│          └─java
│              └─org
│                  └─geektimes
│                      └─web
│                          └─mvc
│                              ├─controller
│                              └─header
│                                  └─annotation
└─user-web
    └─src
        └─main
            ├─java
            │  └─org
            │      └─geektimes
            │          └─projects
            │              └─user
            │                  ├─domain
            │                  ├─repository
            │                  ├─service
            │                  └─web
            │                      ├─controller
            │                      └─filter
            ├─resources
            │  └─META-INF
            │      └─services
            └─webapp
                ├─static
                │  ├─css
                │  └─js
                └─WEB-INF
                    └─jsp
                        ├─coda
                        └─prelude
```