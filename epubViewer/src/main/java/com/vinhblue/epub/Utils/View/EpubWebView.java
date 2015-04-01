package com.vinhblue.epub.Utils.View;

import java.util.ArrayList;

import com.vinhblue.epub.EbookApplication;
import com.vinhblue.epub.R;
import com.vinhblue.epub.Utils.Utility;
import com.vinhblue.epub.epub.Book;
import com.vinhblue.epub.epub.NavPoint;

import android.content.Context;
import android.graphics.Picture;
import android.graphics.Rect;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/*
 * Holds the logic for the App's 
 * special WebView handling
 */
public abstract class EpubWebView extends WebView {

    public interface OnPageURLListener{
        public void onPageChange(String URL);
        public void onPageLoaded();
    }

    private OnPageURLListener mPageURLListener;

    private final static float FLING_THRESHOLD_VELOCITY = 200;

    /*
     * The book view will show
     */
    //private Book mBook;

    private GestureDetector mGestureDetector;
    
    /*
     * "root" page we're currently showing
     */
    private Uri mCurrentResourceUri;
    
    /*
     * Position of document
     */
    private float mScrollY = 0.0f; 

    /*
     * Note that we're loading from a bookmark
     */
    private boolean mScrollWhenPageLoaded = false;
    
    /*
     * The page, as text
     */
    private ArrayList<String> mText;

    private WebViewClient mWebViewClient;
    
    /*
     * Current line being spoken
     */
    private int mTextLine;

    /*
     * Pick an initial default
     */
    private float mFlingMinDistance = 320;

    /*
     * The total available area for drawing on
     */
    private Rect mRawScreenDimensions;

    private EbookApplication mApp;
    public EpubWebView(Context context) {
        this(context, null);
    }

    public EpubWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, mGestureListener);
        WebSettings settings = getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setPluginState(WebSettings.PluginState.ON_DEMAND);
        settings.setBuiltInZoomControls(true);
        addWebSettings(settings);
        setWebViewClient(mWebViewClient = createWebViewClient());
        setWebChromeClient(new WebChromeClient());
    }

    public void setApp(EbookApplication app){
        this.mApp = app;
    }

    /*
     * @ return Android version appropriate WebViewClient 
     */
    abstract protected WebViewClient createWebViewClient();
    
    /*
     *  Do any Web settings specific to the derived class 
     */
    abstract protected void addWebSettings(WebSettings settings);
    
    /*
     * Book to show
     */
    public void setBook(String fileName) {
        // if book already loaded, don't load again
        mApp.setBook(fileName);
    }

    public Book getBook() {
        return mApp.getBook();
    }
    
    protected WebViewClient getWebViewClient() {
        return mWebViewClient;
    }
    
    /*
     * Chapter of book to show
     */
    public void loadChapter(Uri resourceUri) {
        if (mApp.getBook() != null) {
            // if no chapter resourceName supplied, default to first one.
            if (resourceUri == null) {
                resourceUri = mApp.getBook().firstChapter();
            }
            if (resourceUri != null) {
                mCurrentResourceUri = resourceUri;
                // prevent cache, because WebSettings.LOAD_NO_CACHE doesn't always work.
                clearCache(false);
                LoadUri(resourceUri);
            }
        }
    }

    public void loadChapterURL(String resourceUrl){
        if (mApp.getBook() != null) {
            // if no chapter resourceName supplied, default to first one.
            if (resourceUrl == null) {
                resourceUrl = mApp.getBook().firstChapter().toString();
            }
            if (resourceUrl != null) {
                //mCurrentResourceUri = resourceUrl;
                // prevent cache, because WebSettings.LOAD_NO_CACHE doesn't always work.
                clearCache(false);
                LoadUrl(resourceUrl);
            }
        }
    }

    public void loadUri(Uri uri){
        LoadUri(uri);
    }

    /*
     * @ return load contents of URI into WebView,
     *   implementation is android version dependent 
     */
    protected abstract void LoadUri(Uri uri);
    protected abstract void LoadUrl(String url);
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event) ||
             super.onTouchEvent(event);
    }

    private OnClickListener onClickListener;

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        onClickListener = l;
    }

    public NavPoint getCurrentChapter(){
        NavPoint chapter = new NavPoint();
        for (int i = 0; i < getBook().getTableOfContents().size(); i++) {
            NavPoint nv = getBook().getTableOfContents().get(i);
            if(getUrl().equals(nv.getContent()))
                return nv;
        }
        return chapter;
    }

    public NavPoint getPositionOfUrl(String url){
        for (int i = 0; i < getBook().getTableOfContents().size(); i++) {
            NavPoint nv = getBook().getTableOfContents().get(i);
            if(url.equals(nv.getContent()))
                return nv;
        }
        return new NavPoint();
    }

    public void setOnPageURLListenner(OnPageURLListener listenner){
        this.mPageURLListener = listenner;
    }

    GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            // if no book, nothing to do
            if (mApp.getBook() == null) {
                return false;
            }
            
            // also ignore swipes that are vertical, too slow, or too short.
            float deltaX = event2.getX() - event1.getX();
            float deltaY = event2.getY() - event1.getY();
            
            if ((Math.abs(deltaX) < Math.abs(deltaY))
                    || (Math.abs(deltaX) < mFlingMinDistance)
                    || (Math.abs(velocityX) < FLING_THRESHOLD_VELOCITY)) {
                return false;
            } else {
                if (deltaX < 0) {
                    return changeChapter(mApp.getBook().nextResource(mCurrentResourceUri));
                } else {
                    return changeChapter(mApp.getBook().previousResource(mCurrentResourceUri));
                }
            }
        }

        /*
                         * If double tap at top/bottom fifth of screen, scroll page up/down
                         *
                         */
        @Override
        public boolean onDoubleTap (MotionEvent e) {
            float y = e.getY();
            if(onClickListener != null)
                onClickListener.onClick(EpubWebView.this);
            if (y <= mRawScreenDimensions.height() / 5) {
                if (!pageUp(false)) {
                	changeChapter(mApp.getBook().previousResource(mCurrentResourceUri));
                }
                return true;
            } else if (4 * mRawScreenDimensions.height() / 5 <= y) {
                if (!pageDown(false)) {
                	changeChapter(mApp.getBook().nextResource(mCurrentResourceUri));
                };
                return true;
            } else {
                return false;
            }
        }
    };

    private boolean changeChapter(Uri resourceUri) {
        if (resourceUri != null) {
            loadChapter(resourceUri);
            return true;
        } else {
            Utility.showToast(getContext(), R.string.end_of_book);
            return false;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRawScreenDimensions = new Rect(0, 0, w, h);
        mFlingMinDistance = w / 2;
    }

    /*
     * Called when page is loaded,
     * if we're scrolling to a bookmark, we need to set the
     * page size listener here.  Otherwise it can be called
     * for pages other than the one we're interested in 
     */
    @SuppressWarnings("deprecation")
    protected void onPageLoaded() {
        if(mPageURLListener != null)
            mPageURLListener.onPageLoaded();
        if (mScrollWhenPageLoaded) {
            setPictureListener(mPictureListener);
            mScrollWhenPageLoaded = false;
        }
    }

    protected void onChapterChanged(String url){
        if(mPageURLListener != null){
            mPageURLListener.onPageChange(url);
        }

    }

    /*
     * Need to wait until view has figured out how big web page is
     * Otherwise, we can't scroll to last position because 
     * getContentHeight() returns 0.
     * At current time, there is no replacement for PictureListener 
     */
    @SuppressWarnings("deprecation")
    private PictureListener mPictureListener = new PictureListener() {
        @Override
        @Deprecated
        public void onNewPicture(WebView view, Picture picture) {
            // stop listening 
            setPictureListener(null);
            
            scrollTo(0, (int)(getContentHeight() * mScrollY));
            mScrollY = 0.0f;
        }
    };
}
