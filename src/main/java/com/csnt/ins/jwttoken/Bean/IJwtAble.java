package com.csnt.ins.jwttoken.Bean;

import com.csnt.ins.bean.auth.Role;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 古楼城外折戟沉沙，谁与我共驰骋天涯
 * luoxiaojian
 * FOR : 普通JavaBean或者是Model变为可以使用插件的JwtUser
 */
public interface IJwtAble extends Serializable {
    /**
     * 获取角色集合
     *
     * @return
     */
    List<Role> getRoles();

    /**
     * 获取权限集合
     *
     * @return
     */
    List<String> getForces();

    /**
     * 上次修改密码时间
     *
     * @return
     */
    Date getLastModifyPasswordTime();

}
