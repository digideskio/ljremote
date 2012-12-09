package com.ljremote.json.exceptions;

import java.lang.reflect.UndeclaredThrowableException;

public class ExceptionsUtils{

	@SuppressWarnings("unchecked")
	public static <T> T tryGettingCorrectThrown(Throwable t,Class<T> type){
		if (t.getClass() == type) {
			return (T) t;
		}
		if (t instanceof UndeclaredThrowableException) {
			Throwable cause= ((UndeclaredThrowableException) t).getUndeclaredThrowable().getCause();
			if( cause.getClass() == type){
				return (T) cause;
			}
		}
		return null;
	}
}
