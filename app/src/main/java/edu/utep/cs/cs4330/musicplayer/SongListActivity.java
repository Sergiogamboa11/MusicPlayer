package edu.utep.cs.cs4330.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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
        String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
        songCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songColumns, where, null, null);
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

//                String art = getAlbumArt(this, Long.parseLong(albumID));
                String art = getAlbumUri(this, albumID).toString();
//                Uri image = Uri.parse(art);
//                Log.e("ERROR??" , "HERE? " + art);

                songArrayList.add(new SongModel(id, artist, album, name, albumID, duration, art));
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
//                i.putExtra("playing", true);

                SongListActivity.this.startActivity(i);
            }
        });

    }


    private static String getAlbumArt(Context context, long albumID) {
        String path = null;
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?",
                new String[]{Long.toString(albumID)},
                null);
        if (c != null) {
            if (c.moveToFirst()) {
                path = c.getString(0);
            }
            c.close();
        }
        return path;
    }


    public Uri getAlbumUri(Context mContext, String album_id){
        if(mContext!=null) {
            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri imageUri = Uri.withAppendedPath(sArtworkUri, String.valueOf(album_id));
            return imageUri;
        }
        return null;

    }

}
