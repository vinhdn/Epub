package com.dteviot.epubviewer.epub;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xmlpull.v1.XmlSerializer;

import com.dteviot.epubviewer.EpubWebView;
import com.dteviot.epubviewer.EpubWebView23;
import com.dteviot.epubviewer.EpubWebView30;
import com.dteviot.epubviewer.Globals;
import com.dteviot.epubviewer.HrefResolver;
import com.dteviot.epubviewer.IResourceSource;
import com.dteviot.epubviewer.ResourceResponse;
import com.dteviot.epubviewer.Utility;
import com.dteviot.epubviewer.XmlFilter.InlineImageElementFilter;
import com.dteviot.epubviewer.XmlFilter.RemoveSvgElementFilter;
import com.dteviot.epubviewer.XmlFilter.StyleSheetElementFilter;
import com.dteviot.epubviewer.XmlFilter.XmlSerializerToXmlFilterAdapter;
import com.dteviot.epubviewer.XmlUtil;
import com.dteviot.epubviewer.models.SearchResult;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.sax.Element;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/*
 * Represents a book that's been packed into an epub file
 */
public class Book implements IResourceSource {
    private final static String HTTP_SCHEME = "http";
    
    // the container XML
    private static final String XML_NAMESPACE_CONTAINER = "urn:oasis:names:tc:opendocument:xmlns:container";
    private static final String XML_ELEMENT_CONTAINER = "container";
    private static final String XML_ELEMENT_ROOTFILES = "rootfiles";
    private static final String XML_ELEMENT_ROOTFILE = "rootfile";
    private static final String XML_ATTRIBUTE_FULLPATH = "full-path";
    private static final String XML_ATTRIBUTE_MEDIATYPE = "media-type";

    // the .opf XML
    private static final String XML_NAMESPACE_PACKAGE = "http://www.idpf.org/2007/opf";
    private static final String XML_ELEMENT_PACKAGE = "package";
    private static final String XML_ELEMENT_MANIFEST = "manifest";
    private static final String XML_ELEMENT_MANIFESTITEM = "item";
    private static final String XML_ELEMENT_SPINE = "spine";
    private static final String XML_ATTRIBUTE_TOC = "toc"; 
    private static final String XML_ELEMENT_ITEMREF = "itemref";
    private static final String XML_ATTRIBUTE_IDREF = "idref";
    private Context mContext;
    
    /*
     * The zip archive
     */
    private ZipFile mZip;
    
    /*
     * Name of the ".opf" file in the zip archive
     */
    private String mOpfFileName;
    
    /*
     * Id of the "table of contents" entry in manifest
     */
    private String mTocID;
    
    // Allow access to state for unit tests.
    public String getOpfFileName() { return mOpfFileName; }
    public String getTocID() { return mTocID; }
    public ArrayList<ManifestItem> getSpine() { return mSpine; }
    public Manifest getManifest() { return mManifest; }
    public TableOfContents getTableOfContents() { return mTableOfContents; }

    /*
     *  The resources that are in the spine element of the metadata.
     */
    private ArrayList<ManifestItem> mSpine;
    
    /*
     *  The manifest entry in the metadata.
     */
    private Manifest mManifest;
    
    /*
     *  The Table of Contents in the metadata.
     */
    private TableOfContents mTableOfContents;
    
    /*
     * Intended for unit testing
     */
    public Book() {
        mSpine = new ArrayList<ManifestItem>();
        mManifest = new Manifest();
        mTableOfContents = new TableOfContents();
    }
    
    /*
     * Constructor
     * @param fileName the filename of the Zip archive file
     */
    public Book(String fileName) {
        mSpine = new ArrayList<ManifestItem>();
        mManifest = new Manifest();
        mTableOfContents = new TableOfContents();
        try {
            mZip = new ZipFile(fileName);
            parseEpub();
        } catch (IOException e) {
            Log.e(Globals.TAG, "Error opening file", e);
        }
    }

    public Book(String fileName, Context activity) throws Throwable {
        mContext = activity;
        InputStream is = activity.getAssets().open("Breaking the Silence.dev");
        String key = "fuckyou69";
        DESKeySpec dks = new DESKeySpec(key.getBytes());
        SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
        SecretKey desKey = skf.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, desKey);

        	byte[] encry = new byte[is.available()];
			is.read(encry);
			byte[] deCry = cipher.doFinal(encry);
			 //System.out.println("Decrypted Text: " + new String(deCry));
        //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
        File file = File.createTempFile("fifi.fuf",null,activity.getCacheDir());
        //File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"fifi.fuf");
        //file.createNewFile();
        FileOutputStream fo = new FileOutputStream(file);
        fo.write(deCry);
        fo.flush();
        fo.close();

        mSpine = new ArrayList<ManifestItem>();
        mManifest = new Manifest();
        mTableOfContents = new TableOfContents();
        try {
            //mZip = new ZipFile(fileName);
            mZip = new ZipFile(file);
            file.delete();
            parseEpub();
        } catch (IOException e) {
            Log.e(Globals.TAG, "Error opening file", e);
        }
    }

    /*
     * Name of zip file
     */
    public String getFileName() {
        return (mZip == null) ? null : mZip.getName();
    }
    
    /*
     * Fetch file from zip
     */
    private InputStream fetchFromZip(String fileName) {
        InputStream in = null;
        ZipEntry containerEntry = mZip.getEntry(fileName);
        if (containerEntry != null) {
            try {
                in = mZip.getInputStream(containerEntry);
            } catch (IOException e) {
                Log.e(Globals.TAG, "Error reading zip file " + fileName, e);
            }
        }

        if (in == null) {
            Log.e(Globals.TAG, "Unable to find file in zip: " + fileName);
        }
        
        return in;
    }
    
    /*
     * Fetch resource from ebook
     */
    public ResourceResponse fetch(Uri resourceUri) {
        String resourceName = url2ResourceName(resourceUri);        
        ManifestItem item = mManifest.findByResourceName(resourceName);
        if (item != null) {
            ResourceResponse response = new ResourceResponse(item.getMediaType(), 
                    fetchFromZip(resourceName));
            response.setSize(mZip.getEntry(resourceName).getSize());
            return response;
        }

        // if get here, something went wrong  
        Log.e(Globals.TAG, "Unable to find resource in ebook " + resourceName);
        return null;
    }
    
    public Uri firstChapter() {
        return 0 < mSpine.size() ? resourceName2Url(mSpine.get(0).getHref()) : null; 
    }
    
    /*
     * @return URI of next resource in sequence, or null if not one
     */
    public Uri nextResource(Uri resourceUri) {
        String resourceName = url2ResourceName(resourceUri);
        for (int i = 0; i < mSpine.size() - 1; ++i) {
            if (mSpine.get(i).getHref().equals(resourceName)) {
                return resourceName2Url(mSpine.get(i + 1).getHref()); 
            }
        }
        // if get here, not found
        return null;
    }
    
    /*
     * @return URI of previous resource in sequence, or null if not one
     */
    public Uri previousResource(Uri resourceUri) {
        String resourceName = url2ResourceName(resourceUri);
        for (int i = 1; i < mSpine.size(); ++i) {
            if (mSpine.get(i).getHref().equals(resourceName)) {
                return resourceName2Url(mSpine.get(i - 1).getHref()); 
            }
        }
        // if get here not found
        return null;
    }
    
    /*
     * Build up structure of epub
     */
    private void parseEpub() {
        // clear everything
        mOpfFileName = null;
        mTocID = null;
        mSpine.clear();
        mManifest.clear();
        mTableOfContents.clear();
        
        // get the "container" file, this tells us where the ".opf" file is
        parseXmlResource("META-INF/container.xml", constructContainerFileParser());

        if (mOpfFileName != null) {
            parseXmlResource(mOpfFileName, constructOpfFileParser());
        }

        if (mTocID != null) {
            ManifestItem tocManifestItem = mManifest.findById(mTocID);
            if (tocManifestItem != null) {
                String tocFileName = tocManifestItem.getHref();
                HrefResolver resolver = new HrefResolver(tocFileName);
                parseXmlResource(tocFileName, mTableOfContents.constructTocFileParser(resolver));
            }
        }
    }

    private void parseXmlResource(String fileName, ContentHandler handler) {
        InputStream in = fetchFromZip(fileName);
        if (in != null) {
            XmlUtil.parseXmlResource(in, handler, null);
        }
    }
    
    /*
     * build parser to parse the container file,
     * i.e. get the name of the ".opf" file in the zip.
     * @return parser
     */
    public ContentHandler constructContainerFileParser() {
        // describe the relationship of the elements
        RootElement root = new RootElement(XML_NAMESPACE_CONTAINER, XML_ELEMENT_CONTAINER);
        Element rootfiles = root.getChild(XML_NAMESPACE_CONTAINER, XML_ELEMENT_ROOTFILES);
        Element rootfile = rootfiles.getChild(XML_NAMESPACE_CONTAINER, XML_ELEMENT_ROOTFILE);
        
        rootfile.setStartElementListener(new StartElementListener(){
            public void start(Attributes attributes) {
                String mediaType = attributes.getValue(XML_ATTRIBUTE_MEDIATYPE);
                if ((mediaType != null) && mediaType.equals("application/oebps-package+xml")) {
                    mOpfFileName = attributes.getValue(XML_ATTRIBUTE_FULLPATH); 
                }
            }
        });
        return root.getContentHandler();
    }
    
    /*
     * build parser to parse the ".opf" file,
     * @return parser
     */
    public ContentHandler constructOpfFileParser() {
        // describe the relationship of the elements
        RootElement root = new RootElement(XML_NAMESPACE_PACKAGE, XML_ELEMENT_PACKAGE);
        Element manifest = root.getChild(XML_NAMESPACE_PACKAGE, XML_ELEMENT_MANIFEST);
        Element manifestItem = manifest.getChild(XML_NAMESPACE_PACKAGE, XML_ELEMENT_MANIFESTITEM);
        Element spine = root.getChild(XML_NAMESPACE_PACKAGE, XML_ELEMENT_SPINE);
        Element itemref = spine.getChild(XML_NAMESPACE_PACKAGE, XML_ELEMENT_ITEMREF);

        final HrefResolver resolver = new HrefResolver(mOpfFileName);
        manifestItem.setStartElementListener(new StartElementListener(){
            public void start(Attributes attributes) {
                mManifest.add(new ManifestItem(attributes, resolver));
            }
        });
        
        // get name of Table of Contents file from the Spine
        spine.setStartElementListener(new StartElementListener(){
            public void start(Attributes attributes) {
                mTocID = attributes.getValue(XML_ATTRIBUTE_TOC); 
            }
        });
        
        itemref.setStartElementListener(new StartElementListener(){
            public void start(Attributes attributes) {
                String temp = attributes.getValue(XML_ATTRIBUTE_IDREF);
                if (temp != null) {
                    ManifestItem item = mManifest.findById(temp);
                    if (item != null) {
                        mSpine.add(item);
                    }
                }
            }
        });
        return root.getContentHandler();
    }
    
    /*
     * @param url used by WebView
     * @return resourceName used by zip file
     */
    private static String url2ResourceName(Uri url) {
        // we only care about the path part of the URL
        String resourceName = url.getPath();
        
        // if path has a '/' prepended, strip it
        if (resourceName.charAt(0) == '/') {
            resourceName = resourceName.substring(1);
        }
        return resourceName;
    }
    
    /*
     * @param resourceName used by zip file
     * @return URL used by WebView 
     */
    public static Uri resourceName2Url(String resourceName) {
        // build path assuming local file.
        // pack resourceName into path section of a file URI
        // need to leave '/' chars in path, so WebView is aware
        // of path to current resource, so it can can correctly resolve
        // path of any relative URLs in the current resource.
        return new Uri.Builder().scheme(HTTP_SCHEME)
                .encodedAuthority("localhost:" + Globals.WEB_SERVER_PORT)
                .appendEncodedPath(Uri.encode(resourceName, "/"))
                .build();
    }

    public class SearchAsync extends AsyncTask<String,Void,ArrayList<SearchResult>>{

        @Override
        protected ArrayList<SearchResult> doInBackground(String... strings) {
            return searchAsync(strings[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<SearchResult> searchResults) {
            super.onPostExecute(searchResults);
            if(mSearchListener != null)
            mSearchListener.onFinish(searchResults);
        }
    }

    private SearchListener mSearchListener;
    public void search(String str, SearchListener mSearchListener){
        this.mSearchListener = mSearchListener;
        new SearchAsync().execute(str);
    }
    private ArrayList<SearchResult> searchAsync(String str){
        ArrayList<SearchResult> results = new ArrayList<>();
        for (int i = 0; i < mSpine.size(); ++i) {
            Uri uri = resourceName2Url(mSpine.get(i).getHref());
//            Log.d("Search ID", "URI: " + uri.toString() + " /n ID: " + mSpine.get(i).getID());
//            webView.loadUrl(uri.toString());
            String data = getDataOfUri(uri);

            Document doc = Jsoup.parse(data);
            String text = doc.body().text();
            Log.d("TEXT: " ,i + " data " + text);
            if(text.indexOf(str) >= 0){
                Log.d("Search ID", "URI: " + doc.title() + " /n ID: " + mSpine.get(i).getID());
                SearchResult result = new SearchResult(uri,doc.title());
            }

        }
        return results;
    }

    private EpubWebView createView() {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
            return new EpubWebView23(mContext);
        } else {
            return new EpubWebView30(mContext);
        }
    }

    private String getDataOfUri(Uri uri){
//        try {
//            // build SAX pipeline to convert XHTML
//            // Chain is Reader -> stylesheetFilter -> imageFilter -> Serializer
//            XMLFilterImpl stylesheetFilter = new StyleSheetElementFilter(uri, this);
//            XMLFilterImpl svgFilter = new RemoveSvgElementFilter();
//            XMLFilterImpl imageFilter = new InlineImageElementFilter(uri, this);
//            svgFilter.setParent(stylesheetFilter);
//            imageFilter.setParent(svgFilter);
//
//            StringWriter writer = new StringWriter();
//            XmlSerializer serializer = android.util.Xml.newSerializer();
//            XmlSerializerToXmlFilterAdapter serializerFilter =
//                    new XmlSerializerToXmlFilterAdapter(serializer);
//            serializerFilter.setParent(imageFilter);
//
//            // convert the XHTML
//            serializer.setOutput(writer);
//            XmlUtil.parseXmlResource(this.fetch(uri).getData(), stylesheetFilter,
//                    serializerFilter);
//            //Log.d("data URI","URI: " + uri.toString() + " /n " + writer.toString());
//            return writer.toString();
//        } catch (Exception e) {
//            Log.e(Globals.TAG, "Error generating XML ", e);
//            // TODO construct error message HTML
//            return "";
//        }

        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(fetch(uri).getData()));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            return total.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public interface SearchListener{
        public void onFinish(ArrayList<SearchResult> data);
    }
}
