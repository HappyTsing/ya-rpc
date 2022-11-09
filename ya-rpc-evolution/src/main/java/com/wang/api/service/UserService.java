package com.wang.api.service;

import com.wang.api.pojo.User;

/**
 * @author happytsing
 */
public interface UserService {

    /**
     * 根据用户 id 获取用户信息，业务中通过查询数据库获取
     *
     * @param id 用户 id
     * @return 用户信息，如果获取不到，返回 null
     */
    User getUserById(int id);


    /**
     * 根据用户 name 获取用户信息，业务中通过查询数据库获取
     *
     * @param name 用户 name
     * @return 用户信息，如果获取不到，返回 null
     */
    User getUserByName(String name);
}