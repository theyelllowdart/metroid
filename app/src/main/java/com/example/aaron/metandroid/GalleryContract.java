package com.example.aaron.metandroid;

import android.provider.BaseColumns;

public final class GalleryContract {
    public GalleryContract() {}

    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "gallery";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TITLE = "title";
    }


}