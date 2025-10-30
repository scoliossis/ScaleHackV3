package com.github.scoliossis.utils.render;

import com.github.scoliossis.Main;
import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.ClientTickEvent;
import com.github.scoliossis.events.impl.RenderTickEvent;
import com.github.scoliossis.modules.impl.client.HUD;
import com.github.scoliossis.modules.impl.client.ThemeModule;
import com.github.scoliossis.utils.client.C;
import com.github.scoliossis.utils.minecraft.ChatUtil;
import com.github.scoliossis.utils.minecraft.MovementUtil;
import lombok.AllArgsConstructor;
import lombok.Setter;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class FontUtil {
    @Setter
    private static Fonts currentFont = ThemeModule.font;

    // list by minecraft in FontRenderer.renderChar, removed all glyphs.
    private static final String letters = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵž !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»πμΩ∞±≥≤÷≈°∙·√²\u0000";
    private static final String colourCodes = "0123456789abcdef";

    private static final Graphics2D graphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
    static {
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    // remove unused parts of cache after 1 render tick event
    private static final ConcurrentHashMap<String, int[]> texturesCache = new ConcurrentHashMap<>();

    // java.awt.Font is op !!!!
    // generates a buffered image for a given string / character
    private static int getStringTextureID(String string, Font font) {
        int[] textureValues = texturesCache.get(string + font.getSize());

        if (textureValues == null) {
            Rectangle stringBounds = getStringBounds(string, font.getSize());
            BufferedImage image = new BufferedImage(Math.max(1, graphics.getFontMetrics().getStringBounds(string, graphics).getBounds().width), Math.max(1, stringBounds.height), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();

            // improves it a lot.
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.setFont(font);
            graphics.drawString(string, 0, font.getSize());

            DynamicTexture dynamicTexture = new DynamicTexture(image);
            texturesCache.put(string + font.getSize(), new int[] {dynamicTexture.getGlTextureId(), RenderUtil.ticks});
            return new DynamicTexture(image).getGlTextureId();
        }

        textureValues[1] = RenderUtil.ticks;
        texturesCache.put(string + font.getSize(), textureValues);
        return textureValues[0];
    }

    // delete unneeded textures.
    @SubscribeEvent
    public static void onRenderTickEvent(RenderTickEvent e) {
        for (String textureValues : texturesCache.keySet()) {
            int[] textureValuesArray = texturesCache.get(textureValues);

            if (textureValuesArray[1] + 1 < RenderUtil.ticks) {
                TextureUtil.deleteTexture(textureValuesArray[0]);
                texturesCache.remove(textureValues);
            }
        }
    }

    public static float drawStringFade(String string, double x, double y, float size, Color[] colour, double fadeSpeed, double fadeSpread, boolean dropShadow) {
        if (string.isEmpty()) return 0;

        int stringWidth = 0;

        float scaleFactor = getScaleFactor();
        x = (int) x; y = (int) y; size *= scaleFactor;

        Font font = currentFont.font.deriveFont(size);
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, 0);
        GL11.glScalef(1f/scaleFactor, 1f/scaleFactor, 1);

        boolean obfuscated = false, bold = false, strikethrough = false, underline = false, italic = false;

        int index;
        while ((index = string.indexOf("§")) != -1) {
            if (index + 1 >= string.length()) break;

            String colourlessPart = string.substring(0, index);
            int partWidth = drawStringParts(colourlessPart, font, colour, fadeSpeed, fadeSpread, stringWidth, size, obfuscated, dropShadow, bold, strikethrough, underline, italic);
            stringWidth += partWidth;

            // updating colour codes
            String colourCodeString = string.substring(index + 1, index + 2);
            char colourCode = colourCodeString.charAt(0);
            if (colourCodes.contains(colourCodeString)) {
                Color colourCodeColour = new Color(C.mc.fontRendererObj.getColorCode(colourCode));
                for (int i = 0; i < colour.length; i++) colour[i] = new Color(colourCodeColour.getRed(), colourCodeColour.getGreen(), colourCodeColour.getBlue(), colour[i].getAlpha());
                obfuscated = bold = strikethrough = underline = italic = false;
            }
            else {
                switch (colourCode) {
                    case 'k':
                        obfuscated = true;
                        break;
                    case 'l':
                        bold = true;
                        break;
                    case 'm':
                        strikethrough = true;
                        break;
                    case 'n':
                        underline = true;
                        break;
                    case 'o':
                        italic = true;
                        break;

                    case 'r':
                        for (int i = 0; i < colour.length; i++) colour[i] = new Color(255,255,255,colour[i].getAlpha());
                        obfuscated = bold = strikethrough = underline = italic = false;
                        break;
                }
            }

            // remove already drawn part of string
            string = string.substring(Math.min(string.length(), index + 2));
        }

        stringWidth += drawStringParts(string, font, colour, fadeSpeed, fadeSpread, stringWidth, size, obfuscated, dropShadow, bold, strikethrough, underline, italic);
        GL11.glPopMatrix();

        return stringWidth / scaleFactor;
    }

    public static float drawString(String string, double x, double y, float size, Color colour, boolean dropShadow) {
        return drawStringFade(string, x, y, size, new Color[] {colour}, 0, 0, dropShadow);
    }

    public static Random fontRandom = new Random();
    private static String scramblePart(String part) {
        String scrambled = "";

        for (int i = 0; i < part.length(); i++) {
            // code from minecraft, multimillion dorra company, i assume there isnt a faster way to do this, im not gonna cache charater width
            int k = getStringWidth(part.substring(i, i+1), 5, false);
            String c1;

            while (true)
            {
                int j = fontRandom.nextInt(letters.length());
                c1 = letters.substring(j, j+1);

                if (k == (int) getStringWidth(part.substring(i, i+1), 5, false))
                {
                    break;
                }
            }

            scrambled += c1;
        }

        return scrambled;
    }

    public static void drawCenteredString(String string, double x, double y, float size, Color color, boolean dropShadow) {
        drawString(string, x - getStringWidth(string, size) / 2f, y, size, color, dropShadow);
    }

    private static int drawStringParts(String colourlessPart, Font font, Color[] colour, double fadeSpeed, double fadeSpread, int stringPos, double size, boolean obfuscated, boolean dropShadow, boolean bold, boolean strikethrough, boolean underline, boolean italic) {
        int partsWidth = 0;

        ArrayList<String> parts = new ArrayList<>();
        ArrayList<String> glyphs = new ArrayList<>();

        String nonGlyph = "";

        for (int i = 0; i < colourlessPart.length(); i++) {
            if (isSpecialChar(colourlessPart.charAt(i))) {
                glyphs.add(colourlessPart.substring(i, i + 1));

                parts.add(nonGlyph);
                nonGlyph = "";
            } else nonGlyph += colourlessPart.charAt(i);
        }
        parts.add(nonGlyph);

        int currentPart = 0;

        // drawing the parts
        while (currentPart < parts.size() || currentPart < glyphs.size()) {
            if (currentPart < parts.size()) {
                String part = parts.get(currentPart);
                if (obfuscated) part = scramblePart(part);

                int partWidth = drawStringPart(part, font, colour, fadeSpeed, fadeSpread, stringPos, dropShadow, bold, strikethrough, underline, italic);

                GL11.glTranslated(partWidth, 0, 0);
                partsWidth += partWidth;
            }

            if (currentPart < glyphs.size()) {
                String glyph = glyphs.get(currentPart);
                if (obfuscated) glyph = scramblePart(glyph);

                GL11.glPushMatrix();
                GL11.glScaled(0.1 * size, 0.1 * size, 1);

                Color colourFade = RenderUtil.getColorsFade(stringPos*fadeSpread, colour, fadeSpeed);

                int glyphWidth = (int) (C.mc.fontRendererObj.drawString(glyph, 0, 3, colourFade.getRGB()) * 0.1 * size);

                if (underline) RenderUtil.drawRect(0, 0.1 * size, glyphWidth, 1, colourFade);
                if (strikethrough) RenderUtil.drawRect(0, 0.05 * size, glyphWidth, 1, colourFade);

                GL11.glPopMatrix();

                GL11.glTranslated(glyphWidth, 0, 0);

                partsWidth += glyphWidth;
            }

            currentPart++;
        }

        return partsWidth;
    }

    // fun fact: to draw the shadow, the font colour is set to:
    //  colour = (colour & 16579836) >> 2 | colour & -16777216;
    // for white, this simplifies down to new Color(126, 126, 126)
    // which is close to Color.GRAY!

    private static int drawStringPart(String string, Font font, Color[] colour, double fadeSpeed, double fadeSpread, int stringPos, boolean dropShadow, boolean bold, boolean strikethrough, boolean underline, boolean italic) {
        int fontType = (bold && italic) ? Font.BOLD | Font.ITALIC : bold ? Font.BOLD : italic ? Font.ITALIC : Font.PLAIN;
        Rectangle stringBounds = getStringBounds(string, font.getSize()).getBounds();
        int textureID = getStringTextureID(string, font.deriveFont(fontType));

        Color[] colourFade = RenderUtil.getColorsFade(stringPos*fadeSpread, stringBounds.width*fadeSpread, colour, fadeSpeed);

        if (dropShadow) RenderUtil.drawRectTextured(1, 1, stringBounds.width, stringBounds.height, new Color(22,22,22, colourFade[0].getAlpha()), textureID);
        RenderUtil.drawRectTexturedColor(0, 0, stringBounds.width, stringBounds.height, colourFade[0], colourFade[1], textureID);

        if (underline) RenderUtil.drawGradientLR(0, stringBounds.height * 0.8f, stringBounds.width, 1, colour[0], colour[1]);
        if (strikethrough) RenderUtil.drawGradientLR(0, stringBounds.height / 2f, stringBounds.width, 1, colour[0], colour[1]);

        return stringBounds.width;
    }

    // todo: glyph characters arnt accounted for, doesnt really matter ig
    public static int getStringWidth(String string, float fontSize, boolean stripColorCodes) {
        return getStringBounds(stripColorCodes ? string.replaceAll("[§&].", "") : string, fontSize).width;
    }

    public static int getStringWidth(String string, float fontSize) {
        return getStringWidth(string, fontSize, true);
    }
    public static int getFontHeight(float fontSize) {
        return getStringBounds("", fontSize).height;
    }

    public static Rectangle getStringBounds(String string, float fontSize) {
        graphics.setFont(currentFont.font.deriveFont(fontSize));
        return graphics.getFontMetrics().getStringBounds(string, graphics).getBounds();
    }

    public static boolean isSpecialChar(char character) {
        return !letters.contains(String.valueOf(character));
    }

    private static float getScaleFactor() {
        if (RenderUtil.renderSide != RenderUtil.RenderSide.World) {
            float[] scaleFactor = RenderUtil.getCurrentTranslation();

            return Math.max(scaleFactor[3], scaleFactor[4]) * C.res().getScaleFactor();
        }

        return 1;
    }

    public enum Fonts {
        Atkinson("atkinson.ttf"),
        Product_Sans("productsans.ttf"),
        Comic_Sans("comicsans.ttf"),
        Segoe_Print("segoeprint.ttf"),
        Jetbrains_Mono("JetbrainsMono.ttf"),
        Love_Days("lovedays.ttf"),
        Thug_Font("thugfont.otf");

        public final Font font;
        public final Font bold;
        public final Font italic;

        Fonts(String name) {
            Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 0);
            Font bold = new Font(Font.SANS_SERIF, Font.BOLD, 0);
            Font italic = new Font(Font.SANS_SERIF, Font.ITALIC, 0);
            try {
                font =  Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Main.class.getResourceAsStream("/fonts/" + name)));
                bold = font.deriveFont(Font.BOLD);
                italic = font.deriveFont(Font.ITALIC);
            } catch (Exception e) {
                System.err.println("Failed to load font: " + e.getMessage());
                e.printStackTrace();
            }

            this.font = font;
            this.bold = bold;
            this.italic = italic;
        }
    }

}