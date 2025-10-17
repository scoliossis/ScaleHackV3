package com.github.scoliossis.modules.impl.movement;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.MovementInputEvent;
import com.github.scoliossis.events.impl.PlayerUpdateEvent;
import com.github.scoliossis.events.impl.RenderWorldEvent;
import com.github.scoliossis.events.impl.RotationEvent;
import com.github.scoliossis.modules.*;
import com.github.scoliossis.modules.impl.client.ThemeModule;
import com.github.scoliossis.utils.client.C;
import com.github.scoliossis.utils.minecraft.*;
import com.github.scoliossis.utils.render.Render3dUtil;
import com.github.scoliossis.utils.render.RenderUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

@RegisterModule(
        name = "Scaffold",
        description = "wowowowow godbriger 2023???",
        category = Category.MOVEMENT,
        dangerous = true
)
// todo: safewalk, better rotations
public class Scaffold extends Module {
    @RegisterSubModule(name = "Basics")
    public static SubCategory basicCategory = new SubCategory();

    @RegisterSubModule(name = "Block Place Reach", min = 2, max = 6, increment = 0.1, parent = "Basics")
    public static float blockReach = 5f;

    @RegisterSubModule(name = "Blocks Only", description = "Only scaffold if holding blocks", parent = "Basics")
    public static boolean blocksOnly = true;

    @RegisterSubModule(name = "Tower")
    public static SubCategory towerCategory = new SubCategory();

    @RegisterSubModule(name = "Tower Mode", parent = "Tower", description = "Only tower within certain pitch range")
    public static TowerMode towerMode = TowerMode.Legit;

    public enum TowerMode {
        None,
        Legit,
        Vanilla
    }

    @RegisterSubModule(name = "Default To Keep Y", parent = "Tower Mode", modeParentString = {"Legit", "Vanilla"}, description = "If requirements are not met, assumes you want keep y")
    public static boolean defaultKeepY = true;

    @RegisterSubModule(name = "Only Off Ground", parent = "Tower Mode", modeParentString = {"Legit", "Vanilla"}, description = "Only tower if holding your jump key")
    public static boolean onlyOffGround = true;

    @RegisterSubModule(name = "Only If Space Down", parent = "Tower Mode", modeParentString = {"Legit", "Vanilla"}, description = "Only tower if holding your jump key")
    public static boolean onlyIfSpaceDown = true;

    @RegisterSubModule(name = "Tower Pitch Range", parent = "Tower Mode", modeParentString = {"Legit", "Vanilla"}, description = "Only tower within certain pitch range")
    public static boolean onlyTowerLookingUp = true;

    @RegisterSubModule(name = "Min Pitch", parent = "Tower Pitch Range", description = "-90 is looking straight up", min = -90, max = 90)
    public static int minPitch = -90;

    @RegisterSubModule(name = "Max Pitch", parent = "Tower Pitch Range", description = "-90 is looking straight up", min = -90, max = 90)
    public static int maxPitch = 0;

    @RegisterSubModule(name = "Visuals")
    public static SubCategory visuals = new SubCategory();

    @RegisterSubModule(name = "Auto F5", parent = "Visuals")
    public static boolean autoF5 = true;

    @RegisterSubModule(name = "Show Previous Blocks", parent = "Visuals")
    public static boolean showPreviousBlocks = true;

    @RegisterSubModule(name = "Fade Time", parent = "Show Previous Blocks", min = 50, max = 10000, increment = 50)
    public static long showPreviousBlocksTime = 3000;

    @RegisterSubModule(name = "Bypass")
    public static SubCategory bypass = new SubCategory();

    @RegisterSubModule(name = "No Duplicate Rot", description = "Bypasses grims DuplicateRotPlace check", parent = "Bypass")
    public static boolean noDuplicateRot = true;

    @RegisterSubModule(name = "Bridging Mode", parent = "Bypass")
    public static BridgingMode bridgingMode = BridgingMode.God;

    public enum BridgingMode {
        God,
        Telly,
        Derp
    }

    @RegisterSubModule(name = "Only When Jumping", description = "Only telly bridge if space held down", parent = "Bridging Mode", modeParentString = "Telly")
    public static boolean spaceDownOnly = true;

    @RegisterSubModule(name = "Telly Mode", description = "Ticks To Snap Back To Looking Forward After Landing", max = 5, parent = "Bridging Mode", modeParentString = "Telly")
    public static Telly_Mode tellyMode = Telly_Mode.Hypixel;
    @AllArgsConstructor
    public enum Telly_Mode {
        Hypixel(true, 1, 4, 1),
        Grim(false, 1, 0, 0),
        Custom(false, -1, -1, -1) {
            @Override
            public boolean isSmoothRotationTelly() {
                return Scaffold.smoothRotationTelly;
            }
            @Override
            public int getTellyTicks() {
                return Scaffold.tellyTicks;
            }
            @Override
            public int getTellyPlaceDelay() {
                return Scaffold.tellyPlaceDelay;
            }
            @Override
            public int getTellyForwardTicks() {
                return Scaffold.tellyForwardTicks;
            }
        };

        @Getter public final boolean smoothRotationTelly;
        @Getter public final int tellyTicks;
        @Getter public final int tellyPlaceDelay;
        @Getter public final int tellyForwardTicks;
    }


    @RegisterSubModule(name = "Smooth Rotation", description = "Doesn't snap back to placing blocks", parent = "Telly Mode", modeParentString = "Custom")
    public static boolean smoothRotationTelly = true;

    @RegisterSubModule(name = "Telly Ticks", description = "Ticks To Snap Back To Looking Forward After Landing", max = 5, parent = "Telly Mode", modeParentString = "Custom")
    public static int tellyTicks = 1;

    @RegisterSubModule(name = "Telly Place Delay", description = "Ticks Before Placing After Snapping Back", max = 5, parent = "Telly Mode", modeParentString = "Custom")
    public static int tellyPlaceDelay = 4;

    @RegisterSubModule(name = "Telly Forward Ticks", description = "Ticks Before Jumping", max = 5, parent = "Telly Mode", modeParentString = "Custom")
    public static int tellyForwardTicks = 1;

    private static boolean shouldScaffold = false;

    private static final ConcurrentHashMap<BlockPos, Long> previousInteractions = new ConcurrentHashMap<>();

    private static float lastPlacedDeltaX = -1;

    private static int blocksPlaced = 0;

    private static int tellyTicksCounter = 0;
    private static int tellyPlaceDelayCounter = 0;

    private static boolean tellyBlockPlaced = true;
    private static int tellyForwardTicksCount = -1;

    private static boolean lastJump = false;

    @SubscribeEvent
    public static void onKeyInput(MovementInputEvent event) {
        if (shouldScaffold) {
            tellyForwardTicksCount = C.p().onGround && tellyBlockPlaced ? tellyForwardTicksCount+1 : -1;

            if (shouldTelly()) {
                event.movementInput.jump = shouldTellyJump(event.movementInput.jump);
                tellyBlockPlaced &= !event.movementInput.jump;
            }
            else {
                event.movementInput.jump |= towerMode == TowerMode.Legit && shouldTower();
            }

            lastJump = C.mc.gameSettings.keyBindJump.isKeyDown();
        }
    }

    private static boolean didPlace = false;
    private static int previousStack = -1;

    @SubscribeEvent
    public static void onRotationEvent(RotationEvent event) {
        didPlace |= C.p().inventory.currentItem == previousStack && InventoryUtil.isSlotEmpty(C.p().inventory.currentItem) && shouldScaffold;

        if (!InventoryUtil.isValidBlock()) {
            int bestStack = InventoryUtil.biggestBlockSlot();
            if ((blocksOnly && !didPlace) || bestStack == -1) {
                disable();
                return;
            }

            C.p().inventory.currentItem = bestStack;
        }

        if (!shouldScaffold) enable();

        didPlace = false;

        Vec3 positionToRotateFrom = C.p().getPositionVector();
        if (!shouldPlaceBlock()) {
            Vec3 predictedNextPosition = getPredictedNextPosition();
            if (predictedNextPosition != null) positionToRotateFrom = predictedNextPosition;
        }

        BlockTarget targetBlock = getBestTargetBlock(positionToRotateFrom);
        if (targetBlock == null) return;

        if (shouldTelly() && C.p().onGround && (tellyBlockPlaced || tellyMode.getTellyForwardTicks() == 0)) {
            tellyTicksCounter = 0;
            tellyPlaceDelayCounter = 0;
        }

        tellyTicksCounter++;
        if (tellyTicksCounter <= tellyMode.getTellyTicks()) return;

        tellyPlaceDelayCounter++;

        if (shouldRotate()) rotate(positionToRotateFrom, targetBlock, event);
    }

    @SubscribeEvent
    public static void onPlayerUpdate(PlayerUpdateEvent event) {
        if (shouldTower() && towerMovement()) setShouldTower();

        if (!shouldPlaceBlock() || !InventoryUtil.isValidBlock()) return;

        if (shouldTelly() && tellyPlaceDelayCounter < tellyMode.getTellyPlaceDelay()) return;

        MovingObjectPosition rayTrace = WorldUtil.rayTrace(blockReach, PlayerUtil.currentRotation());

        if (rayTrace.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
        if (rayTrace.sideHit == EnumFacing.UP && shouldKeepY()) return;

        if (C.mc.playerController.onPlayerRightClick(C.p(), C.w(), C.p().getHeldItem(), rayTrace.getBlockPos(), rayTrace.sideHit, rayTrace.hitVec)) {
            C.p().swingItem();

            lastPlacedDeltaX = Math.abs(PlayerUtil.currentRotation().yaw - PlayerUtil.lastRotation().yaw);
            blocksPlaced++;
            previousInteractions.put(rayTrace.getBlockPos().offset(rayTrace.sideHit), System.currentTimeMillis());

            if (InventoryUtil.isSlotEmpty(C.p().inventory.currentItem)) {
                C.p().inventory.removeStackFromSlot(C.p().inventory.currentItem);
            }

            previousStack = C.p().inventory.currentItem;

            tellyBlockPlaced = C.p().onGround;
        }
    }

    private static boolean shouldTelly() {
        return bridgingMode == BridgingMode.Telly && (!spaceDownOnly || C.mc.gameSettings.keyBindJump.isKeyDown());
    }

    private static boolean shouldPlaceBlock() {
        return WorldUtil.isOverAir()
                && (C.p().onGround || !shouldKeepY() || WorldUtil.isOverAir(C.p().getPositionVector().subtract(0, 1, 0)));
    }

    private static boolean shouldRotate() {
        return bridgingMode != BridgingMode.Derp || shouldPlaceBlock();
    }

    // 1 second time travel hack
    private static Vec3 getPredictedNextPosition() {
        Vec3 pos = C.p().getPositionVector();
        double velocityX = C.p().posX - C.p().prevPosX;
        double velocityZ = C.p().posZ - C.p().prevPosZ;

        for (int i = 1; i <= 20; i++) {
            pos = pos.add(new Vec3(velocityX, 0, velocityZ));
            if (WorldUtil.isOverAir(pos)) return pos;
        }


        return null;
    }

    @SubscribeEvent
    public static void onRenderWorldEvent(RenderWorldEvent event) {
        if (!showPreviousBlocks) return;

        previousInteractions.forEach((blockPos, time) -> {
            if (System.currentTimeMillis() - time > showPreviousBlocksTime) {
                previousInteractions.remove(blockPos);
                return;
            }

            double animationValue = (double) (System.currentTimeMillis() - time) / showPreviousBlocksTime;

            Color color = RenderUtil.getColorsFade(time / 20d, ThemeModule.getThemeColours(), 0.2f);

            Render3dUtil.draw3dBox(
                    blockPos.getX(),
                    blockPos.getY(),
                    blockPos.getZ(),
                    1,
                    1,
                    1,
                    RenderUtil.setOpacity(color, 0.5*(1 - animationValue)),
                    event.partialTicks,
                    !previousInteractions.containsKey(blockPos.offset(EnumFacing.DOWN)),
                    !previousInteractions.containsKey(blockPos.offset(EnumFacing.UP)),
                    !previousInteractions.containsKey(blockPos.offset(EnumFacing.NORTH)),
                    !previousInteractions.containsKey(blockPos.offset(EnumFacing.SOUTH)),
                    !previousInteractions.containsKey(blockPos.offset(EnumFacing.WEST)),
                    !previousInteractions.containsKey(blockPos.offset(EnumFacing.EAST)),
                    false
            );
        });
    }

    private static boolean shouldTellyJump(boolean jumpDown) {
        return (tellyBlockPlaced && tellyForwardTicksCount >= tellyMode.getTellyForwardTicks())
                || (jumpDown && !lastJump)
                || tellyMode.getTellyForwardTicks() == 0;
    }

    // todo: mess.
    private static boolean shouldTower = false;

    private static boolean shouldKeepY() {
        return !shouldTower() && defaultKeepY && !C.p().onGround;
    }

    private static boolean shouldTower() {
        if (towerMode == TowerMode.None) return false;

        // only stop and start towering when on ground
        if (C.p().onGround || MovementUtil.airTicks == 1) setShouldTower();

        return shouldTower;
    }

    private static void setShouldTower() {
        shouldTower = (!onlyOffGround || !C.p().onGround)
                && (!onlyIfSpaceDown || C.mc.gameSettings.keyBindJump.isKeyDown())
                && (!onlyTowerLookingUp || C.p().rotationPitch <= maxPitch && C.p().rotationPitch >= minPitch);
    }

    /// returns true when the player can stop towering if they want to
    // get em high
    private static boolean towerMovement() {
        switch (towerMode) {
            case Legit:
                return true;
            case Vanilla:
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
    private static void rotate(Vec3 playerPosition, BlockTarget blockTarget, RotationEvent event) {
        MovingObjectPosition blockHitResult = WorldUtil.rayTrace(blockReach, playerPosition, PlayerUtil.lastRotation());

        BlockPos currentBlock = blockHitResult.getBlockPos().offset(blockHitResult.sideHit);
        BlockPos targetBlock = blockTarget.pos.offset(blockTarget.direction);

        // the previous blockpos is good, dont change.
        if (!currentBlock.equals(targetBlock)) {
            float closestPitch = 91;
            float closestYaw = 181;
            boolean foundRotation = false;

            // searches for closest yaw + pitch change
            // <= Math.abs(closestYaw) to stop worthless loops!
            for (float yaw = -closestYaw+1; yaw <= Math.abs(closestYaw); yaw++) {
                for (float pitch = 90; pitch >= 0; pitch--) {
                    RotationUtil.Rotation gcdedRotation = RotationUtil.applyGcd(new RotationUtil.Rotation(pitch, PlayerUtil.lastRotation().yaw + yaw));
                    float yawChange = gcdedRotation.yaw - PlayerUtil.lastRotation().yaw;

                    // check from grim:
                    /* https://github.com/GrimAnticheat/Grim/blob/2.0/common/src/main/java/ac/grim/grimac/checks/impl/scaffolding/DuplicateRotPlace.java
                    // where deltaX = rotationUpdate.getDeltaXRotABS();
                    // and this.lastPlacedDeltaX = deltaX onPostFlyingBlockPlace
                    if (deltaX > 2) {
                        float xDiff = Math.abs(deltaX - lastPlacedDeltaX);
                        if (xDiff < 0.0001) {
                            flagAndAlert("x=" + xDiff + " xdots=" + xDiffDots + " y=" + deltaY);
                        }
                        // rewards if else
                    }
                     */
                    float deltaX = Math.abs(yawChange);
                    if (deltaX > 2 && noDuplicateRot) {
                        float xDiff = Math.abs(deltaX - lastPlacedDeltaX);
                        if (xDiff < 0.0001) continue;
                    }

                    MovingObjectPosition raycast = WorldUtil.rayTrace(blockReach, playerPosition, gcdedRotation);
                    if (raycast == null) continue;
                    BlockPos raycastBlock = raycast.getBlockPos().offset(raycast.sideHit);

                    if (raycastBlock.equals(targetBlock) && (!shouldKeepY() || raycast.sideHit != EnumFacing.UP)) {
                        float bestRotationChange = Math.abs(PlayerUtil.lastRotation().pitch - closestPitch) + Math.abs(closestYaw);
                        float currentRotationChange = Math.abs(PlayerUtil.lastRotation().pitch - gcdedRotation.pitch) + Math.abs(yawChange);
                        if (currentRotationChange < bestRotationChange) {
                            closestYaw = yawChange;
                            closestPitch = gcdedRotation.pitch;
                            foundRotation = true;
                        }
                    }
                }
            }

            if (foundRotation) {
                RotationUtil.Rotation bestRotation = new RotationUtil.Rotation(closestPitch, PlayerUtil.lastRotation().yaw + closestYaw);

                if (shouldSmoothRotate()) {
                    event.rotation = RotationUtil.getSmoothRotation(bestRotation, getSmoothRotateFactor());
                }
                else event.rotation = bestRotation;

                return;
            }
        }

        if (bridgingMode != BridgingMode.Derp) {
            event.rotation = PlayerUtil.lastRotation();
        }
    }

    private static boolean shouldSmoothRotate() {
        return shouldTelly()
                && tellyPlaceDelayCounter < tellyMode.getTellyPlaceDelay()
                && tellyMode.isSmoothRotationTelly();
    }

    private static float getSmoothRotateFactor() {
        return tellyMode.getTellyPlaceDelay()-tellyPlaceDelayCounter;
    }

    private static int previousPerspective = 0;

    private static void enable() {
        shouldScaffold = true;
        blocksPlaced = 0;

        if (autoF5) {
            previousPerspective = C.mc.gameSettings.thirdPersonView;
            C.mc.gameSettings.thirdPersonView = 1;
        }
    }

    @Override
    protected void onEnable() {}

    private static void disable() {
        if (shouldScaffold) {
            shouldScaffold = false;
            shouldTower = false;

            if (autoF5) {
                C.mc.gameSettings.thirdPersonView = previousPerspective;
            }
        }
    }

    @Override
    protected void onDisable() {
        disable();
    }

    @Override
    public String arrayListExtraInfo() {
        return bridgingMode.name();
    }

    // no records :( java 8
    @AllArgsConstructor
    private static class BlockTarget {
        public BlockPos pos;
        public EnumFacing direction;
    }
}