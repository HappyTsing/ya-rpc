package com.wang.serviceImpl.groupName2.version1;

import com.wang.service.DemoService;

/**
 * @author happytsing
 * @group groupName2
 * @version version1
 */
public class DemoServiceImpl implements DemoService {
    @Override
    public String getGroupAndVersion() {
        String group = "groupName2";
        String version = "version1";
        return "group: " + group + "version" + group;

    }
}