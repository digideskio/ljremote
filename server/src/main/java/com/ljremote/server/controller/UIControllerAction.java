package com.ljremote.server.controller;

import java.util.HashMap;
import java.util.Map;


/**
 * Class defining actions for {@link UIController} to process.
 * 
 * An {@link UIControllerAction} follow the structure :
 * <br>	&lt;action_name&gt; {options} {parameters}
 * <br>	where :
 * <br>		options = { -OPT&lt;name&gt; }*
 * <br>		parameters = { -ARG&lt;name&gt;=&lt;value&gt; }*
 * @author Yanis Lisima
 *
 */
public class UIControllerAction {
	public class ParameterNames{

		public static final String CHOICE = "choice";
		public static final String NAME = "name";
		public static final String PATH = "path";
		public static final String VALUE = "value";
		public static final String INPUT = "input";
		public static final String OUTPUT = "output";
		
	}
	public static final String ARG_PREFIX = "-ARG";
	public static final String OPT_PREFIX = "-OPT";
	public static final String PARAM_SEPARATOR = "=";

	
	public static final String CONFIG = "config";
	public static final String NOT_YET_IMPLEMENTED = "not-yet-implented";
	public static final String QUIT = "quit";
	public static final String UI= "gui";
	public static final String UI_REFRESH= UI+"-refresh";
	
	public static final String SERVER = "server";
	public static final String SERVER_START = SERVER + "_start";
	public static final String SERVER_STOP = SERVER + "_stop";
	public static final String SERVER_RESTART = SERVER + "_restart";
	
	
	public static final String USER32 = "user32";
	public static final String USER32_WND = USER32 + "_wnd";
	public static final String USER32_WND_PRINTALL = USER32_WND + "_printall";

	public static String addParameter(final String cmd, final String name,
			final String value) {
		return new StringBuilder(cmd).append(ARG_PREFIX).append(name)
				.append(PARAM_SEPARATOR).append(value).toString();
	}
	
	public static String getParameter(final String cmd, final String name){
		return getParameters(cmd).get(name);
	}
	
	public static Map<String,String> getParameters(final String cmd){
		Map<String, String> map = new HashMap<String, String>();
		int start= cmd.indexOf(ARG_PREFIX);
		String[] param= new String[2];
		if(start > 0){
			String[] params= cmd.substring(start).split(ARG_PREFIX);
			for(int i=0;i<params.length;i++){
				if(params[i].contains(PARAM_SEPARATOR)){
					param= params[i].split(PARAM_SEPARATOR,2);
					map.put(param[0], param[1]);
				}
			}
		}
		return map;
	}
	
}
