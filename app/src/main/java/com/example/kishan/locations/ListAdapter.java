package com.example.kishan.locations;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class ListAdapter extends BaseAdapter
{
    private Context mContext;
    private List<LocInfoNode> locationsInfoList;

    public ListAdapter(Context context, List<LocInfoNode> locationsInfoList)
    {
        this.mContext = context;
        this.locationsInfoList = locationsInfoList;
    }

    @Override
    public int getCount()
    {
        return locationsInfoList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        convertView = View.inflate(mContext, R.layout.activity_listrow, null);

        TextView name = convertView.findViewById(R.id.list_name);
        TextView time = convertView.findViewById(R.id.list_time);
        TextView coordinates = convertView.findViewById(R.id.list_coordinates);
        TextView address = convertView.findViewById(R.id.list_address);

        name.setText(locationsInfoList.get(position).name);
        time.setText(locationsInfoList.get(position).time);
        coordinates.setText(locationsInfoList.get(position).coordinates);
        address.setText(locationsInfoList.get(position).address);

        return convertView;
    }
}
