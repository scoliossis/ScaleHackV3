package com.github.scoliossis.utils.minecraft;

import com.github.scoliossis.utils.client.C;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public class InventoryUtil {
    public static boolean isSlotEmpty(int slot) {
        return C.p().inventory.getStackInSlot(slot) == null || C.p().inventory.getStackInSlot(slot).stackSize <= 0;
    }

    public static int biggestBlockSlot() {
        int biggestSlot = -1;

        for (int i = 0; i < InventoryPlayer.getHotbarSize(); i++) {
            if (!isValidBlock(getItemInSlot(i))) continue;

            if (biggestSlot != -1 && C.p().inventory.getStackInSlot(i).stackSize < C.p().inventory.getStackInSlot(biggestSlot).stackSize) continue;

            biggestSlot = i;
        }

        return biggestSlot;
    }

    public static Item getHeldItem() {
        return C.p().inventory.getCurrentItem() == null ? null : C.p().inventory.getCurrentItem().getItem();
    }

    public static Item getItemInSlot(int i) {
        return C.p().inventory.getStackInSlot(i) == null ? null : C.p().inventory.getStackInSlot(i).getItem();
    }

    public static boolean isValidBlock() {
        return isValidBlock(getHeldItem());
    }

    public static boolean isValidBlock(Item stack) {
        Block block = Block.getBlockFromItem(stack);
        if (block == null) return false;

        return isValidBlock(block);
    }

    public static boolean isValidBlock(Block block) {
        return !isBlockFalling(block) && !isBlockInteractable(block) && isBlockSolid(block);
    }

    public static boolean isBlockInteractable(Block block) {
        if (block == Blocks.tnt || block instanceof BlockStairs) return false;

        try {
            return block.getClass().getMethod("onBlockActivated").getDeclaringClass() != Block.class;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isBlockFalling(Block block) {
        return block == Blocks.sand || block == Blocks.gravel || block == Blocks.dragon_egg || block == Blocks.anvil;
    }

    public static boolean isBlockSolid(Block block) {
        AxisAlignedBB collisionBox = block.getCollisionBoundingBox(C.w(), BlockPos.ORIGIN, block.getDefaultState());

        if (collisionBox == null) return false;

        // only full cubes
        return collisionBox.minX == 0 && collisionBox.minY == 0 && collisionBox.minZ == 0
                && collisionBox.maxX == 1 && collisionBox.maxY == 1 && collisionBox.maxZ == 1;
    }
}
