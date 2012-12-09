package com.ljremote.android.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ljremote.android.MainActivity;
import com.ljremote.android.R;
import com.ljremote.android.types.Static;

public class StaticListAdapter extends ArrayAdapter<Static> {
	
	public StaticListAdapter(Context context, Static[] statics) {
		super(context,R.layout.static_item,statics);
	}
	
	public StaticListAdapter(Context context,
			List<Static> statics) {
		super(context,R.layout.static_item,statics);
	}

	static class ViewHolder {
		public TextView id;
		public TextView label;
		public Button intensity;
		public SeekBar seek_int;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		
		if(convertView == null){
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.static_item, null);
			
			holder = new ViewHolder();
			holder.id = (TextView) convertView.findViewById(R.id.id);
			holder.label = (TextView) convertView.findViewById(R.id.label);
			holder.intensity = (Button) convertView.findViewById(R.id.intensity);
			holder.seek_int = (SeekBar) convertView.findViewById(R.id.seek_intensity);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Static s = (Static) getItem(position);
		if(s != null){
			holder.id.setText(String.valueOf(s.getId()));
			holder.label.setText(s.getLabel());
			holder.label.setSelected(s.isEnabled());
			holder.intensity.setText(String.valueOf(s.getIntensity()));
			holder.seek_int.setProgress(s.getIntensity());
			attachProgressUpdatedListener(holder.seek_int,position);
		}
		return convertView;
	}

	private void attachProgressUpdatedListener(SeekBar seekBar, final int position) {
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Static s= getItem(position);
				int progress = seekBar.getProgress();
				
				if (((MainActivity) getContext()).getDataManager().updateStaticIntensity(s.getId(), progress)){
					s.setIntensity(progress);
				}
				
				notifyDataSetChanged();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				
			}
		});
	}
}
