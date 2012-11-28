package com.ljremote.android.types;

import java.util.ArrayList;
import java.util.List;

public class Static {
	public static List<Static> buildStatics(int number){
		List<Static> statics= new ArrayList<Static>(number);
		for(int i=0; i< number;i++){
			statics.add(new Static(i, "Static " + i, 100,true));
		}
		return statics;
	}
	private int id;
	private boolean enabled;
	private String label;
	private int intensity;
	public Static(int id, String label, int intensity, boolean enabled) {
		super();
		this.id = id;
		this.label = label;
		this.intensity = intensity;
		this.setEnabled(enabled);
	}
	public int getId() {
		return id;
	}
	public int getIntensity() {
		return intensity;
	}
	public String getLabel() {
		return label;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setIntensity(int intensity) {
		this.intensity = intensity;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	@Override
	public String toString() {
		return String.format("Static[%d,%s,%s]", id,label,intensity);
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	
}
