package com.github.scoliossis.modules;

import com.github.scoliossis.utils.EasingUtil;

import java.awt.*;

public enum Category {
    COMBAT(new Color(0xE64D3B)),
    RENDER(new Color(0x3700CF)),
    MOVEMENT(new Color(0x2DCB6E)),
    PLAYER(new Color(0x803D9E)),
    SKYBLOCK(new Color(0x4C9DCC)),
    CLIENT(new Color(0xF29C11));

    Category(Color color) {
        this.color = color;

        this.posX = 0; this.posY = 0;
        this.renderX = 0; this.renderY = 0;
    }

    public boolean shouldShow() {
        return (this.open || EasingUtil.getAnimation(this.name()) != -1) && !ModuleManager.getModulesByCategory(this).isEmpty();
    }

    public final Color color;

    public float posX, posY;
    public float renderX, renderY;

    public float scroll, renderScroll, previousScroll;

    public boolean open = true;
}
