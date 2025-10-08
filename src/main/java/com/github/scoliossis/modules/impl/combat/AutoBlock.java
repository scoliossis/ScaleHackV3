package com.github.scoliossis.modules.impl.combat;

import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;
import com.github.scoliossis.modules.RegisterSubModule;

// shoutout to litdab. for the idea to make autoblock a separate module
// a really old mushroom build ~2020 did the same, but badly, its a cool idea i think
// todo: impl
@RegisterModule(
        name = "AutoBlock",
        description = "im enderflame and you are BLOCKED",
        category = Category.COMBAT,
        dangerous = true
)
public class AutoBlock extends Module {
    @RegisterSubModule(name = "Autoblock Mode", description = "bypahh")
    public AutoBlockMode autoblockMode = AutoBlockMode.Blink;

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    public enum AutoBlockMode {
        Vanilla, Blink, Fake
    }
}
