package edu.utep.cs.cs4330.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
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

    public void play(int time, String SONG_URI, SeekBar seekBar, float tempo, float pitch){

        stop(); // This stops pitch change crash for some reason
        PlaybackParams params = new PlaybackParams();
        params.setPitch(pitch);
        params.setSpeed(tempo);
        Log.e("Speed and pitch: ", params.getSpeed() + " " +  params.getPitch());

        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource( this, Uri.parse(SONG_URI));
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaPlayer.seekTo(time);
            mediaPlayer.setPlaybackParams(params);
            mediaPlayer.start();

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

        else {

            mediaPlayer.seekTo(time);
            mediaPlayer.setPlaybackParams(params);
            mediaPlayer.start();
        }
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
