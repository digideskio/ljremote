package com.ljremote.json.model;

import java.util.LinkedList;
import java.util.List;

public class CueList {

	private int id;
	private String Label;
	private List<Cue> cues;

	public CueList() {
		cues = new LinkedList<Cue>();
	}

	public CueList(int id, String label) {
		this();
		setId(id);
		setLabel(label);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLabel() {
		return Label;
	}

	public void setLabel(String label) {
		Label = label;
	}

	public List<Cue> getCues() {
		return cues;
	}

	public Boolean addCue(int pos, Cue cue){
		return cues.add(cue);
	}
	
	public Cue removeCue(int pos){
		return cues.remove(pos);
	}
	
	public Cue setCue(int pos, Cue cue) {
		return cues.set(pos, cue);
	}
}
