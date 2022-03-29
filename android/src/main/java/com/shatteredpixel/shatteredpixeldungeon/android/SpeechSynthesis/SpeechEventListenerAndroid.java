package com.shatteredpixel.shatteredpixeldungeon.android.SpeechSynthesis;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import com.shatteredpixel.shatteredpixeldungeon.utils.speechSynthesis.StringChangeEvent;
import com.shatteredpixel.shatteredpixeldungeon.utils.speechSynthesis.StringChangeListener;
import com.shatteredpixel.shatteredpixeldungeon.utils.state_management.StateReader;

import java.util.Locale;

/*
Contains the listener added to StateReader.speechEventHandler

when speechEventHandler fires a speech event, the listener receives it and plays the message
 */

public class SpeechEventListenerAndroid {
    public StringChangeListener listener;
    Context context;
    TextToSpeech tts;

    public SpeechEventListenerAndroid(Context context) {

        this.context = context;
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });

        listener = new StringChangeListener() {
            @Override
            public void stateChanged(StringChangeEvent event) {
                String msg = event.getDispatcher().getMsg();
                if (msg == StateReader.speechEventStop) {
                    tts.stop();

                } else {
                    tts.speak(msg, TextToSpeech.QUEUE_ADD, null);
                }
            }
        };
    }


}
