package com.example.kishan.locations;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;


public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View view;
    private Context mContext;

    public CustomInfoWindowAdapter(Context context) {
        mContext = context;
        view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
    }

    private void rendowWindowText(Marker marker, View view){

        String title = marker.getTitle();
        TextView title_textView = view.findViewById(R.id.title);

        if(!title.equals("")){
            title_textView.setText(title);
        }

        Cursor res = MainActivity.mainLocDb.getTime(marker.getTitle(), marker.getPosition().longitude + ", "
            + marker.getPosition().latitude);

        if(res != null && res.getCount() > 0)
        {
            return;
        }
        TextView time_textView = view.findViewById(R.id.snippet);
        while(res.moveToNext())
        {
            time_textView.setText(res.getString(0));
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        rendowWindowText(marker, view);
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendowWindowText(marker, view);
        return view;
    }
}