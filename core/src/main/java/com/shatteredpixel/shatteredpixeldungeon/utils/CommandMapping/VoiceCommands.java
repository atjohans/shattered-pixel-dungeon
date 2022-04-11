package com.shatteredpixel.shatteredpixeldungeon.utils.CommandMapping;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.utils.state_management.StateReader;


public class VoiceCommands {

    //the maximum distance we will look around a tile to find a close approximation of the user command
    private static final int MAX_CHECK_DISTANCE = 6;

    public static HeroAction lastCommand;
    public static String lastDirection;
    public static Integer lastPosition;
    public static Mob lastTarget;

    public static void voiceMoveInDirection(String direction) {

        int targetPosition = checkAroundHelper(direction);
        if (targetPosition == -1) {
            StateReader.speechEventHandler.setMsg("Terrain is impassable " + direction);
            return;
        }
        Dungeon.hero.curAction = new HeroAction.Move(targetPosition);

        lastCommand = Dungeon.hero.curAction;
        lastDirection = direction;
        lastPosition = null;
        lastTarget = null;
        Dungeon.hero.act();
        Dungeon.hero.next();
    }

    public static void voiceLookTraps() {

        for (int tile = 0; tile < Dungeon.level.map.length; tile++) {
            if (Dungeon.hero.fieldOfView[tile]) {
                System.out.println(Dungeon.level.tileName(tile));
                if (Dungeon.level.map[tile] == Terrain.TRAP) {

                    StateReader.speechEventHandler.setMsg(Dungeon.level.tileName(tile)  + CommandMapper.determineRelativePos(tile));
                }
            }
        }
    }
    public static void voiceLookPaths() {


        if (checkAroundHelper("north") != -1 ) {

            StateReader.speechEventHandler.setMsg("Path north");

        }

        if (checkAroundHelper("south") != -1) {

            StateReader.speechEventHandler.setMsg("Path south");

        }
        if (checkAroundHelper("west") != -1) {

            StateReader.speechEventHandler.setMsg("Path west");

        }

        if (checkAroundHelper("east") != -1) {

            StateReader.speechEventHandler.setMsg("Path east");

        }

        if (checkAroundHelper("northwest") != -1) {

            StateReader.speechEventHandler.setMsg("Path northwest");

        }
        if (checkAroundHelper("northeast") != -1) {

            StateReader.speechEventHandler.setMsg("Path northeast");

        }
        if (checkAroundHelper("southwest") != -1) {

            StateReader.speechEventHandler.setMsg("Path southwest");

        }
        if (checkAroundHelper("southeast") != -1) {

            StateReader.speechEventHandler.setMsg("Path southeast");

        }
    }

    public static void voiceLookEnemies() {

        if (Dungeon.hero.visibleEnemies.size() == 0) {
            StateReader.speechEventHandler.setMsg("No enemies in sight");
            return;
        }

        for (Mob mob : Dungeon.hero.visibleEnemies) {
            StateReader.speechEventHandler.setMsg(mob.name() + ", " + mob.state.getTag() + ", " + CommandMapper.determineRelativePos(mob.pos));
        }

    }

    public static void voiceLookRoom(){


        for (int tile = 0; tile < Dungeon.level.map.length; tile ++){
            if (Dungeon.hero.fieldOfView[tile]) {
                System.out.println(Dungeon.level.tileName(tile));
                if (Dungeon.level.map[tile] == Terrain.EXIT){

                    StateReader.speechEventHandler.setMsg("Exit " + CommandMapper.determineRelativePos(tile));

                }else  if (Dungeon.level.map[tile] == Terrain.ENTRANCE){

                    StateReader.speechEventHandler.setMsg("Entrance " + CommandMapper.determineRelativePos(tile));

                }
            }
        }

    }

    public static void voiceLookItems() {


        if (Dungeon.level.heaps.valueList().size() == 0) {
            StateReader.speechEventHandler.setMsg("No items in sight");
            return;
        }
        for (Heap heap : Dungeon.level.heaps.valueList()) {
            if (Dungeon.hero.fieldOfView[heap.pos])
                StateReader.speechEventHandler.setMsg(heap.toString() + ", " + CommandMapper.determineRelativePos(heap.pos));

        }
    }


    public static void voiceLookDoors() {

        boolean hasDoor = false;
        for (int i = 0; i < Dungeon.level.map.length; ++i) {
            if ((Dungeon.level.map[i] == Terrain.DOOR || Dungeon.level.map[i] == Terrain.LOCKED_DOOR) && Dungeon.hero.fieldOfView[i]) {
                String response = "";
                if (Dungeon.level.map[i] == Terrain.LOCKED_DOOR){
                    response += "Locked ";
                }
                response += "Door " + CommandMapper.determineRelativePos(i);
                StateReader.speechEventHandler.setMsg(response);
                hasDoor = true;
            }

        }
        if (!hasDoor) {
            StateReader.speechEventHandler.setMsg("No doors in sight");
            return;
        }
    }

    public static void voiceReadScene() {
        voiceLookEnemies();
        voiceLookTraps();
        voiceLookItems();
        voiceLookDoors();
        voiceLookRoom();
        voiceLookPaths();

    }


    //discover farthest passable tile to stated tile (if tile unpassable)
    private static int checkAroundHelper(String direction) {

        return checkAroundHelperRecurse(direction, Dungeon.hero.pos,Dungeon.hero.pos, 0);

    }

    private static int checkAroundHelperRecurse(String direction, int lastValid, int toCheck, int count) {


        if (count >= MAX_CHECK_DISTANCE){
            if (lastValid == Dungeon.hero.pos){
                return -1;
            }
            return lastValid;
        }

        if (direction.equals("north")) {
            if (toCheck - Dungeon.level.width() > 0){
                toCheck = toCheck - Dungeon.level.width();
            }
        }
        else if (direction.equals("south")) {
            if (toCheck + Dungeon.level.width() < Dungeon.level.length()){
                toCheck = toCheck + Dungeon.level.width();
            }
        }
        else if (direction.equals("west")) {
            if (toCheck - 1 > 0){
                toCheck = toCheck - 1;
            }
        }
        else if (direction.equals("east")) {
            if (toCheck + 1 < Dungeon.level.length()){
                toCheck = toCheck + 1;
            }
        }
        else  if (direction.equals("northeast")) {
            if (toCheck - Dungeon.level.width()  + 1> 0){
                toCheck = toCheck - Dungeon.level.width()  + 1;
            }
        }else  if (direction.equals("northwest")) {
            if (toCheck - Dungeon.level.width()  -1 > 0){
                toCheck = toCheck - Dungeon.level.width()  -1;
            }
        }else  if (direction.equals("southeast")) {
            if (toCheck + Dungeon.level.width()  + 1 < Dungeon.level.length()){
                toCheck = toCheck + Dungeon.level.width()  + 1;
            }
        }else  if (direction.equals("southwest")) {
            if (toCheck + Dungeon.level.width()  - 1 < Dungeon.level.length()){
                toCheck = toCheck + Dungeon.level.width()  - 1;
            }
        }


        //allow players to step on traps
        if (Dungeon.level.map[toCheck] == Terrain.TRAP){
            lastValid = toCheck;
            return lastValid;
        }

        if ((Dungeon.level.passable[toCheck]) && (Dungeon.level.visited[toCheck] || Dungeon.hero.fieldOfView[toCheck]) ){
            lastValid = toCheck;
        }

        return checkAroundHelperRecurse(direction,lastValid,toCheck, count + 1);

    }


}
