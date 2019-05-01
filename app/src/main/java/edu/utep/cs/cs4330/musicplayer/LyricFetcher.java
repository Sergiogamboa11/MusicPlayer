package edu.utep.cs.cs4330.musicplayer;

import android.icu.util.RangeValueIterator;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

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
    String artist;
    String song;

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

    public void handleBrowser( WebView browser, String url, String songArtist, String songName){

        browser.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                if(!url.toLowerCase().contains("genius"))
                {
//                    browser.setVisibility(View.INVISIBLE);
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

                    getToken();
                    artist = songArtist.replace(" ", "_");
                    song = songName.replace(" ", "_");
                    String searchURL = makeQuery(browser, artist+"_"+song);
                    String lyricsURL = findSong(songName, songArtist, searchURL);
                    lyrics = "\n" + songArtist + "\n" + songName + getLyrics(lyricsURL);
//                    updateLyricView(textView, lyrics);
//                    WebViewActivity.lyrics = lyrics;

                    return false;
                }
                return true;
            }
        });


        WebSettings webSettings = browser.getSettings();
        webSettings.setJavaScriptEnabled(true);
        browser.setWebChromeClient(new WebChromeClient());

        browser.bringToFront();
        browser.loadUrl(url);

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

    public String makeQuery(WebView browser, String query){
        String url = "https://api.genius.com/search?q=" + query + "&access_token=" + accessToken.getAccessToken();
        Log.e("Query made: ", url);
        browser.loadUrl(url);
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
            Log.e("Got content maybe?", root.toString()+"this");
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonObject rootobj = root.getAsJsonObject();
        JsonElement response = rootobj.get("response");
        Log.e("Response!", response.toString());
        JsonArray hit = rootobj.getAsJsonObject("response").getAsJsonArray("hits");
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
        String[] extractedLyrics = {""};
        Thread t1 = new Thread(new Runnable() {
            public void run() {
                try {
                    String html = Jsoup.connect(url).get().html();
                    Document doc = Jsoup.parse(html);
                    doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
                    doc.select("br").append("\\n");
                    doc.select("p").prepend("\\n\\n");
                    Elements lyrics = doc.select(".lyrics");
                    Elements clean = lyrics.select("p");
                    extractedLyrics[0] = clean.text().replaceAll("\\\\n", "\n");

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

    public void updateLyricView(TextView textView, String string){
        textView.setText(string);
    }

    public String setLyrics(String lyrics){
        return lyrics;
    }

}