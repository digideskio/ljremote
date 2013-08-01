package com.ljremote.android.ui;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.ljremote.android.R;

public class ConnectivitySpinner extends Spinner {
	
	private View connectivityView;

	public ConnectivitySpinner(Context context) {
		super(context);
		connectivityView = inflate(context, com.ljremote.android.R.layout.connectivity_view, null);
		ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(context, android.R.layout.simple_spinner_item, new Integer[]{0});
		adapter.setDropDownViewResource(R.layout.connectivity_view);
		setAdapter(adapter);
	}
	
}
