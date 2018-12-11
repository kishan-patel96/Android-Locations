package com.example.kishan.locations;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class InfoList extends MainActivity
{
    ListView infoList;
    ListAdapter listAdapter;
    List<LocInfoNode> locationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        setTitle("Check-In List");
        centerTitle();

        locationsList = new ArrayList<>();
        infoList = findViewById(R.id.list_info);
        listAdapter = new ListAdapter(this, locationsList);
        infoList.setAdapter(listAdapter);

        initializeList();
    }

    public void initializeList()
    {
        locationsList.clear();
        Cursor res = mainLocDb.getAll();
        while(res.moveToNext())
        {
            locationsList.add(new LocInfoNode(res.getString(1), res.getString(2),
                    res.getString(3), res.getString(4)));
        }
        listAdapter.notifyDataSetChanged();
    }
}
