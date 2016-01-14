package com.vinhblue.epub.Activity;

import com.vinhblue.epub.EbookApplication;
import com.vinhblue.epub.dialog.SearchDialog;
import com.vinhblue.epub.models.Globals;
import com.vinhblue.epub.Utils.IResourceSource;
import com.vinhblue.epub.R;
import com.vinhblue.epub.Utils.ResourceResponse;
import com.vinhblue.epub.Utils.Utility;
import com.vinhblue.epub.Utils.SAutoBgImageButton;
import com.vinhblue.epub.Utils.View.EpubWebView;
import com.vinhblue.epub.Utils.View.EpubWebView23;
import com.vinhblue.epub.Utils.View.EpubWebView30;
import com.vinhblue.epub.WebServer.FileRequestHandler;
import com.vinhblue.epub.WebServer.ServerSocketThread;
import com.vinhblue.epub.WebServer.WebServer;
import com.vinhblue.epub.database.dao.MyDAOFactory;
import com.vinhblue.epub.dialog.ChapterDialog;
import com.vinhblue.epub.dialog.LoadingDialog;
import com.vinhblue.epub.epub.Book;
import com.vinhblue.epub.epub.NavPoint;
import com.vinhblue.epub.models.Bookmark;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class MainActivity extends FragmentActivity implements IResourceSource, View.OnClickListener, EpubWebView.OnPageURLListener{
    private final static int LIST_EPUB_ACTIVITY_ID = 0; 
    private final static int LIST_CHAPTER_ACTIVITY_ID = 1; 
    private final static int CHECK_TTS_ACTIVITY_ID = 2;
    private final static int SEARCH_ACTIVITY_ID = 3;

    
    public static final String BOOKMARK_EXTRA = "BOOKMARK_EXTRA";
    private Uri mCurrentUri;
    private String mCurrentUrl;
    private NavPoint mCurChapter;
    private RelativeLayout mControllerRL,mTitle;
    private EbookApplication mApp;
    private int scrollYWeb = 0;
    private String mCurrentContentSearch = "";
    /*
     * the app's main view
     */
    private EpubWebView mEpubWebView;
    private SAutoBgImageButton mBookmarkBtn;
    

    private ServerSocketThread mWebServerThread = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (EbookApplication) getApplication();

        RelativeLayout view = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.activity_main,null);
        RelativeLayout content =(RelativeLayout) view.findViewById(R.id.book_content);
        mControllerRL = (RelativeLayout)view.findViewById(R.id.controller);
        mEpubWebView = createView();
        mEpubWebView.setApp(mApp);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        content.addView(mEpubWebView, params);
        mControllerRL.bringToFront();
        setContentView(view);
        new LoadEpubAsyncTask().execute();
        initViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCurChapter != null && mEpubWebView != null && mApp != null) {
            Log.d("Scale", mEpubWebView.getScale() + "   " + mEpubWebView.getScrollY());
            mApp.setLastReading(mCurChapter.getContent(), (int)(mEpubWebView.getScrollY() * 2.5f / mEpubWebView.getScale()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initViews(){
        mTitle = (RelativeLayout) findViewById(R.id.title);
        findViewById(R.id.next_btn).setOnClickListener(this);
        findViewById(R.id.previous_btn).setOnClickListener(this);
        findViewById(R.id.home_btn).setOnClickListener(this);
        findViewById(R.id.chapter_btn).setOnClickListener(this);
        findViewById(R.id.search_btn).setOnClickListener(this);
        findViewById(R.id.show_bookmark_btn).setOnClickListener(this);
        findViewById(R.id.book_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Book Content","OnClick::");
                if(mControllerRL.getVisibility() != View.GONE)
                    mControllerRL.setVisibility(View.GONE);
                else
                    mControllerRL.setVisibility(View.VISIBLE);
            }
        });
        mBookmarkBtn = (SAutoBgImageButton) findViewById(R.id.bookmark_btn);
        mBookmarkBtn.setOnClickListener(this);
        mEpubWebView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("EpubWebView", "OnClick::");
                if (mControllerRL.getVisibility() != View.GONE) {
                    mControllerRL.setVisibility(View.GONE);
                    mTitle.setVisibility(View.GONE);
                } else {
                    mControllerRL.setVisibility(View.VISIBLE);
                    mTitle.setVisibility(View.VISIBLE);
                }
            }
        });
        mEpubWebView.setOnPageURLListenner(this);
        mEpubWebView.getScrollY();
        initData();
    }

    private void initData(){

    }

    private ServerSocketThread createWebServer() {
        FileRequestHandler handler = new FileRequestHandler(this);
        WebServer server = new WebServer(handler);
        return new ServerSocketThread(server, Globals.WEB_SERVER_PORT);
    }
    
    private EpubWebView createView() {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
            return new EpubWebView23(this);
        } else {
            return new EpubWebView30(this);
        }
    }

    /*
     * Should return with epub or chapter to load
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHECK_TTS_ACTIVITY_ID) {
            return;
        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SEARCH_ACTIVITY_ID:
                    onSearchResult(data);
                    break;
                default:
                    Utility.showToast(this, R.string.something_is_badly_broken);
            }
        } else if (resultCode == RESULT_CANCELED) {
            Utility.showErrorToast(this, data);
        }
    }

    private void onSearchResult(Intent data){
        String uri = data.getStringExtra("URI");
        String searchText = data.getStringExtra("SearchText");
        if(!uri.equals("") && uri.length() > 0){
            mCurrentContentSearch = searchText;
            mEpubWebView.loadChapter(Uri.parse(uri));
        }
    }

    @Override
    public void onPageChange(String URL) {
        mCurChapter = mEpubWebView.getPositionOfUrl(URL);
        Log.d("CurrentChapter","ID: " + mCurChapter.getPlayOrder() + " ContentURL: " + mCurChapter.getContent());
        if(mCurChapter.getPlayOrder() > 0){
            if(MyDAOFactory.instance(this).getBookmarkDAO().isSaved(mCurChapter.getPlayOrder())){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBookmarkBtn.setBackgroundResource(R.drawable.saved_bookmarkx180);
                    }
                });
            }else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBookmarkBtn.setBackgroundResource(R.drawable.bookmarkx180);
                    }
                });
            }
        }
        mCurrentUri = Uri.parse(URL);
        mCurrentUrl = URL;
    }

    @Override
    public void onPageLoaded() {
        Log.d("ScrollY", scrollYWeb + "");
        if(scrollYWeb > 0){
            mEpubWebView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mEpubWebView.scrollTo(0,scrollYWeb);
                    scrollYWeb = 0;
                }
            },100);

        }
        if(!mCurrentContentSearch.equals("") && mCurrentContentSearch.length() >= 3){
            mEpubWebView.findAll(mCurrentContentSearch);
            mEpubWebView.findFocus();
            mCurrentContentSearch = "";
        }

    }

    class LoadEpubAsyncTask extends AsyncTask<Void,Void,Void>{
        LoadingDialog mDialogLoading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("time 03", System.currentTimeMillis() +"");
            mDialogLoading = LoadingDialog.newInstance(MainActivity.this,getResources().getString(R.string.loading_book));
            mDialogLoading.setCancelable(false);
            mDialogLoading.show(getSupportFragmentManager(),"Loading dialog");
        }

        @Override
        protected Void doInBackground(Void... epubWebViews) {
            mEpubWebView.setBook("fuckyou69");
            final Bookmark bookmark = mApp.getLastReading();
            if(bookmark.getResourceUri().equals("")) {
              mEpubWebView.post(new Runnable() {
                  @Override
                  public void run() {
                      mEpubWebView.loadChapter(mEpubWebView.getBook().firstChapter());
                  }
              }) ;
            }
            else {
                mEpubWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mEpubWebView.loadChapter(Uri.parse(bookmark.getResourceUri()));
                    }
                });

                scrollYWeb = (int)bookmark.getScrollY();
            }
//            mCurrentUri = getBook().firstChapter();
            mWebServerThread = createWebServer();
            mWebServerThread.startThread();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mDialogLoading.dismiss();
        }
    }

    private void loadEpub(String fileName, Uri chapterUri) {
        mEpubWebView.setBook(fileName);
        mEpubWebView.loadChapter(chapterUri);
//        if(chapterUri == null){
//            mCurrentUri = getBook().firstChapter();
//        }else
//            mCurrentUri = chapterUri;


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mWebServerThread != null)
        mWebServerThread.stopThread();
        if(mCurChapter != null && mEpubWebView != null && mApp != null) {
            Log.d("Scale", mEpubWebView.getScale() + "   " + mEpubWebView.getScrollY());
            mApp.setLastReading(mCurChapter.getContent(), (int)(mEpubWebView.getScrollY() * 2.5f / mEpubWebView.getScale()));
        }
    }

    /*
     * Book currently being used.
     * (Hack to provide book to WebServer.)
     */
    public Book getBook() { 
        return mEpubWebView.getBook(); 
    }


    @Override
    public ResourceResponse fetch(Uri resourceUri) {
        return getBook().fetch(resourceUri);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.previous_btn:
                Uri uriPre = Uri.parse(getBook().previousResource(mCurChapter.getContent()));
                if(uriPre != null && !uriPre.toString().equals("")) {
                    mEpubWebView.loadChapter(uriPre);
//                    mCurrentUri = uriPre;
                }
                break;
            case R.id.next_btn:
                Uri uriNext= Uri.parse(getBook().nextResource(mCurChapter.getContent()));
                if(uriNext != null && !uriNext.toString().equals("")) {
                    mEpubWebView.loadChapter(uriNext);
//                    mCurrentUri = uriNext;
                }
                break;
            case R.id.home_btn:
                finish();
                break;
            case R.id.chapter_btn:
                ChapterDialog dialog = ChapterDialog.newInstance(this,getBook().getTableOfContents(),mCurChapter.getPlayOrder(),new ChapterDialog.OnItemClicked() {
                    @Override
                    public void onClicked(NavPoint item) {
                        Log.d("Item Clicked",item.getNavLabel());
                        Uri uri = item.getContentWithoutTag();
                        mEpubWebView.loadChapter(uri);
                    }
                });
                dialog.show(getSupportFragmentManager(),"Chapter Dialog");
                break;
            case R.id.search_btn:
//                Intent intent = new Intent(this, SearchActivity.class);
//                startActivityForResult(intent, SEARCH_ACTIVITY_ID);
                SearchDialog dialogSearch = SearchDialog.newInstance(this, new SearchDialog.OnItemSearchClick() {
                    @Override
                    public void onClick(String uri, String searchText) {
                        if(!uri.equals("") && uri.length() > 0){
                            mCurrentContentSearch = searchText;
                            mEpubWebView.loadChapter(Uri.parse(uri));
                        }
                    }
                });
                dialogSearch.show(getSupportFragmentManager(),"Search Dialog");
                break;
            case R.id.bookmark_btn:
                if(mCurChapter.getPlayOrder() > 0) {
                    if(!MyDAOFactory.instance(this).getBookmarkDAO().isSaved(mCurChapter.getPlayOrder())) {
                        MyDAOFactory.instance(this).getBookmarkDAO().insertRow(new Bookmark(mCurChapter.getPlayOrder(), mCurChapter.getNavLabel(), mCurChapter.getContent(), mEpubWebView.getScrollY()));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mBookmarkBtn.setBackgroundResource(R.drawable.saved_bookmarkx180);
                            }
                        });
                    }else{
                        MyDAOFactory.instance(this).getBookmarkDAO().deleteRow(mCurChapter.getPlayOrder());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mBookmarkBtn.setBackgroundResource(R.drawable.bookmarkx180);
                            }
                        });
                    }
                }
                break;
            case R.id.show_bookmark_btn:
                com.vinhblue.epub.dialog.BookmarkDialog dialogBookmark = com.vinhblue.epub.dialog.BookmarkDialog.newInstance(this, mCurChapter.getPlayOrder(), new com.vinhblue.epub.dialog.BookmarkDialog.OnItemClicked() {
                    @Override
                    public void onClicked(Bookmark item) {
                        Uri uri = Uri.parse(item.getResourceUri());
                        mEpubWebView.loadChapter(uri);
                    }
                });
                dialogBookmark.show(getSupportFragmentManager(),"Bookmark Dialog");
                break;
        }
    }
}
