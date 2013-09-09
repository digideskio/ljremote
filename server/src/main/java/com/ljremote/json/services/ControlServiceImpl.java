package com.ljremote.json.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ljremote.server.driver.LJDriver;

public class ControlServiceImpl extends AbstractLJService implements
		ControlService {

	private static final Log log = LogFactory.getLog(ControlServiceImpl.class);

	public ControlServiceImpl(LJDriver driver) {
		super(driver);
	}

	public boolean setBlackOut() {
		return driver.control().setBlackOut();
	}

	public boolean restore() {
		return driver.control().restore();
	}

	@Override
	public Log getLog() {
		return log;
	}

}
