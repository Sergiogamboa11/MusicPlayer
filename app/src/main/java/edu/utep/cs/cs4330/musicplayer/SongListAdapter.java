package edu.utep.cs.cs4330.musicplayer;


import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class SongListAdapter extends ArrayAdapter<SongModel> {


    public SongListAdapter(@NonNull Context context, ArrayList<SongModel> songList) {
        super(context, R.layout.song_layout ,songList);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view = layoutInflater.inflate(R.layout.song_layout, parent, false);

        String songArtist =  getItem(position).songArtist;
        String songAlbum =  getItem(position).songAlbum;
        String songName = getItem(position).songName;
        String albumArt = getItem(position).albumArt;

        TextView artistText = view.findViewById(R.id.textView_artist);
        TextView albumText = view.findViewById(R.id.textView_album);
        TextView songText = view.findViewById(R.id.textView_song);
        ImageView artImage = view.findViewById(R.id.imageView);

        artistText.setText(songArtist);
        albumText.setText(songAlbum);
        songText.setText(songName);
        if(albumArt!=null) {
            Glide.with(getContext()).load(albumArt).into(artImage);
        }
        return view;
    }


}
