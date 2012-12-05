package com.ljremote.client;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.googlecode.jsonrpc4j.JsonRpcClient;
import com.googlecode.jsonrpc4j.ReflectionUtil;

public class Main {

	private final static Log log = LogFactory.getLog(Main.class);

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

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Box box = Box.createVerticalBox();
		JButton button;
		try {
			final LJClient client= new LJClient(new Socket("192.168.0.10",2508));
			button = new JButton("Hello World");
			button.addMouseListener(new MouseAdapter() {
				
				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					new Thread(client).start();
				}
				
			});
			box.add(button);
			
			JFrame frame = new JFrame();
			frame.setContentPane(box);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.pack();
			frame.setVisible(true);
		} catch (UnknownHostException e1) {
			log.error(e1);
		} catch (IOException e1) {
			log.error(e1);
		}

	}

	
}
