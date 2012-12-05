package com.ljremote.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.googlecode.jsonrpc4j.JsonRpcClient;
import com.googlecode.jsonrpc4j.ReflectionUtil;
import com.ljremote.json.services.ServerService;

public class LJClient implements Runnable {

	private static final Log log = LogFactory.getLog(LJClient.class);
	private Socket socket;
	
	/**
	 * Creates a {@link Proxy} of the given {@link proxyInterface} that uses the
	 * given {@link JsonRpcClient}.
	 * 
	 * @param <T>
	 *            the proxy type
	 * @param classLoader
	 *            the {@link ClassLoader}
	 * @param proxyInterface
	 *            the interface to proxy
	 * @param client
	 *            the {@link JsonRpcClient}
	 * @param socket
	 *            the {@link Socket}
	 * @return the proxied interface
	 */
	public static <T> T createClientProxy(ClassLoader classLoader,
			Class<T> proxyInterface, final JsonRpcClient client, Socket socket)
			throws IOException {

		// create and return the proxy
		return createClientProxy(classLoader, proxyInterface, false, client,
				socket.getInputStream(), socket.getOutputStream());
	}

	/**
	 * Creates a {@link Proxy} of the given {@link proxyInterface} that uses the
	 * given {@link JsonRpcClient}.
	 * 
	 * @param <T>
	 *            the proxy type
	 * @param classLoader
	 *            the {@link ClassLoader}
	 * @param proxyInterface
	 *            the interface to proxy
	 * @param client
	 *            the {@link JsonRpcClient}
	 * @param ips
	 *            the {@link InputStream}
	 * @param ops
	 *            the {@link OutputStream}
	 * @return the proxied interface
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createClientProxy(ClassLoader classLoader,
			Class<T> proxyInterface, final boolean useNamedParams,
			final JsonRpcClient client, final InputStream ips,
			final OutputStream ops) {

		// create and return the proxy
		return (T) Proxy.newProxyInstance(classLoader,
				new Class<?>[] { proxyInterface }, new InvocationHandler() {
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						Object arguments = ReflectionUtil.parseArguments(
								method, args, useNamedParams);
						return client.invokeAndReadResponse(method.getName(),
								arguments, method.getGenericReturnType(), ops,
								ips);
					}
				});
	}

	public LJClient(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		ServerService service;
		try {
			service = createClientProxy(LJClient.class.getClassLoader(),
					ServerService.class, new JsonRpcClient(), socket);
			log.info(service.helloWord());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
