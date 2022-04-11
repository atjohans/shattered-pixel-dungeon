package com.shatteredpixel.shatteredpixeldungeon.utils.state_management;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.CommandMapping.CommandMapper;
import com.shatteredpixel.shatteredpixeldungeon.utils.speechRecognition.SpeechRecognitionHandler;
import com.shatteredpixel.shatteredpixeldungeon.utils.speechSynthesis.SpeechEventHandler;

import java.util.ArrayList;


/*

Class for collecting game-turn information in one place, allows game info to be
returned to user easily

 */




public class StateReader {

    public static SpeechEventHandler speechEventHandler = new SpeechEventHandler("");
    public static SpeechRecognitionHandler speechRecognitionHandler = new SpeechRecognitionHandler();


    public static final String speechEventStop = "*****XXXX*****";

    public static boolean busy = false;

    public static CommandMapper mapper = new CommandMapper();

    public static String baseCommand = null;

    public static void handleCommand(String cmd) {

        //Base Command system not the best way to handle differentiating between commands but works for now
        System.out.println("Base Command: " + baseCommand);

        //if this is a brand new command
        if (baseCommand == null) {
            mapper.mapCommand(cmd);
        }else{
            //if we are supplying additional information to an existing command
            mapper.mapCommand(baseCommand + " " +  cmd);
            baseCommand = null;
        }
    }


}







