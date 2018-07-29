package edu.utep.cs.cs4330.musicplayer;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    TextView songProgress, songDuration, songTitle;
    MediaPlayer mediaPlayer;
    Button play, stop, pause, forward, back;
    SeekBar seekBar;
    Runnable runnable;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songDuration = findViewById(R.id.tvDuration);
        songProgress = findViewById(R.id.tvProgress);
        seekBar = findViewById(R.id.seekBar);
        back = findViewById(R.id.btnBack);
        play = findViewById(R.id.btnPlay);
        forward = findViewById(R.id.btnFwd);
        pause = findViewById(R.id.btnPause);
        stop = findViewById(R.id.btnStop);
        handler = new Handler();

        play.setOnClickListener(this::play);
        stop.setOnClickListener(this::stop);
        pause.setOnClickListener(this::pause);
        back.setOnClickListener(this::seekBack);
        forward.setOnClickListener(this::seekFwd);

    }

    public void play(View view){
        if(mediaPlayer == null){
            mediaPlayer = MediaPlayer.create(this, R.raw.song1);

            int duration = mediaPlayer.getDuration();
            String time = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
            );
            songDuration.setText(time);

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
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean progressChanged) {
                    if(progressChanged)
                        mediaPlayer.seekTo(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        mediaPlayer.start();
        updateSeekBar();
    }

    public void pause(View view){
        if(mediaPlayer != null)
            mediaPlayer.pause();
    }

    public void stop(View view){
        release();
        seekBar.setProgress(0);
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

            int duration = mediaPlayer.getCurrentPosition();
            String time = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
            );
            songProgress.setText(time);


            if (mediaPlayer.isPlaying()) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        updateSeekBar();
                    }
                };
                handler.postDelayed(runnable, 1000);
            }
        }
    }
}
