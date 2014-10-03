package edu.neu.madcourse.dushyantdeshmukh.wordgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import edu.neu.madcourse.dushyantdeshmukh.R;

public class Prefs extends PreferenceActivity {
    // Option names and default values
    private static final String OPT_MUSIC = "music";
    private static final boolean OPT_MUSIC_DEF = true;
    private static final String OPT_HINTS = "hints";
    private static final boolean OPT_HINTS_DEF = true;
    private static final String OPT_ROWS = "rows";
    private static final String OPT_ROWS_DEF = "7";
    private static final String OPT_COLS = "cols";
    private static final String OPT_COLS_DEF = "5";
    private static final String OPT_DIFF = "difficulty";
    private static final String OPT_DIFF_DEF = "1";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wordgame_preferences);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
    
    /** Get the current value of the music option */

    public static boolean getMusic(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(OPT_MUSIC, OPT_MUSIC_DEF);
    }

    /** Get the current value of the hints option */

    public static boolean getHints(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(OPT_HINTS, OPT_HINTS_DEF);
    }

    /**
     * Get the current value of the rows option
     * 
     * @return the optRows
     */
    public static int getRows(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(OPT_ROWS, OPT_ROWS_DEF));
    }

    /**
     * Get the current value of the cols option
     * 
     * @return the optCols
     */
    public static int getCols(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(OPT_COLS, OPT_COLS_DEF));
    }
    
    /**
     * Get the current value of the difficulty level option
     * 
     * @return the optCols
     */
    public static int getDifficultyLevel(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(OPT_DIFF, OPT_DIFF_DEF));
    }

}
