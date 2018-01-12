package projectapp.is.watchlist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Jernej on 9. 01. 2018.
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.CardViewHolder> {


    public static class CardViewHolder extends RecyclerView.ViewHolder{
        CardView cv;
        TextView movieTitle;
        TextView movieDate;
        TextView movieDesc;
        RatingBar ratingBar;

        ImageButton deleteCardButton;
        ImageButton editCardButton;

        CardViewHolder (final View itemView){
            super(itemView);

            cv = (CardView) itemView.findViewById(R.id.imageCardView);
            movieTitle = (TextView) itemView.findViewById(R.id.imageCardTitle);
            movieDate = (TextView) itemView.findViewById(R.id.imageCardDate);
            movieDesc = (TextView) itemView.findViewById(R.id.imageCardDesc);
            ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBarMain);

            deleteCardButton = (ImageButton) itemView.findViewById(R.id.imageCardDelete);
            editCardButton = (ImageButton) itemView.findViewById(R.id.imageCardEdit);
        }
    }

    List<MainMovieCard> mainMovieCards;
    private final int EDIT_REQUEST = 5001;
    private OnDeleteClick onDeleteCallback;

    RVAdapter(List<MainMovieCard> mainMovieCards, OnDeleteClick onDeleteClick){
        this.mainMovieCards = mainMovieCards;
        this.onDeleteCallback = onDeleteClick;
        //this.decoder = new BitmapDecoder();
    }

    public void removeCard (int position){
        mainMovieCards.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mainMovieCards.size());
    }

    @Override
    public int getItemCount() {
        return mainMovieCards.size();
    }

    @Override
    public RVAdapter.CardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.main_movie_card, viewGroup, false);
        CardViewHolder cvh = new CardViewHolder(v);
        return cvh;
    }

    @Override
    public void onBindViewHolder(final RVAdapter.CardViewHolder holder, final int position) {
        holder.movieTitle.setText(mainMovieCards.get(position).getMovieTitle());

        holder.movieDesc.setText(mainMovieCards.get(position).getMovieDescription());
        String date = mainMovieCards.get(position).getDateText();
        holder.movieDate.setText(date);
        holder.ratingBar.setRating(mainMovieCards.get(position).getRating());

        //holder.movieMainImage.setVisibility(View.GONE);

        holder.deleteCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("press", "DELETE PRESSED" + mainMovieCards.get(position));
                onDeleteCallback.onDeleteClick(mainMovieCards.get(position));
                mainMovieCards.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mainMovieCards.size());
            }
        });

        holder.editCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("press", "EDIT PRESSED");
                Intent intent = new Intent();
                intent.setClass(view.getContext(), AddMovieActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("movieTitle", mainMovieCards.get(position).getMovieTitle());
                //bundle.putString("movieCoverImagePath", mainMovieCards.get(position).getImagePath());
                bundle.putString("movieDesc", mainMovieCards.get(position).getMovieDescription());
                bundle.putString("dateText", mainMovieCards.get(position).getDateText());
                bundle.putFloat("movieRating", mainMovieCards.get(position).getRating());
                bundle.putInt("position", position);
                //String parent = mainMovieCards.get(position).getParentImageFolder();
                //bundle.putString("parentImageFolder", mainMovieCards.get(position).getParentImageFolder());
                intent.putExtras(bundle);
                ((Activity) view.getContext()).startActivityForResult(intent, EDIT_REQUEST);
            }
        });

        /*holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(view.getContext(), SecondActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("movieTitle", mainMovieCards.get(position).getMovieTitle());
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);
            }
        });*/
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}