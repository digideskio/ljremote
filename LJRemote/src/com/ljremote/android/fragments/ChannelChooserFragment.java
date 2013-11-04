package com.ljremote.android.fragments;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Switch;

import com.ljremote.android.R;
import com.ljremote.android.SettingsActivity;
import com.ljremote.android.util.NumberUtils;

public class ChannelChooserFragment extends DialogFragment implements
		OnClickListener {

	private GridView grid;
	private List<Integer> existingChannels;
	private ChannelChooserDialogListener listener;
	private boolean hideExisting;

	public interface ChannelChooserDialogListener {
		public List<Integer> getExistingChannels();

		public void onFinishChosingChannel(List<Integer> chosenChannels);
	}

	public static ChannelChooserFragment newInstance(int title,
			ChannelChooserDialogListener listener) {
		ChannelChooserFragment dialog = new ChannelChooserFragment();
		Bundle args = new Bundle();
		args.putInt("title", title);
		dialog.setArguments(args);
		dialog.setChannelChoserDialogListener(listener);
		return dialog;
	}

	private void setChannelChoserDialogListener(
			ChannelChooserDialogListener listener) {
		this.listener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.channel_chooser_fragment,
				null, true);
		grid = (GridView) mainView.findViewById(R.id.grid);
		grid.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
		existingChannels = getExistingChannels();
		Switch hideSwitch = (Switch) mainView.findViewById(R.id.hideSwitch);
		hideSwitch.setOnClickListener(this);
		attachGridAdapter(hideSwitch.isChecked());

		getDialog().setTitle(getArguments().getInt("title"));

		((Button) mainView.findViewById(R.id.ok_button))
				.setOnClickListener(this);
		((Button) mainView.findViewById(R.id.cancel_button))
				.setOnClickListener(this);
		return mainView;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		try {
			Field childFragmentManager = Fragment.class
					.getDeclaredField("mChildFragmentManager");
			childFragmentManager.setAccessible(true);
			childFragmentManager.set(this, null);

		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public void attachGridAdapter(boolean hideExisting) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String strSize = prefs.getString(SettingsActivity.DmxOutOverridePreferenceFragment.MAX_CHANNEL, "512");
		int size = Integer.parseInt(strSize);

		List<Integer> chosenChannels = getChosenChannels();

		ArrayList<Integer> datas = new ArrayList<Integer>(
				NumberUtils.rangeList(1, size));
		if (hideExisting) {
			datas.removeAll(existingChannels);
		} else {
			chosenChannels.addAll(existingChannels);
		}

		grid.setAdapter(new ArrayAdapter<Integer>(getActivity(),
				R.layout.channel_case, datas));

		setChosenChannels(chosenChannels);
	}

	public List<Integer> getExistingChannels() {
		if (listener != null) {
			return listener.getExistingChannels();
		} else {
			return new ArrayList<Integer>();
		}

	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}

	public List<Integer> getChosenChannels() {
		List<Integer> chosenChannels = new ArrayList<Integer>();

		if (grid != null && grid.getAdapter() != null
				&& grid.getCheckedItemCount() > 0) {
			SparseBooleanArray choices = grid.getCheckedItemPositions();
			for (int i = 0; i < choices.size(); i++) {
				if (choices.valueAt(i)) {
					chosenChannels.add((Integer) grid.getItemAtPosition(choices
							.keyAt(i)));
				}
			}
		}
		return chosenChannels;
	}

	public void setChosenChannels(List<Integer> chosenChannels) {
		if (grid != null) {
			for (int i = 0; i < grid.getCount(); i++) {
				if (chosenChannels.contains(grid.getItemAtPosition(i))) {
					grid.setItemChecked(i, true);
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.hideSwitch:
			hideExisting = ((Switch) v).isChecked();
			attachGridAdapter(hideExisting);
			break;
		case R.id.ok_button:
			List<Integer> choices = getChosenChannels();
			if (hideExisting) {
				choices.addAll(existingChannels);
			}
			listener.onFinishChosingChannel(choices);
			dismiss();
			break;
		case R.id.cancel_button:
			dismiss();
			break;
		default:
			break;
		}
	}
}
