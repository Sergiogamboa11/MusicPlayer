package edu.utep.cs.cs4330.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.view.View;
import android.widget.SeekBar;

import java.io.IOException;

public class SongService extends Service {

    MediaPlayer mediaPlayer;

    public SongService() {
    }

    private final IBinder myBinder = new MyLocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyLocalBinder extends Binder{
        SongService getService(){
            return SongService.this;
        }
    }

    public void play(int time, String SONG_URI, SeekBar seekBar){
        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource( this, Uri.parse(SONG_URI));
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
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
                }
            });
        }
        mediaPlayer.start();
//        updateSeekBar();
    }

    public void pause(){
        if(mediaPlayer != null)
            mediaPlayer.pause();
    }

    public void release(){
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void stop(){
        release();
    }
}
