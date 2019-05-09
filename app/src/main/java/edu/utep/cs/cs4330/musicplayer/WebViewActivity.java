package edu.utep.cs.cs4330.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.scribejava.core.model.OAuth2AccessToken;

public class WebViewActivity extends AppCompatActivity {

    String lyrics = "";
    static OAuth2AccessToken accessToken;
    WebView webView;
    TextView textView;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        webView = findViewById(R.id.webview);
    }

    @Override
    protected void onStart() {
        super.onStart();

         handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                startActivity();
            }
        };

        Intent intent = getIntent();
        String artist = intent.getStringExtra("artist");
        String song = intent.getStringExtra("song");
        LyricFetcher lyricFetcher = new LyricFetcher();
        String url = lyricFetcher.sendAuthRequest();
        lyricFetcher.handleBrowser( webView, url, artist, song);

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while(lyricFetcher.lyrics.equals("")){

                }
                lyrics  = lyricFetcher.lyrics;
                handler.sendEmptyMessage(0);
            }
        });
        t1.start();

    }

    private void startActivity(){

        Intent returnIntent = new Intent();
        if(lyrics.equals("-1")){
            returnIntent.putExtra("lyrics","No lyrics found for this song");
        }
        else{
            returnIntent.putExtra("lyrics",lyrics);
        }
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

}
