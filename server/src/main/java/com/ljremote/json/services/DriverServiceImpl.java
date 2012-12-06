package com.ljremote.json.services;

import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.server.driver.LJDriver;


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
