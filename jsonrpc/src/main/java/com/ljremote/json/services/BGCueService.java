package com.ljremote.json.services;

import java.util.List;

import com.ljremote.json.model.BGCue;

public interface BGCueService {

	public List<BGCue> getBGCueList();
	
	public List<Integer> getCurrentBGCue();
}
