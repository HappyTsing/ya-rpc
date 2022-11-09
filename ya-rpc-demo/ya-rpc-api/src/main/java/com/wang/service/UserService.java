package com.wang.service;

import com.wang.pojo.User;

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
}