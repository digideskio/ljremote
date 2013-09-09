package com.ljremote.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ljremote.android.R;
import com.ljremote.android.data.DMXOutManager;
import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.json.model.DMXChannel;

public class DMXOutCursorAdapter extends AbstractCursorAdapter {
	public DMXOutCursorAdapter(DMXOutManager dmxOutManager) {
		super(dmxOutManager, R.layout.fadder_item);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View convertView = LayoutInflater.from(context).inflate(
				R.layout.fadder_item, null);

		bindView(convertView, context, cursor);
		return convertView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		DMXChannel channel = ((DMXOutManager) manager).getDMXChannelFromCursor(cursor);

		TextView label = (TextView) view.findViewById(R.id.label);
		label.setText(String.format("CH%03d", channel.getChannel()));
		TextView value = (TextView) view.findViewById(R.id.value);
		value.setText(Integer.toString(channel.getValue()));
		value.setSelected(channel.isForce());

		SeekBar seekBar = (SeekBar) view.findViewById(R.id.seek_int);
		seekBar.setProgress(channel.getValue());

		attachStateUpdater(value, context, channel.getChannel());
		attachProgressUpdatedListener(seekBar, context, channel.getChannel(), value);
	}

	private void attachProgressUpdatedListener(SeekBar seekBar,
			final Context context, final int id, final TextView watcher) {
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int progress = seekBar.getProgress();

				if (((DMXOutManager) manager).updateValue(id, progress)) {
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

	private void attachStateUpdater(TextView view, final Context context,
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
		view.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					Log.d("OnTouchListener", "Channel " + id + " selected");
					if( ((DMXOutManager) manager).forceDMXOut(id,!v.isSelected()) ){
						reloadCursor();
					}
					return true;
				default:
					break;
				}
				return false;
			}
		});
	}

	@Override
	public void onTableUpdateListener(Context context, TABLES table) {
		if (table == TABLES.DMXOUT) {
			reloadCursor();
		}
	}
}
