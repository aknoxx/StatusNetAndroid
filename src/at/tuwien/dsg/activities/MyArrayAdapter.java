package at.tuwien.dsg.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import at.tuwien.dsg.R;
import at.tuwien.dsg.entities.Request;


public class MyArrayAdapter extends ArrayAdapter<Request> {
	private final Activity context;
	private final ArrayList<Request> requests;
	private SimpleDateFormat longDate = new SimpleDateFormat("EEE, MMM d, HH:mm:ss", Locale.ENGLISH);
	private SimpleDateFormat shortDate = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);

	public MyArrayAdapter(Activity context, ArrayList<Request> requests) {
		super(context, R.layout.r_item, requests);
		this.context = context;
		this.requests = requests;
	}

	// static to save the reference to the outer class and to avoid access to
	// any members of the containing class
	static class ViewHolder {
		public TextView infoView;
		public TextView requestView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ViewHolder will buffer the assess to the individual fields of the row
		// layout

		ViewHolder holder;
		// Recycle existing view if passed as parameter
		// This will save memory and time on Android
		// This only works if the base layout for all classes are the same
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.r_item, null, true);
			holder = new ViewHolder();
			holder.infoView = (TextView) rowView.findViewById(R.id.request_info);
			holder.requestView = (TextView) rowView.findViewById(R.id.request);
			//holder.imageView = (ImageView) rowView.findViewById(R.id.icon);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}

		Request r = requests.get(position);
		String time;
		if(r.getCreatedAt().getDay() == new Date().getDay())
		{
			time = shortDate.format(r.getCreatedAt());
		}
		else {
			time = longDate.format(r.getCreatedAt());
		}
		
		/*
		if(r.getQualifier().equals("SR")) {
			holder.infoView.setTextColor(0xffffffff);
			holder.requestView.setTextColor(0xccffff);
		}	
		*/ 
		String status;
		if(r.isSaved()) {
			status = "Saved";
		}
		else {
			status = "Unsaved";
		}
		
		holder.infoView.setText("From " + r.getRequester() + " at " + time 
				+ "\t\tStatus: " + status);	
		holder.requestView.setText(r.getCompleteRequestText());
		
		

		return rowView;
	}
}
