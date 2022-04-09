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

    public static void handleCommand(String cmd) {





        mapper.mapCommand(cmd);


    }


}





