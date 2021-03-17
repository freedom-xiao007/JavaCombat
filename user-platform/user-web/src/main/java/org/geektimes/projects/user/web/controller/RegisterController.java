package org.geektimes.projects.user.web.controller;

import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.service.UserService;
import org.geektimes.projects.user.service.impl.UserServiceImpl;
import org.geektimes.web.mvc.controller.PageController;
import org.geektimes.web.mvc.ioc.annotation.MyAutowired;
import org.geektimes.web.mvc.ioc.annotation.MyComponent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * 输出 “Hello,World” Controller
 */
@Path("/register")
@MyComponent(name = "RegisterController")
public class RegisterController implements PageController {

    @MyAutowired(name = "UserService")
    private UserService userService;

    @Override
    @POST
    public String execute(HttpServletRequest request, HttpServletResponse response) {
        String user = request.getParameter("user");
        String password = request.getParameter("password");
        String phoneNumber = request.getParameter("phone");
        System.out.printf("user: %s, password: %s, phone: %s\n", user, password, phoneNumber);
        if (user == null || password == null) {
            return "register.jsp";
        }
        if (userService.register(new User(user, password, "email", phoneNumber))) {
            return "login.jsp";
        }
        return "failed.jsp";
    }
}
