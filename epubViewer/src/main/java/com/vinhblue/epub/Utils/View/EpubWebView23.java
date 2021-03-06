package com.vinhblue.epub.Utils.View;

import java.io.StringWriter;

import org.xml.sax.helpers.XMLFilterImpl;
import org.xmlpull.v1.XmlSerializer;

import com.vinhblue.epub.models.Globals;
import com.vinhblue.epub.XmlFilter.InlineImageElementFilter;
import com.vinhblue.epub.XmlFilter.RemoveSvgElementFilter;
import com.vinhblue.epub.XmlFilter.StyleSheetElementFilter;
import com.vinhblue.epub.XmlFilter.XmlSerializerToXmlFilterAdapter;
import com.vinhblue.epub.Utils.XmlUtil;
import com.vinhblue.epub.epub.Book;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/*
 * EpubWebView for use with Android 2.3.
 * The 2.3 web view can't handle links in HTML
 * So we need to rebuild the HTML converting
 * any links into in-line data
 */
public class EpubWebView23 extends EpubWebView {

    public EpubWebView23(Context context) {
        super(context);
    }

    public EpubWebView23(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*
     *  Do any Web settings specific to the derived class 
     */
    protected void addWebSettings(WebSettings settings) {
        // nothing to do
    }
    
    /*
     * @ return Android version appropriate WebViewClient 
     */
    @Override
    protected WebViewClient createWebViewClient() {
        return new WebViewClient() {
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                onPageLoaded();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                onChapterChanged(url);
            }
        };
    }

    /*
     * Android 2.3 can't fetch linked resources in XHTML, 
     * so add use SAX pipeline to build new XHTML 
     * adding images as Data URIs and putting style sheets in-line
     */
    @Override
    protected void LoadUri(Uri uri) {
        // default to a fail message
        String xhtml =
                "<html><head /><body>Failed to convert XHTML</body></html>";
        try {
            // build SAX pipeline to convert XHTML
            // Chain is Reader -> stylesheetFilter -> imageFilter -> Serializer
            XMLFilterImpl stylesheetFilter = new StyleSheetElementFilter(uri, getBook());
            XMLFilterImpl svgFilter = new RemoveSvgElementFilter();
            XMLFilterImpl imageFilter = new InlineImageElementFilter(uri, getBook());
            svgFilter.setParent(stylesheetFilter);
            imageFilter.setParent(svgFilter);

            StringWriter writer = new StringWriter();
            XmlSerializer serializer = android.util.Xml.newSerializer();
            XmlSerializerToXmlFilterAdapter serializerFilter =
                    new XmlSerializerToXmlFilterAdapter(serializer);
            serializerFilter.setParent(imageFilter);

            // convert the XHTML
            serializer.setOutput(writer);
            XmlUtil.parseXmlResource(getBook().fetch(uri).getData(), stylesheetFilter,
                    serializerFilter);
            xhtml = writer.toString();
        } catch (Exception e) {
            Log.e(Globals.TAG, "Error generating XML ", e);
            // TODO construct error message HTML
        }

        // get WebView to show the XHTML
        String url = uri.toString();
        loadDataWithBaseURL(
                url,
                xhtml,
                "application/xhtml+xml",
                "UTF-8",
                null
        );
    }

    @Override
    protected void LoadUrl(String url) {
        int indexOf = url.indexOf('#');
        String temp = url;
        if (0 < indexOf) {
            temp = url.substring(0, indexOf);
        }
        Uri uri = Book.resourceName2Url(temp);
        LoadUri(uri);
    }
}
