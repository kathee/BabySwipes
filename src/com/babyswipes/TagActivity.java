package com.babyswipes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class TagActivity extends BaseActivity {

	private TextView tagText;
	private TextView timeText;
	private TextView errorText;
	private TextView countdownText;
	private ImageView image; 

	private BabySwipesDB mDataBase;
	private Button closeButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag);

		// Connection to the db
		mDataBase = new BabySwipesDB(getBaseContext());

		// Init fields
		tagText = (TextView) findViewById(R.id.textView_Activity);
		timeText = (TextView) findViewById(R.id.textView_Timestamp);
		countdownText = (TextView) findViewById(R.id.textView_countdown);
		errorText = (TextView) findViewById(R.id.textView_ErrorMessage);
		closeButton = (Button) this.findViewById(R.id.button_close);
		image = (ImageView) findViewById(R.id.imageView1);

		// Read Tag
		Intent intent = getIntent();
		if (intent.getType() != null
				&& intent.getType().equals(MimeType.NFC_MIME)) {

			// Read NdefData
			Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(
					NfcAdapter.EXTRA_NDEF_MESSAGES);
			NdefMessage msg = (NdefMessage) rawMsgs[0];
			NdefRecord activityNdef = msg.getRecords()[0];
			String payload = new String(activityNdef.getPayload());
			displayData(payload);
		} else if(intent.getExtras().containsKey("tag")){
			displayData(intent.getExtras().getString("tag"));
		}
		
	}
	
	private void displayData(String payload){
		// Display Data
		StringTokenizer st = new StringTokenizer(payload, "/");
		String activityName = st.nextToken();
		if (st.hasMoreTokens()) {
		    String reminderTime = st.nextToken();
		}
		
		switchPicture(activityName);
		
		Calendar currentTime = Calendar.getInstance();

		long timestamp = currentTime.getTimeInMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a");
		String displayDate = sdf.format(timestamp);
		
		if (mDataBase.addSwipe(activityName, timestamp)) {
			tagText.setText(activityName);
			errorText.setText("");
			timeText.setText("Recorded at " + displayDate);
			mDataBase.close();
			
			closeButton.setVisibility(View.GONE);

			//Lets do a countdown before we finish
			CountDownTimer countdown = new CountDownTimer(5000, 1000) {
				
				@Override
				public void onTick(long millisUntilFinished) {
		            countdownText.setText(millisUntilFinished / 1000 + " seconds to cancel");
		        }

		        public void onFinish() {
		        	countdownText.setText("");
		        	finish();	        	
		        }

			};
			
			countdown.start();
			
		} else {
			tagText.setText(activityName + " not recorded");
			errorText.setText(activityName
					+ " has not been registered with this device");
			timeText.setText(""); 
			this.closeButton.setText("Add a tag");
			
			this.closeButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(TagActivity.this, TrainingActivity.class);
		            startActivity(i); 
				}
			});
		}
		
		
		
	}

	/**
	 * Some items match images - this will dynamically switch the image based on the activity
	 * @param activityName
	 */
	private void switchPicture(String activityName) {
		
		// @TODO These strings should really be converted to some enum or constants file
		if((activityName.contains("Diaper")) || (activityName.contains("Wet"))
		        || (activityName.contains("BM)"))){
			image.setImageResource(R.drawable.diaper);
		}
		else if((activityName.contains("Feeding")) || (activityName.contains("Leftside"))
		        || (activityName.contains("Rightside")) || (activityName.contains("Bottled"))
		        || (activityName.contains("Formula")) || (activityName.contains("Solid"))) {
			image.setImageResource(R.drawable.bottle);
		}
		else if(activityName.contains("Nap")){
			image.setImageResource(R.drawable.nap);
		}
		else if(activityName.contains("Medicine")){
			image.setImageResource(R.drawable.medicine);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_tag, menu);
		return true;
	}

}
