package com.bruce.service;

import com.bruce.pojo.User;

import java.util.List;

public interface UserService {
    List<User> getUsers(String name);
}
