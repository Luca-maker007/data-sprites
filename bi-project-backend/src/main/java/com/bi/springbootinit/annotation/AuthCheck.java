package com.bi.springbootinit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须有某个角色
     * 通过注解处理器在运行时检查调用该方法的用户是否具备特定的角色或权限。
     * 通过反射检查方法上是否有 @AuthCheck 注解，并获取 mustRole 属性的值。然后，根据当前用户的角色进行权限校验。如果用户没有指定的角色，则抛出权限不足的异常。
     * @return
     */
    String mustRole() default ""; //注解属性

}

