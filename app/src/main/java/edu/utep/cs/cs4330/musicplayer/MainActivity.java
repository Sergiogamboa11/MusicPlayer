package edu.utep.cs.cs4330.musicplayer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.provider.MediaStore;
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

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final int PERMISSIONS_EXTERNAL_STORAGE = 1;
    TextView songProgress, songDuration, songTitle;
    MediaPlayer mediaPlayer;
    Button songView, play, stop, pause, forward, back;
    SeekBar seekBar;
    Runnable runnable;
    Handler handler;
    int time = 0;
    int song = R.raw.song1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

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

    }

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

    public void performActions(){

        play.setOnClickListener(this::play);
        stop.setOnClickListener(this::stop);
        pause.setOnClickListener(this::pause);
        back.setOnClickListener(this::seekBack);
        forward.setOnClickListener(this::seekFwd);
        songView.setOnClickListener(this::showSongList);

        getCurSongInfo(R.raw.song1);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean progressChanged) {
                Log.e("SEEKBAR PROGRESS", "is "+progress);
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

    public void getCurSongInfo(int song){
        MediaPlayer temp = MediaPlayer.create(this, song);
        seekBar.setMax(temp.getDuration());
        setTime(songDuration, temp.getDuration());
    }

    public void play(View view){
        if(mediaPlayer == null){
            mediaPlayer = MediaPlayer.create(this, song);
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
    }

    public void stop(View view){
        release();
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

    public void seekFwd(View view){
        if(mediaPlayer!=null)
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+5000);
    }


    public void seekBack(View view){
        if(mediaPlayer!=null)
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-5000);
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
        int duration = time;
        String update = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
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
