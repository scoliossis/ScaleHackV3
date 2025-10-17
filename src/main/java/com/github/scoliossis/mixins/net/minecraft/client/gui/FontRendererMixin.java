package com.github.scoliossis.mixins.net.minecraft.client.gui;

import com.github.scoliossis.modules.impl.client.ThemeModule;
import com.github.scoliossis.modules.impl.render.NickHider;
import com.github.scoliossis.utils.render.FontUtil;
import com.github.scoliossis.utils.render.RenderUtil;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

// fun fact: minecraft feeds the setColor(r,g,b,a) function r,b,g,a in renderString
// its misnamed, this.blue = (float)(p_renderString_4_ >> 8 & 255) / 255.0F; which is the green part.

@Mixin(FontRenderer.class)
public abstract class FontRendererMixin {
    @Shadow public abstract int getStringWidth(final String p0);

    @Unique private boolean FontRenderer$shouldUseCustomFont() {
        return ThemeModule.globalFont && RenderUtil.renderSide != RenderUtil.RenderSide.World;
    }

    @Shadow public abstract int drawString(String text, int x, int y, int color);

    @Shadow public abstract int drawString(String text, float x, float y, int color, boolean dropShadow);

    @Shadow
    private boolean bidiFlag;

    @Shadow
    protected abstract String bidiReorder(String p_bidiReorder_1_);

    // todo: 3d text rendering is wrong, it isnt culled, you can see signs through walls.
    @Inject(method = "drawString(Ljava/lang/String;FFIZ)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;resetStyles()V", shift = At.Shift.AFTER), cancellable = true)
    public void onDrawString(String text, float x, float y, int colourInt, boolean dropShadow, CallbackInfoReturnable<Integer> cir) {
        if (text == null || text.isEmpty()) return;

        // special chars are always drawn by the default font renderer
        if (text.length() == 1 && FontUtil.isSpecialChar(text.charAt(0))) return;

        // fix text to hide disgusting words.
        String text2 = NickHider.fixText(text);

        if (text2.isEmpty()) {
            cir.setReturnValue(0);
            return;
        }

        if (FontRenderer$shouldUseCustomFont()) {
            if (this.bidiFlag) {
                text2 = this.bidiReorder(text);
            }

            if ((colourInt & -67108864) == 0) {
                colourInt |= -16777216;
            }

            // i dont know why the scoreboard always has the colour 20FFFFFF, which is transparent.
            Color colour = new Color(colourInt, colourInt != 0x20FFFFFF);

            FontUtil.drawString(text2, x, y - (ThemeModule.minecraftFontSize - 7), ThemeModule.minecraftFontSize, colour, dropShadow);
            cir.setReturnValue((int) (x + FontUtil.getStringWidth(text2, ThemeModule.minecraftFontSize)) + (dropShadow ? 1 : 0));
        }
        else if (!text.equals(text2)) {
            cir.setReturnValue(this.drawString(text2, x, y, colourInt, dropShadow));
        }
    }

    @Inject(method = "getStringWidth", at = @At("RETURN"), cancellable = true)
    private void impl$getStringWidth(String text, CallbackInfoReturnable<Integer> cir) {
        String text2 = NickHider.fixText(text);

        if (FontRenderer$shouldUseCustomFont()) cir.setReturnValue(FontUtil.getStringWidth(text2, ThemeModule.minecraftFontSize));
        else if (!text.equals(text2)) cir.setReturnValue(this.getStringWidth(text2));
    }
}