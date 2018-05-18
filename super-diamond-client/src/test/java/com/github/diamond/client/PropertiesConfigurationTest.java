/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.client;

import org.junit.Assert;
import org.junit.Test;

/**
 * Create on @2013-9-1 @下午9:38:08 
 * @author bsli@ustcinfo.com
 */
public class PropertiesConfigurationTest {
	
	

	public void testInterpolator() throws ConfigurationRuntimeException  {
		String config = "app.home = /tmp/home \r\n";
		config += "zk.home=${app.home}/zk \r\n";
		config += "hbase.home=${app.home}/hbase \r\n";
		
		PropertiesConfiguration configuration = new PropertiesConfiguration();
		configuration.load(config);
		
		Assert.assertEquals("/tmp/home", configuration.getString("app.home"));
		Assert.assertEquals("/tmp/home/zk", configuration.getString("zk.home"));
		Assert.assertEquals("/tmp/home/hbase", configuration.getString("hbase.home"));
	}


	public void testSysProperties() throws ConfigurationRuntimeException  {
		String config = "javaVersion = ${sys:java.version} \r\n";
		
		PropertiesConfiguration configuration = new PropertiesConfiguration();
		configuration.load(config);
		
		Assert.assertEquals(System.getProperty("java.version"), configuration.getString("javaVersion"));
	}
	

	public void testSysEvns() throws ConfigurationRuntimeException  {
		String config = "javaHome = ${env:JAVA_HOME}/lib \r\n";
		
		PropertiesConfiguration configuration = new PropertiesConfiguration();
		configuration.load(config);
		
		Assert.assertEquals(System.getenv("JAVA_HOME") + "/lib", configuration.getString("javaHome"));
	}
}
