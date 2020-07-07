package com.csnt.ins.jwttoken.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 古楼城外折戟沉沙，谁与我共驰骋天涯
 * luoxiaojian
 * FOR : 通过Auth注解来判断权限/角色
 */
@Retention(RetentionPolicy.RUNTIME)
/**运行时注解*/
@Target({ElementType.TYPE, ElementType.METHOD})
/**方法或者类的注解*/
public @interface Auth {
    // 需要的权限 满足一个就可以访问--优先级第三
    String[] hasForces() default {};
    // 满足的角色 满足一个就可以访问--优先级第四
    String[] hasRoles() default {};
    // 需要的权限 满足全部才可以访问--优先级第一
    String[] withForces() default {};
    // 满足的橘色 满足全部才可以访问--优先级第二
    String[] withRoles()  default {};

}
