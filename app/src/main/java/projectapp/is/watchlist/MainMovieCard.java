package projectapp.is.watchlist;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Jernej on 9. 01. 2018.
 */

public class MainMovieCard implements Parcelable, Serializable {
    private String movieTitle;
    private String movieDescription;
    private String dateText;

    public MainMovieCard(String movieTitle, String movieDescription, String dateText){
        this.movieTitle = movieTitle;
        this.movieDescription = movieDescription;
        this.dateText = dateText;
    }

    private MainMovieCard(Parcel in){
        movieTitle = in.readString();
        movieDescription = in.readString();
        dateText = in.readString();
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getMovieDescription() {
        return movieDescription;
    }

    public String getDateText() {
        return dateText;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(movieTitle);
        parcel.writeString(movieDescription);
        parcel.writeString(dateText);
    }

    public static final Parcelable.Creator<MainMovieCard> CREATOR = new Parcelable.Creator<MainMovieCard>() {
        public MainMovieCard createFromParcel(Parcel in) {
            return new MainMovieCard(in);
        }

        public MainMovieCard[] newArray(int size) {
            return new MainMovieCard[size];
        }
    };
}
