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

// open to pull requests <3, oldish code! main issue is its unoptimized!
public class FontUtil {
    @Setter
    private static String fontName = "atkinson.ttf";
    // huge
    private static final HashMap<Integer, HashMap<Character, Texture>> fonts = new HashMap<>();

    private static Font currentFont = null;

    // list by minecraft in FontRenderer.renderChar
    private static final String letters = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000";

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

            textureMap.put(character, new Texture(image.getWidth(), image.getHeight(), font.getSize(), texture));
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
        public int width, height, size;
        public DynamicTexture dynamicTexture;
    }

    public static void drawCenteredString(String string, float x, float y, int size, Color colour, boolean shadow) {
        drawString(string, x-(getStringWidth(string, size)/2f), y, size, colour, shadow);
    }

    // todo: according to profiler, the issue is the finishRendering() function, mainly that it calls getTessalator().draw()
    //  so to optimize this i will somehow need to draw each character in order at the end of a frame? because i believe you cant just change texture mid render.
    public static void drawString(String string, float x, float y, int size, Color colour, boolean shadow) {
        GL11.glPushMatrix();

        int scaleFactor = getScaleFactor();
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
                if (colourCodes.indexOf(character) != -1) colour = new Color(C.mc.fontRendererObj.getColorCode(character));
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

                    default:
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

            if (obfuscated) character = letters.charAt((int) (Math.random()*letters.length()));

            Texture texture = textures.get(character);
            if (texture == null) continue;

            float width = texture.width;
            float height = texture.height;

            Color[] colours = new Color[] {colour, colour};
            if (rainbow) colours = RenderUtil.getColorsFade(x, width, RenderUtil.ThemeColours.Gay.colours, 3f);

            if (shadow) RenderUtil.drawRectTexturedColor(x + boldOffset, y + boldOffset, width, height, new Color(20,20,20, colours[0].getAlpha()), new Color(20,20,20, colours[1].getAlpha()), texture.dynamicTexture);
            if (bold) RenderUtil.drawRectTexturedColor(x - boldOffset, y, width, height, colours[0], colours[1], texture.dynamicTexture);

            // draw letter!
            RenderUtil.drawRectTexturedColor(x, y, width, height, colours[0], colours[1], texture.dynamicTexture);

            if (strikethrough) RenderUtil.drawRectFade(x, y + (height/1.7f), width, height/20f, colours[0], colours[1]);
            if (underline) RenderUtil.drawRectFade(x, y + (height / 1.2f), width, height/20f, colours[0], colours[1]);

            x += width;
        }

        GL11.glPopMatrix();
    }

    private static final BufferedImage blankImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    private static final Graphics2D graphics = blankImage.createGraphics();

    private static int getScaleFactor() {
        return RenderUtil.renderSide == RenderUtil.RenderSide.World ? 4 : C.res().getScaleFactor();
    }

    public static int getStringWidth(String string, int size) {
        int scaleFactor = getScaleFactor();
        size *= scaleFactor;
        graphics.setFont(currentFont.deriveFont((float)size));

        return graphics.getFontMetrics().getStringBounds(string.replaceAll("[&§].", ""), graphics).getBounds().width / scaleFactor;
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
