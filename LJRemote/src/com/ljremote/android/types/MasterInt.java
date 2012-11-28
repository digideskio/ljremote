package com.ljremote.android.types;

import java.util.ArrayList;
import java.util.List;

public class MasterInt {

	private int intensity;

	public MasterInt(int i) {
		this.intensity= i;
	}

	public int getIntensity() {
		return intensity;
	}
	
	public static List<MasterInt> buildMasters(int number){
		List<MasterInt> masters= new ArrayList<MasterInt>(number);
		for(int i=0; i< number;i++){
			masters.add(new MasterInt(0));
		}
		return masters;
	}
	@Override
	public String toString() {
		return String.format("Static[%d]", intensity);
	}
}
