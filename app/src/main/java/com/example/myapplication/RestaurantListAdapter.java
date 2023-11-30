package com.example.myapplication;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RestaurantListAdapter extends RecyclerView.Adapter<RestaurantListAdapter.RestaurantListViewHolder> {

    Context context;
    ArrayList<Restaurant> restaurantList;
    String username;

    public RestaurantListAdapter(Context context, ArrayList<Restaurant> restaurantList, String username) {
        this.context = context;
        this.restaurantList = restaurantList;
        this.username = username;
    }

    @NonNull
    @Override
    public RestaurantListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RestaurantListViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_restaurant_item, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull RestaurantListViewHolder holder, int position) {

        holder.name.setText(restaurantList.get(position).getName());
        holder.cuisine.setText(restaurantList.get(position).getCuisine());
        holder.address.setText(restaurantList.get(position).getAddress());
        holder.proximity.setText("nearby");
        String imageUrl = restaurantList.get(position).getImageUrl();

        if (imageUrl != null && !imageUrl.equals("")) {
            Picasso.get().load(imageUrl).into(holder.restaurantPicture);
        }

    }


    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    class RestaurantListViewHolder extends RecyclerView.ViewHolder {

        TextView name, cuisine, address, proximity;
        ImageView restaurantPicture;

        public RestaurantListViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.restaurantNameTextView);
            cuisine = itemView.findViewById(R.id.restaurantCuisineTextView);
            address = itemView.findViewById(R.id.restaurantAddressTextView);
            proximity = itemView.findViewById(R.id.proximityTextView);
            restaurantPicture = itemView.findViewById(R.id.restaurantImageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    Intent restaurantDetailsIntent = new Intent(context, RestaurantDetails.class);
                    restaurantDetailsIntent.putExtra("username", username);
                    restaurantDetailsIntent.putExtra("name", restaurantList.get(position).getName());
                    restaurantDetailsIntent.putExtra("cuisine", restaurantList.get(position).getCuisine());
                    restaurantDetailsIntent.putExtra("address", restaurantList.get(position).getAddress());
                    restaurantDetailsIntent.putExtra("proximity", proximity.getText());
                    restaurantDetailsIntent.putExtra("imageUrl", restaurantList.get(position).getImageUrl());
                    restaurantDetailsIntent.putExtra("restaurant_id", restaurantList.get(position).getRestaurantId());
                    context.startActivity(restaurantDetailsIntent);

                }
            });
        }
    }
}
