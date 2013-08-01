package com.ljremote.android;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ljremote.android.data.DataManager;
import com.ljremote.android.fragments.AbstractDetailFragment;
import com.ljremote.android.fragments.BGCueFragment;
import com.ljremote.android.fragments.ConnectivityDialogFragment;
import com.ljremote.android.fragments.CueFragment;
import com.ljremote.android.fragments.CueListFragment;
import com.ljremote.android.fragments.MasterIntFragment;
import com.ljremote.android.fragments.MenuFragment;
import com.ljremote.android.fragments.SequenceFragment;
import com.ljremote.android.fragments.StaticFragment;
import com.ljremote.android.json.JSonTestActivity;
import com.ljremote.android.json.LJClientService;
import com.ljremote.android.json.LJClientService.MODE;

public class MainActivity extends FragmentActivity implements
		MenuFragment.OnArticleSelectedListener,
		LJClientService.OnModeChangeListener {

	private final static String TAG = "Main";
	private DataManager dm;

	private List<AbstractDetailFragment> detailsFragments;
	private MenuFragment menuFragment = null;

	private final static String STATE_LAST_LOAD_FRAGMENT_POS = "last_load_fragment_position";
	private int lastLoadFragmentPosition;
	private LJClientService ljService;
	private ConnectivityDialogFragment connectivityDialog;
	private Bundle conf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dm = new DataManager(this);

		detailsFragments = new ArrayList<AbstractDetailFragment>();
		registerFragment(new StaticFragment());
		registerFragment(new SequenceFragment());
		registerFragment(new CueFragment());
		registerFragment(new CueListFragment());
		registerFragment(new BGCueFragment());
		registerFragment(new MasterIntFragment());

		menuFragment = new MenuFragment();

		lastLoadFragmentPosition = savedInstanceState == null ? -1
				: savedInstanceState.getInt(STATE_LAST_LOAD_FRAGMENT_POS, -1);

		connectivityDialog = new ConnectivityDialogFragment();
		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

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
			intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			// }
			return true;
		case R.id.menu_testjson:
			startActivity(new Intent(this, JSonTestActivity.class));
			break;
		case R.id.menu_change_mode:
			// connectivityDialog.loadBundle(conf);
			// connectivityDialog.show(getSupportFragmentManager(), TAG);
			// case R.id.menu_mode_bound:
			// onModeChange(MODE.BOUND);
			// break;
			// case R.id.menu_mode_unbound:
			// onModeChange(MODE.UNBOUND);
			// break;
			// case R.id.menu_mode_drive:
			// onModeChange(MODE.DRIVE);
			// break;
		case R.id.menu_settings:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		default:
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean deviceIsLargeScreen() {
		return (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	public DataManager getDataManager() {
		return dm;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (deviceIsLargeScreen()) {
			// on a large screen device ...
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.menu_container, menuFragment).commit();
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
		return true;
	}

	public boolean registerFragment(AbstractDetailFragment fragment) {
		if (detailsFragments.add(fragment)) {
			fragment.setPosition(detailsFragments.size() - 1);
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_LAST_LOAD_FRAGMENT_POS, lastLoadFragmentPosition);
	}

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
		menuFragment.changeServerMode(newMode);
	}
}
