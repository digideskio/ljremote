package com.ljremote.json.model;

public class Seq {

	private int id;
	private String label;
	
	public Seq() {
		super();
	}

	public Seq(int id, String label) {
		super();
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
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
