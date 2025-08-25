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
// todo: auto f5, enable when holding blocks, safewalk, better rotations, telly bridger.
public class Scaffold extends Module {
    @RegisterSubModule(name = "Block Place Reach", min = 2, max = 6, increment = 0.1)
    public static float blockReach = 5f;

    @RegisterSubModule(name = "Show Previous Blocks")
    public static boolean showPreviousBlocks = true;

    @RegisterSubModule(name = "Previous Blocks Time", parent = "Show Previous Blocks", min = 50, max = 10000, increment = 50)
    public static long showPreviousBlocksTime = 3000;

    @RegisterSubModule(name = "Keep Y", description = "im stuck bruh")
    public static boolean keepY = true;

    @RegisterSubModule(name = "Tower", description = "I wanna go to the moon, don't leave so soon, How could I get through?")
    public static boolean tower = true;

    @RegisterSubModule(name = "Tower Pitch Range", parent = "Tower", description = "Only tower within certain pitch range")
    public static boolean onlyTowerLookingUp = true;

    @RegisterSubModule(name = "Min Pitch", parent = "Tower Pitch Range", description = "-90 is looking straight up", min = -90, max = 90)
    public static int minPitch = 0;

    @RegisterSubModule(name = "Max Pitch", parent = "Tower Pitch Range", description = "-90 is looking straight up", min = -90, max = 90)
    public static int maxPitch = 90;

    @RegisterSubModule(name = "Max Blocks Per Tick", parent = "If you want to place multiple blocks in a tick", min = 1, max = 10, dangerous = true)
    public static int maxBlocksPerTick = 1;

    @RegisterSubModule(name = "No Duplicate Rot", description = "Bypasses grims DuplicateRotPlace check")
    public static boolean noDuplicateRot = true;

    @RegisterSubModule(name = "Snap Rotation", description = "Only Rotate To Place Blocks")
    public static boolean snapRotation = true;

    private static final ArrayList<PreviousInteraction> previousInteractions = new ArrayList<>();

    private static RotationUtil.Rotation rotation;
    private static float lastPlacedDeltaX = -1;

    private static int blocksPlaced = 0;

    @SubscribeEvent
    public static void onPlayerUpdate(PlayerUpdateEvent event) {
        if (snapRotation) rotation = RotationUtil.getCurrentClientRotation();

        if (!InventoryUtil.isValidBlock(C.p().getCurrentEquippedItem())) {
            int bestStack = InventoryUtil.biggestBlockSlot();
            if (bestStack == -1) return;

            C.p().inventory.currentItem = bestStack;
        }

        for (int i = 0; i < maxBlocksPerTick; i++) {

            Vec3 positionToRotateFrom = C.p().getPositionVector();
            if (!WorldUtil.isOverAir()) {
                Vec3 predictedNextPosition = getPredictedNextPosition();
                if (predictedNextPosition != null) positionToRotateFrom = predictedNextPosition;
            }

            BlockTarget targetBlock = getBestTargetBlock(positionToRotateFrom);
            if (targetBlock == null) return;

            if (shouldTower() && towerMovement()) setShouldTower();

            if (shouldRotate()) rotate(positionToRotateFrom, targetBlock, event);
            if (!WorldUtil.isOverAir()) return;

            MovingObjectPosition rayTrace = WorldUtil.rayTrace(blockReach, event.rotation);

            if (rayTrace.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
            if (rayTrace.sideHit == EnumFacing.UP && shouldKeepY()) return;

            if (C.mc.playerController.onPlayerRightClick(C.p(), C.w(), C.p().getHeldItem(), rayTrace.getBlockPos(), rayTrace.sideHit, rayTrace.hitVec)) {
                C.p().swingItem();

                float deltaX = Math.abs(event.rotation.yaw - PlayerUtil.getPrevPlayerUpdateEvent().rotation.yaw);
            /* // here to test grim's duplicate rot place check in singleplayer
            if (deltaX > 2) {
                float xDiff = Math.abs(deltaX - lastPlacedDeltaX);
                if (xDiff < 0.0001) {
                    ChatUtil.chat("&bGrim &8» &f" + C.p().getName() + " &bfailed &fDuplicateRotPlace (x&c" + deltaX + "&f)");
                }
            }
             */

                lastPlacedDeltaX = deltaX;
                blocksPlaced++;
                previousInteractions.add(new PreviousInteraction(rayTrace.getBlockPos().offset(rayTrace.sideHit), System.currentTimeMillis(), blocksPlaced));

                // minecraft blehhhh
                if (InventoryUtil.isSlotEmpty(C.p().inventory.currentItem)) C.p().inventory.removeStackFromSlot(C.p().inventory.currentItem);
            }
        }
    }

    private static boolean shouldRotate() {
        return !snapRotation || WorldUtil.isOverAir();
    }

    private static boolean shouldKeepY() {
        return keepY && !shouldTower();
    }

    private static Vec3 getPredictedNextPosition() {
        Vec3 pos = C.p().getPositionVector();
        double averageXvelocity = C.p().posX - C.p().prevPosX;
        double averageZvelocity = C.p().posZ - C.p().prevPosZ;

        for (int i = 1; i <= 20; i++) {
            pos = pos.add(new Vec3(averageXvelocity, 0, averageZvelocity));
            if (WorldUtil.isOverAir(pos)) return pos;
        }

        return null;
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

    // todo: mess.
    private static boolean shouldTower = false;

    private static boolean shouldTower() {
        // only stop and start towering when on ground
        if (C.p().onGround || MovementUtil.airTicks == 1) setShouldTower();

        return tower && shouldTower;
    }

    private static void setShouldTower() {
        shouldTower = C.mc.gameSettings.keyBindJump.isKeyDown() && (!onlyTowerLookingUp || C.p().rotationPitch <= maxPitch && C.p().rotationPitch >= minPitch);
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
            case 41: // 0.42f ~= 0.419999, and im not rounding, im chopping off the digits
                C.p().motionY = 0.33F;
                break;
            case 75: // 42 + 33
                // should always be ~0.25, but it could lose accuracy over time
                C.p().motionY = 1 - (C.p().posY % 1);
                return true;
        }

        return false;
    }

    private static BlockTarget getBestTargetBlock(Vec3 position) {
        BlockPos blockPosition = new BlockPos(position);
        BlockPos point1 = blockPosition.add(-blockReach, -blockReach, -blockReach);
        BlockPos point2 = blockPosition.add(blockReach, -1, blockReach);
        Iterator<BlockPos> blocksInRange = BlockPos.getAllInBox(point1, point2).iterator();

        double bestDistance = Integer.MAX_VALUE;
        BlockTarget bestBlock = null;

        while (blocksInRange.hasNext()) {
            BlockPos blockPos = blocksInRange.next();

            if (!C.w().getBlockState(blockPos).getBlock().isBlockSolid(C.w(), blockPos, EnumFacing.UP)) continue;

            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos blockPosOffset = blockPos.offset(facing);

                // obviously placing a block above you isnt helpful.
                if (facing == EnumFacing.DOWN) continue;
                if (facing == EnumFacing.UP && shouldKeepY()) continue;

                if (C.w().getBlockState(blockPosOffset).getBlock().isBlockSolid(C.w(), blockPosOffset, EnumFacing.UP)) continue;
                if (blockPosOffset.getY() >= C.p().posY) continue;

                Vec3 offsetBlockCentre = new Vec3(blockPosOffset.getX() + 0.5, blockPosOffset.getY() + 0.5, blockPosOffset.getZ() + 0.5);
                double distance = position.distanceTo(offsetBlockCentre);

                if (distance > bestDistance) continue;

                bestDistance = distance;
                bestBlock = new BlockTarget(blockPos, facing);
            }
        }

        if (bestDistance > blockReach) return null;
        return bestBlock;
    }

    // todo: take next block into account.
    // maybe doing the block searching in here would be smarter but idk
    private static void rotate(Vec3 playerPosition, BlockTarget blockTarget, PlayerUpdateEvent event) {
        MovingObjectPosition blockHitResult = WorldUtil.rayTrace(blockReach, playerPosition, rotation);

        // the previous blockpos is good, dont change.
        if (!blockHitResult.getBlockPos().equals(blockTarget.pos) || blockHitResult.sideHit != blockTarget.direction) {
            float closestPitch = 91;
            float closestYaw = 181;
            boolean foundRotation = false;

            BlockPos targetBlock = blockTarget.pos.offset(blockTarget.direction);

            // searches for closest yaw + pitch change
            // <= Math.abs(closestYaw) to stop worthless loops!
            for (float yaw = -closestYaw+1; yaw <= Math.abs(closestYaw); yaw++) {
                for (float pitch = 90; pitch >= 0; pitch--) {
                    RotationUtil.Rotation gcdedRotation = RotationUtil.applyGcd(rotation, new RotationUtil.Rotation(pitch, rotation.yaw + yaw));
                    float yawChange = gcdedRotation.yaw - rotation.yaw;

                    // check from grim:
                    /* https://github.com/GrimAnticheat/Grim/blob/2.0/common/src/main/java/ac/grim/grimac/checks/impl/scaffolding/DuplicateRotPlace.java
                    if (deltaX > 2) {
                        float xDiff = Math.abs(deltaX - lastPlacedDeltaX);
                        if (xDiff < 0.0001) {
                            flagAndAlert("x=" + xDiff + " xdots=" + xDiffDots + " y=" + deltaY);
                        }
                        // rewards if else
                    }
                     */
                    float deltaX = Math.abs(gcdedRotation.yaw - PlayerUtil.getPrevPlayerUpdateEvent().rotation.yaw);
                    if (deltaX > 2 && noDuplicateRot) {
                        float xDiff = Math.abs(deltaX - lastPlacedDeltaX);
                        if (xDiff < 0.0001) continue;
                    }

                    MovingObjectPosition raycast = WorldUtil.rayTrace(blockReach, playerPosition, gcdedRotation);
                    if (raycast == null) continue;
                    BlockPos raycastBlock = raycast.getBlockPos().offset(raycast.sideHit);

                    if (raycastBlock.equals(targetBlock) && (!shouldKeepY() || raycast.sideHit != EnumFacing.UP)) {
                        float bestRotationChange = Math.abs(rotation.pitch - closestPitch) + Math.abs(closestYaw);
                        float currentRotationChange = Math.abs(rotation.pitch - gcdedRotation.pitch) + Math.abs(yawChange);
                        if (currentRotationChange < bestRotationChange) {
                            closestYaw = yawChange;
                            closestPitch = gcdedRotation.pitch;
                            foundRotation = true;
                        }
                    }
                }
            }

            if (foundRotation) {
                rotation = new RotationUtil.Rotation(closestPitch, rotation.yaw + closestYaw);
            }
        }

        event.rotation = rotation;
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
        shouldTower = false;
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