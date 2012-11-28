package com.ljremote.android.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.googlecode.jsonrpc4j.JsonRpcClient;
import com.googlecode.jsonrpc4j.ReflectionUtil;
import com.ljremote.android.R;
import com.ljremote.json.services.ServerService;

public class JSonTestActivity extends FragmentActivity implements
		OnClickListener {

	static class ProxyUtil{
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
	}
	private TextView jsonDisplay;
	private JsonRpcClient jrpc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jsontest);

		jsonDisplay = (TextView) findViewById(R.id.jsonDisplay);

		((Button) findViewById(R.id.jsonHelloWorld)).setOnClickListener(this);
		((Button) findViewById(R.id.jsonLJReady)).setOnClickListener(this);
		((Button) findViewById(R.id.jsonLJVersion)).setOnClickListener(this);
		jrpc = new JsonRpcClient();
	}

	private void processJSon(final int id) {
		Thread thread = new Thread() {
			public void run() {
				Socket socket = null;
				try {
					socket = new Socket("192.168.0.10", 2508);
					String ret = null;

					// getClassLoader().loadClass(ServerService.class.getName());
					// ServerService serverService =
					// ProxyUtil.createClientProxy(
					// ServerService.class.getClassLoader(),
					// ServerService.class, jrpc, socket);
					// ret = serverService.helloWord();

					switch (id) {
					case R.id.jsonHelloWorld:
						ret = 
						ProxyUtil.createClientProxy(getClassLoader(), ServerService.class, jrpc, socket).helloWord();
//						jrpc.invokeAndReadResponse("helloWord",
//								new Object[] {}, String.class,
//								socket.getOutputStream(),
//								socket.getInputStream());
						break;
					case R.id.jsonLJReady:
						ret = String.valueOf(jrpc.invokeAndReadResponse(
								"isLJready", new Object[] {}, Boolean.class,
								socket.getOutputStream(),
								socket.getInputStream()));
						break;
					case R.id.jsonLJVersion:
						ret = jrpc.invokeAndReadResponse("getLJversion",
								new Object[] {}, String.class,
								socket.getOutputStream(),
								socket.getInputStream());
						break;
					default:
						break;
					}

					if (ret == null) {
						ret = "Failed !";
					}

					final String toShow = ret;

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							jsonDisplay.append("\n");
							jsonDisplay.append(toShow);
						}
					});
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (socket != null) {
						try {
							socket.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		};
		thread.start();
	}

	@Override
	public void onClick(View v) {
		processJSon(v.getId());
	}
}
