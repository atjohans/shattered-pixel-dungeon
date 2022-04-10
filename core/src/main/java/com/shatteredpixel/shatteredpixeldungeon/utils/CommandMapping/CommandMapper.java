package com.shatteredpixel.shatteredpixeldungeon.utils.CommandMapping;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArcaneArmor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.journal.Journal;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.CommandMapping.TextProcessing.textProcessor;
import com.shatteredpixel.shatteredpixeldungeon.utils.state_management.StateReader;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndGame;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHero;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTabbed;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class CommandMapper {

    private static textProcessor textProcessor = new textProcessor();

    public static ArrayList<String> possibleDirections = new ArrayList<String>(Arrays.asList("north", "south", "east", "west", "northwest", "northeast", "southwest", "southeast"));


    public static String lastCommand;


    private static boolean checkLevelUpCommand(ArrayList<String> processedCommandList) {

        return false;
    }

    private static boolean checkMenuCommand(ArrayList<String> processedCommandList) {

         if (processedCommandList.contains("menu")) {
            GameScene.show(new WndGame());
            return true;
        }

        return false;
    }

    private static boolean checkSearchCommand(ArrayList<String> processedCommandList) {

        if (processedCommandList.contains("search")) {
            Dungeon.hero.search(true);
            return true;
        }

        return false;
    }


    private static boolean checkRestCommand(ArrayList<String> processedCommandList) {

        if (processedCommandList.contains("rest")) {
            if (processedCommandList.contains("full")) {

                Dungeon.hero.rest(true);
                return true;
            }
            Dungeon.hero.rest(false);
            return true;
        }

        return false;
    }

    private static boolean checkItemCommand(ArrayList<String> processedCommandList) {

        String direction = getDirection(processedCommandList);
        Item bestMatch = null;
        Integer curBest = 0;
        int numMatching = 0;
        for (Item item : Dungeon.hero.belongings.backpack) {

            ArrayList<String> itemNameElts = new ArrayList<>(Arrays.asList(item.name().toLowerCase(Locale.ROOT).split("\\s+")));

            numMatching = numMatching(itemNameElts, processedCommandList);
            if (numMatching > 0 && numMatching > curBest) {

                curBest = numMatching;
                bestMatch = item;
            }

        }

        if (bestMatch == null) {
            return false;
        }
        System.out.println("ASSUMING: " + bestMatch.name());

        if (processedCommandList.contains("equip")) {
            if (bestMatch instanceof EquipableItem) {
                ((EquipableItem) bestMatch).doEquip(Dungeon.hero);
                StateReader.speechEventHandler.setMsg(bestMatch.name() + " equipped");
                return true;
            }

        } else if (processedCommandList.contains("unequip")) {
            if (bestMatch instanceof EquipableItem) {
                ((EquipableItem) bestMatch).doUnequip(Dungeon.hero, true);
                StateReader.speechEventHandler.setMsg(bestMatch.name() + " equipped");
                return true;
            }

        } else if (processedCommandList.contains("drop")) {
            bestMatch.doDrop(Dungeon.hero);
            StateReader.speechEventHandler.setMsg("Dropped " + bestMatch.name());
            return true;

        } else if (processedCommandList.contains("info")) {
            System.out.println("OK");
            StateReader.speechEventHandler.setMsg(bestMatch.info());
            return true;
        } else if (processedCommandList.contains("throw")) {


            for (Mob mob : Dungeon.hero.visibleEnemies) {
                String[] enemyNameElts = mob.name().toLowerCase(Locale.ROOT).split("\\s+");

                for (String enemyNameElt : enemyNameElts) {
                    if (processedCommandList.contains(enemyNameElt)) {

                        if (direction == null) {
                            bestMatch.cast(Dungeon.hero, mob.pos);
                            StateReader.speechEventHandler.setMsg("Throwing " + bestMatch.name() + " at " + mob.name());
                            return true;
                        } else {
                            if (determineRelativePos(mob.pos).equals(direction)) {
                                bestMatch.cast(Dungeon.hero, mob.pos);
                                StateReader.speechEventHandler.setMsg("Throwing " + bestMatch.name() + " at " + mob.name());
                                return true;
                            }
                        }
                    }
                }
            }

            if (direction != null) {
                StateReader.speechEventHandler.setMsg("No mob to target to the " + direction);
            } else {
                StateReader.speechEventHandler.setMsg("No mob of name to target");
            }

            return true;

        } else if (processedCommandList.contains("eat")) {

            if (bestMatch instanceof Food) {

                ((Food) bestMatch).execute(Dungeon.hero, Food.AC_EAT);
            } else {
                StateReader.speechEventHandler.setMsg("Cannot eat " + bestMatch.name());
            }
            return true;
        } else if (processedCommandList.contains("drink")) {

            if (bestMatch instanceof Potion) {

                ((Potion) bestMatch).execute(Dungeon.hero, Potion.AC_DRINK);
            } else if (bestMatch instanceof Waterskin) {

                ((Waterskin) bestMatch).execute(Dungeon.hero, Waterskin.AC_DRINK);

            } else {
                StateReader.speechEventHandler.setMsg("Cannot drink " + bestMatch.name());
            }
            return true;
        } else if (processedCommandList.contains("use")) {


            StateReader.speechEventHandler.setMsg("Click to scroll through options, double click to activate, triple click to close");


            WndBag tempBagWnd = (new WndBag(Dungeon.hero.belongings.backpack));
            GameScene.show(new WndUseItem(tempBagWnd, bestMatch));
            return true;
        }


        return false;
    }

    private static boolean checkInterfaceCommand(ArrayList<String> processedCommandList) {

        if (processedCommandList.contains("inventory")) {

            GameScene.show(new WndBag(Dungeon.hero.belongings.backpack));
            return true;

        }
        if (processedCommandList.contains("journal")) {
            return true;
        }
        if (processedCommandList.contains("status")) {

            StateReader.speechEventHandler.setMsg("Health at " + (int) (((float) Dungeon.hero.HP / (float) Dungeon.hero.HT) * 100) + " percent");
            return true;

        }


        return false;
    }


    private static boolean checkAttackCommand(ArrayList<String> processedCommandList) {

        if (processedCommandList.contains("attack")) {

            if (Dungeon.hero.visibleEnemies.size() == 0) {

                StateReader.speechEventHandler.setMsg("No visible enemies");
                return true;
            }


            String direction = getDirection(processedCommandList);
            Mob bestMatch = null;
            Integer curBest = 0;
            int numMatching = 0;
            for (Mob mob : Dungeon.hero.visibleEnemies) {

                ArrayList<String> mobNameElts = new ArrayList<>(Arrays.asList(mob.name().toLowerCase(Locale.ROOT).split("\\s+")));

                numMatching = numMatching(mobNameElts, processedCommandList);
                if (numMatching > 0 && numMatching > curBest) {
                    if (direction == null) {
                        curBest = numMatching;
                        bestMatch = mob;
                    } else {
                        if (determineRelativePos(mob.pos).equals(direction)) {
                            curBest = numMatching;
                            bestMatch = mob;
                        }
                    }
                }

            }

            if (bestMatch == null) {
                if (direction == null)
                    StateReader.speechEventHandler.setMsg("No matching mob nearby");
                else
                    StateReader.speechEventHandler.setMsg("No matching mob to the " + direction);
                return true;
            }


            Dungeon.hero.handle(bestMatch.pos);
            Dungeon.hero.act();
            Dungeon.hero.next();
            return true;

        }
        return false;
    }


    private static boolean checkGrabCommand(ArrayList<String> processedCommandList) {

        String direction = getDirection(processedCommandList);

        if (processedCommandList.contains("grab")) {

            if (Dungeon.level.heaps.valueList().size() == 0) {

                StateReader.speechEventHandler.setMsg("No visible items");
                return true;
            }

            Heap bestMatch = null;
            Integer curBest = 0;
            int numMatching = 0;
            for (Heap heap : Dungeon.level.heaps.valueList()) {
                if (Dungeon.hero.fieldOfView[heap.pos]) {
                    ArrayList<String> mobNameElts = new ArrayList<>(Arrays.asList(heap.toString().toLowerCase(Locale.ROOT).split("\\s+")));

                    numMatching = numMatching(mobNameElts, processedCommandList);
                    if (numMatching > 0 && numMatching > curBest) {
                        if (direction == null) {
                            curBest = numMatching;
                            bestMatch = heap;
                        } else {
                            if (determineRelativePos(heap.pos).equals(direction)) {
                                curBest = numMatching;
                                bestMatch = heap;
                            }
                        }
                    }

                }
            }
            if (bestMatch == null) {
                if (direction == null)
                    StateReader.speechEventHandler.setMsg("No matching item nearby");
                else
                    StateReader.speechEventHandler.setMsg("No matching item to the " + direction);
                return true;
            }


            Dungeon.hero.handle(bestMatch.pos);
            Dungeon.hero.act();
            Dungeon.hero.next();
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
            if (!(Dungeon.hero.fieldOfView == null) && Dungeon.hero.fieldOfView[i]) {
                for (String cmd : processedCommandList) {
                    for (String nameElt : tileName.split("\\s+")) {
                        if (cmd.contains(nameElt)) {
                            keyword = nameElt;
                            tracker = i;
                            if (direction != null && direction.equals(determineRelativePos(i))) {
                                Dungeon.hero.handle(i);
                                Dungeon.hero.act();
                                Dungeon.hero.next();
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

        if (keyword == null && direction != null) {
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
                        Dungeon.hero.handle(tracker);
                        Dungeon.hero.act();
                        Dungeon.hero.next();
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

    private static boolean checkMapCommand(ArrayList<String> processedCommandList) {

        if (processedCommandList.contains("map")) {


            return true;
        }

        return false;
    }

    private static boolean checkLookCommand(ArrayList<String> processedCommandList) {


        if (processedCommandList.contains("look")) {

            if (processedCommandList.contains("enemy") || processedCommandList.contains("enemies")) {

                VoiceCommands.voiceLookEnemies();
                return true;
            }
            if (processedCommandList.contains("door") || processedCommandList.contains("doors")) {

                VoiceCommands.voiceLookDoors();
                return true;
            }
            if (processedCommandList.contains("item") || processedCommandList.contains("items")) {
                VoiceCommands.voiceLookItems();
                return true;
            }
            if (processedCommandList.contains("paths")) {
                VoiceCommands.voiceLookPaths();
                return true;
            }
            if (processedCommandList.contains(("room"))) {
                System.out.println("LOOK ROOM");
                VoiceCommands.voiceLookRoom();
                return true;

            }

            VoiceCommands.voiceReadScene();
            return true;

        }

        //wasnt a look command
        return false;

    }


    public static void mapCommand(String command) {
        if (!Dungeon.hero.ready) {
            Dungeon.hero.next();
        }
        ArrayList<String> processedCommandList = textProcessor.processCommand(command);

        System.out.println(processedCommandList);


        //ORDER MATTERS FOR CONDITION CHECKS - Move command is most general, so we put at bottom
        if (Dungeon.hero.isAlive()) {

            if (checkLevelUpCommand(processedCommandList)) {
                System.out.println("LEVELUP COMMAND");
                lastCommand = command;
                return;
            }
            if (checkMenuCommand(processedCommandList)) {
                System.out.println("MENU COMMAND");
                lastCommand = command;
                return;
            }
            if (checkAttackCommand(processedCommandList)) {
                System.out.println("ATTACK COMMAND");
                lastCommand = command;
                return;
            }
            if (checkSearchCommand(processedCommandList)) {
                System.out.println("SEARCH COMMAND");
                lastCommand = command;
                return;
            }
            if (checkRestCommand(processedCommandList)) {
                System.out.println("REST COMMAND");
                lastCommand = command;
                return;
            }
            if (checkItemCommand(processedCommandList)) {
                System.out.println("ITEM COMMAND");
                lastCommand = command;
                return;
            }
            if (checkInterfaceCommand((processedCommandList))) {
                System.out.println("INTERFACE COMMAND");
                lastCommand = command;
                return;
            }
            if (checkMapCommand(processedCommandList)) {
                System.out.println("MAP COMMAND");
                lastCommand = command;

                return;
            }
            if (checkLookCommand(processedCommandList)) {
                System.out.println("LOOK COMMAND");
                lastCommand = command;

                return;
            }
            if (checkGrabCommand(processedCommandList)) {
                System.out.println("GRAB COMMAND");
                lastCommand = command;

                return;
            }
            if (checkMoveCommand(processedCommandList)) {
                System.out.println("MOVE COMMAND");
                lastCommand = command;

                return;
            }
        }


        StateReader.speechEventHandler.setMsg("Unknown Command, please repeat");
    }



    /*-----HELPER FUNCTIONS-----*/


    private static int numMatching(ArrayList<String> tester, ArrayList<String> toCheck) {

        int numContained = 0;
        for (String elt : tester) {

            if (toCheck.contains(elt)) {
                numContained += 1;
            }

        }
        return numContained;
    }


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
