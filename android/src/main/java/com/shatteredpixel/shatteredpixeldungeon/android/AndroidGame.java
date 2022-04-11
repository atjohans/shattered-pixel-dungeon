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
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.ViewConfiguration;
import android.widget.Toast;

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
import com.shatteredpixel.shatteredpixeldungeon.utils.state_management.StateReader;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Button;
import com.watabou.utils.FileUtils;

import java.util.ArrayList;

public class AndroidGame extends AndroidApplication implements RecognitionListener {

    public static AndroidApplication instance;

    private static AndroidPlatformSupport support;

    public static SpeechEventListenerAndroid speechEventListenerAndroid;

    public static SpeechRecognitionListener recognitionListener;


    //Speech Recognition
    private SpeechRecognizer speechRecognizer = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    private static final int REQUEST_RECORD_PERMISSION = 100;

    private AndroidGame ref = this;
    Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //-------SPEECH_RECOGNITION-------
        this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_PERMISSION);

        recognitionListener = new SpeechRecognitionListener() {

            Runnable listenStarter = new Runnable() {
                @Override
                public void run() {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(ref);
                    Log.i(LOG_TAG, "Recognition Available: " + SpeechRecognizer.isRecognitionAvailable(ref));
                    speechRecognizer.setRecognitionListener(ref);
                    speechRecognizer.startListening(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH));
					/*
					MediaPlayer player = MediaPlayer.create(AndroidGame.this, Settings.System.DEFAULT_ALARM_ALERT_URI);
					player.start();
					player.release();
					*/

                    // Vibrate for 500 milliseconds
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(150, -1));
                    } else {
                        //deprecated in API 26
                        v.vibrate(150);
                    }

                    StateReader.busy = true;
                }
            };

            Runnable listenStopper = new Runnable() {
                @Override
                public void run() {
                    System.out.println("Stopping Recognizer");
                    StateReader.busy = false;
                    if (speechRecognizer != null) {
                        speechRecognizer.destroy();
                        Log.i(LOG_TAG, "destroy");

                        // Vibrate for 500 milliseconds
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


                            v.vibrate(VibrationEffect.createWaveform(new long[]{0,150,150, 150}, -1));

                        } else {
                            //deprecated in API 26
                            v.vibrate(150);
                            v.vibrate(150);
                        }

                    }
                }
            };

            @Override
            public void getCommand() {
                runOnUiThread(listenStarter);
            }

            @Override
            public void kill() {
                runOnUiThread(listenStopper);
            }


        };
        StateReader.speechRecognitionHandler.addSpeechRecognitionListener(recognitionListener);


        //--------------------------------


        //----------Speech Synthesis-----------
        speechEventListenerAndroid = new SpeechEventListenerAndroid(getApplicationContext());
        //speechEventHandler collects Listeners that fire when an event occurs
        StateReader.speechEventHandler.addStringChangeListener(speechEventListenerAndroid.listener);
        //-------------------------------------


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
            instance.setRequestedOrientation(SPDSettings.landscape() ?
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE :
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
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
        else support.reloadGenerators();

        support.updateSystemUI();

        Button.longClick = ViewConfiguration.getLongPressTimeout() / 1000f;

        initialize(new ShatteredPixelDungeon(support), config);

    }

    //-------Speech Recognition--------
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {

        Log.i(LOG_TAG, "OnReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        StateReader.busy = true;
    }

    @Override
    public void onRmsChanged(float v) {

        //Log.i(LOG_TAG, "onRmsChanged: " + v);
    }

    @Override
    public void onBufferReceived(byte[] bytes) {

        Log.i(LOG_TAG, "OnBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {

        Log.i(LOG_TAG, "OnEndOfSpeech");
    }

    @Override
    public void onError(int errorCode) {

        Log.i(LOG_TAG, "OnError");
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        speechRecognizer.stopListening();
        speechRecognizer.destroy();
        StateReader.busy = false;

        StateReader.speechRecognitionHandler.dispatchListenEvent();
        Log.e(LOG_TAG, message);
    }

    @Override
    public void onResults(Bundle results) {

        speechRecognizer.stopListening();
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches)
            text = result + "\n";
        //StateReader.speechEventHandler.setMsg(text);
        StateReader.busy = false;
        speechRecognizer.stopListening();
        speechRecognizer.destroy();

        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            v.vibrate(VibrationEffect.createWaveform(new long[]{0,150,150, 150}, -1));
        } else {
            //deprecated in API 26
            v.vibrate(150);
            v.vibrate(150);
        }

        StateReader.handleCommand(text);
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        Log.i(LOG_TAG, "OnPartialResults");
        //speechRecognizer.stopListening();
        //StateReader.busy = false;

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    @Override
    public void onStop() {
        super.onStop();
        StateReader.busy = false;
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            Log.i(LOG_TAG, "destroy");
        }
    }
    //--------------------------------


    @Override
    public AndroidAudio createAudio(Context context, AndroidApplicationConfiguration config) {
        return new AsynchronousAndroidAudio(context, config);
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