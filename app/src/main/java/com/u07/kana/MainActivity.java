package com.u07.kana;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String LOCAL_HOST = "u07.local";
    private WebView web;
    private TextToSpeech tts;
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(0xFFFFFFFF);
        getWindow().setNavigationBarColor(0xFFFFFFFF);
        getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS && tts != null) tts.setLanguage(Locale.JAPANESE);
        });
        web = new WebView(this);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                android.net.Uri uri = request.getUrl();
                if (!"https".equals(uri.getScheme()) || !LOCAL_HOST.equals(uri.getHost())) return null;
                String path = uri.getPath();
                if (path == null || path.equals("/")) path = "/index.html";
                if (path.contains("..") || path.contains("\\")) return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", null, new java.io.ByteArrayInputStream(new byte[0]));
                String asset = path.substring(1);
                String mime = "application/octet-stream";
                if (asset.endsWith(".html")) mime = "text/html";
                else if (asset.endsWith(".js")) mime = "application/javascript";
                else if (asset.endsWith(".css")) mime = "text/css";
                else if (asset.endsWith(".svg")) mime = "image/svg+xml";
                else if (asset.endsWith(".ogg")) mime = "audio/ogg";
                else if (asset.endsWith(".png")) mime = "image/png";
                try {
                    InputStream stream = getAssets().open(asset);
                    return new WebResourceResponse(mime, "UTF-8", stream);
                } catch (IOException ex) {
                    return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", null, new java.io.ByteArrayInputStream(new byte[0]));
                }
            }
        });
        web.addJavascriptInterface(new Bridge(), "u07Native");
        setContentView(web);
        web.loadUrl("https://" + LOCAL_HOST + "/index.html");
    }
    public class Bridge {
        @JavascriptInterface public void speak(String value) {
            runOnUiThread(() -> { if (tts != null) { tts.setSpeechRate(.82f); tts.speak(value, TextToSpeech.QUEUE_FLUSH, null, "u07"); } });
        }
    }
    @Override public void onBackPressed() {
        if (web != null) web.evaluateJavascript("window.goBack && window.goBack()", null);
        else super.onBackPressed();
    }
    @Override public void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (web != null) web.destroy();
        super.onDestroy();
    }
}
