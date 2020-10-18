package data;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DBOpenHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "distancias.db";
    private static final int DATABASE_VERSION = 1;

    public DBOpenHelper(Context context){
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
    }


}
