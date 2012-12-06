package com.ljremote.json.exceptions;

import com.googlecode.jsonrpc4j.ErrorResolver;
import com.googlecode.jsonrpc4j.ExceptionResolver;

public class LJNotFoundException extends Exception implements JSonRpcResolver{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9222177114700047170L;

	public ErrorResolver getErrorResolver() {
		// TODO Auto-generated method stub
		return null;
	}

	public ExceptionResolver getExceptionResolver() {
		// TODO Auto-generated method stub
		return null;
	}

}
