package com.rmit.tictactoe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GamePlay extends AppCompatActivity {
    private static final String TAG = "GAMEPLAY";


    // Config
    private static final int SIZE = 3;
    private static final int PLAYER_X = 1;
    private static final int PLAYER_O = -1;
    private static final int EMPTY = 0;
    private static long[][] arr = new long[SIZE][SIZE];
    ImageButton[][] buttons = new ImageButton[SIZE][SIZE];
    private static boolean isTurn = false;


    // Firebase
    private static DocumentReference roomRef = null;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();


    // Current user/match's config
    static long role = PLAYER_X;
    static String roomId = "";
    static String xEmail = "";
    static String yEmail = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        for (long[] row: arr) Arrays.fill(row, 0);

        Intent intent = getIntent();
        roomId = intent.getStringExtra("roomId");
        xEmail = intent.getStringExtra("xEmail");
        yEmail = intent.getStringExtra("yEmail");
        role = intent.getBooleanExtra("isX", false) ? PLAYER_X : PLAYER_O;

        isTurn = role == PLAYER_X;

        // TODO: Extract data from intent
        roomRef = db.collection("rooms").document(roomId);
        roomRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.i(TAG, "Receive updates");
                    Map<String, Object> currentData = snapshot.getData();

                    long[][] newArr = firebaseFlattenedTo2D((ArrayList<Long>) Objects.requireNonNull(currentData.get("game")));

                    for (int i = 0; i < SIZE; i++) {
                        for (int j = 0; j < SIZE; j++) {
                            if (newArr[i][j] != arr[i][j]) {
                                String buttonID = "button" + (i * SIZE + j);
                                View chosenCell = findViewById(getResources().getIdentifier(buttonID, "id", getPackageName()));

                                Log.i(TAG, "Diff cell: " + i + " " + j);
                                makeMove(newArr[i][j], chosenCell, i, j);
                            }
                        }
                    }

                    Log.d(TAG, "Current data: " + snapshot.getData());
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        // Set board content
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                String buttonID = "button" + (i * SIZE + j);
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                buttons[i][j] = findViewById(resID);
                buttons[i][j].setOnClickListener(this::handleCellClick);
            }
        }
    }


    private void handleCellClick(View v) {
        try {
            if (!isTurn) {
                Toast.makeText(GamePlay.this, "This is not your turn", Toast.LENGTH_LONG).show();
                return;
            }

            int buttonId = Integer.parseInt(
                    v.getResources().getResourceName(v.getId()).split("/")[1].substring("button".length()));
            Log.i(TAG, buttonId + "");

            int row = buttonId / SIZE;
            int column = buttonId % SIZE;
            Log.i(TAG, "Row " + row + " Column " + column);

            if (arr[row][column] != EMPTY) {
                Toast.makeText(GamePlay.this, "This row has been played before", Toast.LENGTH_LONG).show();
                return;
            }

            makeMove(role, v, row, column);
        } catch (Exception e) {
            Toast.makeText(GamePlay.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void makeMove(long playingRole, View chosenCell, int row, int column) {
        try {
            if (arr[row][column] != EMPTY) {
                Log.w(TAG, "makeMove, move already played " + row + " " + column);
                return;
            }

            int srcID = playingRole == PLAYER_X ? R.drawable.close : R.drawable.circle;
            ((ImageButton) chosenCell).setImageResource(srcID);

            arr[row][column] = playingRole;
            Log.i(TAG, Arrays.deepToString(arr));

            if (playingRole == role) {
                HashMap<String, Object> updates = new HashMap<>();
                updates.put("game", firebaseFlattenTo1D(arr));
                Log.i(TAG, Objects.requireNonNull(updates.get("game")).toString());
                isTurn = false;

                roomRef.update(updates)
                        .addOnCompleteListener(task -> Log.i(TAG, "Game Updated"))
                        .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GamePlay.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                isTurn = true;
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private long[][] firebaseFlattenedTo2D(ArrayList<Long> array) {
        long[][] res = new long[SIZE][SIZE];
        for (int i = 0; i < array.size(); i++) {
            res[i / SIZE][i % SIZE] = array.get(i);
        }

        return res;
    }


    private ArrayList<Long> firebaseFlattenTo1D(long[][] array) {
        ArrayList<Long> res = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                res.add(array[i][j]);
            }
        }

        return res;
    }
}