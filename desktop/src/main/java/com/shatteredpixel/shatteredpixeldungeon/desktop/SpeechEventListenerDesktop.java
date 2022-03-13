package com.shatteredpixel.shatteredpixeldungeon.desktop;

import com.audiopixel.audiopixeldungeon.desktop.TTSManager;
import com.shatteredpixel.shatteredpixeldungeon.utils.speechSynthesis.StringChangeEvent;
import com.shatteredpixel.shatteredpixeldungeon.utils.speechSynthesis.StringChangeListener;

/*
Contains the listener added to StateReader.speechEventHandler

when speechEventHandler fires a speech event, the listener receives it and plays the message
 */

public class SpeechEventListenerDesktop {
    StringChangeListener listener;
    TTSManager ttsManager = new TTSManager();
    public SpeechEventListenerDesktop() {
        listener = new StringChangeListener() {
            @Override
            public void stateChanged(StringChangeEvent event) {
                ttsManager.speakMsg(event.getDispatcher().getMsg());
            }
        };
    }

}
