package com.ljremote.json.services;

import org.apache.commons.logging.Log;

import com.ljremote.server.driver.LJDriver;

public abstract class AbstractLJService {

	protected LJDriver driver;
	
	public AbstractLJService(LJDriver driver) {
		this.driver= driver;
		getLog().info("Binding service : " + getClass().getCanonicalName());
	}
	
	public abstract Log getLog();
}
