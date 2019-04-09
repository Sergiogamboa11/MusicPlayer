package edu.utep.cs.cs4330.musicplayer;

import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.github.scribejava.apis.GeniusApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import com.github.scribejava.core.model.OAuth1RequestToken;
import java.net.*;

public class LyricFetcher {

    String code = "";
    OAuth20Service service;
    OAuth2AccessToken accessToken;

    public String sendAuthRequest(){
        String clientId = "A6TUo5x_o84rgmnegSeME_toVmfj8QzV8TruDKeL0hAbPnB1TahmnIiXspVUs4W4";
        String clientSecret = "1ViW0RoSWsIgB247kz8JY9XzCVvRdpJiRwmtRlxL6Svw6bUYpP9G5f2OIFuwkCx4x4hRi0TsIv7XL34CKv1V5A";
        final String secretState = "1";
        service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .scope("me")
                .state(secretState)
                .callback("http://example.com/")
                .userAgent("ScribeJava")
                .build(GeniusApi.instance());
        final String authorizationUrl = service.getAuthorizationUrl();

        return authorizationUrl;

    }

    public String handleBrowser(WebView browser, LinearLayout linearLayout, String url){

        browser.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                if(!url.toLowerCase().contains("genius"))
                {
                    linearLayout.bringToFront();

                    URL authURL;

                    try {
                        authURL = new URL(url);
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    String query = authURL.getQuery();

                    Map<String, String> map = new HashMap<String, String>();
                    String [] params = query.split("&");
                    for (String param : params)
                    {
                        String name = param.split("=")[0];
                        String value = param.split("=")[1];
                        map.put(name, value);
                    }

                    for(String key: map.keySet()){
                        if(key.equals("code"))
                            code = map.get("code");
                    }

                    Log.e("Code", code);

                    getToken();
                    makeQuery(browser, linearLayout, "tesseract");

                    return true;
                }
                return false;
            }
        });

        WebSettings webSettings = browser.getSettings();
        webSettings.setJavaScriptEnabled(true);
        browser.setWebChromeClient(new WebChromeClient());

        browser.bringToFront();
        browser.loadUrl(url);

        return url;
    }

    public void getToken(){

            Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        accessToken = service.getAccessToken(code);
                        Log.e("accesstoken1", accessToken.getAccessToken());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });

            t1.start();

            while (true){
                try{
                    t1.join();
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

    }

    public void makeQuery(WebView browser, LinearLayout linearLayout, String query){

//        new Thread(new Runnable() {
//            public void run() {
                browser.bringToFront();
                browser.loadUrl("https://api.genius.com/search?q=" + query + "&access_token=" + accessToken.getAccessToken());
                Log.e("We used", code + " empty???");
//            }
//        }).start();

    }

}