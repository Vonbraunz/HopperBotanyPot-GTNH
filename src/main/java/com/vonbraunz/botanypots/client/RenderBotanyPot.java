package com.vonbraunz.botanypots.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import com.vonbraunz.botanypots.blocks.BlockBotanyPot;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBotanyPot implements ISimpleBlockRenderingHandler {

    // Pot geometry: 12/16 wide, 8/16 tall, 2px walls
    private static final float PO = 2f / 16f; // outer min XZ
    private static final float PO2 = 14f / 16f; // outer max XZ
    private static final float PI = 4f / 16f; // inner min XZ (2px wall)
    private static final float PI2 = 12f / 16f; // inner max XZ (2px wall)
    private static final float PH = 8f / 16f; // pot height
    private static final float PBT = 2f / 16f; // bottom plate thickness

    public static void register() {
        RenderBotanyPot instance = new RenderBotanyPot();
        BlockBotanyPot.RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(BlockBotanyPot.RENDER_ID, instance);
    }

    /** Renders five sub-boxes: one base plate + four walls = open cup. */
    private static void renderPot(Block block, int x, int y, int z, RenderBlocks renderer) {
        // base plate
        renderer.setRenderBounds(PO, 0, PO, PO2, PBT, PO2);
        renderer.renderStandardBlock(block, x, y, z);

        // walls sit on top of the base plate to avoid overlapping faces
        // front wall (Z-)
        renderer.setRenderBounds(PO, PBT, PO, PO2, PH, PI);
        renderer.renderStandardBlock(block, x, y, z);

        // back wall (Z+)
        renderer.setRenderBounds(PO, PBT, PI2, PO2, PH, PO2);
        renderer.renderStandardBlock(block, x, y, z);

        // left wall (X-)
        renderer.setRenderBounds(PO, PBT, PI, PI, PH, PI2);
        renderer.renderStandardBlock(block, x, y, z);

        // right wall (X+)
        renderer.setRenderBounds(PI2, PBT, PI, PO2, PH, PI2);
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
        IIcon sideIcon = block.getIcon(0, metadata);
        IIcon topIcon = sideIcon;
        if (sideIcon == null) return;
        Tessellator t = Tessellator.instance;

        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

        // Render each pot sub-box face by face; base plate top uses the overlay icon
        float[][] boxes = {
            // minX, minY, minZ, maxX, maxY, maxZ
            { PO, 0, PO, PO2, PBT, PO2 }, // base (index 0 — top face gets topIcon)
            { PO, PBT, PO, PO2, PH, PI }, // front wall
            { PO, PBT, PI2, PO2, PH, PO2 }, // back wall
            { PO, PBT, PI, PI, PH, PI2 }, // left wall
            { PI2, PBT, PI, PO2, PH, PI2 } }; // right wall

        for (int i = 0; i < boxes.length; i++) {
            float[] b = boxes[i];
            renderer.setRenderBounds(b[0], b[1], b[2], b[3], b[4], b[5]);
            IIcon faceTopIcon = (i == 0) ? topIcon : sideIcon;

            t.startDrawingQuads();
            t.setNormal(0, -1, 0);
            renderer.renderFaceYNeg(block, 0, 0, 0, sideIcon);
            t.draw();

            t.startDrawingQuads();
            t.setNormal(0, 1, 0);
            renderer.renderFaceYPos(block, 0, 0, 0, faceTopIcon);
            t.draw();

            t.startDrawingQuads();
            t.setNormal(0, 0, -1);
            renderer.renderFaceZNeg(block, 0, 0, 0, sideIcon);
            t.draw();

            t.startDrawingQuads();
            t.setNormal(0, 0, 1);
            renderer.renderFaceZPos(block, 0, 0, 0, sideIcon);
            t.draw();

            t.startDrawingQuads();
            t.setNormal(-1, 0, 0);
            renderer.renderFaceXNeg(block, 0, 0, 0, sideIcon);
            t.draw();

            t.startDrawingQuads();
            t.setNormal(1, 0, 0);
            renderer.renderFaceXPos(block, 0, 0, 0, sideIcon);
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
        return BlockBotanyPot.RENDER_ID;
    }
}
