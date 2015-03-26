package com.dteviot.epubviewer.models;

import android.net.Uri;


/**
 * Created by vinhdo on 3/26/15.
 */
public class SearchResult {
    private Uri uri;
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public SearchResult(Uri uri, String title){
        this.uri = uri;
        this.title = title;
    }
}
