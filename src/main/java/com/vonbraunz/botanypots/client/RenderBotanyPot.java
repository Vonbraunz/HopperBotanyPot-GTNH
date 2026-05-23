package com.vonbraunz.botanypots.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBotanyPot implements ISimpleBlockRenderingHandler {

    public static int RENDER_ID;

    // Pot geometry — matches vanilla flower pot footprint exactly
    private static final float PO = 5f / 16f; // outer min XZ
    private static final float PO2 = 11f / 16f; // outer max XZ
    private static final float PI = 6f / 16f; // inner min XZ (1px wall)
    private static final float PI2 = 10f / 16f; // inner max XZ
    private static final float PH = 6f / 16f; // pot height
    private static final float PBT = 1f / 16f; // bottom plate thickness

    public static void register() {
        RenderBotanyPot instance = new RenderBotanyPot();
        RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(RENDER_ID, instance);
    }

    /** Renders five sub-boxes: one base plate + four 1-px walls = open cup. */
    private static void renderPot(Block block, int x, int y, int z, RenderBlocks renderer) {
        // base plate
        renderer.setRenderBounds(PO, 0, PO, PO2, PBT, PO2);
        renderer.renderStandardBlock(block, x, y, z);

        // front wall (Z-)
        renderer.setRenderBounds(PO, 0, PO, PO2, PH, PI);
        renderer.renderStandardBlock(block, x, y, z);

        // back wall (Z+)
        renderer.setRenderBounds(PO, 0, PI2, PO2, PH, PO2);
        renderer.renderStandardBlock(block, x, y, z);

        // left wall (X-)
        renderer.setRenderBounds(PO, 0, PI, PI, PH, PI2);
        renderer.renderStandardBlock(block, x, y, z);

        // right wall (X+)
        renderer.setRenderBounds(PI2, 0, PI, PO2, PH, PI2);
        renderer.renderStandardBlock(block, x, y, z);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelID,
        RenderBlocks renderer) {
        renderPot(block, x, y, z, renderer);
        return true;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
        IIcon icon = block.getIcon(0, metadata);
        if (icon == null) return;
        Tessellator t = Tessellator.instance;

        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

        // Render each pot sub-box face by face for inventory display
        float[][] boxes = {
            // minX, minY, minZ, maxX, maxY, maxZ
            { PO, 0, PO, PO2, PBT, PO2 }, // base
            { PO, 0, PO, PO2, PH, PI }, // front wall
            { PO, 0, PI2, PO2, PH, PO2 }, // back wall
            { PO, 0, PI, PI, PH, PI2 }, // left wall
            { PI2, 0, PI, PO2, PH, PI2 } }; // right wall

        for (float[] b : boxes) {
            renderer.setRenderBounds(b[0], b[1], b[2], b[3], b[4], b[5]);

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
        }

        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
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
