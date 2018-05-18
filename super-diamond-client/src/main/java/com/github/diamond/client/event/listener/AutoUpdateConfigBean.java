package com.github.diamond.client.event.listener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @descrition : 在需要设置属性类上设置此注解(必须)
 * @author gaofeng
 * @time  2018/05/18
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoUpdateConfigBean {

}
