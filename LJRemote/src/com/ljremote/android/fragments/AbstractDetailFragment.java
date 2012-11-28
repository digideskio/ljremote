package com.ljremote.android.fragments;

import android.support.v4.app.Fragment;

public class AbstractDetailFragment extends Fragment {
	
	protected int str_id;
	
	public AbstractDetailFragment(int str_id) {
		this.str_id= str_id;
	}
	
	public int getStr_id() {
		return str_id;
	}
	public void setStr_id(int str_id) {
		this.str_id = str_id;
	}

	private int pos=-1;
	
	public int getPosition() {
		return pos;
	}
	public void setPosition(int pos) {
		this.pos = pos;
	}
	
}
