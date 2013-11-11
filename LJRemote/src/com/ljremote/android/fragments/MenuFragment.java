package com.ljremote.android.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ljremote.android.MainActivity;
import com.ljremote.android.R;
import com.ljremote.android.json.LJClientService.MODE;

public class MenuFragment extends Fragment implements OnItemClickListener {
	OnArticleSelectedListener listener;

	public interface OnArticleSelectedListener {
		public void onArticleSelected(int position);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnArticleSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnArticleSelectedListener");
		}
	}

	public final static String SERVICE_MODE = "service.mode";
	private ListView menu;
	private View mainView = null;
	private ImageView server_mode_icon;
	private TextView server_mode_text;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("MenuFragment", "onCreateView tag=" + getTag() + ", "
				+ getArguments());
		mainView = inflater.inflate(R.layout.menu_fragment, null, true);
		updateList();

		if (getArguments() != null) {
			changeServerMode((MODE) getArguments().get(SERVICE_MODE));
		}
		MainActivity main = (MainActivity) getActivity();
		if (!main.deviceIsLargeScreen()) {
			final ActionBar actionBar = main.getActionBar();
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			actionBar.setDisplayShowTitleEnabled(true);
		}

		return mainView;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		listener.onArticleSelected(position);
	}

	public void updateList() {
		if (mainView != null) {
			menu = (ListView) mainView.findViewById(R.id.menu);
			server_mode_icon = (ImageView) mainView
					.findViewById(R.id.server_mode_icon);
			server_mode_text = (TextView) mainView
					.findViewById(R.id.server_mode_text);

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getActivity(), android.R.layout.simple_list_item_1,
					((MainActivity) getActivity()).getFragmentLabels());
			menu.setAdapter(adapter);
			menu.setOnItemClickListener(this);
		}
	}

	public void changeServerMode(MODE newMode) {
		changeServerMode(newMode, null);
	}

	public void changeServerMode(MODE newMode, MenuItem itemChangeMode) {
		if (isVisible()) {
			int icon_id;
			int text_id;
			switch (newMode) {
			case DRIVE:
				icon_id = R.drawable.ic_menu_cycle_green;
				text_id = R.string.server_mode_driver;
				break;
			case BOUND:
				icon_id = R.drawable.ic_menu_cycle_orange;
				text_id = R.string.server_mode_bound;
				break;
			default:
				icon_id = R.drawable.ic_menu_cycle_red;
				text_id = R.string.server_mode_unbound;
				break;
			}
			server_mode_icon.setImageResource(icon_id);
			server_mode_text.setText(text_id);
			if (itemChangeMode != null) {
				itemChangeMode.setIcon(icon_id);
			}

		}
	}

}
