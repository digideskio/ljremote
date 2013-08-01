package com.ljremote.android.fragments;

import com.ljremote.android.R;
import com.ljremote.android.json.LJClientService.MODE;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ConnectivityDialogFragment extends DialogFragment{
	
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v= inflater.inflate(R.layout.connectivity_view, null);
		serverStatusIcon= (ImageView) v.findViewById(R.id.server_status_icon);
		serverStatusLarge= (TextView) v.findViewById(R.id.server_status_large);
		serverStatusAddress= (TextView) v.findViewById(R.id.server_status_address);
		driverStatusIcon= (ImageView) v.findViewById(R.id.driver_status_icon);
		driverStatusLarge= (TextView) v.findViewById(R.id.driver_status_large);
		driverStatusVersion= (TextView) v.findViewById(R.id.driver_status_version);
		return v;
	};
	
	public void loadBundle(Bundle conf){
		if(isVisible()){
			switch ((MODE)conf.get(SERVICE_MODE)) {
			case BOUND:
				serverStatusIcon.setImageResource(R.drawable.ic_menu_cycle_green);
				serverStatusLarge.setText("Server connected");
				serverStatusAddress.setText(conf.getCharSequence(SOCKET_ADDRESS));
				driverStatusIcon.setImageResource(R.drawable.ic_menu_cycle_red);
				driverStatusLarge.setText("LJ not driven");
				driverStatusVersion.setText("N/A");
				break;
			case DRIVE:
				driverStatusIcon.setImageResource(R.drawable.ic_menu_cycle_green);
				driverStatusLarge.setText("LJ driven");
				driverStatusVersion.setText(conf.getCharSequence(LJ_VERSION));
				break;
			case UNBOUND:
				serverStatusIcon.setImageResource(R.drawable.ic_menu_cycle_red);
				serverStatusLarge.setText("Server connected");
				serverStatusAddress.setText(conf.getCharSequence(SOCKET_ADDRESS));
				driverStatusIcon.setImageResource(R.drawable.ic_menu_cycle_red);
				driverStatusLarge.setText("LJ not driven");
				driverStatusVersion.setText("N/A");
				break;
			default:
				break;
			}
		}
	}
}
