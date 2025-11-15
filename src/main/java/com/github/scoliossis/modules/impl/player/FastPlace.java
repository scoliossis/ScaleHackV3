package com.github.scoliossis.modules.impl.player;

import com.github.scoliossis.modules.*;

@RegisterModule(
        name = "Fast Place",
        description = "todo: add description",
        category = Category.PLAYER
)
public class FastPlace extends Module {
    @RegisterSubModule(name = "Place Delay", max = 5)
    public static int placeDelay = 1;

    public static int getPlaceDelay() {
        return ModuleManager.isEnabled(FastPlace.class) ? placeDelay : 4;
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
