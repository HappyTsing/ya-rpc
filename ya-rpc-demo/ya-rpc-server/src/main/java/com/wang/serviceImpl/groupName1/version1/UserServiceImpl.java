package com.wang.serviceImpl.groupName1.version1;


import com.wang.pojo.User;
import com.wang.service.UserService;

/**
 * @author happytsing
 * @group groupName1
 * @version version1
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
