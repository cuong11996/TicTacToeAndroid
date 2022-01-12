package com.rmit.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainMenuActivity extends AppCompatActivity {
    User user;
    TextView usernameTxt;
    TextView winNoTxt;
    TextView loseNoTxt;
    TextView matchNoTxt;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        usernameTxt = findViewById(R.id.usernameTxt);
        winNoTxt = findViewById(R.id.winNoTxt);
        loseNoTxt = findViewById(R.id.loseNoTxt);
        matchNoTxt = findViewById(R.id.matchNoTxt);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("Check get data", "DocumentSnapshot data: " + document.getData());
                        String fullName = (String) Objects.requireNonNull(document.get("fullName")).toString();
                        usernameTxt.setText(fullName);
                        int age = Integer.parseInt((String) document.get("age"));
                        int matchNo = Integer.parseInt((String) document.get("matchNo"));
                        matchNoTxt.setText(Integer.toString(matchNo));
                        int winNo = Integer.parseInt((String) document.get("winNo"));
                        winNoTxt.setText(Integer.toString(winNo));
                        loseNoTxt.setText(Integer.toString (matchNo-winNo));
                        user = new User(fullName,age,matchNo,winNo);
                    } else {
                        Log.d("Check get data", "No such document");
                    }
                } else {
                    Log.d("Check get data", "get failed with ", task.getException());
                }
            }
        });
    }
}