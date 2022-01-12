package com.rmit.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullname, etAge, etEmail, etPassword;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        TextView banner = findViewById(R.id.banner);
        banner.setOnClickListener(v -> finish());


//        Button backButton = findViewById(R.id.backButton);
//        backButton.setOnClickListener(v -> toLogin());


        Button register = findViewById(R.id.register);
        register.setOnClickListener(v -> {
            etFullname = findViewById(R.id.fullName);
            etAge = findViewById(R.id.age);
            etEmail = findViewById(R.id.email);
            etPassword = findViewById(R.id.password);

            String fullName = etFullname.getText().toString().trim();
            String age = etAge.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (fullName.isEmpty()) {
                etFullname.setError("User name is required");
                etFullname.requestFocus();
                return;
            }

            if (age.isEmpty()) {
                etAge.setError("User age is required");
                etAge.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                etEmail.setError("Email is required");
                etEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Email is invalid");
                etEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Password is required");
                etPassword.requestFocus();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("fullName", fullName);
                            userMap.put("age", age);

                            db.collection("users").document(email).set(userMap)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_LONG).show();
                                        finish();
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(RegisterActivity.this, "User unable to connect to database", Toast.LENGTH_LONG).show();
                                        }});
                        } else{
                            Toast.makeText(RegisterActivity.this, "User has failed to register", Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

//    private void toLogin() {
//        Intent intent = new Intent(this, EmailLoginActivity.class);
//        startActivity(intent);
//    }
}