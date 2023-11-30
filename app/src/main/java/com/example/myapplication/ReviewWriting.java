package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.bson.Document;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class ReviewWriting extends AppCompatActivity {
    private App app;
    User user;
    String Appid = "foodiefindsapp-fxtqu";
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection<Document> mongoCollection;
    TextView reviewSubjectTextView, reviewTextView, ratingTextView;
    Button submitButton;
    ProgressBar progressBar;
    RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_writing);

        Realm.init(getApplicationContext());
        app = new App(new AppConfiguration.Builder(Appid).build());

        Intent intent = getIntent();
        String restaurantId = intent.getStringExtra("restaurant_id");
        String username = intent.getStringExtra("username");

        progressBar = findViewById(R.id.progress);
        reviewSubjectTextView = findViewById(R.id.reviewSubjectTextView);
        reviewTextView = findViewById(R.id.reviewTextView);
        ratingTextView = findViewById(R.id.ratingTextView);
        ratingBar = findViewById(R.id.ratingBar);
        submitButton = findViewById(R.id.buttonSubmit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = String.valueOf(reviewSubjectTextView.getText());
                String review = String.valueOf(reviewTextView.getText());
                String rating = String.valueOf(ratingTextView.getText());
                Double ratingDoubleValue;
                if (rating != null && !rating.equals("")) {
                    ratingDoubleValue = Double.parseDouble(rating);
                } else {
                    ratingDoubleValue = 0.00;
                }

                if (!subject.equals("") && subject != null && !review.equals("") && review != null && !rating.equals("") && rating != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    if (app.currentUser() != null) {
                        app.currentUser().logOutAsync(result -> {
                            if (result.isSuccess()) {
                                Log.v("Logged Out", "Logged Out");
                            }
                        });
                    }
                    app.loginAsync(Credentials.anonymous(), new App.Callback<io.realm.mongodb.User>() {
                        @Override
                        public void onResult(App.Result<User> result) {
                            if (result.isSuccess()) {
                                user = app.currentUser();
                                mongoClient = user.getMongoClient("mongodb-atlas");
                                mongoDatabase = mongoClient.getDatabase("test");
                                mongoCollection = mongoDatabase.getCollection("reviews");
                                Document queryFilter = new Document().append("username", username).append("subject", subject);
                                RealmResultTask<MongoCursor<Document>> findTask = mongoCollection.find(queryFilter).iterator();

                                findTask.getAsync(task -> {
                                    if (task.isSuccess()) {
                                        MongoCursor<Document> results = task.get();

                                        if (results.hasNext()) {
                                            Log.v("Duplicated review", "This username is already submitted the review with the same subject!");
                                            Toast.makeText(getApplicationContext(), "This username has already commented before!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.v("Insert new review", "Ready to insert new account data");
                                            Log.v("ReviewWriting", "Username: " + username);
                                            mongoCollection.insertOne(new Document().append("username", username).append("comments", review)
                                                    .append("subject", subject).append("rating", ratingDoubleValue)
                                                    .append("restaurant_id", restaurantId)).getAsync(insertResult -> {
                                                if (insertResult.isSuccess()) {
                                                    Log.v("AddFunction", "Inserted Data");
                                                    Toast.makeText(getApplicationContext(), "New review is submitted!", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                } else {
                                                    Log.v("AddFunction", "Error" + insertResult.getError().toString());
                                                    Toast.makeText(getApplicationContext(), "Unable to add new review!", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    } else {
                                        Log.v("Error", task.getError().toString());
                                    }
                                });
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}