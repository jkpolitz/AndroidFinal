package com.example.upresent;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class Adapter extends ArrayAdapter<Presentation>{
	Context context;
	int layoutID;
	ArrayList<Presentation> data = new ArrayList<Presentation>();
	
	public Adapter(Context context, int layoutResourceId, ArrayList<Presentation> data) {
        super(context, layoutResourceId, data);
        this.layoutID = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Holder hd = null;
        
        if(v == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            v = inflater.inflate(layoutID, parent, false);
            
            hd = new Holder();
            hd.name = (TextView)v.findViewById(R.id.txt);
            
            v.setTag(hd);
        }
        else
        {
            hd = (Holder)v.getTag();
        }
        
        Presentation pres = data.get(position);
        hd.name.setText(pres.name);
        
        return v;
    }
    
    static class Holder
    {
    	TextView name;
    }
}
