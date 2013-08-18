package com.ljremote.json.services;

import java.util.List;

import com.ljremote.json.model.Cue;

public interface CueService {

	public enum TrigMode {
		DEFAULT, FORWARD, REVERSE, RANDOM, BOUNCE
	}

	public Cue getCurrentCue();
	
	public int getCurrentCueId();

	public Boolean LoadCue(int id);
	
	public List<Cue> getCueList();
	
	public Boolean trigSequence(int pos, TrigMode mode);
	
	public Boolean toggleSequence(int pos, boolean enable);
	
	public Boolean clearSequence(int pos);
}
