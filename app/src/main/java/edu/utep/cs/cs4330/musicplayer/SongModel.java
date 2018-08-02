package edu.utep.cs.cs4330.musicplayer;

import android.os.Parcel;
import android.os.Parcelable;

public class SongModel implements Parcelable{

    String songID;
    String songArtist;
    String songAlbum;
    String songName;

    SongModel(String id, String artist, String album, String name){
        songID = id;
        songArtist = artist;
        songAlbum = album;
        songName = name;
    }

    protected SongModel(Parcel in) {
        songID = in.readString();
        songArtist = in.readString();
        songAlbum = in.readString();
        songName = in.readString();
    }

    public static final Creator<SongModel> CREATOR = new Creator<SongModel>() {
        @Override
        public SongModel createFromParcel(Parcel in) {
            return new SongModel(in);
        }

        @Override
        public SongModel[] newArray(int size) {
            return new SongModel[size];
        }
    };

    public void setSongID(String songID){
        this.songID = songID;
    }

    public void setSongArtist(String songArtist){
        this.songArtist = songArtist;
    }

    public void setSongAlbum(String songAlbum){
        this.songAlbum = songAlbum;
    }

    public void setSongName(String songName){
        this.songName = songName;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(songID);
        dest.writeString(songArtist);
        dest.writeString(songAlbum);
        dest.writeString(songName);
    }
}
