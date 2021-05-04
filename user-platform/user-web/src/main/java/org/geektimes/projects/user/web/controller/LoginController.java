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
@MyComponent(name = "LoginController")
public class LoginController implements PageController {

    @MyAutowired(name = "UserService")
    private UserService userService;

    @Override
    @POST
    @Path("/user")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        return String.valueOf(userService.deregister(null));
//        if (true) {
//            return "success.jsp";
//        }
//        return "failed.jsp";
    }
}
