package com.github.scoliossis.modules.impl.render;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.MovementInputEvent;
import com.github.scoliossis.events.impl.RenderWorldEvent;
import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;
import com.github.scoliossis.modules.RegisterSubModule;
import com.github.scoliossis.utils.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@RegisterModule(
        name = "Freecam",
        description = "But first, we need to talk about parallel universes",
        category = Category.RENDER
)
public class Freecam extends Module {
    @RegisterSubModule(name = "Horizontal Speed", max = 10)
    public static float horizontalSpeed = 2;
    @RegisterSubModule(name = "Vertical Speed", max = 10)
    public static float verticalSpeed = 2;

    @RegisterSubModule(name = "Allow Interact", description = "Allows you to interact with the world while in freecam")
    public static boolean allowInteract = false;

    // draw real player pos, probably isnt hard to just render the real player, but oh well, lazy ig
    @SubscribeEvent
    public static void onRenderWorld(RenderWorldEvent event) {
        if (PlayerUtil.realPos == null) return;

        float playerWidth = C.p().width;
        float playerHeight = C.p().height;

        Render3dUtil.draw3dBox(PlayerUtil.realPos.xCoord-playerWidth/2, PlayerUtil.realPos.yCoord, PlayerUtil.realPos.zCoord-playerWidth/2, playerWidth, playerHeight, playerWidth, new Color(255,100,200, 100), event.partialTicks);
    }

    @SubscribeEvent
    public static void onMovementInputEvent(MovementInputEvent event) {
        // make sure we can see through walls!
        PlayerUtil.noClipRender = true;

        if (C.mc.currentScreen != null) return;

        float y = Keyboard.isKeyDown(C.mc.gameSettings.keyBindJump.getKeyCode()) ? verticalSpeed : 0;
        y += Keyboard.isKeyDown(C.mc.gameSettings.keyBindSneak.getKeyCode()) ? -verticalSpeed : 0;

        if (PlayerUtil.fakeCameraPos == null)
            PlayerUtil.setFakeCameraPos(C.p().getPositionVector());
        if (PlayerUtil.fakeRotation == null)
            PlayerUtil.fakeRotation = PlayerUtil.realRotation = RotationUtil.getCurrentClientRotation();

        double x = event.movementInput.moveForward * Math.sin(Math.toRadians(-PlayerUtil.fakeRotation.yaw)) + event.movementInput.moveStrafe * Math.cos(Math.toRadians(-PlayerUtil.fakeRotation.yaw));
        double z = event.movementInput.moveForward * Math.cos(Math.toRadians(-PlayerUtil.fakeRotation.yaw)) - event.movementInput.moveStrafe * Math.sin(Math.toRadians(-PlayerUtil.fakeRotation.yaw));

        // reset all pressed
        event.movementInput.moveForward = event.movementInput.moveStrafe = 0;
        event.movementInput.sneak = event.movementInput.jump = false;

        // move camera!
        PlayerUtil.setFakeCameraPos(PlayerUtil.fakeCameraPos.addVector(x*horizontalSpeed, y, z*horizontalSpeed));
    }

    @Override
    protected void onEnable() {
        if (!C.isInGame()) {
            this.toggle();
            return;
        }

        // get ready to rumble
        PlayerUtil.setFakeCameraPos(C.p().getPositionVector());
        PlayerUtil.realPos = C.p().getPositionVector();

        PlayerUtil.fakeRotation = PlayerUtil.realRotation = RotationUtil.getCurrentClientRotation();
    }

    @Override
    protected void onDisable() {
        if (!C.isInGame()) return;

        // reset fake camera
        PlayerUtil.setFakeCameraPos(null);
        PlayerUtil.fakeRotation = null;

        // no more seeing through walls, not on constantly because i think it decreases fps
        PlayerUtil.noClipRender = false;

        C.mc.renderGlobal.loadRenderers();
    }
}
