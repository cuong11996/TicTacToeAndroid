package com.rmit.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class EmailLoginActivity extends AppCompatActivity {

    private TextView banner, register;
    private EditText etEmail, etPassword;
    private Button signIn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        mAuth = FirebaseAuth.getInstance();

        banner = (TextView) findViewById(R.id.banner);
        banner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        register = (TextView) findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EmailLoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });

        signIn = (Button) findViewById(R.id.login);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etEmail = (EditText) findViewById(R.id.email);
                etPassword = (EditText) findViewById(R.id.password);

                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if(email.isEmpty()){
                    etEmail.setError("Email is required");
                    etEmail.requestFocus();
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    etEmail.setError("Email is invalid");
                    etEmail.requestFocus();
                    return;
                }

                if(password.isEmpty()){
                    etPassword.setError("Password is required");
                    etPassword.requestFocus();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Intent i = new Intent(EmailLoginActivity.this, MainMenuActivity.class);
                            startActivity(i);
                        }
                        else{
                            Toast.makeText(EmailLoginActivity.this, "Failed to login, credentials not found", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}