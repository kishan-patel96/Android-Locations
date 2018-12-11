package com.example.kishan.locations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocationDatabase extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "locationsinfo.db";
    public static final String TABLE_NAME = "locations_data";
    public static final String COL1 = "ID";
    public static final String COL2 = "NAME";
    public static final String COL3 = "TIME";
    public static final String COl4 = "COORDINATES";
    public static final String COl5 = "ADDRESS";
    Context mContext;

    public LocationDatabase(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "NAME TEXT, TIME TEXT, COORDINATES TEXT, ADDRESS TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public boolean addData(String name, String time, String coordinates, String address)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, name);
        contentValues.put(COL3, time);
        contentValues.put(COl4, coordinates);
        contentValues.put(COl5, address);

        long result = db.insert(TABLE_NAME, null, contentValues);

        if(result == -1)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean updateMarkerData(int id, String name, String time, String coordinates, String address)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL2, name);
        contentValues.put(COL3, time);
        contentValues.put(COl4, coordinates);
        contentValues.put(COl5, address);


        long result = db.update(TABLE_NAME, contentValues, COL1 + " = ?", new String[] {id + ""});

        if(result == -1)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public Cursor getId(String name, String coordinates)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT " + COL1 + " FROM " + TABLE_NAME + " WHERE " +
                COL2 + " = ?" + "AND " + COl4 + " = ?", new String[] {name, coordinates});
        return data;
    }

    public Cursor getTime(String name, String coordinates)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT " + COL3 + " FROM " + TABLE_NAME + " WHERE " +
                COL2 + " = ?" + "AND " + COl4 + " = ?", new String[] {name, coordinates});
        return data;
    }

    public boolean deleteData(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        //Toast.makeText(mContext, id + "", Toast.LENGTH_SHORT).show();

        int res = db.delete(TABLE_NAME, "ID = ?", new String[]{id + ""});
        if(res > 0)
        {
            //Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
            return true;
        }
        else
        {
            //Toast.makeText(mContext, "Failed!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public Cursor getAll()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return data;
    }

    /*
    public Cursor getId_Names()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT " + COL1 + ", " + COL2 + " FROM " + TABLE_NAME, null);
        return data;
    }

    public Cursor getName(String id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT " + COL2 + " FROM " + TABLE_NAME + " WHERE " +
                COL1 + " = ?", new String[] {id});
        return data;
    }

    public Cursor getRow(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT " + COL2 + ", " + COL3 + ", " + COl4 + " FROM "
                + TABLE_NAME + " WHERE " + COL1 + " = ?", new String[] {id + ""});
        return data;
    }

    public Cursor getRelationship(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT " + COl4 + " FROM " + TABLE_NAME +
                " WHERE " + COL1 + " = ?", new String[] {id + ""});
        return data;
    }
    */

}
