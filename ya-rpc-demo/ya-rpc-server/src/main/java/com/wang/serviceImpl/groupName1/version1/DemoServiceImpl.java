package com.wang.serviceImpl.groupName1.version1;

import com.wang.service.DemoService;


/**
 * @author happytsing
 * @group groupName1
 * @version version1
 */
public class DemoServiceImpl implements DemoService {
    @Override
    public String getGroupAndVersion() {
        String group = "groupName1";
        String version = "version1";
        return "group: " + group + "version" + group;

    }
}