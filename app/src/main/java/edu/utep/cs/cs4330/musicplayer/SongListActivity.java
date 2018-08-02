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
    ArrayList<SongModel> songArrayList;
    String[] songColumns;
    Cursor songCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_list);

        songView = (ListView) findViewById(R.id.songList);
        songArrayList = new ArrayList<SongModel>();
//        songList = new ArrayList<>();
        songColumns = new String[] { /*MediaStore.Audio.Media._ID,*/ MediaStore.Audio.Media._ID,MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.TITLE};

        songCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songColumns, null, null, null);
        Log.e("THING","This: " + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);


        if (songCursor.moveToFirst()) {
            do {
                String id = songCursor.getString(songCursor.getColumnIndex(
                        MediaStore.Audio.Media._ID));
                String artist = songCursor.getString(songCursor.getColumnIndex(
                        MediaStore.Audio.Media.ARTIST));
                String album = songCursor.getString(songCursor.getColumnIndex(
                        MediaStore.Audio.Media.ALBUM));
                String name = songCursor.getString(songCursor.getColumnIndex(
                        MediaStore.Audio.Media.TITLE));
                songArrayList.add(new SongModel(id, artist, album, name));
            } while (songCursor.moveToNext());
        }
        songCursor.close();
        SongListAdapter adapter = new SongListAdapter(this, songArrayList);
        songView.setAdapter(adapter);


        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(SongListActivity.this, MainActivity.class);

            /*    String songName = songList.get(position);
                i.putExtra("songName", MediaStore.Audio.Media.EXTERNAL_CONTENT_URI+ "/"+songName);*/
                i.putExtra("songList", songArrayList);

                SongListActivity.this.startActivity(i);
            }
        });

        /*if(songCursor != null){
            if(songCursor.moveToFirst()){
                do{
//                    int audioIndex = songCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
//                    songList.add(songCursor.getString(audioIndex));
                    songList.add(songCursor.getString(0)*//* + "\n"
                            + songCursor.getString(1) + "\n"
                            + songCursor.getString(2)+ " \n"
                            + songCursor.getString(3)+ " \n"
                            + songCursor.getString(4)*//*);
                }while(songCursor.moveToNext());
            }

*//*            if (songCursor.moveToFirst()) {
                do {
                    String artist = songCursor.getString(songCursor.getColumnIndex(
                            MediaStore.Audio.Media.ARTIST));
                    String title = songCursor.getString(songCursor.getColumnIndex(
                            MediaStore.Audio.Media.TITLE));
                } while (songCursor.moveToNext());
            }*//*

        }

        songCursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,android.R.id.text1, songList);
        songView.setAdapter(adapter);

        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(SongListActivity.this, MainActivity.class);

                String songName = songList.get(position);
                i.putExtra("songName", MediaStore.Audio.Media.EXTERNAL_CONTENT_URI+ "/"+songName);
                SongListActivity.this.startActivity(i);
            }
        });*/
    }

}
