package com.shatteredpixel.shatteredpixeldungeon.utils.speechSynthesis;

import java.util.List;
import java.util.ArrayList;

public class SpeechEventHandler implements StringChangeDispatcher {

    private String msg;
    private List<StringChangeListener> listeners;

    public SpeechEventHandler(String initialFlagState) {
        msg = initialFlagState;
        listeners = new ArrayList<StringChangeListener>();
    }

    @Override
    public void addStringChangeListener(StringChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void setMsg(String msg) {
        if (this.msg != msg) {
            this.msg = msg;
            dispatchEvent();
        }
    }

    @Override
    public String getMsg() {
        return msg;
    }

    private void dispatchEvent() {
        final StringChangeEvent event = new StringChangeEvent(this);
        for (StringChangeListener l : listeners) {
            dispatchRunnableOnEventQueue(l, event);
        }
    }


    private void dispatchRunnableOnEventQueue(
            final StringChangeListener listener,
            final StringChangeEvent event) {
        listener.stateChanged(event);
        System.out.println("OK");
    }


}




