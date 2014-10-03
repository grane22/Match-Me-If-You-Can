package edu.neu.madcourse.dushyantdeshmukh;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class About extends Activity implements OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		TelephonyManager tManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		String uid = tManager.getDeviceId();
		String uidLabel = this.getResources().getString(R.string.about_phone_unique_id_label);
		
		TextView phoneUniqueIdView = (TextView) findViewById(R.id.about_phone_unique_id);
		phoneUniqueIdView.setText(uidLabel + ": " + uid);

		// Set up click listeners for all the buttons
		View okButton = findViewById(R.id.ok_button);
		okButton.setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok_button:
			finish();
			break;
		}
	}
}
