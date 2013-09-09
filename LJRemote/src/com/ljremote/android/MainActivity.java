package com.ljremote.android;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ljremote.android.data.DataManager;
import com.ljremote.android.fragments.AbstractDetailFragment;
import com.ljremote.android.fragments.BGCueFragment;
import com.ljremote.android.fragments.ConnectivityDialogFragment;
import com.ljremote.android.fragments.ConnectivityDialogFragment.COMMAND;
import com.ljremote.android.fragments.ConnectivityDialogFragment.OnCommandListener;
import com.ljremote.android.fragments.CueListFragment;
import com.ljremote.android.fragments.DMXOutFragment;
import com.ljremote.android.fragments.MasterIntFragment;
import com.ljremote.android.fragments.MenuFragment;
import com.ljremote.android.fragments.SequenceFragment;
import com.ljremote.android.fragments.StaticFragment;
import com.ljremote.android.fragments.TabbedCueFragment;
import com.ljremote.android.fragments.TabbedLJFunctionFragment;
import com.ljremote.android.json.JSonRpcTask;
import com.ljremote.android.json.LJClientService;
import com.ljremote.android.json.LJClientService.LocalBinder;
import com.ljremote.android.json.LJClientService.MODE;
import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.services.DriverService;

public class MainActivity extends FragmentActivity implements
		MenuFragment.OnArticleSelectedListener,
		LJClientService.OnModeChangeListener, OnCommandListener,
		OnSharedPreferenceChangeListener {

	private final static String TAG = "Main";
	private DataManager dm;

	private List<AbstractDetailFragment> detailsFragments;
	private MenuFragment menuFragment = null;

	private final static String STATE_LAST_LOAD_FRAGMENT_POS = "last_load_fragment_position";
	private int lastLoadFragmentPosition;
	private LJClientService ljService;
	private ConnectivityDialogFragment connectivityDialog;
	private Bundle conf;
	private boolean serviceBound;
	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			serviceBound = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected");
			ljService = ((LocalBinder) service).getService();
			ljService.registerOnModeChangeListener(MainActivity.this);
			dm.bindService(ljService);
			serviceBound = true;
			if (ljService.getCurrentMode() == MODE.UNBOUND) {
				try {
					String host_adress = getSettings().getString(
							SettingsActivity.SERVER_HOST_ADDRESS, "0.0.0.0");
					int host_port = Integer.parseInt(getSettings().getString(
							SettingsActivity.SERVER_HOST_PORT, "-1"));
					ljService.setHost(host_adress, host_port);
					conf.putString(ConnectivityDialogFragment.SOCKET_ADDRESS,
							host_adress + ":" + host_port);
				} catch (UnknownHostException e) {
				} catch (IllegalArgumentException e) {
				}
			}
			onModeChange(ljService.getCurrentMode());
		}
	};
	private MenuItem itemChangeMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate " + savedInstanceState + ", "
				+ lastLoadFragmentPosition);
		setContentView(R.layout.activity_main);
		initConf();

		dm = new DataManager(this);

		detailsFragments = new ArrayList<AbstractDetailFragment>();
		registerFragment(new TabbedLJFunctionFragment());
		registerFragment(new StaticFragment());
		registerFragment(new SequenceFragment());
		registerFragment(new TabbedCueFragment());
		registerFragment(new CueListFragment());
		registerFragment(new BGCueFragment());
		registerFragment(new MasterIntFragment());
		registerFragment(new DMXOutFragment());

		menuFragment = new MenuFragment();

		lastLoadFragmentPosition = savedInstanceState == null ? -1
				: savedInstanceState.getInt(STATE_LAST_LOAD_FRAGMENT_POS, -1);

		connectivityDialog = new ConnectivityDialogFragment();
		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Service
		Intent intent = new Intent(this, LJClientService.class);
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		unbindService(serviceConnection);
		super.onDestroy();
	}

	private void initConf() {
		conf = new Bundle();
		conf.putSerializable(ConnectivityDialogFragment.SERVICE_MODE,
				MODE.UNBOUND);
		conf.putString(ConnectivityDialogFragment.SOCKET_ADDRESS,
				"0.0.0.0:2508");
		conf.putString(ConnectivityDialogFragment.LJ_VERSION, "0.0.0");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case android.R.id.home:
			// if (!deviceIsLargeScreen() && lastLoadFragmentPosition > 0) {
			// app icon in action bar clicked; go home
			// intent = new Intent(this, MainActivity.class);
			// startActivity(intent);
			// }
			// return true;
			if (!deviceIsLargeScreen()) {
				if (serviceBound && ljService != null) {
					Bundle args = new Bundle();
					args.putSerializable(MenuFragment.SERVICE_MODE,
							ljService.getCurrentMode());
					menuFragment.setArguments(args);
				}
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.detail_container, menuFragment).commit();
			}
			break;
		// case R.id.menu_testjson:
		// startActivity(new Intent(this, JSonTestActivity.class));
		// break;
		case R.id.menu_change_mode:
			updateConf();
			connectivityDialog.setArguments(conf);
			connectivityDialog.show(getSupportFragmentManager(), TAG);
			break;
		case R.id.menu_settings:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_black_out:
			toggleBlackOut(item);
		default:
		}
		return super.onOptionsItemSelected(item);
	}

	private void toggleBlackOut(MenuItem item) {
	}

	private void updateConf() {
		String host_adress = getSettings().getString(
				SettingsActivity.SERVER_HOST_ADDRESS, "0.0.0.0");
		int host_port = Integer.parseInt(getSettings().getString(
				SettingsActivity.SERVER_HOST_PORT, "-1"));
		conf.putString(ConnectivityDialogFragment.SOCKET_ADDRESS, host_adress
				+ ":" + host_port);
		conf.putSerializable(ConnectivityDialogFragment.SERVICE_MODE,
				ljService.getCurrentMode());
	}

	public boolean deviceIsLargeScreen() {
		return (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	public DataManager getDataManager() {
		return dm;
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume" + ", " + lastLoadFragmentPosition);
		super.onResume();
		Map<String, ?> map = getSettings().getAll();
		Log.d(TAG, "Settings : " + map.toString());
		getSettings().registerOnSharedPreferenceChangeListener(this);
		if (deviceIsLargeScreen()) {
			// on a large screen device ...
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.menu_container, menuFragment).commit();
			Fragment fragment = null;
			if (lastLoadFragmentPosition >= 0
					&& lastLoadFragmentPosition < detailsFragments.size()) {
				fragment = detailsFragments.get(lastLoadFragmentPosition);
				Log.d(TAG, "toto " + detailsFragments.size() + ", f-> "
						+ fragment);
				getSupportFragmentManager().executePendingTransactions();
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.detail_container, fragment).commit();
			}
		} else {
			Fragment fragment = null;
			if (lastLoadFragmentPosition < 0
					|| lastLoadFragmentPosition >= detailsFragments.size()) {
				fragment = menuFragment;
			} else {
				fragment = detailsFragments.get(lastLoadFragmentPosition);
			}
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.detail_container, fragment).commit();
		}
		if (serviceBound && ljService != null) {
			onModeChange(ljService.getCurrentMode());
		}
	}

	/**
	 * Backward-compatible version of {@link ActionBar#getThemedContext()} that
	 * simply returns the {@link android.app.Activity} if
	 * <code>getThemedContext</code> is unavailable.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private Context getActionBarThemedContextCompat() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return getActionBar().getThemedContext();
		} else {
			return this;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		itemChangeMode = menu.findItem(R.id.menu_change_mode);
		return true;
	}

	public boolean registerFragment(AbstractDetailFragment fragment) {
		if (detailsFragments.add(fragment)) {
			fragment.setPosition(detailsFragments.size() - 1);
			return true;
		}
		return false;
	}

	public boolean updateFragment(AbstractDetailFragment oldFragment,
			AbstractDetailFragment newFragment) {
		return updateFragment(oldFragment.getPosition(), newFragment);
	}

	public boolean updateFragment(int position, AbstractDetailFragment fragment) {
		if (position < 0 || position >= detailsFragments.size()) {
			return false;
		}
		AbstractDetailFragment oldFragment = detailsFragments.get(position);
		if (detailsFragments.set(position, fragment).equals(oldFragment)) {
			Log.d(TAG, "Fragment " + position + " update " + oldFragment
					+ " ====> " + fragment);
			fragment.setPosition(position);
			return true;
		}
		return false;
	}

	public boolean removeFragment(int position) {
		AbstractDetailFragment fragment = detailsFragments.remove(position);
		fragment.setPosition(-1);
		if (lastLoadFragmentPosition == position) {
			lastLoadFragmentPosition = -1;
		} else if (lastLoadFragmentPosition > position) {
			lastLoadFragmentPosition -= 1;
		}
		return true;
	}

	// @Override
	// protected void onSaveInstanceState(Bundle outState) {
	// outState.putInt(STATE_LAST_LOAD_FRAGMENT_POS, lastLoadFragmentPosition);
	// Log.d(TAG, "onSaveInstanceState " + outState);
	// super.onSaveInstanceState(outState);
	// }
	//
	//
	//
	// @Override
	// protected void onRestoreInstanceState(Bundle savedInstanceState) {
	// Log.d(TAG, "onRestoreInstanceState " + savedInstanceState);
	// lastLoadFragmentPosition = savedInstanceState == null ? -1
	// : savedInstanceState.getInt(STATE_LAST_LOAD_FRAGMENT_POS, -1);
	// super.onRestoreInstanceState(savedInstanceState);
	// }

	public List<String> getFragmentLabels() {
		List<String> labels = new ArrayList<String>(detailsFragments.size());
		for (AbstractDetailFragment fragment : detailsFragments) {
			labels.add(getString(fragment.getStr_id()));
		}
		return labels;
	}

	@Override
	public void onArticleSelected(int position) {
		AbstractDetailFragment fragment = detailsFragments.get(position);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.detail_container, fragment).commit();
		lastLoadFragmentPosition = position;
	}

	public void toggle() {
		View menu = findViewById(R.id.menu_container);
		if (menu.getVisibility() == View.VISIBLE) {
			menu.setVisibility(View.GONE);
			findViewById(R.id.detail_container).setVisibility(View.VISIBLE);
		} else {
			menu.setVisibility(View.VISIBLE);
			findViewById(R.id.detail_container).setVisibility(View.GONE);
		}
	}

	@Override
	public void onModeChange(MODE newMode) {
		conf.putSerializable(ConnectivityDialogFragment.SERVICE_MODE, newMode);
		// TypedArray a = getTheme().obtainStyledAttributes(new
		// int[]{R.attr.MyServerStatus});
		// Log.d(TAG, "TypedArray : " + a);
		// int id = a.getResourceId(0, 0);
		// Log.d(TAG, "Expected : " + R.style.ServerStatus + ", actual : " +
		// id);
		// a.recycle();
		//
		// getTheme().applyStyle(R.style.AppTheme_ApplyServerStatusBound, true);
		//
		// TypedArray b = getTheme().obtainStyledAttributes(new
		// int[]{R.attr.MyServerStatus});
		// Log.d(TAG, "TypedArray : " + b);
		// int idb = a.getResourceId(0, 0);
		// Log.d(TAG, "Expected : " + R.style.ServerStatus_Bound + ", actual : "
		// + idb);
		// a.recycle();

		menuFragment.changeServerMode(newMode, itemChangeMode);
		if (connectivityDialog.isVisible()) {
			connectivityDialog.updateUI(conf);
		}
	}

	@Override
	public void onCommand(COMMAND cmd, String[] args) {
		if (ljService == null || !serviceBound) {
			return;
		}
		switch (cmd) {
		case DRIVE:
			ljService.drive();
			JSonRpcTask<Void, Void, String> task = new JSonRpcTask<Void, Void, String>() {

				@Override
				protected String doInBackground(Void... params) {
					try {
						return ljService.getClientProxy(DriverService.class)
								.getLJversion();
					} catch (LJNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
				}

				@Override
				protected void onPostExecute(String result) {
					if (result != null) {
						conf.putString(ConnectivityDialogFragment.LJ_VERSION,
								result);
						connectivityDialog.updateUI(conf);
					}
				}
			};
			task.execute();
			break;
		case UNDRIVE:
			ljService.stopDrive();
			break;
		case CONNECT:
			ljService.connect();
			break;
		case DISCONNECT:
			ljService.disconnect();
			break;
		default:
			break;
		}
	}

	public SharedPreferences getSettings() {
		return PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		getSettings().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d(TAG, "Preferences changed : " + key);
		if (key.equals(SettingsActivity.SERVER_HOST_ADDRESS)
				|| key.equals(SettingsActivity.SERVER_HOST_PORT)) {
			String host_adress = sharedPreferences.getString(
					SettingsActivity.SERVER_HOST_ADDRESS, "0.0.0.0");
			int host_port = Integer.parseInt(getSettings().getString(
					SettingsActivity.SERVER_HOST_PORT, "-1"));
			if (ljService != null && serviceBound) {
				unbindService(serviceConnection);
				Intent intent = new Intent(this, LJClientService.class);
				bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
			}
		}
	}

}
