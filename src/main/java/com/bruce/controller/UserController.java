package com.bruce.controller;

import com.springmvc.annotation.*;
import com.bruce.pojo.User;
import com.bruce.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
public class UserController {
    //逻辑层对象
    @AutoWired
    UserService userService;

    @RequestMapping("/userList")
    public String List(HttpServletRequest request, HttpServletResponse response, @RequestParm("name") String name, String age) {
        response.setContentType("text/html;charset=utf-8");
        List<User> users = userService.getUsers(name);
        request.setAttribute("User",users);
        //转发到页面
        return "forward:user.jsp";
    }
    @RequestMapping("/user/userList")
    public String List1(HttpServletRequest request, HttpServletResponse response, @RequestParm("name") String name, String age) {
        response.setContentType("text/html;charset=utf-8");
        List<User> users = userService.getUsers(name);
        request.setAttribute("User",users);
        //转发到页面
        return "forward:user/user.jsp";
    }

    @RequestMapping("/user/queryJson")
    @ResponseBody
    public List<User> queryUser(HttpServletRequest request, HttpServletResponse response, @RequestParm("name") String name, String age){
        return userService.getUsers(name);
    }
}
