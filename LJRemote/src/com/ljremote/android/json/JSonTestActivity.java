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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.googlecode.jsonrpc4j.JsonRpcClient;
import com.googlecode.jsonrpc4j.ReflectionUtil;
import com.ljremote.android.R;
import com.ljremote.android.json.LJClientService.LocalBinder;
import com.ljremote.android.json.LJClientService.MODE;
import com.ljremote.json.services.ServerService;

public class JSonTestActivity extends FragmentActivity implements
		OnClickListener {

	private TextView jsonDisplay;
	private LJClientService ljService;
	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			serviceBound = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			ljService = ((LocalBinder) service).getService();
			ljService.setOnModeChangeListener(new LJClientService.OnModeChangeListener() {
				
				@Override
				public void onModeChange(MODE newMode) {
					jsonDisplay.append("/n" + newMode);
					if(newMode == MODE.BOUND){
						ljService.drive();
					}
				}
			});
			serviceBound = true;
			try {
				ljService.setHost("192.168.0.10", 2508);
				ljService.connect();
			} catch (UnknownHostException e) {
				jsonDisplay.append("\n" + e);
			}
		}
	};
	private boolean serviceBound;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jsontest);

		jsonDisplay = (TextView) findViewById(R.id.jsonDisplay);

		((Button) findViewById(R.id.jsonHelloWorld)).setOnClickListener(this);
		((Button) findViewById(R.id.jsonLJReady)).setOnClickListener(this);
		((Button) findViewById(R.id.jsonLJVersion)).setOnClickListener(this);
		((Button) findViewById(R.id.jsonException)).setOnClickListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();

		Intent intent = new Intent(this, LJClientService.class);
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (serviceBound) {
			ljService.disconnect();
			unbindService(serviceConnection);
			serviceBound = false;
		}
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
						// ret = ProxyUtil.createClientProxy(getClassLoader(),
						// ServerService.class, jrpc, socket).helloWord();
						// jrpc.invokeAndReadResponse("helloWord",
						// new Object[] {}, String.class,
						// socket.getOutputStream(),
						// socket.getInputStream());
						break;
					case R.id.jsonLJReady:
						// ret = String.valueOf(jrpc.invokeAndReadResponse(
						// "isLJready", new Object[] {}, Boolean.class,
						// socket.getOutputStream(),
						// socket.getInputStream()));
						break;
					case R.id.jsonLJVersion:
						// ret = jrpc.invokeAndReadResponse("getLJversion",
						// new Object[] {}, String.class,
						// socket.getOutputStream(),
						// socket.getInputStream());
						break;
					case R.id.jsonException:
						// try {
						// ProxyUtil
						// .createClientProxy(getClassLoader(),
						// ServerService.class, jrpc, socket)
						// .iWantMyException(
						// new IllegalArgumentException(
						// "Tout le monde veut le coco"));
						// } catch (IllegalArgumentException e) {
						// ret = e.getMessage();
						// }
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
