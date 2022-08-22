package com.bruce.service.impl;

import com.springmvc.annotation.Service;
import com.bruce.pojo.User;
import com.bruce.service.UserService;

import java.util.ArrayList;
import java.util.List;
@Service
public class UserServiceImpl implements UserService {
    @Override
    public List<User> getUsers(String name) {
        //模拟数据

        ArrayList<User> users = new ArrayList<>();
        users.add(new User(1,"alan","123"));
        users.add(new User(2,"sara","123"));
        users.add(new User(3,"joseph","123"));
        return users;
    }
}
