package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.HeroSelectScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.StartScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ShamanSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.state_management.StateReader;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBlacksmith;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSettings;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTabbed;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.Scene;
import com.watabou.noosa.ui.Button;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class AccessibleInterface extends StyledButton {
    final Chrome.Type GREY_TR = Chrome.Type.GREY_BUTTON_TR;
    public ArrayList<Button> menuButtons = new ArrayList<>();
    Integer ButtonIndex = null;
    String helpMessage;

    public void add(Button button) {
        menuButtons.add(button);
    }

    public void readName() {

        StateReader.speechEventHandler.setMsg(this.helpMessage);
    }

    public void next() {
        if (ButtonIndex != null) {

            ButtonIndex += 1;

            if (ButtonIndex == menuButtons.size()) {

                ButtonIndex = 0;

            }

        } else {
            ButtonIndex = 0;
        }

        if (menuButtons.get(ButtonIndex) == null) {
            next();
            return;
        }

        Button currentMenuButton = menuButtons.get(ButtonIndex);

        if (currentMenuButton instanceof HeroSelectScene.HeroBtn) {
            StateReader.speechEventHandler.setMsg("Select: " + ((HeroSelectScene.HeroBtn) menuButtons.get(ButtonIndex)).getCl().name());
        } else if (currentMenuButton instanceof StyledButton) {
            StateReader.speechEventHandler.setMsg(((StyledButton) currentMenuButton).text());
        } else if (currentMenuButton instanceof StartScene.SaveSlotButton) {
            BitmapText depth = (((StartScene.SaveSlotButton) currentMenuButton).getDepth());
            if (depth != null) {
                StateReader.speechEventHandler.setMsg("Class: " + ((StartScene.SaveSlotButton) currentMenuButton).getName().text);
                StateReader.speechEventHandler.setMsg("Dungeon Depth: " + ((StartScene.SaveSlotButton) currentMenuButton).getDepth().text());
            } else {
                StateReader.speechEventHandler.setMsg(((StartScene.SaveSlotButton) currentMenuButton).getName().text());
            }
        } else if (currentMenuButton instanceof ExitButton) {
            StateReader.speechEventHandler.setMsg("Exit");
        } else if (currentMenuButton instanceof IconButton) {
            StateReader.speechEventHandler.setMsg(((IconButton) currentMenuButton).getName());
        } else if (currentMenuButton instanceof RedButton) {
            StateReader.speechEventHandler.setMsg(((RedButton) currentMenuButton).text());
        } else if (currentMenuButton instanceof BadgesGrid.BadgeButton) {
            StateReader.speechEventHandler.setMsg(((BadgesGrid.BadgeButton) currentMenuButton).getBadge().desc());
            if (((BadgesGrid.BadgeButton) currentMenuButton).getStatus() == true) {
                StateReader.speechEventHandler.setMsg("Unlocked");
            } else {
                StateReader.speechEventHandler.setMsg("Locked");
            }
        } else if (currentMenuButton instanceof WndBag.ItemButton) {

            if (((WndBag.ItemButton) currentMenuButton).getItem() == null || ((WndBag.ItemButton) currentMenuButton).getItem().name() == null ) {
                next();
                return;
            } else {
                StateReader.speechEventHandler.setMsg(((WndBag.ItemButton) currentMenuButton).getItem().name());
            }
        }else if (currentMenuButton instanceof TalentButton){

            StateReader.speechEventHandler.setMsg(((TalentButton)currentMenuButton).talent.name().replace("_"," "));

        }
    }


    public void create(Group container) {

        if (container instanceof Window) {
            Window containingWindow = (Window)container;
            containingWindow.width = PixelScene.uiCamera.width;
            containingWindow.height = PixelScene.uiCamera.height;
            containingWindow.resize(containingWindow.width, containingWindow.height);
            containingWindow.visible = false;
        }

        else if (container instanceof Component){

            Component containingComponent = (Component)container;
            //containingComponent = containingComponent.setRect(0,0,PixelScene.uiCamera.width,PixelScene.uiCamera.height);
            //containingComponent.visible = false;
        }

        this.replaceInterface();
        this.setRect(0, 0, PixelScene.uiCamera.width, PixelScene.uiCamera.height);
        container.add(this);
        this.readName();

    }

    public AccessibleInterface(Chrome.Type type, String label, String SceneName) {
        super(type, label);
        this.helpMessage = SceneName;
    }

    public void replaceInterface() {
        for (Button button : this.menuButtons) {
            button.visible = false;
        }
    }

    boolean hasClicked = false;
    boolean hasClickedTwice = false;
    boolean hasClickedThrice = false;

    @Override
    protected void onClick() {
        super.onClick();

        StateReader.speechEventHandler.setMsg(StateReader.speechEventStop);
        if (StateReader.busy) {
            StateReader.speechRecognitionHandler.dispatchKillEvent();
            StateReader.busy = false;
        }

        if (hasClicked) {
            if (hasClickedTwice) {
                hasClickedThrice = true;
            }
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
                            hasClickedThrice = false;
                            return;
                        }

                        if (hasClicked && hasClickedTwice && !hasClickedThrice) {

                            onDoubleClick();
                            hasClicked = false;
                            hasClickedTwice = false;
                            hasClickedThrice = false;
                            return;
                        }
                        if (hasClicked && hasClickedTwice && hasClickedThrice) {
                            onTripleClick();
                            hasClicked = false;
                            hasClickedTwice = false;
                            hasClickedThrice = false;
                        }

                    }
                },
                350
        );

    }

    void onSingleClick() {

        System.out.println("SINGLE ClICK");
        StateReader.speechEventHandler.setMsg(StateReader.speechEventStop);
        next();
    }

    void onDoubleClick() {
        System.out.println("DOUBLE ClICK");
        if (ButtonIndex != null && menuButtons != null && menuButtons.size() > 0)
            menuButtons.get(ButtonIndex).activate();
        else
            StateReader.speechEventHandler.setMsg("No Button Selected.  Click to Scroll Through Buttons. Double Click to Activate");
    }

    void onTripleClick() {
        System.out.println("TRIPLE ClICK");
        for (Button button : menuButtons) {
            if (button instanceof ExitButton) {
                StateReader.speechEventHandler.setMsg("back");
                ((ExitButton) button).onClick();
                this.destroy();
                return;
            }
        }

        if (this.parent instanceof Window) {
            StateReader.speechEventHandler.setMsg("back");

            Window containingWindow = ((Window) this.parent);

            if (containingWindow instanceof WndUseItem) {
                ((WndUseItem) containingWindow).getOwner().onBackPressed();
            }

            containingWindow.onBackPressed();
            return;
        }

        StateReader.speechEventHandler.setMsg("Cannot go back");
    }

    public void suppressButton(Button button) {

        button.visible = false;

    }

}
