package com.ljremote.json.services;

import java.util.List;

import com.ljremote.json.model.CueList;

public interface CueListService {

	public List<CueList> getCueListList();
	
	public int getCurrentCueListId();
	
	public Boolean loadCueList(int id);
	
	public Boolean controlGo();
	
	public Boolean controlBack();
}
