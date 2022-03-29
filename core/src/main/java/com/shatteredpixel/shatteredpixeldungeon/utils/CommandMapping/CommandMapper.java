package com.shatteredpixel.shatteredpixeldungeon.utils.CommandMapping;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArcaneArmor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.utils.CommandMapping.TextProcessing.textProcessor;
import com.shatteredpixel.shatteredpixeldungeon.utils.state_management.StateReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class CommandMapper {

    private static textProcessor textProcessor = new textProcessor();

    public static ArrayList<String> possibleDirections = new ArrayList<String>(Arrays.asList("north", "south", "east", "west", "northwest", "northeast", "southwest", "southeast"));


    public static String lastCommand;



    private static boolean checkAttackCommand(ArrayList<String> processedCommandList) {
        String direction = null;

        if (processedCommandList.contains("attack")) {

            if (Dungeon.hero.visibleEnemies.size() == 0) {

                StateReader.speechEventHandler.setMsg("No visible enemies");
                return true;
            }


            String[] enemyNameElts;

            boolean hasCandidateMob = false;
            Mob CandidateMob = null;
            if (direction == null) {

                for (Mob mob : Dungeon.hero.visibleEnemies) {

                    enemyNameElts = mob.name().toLowerCase(Locale.ROOT).split("\\s+");

                    for (String nameElt : enemyNameElts) {
                        System.out.println(nameElt);
                        if (processedCommandList.contains(textProcessor.stemWord(nameElt))) {

                            while (!Dungeon.hero.canAttack(mob)) {

                                //move direction
                                VoiceCommands.voiceMoveTowardsObject(mob.pos);
                            }

                            VoiceCommands.voiceAttack(mob);
                            return true;
                        }
                    }

                }

                StateReader.speechEventHandler.setMsg("No mob of specified name nearby");


            } else {

                boolean hasMobInDirection = false;
                for (Mob mob : Dungeon.hero.visibleEnemies) {

                    if (determineRelativePos(mob.pos).equals(direction)) {
                        hasMobInDirection = true;
                        enemyNameElts = mob.name().toLowerCase(Locale.ROOT).split("\\s+");
                        for (String nameElt : enemyNameElts) {
                            if (processedCommandList.contains(textProcessor.stemWord(nameElt))) {

                                while (!Dungeon.hero.canAttack(mob)) {
                                    VoiceCommands.voiceMoveTowardsObject(mob.pos);
                                }
                                VoiceCommands.voiceAttack(mob);
                                return true;
                            }
                        }
                    }
                }

                if (!hasMobInDirection) {
                    StateReader.speechEventHandler.setMsg("No enemies in direction " + direction);
                } else {
                    StateReader.speechEventHandler.setMsg("No mob of specified name nearby");
                }

            }
            VoiceCommands.voiceLookEnemies();
            return true;


        }

        return false;
    }


    private static boolean checkGrabCommand(ArrayList<String> processedCommandList) {

        String direction = null;

        if (processedCommandList.contains("grab")) {

            if (Dungeon.level.heaps.valueList().size() == 0) {

                StateReader.speechEventHandler.setMsg("No visible items");
                return true;
            }

            String[] itemNameElts;

            boolean hasCandidateItem = false;
            Heap CandidateItem = null;

            if (direction == null) {

                for (Heap heap : Dungeon.level.heaps.valueList()) {

                    if (Dungeon.hero.fieldOfView[heap.pos]) {
                        itemNameElts = heap.toString().toLowerCase(Locale.ROOT).split("\\s+");

                        for (String nameElt : itemNameElts) {
                            System.out.println(nameElt);
                            if (processedCommandList.contains(textProcessor.stemWord(nameElt))) {

                                VoiceCommands.voiceGrabItem(heap.pos);
                                return true;
                            }
                        }

                    }
                }

                    StateReader.speechEventHandler.setMsg("No item of specified name nearby");

            } else {

                boolean hasItemInDirection = false;
                for (Heap heap : Dungeon.level.heaps.valueList()) {
                if (Dungeon.hero.fieldOfView[heap.pos]) {
                    if (determineRelativePos(heap.pos).equals(direction)) {
                        hasItemInDirection = true;
                        itemNameElts = heap.toString().toLowerCase(Locale.ROOT).split("\\s+");
                        for (String nameElt : itemNameElts) {
                            if (processedCommandList.contains(nameElt)) {
                              VoiceCommands.voiceGrabItem(heap.pos);
                            }
                        }
                    }
                }
                }

                if (!hasItemInDirection) {
                    StateReader.speechEventHandler.setMsg("No items in direction " + direction);
                } else {
                    StateReader.speechEventHandler.setMsg("No item of specified name nearby");
                }

            }
            VoiceCommands.voiceLookItems();
            return true;



        }
        return false;
    }

    private static boolean checkMoveCommand(ArrayList<String> processedCommandList) {


        String direction = getDirection(processedCommandList);
        String keyword = null;

        HashMap<String, Integer> instanceCount = new HashMap<>();
        int tracker = -1;
        //iterate over the field of view, check if any game objects match the input
        for (int i = 0; i < Dungeon.level.map.length; ++i) {
            String tileName = tileToString(i);
            if (Dungeon.hero.fieldOfView[i]) {
                for (String cmd : processedCommandList) {
                    for (String nameElt : tileName.split("\\s+")) {
                        if (cmd.contains(nameElt)) {
                            keyword = nameElt;
                            tracker = i;
                            if (direction != null && direction.equals(determineRelativePos(i))) {
                                VoiceCommands.voiceMoveTowardsObject(i);
                                return true;
                            }

                            if (instanceCount.containsKey(tileName)) {
                                instanceCount.put(tileName, instanceCount.get(tileName) + 1);
                            } else {
                                instanceCount.put(tileName, 1);
                            }

                        }
                    }
                }
            }
        }

        if (keyword == null && direction != null){
            VoiceCommands.voiceMoveInDirection(direction);
            return true;
        }

        //if we have checked all visible tiles and no match is found
        if (instanceCount.size() > 0) {

            if (direction != null) {

                StateReader.speechEventHandler.setMsg("No " + keyword + "s to the " + direction);
                return true;

            } else {

                if (instanceCount.containsKey(tileToString(tracker))) {

                    if (instanceCount.get(tileToString(tracker)) == 1) {

                        VoiceCommands.voiceMoveTowardsObject(tracker);
                        return true;

                    }

                }

                StateReader.speechEventHandler.setMsg("Multiple " + keyword + "s visible, specify direction");
                return true;

            }

        }

        //wasn't a move command
        return false;
    }

    private static boolean checkMapCommand(ArrayList<String> processedCommandList){

        if (processedCommandList.contains("map")){


            return true;
        }

        return false;
    }

    private static boolean checkLookCommand(ArrayList<String> processedCommandList) {


        if (processedCommandList.contains("look")) {

            if (processedCommandList.contains("enemi")) {

                VoiceCommands.voiceLookEnemies();
                return true;
            }
            if (processedCommandList.contains("door")) {

                VoiceCommands.voiceLookDoors();
                return true;
            }
            if (processedCommandList.contains("item")) {
                VoiceCommands.voiceLookItems();
                return true;
            }
            if (processedCommandList.contains("paths")) {
                VoiceCommands.voiceLookPaths();
                return true;
            }

            VoiceCommands.voiceReadScene();
            return true;

        }

        //wasnt a look command
        return false;

    }



    public static void mapCommand(String command) {

        ArrayList<String> processedCommandList = textProcessor.processCommand(command);

        System.out.println(processedCommandList);

        //ORDER MATTERS FOR CONDITION CHECKS - Move command is most general, so we put at bottom

        if (checkAttackCommand(processedCommandList)) {
            System.out.println("ATTACK COMMAND");
            lastCommand = command;
            return;
        }
        if (checkMapCommand(processedCommandList)){
            System.out.println("MAP COMMAND");
            lastCommand = command;
            return;
        }
        if (checkLookCommand(processedCommandList)) {
            System.out.println("LOOK COMMAND");
            lastCommand = command;
            return;
        }
        if (checkGrabCommand(processedCommandList)){
            System.out.println("GRAB COMMAND");
            lastCommand = command;
            return;
        }
        if (checkMoveCommand(processedCommandList)) {
            System.out.println("MOVE COMMAND");
            lastCommand = command;
            return;
        }

        StateReader.speechEventHandler.setMsg("Unknown Command " + command + " please repeat");

    }



    /*-----HELPER FUNCTIONS-----*/


    public static String tileToString(int tile) {


        //prioritize mobs, return mob at tile
        Mob mob = Dungeon.level.findMob(tile);

        if (mob != null) {
            return mob.name().toLowerCase(Locale.ROOT);
        }

        //then items

        for (Heap heap : Dungeon.level.heaps.valueList()) {
            if (heap.pos == tile) {
                return heap.toString().toLowerCase(Locale.ROOT);
            }
        }
        //then tile
        return Dungeon.level.tileName(Dungeon.level.map[tile]).toLowerCase(Locale.ROOT);
    }

    public static String determineRelativePos(int tile) {

        String relativePos = "";
        int heroPos = Dungeon.hero.pos;

        if (tile <= heroPos - Dungeon.level.width()) {
            relativePos += "north";
        }
        if (tile >= heroPos + Dungeon.level.width()) {
            relativePos += "south";
        }
        if (tile % Dungeon.level.width() < heroPos % Dungeon.level.width()) {
            relativePos += "west";
        }
        if (tile % Dungeon.level.width() > heroPos % Dungeon.level.width()) {
            relativePos += "east";
        }
        return relativePos;
    }

    private static String getDirection(ArrayList<String> processedCommandList) {

        String direction = null;
        for (String word : processedCommandList) {
            if (possibleDirections.contains(word)) {
                direction = word;
            }


        }

        return direction;
    }
}
