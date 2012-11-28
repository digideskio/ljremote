package com.ljremote.android.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ljremote.android.R;
import com.ljremote.android.types.MasterInt;
import com.ljremote.android.types.Static;

public class MasterIntListAdapter extends ArrayAdapter<MasterInt> {
	
	public MasterIntListAdapter(Context context, MasterInt[] masters) {
		super(context,R.layout.mstr_int_item,masters);
	}
	
	public MasterIntListAdapter(Context context,
			List<MasterInt> masters) {
		super(context,R.layout.mstr_int_item,masters);
	}

	static class ViewHolder {
		public SeekBar seek_mst_int;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		
		if(convertView == null){
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.mstr_int_item, null);
			
			holder = new ViewHolder();
			holder.seek_mst_int = (SeekBar) convertView.findViewById(R.id.seek_int);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		MasterInt s = (MasterInt) getItem(position);
		if(s != null){
			holder.seek_mst_int.setProgress(s.getIntensity());
		}
		return convertView;
	}

}
