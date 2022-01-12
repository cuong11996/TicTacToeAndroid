package com.rmit.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button emailLogin;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            Intent i = new Intent(MainActivity.this, MainMenuActivity.class);
            startActivity(i);
        }

        emailLogin = (Button) findViewById(R.id.emailLogin);
        emailLogin.setOnClickListener(v -> {
            Intent i =  new Intent(MainActivity.this, EmailLoginActivity.class);
            startActivity(i);
        });

    }
}