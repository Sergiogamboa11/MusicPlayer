package edu.utep.cs.cs4330.musicplayer;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class SongListActivity extends AppCompatActivity {

    ListView songView;
    ArrayList<String> songList;

    Cursor songCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_list);
        songView = (ListView) findViewById(R.id.songList);
        songList = new ArrayList<>();

        String[] songs = {/* MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME,*/MediaStore.Audio.Media.TITLE };

        songCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songs, null, null, null);

        if(songCursor != null){
            if(songCursor.moveToFirst()){
                do{
                    int audioIndex = songCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                    songList.add(songCursor.getString(audioIndex));
                }while(songCursor.moveToNext());
            }
        }
        songCursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,android.R.id.text1, songList);
        songView.setAdapter(adapter);
    }





}
