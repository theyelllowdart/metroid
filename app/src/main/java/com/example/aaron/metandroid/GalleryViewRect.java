package com.example.aaron.metandroid;

import android.graphics.Rect;
import android.graphics.RectF;

public class GalleryViewRect implements GalleryView{
    private final int id;
    private final RectF original;
    private final RectF scaled;
    private final RectF transformed;

    public GalleryViewRect(int id, float x1, float y1, float x2, float y2, float density) {
        this.id = id;
        original = new RectF(x1, y1, x2, y2);
        scaled = new RectF(x1 * density, y1 * density, x2 * density, y2 * density);
        transformed = new RectF(scaled);
    }

    @Override
    public boolean contains(float x, float y) {
        return original.contains(x, y);
    }

    @Override
    public int getId() {
        return id;
    }

    public RectF getOriginal() {
        return original;
    }

    public RectF getScaled() {
        return scaled;
    }

    public RectF getTransformed() {
        return transformed;
    }
}
