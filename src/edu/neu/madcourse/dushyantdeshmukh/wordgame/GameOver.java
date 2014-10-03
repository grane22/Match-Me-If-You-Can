package edu.neu.madcourse.dushyantdeshmukh.wordgame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import edu.neu.madcourse.dushyantdeshmukh.R;

public class GameOver extends Activity implements OnClickListener {

    private static final String TAG = "Word Game";
    
    public GameOver() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wordgame_gameover);

        // Set up click listeners for all the buttons
        View restartButton = findViewById(R.id.wordgame_gameover_restart_button);
        restartButton.setOnClickListener(this);

        View quitButton = findViewById(R.id.wordgame_gameover_quit_button);
        quitButton.setOnClickListener(this);

        showDetails();
    }

    private void showDetails() {
        int totalScore = getIntent().getIntExtra(Game.PREF_CURR_SCORE, 0);
        int totalCorrectWords = getIntent().getIntExtra(
                Game.PREF_CORRECT_WORDS, 0);
        int totalIncorrectWords = getIntent().getIntExtra(
                Game.PREF_INCORRECT_WORDS, 0);
        String longestWord = getIntent().getStringExtra(Game.PREF_LONGEST_WORD);
        int accuracy = (int) getAccuracy(totalCorrectWords, totalIncorrectWords);

        TextView currScoreView = (TextView) findViewById(R.id.wordgame_gameover_score);
        currScoreView.setText("Total Score: " + totalScore);

        TextView longestWordView = (TextView) findViewById(R.id.wordgame_gameover_longestword);
        longestWordView.setText("Longest Word: " + longestWord);

        TextView accuracyView = (TextView) findViewById(R.id.wordgame_gameover_accuracy);
        accuracyView.setText("Accuracy: " + accuracy + " %");
    }

    private double getAccuracy(int totalCorrectWords, int totalIncorrectWords) {
        double accuracy = (totalCorrectWords * 100)
                / ((totalCorrectWords + totalIncorrectWords) * 1.0);
        return accuracy;
    }

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.wordgame_gameover_restart_button:
            finish();
//            WordGame wordGame = new WordGame();
//            wordGame.startGame(false);
            Intent intent = new Intent(this, Game.class);
            intent.putExtra(Game.CONTINUE_GAME, false);
            startActivity(intent);
            break;
        case R.id.wordgame_gameover_quit_button:
            Log.d(TAG, "\n Gameover Quit btn start");
            finish();
            Log.d(TAG, "\n Gameover Quit btn after 1st finish");
            finish();
            Log.d(TAG, "\n Gameover Quit btn after 2nd finish");
            break;
        }
    }
}
