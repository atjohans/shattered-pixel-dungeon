package com.shatteredpixel.shatteredpixeldungeon.utils.CommandMapping;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.utils.state_management.StateReader;

import java.util.ArrayList;


public class VoiceCommands {

    //the maximum distance we will look around a tile to find a close approximation of the user command
    private static final int MAX_CHECK_DISTANCE = 10;

    public static HeroAction lastCommand;
    public static String lastDirection;
    public static Integer lastPosition;
    public static Mob lastTarget;

    public static void voiceMoveInDirection(String direction) {

        int targetPosition = checkAroundHelper(direction);
        if (targetPosition == -1 || targetPosition == Dungeon.hero.pos){
            StateReader.speechEventHandler.setMsg("Terrain is impassable to the "+ direction);
            return;
        }
        Dungeon.hero.curAction = new HeroAction.Move(targetPosition);
        lastCommand = Dungeon.hero.curAction;
        lastDirection = direction;
        lastPosition = null;
        lastTarget = null;
        Dungeon.hero.act();

        StateReader.updateState();
        Dungeon.hero.next();
    }

    public static void voiceMoveTowardsObject(int objPosition) {
        System.out.println("MOVING TOWARDS: " + CommandMapper.determineRelativePos((Integer) objPosition));
        Dungeon.hero.curAction = new HeroAction.Move((Integer) objPosition);
        lastCommand = Dungeon.hero.curAction;
        lastDirection = null;
        lastTarget = null;
        lastPosition = objPosition;
        Dungeon.hero.act();

        StateReader.updateState();
        Dungeon.hero.next();

    }


    public static void voiceAttack(Mob mob) {

        Dungeon.hero.curAction = new HeroAction.Attack((Mob) mob);
        lastCommand = Dungeon.hero.curAction;
        lastDirection = null;
        lastPosition = null;
        lastTarget = mob;
        Dungeon.hero.act();

        StateReader.updateState();
        Dungeon.hero.next();

    }



    public static void voiceGrabItem(Integer pos) {

        Dungeon.hero.curAction = new HeroAction.PickUp(pos);
        Dungeon.hero.act();
        StateReader.updateState();
        Dungeon.hero.next();
    }


    public static void repeat() {

        if (lastCommand instanceof HeroAction.Move){

            if (lastDirection != null){
                voiceMoveInDirection(lastDirection);

            }
            else if(lastPosition != null){
                voiceMoveTowardsObject(lastPosition);

            }

        }

        else if (lastCommand instanceof HeroAction.Attack){

            if (lastTarget.isAlive()){
                voiceAttack(lastTarget);

            }

        }
    }

    public static void voiceLookPaths() {


        if (checkAroundHelper("north") != -1 ) {

            StateReader.speechEventHandler.setMsg("Path to the north");

        }

        if (checkAroundHelper("south") != -1) {

            StateReader.speechEventHandler.setMsg("Path to the south");

        }
        if (checkAroundHelper("west") != -1) {

            StateReader.speechEventHandler.setMsg("Path to the west");

        }

        if (checkAroundHelper("east") != -1) {

            StateReader.speechEventHandler.setMsg("Path to the east");

        }

        if (checkAroundHelper("northwest") != -1) {

            StateReader.speechEventHandler.setMsg("Path to the northwest");

        }
        if (checkAroundHelper("northeast") != -1) {

            StateReader.speechEventHandler.setMsg("Path to the northeast");

        }
        if (checkAroundHelper("southwest") != -1) {

            StateReader.speechEventHandler.setMsg("Path to the southwest");

        }
        if (checkAroundHelper("southeast") != -1) {

            StateReader.speechEventHandler.setMsg("Path to the southeast");

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
            if (Dungeon.level.map[i] == Terrain.DOOR && Dungeon.hero.fieldOfView[i]) {
                StateReader.speechEventHandler.setMsg("Door " + CommandMapper.determineRelativePos(i));
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
        voiceLookItems();
        voiceLookDoors();
        voiceLookPaths();
    }


    public static void voiceLookMap(){



    }

    //discover farthest passable tile to stated tile (if tile unpassable)
    private static int checkAroundHelper(String direction) {


        ArrayList<Integer> candidatePoints = new ArrayList<>();

        for (int i = 0; i < Dungeon.level.map.length; ++i){

            if (Dungeon.level.passable[i] /*&& Dungeon.hero.fieldOfView[i]*/ && CommandMapper.determineRelativePos(i).equals(direction)){
                candidatePoints.add(i);
            }

        }

        int x;
        int y;
        int playerX = Dungeon.hero.pos % Dungeon.level.width();
        int playerY = Dungeon.hero.pos % Dungeon.level.height();
        int distance;
        int furthestPoint = -1;
        if (candidatePoints.size() > 0){
            Integer maxDist = -1;
            
            for (Integer point: candidatePoints){
                x = point % Dungeon.level.width();
                y = point % Dungeon.level.height();

                distance = (int) Math.sqrt(Math.pow((double)(playerX-x) ,2) + Math.pow((double)(playerY-y) ,2));

                if (distance > maxDist ){
                    maxDist = distance;
                    furthestPoint = point;
                }

            }
            return furthestPoint;
        }

        return -1;
    }


    private static int directionToValue(String direction, int dest) {

        //if still no nearby tile is passable, recurse in direction of "direction" arg
        if (direction.equals("north")) {

            return dest - Dungeon.level.width();
        }
        if (direction.equals("south")) {

            return dest + Dungeon.level.width();
        }
        if (direction.equals("east")) {

            return dest + 1;
        }
        if (direction.equals("west")) {

            return dest - 1;
        }
        if (direction.equals("northeast")) {

            return dest - (Dungeon.level.width()) + 1;
        }
        if (direction.equals("northwest")) {
            return dest - (Dungeon.level.width()) - 1;

        }
        if (direction.equals("southeast")) {
            return dest + (Dungeon.level.width()) + 1;

        }
        if (direction.equals("southwest")) {
            return dest + (Dungeon.level.width()) - 1;

        }

        return -1;
    }
}
