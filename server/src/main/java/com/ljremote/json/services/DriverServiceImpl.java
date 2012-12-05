package com.ljremote.json.services;

import com.ljremote.server.driver.LJDriver;
import com.ljremote.server.exceptions.LJNotFoundException;


public class DriverServiceImpl extends AbstractLJService implements DriverService {

	public DriverServiceImpl(LJDriver driver) {
		super(driver);
	}

	public boolean isLJready() {
		try {
			return driver != null && driver.isLJReady();
		} catch (LJNotFoundException e) {
			return false;
		}
	}

	public String getLJversion() {
		try {
			return driver ==null ? "" : driver.getLJVersion();
		} catch (LJNotFoundException e) {
			return "0.0.0";
		}
	}

}
