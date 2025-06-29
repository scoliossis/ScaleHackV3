package com.github.scoliossis.modules.impl.player;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.PlayerUpdateEvent;
import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;
import com.github.scoliossis.utils.MathUtil;
import com.github.scoliossis.utils.PlayerUtil;
import com.github.scoliossis.utils.RotationUtil;

@RegisterModule(
        name = "Anti Aim",
        description = "poopware antiaim bypahh faceit 2026",
        category = Category.PLAYER,
        dangerous = true
)
public class AntiAim extends Module {
    @SubscribeEvent
    public static void onPlayerUpdate(PlayerUpdateEvent event) {
        if (PlayerUtil.prevPlayerUpdateEvent == null) return;

        event.rotation = new RotationUtil.Rotation(
                MathUtil.getRandomInRange(-90, 90),
                PlayerUtil.prevPlayerUpdateEvent.rotation.yaw + MathUtil.getRandomInRange(-180, 180)
        );
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
