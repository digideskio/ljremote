package com.ljremote.json.services;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ljremote.json.model.Seq;
import com.ljremote.server.driver.LJDriver;

public class SequenceServiceImpl extends AbstractLJService implements
		SequenceService {

	private final static Log log= LogFactory.getLog(SequenceServiceImpl.class);
	
	public SequenceServiceImpl(LJDriver driver) {
		super(driver);
	}

	public List<Seq> getSequenceList() {
		return driver.sequences().getSequencesList();
	}

	@Override
	public Log getLog() {
		return log;
	}

	public int getCurrentSequenceId() {
		return driver.sequences().getCurrentSeq();
	}

}
