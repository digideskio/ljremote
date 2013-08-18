package com.ljremote.json.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.server.driver.LJDriver;

public class DriverServiceImpl extends AbstractLJService implements
		DriverService {
	
	private final static Log log= LogFactory.getLog(DriverServiceImpl.class);

	public DriverServiceImpl(LJDriver driver) {
		super(driver);
	}

	public boolean isLJready() {
		return driver != null && driver.isLJReady();
	}

	public String getLJversion() {
		return driver == null ? "" : driver.getLJVersion();
	}

	public void findLJ() throws LJNotFoundException {
		if (!driver.findLJ()) {
			throw new LJNotFoundException();
		}
	}

	@Override
	public Log getLog() {
		return log;
	}

}
