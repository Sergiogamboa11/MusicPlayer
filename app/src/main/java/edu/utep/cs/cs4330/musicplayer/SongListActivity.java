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
        songColumns = new String[] { MediaStore.Audio.Media._ID,MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION};

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
                String albumID = songCursor.getString(songCursor.getColumnIndex(
                        MediaStore.Audio.Media.ALBUM_ID));
                long duration = songCursor.getLong(songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                songArrayList.add(new SongModel(id, artist, album, name, albumID,duration));
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
                i.putExtra("position", position);

                SongListActivity.this.startActivity(i);
            }
        });

    }

}
