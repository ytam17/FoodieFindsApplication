package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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

public class RestaurantDetails extends AppCompatActivity {
    private App app;
    User user;
    String Appid = "foodiefindsapp-fxtqu";
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection<Document> mongoCollection, mongoReviewsCollection;
    RecyclerView allReviewRecyclerView;
    ArrayList<Review> reviewList = new ArrayList<>();

    Button writeReviewButton;

    TextView nameTextView, addressTextView, sunHoursTextView, monHoursTextView, tueHoursTextView, wedHoursTextView,
            thuHoursTextView, friHoursTextView, satHoursTextView, ratingTextView;

    ImageView imageView;

    RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        Realm.init(getApplicationContext());
        app = new App(new AppConfiguration.Builder(Appid).build());

        Intent intent = getIntent();
        String restaurantName = intent.getStringExtra("name");
        String restaurantAddress = intent.getStringExtra("address");
        String restaurantId = intent.getStringExtra("restaurant_id");
        String username = intent.getStringExtra("username");
        String imageUrl = intent.getStringExtra("imageUrl");

        Log.v("Restaurant Details", "restaurant_id: " + restaurantId);
        user = app.currentUser();
        mongoClient = user.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("test");
        mongoCollection = mongoDatabase.getCollection("restaurants");
        mongoReviewsCollection = mongoDatabase.getCollection("reviews");

        imageView = findViewById(R.id.imageView);
        nameTextView = findViewById(R.id.nameTextView);
        addressTextView = findViewById(R.id.addressTextView);
        sunHoursTextView = findViewById(R.id.sunHoursTextView);
        monHoursTextView = findViewById(R.id.monHoursTextView);
        tueHoursTextView = findViewById(R.id.tueHoursTextView);
        wedHoursTextView = findViewById(R.id.wedHoursTextView);
        thuHoursTextView = findViewById(R.id.thuHoursTextView);
        friHoursTextView = findViewById(R.id.friHoursTextView);
        satHoursTextView = findViewById(R.id.satHoursTextView);
        ratingTextView = findViewById(R.id.ratingTextView);
        ratingBar = findViewById(R.id.ratingBar);
        writeReviewButton = findViewById(R.id.writeReviewButton);

        Document queryFilter = new Document().append("restaurant_id", restaurantId);

        RealmResultTask<MongoCursor<Document>> findTask = mongoCollection.find(queryFilter).iterator();
        RealmResultTask<MongoCursor<Document>> findReviewsTask = mongoReviewsCollection.find(queryFilter).iterator();
        app.loginAsync(Credentials.anonymous(), new App.Callback<io.realm.mongodb.User>() {
            @Override
            public void onResult(App.Result<User> result) {
                if (result.isSuccess()) {
                    findTask.getAsync(task -> {
                        if (task.isSuccess()) {
                            MongoCursor<Document> results = task.get();
                            if (!results.hasNext()) {
                                Toast.makeText(getApplicationContext(), "No restaurant found!", Toast.LENGTH_SHORT).show();
                            }
                            String sunOpenHrs = "";
                            String monOpenHrs = "";
                            String tueOpenHrs = "";
                            String wedOpenHrs = "";
                            String thuOpenHrs = "";
                            String friOpenHrs = "";
                            String satOpenHrs = "";
                            while (results.hasNext()) {
                                Document currentDoc = results.next();
                                try {
                                    JSONObject restaurant = new JSONObject(currentDoc.toJson());
                                    JSONArray OpenHrs = (JSONArray) restaurant.get("openinghours");
                                    sunOpenHrs = OpenHrs.get(0).toString();
                                    monOpenHrs = OpenHrs.get(1).toString();
                                    tueOpenHrs = OpenHrs.get(2).toString();
                                    wedOpenHrs = OpenHrs.get(3).toString();
                                    thuOpenHrs = OpenHrs.get(4).toString();
                                    friOpenHrs = OpenHrs.get(5).toString();
                                    satOpenHrs = OpenHrs.get(6).toString();
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                String cuisine = (String) currentDoc.get("cuisine");
                                Double rating = (Double) currentDoc.get("rating");
                                if (restaurantName == "" && restaurantAddress == "" && cuisine == "") {
                                    Toast.makeText(getApplicationContext(), "Invalid restaurant!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    String ratingStr = rating.toString();
                                    if(imageUrl != null && !imageUrl.equals("")){
                                        Picasso.get().load(imageUrl).into(imageView);
                                    }
                                    nameTextView.setText(restaurantName);
                                    addressTextView.setText(restaurantAddress);
                                    sunHoursTextView.setText(sunOpenHrs);
                                    monHoursTextView.setText(monOpenHrs);
                                    tueHoursTextView.setText(tueOpenHrs);
                                    wedHoursTextView.setText(wedOpenHrs);
                                    thuHoursTextView.setText(thuOpenHrs);
                                    friHoursTextView.setText(friOpenHrs);
                                    satHoursTextView.setText(satOpenHrs);
                                    ratingBar.setNumStars(5);
                                    if (rating > 0) {
                                        Log.v("RestaurantDetails", "rating.floatValue(): " + rating.floatValue());
                                        Log.v("RestaurantDetails", "rating.intValue(): " + rating.intValue());
                                        ratingBar.setRating(rating.floatValue());
                                        ratingTextView.setText(ratingStr);
                                    } else {
                                        ratingBar.setRating(0);
                                    }
                                }
                            }
                        } else {
                            Log.v("Task Error", task.getError().toString());
                        }
                    });

                    findReviewsTask.getAsync(ndtask -> {
                        if (ndtask.isSuccess()) {
                            MongoCursor<Document> ndresults = ndtask.get();
                            if (!ndresults.hasNext()) {
                                Toast.makeText(getApplicationContext(), "No review found!", Toast.LENGTH_SHORT).show();
                            }
                            while (ndresults.hasNext()) {
                                Document currentDoc = ndresults.next();
                                Review review = new Review();
                                String username = (String) currentDoc.get("username");
                                String comment = (String) currentDoc.get("comments");
                                String reviewSubject = (String) currentDoc.get("subject");
                                Double rating = (Double) currentDoc.get("rating");
                                review.setUsername(username);
                                review.setSubject(reviewSubject);
                                review.setComments(comment);
                                review.setRating(rating);
                                reviewList.add(review);
                            }
                        } else {
                            Log.v("Task Error", ndtask.getError().toString());
                        }

                        Log.v("ReviewList", "reviewList.size: " + reviewList.size());
                        ReviewListAdapter adapter = new ReviewListAdapter(RestaurantDetails.this, reviewList);
                        allReviewRecyclerView = findViewById(R.id.all_reviews_recycler_view);
                        allReviewRecyclerView.setAdapter(adapter);
                        allReviewRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    });


                }
            }
        });
        writeReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("writeReviewButton", "Username: " + username);
                Intent intent = new Intent(getApplicationContext(), ReviewWriting.class);
                intent.putExtra("restaurant_id", restaurantId);
                intent.putExtra("username", username);
                startActivity(intent);
                finish();
            }
        });
    }
}