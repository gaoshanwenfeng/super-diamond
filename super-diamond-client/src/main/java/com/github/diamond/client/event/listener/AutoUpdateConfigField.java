package com.github.diamond.client.event.listener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @descrition : 设置在简单属性上，目前支持string和int类型; 包含此注解的类必须同时设置 @AutoUpdateConfigBean 
 * @author gaofeng
 * @time  2018/05/18
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoUpdateConfigField {

	String propName() default "";
}
