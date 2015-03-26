package com.dteviot.epubviewer;

import com.dteviot.epubviewer.WebServer.FileRequestHandler;
import com.dteviot.epubviewer.WebServer.ServerSocketThread;
import com.dteviot.epubviewer.WebServer.WebServer;
import com.dteviot.epubviewer.epub.Book;
import com.dteviot.epubviewer.epub.TableOfContents;
import com.dteviot.epubviewer.models.SearchResult;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class MainActivity extends Activity implements IResourceSource, View.OnClickListener{
    private final static int LIST_EPUB_ACTIVITY_ID = 0; 
    private final static int LIST_CHAPTER_ACTIVITY_ID = 1; 
    private final static int CHECK_TTS_ACTIVITY_ID = 2; 
    
    public static final String BOOKMARK_EXTRA = "BOOKMARK_EXTRA";
    private Uri mCurrentUri;
    private RelativeLayout mControllerRL;
    private EbookApplication mApp;
    /*
     * the app's main view
     */
    private EpubWebView mEpubWebView;
    

    private ServerSocketThread mWebServerThread = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (EbookApplication) getApplication();

        //setContentView(R.layout.activity_main);
        Log.d("time 01", System.currentTimeMillis() +"");
        RelativeLayout view = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.activity_main,null);
        mControllerRL = (RelativeLayout)view.findViewById(R.id.controller);
        //mEpubWebView = (EpubWebView) findViewById(R.id.webview);
        mEpubWebView = createView();
        mEpubWebView.setApp(mApp);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //params.addRule(RelativeLayout.ABOVE, R.id.controller);



        view.addView(mEpubWebView, params);
        mControllerRL.bringToFront();
        setContentView(view);

        //setContentView(mEpubWebView);
        Log.d("time 02", System.currentTimeMillis() + "");
        new LoadEpubAsyncTask().execute();

        findViewById(R.id.next_btn).setOnClickListener(this);
        findViewById(R.id.previous_btn).setOnClickListener(this);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                loadEpub("",null);
//            }
//        });
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.menu_chapters:
            launchChaptersList();
            return true;
        case R.id.menu_bookmarks:
            mApp.getBook().search("A Family that Fights", new Book.SearchListener() {
                @Override
                public void onFinish(ArrayList<SearchResult> data) {

                }
            });
                return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void launchBookList() {
        Intent listComicsIntent = new Intent(this, ListEpubActivity.class);
        startActivityForResult(listComicsIntent, LIST_EPUB_ACTIVITY_ID);
    }

    private void launchChaptersList() {
        Book book = getBook(); 
        if (book == null) {
            Utility.showToast(this, R.string.no_book_selected);
        } else {
            TableOfContents toc = book.getTableOfContents();
            if (toc.size() == 0) {
                Utility.showToast(this, R.string.table_of_contents_missing);
            } else {
                Intent listChaptersIntent = new Intent(this, ListChaptersActivity.class);
                toc.pack(listChaptersIntent, ListChaptersActivity.CHAPTERS_EXTRA);
                startActivityForResult(listChaptersIntent, LIST_CHAPTER_ACTIVITY_ID);
            }
        }
    }

    private void launchBookmarkDialog() {
        BookmarkDialog dlg = new BookmarkDialog(this);
        dlg.show();
        dlg.setSetBookmarkAction(mSaveBookmark);
        dlg.setGotoBookmarkAction(mGotoBookmark);
        dlg.setStartSpeechAction(mStartSpeech);
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
                case LIST_EPUB_ACTIVITY_ID:
                    onListEpubResult(data);
                    break;

                case LIST_CHAPTER_ACTIVITY_ID:
                    onListChapterResult(data);
                    break;
                    
                default:
                    Utility.showToast(this, R.string.something_is_badly_broken);
            }
        } else if (resultCode == RESULT_CANCELED) {
            Utility.showErrorToast(this, data);
        }
    }

    private void onListEpubResult(Intent data) {
        String fileName = data.getStringExtra(ListEpubActivity.FILENAME_EXTRA);
        loadEpub(fileName, null);
    }

    private void onListChapterResult(Intent data) {
        Uri chapterUri = data.getParcelableExtra(ListChaptersActivity.CHAPTER_EXTRA);
        String url = data.getStringExtra(ListChaptersActivity.CHAPTER_URL);
        mEpubWebView.loadChapter(chapterUri);
        mCurrentUri = chapterUri;
//        mEpubWebView.loadChapterURL(url);
    }

    class LoadEpubAsyncTask extends AsyncTask<Void,Void,Void>{
        ProgressDialog mDialogLoading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("time 03", System.currentTimeMillis() +"");
            mDialogLoading = new ProgressDialog(MainActivity.this,ProgressDialog.STYLE_SPINNER);
            mDialogLoading.setIndeterminate(true);
            mDialogLoading.setMessage("Loading");
            mDialogLoading.setCancelable(false);
            mDialogLoading.show();

        }

        @Override
        protected Void doInBackground(Void... epubWebViews) {
            mEpubWebView.setBook("");
            mEpubWebView.loadChapter(mEpubWebView.getBook().firstChapter());
            mCurrentUri = getBook().firstChapter();
            mWebServerThread = createWebServer();
            mWebServerThread.startThread();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("time 04", System.currentTimeMillis() +"");
            Toast.makeText(MainActivity.this,"Load success",Toast.LENGTH_LONG).show();
            mDialogLoading.dismiss();
        }
    }

    private void loadEpub(String fileName, Uri chapterUri) {
        mEpubWebView.setBook(fileName);
        mEpubWebView.loadChapter(chapterUri);
        if(chapterUri == null){
            mCurrentUri = getBook().firstChapter();
        }else
            mCurrentUri = chapterUri;


    }
    
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        Bookmark bookmark = mEpubWebView.getBookmark();
        if (bookmark != null) {
            bookmark.save(outState);
        }
    }

    private IAction mSaveBookmark = new IAction() {
        public void doAction() {
            Bookmark bookmark = mEpubWebView.getBookmark();
            if (bookmark != null) {
                bookmark.saveToSharedPreferences(MainActivity.this);
            }
        }
    };

    private IAction mGotoBookmark = new IAction() {
        public void doAction() {
            mEpubWebView.gotoBookmark(new Bookmark(MainActivity.this));
        }
    };

    private IAction mStartSpeech = new IAction() {
        public void doAction() {
        }
    };
    

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebServerThread.stopThread();
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
                Uri uriPre = getBook().previousResource(mCurrentUri);
                if(uriPre != null) {
                    mEpubWebView.loadChapter(uriPre);
                    mCurrentUri = uriPre;
                }
                break;
            case R.id.next_btn:
                Uri uriNext= getBook().nextResource(mCurrentUri);
                if(uriNext != null) {
                    mEpubWebView.loadChapter(uriNext);
                    mCurrentUri = uriNext;
                }
                break;
            default:
                break;
        }
    }
}
