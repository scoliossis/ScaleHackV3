package com.github.scoliossis.modules.impl.movement;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.PlayerUpdateEvent;
import com.github.scoliossis.events.impl.RenderWorldEvent;
import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;
import com.github.scoliossis.modules.RegisterSubModule;
import com.github.scoliossis.modules.impl.client.ThemeModule;
import com.github.scoliossis.utils.*;
import lombok.AllArgsConstructor;
import net.minecraft.util.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

@RegisterModule(
        name = "Scaffold",
        description = "wowowowow godbriger 2023???",
        category = Category.MOVEMENT,
        dangerous = true
)
// todo: duplicaterotplace on grim, just move yaw a lil sometimes.
public class Scaffold extends Module {
    @RegisterSubModule(name = "Block Place Reach", min = 1, max = 6)
    public static float blockReach = 5f;

    @RegisterSubModule(name = "Show Previous Blocks")
    public static boolean showPreviousBlocks = true;

    @RegisterSubModule(name = "Previous Blocks Time", parent = "Show Previous Blocks", min = 50, max = 10000, increment = 50)
    public static long showPreviousBlocksTime = 3000;

    @RegisterSubModule(name = "Tower", description = "I wanna go to the moon, don't leave so soon, How could I get through?")
    public static boolean tower = true;

    private static final ArrayList<PreviousInteraction> previousInteractions = new ArrayList<>();

    private static RotationUtil.Rotation rotation;

    private static int blocksPlaced = 0;
    private static boolean jumpStarted = false;

    @SubscribeEvent
    public static void onPlayerUpdate(PlayerUpdateEvent event) {
        if (!InventoryUtil.isValidBlock(C.p().getCurrentEquippedItem())) {
            int bestStack = InventoryUtil.biggestBlockSlot();
            if (bestStack == -1) return;

            C.p().inventory.currentItem = bestStack;
        }

        BlockTarget targetBlock = getBestTargetBlock();
        if (targetBlock == null) return;

        if (shouldTower() && towerMovement()) {
            jumpStarted = C.mc.gameSettings.keyBindJump.isKeyDown();
        }

        if (!rotate(targetBlock, event)) {
            event.rotation = rotation;
        }

        if (WorldUtil.isOverAir()) {
            MovingObjectPosition rayTrace = WorldUtil.rayTrace(blockReach, event.rotation);

            if (rayTrace.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

            if (C.mc.playerController.onPlayerRightClick(C.p(), C.w(), C.p().getHeldItem(), rayTrace.getBlockPos(), rayTrace.sideHit, rayTrace.hitVec)) {
                C.p().swingItem();

                blocksPlaced++;
                previousInteractions.add(new PreviousInteraction(rayTrace.getBlockPos().offset(rayTrace.sideHit), System.currentTimeMillis(), blocksPlaced));

                // minecraft blehhhh
                if (InventoryUtil.isSlotEmpty(C.p().inventory.currentItem)) C.p().inventory.removeStackFromSlot(C.p().inventory.currentItem);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderWorldEvent(RenderWorldEvent event) {
        if (!showPreviousBlocks) return;

        for (int i = 0; i < previousInteractions.size(); i++) {
            PreviousInteraction interaction = previousInteractions.get(i);
            if (System.currentTimeMillis() - interaction.time > showPreviousBlocksTime) {
                previousInteractions.remove(interaction);
                continue;
            }

            double animationValue = (double) (System.currentTimeMillis() - interaction.time) / showPreviousBlocksTime;
            double alpha = MathHelper.clamp_double(0.5 * (1 - animationValue), 0, 1);

            Color color = RenderUtil.getColorsFade(interaction.blockNumber * 20, ThemeModule.getThemeColours(), 0.2f);

            Render3dUtil.drawCentered3dBox(
                    interaction.pos.getX()+.5,
                    interaction.pos.getY()+.5,
                    interaction.pos.getZ()+.5,
                    1.01,
                    1.01,
                    1.01,
                    RenderUtil.setOpacity(color, alpha),
                    event.partialTicks,
                    true
            );
        }
    }

    private static boolean shouldTower() {
        // only stop and start towering when on ground
        if (C.p().onGround || MovementUtil.airTicks == 1)
            jumpStarted = C.mc.gameSettings.keyBindJump.isKeyDown();

        return tower && jumpStarted;
    }

    /// returns true when the player can stop towering if they want to
    // get em high
    private static boolean towerMovement() {
        // slightly more reliable than airTicks % 3
        int playerYto2Decimals = (int) ((C.p().posY % 1) * 100);
        switch (playerYto2Decimals) {
            case 0:
                //getJumpUpwardsMotion() is always 0.42F
                // todo: implement jumpboost check maybe?
                // if (this.isPotionActive(Potion.jump)) this.motionY += (double)((float)(this.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F);
                C.p().motionY = 0.42F;
                break;
            case 41:
                C.p().motionY = 0.33F;
                break;
            case 75: // 42 + 33
                // should always be ~0.25, but it could lose accuracy over time
                C.p().motionY = 1 - (C.p().posY % 1);
                return true;
        }

        return false;
    }

    private static BlockTarget getBestTargetBlock() {
        BlockPos point1 = C.p().getPosition().add(-blockReach, -blockReach, -blockReach);
        BlockPos point2 = C.p().getPosition().add(blockReach, -1, blockReach);
        Iterator<BlockPos> blocksInRange = BlockPos.getAllInBox(point1, point2).iterator();

        double bestDistance = Integer.MAX_VALUE;
        BlockTarget bestBlock = null;

        while (blocksInRange.hasNext()) {
            BlockPos blockPos = blocksInRange.next();

            if (!C.w().getBlockState(blockPos).getBlock().isBlockSolid(C.w(), blockPos, EnumFacing.UP)) continue;

            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos blockPosOffset = blockPos.offset(facing);

                if (blockPosOffset.equals(C.p().getPosition()) ||
                        C.w().getBlockState(blockPosOffset).getBlock().isBlockSolid(C.w(), blockPosOffset, EnumFacing.UP) ||
                        blockPosOffset.getY() >= C.p().posY ||
                        facing == EnumFacing.DOWN
                )
                    continue;

                Vec3 offsetBlockCentre = new Vec3(blockPosOffset.getX() + 0.5, blockPosOffset.getY() + 0.5, blockPosOffset.getZ() + 0.5);
                double distance = C.p().getPositionVector().distanceTo(offsetBlockCentre);

                if (bestBlock != null && blockPos.offset(facing).equals(bestBlock.pos.offset(bestBlock.direction))) {
                    Vec3 currentBlockRotationPoint = getPredictedRotationPoint(new BlockTarget(blockPos, facing));
                    Vec3 bestBlockRotationPoint = getPredictedRotationPoint(bestBlock);

                    RotationUtil.Rotation rotationNeededCurrent = RotationUtil.getRotation(C.p().getPositionEyes(1), currentBlockRotationPoint);
                    RotationUtil.Rotation rotationNeededBest = RotationUtil.getRotation(C.p().getPositionEyes(1), bestBlockRotationPoint);

                    double yawChangeCurrent = Math.abs(PlayerUtil.getPrevPlayerUpdateEvent().rotation.yaw - rotationNeededCurrent.yaw);
                    double yawChangeBest = Math.abs(PlayerUtil.getPrevPlayerUpdateEvent().rotation.yaw - rotationNeededBest.yaw);

                    if (yawChangeCurrent > yawChangeBest) continue;
                }
                else if (distance > bestDistance) continue;

                bestDistance = distance;
                bestBlock = new BlockTarget(blockPos, facing);
            }
        }

        if (bestDistance > blockReach) return null;
        return bestBlock;
    }

    // this function is maybe dumb?
    private static Vec3 getPredictedRotationPoint(BlockTarget blockTarget) {
        return new Vec3(blockTarget.pos)
                .add(new Vec3(0.5,0,0.5))
                .add(new Vec3(blockTarget.direction.getFrontOffsetX()*0.5, blockTarget.direction.getFrontOffsetY()*0.5, blockTarget.direction.getFrontOffsetZ()*0.5));
    }

    private static boolean rotate(BlockTarget blockTarget, PlayerUpdateEvent event) {
        MovingObjectPosition blockHitResult = WorldUtil.rayTrace(blockReach, rotation);

        // the previous blockpos is good, dont change.
        if (blockHitResult.getBlockPos().equals(blockTarget.pos) && blockHitResult.sideHit == blockTarget.direction) return false;

        float closestPitch = 91;
        float closestYaw = 181;
        boolean foundRotation = false;

        // searches for closest yaw + pitch change, prioritizes yaw
        // <= Math.abs(closestYaw) to stop worthless loops!
        for (float yaw = -closestYaw; yaw <= Math.abs(closestYaw); yaw++) {
            for (float pitch = 90; pitch >= 0; pitch--) {
                MovingObjectPosition raycast = WorldUtil.rayTrace(blockReach, new RotationUtil.Rotation(pitch, rotation.yaw + yaw));
                if (raycast.sideHit == blockTarget.direction && raycast.getBlockPos().equals(blockTarget.pos)) {
                    if (yaw < closestYaw || Math.abs(rotation.pitch - pitch) > Math.abs(closestPitch - pitch)) {
                        closestYaw = yaw;
                        closestPitch = pitch;
                        foundRotation = true;
                    }
                }
            }
        }

        if (foundRotation) {
            event.rotation = rotation = new RotationUtil.Rotation(closestPitch, rotation.yaw + closestYaw);
            return true;
        }

        return false;
    }

    @Override
    protected void onEnable() {
        if (!C.isInGame()) {
            this.toggle();
            return;
        }

        blocksPlaced = 0;
        rotation = RotationUtil.getCurrentClientRotation();
    }

    @Override
    protected void onDisable() {
        previousInteractions.clear();
    }

    // no records :( java 8
    @AllArgsConstructor
    private static class BlockTarget {
        public BlockPos pos;
        public EnumFacing direction;
    }
    @AllArgsConstructor
    private static class PreviousInteraction {
        public BlockPos pos;
        public long time;
        public int blockNumber;
    }
}