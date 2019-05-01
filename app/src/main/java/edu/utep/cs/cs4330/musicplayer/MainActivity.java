package edu.utep.cs.cs4330.musicplayer;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import edu.utep.cs.cs4330.musicplayer.SongService.MyLocalBinder;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    SongService songService;
    boolean isBound = false;

    public static final int PERMISSIONS_EXTERNAL_STORAGE = 0;
    ImageView albumArt;
    TextView songProgress, songDuration, songName, songArtist, songAlbum;
    MediaPlayer mediaPlayer;
    ImageButton imgPlay, forward, back;
    SeekBar seekBar;
    Runnable runnable;
    Handler handler;
    int time = 0;
    ArrayList<SongModel> songList;
    String SONG_URI = "";
    int CURRENT_POSITION = -1 ;
    long SONG_DURATION = -1;
    boolean PLAYING = false;
    boolean NEW_SONG = false;
    android.webkit.WebView browser;
    LinearLayout linearLayout;
    ScrollView scrollView;
    Button lyricsButton;
    TextView lyricsView;
    ViewGroup.LayoutParams lyricParams;
    ViewGroup.LayoutParams lyricsBtnParams;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setNavigationViewListner();

        songAlbum = findViewById(R.id.textViewMain_album);
        albumArt = findViewById(R.id.imageViewMain_AlbumArt);
        songArtist = findViewById(R.id.textViewMain_artist);
        songName = findViewById(R.id.textViewMain_song);
        songDuration = findViewById(R.id.tvDuration);
        songProgress = findViewById(R.id.tvProgress);
        seekBar = findViewById(R.id.seekBar);
        back = findViewById(R.id.btnBack);
        imgPlay = findViewById(R.id.imageButtonPlay);
        forward = findViewById(R.id.btnFwd);
        handler = new Handler();
        browser = (android.webkit.WebView) findViewById(R.id.webview);
        linearLayout = findViewById(R.id.linearlayout);
        scrollView = findViewById(R.id.scrollview);
        lyricsButton = findViewById(R.id.lyricsBtn);
        lyricsView = findViewById(R.id.lyricsView);

        lyricsView.setVisibility(View.INVISIBLE);
        lyricParams = lyricsView.getLayoutParams();
        lyricsBtnParams = lyricsButton.getLayoutParams();


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        lyricsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebViewActivity();
            }
        });

        Intent serviceIntent = new Intent(this, SongService.class);
        bindService(serviceIntent, myConntection, Context.BIND_AUTO_CREATE);

        checkForUpdates();

        if(songList==null){
            lyricsButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                String lyrics = data.getStringExtra("lyrics");

                lyricParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                lyricsView.setLayoutParams(lyricParams);

                lyricsBtnParams.height = 0;
                lyricsButton.setLayoutParams(lyricsBtnParams);

                lyricsButton.setVisibility(View.INVISIBLE);
                lyricsView.setVisibility(View.VISIBLE);
                lyricsView.setText(lyrics);
            }
            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    public void openWebViewActivity(){
        Intent webViewIntent = new Intent(this, WebViewActivity.class);
        webViewIntent.putExtra("artist", songList.get(CURRENT_POSITION).songArtist);
        webViewIntent.putExtra("song", songList.get(CURRENT_POSITION).songName);
        startActivityForResult(webViewIntent, 1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (myConntection != null) {
            unbindService(myConntection);
        }
    }

    public void startClick(View view){
        if(!PLAYING) {
            PLAYING = true;
            play(imgPlay);
        }
        else {
            PLAYING = false;
            pause(imgPlay);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Service!
     */
    private ServiceConnection myConntection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyLocalBinder binder = (MyLocalBinder) service;
            songService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };


    public void checkForUpdates(){

        Intent intent = getIntent();
        songList = (ArrayList<SongModel>)getIntent().getSerializableExtra("songList");
        if(songList!=null) {
            CURRENT_POSITION = intent.getIntExtra("position", -1);
            SONG_DURATION = songList.get(CURRENT_POSITION).songLength;
            SONG_URI = "content://media/external/audio/media/" + songList.get(CURRENT_POSITION).songID;
            updateDisplay();
            NEW_SONG = true;
            if(NEW_SONG) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stop();
                        play(imgPlay);
                    }
                }, 250);
            }
        }
    }

    /**
     * Gets information of the current song
     * @param
     */
    public void getCurSongInfo(){
        seekBar.setMax((int) SONG_DURATION);
        setTime(songDuration, (int) SONG_DURATION);
    }

    /**
     * Updates activity to show song information
     */
    public void updateDisplay(){
        songName.setText(songList.get(CURRENT_POSITION).songName);
        songArtist.setText(songList.get(CURRENT_POSITION).songArtist);
        songAlbum.setText(songList.get(CURRENT_POSITION).songAlbum);
        Glide.with(this).load(songList.get(CURRENT_POSITION).albumArt).apply(new RequestOptions().placeholder(R.mipmap.generic_cd)).into(albumArt);
    }

    /**
     * If storage permission granted, we go on. If not, we don't go on
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PERMISSIONS_EXTERNAL_STORAGE == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performActions();
            } else {
                Toast.makeText(this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Activates buttons, gets info for the current song
     */
    public void performActions(){
        imgPlay.setOnClickListener(this::startClick);
        back.setOnClickListener(this::skipBack);
        forward.setOnClickListener(this::skipForward);

        getCurSongInfo();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean progressChanged) {
                time=progress;
                setTime(songProgress,time);
                if(time<=0)
                    songProgress.setText("00:00");
                if(progressChanged && songService.mediaPlayer!=null) {
                    songService.mediaPlayer.seekTo(time);
                }
//                        mediaPlayer.seekTo(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    public void play(View view){
        imgPlay.setBackgroundResource(R.drawable.round_pause_circle_outline_24);
        imgPlay.setImageResource(R.mipmap.baseline_pause_circle_outline_white_48);
        songService.play(time, SONG_URI, seekBar);
        PLAYING = true;
        updateSeekBar();
        songService.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                skipForward(forward);
            }
        });
    }

    public void pause(View view){
        imgPlay.setBackgroundResource(R.drawable.round_play_circle_outline_24);
        imgPlay.setImageResource(R.mipmap.baseline_play_circle_outline_white_48);
        songService.pause();
        PLAYING = false;
    }

    public void stop(){
        songService.stop();
        PLAYING = false;
        time=0;
        seekBar.setProgress(0);
        songProgress.setText("00:00");
    }

    public void release(){
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        release();
    }

    public void skipForward(View view){
        if(songList!=null && CURRENT_POSITION != -1){
            if(CURRENT_POSITION == songList.size()-1){
                CURRENT_POSITION = 0;
            }
            else
                CURRENT_POSITION++;
            boolean temp = PLAYING;
            stop();
            SONG_URI = "content://media/external/audio/media/" + songList.get(CURRENT_POSITION).songID;
            SONG_DURATION = songList.get(CURRENT_POSITION).songLength;
            getCurSongInfo(); // delete this later
            updateDisplay();
            lyricsView.setVisibility(View.INVISIBLE);
            lyricParams.height = 0;
            lyricsView.setLayoutParams(lyricParams);

            lyricsBtnParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;;
            lyricsButton.setLayoutParams(lyricsBtnParams);
            lyricsButton.setVisibility(View.VISIBLE);

            if(temp)
                play(view);
        }
    }

    public void skipBack(View view){
        if(songList!=null && CURRENT_POSITION!=-1){
            if(CURRENT_POSITION == 0){
                CURRENT_POSITION = songList.size()-1;
            }
            else
                CURRENT_POSITION--;
            boolean temp = PLAYING;
            stop();
            SONG_URI = "content://media/external/audio/media/" + songList.get(CURRENT_POSITION).songID;
            SONG_DURATION = songList.get(CURRENT_POSITION).songLength;
            getCurSongInfo(); // delete this later
            updateDisplay();
            lyricsView.setVisibility(View.INVISIBLE);
            lyricParams.height = 0;
            lyricsView.setLayoutParams(lyricParams);

            lyricsBtnParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;;
            lyricsButton.setLayoutParams(lyricsBtnParams);
            lyricsButton.setVisibility(View.VISIBLE);
            if(temp)
                play(view);
        }
    }

    private void updateSeekBar() {
        if (songService.mediaPlayer != null) {
            seekBar.setProgress(songService.mediaPlayer.getCurrentPosition());
            setTime(songProgress, songService.mediaPlayer.getCurrentPosition());
            if(time<=0)
                songProgress.setText("00:00");
            if (songService.mediaPlayer.isPlaying()) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        updateSeekBar();
                    }
                };
                handler.postDelayed(runnable, 100);
            }
        }
    }

    private void setTime(TextView view, int time){
        String update = String.format(Locale.US, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(time),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
        );
        view.setText(update);
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Please allow Storage Permissions in App Setitngs", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_EXTERNAL_STORAGE);
        }
    }

    private void setNavigationViewListner() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.song_list: {
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, SongListActivity.class);
                    this.startActivity(intent);
                }
                else
                    Toast.makeText(this, "Storage Permissions Required", Toast.LENGTH_SHORT);
            }
        }
        return false;
    }
}
