package edu.neu.madcourse.dushyantdeshmukh.two_player_wordgame;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.games.leaderboard.ScoreSubmissionData;

import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.utilities.BloomFilter;
import edu.neu.madcourse.dushyantdeshmukh.utilities.Util;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class Game extends Activity implements OnClickListener {

  private static final String TAG = "Game Activity";
  // public static final String KEY_DIFFICULTY = "wordgame_dificulty";
  public static final int DIFFICULTY_EASY = 1;
  public static final int DIFFICULTY_MEDIUM = 2;
  public static final int DIFFICULTY_HARD = 3;

  public static final int DIFFICULTY_EASY_TIME_INTERVAL = 1800;
  public static final int DIFFICULTY_MEDIUM_TIME_INTERVAL = 1200;
  public static final int DIFFICULTY_HARD_TIME_INTERVAL = 600;

  public static final String CONTINUE_GAME = "Continue_Game";

  public int total_rows = 7;
  public int total_cols = 5;
  private int totalCells = 35;
  private int totalLetters = 0;

  private boolean playBgMusic = false;
  private boolean showHint = false;

  public char board[][];
  public static boolean isPaused = false;
  public static boolean autoOnPause = false;
  // private boolean overrideAndContinue = false;

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

  public static String topScorersFormatedStr = "";
  
  private String username, regId, opponentName, oppRegId;
  private boolean gameOver = false;
  private boolean isPlayerOne = false;
  private int roundNo = 0;
  private int oppScore = 0;
  private boolean isMyTurn = false;
  
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

  final Runnable updateTimerRunnable = new Runnable() {
    public void run() {
      // introduce a new letter
      char newLetter = getNewLetter();
      Log.d(TAG, "Inserting new letter: " + newLetter);
      insertNewLetter(newLetter);

    }

  };

  // Timer to check remaining time in the game
  protected long timeRemaining = Constants.TIME_PER_ROUND_IN_SECS * 1000;

  CountDownTimer timeRemainingCounter;

  public class TimeRemainingCounter extends CountDownTimer {
    public TimeRemainingCounter(long millisInFuture, long countDownInterval) {
      super(millisInFuture, countDownInterval);
    }

    @Override
    public void onFinish() {
      // showDebugToast("Calling endRound() from onFininsh()");
      TextView timeTextView = (TextView) findViewById(R.id.two_player_wordgame_time);
      timeTextView.setText("Time: 0 sec(s)");
      endRound();
    }

    @Override
    public void onTick(long millisUntilFinished) {
      Log.d(TAG, "Inside onTick()");
      timeRemaining = millisUntilFinished;
      TextView timeTextView = (TextView) findViewById(R.id.two_player_wordgame_time);
      timeTextView.setText("Time: " + timeRemaining / 1000 + " sec(s)");
    }
  }

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

  BroadcastReceiver receiver;
  private String scoreboardStr = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    // This will handle the broadcast
    receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Inside onReceive of Broadcast receiver of Game.class");
        String action = intent.getAction();
        if (action.equals(Constants.INTENT_ACTION_2P_WORD_GAME)) {
          String data = intent.getStringExtra("data");
          Log.d(TAG, "data = " + data);
          handleOpponentResponse(data);
        }
      }
    };

    total_rows = Prefs.getRows(this);
    total_cols = Prefs.getCols(this);
    totalCells = total_rows * total_cols;

    Log.d(TAG, "Loading dictionary from file...");
    bloomFilter = Util.loadBitsetFromFile("compressedWordlist.txt",
        this.getAssets());

    newLetterInterval = getNewLetterInterval(Prefs.getDifficultyLevel(this));

    setContentView(R.layout.two_player_wordgame_game);

    // set board size on screen
    setBoardSizeOnScreen();

    SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF_CONST,
        Context.MODE_PRIVATE);
    this.username = sp.getString(Constants.PREF_USERNAME, "");
    this.regId = sp.getString(Constants.PREF_REG_ID, "");
    this.opponentName = sp.getString(Constants.PREF_OPPONENT_NAME, "");
    this.oppRegId = sp.getString(Constants.PREF_OPPONENT_REG_ID, "");
//    Log.d(TAG, "\n\n Retrieved this.oppRegId in SP: " + this.oppRegId + "\n\n");
//    showDebugToast("this.oppRegId = " + this.oppRegId);

    // evaluate extras
    Intent intent = getIntent();
    this.roundNo = intent.getIntExtra(Constants.EXTRA_ROUND, 0);
    this.isPlayerOne = intent.getBooleanExtra(Constants.EXTRA_IS_PLAYER_ONE,
        false);
    int oppScore = -1;

    Log.d(TAG, "\n OnCreate() Evaluating extras");
    Log.d(TAG, "\n roundNo= " + roundNo);
    Log.d(TAG, "\n isPlayerOne= " + isPlayerOne);

    if (this.roundNo > 0) {
      if (this.isPlayerOne) {
        this.roundNo += 1;
      }
      this.isMyTurn = true;
      sp.edit().putBoolean(Constants.PREF_IS_MY_TURN, true).commit();
      oppScore = intent.getIntExtra(Constants.EXTRA_OPP_CURR_SCORE, 0);
      updateScoreboard(1, roundNo, oppScore);
    } else {
      this.roundNo = 1;
      if (this.isPlayerOne) {
        this.isMyTurn = true;
        sp.edit().putBoolean(Constants.PREF_IS_MY_TURN, true).commit();
      } else {
        this.isMyTurn = false;
        sp.edit().putBoolean(Constants.PREF_IS_MY_TURN, false).commit();
      }
    }
    sp.edit().putInt(Constants.PREF_CURR_ROUND_NO, this.roundNo).commit();

//    showDebugToast("OnCreate()\n roundNo: " + roundNo + "\n isPlayerOne: "
//        + isPlayerOne + "\n isMyTurn: " + isMyTurn + "\n oppScore: " + oppScore);

    // checkAndHandleContinueGame();

    // Set up click listeners for all the buttons

    View clearButton = findViewById(R.id.two_player_wordgame_clear_button);
    clearButton.setOnClickListener(this);

    View currwordButton = findViewById(R.id.two_player_wordgame_currword_button);
    currwordButton.setOnClickListener(this);

    View pauseButton = findViewById(R.id.two_player_wordgame_pause_button);
    pauseButton.setOnClickListener(this);

    // View hintButton = findViewById(R.id.two_player_wordgame_hint_button);
    // hintButton.setOnClickListener(this);

    View quitButton = findViewById(R.id.two_player_wordgame_quit_button);
    quitButton.setOnClickListener(this);
  }

  protected void handleOpponentResponse(String data) {
    Log.d(TAG, "Inside handleOpponentResponse()");
    HashMap<String, String> dataMap = Util.getDataMap(data, TAG);
    
    if (dataMap.containsKey(Constants.KEY_MSG_TYPE)) {
      String msgType = dataMap.get(Constants.KEY_MSG_TYPE);
      Log.d(TAG, Constants.KEY_MSG_TYPE + ": " + msgType);
      if (msgType.equals(Constants.MSG_TYPE_2P_MOVE)) {
        this.roundNo = Integer.parseInt(dataMap.get(Constants.KEY_ROUND));
        this.oppScore = Integer.parseInt(dataMap.get(Constants.KEY_OPP_SCORE));

        if (isPlayerOne && roundNo == Constants.NO_OF_ROUNDS) {
          //  Game end
          updateScoreboard(1, roundNo, this.oppScore);
          showDebugToast("'" + this.opponentName + "' made " + oppScore + " points in round " + roundNo+ ".");
          //  show game end dialog
          showGameEndDialog();
          // Add urself to AVAILABLE_USERS_LIST
          Util.addValuesToKeyOnServer(Constants.AVAILABLE_USERS_LIST,
              this.username, this.regId);
          // Clear opp name and Id from SP
          clearOppFromSP();
        } else {
          //  show move dialog
          showMoveDialog(roundNo);
        }
      } else if (msgType.equals(Constants.MSG_TYPE_2P_QUIT)) {
          showDebugToast("'" + this.opponentName + "' has quit the game.");

          // Add urself to AVAILABLE_USERS_LIST
          Util.addValuesToKeyOnServer(Constants.AVAILABLE_USERS_LIST,
              username, regId);
          
          // Clear opp name and Id from SP
          clearOppFromSP();
          
          //  Go back to Choose Opponent activity
          Intent i = new Intent(Game.this, ChooseOpponent.class);
          i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
              | Intent.FLAG_ACTIVITY_SINGLE_TOP);
          startActivity(i);
        }
    }
  }

  private void clearOppFromSP() {
    Editor e = getSharedPreferences(Constants.SHARED_PREF_CONST,
        Context.MODE_PRIVATE).edit();
    e.putString(Constants.PREF_OPPONENT_NAME, "");
    e.putString(Constants.PREF_OPPONENT_REG_ID, "");
    e.commit();
  }

  private void showMoveDialog(int roundNo) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setCancelable(true);
    builder.setTitle("Opponent's Move");
    builder.setMessage("'" + this.opponentName + "' made " + oppScore + " points in round " + roundNo+ ". \n Your turn.");
//    builder.setInverseBackgroundForced(true);
    builder.setPositiveButton("Play", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        playMove();
      }
    });
    
    AlertDialog alert = builder.create();
    alert.show();
  }
  
  protected void playMove() {
    SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF_CONST,
        Context.MODE_PRIVATE);
    if (this.isPlayerOne) {
      this.roundNo += 1;
    }
    this.isMyTurn = true;
    sp.edit().putBoolean(Constants.PREF_IS_MY_TURN, true).commit();
    updateScoreboard(1, roundNo, this.oppScore);
    updateScoreOnUI();
    //setupInitialBoard();
    showBoard(false);
  }

  private boolean isMyTurn() {
    SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF_CONST,
        Context.MODE_PRIVATE);
    this.isMyTurn = sp.getBoolean(Constants.PREF_IS_MY_TURN, false);
    return this.isMyTurn;
  }

  private void showBoard(boolean paused) {
    // showDebugToast("Inside showBoard(), isMyTurn() returned " + isMyTurn()
    // + "\n paused: " + paused);
    BoardView boardView = (BoardView) findViewById(R.id.two_player_wordgame_board_view);
    TextView noboardTextView = (TextView) findViewById(R.id.two_player_wordgame_no_board_textview);

    if (timeRemainingCounter != null) {
      timeRemainingCounter.cancel();
    }
    stopNewLetterTimer();

    if (paused) {
      // paused
      boardView.setVisibility(View.GONE);
      noboardTextView.setVisibility(View.VISIBLE);
      noboardTextView.setText("GAME is PAUSED.");
    } else {
      if (isMyTurn()) {

        // My turn to play
        boardView.setVisibility(View.VISIBLE);
        noboardTextView.setVisibility(View.GONE);
        timeRemainingCounter = new TimeRemainingCounter(timeRemaining, 1000);
        timeRemainingCounter.start();
        startNewLetterTimer();
      } else {
        // waiting for opponent
        boardView.setVisibility(View.GONE);
        noboardTextView.setVisibility(View.VISIBLE);
        noboardTextView.setText("Waiting for opponent's turn to complete.");
      }
    }
  }

  private void setBoardSizeOnScreen() {
    DisplayMetrics displaymetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    int height = displaymetrics.heightPixels;
    int width = displaymetrics.widthPixels;
    Log.d(TAG, "height = " + height + ", width = " + width);
    BoardView boardView = (BoardView) findViewById(R.id.two_player_wordgame_board_view);
    boardView.setLayoutParams(new LayoutParams((width * 9) / 10,
        (height * 65) / 100));
    TextView noboardTextView = (TextView) findViewById(R.id.two_player_wordgame_no_board_textview);
    noboardTextView.setLayoutParams(new LayoutParams((width * 9) / 10,
        (height * 65) / 100));
  }

  /**
   * 
   * @param playerIndex
   *          - 0 means my score, 1 means opponent score
   * @param roundNo
   * @param ocurrScore
   */
  private void updateScoreboard(int playerIndex, int roundNo, int currScore) {
    Log.d(TAG, "Inside updateScoreboard() ");
    SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF_CONST,
        Context.MODE_PRIVATE);
    this.scoreboardStr = sp.getString(Constants.PREF_CURR_SCOREBOARD, "");

    Log.d(TAG, "initial scoreboardStr = " + scoreboardStr);

    int[][] scoreboardArr = Util.scoreboardStrToArr(scoreboardStr);
    scoreboardArr[roundNo - 1][playerIndex] = currScore;

    this.scoreboardStr = Util.scoreboardArrToStr(scoreboardArr);

    Log.d(TAG, "Udated scoreboardStr = " + scoreboardStr);

    sp.edit().putString(Constants.PREF_CURR_SCOREBOARD, this.scoreboardStr).commit();
  }

  private void checkAndHandleContinueGame() {
    boolean continueGame = getIntent().getBooleanExtra(CONTINUE_GAME, false);
    boolean initiateGame = getIntent().getBooleanExtra(
        Constants.EXTRA_INITIATE_GAME, false);

     boolean autoOnPause = getPreferences(MODE_PRIVATE).getBoolean(Constants.PREF_AUTO_ON_PAUSE, false);

//    showDebugToast("onResume(): autoOnPause - " + autoOnPause
//        + "\n isPaused - " + isPaused);

    if (!initiateGame && (this.autoOnPause || isPaused)) {
      continueGame = true;
//      this.autoOnPause = false;
      // getPreferences(MODE_PRIVATE).edit().putBoolean(Constants.PREF_AUTO_ON_PAUSE,
      // false).commit();
    }
    if (continueGame) {
      // continue game
      this.gameOver = getPreferences(MODE_PRIVATE).getBoolean(
          Constants.PREF_GAME_OVER, false);
      restoreState();
    } else {
      setupInitialBoard();
    }
    Log.d(TAG, "Countdown timer started with time: " + timeRemaining / 1000
        + " sec(s).");

    showBoard(isPaused);
    // if(isPaused) {
    // showBoard(false, "GAME IS PAUSED.");
    // } else {
    // if (isMyTurn()) {
    // showDebugToast("isMyTurn() returned true");
    // showBoard(true, "");
    // timeRemainingCounter = new TimeRemainingCounter(timeRemaining, 1000);
    // timeRemainingCounter.start();
    // } else {
    // showBoard(false, "Waiting for opponent's turn to complete.");
    // }
    // }
  }

  private void setupInitialBoard() {
    board = getInitialBoard();
    timeRemaining = Constants.TIME_PER_ROUND_IN_SECS * 1000;
    // initialize scoreboard and store in SP
    String initialScoreboard = Util.scoreboardArrToStr(Util
        .getInitialScoreboard());
    SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF_CONST,
        Context.MODE_PRIVATE);
    sp.edit().putString(Constants.PREF_CURR_SCOREBOARD, initialScoreboard)
        .commit();
  }

  private void restoreState() {
    SharedPreferences savedState = getPreferences(MODE_PRIVATE);

    this.currScore = savedState.getInt(Constants.PREF_CURR_SCORE, 0);
    this.totalCorrectWords = savedState.getInt(Constants.PREF_CORRECT_WORDS, 0);
    this.totalIncorrectWords = savedState.getInt(
        Constants.PREF_INCORRECT_WORDS, 0);
    this.longestWord = savedState.getString(Constants.PREF_LONGEST_WORD, "");
    this.timeRemaining = savedState.getLong(Constants.PREF_TIME_REMAINING, 0);
    this.isMyTurn = savedState.getBoolean(Constants.PREF_IS_MY_TURN, false);

    this.scoreboardStr = savedState.getString(Constants.PREF_CURR_SCOREBOARD, "");
    
//    showDebugToast("scoreboardStr onResume() : " + scoreboardStr);
    
    String savedBoardStr = savedState.getString(Constants.PREF_BOARD_STATE, null);
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

    registerReceiver(receiver, new IntentFilter(
        Constants.INTENT_ACTION_2P_WORD_GAME));

    playBgMusic = Prefs.getMusic(this);
    showHint = Prefs.getHints(this);
    playBgMusic();
    checkAndHandleContinueGame();
    this.gameOver = false;
    // if (!isPaused) {
    // startNewLetterTimer();
    // }
    updateScoreOnUI();
  }

  private void updateScoreOnUI() {

    // Log.d(TAG, "Inside updateScoreOnUI()");

    SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF_CONST,
        Context.MODE_PRIVATE);
    this.scoreboardStr = sp.getString(Constants.PREF_CURR_SCOREBOARD, "");
    int[][] scoreboardArr = Util.scoreboardStrToArr(scoreboardStr);
    int myTotalScore = 0, oppTotalScore = 0;

    // Log.d(TAG, "scoreboardStr = " + scoreboardStr);
    // Util.printScoreboard(scoreboardArr);

    for (int i = 0; i < scoreboardArr.length; i++) {
      myTotalScore += scoreboardArr[i][0];
      oppTotalScore += scoreboardArr[i][1];
      // Log.d(TAG, "Added " + scoreboardArr[i][0] + " to myTotalScore (" +
      // myTotalScore + ")");
      // Log.d(TAG, "Added " + scoreboardArr[i][1] + " to oppTotalScore (" +
      // oppTotalScore + ") \n");
    }

    // Log.d(TAG, "scoreboardStr = " + scoreboardStr);
    // Log.d(TAG, "myTotalScore = " + myTotalScore);
    // Log.d(TAG, "oppTotalScore = " + oppTotalScore);

    TextView p1TxtView = (TextView) findViewById(R.id.two_player_wordgame_player1);
    p1TxtView.setText(this.username + ": ");

    TextView p2TxtView = (TextView) findViewById(R.id.two_player_wordgame_player2);
    p2TxtView.setText(this.opponentName + ": ");

    TextView p1ScoreTxtView = (TextView) findViewById(R.id.two_player_wordgame_p1_score);
    p1ScoreTxtView.setText(String.valueOf(myTotalScore));

    TextView p2ScoreTxtView = (TextView) findViewById(R.id.two_player_wordgame_p2_score);
    p2ScoreTxtView.setText(String.valueOf(oppTotalScore));

    TextView currRoundTxtView = (TextView) findViewById(R.id.two_player_wordgame_currround);
    currRoundTxtView.setText("Round # " + this.roundNo);
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

    unregisterReceiver(receiver);

    Log.d(TAG, "\n onPause() start, this.gameOver=" + this.gameOver);
     getPreferences(MODE_PRIVATE).edit().putBoolean(Constants.PREF_AUTO_ON_PAUSE, true).commit();
    this.autoOnPause = true;

    stopNewLetterTimer();
    if (timeRemainingCounter != null) {
      timeRemainingCounter.cancel();
    }
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
    Editor e = getPreferences(MODE_PRIVATE).edit();
    e.putString(Constants.PREF_BOARD_STATE, toBoardString(this.board));
    e.putInt(Constants.PREF_CURR_SCORE, this.currScore);
    e.putInt(Constants.PREF_CORRECT_WORDS, this.totalCorrectWords);
    e.putInt(Constants.PREF_INCORRECT_WORDS, this.totalIncorrectWords);
    e.putString(Constants.PREF_LONGEST_WORD, this.longestWord);
    e.putLong(Constants.PREF_TIME_REMAINING, this.timeRemaining);
    e.putInt(Constants.PREF_CURR_ROUND_NO, this.roundNo);
    e.putBoolean(Constants.PREF_CONTINUE_GAME, !this.gameOver);
    e.putBoolean(Constants.PREF_IS_MY_TURN, isMyTurn());
    e.putString(Constants.PREF_CURR_SCOREBOARD, this.scoreboardStr);
    e.commit();
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
          BoardView boardView = (BoardView) findViewById(R.id.two_player_wordgame_board_view);
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

    // showDebugToast("Callinf endRound() from insertNewLetter()");
    endRound();
  }

  private void endRound() {
    this.gameOver = true;
    // getSharedPreferences("WORD_GAME",
    // MODE_PRIVATE).edit().putBoolean(PREF_CONTINUE_GAME, false)
    // .commit();
    stopNewLetterTimer();
    if (timeRemainingCounter != null) {
      timeRemainingCounter.cancel();
    }
    if (mpCountDown != null) {
      mpCountDown.release();
    }
    Util.playSound(this.getApplicationContext(), mpGameOver, gameOverResId,
        false);
    if (mpGameOver != null) {
      mpGameOver.release();
    }
    // showDebugToast("Inside endRound" + "\nroundNo= " + roundNo
    // + "\nisPlayerOne = " + isPlayerOne);
    if (this.roundNo == Constants.NO_OF_ROUNDS && !isPlayerOne) {
      // Game ended
      // endGame();
      // notifyEndOfGame();
      notifyEndOfRound();
      showDebugToast("You scored " + this.currScore + " points in round " + this.roundNo);
      updateScoreboard(0, this.roundNo, this.currScore);
      showGameEndDialog();
      // Add urself to AVAILABLE_USERS_LIST
      Util.addValuesToKeyOnServer(Constants.AVAILABLE_USERS_LIST,
          this.username, this.regId);
      //  Clear opp name and Id from SP
      clearOppFromSP();
    } else {
      // Round complete, notify opponent
      notifyEndOfRound();
      showWaitingForUser();
    }
    this.timeRemaining = Constants.TIME_PER_ROUND_IN_SECS * 1000;
    this.currScore = 0;

  }

  private void showGameEndDialog() {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setCancelable(true);
      builder.setTitle("Game End");
      builder.setMessage("Final Scoreboard: \n\n"
          + "Players: \t \t '" + this.username + "' \t '" + this.opponentName + "' \n" 
          + Util.getScoreboardDisplayStr(this.scoreboardStr));
//      builder.setInverseBackgroundForced(true);
      builder.setPositiveButton("Back to Menu", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
          //  Go back to Choose Opponent activity
          Intent i = new Intent(Game.this, ChooseOpponent.class);
          i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
          startActivity(i);
        }
      });
      
      AlertDialog alert = builder.create();
      alert.show();
    
      //  update Top Scorer's List on server
      Util.updateTopScorersList(username, Util.getTotalScore(this.scoreboardStr), Util.getCurrentDateTime());
  }

  private void showWaitingForUser() {
    this.isMyTurn = false;
    getSharedPreferences(Constants.SHARED_PREF_CONST, Context.MODE_PRIVATE)
        .edit().putBoolean(Constants.PREF_IS_MY_TURN, false).commit();

    showDebugToast("You scored " + this.currScore + " points in round " + this.roundNo);
    updateScoreboard(0, this.roundNo, this.currScore);
    updateScoreOnUI();
    showBoard(false);
  }

  private void notifyEndOfRound() {
    // showDebugToast("Inside notifyEndOfRound");
    new AsyncTask<String, Integer, String>() {
      @Override
      protected String doInBackground(String... params) {
        String retVal = "";
        String result = "";
        try {
          retVal = Util.sendPost("data." + Constants.KEY_MSG_TYPE + "="
              + Constants.MSG_TYPE_2P_MOVE + "&data." + Constants.KEY_ROUND
              + "=" + roundNo + "&data." + Constants.KEY_IS_PLAYER_ONE + "="
              + String.valueOf(!isPlayerOne) + "&data."
              + Constants.KEY_OPP_SCORE + "=" + currScore + "&data."
              + Constants.KEY_OPP_NAME + "=" + username, oppRegId);
          Log.d(TAG, "Result of HTTP POST: " + retVal);
          // displayMsg("Connected to user:" + oppName + " (" +
          // oppRegId + ")");
          retVal = "Sent round no and score to opponent:" + opponentName + " ("
              + oppRegId + ")";
          // sendPost("data=" + myRegId);
        } catch (Exception e) {
          // displayMsg("Error occurred while making an HTTP post call.");
          retVal = "Error occured while making an HTTP post call.";
          e.printStackTrace();
        }
        return retVal;
      }

      @Override
      protected void onPostExecute(String result) {
        // mDisplay.append(msg + "\n");
//        Toast t = Toast.makeText(getApplicationContext(), result, 2000);
//        t.show();
        Log.d(TAG, "\n===================================================\n");
        Log.d(TAG, "result: " + result);
      }
    }.execute(String.valueOf(this.roundNo), String.valueOf(!this.isPlayerOne),
        String.valueOf(this.currScore), this.username, this.oppRegId);
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
    if (newLetterTimerTask != null) {
      newLetterTimerTask.cancel();
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.two_player_wordgame_clear_button:
      if (isPaused) {
        return;
      }
      Button currwordButton = (Button) findViewById(R.id.two_player_wordgame_currword_button);
      currwordButton.setText("");
      this.currWord = "";
      BoardView boardView = (BoardView) findViewById(R.id.two_player_wordgame_board_view);
      HashSet<String> tempRectList = currSelections;
      boardView.inValidateMultipleRects(tempRectList);
      this.currSelections.clear();
      break;
    case R.id.two_player_wordgame_currword_button:
      if (isPaused) {
        return;
      }
      if (isCurrWordValid) {
        processValidWord(v);
      } else {
        Util.playSound(this.getApplicationContext(), mpInvalidWord,
            invalidWordBeepResId, false);
        this.totalIncorrectWords++;
        ((Button) v).setTextColor(Color.RED);
      }
      break;
    case R.id.two_player_wordgame_pause_button:
      // isPaused = true;
      // Intent i = new Intent(this, PauseDialog.class);
      // startActivity(i);
      // BoardView boardView2 = (BoardView)
      // findViewById(R.id.two_player_wordgame_board_view);
      if (isPaused) {
        isPaused = false;
        if (playBgMusic) {
          playBgMusic();
        }
        // startNewLetterTimer();
        // timeRemainingCounter = new TimeRemainingCounter(timeRemaining, 1000);
        // timeRemainingCounter.start();
        ((Button) v).setText(R.string.two_player_wordgame_pause);
        // boardView2.setVisibility(View.VISIBLE);
      } else {
        isPaused = true;
        if (mpBgMusic != null) {
          mpBgMusic.release();
        }
        if (mpCountDown != null) {
          mpCountDown.release();
        }
        // stopNewLetterTimer();
        // showDebugToast("Cancelling 'timeRemainingCounter'");
        // if (timeRemainingCounter != null) {
        // timeRemainingCounter.cancel();
        // }
        ((Button) v).setText(R.string.two_player_wordgame_resume);
        // boardView2.setVisibility(View.INVISIBLE);
      }
      showBoard(isPaused);
      break;
    // case R.id.wordgame_hint_button:
    // if (showHint) {
    // // stopNewLetterTimer();
    // Intent i2 = new Intent(this, Hints.class);
    // i2.putExtra(Game.PREF_BOARD_STATE, toBoardString(board));
    // // i.putExtra(Game.CONTINUE_GAME, true);
    // overrideAndContinue = true;
    // startActivity(i2);
    // }
    // break;
    case R.id.two_player_wordgame_quit_button:
      getPreferences(MODE_PRIVATE).edit()
          .putBoolean(Constants.PREF_GAME_OVER, false).commit();
      // finish();
      
      stopNewLetterTimer();
      // showDebugToast("Cancelling 'timeRemainingCounter'");
       if (timeRemainingCounter != null) {
         timeRemainingCounter.cancel();
       }
      
      showQuitGameDialog();
      break;
    }
  }

  private void showQuitGameDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setCancelable(true);
    builder.setTitle("Quit Game");
    builder.setMessage("Are you sure you to quit the game?");
//    builder.setInverseBackgroundForced(true);
    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        // Add urself to AVAILABLE_USERS_LIST
        Util.addValuesToKeyOnServer(Constants.AVAILABLE_USERS_LIST,
            username, regId);
        // Clear opp name and Id from SP
        clearOppFromSP();
        
//      Go back to Choose Opponent activity
        Intent i = new Intent(Game.this, ChooseOpponent.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        
        //  notify opponent 
        notifyQuitGame();
      }
    });
    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        startNewLetterTimer();
        // showDebugToast("Cancelling 'timeRemainingCounter'");
         timeRemainingCounter = new TimeRemainingCounter(timeRemaining, 1000);
         timeRemainingCounter.start();
      }
    });
    AlertDialog alert = builder.create();
    alert.show();
  }

  protected void notifyQuitGame() {
      // showDebugToast("Inside notifyEndOfRound");
      new AsyncTask<String, Integer, String>() {
        @Override
        protected String doInBackground(String... params) {
          String retVal = "";
          String result = "";
          try {
            retVal = Util.sendPost("data." + Constants.KEY_MSG_TYPE + "="
                + Constants.MSG_TYPE_2P_QUIT + "&data."
                + Constants.KEY_OPP_NAME + "=" + username, oppRegId);
            Log.d(TAG, "Result of HTTP POST: " + retVal);
            // displayMsg("Connected to user:" + oppName + " (" +
            // oppRegId + ")");
            retVal = "Sent quit msg to opponent:" + opponentName + " ("
                + oppRegId + ")";
            // sendPost("data=" + myRegId);
          } catch (Exception e) {
            // displayMsg("Error occurred while making an HTTP post call.");
            retVal = "Error occured while making an HTTP post call.";
            e.printStackTrace();
          }
          return retVal;
        }

        @Override
        protected void onPostExecute(String result) {
          // mDisplay.append(msg + "\n");
//          Toast t = Toast.makeText(getApplicationContext(), result, 2000);
//          t.show();
          Log.d(TAG, "\n===================================================\n");
          Log.d(TAG, "result: " + result);
        }
      }.execute(String.valueOf(this.roundNo), String.valueOf(!this.isPlayerOne),
          String.valueOf(this.currScore), this.username, this.oppRegId);
    }

  private void processValidWord(View v) {
    Util.playSound(this.getApplicationContext(), mpValidWord,
        validWordBeepResId, false);
    String currWord = ((Button) v).getText().toString();
    addWord(currWord);
    ((Button) v).setText("");
    this.currWord = "";
    int currWordLength = currWord.length();
    this.currScore += currWordLength;
    TextView currScoreView = (TextView) findViewById(R.id.two_player_wordgame_score);
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
    BoardView boardView = (BoardView) findViewById(R.id.two_player_wordgame_board_view);
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
//      Log.d(TAG, "Selected letter '" + board[row][col] + "'");
      // add letter to currWord
      Button currWordBtn = (Button) findViewById(R.id.two_player_wordgame_currword_button);
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

  public void showDebugToast(String msg) {
    Toast t = Toast.makeText(getApplicationContext(), msg, 2000);
    t.show();
    Log.d(TAG, "\n===================================================\n");
    Log.d(TAG, msg);
    Log.d(TAG, "\n===================================================\n");
    // TextView msgTxtView = (TextView)
    // findViewById(R.id.communication_interphone_comm_msg_textview);
    // msgTxtView.setText(msg);
  }
}
