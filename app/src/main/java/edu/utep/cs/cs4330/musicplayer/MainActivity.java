package edu.utep.cs.cs4330.musicplayer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final int PERMISSIONS_EXTERNAL_STORAGE = 1;
    TextView songProgress, songDuration, songName, songArtist;
    MediaPlayer mediaPlayer;
    Button songView, play, stop, pause, forward, back;
    SeekBar seekBar;
    Runnable runnable;
    Handler handler;
    int time = 0;
    ArrayList<SongModel> songList;
    String SONG_URI = "content://media/external/audio/media/83";
    int CURRENT_POSITION = -1 ;
    long SONG_DURATION = -1;
    boolean PLAYING = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        songArtist = findViewById(R.id.textViewMain_artist);
        songName = findViewById(R.id.textViewMain_song);
        songView = findViewById(R.id.btnSongs);
        songDuration = findViewById(R.id.tvDuration);
        songProgress = findViewById(R.id.tvProgress);
        seekBar = findViewById(R.id.seekBar);
        back = findViewById(R.id.btnBack);
        play = findViewById(R.id.btnPlay);
        forward = findViewById(R.id.btnFwd);
        pause = findViewById(R.id.btnPause);
        stop = findViewById(R.id.btnStop);
        handler = new Handler();


        checkForUpdates();


    }

    public void checkForUpdates(){
        Intent intent = getIntent();
        songList = (ArrayList<SongModel>)getIntent().getSerializableExtra("songList");
        if(songList!=null) {
//            Log.e("CHECK:", songList.get(1).songName + " ???");
            CURRENT_POSITION = intent.getIntExtra("position", -1);
            SONG_DURATION = songList.get(CURRENT_POSITION).songLength;
//            Log.e("Position! ", pos+"??");
            Log.e("Current song info!","Id: " + songList.get(CURRENT_POSITION).songID + " Name: " + songList.get(CURRENT_POSITION).songName);
            SONG_URI = "content://media/external/audio/media/" + songList.get(CURRENT_POSITION).songID;
            updateDisplay();
            play(play);  //this WILL cause bigs later
        }
    }

    /**
     * Gets information of the current song
     * @param
     */
    public void getCurSongInfo(){
       /* MediaPlayer temp = new MediaPlayer();
        try {
            temp.setDataSource(this, Uri.parse(SONG_URI));
            temp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
//        Log.e("Duration?!?!?!?","Is: " + temp.getDuration());
        seekBar.setMax((int) SONG_DURATION);
        setTime(songDuration, (int) SONG_DURATION);
    }

    /**
     * Updates activity to show song information
     */
    public void updateDisplay(){
        songName.setText(songList.get(CURRENT_POSITION).songName);
        songArtist.setText(songList.get(CURRENT_POSITION).songArtist);
//        setTime(songDuration, (int)SONG_DURATION);
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

        play.setOnClickListener(this::play);
        stop.setOnClickListener(this::stop);
        pause.setOnClickListener(this::pause);
        back.setOnClickListener(this::skipBack);
        forward.setOnClickListener(this::skipForward);
        songView.setOnClickListener(this::showSongList);

        getCurSongInfo();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean progressChanged) {
                time=progress;
                setTime(songProgress,time);
                if(progressChanged && mediaPlayer!=null) {
                    mediaPlayer.seekTo(time);
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
        if(mediaPlayer == null){
//            mediaPlayer = MediaPlayer.create(this, song);
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource( this, Uri.parse(SONG_URI));
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
            PLAYING = true;
            mediaPlayer.seekTo(time);

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    seekBar.setMax(mediaPlayer.getDuration());
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    release();
                }
            });

        }
        mediaPlayer.start();
        updateSeekBar();
    }

    public void showSongList(View view){
        Intent intent = new Intent(this, SongListActivity.class);
        this.startActivity(intent);
    }

    public void pause(View view){
        if(mediaPlayer != null)
            mediaPlayer.pause();
        PLAYING = false;
    }

    public void stop(View view){
        release();
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
            stop(view);
            SONG_URI = "content://media/external/audio/media/" + songList.get(CURRENT_POSITION).songID;
            SONG_DURATION = songList.get(CURRENT_POSITION).songLength;
            getCurSongInfo(); // delete this later
            updateDisplay();
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
            stop(view);
            SONG_URI = "content://media/external/audio/media/" + songList.get(CURRENT_POSITION).songID;
            SONG_DURATION = songList.get(CURRENT_POSITION).songLength;
            getCurSongInfo(); // delete this later
            updateDisplay();
            if(temp)
                play(view);
        }
    }

    private void updateSeekBar() {
        if (mediaPlayer != null) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            setTime(songProgress, mediaPlayer.getCurrentPosition());
            if (mediaPlayer.isPlaying()) {
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

}
