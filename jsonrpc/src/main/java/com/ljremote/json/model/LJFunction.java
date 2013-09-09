package com.ljremote.json.model;

import java.util.ArrayList;
import java.util.List;

public class LJFunction {
	
	private int id;
	private String name;
	private int parent_id;
	private List<LJFunction> functions;
	private boolean group;

	public LJFunction() {
	}

	public LJFunction(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getParent_id() {
		return parent_id;
	}

	public void setParent_id(int parent_id) {
		this.parent_id = parent_id;
	}
	
	@Override
	public String toString() {
		return "LJFunction [id=" + id + ", name=" + name + "]";
	}
	
	public boolean isGroup(){
		return group || functions != null && functions.size() > 0;
	}
	
	public void hasGroup(boolean hasGroup) {
		this.group = hasGroup;
	}
	
	public List<LJFunction> getFunctions() {
		return functions;
	}

	public boolean addFunction(LJFunction function) {
		if ( functions == null ) {
			functions = new ArrayList<LJFunction>();
		}
		return functions.add(function);
	}
}
