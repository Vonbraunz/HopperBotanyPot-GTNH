package com.vonbraunz.botanypots.client;

import com.vonbraunz.botanypots.blocks.BlockBotanyPot;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderBotanyPot implements ISimpleBlockRenderingHandler {

    public static int RENDER_ID;

    public static void register() {
        RenderBotanyPot instance = new RenderBotanyPot();
        RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(RENDER_ID, instance);
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
        renderer.setRenderBounds(BlockBotanyPot.MIN_XZ, 0, BlockBotanyPot.MIN_XZ,
                                  BlockBotanyPot.MAX_XZ, BlockBotanyPot.MAX_Y, BlockBotanyPot.MAX_XZ);

        IIcon icon = block.getIcon(0, metadata);
        Tessellator t = Tessellator.instance;

        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

        t.startDrawingQuads();
        t.setNormal(0, -1, 0);
        renderer.renderFaceYNeg(block, 0, 0, 0, icon);
        t.draw();

        t.startDrawingQuads();
        t.setNormal(0, 1, 0);
        renderer.renderFaceYPos(block, 0, 0, 0, icon);
        t.draw();

        t.startDrawingQuads();
        t.setNormal(0, 0, -1);
        renderer.renderFaceZNeg(block, 0, 0, 0, icon);
        t.draw();

        t.startDrawingQuads();
        t.setNormal(0, 0, 1);
        renderer.renderFaceZPos(block, 0, 0, 0, icon);
        t.draw();

        t.startDrawingQuads();
        t.setNormal(-1, 0, 0);
        renderer.renderFaceXNeg(block, 0, 0, 0, icon);
        t.draw();

        t.startDrawingQuads();
        t.setNormal(1, 0, 0);
        renderer.renderFaceXPos(block, 0, 0, 0, icon);
        t.draw();

        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                    Block block, int modelID, RenderBlocks renderer) {
        renderer.setRenderBounds(BlockBotanyPot.MIN_XZ, 0, BlockBotanyPot.MIN_XZ,
                                  BlockBotanyPot.MAX_XZ, BlockBotanyPot.MAX_Y, BlockBotanyPot.MAX_XZ);
        renderer.renderStandardBlock(block, x, y, z);
        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelID) {
        return true;
    }

    @Override
    public int getRenderId() {
        return RENDER_ID;
    }
}
