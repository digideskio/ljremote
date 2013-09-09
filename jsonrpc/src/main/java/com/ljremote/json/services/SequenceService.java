package com.ljremote.json.services;

import java.util.List;

import com.ljremote.json.model.Seq;

public interface SequenceService {

	public List<Seq> getSequenceList();
	
	public int getCurrentSequenceId();
}
