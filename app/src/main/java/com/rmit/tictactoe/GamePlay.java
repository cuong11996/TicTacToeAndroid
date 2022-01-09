package com.rmit.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class GamePlay extends AppCompatActivity {
    private static final int SIZE = 3;
    private static final int PLAYER_X = 1;
    private static final int PLAYER_O = -1;
    private static final int EMPTY = 0;
    int arr[][] = new int[SIZE][SIZE];
    ImageButton[][] buttons = new ImageButton[SIZE][SIZE];
    int role = PLAYER_X;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);
        for (int i = 0;i < 3 ;i++){
            for (int j = 0;j <3 ;j++){
                String buttonID = "button" + i +j;
                int resID = getResources().getIdentifier(buttonID,"id",getPackageName());
                buttons[i][j] = findViewById(resID);
                buttons[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int srcID;
                        if (role == PLAYER_X) srcID = R.drawable.close;
                        else srcID = R.drawable.circle;
                        ((ImageButton) v).setImageResource(srcID);
                    }
                });
            }
        }
    }
}