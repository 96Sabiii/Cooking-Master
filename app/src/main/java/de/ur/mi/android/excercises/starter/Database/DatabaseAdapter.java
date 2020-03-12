package de.ur.mi.android.excercises.starter.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.ur.mi.android.excercises.starter.Domain.ListItem;

/**
 * Created by Sabrina Hartl on 12.08.2017.
 */


public class DatabaseAdapter {

    private static final String DATABASE_NAME = "receptlist.db";
    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_TABLE = "receptlistitems";

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_KATEGORY = "kategory";
    public static final String KEY_INGREDIENTS = "ingredients";
    public static final String KEY_DIRECTIONS = "directions";
    public static final String KEY_IMAGE = "image_data";

    public static final String[] ALL_KEYS = new String[] {KEY_ID, KEY_NAME, KEY_KATEGORY, KEY_INGREDIENTS, KEY_DIRECTIONS, KEY_IMAGE};

    private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" +
            KEY_ID  + " integer primary key autoincrement, "
            + KEY_NAME + " text not null, "
            + KEY_KATEGORY  + " text, "
            + KEY_INGREDIENTS + " text not null, "
            + KEY_DIRECTIONS  + " text not null, "
            +  KEY_IMAGE + " BLOB);";

    private final Context context;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DatabaseAdapter(Context context) {
        this.context = context;
        DBHelper = new DatabaseHelper(context);
    }

    public DatabaseAdapter open(){
        try {
            db = DBHelper.getWritableDatabase();
        } catch (SQLException s) {
            new Exception("Error with DB Open");
        }
        return this;
    }

    public void close(){
        if(db != null && db.isOpen()) {DBHelper.close();}
    }


    //Neue Liste von Daten in Datenbank
    public long insertReceptItem(ListItem item) {
        ContentValues itemValues = new ContentValues();
        itemValues.put(KEY_NAME, item.getName());
        itemValues.put(KEY_KATEGORY,item.getKategory());
        itemValues.put(KEY_INGREDIENTS, item.getIngredients());
        itemValues.put(KEY_DIRECTIONS, item.getDirection());
        itemValues.put(KEY_IMAGE, item.getImage());

        //Speichern in die Datenbank
        return db.insert(DATABASE_TABLE, null, itemValues);
    }

    //Löschen eines Items durch den primary Key
    public boolean deleteItem(long itemID){
        String where = KEY_ID + "=" + itemID;
        return db.delete(DATABASE_TABLE, where, null) != 0;
    }


    //Zurückgeben aller Daten in der Datenbank
    public Cursor getAllRows(){
        String where = null;
        Cursor c = db.query(true, DATABASE_TABLE, ALL_KEYS, where, null,null,null,null,null);
        if (c != null) c.moveToFirst();
        return c;
    }

    //Ansatz für die Möglichkeit des Sortieren von Items
    public Cursor sortItems(){
        String where = null;
        Cursor c = db.query(true, DATABASE_TABLE, ALL_KEYS, where, null,null,null, DATABASE_NAME + " ASC," + KEY_KATEGORY + " ASC", null);
        if (c != null) c.moveToFirst();
        return c;
    }

    //Auf bestimmte Reihe zugreifen
    public Cursor getRow(long rowId){
        String where = KEY_ID + "=" + rowId;
        Cursor c = db.query(true, DATABASE_TABLE, ALL_KEYS, where, null,null,null,null,null);
        if (c != null) c.moveToFirst();
        return c;
    }

    //ersetzen einen bestehenden eintrags
    public boolean update(long rowId, ListItem item){

        String where = KEY_ID + "=" + rowId;
        ContentValues newItemValues = new ContentValues();
        newItemValues.put(KEY_NAME, item.getName());
        newItemValues.put(KEY_KATEGORY,item.getKategory());
        newItemValues.put(KEY_INGREDIENTS, item.getIngredients());
        newItemValues.put(KEY_DIRECTIONS, item.getDirection());
        newItemValues.put(KEY_IMAGE, item.getImage());

        //in Datenbank speichern
        return db.update(DATABASE_TABLE, newItemValues, where, null) != 0;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper{

        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

    }

}

