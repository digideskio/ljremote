package com.ljremote.json.model;

public class Static {

	private int id;
	private String label;
	private int intensity;
	private boolean enable;

	public Static () {
	}
	
	public Static (int id, String label) {
		super();
		setId(id);
		setLabel(label);
	}
	
	public Static(int id, String label, int intensity, boolean enabled) {
		super();
		setId(id);
		setLabel(label);
		setIntensity(intensity);
		setEnable(enabled);
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

	public int getIntensity() {
		return intensity;
	}

	public void setIntensity(int intensity) {
		this.intensity = intensity;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	@Override
	public String toString() {
		return String.format("Static[%d,%s,%s,%b]", id,label,intensity, enable);
	}
}
