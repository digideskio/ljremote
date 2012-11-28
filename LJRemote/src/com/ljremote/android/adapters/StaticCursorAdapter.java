package com.ljremote.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ljremote.android.MainActivity;
import com.ljremote.android.R;
import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.android.data.Database.Statics;

public class StaticCursorAdapter extends SimpleCursorAdapter {
	public StaticCursorAdapter(MainActivity main) {
		super(main, R.layout.static_item, main.getDataManager().getCursor(
				TABLES.STATICS), Statics.COLUMN_NAMES, null, NO_SELECTION);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View convertView = LayoutInflater.from(context).inflate(
				R.layout.static_item, null);

		// ((TextView) convertView.findViewById(R.id.id)).setText(String
		// .valueOf(cursor.getInt(Statics.NUM_COL_ID)));
		// ((TextView) convertView.findViewById(R.id.label)).setText(cursor
		// .getString(Statics.NUM_COL_LABEL));
		// ((Button) convertView.findViewById(R.id.intensity)).setText(cursor
		// .getString(Statics.NUM_COL_INTENSITY));
		bindView(convertView, context, cursor);
		return convertView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		int id = cursor.getInt(Statics.NUM_COL_ID);
		boolean enabled = cursor.getInt(Statics.NUM_COL_ENABLED) > 0;

		((TextView) view.findViewById(R.id.id)).setText(String.valueOf(id));
		TextView label = (TextView) view.findViewById(R.id.label);
		label.setText(cursor.getString(Statics.NUM_COL_LABEL));
		label.setSelected(enabled);
		if(enabled){
			view.setBackgroundResource(R.drawable.green);
		} else {
			view.setBackgroundResource(android.R.color.transparent);
		}
		Button intensity = (Button) view.findViewById(R.id.intensity);
		intensity.setText(cursor.getString(Statics.NUM_COL_INTENSITY));

		SeekBar seekBar = (SeekBar) view.findViewById(R.id.seek_intensity);
		seekBar.setProgress(cursor.getInt(Statics.NUM_COL_INTENSITY));

		attachStateUpdater(label, context, id);
		attachProgressUpdatedListener(seekBar, context, id, intensity);
	}

	private void attachProgressUpdatedListener(SeekBar seekBar,
			final Context context, final int id, final TextView watcher) {
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int progress = seekBar.getProgress();

				if (((MainActivity) context).getDataManager()
						.updateStaticIntensity(id, progress)) {
					swapCursor(((MainActivity) context).getDataManager()
							.getCursor(TABLES.STATICS));
				}

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

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
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(((MainActivity) context).getDataManager().updateStaticState(id,!v.isSelected())){
					swapCursor(((MainActivity) context).getDataManager()
							.getCursor(TABLES.STATICS));
				}
			}
		});
	}
	
}
