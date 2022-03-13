package com.shatteredpixel.shatteredpixeldungeon.utils.speechSynthesis;

import java.util.EventListener;

/**
 * Listener interface for classes interested in knowing about a boolean
 * flag change.
 */
public interface StringChangeListener extends EventListener {

    public void stateChanged(StringChangeEvent event);

}

