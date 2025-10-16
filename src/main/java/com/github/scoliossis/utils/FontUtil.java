package com.github.scoliossis.utils;

import com.github.scoliossis.Main;
import lombok.AllArgsConstructor;
import lombok.Setter;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

// open to pull requests <3, oldish code! main issue is its unoptimized!
public class FontUtil {
    @Setter
    private static String fontName = "atkinson.ttf";
    // huge
    private static final HashMap<Integer, HashMap<Character, Texture>> fonts = new HashMap<>();

    private static Font currentFont = null;

    // list by minecraft in FontRenderer.renderChar, removed all glyphs.
    private static final String letters = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵž !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»πμΩ∞±≥≤÷≈°∙·√²\u0000";

    private static Font getCustomFont(int fontSize) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Main.class.getResourceAsStream("/fonts/" + fontName))).deriveFont((float) fontSize);
        } catch (Exception e) {
            System.err.println("Failed to load font: " + e.getMessage());
            e.printStackTrace();
        }

        // fail :(
        return new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);
    }

    public static void unloadFont() {
        fonts.clear();
    }

    public static void loadFont(int fontSize) {
        Font font = getCustomFont(fontSize);
        currentFont = font;

        HashMap<Character, Texture> textureMap = new HashMap<>();

        for (int i = 0; i < letters.length(); i++) {
            char character = letters.charAt(i);

            BufferedImage image = generateStringImage(String.valueOf(character), font);
            DynamicTexture texture = new DynamicTexture(image);

            textureMap.put(character, new Texture(image.getWidth(), image.getHeight(), texture));
        }

        fonts.put(font.getSize(), textureMap);
    }

    // java.awt.Font is op !!!!
    // generates a buffered image for a given string / character
    public static BufferedImage generateStringImage(String string, Font font) {
        graphics.setFont(font);
        //graphics.getFontMetrics().getStringBounds(string, graphics);
        int characterWidth = Math.max(graphics.getFontMetrics().stringWidth(string), 1);
        int characterHeight = Math.max((int) getFontHeight(font.getSize()), 1);
        BufferedImage image = new BufferedImage(characterWidth, characterHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();

        // improves it a lot.
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setFont(font);
        graphics.drawString(string, 0, font.getSize());

        return image;
    }

    @AllArgsConstructor
    private static class Texture {
        public int width, height;
        public DynamicTexture dynamicTexture;
    }

    public static void drawCenteredString(String string, float x, float y, int size, Color colour, boolean shadow) {
        drawString(string, x-(getStringWidth(string, size)/2f), y, size, colour, shadow);
    }

    public static Random fontRandom = new Random();

    public static void drawString(String string, float x, float y, int size, Color colour1, boolean shadow) {
        drawString(string, x, y, size, new Color[] {colour1,colour1}, 3f, 1f, shadow);
    }

    // todo: according to profiler, the issue is the finishRendering() function, mainly that it calls getTessalator().draw()
    //  so to optimize this i will somehow need to draw each character in order at the end of a frame? because i believe you cant just change texture mid render.
    public static void drawString(String string, float x, float y, int size, Color[] colours, double fadeSpeed, double fadeSpread, boolean shadow) {
        GL11.glPushMatrix();

        Color[] originalColours = colours;

        int scaleFactor = C.res().getScaleFactor();
        GL11.glScaled(1d / scaleFactor, 1d / scaleFactor, 1);

        // weird artifacts appear at non rounded values, sowwy
        x = (int) (x * scaleFactor);
        y = (int) (y * scaleFactor);
        size *= scaleFactor;

        float boldOffset = 0.5f * scaleFactor;

        HashMap<Character, Texture> textures = getFont(size);

        boolean colorCode = false;

        boolean obfuscated = false;
        boolean underline = false;
        boolean bold = false;
        boolean strikethrough = false;

        boolean rainbow = false;

        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);

            if (colorCode) {
                colorCode = false;

                String colourCodes = "0123456789abcdef";
                if (colourCodes.indexOf(character) != -1) {
                    obfuscated = false;
                    underline = false;
                    bold = false;
                    strikethrough = false;

                    for (int j = 0; j < colours.length; j++) {
                        colours[j] = RenderUtil.setOpacity(new Color(C.mc.fontRendererObj.getColorCode(character)), originalColours[j].getAlpha()/255d);
                    }
                }
                switch (character) {
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
                    case 'z':
                        rainbow = true;
                        break;

                    case 'r':
                        System.arraycopy(originalColours, 0, colours, 0, colours.length);
                        obfuscated = false;
                        underline = false;
                        bold = false;
                        strikethrough = false;
                        rainbow = false;
                        break;
                }

                continue;
            }

            if (character == '&' || character == '§') {
                colorCode = true;
                continue;
            }

            // code from net.minecraft.client.gui.FontRenderer.renderStringAtPos
            if (obfuscated) {
                int k = (int) getCharWidth(character);
                char c1;

                while (true)
                {
                    int j = fontRandom.nextInt(letters.length());
                    c1 = letters.charAt(j);

                    if (k == (int) getCharWidth(c1))
                    {
                        break;
                    }
                }

                character = c1;
            }

            Texture texture = textures.get(character);

            float width = texture != null ? texture.width : C.mc.fontRendererObj.getCharWidth(character) * (size/10);

            Color[] fadeColours = rainbow
                    ? RenderUtil.getColorsFade(x, width, RenderUtil.ThemeColours.Gay.getColours(), fadeSpeed)
                    : RenderUtil.getColorsFade(x*fadeSpread, width*fadeSpread, colours, fadeSpeed);

            if (FontUtil.isSpecialChar(character)) {
                GL11.glPushMatrix();
                GL11.glTranslated(x, y + size/3f, 0);
                GL11.glScaled(0.1*size, 0.1*size, 1);

                C.mc.fontRendererObj.drawString(String.valueOf(character), 0, 0, fadeColours[0].getRGB());
                if (width != 0) {
                    x += width;
                }

                GL11.glPopMatrix();
                continue;
            }

            if (texture == null) continue;
            float height = texture.height;

            if (shadow) RenderUtil.drawRectTexturedColor(x + boldOffset, y + boldOffset, width, height, new Color(20,20,20, fadeColours[0].getAlpha()), new Color(20,20,20, colours[1].getAlpha()), texture.dynamicTexture);
            if (bold) RenderUtil.drawRectTexturedColor(x - boldOffset, y, width, height, fadeColours[0], fadeColours[1], texture.dynamicTexture);

            // draw letter!
            RenderUtil.drawRectTexturedColor(x, y, width, height, fadeColours[0], fadeColours[1], texture.dynamicTexture);

            if (strikethrough) RenderUtil.drawGradientLR(x, y + (height/1.7f), width, height/20f, fadeColours[0], fadeColours[1]);
            if (underline) RenderUtil.drawGradientLR(x, y + (height / 1.2f), width, height/20f, fadeColours[0], fadeColours[1]);

            x += width;
        }

        GL11.glPopMatrix();
    }

    private static final BufferedImage blankImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    private static final Graphics2D graphics = blankImage.createGraphics();

    public static float getCharWidth(char character) {
        if (isSpecialChar(character))
            return C.mc.fontRendererObj.getCharWidth(character) * (graphics.getFont().getSize() / 10f);
        else {
            return graphics.getFontMetrics().getStringBounds(String.valueOf(character), graphics).getBounds().width;
        }

    }

    public static int getStringWidth(String string, int size) {
        String validText = string.replaceAll("[&§].", "");
        if (validText.isEmpty()) return 0;

        float width = 0;

        int scaleFactor = C.res().getScaleFactor();

        size *= scaleFactor;
        graphics.setFont(currentFont.deriveFont((float)size));

        for (int i = 0; i < validText.length(); i++) width += getCharWidth(validText.charAt(i));

        return (int) (width / scaleFactor);
    }

    public static boolean isSpecialChar(char character) {
        return !letters.contains(String.valueOf(character));
    }

    public static float getFontHeight(float size) {
        graphics.setFont(currentFont.deriveFont(size));

        return graphics.getFontMetrics().getHeight();
    }

    private static HashMap<Character, Texture> getFont(int size) {
        boolean validFontSize = fonts.containsKey(size);
        if (!validFontSize) loadFont(size);

       return fonts.get(size);
    }

    @AllArgsConstructor
    public enum Fonts {
        Atkinson("atkinson.ttf"),
        Product_Sans("productsans.ttf"),
        Comic_Sans("comicsans.ttf"),
        Segoe_Print("segoeprint.ttf"),
        Jetbrains_Mono("JetbrainsMono.ttf"),
        Love_Days("lovedays.ttf"),
        Thug_Font("thugfont.otf");

        public final String name;
    }

}
