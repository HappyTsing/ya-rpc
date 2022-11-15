package com.wang.serviceImpl;


import com.wang.pojo.User;
import com.wang.service.UserService;

/**
 * @author happytsing
 */
public class UserServiceImpl implements UserService {

    @Override
    public User getUserById(int id) {
        return new User(id,"happytsing");
    }

    @Override
    public User getUserByName(String name) {
        return new User(18160207,name);
    }
}
