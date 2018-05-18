package com.github.diamond.client.event.listener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;

import com.github.diamond.client.event.ConfigurationEvent;
import com.github.diamond.client.event.ConfigurationListener;

/**
 * @descrition : 自动更新通知属性变化
 * @author gaofeng
 * @time 2018/05/18
 */
public class ConfigurationChangedListener extends ApplicationObjectSupport implements ConfigurationListener {

	private static final Map<String, List<String>> classInfo = new ConcurrentHashMap<String, List<String>>();
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationChangedListener.class);
	private static final boolean useClassCache = false;
	private static boolean beanScaned = false;

	private boolean inited = false;

	public ConfigurationChangedListener() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				logger.info("开启配置变更事件处理。");
				ApplicationContext context = getApplicationContext();
				while (context == null) {
					try {
						Thread.sleep(300);
					} catch (Exception e) {
					}
					context = getApplicationContext();
				}
				inited = true;
				logger.info("配置变更事件初始化完成。");
			}
		}).start();

	}

	@Override
	public void configurationChanged(ConfigurationEvent event) {

		String propName = event.getPropertyName();
		Object propValue = event.getPropertyValue();
		ApplicationContext ctx = getApplicationContext();
		logger.info("收到配置变更事件：" + propName + "->" + propValue);
		if (inited && ctx != null) {
			if (ctx instanceof AbstractRefreshableApplicationContext) {
				ConfigurableListableBeanFactory factory = ((AbstractRefreshableApplicationContext) ctx)
						.getBeanFactory();
				if (useClassCache) {
					if (!beanScaned) {
						synchronized (this) {
							if (!beanScaned) {
								String[] names = factory.getBeanDefinitionNames();
								for (String name : names) {
									if (isVailedBeanName(name)) {
										processBean(name, ctx, propName, propValue);
									}
								}
								beanScaned = true;
							}
						}
					}
					// 从缓存里检查类
					Set<String> cachedBean = classInfo.keySet();
					for (String name : cachedBean) {
						if (isVailedBeanName(name)) {
							try {
								processBean(name, ctx, propName, propValue);
							} catch (BeansException e) {
								logger.warn("无法访问bean:" + name, e);
							} catch (Exception e) {
								logger.warn("设置bean属性出错:" + name, e);
							}
						}
					}
				} else {
					String[] names = factory.getBeanDefinitionNames();
					for (String name : names) {
						if (isVailedBeanName(name)) {
							try {
								processBean(name, ctx, propName, propValue);
							} catch (BeansException e) {
								logger.warn("无法访问bean:" + name, e);
							} catch (Exception e) {
								logger.warn("设置bean属性出错:" + name, e);
							}
						}

					}
				}
			} else {
				logger.warn("配置变更不支持的AppContext类型：" + ctx.getClass().getName());

			}
		} else {
			logger.info("配置变更监听初始化未完成。无法处理变动");
		}
	}

	private boolean isVailedBeanName(String name) {
		if (name == null || name.trim().length() < 1) {
			return false;
		} else {
			String str = name.toLowerCase();
			return !name.startsWith("org.") && !str.contains("proxy") && !str.contains("factory");
		}
	}

	private void processBean(String name, ApplicationContext ctx, String propName, Object propValue) {
		Object bean = ctx.getBean(name);
		if (bean.getClass().getAnnotation(AutoUpdateConfigBean.class) != null) {
			if (useClassCache) {
				List<String> fieldNames = classInfo.get(name);
				if (fieldNames == null) {
					fieldNames = new ArrayList<String>();
					classInfo.put(name, new ArrayList<String>());
				}
			}
			if (propName != null && propValue != null) {
				Field[] fields = bean.getClass().getDeclaredFields();
				for (Field field : fields) {
					AutoUpdateConfigField anno = field.getAnnotation(AutoUpdateConfigField.class);
					if (anno != null) {
						if (propName.equals(anno.propName())) {
							logger.debug("发现bean:" + name + " " + bean.getClass().getName());
							field.setAccessible(true);
							String type = field.getType().toString();
							try {
								if (type.endsWith("String")) {
									field.set(bean, propValue);
								} else if (type.endsWith("int") || type.endsWith("Integer")) {
									field.set(bean, Integer.parseInt((String) propValue));
								} else if (type.endsWith("boolean") || type.endsWith("Boolean")) {
									field.set(bean, Boolean.parseBoolean((String) propValue));
								} else {

								}
							} catch (Exception e) {
								logger.error("配置变更设置bean[" + name + "]属性发生异常", e);
							}
						}
					}
				}
			}
		}
	}

}
