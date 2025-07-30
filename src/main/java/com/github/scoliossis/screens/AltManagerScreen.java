package com.github.scoliossis.screens;

import com.github.scoliossis.Main;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.FontUtil;
import com.github.scoliossis.utils.RenderUtil;
import com.github.scoliossis.utils.ScreenUtil;
import com.github.scoliossis.utils.alts.Login;
import com.github.scoliossis.utils.alts.SessionUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.apache.commons.io.FileUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AltManagerScreen extends GuiScreen {

    private final float sidebarWidth = 200;
    private final int sidebarFont = 18;
    
    private final int altsFont = 15;
    private final int altInformationFont = 6;

    private final float headSize = 24;
    private final float headIndent = 4;

    private final float xOffset = headSize + headIndent*2;
    private final float yOffset = 20;

    private final float textX = 5;

    private final float altFontHeight = FontUtil.getFontHeight(altsFont);
    private final float altSubFontHeight = FontUtil.getFontHeight(altInformationFont);
    private final float height = altFontHeight + altSubFontHeight + 4;
    private final float gapX = 5;
    private final float gapY = 20;

    private final float altX = sidebarWidth + gapX;
    private final float altY = yOffset;

    private final int iconDiameter = 8;

    private DynamicTexture binTexture;
    private DynamicTexture pencilTexture;

    private final GuiTextField renameTextBox = new GuiTextField(0, C.mc.fontRendererObj, -1, -1, 200, (int) altFontHeight);

    @Override
    public void initGui() {
        if (!Files.exists(Login.altsPath)) Login.altsPath.toFile().mkdirs();
        renameTextBox.setMaxStringLength(16);
        renameTextBox.setCanLoseFocus(false);
        renameTextBox.setFocused(true);
        renameTextBox.setVisible(false);

        try {
            binTexture = new DynamicTexture(ImageIO.read(Main.class.getResourceAsStream("/bin.png")));
            pencilTexture = new DynamicTexture(ImageIO.read(Main.class.getResourceAsStream("/pencil.png")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
        alts.clear();

        for (File file : Login.altsPath.toFile().listFiles()) {
            try {
                if (file.isDirectory()) continue;
                if (!file.getName().endsWith(".json")) continue;

                HashMap<String, String> json = gson.fromJson(FileUtils.readFileToString(file), HashMap.class);

                Login.AltTypes altType = Login.AltTypes.Session;
                if (json.containsKey("cookie")) altType = Login.AltTypes.Cookie;
                if (json.containsKey("refreshToken")) altType = Login.AltTypes.Microsoft;

                alts.add(new Login.Alt(json.get("name"), json.get("uuid"), altType, json));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static final List<Login.Alt> alts = new ArrayList<>();

    private Login.Alt renamedAlt;

    public float scrollAmount = 0;
    public float renderedScroll = 0;

    @Override
    public void drawScreen(int mX, int mY, float partialTicks) {
        super.drawBackground(0);

        String hoverText = null;

        RenderUtil.drawBlurRect(0, 0, sidebarWidth, C.res().getScaledHeight(), 10);
        RenderUtil.drawRect(0, 0, sidebarWidth, C.res().getScaledHeight(), new Color(22,22,22, 100));

        GL11.glPushMatrix();
        for (Login.AltTypes altType : Login.AltTypes.values()) {
            float textX = sidebarWidth / 2;
            float height = FontUtil.getFontHeight(sidebarFont);
            float buttonY = height * 2;

            boolean hovered = ScreenUtil.isMouseOver(0, buttonY, sidebarWidth, height, mX, mY);

            Color stringColor = hovered ? new Color(255,255,255,255) : new Color(255,255,255,190);

            FontUtil.drawCenteredString(altType.name, textX, buttonY, sidebarFont, stringColor, true);
            FontUtil.drawCenteredString(altType.description, textX, buttonY + height, sidebarFont/2, new Color(100,100,100), true);

            if (hovered && mouseButton == 0) {
                altType.action.run();
                mouseButton = -1;
            }

            GL11.glTranslated(0, buttonY, 0);
        }
        GL11.glPopMatrix();

        GL11.glPushMatrix();

        GL11.glTranslated(0, renderedScroll, 0);

        float[] translation = RenderUtil.getCurrentTranslation();

        // not using a fancy for loop to avoid ConcurrentModificationException
        for (int i = 0; i < alts.size(); i++) {
            Login.Alt alt = alts.get(i);

            DynamicTexture texture = new DynamicTexture(alt.getHead());

            boolean isCurrentAccount = alt.uuid.equals(C.mc.getSession().getPlayerID());
            boolean banned = isBanned(alt);
            boolean nameChange = canNameChange(alt);

            String altName = (renamedAlt != null && renamedAlt == alt) ? renameTextBox.getText() + "_" : alt.name;
            String info = (alt.type.colour + alt.type.name + " &f| " +
                    (banned ? "&cBanned" : "&aUnbanned") + " &f| " +
                    (nameChange ? "&a" : "&c") + "Name &f| ");
            float infoStringWidth = FontUtil.getStringWidth(info, altInformationFont);

            float width = Math.max(FontUtil.getStringWidth(altName, altsFont), infoStringWidth + iconDiameter*2) + textX + xOffset;

            translation = RenderUtil.getCurrentTranslation();

            if (translation[0] != 0 && altX + translation[0] + width > C.res().getScaledWidth()) {
                GL11.glTranslated(-translation[0], gapY + height, 0);
                translation[0] = 0;
                translation[1] += gapY + height;
            }

            boolean subtextHovered = ScreenUtil.isMouseOver(altX + xOffset, altY + altFontHeight, width, altSubFontHeight + 4, mX, mY);
            boolean hovered = !subtextHovered && !isCurrentAccount && ScreenUtil.isMouseOver(altX, altY, width, height, mX, mY);

            Color stringColor =
                    (isCurrentAccount && renamedAlt != alt) ? new Color(99, 255, 102,255) :
                    hovered ? new Color(255,255,255,255) : new Color(255,255,255,190);
            RenderUtil.drawBlurRect(altX, altY, width, height, 3);
            RenderUtil.drawRect(altX, altY, width, height, new Color(22,22,22, 100));
            RenderUtil.drawRectTextured(altX + headIndent, altY + headIndent, headSize, headSize, Color.WHITE, texture);
            Color[] colorsFade = RenderUtil.getColorsFade(translation[0]+altX, width, RenderUtil.ThemeColours.Gay.colours, 1);
            RenderUtil.drawGradientLR(altX, altY, width, 1, colorsFade[0], colorsFade[1]);

            FontUtil.drawString(altName, altX + xOffset, altY, altsFont, stringColor, true);
            FontUtil.drawString(info, altX + xOffset, altY + altFontHeight, altInformationFont, Color.BLACK, true);

            boolean binHovered = ScreenUtil.isMouseOver(altX + xOffset + infoStringWidth, altY + altFontHeight, iconDiameter, iconDiameter, mX, mY);
            boolean pencilHovered = ScreenUtil.isMouseOver(altX + xOffset + infoStringWidth + iconDiameter, altY + altFontHeight, iconDiameter, iconDiameter, mX, mY);

            RenderUtil.drawRectTextured(altX + xOffset + infoStringWidth, altY + altFontHeight, iconDiameter, iconDiameter, binHovered ? Color.LIGHT_GRAY : Color.WHITE, binTexture);
            RenderUtil.drawRectTextured(altX + xOffset + infoStringWidth + iconDiameter, altY + altFontHeight, iconDiameter, iconDiameter, pencilHovered ? Color.LIGHT_GRAY : Color.WHITE, pencilTexture);

            if (binHovered) {
                if (mouseButton == 0) {
                    try {
                        Files.delete(Login.getAccountPath(alt.uuid));
                        AltManagerScreen.alts.remove(alt);
                        Login.addProgressReport("Removed alt: &6" + alt.name);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mouseButton = -1;
                }
            }
            else if (pencilHovered) {
                if (mouseButton == 0) {
                    if (nameChange) {
                        renamedAlt = alt;
                        renameTextBox.setText("");
                        Login.addProgressReport("Type in a new name for account: &6" + alt.name + "&f.");
                    }
                    else {
                        Login.setErrorMessage("Cannot name change account: &6" + alt.name + "&f.");
                    }

                    mouseButton = -1;
                }
            }
            else if (subtextHovered) {
                String[] infoParts = info.split("\\|");

                int partOffset = 0;
                for (String infoPart : infoParts) {
                    int textLength = FontUtil.getStringWidth(infoPart, altInformationFont);

                    if (ScreenUtil.isMouseOver(altX + xOffset + partOffset, altY + altFontHeight, textLength, altSubFontHeight + 4, mX, mY)) {
                        hoverText = getInfoPart(infoPart, alt);
                        break;
                    }

                    partOffset += textLength;
                }

            }
            else if (hovered) {
                if (mouseButton == 0) {
                    if (!alt.json.containsKey("session") || !Login.loginSession(alt.json.get("session"))) {
                        switch (alt.type) {
                            case Microsoft:
                                if (alt.json.containsKey("refreshToken")) {
                                    Login.loginMicrosoft(alt.json.get("refreshToken"));
                                    break;
                                }
                            case Cookie:
                                if (alt.json.containsKey("cookie")) {
                                    Login.loginCookie(alt.json.get("cookie"));
                                }
                                break;
                            case Session:
                                if (alt.json.containsKey("session")) {
                                    Login.loginSession(alt.json.get("session"));
                                    break;
                                }
                        }
                    }
                }
            }

            GL11.glTranslated(width + gapX, 0,0);

            texture.deleteGlTexture();
        }

        scrollAmount += Mouse.getDWheel() / 5f;
        scrollAmount = Math.max(Math.min(0, scrollAmount), renderedScroll-translation[1]);
        renderedScroll += (scrollAmount - renderedScroll) / (Math.max(Minecraft.getDebugFPS() * 0.1f, 2));

        GL11.glPopMatrix();

        if (hoverText != null && !hoverText.isEmpty()) {
            int hoverTextLength = FontUtil.getStringWidth(hoverText, altInformationFont);
            float x = mX;
            if (x + hoverTextLength > C.res().getScaledWidth()) x -= hoverTextLength;
            RenderUtil.drawRect(x, mY - altSubFontHeight - 4, hoverTextLength + 4, altSubFontHeight + 4, new Color(22,22,22, 100));
            FontUtil.drawString(hoverText, x + 2, mY - altSubFontHeight - 2, altInformationFont, Color.WHITE, true);
        }

        renameTextBox.drawTextBox();

        super.drawScreen(mX, mY, partialTicks);

        mouseButton = -1;
    }

    private boolean isBanned(Login.Alt alt) {
        if (alt.json.containsKey("unbanDate")) {
            String unbanDate = alt.json.get("unbanDate");

            if (unbanDate.equals("now")) return false;
            if (unbanDate.equals("never")) return true;

            return Instant.parse(unbanDate).isAfter(Instant.now());
        }

        return false;
    }

    private boolean canNameChange(Login.Alt alt) {
        if (alt.json.containsKey("nameChangeDate")) {
            String nameChangeDate = alt.json.get("nameChangeDate");

            if (nameChangeDate.equals("ALLOWED")) return true;
            // this means the request FAILED. idk what to do, just pretend it worked
            if (nameChangeDate.contains("path")) return true;

            // 30 days in seconds
            return Instant.now().isAfter(Instant.parse(nameChangeDate).plusSeconds(2592000));
        }

        return true;
    }

    private String getInfoPart(String info, Login.Alt alt) {
        if (info.contains("Banned") && alt.json.containsKey("unbanDate")) {
            String unbanDate = alt.json.get("unbanDate");

            if (unbanDate.equals("now")) return "";
            if (unbanDate.equals("never")) return "&4Banned PERMANENTLY";

            // todo: format date.
            return "Banned until: &c" + formatInstant(Instant.parse(unbanDate));
        }

        else if (info.contains("Name") && alt.json.containsKey("nameChangeDate")) {
            String nameChangeDate = alt.json.get("nameChangeDate");

            if (nameChangeDate.equals("ALLOWED")) return "";
            if (nameChangeDate.contains("path")) return "Failed to get name change date.";

            return "Cannot change name until: &c" + formatInstant(Instant.parse(nameChangeDate).plusSeconds(2592000));
        }

        return "";
    }

    private static String formatInstant(Instant instant) {
        Date myDate = Date.from(instant);

        SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mma");
        return formatter.format(myDate);
    }

    public static int mouseButton = -1;

    @Override
    protected void mouseClicked(int altX, int altY, int button) throws IOException {
        super.mouseClicked(altX, altY, button);
        renamedAlt = null;

        mouseButton = button;
    }

    @Override
    protected void mouseReleased(int altX, int altY, int button) {
        super.mouseReleased(altX, altY, button);

        mouseButton = -1;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (renamedAlt != null) {
            if (keyCode == 1) {
                renamedAlt = null;
                return;
            }
            if (keyCode == 28) {
                Login.addProgressReport(SessionUtil.changeName(renamedAlt.json.get("session"), renameTextBox.getText()));
                return;
            }
            renameTextBox.textboxKeyTyped(typedChar, keyCode);
        }
        else {
            super.keyTyped(typedChar, keyCode);
        }
    }
}
