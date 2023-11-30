package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

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

import androidx.appcompat.app.AppCompatActivity;
import org.bson.Document;

public class Login extends AppCompatActivity {
    private App app;
    String appId = "foodiefindsapp-fxtqu";
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection<Document> mongoCollection;
    User user;
    TextInputEditText textInputLayoutUsername, textInputLayoutPassword;
    Button buttonLogin;
    TextView textViewRegister;
    ProgressBar progressBar;
    String registeredPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Realm.init(getApplicationContext());
        app = new App(new AppConfiguration.Builder(appId).build());

        textInputLayoutUsername = findViewById(R.id.username);
        textInputLayoutPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.registerText);
        progressBar = findViewById(R.id.progress);

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("Logging in", "Logging in");
                String username, password;
                username = String.valueOf(textInputLayoutUsername.getText());
                password = String.valueOf(textInputLayoutPassword.getText());

                if (!username.equals("") && !password.equals("")) {
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
                                mongoCollection = mongoDatabase.getCollection("registerlogins");

                                Document queryFilter = new Document().append("username", username);

                                RealmResultTask<MongoCursor<Document>> findTask = mongoCollection.find(queryFilter).iterator();

                                findTask.getAsync(task -> {
                                    if (task.isSuccess()) {
                                        MongoCursor<Document> results = task.get();
                                        if (!results.hasNext()) {
                                            Toast.makeText(getApplicationContext(), "No user found!", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                        while (results.hasNext()) {
                                            Document currentDoc = results.next();
                                            registeredPassword = (String) currentDoc.get("password");
                                            if (registeredPassword == null) {
                                                Toast.makeText(getApplicationContext(), "Invalid password!", Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.GONE);
                                            } else {
                                                if(!password.equals(registeredPassword)){
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(getApplicationContext(), "Invalid password!", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Log in successfully!", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                                                    intent.putExtra("username", username);
                                                    Log.v("Login", "Username: " + username);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                        }
                                    } else {
                                        Log.v("Task Error", task.getError().toString());
                                    }
                                });
                            } else {
                                Log.v("User", "Failed to Login");
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
