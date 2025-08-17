package com.github.scoliossis.mixins.net.minecraft.client.gui;

import com.github.scoliossis.modules.impl.client.ThemeModule;
import com.github.scoliossis.modules.impl.render.NickHider;
import com.github.scoliossis.utils.ChatUtil;
import com.github.scoliossis.utils.FontUtil;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

// fun fact: minecraft feeds the setColor(r,g,b,a) function r,b,g,a. this is a WILD typo and happened 3 TIMES??
@Mixin(FontRenderer.class)
public abstract class FontRendererMixin {
    @Shadow public abstract int getStringWidth(final String p0);

    @Unique private boolean FontRenderer$shouldUseCustomFont() {
        return ThemeModule.globalFont;
    }

    @Shadow public abstract int drawString(String text, int x, int y, int color);

    @Shadow public abstract int drawString(String text, float x, float y, int color, boolean dropShadow);

    @Inject(method = "drawString(Ljava/lang/String;FFIZ)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;resetStyles()V", shift = At.Shift.AFTER), cancellable = true)
    public void onDrawString(String text, float x, float y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> cir) {
        // fix text to hide nick
        String text2 = NickHider.fixText(text);

        if (text2.isEmpty()) {
            cir.setReturnValue(0);
            return;
        }

        if (FontRenderer$shouldUseCustomFont()) {
            if ((color & -67108864) == 0) {
                color |= -16777216;
            }

            // minecraft uses red, blue, green colouring btw.
            Color colour = new Color((float)(color >> 16 & 255) / 255.0F,
                    (float)(color >> 8 & 255) / 255.0F,
                    (float)(color & 255) / 255.0F
                    //(float)(color >> 24 & 255) / 255.0F
            );

            FontUtil.drawString(text2, x, y - (ThemeModule.minecraftFontSize - 7), ThemeModule.minecraftFontSize, colour, dropShadow);
            cir.setReturnValue((int) (x + FontUtil.getStringWidth(text2, ThemeModule.minecraftFontSize)));
        }
        else if (!text.equals(text2)) {
            cir.setReturnValue(this.drawString(text2, x, y, color, dropShadow));
        }
    }

    @Inject(method = "getStringWidth", at = @At("RETURN"), cancellable = true)
    private void impl$getStringWidth(String text, CallbackInfoReturnable<Integer> cir) {
        String text2 = NickHider.fixText(text);

        if (FontRenderer$shouldUseCustomFont()) cir.setReturnValue(FontUtil.getStringWidth(text2, ThemeModule.minecraftFontSize));
        else if (!text.equals(text2)) cir.setReturnValue(this.getStringWidth(text2));
    }
}