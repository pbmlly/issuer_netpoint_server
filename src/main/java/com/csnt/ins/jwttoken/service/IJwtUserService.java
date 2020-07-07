package com.csnt.ins.jwttoken.service;


import com.csnt.ins.bean.auth.Role;
import com.csnt.ins.jwttoken.Bean.IJwtAble;

import java.util.List;

/**
 * 古楼城外折戟沉沙，谁与我共驰骋天涯
 * luoxiaojian ${DATE}
 * FOR : 必须实现的接口
 *
 * @author cloud
 */
public interface IJwtUserService<T> {
    /**
     * 登录接口 返回一个 IJwtAble  的数据
     *
     * @param userName
     * @param password
     * @return
     */
    IJwtAble login(String userName, String password, T extendInfo);

    /**
     * 查询用户角色列表
     *
     * @param userId
     * @return
     */
    List<Role> listRoles(String userId);
}
