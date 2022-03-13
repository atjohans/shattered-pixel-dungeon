package com.shatteredpixel.shatteredpixeldungeon.utils.speechSynthesis;

public interface StringChangeDispatcher {

    public void addStringChangeListener(StringChangeListener listener);
    public String getMsg();
    public void setMsg(String msg);

}
