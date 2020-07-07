package com.csnt.ins.jwttoken.service.impl;

import com.alibaba.druid.filter.config.ConfigTools;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.auth.Role;
import com.csnt.ins.bean.auth.UserInfo;
import com.csnt.ins.expection.BizException;
import com.csnt.ins.jwttoken.Bean.IJwtAble;
import com.csnt.ins.jwttoken.service.IJwtUserService;
import com.csnt.ins.model.issuer.PosLoginLog;
import com.csnt.ins.model.issuer.PosUser;
import com.csnt.ins.utils.DbUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author luoxiaojian
 * @Description: 权限校验服务类
 * @Copyright cstc. 交通信息化部
 * @date 2019/6/26.
 */
public class AuthService implements IJwtUserService<UserInfo> {
    public static final AuthService me = new AuthService();
    String serviceName = "[用户服务类]";
    Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final int LOGIN_STATUS_FAIL = 0;
    private final int LOGIN_STATUS_SUCCESS = 1;

    /**
     * 用户登录方法
     *
     * @param userId     用户名
     * @param password   密码
     * @param extendInfo 扩展网点信息
     * @return
     */
    @Override
    public IJwtAble login(String userId, String password, UserInfo extendInfo) {
        try {
            PosUser user = PosUser.dao.findFirst(Db.getSql("mysql.userLogin"), userId);
            if (null == user) {
                logger.error("{}用户不存在", serviceName);
                saveLoginLog(userId, "0", LOGIN_STATUS_FAIL, "用户不存在！", extendInfo);
                throw new BizException("用户不存在！");
            }
            //判断用户是否激活
            if (user.getIsActive() != 1) {
                logger.error("{}用户已被锁定", serviceName);
                saveLoginLog(userId, "0", LOGIN_STATUS_FAIL, "用户已被锁定！", extendInfo);
                throw new BizException("用户已被锁定！");
            }

            //验证密码
            String pwdEncrypt = ConfigTools.encrypt(password);
            if (!user.getUserPwd().equals(pwdEncrypt)) {
                logger.error("{}用户名或密码错误", serviceName);
                saveLoginLog(userId, user.getPosId(), LOGIN_STATUS_FAIL, "用户名或密码错误！", extendInfo);
                throw new BizException("用户名或密码错误！");
            }
            //拷贝属性
            UserInfo userInfo = new UserInfo();
            userInfo.setUserPwd(user.getUserPwd());
            userInfo.setIsActive(user.getIsActive());
            userInfo.setUserName(user.getUserName());
            userInfo.setPosId(user.getPosId());
            userInfo.setPosName(user.getPosName());
            userInfo.setType(user.getType());
            userInfo.setChannelType(user.getChannelType());
            userInfo.setLastModifyPasswordTime(user.getLastModifyPasswordTime());
            //设置用户角色信息
            userInfo.setRoles(listRoles(user.getUserId()));
            saveLoginLog(userId, user.getPosId(), LOGIN_STATUS_SUCCESS, "成功登录", extendInfo);
            return userInfo;
        } catch (BizException e) {
            logger.error("{}用户登录异常:{}", serviceName, e.toString());
            throw new BizException(e.getExceptionMsg());
        } catch (Exception e) {
            logger.error("{}用户登录系统异常:{}", e.toString(), e);
            throw new BizException(e.getMessage());
        }
    }

    @Override
    public List<Role> listRoles(String userId) {
        List<Role> roles = new ArrayList<>();
        List<Record> roleRecords = Db.findByCache(CommonAttribute.CACHE_NAME_HALFHOUR, userId, DbUtil.getSql("queryRolesByOrgCodeAndType"), userId, userId);
        for (Record roleRecord : roleRecords) {
            Role role = new Role();
            role.setRoleCode(roleRecord.getStr("roleCode"));
            role.setRoleName(roleRecord.getStr("roleName"));
            roles.add(role);
        }
        return roles;
    }

    public void saveLoginLog(String userId, String posId, Integer loginStatus, String loginMsg, UserInfo extendInfo) {
        PosLoginLog loginLog = new PosLoginLog();
        loginLog.setUUID(UUID.randomUUID().toString().replace("-", ""));
        loginLog.setUserId(userId);
        loginLog.setPosId(posId);
        loginLog.setLoginStatus(loginStatus);
        loginLog.setLoginTime(new Date());
        loginLog.setLoginMsg(loginMsg);
        loginLog.setHOST(extendInfo.getHost());
        loginLog.setLongitude(extendInfo.getLongitude());
        loginLog.setLatitude(extendInfo.getLatitude());
        loginLog.save();
        logger.info("登录日志记录成功:{}", loginLog);
    }


}

