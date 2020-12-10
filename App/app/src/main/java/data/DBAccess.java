package data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;

    public DBAccess(Context context){
        this.openHelper = new DBOpenHelper(context);
    }

    public void open(){
        this.database = openHelper.getReadableDatabase();
    }

    public void close(){
        if(database != null){
            this.database.close();
        }
    }

    public double getDistance(String country){
        String[] values = {country};
        Cursor cursor = database.rawQuery("SELECT DISTANCE FROM DISTANCES WHERE COUNTRY=?",values);
        if(cursor.moveToFirst()){
            return cursor.getDouble(0);
        }else{
            return 2.0;
        }
    }
}
