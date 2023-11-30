package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;

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

public class RestaurantList extends AppCompatActivity {
    private App app;
    User user;
    String Appid = "foodiefindsapp-fxtqu";
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection<Document> mongoCollection;
    RecyclerView allRestaurantRecyclerView;
    RealmResultTask<MongoCursor<Document>> findTask;
    ArrayList<Restaurant> restaurantList = new ArrayList<>();
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);

        Intent intent = getIntent();
        String searchKey = intent.getStringExtra("search_key");
        String searchValue = intent.getStringExtra("search_value");
        String username = intent.getStringExtra("username");
        Log.v("RestaurantList", "onCreate Username: " + username);

        Realm.init(getApplicationContext());
        app = new App(new AppConfiguration.Builder(Appid).build());

        title = findViewById(R.id.title);

        user = app.currentUser();
        mongoClient = user.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("test");
        mongoCollection = mongoDatabase.getCollection("restaurants");

        Log.v("RestaurantList", "searchKey: " + searchKey);
        Log.v("RestaurantList", "searchValue: " + searchValue);

        if (searchKey.equals("") || searchKey == null) {
            searchKey = Constants.nameCategory;
        }

        if (searchValue.equals("")|| searchValue == null) {
            //find all restaurants
            findTask = mongoCollection.find().iterator();
        } else {
            if (searchKey.equals(Constants.nameCategory) || searchKey.equals(Constants.cuisineCategory)) {
                Document queryFilter = new Document().append(searchKey, new Document("$regex", "\\b" + searchValue + "\\b").append("$options", "i"));
                findTask = mongoCollection.find(queryFilter).iterator();
            } else if (searchKey.equals(Constants.locationCategory)) {
                Document queryFilter = new Document("$or", Arrays.asList(
                        new Document("address.street", new Document("$regex", "\\b" + searchValue + "\\b").append("$options", "i")),
                        new Document("address.city", new Document("$regex", "\\b" + searchValue + "\\b").append("$options", "i")),
                        new Document("address.province", new Document("$regex", "\\b" + searchValue + "\\b").append("$options", "i")),
                        new Document("address.postalcode", new Document("$regex", "\\b" + searchValue + "\\b").append("$options", "i"))
                ));
                findTask = mongoCollection.find(queryFilter).iterator();
            }
        }

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
                            String addressStreet = "";
                            String addressCity = "";
                            String addressProvince = "";
                            String addressPostalCode = "";
                            while (results.hasNext()) {
                                Document currentDoc = results.next();
                                Restaurant restaurant = new Restaurant();
                                String restaurantName = (String) currentDoc.get("name");
                                restaurant.setName(restaurantName);
                                String imageUrl = (String) currentDoc.get("images");
                                try {
                                    JSONObject restaurantJSONObject = new JSONObject(currentDoc.toJson());
                                    JSONObject addresses = (JSONObject) restaurantJSONObject.get("address");
                                    addressStreet = addresses.get("street").toString();
                                    addressCity = addresses.get("city").toString();
                                    addressProvince = addresses.get("province").toString();
                                    addressPostalCode = addresses.get("postalcode").toString();
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                String cuisine = (String) currentDoc.get("cuisine");
                                String restaurantId = (String) currentDoc.get("restaurant_id");
                                restaurant.setImageUrl(imageUrl);
                                restaurant.setCuisine(cuisine);
                                restaurant.setRestaurantId(restaurantId);
                                String concatAddress = addressStreet + "," + addressCity + "," + addressProvince + "," + addressPostalCode;
                                restaurant.setAddress(concatAddress);
                                restaurantList.add(restaurant);
                            }
                        } else {
                            Log.v("Task Error", task.getError().toString());
                        }
                        Log.v("RestaurantList", "restaurantList.size: " + restaurantList.size());
                        RestaurantListAdapter adapter = new RestaurantListAdapter(RestaurantList.this, restaurantList, username);
                        allRestaurantRecyclerView = findViewById(R.id.all_restaurants_recycler_view);
                        allRestaurantRecyclerView.setAdapter(adapter);
                        allRestaurantRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        title.setText(R.string.search_result_restaurant);
                    });
                }
            }
        });
    }
}