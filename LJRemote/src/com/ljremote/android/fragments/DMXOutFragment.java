package com.ljremote.android.fragments;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.devsmart.android.ui.HorizontalListView;
import com.ljremote.android.MainActivity;
import com.ljremote.android.R;
import com.ljremote.android.adapters.DMXOutCursorAdapter;
import com.ljremote.android.data.DMXOutManager;
import com.ljremote.android.fragments.ChannelChooserFragment.ChannelChooserDialogListener;
import com.ljremote.json.model.DMXChannel;

public class DMXOutFragment extends AbstractDetailFragment implements ChannelChooserDialogListener {

	public DMXOutFragment() {
		super(R.string.fName_DmxOut);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("LJFunctionFragment", "onCreateView " + savedInstanceState);
		View mainView = inflater.inflate(R.layout.dmx_out_fragment, null, true);
		MainActivity main = (MainActivity) getActivity();
		setDataManager(main.getDataManager().getDMXOutManager());

		HorizontalListView list = (HorizontalListView) mainView
				.findViewById(R.id.dmx_list);
		DMXOutCursorAdapter adapter = new DMXOutCursorAdapter((DMXOutManager) getDataManager());
		list.setAdapter(adapter);
		return mainView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.dmx_out_fragment, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_dmx_out:
			ChannelChooserFragment.newInstance(R.string.dmx_out_add_dialog_title, this).show(getFragmentManager(), getTag());
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public List<Integer> getExistingChannels() {
		MainActivity main = (MainActivity) getActivity();
		DMXOutManager manager = main.getDataManager().getDMXOutManager();
		Cursor cursor = manager.getCursor();
		ArrayList<Integer> channels = new ArrayList<Integer>(cursor.getCount());
		if (cursor.moveToFirst()) {
			DMXChannel channel;
			do {
				channel = manager.getDMXChannelFromCursor(cursor);
				channels.add(channel.getChannel());
			} while (cursor.moveToNext());
		}
		cursor.close();
		return channels;
	}

	@Override
	public void onFinishChosingChannel(List<Integer> chosenChannels) {
		Log.d("LJFunctionFragment", chosenChannels.toString());
		MainActivity main = (MainActivity) getActivity();
		DMXOutManager manager = main.getDataManager().getDMXOutManager();
		manager.addOrRemoveChannels(chosenChannels);
	}
	
	
}
