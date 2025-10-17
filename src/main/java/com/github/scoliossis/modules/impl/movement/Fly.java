package com.github.scoliossis.modules.impl.movement;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.MoveFlyingEvent;
import com.github.scoliossis.modules.*;
import com.github.scoliossis.modules.impl.render.Freecam;
import com.github.scoliossis.utils.client.C;
import org.lwjgl.input.Keyboard;

@RegisterModule(
        name = "Fly",
        description = "aeroplane hake",
        category = Category.MOVEMENT,
        dangerous = true
)
public class Fly extends Module {
    @RegisterSubModule(name = "Horizontal Speed", max = 10)
    public static float horizontalSpeed = 2;
    @RegisterSubModule(name = "Vertical Speed", max = 10)
    public static float verticalSpeed = 2;

    @SubscribeEvent
    public static void onMoveFlying(MoveFlyingEvent event) {
        C.p().setVelocity(0,0,0);

        if (ModuleManager.isEnabled(Freecam.class) || C.mc.currentScreen != null) return;

        event.friction = horizontalSpeed;
        C.p().motionY +=
                (Keyboard.isKeyDown(C.mc.gameSettings.keyBindJump.getKeyCode()) ? verticalSpeed : 0) +
                        (Keyboard.isKeyDown(C.mc.gameSettings.keyBindSneak.getKeyCode()) ? -verticalSpeed : 0);
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
