package edu.neu.madcourse.dushyantdeshmukh.wordgame;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.utilities.BloomFilter;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class Game extends Activity implements OnClickListener {

  private static final String TAG = "Word Game";
  // public static final String KEY_DIFFICULTY = "wordgame_dificulty";
  public static final int DIFFICULTY_EASY = 1;
  public static final int DIFFICULTY_MEDIUM = 2;
  public static final int DIFFICULTY_HARD = 3;

  public static final int DIFFICULTY_EASY_TIME_INTERVAL = 1800;
  public static final int DIFFICULTY_MEDIUM_TIME_INTERVAL = 1200;
  public static final int DIFFICULTY_HARD_TIME_INTERVAL = 600;

  public static final String CONTINUE_GAME = "Continue_Game";

  private static final String PREF_BOARD_STATE = "BOARD_STATE";
  protected static final String PREF_CURR_SCORE = "CURR_SCORE";
  protected static final String PREF_CORRECT_WORDS = "CORRECT_WORDS";
  protected static final String PREF_INCORRECT_WORDS = "INCORRECT_WORDS";
  protected static final String PREF_LONGEST_WORD = "LONGEST_WORD";
  protected static final String PREF_GAME_OVER = "GAME_OVER";
  protected static final String PREF_CONTINUE_GAME = "CONTINUE_GAME";

  public int total_rows = 7;
  public int total_cols = 5;
  private int totalCells = 35;
  private int totalLetters = 0;

  private boolean playBgMusic = false;
  private boolean showHint = false;

  public char board[][];
  protected static boolean isPaused = false;
  private boolean overrideAndContinue = false;

  private String currWord = "";
  protected HashSet<String> currSelections = new HashSet<String>();

  private BloomFilter<String> bloomFilter;
  private HashSet<String> wordList = new HashSet<String>();

  private boolean isCurrWordValid = false;

  private int currScore = 0;
  private int totalCorrectWords = 0;
  private int totalIncorrectWords = 0;
  private String longestWord = "";

  private char letterSet1[] = { 'B', 'C', 'D', 'G', 'H', 'K', 'L', 'M', 'N',
      'P', 'R', 'S', 'T' };
  private char letterSet2[] = { 'A', 'E', 'I', 'O', 'U' };
  private char letterSet3[] = { 'F', 'J', 'V', 'Q', 'W', 'X', 'Y', 'Z' };
  private int letterSetCount = 0;

  private MediaPlayer mpValidWord;
  private int validWordBeepResId = R.raw.valid_word_beep;

  private MediaPlayer mpInvalidWord;
  private int invalidWordBeepResId = R.raw.invalid_word_beep;

  private MediaPlayer mpBgMusic;
  private int bgMusicResId = R.raw.wordgame_bg_music;

  private MediaPlayer mpGameOver;
  private int gameOverResId = R.raw.game_over_gong;

  private MediaPlayer mpCountDown;
  private int countDownResId = R.raw.count_down;

  private boolean gameOver = false;

  Timer myTimer = new Timer();
  final Handler myTimerHandler = new Handler();

  int newLetterInterval = 2300;

  TimerTask newLetterTimerTask;

  final Runnable newLetterRunnable = new Runnable() {
    public void run() {
      // introduce a new letter
      char newLetter = getNewLetter();
      Log.d(TAG, "Inserting new letter: " + newLetter);
      insertNewLetter(newLetter);

    }

  };

  private void chkCountdown() {
    if (mpCountDown != null) {
      mpCountDown.release();
    }
    if (totalLetters >= (totalCells - (2 * total_cols) - 1)) {
      // Log.d(TAG, "totalLetters = " + totalLetters);
      // Log.d(TAG, "totalCells = " + totalCells);
      // Log.d(TAG, "total_cols = " + total_cols);
      // Create a new MediaPlayer to play this sound
      mpCountDown = MediaPlayer.create(this, countDownResId);
      mpCountDown.start();
      mpCountDown.setLooping(true);
    }
  }

  public Game() {

  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    total_rows = Prefs.getRows(this);
    total_cols = Prefs.getCols(this);
    totalCells = total_rows * total_cols;

    Log.d(TAG, "Loading dictionary from file...");
    loadBitsetFromFile("compressedWordlist.txt");

    newLetterInterval = getNewLetterInterval(Prefs.getDifficultyLevel(this));

    checkAndHandleContinueGame();

    setContentView(R.layout.wordgame_game);

    // set board size on screen
    DisplayMetrics displaymetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    int height = displaymetrics.heightPixels;
    int width = displaymetrics.widthPixels;
    Log.d(TAG, "height = " + height + ", width = " + width);
    BoardView boardView = (BoardView) findViewById(R.id.wordgame_board_view);
    boardView.setLayoutParams(new LayoutParams((width * 9) / 10,
        (height * 65) / 100));

    // Set up click listeners for all the buttons

    View clearButton = findViewById(R.id.wordgame_clear_button);
    clearButton.setOnClickListener(this);

    View currwordButton = findViewById(R.id.wordgame_currword_button);
    currwordButton.setOnClickListener(this);

    View pauseButton = findViewById(R.id.wordgame_pause_button);
    pauseButton.setOnClickListener(this);

    View hintButton = findViewById(R.id.wordgame_hint_button);
    hintButton.setOnClickListener(this);

    View quitButton = findViewById(R.id.wordgame_quit_button);
    quitButton.setOnClickListener(this);
  }

  private void checkAndHandleContinueGame() {
    boolean continueGame = getIntent().getBooleanExtra(CONTINUE_GAME, false);

    if (overrideAndContinue) {
      continueGame = true;
      overrideAndContinue = false;
    }
    if (continueGame) {
      // continue game
      this.gameOver = getPreferences(MODE_PRIVATE).getBoolean(PREF_GAME_OVER,
          false);
      restoreState();
    } else {
      board = getInitialBoard();
    }
  }

  private void restoreState() {
    SharedPreferences savedState = getPreferences(MODE_PRIVATE);

    this.currScore = savedState.getInt(PREF_CURR_SCORE, 0);
    this.totalCorrectWords = savedState.getInt(PREF_CORRECT_WORDS, 0);
    this.totalIncorrectWords = savedState.getInt(PREF_INCORRECT_WORDS, 0);
    this.longestWord = savedState.getString(PREF_LONGEST_WORD, "");

    String savedBoardStr = savedState.getString(PREF_BOARD_STATE, null);
    if (this.gameOver || savedBoardStr == null) {
      board = getInitialBoard();
    } else {
      board = fromBoardString(savedBoardStr);
    }
  }

  private int getNewLetterInterval(int diff) {
    int timeInterval;
    switch (diff) {
    case DIFFICULTY_HARD:
      timeInterval = DIFFICULTY_HARD_TIME_INTERVAL;
      break;
    case DIFFICULTY_MEDIUM:
      timeInterval = DIFFICULTY_MEDIUM_TIME_INTERVAL;
      break;
    case DIFFICULTY_EASY:
    default:
      timeInterval = DIFFICULTY_EASY_TIME_INTERVAL;
    }
    return timeInterval;
  }

  @Override
  protected void onResume() {
    super.onResume();
    playBgMusic = Prefs.getMusic(this);
    showHint = Prefs.getHints(this);
    playBgMusic();
    checkAndHandleContinueGame();
    this.gameOver = false;
    if (!isPaused) {
      startNewLetterTimer();
    }
  }

  private void playBgMusic() {
    if (playBgMusic) {
      if (mpBgMusic != null) {
        mpBgMusic.release();
      }
      // Create a new MediaPlayer to play this sound
      mpBgMusic = MediaPlayer.create(this, bgMusicResId);
      mpBgMusic.start();
      mpBgMusic.setLooping(true);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    Log.d(TAG, "\n onPause() start, this.gameOver=" + this.gameOver);
    stopNewLetterTimer();
    if (mpCountDown != null) {
      mpCountDown.release();
    }
    if (mpBgMusic != null) {
      mpBgMusic.release();
    }
    // Save current state of the board
    storeCurrState();
    Log.d(TAG, "\n onPause() end, this.gameOver=" + this.gameOver);
    // String boardStr = toBoardString(board);
    // Log.d(TAG, "toBoardString(board): " + toBoardString(board));
    // Log.d(TAG, "fromBoardString(boardStr): " +
    // fromBoardString(boardStr));

  }

  private void storeCurrState() {
    getPreferences(MODE_PRIVATE).edit()
        .putString(PREF_BOARD_STATE, toBoardString(this.board)).commit();
    getPreferences(MODE_PRIVATE).edit().putInt(PREF_CURR_SCORE, this.currScore)
        .commit();
    getPreferences(MODE_PRIVATE).edit()
        .putInt(PREF_CORRECT_WORDS, this.totalCorrectWords).commit();
    getPreferences(MODE_PRIVATE).edit()
        .putInt(PREF_INCORRECT_WORDS, this.totalIncorrectWords).commit();
    getPreferences(MODE_PRIVATE).edit()
        .putString(PREF_LONGEST_WORD, this.longestWord).commit();

    getSharedPreferences("WORD_GAME", MODE_PRIVATE).edit().putBoolean(PREF_CONTINUE_GAME, !this.gameOver)
        .commit();
  }

  private String toBoardString(char[][] boardCharArr) {
    String boardStr = "";
    for (int i = 0; i < total_rows - 1; i++) {
      String currRow = new String(boardCharArr[i]);
      boardStr += currRow + ",";
    }
    boardStr += new String(boardCharArr[total_rows - 1]);
    return boardStr;
  }

  private char[][] fromBoardString(String boardStr) {
    char[][] boardCharArr = new char[total_rows][total_cols];
    String tempStrArr[] = boardStr.split(",");
    for (int i = total_rows - 1; i >= 0; i--) {
      String currRow = tempStrArr[i];
      for (int j = 0; j < total_cols; j++) {
        char currChar = currRow.charAt(j);
        if (currChar != ' ') {
          boardCharArr[i][j] = currRow.charAt(j);
        } else {
          boardCharArr[i][j] = '\u0000';
        }
      }
    }
    return boardCharArr;
  }

  protected void insertNewLetter(char newLetter) {
    for (int i = total_rows - 1; i >= 0; i--) {
      for (int j = 0; j < total_cols; j++) {
        if (board[i][j] == '\u0000') {
          board[i][j] = newLetter;
          BoardView boardView = (BoardView) findViewById(R.id.wordgame_board_view);
          boardView.invalidateRect(j, i);

          totalLetters++;
          chkCountdown();
          return;
        }
      }
    }
    // Game over!!!
    // Log.d(TAG, "\n Gameover case start, this.gameOver= " +
    // this.gameOver);
    this.gameOver = true;
//    getSharedPreferences("WORD_GAME", MODE_PRIVATE).edit().putBoolean(PREF_CONTINUE_GAME, false)
//        .commit();
    stopNewLetterTimer();
    if (mpCountDown != null) {
      mpCountDown.release();
    }
    playSound(mpGameOver, gameOverResId, false);

    Intent i = new Intent(this, GameOver.class);
    i.putExtra(PREF_CURR_SCORE, this.currScore);
    i.putExtra(PREF_LONGEST_WORD, this.longestWord);
    i.putExtra(PREF_CORRECT_WORDS, this.totalCorrectWords);
    i.putExtra(PREF_INCORRECT_WORDS, this.totalIncorrectWords);
    startActivity(i);
    finish();
  }

  /**
   * Selects a letter from the 3 sets in the following ratio letterSet1 :
   * letterSet2 : letterSet3 = 4 : 4 : 1
   * 
   * @return
   */
  protected char getNewLetter() {
    char retChar;
    letterSetCount = (letterSetCount + 1) % 9;
    if (letterSetCount == 1 || letterSetCount == 3 || letterSetCount == 5
        || letterSetCount == 7) {
      // get next letter from set 1
      retChar = getRandomChar(this.letterSet1);
    } else if (letterSetCount == 2 || letterSetCount == 4
        || letterSetCount == 6 || letterSetCount == 8) {
      // get next letter from set 2
      retChar = getRandomChar(this.letterSet2);
    } else {
      // get next letter from set 3
      retChar = getRandomChar(this.letterSet3);
    }
    return retChar;
  }

  private char getRandomChar(char[] letterSet) {
    int randomIndex = (int) (Math.random() * letterSet.length);
    return letterSet[randomIndex];
  }

  private void startNewLetterTimer() {
    newLetterTimerTask = new TimerTask() {
      @Override
      public void run() {
        myTimerHandler.post(newLetterRunnable);
      }
    };
    myTimer.schedule(newLetterTimerTask, 0, newLetterInterval);
  }

  private void stopNewLetterTimer() {
    newLetterTimerTask.cancel();
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.wordgame_clear_button:
      if (isPaused) {
        return;
      }
      Button currwordButton = (Button) findViewById(R.id.wordgame_currword_button);
      currwordButton.setText("");
      this.currWord = "";
      BoardView boardView = (BoardView) findViewById(R.id.wordgame_board_view);
      HashSet<String> tempRectList = currSelections;
      boardView.inValidateMultipleRects(tempRectList);
      this.currSelections.clear();
      break;
    case R.id.wordgame_currword_button:
      if (isPaused) {
        return;
      }
      if (isCurrWordValid) {
        processValidWord(v);
      } else {
        playSound(mpInvalidWord, invalidWordBeepResId, false);
        this.totalIncorrectWords++;
        ((Button) v).setTextColor(Color.RED);
      }
      break;
    case R.id.wordgame_pause_button:
//      isPaused = true;
//      Intent i = new Intent(this, PauseDialog.class);
//      startActivity(i);
      BoardView boardView2 = (BoardView) findViewById(R.id.wordgame_board_view);
      if (isPaused) {
        isPaused = false;
        if (playBgMusic) {
          playBgMusic();
        }
        startNewLetterTimer();
        ((Button) v).setText(R.string.wordgame_pause);
        boardView2.setVisibility(View.VISIBLE);
      } else {
        isPaused = true;
        if (mpBgMusic != null) {
          mpBgMusic.release();
        }
        if (mpCountDown != null) {
          mpCountDown.release();
        }
        stopNewLetterTimer();
        ((Button) v).setText(R.string.wordgame_resume);
        boardView2.setVisibility(View.INVISIBLE);
      }
      break;
    case R.id.wordgame_hint_button:
      if (showHint) {
        // stopNewLetterTimer();
        Intent i2 = new Intent(this, Hints.class);
        i2.putExtra(Game.PREF_BOARD_STATE, toBoardString(board));
        // i.putExtra(Game.CONTINUE_GAME, true);
        overrideAndContinue = true;
        startActivity(i2);
      }
      break;
    case R.id.wordgame_quit_button:
      getPreferences(MODE_PRIVATE).edit()
          .putBoolean(Game.PREF_GAME_OVER, false).commit();
      finish();
      break;
    }
  }

  private void processValidWord(View v) {
    playSound(mpValidWord, validWordBeepResId, false);
    String currWord = ((Button) v).getText().toString();
    addWord(currWord);
    ((Button) v).setText("");
    this.currWord = "";
    int currWordLength = currWord.length();
    this.currScore += currWordLength;
    TextView currScoreView = (TextView) findViewById(R.id.wordgame_currscore);
    currScoreView.setText("Score: " + currScore);
    if (currWordLength > this.longestWord.length()) {
      this.longestWord = currWord;
    }
    this.totalCorrectWords++;
    Log.d(TAG, "old totalLetters = " + totalLetters);
    Log.d(TAG, "totalLetters = " + totalLetters);
    this.totalLetters = this.totalLetters - currWordLength;
    Log.d(TAG, "new totalLetters = " + totalLetters);
    removeCurrWordLetters();
  }

  private void removeCurrWordLetters() {
    Iterator<String> iterator = this.currSelections.iterator();
    BoardView boardView = (BoardView) findViewById(R.id.wordgame_board_view);
    while (iterator.hasNext()) {
      String tempStrArr[] = iterator.next().split(",");
      int i = Integer.parseInt(tempStrArr[0]);
      int j = Integer.parseInt(tempStrArr[1]);
      board[i][j] = '\u0000';
      boardView.invalidateRect(j, i);
    }
    this.currSelections.clear();

  }

  private char[][] getInitialBoard() {
    board = new char[total_rows][total_cols];
    for (int j = 0; j < total_cols; j++) {
      board[total_rows - 1][j] = getNewLetter();
    }
    return board;
  }

  public void selectLetter(int row, int col) {
    // Check if letter exists and not already selected
    if (board[row][col] != '\u0000'
        && !currSelections.contains(row + "," + col)) {
      Log.d(TAG, "Selected letter '" + board[row][col] + "'");
      // add letter to currWord
      Button currWordBtn = (Button) findViewById(R.id.wordgame_currword_button);
      currWord = currWordBtn.getText().toString();
      currWord += board[row][col];
      currWordBtn.setText(currWord);
      // store (r, c) in currSelections list
      currSelections.add(row + "," + col);
      if (currWord.length() > 2 && isWordValid(currWord)) {
        currWordBtn.setTextColor(Color.GREEN);
        isCurrWordValid = true;
      } else {
        isCurrWordValid = false;
        currWordBtn.setTextColor(getResources().getColor(R.color.board_letter));
      }
    }
  }

  protected boolean isWordValid(String ipWord) {
    ipWord = ipWord.toLowerCase();
    if (bloomFilter.contains(ipWord) && !wordList.contains(ipWord)) {
      Log.d(TAG, ipWord + " is a valid word.");
      // playValidWordBeep();
      // addWord(ipWord);
      return true;
    } else {
      Log.d(TAG, ipWord + " is an invalid word.");
      return false;
    }
  }

  private void addWord(String ipWord) {
    wordList.add(ipWord);
    // renderWorList();
  }

  protected void playSound(MediaPlayer mp, int soundResId, boolean loop) {
    // Release any resources from previous MediaPlayer
    if (mp != null) {
      mp.release();
    }
    // Create a new MediaPlayer to play this sound
    mp = MediaPlayer.create(this, soundResId);
    mp.start();
    mp.setLooping(loop);
  }

  private BloomFilter<String> loadBitsetFromFile(String filepath) {
    try {
      AssetManager am = this.getAssets();
      int fileLength = (int) am.openFd(filepath).getLength();
      Log.d(TAG, "compressed file length = " + fileLength);
      InputStream is = am.open(filepath);

      byte[] fileData = new byte[fileLength];
      DataInputStream dis = new DataInputStream(is);
      dis.readFully(fileData);
      dis.close();
      bloomFilter = new BloomFilter<String>(0.0001, 450000);
      bloomFilter = BloomFilter.loadBitsetWithByteArray(fileData, bloomFilter);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return bloomFilter;
  }
}
