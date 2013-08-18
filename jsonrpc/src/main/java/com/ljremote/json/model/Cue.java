package com.ljremote.json.model;

import java.util.ArrayList;
import java.util.List;

public class Cue {

	private int id;
	private String label;
	private List<Integer> sequences;
	public final static int NB_SEQ = 12;
	
	public Cue() {
		sequences = new ArrayList<Integer>(NB_SEQ);
		clearAllSequences();
	}
	
	public Cue(int id, String label) {
		this();
		this.id = id;
		this.label = label;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<Integer> getSequences() {
		return sequences;
	}

	public void setSequence(int pos, Seq seq) {
		sequences.set(pos, seq.getId());
	}
	
	public void getSequence(int pos){
		sequences.get(pos);
	}
	
	public void clearSequence(int pos){
//		sequences.set(pos, -1);
	}
	
	public void clearAllSequences(){
		for(int i =0; i < NB_SEQ; i++){
			clearSequence(i);
		}
	}
}
