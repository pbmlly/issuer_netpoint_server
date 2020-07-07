package com.csnt.ins.bean.auth;

import java.io.Serializable;

/**
 * @author duwanjiang
 * @Description: 角色
 * @Copyright cstc. 交通信息化部
 * @date 2019/6/26.
 */
public class Role implements Serializable {
    /**
     * 角色编码
     */
    private String roleCode;
    /**
     * 角色名称
     */
    private String roleName;

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }


    @Override
    public String toString() {
        return "Role{" +
                "roleCode='" + roleCode + '\'' +
                ", roleName='" + roleName + '\'' +
                '}';
    }
}
