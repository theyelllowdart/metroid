package com.example.aaron.metandroid;

import android.graphics.Rect;
import android.graphics.RectF;

public class GalleryViewRect implements GalleryView{
    private final int id;
    private final RectF scaled;

    public GalleryViewRect(int id, float x1, float y1, float x2, float y2) {
        this.id = id;
        scaled = new RectF(x1, y1, x2, y2);
    }

    @Override
    public boolean contains(float x, float y) {
        return scaled.contains(x, y);
    }

    @Override
    public int getId() {
        return id;
    }

    public RectF getScaled() {
        return scaled;
    }
}
