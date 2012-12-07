package com.ljremote.json.exceptions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.jsonrpc4j.ErrorResolver;
import com.googlecode.jsonrpc4j.ExceptionResolver;

public class LJNotFoundException extends RuntimeException implements JSonRpcResolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9222177114700047170L;
	private static final int code = 1;

	private final static ErrorResolver errorResolver = new ErrorResolver() {

		public JsonError resolveError(Throwable t, Method method,
				List<JsonNode> arguments) {
			if (t instanceof LJNotFoundException) {
				// LJNotFoundException e = (LJNotFoundException) t;
				return new JsonError(code, LJNotFoundException.class.getName(),
						null);
			} else if (t instanceof UndeclaredThrowableException) {
				if(((UndeclaredThrowableException) t).getUndeclaredThrowable().getCause() instanceof LJNotFoundException){
					return new JsonError(code, LJNotFoundException.class.getName(),
							null);
				}
			}
			return null;
		}
	};
	private static final ExceptionResolver exceptionResolveur = new ExceptionResolver() {
		
		public Throwable resolveException(ObjectNode response) {
			// get the error object
			ObjectNode errorObject = ObjectNode.class.cast(response.get("error"));
			
			if(errorObject.get("code").asInt() == code){
				return new LJNotFoundException();
			}
			return null;
		}
	};

	public ErrorResolver getErrorResolver() {
		return errorResolver;
	}

	public ExceptionResolver getExceptionResolver() {
		return exceptionResolveur;
	}

}
