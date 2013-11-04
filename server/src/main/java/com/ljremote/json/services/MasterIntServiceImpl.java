package com.ljremote.json.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ljremote.server.driver.LJDriver;

public class MasterIntServiceImpl extends AbstractLJService implements
		MasterIntService {

	private final static Log log= LogFactory.getLog(MasterIntServiceImpl.class);
	
	public MasterIntServiceImpl(LJDriver driver) {
		super(driver);
	}

	@Override
	public Log getLog() {
		return log;
	}

	public boolean setIntensity(int id, int intensity) {
		return driver.masterInt().setIntensity(id, intensity);
	}

}
