package com.ljremote.android.json;

import java.net.UnknownHostException;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.ljremote.android.R;
import com.ljremote.android.json.LJClientService.LocalBinder;
import com.ljremote.android.json.LJClientService.MODE;
import com.ljremote.json.services.DriverService;
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
			ljService
					.registerOnModeChangeListener(new LJClientService.OnModeChangeListener() {

						@Override
						public void onModeChange(MODE newMode) {
							jsonDisplay.append("\nMode: " + newMode);
							// if(newMode == MODE.BOUND){
							// ljService.drive();
							// }
						}
					});
			serviceBound = true;
			try {
				ljService.setHost("192.168.0.10", 2508);
				// ljService.connect();
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

		((Button) findViewById(R.id.jsonConnect)).setOnClickListener(this);
		((Button) findViewById(R.id.jsonDisconnect)).setOnClickListener(this);
		((Button) findViewById(R.id.jsonDrive)).setOnClickListener(this);
		((Button) findViewById(R.id.jsonUnDrive)).setOnClickListener(this);
		((Button) findViewById(R.id.jsonHelloWorld)).setOnClickListener(this);
		((Button) findViewById(R.id.jsonLJReady)).setOnClickListener(this);
		((Button) findViewById(R.id.jsonLJVersion)).setOnClickListener(this);
		((Button) findViewById(R.id.jsonException)).setOnClickListener(this);

		Intent intent = new Intent(this, LJClientService.class);
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private void processJSon(final int id) {
		JSonRpcTask<Integer, Void, String> task = new JSonRpcTask<Integer, Void, String>() {

			@Override
			protected String doInBackground(Integer... params) {
				try {
					switch (params[0]) {
					case R.id.jsonConnect:
						ljService.connect();
						return "Connecting ...";
					case R.id.jsonDisconnect:
						ljService.disconnect();
						return "Disconnecting ...";
					case R.id.jsonDrive:
						ljService.drive();
						return "Driving ...";
					case R.id.jsonUnDrive:
						ljService.stopDrive();
						return "Stop driving ...";
					case R.id.jsonHelloWorld:
						return getClientProxy(ServerService.class).helloWord();
					case R.id.jsonLJReady:
						return getClientProxy(DriverService.class).isLJready() ? "LJ Ready !"
								: "LJ not Ready";
					case R.id.jsonLJVersion:
						return getClientProxy(DriverService.class).getLJversion();
					case R.id.jsonException:
						try {
							getClientProxy(ServerService.class)
									.iWantMyException(
											new IllegalArgumentException(
													"Tout le monde veut le coco"));
						} catch (Exception e) {
							return e.getMessage();
						}
					default:
						break;
					}
				} catch (Exception e) {
					return e.getMessage();
				}
				return null;
			}

			@Override
			protected void onPostExecute(String result) {
				if(result!= null){
					jsonDisplay.append("\n");
					jsonDisplay.append(result);
				}
			}
			
			
		};
		ljService.submit(task,id);
	}

	@Override
	public void onClick(View v) {
		processJSon(v.getId());
	}

	@Override
	protected void onDestroy() {
		if (serviceBound) {
			// ljService.disconnect();
			unbindService(serviceConnection);
			serviceBound = false;
		}
		super.onDestroy();
	}

}
