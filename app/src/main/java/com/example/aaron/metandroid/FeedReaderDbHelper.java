package com.example.aaron.metandroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FeedReaderDbHelper extends SQLiteOpenHelper {
    private final Context context;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "FeedReader.db";

    public FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE processed_stop (\n" +
                "  id INTEGER PRIMARY KEY NOT NULL,\n" +
                "  objectId INTEGER NOT NULL,\n" +
                "  title CHARACTER VARYING NOT NULL,\n" +
                "  gallery INTEGER NOT NULL,\n" +
                "  image CHARACTER VARYING NOT NULL\n" +
                ")");
        db.execSQL("CREATE TABLE processed_media (\n" +
                "  id INTEGER PRIMARY KEY NOT NULL,\n" +
                "  stop INTEGER NOT NULL,\n" +
                "  position INTEGER NOT NULL,\n" +
                "  title CHARACTER VARYING NOT NULL,\n" +
                "  uri CHARACTER VARYING NOT NULL\n" +
                ")");
        try {
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(context.getResources().openRawResource(R.raw.met_public_processed_stop)))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        String[] matches = line.split(",");
                        ContentValues values = new ContentValues();
                        values.put("id", matches[0]);
                        values.put("objectId", matches[1]);
                        values.put("title", matches[2]);
                        values.put("gallery", matches[3]);
                        values.put("image", matches[4]);
                        db.insert("processed_stop", null, values);
                    }
                }
            }
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(context.getResources().openRawResource(R.raw.met_public_processed_media)))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        String[] matches = line.split(",");
                        ContentValues values = new ContentValues();
                        values.put("id", matches[0]);
                        values.put("stop", matches[1]);
                        values.put("position", matches[2]);
                        values.put("title", matches[3]);
                        if (matches.length == 5) {
                            values.put("uri", matches[4]);
                        }
                        db.insert("processed_media", null, values);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS processed_stop");
        db.execSQL("DROP TABLE IF EXISTS processed_media");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}