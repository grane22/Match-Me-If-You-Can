package edu.neu.madcourse.dushyantdeshmukh.finalproject;

public class ProjectConstants {

  // Shared Preference ProjectConstants
  public static final String SHARED_DUAL_PHONE_MODE_ON = "isTwoPhoneModeOn";
  public static final String USER_NAME = "USER_NAME";
  public static final String USER_REG_ID = "USER_REG_ID";

  // Connection ProjectConstants
  // public static final String KEY_MSG_TYPE = "MSG_TYPE";
  public static final String MSG_TYPE_FP_MOVE = "FP_MOVE";
  public static final String MSG_TYPE_FP_CONNECT = "FP_CONNECT";
  public static final String MSG_TYPE_FP_ACK_ACCEPT = "FP_ACK_ACC";
  public static final String MSG_TYPE_FP_ACK_REJECT = "FP_ACK_REJ";
  public static final String MSG_TYPE_FP_GAME_OVER = "FP_GAME_OVER";
  public static final String FINAL_PROJECT = "FINAL_PROJ";

  // mHealth server constants
  public static final String REGISTERED_USERS_LIST = "REGISTERED_USERS_LIST";
  public static final String PASSWORD = "numad14s";
  public static final String TEAM_NAME = "Dushyant";

  public static final String KEY_REG_ID = "REG_ID";
  public static final String KEY_USERNAME = "USERNAME";

  public static final String PREF_OPPONENT_REG_ID = "OPPONENT_REG_ID";
  public static final String PREF_OPPONENT_NAME = "OPPONENT_NAME";

  // GCM constants
  public static final String PROPERTY_REG_ID = "registration_id";
  public static final String PROPERTY_APP_VERSION = "appVersion";
  public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
  public static final String SENDER_ID = "94466405712";

  public static final String INTENT_ACTION_CONNECTION = "INTENT_ACTION_CONNECTION";
  public static final String INTENT_ACTION_GAME_MOVE_AND_FINISH = "INTENT_ACTION_GAME_MOVE_AND_FINISH";

  public static final String OPPONENT_NOT_FOUND = "Unable to find specified opponent."
      + "Please search for another user or connect to a random opponent.";
  
  public static final String OPPONENT_SAME_AS_USER = "You cannot search for your own name as opponent!";

  public static final String KEY_NOTIFICATION_DATA = "NOTIFICATION_DATA";

  // Match and Capture constants

  public static double PSNR_THRESHOLD_EASY = 12.0;
  public static double PSNR_THRESHOLD_MEDIUM = 14.0;
  public static double PSNR_THRESHOLD_HARD = 15.5;
  
  public static String IMG_DIR_NAME = "images_to_match";
  public static String P1_IMG_NAME_PREFIX = "P1_IMAGE_NO_";
  public static String P2_IMG_NAME_PREFIX = "P2_IMAGE_NO_";
  public static final int IMG_ALPHA = 85; // larger value indicates more focus
                                          // on img to match
  public static final String CAPTURE_SWAP_MSG = "Swap Phone and ask opponent to press start to capture images";
  public static final String MATCH_SWAP_MSG = "Swap Phone and ask opponent to start matching challenge";
  public static final String START = "Start";
  public static final String SWAP_TITLE = "Swap Phones";

  public static final String TOTAL_MATCHING_TIME = "TOTAL_MATCHING_TIME";
  public static final String NUMBER_OF_IMAGES = "NUMBER_OF_IMAGES";
  public static final String NUMBER_OF_IMAGES_CAPTURED = "NUMBER_OF_IMAGES_CAPTURED";
  public static final String NUMBER_OF_IMAGES_MATCHED = "NUMBER_OF_IMAGES_MATCHED";
  public static final String START_TIME = "START_TIME";

  // Messages
  public static String MATCH_WAIT_MSG = "Matching image...";
  public static String CAPTURE_WAIT_MSG = "Processing captured image...";
  public static String MATCH_SUCCESS_MSG = "Images matched successfully.";
  public static String MATCH_FAIL_MSG = "Images did not match. Try again.";
  public static String SKIP_FAIL_MSG = "Cannot skip. Only one image is left to match.";

  public static String PREF_MUSIC_ON = "PREF_MUSIC_ON";
  public static String PREF_SHOW_TUTORIAL = "PREF_SHOW_TUTORIAL";
  public static String PREF_TOTAL_NO_OF_IMAGES = "PREF_TOTAL_NO_OF_IMAGES";
  public static String PREF_MATCHING_DIFFICULTY = "PREF_MATCHING_DIFFICULTY";

  public static final String QUIT_GAME_MESSAGE = "Are you sure you want to quit? "
      + "This will end the current game";
  public static final String QUIT_TITLE = "Quit Menu";
  public static final String YES = "Yes";
  public static final String NO = "No";

  public static final String WAITING_MESSAGE = "Waiting for the opponent to finish game!\n"
      + "You can quit this game to return to main menu!";
  public static final String WAITING_TITLE = "Waiting...";
  public static final String WAITING_DIALOG_SHOW = "WAITING_DIALOG_SHOW";
  public static final String QUIT_BUTTON = "QUIT";
  public static final String CURRENT_STEP_NO = "CURRENT_STEP_NO";

  public static String IS_OPPONENT_GAME_OVER = "IS_OPPONENT_GAME_OVER";
  public static String OPPONENT_TIME = "OPPONENT_TIME";
  public static String OPPONENT_IMAGE_COUNT = "OPPONENT_IMAGE_COUNT";
  public static String PLAYER_TIME = "PLAYER_TIME";
  public static String PLAYER_IMAGE_COUNT = "PLAYER_IMAGE_COUNT";

  // Single phone mode specific constants
  public static String IS_SINGLE_PHONE_MODE = "IS_SINGLE_PHONE_MODE";
  public static String SINGLE_PHONE_CURR_STATE = "SINGLE_PHONE_CURR_STATE";

  public static final int SINGLE_PHONE_P1_CAPTURE_STATE = 1;
  public static final int SINGLE_PHONE_P2_CAPTURE_STATE = 2;
  public static final int SINGLE_PHONE_P1_MATCH_STATE = 3;
  public static final int SINGLE_PHONE_P2_MATCH_STATE = 4;

  public static final String CAPTURE_TITLE = "Capture Images";
  public static final String MATCH_TITLE = "Match Images";
  public static final String SINGLE_PHONE_P1_CAPTURE_MSG = "Let's start the game. \n" +
		  												   "Player 1's turn to capture images.";
  public static final String SINGLE_PHONE_P2_CAPTURE_MSG = "Player 2's turn to capture images.";
  public static final String SINGLE_PHONE_P1_MATCH_MSG = "Player 1's turn to match images.";
  public static final String SINGLE_PHONE_P2_MATCH_MSG = "Player 2's turn to match images.";
  
  public static String PLAYER_1_TIME = "PLAYER_1_TIME";
  public static String PLAYER_1_IMAGE_COUNT = "PLAYER_1_IMAGE_COUNT";
  public static String PLAYER_2_TIME = "PLAYER_2_TIME";
  public static String PLAYER_2_IMAGE_COUNT = "PLAYER_2_IMAGE_COUNT";
  
  public static final String IS_ACTIVITY_PAUSED = "IS_ACTIVITY_PAUSED";
  public static final String OPPONENT = "Opponent";
  public static final String IS_SWAP_ALERT_DIALOG_SHOWN = "IS_SWAP_ALERT_DIALOG_SHOWN";
  public static final String IS_WAITING_ALERT_DIALOG_SHOWN = "IS_WAITING_ALERT_DIALOG_SHOWN";
  public static final String IS_ACCEPT_REJECT_ALERT_PAUSED = "IS_ACCEPT_REJECT_ALERT_PAUSED";
  public static final String POTENTIAL_OPPONENT_NAME = "POTENTIAL_OPPONENT_NAME";
  
  public static final String IS_SINGLE_PHONE_DIALOG_SHOWN = "IS_SINGLE_PHONE_DIALOG_SHOWN";
  
  public static final String END_GAME = "End Game";
  public static final String IS_MY_GAME_OVER = "IS_MY_GAME_OVER";
  
}
