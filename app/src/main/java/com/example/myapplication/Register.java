package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.bson.Document;

import io.realm.mongodb.App;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;
import io.realm.Realm;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.mongo.MongoCollection;

public class Register extends AppCompatActivity {
    private App app;
    User user;
    String Appid = "foodiefindsapp-fxtqu";
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection<Document> mongoCollection;
    TextInputEditText textInputLayoutFullname, textInputLayoutEmail, textInputLayoutUsername, textInputLayoutPassword;
    Button buttonRegister;
    TextView textViewLogin;
    ProgressBar progressBar;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Realm.init(getApplicationContext());
        app = new App(new AppConfiguration.Builder(Appid).build());

        textInputLayoutFullname = findViewById(R.id.fullname);
        textInputLayoutEmail = findViewById(R.id.email);
        textInputLayoutUsername = findViewById(R.id.username);
        textInputLayoutPassword = findViewById(R.id.password);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.loginText);
        progressBar = findViewById(R.id.progress);
        spinner = findViewById(R.id.roleDropDownMenu);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.roles, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("Signing up", "Signing up");
                String fullname, username, password, email, role;
                fullname = String.valueOf(textInputLayoutFullname.getText());
                username = String.valueOf(textInputLayoutUsername.getText());
                password = String.valueOf(textInputLayoutPassword.getText());
                email = String.valueOf(textInputLayoutEmail.getText());
                role = String.valueOf(spinner.getSelectedItem());

                if (!fullname.equals("") && fullname != null && !username.equals("") && username != null && !password.equals("")
                        && password != null && !email.equals("")
                        && email != null && !role.equals("") && role != null) {
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

                                        if (results.hasNext()) {
                                            Log.v("Duplicated Username", "The username is already registered");
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(getApplicationContext(), "The username is already registered!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.v("Insert new account", "Ready to insert new account data");
                                            mongoCollection.insertOne(new Document().append("username", username).append("password", password)
                                                    .append("fullname", fullname).append("email", email).append("role", role)).getAsync(insertResult -> {
                                                if (insertResult.isSuccess()) {
                                                    Log.v("AddFunction", "Inserted Data");
                                                    Toast.makeText(getApplicationContext(), "New account is registered!", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Log.v("AddFunction", "Error" + insertResult.getError().toString());
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(getApplicationContext(), "Unable to register new account!", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    } else {
                                        Log.v("Error", task.getError().toString());
                                    }
                                });
                            } else {
                                Log.v("User", "Failed to Login");
                            }
                        }
                    });
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}