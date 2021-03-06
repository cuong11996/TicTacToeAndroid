package com.rmit.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainMenuActivity extends AppCompatActivity {
    private User user;
    private TextView usernameTxt;
    private TextView winNoTxt;
    private TextView loseNoTxt;
    private TextView matchNoTxt;
    private TextView logout;
    private LinearLayout findMatchBtn;
    private LinearLayout confirmField;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onResume(){
        super.onResume();
        //get data of user from database to display
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        usernameTxt = findViewById(R.id.usernameTxt);
        winNoTxt = findViewById(R.id.winNoTxt);
        loseNoTxt = findViewById(R.id.loseNoTxt);
        matchNoTxt = findViewById(R.id.matchNoTxt);
        findMatchBtn = findViewById(R.id.findMatchBtn);
        confirmField = findViewById(R.id.confirmField);
        logout = findViewById(R.id.logoutBtn);


        setVisible(R.id.findMatchBtn,true);
        setVisible(R.id.confirmField,false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //get data of user from database to display
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

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });

        //add function for findMatchBtn
        findMatchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CollectionReference roomCollectionRef = db.collection("rooms");
                roomCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean isChangedActivity = false;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String playerX = (String) document.get("playerX");
                                String playerY = (String) document.get("playerY");
                                if (!playerX.equals("") && playerY.equals("") && !playerX.equals(mAuth.getCurrentUser().getEmail())){
                                    DocumentReference roomDocument = document.getReference();
                                    roomDocument.update("playerY",mAuth.getCurrentUser().getEmail());
                                    Intent intent = new Intent(MainMenuActivity.this,GamePlay.class);
                                    intent.putExtra("roomId",roomDocument.getId());
                                    intent.putExtra("xEmail",playerX);
                                    intent.putExtra("yEmail",mAuth.getCurrentUser().getEmail());
                                    startActivity(intent);
                                    isChangedActivity = true;
                                    break;
//                                    setVisible(R.id.findMatchBtn,false);
//                                    setVisible(R.id.confirmField,true);

//                                    Button acceptBtn = findViewById(R.id.acceptBtn);
//                                    acceptBtn.setOnClickListener(new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//                                            DocumentReference roomDocument = document.getReference();
//                                            roomDocument.update("playerY",mAuth.getCurrentUser().getEmail());
//                                            Intent intent = new Intent(MainMenuActivity.this,GamePlay.class);
//                                            intent.putExtra("roomId",roomDocument.getId());
//                                            intent.putExtra("xEmail",playerX);
//                                            intent.putExtra("yEmail",playerY);
//                                            startActivity(intent);
//                                        }
//                                    });
//
//                                    Button declineBtn = findViewById(R.id.declineBtn);
//                                    declineBtn.setOnClickListener(new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//                                            setVisible(R.id.findMatchBtn,true);
//                                            setVisible(R.id.confirmField,false);
//                                        }
//                                    });
                                }
                            }
                            if(!isChangedActivity)  addRoom();
                        }
                    }
                });
            }
        });
    }

    public void addRoom(){
        Map<String, Object> data = new HashMap<>();
        ArrayList<Integer> game = new ArrayList<>();
        for (int i = 0;i < 9;i++){
            game.add(0);
        }
        data.put("game", game);
        data.put("playerX", mAuth.getCurrentUser().getEmail());
        data.put("playerY","");

        db.collection("rooms")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Add room", "DocumentSnapshot written with ID: " + documentReference.getId());
                        Intent intent = new Intent(MainMenuActivity.this, GamePlay.class);
                        intent.putExtra("roomId", documentReference.getId());
                        intent.putExtra("xEmail",mAuth.getCurrentUser().getEmail());
                        intent.putExtra("yEmail","");
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Add room", "Error adding document", e);
                    }
                });
    }

    public void setVisible(int id , boolean isVisible){
        View aview = findViewById(id);
        if (isVisible){
            aview.setVisibility(View.VISIBLE);
        }
        else {
            aview.setVisibility(View.INVISIBLE);
        }
    }

}