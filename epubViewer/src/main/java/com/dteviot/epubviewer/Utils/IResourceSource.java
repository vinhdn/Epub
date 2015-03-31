package com.dteviot.epubviewer.Utils;

import android.net.Uri;

public interface IResourceSource {
    /*
     * Fetch the requested resource
     */
    public ResourceResponse fetch(Uri resourceUri);
}
