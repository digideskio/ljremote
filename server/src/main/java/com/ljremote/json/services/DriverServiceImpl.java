package com.ljremote.json.services;

import com.ljremote.server.driver.LJDriver;


public class DriverServiceImpl extends AbstractLJService implements DriverService {

	public DriverServiceImpl(LJDriver driver) {
		super(driver);
	}

	public boolean isLJready() {
		return driver != null && driver.isLJReady();
	}

	public String getLJversion() {
		return driver ==null ? "" : driver.getLJVersion();
	}

}
