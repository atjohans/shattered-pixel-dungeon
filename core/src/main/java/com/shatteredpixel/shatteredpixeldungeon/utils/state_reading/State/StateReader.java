package com.shatteredpixel.shatteredpixeldungeon.utils.state_reading.State;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.utils.speechRecognition.SpeechRecognitionHandler;
import com.shatteredpixel.shatteredpixeldungeon.utils.speechSynthesis.SpeechEventHandler;

/*

Class for collecting game-turn information in one place, allows game info to be
returned to user easily

 */
public class StateReader {

    public static SpeechEventHandler speechEventHandler = new SpeechEventHandler("");
    public static SpeechRecognitionHandler speechRecognitionHandler = new SpeechRecognitionHandler();

    public static boolean hasMessage = false;
    public static String command = "";

    Hero gameHero;
    Dungeon currDungeon;
    boolean[] currFieldOfView;

    public static void setCommand(String cmd){
        command = cmd;
        speechEventHandler.setMsg(cmd);
        System.out.println(cmd);



    }

    public void setGameHero(Hero gameHero){
        this.gameHero = gameHero;
    }

    public void setCurrDungeon(Dungeon dungeon){
        currDungeon = dungeon;
    }


}


