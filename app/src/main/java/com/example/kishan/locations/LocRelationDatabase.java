package com.example.kishan.locations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocRelationDatabase extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "locationsrelations.db";
    public static final String TABLE_NAME = "locations_relations";
    public static final String COL1 = "ID";
    public static final String COL2 = "RELATION";
    public static final String COL3 = "NAME";
    Context mContext;

    public LocRelationDatabase(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "RELATION TEXT, NAME TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public boolean addData(String relation, String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, relation);
        contentValues.put(COL3, name);

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
}
