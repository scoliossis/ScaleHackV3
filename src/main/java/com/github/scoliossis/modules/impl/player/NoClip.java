package com.github.scoliossis.modules.impl.player;

import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;
import com.github.scoliossis.utils.PlayerUtil;

@RegisterModule(
        name = "No Clip",
        description = "im just a spooky ghost i swr",
        category = Category.PLAYER
)
public class NoClip extends Module {
    @Override
    protected void onEnable() {
        PlayerUtil.noClip = true;
        PlayerUtil.noClipRender = true;
    }

    @Override
    protected void onDisable() {
        PlayerUtil.noClip = false;
        PlayerUtil.noClipRender = false;
    }
}
