package com.ljremote.json.exceptions;

import com.googlecode.jsonrpc4j.ErrorResolver;
import com.googlecode.jsonrpc4j.ExceptionResolver;

public interface JSonRpcResolver {

	public ErrorResolver getErrorResolver();
	
	public ExceptionResolver getExceptionResolver();
}
