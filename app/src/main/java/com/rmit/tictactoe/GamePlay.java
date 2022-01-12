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
import android.widget.TextView;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
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

        // Extract data from intent
        Intent intent = getIntent();
        roomId = intent.getStringExtra("roomId");
        xEmail = intent.getStringExtra("xEmail");
        yEmail = intent.getStringExtra("yEmail");
        role = yEmail.isEmpty() ? PLAYER_X : PLAYER_O;

        isTurn = role == PLAYER_X;
        Log.i(TAG, xEmail + " " + yEmail + " " + role);

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
                    String newYEmail = (String) currentData.get("yEmail");
                    Log.i(TAG, "Received new YEmail " + newYEmail);

                    if (newYEmail.isEmpty() || newYEmail == null) {
                        return;
                    }

                    yEmail = newYEmail;
                    db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document: task.getResult()) {
                                    if (document.getId().equals(xEmail)) {
                                        String xUsername = (String) document.get("fullName");
                                        TextView xUsernameText = (TextView) findViewById(R.id.xUsername);
                                        xUsernameText.setText(xUsername);

                                        String xWinNo = (String) document.get("winNo");
                                        int xLostNo = (Integer.parseInt((String) document.get("matchNo")) - Integer.parseInt(xWinNo));

                                        TextView xWinNoText = (TextView) findViewById(R.id.xWinNo);
                                        TextView xLostNoText = (TextView) findViewById(R.id.xLoseNo);
                                        xWinNoText.setText(xWinNo);
                                        xLostNoText.setText(xLostNo);
                                    }

                                    if (document.getId().equals(yEmail)) {
                                        String yUsername = (String) document.get("fullName");
                                        TextView yUsernameText = (TextView) findViewById(R.id.yUsername);
                                        yUsernameText.setText(yUsername);

                                        String yWinNo = (String) document.get("winNo");
                                        int yLostNo = (Integer.parseInt((String) document.get("matchNo")) - Integer.parseInt(yWinNo));

                                        TextView yWinNoText = (TextView) findViewById(R.id.yWinNo);
                                        TextView yLostNoText = (TextView) findViewById(R.id.yLoseNo);
                                        yWinNoText.setText(yWinNo);
                                        yLostNoText.setText(yLostNo);
                                    }
                                }
                            } else {
                                Toast.makeText(GamePlay.this, "Fetch users failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    // TODO: Update user data

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
            if (yEmail.isEmpty()) {
                Toast.makeText(GamePlay.this, "The other player has not entered", Toast.LENGTH_LONG).show();
                return;
            }

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

            int winner = getWinner();
            if (winner == role) handleWin();
            else if (winner == -role) handleLose();

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void handleLose() {
        Toast.makeText(this, "You lost!", Toast.LENGTH_LONG).show();
    }


    private void handleWin() {
        Toast.makeText(this, "You won!", Toast.LENGTH_LONG).show();
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


    private int getWinner() {
        // Horizontal
        for (int row = 0; row < SIZE; row++) {
            long rowSum = arr[row][0] + arr[row][1] + arr[row][2];
            if (rowSum == 3 * PLAYER_X) return PLAYER_X;
            if (rowSum == 3 * PLAYER_O) return PLAYER_O;
        }

        // Vertical
        for (int column = 0; column < SIZE; column++) {
            long columnSum = arr[0][column] + arr[1][column] + arr[2][column];
            if (columnSum == 3 * PLAYER_X) return PLAYER_X;
            if (columnSum == 3 * PLAYER_O) return PLAYER_O;
        }

        long mainDiagonalSum = arr[0][0] + arr[1][1] + arr[2][2];
        if (mainDiagonalSum == 3 * PLAYER_X) return PLAYER_X;
        if (mainDiagonalSum == 3 * PLAYER_O) return PLAYER_O;

        long secondaryDiagonalSum = arr[2][0] + arr[1][1] + arr[0][2];
        if (secondaryDiagonalSum == 3 * PLAYER_X) return PLAYER_X;
        if (secondaryDiagonalSum == 3 * PLAYER_O) return PLAYER_O;

        return EMPTY;
    }
}