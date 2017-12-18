package com.lu.richtexteditorlib.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lu.richtexteditorlib.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.os.Build.VERSION.SDK;

@SuppressWarnings({"unused"})
public abstract class RichEditor extends WebView {

    public enum Type {
        BOLD(0x06),
        ITALIC(0x07),
        STRIKETHROUGH(0x08),
        BLOCKQUOTE(0x09),
        H1(0x0a),
        H2(0x0b),
        H3(0x0c),
        H4(0x0d);

        //SUPERSCRIPT(1),//SUBSCRIPT(2),//UNDERLINE(3),
        private long typeCode;
        Type(long i) {
            typeCode = i;
        }

        public long getTypeCode() {
            return typeCode;
        }

        public boolean isMapTo(long id){
            return typeCode == id;
        }
    }

    public interface OnTextChangeListener {
        void onTextChange(String text);
    }

    public interface OnStateChangeListener {
        void onStateChangeListener(String text, List<Type> types);
    }

    public interface OnLinkClickListener{
        void onLinkClick(String linkName, String url);
    }

    public interface OnFocusChangeListener{
        void onFocusChange(boolean isFocus);
    }

    public interface AfterInitialLoadListener {
        void onAfterInitialLoad(boolean isReady);
    }

    public interface OnImageClickListener{
        void onImageClick(Long url);
    }

    public interface OnTextLengthChangeListener{
        void onTextLengthChange(long length);
    }

    private static final String SETUP_HTML = "file:///android_asset/editor.html";
    private static final String CALLBACK_SCHEME = "callback://";
    private static final String STATE_SCHEME = "state://";
    private static final String LINK_CHANGE_SCHEME = "change://";
    private static final String FOCUS_CHANGE_SCHEME = "focus://";
    private static final String IMAGE_CLICK_SCHEME = "image://";
    private boolean isReady = false;
    private String mContents;
    private long mContentLength;
    private OnTextChangeListener mTextChangeListener;
    private OnStateChangeListener mStateChangeListener;
    private AfterInitialLoadListener mLoadListener;
    private OnScrollChangedCallback mOnScrollChangedCallback;
    private OnLinkClickListener mOnLinkClickListener;
    private OnFocusChangeListener mOnFocusChangeListener;
    private OnImageClickListener mOnImageClickListener;
    private OnTextLengthChangeListener mOnTextLengthChangeListener;


    public RichEditor(Context context) {
        this(context, null);
    }

    public RichEditor(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    @SuppressLint({"SetJavaScriptEnabled","addJavascriptInterface"})
    public RichEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(isInEditMode())
            return;
        
        addJavascriptInterface(new Android4JsInterface(),"AndroidInterface");
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        setWebViewClient(createWebViewClient());
        setWebChromeClient(new WebChromeClient());
        mContentLength = 0;
        getSettings().setJavaScriptEnabled(true);
        load();
        //applyAttributes(context, attrs);
    }

    protected EditorWebViewClient createWebViewClient() {
        return new EditorWebViewClient();

    }

    protected void setOnTextChangeListener(OnTextChangeListener listener) {
        mTextChangeListener = listener;
    }

    public void setOnTextLengthChangeListener(OnTextLengthChangeListener onTextLengthChangeListener) {
        this.mOnTextLengthChangeListener = onTextLengthChangeListener;
    }

    protected void setOnDecorationChangeListener(OnStateChangeListener listener) {
        mStateChangeListener = listener;
    }

    protected void setOnInitialLoadListener(AfterInitialLoadListener listener) {
        mLoadListener = listener;
    }

    public void setOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
        this.mOnFocusChangeListener = onFocusChangeListener;
    }

    protected void setOnLinkClickListener(OnLinkClickListener onLinkClickListener) {
        this.mOnLinkClickListener = onLinkClickListener;
    }

    protected void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.mOnImageClickListener = onImageClickListener;
    }

    private void callback(String text) {
        mContents = text.replaceFirst(CALLBACK_SCHEME, "");
        if (mTextChangeListener != null) {
            mTextChangeListener.onTextChange(mContents);
        }
    }

    private void linkChangeCallBack(String text) {
        text = text.replaceFirst(LINK_CHANGE_SCHEME, "");
        String[] result = text.split("@_@");
        if (mOnLinkClickListener != null && result.length >= 2) {
            mOnLinkClickListener.onLinkClick(result[0],result[1]);
        }
    }

    private void imageClickCallBack(String url){
        if(mOnImageClickListener != null)
            mOnImageClickListener.onImageClick(Long.valueOf(url.replaceFirst(IMAGE_CLICK_SCHEME,"")));
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (mOnScrollChangedCallback != null) {
            mOnScrollChangedCallback.onScroll(l - oldl, t - oldt);
        }

    }

    public OnScrollChangedCallback getOnScrollChangedCallback() {
        return mOnScrollChangedCallback;
    }

    public void setOnScrollChangedCallback(
            final OnScrollChangedCallback onScrollChangedCallback) {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }

    /**
     * Impliment in the activity/fragment/view that you want to listen to the webview
     */
    @SuppressWarnings("WeakerAccess")
    public interface OnScrollChangedCallback {
        void onScroll(int dx, int dy);
    }


    public void stateCheck(String text) {

        String state = text.replaceFirst(STATE_SCHEME, "").toUpperCase(Locale.ENGLISH);
        List<Type> types = new ArrayList<>();
        for (Type type : Type.values()) {
            if (TextUtils.indexOf(state, type.name()) != -1) {
                types.add(type);
            }
        }

        if (mStateChangeListener != null) {
            mStateChangeListener.onStateChangeListener(state, types);
        }
    }

    public void getHtmlAsyn() {
        exec("javascript:RE.getHtml4Android()");
    }

    public String getHtml(){
        return mContents;
    }

    public void load(){
        Log.e("load","before load");
        loadUrl(SETUP_HTML);
        Log.e("load","after load");

    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        exec("javascript:RE.setPadding('" + left + "px', '" + top + "px', '" + right + "px', '" + bottom
                + "px');");
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        // still not support RTL.
        setPadding(start, top, end, bottom);
    }

    public void setEditorBackgroundColor(int color) {
        setBackgroundColor(color);
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
    }


    public void setPlaceholder(String placeholder) {
        exec("javascript:RE.setPlaceholder('" + placeholder + "');");
    }

    public void loadCSS(String cssFile) {
        String jsCSSImport = "(function() {" +
                "    var head  = document.getElementsByTagName(\"head\")[0];" +
                "    var link  = document.createElement(\"link\");" +
                "    link.rel  = \"stylesheet\";" +
                "    link.type = \"text/css\";" +
                "    link.href = \"" + cssFile + "\";" +
                "    link.media = \"all\";" +
                "    head.appendChild(link);" +
                "}) ();";
        exec("javascript:" + jsCSSImport + "");
    }

    public void undo() {
        exec("javascript:RE.exec('undo');");
    }

    public void redo() {
        exec("javascript:RE.exec('redo');");
    }

    public void setBold() {

        exec("javascript:RE.saveRange();");
        exec("javascript:RE.exec('bold');");
    }



    public void setItalic() {
        exec("javascript:RE.saveRange();");
        exec("javascript:RE.exec('italic');");
    }

    public void setStrikeThrough() {
        exec("javascript:RE.saveRange()");
        exec("javascript:RE.exec('strikethrough');");
    }

    public void setHeading(int heading, boolean b) {
        exec("javascript:RE.saveRange();");
        if (b){
            exec("javascript:RE.exec('h"+heading+"')");
        }else {
            exec("javascript:RE.exec('p')");
        }
    }

    public void setBlockquote(boolean b) {
        exec("javascript:RE.saveRange();");
        if(b){
            exec("javascript:RE.exec('blockquote')");
        }else {
            exec("javascript:RE.exec('p')");
        }
    }

    public void insertImage(String url,Long id, long width ,long height) {
        exec("javascript:RE.saveRange();");
        exec("javascript:RE.insertImage('" + url +"',"+ id + ", " + width + ","+ height + ");");
    }

    public void deleteImageById(Long id){
        exec("javascript:RE.saveRange();");
        exec("javascript:RE.removeImage("+id+");");
    }

    public void insertHr() {
        exec("javascript:RE.saveRange();");
        exec("javascript:RE.insertLine();");
    }


    public void insertLink(String href, String title) {
        exec("javascript:RE.saveRange();");
        exec("javascript:RE.insertLink('" + title + "', '" + href + "');");
    }

    public void changeLink(String href, String title) {
        exec("javascript:RE.saveRange();");
        exec("javascript:RE.changeLink('" + title + "', '" + href + "');");
    }

    public void insertTodo() {
        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.setTodo('" + Utils.getCurrentTime() + "');");
    }

    public void setImageUploadProcess(long id,int process){
        exec("javascript:RE.changeProcess("+ id +", "+ process +");");
    }

    public void setImageFailed(long id){
        exec("javascript:RE.uploadFailure("+ id +");");
    }

    public void setImageReload(long id){
        exec("javascript:RE.uploadReload("+ id +");");
    }

    public void focusEditor() {
        requestFocus();
    }

    public void clearFocusEditor() {
        exec("javascript:RE.blurFocus();");
    }

    protected void exec(final String trigger) {
        if (isReady) {
            load(trigger);
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    exec(trigger);
                }
            }, 100);
        }
    }

    private void load(String trigger) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(trigger, null);
        } else {
            loadUrl(trigger);
        }
    }

    protected class EditorWebVIewClient2 extends WebChromeClient{
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
            return super.onJsBeforeUnload(view, url, message, result);
        }
    }

    private class EditorWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            isReady = url.equalsIgnoreCase(SETUP_HTML);
            Log.e("load","after onPageFinished");

            if (mLoadListener != null) {
                mLoadListener.onAfterInitialLoad(isReady);
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            String decode;
            try {
                decode = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // No handling
                return false;
            }

            Log.e("decode",decode);

            if (TextUtils.indexOf(url, CALLBACK_SCHEME) == 0) {
                callback(decode);
                return true;
            } else if (TextUtils.indexOf(url, STATE_SCHEME) == 0) {
                stateCheck(decode);
                return true;
            }
            if(TextUtils.indexOf(url,LINK_CHANGE_SCHEME) == 0){
                linkChangeCallBack(decode);
                return true;
            }

            if(TextUtils.indexOf(url,IMAGE_CLICK_SCHEME) == 0){
                imageClickCallBack(decode);
                return true;
            }

            return super.shouldOverrideUrlLoading(view, url);
        }

    }

    public long getContentLength() {
        return mContentLength;
    }

    private class Android4JsInterface {
        @JavascriptInterface
        public void setViewEnabled(boolean enabled){
            if(mOnFocusChangeListener != null)
                mOnFocusChangeListener.onFocusChange(enabled);
        }
        @JavascriptInterface
        public void setHtmlContent(String htmlContent){
            mContents = htmlContent;
            if(mTextChangeListener != null)
                mTextChangeListener.onTextChange(htmlContent);
        }

        @JavascriptInterface
        public void staticWords(long num){
            mContentLength = num;
            if(mOnTextLengthChangeListener != null)
                mOnTextLengthChangeListener.onTextLengthChange(num);
        }
    }
}