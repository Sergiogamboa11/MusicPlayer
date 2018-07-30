package edu.utep.cs.cs4330.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class SongListActivity extends AppCompatActivity {

    ListView songView;
    ArrayList<String> songList;
    String[] songColumns;
    Cursor songCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_list);

        songView = (ListView) findViewById(R.id.songList);
        songList = new ArrayList<>();
        songColumns = new String[] { /*MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME,*/MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.TITLE};

        songCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songColumns, null, null, null);

        if(songCursor != null){
            if(songCursor.moveToFirst()){
                do{
                    /*int audioIndex = songCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                    songList.add(songCursor.getString(audioIndex));*/
                    songList.add(songCursor.getString(0) + " - "
                            + songCursor.getString(1) + " - "
                            + songCursor.getString(2));
                }while(songCursor.moveToNext());
            }
        }

        songCursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,android.R.id.text1, songList);
        songView.setAdapter(adapter);


        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(SongListActivity.this, MainActivity.class);
                SongListActivity.this.startActivity(i);
            }
        });
    }

}
