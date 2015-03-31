package com.dteviot.epubviewer.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import java.net.URI;

public class Bookmark{

    private String mFileName;
    private String mResourceUri;
    private float mScrollY;
    private String name;
    private int id;

    /*
     * ctor
     */
    public Bookmark(int id,String name, String resourceUri, float scrollY) {
        this.name = name;
        this.id  = id;
        mResourceUri = resourceUri;
        mScrollY = scrollY;
    }

    public Bookmark(){
        name = "";
        id = 0;
        mResourceUri = "";
        mScrollY = 0f;
    }

    /*
     * return true if bookmark is "empty", i.e. doesn't hold a useful value
     */
    public boolean isEmpty() {
        return ((mFileName == null) || (mFileName.length() <= 0) 
                || (mResourceUri == null));
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    public float getScrollY() {
        return mScrollY;
    }

    public void setScrollY(float mScrollY) {
        this.mScrollY = mScrollY;
    }

    public void setResourceUri(String mResourceUri) {
        this.mResourceUri = mResourceUri;
    }

    public String getResourceUri() {
        return mResourceUri;
    }
}
