package com.ljremote.json.services;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ljremote.json.model.BGCue;
import com.ljremote.server.driver.LJDriver;

public class BGCueServiceImpl extends AbstractLJService implements BGCueService {

	private final static Log log= LogFactory.getLog(BGCueServiceImpl.class);
	
	public BGCueServiceImpl(LJDriver driver) {
		super(driver);
	}


	public List<BGCue> getBGCueList() {
		return driver.bgCues().getBGCueList();
	}


	@Override
	public Log getLog() {
		return log;
	}


	public List<Integer> getCurrentBGCue() {
		return null;
	}

}
