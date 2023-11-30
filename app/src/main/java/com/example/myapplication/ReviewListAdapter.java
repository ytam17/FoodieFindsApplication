package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ReviewListViewHolder> {

    Context context;
    ArrayList<Review> reviewList;

    public ReviewListAdapter(Context context, ArrayList<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReviewListViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_restaurant_review, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull ReviewListViewHolder holder, int position) {

        holder.username.setText(reviewList.get(position).getUsername());
        holder.subject.setText(reviewList.get(position).getSubject());
        holder.comments.setText(reviewList.get(position).getComments());
        holder.rating.setText(reviewList.get(position).getRating().toString());

    }


    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    class ReviewListViewHolder extends RecyclerView.ViewHolder {

        TextView username, subject, comments, rating;
        ImageView userImage;

        public ReviewListViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.userTextView);
            subject = itemView.findViewById(R.id.reviewSubjectTextView);
            comments = itemView.findViewById(R.id.reviewTextView);
            rating = itemView.findViewById(R.id.ratingTextView);
            userImage = itemView.findViewById(R.id.userImageView);
        }
    }

}
