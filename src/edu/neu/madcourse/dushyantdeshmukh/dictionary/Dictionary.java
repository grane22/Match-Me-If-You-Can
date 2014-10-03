package edu.neu.madcourse.dushyantdeshmukh.dictionary;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.utilities.BloomFilter;

public class Dictionary extends Activity implements OnClickListener {

	private TextView tvWordsList;
	private MediaPlayer mp;
	private int beepResId = R.raw.dictionary_beep;

	private BloomFilter<String> bloomFilter;
	HashSet<String> wordList = new HashSet<String>();

	final static String TAG = "\n\n#DEBUG Dictionary";

	public Dictionary() {

	}

	@Override
	protected void onResume() {
		super.onResume();
		//loadBitsetFromFile("compressedWordlist.txt");
		renderWorList();
	}

	private void renderWorList() {
		Log.d(TAG, "Inside renderWorList() \n\n");
		tvWordsList.setText(getWordListCharSeq());
	}

	private CharSequence getWordListCharSeq() {
		String wordListSeq = "WORD LIST: ";
		Iterator<String> iterator = wordList.iterator();
		while (iterator.hasNext()) {
			wordListSeq += iterator.next() + ", ";
		}
		wordListSeq = wordListSeq.substring(0, wordListSeq.length() - 2);
		Log.d(TAG, "WordListSeq - " + wordListSeq + " \n\n");
		return wordListSeq;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dictionary_main);
		loadBitsetFromFile("compressedWordlist.txt");
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		tvWordsList = (TextView) findViewById(R.id.dictionary_wordlist);

		// Set up click listeners for all the buttons
		EditText editText = (EditText) findViewById(R.id.dictionary_editText);

		// add a text change listener to keep track user input
		editText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {	
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				Log.d(TAG, "Inside onTextChanged(), arg0 - " + arg0 + " \n\n");
				if (arg0.length() > 2) {
					checkWord(arg0.toString());
				}
			}

		});

		View clearButton = findViewById(R.id.dictionary_clear_button);
		clearButton.setOnClickListener(this);

		View returnButton = findViewById(R.id.dictionary_return_button);
		returnButton.setOnClickListener(this);

		View ackButton = findViewById(R.id.dictionary_ack_button);
		ackButton.setOnClickListener(this);
	}

	protected void checkWord(String ipWord) {
		ipWord = ipWord.toLowerCase();
		if (bloomFilter.contains(ipWord) && !wordList.contains(ipWord)) {
			Log.d(TAG, ipWord + " exists");
			playValidWordBeep();
			addWord(ipWord);
		} else
			Log.d(TAG, ipWord + " does not exist");
	}

	private void playValidWordBeep() {
		// Release any resources from previous MediaPlayer
		if (mp != null) {
			mp.release();
		}		
		// Create a new MediaPlayer to play this sound
		mp = MediaPlayer.create(this, beepResId);
		mp.start();
	}

	private void addWord(String ipWord) {
		wordList.add(ipWord);
		renderWorList();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dictionary_clear_button:
			EditText editText = (EditText) findViewById(R.id.dictionary_editText);
			editText.setText("");
			wordList.clear();renderWorList();
			break;
		case R.id.dictionary_return_button:
			finish();
			break;
		case R.id.dictionary_ack_button:
			Intent i = new Intent(this, Acknowledgements.class);
			startActivity(i);
			break;
		}
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
			bloomFilter = BloomFilter.loadBitsetWithByteArray(fileData,
					bloomFilter);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bloomFilter;
	}
}
