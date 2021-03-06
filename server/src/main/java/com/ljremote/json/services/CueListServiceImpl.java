package com.ljremote.json.services;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ljremote.json.model.CueList;
import com.ljremote.server.driver.LJDriver;

public class CueListServiceImpl extends AbstractLJService implements
		CueListService {

	private final static Log log= LogFactory.getLog(CueListServiceImpl.class);
	
	public CueListServiceImpl(LJDriver driver) {
		super(driver);
	}
	public List<CueList> getCueListList() {
		return driver.cueLists().getCueListsList();
	}
	@Override
	public Log getLog() {
		return log;
	}
	public int getCurrentCueListId() {
		return driver.cueLists().getCurrentCueList();
	}
	public Boolean loadCueList(int id) {
		return driver.cueLists().loadCueList(id);
	}
	public Boolean controlGo() {
		return driver.cueLists().cueListGo();
	}
	public Boolean controlBack() {
		return driver.cueLists().cueListBack();
	}

}
