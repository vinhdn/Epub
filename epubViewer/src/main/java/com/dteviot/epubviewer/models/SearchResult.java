package com.dteviot.epubviewer.models;

import android.net.Uri;


/**
 * Created by vinhdo on 3/26/15.
 */
public class SearchResult {
    private Uri uri = Uri.EMPTY;
    private String title = "";
    private String previewContent = "";

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

    public SearchResult(Uri uri, String title,String previewContent){
        this.uri = uri;
        this.title = title;
        this.previewContent = previewContent;
    }

    public String getPreviewContent() {
        return previewContent;
    }

    public void setPreviewContent(String previewContent) {
        this.previewContent = previewContent;
    }
}
