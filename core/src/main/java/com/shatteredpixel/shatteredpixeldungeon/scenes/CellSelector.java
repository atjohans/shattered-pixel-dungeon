/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.CommandMapping.CommandMapper;
import com.shatteredpixel.shatteredpixeldungeon.utils.CommandMapping.VoiceCommands;
import com.shatteredpixel.shatteredpixeldungeon.utils.state_management.StateReader;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.input.ScrollEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ScrollArea;
import com.watabou.utils.GameMath;
import com.watabou.utils.PointF;
import com.watabou.utils.Signal;

public class CellSelector extends ScrollArea {

    public Listener listener = null;

    public boolean enabled;

    private float dragThreshold;

    private float swipeThreshold;

    private float directionThreshold;

    public CellSelector(DungeonTilemap map) {
        super(map);
        camera = map.camera();

        dragThreshold = PixelScene.defaultZoom * DungeonTilemap.SIZE / 2;

        swipeThreshold = PixelScene.defaultZoom * DungeonTilemap.SIZE * 2;

        directionThreshold = PixelScene.defaultZoom * DungeonTilemap.SIZE/ 2;

        mouseZoom = camera.zoom;
        KeyEvent.addKeyListener(keyListener);
    }

    private float mouseZoom;

    @Override
    protected void onScroll(ScrollEvent event) {


        float diff = event.amount / 10f;

        //scale zoom difference so zooming is consistent
        diff /= ((camera.zoom + 1) / camera.zoom) - 1;
        diff = Math.min(1, diff);
        mouseZoom = GameMath.gate(PixelScene.minZoom, mouseZoom - diff, PixelScene.maxZoom);

        zoom(Math.round(mouseZoom));

    }


    private boolean hasClicked = false;
    private boolean hasClickedTwice = false;

    protected void onDoubleClick() {
        System.out.println("DOUBLE CLICK");
        StateReader.speechEventHandler.setMsg(StateReader.speechEventStop);
        if (CommandMapper.lastCommand != null) {
            CommandMapper.mapCommand(CommandMapper.lastCommand);
        }else{
            StateReader.speechEventHandler.setMsg("No command to repeat, single click to speak a new command");
        }
    }

    protected void onSingleClick() {


        System.out.println("SINGLE CLICK");
        if (!StateReader.busy) {
            StateReader.speechEventHandler.setMsg(StateReader.speechEventStop);
            StateReader.speechRecognitionHandler.dispatchListenEvent();
            StateReader.busy = true;
        }else{
            StateReader.speechRecognitionHandler.dispatchKillEvent();
            StateReader.busy= false;
        }
    }

    @Override
    protected void onClick(PointerEvent event) {
        if (ShatteredPixelDungeon.isAccessibilityMode) {
            if (dragging) {

                dragging = false;

            }else {
                if (hasClicked) {
                    hasClickedTwice = true;
                } else {
                    hasClicked = true;
                }
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {

                                if (hasClicked && !hasClickedTwice) {
                                    onSingleClick();
                                    hasClicked = false;
                                    hasClickedTwice = false;
                                    return;
                                }

                                if (hasClicked && hasClickedTwice) {

                                    onDoubleClick();
                                    hasClicked = false;
                                    hasClickedTwice = false;
                                    return;
                                }
                            }
                        },
                        250
                );


            }

        } else {
            if (dragging) {

                dragging = false;

            } else {

                PointF p = Camera.main.screenToCamera((int) event.current.x, (int) event.current.y);

                //Prioritizes a sprite if it and a tile overlap, so long as that sprite isn't more than 4 pixels into another tile.
                //The extra check prevents large sprites from blocking the player from clicking adjacent tiles

                //hero first
                if (Dungeon.hero.sprite != null && Dungeon.hero.sprite.overlapsPoint(p.x, p.y)) {
                    PointF c = DungeonTilemap.tileCenterToWorld(Dungeon.hero.pos);
                    if (Math.abs(p.x - c.x) <= 12 && Math.abs(p.y - c.y) <= 12) {
                        select(Dungeon.hero.pos);
                        return;
                    }
                }

                //then mobs
                for (Char mob : Dungeon.level.mobs.toArray(new Mob[0])) {
                    if (mob.sprite != null && mob.sprite.overlapsPoint(p.x, p.y)) {
                        PointF c = DungeonTilemap.tileCenterToWorld(mob.pos);
                        if (Math.abs(p.x - c.x) <= 12 && Math.abs(p.y - c.y) <= 12) {
                            select(mob.pos);
                            return;
                        }
                    }
                }

                //then heaps
                for (Heap heap : Dungeon.level.heaps.valueList()) {
                    if (heap.sprite != null && heap.sprite.overlapsPoint(p.x, p.y)) {
                        PointF c = DungeonTilemap.tileCenterToWorld(heap.pos);
                        if (Math.abs(p.x - c.x) <= 12 && Math.abs(p.y - c.y) <= 12) {
                            select(heap.pos);
                            return;
                        }
                    }
                }

                select(((DungeonTilemap) target).screenToTile(
                        (int) event.current.x,
                        (int) event.current.y,
                        true));
            }
        }
    }

    private float zoom(float value) {

        value = GameMath.gate(PixelScene.minZoom, value, PixelScene.maxZoom);
        SPDSettings.zoom((int) (value - PixelScene.defaultZoom));
        camera.zoom(value);

        //Resets character sprite positions with the new camera zoom
        //This is important as characters are centered on a 16x16 tile, but may have any sprite size
        //This can lead to none-whole coordinate, which need to be aligned with the zoom
        for (Char c : Actor.chars()) {
            if (c.sprite != null && !c.sprite.isMoving) {
                c.sprite.point(c.sprite.worldToCamera(c.pos));
            }
        }

        return value;
    }

    public void select(int cell) {
        if (enabled && Dungeon.hero.ready && !GameScene.isShowingWindow()
                && listener != null && cell != -1) {

            listener.onSelect(cell);
            GameScene.ready();

        } else {

            GameScene.cancel();

        }
    }

    private boolean pinching = false;
    private PointerEvent another;
    private float startZoom;
    private float startSpan;

    @Override
    protected void onPointerDown(PointerEvent event) {

        if (event != curEvent && another == null) {

            if (!curEvent.down) {
                curEvent = event;
                onPointerDown(event);
                return;
            }

            pinching = true;

            another = event;
            startSpan = PointF.distance(curEvent.current, another.current);
            startZoom = camera.zoom;

            dragging = false;
        } else if (event != curEvent) {
            reset();
        }
    }

    @Override
    protected void onPointerUp(PointerEvent event) {
        if (justDragged){
            justDragged = false;
        }
        if (pinching && (event == curEvent || event == another)) {

            pinching = false;

            zoom(Math.round(camera.zoom));

            dragging = true;
            if (event == curEvent) {
                curEvent = another;
            }
            another = null;
            lastPos.set(curEvent.current);
        }
    }

    private boolean dragging = false;
    private PointF lastPos = new PointF();
    private PointF startPos = new PointF();
    private boolean justDragged = false;

    private String determineSwipeDirection(PointF start, PointF end){


        float yDiff = Math.abs(end.y - start.y);
        float xDiff = Math.abs(end.x-start.x);

            if (xDiff >= swipeThreshold && yDiff < swipeThreshold){

                if (start.x < end.x){
                    return "East ";
                }else{
                    return "West ";
                }


            }else if(xDiff < swipeThreshold && yDiff >= swipeThreshold){

                if (start.y > end.y){
                    return "north ";
                }else{
                    return "south ";
                }

            }
            else if(xDiff >= swipeThreshold/2 && yDiff >= swipeThreshold/2){

                if (start.y < end.y && start.x < end.x){
                    return "southeast ";
                }else if (start.y < end.y && start.x > end.x){
                    return "southwest ";
                }if (start.y > end.y && start.x < end.x){
                    return "northeast ";
                }if (start.y > end.y && start.x > end.x){
                    return "northwest ";
                }
            }

        return null;

    }
    protected void onSwipe(){
        System.out.println("SWIPE");

        String direction = determineSwipeDirection(startPos,lastPos);

        if(direction != null){
            CommandMapper.mapCommand(direction);
        }


    }

    @Override
    protected void onDrag(PointerEvent event) {

        if (ShatteredPixelDungeon.isAccessibilityMode){
            //remove drag functionality if accessibility mode - replace with swipe to move

            if (!dragging && PointF.distance(event.current, event.start) > dragThreshold) {

                dragging = true;
                lastPos.set(event.current);
                startPos.set(event.current);

            } else if (dragging) {

                //System.out.println(PointF.diff(lastPos,event.current));

                lastPos.set(event.current);
                if (PointF.distance(startPos, lastPos) > swipeThreshold && !justDragged){

                    onSwipe();
                    justDragged = true;

                    return;

                }

            }


        }else {

            if (pinching) {

                float curSpan = PointF.distance(curEvent.current, another.current);
                float zoom = (startZoom * curSpan / startSpan);
                camera.zoom(GameMath.gate(
                        PixelScene.minZoom,
                        zoom - (zoom % 0.1f),
                        PixelScene.maxZoom));

            } else {

                if (!dragging && PointF.distance(event.current, event.start) > dragThreshold) {

                    dragging = true;
                    lastPos.set(event.current);

                } else if (dragging) {
                    camera.shift(PointF.diff(lastPos, event.current).invScale(camera.zoom));
                    lastPos.set(event.current);
                }
            }
        }
    }


    private GameAction heldAction = SPDAction.NONE;
    private int heldTurns = 0;

    private Signal.Listener<KeyEvent> keyListener = new Signal.Listener<KeyEvent>() {
        @Override
        public boolean onSignal(KeyEvent event) {
            GameAction action = KeyBindings.getActionForKey(event);
            if (!event.pressed) {

                if (heldAction != SPDAction.NONE && heldAction == action) {
                    resetKeyHold();
                    return true;
                } else {
                    if (action == SPDAction.ZOOM_IN) {
                        zoom(camera.zoom + 1);
                        mouseZoom = camera.zoom;
                        return true;

                    } else if (action == SPDAction.ZOOM_OUT) {
                        zoom(camera.zoom - 1);
                        mouseZoom = camera.zoom;
                        return true;
                    }
                }
            } else if (moveFromAction(action)) {
                heldAction = action;
                return true;
            }

            return false;
        }
    };

    public boolean moveFromAction(GameAction action) {
        if (Dungeon.hero == null) {
            return false;
        }

        int cell = Dungeon.hero.pos;

        if (action == SPDAction.N) cell += -Dungeon.level.width();
        if (action == SPDAction.NE) cell += +1 - Dungeon.level.width();
        if (action == SPDAction.E) cell += +1;
        if (action == SPDAction.SE) cell += +1 + Dungeon.level.width();
        if (action == SPDAction.S) cell += +Dungeon.level.width();
        if (action == SPDAction.SW) cell += -1 + Dungeon.level.width();
        if (action == SPDAction.W) cell += -1;
        if (action == SPDAction.NW) cell += -1 - Dungeon.level.width();

        if (cell != Dungeon.hero.pos) {
            //each step when keyboard moving takes 0.15s, 0.125s, 0.1s, 0.1s, ...
            // this is to make it easier to move 1 or 2 steps without overshooting
            CharSprite.setMoveInterval(CharSprite.DEFAULT_MOVE_INTERVAL +
                    Math.max(0, 0.05f - heldTurns * 0.025f));
            select(cell);
            return true;

        } else {
            return false;
        }

    }

    public void processKeyHold() {
        if (heldAction != SPDAction.NONE) {
            enabled = Dungeon.hero.ready = true;
            Dungeon.observe();
            heldTurns++;
            moveFromAction(heldAction);
        }
    }

    public void resetKeyHold() {
        heldAction = SPDAction.NONE;
        heldTurns = 0;
        CharSprite.setMoveInterval(CharSprite.DEFAULT_MOVE_INTERVAL);
    }

    public void cancel() {

        if (listener != null) {
            listener.onSelect(null);
        }

        GameScene.ready();
    }

    @Override
    public void reset() {
        super.reset();
        another = null;
        if (pinching) {
            pinching = false;

            zoom(Math.round(camera.zoom));
        }
    }

    public void enable(boolean value) {
        if (enabled != value) {
            enabled = value;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        KeyEvent.removeKeyListener(keyListener);
    }

    public interface Listener {
        void onSelect(Integer cell);

        String prompt();
    }
}
