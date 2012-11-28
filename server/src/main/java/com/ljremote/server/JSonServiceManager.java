package com.ljremote.server;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.googlecode.jsonrpc4j.ProxyUtil;

public class JSonServiceManager {

	private List<Object> serviceImpl;
	private List<Class<?>> serviceClass;
	private static final Log log = LogFactory.getLog(JSonServiceManager.class);

	public JSonServiceManager() {
		serviceImpl = new LinkedList<Object>();
		serviceClass = new LinkedList<Class<?>>();
	}

	public boolean registerService(Object impl, Class<?> type) {
		if (!type.isInstance(impl)) {
			throw new IllegalArgumentException(String.format(
					"Object %s cannot be cast to %s", impl.getClass(), type));
		}
		if (serviceImpl.add(impl) || serviceClass.add(type)) {
			log.info(String.format("Service %s registered", type));
			return true;
		}
		return false;
	}

	public Object getCompositeService(ClassLoader classloader) {
		return ProxyUtil.createCompositeServiceProxy(classloader, serviceImpl.toArray(), serviceClass
				.toArray(new Class<?>[0]), true);
	}

	public Object[] getServiceImpl() {
		return serviceImpl.toArray();
	}

	public Class<?>[] getServiceClass() {
		return serviceClass.toArray(new Class<?>[0]);
	}
}
