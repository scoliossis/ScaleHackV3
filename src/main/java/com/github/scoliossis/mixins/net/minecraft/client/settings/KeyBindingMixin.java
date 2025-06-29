package com.github.scoliossis.mixins.net.minecraft.client.settings;

import com.github.scoliossis.bridge.net.minecraft.client.settings.KeyBindingBridge;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyBinding.class)
public class KeyBindingMixin implements KeyBindingBridge {
    @Shadow private boolean pressed;

    @Override
    public void bridge$setPressed(boolean pressed) {
        this.pressed = pressed;
    }
}
