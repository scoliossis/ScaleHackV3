package com.github.scoliossis.modules.impl.movement;

import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;

@RegisterModule(
        name = "Sneak",
        description = "sneaking shouldnt slow me! im unstopable!",
        category = Category.MOVEMENT,
        dangerous = true
)
public class Sneak extends Module {
    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
