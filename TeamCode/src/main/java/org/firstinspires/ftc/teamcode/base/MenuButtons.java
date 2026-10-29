package org.firstinspires.ftc.teamcode.base;

import com.qualcomm.robotcore.hardware.Gamepad;

/**
 * Turns each new press of a gamepad's D-pad, A and B into one move of an
 * {@link OptionMenu}.
 *
 * <p>Uses {@link OptionMenu}, and the SDK's {@link Gamepad}.
 */
class MenuButtons {

    private OptionMenu optionMenu;

    MenuButtons(OptionMenu optionMenu) {
        this.optionMenu = optionMenu;
    }

    /** Reads the presses since the last call, and moves the menu for each. */
    void loop(Gamepad gamepad) {
        if (gamepad.dpadUpWasPressed()) {
            optionMenu.goUp();
        }
        if (gamepad.dpadDownWasPressed()) {
            optionMenu.goDown();
        }
        if (gamepad.dpadLeftWasPressed()) {
            optionMenu.goLeft();
        }
        if (gamepad.dpadRightWasPressed()) {
            optionMenu.goRight();
        }
        if (gamepad.aWasPressed()) {
            optionMenu.select();
        }
        if (gamepad.bWasPressed()) {
            optionMenu.goBack();
        }
    }
}
