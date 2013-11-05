package com.ljremote.server.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UIControllerActionBuilder {

	/**
	 * @uml.property  name="base_cmd"
	 */
	private String base_cmd;
	/**
	 * @uml.property  name="options"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
	 */
	private List<String> options;
	/**
	 * @uml.property  name="parameters"
	 * @uml.associationEnd  qualifier="name:java.lang.String java.lang.String"
	 */
	private Map<String, String> parameters;

	public UIControllerActionBuilder(String base_cmd) {
		super();
		this.base_cmd = base_cmd;
		options=new ArrayList<String>();
		parameters = new HashMap<String, String>();
	}

	public UIControllerActionBuilder addParameter(final String name, final String value) {
		parameters.put(name, value);
		return this;
	}
	
	public UIControllerActionBuilder transferParameter(final String cmdSource, final String name){
		String value= UIControllerAction.getParameter(cmdSource, name);
		if(value != null){
			addParameter(name, value);
		}
		return this;
	}
	
	public UIControllerActionBuilder transferAllParameters(final String cmdSource){
		parameters.putAll(UIControllerAction.getParameters(cmdSource));
		return this;
	}
	
	public UIControllerActionBuilder addOption(final String option){
		options.add(option);
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(base_cmd);
		for(String option : options){
			str.append(UIControllerAction.OPT_PREFIX);
			str.append(option);
		}
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			str.append(UIControllerAction.ARG_PREFIX)
					.append(entry.getKey())
					.append(UIControllerAction.PARAM_SEPARATOR)
					.append(entry.getValue());
		}
		return str.toString();
	}

}
