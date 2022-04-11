package com.shatteredpixel.shatteredpixeldungeon.utils.CommandMapping;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArcaneArmor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.DamageWand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Crossbow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Journal;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
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
        Item bestMatch = (Item) bestMatching("item", direction, processedCommandList);
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
                StateReader.speechEventHandler.setMsg("No mob to target " + direction);
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

        if (processedCommandList.contains("attack") || processedCommandList.contains("shoot")) {
            if (Dungeon.hero.visibleEnemies.size() == 0) {
                StateReader.speechEventHandler.setMsg("No visible enemies");
                return true;
            }
            String direction = getDirection(processedCommandList);
            Mob bestMatch = (Mob) bestMatching("mob", direction, processedCommandList);
            if (bestMatch == null) {
                if (direction == null)
                    StateReader.speechEventHandler.setMsg("No matching mob nearby");
                else
                    StateReader.speechEventHandler.setMsg("No matching mob  " + direction);
                return true;
            }

            if (processedCommandList.contains("attack")) {
                Dungeon.hero.handle(bestMatch.pos);
                Dungeon.hero.act();
                Dungeon.hero.next();
            } else {

                //pobably a better way to do this than enumerating possible weapons
                KindOfWeapon weapon = Dungeon.hero.belongings.weapon();
                if (weapon instanceof MagesStaff){

                    if (((MagesStaff) weapon).getWand().curCharges > 0) {
                        ((MagesStaff) weapon).getWand().execute(Dungeon.hero, bestMatch, ((MagesStaff) weapon).getWand().defaultAction);
                    }else{
                        StateReader.speechEventHandler.setMsg("Staff Wand out of Charge");
                    }
                }
                else if (weapon instanceof SpiritBow){
                    ((SpiritBow)weapon).execute(Dungeon.hero, bestMatch,weapon.defaultAction);
                }
            }
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
            Heap bestMatch = (Heap) bestMatching("heap", direction, processedCommandList);
            if (bestMatch == null) {
                if (direction == null)
                    StateReader.speechEventHandler.setMsg("No matching item nearby");
                else
                    StateReader.speechEventHandler.setMsg("No matching item " + direction);
                return true;
            }
            System.out.println("Grabbing Item");
            Dungeon.hero.handle(bestMatch.pos);
            Dungeon.hero.act();
            Dungeon.hero.next();
            return true;
        }
        return false;
    }

    private static boolean bestGuess(ArrayList<String> processedCommandList) {

        String direction = getDirection(processedCommandList);
        //check for enemy name
        Mob bestMob = (Mob) bestMatching("mob", direction, processedCommandList);

        if (bestMob != null) {
            processedCommandList.add("attack");
            return checkAttackCommand(processedCommandList);
        }

        Heap bestHeap = (Heap) bestMatching("heap", direction, processedCommandList);
        if (bestHeap != null) {
            processedCommandList.add("grab");
            return checkGrabCommand(processedCommandList);
        }

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
            if (processedCommandList.contains(("traps"))) {
                System.out.println("LOOK ROOM");
                VoiceCommands.voiceLookTraps();
                return true;

            }
            VoiceCommands.voiceReadScene();
            return true;

        }

        //wasnt a look command
        return false;

    }

    private static boolean checkMoveCommand(ArrayList<String> processedCommandList) {

        String direction = getDirection(processedCommandList);

        if (processedCommandList.contains("exit") || processedCommandList.contains("entrance")){

            for (int i = 0; i < Dungeon.level.map.length; ++i){


                if (Dungeon.hero.fieldOfView[i] && (Dungeon.level.map[i] == Terrain.EXIT || Dungeon.level.map[i] == Terrain.ENTRANCE)){

                    Dungeon.hero.handle(i);
                    Dungeon.hero.act();
                    Dungeon.hero.next();
                    return true;
                }


            }


        }

        if (direction != null) {

            VoiceCommands.voiceMoveInDirection(direction);

            return true;
        }
        return false;
    }

    private static boolean checkHelpCommand(ArrayList<String> processedCommandList) {

        if (processedCommandList.contains("help")) {

            StateReader.speechEventHandler.setMsg("Swipe on the screen to move in that direction");

            StateReader.speechEventHandler.setMsg("Single click to initiate voice recognition");

            StateReader.speechEventHandler.setMsg("Use voice commands to grab items, attack and use certain items");

            StateReader.speechEventHandler.setMsg("A typical voice command specifies an object name, and an action such as attack or grab. You can also provide a direction");

            StateReader.speechEventHandler.setMsg("Examine the dungeon by using the look command");

            StateReader.speechEventHandler.setMsg("Double click the screen to repeat a command");

            StateReader.speechEventHandler.setMsg("You can also use voice commands to open windows, such as your inventory or the pause menu");

            StateReader.speechEventHandler.setMsg("Navigate through these windows by clicking to scroll through options, double click to activate, and triple click to go back");


            return true;
        }

        return false;
    }

    public static void mapCommand(String command) {
        if (!Dungeon.hero.ready) {
            Dungeon.hero.next();
        }
        ArrayList<String> processedCommandList = textProcessor.processCommand(command);

        System.out.println(processedCommandList);

        if (checkMenuCommand(processedCommandList)) {
            System.out.println("MENU COMMAND");
            lastCommand = command;
            return;
        }
        if (checkHelpCommand(processedCommandList)) {
            System.out.println("HELP COMMAND");
            lastCommand = command;
            return;
        }

        //ORDER MATTERS FOR CONDITION CHECKS - Move command is most general, so we put at bottom
        if (Dungeon.hero.isAlive()) {

            if (checkLevelUpCommand(processedCommandList)) {
                System.out.println("LEVELUP COMMAND");
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
            if (bestGuess(processedCommandList)) {
                System.out.println("GUESS COMMAND");
                lastCommand = command;
                return;
            }
        }


        StateReader.speechEventHandler.setMsg("Unknown Command, " + command + "please repeat");

    }





    /*-----HELPER FUNCTIONS-----*/

    private static Object bestMatching(String objType, String direction, ArrayList<String> processedCommandList) {

        Integer curBest = 0;
        ArrayList<String> entityNameElts;
        int numMatching = 0;

        if (objType.equals("heap")) {
            Heap bestMatch = null;
            for (Heap heap : Dungeon.level.heaps.valueList()) {
                entityNameElts = new ArrayList<>(Arrays.asList(heap.toString().toLowerCase(Locale.ROOT).split("\\s+")));
                numMatching = numMatching(entityNameElts, processedCommandList);
                if (numMatching > 0 && numMatching > curBest) {
                    if (direction == null) {
                        if (bestMatch == null) {
                            bestMatch = heap;
                            curBest = numMatching;
                        } else {

                            if (determineDistanceToHero(heap.pos) < determineDistanceToHero(bestMatch.pos)) {
                                bestMatch = heap;
                                curBest = numMatching;
                            }

                        }
                    } else {
                        if (determineRelativePos(heap.pos).equals(direction)) {
                            if (bestMatch == null) {
                                bestMatch = heap;
                                curBest = numMatching;
                            } else {

                                if (determineDistanceToHero(heap.pos) < determineDistanceToHero(bestMatch.pos)) {
                                    bestMatch = heap;
                                    curBest = numMatching;
                                }

                            }
                        }
                    }
                }
            }

            return bestMatch;

        } else if (objType.equals("mob")) {
            Mob bestMatch = null;
            for (Mob mob : Dungeon.hero.visibleEnemies) {
                entityNameElts = new ArrayList<>(Arrays.asList(mob.name().toLowerCase(Locale.ROOT).split("\\s+")));
                numMatching = numMatching(entityNameElts, processedCommandList);
                if (numMatching > 0 && numMatching > curBest) {
                    if (direction == null) {
                        if (bestMatch == null) {
                            bestMatch = mob;
                            curBest = numMatching;
                        } else {

                            if (determineDistanceToHero(mob.pos) < determineDistanceToHero(bestMatch.pos)) {
                                bestMatch = mob;
                                curBest = numMatching;
                            }

                        }
                    } else {
                        if (determineRelativePos(mob.pos).equals(direction)) {
                            if (bestMatch == null) {
                                bestMatch = mob;
                                curBest = numMatching;
                            } else {

                                if (determineDistanceToHero(mob.pos) < determineDistanceToHero(bestMatch.pos)) {
                                    bestMatch = mob;
                                    curBest = numMatching;
                                }

                            }
                        }
                    }
                }
            }

            return bestMatch;

        } else if (objType.equals("item")) {
            Item bestMatch = null;
            for (Item item : Dungeon.hero.belongings.backpack) {

                ArrayList<String> itemNameElts = new ArrayList<>(Arrays.asList(item.name().toLowerCase(Locale.ROOT).split("\\s+")));

                numMatching = numMatching(itemNameElts, processedCommandList);
                if (numMatching > 0 && numMatching > curBest) {

                    curBest = numMatching;
                    bestMatch = item;
                }

            }
            return bestMatch;
        }
        return null;
    }

    private static double determineDistanceToHero(int pos) {

        int posX = pos % Dungeon.level.width();
        int posY = pos % Dungeon.level.height();

        int heroX = Dungeon.hero.pos % Dungeon.level.width();
        int heroY = Dungeon.hero.pos % Dungeon.level.height();


        double dist = Math.sqrt(Math.pow(posX - heroX, 2) + Math.pow(posY - heroY, 2));

        return dist;
    }

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

        if (heroPos == tile) {
            return " below you ";
        }

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
