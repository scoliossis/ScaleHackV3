package com.github.scoliossis.modules.impl.render;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.KeyPressedEvent;
import com.github.scoliossis.modules.*;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.PlayerUtil;
import com.github.scoliossis.utils.RotationUtil;

@RegisterModule(
        name = "Freelook",
        description = "Yeah, I'm out that Brooklyn, now I'm down in Tribeca Right next to De Niro, but I'll be hood forever",
        category = Category.RENDER
)
public class Freelook extends Module {
    @RegisterSubModule(name = "Disable On Key Release")
    public static boolean disableOnKeyRelease = true;

    @SubscribeEvent
    public static void onKeyRelease(KeyPressedEvent event) {
        if (disableOnKeyRelease && !event.pressed) {
            Module thiz = ModuleManager.getModule(Freelook.class);
            if (event.keyCode == thiz.getKeybind()) {
                thiz.setEnabled(false);
            }
        }
    }

    private int lastPerspective = 0;

    @Override
    protected void onEnable() {
        if (!C.isInGame() || ModuleManager.isEnabled(Freecam.class)) {
            this.toggle();
            return;
        }

        PlayerUtil.fakeRotation = PlayerUtil.realRotation = RotationUtil.getCurrentClientRotation();
        lastPerspective = C.mc.gameSettings.thirdPersonView;
        C.mc.gameSettings.thirdPersonView = 1;
    }

    @Override
    protected void onDisable() {
        if (!C.isInGame() || ModuleManager.isEnabled(Freecam.class)) return;

        if (C.mc.gameSettings.thirdPersonView == 1)
            C.mc.gameSettings.thirdPersonView = lastPerspective;

        // reset fake camera
        PlayerUtil.fakeRotation = null;
    }
}
