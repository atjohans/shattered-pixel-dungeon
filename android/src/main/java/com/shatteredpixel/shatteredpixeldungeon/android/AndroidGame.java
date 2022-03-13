/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidAudio;
import com.badlogic.gdx.backends.android.AsynchronousAndroidAudio;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.android.SpeechSynthesis.SpeechEventListenerAndroid;
import com.shatteredpixel.shatteredpixeldungeon.services.news.News;
import com.shatteredpixel.shatteredpixeldungeon.services.news.NewsImpl;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.UpdateImpl;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.Updates;
import com.shatteredpixel.shatteredpixeldungeon.utils.speechRecognition.SpeechRecognitionListener;
import com.shatteredpixel.shatteredpixeldungeon.utils.state_reading.State.StateReader;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Button;
import com.watabou.utils.FileUtils;

import java.util.ArrayList;

public class AndroidGame extends AndroidApplication implements RecognitionListener {
	
	public static AndroidApplication instance;
	
	private static AndroidPlatformSupport support;

	public static SpeechEventListenerAndroid speechEventListenerAndroid;

	SpeechRecognitionListener speechRecognitionListener;

	static final int REQUEST_RECORD_PERMISSION = 100;
	SpeechRecognizer speechRecognizer = null;
	Intent recognizerIntent;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);



		//Speech Stuff
		speechEventListenerAndroid = new SpeechEventListenerAndroid(getApplicationContext());
		//speechEventHandler collects Listeners that fire when an event occurs
		StateReader.speechEventHandler.addStringChangeListener(speechEventListenerAndroid.listener);


		//speech recognizer
		speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this.getApplicationContext());
		speechRecognizer.setRecognitionListener(this);
		recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"US-en");
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
		this.requestPermissions( new String[]{Manifest.permission.RECORD_AUDIO},
				REQUEST_RECORD_PERMISSION);


		speechRecognitionListener = new SpeechRecognitionListener() {

			Runnable runner = new Runnable() {
				@Override
				public void run() {
					speechRecognizer.startListening(recognizerIntent);
				}
			};
			@Override
			public void execute() {
				runOnUiThread(runner);
			}
		};

		StateReader.speechRecognitionHandler.addSpeechRecognitionListener(speechRecognitionListener);

		//there are some things we only need to set up on first launch
		if (instance == null) {

			instance = this;

			try {
				Game.version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			} catch (PackageManager.NameNotFoundException e) {
				Game.version = "???";
			}
			try {
				Game.versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			} catch (PackageManager.NameNotFoundException e) {
				Game.versionCode = 0;
			}

			if (UpdateImpl.supportsUpdates()) {
				Updates.service = UpdateImpl.getUpdateService();
			}
			if (NewsImpl.supportsNews()) {
				News.service = NewsImpl.getNewsService();
			}

			FileUtils.setDefaultFileProperties(Files.FileType.Local, "");

			// grab preferences directly using our instance first
			// so that we don't need to rely on Gdx.app, which isn't initialized yet.
			// Note that we use a different prefs name on android for legacy purposes,
			// this is the default prefs filename given to an android app (.xml is automatically added to it)
			SPDSettings.set(instance.getPreferences("ShatteredPixelDungeon"));

		} else {
			instance = this;
		}
		
		//set desired orientation (if it exists) before initializing the app.
		if (SPDSettings.landscape() != null) {
			instance.setRequestedOrientation( SPDSettings.landscape() ?
					ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE :
					ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT );
		}
		
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.depth = 0;
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			//use rgb565 on ICS devices for better performance
			config.r = 5;
			config.g = 6;
			config.b = 5;
		}
		
		config.useCompass = false;
		config.useAccelerometer = false;
		
		if (support == null) support = new AndroidPlatformSupport();
		else                 support.reloadGenerators();
		
		support.updateSystemUI();

		Button.longClick = ViewConfiguration.getLongPressTimeout()/1000f;
		
		initialize(new ShatteredPixelDungeon(support), config);
		
	}




	@Override
	public AndroidAudio createAudio(Context context, AndroidApplicationConfiguration config) {
		return new AsynchronousAndroidAudio(context, config);
	}


	@Override
	protected void onResume() {
		//prevents weird rare cases where the app is running twice
		if (instance != this){
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				finishAndRemoveTask();
			} else {
				finish();
			}
		}
		super.onResume();
	}
	@Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onStop() {
		super.onStop();
		if (speechRecognizer != null) {
			speechRecognizer.destroy();
			Log.i(this.toString(), "destroy");
		}
	}
	@Override
	public void onReadyForSpeech(Bundle bundle) {
		Log.i("onReadyForSpeech", "onReadyForSpeech");

	}

	@Override
	public void onBeginningOfSpeech() {
		Log.i("onBeginningOfSpeech", "onBeginningOfSpeech");

	}

	@Override
	public void onRmsChanged(float v) {

	}

	@Override
	public void onBufferReceived(byte[] bytes) {

	}

	@Override
	public void onEndOfSpeech() {


	}

	@Override
	public void onError(int i) {

	}

	@Override
	public void onResults(Bundle results) {
		Log.i("onResults", "onResults");
		ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		String text = "";
		for (String result : matches)
			text = result + "\n";

		StateReader.setCommand(text);

	}

	@Override
	public void onPartialResults(Bundle bundle) {

	}

	@Override
	public void onEvent(int i, Bundle bundle) {

	}

	@Override
	public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case REQUEST_RECORD_PERMISSION:
				if (grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED) {

				} else {
					Toast.makeText(this, "Permission Denied!", Toast .LENGTH_SHORT).show();
				}
		}
	}

	@Override
	public void onBackPressed() {
		//do nothing, game should catch all back presses
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		support.updateSystemUI();
	}
	
	@Override
	public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
		super.onMultiWindowModeChanged(isInMultiWindowMode);
		support.updateSystemUI();
	}


}