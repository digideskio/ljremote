package com.ljremote.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ljremote.android.R;
import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.android.data.StaticManager;
import com.ljremote.json.model.Static;

public class StaticCursorAdapter extends AbstractCursorAdapter {
	public StaticCursorAdapter(StaticManager abstractDataManager) {
		super(abstractDataManager, R.layout.static_item);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View convertView = LayoutInflater.from(context).inflate(
				R.layout.static_item, null);

		bindView(convertView, context, cursor);
		return convertView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Static s = ((StaticManager) manager).getStaticFromCursor(cursor);

		((TextView) view.findViewById(R.id.id)).setText(String.valueOf(s
				.getId()));
		TextView label = (TextView) view.findViewById(R.id.label);
		label.setText(s.getLabel());
		label.setSelected(s.isEnable());
		if (s.isEnable()) {
			view.setBackgroundResource(R.drawable.green);
		} else {
			view.setBackgroundResource(android.R.color.transparent);
		}
		Button intensity = (Button) view.findViewById(R.id.intensity);
		intensity.setText(Integer.toString(s.getIntensity()));

		SeekBar seekBar = (SeekBar) view.findViewById(R.id.seek_intensity);
		seekBar.setProgress(s.getIntensity());

		attachStateUpdater(label, context, s.getId());
		attachProgressUpdatedListener(seekBar, context, s.getId(), intensity);
	}

	private void attachProgressUpdatedListener(SeekBar seekBar,
			final Context context, final int id, final TextView watcher) {
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int progress = seekBar.getProgress();

				if (((StaticManager) manager).updateIntensity(id, progress)) {
					reloadCursor();
				}

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				watcher.setText(String.valueOf(seekBar.getProgress()));
			}
		});
	}

	private void attachStateUpdater(View view, final Context context,
			final int id) {
//		view.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
//			
//			@Override
//			public void onCreateContextMenu(ContextMenu menu, View v,
//					ContextMenuInfo menuInfo) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((StaticManager) manager).updateState(id, !v.isSelected())) {
					reloadCursor();
				}
			}
		});
	}

	@Override
	public void onTableUpdateListener(Context context, TABLES table) {
		if (table == TABLES.STATICS) {
			reloadCursor();
		}
	}
}
