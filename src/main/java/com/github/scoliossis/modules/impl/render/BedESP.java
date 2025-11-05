package com.github.scoliossis.modules.impl.render;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.RenderWorldEvent;
import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;
import com.github.scoliossis.modules.RegisterSubModule;
import com.github.scoliossis.utils.client.C;
import com.github.scoliossis.utils.minecraft.BlockTracker;
import com.github.scoliossis.utils.render.Render3dUtil;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.ArrayList;

@RegisterModule(
        name = "Bed ESP",
        description = "scale-chan, c-can we,,, put our m-m-minecraft beds next to eachother >.<",
        category = Category.RENDER
)
public class BedESP extends Module {
    @RegisterSubModule(name = "Colour")
    public static Color colour = new Color(255, 167, 167, 50);

    @SubscribeEvent
    public static void drawBeds(RenderWorldEvent event) {
        ArrayList<BlockPos> beds = BlockTracker.getBlockPositions(Blocks.bed);

        if (beds == null) return;

        for (int i = 0; i < beds.size(); i++) {
            BlockPos blockPos = beds.get(i);
            IBlockState state = C.w().getBlockState(blockPos);

            if (!state.getProperties().containsKey(BlockBed.PART) || !state.getProperties().containsKey(BlockBed.FACING)) continue;
            if (state.getValue(BlockBed.PART) != BlockBed.EnumPartType.FOOT) continue;

            EnumFacing facing = state.getValue(BlockBed.FACING);
            Vec3 size = new Vec3(1.0F + Math.abs(facing.getFrontOffsetX()), 0.5625F, 1.0F + Math.abs(facing.getFrontOffsetZ()));
            Render3dUtil.draw3dBox(blockPos.getX() + Math.min(facing.getFrontOffsetX(), 0), blockPos.getY(), blockPos.getZ() + Math.min(facing.getFrontOffsetZ(), 0), size.xCoord, size.yCoord, size.zCoord, colour, event.partialTicks);
        }
    }

    @Override
    protected void onEnable() {
        BlockTracker.beginTracking(Blocks.bed);
    }

    @Override
    protected void onDisable() {
        BlockTracker.stopTracking(Blocks.bed);
    }
}
