package com.github.scoliossis.modules.impl.client;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.PlayerUpdateEvent;
import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;
import com.github.scoliossis.utils.PlayerUtil;
import com.github.scoliossis.utils.RotationUtil;

@RegisterModule(
        name = "Correct GCD",
        description = "Fixes rotation by client always be valid minecraft rotations",
        category = Category.CLIENT,
        enabledByDefault = true
)
public class CorrectGCD extends Module {
    // always goes last
    @SubscribeEvent(priority = 9999)
    public static void onPlayerUpdate(PlayerUpdateEvent event) {
        if (PlayerUtil.prevPlayerUpdateEvent == null) return;

        if (PlayerUtil.currentTickClientRotation != event.rotation) {
            event.rotation = RotationUtil.applyGcd(
                    PlayerUtil.prevPlayerUpdateEvent.rotation,
                    PlayerUtil.playerUpdateEvent.rotation
            );
        }
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
