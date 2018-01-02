package com.dozydroid.fmp.utilities;

/**
 * Created by MIRSAAB on 11/9/2017.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dozydroid.fmp.models.Video;

import org.json.JSONArray;
import org.json.JSONObject;

public class VideosDBHandler extends SQLiteOpenHelper {

    static final int DB_VERSION = 1;
    static final String DB_NAME = "fmp_videos.db";
    static final String TABLE_NAME = "videos";
    static final String COLUMN_ID = "id";
    static final String COLUMN_VIDEO_TITLE = "video_title";
    static final String COLUMN_VIDEO_DATA = "video_data";
    static final String COLUMN_VIDEO_DURATION = "video_duration";
    static final String COLUMN_VIDEO_RESOLUTION = "video_resolution";
    static final String COLUMN_RECORD_CATEGORY = "record_category";
    public static final String KEY_HISTORY = "history";
    public static final String KEY_FAVORITE = "favorite";

    int id = 0;

    SQLiteDatabase myDB;

    public VideosDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, factory, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER NOT NULL, " +
                COLUMN_VIDEO_TITLE + " TEXT, " +
                COLUMN_VIDEO_DATA + " TEXT, " +
                COLUMN_VIDEO_DURATION + " TEXT, " +
                COLUMN_RECORD_CATEGORY + " TEXT, " +
                COLUMN_VIDEO_RESOLUTION + " TEXT " +
                ");";
        db.execSQL(query);
//        Log.d("onCreate","Database OnCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void truncateTable(){
        myDB.execSQL("DELETE FROM " + TABLE_NAME);
    }

    public void deleteByCategory(String category){
        if(myDB!=null && myDB.isOpen())
            myDB.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_RECORD_CATEGORY + " LIKE '" +category +"'");
        else{
            openDB();
            myDB.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_RECORD_CATEGORY + " LIKE '" +category +"'");
        }
    }

    public void deleteSingleVideo(Video video){
        String deleteQuery = "DELETE FROM " + TABLE_NAME +
                " WHERE " + COLUMN_VIDEO_TITLE + " = '" + video.getTitle() + "' " +
                " AND " + COLUMN_VIDEO_DATA + " = '" + video.getData() + "'";
        if(myDB!=null && myDB.isOpen())
            myDB.execSQL(deleteQuery);
        else{
            openDB();
            myDB.execSQL(deleteQuery);
        }
    }

    public void unFavoriteVideo(Video video){
        String deleteQuery = "DELETE FROM " + TABLE_NAME +
                " WHERE " + COLUMN_VIDEO_TITLE + " = '" + video.getTitle() + "' " +
                " AND " + COLUMN_VIDEO_DATA + " = '" + video.getData() + "' " +
                " AND " + COLUMN_RECORD_CATEGORY + " = '"+ KEY_FAVORITE + "'";
        if(myDB!=null && myDB.isOpen())
            myDB.execSQL(deleteQuery);
        else{
            openDB();
            myDB.execSQL(deleteQuery);
        }
    }

    public void addVideo(Video video, String category) {
        JSONArray jsonArray;
        if(category.equals("favorite")){
            jsonArray = checkCurrentFavorite(video);
        }else{
            jsonArray = getCurrentItem(video);
        }
        if(jsonArray!=null)
            if(jsonArray.length() > 0)
                return;
        ContentValues videoValues = new ContentValues();
        videoValues.put(COLUMN_ID, id);
        videoValues.put(COLUMN_VIDEO_DATA, video.getData());
        videoValues.put(COLUMN_VIDEO_DURATION, video.getDuration());
        videoValues.put(COLUMN_VIDEO_RESOLUTION, video.getResolution());
        videoValues.put(COLUMN_VIDEO_TITLE, video.getTitle());
        videoValues.put(COLUMN_RECORD_CATEGORY, category);
        id++;
//        SQLiteDatabase db = getWritableDatabase();
//        db.insert(TABLE_NAME, null, petValues);
//        db.close();
        if(!myDB.isOpen() || myDB==null) {
            openDB();
            Long result = myDB.insert(TABLE_NAME, null, videoValues);
//            Log.d("addPet", "DB Insert : " + result);
        }else{
            Long result = myDB.insert(TABLE_NAME, null, videoValues);
//            Log.d("addPet", "DB Insert : " + result);
        }
    }

    public void openDB(){
        myDB = getWritableDatabase();
    }

    public void closeDB(){
        if(myDB!=null && myDB.isOpen()){
            myDB.close();
        }
    }

    public JSONArray getCurrentItem(Video video){
        if(myDB==null || !myDB.isOpen()){
            openDB();
        }
        String searchQuery = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + COLUMN_VIDEO_TITLE + " = '" + video.getTitle() + "' " +
                " AND " + COLUMN_VIDEO_DATA + " = '" + video.getData() + "'";
        Cursor cursor = myDB.rawQuery(searchQuery, null);
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.getString(i) != null) {
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
//                        Log.d("TAG_NAME", e.getMessage());
                    } // End of try-catch
                } // End of if (cursor.getColumnName(i) != null)
            } // End of for-loop
            resultSet.put(rowObject);
            cursor.moveToNext();
        } // End of while-loop
        cursor.close();
        //Log.d("TAG_NAME", resultSet.toString());
        return resultSet;
    }

    public JSONArray checkCurrentFavorite(Video video){
        if(myDB==null || !myDB.isOpen()){
            openDB();
        }
        String searchQuery = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + COLUMN_VIDEO_TITLE + " = '" + video.getTitle() + "' " +
                " AND " + COLUMN_VIDEO_DATA + " = '" + video.getData() + "' " +
                " AND " + COLUMN_RECORD_CATEGORY + " = '"+ KEY_FAVORITE + "'";
        Cursor cursor = myDB.rawQuery(searchQuery, null);
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.getString(i) != null) {
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
//                        Log.d("TAG_NAME", e.getMessage());
                    } // End of try-catch
                } // End of if (cursor.getColumnName(i) != null)
            } // End of for-loop
            resultSet.put(rowObject);
            cursor.moveToNext();
        } // End of while-loop
        cursor.close();
        //Log.d("TAG_NAME", resultSet.toString());
        return resultSet;
    }

    public JSONArray getResults(String category) {
        if(myDB==null || !myDB.isOpen()){
            openDB();
        }
//        SQLiteDatabase myDataBase = getReadableDatabase();
        String searchQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_RECORD_CATEGORY + " LIKE '" +category +"' ORDER BY " + COLUMN_ID + " ASC";
        Cursor cursor = myDB.rawQuery(searchQuery, null);
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.getString(i) != null) {
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        //Log.d("TAG_NAME", e.getMessage());
                    } // End of try-catch
                } // End of if (cursor.getColumnName(i) != null)
            } // End of for-loop
            resultSet.put(rowObject);
            cursor.moveToNext();
        } // End of while-loop
        cursor.close();
        //Log.d("TAG_NAME", resultSet.toString());
        return resultSet;
    }

}
