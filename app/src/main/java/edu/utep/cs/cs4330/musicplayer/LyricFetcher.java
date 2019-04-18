package edu.utep.cs.cs4330.musicplayer;

import android.icu.util.RangeValueIterator;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ScrollView;

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import com.github.scribejava.core.model.OAuth1RequestToken;
import java.net.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.*;

public class LyricFetcher {

    String code = "";
    OAuth20Service service;
    OAuth2AccessToken accessToken;
    String lyrics = "";

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

    public String handleBrowser(WebView browser, ScrollView scrollView, String url){

        browser.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                if(!url.toLowerCase().contains("genius"))
                {
                    scrollView.bringToFront();

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

//                    Log.e("Code", code);

                    getToken();
                    String searchURL = makeQuery(browser, scrollView, "tesseract");
                    String lyricsURL = findSong("tourniquet", "tesseract", searchURL);
                    lyrics = getLyrics(lyricsURL);
                    Log.e("Gottem", lyrics + "1");
                    //
                    //
                    //


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

        return lyrics;
    }

    public void getToken(){

            Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        accessToken = service.getAccessToken(code);
//                        Log.e("accesstoken1", accessToken.getAccessToken());
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

    public String makeQuery(WebView browser, ScrollView scrollView, String query){

//        new Thread(new Runnable() {
//            public void run() {
//                browser.bringToFront();
                String url = "https://api.genius.com/search?q=" + query + "&access_token=" + accessToken.getAccessToken();
                browser.loadUrl(url);
//                Log.e("url", url);

//            }
//        }).start();

        return url;
    }

    public String findSong(String title, String artist, String inURL){

        URL url = null;
        try {
            url = new URL(inURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        URLConnection request = null;
        try {
            request = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            request.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonParser jp = new JsonParser();
        JsonElement root = null;
        try {
            root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonObject rootobj = root.getAsJsonObject();
        JsonElement response = rootobj.get("response");
//        Log.e("Response!", response.toString());
        JsonArray hit = rootobj.getAsJsonObject("response").getAsJsonArray("hits");
//        Log.e("Response2", thing.toString());
        String foundURL = "";
        for(int i = 0; i<hit.size();i++) {
            JsonObject current = hit.get(i).getAsJsonObject();
            current = current.getAsJsonObject("result");
            JsonElement songName = current.get("title");
            JsonElement songURL = current.get("url");
            JsonElement songArtist = current.getAsJsonObject("primary_artist").get("name");
            if(title.equalsIgnoreCase(songName.getAsString()) && artist.equalsIgnoreCase(songArtist.getAsString())){
                foundURL = songURL.getAsString();
            }

        }
//        Log.e("URL found:", foundURL);
        return foundURL;
    }

    public String getLyrics(String url){

//        Log.e("url being used: ", url + "!");
        String[] extractedLyrics = {""};
        Thread t1 = new Thread(new Runnable() {
            public void run() {
                try {
                    String html = Jsoup.connect(url).get().html();
                    Document doc = Jsoup.parse(html);
                    doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
                    doc.select("br").append("\\n");
//                    doc.select("p").prepend("\\n\\n");
//                    String s = doc.html().replaceAll("\\\\n", "\n");
                    Elements lyrics = doc.select(".lyrics");
                    Elements clean = lyrics.select("p");
                    extractedLyrics[0] = clean.text();
                    Log.e("Clean data", clean.text());


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });

        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return extractedLyrics[0];

    }

}