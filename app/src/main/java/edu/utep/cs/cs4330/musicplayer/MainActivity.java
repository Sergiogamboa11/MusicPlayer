package edu.utep.cs.cs4330.musicplayer;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    Button play, stop, pause, forward, back;
    SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBar = findViewById(R.id.seekBar);
        back = findViewById(R.id.btnBack);
        play = findViewById(R.id.btnPlay);
        forward = findViewById(R.id.btnFwd);
        pause = findViewById(R.id.btnPause);
        stop = findViewById(R.id.btnStop);

        play.setOnClickListener(this::play);
        stop.setOnClickListener(this::stop);
        pause.setOnClickListener(this::pause);

    }

    public void play(View view){
        if(mediaPlayer == null){
            mediaPlayer = MediaPlayer.create(this, R.raw.song1);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    release();
                }
            });
        }
        mediaPlayer.start();
    }

    public void stop(View view){
        release();
    }

    public void pause(View view){
        if(mediaPlayer != null)
            mediaPlayer.pause();
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
}
