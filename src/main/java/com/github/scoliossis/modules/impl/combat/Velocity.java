package com.github.scoliossis.modules.impl.combat;

import com.github.scoliossis.bridge.net.minecraft.network.play.server.S12PacketEntityVelocityBridge;
import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.ClientTickEvent;
import com.github.scoliossis.events.impl.MovementInputEvent;
import com.github.scoliossis.events.impl.PacketEvent;
import com.github.scoliossis.events.impl.RotationEvent;
import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;
import com.github.scoliossis.modules.RegisterSubModule;
import com.github.scoliossis.utils.BlinkUtil;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.MovementUtil;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.Vec3;

@RegisterModule(
        name = "Velocity",
        description = "heavy dick n balls or whatever, todo: add original description",
        category = Category.COMBAT
)
public class Velocity extends Module {
    @RegisterSubModule(name = "0 Check")
    public static boolean zeroCheck = true;

    @RegisterSubModule(name = "Mode")
    public static VelocityMode velocityMode = VelocityMode.Dynamic;

    // todo: add mode that delays velocity in air until land, lazy
    public enum VelocityMode {
        Vanilla,
        Keep,
        Delay,
        Jump_Reset,
        Dynamic
    }

    @RegisterSubModule(name = "Velocity X", parent = "Mode", modeParentString = {"Vanilla", "Keep"}, min = -2, max = 2)
    public static float xMulti = 1;
    @RegisterSubModule(name = "Velocity Y", parent = "Mode", modeParentString = {"Vanilla", "Keep"}, min = -2, max = 2)
    public static float yMulti = 1;
    @RegisterSubModule(name = "Velocity Z", parent = "Mode", modeParentString = {"Vanilla", "Keep"}, min = -2, max = 2)
    public static float zMulti = 1;

    @RegisterSubModule(name = "Max Delay Ticks", parent = "Mode", modeParentString = {"Delay", "Dynamic"}, min = 1, max = 20)
    public static int maxDelayTicks = 5;

    @RegisterSubModule(name = "Force Sprint", parent = "Mode", modeParentString = {"Jump_Reset", "Dynamic"})
    public static boolean forceSprint = true;

    private static boolean shouldJump = false;

    private static boolean shouldDelay = false;
    private static int blinkStartTick = -1;

    private static Vec3 lastVelocity;

    @SubscribeEvent
    public static void handleVelocityPacket(PacketEvent.Receive event) {
        if (!(event.packet instanceof S12PacketEntityVelocity)) return;

        S12PacketEntityVelocity packet = ((S12PacketEntityVelocity) event.packet);
        if (packet.getEntityID() != C.p().getEntityId()) return;

        S12PacketEntityVelocityBridge accessiblePacket = S12PacketEntityVelocityBridge.from(packet);
        Vec3 originalVelocity = new Vec3(packet.getMotionX()/8000d, packet.getMotionY()/8000d, packet.getMotionZ()/8000d);

        if (Math.abs(originalVelocity.xCoord) <= 0.1 && Math.abs(originalVelocity.zCoord) <= 0.1 && zeroCheck) return;

        if (!shouldDelay) lastVelocity = originalVelocity;

        switch (velocityMode) {
            case Vanilla:
                accessiblePacket.bridge$setMotionX(originalVelocity.xCoord*xMulti);
                accessiblePacket.bridge$setMotionY(originalVelocity.yCoord*yMulti);
                accessiblePacket.bridge$setMotionZ(originalVelocity.zCoord*zMulti);
                break;

            case Keep:
                accessiblePacket.bridge$setMotionX(originalVelocity.xCoord*xMulti + C.p().motionX*(1-xMulti));
                accessiblePacket.bridge$setMotionY(originalVelocity.yCoord*yMulti + C.p().motionY*(1-yMulti));
                accessiblePacket.bridge$setMotionZ(originalVelocity.zCoord*zMulti + C.p().motionZ*(1-zMulti));
                break;

            case Jump_Reset:
            case Dynamic:
                if (C.p().onGround || velocityMode == VelocityMode.Jump_Reset) {
                    shouldJump |= C.p().onGround;
                    break;
                }
            case Delay:
                if (shouldDelay) break;

                event.setCancelled(true);
                BlinkUtil.pushBlink(false, true, event.packet);

                shouldDelay = true;
                blinkStartTick = MovementUtil.ticks;
                break;
        }
    }

    @SubscribeEvent(priority = 2000)
    public static void fixRotationForJumpReset(RotationEvent event) {
        if (!shouldJump || lastVelocity == null || !forceSprint) return;

        event.rotation.yaw = getYawFromVelocity(lastVelocity.xCoord, lastVelocity.zCoord);

        // todo: autoblock still blocks, figure out a smarter way than just not letting ab block
        MovementUtil.oneTickKeybind(C.mc.gameSettings.keyBindUseItem, false);
    }

    @SubscribeEvent
    public static void jumpReset(MovementInputEvent event) {
        if (!shouldJump) return;

        if (forceSprint) {
            event.movementInput.moveForward = event.movementInput.sneak ? (float) 0.3D : 1;
            event.movementInput.moveStrafe = 0;
            MovementUtil.oneTickKeybind(C.mc.gameSettings.keyBindSprint, true);
        }

        event.movementInput.jump |= C.p().onGround;
        shouldJump = false;
    }

    @SubscribeEvent
    public static void resetBlink(ClientTickEvent event) {
        if (!shouldStopBlink()) return;

        stopBlink();
    }

    private static float getYawFromVelocity(double x, double z) {
        float yaw = (float) (Math.toDegrees(Math.atan2(x + z, x - z)) + 45);
        return yaw > 180 ? yaw - 360 : yaw;
    }

    private static boolean shouldStopBlink() {
        return blinkStartTick != -1 && shouldDelay && (MovementUtil.ticks - blinkStartTick > maxDelayTicks || (C.p().onGround && velocityMode == VelocityMode.Dynamic));
    }

    private static void stopBlink() {
        BlinkUtil.popBlink(false, true);
        blinkStartTick = -1;
        shouldDelay = false;
        shouldJump |= velocityMode == VelocityMode.Dynamic && C.p().onGround;
    }

    @Override
    public String arrayListExtraInfo() {
        return velocityMode.name();
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {
        if (blinkStartTick != -1) stopBlink();
    }
}
