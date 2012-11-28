package com.ljremote.json.services;

import com.ljremote.server.driver.LJDriver;

public abstract class AbstractLJService {

	protected LJDriver driver;
	
	public AbstractLJService(LJDriver driver) {
		this.driver= driver;
	}
}
