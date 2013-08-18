package com.ljremote.android.fragments;

import com.ljremote.android.R;
import com.ljremote.android.json.LJClientService.MODE;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ConnectivityDialogFragment extends DialogFragment implements OnClickListener {

	public static final String SERVICE_MODE = "service_mode";
	public static final String SOCKET_ADDRESS = "sockect_address";
	public static final String LJ_VERSION = "lj_version";
	private View v;
	private ImageView serverStatusIcon;
	private ImageView driverStatusIcon;
	private TextView serverStatusLarge;
	private TextView serverStatusAddress;
	private TextView driverStatusLarge;
	private TextView driverStatusVersion;
	private Button serverConnectButton;
	private Button driverLaunchButton;
	private OnCommandListener listener;

	public enum COMMAND {
		CONNECT, DISCONNECT, DRIVE, UNDRIVE;
	}
	
	public interface OnCommandListener {
		public void onCommand (COMMAND cmd, String... args);
	}
	
	public void onAttach(Activity activity){
		super.onAttach(activity);
		try {
			listener = (OnCommandListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("ConnectivityDialogFragment", "onCreateView");
		v = inflater.inflate(R.layout.connectivity_view, null);
		serverStatusIcon = (ImageView) v.findViewById(R.id.server_status_icon);
		serverStatusLarge = (TextView) v.findViewById(R.id.server_status_large);
		serverStatusAddress = (TextView) v
				.findViewById(R.id.server_status_address);
		serverConnectButton = (Button) v
				.findViewById(R.id.server_connect_button);
		driverStatusIcon = (ImageView) v.findViewById(R.id.driver_status_icon);
		driverStatusLarge = (TextView) v.findViewById(R.id.driver_status_large);
		driverStatusVersion = (TextView) v
				.findViewById(R.id.driver_status_version);
		driverLaunchButton = (Button) v.findViewById(R.id.driver_drive_button);

		serverConnectButton.setOnClickListener(this);
		driverLaunchButton.setOnClickListener(this);
		
		Bundle conf = getArguments();
		updateUI(conf);
		return v;
	};

	public void updateUI(Bundle conf) {
		if (conf == null) {
			return;
		}
		Log.d("ConnectivityDialogFragment", "loadConf");
		switch ((MODE) conf.get(SERVICE_MODE)) {
		case DRIVE:
			serverStatusIcon.setImageResource(R.drawable.ic_menu_cycle_green);
			serverStatusLarge.setText("Server connected");
			serverConnectButton.setText(R.string.disconnect);
			driverStatusIcon.setImageResource(R.drawable.ic_menu_cycle_green);
			driverStatusLarge.setText("LJ driven");
			driverLaunchButton.setText(R.string.undrive);
			break;
		case BOUND:
			serverStatusIcon.setImageResource(R.drawable.ic_menu_cycle_green);
			serverStatusLarge.setText("Server connected");
			serverConnectButton.setText(R.string.disconnect);
			driverStatusIcon.setImageResource(R.drawable.ic_menu_cycle_red);
			driverStatusLarge.setText("LJ not driven");
			driverLaunchButton.setEnabled(true);
			driverLaunchButton.setText(R.string.drive);
			break;
		case UNBOUND:
		default:
			serverStatusIcon.setImageResource(R.drawable.ic_menu_cycle_red);
			serverStatusLarge.setText("Server not connected");
			serverConnectButton.setText(R.string.connect);
			driverStatusIcon.setImageResource(R.drawable.ic_menu_cycle_red);
			driverStatusLarge.setText("LJ not driven");
			driverLaunchButton.setEnabled(false);
			driverLaunchButton.setText(R.string.drive);
			break;
		}
		serverStatusAddress.setText(conf.getCharSequence(SOCKET_ADDRESS));
		driverStatusVersion.setText(conf.getCharSequence(LJ_VERSION));
	}

	@Override
	public void onClick(View v) {
		Bundle conf = getArguments();
		Log.d("d;pd,indind", conf.get(SERVICE_MODE).toString());
		switch (v.getId()) {
		case R.id.server_connect_button:
			switch ((MODE) conf.get(SERVICE_MODE)) {
			case BOUND:
			case DRIVE:
				listener.onCommand(COMMAND.DISCONNECT);
				break;
			case UNBOUND:
				listener.onCommand(COMMAND.CONNECT);
				break;
			default:
				break;
			}
			break;
		case R.id.driver_drive_button:
			switch ((MODE) conf.get(SERVICE_MODE)) {
			case BOUND:
				listener.onCommand(COMMAND.DRIVE);
				break;
			case DRIVE:
				listener.onCommand(COMMAND.UNDRIVE);
				break;
			case UNBOUND:
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}
}
