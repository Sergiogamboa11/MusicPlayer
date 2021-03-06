package edu.utep.cs.cs4330.musicplayer;

import android.icu.util.RangeValueIterator;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
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


    /**
     * Sends authorization request to Genius
     * @return The URL that is returned from Genius
     */
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
        Log.e("AuthURL", authorizationUrl);
        return authorizationUrl;

    }

    /**
     * Sets up webView and shouldOverrideUrlLoading listener to check if user stays within the websites allowed
     * Queries and saves lyrics once user accepts permissions
     * @param browser The browser that will be used
     * @param url The url
     * @param songArtist The name of the song's artist
     * @param songName The song's name
     */
    public void handleBrowser(WebView browser, String url, String songArtist, String songName){
        WebSettings webSettings = browser.getSettings();
        webSettings.setJavaScriptEnabled(true);
        browser.setWebChromeClient(new WebChromeClient()); //prev webchrome
        browser.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.toLowerCase().contains("genius")){
                    return false;
                }
                else{
                    getCode(url);
                    String lyricsURL = "";
                    if(code.equals(""))
                        lyrics = "-1";
                    else {

                        WebViewActivity.accessToken = getToken(code);

                        artist = songArtist.replace(" ", "_");
                        song = songName.replace(" ", "_");
                        String searchURL = makeQuery(browser, artist + "_" + song);
                        lyricsURL = findSong(songName, songArtist, searchURL);

                        if(lyricsURL.equals("")){
                            lyrics = "-2"; //If no URL, err msg
                        }
                        else {
                            lyrics = "\n" + songArtist + "\n" + songName + getLyrics(lyricsURL) + "\n";
                        }
                    }
                    return true;
                }
            }
        });
        browser.bringToFront();
        browser.loadUrl(url);
    }

    /**
     * Retrieves code from the url specified
     * @param url The URL
     * @return The code that was found
     */
    public String getCode(String url){
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
        Log.e("This is our code", code+ "@@@");
        return code;
    }

    /**
     * Uses artist name, song name, and a browser, to make a query, find a song, and save it
     * If no song is found, the string will be set to "No lyrics found for this song"
     * @param browser The browser we will use
     * @param songArtist The artist of the Song
     * @param songName The name of the song
     */
    public void saveLyrics(WebView browser, String songArtist, String songName){
        accessToken = WebViewActivity.accessToken;
        artist = songArtist.replace(" ", "_");
        song = songName.replace(" ", "_");
        String searchURL = makeQuery(browser, artist+"_"+song);
        String lyricsURL = findSong(songName, songArtist, searchURL);
        if(lyricsURL.equals("")){
            lyrics = "No lyrics found for this song";
        }
        else {
            lyrics = "\n" + songArtist + "\n" + songName + getLyrics(lyricsURL);
        }
//        Log.e("Our lyrics", lyrics);
    }

    /**
     * This method uses
     * @param code The code that will be used to get an access token
     * @return An access token
     */
    public OAuth2AccessToken getToken(String code){
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
            return accessToken;
    }

    /**
     * This method forms a URL to make a search query on the genius API
     * @param browser The webview that is to be used
     * @param query The query rhat will be input to Genius search API
     * @return The url that we formed
     */
    public String makeQuery(WebView browser, String query){
        String url = "https://api.genius.com/search?q=" + query + "&access_token=" + accessToken.getAccessToken();
        return url;
    }

    /**
     * This method searches for a song URL using the Genius API and returns it as a string if found
     * @param title The title of the song
     * @param artist The artist of the song
     * @param inURL The url for the serch
     * @return A string containing the URL of the song
     */
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


    /**
     * Takes a url and searches for the lyrics in the source html
     * @param url A string containing the URL where the lyrics will be taken from
     * @return A string containing the found lyrics
     */
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

        Log.e("Our extracted lyrics", extractedLyrics[0]);
        return extractedLyrics[0];
    }

}