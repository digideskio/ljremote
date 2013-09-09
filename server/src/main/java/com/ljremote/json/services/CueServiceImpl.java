package com.ljremote.json.services;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ljremote.json.model.Cue;
import com.ljremote.server.driver.LJDriver;

public class CueServiceImpl extends AbstractLJService implements CueService {
	
	private final static Log log= LogFactory.getLog(CueServiceImpl.class);

	public CueServiceImpl(LJDriver driver) {
		super(driver);
	}

	public Cue getCurrentCue() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getCurrentCueId() {
		int id = driver.cues().getCurrentCue();
		log.debug("Current Cue :" + id);
		return id;
	}

	public Boolean LoadCue(int id) {
		return driver.cues().loadCue(id);
	}

	public List<Cue> getCueList() {
		log.debug("Sending all cues");
		return driver.cues().getCuesList();
	}

	public Boolean trigSequence(int pos, TrigMode mode) {
		return false;
	}

	public Boolean toggleSequence(int pos, boolean enable) {
		// TODO Auto-generated method stub
		return false;
	}

	public Boolean clearSequence(int pos) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Log getLog() {
		return log;
	}

}
