package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Dashboard extends AppCompatActivity {
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    ActionBarDrawerToggle actionBarDrawerToggle;
    ImageButton searchRestaurantButton;
    TextView searchRestText;
    CardView nameCardView, cuisineCardView, locationCardView;
    String searchKey = "";
    String searchValue = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer);
        searchRestaurantButton = findViewById(R.id.buttonSearch);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        searchRestText = findViewById(R.id.searchRestText);
        nameCardView = findViewById(R.id.nameCardView);
        cuisineCardView = findViewById(R.id.cuisineCardView);
        locationCardView = findViewById(R.id.locationCardView);

        nameCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchKey = Constants.nameCategory;
            }
        });

        cuisineCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchKey = Constants.cuisineCategory;
            }
        });

        locationCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchKey = Constants.locationCategory;
            }
        });

        searchRestaurantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchValue = String.valueOf(searchRestText.getText());
                if (Services.onlySpecialCharacters(searchValue)) {
                    Toast.makeText(getApplicationContext(), "Invalid input!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), RestaurantList.class);
                intent.putExtra("username", username);
                intent.putExtra("search_key", searchKey);
                intent.putExtra("search_value", searchValue);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}