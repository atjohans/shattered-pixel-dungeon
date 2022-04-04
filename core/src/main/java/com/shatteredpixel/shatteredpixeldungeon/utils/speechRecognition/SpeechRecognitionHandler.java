package com.shatteredpixel.shatteredpixeldungeon.utils.speechRecognition;

import com.shatteredpixel.shatteredpixeldungeon.utils.state_management.StateReader;

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
        if (!StateReader.busy) {
            for (SpeechRecognitionListener l : listeners) {
                    l.execute();

            }
        }
    }
    public void dispatchKillEvent(){
        for (SpeechRecognitionListener l : listeners) {
            l.kill();

        }
    }

}
