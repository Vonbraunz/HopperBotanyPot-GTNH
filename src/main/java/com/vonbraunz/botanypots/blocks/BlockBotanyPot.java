package com.vonbraunz.botanypots.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.vonbraunz.botanypots.tileentity.TileEntityBotanyPot;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockBotanyPot extends BlockContainer {

    // Set by ClientProxy during renderer registration; -1 = no custom renderer (server)
    public static int RENDER_ID = -1;

    // Hitbox matches vanilla flower pot footprint exactly
    private static final float BMIN = 5f / 16f;
    private static final float BMAX = 11f / 16f;
    private static final float BHEIGHT = 6f / 16f;

    /**
     * Resolves a Material instance via reflection so we are immune to GTNH's
     * remapped MCP field names (both "clay" and "ground" are renamed in their fork).
     * Falls back to iterating every declared field on Material until one is found.
     */
    private static Material resolveMaterial() {
        // Try common MCP names first (works in vanilla dev environment)
        for (String name : new String[] { "clay", "ground", "rock", "iron", "wood", "grass" }) {
            try {
                Object val = Material.class.getField(name)
                    .get(null);
                if (val instanceof Material) return (Material) val;
            } catch (Exception ignored) {}
        }
        // Fallback: find first SOLID non-liquid Material — skip air/fire/portal/water/lava
        for (java.lang.reflect.Field f : Material.class.getDeclaredFields()) {
            try {
                f.setAccessible(true);
                Object val = f.get(null);
                if (val instanceof Material) {
                    Material m = (Material) val;
                    if (m.isSolid() && !m.isLiquid()) return m;
                }
            } catch (Exception ignored) {}
        }
        throw new RuntimeException("BotanyPots: failed to resolve any solid Material instance");
    }

    private static final Material POT_MATERIAL = resolveMaterial();

    public BlockBotanyPot() {
        super(POT_MATERIAL);
        setBlockName("botany_pot");
        setHardness(0.6f);
        setCreativeTab(CreativeTabs.tabDecorations);
        setBlockBounds(BMIN, 0, BMIN, BMAX, BHEIGHT, BMAX);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister reg) {
        blockIcon = reg.registerIcon("minecraft:hardened_clay");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        // If our registered icon failed (Angelica/GTNH can return null for missing textures),
        // fall back to hardened clay's icon directly at render time.
        if (blockIcon != null) return blockIcon;
        IIcon fallback = Blocks.hardened_clay.getIcon(side, meta);
        return fallback != null ? fallback : Blocks.clay.getIcon(side, meta);
    }

    @Override
    public int getRenderType() {
        return RENDER_ID;
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(BMIN, 0, BMIN, BMAX, BHEIGHT, BMAX);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setBlockBounds(BMIN, 0, BMIN, BMAX, BHEIGHT, BMAX);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityBotanyPot();
    }

    // Forge overrides — GTNH calls these instead of vanilla createNewTileEntity
    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityBotanyPot();
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (world.isRemote) return true;

        TileEntity raw = world.getTileEntity(x, y, z);
        if (!(raw instanceof TileEntityBotanyPot)) return false;
        TileEntityBotanyPot te = (TileEntityBotanyPot) raw;

        // Shift+right-click extracts; plain right-click inserts
        if (player.isSneaking()) {
            return te.onInteractEmpty(player);
        }
        ItemStack held = player.getHeldItem();
        if (held != null) {
            return te.onInteractWithItem(player, held);
        }
        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntityBotanyPot te = (TileEntityBotanyPot) world.getTileEntity(x, y, z);
        if (te != null) {
            for (int i = 0; i < te.getSizeInventory(); i++) {
                ItemStack stack = te.getStackInSlot(i);
                if (stack != null) {
                    EntityItem ei = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, stack.copy());
                    world.spawnEntityInWorld(ei);
                }
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }
}
