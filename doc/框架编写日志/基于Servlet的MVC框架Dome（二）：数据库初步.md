# 基于Servlet的MVC框架Dome（二）：数据库初步
***
## 简介
在前面进行了相关的初始化后，能访问相关的hello页面，本次编写初步的数据库操作部分

工程GitHub地址：[https://github.com/lw1243925457/JavaCombat](https://github.com/lw1243925457/JavaCombat)

## 需求场景
用户访问注册页面，填写相关的信息后，注册到后台数据库中

## 代码编写
### 依赖相关
本地需要使用内存数据H2，添加相关的依赖到模块：my-web-mvc

```xml
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>1.4.194</version>
        </dependency>
```

### 建立相关的JSP页面
JSP相当于前端页面，建立下面三个：

- register.jsp :用户注册页面
- login.jsp ：用户登录页面，用作注册成功后的展示，还没有具体逻辑
- failed.jsp ：失败页面

对应内容如下：

```xml
<%@ page import="java.util.Enumeration" %>
<head>
    <jsp:directive.include file="/WEB-INF/jsp/prelude/include-head-meta.jspf" />
    <title>注册</title>
</head>
<body>
<div class="container">
    <h1>用户注册</h1>
    <form action="/register" method="post">
        <table>
            <tr>
                <td>用户：<input name="user" type="text"></td>
            </tr>
            <tr>
                <td>密码：<input name="password" type="text"></td>
            </tr>
            <tr>
                <td><input name="submit" type="submit" value="提交"></td>
            </tr>
        </table>
    </form>
</div>
</body>
```

```xml
<%@ page import="java.util.Enumeration" %>
<head>
    <jsp:directive.include file="/WEB-INF/jsp/prelude/include-head-meta.jspf" />
    <title>登录</title>
</head>
<body>
<div class="container">
    <h1>用户登录</h1>
    <h6>已注册成功</h6>
    <form action="/register" method="post">
        <table>
            <tr>
                <td>用户：<input name="user" type="text"></td>
            </tr>
            <tr>
                <td>密码：<input name="password" type="text"></td>
            </tr>
            <tr>
                <td><input name="submit" type="submit" value="提交"></td>
            </tr>
        </table>
    </form>
</div>
</body>
```

```xml
<head>
    <jsp:directive.include
            file="/WEB-INF/jsp/prelude/include-head-meta.jspf" />
    <title>My Home Page</title>
</head>
<body>
<div class="container-lg">
    <!-- Content here -->
    操作失败
</div>
</body>
```

建立完后，重启应用是可以通过连接进行访问的，如：localhost:8080/register.jsp

### 初始化数据库源
简单直接的使用下面的方式加载数据库驱动，并建立后相关的用户表，代码大致如下：

```java
import java.sql.*;
import java.util.Properties;

public class H2Database {

    public static H2Database getInstance() {
        return new H2Database();
    }

    private Connection connection;

    private H2Database() {
        try {
            init();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static final String CREATE_USERS_TABLE_DDL_SQL = "create table mvc_user ( " +
            "id integer auto_increment," +
            "name varchar(20)," +
            "password varchar(20)," +
            "email varchar(20)," +
            "phoneNumber varchar(20)," +
            "primary key (`id`));";

    private void init() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        Driver driver = DriverManager.getDriver("jdbc:h2:mem:myDb;DB_CLOSE_DELAY=-1");
        connection = driver.connect("jdbc:h2:mem:myDb;DB_CLOSE_DELAY=-1", new Properties());
        Statement statement = connection.createStatement();
        // 创建 users 表
        System.out.println(statement.execute(CREATE_USERS_TABLE_DDL_SQL));
        statement.close();
    }

    public Connection getConnection() {
        return connection;
    }
}
```

### Controllers编写
新建一个 RegisterController,从请求中读取用户和密码（其他属性后面再完善）

成功跳转到登录页面，失败跳转到失败页面

```java
import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.service.UserService;
import org.geektimes.projects.user.service.impl.UserServiceImpl;
import org.geektimes.web.mvc.controller.PageController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/register")
public class RegisterController implements PageController {

    private UserService userService = new UserServiceImpl();

    @Override
    @POST
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        String user = request.getParameter("user");
        String password = request.getParameter("password");
        System.out.printf("user: %s, password: %s\n", user, password);
        if (user == null || password == null) {
            return "register.jsp";
        }

        if (userService.register(new User(user, password, "1", "1"))) {
            return "login.jsp";
        }
        return "failed.jsp";
    }
}
```

### Service编写
这么模仿Mybatis，Repository使用代码生成，先调用register方法，其他的后面完善

```java
import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.repository.UserRepository;
import org.geektimes.projects.user.service.UserService;
import org.geektimes.web.mvc.dbProxy.JdkRepositoryProxy;

public class UserServiceImpl implements UserService {

    UserRepository userRepository = JdkRepositoryProxy.create(UserRepository.class);

    @Override
    public boolean register(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean deregister(User user) {
        return false;
    }

    @Override
    public boolean update(User user) {
        return false;
    }

    @Override
    public User queryUserById(Long id) {
        return null;
    }

    @Override
    public User queryUserByNameAndPassword(String name, String password) {
        return null;
    }
}
```

### Repository编写
这里直接在接口中使用自己写的注解：Insert、Select

- Insert ：除了列名和值，其余的交给程序自动生成
- Select ：查询语句目前完全由用户填写，同时传入需要返回的类型

用过Mybatis的应该能懂大致意思，注解和Repository如下：

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Insert {

    String value();
}
```

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Select {

    String value();

    String returnType();
}
```

```java
import org.geektimes.web.mvc.sql.Insert;
import org.geektimes.web.mvc.sql.Select;
import org.geektimes.projects.user.domain.User;

import java.util.Collection;

/**
 * 用户存储仓库
 *
 * @since 1.0
 */
public interface UserRepository  {

    @Insert("insert into mvc_user (%s) values (%s)")
    boolean save(User user);

    boolean deleteById(Long userId);

    boolean update(User user);

    User getById(Long userId);

    User getByNameAndPassword(String userName, String password);

    @Select(value = "select * from mvc_user;", returnType = "org.geektimes.projects.user.domain.User")
    Collection<User> getAll();
}
```

### Repository代理编写
这里代码需要缓存起来，毕竟是没有状态的，使用一个实例就够了，第一次获取需要生成，后面直接后台缓存里面的

```java
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JdkRepositoryProxy {

    private static Map<String, Object> repositoryMap = new ConcurrentHashMap<>();

    public static <T> T create(Class clazz) {
        // 查询是否之前生成过，存储的直接返回
        if (!repositoryMap.containsKey(clazz.getName())) {
            repositoryMap.put(clazz.getName(), newProxy(clazz));
        }
        return (T) repositoryMap.get(clazz.getName());
    }

    private static <T> T newProxy(Class clazz) {
        ClassLoader loader = JdkRepositoryProxy.class.getClassLoader();
        Class[] classes = new Class[]{clazz};
        return (T) Proxy.newProxyInstance(loader, classes, new RepositoryInvocationHandler());
    }
}
```

在具体的代理处理中，目前先写了两个简单的插入和查询处理

- 插入处理：假定传入的对象属性和列对应，直接拼接生成相关语句即可
- 查询语句：更加注册传入的需要返回的类路径，可以生成相关的类实例，然后使用反射进行相关的设置

```java
import org.geektimes.web.mvc.sql.Insert;
import org.geektimes.web.mvc.sql.Select;
import org.geektimes.web.mvc.database.H2Database;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lw1243925457
 */
public class RepositoryInvocationHandler implements InvocationHandler {

    H2Database database = H2Database.getInstance();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation: annotations) {
            if (annotation instanceof Insert) {
                try {
                    return executeInsertSql(((Insert) annotation).value(), args);
                } catch (IllegalAccessException | SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            if (annotation instanceof Select) {
                try {
                    return executeQuerySql(((Select) annotation).value(), ((Select) annotation).returnType());
                } catch (SQLException | ClassNotFoundException | IllegalAccessException | IntrospectionException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private Object executeQuerySql(String querySql, String returnType) throws SQLException, ClassNotFoundException, IllegalAccessException, IntrospectionException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        List<Object> result = new ArrayList<>();

        Connection conn = database.getConnection();
        Statement statement = conn.createStatement();

        ResultSet res = statement.executeQuery(querySql);
        // BeanInfo
        BeanInfo userBeanInfo = Introspector.getBeanInfo(Class.forName(returnType), Object.class);

        while (res.next()) {
            Object object = Class.forName(returnType).newInstance();
            for (PropertyDescriptor propertyDescriptor : userBeanInfo.getPropertyDescriptors()) {
                String fieldName = propertyDescriptor.getName();
                Class fieldType = propertyDescriptor.getPropertyType();
                String methodName = typeMethodMappings.get(fieldType);
                // 可能存在映射关系（不过此处是相等的）
                String columnLabel = fieldName;
                Method resultSetMethod = ResultSet.class.getMethod(methodName, String.class);
                // 通过放射调用 getXXX(String) 方法
                Object resultValue = resultSetMethod.invoke(res, columnLabel);
                // 获取 User 类 Setter方法
                // PropertyDescriptor ReadMethod 等于 Getter 方法
                // PropertyDescriptor WriteMethod 等于 Setter 方法
                Method setterMethodFromUser = propertyDescriptor.getWriteMethod();
                // 以 id 为例，  user.setId(resultSet.getLong("id"));
                setterMethodFromUser.invoke(object, resultValue);
            }

            System.out.println(object.toString());
        }

        statement.close();
        return result;
    }

    /**
     * 数据类型与 ResultSet 方法名映射
     */
    static Map<Class, String> typeMethodMappings = new HashMap<>();

    static {
        typeMethodMappings.put(Long.class, "getLong");
        typeMethodMappings.put(String.class, "getString");
    }

    private Object executeInsertSql(String sqlTemplate, Object[] args) throws IllegalAccessException, SQLException {
        System.out.println("execute insert: " + sqlTemplate);
        StringBuilder cols = new StringBuilder();
        StringBuilder values = new StringBuilder();

        Object data = args[0];
        Field[] fields = data.getClass().getDeclaredFields();
        for (Field field: fields) {
            if (field.get(data) == null) {
                continue;
            }
            cols.append(field.getName()).append(",");
            values.append(field.get(data)).append(",");
        }

        String sql = String.format(sqlTemplate, cols.toString(), values.toString());
        System.out.println("sql string: " + sql);

        Connection conn = database.getConnection();
        Statement statement = conn.createStatement();
        statement.execute(sql);

        statement.close();
        return true;
    }
}
```

## 运行测试

```shell script
# 进入user-platform目录
cd user-platform
mvn clean package -U
java -jar .\user-web\target\user-web-v1-SNAPSHOT-war-exec.jar
```

**时间有点紧，目前注册只能使用数字，不支持英文**

按照上面的命令运行相应的程序后，访问用户注册链接：http://localhost:8080/register

成功后成功跳转到用户登录页面

## 参考链接
- [List of In-Memory Databases](https://www.baeldung.com/java-in-memory-databases)
- [H2数据库快速指南](http://www.vue5.com/h2_database/h2_database_quick_guide.html)