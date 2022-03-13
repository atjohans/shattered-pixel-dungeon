package com.shatteredpixel.shatteredpixeldungeon.utils.speechRecognition;

import com.shatteredpixel.shatteredpixeldungeon.utils.speechSynthesis.StringChangeListener;

import java.util.ArrayList;
import java.util.List;

public class SpeechRecognitionHandler {

    private List<SpeechRecognitionListener> listeners;

    public SpeechRecognitionHandler(){
        listeners = new ArrayList<SpeechRecognitionListener>();
    }

    public void addSpeechRecognitionListener(SpeechRecognitionListener l){
        listeners.add(l);
    }

    public void dispatchListenEvent(){
        for (SpeechRecognitionListener l : listeners) {
            l.execute();
        }
    }
}
