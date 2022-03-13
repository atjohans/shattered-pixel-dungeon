package com.shatteredpixel.shatteredpixeldungeon.utils.speechSynthesis;

import java.util.EventObject;

/**
 * This class lets the listener know when the change occured and what
 * object was changed.
 */
public class StringChangeEvent extends EventObject {

    private final StringChangeDispatcher dispatcher;

    public StringChangeEvent(StringChangeDispatcher dispatcher) {
        super(dispatcher);
        this.dispatcher = dispatcher;
    }

    // type safe way to get source (as opposed to getSource of EventObject
    public StringChangeDispatcher getDispatcher() {
        return dispatcher;
    }
}