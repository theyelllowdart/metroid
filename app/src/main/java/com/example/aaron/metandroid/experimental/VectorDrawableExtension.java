package com.example.aaron.metandroid.experimental;

import android.graphics.drawable.VectorDrawable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class VectorDrawableExtension {
    private final VectorDrawable drawable;
    private static Method getTargetByName;
    private static Method setFillAlpha;

    static {
        try {
            getTargetByName = VectorDrawable.class.getDeclaredMethod("getTargetByName", String.class);
            getTargetByName.setAccessible(true);
            Class<?> vFullPathClass = Class.forName("android.graphics.drawable.VectorDrawable$VFullPath");
            setFillAlpha = vFullPathClass.getDeclaredMethod("setFillAlpha", float.class);
            setFillAlpha.setAccessible(true);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public VectorDrawableExtension(VectorDrawable drawable) {
        this.drawable = drawable;
    }

    public void setFillAlpha(String vFullPathName, Float opactiy) throws InvocationTargetException, IllegalAccessException {
        Object vFullPath = getTargetByName.invoke(drawable, vFullPathName);
        setFillAlpha.invoke(vFullPath, opactiy);
    }
}
