package edu.neu.madcourse.dushyantdeshmukh.wordgame;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.wordgame.Prefs;
import edu.neu.madcourse.dushyantdeshmukh.wordgame.Game;
import edu.neu.madcourse.dushyantdeshmukh.wordgame.Acknowledgements;
import android.view.View.OnClickListener;

public class WordGame extends Activity implements OnClickListener {

  private static final String TAG = "Word Game";

  private MediaPlayer mpMenuMusic;
  private int menuMusicResId = R.raw.wordgame_menu_music;
  private boolean playBgMusic = false;

  private boolean isContinueAvailable;

  public WordGame() {
    // TODO Auto-generated constructor stub
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.wordgame_main);

    // Set up click listeners for all the buttons
    View newgameButton = findViewById(R.id.wordgame_newgame_button);
    newgameButton.setOnClickListener(this);

    View continueButton = findViewById(R.id.wordgame_continue_button);
    continueButton.setOnClickListener(this);
    
    View ackButton = findViewById(R.id.wordgame_ack_button);
    ackButton.setOnClickListener(this);

    View instructionsButton = findViewById(R.id.wordgame_instructions_button);
    instructionsButton.setOnClickListener(this);

    View settingsButton = findViewById(R.id.wordgame_settings_button);
    settingsButton.setOnClickListener(this);

    View returnButton = findViewById(R.id.wordgame_return_button);
    returnButton.setOnClickListener(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    playBgMusic = Prefs.getMusic(this);

    this.isContinueAvailable = getSharedPreferences("WORD_GAME", MODE_PRIVATE).getBoolean(
        Game.PREF_CONTINUE_GAME, false);
    
    View continueButton = findViewById(R.id.wordgame_continue_button);
    if (isContinueAvailable) {
      continueButton.setVisibility(View.VISIBLE); 
    } else {
      continueButton.setVisibility(View.GONE);
    }
    
    if (playBgMusic) {
      if (mpMenuMusic != null) {
        mpMenuMusic.release();
      }
      // Create a new MediaPlayer to play this sound
      mpMenuMusic = MediaPlayer.create(this, menuMusicResId);
      mpMenuMusic.start();
      mpMenuMusic.setLooping(true);
    }
  }

  protected void onPause() {
    super.onPause();
    if (mpMenuMusic != null) {
      mpMenuMusic.release();
    }
  }

  // @Override
  // public boolean onCreateOptionsMenu(Menu menu) {
  // super.onCreateOptionsMenu(menu);
  // MenuInflater inflater = getMenuInflater();
  // inflater.inflate(R.menu.wordgame_menu, menu);
  // return true;
  // }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.wordgame_newgame_button:
      startGame(false);
      break;
    case R.id.wordgame_continue_button:
      startGame(true);
      break;
    case R.id.wordgame_settings_button:
      startActivity(new Intent(this, Prefs.class));
      break;
    case R.id.wordgame_ack_button:
      Intent i = new Intent(this, Acknowledgements.class);
      startActivity(i);
      break;
    case R.id.wordgame_instructions_button:
      Intent i2 = new Intent(this, Instructions.class);
      startActivity(i2);
      break;
    case R.id.wordgame_return_button:
      if (mpMenuMusic != null) {
        mpMenuMusic.release();
      }
      finish();
      break;
    }
  }

  /** Start a new game with the given difficulty level */
  protected void startGame(boolean continueGame) {
    // Log.d(TAG, "clicked on " + i);
    Intent intent = new Intent(this, Game.class);
    intent.putExtra(Game.CONTINUE_GAME, continueGame);
    startActivity(intent);
  }
}
