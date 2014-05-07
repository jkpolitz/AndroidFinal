package com.example.upresent;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class Adapter extends ArrayAdapter<Presentation> {
	Context context;
	int layoutID;
	ArrayList<Presentation> data = new ArrayList<Presentation>();

	public Adapter(Context context, int layoutResourceId,
			ArrayList<Presentation> data) {
		super(context, layoutResourceId, data);
		this.layoutID = layoutResourceId;
		this.context = context;
		this.data = data;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		View v = convertView;
		Holder hd = null;
		final int position = pos;

		if (v == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			v = inflater.inflate(layoutID, parent, false);

			hd = new Holder();
			hd.name = (TextView) v.findViewById(R.id.txt);

			v.setTag(hd);
		} else {
			hd = (Holder) v.getTag();
		}

		Presentation pres = data.get(position);
		hd.name.setText(pres.name);

		hd.present = (TextView) v.findViewById(R.id.present);
		hd.delete = (TextView) v.findViewById(R.id.delete);

		hd.present.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Home) context).launchRemote(data.get(position).presId,
						data.get(position).name);
			}
		});
		hd.delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Home) context).deletePresentation(data.get(position).name,
						position);
			}
		});

		return v;
	}

	static class Holder {
		TextView name;
		TextView present;
		TextView delete;
	}
}
