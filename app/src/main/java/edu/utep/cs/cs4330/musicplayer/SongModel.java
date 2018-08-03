package edu.utep.cs.cs4330.musicplayer;

import android.os.Parcel;
import android.os.Parcelable;

public class SongModel implements Parcelable{

    String songID;
    String songArtist;
    String songAlbum;
    String songName;
    String songAlbumID;
    long songLength;
    String albumArt;

    SongModel(String id, String artist, String album, String name, String albumID, long length, String art){
        songID = id;
        songArtist = artist;
        songAlbum = album;
        songName = name;
        songAlbumID =albumID;
        songLength = length;
        albumArt = art;
    }

    protected SongModel(Parcel in) {
        songID = in.readString();
        songArtist = in.readString();
        songAlbum = in.readString();
        songName = in.readString();
        songAlbumID = in.readString();
        songLength = in.readLong();
        albumArt = in.readString();
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

    public void setSongAlbumID(String songAlbumID){this.songAlbumID = songAlbumID; }

    public void setSongLength(long songLength){this.songLength = songLength; }

    public void setAlbumArt(String albumArt){this.albumArt = albumArt; }

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
        dest.writeString(songAlbumID);
        dest.writeLong(songLength);
        dest.writeString(albumArt);
    }
}
